package org.bouncycastle.asn1.bc;

import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.util.Arrays;

public class SignatureCheck extends ASN1Object {
    private final ASN1Sequence certificates;
    private final AlgorithmIdentifier signatureAlgorithm;
    private final ASN1BitString signatureValue;

    private SignatureCheck(ASN1Sequence aSN1Sequence) {
        this.signatureAlgorithm = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
        int i = 1;
        if (aSN1Sequence.getObjectAt(1) instanceof ASN1TaggedObject) {
            this.certificates = ASN1Sequence.getInstance(ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(1)).getObject());
            i = 2;
        } else {
            this.certificates = null;
        }
        this.signatureValue = DERBitString.getInstance(aSN1Sequence.getObjectAt(i));
    }

    public SignatureCheck(AlgorithmIdentifier algorithmIdentifier, byte[] bArr) {
        this.signatureAlgorithm = algorithmIdentifier;
        this.certificates = null;
        this.signatureValue = new DERBitString(Arrays.clone(bArr));
    }

    public SignatureCheck(AlgorithmIdentifier algorithmIdentifier, Certificate[] certificateArr, byte[] bArr) {
        this.signatureAlgorithm = algorithmIdentifier;
        this.certificates = new DERSequence(certificateArr);
        this.signatureValue = new DERBitString(Arrays.clone(bArr));
    }

    public static SignatureCheck getInstance(Object obj) {
        if (obj instanceof SignatureCheck) {
            return (SignatureCheck) obj;
        }
        if (obj != null) {
            return new SignatureCheck(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public Certificate[] getCertificates() {
        ASN1Sequence aSN1Sequence = this.certificates;
        if (aSN1Sequence == null) {
            return null;
        }
        Certificate[] certificateArr = new Certificate[aSN1Sequence.size()];
        for (int i = 0; i != certificateArr.length; i++) {
            certificateArr[i] = Certificate.getInstance(this.certificates.getObjectAt(i));
        }
        return certificateArr;
    }

    public ASN1BitString getSignature() {
        return new DERBitString(this.signatureValue.getBytes(), this.signatureValue.getPadBits());
    }

    public AlgorithmIdentifier getSignatureAlgorithm() {
        return this.signatureAlgorithm;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(3);
        aSN1EncodableVector.add(this.signatureAlgorithm);
        ASN1Sequence aSN1Sequence = this.certificates;
        if (aSN1Sequence != null) {
            aSN1EncodableVector.add(new DERTaggedObject(0, aSN1Sequence));
        }
        aSN1EncodableVector.add(this.signatureValue);
        return new DERSequence(aSN1EncodableVector);
    }
}
