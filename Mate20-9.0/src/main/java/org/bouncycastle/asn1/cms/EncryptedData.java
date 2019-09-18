package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.BERTaggedObject;

public class EncryptedData extends ASN1Object {
    private EncryptedContentInfo encryptedContentInfo;
    private ASN1Set unprotectedAttrs;
    private ASN1Integer version;

    private EncryptedData(ASN1Sequence aSN1Sequence) {
        this.version = ASN1Integer.getInstance(aSN1Sequence.getObjectAt(0));
        this.encryptedContentInfo = EncryptedContentInfo.getInstance(aSN1Sequence.getObjectAt(1));
        if (aSN1Sequence.size() == 3) {
            this.unprotectedAttrs = ASN1Set.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(2), false);
        }
    }

    public EncryptedData(EncryptedContentInfo encryptedContentInfo2) {
        this(encryptedContentInfo2, null);
    }

    public EncryptedData(EncryptedContentInfo encryptedContentInfo2, ASN1Set aSN1Set) {
        this.version = new ASN1Integer(aSN1Set == null ? 0 : 2);
        this.encryptedContentInfo = encryptedContentInfo2;
        this.unprotectedAttrs = aSN1Set;
    }

    public static EncryptedData getInstance(Object obj) {
        if (obj instanceof EncryptedData) {
            return (EncryptedData) obj;
        }
        if (obj != null) {
            return new EncryptedData(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public EncryptedContentInfo getEncryptedContentInfo() {
        return this.encryptedContentInfo;
    }

    public ASN1Set getUnprotectedAttrs() {
        return this.unprotectedAttrs;
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.encryptedContentInfo);
        if (this.unprotectedAttrs != null) {
            aSN1EncodableVector.add(new BERTaggedObject(false, 1, this.unprotectedAttrs));
        }
        return new BERSequence(aSN1EncodableVector);
    }
}
