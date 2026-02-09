package hello.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.UnexpectedRollbackException;

// import static 자주 쓰는 외부 메서드 static 으로 선언하면 유용하다!
import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
class MemberServiceTest {
    // private final 로 선언해버리면 내가 수동으로라도 의존성 주입을 해주어야 함!
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /*
     *  별도 트랜잭션
     *  memberService 트랜잭션 off
     *  memberRepository 트랜잭션 On
     *  logRepository 트랜잭션 On
     */
    @Test
    void outerTxOff_success() {

        // given
        String username = "outerTxOff Test";
        // when
        memberService.joinV1(username);

        // then
        assertThat(memberRepository.findByUsername(username).isPresent()).isTrue();
        assertThat(logRepository.findByMessage(username).isPresent()).isTrue();
    }

    /*
     *  별도 트랜잭션
     *  memberService 트랜잭션 off  
     *  memberRepository 트랜잭션 On        커밋
     *  logRepository 트랜잭션 On Exception 롤백
     *  -> 따라서 이 상황은 데이터 원자성에 어긋난다!
     */
    @Test
    void outerTxOff_fail() {

        // given
        String username = "로그예외";
        // when
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(MyLogException.class);

        // then
        assertThat(memberRepository.findByUsername(username).isPresent()).isTrue();
        assertThat(logRepository.findByMessage(username).isEmpty()).isTrue();
    }

    /*
     *  서비스 단에만 Transaction 단일 적용
     *  memberService 트랜잭션 on            롤백
     *  memberRepository 트랜잭션 Off
     *  logRepository 트랜잭션 Off Exception
     *  -> 따라서 하나라도 문제가 생기면 서비스에서 롤백! ( 원자성 준수 )
     */
    @Test
    void singleTx_fail() {

        // given
        String username = "로그예외";
        // when
        assertThatThrownBy(() -> memberService.joinV3(username))
                .isInstanceOf(MyLogException.class);

        // then
        assertThat(memberRepository.findByUsername(username).isEmpty()).isTrue();
        assertThat(logRepository.findByMessage(username).isEmpty()).isTrue();
    }

    /*
     *  서비스 단, Repository 모두 트랜잭션 적용 + 전부 커밋되는 Case
     *  트랜잭션 전파 개념 적용 Case & 디폴트 옵션 REQUIRED
     *  ( 트랜잭션 있으면 참여, 없으면 트랜잭션 신규 생성,
     *  즉  이 옵션은 논리트랜잭션은 각각 존재하나 물리 트랜잭션은 하나로 묶인다 )
     *  memberService 트랜잭션 on
     *  memberRepository 트랜잭션 on
     *  logRepository 트랜잭션 on
     *  -> 따라서 하나라도 문제가 생기면 서비스에서 롤백! ( 원자성 준수 )
     */
    @Test
    void AllTx_success() {

        // given
        String username = "전체 트랜잭션 적용 Ver";
        // when
        memberService.joinV3(username);

        // then
        assertThat(memberRepository.findByUsername(username).isPresent()).isTrue();
        assertThat(logRepository.findByMessage(username).isPresent()).isTrue();
    }

    /*
     *  서비스, 리파지토리 전부 적용 Ver + REQUIRED 전파(propagation) 옵션 ( 디폴트 )
     *  memberService 트랜잭션 on
     *  memberRepository 트랜잭션 on
     *  logRepository 트랜잭션 on Exception
     *  -> 따라서 서비스 호출 될 때 하나라도 문제가 생기면 서비스 포함 전체 롤백! ( 논리적 트랜잭션은 각각 존재하나 물리적 트랜잭션은 하나 )
     *  당연히 리포지터리 save 만 별도로 호출하는 메서드는 각자 물리적 트랜잭션이 적용 ( 당연 )
     *
     *  ※ 이때는 서비스에서도 별도로 처리 안하고 예외를 던져버리기 때문에 AOP 프록시까지 예외가 전달되어 버린다.
     *  따라서 rollback_only 옵션유무와 상관 없이 AOP Proxy 까지 예외가 전달었으므로 그냥 무조건 롤백 됨!
     */
    @Test
    void AllTx_fail_required() {

        // given
        String username = "로그예외 전체 Tx 적용 Ver";
        // when
        assertThatThrownBy(() -> memberService.joinV3(username))
                .isInstanceOf(MyLogException.class);

        // then
        assertThat(memberRepository.findByUsername(username).isEmpty()).isTrue();
        assertThat(logRepository.findByMessage(username).isEmpty()).isTrue();
    }

    /*
     *  서비스, 리파지토리 전부 적용 Ver + REQUIRED 전파(propagation) 옵션만 사용 ( 디폴트 )
     *  memberService 트랜잭션 on / propagation.REQUIRED
     *  memberRepository 트랜잭션 on / propagation.REQUIRED
     *  logRepository 트랜잭션 on  / propagation.REQUIRED  / Exception 발생시 예외 throw
     *  
     *  초기 의도 : 서비스에서 예외 처리하여 정상흐름 반환하므로 member 정보만 커밋되기를 희망
     *  but 실제 : 물리적 트랜잭션이 현재 하나이므로 logRepository 에서 발생한 예외로 인해 rollback_only 옵션이 되어버리며
     *  AOP 프록시로 정상흐름으로 넘어가는 것이 아닌 UnexpectedRollbackException 예외가 전달됨! ( 이부분 중요! )
     */
    @Test
    void AllTx_recovery_required() {

        // given
        String username = "전파 옵션 REQUIRED Ver 으로 로그예외 복구 시도 Ver";
        // when
        assertThatThrownBy(() -> memberService.joinV4(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        // then
        assertThat(memberRepository.findByUsername(username).isEmpty()).isTrue();
        assertThat(logRepository.findByMessage(username).isEmpty()).isTrue();
    }

    /*
     *  서비스, 리파지토리 전부 적용 Ver + Log Repository 에는 REQUIRES_NEW 적용
     *  memberService 트랜잭션 on / propagation.REQUIRED
     *  memberRepository 트랜잭션 on / propagation.REQUIRED
     *  logRepository 트랜잭션 on  / propagation.REQUIRES_NEW  / Exception 발생시 예외 throw
     * 
     *  결과 : memberService/memberRepository 는 하나의 물리 트랜잭션 , logRepository는 별도의 또다른 물리트랜잭션을 가지게 됨
     *  따라서 logRepository 만 롤백, 나머지는 롤백 되며 AOP 프록시로는 정상 흐름 반환
     */
    @Test
    void AllTx_recovery_requiresNew() {

        // given
        String username = "전파 옵션 REQUIRES_NEW 활용으로 로그예외 복구 시도 Ver";
        // when
        memberService.joinV5(username);

        // then
        // 로그 Repository의 물리트랜잭션을 분리함으로써 Member는 저장이 된다.
        assertThat(memberRepository.findByUsername(username).isPresent()).isTrue();
        assertThat(logRepository.findByMessage(username).isEmpty()).isTrue();
    }
}