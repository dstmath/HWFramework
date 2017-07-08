package com.android.org.bouncycastle.asn1.pkcs;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
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

public class RSAESOAEPparams extends ASN1Object {
    public static final AlgorithmIdentifier DEFAULT_HASH_ALGORITHM = null;
    public static final AlgorithmIdentifier DEFAULT_MASK_GEN_FUNCTION = null;
    public static final AlgorithmIdentifier DEFAULT_P_SOURCE_ALGORITHM = null;
    private AlgorithmIdentifier hashAlgorithm;
    private AlgorithmIdentifier maskGenAlgorithm;
    private AlgorithmIdentifier pSourceAlgorithm;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.pkcs.RSAESOAEPparams.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.pkcs.RSAESOAEPparams.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.pkcs.RSAESOAEPparams.<clinit>():void");
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

    public RSAESOAEPparams() {
        this.hashAlgorithm = DEFAULT_HASH_ALGORITHM;
        this.maskGenAlgorithm = DEFAULT_MASK_GEN_FUNCTION;
        this.pSourceAlgorithm = DEFAULT_P_SOURCE_ALGORITHM;
    }

    public RSAESOAEPparams(AlgorithmIdentifier hashAlgorithm, AlgorithmIdentifier maskGenAlgorithm, AlgorithmIdentifier pSourceAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
        this.maskGenAlgorithm = maskGenAlgorithm;
        this.pSourceAlgorithm = pSourceAlgorithm;
    }

    public RSAESOAEPparams(ASN1Sequence seq) {
        this.hashAlgorithm = DEFAULT_HASH_ALGORITHM;
        this.maskGenAlgorithm = DEFAULT_MASK_GEN_FUNCTION;
        this.pSourceAlgorithm = DEFAULT_P_SOURCE_ALGORITHM;
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
                    this.pSourceAlgorithm = AlgorithmIdentifier.getInstance(o, true);
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

    public AlgorithmIdentifier getPSourceAlgorithm() {
        return this.pSourceAlgorithm;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        if (!this.hashAlgorithm.equals(DEFAULT_HASH_ALGORITHM)) {
            v.add(new DERTaggedObject(true, 0, this.hashAlgorithm));
        }
        if (!this.maskGenAlgorithm.equals(DEFAULT_MASK_GEN_FUNCTION)) {
            v.add(new DERTaggedObject(true, 1, this.maskGenAlgorithm));
        }
        if (!this.pSourceAlgorithm.equals(DEFAULT_P_SOURCE_ALGORITHM)) {
            v.add(new DERTaggedObject(true, 2, this.pSourceAlgorithm));
        }
        return new DERSequence(v);
    }
}
