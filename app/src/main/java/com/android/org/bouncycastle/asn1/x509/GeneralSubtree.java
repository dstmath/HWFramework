package com.android.org.bouncycastle.asn1.x509;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.ASN1TaggedObject;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.asn1.DERTaggedObject;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECFieldElement.F2m;
import com.android.org.bouncycastle.x509.ExtendedPKIXParameters;
import java.math.BigInteger;

public class GeneralSubtree extends ASN1Object {
    private static final BigInteger ZERO = null;
    private GeneralName base;
    private ASN1Integer maximum;
    private ASN1Integer minimum;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.x509.GeneralSubtree.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.x509.GeneralSubtree.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x509.GeneralSubtree.<clinit>():void");
    }

    private GeneralSubtree(ASN1Sequence seq) {
        this.base = GeneralName.getInstance(seq.getObjectAt(0));
        switch (seq.size()) {
            case ExtendedPKIXParameters.CHAIN_VALIDITY_MODEL /*1*/:
            case F2m.TPB /*2*/:
                ASN1TaggedObject o = ASN1TaggedObject.getInstance(seq.getObjectAt(1));
                switch (o.getTagNo()) {
                    case ECCurve.COORD_AFFINE /*0*/:
                        this.minimum = ASN1Integer.getInstance(o, false);
                    case ExtendedPKIXParameters.CHAIN_VALIDITY_MODEL /*1*/:
                        this.maximum = ASN1Integer.getInstance(o, false);
                    default:
                        throw new IllegalArgumentException("Bad tag number: " + o.getTagNo());
                }
            case F2m.PPB /*3*/:
                ASN1TaggedObject oMin = ASN1TaggedObject.getInstance(seq.getObjectAt(1));
                if (oMin.getTagNo() != 0) {
                    throw new IllegalArgumentException("Bad tag number for 'minimum': " + oMin.getTagNo());
                }
                this.minimum = ASN1Integer.getInstance(oMin, false);
                ASN1TaggedObject oMax = ASN1TaggedObject.getInstance(seq.getObjectAt(2));
                if (oMax.getTagNo() != 1) {
                    throw new IllegalArgumentException("Bad tag number for 'maximum': " + oMax.getTagNo());
                }
                this.maximum = ASN1Integer.getInstance(oMax, false);
            default:
                throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
    }

    public GeneralSubtree(GeneralName base, BigInteger minimum, BigInteger maximum) {
        this.base = base;
        if (maximum != null) {
            this.maximum = new ASN1Integer(maximum);
        }
        if (minimum == null) {
            this.minimum = null;
        } else {
            this.minimum = new ASN1Integer(minimum);
        }
    }

    public GeneralSubtree(GeneralName base) {
        this(base, null, null);
    }

    public static GeneralSubtree getInstance(ASN1TaggedObject o, boolean explicit) {
        return new GeneralSubtree(ASN1Sequence.getInstance(o, explicit));
    }

    public static GeneralSubtree getInstance(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof GeneralSubtree) {
            return (GeneralSubtree) obj;
        }
        return new GeneralSubtree(ASN1Sequence.getInstance(obj));
    }

    public GeneralName getBase() {
        return this.base;
    }

    public BigInteger getMinimum() {
        if (this.minimum == null) {
            return ZERO;
        }
        return this.minimum.getValue();
    }

    public BigInteger getMaximum() {
        if (this.maximum == null) {
            return null;
        }
        return this.maximum.getValue();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.base);
        if (!(this.minimum == null || this.minimum.getValue().equals(ZERO))) {
            v.add(new DERTaggedObject(false, 0, this.minimum));
        }
        if (this.maximum != null) {
            v.add(new DERTaggedObject(false, 1, this.maximum));
        }
        return new DERSequence(v);
    }
}
