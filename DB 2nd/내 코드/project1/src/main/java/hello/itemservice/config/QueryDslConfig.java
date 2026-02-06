package hello.itemservice.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.jpa.JpaItemRepositoryV2;
import hello.itemservice.repository.jpa.JpaItemRepositoryV3;
import hello.itemservice.repository.jpa.SpringDataJpaItemRepository;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV1;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
// 이거 안하면 에러나는건 버그인듯..? 어차피 scan 범위라서 굳이 안붙여도 되고 실제로 잘 동작 함
//@MapperScan("hello.itemservice.repository.mybatis") // Scan 위치 수동으로 지정해주는 방법
public class QueryDslConfig {

    private final EntityManager entityManager;

    // 빈 등록 및 의존성 주입
    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV3(entityManager);
    }
}
