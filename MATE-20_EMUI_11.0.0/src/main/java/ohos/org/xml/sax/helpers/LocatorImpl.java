package ohos.org.xml.sax.helpers;

import ohos.org.xml.sax.Locator;

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

    @Override // ohos.org.xml.sax.Locator
    public String getPublicId() {
        return this.publicId;
    }

    @Override // ohos.org.xml.sax.Locator
    public String getSystemId() {
        return this.systemId;
    }

    @Override // ohos.org.xml.sax.Locator
    public int getLineNumber() {
        return this.lineNumber;
    }

    @Override // ohos.org.xml.sax.Locator
    public int getColumnNumber() {
        return this.columnNumber;
    }

    public void setPublicId(String str) {
        this.publicId = str;
    }

    public void setSystemId(String str) {
        this.systemId = str;
    }

    public void setLineNumber(int i) {
        this.lineNumber = i;
    }

    public void setColumnNumber(int i) {
        this.columnNumber = i;
    }
}
