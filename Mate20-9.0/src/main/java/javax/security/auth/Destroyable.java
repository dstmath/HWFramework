package javax.security.auth;

public interface Destroyable {
    void destroy() throws DestroyFailedException {
        throw new DestroyFailedException();
    }

    boolean isDestroyed() {
        return false;
    }
}
