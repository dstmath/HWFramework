package huawei.android.security.securityprofile;

public class SignatureInvalidException extends Exception {
    private static final long serialVersionUID = 1;

    public SignatureInvalidException(String message) {
        super(message);
    }
}
