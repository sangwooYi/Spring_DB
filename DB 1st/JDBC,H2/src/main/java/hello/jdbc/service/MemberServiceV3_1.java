package hello.jdbc.service;


import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;

/**
 *   트랜잭션 매니저 사용
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    // DataSource 대신 이거 사용! ( DataSource 는 JDBC 기술이라 의존성!)
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    // 트랜잭션 매니저가 Release 작업을 알아서 해준다!
    // 트랜잭션 관리 , 비즈니스 로직을 분리 ( 이렇게 레이어가 다르면 메서드 분리하는 습관 들이자! )
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            this.bizLogic(fromId, toId, money);
            // 성공시 commit
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            // 예외 발생시 rollback
            transactionManager.rollback(transactionStatus);
            throw new IllegalStateException(e);
        }
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
