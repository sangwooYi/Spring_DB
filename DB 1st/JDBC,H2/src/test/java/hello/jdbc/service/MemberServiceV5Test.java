package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV4_2;
import hello.jdbc.repository.MemberRepositoryV5;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 *  트랜잭션 - DataSource, transacationManager 자동 등록
 */
// @SpringBootTest 는 테스트 할때 SpringBootApplication 어노테이션과 동일한 역할을 해준다 .
// JUnit 테스트 실행시, 스프링 컨테이너를 생성해 줌
@SpringBootTest
@Slf4j
public class MemberServiceV5Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepository memberRepository;
    private MemberServiceV4 memberService;

    @Autowired
    public MemberServiceV5Test(MemberRepository memberRepository, MemberServiceV4 memberService) {
        this.memberRepository = memberRepository;
        this.memberService = memberService;
    }

    @AfterEach
    void after() {
        memberRepository.deleteById(MEMBER_A);
        memberRepository.deleteById(MEMBER_B);
        memberRepository.deleteById(MEMBER_EX);
    }

    // @Configuration  과 동일! 테스트용!
    // 수동 의존성 주입
    @TestConfiguration
    @RequiredArgsConstructor
    static class TestConfig {

        private final DataSource dataSource;
        @Bean
        MemberRepository memberRepositoryV4_1() {
            return new MemberRepositoryV5(dataSource);
        }
        @Bean
        MemberServiceV4 memberServiceV4() {
            return new MemberServiceV4(memberRepositoryV4_1());
        }
    }


    @Test
    @DisplayName("정상 이체")
    void accountTransfer()  {

        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberB);

        // when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        Assertions.assertThat(findMemberA.getMoney()).isEqualTo(8000);
        Assertions.assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 예외 발생")
    void accountTransferException()  {

        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when
        Assertions.assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);


        // then
        // 롤백 되어서, 기존 만원으로 유지 됨!
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberEx = memberRepository.findById(memberEx.getMemberId());
        Assertions.assertThat(findMemberA.getMoney()).isEqualTo(10000);
        Assertions.assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }

    @Test
    @DisplayName("AOP 프록시 체크")
    void aopCheck() {
        log.info("memberService Class = {}", memberService.getClass());
        log.info("memberRepository Class = {}", memberRepository.getClass());

        // AOP 프록시에 의해 호출된게 맞는지 체크
        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }
}
