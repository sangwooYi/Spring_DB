package hello.itemservice.config;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.jpa.JpaItemRepositoryV3;
import hello.itemservice.repository.v2.ItemQueryRepositoryV2;
import hello.itemservice.repository.v2.ItemRepositoryV2;
import hello.itemservice.service.ItemService;
import hello.itemservice.service.ItemServiceV2;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
// 이거 안하면 에러나는건 버그인듯..? 어차피 scan 범위라서 굳이 안붙여도 되고 실제로 잘 동작 함
//@MapperScan("hello.itemservice.repository.mybatis") // Scan 위치 수동으로 지정해주는 방법
public class V2Config {

    private final EntityManager entityManager;
    private final ItemRepositoryV2 itemRepositoryV2;    // spring Data Jpa 에서 자동으로 등록해 준다.

    // 빈 등록 및 의존성 주입
    @Bean
    public ItemService itemService() {
        return new ItemServiceV2(itemRepositoryV2, itemQueryRepositoryV2());
    }

    @Bean
    public ItemQueryRepositoryV2 itemQueryRepositoryV2() {
        return new ItemQueryRepositoryV2(entityManager);
    }

    // 테스트 데이터 초기화 용
    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV3(entityManager);
    }

}
