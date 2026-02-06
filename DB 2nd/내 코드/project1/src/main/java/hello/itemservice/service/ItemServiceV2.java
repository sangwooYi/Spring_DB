package hello.itemservice.service;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import hello.itemservice.repository.ItemSearchCond;
import hello.itemservice.repository.ItemUpdateDto;
import hello.itemservice.repository.v2.ItemQueryRepositoryV2;
import hello.itemservice.repository.v2.ItemRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceV2 implements ItemService {

    // 이렇게 실용적인 설계도 충분히 가능함!
    private final ItemRepositoryV2 itemRepositoryV2;
    private final ItemQueryRepositoryV2 itemQueryRepositoryV2;

    // save, update 가 적용되는 시점 -> 트랜잭션 commit 될 때
    @Override
    public Item save(Item item) {
        return itemRepositoryV2.save(item);
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item item = itemRepositoryV2.findById(itemId).orElse(null);
        if (item != null) {
            // 위에서 find 해오면 영속성 관리 대상에 있으므로
            // 아래처럼 값 변경해주면 알아서 변경사항 업데이트 해준다 !!
            item.setItemName(updateParam.getItemName());
            item.setPrice(updateParam.getPrice());
            item.setQuantity(updateParam.getQuantity());
        }
    }

    @Override
    public Optional<Item> findById(Long id) {
        return itemRepositoryV2.findById(id);
    }

    // 이렇게 좀 복잡한 조건은 queryDsl 사용한 Repository 사용
    @Override
    public List<Item> findItems(ItemSearchCond cond) {
        return itemQueryRepositoryV2.findAll(cond);
    }
}
