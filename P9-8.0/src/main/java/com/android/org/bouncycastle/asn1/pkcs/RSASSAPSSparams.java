package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import java.math.BigInteger;

public class RSASSAPSSparams extends ASN1Object {
    public static final AlgorithmIdentifier DEFAULT_HASH_ALGORITHM = new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE);
    public static final AlgorithmIdentifier DEFAULT_MASK_GEN_FUNCTION = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, DEFAULT_HASH_ALGORITHM);
    public static final ASN1Integer DEFAULT_SALT_LENGTH = new ASN1Integer(20);
    public static final ASN1Integer DEFAULT_TRAILER_FIELD = new ASN1Integer(1);
    private AlgorithmIdentifier hashAlgorithm;
    private AlgorithmIdentifier maskGenAlgorithm;
    private ASN1Integer saltLength;
    private ASN1Integer trailerField;

    public static RSASSAPSSparams getInstance(Object obj) {
        if (obj instanceof RSASSAPSSparams) {
            return (RSASSAPSSparams) obj;
        }
        if (obj != null) {
            return new RSASSAPSSparams(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public RSASSAPSSparams() {
        this.hashAlgorithm = DEFAULT_HASH_ALGORITHM;
        this.maskGenAlgorithm = DEFAULT_MASK_GEN_FUNCTION;
        this.saltLength = DEFAULT_SALT_LENGTH;
        this.trailerField = DEFAULT_TRAILER_FIELD;
    }

    public RSASSAPSSparams(AlgorithmIdentifier hashAlgorithm, AlgorithmIdentifier maskGenAlgorithm, ASN1Integer saltLength, ASN1Integer trailerField) {
        this.hashAlgorithm = hashAlgorithm;
        this.maskGenAlgorithm = maskGenAlgorithm;
        this.saltLength = saltLength;
        this.trailerField = trailerField;
    }

    private RSASSAPSSparams(ASN1Sequence seq) {
        this.hashAlgorithm = DEFAULT_HASH_ALGORITHM;
        this.maskGenAlgorithm = DEFAULT_MASK_GEN_FUNCTION;
        this.saltLength = DEFAULT_SALT_LENGTH;
        this.trailerField = DEFAULT_TRAILER_FIELD;
        for (int i = 0; i != seq.size(); i++) {
            ASN1TaggedObject o = (ASN1TaggedObject) seq.getObjectAt(i);
            switch (o.getTagNo()) {
                case 0:
                    this.hashAlgorithm = AlgorithmIdentifier.getInstance(o, true);
                    break;
                case 1:
                    this.maskGenAlgorithm = AlgorithmIdentifier.getInstance(o, true);
                    break;
                case 2:
                    this.saltLength = ASN1Integer.getInstance(o, true);
                    break;
                case 3:
                    this.trailerField = ASN1Integer.getInstance(o, true);
                    break;
                default:
                    throw new IllegalArgumentException("unknown tag");
            }
        }
    }

    public AlgorithmIdentifier getHashAlgorithm() {
        return this.hashAlgorithm;
    }

    public AlgorithmIdentifier getMaskGenAlgorithm() {
        return this.maskGenAlgorithm;
    }

    public BigInteger getSaltLength() {
        return this.saltLength.getValue();
    }

    public BigInteger getTrailerField() {
        return this.trailerField.getValue();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        if (!this.hashAlgorithm.equals(DEFAULT_HASH_ALGORITHM)) {
            v.add(new DERTaggedObject(true, 0, this.hashAlgorithm));
        }
        if (!this.maskGenAlgorithm.equals(DEFAULT_MASK_GEN_FUNCTION)) {
            v.add(new DERTaggedObject(true, 1, this.maskGenAlgorithm));
        }
        if (!this.saltLength.equals(DEFAULT_SALT_LENGTH)) {
            v.add(new DERTaggedObject(true, 2, this.saltLength));
        }
        if (!this.trailerField.equals(DEFAULT_TRAILER_FIELD)) {
            v.add(new DERTaggedObject(true, 3, this.trailerField));
        }
        return new DERSequence(v);
    }
}
