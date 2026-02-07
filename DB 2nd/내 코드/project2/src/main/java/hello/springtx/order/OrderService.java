package hello.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


// 팁 Alt + Insert 누르고 나서 테스트 선택하면 같은 뎁스로 테스트 위치에다가 테스트 클래스 만들어 준다!
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    // 트랜잭션 커밋 시점에 Order 데이터를 DB에 반영
    @Transactional
    public void order(Order order) throws NotEnoughMoneyException{
        log.info("order 호출");
        orderRepository.save(order);    // Jpa persist

        log.info("결제 프로세스 호출");
        if (order.getUserName().equals("예외")) {
            log.info("시스템 예외 발생");
            throw new RuntimeException("시스템 예외 발생");
        } else if (order.getUserName().equals("잔고부족")) {
            log.info("잔액 부족 예외 발생");
            order.setPayStatus("대기");
            throw new NotEnoughMoneyException("잔고 부족합니다.");
        } else {
            log.info("정상 승인");
            order.setPayStatus("완료");
        }
        log.info("결제 프로세스 완료");
    }

}
