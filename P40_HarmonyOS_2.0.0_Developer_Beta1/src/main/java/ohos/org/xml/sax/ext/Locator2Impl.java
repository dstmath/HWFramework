package ohos.org.xml.sax.ext;

import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.helpers.LocatorImpl;

public class Locator2Impl extends LocatorImpl implements Locator2 {
    private String encoding;
    private String version;

    public Locator2Impl() {
    }

    public Locator2Impl(Locator locator) {
        super(locator);
        if (locator instanceof Locator2) {
            Locator2 locator2 = (Locator2) locator;
            this.version = locator2.getXMLVersion();
            this.encoding = locator2.getEncoding();
        }
    }

    @Override // ohos.org.xml.sax.ext.Locator2
    public String getXMLVersion() {
        return this.version;
    }

    @Override // ohos.org.xml.sax.ext.Locator2
    public String getEncoding() {
        return this.encoding;
    }

    public void setXMLVersion(String str) {
        this.version = str;
    }

    public void setEncoding(String str) {
        this.encoding = str;
    }
}
