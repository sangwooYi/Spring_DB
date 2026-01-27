package hello.jdbc.exception.translator;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import hello.jdbc.repository.ex.MyDuplicatedKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class TranslatorV1Test {

    Repository repository;
    Service service;

    @BeforeEach
    void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        // 의존성 주입
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    void duplicateKeyTest() {
        service.create("aabbcc");
        service.create("aabbcc");
    }

    @RequiredArgsConstructor
    static class Service {
        private final Repository repository;

        public void create(String memberId) {

            try {
                repository.save(new Member(memberId, 1000));
                log.info("saveId = {}", memberId);
            } catch (MyDuplicatedKeyException e) {
                log.info("키 중복 발생 복구 시도", e);

                // 필요시 이렇게 서버 내부적으로 예외 처리도 가능함!
                String newId = generateNewId(memberId);
                log.info("newId = {}", newId);
                repository.save(new Member(newId, 1000));
            } catch (MyDbException e) { // catch 여러개 적용할때는 반드시 하위부터 -> 상위 순으로 
                log.info("DB 예외 발생", e);    // 상위 예외는 하위 예외를 전부 포함해서 받아버리므로
            }
        }

        private String generateNewId(String memberId) {
                                // nextInt(10000) 이면 0 ~ 9999 까지 랜덤
            return memberId + (new Random().nextInt(10000)+1);
        }
    }


    @RequiredArgsConstructor
    static class Repository {

        private final DataSource dataSource;

        public Member save(Member member) {
            String sql = "insert into member(member_id, money) values (?, ?)";
            Connection connection = null;
            PreparedStatement preparedStatement = null;

            try {
                connection = dataSource.getConnection();
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, member.getMemberId());
                preparedStatement.setInt(2, member.getMoney());
                preparedStatement.executeUpdate();
                return member;
            } catch (SQLException e) {
                // 예외 전환  ( H2 기준 key 중복 예외 )
                if (e.getErrorCode() == 23505) {
                    throw new MyDuplicatedKeyException(e);
                }
                throw new MyDbException();
            } finally {
                JdbcUtils.closeStatement(preparedStatement);
                JdbcUtils.closeConnection(connection);
            }

        }
    }

}
