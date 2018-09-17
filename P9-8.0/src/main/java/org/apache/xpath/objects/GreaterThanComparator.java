package org.apache.xpath.objects;

import org.apache.xml.utils.XMLString;

/* compiled from: XNodeSet */
class GreaterThanComparator extends Comparator {
    GreaterThanComparator() {
    }

    boolean compareStrings(XMLString s1, XMLString s2) {
        return s1.toDouble() > s2.toDouble();
    }

    boolean compareNumbers(double n1, double n2) {
        return n1 > n2;
    }
}
