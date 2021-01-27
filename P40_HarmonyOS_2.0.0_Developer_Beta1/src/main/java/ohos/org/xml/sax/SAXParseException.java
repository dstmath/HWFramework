package ohos.org.xml.sax;

public class SAXParseException extends SAXException {
    static final long serialVersionUID = -5651165872476709336L;
    private int columnNumber;
    private int lineNumber;
    private String publicId;
    private String systemId;

    public SAXParseException(String str, Locator locator) {
        super(str);
        if (locator != null) {
            init(locator.getPublicId(), locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
        } else {
            init(null, null, -1, -1);
        }
    }

    public SAXParseException(String str, Locator locator, Exception exc) {
        super(str, exc);
        if (locator != null) {
            init(locator.getPublicId(), locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
        } else {
            init(null, null, -1, -1);
        }
    }

    public SAXParseException(String str, String str2, String str3, int i, int i2) {
        super(str);
        init(str2, str3, i, i2);
    }

    public SAXParseException(String str, String str2, String str3, int i, int i2, Exception exc) {
        super(str, exc);
        init(str2, str3, i, i2);
    }

    private void init(String str, String str2, int i, int i2) {
        this.publicId = str;
        this.systemId = str2;
        this.lineNumber = i;
        this.columnNumber = i2;
    }

    public String getPublicId() {
        return this.publicId;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int getColumnNumber() {
        return this.columnNumber;
    }

    @Override // ohos.org.xml.sax.SAXException, java.lang.Throwable, java.lang.Object
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        String localizedMessage = getLocalizedMessage();
        if (this.publicId != null) {
            sb.append("publicId: ");
            sb.append(this.publicId);
        }
        if (this.systemId != null) {
            sb.append("; systemId: ");
            sb.append(this.systemId);
        }
        if (this.lineNumber != -1) {
            sb.append("; lineNumber: ");
            sb.append(this.lineNumber);
        }
        if (this.columnNumber != -1) {
            sb.append("; columnNumber: ");
            sb.append(this.columnNumber);
        }
        if (localizedMessage != null) {
            sb.append("; ");
            sb.append(localizedMessage);
        }
        return sb.toString();
    }
}
