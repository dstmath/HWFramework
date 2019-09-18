package org.xml.sax;

public class SAXParseException extends SAXException {
    private int columnNumber;
    private int lineNumber;
    private String publicId;
    private String systemId;

    public SAXParseException(String message, Locator locator) {
        super(message);
        if (locator != null) {
            init(locator.getPublicId(), locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
        } else {
            init(null, null, -1, -1);
        }
    }

    public SAXParseException(String message, Locator locator, Exception e) {
        super(message, e);
        if (locator != null) {
            init(locator.getPublicId(), locator.getSystemId(), locator.getLineNumber(), locator.getColumnNumber());
        } else {
            init(null, null, -1, -1);
        }
    }

    public SAXParseException(String message, String publicId2, String systemId2, int lineNumber2, int columnNumber2) {
        super(message);
        init(publicId2, systemId2, lineNumber2, columnNumber2);
    }

    public SAXParseException(String message, String publicId2, String systemId2, int lineNumber2, int columnNumber2, Exception e) {
        super(message, e);
        init(publicId2, systemId2, lineNumber2, columnNumber2);
    }

    private void init(String publicId2, String systemId2, int lineNumber2, int columnNumber2) {
        this.publicId = publicId2;
        this.systemId = systemId2;
        this.lineNumber = lineNumber2;
        this.columnNumber = columnNumber2;
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
}
