package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import ohos.com.sun.org.apache.xerces.internal.util.URI;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.javax.xml.transform.Source;

public final class XMLInputSourceAdaptor implements Source {
    public final XMLInputSource fSource;

    public XMLInputSourceAdaptor(XMLInputSource xMLInputSource) {
        this.fSource = xMLInputSource;
    }

    public void setSystemId(String str) {
        this.fSource.setSystemId(str);
    }

    public String getSystemId() {
        try {
            return XMLEntityManager.expandSystemId(this.fSource.getSystemId(), this.fSource.getBaseSystemId(), false);
        } catch (URI.MalformedURIException unused) {
            return this.fSource.getSystemId();
        }
    }
}
