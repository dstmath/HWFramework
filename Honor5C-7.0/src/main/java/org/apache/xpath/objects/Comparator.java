package org.apache.xpath.objects;

import org.apache.xml.utils.XMLString;

/* compiled from: XNodeSet */
abstract class Comparator {
    abstract boolean compareNumbers(double d, double d2);

    abstract boolean compareStrings(XMLString xMLString, XMLString xMLString2);

    Comparator() {
    }
}
