package ohos.security.keystore.provider;

public class KeyStoreException extends Exception {
    private static final long serialVersionUID = 2766705176013315008L;
    private final int errorCode;

    public KeyStoreException(int i, String str) {
        super(str);
        this.errorCode = i;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
