package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Enumerated;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERBitString;
import com.android.org.bouncycastle.asn1.DERSequence;

public class ObjectDigestInfo extends ASN1Object {
    public static final int otherObjectDigest = 2;
    public static final int publicKey = 0;
    public static final int publicKeyCert = 1;
    AlgorithmIdentifier digestAlgorithm;
    ASN1Enumerated digestedObjectType;
    DERBitString objectDigest;
    ASN1ObjectIdentifier otherObjectTypeID;

    public static ObjectDigestInfo getInstance(Object obj) {
        if (obj instanceof ObjectDigestInfo) {
            return (ObjectDigestInfo) obj;
        }
        if (obj != null) {
            return new ObjectDigestInfo(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public static ObjectDigestInfo getInstance(ASN1TaggedObject obj, boolean explicit) {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public ObjectDigestInfo(int digestedObjectType2, ASN1ObjectIdentifier otherObjectTypeID2, AlgorithmIdentifier digestAlgorithm2, byte[] objectDigest2) {
        this.digestedObjectType = new ASN1Enumerated(digestedObjectType2);
        if (digestedObjectType2 == 2) {
            this.otherObjectTypeID = otherObjectTypeID2;
        }
        this.digestAlgorithm = digestAlgorithm2;
        this.objectDigest = new DERBitString(objectDigest2);
    }

    private ObjectDigestInfo(ASN1Sequence seq) {
        if (seq.size() > 4 || seq.size() < 3) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        this.digestedObjectType = ASN1Enumerated.getInstance(seq.getObjectAt(0));
        int offset = 0;
        if (seq.size() == 4) {
            this.otherObjectTypeID = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(1));
            offset = 0 + 1;
        }
        this.digestAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1 + offset));
        this.objectDigest = DERBitString.getInstance(seq.getObjectAt(2 + offset));
    }

    public ASN1Enumerated getDigestedObjectType() {
        return this.digestedObjectType;
    }

    public ASN1ObjectIdentifier getOtherObjectTypeID() {
        return this.otherObjectTypeID;
    }

    public AlgorithmIdentifier getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    public DERBitString getObjectDigest() {
        return this.objectDigest;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.digestedObjectType);
        if (this.otherObjectTypeID != null) {
            v.add(this.otherObjectTypeID);
        }
        v.add(this.digestAlgorithm);
        v.add(this.objectDigest);
        return new DERSequence(v);
    }
}
