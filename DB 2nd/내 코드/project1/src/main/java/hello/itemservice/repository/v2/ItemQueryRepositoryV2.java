package hello.itemservice.repository.v2;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hello.itemservice.domain.Item;
import hello.itemservice.domain.QItem;
import hello.itemservice.repository.ItemSearchCond;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

// 복잡한 쿼리문 조회 queryDsl 사용
@Repository
@RequiredArgsConstructor
public class ItemQueryRepositoryV2 {

    private final JPAQueryFactory query;
    private QItem item = new QItem("i");

    public ItemQueryRepositoryV2(EntityManager em) {
        // JpaQueryFactory 는 초기화시 EntityManager 가 필수!
        this.query = new JPAQueryFactory(em);
    }

    public List<Item> findAll(ItemSearchCond cond) {
        return query.select(item)   // 가져올 필드에 대한 정보
                .from(item)         // 참조할 Q타입객체의 정보
                .where(itemNameEx(cond.getItemName()), maxPriceEx(cond.getMaxPrice()))
                .fetch();
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
