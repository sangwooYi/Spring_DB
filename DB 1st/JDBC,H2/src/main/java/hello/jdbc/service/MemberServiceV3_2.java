package hello.jdbc.service;


import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;

/**
 *   트랜잭션 탬플릿 사용
 */
@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    // 생성자 주입받을때 parameter 로 transactionManager 전달받는 형태
    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {

        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;

    }

    // 트랜잭션 매니저가 Release 작업을 알아서 해준다!
    // 참고 클래스명<T> 이런건 제네릭 클래스! ( T 타입을 받아 수행되는 클래스 )
    // <T> T 메서드명 이런건 제네릭 메서드! ( 여기선 T 타입을 받아 T 를 반환하는 것  당연히 얼마든지 다른 타입 설정 가능
    // ex <K> T 메서드명 이런식 ! )
    // 트랜잭션 관리 , 비즈니스 로직을 분리 ( 이렇게 레이어가 다르면 메서드 분리하는 습관 들이자! )
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        // execute 는 TransactionCallback 에서 콜백으로부터 특정 오브젝트를 받거나 접근 할 필요가 있을 때 사용
        // executeWithoutResult 는 메서드명 그대로 반환 타입 void
        // 내부적으로 그냥 성공하면 commit, 실패하면 rollback 을 처리해줌!
        txTemplate.executeWithoutResult((status) -> {
            try {
                this.bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    // 레이어가 다른 경우는 이렇게 분리하는게 유지보수에 유리하다!
    private void bizLogic(String fromId, String toId, int money) throws SQLException{
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.updateById(fromId, fromMember.getMoney()-money);
        this.validation(toMember);
        memberRepository.updateById(toId, toMember.getMoney()+money);
    }

    private void validation(Member member) {
        if (member.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
