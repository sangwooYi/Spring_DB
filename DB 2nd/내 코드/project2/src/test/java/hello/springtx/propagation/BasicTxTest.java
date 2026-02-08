package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class BasicTxTestConfig {
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("커밋 시작");
        txManager.commit(status);

        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");

        // 트랜잭션의 상태를 확인 가능 ( 새 트랜잭션인지, readOnly 인지, 완료된 트랜잭션인지 등등 )
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("status class = {}", status.getClass());

        log.info("롤백 시작");
        txManager.rollback(status);

        log.info("트랜잭션 롤백 완료");
    }

    @Test
    void doubleCommit() {

        /*
            특이사항은
            같은 커넥션을 사용한다!
            하지만 히카리 Proxy 객체의 주소가 다름.
            즉 같은 커넥션을 쓰나, 트랜잭션마다, 새로운 프록시 객체가 생성
            커밋or롤백 후 이때 객체는 파괴 됨
            따라서 아래와 같은 흐름으로 진행 됨.
            커넥션 풀에서 커넥션 get -> 이 커넥션을 이용 프록시 객체 생성
            -> 커밋 or 롤백 -> 프록시 객체 해제 -> 커넥션 풀로 release
         */

        log.info("TX-1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("TX-1 커밋");
        txManager.commit(tx1);

        log.info("TX-2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("TX-2 커밋");
        txManager.commit(tx2);
    }


    @Test
    void commitAndRollback() {

        log.info("TX-1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("TX-1 커밋");
        txManager.commit(tx1);

        log.info("TX-2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("TX-2 롤백");
        txManager.rollback(tx2);
    }

    // PROPAGATION_REQUIRED ( 디폴트 상태 )
    // 둘다 커밋
    @Test
    void outerInnerAllCommit() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        // 물리 트랜잭션 신규 여부
        log.info("isNewTransaction = {}", outer.isNewTransaction());    // 물리 트랜잭션 따라서 여긴 true
        log.info("내부 트랜잭션 시작");
        // Participating in existing transaction 로그 찍힘 ( 외부 트랜잭션에 참여하는 형태임, 같은 물리 트랜잭션, 이 때 외부가 커밋,롤백 관리)
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("isNewTransaction = {}", inner.isNewTransaction());    // 물리 트랜잭션이므로 false
        log.info("isNested = {}", inner.isNested()); // PROPAGATION_REQUIRED (참여) 디폴트 옵션이므로 여기 false
        // ( 물리적 트랜잭션은 여전히 같으나 이건 참여가 아닌, 논리적 트랜잭션에 따라 save point 를 구분하겠다는 옵션 )

        // isNested 는 PROPAGATION_NESTED 옵션일 때만 유효한 값이다!, 확인 방법은 아래 옵션으로 TransactionStatus 생성하면 됨
        // DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
        // attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);

        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);    // 사실 이 때는 커밋/롤백 관련 별다른 작업을 안한다.
                                    // ( 물리적으로 같은 트랜잭션이므로 내부 트랜잭션이 외부 트랜잭션에 참여한 형태 ) 
        
        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);    // 이때 실제 커밋이 발생
    }
    // 외부만 rollback
    @Test
    void outerRollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        // 물리 트랜잭션 신규 여부
        log.info("isNewTransaction = {}", outer.isNewTransaction());    // 물리 트랜잭션 따라서 여긴 true

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("isNewTransaction = {}", inner.isNewTransaction());    // 물리 트랜잭션이므로 false
        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);

        log.info("외부 트랜잭션 롤백");
        txManager.rollback(outer);
    }

    // inner 만 rollback
    @Test
    void innerRollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        // 물리 트랜잭션 신규 여부
        log.info("isNewTransaction = {}", outer.isNewTransaction());    // 물리 트랜잭션 따라서 여긴 true

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("isNewTransaction = {}", inner.isNewTransaction());    // 물리 트랜잭션이므로 false
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner);  // 참여한 물리 트랜잭션을 rollback-only 옵션으로 변경함 ( 실제 커밋/롤백은 X )

        log.info("외부 트랜잭션 커밋");
        log.info("isRollbackOnly = {}", outer.isRollbackOnly());
        Assertions.assertThatThrownBy(() -> txManager.commit(outer))
                // 현재 rollback-only 상태이므로 이때 UnexpectedRollbackException 예외 발생
                // 참고. 스프링 예외이므로 당연히 언체크 예외 ( RuntimeException )
                .isInstanceOf(UnexpectedRollbackException.class);
    }

    // PROPAGATION_REQUIRES_NEW 옵션 사용  ( wrapping 이라고 표현 됨 )
    // 새로운 물리 트랜잭션을 별도로 생성하는 옵션
    @Test
    void innerRollbackRequiresNew() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        // 물리 트랜잭션 신규 여부
        log.info("isNewTransaction = {}", outer.isNewTransaction());    // 물리 트랜잭션 따라서 여긴 true

        log.info("내부 트랜잭션 시작");
        // attribution 에 옵션을 넣어주려면 아래처럼 해주어야 함.
        DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
        attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);   // 물리적으로 새로운 트랜잭션 생성 옵션
        TransactionStatus inner = txManager.getTransaction(attribute);
        log.info("isNewTransaction = {}", inner.isNewTransaction());    // true  ( PROPAGATION_REQUIRES_NEW 옵션으로 인해 )
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner);      // 물리적으로 트랜잭션이 구분되어 여기서 실제로 롤백 발생

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);    // 별도의 물리 트랜잭션이므로 커밋 가능
    }

    @Test
    void innerRollbackRequiresNewV2() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        // 물리 트랜잭션 신규 여부
        log.info("isNewTransaction = {}", outer.isNewTransaction());    // 물리 트랜잭션 따라서 여긴 true

        log.info("내부 트랜잭션 시작");
        // attribution 에 옵션을 넣어주려면 아래처럼 해주어야 함.
        DefaultTransactionAttribute attribute = new DefaultTransactionAttribute();
        attribute.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);   // 물리적으로 새로운 트랜잭션 생성 옵션
        TransactionStatus inner = txManager.getTransaction(attribute);
        log.info("isNewTransaction = {}", inner.isNewTransaction());    // true  ( PROPAGATION_REQUIRES_NEW 옵션으로 인해 )
        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);      // 물리적으로 트랜잭션이 구분되어 여기서 실제로 커밋 발생

        log.info("외부 트랜잭션 롤백");
        txManager.rollback(outer);    // 외부만 롤백되고, 내부는 이미 커밋 된 상태
    }
}
