package org.bouncycastle.asn1.tsp;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cms.Attributes;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class ArchiveTimeStamp extends ASN1Object {
    private Attributes attributes;
    private AlgorithmIdentifier digestAlgorithm;
    private ASN1Sequence reducedHashTree;
    private ContentInfo timeStamp;

    private ArchiveTimeStamp(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() < 1 || aSN1Sequence.size() > 4) {
            throw new IllegalArgumentException("wrong sequence size in constructor: " + aSN1Sequence.size());
        }
        for (int i = 0; i < aSN1Sequence.size() - 1; i++) {
            ASN1Encodable objectAt = aSN1Sequence.getObjectAt(i);
            if (objectAt instanceof ASN1TaggedObject) {
                ASN1TaggedObject instance = ASN1TaggedObject.getInstance(objectAt);
                int tagNo = instance.getTagNo();
                if (tagNo == 0) {
                    this.digestAlgorithm = AlgorithmIdentifier.getInstance(instance, false);
                } else if (tagNo == 1) {
                    this.attributes = Attributes.getInstance(instance, false);
                } else if (tagNo == 2) {
                    this.reducedHashTree = ASN1Sequence.getInstance(instance, false);
                } else {
                    throw new IllegalArgumentException("invalid tag no in constructor: " + instance.getTagNo());
                }
            }
        }
        this.timeStamp = ContentInfo.getInstance(aSN1Sequence.getObjectAt(aSN1Sequence.size() - 1));
    }

    public ArchiveTimeStamp(ContentInfo contentInfo) {
        this.timeStamp = contentInfo;
    }

    public ArchiveTimeStamp(AlgorithmIdentifier algorithmIdentifier, Attributes attributes2, PartialHashtree[] partialHashtreeArr, ContentInfo contentInfo) {
        this.digestAlgorithm = algorithmIdentifier;
        this.attributes = attributes2;
        this.reducedHashTree = new DERSequence(partialHashtreeArr);
        this.timeStamp = contentInfo;
    }

    public ArchiveTimeStamp(AlgorithmIdentifier algorithmIdentifier, PartialHashtree[] partialHashtreeArr, ContentInfo contentInfo) {
        this.digestAlgorithm = algorithmIdentifier;
        this.reducedHashTree = new DERSequence(partialHashtreeArr);
        this.timeStamp = contentInfo;
    }

    public static ArchiveTimeStamp getInstance(Object obj) {
        if (obj instanceof ArchiveTimeStamp) {
            return (ArchiveTimeStamp) obj;
        }
        if (obj != null) {
            return new ArchiveTimeStamp(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public AlgorithmIdentifier getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    public AlgorithmIdentifier getDigestAlgorithmIdentifier() {
        AlgorithmIdentifier algorithmIdentifier = this.digestAlgorithm;
        if (algorithmIdentifier != null) {
            return algorithmIdentifier;
        }
        if (this.timeStamp.getContentType().equals((ASN1Primitive) CMSObjectIdentifiers.signedData)) {
            SignedData instance = SignedData.getInstance(this.timeStamp.getContent());
            if (instance.getEncapContentInfo().getContentType().equals((ASN1Primitive) PKCSObjectIdentifiers.id_ct_TSTInfo)) {
                return TSTInfo.getInstance(instance.getEncapContentInfo()).getMessageImprint().getHashAlgorithm();
            }
            throw new IllegalStateException("cannot parse time stamp");
        }
        throw new IllegalStateException("cannot identify algorithm identifier for digest");
    }

    public PartialHashtree[] getReducedHashTree() {
        ASN1Sequence aSN1Sequence = this.reducedHashTree;
        if (aSN1Sequence == null) {
            return null;
        }
        PartialHashtree[] partialHashtreeArr = new PartialHashtree[aSN1Sequence.size()];
        for (int i = 0; i != partialHashtreeArr.length; i++) {
            partialHashtreeArr[i] = PartialHashtree.getInstance(this.reducedHashTree.getObjectAt(i));
        }
        return partialHashtreeArr;
    }

    public ContentInfo getTimeStamp() {
        return this.timeStamp;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(4);
        AlgorithmIdentifier algorithmIdentifier = this.digestAlgorithm;
        if (algorithmIdentifier != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 0, algorithmIdentifier));
        }
        Attributes attributes2 = this.attributes;
        if (attributes2 != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, attributes2));
        }
        ASN1Sequence aSN1Sequence = this.reducedHashTree;
        if (aSN1Sequence != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 2, aSN1Sequence));
        }
        aSN1EncodableVector.add(this.timeStamp);
        return new DERSequence(aSN1EncodableVector);
    }
}
