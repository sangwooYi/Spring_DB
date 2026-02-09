package hello.springtx.propagation;


import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager entityManager;

    @Transactional
    public void save(Member member) {
        log.info("Member 저장");
        // 순수 JPA 는 저장이 persist
        entityManager.persist(member);
    }

    public Optional<Member> findByUsername(String username) {
        // 여기에 들어가는 쿼리 JPQL 에서 from 다음에 오는건 "엔티티" 이다 ( 이 엔티티를 JPA 가 실제 테이블이랑 매핑 시켜줌 )
        return entityManager.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList().stream().findAny();    // findAny 는 해당 조건에서 가장 처음 찾은 하나만 반환해 줌
    }
}
