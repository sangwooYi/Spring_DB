package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    public InternalCallService internalCallService;
    public ExternalCallService externalCallService;

    @Autowired
    public InternalCallV2Test(InternalCallService internalCallService, ExternalCallService externalCallService) {
        this.internalCallService = internalCallService;
        this.externalCallService = externalCallService;
    }

    @Test
    void test() {
        log.info("====== external 메서드 호출 ========");
        externalCallService.external();

        log.info("====== internal 메서드 호출 ========");
        internalCallService.internal();
    }



    @TestConfiguration
    // 주의 Config 용 클래스는 static 으로 생성되어있어야한다! ( 실행 단계에 호출되어야 하므로 )
    static class InternalCallV1TestConfig {

        // 의존성 수동 주입도 할 줄 알아야 한다.

        @Bean
        InternalCallService internalCallService() {
            return new InternalCallService();
        }

        @Bean
        ExternalCallService externalCallService() {
            return new ExternalCallService(internalCallService());
        }

    }
    
    static class ExternalCallService {

        // 주입 받아야 한다.
        private final InternalCallService internalCallService;

        public ExternalCallService(InternalCallService internalCallService) {
            this.internalCallService = internalCallService;
        }

        public void external() {
            log.info("call External");
            this.printTxInfo();

            // 별도로 클래스로 분리해버림 ( 이러면 내부 호출이 아님! )
            internalCallService.internal();
        }

        private void printTxInfo() {
            // 트랜잭션매니저 활성화 여부
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();

            log.info("txActive = {}", txActive);
        }
    }

    @Transactional
    static class InternalCallService {

        public void internal() {
            log.info("call Internal");
            this.printTxInfo();
        }

        private void printTxInfo() {
            // 트랜잭션매니저 활성화 여부
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();

            log.info("txActive = {}", txActive);
        }
    }
    
}
