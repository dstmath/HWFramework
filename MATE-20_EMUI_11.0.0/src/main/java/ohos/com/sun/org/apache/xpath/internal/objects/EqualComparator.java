package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xml.internal.utils.XMLString;

/* compiled from: XNodeSet */
class EqualComparator extends Comparator {
    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xpath.internal.objects.Comparator
    public boolean compareNumbers(double d, double d2) {
        return d == d2;
    }

    EqualComparator() {
    }

    /* access modifiers changed from: package-private */
    @Override // ohos.com.sun.org.apache.xpath.internal.objects.Comparator
    public boolean compareStrings(XMLString xMLString, XMLString xMLString2) {
        return xMLString.equals(xMLString2);
    }
}
