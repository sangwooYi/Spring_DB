package hello.jdbc.repository.ex;

public class MyDuplicatedKeyException extends MyDbException {

    // 예외 전환 용 예외 만들때는 아래 4가지 생성자 습관적으로 선언할 것
    public MyDuplicatedKeyException() {
    }

    public MyDuplicatedKeyException(String message) {
        super(message);
    }

    public MyDuplicatedKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicatedKeyException(Throwable cause) {
        super(cause);
    }

}
