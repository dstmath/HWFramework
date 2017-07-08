package org.apache.xpath.objects;

import org.apache.xpath.XPathContext;

public class XNull extends XNodeSet {
    static final long serialVersionUID = -6841683711458983005L;

    public int getType() {
        return -1;
    }

    public String getTypeString() {
        return "#CLASS_NULL";
    }

    public double num() {
        return 0.0d;
    }

    public boolean bool() {
        return false;
    }

    public String str() {
        return SerializerConstants.EMPTYSTRING;
    }

    public int rtf(XPathContext support) {
        return -1;
    }

    public boolean equals(XObject obj2) {
        return obj2.getType() == -1;
    }
}
