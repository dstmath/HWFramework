package ohos.global.i18n.phonenumbers;

public class PhoneNumberParseException extends Exception {
    private static final long serialVersionUID = 1;
    private Type errorType;
    private String msg;

    public enum Type {
        WRONG_COUNTRY_CODE,
        NOT_A_NUMBER,
        TOO_SHORT_AFTER_IDD,
        TOO_SHORT_NSN,
        TOO_LONG
    }

    public PhoneNumberParseException(Type type, String str) {
        super(str);
        this.msg = str;
        this.errorType = type;
    }

    public Type getErrorType() {
        return this.errorType;
    }

    @Override // java.lang.Throwable, java.lang.Object
    public String toString() {
        return "Error type: " + this.errorType + ". " + this.msg;
    }
}
