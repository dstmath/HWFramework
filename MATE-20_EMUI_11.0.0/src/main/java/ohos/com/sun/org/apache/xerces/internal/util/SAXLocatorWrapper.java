package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.ext.Locator2;

public final class SAXLocatorWrapper implements XMLLocator {
    private Locator fLocator = null;
    private Locator2 fLocator2 = null;

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getBaseSystemId() {
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public int getCharacterOffset() {
        return -1;
    }

    public void setLocator(Locator locator) {
        this.fLocator = locator;
        if ((locator instanceof Locator2) || locator == null) {
            this.fLocator2 = (Locator2) locator;
        }
    }

    public Locator getLocator() {
        return this.fLocator;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getPublicId() {
        Locator locator = this.fLocator;
        if (locator != null) {
            return locator.getPublicId();
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getLiteralSystemId() {
        Locator locator = this.fLocator;
        if (locator != null) {
            return locator.getSystemId();
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getExpandedSystemId() {
        return getLiteralSystemId();
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public int getLineNumber() {
        Locator locator = this.fLocator;
        if (locator != null) {
            return locator.getLineNumber();
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public int getColumnNumber() {
        Locator locator = this.fLocator;
        if (locator != null) {
            return locator.getColumnNumber();
        }
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getEncoding() {
        Locator2 locator2 = this.fLocator2;
        if (locator2 != null) {
            return locator2.getEncoding();
        }
        return null;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator
    public String getXMLVersion() {
        Locator2 locator2 = this.fLocator2;
        if (locator2 != null) {
            return locator2.getXMLVersion();
        }
        return null;
    }
}
