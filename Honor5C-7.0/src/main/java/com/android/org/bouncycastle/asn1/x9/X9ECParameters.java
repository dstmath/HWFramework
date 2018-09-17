package com.android.org.bouncycastle.asn1.x9;

import com.android.org.bouncycastle.asn1.ASN1EncodableVector;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1Object;
import com.android.org.bouncycastle.asn1.ASN1OctetString;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.ASN1Sequence;
import com.android.org.bouncycastle.asn1.DERSequence;
import com.android.org.bouncycastle.math.ec.ECAlgorithms;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECPoint;
import com.android.org.bouncycastle.math.field.PolynomialExtensionField;
import java.math.BigInteger;

public class X9ECParameters extends ASN1Object implements X9ObjectIdentifiers {
    private static final BigInteger ONE = null;
    private ECCurve curve;
    private X9FieldID fieldID;
    private X9ECPoint g;
    private BigInteger h;
    private BigInteger n;
    private byte[] seed;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.x9.X9ECParameters.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.x9.X9ECParameters.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.x9.X9ECParameters.<clinit>():void");
    }

    private X9ECParameters(ASN1Sequence seq) {
        if ((seq.getObjectAt(0) instanceof ASN1Integer) && ((ASN1Integer) seq.getObjectAt(0)).getValue().equals(ONE)) {
            X9Curve x9c = new X9Curve(X9FieldID.getInstance(seq.getObjectAt(1)), ASN1Sequence.getInstance(seq.getObjectAt(2)));
            this.curve = x9c.getCurve();
            Object p = seq.getObjectAt(3);
            if (p instanceof X9ECPoint) {
                this.g = (X9ECPoint) p;
            } else {
                this.g = new X9ECPoint(this.curve, (ASN1OctetString) p);
            }
            this.n = ((ASN1Integer) seq.getObjectAt(4)).getValue();
            this.seed = x9c.getSeed();
            if (seq.size() == 6) {
                this.h = ((ASN1Integer) seq.getObjectAt(5)).getValue();
                return;
            }
            return;
        }
        throw new IllegalArgumentException("bad version in X9ECParameters");
    }

    public static X9ECParameters getInstance(Object obj) {
        if (obj instanceof X9ECParameters) {
            return (X9ECParameters) obj;
        }
        if (obj != null) {
            return new X9ECParameters(ASN1Sequence.getInstance(obj));
        }
        return null;
    }

    public X9ECParameters(ECCurve curve, ECPoint g, BigInteger n) {
        this(curve, g, n, null, null);
    }

    public X9ECParameters(ECCurve curve, X9ECPoint g, BigInteger n, BigInteger h) {
        this(curve, g, n, h, null);
    }

    public X9ECParameters(ECCurve curve, ECPoint g, BigInteger n, BigInteger h) {
        this(curve, g, n, h, null);
    }

    public X9ECParameters(ECCurve curve, ECPoint g, BigInteger n, BigInteger h, byte[] seed) {
        this(curve, new X9ECPoint(g), n, h, seed);
    }

    public X9ECParameters(ECCurve curve, X9ECPoint g, BigInteger n, BigInteger h, byte[] seed) {
        this.curve = curve;
        this.g = g;
        this.n = n;
        this.h = h;
        this.seed = seed;
        if (ECAlgorithms.isFpCurve(curve)) {
            this.fieldID = new X9FieldID(curve.getField().getCharacteristic());
        } else if (ECAlgorithms.isF2mCurve(curve)) {
            int[] exponents = ((PolynomialExtensionField) curve.getField()).getMinimalPolynomial().getExponentsPresent();
            if (exponents.length == 3) {
                this.fieldID = new X9FieldID(exponents[2], exponents[1]);
            } else if (exponents.length == 5) {
                this.fieldID = new X9FieldID(exponents[4], exponents[1], exponents[2], exponents[3]);
            } else {
                throw new IllegalArgumentException("Only trinomial and pentomial curves are supported");
            }
        } else {
            throw new IllegalArgumentException("'curve' is of an unsupported type");
        }
    }

    public ECCurve getCurve() {
        return this.curve;
    }

    public ECPoint getG() {
        return this.g.getPoint();
    }

    public BigInteger getN() {
        return this.n;
    }

    public BigInteger getH() {
        return this.h;
    }

    public byte[] getSeed() {
        return this.seed;
    }

    public X9Curve getCurveEntry() {
        return new X9Curve(this.curve, this.seed);
    }

    public X9FieldID getFieldIDEntry() {
        return this.fieldID;
    }

    public X9ECPoint getBaseEntry() {
        return this.g;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(ONE));
        v.add(this.fieldID);
        v.add(new X9Curve(this.curve, this.seed));
        v.add(this.g);
        v.add(new ASN1Integer(this.n));
        if (this.h != null) {
            v.add(new ASN1Integer(this.h));
        }
        return new DERSequence(v);
    }
}
