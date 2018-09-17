package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1UTCTime;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import com.android.org.bouncycastle.asn1.x500.X500Name;

public class V3TBSCertificateGenerator {
    private boolean altNamePresentAndCritical;
    Time endDate;
    Extensions extensions;
    X500Name issuer;
    private DERBitString issuerUniqueID;
    ASN1Integer serialNumber;
    AlgorithmIdentifier signature;
    Time startDate;
    X500Name subject;
    SubjectPublicKeyInfo subjectPublicKeyInfo;
    private DERBitString subjectUniqueID;
    DERTaggedObject version = new DERTaggedObject(true, 0, new ASN1Integer(2));

    public void setSerialNumber(ASN1Integer serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setSignature(AlgorithmIdentifier signature) {
        this.signature = signature;
    }

    public void setIssuer(X509Name issuer) {
        this.issuer = X500Name.getInstance(issuer);
    }

    public void setIssuer(X500Name issuer) {
        this.issuer = issuer;
    }

    public void setStartDate(ASN1UTCTime startDate) {
        this.startDate = new Time((ASN1Primitive) startDate);
    }

    public void setStartDate(Time startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(ASN1UTCTime endDate) {
        this.endDate = new Time((ASN1Primitive) endDate);
    }

    public void setEndDate(Time endDate) {
        this.endDate = endDate;
    }

    public void setSubject(X509Name subject) {
        this.subject = X500Name.getInstance(subject.toASN1Primitive());
    }

    public void setSubject(X500Name subject) {
        this.subject = subject;
    }

    public void setIssuerUniqueID(DERBitString uniqueID) {
        this.issuerUniqueID = uniqueID;
    }

    public void setSubjectUniqueID(DERBitString uniqueID) {
        this.subjectUniqueID = uniqueID;
    }

    public void setSubjectPublicKeyInfo(SubjectPublicKeyInfo pubKeyInfo) {
        this.subjectPublicKeyInfo = pubKeyInfo;
    }

    public void setExtensions(X509Extensions extensions) {
        setExtensions(Extensions.getInstance(extensions));
    }

    public void setExtensions(Extensions extensions) {
        this.extensions = extensions;
        if (extensions != null) {
            Extension altName = extensions.getExtension(Extension.subjectAlternativeName);
            if (altName != null && altName.isCritical()) {
                this.altNamePresentAndCritical = true;
            }
        }
    }

    public TBSCertificate generateTBSCertificate() {
        if (this.serialNumber == null || this.signature == null || this.issuer == null || this.startDate == null || this.endDate == null || ((this.subject == null && (this.altNamePresentAndCritical ^ 1) != 0) || this.subjectPublicKeyInfo == null)) {
            throw new IllegalStateException("not all mandatory fields set in V3 TBScertificate generator");
        }
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.version);
        v.add(this.serialNumber);
        v.add(this.signature);
        v.add(this.issuer);
        ASN1EncodableVector validity = new ASN1EncodableVector();
        validity.add(this.startDate);
        validity.add(this.endDate);
        v.add(new DERSequence(validity));
        if (this.subject != null) {
            v.add(this.subject);
        } else {
            v.add(new DERSequence());
        }
        v.add(this.subjectPublicKeyInfo);
        if (this.issuerUniqueID != null) {
            v.add(new DERTaggedObject(false, 1, this.issuerUniqueID));
        }
        if (this.subjectUniqueID != null) {
            v.add(new DERTaggedObject(false, 2, this.subjectUniqueID));
        }
        if (this.extensions != null) {
            v.add(new DERTaggedObject(true, 3, this.extensions));
        }
        return TBSCertificate.getInstance(new DERSequence(v));
    }
}
