package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;

public class AttributeCertificate extends ASN1Object {
    AttributeCertificateInfo acinfo;
    AlgorithmIdentifier signatureAlgorithm;
    DERBitString signatureValue;

    public AttributeCertificate(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 3) {
            this.acinfo = AttributeCertificateInfo.getInstance(aSN1Sequence.getObjectAt(0));
            this.signatureAlgorithm = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(1));
            this.signatureValue = DERBitString.getInstance(aSN1Sequence.getObjectAt(2));
            return;
        }
        throw new IllegalArgumentException("Bad sequence size: " + aSN1Sequence.size());
    }

    public AttributeCertificate(AttributeCertificateInfo attributeCertificateInfo, AlgorithmIdentifier algorithmIdentifier, DERBitString dERBitString) {
        this.acinfo = attributeCertificateInfo;
        this.signatureAlgorithm = algorithmIdentifier;
        this.signatureValue = dERBitString;
    }

    public static AttributeCertificate getInstance(Object obj) {
        if (obj instanceof AttributeCertificate) {
            return (AttributeCertificate) obj;
        }
        if (obj != null) {
            return new AttributeCertificate(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public AttributeCertificateInfo getAcinfo() {
        return this.acinfo;
    }

    public AlgorithmIdentifier getSignatureAlgorithm() {
        return this.signatureAlgorithm;
    }

    public DERBitString getSignatureValue() {
        return this.signatureValue;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.acinfo);
        aSN1EncodableVector.add(this.signatureAlgorithm);
        aSN1EncodableVector.add(this.signatureValue);
        return new DERSequence(aSN1EncodableVector);
    }
}
