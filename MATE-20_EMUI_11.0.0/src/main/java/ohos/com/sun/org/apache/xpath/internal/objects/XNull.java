package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xpath.internal.XPath;
import ohos.com.sun.org.apache.xpath.internal.XPathContext;

public class XNull extends XNodeSet {
    static final long serialVersionUID = -6841683711458983005L;

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean bool() {
        return false;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public int getType() {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String getTypeString() {
        return "#CLASS_NULL";
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public double num() {
        return XPath.MATCH_SCORE_QNAME;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public int rtf(XPathContext xPathContext) {
        return -1;
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public String str() {
        return "";
    }

    @Override // ohos.com.sun.org.apache.xpath.internal.objects.XNodeSet, ohos.com.sun.org.apache.xpath.internal.objects.XObject
    public boolean equals(XObject xObject) {
        return xObject.getType() == -1;
    }
}
