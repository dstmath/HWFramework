package org.apache.xpath;

public class XPathProcessorException extends XPathException {
    static final long serialVersionUID = 1215509418326642603L;

    public XPathProcessorException(String message) {
        super(message);
    }

    public XPathProcessorException(String message, Exception e) {
        super(message, e);
    }
}
