package android.sax;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/* access modifiers changed from: package-private */
public class BadXmlException extends SAXParseException {
    public BadXmlException(String message, Locator locator) {
        super(message, locator);
    }

    @Override // java.lang.Throwable, org.xml.sax.SAXException
    public String getMessage() {
        return "Line " + getLineNumber() + ": " + super.getMessage();
    }
}
