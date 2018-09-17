package tmsdk.common.exception;

public class RootAccessNotGainedException extends RuntimeException {
    public String getMessage() {
        return "Root permission is not granted!";
    }
}
