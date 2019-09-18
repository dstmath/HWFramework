package org.xml.sax.helpers;

import org.xml.sax.Locator;

public class LocatorImpl implements Locator {
    private int columnNumber;
    private int lineNumber;
    private String publicId;
    private String systemId;

    public LocatorImpl() {
    }

    public LocatorImpl(Locator locator) {
        setPublicId(locator.getPublicId());
        setSystemId(locator.getSystemId());
        setLineNumber(locator.getLineNumber());
        setColumnNumber(locator.getColumnNumber());
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

    public void setPublicId(String publicId2) {
        this.publicId = publicId2;
    }

    public void setSystemId(String systemId2) {
        this.systemId = systemId2;
    }

    public void setLineNumber(int lineNumber2) {
        this.lineNumber = lineNumber2;
    }

    public void setColumnNumber(int columnNumber2) {
        this.columnNumber = columnNumber2;
    }
}
