package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class PKIMessages extends ASN1Object {
    private ASN1Sequence content;

    private PKIMessages(ASN1Sequence aSN1Sequence) {
        this.content = aSN1Sequence;
    }

    public PKIMessages(PKIMessage pKIMessage) {
        this.content = new DERSequence((ASN1Encodable) pKIMessage);
    }

    public PKIMessages(PKIMessage[] pKIMessageArr) {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        for (PKIMessage add : pKIMessageArr) {
            aSN1EncodableVector.add(add);
        }
        this.content = new DERSequence(aSN1EncodableVector);
    }

    public static PKIMessages getInstance(Object obj) {
        if (obj instanceof PKIMessages) {
            return (PKIMessages) obj;
        }
        if (obj != null) {
            return new PKIMessages(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.content;
    }

    public PKIMessage[] toPKIMessageArray() {
        PKIMessage[] pKIMessageArr = new PKIMessage[this.content.size()];
        for (int i = 0; i != pKIMessageArr.length; i++) {
            pKIMessageArr[i] = PKIMessage.getInstance(this.content.getObjectAt(i));
        }
        return pKIMessageArr;
    }
}
