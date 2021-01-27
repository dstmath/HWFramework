package org.bouncycastle.asn1.esf;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class OcspListID extends ASN1Object {
    private ASN1Sequence ocspResponses;

    private OcspListID(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 1) {
            this.ocspResponses = (ASN1Sequence) aSN1Sequence.getObjectAt(0);
            Enumeration objects = this.ocspResponses.getObjects();
            while (objects.hasMoreElements()) {
                OcspResponsesID.getInstance(objects.nextElement());
            }
            return;
        }
        throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
    }

    public OcspListID(OcspResponsesID[] ocspResponsesIDArr) {
        this.ocspResponses = new DERSequence(ocspResponsesIDArr);
    }

    public static OcspListID getInstance(Object obj) {
        if (obj instanceof OcspListID) {
            return (OcspListID) obj;
        }
        if (obj != null) {
            return new OcspListID(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public OcspResponsesID[] getOcspResponses() {
        OcspResponsesID[] ocspResponsesIDArr = new OcspResponsesID[this.ocspResponses.size()];
        for (int i = 0; i < ocspResponsesIDArr.length; i++) {
            ocspResponsesIDArr[i] = OcspResponsesID.getInstance(this.ocspResponses.getObjectAt(i));
        }
        return ocspResponsesIDArr;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        return new DERSequence(this.ocspResponses);
    }
}
