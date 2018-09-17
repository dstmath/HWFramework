package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1UTCTime;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import com.android.org.bouncycastle.asn1.x500.X500Name;

public class V1TBSCertificateGenerator {
    Time endDate;
    X500Name issuer;
    ASN1Integer serialNumber;
    AlgorithmIdentifier signature;
    Time startDate;
    X500Name subject;
    SubjectPublicKeyInfo subjectPublicKeyInfo;
    DERTaggedObject version = new DERTaggedObject(true, 0, new ASN1Integer(0));

    public void setSerialNumber(ASN1Integer serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setSignature(AlgorithmIdentifier signature) {
        this.signature = signature;
    }

    public void setIssuer(X509Name issuer) {
        this.issuer = X500Name.getInstance(issuer.toASN1Primitive());
    }

    public void setIssuer(X500Name issuer) {
        this.issuer = issuer;
    }

    public void setStartDate(Time startDate) {
        this.startDate = startDate;
    }

    public void setStartDate(ASN1UTCTime startDate) {
        this.startDate = new Time((ASN1Primitive) startDate);
    }

    public void setEndDate(Time endDate) {
        this.endDate = endDate;
    }

    public void setEndDate(ASN1UTCTime endDate) {
        this.endDate = new Time((ASN1Primitive) endDate);
    }

    public void setSubject(X509Name subject) {
        this.subject = X500Name.getInstance(subject.toASN1Primitive());
    }

    public void setSubject(X500Name subject) {
        this.subject = subject;
    }

    public void setSubjectPublicKeyInfo(SubjectPublicKeyInfo pubKeyInfo) {
        this.subjectPublicKeyInfo = pubKeyInfo;
    }

    public TBSCertificate generateTBSCertificate() {
        if (this.serialNumber == null || this.signature == null || this.issuer == null || this.startDate == null || this.endDate == null || this.subject == null || this.subjectPublicKeyInfo == null) {
            throw new IllegalStateException("not all mandatory fields set in V1 TBScertificate generator");
        }
        ASN1EncodableVector seq = new ASN1EncodableVector();
        seq.add(this.serialNumber);
        seq.add(this.signature);
        seq.add(this.issuer);
        ASN1EncodableVector validity = new ASN1EncodableVector();
        validity.add(this.startDate);
        validity.add(this.endDate);
        seq.add(new DERSequence(validity));
        seq.add(this.subject);
        seq.add(this.subjectPublicKeyInfo);
        return TBSCertificate.getInstance(new DERSequence(seq));
    }
}
