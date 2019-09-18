package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.BERSequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class DigestedData extends ASN1Object {
    private ASN1OctetString digest;
    private AlgorithmIdentifier digestAlgorithm;
    private ContentInfo encapContentInfo;
    private ASN1Integer version;

    private DigestedData(ASN1Sequence aSN1Sequence) {
        this.version = (ASN1Integer) aSN1Sequence.getObjectAt(0);
        this.digestAlgorithm = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(1));
        this.encapContentInfo = ContentInfo.getInstance(aSN1Sequence.getObjectAt(2));
        this.digest = ASN1OctetString.getInstance(aSN1Sequence.getObjectAt(3));
    }

    public DigestedData(AlgorithmIdentifier algorithmIdentifier, ContentInfo contentInfo, byte[] bArr) {
        this.version = new ASN1Integer(0);
        this.digestAlgorithm = algorithmIdentifier;
        this.encapContentInfo = contentInfo;
        this.digest = new DEROctetString(bArr);
    }

    public static DigestedData getInstance(Object obj) {
        if (obj instanceof DigestedData) {
            return (DigestedData) obj;
        }
        if (obj != null) {
            return new DigestedData(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static DigestedData getInstance(ASN1TaggedObject aSN1TaggedObject, boolean z) {
        return getInstance(ASN1Sequence.getInstance(aSN1TaggedObject, z));
    }

    public byte[] getDigest() {
        return this.digest.getOctets();
    }

    public AlgorithmIdentifier getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    public ContentInfo getEncapContentInfo() {
        return this.encapContentInfo;
    }

    public ASN1Integer getVersion() {
        return this.version;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.version);
        aSN1EncodableVector.add(this.digestAlgorithm);
        aSN1EncodableVector.add(this.encapContentInfo);
        aSN1EncodableVector.add(this.digest);
        return new BERSequence(aSN1EncodableVector);
    }
}
