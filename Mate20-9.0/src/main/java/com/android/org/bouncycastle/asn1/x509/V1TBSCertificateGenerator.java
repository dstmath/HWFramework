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

    public void setSerialNumber(ASN1Integer serialNumber2) {
        this.serialNumber = serialNumber2;
    }

    public void setSignature(AlgorithmIdentifier signature2) {
        this.signature = signature2;
    }

    public void setIssuer(X509Name issuer2) {
        this.issuer = X500Name.getInstance(issuer2.toASN1Primitive());
    }

    public void setIssuer(X500Name issuer2) {
        this.issuer = issuer2;
    }

    public void setStartDate(Time startDate2) {
        this.startDate = startDate2;
    }

    public void setStartDate(ASN1UTCTime startDate2) {
        this.startDate = new Time((ASN1Primitive) startDate2);
    }

    public void setEndDate(Time endDate2) {
        this.endDate = endDate2;
    }

    public void setEndDate(ASN1UTCTime endDate2) {
        this.endDate = new Time((ASN1Primitive) endDate2);
    }

    public void setSubject(X509Name subject2) {
        this.subject = X500Name.getInstance(subject2.toASN1Primitive());
    }

    public void setSubject(X500Name subject2) {
        this.subject = subject2;
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
