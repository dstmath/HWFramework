package ohos.com.sun.org.apache.xerces.internal.xs;

public class XSException extends RuntimeException {
    public static final short INDEX_SIZE_ERR = 2;
    public static final short NOT_SUPPORTED_ERR = 1;
    static final long serialVersionUID = 3111893084677917742L;
    public short code;

    public XSException(short s, String str) {
        super(str);
        this.code = s;
    }
}
