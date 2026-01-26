package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

/**
 *  트랜잭션 적용
 *  애초에 @Transactional 이 작동하려면 스프링 컨테이너에 관리되는 컴퍼넌트여야 한다!
 *  스프링 컨테이너에 관리 대상으로 등록하는 방법
 *  1. @Configuration 설정된 클래스에 의존성을 @Bean 을 통해 스프링 컨테이너가 관리하는 빈에 등록
 *  2. @ComponantScan 이 설정된 클래스 하위 레벨에 @Componant 클래스 등록 ( 스프링이 알아서 의존성 관리해 줌 )
 *
 *  || 스프링 빈에 등록 / 의존성 주입 은 구분! ||
 *  -> 참고로 의존성 주입 받는 방법은 클래스 or 생성자에 @Autowired 설정 혹은
 *   @RequiredArgsConstructor 설정 후 주입받을 필드들 private final 로 필드 선언
 *
 */
// @SpringBootTest 는 테스트 할때 SpringBootApplication 어노테이션과 동일한 역할을 해준다 .
// JUnit 테스트 실행시, 스프링 컨테이너를 생성해 줌
@SpringBootTest
@Slf4j
public class MemberServiceV3_3Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV3 memberRepository;
    private MemberServiceV3_3 memberService;

    @Autowired
    public MemberServiceV3_3Test(MemberRepositoryV3 memberRepository, MemberServiceV3_3 memberService) {
        this.memberRepository = memberRepository;
        this.memberService = memberService;
    }

    @AfterEach
    void after() throws SQLException{
        memberRepository.deleteById(MEMBER_A);
        memberRepository.deleteById(MEMBER_B);
        memberRepository.deleteById(MEMBER_EX);
    }

    // @Configuration  과 동일! 테스트용!
    @TestConfiguration
    static class TestConfig {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource());
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }
    }


    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {

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
    void accountTransferException() throws SQLException {

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
