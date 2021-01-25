package org.bouncycastle.asn1.cms;

import java.util.Enumeration;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class AuthenticatedData extends ASN1Object {
    private ASN1Set authAttrs;
    private AlgorithmIdentifier digestAlgorithm;
    private ContentInfo encapsulatedContentInfo;
    private ASN1OctetString mac;
    private AlgorithmIdentifier macAlgorithm;
    private OriginatorInfo originatorInfo;
    private ASN1Set recipientInfos;
    private ASN1Set unauthAttrs;
    private ASN1Integer version;

    private AuthenticatedData(ASN1Sequence aSN1Sequence) {
        int i;
        ASN1Encodable aSN1Encodable;
        this.version = (ASN1Integer) aSN1Sequence.getObjectAt(0);
        ASN1Encodable objectAt = aSN1Sequence.getObjectAt(1);
        int i2 = 2;
        if (objectAt instanceof ASN1TaggedObject) {
            this.originatorInfo = OriginatorInfo.getInstance((ASN1TaggedObject) objectAt, false);
            i2 = 3;
            objectAt = aSN1Sequence.getObjectAt(2);
        }
        this.recipientInfos = ASN1Set.getInstance(objectAt);
        int i3 = i2 + 1;
        this.macAlgorithm = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(i2));
        int i4 = i3 + 1;
        ASN1Encodable objectAt2 = aSN1Sequence.getObjectAt(i3);
        if (objectAt2 instanceof ASN1TaggedObject) {
            this.digestAlgorithm = AlgorithmIdentifier.getInstance((ASN1TaggedObject) objectAt2, false);
            int i5 = i4 + 1;
            ASN1Encodable objectAt3 = aSN1Sequence.getObjectAt(i4);
            i4 = i5;
            objectAt2 = objectAt3;
        }
        this.encapsulatedContentInfo = ContentInfo.getInstance(objectAt2);
        int i6 = i4 + 1;
        ASN1Encodable objectAt4 = aSN1Sequence.getObjectAt(i4);
        if (objectAt4 instanceof ASN1TaggedObject) {
            this.authAttrs = ASN1Set.getInstance((ASN1TaggedObject) objectAt4, false);
            i = i6 + 1;
            aSN1Encodable = aSN1Sequence.getObjectAt(i6);
        } else {
            i = i6;
            aSN1Encodable = objectAt4;
        }
        this.mac = ASN1OctetString.getInstance(aSN1Encodable);
        if (aSN1Sequence.size() > i) {
            this.unauthAttrs = ASN1Set.getInstance((ASN1TaggedObject) aSN1Sequence.getObjectAt(i), false);
        }
    }

    public AuthenticatedData(OriginatorInfo originatorInfo2, ASN1Set aSN1Set, AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, ContentInfo contentInfo, ASN1Set aSN1Set2, ASN1OctetString aSN1OctetString, ASN1Set aSN1Set3) {
        if (!(algorithmIdentifier2 == null && aSN1Set2 == null) && (algorithmIdentifier2 == null || aSN1Set2 == null)) {
            throw new IllegalArgumentException("digestAlgorithm and authAttrs must be set together");
        }
        this.version = new ASN1Integer((long) calculateVersion(originatorInfo2));
        this.originatorInfo = originatorInfo2;
        this.macAlgorithm = algorithmIdentifier;
        this.digestAlgorithm = algorithmIdentifier2;
        this.recipientInfos = aSN1Set;
        this.encapsulatedContentInfo = contentInfo;
        this.authAttrs = aSN1Set2;
        this.mac = aSN1OctetString;
        this.unauthAttrs = aSN1Set3;
    }

    public static int calculateVersion(OriginatorInfo originatorInfo2) {
        int i = 0;
        if (originatorInfo2 == null) {
            return 0;
        }
        Enumeration objects = originatorInfo2.getCertificates().getObjects();
        while (true) {
            if (!objects.hasMoreElements()) {
                break;
            }
            Object nextElement = objects.nextElement();
            if (nextElement instanceof ASN1TaggedObject) {
                ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) nextElement;
                if (aSN1TaggedObject.getTagNo() == 2) {
                    i = 1;
                } else if (aSN1TaggedObject.getTagNo() == 3) {
                    i = 3;
                    break;
                }
            }
        }
        if (originatorInfo2.getCRLs() == null) {
            return i;
        }
        Enumeration objects2 = originatorInfo2.getCRLs().getObjects();
        while (objects2.hasMoreElements()) {
            Object nextElement2 = objects2.nextElement();
            if ((nextElement2 instanceof ASN1TaggedObject) && ((ASN1TaggedObject) nextElement2).getTagNo() == 1) {
                return 3;
            }
        }
        return i;
    }

    public static AuthenticatedData getInstance(Object obj) {
        if (obj instanceof AuthenticatedData) {
            return (AuthenticatedData) obj;
        }
        if (obj != null) {
            return new AuthenticatedData(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static AuthenticatedData getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public ASN1Set getAuthAttrs() {
        return this.authAttrs;
    }

    public AlgorithmIdentifier getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    public ContentInfo getEncapsulatedContentInfo() {
        return this.encapsulatedContentInfo;
    }

    public ASN1OctetString getMac() {
        return this.mac;
    }

    public AlgorithmIdentifier getMacAlgorithm() {
        return this.macAlgorithm;
    }

    public OriginatorInfo getOriginatorInfo() {
        return this.originatorInfo;
    }

    public ASN1Set getRecipientInfos() {
        return this.recipientInfos;
    }

    public ASN1Set getUnauthAttrs() {
        return this.unauthAttrs;
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(9);
        aSN1EncodableVector.add(this.version);
        OriginatorInfo originatorInfo2 = this.originatorInfo;
        if (originatorInfo2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, originatorInfo2));
        }
        aSN1EncodableVector.add(this.recipientInfos);
        aSN1EncodableVector.add(this.macAlgorithm);
        AlgorithmIdentifier algorithmIdentifier = this.digestAlgorithm;
        if (algorithmIdentifier != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, algorithmIdentifier));
        }
        aSN1EncodableVector.add(this.encapsulatedContentInfo);
        ASN1Set aSN1Set = this.authAttrs;
        if (aSN1Set != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 2, aSN1Set));
        }
        aSN1EncodableVector.add(this.mac);
        ASN1Set aSN1Set2 = this.unauthAttrs;
        if (aSN1Set2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 3, aSN1Set2));
        }
        return new BERSequence(aSN1EncodableVector);
    }
}
