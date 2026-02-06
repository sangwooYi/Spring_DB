package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class TxBasicTest {

    @Autowired BasicService basicService;

    @Test
    @DisplayName("트랜잭션 Test")
    void txTest() {
        basicService.callTx();
        basicService.callNonTx();
    }

    @Test
    @DisplayName("프록시 체크")
    void proxyCheck() {
        // 프록시가 스프링 빈으로 등록 됨! ( BasicService 를 상속받아 프록시가 구현 됨 )
        log.info("AOP class = {}", basicService.getClass());

        // AopUtils.isAopProxy(basicService) 호출하면 AOP 프록시 여부를 boolean 으로 뱉어준다.
        Assertions.assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }

    // 이럴 땐 빈 등록 Config 로 해줘야 함!
    // Test 에서는 @TestConfiguration.  (실제 코드에선 @Configuration)
    @TestConfiguration
    static class TxApplyBasicConfig {
        @Bean
        BasicService basicService() {
            return new BasicService();
        }
    }

    @Slf4j
    static class BasicService {

        @Transactional
        public void callTx() {
            log.info("Call Tx");
            boolean isTxActive = this.isTaxActive();
            log.info("Tx is Active? = {}", isTxActive);
        }

        public void callNonTx() {
            log.info("Call Tx");
            boolean isTxActive = this.isTaxActive();
            log.info("Tx is Active? = {}", isTxActive);
        }

        private boolean isTaxActive() {
            return TransactionSynchronizationManager.isActualTransactionActive();
        }
    }
}
