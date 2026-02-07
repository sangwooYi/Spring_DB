package hello.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Call;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    public CallService callService;

    @Autowired
    public InternalCallV1Test(CallService callService) {
        this.callService  = callService;
    }

    @Test
    void test() {
        log.info("====== external 메서드 호출 ========");
        callService.external();

        log.info("====== internal 메서드 호출 ========");
        callService.internal();
    }


    @TestConfiguration
    // 주의 Config 용 클래스는 static 으로 생성되어있어야한다! ( 실행 단계에 호출되어야 하므로 )
    static class InternalCallV1TestConfig {

        @Bean
        CallService callService() {
            return new CallService();
        }

    }

    static class CallService {

        /*
            당연히 class 단위에 @Transactional 을 걸면 이런 문제는 신경 안써 됨.

            다만 이렇게 메서드에 설정하는 경우
            @Transactional 이 설정 안된 메서드에서, @Transactional 설정된 메서드를 내부호출하면
            Transaction 세팅이 안된다! ( 애초에 프록시 패턴이기 때문에, 생성되고나서 -> 메서드 호출이기 떄문 )
            -> 따라서 @Transactional 설정 안된 메서드에서 다른 메서드들이 호출 될 경우
            -> 이때 내부에 호출된 메서드 들은 this.메서드명 이며, 즉 호출 될 당시 인스턴스가 내부적으로 호출 함
            -> 따라서 이건 프록시 거친 호출이 아닌, 인스턴스가 내부호출 해 버린 것
            -> 이로 인해 메서드들에 트랜잭션 적용이 안 됨 ( 내부 호출 된 메서드가 @Transactional 이 설정되었을지라도 )
         */

        public void external() {
            log.info("call External");
            this.printTxInfo();

            // 메서드를 내부에서 호출
            // ( 메서드가 호출 될 때의 인스턴스가 본인 내부적으로 호출하는 것 )
            this.internal();
        }

        @Transactional
        public void internal() {
            log.info("call Internal");
            this.printTxInfo();
        }

        private void printTxInfo() {
            // 트랜잭션매니저 활성화 여부
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            // readOnly 여부
//            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

            log.info("txActive = {}", txActive);
//            log.info("readOnly = {}", readOnly);
        }

    }
}
