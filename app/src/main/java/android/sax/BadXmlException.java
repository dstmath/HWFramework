package android.sax;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

class BadXmlException extends SAXParseException {
    public BadXmlException(String message, Locator locator) {
        super(message, locator);
    }

    public String getMessage() {
        return "Line " + getLineNumber() + ": " + super.getMessage();
    }
}
