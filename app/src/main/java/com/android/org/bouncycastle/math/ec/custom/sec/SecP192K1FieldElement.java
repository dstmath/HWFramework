package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.ec.ECFieldElement;
import com.android.org.bouncycastle.math.raw.Mod;
import com.android.org.bouncycastle.math.raw.Nat192;
import com.android.org.bouncycastle.util.Arrays;
import java.math.BigInteger;

public class SecP192K1FieldElement extends ECFieldElement {
    public static final BigInteger Q = null;
    protected int[] x;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.math.ec.custom.sec.SecP192K1FieldElement.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.math.ec.custom.sec.SecP192K1FieldElement.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.math.ec.custom.sec.SecP192K1FieldElement.<clinit>():void");
    }

    public SecP192K1FieldElement(BigInteger x) {
        if (x == null || x.signum() < 0 || x.compareTo(Q) >= 0) {
            throw new IllegalArgumentException("x value invalid for SecP192K1FieldElement");
        }
        this.x = SecP192K1Field.fromBigInteger(x);
    }

    public SecP192K1FieldElement() {
        this.x = Nat192.create();
    }

    protected SecP192K1FieldElement(int[] x) {
        this.x = x;
    }

    public boolean isZero() {
        return Nat192.isZero(this.x);
    }

    public boolean isOne() {
        return Nat192.isOne(this.x);
    }

    public boolean testBitZero() {
        return Nat192.getBit(this.x, 0) == 1;
    }

    public BigInteger toBigInteger() {
        return Nat192.toBigInteger(this.x);
    }

    public String getFieldName() {
        return "SecP192K1Field";
    }

    public int getFieldSize() {
        return Q.bitLength();
    }

    public ECFieldElement add(ECFieldElement b) {
        int[] z = Nat192.create();
        SecP192K1Field.add(this.x, ((SecP192K1FieldElement) b).x, z);
        return new SecP192K1FieldElement(z);
    }

    public ECFieldElement addOne() {
        int[] z = Nat192.create();
        SecP192K1Field.addOne(this.x, z);
        return new SecP192K1FieldElement(z);
    }

    public ECFieldElement subtract(ECFieldElement b) {
        int[] z = Nat192.create();
        SecP192K1Field.subtract(this.x, ((SecP192K1FieldElement) b).x, z);
        return new SecP192K1FieldElement(z);
    }

    public ECFieldElement multiply(ECFieldElement b) {
        int[] z = Nat192.create();
        SecP192K1Field.multiply(this.x, ((SecP192K1FieldElement) b).x, z);
        return new SecP192K1FieldElement(z);
    }

    public ECFieldElement divide(ECFieldElement b) {
        int[] z = Nat192.create();
        Mod.invert(SecP192K1Field.P, ((SecP192K1FieldElement) b).x, z);
        SecP192K1Field.multiply(z, this.x, z);
        return new SecP192K1FieldElement(z);
    }

    public ECFieldElement negate() {
        int[] z = Nat192.create();
        SecP192K1Field.negate(this.x, z);
        return new SecP192K1FieldElement(z);
    }

    public ECFieldElement square() {
        int[] z = Nat192.create();
        SecP192K1Field.square(this.x, z);
        return new SecP192K1FieldElement(z);
    }

    public ECFieldElement invert() {
        int[] z = Nat192.create();
        Mod.invert(SecP192K1Field.P, this.x, z);
        return new SecP192K1FieldElement(z);
    }

    public ECFieldElement sqrt() {
        int[] x1 = this.x;
        if (Nat192.isZero(x1) || Nat192.isOne(x1)) {
            return this;
        }
        int[] x2 = Nat192.create();
        SecP192K1Field.square(x1, x2);
        SecP192K1Field.multiply(x2, x1, x2);
        int[] x3 = Nat192.create();
        SecP192K1Field.square(x2, x3);
        SecP192K1Field.multiply(x3, x1, x3);
        int[] x6 = Nat192.create();
        SecP192K1Field.squareN(x3, 3, x6);
        SecP192K1Field.multiply(x6, x3, x6);
        int[] x8 = x6;
        SecP192K1Field.squareN(x6, 2, x6);
        SecP192K1Field.multiply(x6, x2, x6);
        int[] x16 = x2;
        SecP192K1Field.squareN(x6, 8, x2);
        SecP192K1Field.multiply(x2, x6, x2);
        int[] x19 = x6;
        SecP192K1Field.squareN(x2, 3, x6);
        SecP192K1Field.multiply(x6, x3, x6);
        int[] x35 = Nat192.create();
        SecP192K1Field.squareN(x6, 16, x35);
        SecP192K1Field.multiply(x35, x2, x35);
        int[] x70 = x2;
        SecP192K1Field.squareN(x35, 35, x2);
        SecP192K1Field.multiply(x2, x35, x2);
        int[] x140 = x35;
        SecP192K1Field.squareN(x2, 70, x35);
        SecP192K1Field.multiply(x35, x2, x35);
        int[] x159 = x2;
        SecP192K1Field.squareN(x35, 19, x2);
        SecP192K1Field.multiply(x2, x6, x2);
        int[] t1 = x2;
        SecP192K1Field.squareN(x2, 20, x2);
        SecP192K1Field.multiply(x2, x6, x2);
        SecP192K1Field.squareN(x2, 4, x2);
        SecP192K1Field.multiply(x2, x3, x2);
        SecP192K1Field.squareN(x2, 6, x2);
        SecP192K1Field.multiply(x2, x3, x2);
        SecP192K1Field.square(x2, x2);
        int[] t2 = x3;
        SecP192K1Field.square(x2, x3);
        return Nat192.eq(x1, x3) ? new SecP192K1FieldElement(x2) : null;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SecP192K1FieldElement)) {
            return false;
        }
        return Nat192.eq(this.x, ((SecP192K1FieldElement) other).x);
    }

    public int hashCode() {
        return Q.hashCode() ^ Arrays.hashCode(this.x, 0, 6);
    }
}
