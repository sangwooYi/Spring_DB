package hello.jdbc.repository.ex;

public class MyDbException extends RuntimeException {

    // 예외 전환 용 예외 만들때는 아래 4가지 생성자 습관적으로 선언할 것

    public MyDbException() {
    }

    public MyDbException(String message) {
        super(message);
    }

    public MyDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDbException(Throwable cause) {
        super(cause);
    }
}
