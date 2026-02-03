package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository // @Repository 을 통해 AOP 예외 적용 대상이 되어 JPA 예외변환이 이루어짐!
@Transactional  // 트랜잭션 관리용
@RequiredArgsConstructor
public class JpaItemRepositoryV2 implements ItemRepository {

    private final SpringDataJpaItemRepository repository;

    @Override
    public Item save(Item item) {
        repository.save(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {

        Item findItem = this.findById(itemId).orElseThrow(() -> new EntityNotFoundException("엔티티가 없습니다."));
        // JPA Persist 에 의해 관리중이므로 별도로 저장할 필요 X
        // 내부적으로 Update 쿼리를 날려 줌!
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        List<Item> itemList = new ArrayList<>();

        // 이런 동적 쿼리 해결은 QueryDsl 로 !
        // 둘다 존재하는 조건부터 내려가는게 if else if 논리가 더 깔끔함!
        if (StringUtils.hasText(itemName) && maxPrice != null) {
            itemList = repository.findItems(itemName, maxPrice);
        } else if (StringUtils.hasText(itemName)){
            itemList = repository.findByItemNameContaining(itemName);
        } else if (maxPrice != null) {
            itemList = repository.findByPriceLessThanEqual(maxPrice);
        } else {
            itemList = repository.findAll();
        }
        log.info("itemList = {}", itemList);
        return itemList;
    }
}
