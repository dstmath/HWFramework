package com.android.org.bouncycastle.asn1;

import java.util.Enumeration;
import java.util.Vector;

public class ASN1EncodableVector {
    private final Vector v = new Vector();

    public void add(ASN1Encodable obj) {
        this.v.addElement(obj);
    }

    public void addAll(ASN1EncodableVector other) {
        Enumeration en = other.v.elements();
        while (en.hasMoreElements()) {
            this.v.addElement(en.nextElement());
        }
    }

    public ASN1Encodable get(int i) {
        return (ASN1Encodable) this.v.elementAt(i);
    }

    public int size() {
        return this.v.size();
    }
}
