package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class CertificationRequest extends ASN1Object {
    protected CertificationRequestInfo reqInfo = null;
    protected AlgorithmIdentifier sigAlgId = null;
    protected DERBitString sigBits = null;

    public static CertificationRequest getInstance(Object o) {
        if (o instanceof CertificationRequest) {
            return (CertificationRequest) o;
        }
        if (o != null) {
            return new CertificationRequest(ASN1Sequence.getInstance(o));
        }
        return null;
    }

    protected CertificationRequest() {
    }

    public CertificationRequest(CertificationRequestInfo requestInfo, AlgorithmIdentifier algorithm, DERBitString signature) {
        this.reqInfo = requestInfo;
        this.sigAlgId = algorithm;
        this.sigBits = signature;
    }

    public CertificationRequest(ASN1Sequence seq) {
        this.reqInfo = CertificationRequestInfo.getInstance(seq.getObjectAt(0));
        this.sigAlgId = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
        this.sigBits = (DERBitString) seq.getObjectAt(2);
    }

    public CertificationRequestInfo getCertificationRequestInfo() {
        return this.reqInfo;
    }

    public AlgorithmIdentifier getSignatureAlgorithm() {
        return this.sigAlgId;
    }

    public DERBitString getSignature() {
        return this.sigBits;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.reqInfo);
        v.add(this.sigAlgId);
        v.add(this.sigBits);
        return new DERSequence(v);
    }
}
