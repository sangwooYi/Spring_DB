package hello.itemservice.domain;

import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import hello.itemservice.repository.memory.MemoryItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Transactional  // @Transactional 은 테스트 코드에서는 무조건 롤백 후 트랜잭션을 종료 해 준다.
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    TransactionStatus status;

//    @BeforeEach
//    void beforeEach() {
//        // 트랜잭션 시작
//        this.status = transactionManager.getTransaction(new DefaultTransactionDefinition());
//    }

    @AfterEach
    void afterEach() {

        // MemoryItemRepository 의 경우 제한적으로 사용 이런 경우 instanceof 로 꼭 체크 해 줄 것
        if (itemRepository instanceof MemoryItemRepository) {
            ((MemoryItemRepository) itemRepository).clearStore();
        }
        // 트랜잭션 roll back
        //transactionManager.rollback(status);
    }

    @Test
    void save() {
        //given
        Item item = new Item("itemA", 10000, 10);

        //when
        Item savedItem = itemRepository.save(item);

        //then
        Item findItem = itemRepository.findById(item.getId()).get();
        assertThat(findItem).isEqualTo(savedItem);
    }

    // 만약 테스트에서도 commit 하고 싶으면 이 어노테이션 추가 ( @Transactional 처럼 당연히 클래스 단위로도 붙일 수 있음 )
//    @Commit
    // 참고! JPA update 구문 보려면 커밋 되야한다! ( 수정된 엔티티가 캐시에만 저장 되어있다 롤백하면 그냥 업데이트 안하고 날라감 )
    @Test
    void updateItem() {
        //given
        Item item = new Item("item1", 10000, 10);
        Item savedItem = itemRepository.save(item);
        Long itemId = savedItem.getId();

        //when
        ItemUpdateDto updateParam = new ItemUpdateDto("item2", 20000, 30);
        itemRepository.update(itemId, updateParam);

        //then
        Item findItem = itemRepository.findById(itemId).get();
        assertThat(findItem.getItemName()).isEqualTo(updateParam.getItemName());
        assertThat(findItem.getPrice()).isEqualTo(updateParam.getPrice());
        assertThat(findItem.getQuantity()).isEqualTo(updateParam.getQuantity());
    }

    @Test
    void findItems() {
        //given
        Item item1 = new Item("itemA-1", 10000, 10);
        Item item2 = new Item("itemA-2", 20000, 20);
        Item item3 = new Item("itemB-1", 30000, 30);

        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);

        //둘 다 없음 검증
        test(null, null, item1, item2, item3);
        test("", null, item1, item2, item3);

        //itemName 검증
        test("itemA", null, item1, item2);
        test("temA", null, item1, item2);
        test("itemB", null, item3);

        //maxPrice 검증
        test(null, 10000, item1);

        //둘 다 있음 검증
        test("itemA", 10000, item1);
    }
    // Item... 은 가변 인자 ( 여기에 해당 타입으로 몇개라도 넣을 수 있다. )
    // 이때 해당 items 는 Item[] 타입으로 전달된 인자들을 받는다.
    // 즉 Item... items 랑 Item[] items 은 사실 같음. 다만 해당 메서드 호춣할때 방법이 다름
    // 전자는 ( , item1, item2, item3) 이런식으로 나열,  후자는 Item[] 타입 상태로 전달해야 함 ( or new Item[]{item1, item2 .. } )
    void test(String itemName, Integer maxPrice, Item... items) {
        List<Item> result = itemRepository.findAll(new ItemSearchCond(itemName, maxPrice));
        assertThat(result).contains(items);
    }
}
