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

    public SAXParseException(String message, String publicId, String systemId, int lineNumber, int columnNumber) {
        super(message);
        init(publicId, systemId, lineNumber, columnNumber);
    }

    public SAXParseException(String message, String publicId, String systemId, int lineNumber, int columnNumber, Exception e) {
        super(message, e);
        init(publicId, systemId, lineNumber, columnNumber);
    }

    private void init(String publicId, String systemId, int lineNumber, int columnNumber) {
        this.publicId = publicId;
        this.systemId = systemId;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
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
