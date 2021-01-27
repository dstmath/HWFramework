package ohos.org.w3c.dom.xpath;

public class XPathException extends RuntimeException {
    public static final short INVALID_EXPRESSION_ERR = 1;
    public static final short TYPE_ERR = 2;
    public short code;

    public XPathException(short s, String str) {
        super(str);
        this.code = s;
    }
}
