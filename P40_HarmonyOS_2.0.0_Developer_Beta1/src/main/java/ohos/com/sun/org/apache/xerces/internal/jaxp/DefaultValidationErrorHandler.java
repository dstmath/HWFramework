package ohos.com.sun.org.apache.xerces.internal.jaxp;

import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.util.SAXMessageFormatter;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXParseException;
import ohos.org.xml.sax.helpers.DefaultHandler;

class DefaultValidationErrorHandler extends DefaultHandler {
    private static int ERROR_COUNT_LIMIT = 10;
    private int errorCount = 0;
    private Locale locale = Locale.getDefault();

    public DefaultValidationErrorHandler(Locale locale2) {
        this.locale = locale2;
    }

    public void error(SAXParseException sAXParseException) throws SAXException {
        int i = this.errorCount;
        if (i < ERROR_COUNT_LIMIT) {
            if (i == 0) {
                System.err.println(SAXMessageFormatter.formatMessage(this.locale, "errorHandlerNotSet", new Object[]{Integer.valueOf(this.errorCount)}));
            }
            String systemId = sAXParseException.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            System.err.println("Error: URI=" + systemId + " Line=" + sAXParseException.getLineNumber() + ": " + sAXParseException.getMessage());
            this.errorCount = this.errorCount + 1;
        }
    }
}
