package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class UnCheckedTest {

    @Test
    @DisplayName("언체크 처리")
    void unCheckedCatchTest() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    @DisplayName("언체크 Throws")
    void unCheckedThrowsTest() {
        Service service = new Service();
        Assertions.assertThatThrownBy(() -> service.callThrowsSkip())
                .isInstanceOf(MyUnCheckedException.class);

        // 람다식 -> 메서드 참조 표현! ( 근데 그냥 일반적으로는 위의 ver 으로 )
        Assertions.assertThatThrownBy(service::callThrowsSkip)
                .isInstanceOf(MyUnCheckedException.class);
    }

    /**
     * RuntimeException 을 상속받은 예외들은 전부 언체크 예외
     */
    static class MyUnCheckedException extends RuntimeException {
        public MyUnCheckedException(String message) {
            super(message);
        }
    }

    static class Repository {
        public void call() {
            throw new MyUnCheckedException("언체크 Ex 발생");
        }
    }

    static class Service {
        Repository repository = new Repository();

        public void callCatch() {

            try {
                repository.call();
            } catch (MyUnCheckedException e) {
                log.info("message = {}", e.getMessage(), e);
            }
        }
        // throws 생략 ( 생략해도 throws 된다 ! 물론 명시해줘도 됨 )
        public void callThrowsSkip() {
            repository.call();
        }

        // throws 명시 ( 주요 예외의 경우는 유지보수를 위해 명시해 두자 )
        public void callThrows() throws MyUnCheckedException {
            repository.call();
        }
    }
}
