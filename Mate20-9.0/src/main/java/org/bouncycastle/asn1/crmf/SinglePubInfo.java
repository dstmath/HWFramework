package org.bouncycastle.asn1.crmf;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.GeneralName;

public class SinglePubInfo extends ASN1Object {
    private GeneralName pubLocation;
    private ASN1Integer pubMethod;

    private SinglePubInfo(ASN1Sequence aSN1Sequence) {
        this.pubMethod = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0));
        if (aSN1Sequence.size() == 2) {
            this.pubLocation = GeneralName.getInstance(aSN1Sequence.getObjectAt(1));
        }
    }

    public static SinglePubInfo getInstance(Object obj) {
        if (obj instanceof SinglePubInfo) {
            return (SinglePubInfo) obj;
        }
        if (obj != null) {
            return new SinglePubInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public GeneralName getPubLocation() {
        return this.pubLocation;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.pubMethod);
        if (this.pubLocation != null) {
            aSN1EncodableVector.add(this.pubLocation);
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
