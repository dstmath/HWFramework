package org.apache.xpath.objects;

import org.apache.xml.utils.XMLString;

/* compiled from: XNodeSet */
abstract class Comparator {
    /* access modifiers changed from: package-private */
    public abstract boolean compareNumbers(double d, double d2);

    /* access modifiers changed from: package-private */
    public abstract boolean compareStrings(XMLString xMLString, XMLString xMLString2);

    Comparator() {
    }
}
