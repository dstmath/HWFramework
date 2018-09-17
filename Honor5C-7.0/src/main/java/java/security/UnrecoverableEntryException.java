package java.security;

public class UnrecoverableEntryException extends GeneralSecurityException {
    private static final long serialVersionUID = -4527142945246286535L;

    public UnrecoverableEntryException(String msg) {
        super(msg);
    }
}
