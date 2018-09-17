package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x500.X500Name;

public class X509CertificateStructure extends ASN1Object implements X509ObjectIdentifiers, PKCSObjectIdentifiers {
    ASN1Sequence seq;
    DERBitString sig;
    AlgorithmIdentifier sigAlgId;
    TBSCertificateStructure tbsCert;

    public static X509CertificateStructure getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static X509CertificateStructure getInstance(Object obj) {
        if (obj instanceof X509CertificateStructure) {
            return (X509CertificateStructure) obj;
        }
        if (obj != null) {
            return new X509CertificateStructure(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public X509CertificateStructure(ASN1Sequence seq) {
        this.seq = seq;
        if (seq.size() == 3) {
            this.tbsCert = TBSCertificateStructure.getInstance(seq.getObjectAt(0));
            this.sigAlgId = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
            this.sig = DERBitString.getInstance(seq.getObjectAt(2));
            return;
        }
        throw new IllegalArgumentException("sequence wrong size for a certificate");
    }

    public TBSCertificateStructure getTBSCertificate() {
        return this.tbsCert;
    }

    public int getVersion() {
        return this.tbsCert.getVersion();
    }

    public ASN1Integer getSerialNumber() {
        return this.tbsCert.getSerialNumber();
    }

    public X500Name getIssuer() {
        return this.tbsCert.getIssuer();
    }

    public Time getStartDate() {
        return this.tbsCert.getStartDate();
    }

    public Time getEndDate() {
        return this.tbsCert.getEndDate();
    }

    public X500Name getSubject() {
        return this.tbsCert.getSubject();
    }

    public SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
        return this.tbsCert.getSubjectPublicKeyInfo();
    }

    public AlgorithmIdentifier getSignatureAlgorithm() {
        return this.sigAlgId;
    }

    public DERBitString getSignature() {
        return this.sig;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.seq;
    }
}
