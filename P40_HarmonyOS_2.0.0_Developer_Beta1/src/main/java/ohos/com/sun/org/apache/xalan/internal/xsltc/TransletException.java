package ohos.com.sun.org.apache.xalan.internal.xsltc;

import ohos.org.xml.sax.SAXException;

public final class TransletException extends SAXException {
    static final long serialVersionUID = -878916829521217293L;

    public TransletException() {
        super("Translet error");
    }

    public TransletException(Exception exc) {
        super(exc.toString());
        initCause(exc);
    }

    public TransletException(String str) {
        super(str);
    }
}
