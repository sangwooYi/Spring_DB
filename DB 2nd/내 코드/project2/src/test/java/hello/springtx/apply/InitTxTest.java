package hello.springtx.apply;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 *  초기화 시점에 트랜잭션 주의할 때 주의 사항
 *
 * @PostConstruct vs @EventListener(ApplicationReadyEvent.class)
 *
 */
@Slf4j
@SpringBootTest
public class InitTxTest {
    
    // 이게 @RequiredArgsConstructor 설정과 완전 동일
    private final Hello hello;

    @Autowired
    public InitTxTest(Hello hello) {
        this.hello = hello;
    }

    @Test
    void test() {
    }


    @TestConfiguration
    static class InitTxTestConfig {
        @Bean
        Hello hello() {
            return new Hello();
        }
    }

    static class Hello {

        // 호출 시점으로 인한 차이 주의!

        // @PostConstruct 붙은 메서드는
        // 해당 클래스의 생성자가 생성 된 이후 자동 호출
        @PostConstruct
        @Transactional
        // 이렇게 트랜잭션명을 설정해 줄 수도 있다
        // @Transactional(value = "initTxManager")
        // 기본적으로 Error, RuntimeException (언체크 예외) 은 롤백, Exception (체크 예외) 은 그냥 커밋 한다
        // 이때 체크예외도 그냥 롤백 시키고 싶으면 다음 옵션을 넣어주면 됨 ( Exception 클래스 및 하위 클래스들 발생시 rollback 하겠다는 의미)
        // @Transactional(rollbackFor = Exception.class)  ( 이거랑 반대인 noRollbackFor 란 옵션도 있다. 이건 말그대로 정반다 )
        public void initV1() {
            log.info("======= call initV1 =======");
            this.printTx();
        }

        // 이건 스프링 컨테이너가 다 세팅 된 이후에 설정 된 메서드를 자동 호출
        // 로그 시점을 보면 스프링 실행이 완료 된 시점 이후에 호출 ( Started {메서드명} in xxx seconds (process running for xxx) 이 메서드가 스프링 실행 완료 로그 )
        @EventListener(ApplicationReadyEvent.class)
        @Transactional
        public void initV2() {
            log.info("======= call initV2 =======");
            this.printTx();
        }

        private void printTx() {
            boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("isActive = {}", isActive);
        }

    }
}
