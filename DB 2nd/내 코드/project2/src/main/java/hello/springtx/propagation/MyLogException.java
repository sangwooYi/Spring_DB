package hello.springtx.propagation;

public class MyLogException extends RuntimeException {
    public MyLogException(String message) {
        super(message);
    }
}
