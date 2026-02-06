package hello.itemservice.repository.v2;

import hello.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// 어차피 Spring Data JPA 가 해당 인터페이스 구현하면서 빈으로 자동으로 등록해 줌
// 따라서 @Repository 굳이 설정할 필요 전혀 X
// 간단한 기본 CRUD 용
public interface ItemRepositoryV2 extends JpaRepository<Item, Long> {
    
}
