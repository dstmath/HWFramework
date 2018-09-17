package com.android.org.bouncycastle.asn1.cms;

import com.android.org.bouncycastle.asn1.ASN1Choice;
import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERTaggedObject;

public class SignerIdentifier extends ASN1Object implements ASN1Choice {
    private ASN1Encodable id;

    public SignerIdentifier(IssuerAndSerialNumber id) {
        this.id = id;
    }

    public SignerIdentifier(ASN1OctetString id) {
        this.id = new DERTaggedObject(false, 0, id);
    }

    public SignerIdentifier(ASN1Primitive id) {
        this.id = id;
    }

    public static SignerIdentifier getInstance(Object o) {
        if (o == null || (o instanceof SignerIdentifier)) {
            return (SignerIdentifier) o;
        }
        if (o instanceof IssuerAndSerialNumber) {
            return new SignerIdentifier((IssuerAndSerialNumber) o);
        }
        if (o instanceof ASN1OctetString) {
            return new SignerIdentifier((ASN1OctetString) o);
        }
        if (o instanceof ASN1Primitive) {
            return new SignerIdentifier((ASN1Primitive) o);
        }
        throw new IllegalArgumentException("Illegal object in SignerIdentifier: " + o.getClass().getName());
    }

    public boolean isTagged() {
        return this.id instanceof ASN1TaggedObject;
    }

    public ASN1Encodable getId() {
        if (this.id instanceof ASN1TaggedObject) {
            return ASN1OctetString.getInstance((ASN1TaggedObject) this.id, false);
        }
        return this.id;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.id.toASN1Primitive();
    }
}
