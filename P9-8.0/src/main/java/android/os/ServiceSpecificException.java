package android.os;

public class ServiceSpecificException extends RuntimeException {
    public final int errorCode;

    public ServiceSpecificException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ServiceSpecificException(int errorCode) {
        this.errorCode = errorCode;
    }

    public String toString() {
        return super.toString() + " (code " + this.errorCode + ")";
    }
}
