package android.net.lowpan;

public class InterfaceDisabledException extends LowpanException {
    public InterfaceDisabledException() {
    }

    public InterfaceDisabledException(String message) {
        super(message);
    }

    public InterfaceDisabledException(String message, Throwable cause) {
        super(message, cause);
    }

    protected InterfaceDisabledException(Exception cause) {
        super(cause);
    }
}
