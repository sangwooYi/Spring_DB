package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

@Slf4j
public class CheckedAppTest {

    @Test
    void exController() {
        Controller controller = new Controller();

        Assertions.assertThatThrownBy(() -> controller.request())
                .isInstanceOfAny(ConnectException.class, SQLException.class);
    }

    static class Controller {

        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }



    }

    static class Service {

        NetworkClient networkClient = new NetworkClient();
        Repository repository = new Repository();

        // 체크 예외는 메서드 외부로 던지려면
        // 예외들이 전부 명시적으로 throws 되어야 함
        public void logic() throws SQLException, ConnectException {
            networkClient.call();
            repository.call();
        }

    }

    static class NetworkClient {
        public void call() throws ConnectException {
            throw new ConnectException("connect failed");
        }
    }

    static class Repository {
        // 체크 예외
        public void call() throws SQLException {
            throw new SQLException("Repository exception");
        }
    }
}
