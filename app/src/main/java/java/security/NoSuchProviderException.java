package java.security;

public class NoSuchProviderException extends GeneralSecurityException {
    private static final long serialVersionUID = 8488111756688534474L;

    public NoSuchProviderException(String msg) {
        super(msg);
    }
}
