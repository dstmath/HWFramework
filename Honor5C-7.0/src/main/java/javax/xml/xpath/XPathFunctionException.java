package javax.xml.xpath;

public class XPathFunctionException extends XPathExpressionException {
    private static final long serialVersionUID = -1837080260374986980L;

    public XPathFunctionException(String message) {
        super(message);
    }

    public XPathFunctionException(Throwable cause) {
        super(cause);
    }
}
