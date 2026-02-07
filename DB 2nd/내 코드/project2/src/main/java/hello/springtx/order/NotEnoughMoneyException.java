package hello.springtx.order;

/**
 *  결제 잔고 부족시 발생 예외 ( 비즈니스 예외 )
 *  반드시 체크하고 넘어도록 하기 위해 체크 예외로 설정
 */
public class NotEnoughMoneyException extends Exception {

    public NotEnoughMoneyException(String message) {
        super(message);
    }

}
