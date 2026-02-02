package hello.itemservice.repository.jpa;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
public class JpaItemRepository implements ItemRepository {

    private final EntityManager entityManager;

    public JpaItemRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Item save(Item item) {
        // 보존하겠다는 의미 따라서 save
        // PK 값도 자동으로 item 에 넣어준다!
        entityManager.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {

        Item findItem = this.findById(itemId).orElse(null);
        if (findItem != null) {
            // JPA Persist 에 의해 관리중이므로 별도로 저장할 필요 X
            // 내부적으로 Update 쿼리를 날려 줌!
            findItem.setItemName(updateParam.getItemName());
            findItem.setPrice(updateParam.getPrice());
            findItem.setQuantity(updateParam.getQuantity());
        }

    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = entityManager.find(Item.class, id);
        // ofNullable 은 혹시 전달된 인자가 null 일 경우 Optional 의 EMPTY 로 반환해 줌
        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        // 이럴떈 JPQL 을 쓴다!
        String jpql = "select i from Item i";

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        List<Object> param = new ArrayList<>();
        if (StringUtils.hasText(itemName) || maxPrice != null) {

            jpql += " where 1=1";

            if (StringUtils.hasText(itemName)) {
                jpql += " and i.itemName like concat('%', :itemName,'%')";
                param.add(itemName);
            }
            if (maxPrice != null) {
                jpql += " and i.price <= :maxPrice";
                param.add(maxPrice);
            }
        }
        log.info("jpql = {}", jpql);

        TypedQuery<Item> query = entityManager.createQuery(jpql, Item.class);

        if (StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }

        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        return query.getResultList();
    }
}
