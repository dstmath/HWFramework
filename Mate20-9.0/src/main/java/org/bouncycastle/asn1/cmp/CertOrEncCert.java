package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.crmf.EncryptedValue;

public class CertOrEncCert extends ASN1Object implements ASN1Choice {
    private CMPCertificate certificate;
    private EncryptedValue encryptedCert;

    private CertOrEncCert(ASN1TaggedObject aSN1TaggedObject) {
        if (aSN1TaggedObject.getTagNo() == 0) {
            this.certificate = CMPCertificate.getInstance(aSN1TaggedObject.getObject());
        } else if (aSN1TaggedObject.getTagNo() == 1) {
            this.encryptedCert = EncryptedValue.getInstance(aSN1TaggedObject.getObject());
        } else {
            throw new IllegalArgumentException("unknown tag: " + aSN1TaggedObject.getTagNo());
        }
    }

    public CertOrEncCert(CMPCertificate cMPCertificate) {
        if (cMPCertificate != null) {
            this.certificate = cMPCertificate;
            return;
        }
        throw new IllegalArgumentException("'certificate' cannot be null");
    }

    public CertOrEncCert(EncryptedValue encryptedValue) {
        if (encryptedValue != null) {
            this.encryptedCert = encryptedValue;
            return;
        }
        throw new IllegalArgumentException("'encryptedCert' cannot be null");
    }

    public static CertOrEncCert getInstance(Object obj) {
        if (obj instanceof CertOrEncCert) {
            return (CertOrEncCert) obj;
        }
        if (obj instanceof ASN1TaggedObject) {
            return new CertOrEncCert((ASN1TaggedObject) obj);
        }
        return null;
    }

    public CMPCertificate getCertificate() {
        return this.certificate;
    }

    public EncryptedValue getEncryptedCert() {
        return this.encryptedCert;
    }

    public ASN1Primitive toASN1Primitive() {
        return this.certificate != null ? new DERTaggedObject(true, 0, this.certificate) : new DERTaggedObject(true, 1, this.encryptedCert);
    }
}
