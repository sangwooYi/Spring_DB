package hello.jdbc.service;


import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *   트랜잭션 적용.
 *   서비스 메서드 기준으로 connection 유지함으로써 트랜잭션 관리
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    // 트랜잭션 관리 , 비즈니스 로직을 분리 ( 이렇게 레이어가 다르면 메서드 분리하는 습관 들이자! )
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        Connection connection = dataSource.getConnection();
        try {
            // 이 명령어가 트랜잭션을 사용하겠다는 의미임
            // (이러면 commit / rollback 하기 전까지 트랜잭션 유지
            connection.setAutoCommit(false);

            this.bizLogic(fromId, toId, money, connection);

            // 성공시 commit
            connection.commit();
        } catch (Exception e) {
            // 예외 발생시 rollback
            connection.rollback();
            throw new IllegalStateException(e);
        } finally {
            this.release(connection);
        }
    }

    // 레이어가 다른 경우는 이렇게 분리하는게 유지보수에 유리하다!
    private void bizLogic(String fromId, String toId, int money, Connection connection) throws SQLException{
        Member fromMember = memberRepository.findById(fromId, connection);
        Member toMember = memberRepository.findById(toId, connection);

        memberRepository.updateById(fromId, fromMember.getMoney()-money, connection);
        this.validation(toMember);
        memberRepository.updateById(toId, toMember.getMoney()+money, connection);
    }

    private void release(Connection connection) {
        if (connection != null) {
            try {
                // 풀에 반환하기 전에 true 상태로 꼭 변경한 후에 돌려줄 것!
                // 디폴트가 true 이므로 디폴트 상태로 돌려줘야 나중에 문제가 없다.
                connection.setAutoCommit(true);
                connection.close();
            } catch (Exception e) {
                log.error("error ", e);
            }
        }
    }

    private void validation(Member member) {
        if (member.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
