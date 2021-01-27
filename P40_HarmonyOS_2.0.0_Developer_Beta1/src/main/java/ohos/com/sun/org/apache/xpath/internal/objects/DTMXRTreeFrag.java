package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xml.internal.dtm.DTM;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;

public final class DTMXRTreeFrag {
    private DTM m_dtm;
    private int m_dtmIdentity = -1;
    private XPathContext m_xctxt;

    public DTMXRTreeFrag(int i, XPathContext xPathContext) {
        this.m_xctxt = xPathContext;
        this.m_dtmIdentity = i;
        this.m_dtm = xPathContext.getDTM(i);
    }

    public final void destruct() {
        this.m_dtm = null;
        this.m_xctxt = null;
    }

    /* access modifiers changed from: package-private */
    public final DTM getDTM() {
        return this.m_dtm;
    }

    public final int getDTMIdentity() {
        return this.m_dtmIdentity;
    }

    /* access modifiers changed from: package-private */
    public final XPathContext getXPathContext() {
        return this.m_xctxt;
    }

    public final int hashCode() {
        return this.m_dtmIdentity;
    }

    public final boolean equals(Object obj) {
        if (!(obj instanceof DTMXRTreeFrag) || this.m_dtmIdentity != ((DTMXRTreeFrag) obj).getDTMIdentity()) {
            return false;
        }
        return true;
    }
}
