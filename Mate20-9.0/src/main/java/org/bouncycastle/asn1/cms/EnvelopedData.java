package org.bouncycastle.asn1.cms;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

public class EnvelopedData extends ASN1Object {
    private EncryptedContentInfo encryptedContentInfo;
    private OriginatorInfo originatorInfo;
    private ASN1Set recipientInfos;
    private ASN1Set unprotectedAttrs;
    private ASN1Integer version;

    public EnvelopedData(ASN1Sequence aSN1Sequence) {
        this.version = (ASN1Integer) aSN1Sequence.getObjectAt(0);
        ASN1Encodable objectAt = aSN1Sequence.getObjectAt(1);
        int i = 2;
        if (objectAt instanceof ASN1TaggedObject) {
            this.originatorInfo = OriginatorInfo.getInstance((ASN1TaggedObject) objectAt, false);
            i = 3;
            objectAt = aSN1Sequence.getObjectAt(2);
        }
        this.recipientInfos = ASN1Set.getInstance(objectAt);
        int i2 = i + 1;
        this.encryptedContentInfo = EncryptedContentInfo.getInstance(aSN1Sequence.getObjectAt(i));
        if (aSN1Sequence.size() > i2) {
            this.unprotectedAttrs = ASN1Set.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(i2), false);
        }
    }

    public EnvelopedData(OriginatorInfo originatorInfo2, ASN1Set aSN1Set, EncryptedContentInfo encryptedContentInfo2, ASN1Set aSN1Set2) {
        this.version = new ASN1Integer((long) calculateVersion(originatorInfo2, aSN1Set, aSN1Set2));
        this.originatorInfo = originatorInfo2;
        this.recipientInfos = aSN1Set;
        this.encryptedContentInfo = encryptedContentInfo2;
        this.unprotectedAttrs = aSN1Set2;
    }

    public EnvelopedData(OriginatorInfo originatorInfo2, ASN1Set aSN1Set, EncryptedContentInfo encryptedContentInfo2, Attributes attributes) {
        this.version = new ASN1Integer((long) calculateVersion(originatorInfo2, aSN1Set, ASN1Set.getInstance(attributes)));
        this.originatorInfo = originatorInfo2;
        this.recipientInfos = aSN1Set;
        this.encryptedContentInfo = encryptedContentInfo2;
        this.unprotectedAttrs = ASN1Set.getInstance(attributes);
    }

    public static int calculateVersion(OriginatorInfo originatorInfo2, ASN1Set aSN1Set, ASN1Set aSN1Set2) {
        int i = 2;
        if (originatorInfo2 == null) {
            if (aSN1Set2 != null) {
                return 2;
            }
            Enumeration objects = aSN1Set.getObjects();
            while (objects.hasMoreElements()) {
                if (RecipientInfo.getInstance(objects.nextElement()).getVersion().getValue().intValue() != 0) {
                    return 2;
                }
            }
            i = 0;
        }
        return i;
    }

    public static EnvelopedData getInstance(Object obj) {
        if (obj instanceof EnvelopedData) {
            return (EnvelopedData) obj;
        }
        if (obj != null) {
            return new EnvelopedData(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static EnvelopedData getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public EncryptedContentInfo getEncryptedContentInfo() {
        return this.encryptedContentInfo;
    }

    public OriginatorInfo getOriginatorInfo() {
        return this.originatorInfo;
    }

    public ASN1Set getRecipientInfos() {
        return this.recipientInfos;
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
        if (this.originatorInfo != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, this.originatorInfo));
        }
        aSN1EncodableVector.add(this.recipientInfos);
        aSN1EncodableVector.add(this.encryptedContentInfo);
        if (this.unprotectedAttrs != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, this.unprotectedAttrs));
        }
        return new BERSequence(aSN1EncodableVector);
    }
}
