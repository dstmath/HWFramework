package libcore.tzdata.shared2;

public class DistroException extends Exception {
    public DistroException(String message) {
        super(message);
    }

    public DistroException(String message, Throwable cause) {
        super(message, cause);
    }
}
