package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 *  우선순위
 *  -> 항상 구체적이고, 자세한게 우선순위가 높다
 *
 */
@Slf4j
@SpringBootTest
public class TxLevelTest {

    @Autowired LevelService levelService;

    @Test
    void testLevel() {
        levelService.write();
        log.info("=====================");
        levelService.read();
    }

    // 빈 의존성 주입
    @TestConfiguration
    static class testConfig {
        @Bean
        LevelService levelService() {
            return new LevelService();
        }
    }

    @Slf4j
    // springframework 의 Transactional 써야한다
    @Transactional(readOnly = true) // false 가 디폴트
    static class LevelService {

        @Transactional(readOnly = false)
        public void write() {
            log.info("call write");
            this.printTxInfo();
        }

        public void read() {
            log.info("call read");
            this.printTxInfo();
        }

        private void printTxInfo() {
            // 트랜잭션매니저 활성화 여부
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            // readOnly 여부
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

            log.info("txActive = {}", txActive);
            log.info("readOnly = {}", readOnly);
        }

    }

}
