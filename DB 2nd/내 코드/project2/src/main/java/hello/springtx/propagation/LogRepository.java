package hello.springtx.propagation;


import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LogRepository {

    private final EntityManager entityManager;

    @Transactional
    public void save(Log logMessage) {
        log.info("logMessage 저장");
        // 순수 JPA 는 저장이 persist
        entityManager.persist(logMessage);
    
        // 실제로는 상수 or Enum 사용해서 공통 변수를 써야하는게 상식, 그냥 예제니까 스트링으로 한 것
        if (logMessage.getMessage().contains("로그예외")) {
            log.info("로그 예외 발생!!");
            throw new MyLogException("예외 발생");
        }
    }

    // 로그 Repository 에는 REQUIRES_NEW 전파옵션 적용 ( 이를 이용 물리 트랜잭션을 분리 )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveTxRequiresNew(Log logMessage) {
        log.info("logMessage 저장");
        // 순수 JPA 는 저장이 persist
        entityManager.persist(logMessage);

        // 실제로는 상수 or Enum 사용해서 공통 변수를 써야하는게 상식, 그냥 예제니까 스트링으로 한 것
        if (logMessage.getMessage().contains("로그예외")) {
            log.info("로그 예외 발생!!");
            throw new MyLogException("예외 발생");
        }
    }

    public Optional<Log> findByMessage(String message) {
        return entityManager.createQuery("select l from Log l where l.message = :message", Log.class)
                .setParameter("message", message)
                .getResultList().stream().findAny();
    }

}
