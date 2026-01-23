package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    @Test
    void driverManagerTest() throws SQLException {
        Connection connection1 = DriverManager.getConnection(URL, USERNAME, PASSWORD );
        Connection connection2 = DriverManager.getConnection(URL, USERNAME, PASSWORD );

        log.info("connection1 = {}  / class = {}", connection1, connection1.getClass());
        log.info("connection2 = {} / class = {}", connection2, connection2.getClass());

        Assertions.assertThat(connection1).isNotSameAs(connection2);
    }

    @Test
    void datasourceDriverManagerTest() throws SQLException {

        // DriverManager 를 사용 따라서 그냥 항상 새로운 커넥션 획득!
        DriverManagerDataSource datasource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // DriverManagerDataSource 의 경우 DriverManager와 동일 따라서
        // .getConnection() 할때마다 새로운 커넥션을 함.
        this.userDataSource(datasource);
    }

    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        // HikariDataSource 사용하여 커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        
        // 데이터풀 디폴트값이 10개
        dataSource.setMaximumPoolSize(10);  // 커넥션 풀 최대 10개로 제한 Max Pool 이상으로 커넥션이 이루어지면 설정시간동안 대기한다.
                                            // connectionTimeout 속성값! (밀리초 단위로 설정되어있다, 디폴트 30초 (30,000 밀리초) )
        
        dataSource.setPoolName("MyPool"); // 커넥션 풀 이름 설정

        userDataSource(dataSource);
        // 풀링 (커넥션풀에 최대 풀 생성하는 작업)은 해당 테스트와 별도의 스레드에서 작동,
        // 따라서 .sleep 안주면 로그확인이 제대로 안된다.
        Thread.sleep(1000); // 단위는 밀리초 설정된 시간동안 스레드 대기시간을 주는 것
    }

    private void userDataSource(DataSource dataSource) throws SQLException {

        Connection connection1 = dataSource.getConnection();
        Connection connection2 = dataSource.getConnection();
        log.info("connection1 = {}  / class = {}", connection1, connection1.getClass());
        log.info("connection2 = {} / class = {}", connection2, connection2.getClass());
    }
}
