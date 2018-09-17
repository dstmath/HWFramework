package java.util.concurrent;

public class CancellationException extends IllegalStateException {
    private static final long serialVersionUID = -9202173006928992231L;

    public CancellationException(String message) {
        super(message);
    }
}
