package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;

public class DraconianErrorHandler implements ErrorHandler {
    public static final ErrorHandler theInstance = new DraconianErrorHandler();

    public void warning(SAXParseException sAXParseException) throws SAXException {
    }

    private DraconianErrorHandler() {
    }

    public void error(SAXParseException sAXParseException) throws SAXException {
        throw sAXParseException;
    }

    public void fatalError(SAXParseException sAXParseException) throws SAXException {
        throw sAXParseException;
    }
}
