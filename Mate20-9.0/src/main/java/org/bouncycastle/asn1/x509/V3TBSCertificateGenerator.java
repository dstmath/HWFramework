package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x500.X500Name;

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

    public TBSCertificate generateTBSCertificate() {
        if (this.serialNumber == null || this.signature == null || this.issuer == null || this.startDate == null || this.endDate == null || ((this.subject == null && !this.altNamePresentAndCritical) || this.subjectPublicKeyInfo == null)) {
            throw new IllegalStateException("not all mandatory fields set in V3 TBScertificate generator");
        }
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.serialNumber);
        aSN1EncodableVector.add(this.signature);
        aSN1EncodableVector.add(this.issuer);
        ASN1EncodableVector aSN1EncodableVector2 = new ASN1EncodableVector();
        aSN1EncodableVector2.add(this.startDate);
        aSN1EncodableVector2.add(this.endDate);
        aSN1EncodableVector.add(new DERSequence(aSN1EncodableVector2));
        aSN1EncodableVector.add(this.subject != null ? this.subject : new DERSequence());
        aSN1EncodableVector.add(this.subjectPublicKeyInfo);
        if (this.issuerUniqueID != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, this.issuerUniqueID));
        }
        if (this.subjectUniqueID != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 2, this.subjectUniqueID));
        }
        if (this.extensions != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 3, this.extensions));
        }
        return TBSCertificate.getInstance(new DERSequence(aSN1EncodableVector));
    }

    public void setEndDate(ASN1UTCTime aSN1UTCTime) {
        this.endDate = new Time((ASN1Primitive) aSN1UTCTime);
    }

    public void setEndDate(Time time) {
        this.endDate = time;
    }

    public void setExtensions(Extensions extensions2) {
        this.extensions = extensions2;
        if (extensions2 != null) {
            Extension extension = extensions2.getExtension(Extension.subjectAlternativeName);
            if (extension != null && extension.isCritical()) {
                this.altNamePresentAndCritical = true;
            }
        }
    }

    public void setExtensions(X509Extensions x509Extensions) {
        setExtensions(Extensions.getInstance(x509Extensions));
    }

    public void setIssuer(X500Name x500Name) {
        this.issuer = x500Name;
    }

    public void setIssuer(X509Name x509Name) {
        this.issuer = X500Name.getInstance(x509Name);
    }

    public void setIssuerUniqueID(DERBitString dERBitString) {
        this.issuerUniqueID = dERBitString;
    }

    public void setSerialNumber(ASN1Integer aSN1Integer) {
        this.serialNumber = aSN1Integer;
    }

    public void setSignature(AlgorithmIdentifier algorithmIdentifier) {
        this.signature = algorithmIdentifier;
    }

    public void setStartDate(ASN1UTCTime aSN1UTCTime) {
        this.startDate = new Time((ASN1Primitive) aSN1UTCTime);
    }

    public void setStartDate(Time time) {
        this.startDate = time;
    }

    public void setSubject(X500Name x500Name) {
        this.subject = x500Name;
    }

    public void setSubject(X509Name x509Name) {
        this.subject = X500Name.getInstance(x509Name.toASN1Primitive());
    }

    public void setSubjectPublicKeyInfo(SubjectPublicKeyInfo subjectPublicKeyInfo2) {
        this.subjectPublicKeyInfo = subjectPublicKeyInfo2;
    }

    public void setSubjectUniqueID(DERBitString dERBitString) {
        this.subjectUniqueID = dERBitString;
    }
}
