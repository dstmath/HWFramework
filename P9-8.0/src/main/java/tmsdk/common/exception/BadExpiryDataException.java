package tmsdk.common.exception;

public final class BadExpiryDataException extends IllegalArgumentException {
    public BadExpiryDataException() {
        super("Bad expiry data");
    }
}
