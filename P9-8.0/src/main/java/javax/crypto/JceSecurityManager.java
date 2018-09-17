package javax.crypto;

final class JceSecurityManager extends SecurityManager {
    static final JceSecurityManager INSTANCE = null;

    private JceSecurityManager() {
    }

    CryptoPermission getCryptoPermission(String alg) {
        return null;
    }
}
