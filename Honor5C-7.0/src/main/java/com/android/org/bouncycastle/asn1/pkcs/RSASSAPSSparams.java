package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECFieldElement.F2m;
import com.android.org.bouncycastle.x509.ExtendedPKIXParameters;
import java.math.BigInteger;

public class RSASSAPSSparams extends ASN1Object {
    public static final AlgorithmIdentifier DEFAULT_HASH_ALGORITHM = null;
    public static final AlgorithmIdentifier DEFAULT_MASK_GEN_FUNCTION = null;
    public static final ASN1Integer DEFAULT_SALT_LENGTH = null;
    public static final ASN1Integer DEFAULT_TRAILER_FIELD = null;
    private AlgorithmIdentifier hashAlgorithm;
    private AlgorithmIdentifier maskGenAlgorithm;
    private ASN1Integer saltLength;
    private ASN1Integer trailerField;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.pkcs.RSASSAPSSparams.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.pkcs.RSASSAPSSparams.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.pkcs.RSASSAPSSparams.<clinit>():void");
    }

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
                case ECCurve.COORD_AFFINE /*0*/:
                    this.hashAlgorithm = AlgorithmIdentifier.getInstance(o, true);
                    break;
                case ExtendedPKIXParameters.CHAIN_VALIDITY_MODEL /*1*/:
                    this.maskGenAlgorithm = AlgorithmIdentifier.getInstance(o, true);
                    break;
                case F2m.TPB /*2*/:
                    this.saltLength = ASN1Integer.getInstance(o, true);
                    break;
                case F2m.PPB /*3*/:
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
