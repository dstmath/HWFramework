package org.bouncycastle.asn1.pkcs;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class RSAESOAEPparams extends ASN1Object {
    public static final AlgorithmIdentifier DEFAULT_HASH_ALGORITHM = new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE);
    public static final AlgorithmIdentifier DEFAULT_MASK_GEN_FUNCTION = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, DEFAULT_HASH_ALGORITHM);
    public static final AlgorithmIdentifier DEFAULT_P_SOURCE_ALGORITHM = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_pSpecified, new DEROctetString(new byte[0]));
    private AlgorithmIdentifier hashAlgorithm;
    private AlgorithmIdentifier maskGenAlgorithm;
    private AlgorithmIdentifier pSourceAlgorithm;

    public RSAESOAEPparams() {
        this.hashAlgorithm = DEFAULT_HASH_ALGORITHM;
        this.maskGenAlgorithm = DEFAULT_MASK_GEN_FUNCTION;
        this.pSourceAlgorithm = DEFAULT_P_SOURCE_ALGORITHM;
    }

    public RSAESOAEPparams(ASN1Sequence aSN1Sequence) {
        this.hashAlgorithm = DEFAULT_HASH_ALGORITHM;
        this.maskGenAlgorithm = DEFAULT_MASK_GEN_FUNCTION;
        this.pSourceAlgorithm = DEFAULT_P_SOURCE_ALGORITHM;
        for (int i = 0; i != aSN1Sequence.size(); i++) {
            ASN1TaggedObject aSN1TaggedObject = (ASN1TaggedObject) aSN1Sequence.getObjectAt(i);
            int tagNo = aSN1TaggedObject.getTagNo();
            if (tagNo == 0) {
                this.hashAlgorithm = AlgorithmIdentifier.getInstance(aSN1TaggedObject, true);
            } else if (tagNo == 1) {
                this.maskGenAlgorithm = AlgorithmIdentifier.getInstance(aSN1TaggedObject, true);
            } else if (tagNo == 2) {
                this.pSourceAlgorithm = AlgorithmIdentifier.getInstance(aSN1TaggedObject, true);
            } else {
                throw new IllegalArgumentException("unknown tag");
            }
        }
    }

    public RSAESOAEPparams(AlgorithmIdentifier algorithmIdentifier, AlgorithmIdentifier algorithmIdentifier2, AlgorithmIdentifier algorithmIdentifier3) {
        this.hashAlgorithm = algorithmIdentifier;
        this.maskGenAlgorithm = algorithmIdentifier2;
        this.pSourceAlgorithm = algorithmIdentifier3;
    }

    public static RSAESOAEPparams getInstance(Object obj) {
        if (obj instanceof RSAESOAEPparams) {
            return (RSAESOAEPparams) obj;
        }
        if (obj != null) {
            return new RSAESOAEPparams(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public AlgorithmIdentifier getHashAlgorithm() {
        return this.hashAlgorithm;
    }

    public AlgorithmIdentifier getMaskGenAlgorithm() {
        return this.maskGenAlgorithm;
    }

    public AlgorithmIdentifier getPSourceAlgorithm() {
        return this.pSourceAlgorithm;
    }

    @Override // org.bouncycastle.asn1.ASN1Object, org.bouncycastle.asn1.ASN1Encodable
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector aSN1EncodableVector = new ASN1EncodableVector(3);
        if (!this.hashAlgorithm.equals(DEFAULT_HASH_ALGORITHM)) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 0, this.hashAlgorithm));
        }
        if (!this.maskGenAlgorithm.equals(DEFAULT_MASK_GEN_FUNCTION)) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 1, this.maskGenAlgorithm));
        }
        if (!this.pSourceAlgorithm.equals(DEFAULT_P_SOURCE_ALGORITHM)) {
            aSN1EncodableVector.add(new DERTaggedObject(true, 2, this.pSourceAlgorithm));
        }
        return new DERSequence(aSN1EncodableVector);
    }
}
