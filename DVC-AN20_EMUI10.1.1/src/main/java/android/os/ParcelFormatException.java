package android.os;

public class ParcelFormatException extends RuntimeException {
    public ParcelFormatException() {
    }

    public ParcelFormatException(String reason) {
        super(reason);
    }
}
