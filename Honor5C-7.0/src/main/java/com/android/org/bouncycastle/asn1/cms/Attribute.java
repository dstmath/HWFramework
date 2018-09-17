package com.android.org.bouncycastle.asn1.cms;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1Set;
import com.android.org.bouncycastle.asn1.DERSequence;

public class Attribute extends ASN1Object {
    private ASN1ObjectIdentifier attrType;
    private ASN1Set attrValues;

    public static Attribute getInstance(Object o) {
        if (o instanceof Attribute) {
            return (Attribute) o;
        }
        if (o != null) {
            return new Attribute(ASN1Sequence.getInstance(o));
        }
        return null;
    }

    private Attribute(ASN1Sequence seq) {
        this.attrType = (ASN1ObjectIdentifier) seq.getObjectAt(0);
        this.attrValues = (ASN1Set) seq.getObjectAt(1);
    }

    public Attribute(ASN1ObjectIdentifier attrType, ASN1Set attrValues) {
        this.attrType = attrType;
        this.attrValues = attrValues;
    }

    public ASN1ObjectIdentifier getAttrType() {
        return this.attrType;
    }

    public ASN1Set getAttrValues() {
        return this.attrValues;
    }

    public ASN1Encodable[] getAttributeValues() {
        return this.attrValues.toArray();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.attrType);
        v.add(this.attrValues);
        return new DERSequence(v);
    }
}
