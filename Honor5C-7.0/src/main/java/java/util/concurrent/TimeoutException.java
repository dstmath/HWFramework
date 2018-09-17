package java.util.concurrent;

public class TimeoutException extends Exception {
    private static final long serialVersionUID = 1900926677490660714L;

    public TimeoutException(String message) {
        super(message);
    }
}
