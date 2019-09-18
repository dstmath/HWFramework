package org.bouncycastle.asn1.cmc;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.util.Arrays;

public class PendInfo extends ASN1Object {
    private final ASN1GeneralizedTime pendTime;
    private final byte[] pendToken;

    private PendInfo(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 2) {
            this.pendToken = Arrays.clone(ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(0)).getOctets());
            this.pendTime = ASN1GeneralizedTime.getInstance(aSN1Sequence.getObjectAt(1));
            return;
        }
        throw new IllegalArgumentException("incorrect sequence size");
    }

    public PendInfo(byte[] bArr, ASN1GeneralizedTime aSN1GeneralizedTime) {
        this.pendToken = Arrays.clone(bArr);
        this.pendTime = aSN1GeneralizedTime;
    }

    public static PendInfo getInstance(Object obj) {
        if (obj instanceof PendInfo) {
            return (PendInfo) obj;
        }
        if (obj != null) {
            return new PendInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1GeneralizedTime getPendTime() {
        return this.pendTime;
    }

    public byte[] getPendToken() {
        return this.pendToken;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(new DEROctetString(this.pendToken));
        aSN1EncodableVector.add(this.pendTime);
        return new DERSequence(aSN1EncodableVector);
    }
}
