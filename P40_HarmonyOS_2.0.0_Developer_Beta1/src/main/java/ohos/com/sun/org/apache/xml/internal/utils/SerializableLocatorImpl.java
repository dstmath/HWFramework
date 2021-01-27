package ohos.com.sun.org.apache.xml.internal.utils;

import java.io.Serializable;
import ohos.org.xml.sax.Locator;

public class SerializableLocatorImpl implements Locator, Serializable {
    static final long serialVersionUID = -2660312888446371460L;
    private int columnNumber;
    private int lineNumber;
    private String publicId;
    private String systemId;

    public SerializableLocatorImpl() {
    }

    public SerializableLocatorImpl(Locator locator) {
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
