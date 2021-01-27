package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParseException;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;

public abstract class ErrorHandlerAdaptor implements XMLErrorHandler {
    private boolean hadError = false;

    /* access modifiers changed from: protected */
    public abstract ErrorHandler getErrorHandler();

    public boolean hadError() {
        return this.hadError;
    }

    public void reset() {
        this.hadError = false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void fatalError(String str, String str2, XMLParseException xMLParseException) {
        try {
            this.hadError = true;
            getErrorHandler().fatalError(Util.toSAXParseException(xMLParseException));
        } catch (SAXException e) {
            throw new WrappedSAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void error(String str, String str2, XMLParseException xMLParseException) {
        try {
            this.hadError = true;
            getErrorHandler().error(Util.toSAXParseException(xMLParseException));
        } catch (SAXException e) {
            throw new WrappedSAXException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler
    public void warning(String str, String str2, XMLParseException xMLParseException) {
        try {
            getErrorHandler().warning(Util.toSAXParseException(xMLParseException));
        } catch (SAXException e) {
            throw new WrappedSAXException(e);
        }
    }
}
