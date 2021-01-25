package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.org.xml.sax.Locator;

public class LocatorWrapper implements XMLLocator {
    private final Locator locator;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getBaseSystemId() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public int getCharacterOffset() {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getEncoding() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getXMLVersion() {
        return null;
    }

    public LocatorWrapper(Locator locator2) {
        this.locator = locator2;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public int getColumnNumber() {
        return this.locator.getColumnNumber();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public int getLineNumber() {
        return this.locator.getLineNumber();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getExpandedSystemId() {
        return this.locator.getSystemId();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getLiteralSystemId() {
        return this.locator.getSystemId();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getPublicId() {
        return this.locator.getPublicId();
    }
}
