package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x500.X500Name;

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

    public static TBSCertificateStructure getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
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

    public TBSCertificateStructure(ASN1Sequence seq2) {
        int seqStart = 0;
        this.seq = seq2;
        if (seq2.getObjectAt(0) instanceof DERTaggedObject) {
            this.version = ASN1Integer.getInstance((ASN1TaggedObject) seq2.getObjectAt(0), true);
        } else {
            seqStart = -1;
            this.version = new ASN1Integer(0);
        }
        this.serialNumber = ASN1Integer.getInstance(seq2.getObjectAt(seqStart + 1));
        this.signature = AlgorithmIdentifier.getInstance(seq2.getObjectAt(seqStart + 2));
        this.issuer = X500Name.getInstance(seq2.getObjectAt(seqStart + 3));
        ASN1Sequence dates = (ASN1Sequence) seq2.getObjectAt(seqStart + 4);
        this.startDate = Time.getInstance(dates.getObjectAt(0));
        this.endDate = Time.getInstance(dates.getObjectAt(1));
        this.subject = X500Name.getInstance(seq2.getObjectAt(seqStart + 5));
        this.subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(seq2.getObjectAt(seqStart + 6));
        int extras = (seq2.size() - (seqStart + 6)) - 1;
        while (true) {
            int extras2 = extras;
            if (extras2 > 0) {
                DERTaggedObject extra = (DERTaggedObject) seq2.getObjectAt(seqStart + 6 + extras2);
                switch (extra.getTagNo()) {
                    case 1:
                        this.issuerUniqueId = DERBitString.getInstance(extra, false);
                        break;
                    case 2:
                        this.subjectUniqueId = DERBitString.getInstance(extra, false);
                        break;
                    case 3:
                        this.extensions = X509Extensions.getInstance(extra);
                        break;
                }
                extras = extras2 - 1;
            } else {
                return;
            }
        }
    }

    public int getVersion() {
        return this.version.getValue().intValue() + 1;
    }

    public ASN1Integer getVersionNumber() {
        return this.version;
    }

    public ASN1Integer getSerialNumber() {
        return this.serialNumber;
    }

    public AlgorithmIdentifier getSignature() {
        return this.signature;
    }

    public X500Name getIssuer() {
        return this.issuer;
    }

    public Time getStartDate() {
        return this.startDate;
    }

    public Time getEndDate() {
        return this.endDate;
    }

    public X500Name getSubject() {
        return this.subject;
    }

    public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
        return this.subjectPublicKeyInfo;
    }

    public DERBitString getIssuerUniqueId() {
        return this.issuerUniqueId;
    }

    public DERBitString getSubjectUniqueId() {
        return this.subjectUniqueId;
    }

    public X509Extensions getExtensions() {
        return this.extensions;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.seq;
    }
}
