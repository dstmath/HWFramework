package org.xml.sax.ext;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

public class Locator2Impl extends LocatorImpl implements Locator2 {
    private String encoding;
    private String version;

    public Locator2Impl(Locator locator) {
        super(locator);
        if (locator instanceof Locator2) {
            Locator2 l2 = (Locator2) locator;
            this.version = l2.getXMLVersion();
            this.encoding = l2.getEncoding();
        }
    }

    public String getXMLVersion() {
        return this.version;
    }

    public String getEncoding() {
        return this.encoding;
    }

    public void setXMLVersion(String version) {
        this.version = version;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
