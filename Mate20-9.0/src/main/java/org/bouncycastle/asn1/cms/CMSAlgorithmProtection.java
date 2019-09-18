package org.bouncycastle.asn1.cms;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class CMSAlgorithmProtection extends ASN1Object {
    public static final int MAC = 2;
    public static final int SIGNATURE = 1;
    private final AlgorithmIdentifier digestAlgorithm;
    private final AlgorithmIdentifier macAlgorithm;
    private final AlgorithmIdentifier signatureAlgorithm;

    private CMSAlgorithmProtection(ASN1Sequence aSN1Sequence) {
        if (aSN1Sequence.size() == 2) {
            this.digestAlgorithm = AlgorithmIdentifier.getInstance(aSN1Sequence.getObjectAt(0));
            ASN1TaggedObject instance = ASN1TaggedObject.getInstance(aSN1Sequence.getObjectAt(1));
            if (instance.getTagNo() == 1) {
                this.signatureAlgorithm = AlgorithmIdentifier.getInstance(instance, false);
                this.macAlgorithm = null;
            } else if (instance.getTagNo() == 2) {
                this.signatureAlgorithm = null;
                this.macAlgorithm = AlgorithmIdentifier.getInstance(instance, false);
            } else {
                throw new IllegalArgumentException("Unknown tag found: " + instance.getTagNo());
            }
        } else {
            throw new IllegalArgumentException("Sequence wrong size: One of signatureAlgorithm or macAlgorithm must be present");
        }
    }

    public CMSAlgorithmProtection(AlgorithmIdentifier algorithmIdentifier, int i, AlgorithmIdentifier algorithmIdentifier2) {
        if (algorithmIdentifier == null || algorithmIdentifier2 == null) {
            throw new NullPointerException("AlgorithmIdentifiers cannot be null");
        }
        this.digestAlgorithm = algorithmIdentifier;
        if (i == 1) {
            this.signatureAlgorithm = algorithmIdentifier2;
            this.macAlgorithm = null;
        } else if (i == 2) {
            this.signatureAlgorithm = null;
            this.macAlgorithm = algorithmIdentifier2;
        } else {
            throw new IllegalArgumentException("Unknown type: " + i);
        }
    }

    public static CMSAlgorithmProtection getInstance(Object obj) {
        if (obj instanceof CMSAlgorithmProtection) {
            return (CMSAlgorithmProtection) obj;
        }
        if (obj != null) {
            return new CMSAlgorithmProtection(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public AlgorithmIdentifier getDigestAlgorithm() {
        return this.digestAlgorithm;
    }

    public AlgorithmIdentifier getMacAlgorithm() {
        return this.macAlgorithm;
    }

    public AlgorithmIdentifier getSignatureAlgorithm() {
        return this.signatureAlgorithm;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector();
        aSN1EncodableVector.add(this.digestAlgorithm);
        if (this.signatureAlgorithm != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 1, this.signatureAlgorithm));
        }
        if (this.macAlgorithm != null) {
            aSN1EncodableVector.add(new DERTaggedObject(false, 2, this.macAlgorithm));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
