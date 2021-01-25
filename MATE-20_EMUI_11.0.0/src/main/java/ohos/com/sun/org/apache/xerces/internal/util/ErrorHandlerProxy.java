package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLErrorHandler;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;

public abstract class ErrorHandlerProxy implements ErrorHandler {
    /* access modifiers changed from: protected */
    public abstract XMLErrorHandler getErrorHandler();

    public void error(SAXParseException sAXParseException) throws SAXException {
        XMLErrorHandler errorHandler = getErrorHandler();
        if (errorHandler instanceof ErrorHandlerWrapper) {
            ((ErrorHandlerWrapper) errorHandler).fErrorHandler.error(sAXParseException);
        } else {
            errorHandler.error("", "", ErrorHandlerWrapper.createXMLParseException(sAXParseException));
        }
    }

    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        XMLErrorHandler errorHandler = getErrorHandler();
        if (errorHandler instanceof ErrorHandlerWrapper) {
            ((ErrorHandlerWrapper) errorHandler).fErrorHandler.fatalError(sAXParseException);
        } else {
            errorHandler.fatalError("", "", ErrorHandlerWrapper.createXMLParseException(sAXParseException));
        }
    }

    public void warning(SAXParseException sAXParseException) throws SAXException {
        XMLErrorHandler errorHandler = getErrorHandler();
        if (errorHandler instanceof ErrorHandlerWrapper) {
            ((ErrorHandlerWrapper) errorHandler).fErrorHandler.warning(sAXParseException);
        } else {
            errorHandler.warning("", "", ErrorHandlerWrapper.createXMLParseException(sAXParseException));
        }
    }
}
