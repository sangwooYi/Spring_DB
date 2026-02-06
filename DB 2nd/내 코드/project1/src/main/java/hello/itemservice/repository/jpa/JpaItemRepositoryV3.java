package hello.itemservice.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.domain.QItem;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
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
public class JpaItemRepositoryV3 implements ItemRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public QItem item = new QItem("i");

    public JpaItemRepositoryV3(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Item save(Item item) {

        entityManager.persist(item);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {

        Item findItem = entityManager.find(Item.class, itemId);
        // JPA Persist 에 의해 관리중이므로 별도로 저장할 필요 X
        // 내부적으로 Update 쿼리를 날려 줌!
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = entityManager.find(Item.class, id);

        return Optional.ofNullable(item);
    }

    // BooleanBuilder 이용
    @Override
    public List<Item> findAll(ItemSearchCond cond) {

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        BooleanBuilder builder = new BooleanBuilder();
        if (StringUtils.hasText(itemName)) {
            builder.and(item.itemName.like("%" + itemName + "%"));
        }
        if (maxPrice != null) {
            /*
                loe <=
                lt <
                goe >=
                gt >
             */
            builder.and(item.price.loe(maxPrice));
        }

        List<Item> result  = queryFactory.select(item)
                .from(item)
                .where(builder)
                .fetch();       // fetch 하면 해당 쿼리 결과를 List로 반환

        return result;
    }
    // 사실 이방법이 더 좋다! BooleanExpression 이용
    public List<Item> findAllNew(ItemSearchCond cond) {

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        // JpaQueryFactory 를 사용하고 여기에 Q타입 객체를 통해 아래처럼 작성하는게 사용법
        List<Item> result  = queryFactory.select(item)
                .from(item)
                // 이러면 자동으로 and 조건, or 조건으로 하려면 where(itemNameEx(itemName).or(maxPriceEx(maxPrice))); 이렇게 해줘야 함
                .where(itemNameEx(itemName), maxPriceEx(maxPrice))
                .fetch();       // fetch 하면 해당 쿼리 결과를 List로 반환

        return result;
    }

    // 이렇게 BooleanExpression 으로 모듈화 해두면 재사용 가능!
    private BooleanExpression itemNameEx(String itemName) {
        if (StringUtils.hasText(itemName)) {
            return item.itemName.like("%" + itemName + "%");
        }
        return null;
    }

    private BooleanExpression maxPriceEx(Integer maxPrice) {
        if (maxPrice != null) {
            return item.price.loe(maxPrice);
        }
        return null;
    }
}
