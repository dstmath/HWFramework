package ohos.com.sun.org.apache.xml.internal.utils;

public class NSInfo {
    public static final int ANCESTORHASXMLNS = 1;
    public static final int ANCESTORNOXMLNS = 2;
    public static final int ANCESTORXMLNSUNPROCESSED = 0;
    public int m_ancestorHasXMLNSAttrs;
    public boolean m_hasProcessedNS;
    public boolean m_hasXMLNSAttrs;
    public String m_namespace;

    public NSInfo(boolean z, boolean z2) {
        this.m_hasProcessedNS = z;
        this.m_hasXMLNSAttrs = z2;
        this.m_namespace = null;
        this.m_ancestorHasXMLNSAttrs = 0;
    }

    public NSInfo(boolean z, boolean z2, int i) {
        this.m_hasProcessedNS = z;
        this.m_hasXMLNSAttrs = z2;
        this.m_ancestorHasXMLNSAttrs = i;
        this.m_namespace = null;
    }

    public NSInfo(String str, boolean z) {
        this.m_hasProcessedNS = true;
        this.m_hasXMLNSAttrs = z;
        this.m_namespace = str;
        this.m_ancestorHasXMLNSAttrs = 0;
    }
}
