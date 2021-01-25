package ohos.com.sun.org.apache.xpath.internal;

public class XPathProcessorException extends XPathException {
    static final long serialVersionUID = 1215509418326642603L;

    public XPathProcessorException(String str) {
        super(str);
    }

    public XPathProcessorException(String str, Exception exc) {
        super(str, exc);
    }
}
