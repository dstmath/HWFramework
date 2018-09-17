package javax.security.auth;

public interface Destroyable {
    void destroy() throws DestroyFailedException;

    boolean isDestroyed();
}
