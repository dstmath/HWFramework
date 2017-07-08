package java.security;

public class PrivilegedActionException extends Exception {
    public PrivilegedActionException(Exception exception) {
        super((Throwable) exception);
    }

    public Exception getException() {
        return null;
    }
}
