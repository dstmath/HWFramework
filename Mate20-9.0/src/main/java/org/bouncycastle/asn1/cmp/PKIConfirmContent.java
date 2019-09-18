package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1Null;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERNull;

public class PKIConfirmContent extends ASN1Object {
    private ASN1Null val;

    public PKIConfirmContent() {
        this.val = DERNull.INSTANCE;
    }

    private PKIConfirmContent(ASN1Null aSN1Null) {
        this.val = aSN1Null;
    }

    public static PKIConfirmContent getInstance(Object obj) {
        if (obj == null || (obj instanceof PKIConfirmContent)) {
            return (PKIConfirmContent) obj;
        }
        if (obj instanceof ASN1Null) {
            return new PKIConfirmContent((ASN1Null) obj);
        }
        throw new IllegalArgumentException("Invalid object: " + obj.getClass().getName());
    }

    public ASN1Primitive toASN1Primitive() {
        return this.val;
    }
}
