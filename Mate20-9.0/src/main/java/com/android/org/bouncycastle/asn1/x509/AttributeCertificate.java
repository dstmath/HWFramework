package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERSequence;

public class AttributeCertificate extends ASN1Object {
    AttributeCertificateInfo acinfo;
    AlgorithmIdentifier signatureAlgorithm;
    DERBitString signatureValue;

    public static AttributeCertificate getInstance(Object obj) {
        if (obj instanceof AttributeCertificate) {
            return (AttributeCertificate) obj;
        }
        if (obj != null) {
            return new AttributeCertificate(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public AttributeCertificate(AttributeCertificateInfo acinfo2, AlgorithmIdentifier signatureAlgorithm2, DERBitString signatureValue2) {
        this.acinfo = acinfo2;
        this.signatureAlgorithm = signatureAlgorithm2;
        this.signatureValue = signatureValue2;
    }

    public AttributeCertificate(ASN1Sequence seq) {
        if (seq.size() == 3) {
            this.acinfo = AttributeCertificateInfo.getInstance(seq.getObjectAt(0));
            this.signatureAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
            this.signatureValue = DERBitString.getInstance(seq.getObjectAt(2));
            return;
        }
        throw new IllegalArgumentException("Bad sequence size: " + seq.size());
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
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.acinfo);
        v.add(this.signatureAlgorithm);
        v.add(this.signatureValue);
        return new DERSequence(v);
    }
}
