package org.apache.xpath.objects;

import org.apache.xml.utils.XMLString;

/* compiled from: XNodeSet */
class EqualComparator extends Comparator {
    EqualComparator() {
    }

    boolean compareStrings(XMLString s1, XMLString s2) {
        return s1.equals(s2);
    }

    boolean compareNumbers(double n1, double n2) {
        return n1 == n2;
    }
}
