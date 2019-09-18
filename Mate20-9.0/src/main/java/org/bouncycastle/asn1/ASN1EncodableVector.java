package org.bouncycastle.asn1;

import java.util.Enumeration;
import java.util.Vector;

public class ASN1EncodableVector {
    private final Vector v = new Vector();

    public void add(ASN1Encodable aSN1Encodable) {
        this.v.addElement(aSN1Encodable);
    }

    public void addAll(ASN1EncodableVector aSN1EncodableVector) {
        Enumeration elements = aSN1EncodableVector.v.elements();
        while (elements.hasMoreElements()) {
            this.v.addElement(elements.nextElement());
        }
    }

    public ASN1Encodable get(int i) {
        return (ASN1Encodable) this.v.elementAt(i);
    }

    public int size() {
        return this.v.size();
    }
}
