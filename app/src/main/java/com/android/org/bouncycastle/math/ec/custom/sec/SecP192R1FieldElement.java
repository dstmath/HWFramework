package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.ec.ECFieldElement;
import com.android.org.bouncycastle.math.raw.Mod;
import com.android.org.bouncycastle.math.raw.Nat192;
import com.android.org.bouncycastle.util.Arrays;
import java.math.BigInteger;

public class SecP192R1FieldElement extends ECFieldElement {
    public static final BigInteger Q = null;
    protected int[] x;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.math.ec.custom.sec.SecP192R1FieldElement.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.math.ec.custom.sec.SecP192R1FieldElement.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.math.ec.custom.sec.SecP192R1FieldElement.<clinit>():void");
    }

    public SecP192R1FieldElement(BigInteger x) {
        if (x == null || x.signum() < 0 || x.compareTo(Q) >= 0) {
            throw new IllegalArgumentException("x value invalid for SecP192R1FieldElement");
        }
        this.x = SecP192R1Field.fromBigInteger(x);
    }

    public SecP192R1FieldElement() {
        this.x = Nat192.create();
    }

    protected SecP192R1FieldElement(int[] x) {
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
        return "SecP192R1Field";
    }

    public int getFieldSize() {
        return Q.bitLength();
    }

    public ECFieldElement add(ECFieldElement b) {
        int[] z = Nat192.create();
        SecP192R1Field.add(this.x, ((SecP192R1FieldElement) b).x, z);
        return new SecP192R1FieldElement(z);
    }

    public ECFieldElement addOne() {
        int[] z = Nat192.create();
        SecP192R1Field.addOne(this.x, z);
        return new SecP192R1FieldElement(z);
    }

    public ECFieldElement subtract(ECFieldElement b) {
        int[] z = Nat192.create();
        SecP192R1Field.subtract(this.x, ((SecP192R1FieldElement) b).x, z);
        return new SecP192R1FieldElement(z);
    }

    public ECFieldElement multiply(ECFieldElement b) {
        int[] z = Nat192.create();
        SecP192R1Field.multiply(this.x, ((SecP192R1FieldElement) b).x, z);
        return new SecP192R1FieldElement(z);
    }

    public ECFieldElement divide(ECFieldElement b) {
        int[] z = Nat192.create();
        Mod.invert(SecP192R1Field.P, ((SecP192R1FieldElement) b).x, z);
        SecP192R1Field.multiply(z, this.x, z);
        return new SecP192R1FieldElement(z);
    }

    public ECFieldElement negate() {
        int[] z = Nat192.create();
        SecP192R1Field.negate(this.x, z);
        return new SecP192R1FieldElement(z);
    }

    public ECFieldElement square() {
        int[] z = Nat192.create();
        SecP192R1Field.square(this.x, z);
        return new SecP192R1FieldElement(z);
    }

    public ECFieldElement invert() {
        int[] z = Nat192.create();
        Mod.invert(SecP192R1Field.P, this.x, z);
        return new SecP192R1FieldElement(z);
    }

    public ECFieldElement sqrt() {
        int[] x1 = this.x;
        if (Nat192.isZero(x1) || Nat192.isOne(x1)) {
            return this;
        }
        int[] t1 = Nat192.create();
        int[] t2 = Nat192.create();
        SecP192R1Field.square(x1, t1);
        SecP192R1Field.multiply(t1, x1, t1);
        SecP192R1Field.squareN(t1, 2, t2);
        SecP192R1Field.multiply(t2, t1, t2);
        SecP192R1Field.squareN(t2, 4, t1);
        SecP192R1Field.multiply(t1, t2, t1);
        SecP192R1Field.squareN(t1, 8, t2);
        SecP192R1Field.multiply(t2, t1, t2);
        SecP192R1Field.squareN(t2, 16, t1);
        SecP192R1Field.multiply(t1, t2, t1);
        SecP192R1Field.squareN(t1, 32, t2);
        SecP192R1Field.multiply(t2, t1, t2);
        SecP192R1Field.squareN(t2, 64, t1);
        SecP192R1Field.multiply(t1, t2, t1);
        SecP192R1Field.squareN(t1, 62, t1);
        SecP192R1Field.square(t1, t2);
        return Nat192.eq(x1, t2) ? new SecP192R1FieldElement(t1) : null;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SecP192R1FieldElement)) {
            return false;
        }
        return Nat192.eq(this.x, ((SecP192R1FieldElement) other).x);
    }

    public int hashCode() {
        return Q.hashCode() ^ Arrays.hashCode(this.x, 0, 6);
    }
}
