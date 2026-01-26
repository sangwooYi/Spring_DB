package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
public class CheckedTest {

    @Test
    @DisplayName("체크 예외 처리")
    void checkedCatch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    @DisplayName("체크 예외 Throws")
    void checkedUnCatch() {
        Service service = new Service();
        // assertThatThrownBy 그냥 이거 쓰자!
        Assertions.assertThatThrownBy(() -> service.callUnCatch())
                .isInstanceOf(MyCheckedException.class);

        // 이것도 있긴 한데 그냥 assertThatThrownBy 쓴다! 어차피 똑같음
//        Assertions.assertThatException().isThrownBy(() -> service.callUnCatch())
//                .isInstanceOf(MyCheckedException.class);
    }

    /**
     * Exception 을 상속받은 예외는 체크 ( RuntimeException 제외 )
     */
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            super(message);
        }
    }

    static class Service {
        Repository repository = new Repository();

        /*
         * 예외를 잡아서 처리
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyCheckedException e) {
                log.info("MyCheckedException! message = {} ", e.getMessage(), e);
            }
        }
        
        /*
            예외 던지기
         */
        public void callUnCatch() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository {
        // throw 로 던질거면 해당 메서드에 반드시 throws 로 설정 해줘야 한다.
        // 아니면 해당 메서드 내부적으로 try - catch 로 처리해야함
        public void call() throws MyCheckedException {
            throw new MyCheckedException("ex 발생!");
        }
    }
}
