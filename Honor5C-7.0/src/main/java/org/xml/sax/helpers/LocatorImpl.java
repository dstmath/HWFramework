package org.xml.sax.helpers;

import org.xml.sax.Locator;

public class LocatorImpl implements Locator {
    private int columnNumber;
    private int lineNumber;
    private String publicId;
    private String systemId;

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

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }
}
