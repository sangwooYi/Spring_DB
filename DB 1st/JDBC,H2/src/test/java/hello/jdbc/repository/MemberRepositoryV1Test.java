package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class MemberRepositoryV1Test {

    MemberRepositoryV1 repositoryV1;

    // 각 테스트가 실행되기전에 직전에 먼저 호출되는 부분
    @BeforeEach
    void beforeEach() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repositoryV1 = new MemberRepositoryV1(dataSource);
    }

    int ord = 1;

    @Test
    void testV1() {

        String memberId = "member";
        String chckMemberId = "";
        while(true) {
            chckMemberId = memberId + ord;

            boolean isExist = false;
            try {
                isExist = repositoryV1.isExistById(chckMemberId);
            } catch (SQLException e) {
                log.error("SQL Exception", e);
            }
            if (isExist) {
                ord++;
            } else {
                memberId = chckMemberId;
                break;
            }
        }

        Member member = new Member(memberId, 120000);

        try {
            repositoryV1.save(member);

            Member findMember = repositoryV1.findById(member.getMemberId());
            log.info("find Member = {}", findMember);
            
            // 일반적으로 객체비교는 isEqualTo 사용할 것
            Assertions.assertThat(findMember).isEqualTo(member);    // 이건 같은 값인지 여부 체크
            
            // 따라서 isSameAs 는 실제 인스턴스 체크 or primitive 데이터에 대해서만 사용할 것 
//            Assertions.assertThat(findMember).isSameAs(member);    // 이건 같은 인스턴스 여부를  체크 (주소값 체크)

        } catch (SQLException e) {
            log.error("SQL Exception", e);
        }
    }

    @Test
    void updateTest() {

        try {
            repositoryV1.updateById("member1", 200000);
        } catch (SQLException e) {
            log.error("SQL Exception", e);
        }

    }

    @Test
    void deleteTest() {
        try {
            repositoryV1.deleteById("member1");

            Member member = repositoryV1.findById("member1");
            Assertions.assertThat(member).isNull();

            // 만약 강의 처럼 Exception 던지는 형태면 아래처럼 쓰면 됨. ( isInstanceOf 안에 예상되는 예외 클래스 넣어주면 됨 0
            //Assertions.assertThatThrownBy(() -> repositoryV1.findById("member1")).isInstanceOf(NoSuchElementException.class);

        } catch (SQLException e) {
            log.error("SQL Exception", e);
        }
    }

}
