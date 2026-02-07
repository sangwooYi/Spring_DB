package hello.springtx.exception;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
public class RollbackTest {

    private final RollbackService rollbackService;

    @Autowired
    public RollbackTest(RollbackService rollbackService) {
        this.rollbackService = rollbackService;
    }

    // Initiating transaction rollback
    @Test
    void runtimeException() {
        Assertions.assertThatThrownBy(() -> rollbackService.runtimeException())
                .isInstanceOf(RuntimeException.class);
    }

    // Initiating transaction commit
    @Test
    void checkedException() {
        Assertions.assertThatThrownBy(() -> rollbackService.checkedExceptionNorm())
                .isInstanceOf(MyException.class);
    }

    // Initiating transaction rollback
    @Test
    void checkedExceptionRollback() {
        Assertions.assertThatThrownBy(() -> rollbackService.checkedExceptionRollback())
                .isInstanceOf(MyException.class);
    }

    @TestConfiguration
    static class RollbackTestConfig {

        @Bean
        RollbackService rollbackService() {
            return new RollbackService();
        }

    }

    @Slf4j
    static class RollbackService {
        
        // 런타임 예외 : 롤백
        @Transactional
        public void runtimeException() {
            log.info("runtime Exception !");
            throw new RuntimeException();
        }

        // 체크 예외 : 커밋
        @Transactional
        public void checkedExceptionNorm() throws MyException {
            log.info("checked Exception ! just normal");
            throw new MyException();
        }

        // 체크 예외 rollbackFor 지정으로 롤백
        // 그냥 Exception.class 로해버리면 checkedExceptionNorm 도 영향을 받아버리므로
        // 별도로 MyException 을 Exception 상속받아 만든 것!
        @Transactional(rollbackFor = MyException.class)
        public void checkedExceptionRollback() throws MyException {
            log.info("checked Exception ! Roll back!!");
            throw new MyException();
        }
    }

    static class MyException extends Exception {
    }
}
