package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.ec.ECFieldElement;
import com.android.org.bouncycastle.math.raw.Mod;
import com.android.org.bouncycastle.math.raw.Nat;
import com.android.org.bouncycastle.math.raw.Nat224;
import com.android.org.bouncycastle.util.Arrays;
import java.math.BigInteger;

public class SecP224R1FieldElement extends ECFieldElement {
    public static final BigInteger Q = SecP224R1Curve.q;
    protected int[] x;

    public SecP224R1FieldElement(BigInteger x) {
        if (x == null || x.signum() < 0 || x.compareTo(Q) >= 0) {
            throw new IllegalArgumentException("x value invalid for SecP224R1FieldElement");
        }
        this.x = SecP224R1Field.fromBigInteger(x);
    }

    public SecP224R1FieldElement() {
        this.x = Nat224.create();
    }

    protected SecP224R1FieldElement(int[] x) {
        this.x = x;
    }

    public boolean isZero() {
        return Nat224.isZero(this.x);
    }

    public boolean isOne() {
        return Nat224.isOne(this.x);
    }

    public boolean testBitZero() {
        return Nat224.getBit(this.x, 0) == 1;
    }

    public BigInteger toBigInteger() {
        return Nat224.toBigInteger(this.x);
    }

    public String getFieldName() {
        return "SecP224R1Field";
    }

    public int getFieldSize() {
        return Q.bitLength();
    }

    public ECFieldElement add(ECFieldElement b) {
        int[] z = Nat224.create();
        SecP224R1Field.add(this.x, ((SecP224R1FieldElement) b).x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement addOne() {
        int[] z = Nat224.create();
        SecP224R1Field.addOne(this.x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement subtract(ECFieldElement b) {
        int[] z = Nat224.create();
        SecP224R1Field.subtract(this.x, ((SecP224R1FieldElement) b).x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement multiply(ECFieldElement b) {
        int[] z = Nat224.create();
        SecP224R1Field.multiply(this.x, ((SecP224R1FieldElement) b).x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement divide(ECFieldElement b) {
        int[] z = Nat224.create();
        Mod.invert(SecP224R1Field.P, ((SecP224R1FieldElement) b).x, z);
        SecP224R1Field.multiply(z, this.x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement negate() {
        int[] z = Nat224.create();
        SecP224R1Field.negate(this.x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement square() {
        int[] z = Nat224.create();
        SecP224R1Field.square(this.x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement invert() {
        int[] z = Nat224.create();
        Mod.invert(SecP224R1Field.P, this.x, z);
        return new SecP224R1FieldElement(z);
    }

    public ECFieldElement sqrt() {
        ECFieldElement eCFieldElement = null;
        int[] c = this.x;
        if (Nat224.isZero(c) || Nat224.isOne(c)) {
            return this;
        }
        int[] nc = Nat224.create();
        SecP224R1Field.negate(c, nc);
        int[] r = Mod.random(SecP224R1Field.P);
        int[] t = Nat224.create();
        if (!isSquare(c)) {
            return null;
        }
        while (!trySqrt(nc, r, t)) {
            SecP224R1Field.addOne(r, r);
        }
        SecP224R1Field.square(t, r);
        if (Nat224.eq(c, r)) {
            eCFieldElement = new SecP224R1FieldElement(t);
        }
        return eCFieldElement;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof SecP224R1FieldElement)) {
            return false;
        }
        return Nat224.eq(this.x, ((SecP224R1FieldElement) other).x);
    }

    public int hashCode() {
        return Q.hashCode() ^ Arrays.hashCode(this.x, 0, 7);
    }

    private static boolean isSquare(int[] x) {
        int[] t1 = Nat224.create();
        int[] t2 = Nat224.create();
        Nat224.copy(x, t1);
        for (int i = 0; i < 7; i++) {
            Nat224.copy(t1, t2);
            SecP224R1Field.squareN(t1, 1 << i, t1);
            SecP224R1Field.multiply(t1, t2, t1);
        }
        SecP224R1Field.squareN(t1, 95, t1);
        return Nat224.isOne(t1);
    }

    private static void RM(int[] nc, int[] d0, int[] e0, int[] d1, int[] e1, int[] f1, int[] t) {
        SecP224R1Field.multiply(e1, e0, t);
        SecP224R1Field.multiply(t, nc, t);
        SecP224R1Field.multiply(d1, d0, f1);
        SecP224R1Field.add(f1, t, f1);
        SecP224R1Field.multiply(d1, e0, t);
        Nat224.copy(f1, d1);
        SecP224R1Field.multiply(e1, d0, e1);
        SecP224R1Field.add(e1, t, e1);
        SecP224R1Field.square(e1, f1);
        SecP224R1Field.multiply(f1, nc, f1);
    }

    private static void RP(int[] nc, int[] d1, int[] e1, int[] f1, int[] t) {
        Nat224.copy(nc, f1);
        int[] d0 = Nat224.create();
        int[] e0 = Nat224.create();
        for (int i = 0; i < 7; i++) {
            Nat224.copy(d1, d0);
            Nat224.copy(e1, e0);
            int j = 1 << i;
            while (true) {
                j--;
                if (j < 0) {
                    break;
                }
                RS(d1, e1, f1, t);
            }
            RM(nc, d0, e0, d1, e1, f1, t);
        }
    }

    private static void RS(int[] d, int[] e, int[] f, int[] t) {
        SecP224R1Field.multiply(e, d, e);
        SecP224R1Field.twice(e, e);
        SecP224R1Field.square(d, t);
        SecP224R1Field.add(f, t, d);
        SecP224R1Field.multiply(f, t, f);
        SecP224R1Field.reduce32(Nat.shiftUpBits(7, f, 2, 0), f);
    }

    private static boolean trySqrt(int[] nc, int[] r, int[] t) {
        int[] d1 = Nat224.create();
        Nat224.copy(r, d1);
        int[] e1 = Nat224.create();
        e1[0] = 1;
        int[] f1 = Nat224.create();
        RP(nc, d1, e1, f1, t);
        int[] d0 = Nat224.create();
        int[] e0 = Nat224.create();
        for (int k = 1; k < 96; k++) {
            Nat224.copy(d1, d0);
            Nat224.copy(e1, e0);
            RS(d1, e1, f1, t);
            if (Nat224.isZero(d1)) {
                Mod.invert(SecP224R1Field.P, e0, t);
                SecP224R1Field.multiply(t, d0, t);
                return true;
            }
        }
        return false;
    }
}
