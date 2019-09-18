package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

public class CertConfirmContent extends ASN1Object {
    private ASN1Sequence content;

    private CertConfirmContent(ASN1Sequence aSN1Sequence) {
        this.content = aSN1Sequence;
    }

    public static CertConfirmContent getInstance(Object obj) {
        if (obj instanceof CertConfirmContent) {
            return (CertConfirmContent) obj;
        }
        if (obj != null) {
            return new CertConfirmContent(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.content;
    }

    public CertStatus[] toCertStatusArray() {
        CertStatus[] certStatusArr = new CertStatus[this.content.size()];
        for (int i = 0; i != certStatusArr.length; i++) {
            certStatusArr[i] = CertStatus.getInstance(this.content.getObjectAt(i));
        }
        return certStatusArr;
    }
}
