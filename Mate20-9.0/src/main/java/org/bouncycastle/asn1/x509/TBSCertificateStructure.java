package org.bouncycastle.asn1.x509;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;

public class TBSCertificateStructure extends ASN1Object implements X509ObjectIdentifiers, PKCSObjectIdentifiers {
    Time endDate;
    X509Extensions extensions;
    X500Name issuer;
    DERBitString issuerUniqueId;
    ASN1Sequence seq;
    ASN1Integer serialNumber;
    AlgorithmIdentifier signature;
    Time startDate;
    X500Name subject;
    SubjectPublicKeyInfo subjectPublicKeyInfo;
    DERBitString subjectUniqueId;
    ASN1Integer version;

    public TBSCertificateStructure(ASN1Sequence aSN1Sequence) {
        int i;
        this.seq = aSN1Sequence;
        if (aSN1Sequence.getObjectAt(0) instanceof DERTaggedObject) {
            this.version = ASN1Integer.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(0), true);
            i = 0;
        } else {
            this.version = new ASN1Integer(0);
            i = -1;
        }
        this.serialNumber = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(i + 1));
        this.signature = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(i + 2));
        this.issuer = X500Name.getInstance(aSN1Sequence.getObjectAt(i + 3));
        ASN1Sequence aSN1Sequence2 = (ASN1Sequence) aSN1Sequence.getObjectAt(i + 4);
        this.startDate = Time.getInstance(aSN1Sequence2.getObjectAt(0));
        this.endDate = Time.getInstance(aSN1Sequence2.getObjectAt(1));
        this.subject = X500Name.getInstance(aSN1Sequence.getObjectAt(i + 5));
        int i2 = i + 6;
        this.subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(aSN1Sequence.getObjectAt(i2));
        for (int size = (aSN1Sequence.size() - i2) - 1; size > 0; size--) {
            DERTaggedObject dERTaggedObject = (DERTaggedObject) aSN1Sequence.getObjectAt(i2 + size);
            switch (dERTaggedObject.getTagNo()) {
                case 1:
                    this.issuerUniqueId = DERBitString.getInstance(dERTaggedObject, false);
                    break;
                case 2:
                    this.subjectUniqueId = DERBitString.getInstance(dERTaggedObject, false);
                    break;
                case 3:
                    this.extensions = X509Extensions.getInstance(dERTaggedObject);
                    break;
            }
        }
    }

    public static TBSCertificateStructure getInstance(Object obj) {
        if (obj instanceof TBSCertificateStructure) {
            return (TBSCertificateStructure) obj;
        }
        if (obj != null) {
            return new TBSCertificateStructure(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static TBSCertificateStructure getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public Time getEndDate() {
        return this.endDate;
    }

    public X509Extensions getExtensions() {
        return this.extensions;
    }

    public X500Name getIssuer() {
        return this.issuer;
    }

    public DERBitString getIssuerUniqueId() {
        return this.issuerUniqueId;
    }

    public ASN1Integer getSerialNumber() {
        return this.serialNumber;
    }

    public AlgorithmIdentifier getSignature() {
        return this.signature;
    }

    public Time getStartDate() {
        return this.startDate;
    }

    public X500Name getSubject() {
        return this.subject;
    }

    public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
        return this.subjectPublicKeyInfo;
    }

    public DERBitString getSubjectUniqueId() {
        return this.subjectUniqueId;
    }

    public int getVersion() {
        return this.version.getValue().intValue() + 1;
    }

    public ASN1Integer getVersionNumber() {
        return this.version;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.seq;
    }
}
