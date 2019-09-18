package com.android.org.bouncycastle.x509;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Set;
import com.android.org.bouncycastle.asn1.DERSet;
import com.android.org.bouncycastle.asn1.x509.Attribute;

public class X509Attribute extends ASN1Object {
    Attribute attr;

    X509Attribute(ASN1Encodable at) {
        this.attr = Attribute.getInstance(at);
    }

    public X509Attribute(String oid, ASN1Encodable value) {
        this.attr = new Attribute(new ASN1ObjectIdentifier(oid), new DERSet(value));
    }

    public X509Attribute(String oid, ASN1EncodableVector value) {
        this.attr = new Attribute(new ASN1ObjectIdentifier(oid), new DERSet(value));
    }

    public String getOID() {
        return this.attr.getAttrType().getId();
    }

    public ASN1Encodable[] getValues() {
        ASN1Set s = this.attr.getAttrValues();
        ASN1Encodable[] values = new ASN1Encodable[s.size()];
        for (int i = 0; i != s.size(); i++) {
            values[i] = s.getObjectAt(i);
        }
        return values;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.attr.toASN1Primitive();
    }
}
