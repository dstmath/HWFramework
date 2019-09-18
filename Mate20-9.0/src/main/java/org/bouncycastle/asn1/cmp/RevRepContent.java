package org.bouncycastle.asn1.cmp;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.crmf.CertId;
import org.bouncycastle.asn1.x509.CertificateList;

public class RevRepContent extends ASN1Object {
    private ASN1Sequence crls;
    private ASN1Sequence revCerts;
    private ASN1Sequence status;

    private RevRepContent(ASN1Sequence aSN1Sequence) {
        Enumeration objects = aSN1Sequence.getObjects();
        this.status = ASN1Sequence.getInstance(objects.nextElement());
        while (objects.hasMoreElements()) {
            ASN1TaggedObject instance = ASN1TaggedObject.getInstance(objects.nextElement());
            if (instance.getTagNo() == 0) {
                this.revCerts = ASN1Sequence.getInstance(instance, true);
            } else {
                this.crls = ASN1Sequence.getInstance(instance, true);
            }
        }
    }

    private void addOptional(ASN1EncodableVector aSN1EncodableVector, int i, ASN1Encodable aSN1Encodable) {
        if (aSN1Encodable != null) {
            aSN1EncodableVector.add(new DERTaggedObject(true, i, aSN1Encodable));
        }
    }

    public static RevRepContent getInstance(Object obj) {
        if (obj instanceof RevRepContent) {
            return (RevRepContent) obj;
        }
        if (obj != null) {
            return new RevRepContent(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public CertificateList[] getCrls() {
        if (this.crls == null) {
            return null;
        }
        CertificateList[] certificateListArr = new CertificateList[this.crls.size()];
        for (int i = 0; i != certificateListArr.length; i++) {
            certificateListArr[i] = CertificateList.getInstance(this.crls.getObjectAt(i));
        }
        return certificateListArr;
    }

    public CertId[] getRevCerts() {
        if (this.revCerts == null) {
            return null;
        }
        CertId[] certIdArr = new CertId[this.revCerts.size()];
        for (int i = 0; i != certIdArr.length; i++) {
            certIdArr[i] = CertId.getInstance(this.revCerts.getObjectAt(i));
        }
        return certIdArr;
    }

    public PKIStatusInfo[] getStatus() {
        PKIStatusInfo[] pKIStatusInfoArr = new PKIStatusInfo[this.status.size()];
        for (int i = 0; i != pKIStatusInfoArr.length; i++) {
            pKIStatusInfoArr[i] = PKIStatusInfo.getInstance(this.status.getObjectAt(i));
        }
        return pKIStatusInfoArr;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.status);
        addOptional(aSN1EncodableVector, 0, this.revCerts);
        addOptional(aSN1EncodableVector, 1, this.crls);
        return new DERSequence(aSN1EncodableVector);
    }
}
