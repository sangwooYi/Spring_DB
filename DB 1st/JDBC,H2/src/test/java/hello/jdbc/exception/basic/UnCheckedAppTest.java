package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

@Slf4j
public class UnCheckedAppTest {

    @Test
    void exController() {
        Controller controller = new Controller();

        Assertions.assertThatThrownBy(() -> controller.request())
                .isInstanceOfAny(RuntimeConnectException.class, RuntimeSQLException.class);
    }

    static class Controller {

        Service service = new Service();

        public void request() {
            service.logic();
        }
    }

    static class Service {

        NetworkClient networkClient = new NetworkClient();
        Repository repository = new Repository();

        // 체크 예외는 메서드 외부로 던지려면
        // 예외들이 전부 명시적으로 throws 되어야 함
        public void logic() {
            networkClient.call();
            repository.call();
        }

    }

    static class NetworkClient {
        public void call()  {
            throw new RuntimeConnectException("connect failed");
        }
    }

    // 의존성 있는 체크예외 처리 방법!
    // 처리하는 쪽에서 try - catch 로 감싸서 내가 정의한 Runtime 예외로 throw 해버리자.
    // 런타임예외 == 언체크 예외
    static class Repository {
        // RuntimeException으로 감싸주는것
        public void call() {
            try {
                this.runSQL();
            // 예외 전환  ( 의존성을 갖는 체크 예외를 언체크 예외로 바꿔주는 작업, 목적은 의존성 최소화 )
            // 주의할 점은 반드시 아래처럼 기존 에러를 그대로 전달해 줘야한다! ( 원래 에러의 스택트레이스가 필요! )
            } catch (SQLException e) {
                throw new RuntimeSQLException(e); // 이렇게 에러 전달해주는 부분 안 까먹도록 주의!
            }
        }

        public void runSQL() throws SQLException {
            throw new SQLException("SQL Ex 발생");
        }
    }

    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }
    static class RuntimeSQLException extends RuntimeException {
            /*
                상속 관계는 아래와 같다 꼭 기억!
                            Throwable
                  Exception             Error
             체크예외   언체크예외
             */
            public RuntimeSQLException(Throwable cause) {
            super(cause);   // 메시지 대신에 에러 자체를 전달해주면 상속받은 예외에서도 
                            // 원래 에러의 스택트레이스 함께 출력 가능
        }
    }
}
