package org.bouncycastle.asn1.cmc;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

public class ExtendedFailInfo extends ASN1Object {
    private final ASN1ObjectIdentifier failInfoOID;
    private final ASN1Encodable failInfoValue;

    public ExtendedFailInfo(ASN1ObjectIdentifier aSN1ObjectIdentifier, ASN1Encodable aSN1Encodable) {
        this.failInfoOID = aSN1ObjectIdentifier;
        this.failInfoValue = aSN1Encodable;
    }

    private ExtendedFailInfo(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 2) {
            this.failInfoOID = ASN1ObjectIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
            this.failInfoValue = aSN1Sequence.getObjectAt(1);
            return;
        }
        throw new IllegalArgumentException("Sequence must be 2 elements.");
    }

    public static ExtendedFailInfo getInstance(Object obj) {
        if (obj instanceof ExtendedFailInfo) {
            return (ExtendedFailInfo) obj;
        }
        if (obj instanceof ASN1Encodable) {
            ASN1Primitive aSN1Primitive = ((ASN1Encodable) obj).toASN1Primitive();
            if (aSN1Primitive instanceof ASN1Sequence) {
                return new ExtendedFailInfo((ASN1Sequence) aSN1Primitive);
            }
        } else if (obj instanceof byte[]) {
            return getInstance(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public ASN1ObjectIdentifier getFailInfoOID() {
        return this.failInfoOID;
    }

    public ASN1Encodable getFailInfoValue() {
        return this.failInfoValue;
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERSequence(new ASN1Encodable[]{this.failInfoOID, this.failInfoValue});
    }
}
