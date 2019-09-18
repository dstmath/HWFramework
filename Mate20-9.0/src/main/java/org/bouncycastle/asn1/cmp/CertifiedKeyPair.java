package org.bouncycastle.asn1.cmp;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.crmf.EncryptedValue;
import org.bouncycastle.asn1.crmf.PKIPublicationInfo;

public class CertifiedKeyPair extends ASN1Object {
    private CertOrEncCert certOrEncCert;
    private EncryptedValue privateKey;
    private PKIPublicationInfo publicationInfo;

    private CertifiedKeyPair(ASN1Sequence aSN1Sequence) {
        Object obj;
        this.certOrEncCert = CertOrEncCert.getInstance(aSN1Sequence.getObjectAt(0));
        if (aSN1Sequence.size() >= 2) {
            if (aSN1Sequence.size() == 2) {
                ASN1TaggedObject instance = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(1));
                if (instance.getTagNo() == 0) {
                    this.privateKey = EncryptedValue.getInstance(instance.getObject());
                    return;
                }
                obj = instance.getObject();
            } else {
                this.privateKey = EncryptedValue.getInstance(ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(1)));
                obj = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(2));
            }
            this.publicationInfo = PKIPublicationInfo.getInstance(obj);
        }
    }

    public CertifiedKeyPair(CertOrEncCert certOrEncCert2) {
        this(certOrEncCert2, null, null);
    }

    public CertifiedKeyPair(CertOrEncCert certOrEncCert2, EncryptedValue encryptedValue, PKIPublicationInfo pKIPublicationInfo) {
        if (certOrEncCert2 != null) {
            this.certOrEncCert = certOrEncCert2;
            this.privateKey = encryptedValue;
            this.publicationInfo = pKIPublicationInfo;
            return;
        }
        throw new IllegalArgumentException("'certOrEncCert' cannot be null");
    }

    public static CertifiedKeyPair getInstance(Object obj) {
        if (obj instanceof CertifiedKeyPair) {
            return (CertifiedKeyPair) obj;
        }
        if (obj != null) {
            return new CertifiedKeyPair(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public CertOrEncCert getCertOrEncCert() {
        return this.certOrEncCert;
    }

    public EncryptedValue getPrivateKey() {
        return this.privateKey;
    }

    public PKIPublicationInfo getPublicationInfo() {
        return this.publicationInfo;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.certOrEncCert);
        if (this.privateKey != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, this.privateKey));
        }
        if (this.publicationInfo != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 1, this.publicationInfo));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
