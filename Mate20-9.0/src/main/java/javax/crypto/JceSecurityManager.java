package javax.crypto;

final class JceSecurityManager extends SecurityManager {
    static final JceSecurityManager INSTANCE = null;

    private JceSecurityManager() {
    }

    /* access modifiers changed from: package-private */
    public CryptoPermission getCryptoPermission(String alg) {
        return null;
    }
}
