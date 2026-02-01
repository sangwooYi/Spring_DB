package hello.itemservice;

import hello.itemservice.domain.Item;
import hello.itemservice.repository.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
public class TestDataInit {

    private final ItemRepository itemRepository;
    
    // 생성자 생성 후 호출되는 친구
    // 이렇게 쓸 때 AOP 처리가 안된 채로 호출 될 수 있다.
//    @PostConstruct
//    public void init() {
//        log.info("test Data init");
//        itemRepository.save(new Item("itemA", 10000, 10));
//        itemRepository.save(new Item("itemB", 20000, 20));
//        itemRepository.save(new Item("itemC", 50000, 5));
//    }

    // 스프링컨테이너가 완전히 실행 된 후에 호출 됨 ApplicationReadyEvent.class 의 역할
    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        log.info("test Data Init!");
        itemRepository.save(new Item("itemA", 10000, 100));
        itemRepository.save(new Item("itemB", 3000, 2000));
    }
}
