package ohos.com.sun.org.apache.xpath.internal.objects;

import ohos.com.sun.org.apache.xml.internal.utils.XMLString;

/* access modifiers changed from: package-private */
/* compiled from: XNodeSet */
public abstract class Comparator {
    /* access modifiers changed from: package-private */
    public abstract boolean compareNumbers(double d, double d2);

    /* access modifiers changed from: package-private */
    public abstract boolean compareStrings(XMLString xMLString, XMLString xMLString2);

    Comparator() {
    }
}
