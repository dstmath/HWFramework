package ohos.com.sun.org.apache.xerces.internal.dom;

import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.org.w3c.dom.DOMError;
import ohos.org.w3c.dom.DOMLocator;

public class DOMErrorImpl implements DOMError {
    public Exception fException = null;
    public DOMLocatorImpl fLocator = new DOMLocatorImpl();
    public String fMessage = null;
    public Object fRelatedData;
    public short fSeverity = 1;
    public String fType;

    public DOMErrorImpl() {
    }

    public DOMErrorImpl(short s, XMLParseException xMLParseException) {
        this.fSeverity = s;
        this.fException = xMLParseException;
        this.fLocator = createDOMLocator(xMLParseException);
    }

    public short getSeverity() {
        return this.fSeverity;
    }

    public String getMessage() {
        return this.fMessage;
    }

    public DOMLocator getLocation() {
        return this.fLocator;
    }

    private DOMLocatorImpl createDOMLocator(XMLParseException xMLParseException) {
        return new DOMLocatorImpl(xMLParseException.getLineNumber(), xMLParseException.getColumnNumber(), xMLParseException.getCharacterOffset(), xMLParseException.getExpandedSystemId());
    }

    public Object getRelatedException() {
        return this.fException;
    }

    public void reset() {
        this.fSeverity = 1;
        this.fException = null;
    }

    public String getType() {
        return this.fType;
    }

    public Object getRelatedData() {
        return this.fRelatedData;
    }
}
