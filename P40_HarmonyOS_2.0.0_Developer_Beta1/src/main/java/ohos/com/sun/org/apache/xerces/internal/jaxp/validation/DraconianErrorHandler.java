package ohos.com.sun.org.apache.xerces.internal.jaxp.validation;

import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;

/* access modifiers changed from: package-private */
public final class DraconianErrorHandler implements ErrorHandler {
    private static final DraconianErrorHandler ERROR_HANDLER_INSTANCE = new DraconianErrorHandler();

    public void warning(SAXParseException sAXParseException) throws SAXException {
    }

    private DraconianErrorHandler() {
    }

    public static DraconianErrorHandler getInstance() {
        return ERROR_HANDLER_INSTANCE;
    }

    public void error(SAXParseException sAXParseException) throws SAXException {
        throw sAXParseException;
    }

    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        throw sAXParseException;
    }
}
