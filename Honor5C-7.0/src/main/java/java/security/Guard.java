package java.security;

public interface Guard {
    void checkGuard(Object obj) throws SecurityException;
}
