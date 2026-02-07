package hello.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;


@Slf4j
@SpringBootTest
class OrderServiceTest {
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    // 테스트에서 별도의 DB 설정 없으면 자동으로 메모리 DB로 실행하며
    // 자동으로 테이블 만들어주고 종료될때 다시 drop 시켜버림
    @Test
    void complete() throws NotEnoughMoneyException {
        // given
        Order order = new Order();
        order.setUserName("정상");

        // when
        orderService.order(order);

        // then
        Order findOrder = orderRepository.findById(order.getId()).get();

        Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("완료");
    }

    @Test
    void runtimeException() throws NotEnoughMoneyException {

        // given
        Order order = new Order();
        order.setUserName("예외");

        // when
        Assertions.assertThatThrownBy(() -> orderService.order(order))
                .isInstanceOf(RuntimeException.class);

        // then
        // Optional 은 .isEmpty() 로 값이 비었는지 아닌지 체크 가능
        Optional<Order> findOrder = orderRepository.findById(order.getId());
        Assertions.assertThat(findOrder.isEmpty()).isTrue();

    }

    // 체크 예외는 커밋 하는 이유?
    // 체크 예외 -> 비즈니스 예외라고 스프링이 가정 하고 있기 때문
    // ex ) 이 예시 처럼 잔고부족은 단순 예외가 아닌, 해당 예외를 이용해 별다른 비즈니스적인 처리를 하고 싶은 상태
    // 이때 롤백해버리면 기존 고객에 요청정보가 날라가버림 , 따라서 커밋해서 고객의 기존 요청을 저장하고 이를 이용해 고객에게 안내를 할 수 있도록 
    // 비즈니스플로우 설정이 가능
    @Test
    void businessException() {
        // given
        Order order = new Order();
        order.setUserName("잔고부족");

        // when
        try {
            orderService.order(order);
        } catch (NotEnoughMoneyException e) {
            log.info("고객에게 잔고 부족 알리고 별도 계좌 입금하라 안내함");
        }

        // then
        Order findOrder = orderRepository.findById(order.getId()).get();
        Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("대기");
    }

}