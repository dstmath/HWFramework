package com.android.org.bouncycastle.math.ec.custom.sec;

import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.ec.ECFieldElement;
import com.android.org.bouncycastle.math.ec.ECPoint;
import com.android.org.bouncycastle.math.ec.ECPoint.AbstractFp;
import com.android.org.bouncycastle.math.raw.Nat;
import com.android.org.bouncycastle.math.raw.Nat192;

public class SecP192R1Point extends AbstractFp {
    public SecP192R1Point(ECCurve curve, ECFieldElement x, ECFieldElement y) {
        this(curve, x, y, false);
    }

    public SecP192R1Point(ECCurve curve, ECFieldElement x, ECFieldElement y, boolean withCompression) {
        Object obj;
        Object obj2 = 1;
        super(curve, x, y);
        if (x == null) {
            obj = 1;
        } else {
            obj = null;
        }
        if (y != null) {
            obj2 = null;
        }
        if (obj != obj2) {
            throw new IllegalArgumentException("Exactly one of the field elements is null");
        }
        this.withCompression = withCompression;
    }

    SecP192R1Point(ECCurve curve, ECFieldElement x, ECFieldElement y, ECFieldElement[] zs, boolean withCompression) {
        super(curve, x, y, zs);
        this.withCompression = withCompression;
    }

    protected ECPoint detach() {
        return new SecP192R1Point(null, getAffineXCoord(), getAffineYCoord());
    }

    public ECPoint add(ECPoint b) {
        if (isInfinity()) {
            return b;
        }
        if (b.isInfinity()) {
            return this;
        }
        if (this == b) {
            return twice();
        }
        int[] U2;
        int[] S2;
        int[] U1;
        int[] S1;
        ECCurve curve = getCurve();
        SecP192R1FieldElement X1 = (SecP192R1FieldElement) this.x;
        SecP192R1FieldElement Y1 = (SecP192R1FieldElement) this.y;
        SecP192R1FieldElement X2 = (SecP192R1FieldElement) b.getXCoord();
        SecP192R1FieldElement Y2 = (SecP192R1FieldElement) b.getYCoord();
        SecP192R1FieldElement Z1 = this.zs[0];
        SecP192R1FieldElement Z2 = (SecP192R1FieldElement) b.getZCoord(0);
        int[] tt1 = Nat192.createExt();
        int[] t2 = Nat192.create();
        int[] t3 = Nat192.create();
        int[] t4 = Nat192.create();
        boolean Z1IsOne = Z1.isOne();
        if (Z1IsOne) {
            U2 = X2.x;
            S2 = Y2.x;
        } else {
            S2 = t3;
            SecP192R1Field.square(Z1.x, t3);
            U2 = t2;
            SecP192R1Field.multiply(t3, X2.x, t2);
            SecP192R1Field.multiply(t3, Z1.x, t3);
            SecP192R1Field.multiply(t3, Y2.x, t3);
        }
        boolean Z2IsOne = Z2.isOne();
        if (Z2IsOne) {
            U1 = X1.x;
            S1 = Y1.x;
        } else {
            S1 = t4;
            SecP192R1Field.square(Z2.x, t4);
            U1 = tt1;
            SecP192R1Field.multiply(t4, X1.x, tt1);
            SecP192R1Field.multiply(t4, Z2.x, t4);
            SecP192R1Field.multiply(t4, Y1.x, t4);
        }
        int[] H = Nat192.create();
        SecP192R1Field.subtract(U1, U2, H);
        int[] R = t2;
        SecP192R1Field.subtract(S1, S2, t2);
        if (!Nat192.isZero(H)) {
            int[] HSquared = t3;
            SecP192R1Field.square(H, t3);
            int[] G = Nat192.create();
            SecP192R1Field.multiply(t3, H, G);
            int[] V = t3;
            SecP192R1Field.multiply(t3, U1, t3);
            SecP192R1Field.negate(G, G);
            Nat192.mul(S1, G, tt1);
            SecP192R1Field.reduce32(Nat192.addBothTo(t3, t3, G), G);
            SecP192R1FieldElement X3 = new SecP192R1FieldElement(t4);
            SecP192R1Field.square(t2, X3.x);
            SecP192R1Field.subtract(X3.x, G, X3.x);
            SecP192R1FieldElement Y3 = new SecP192R1FieldElement(G);
            SecP192R1Field.subtract(t3, X3.x, Y3.x);
            SecP192R1Field.multiplyAddToExt(Y3.x, t2, tt1);
            SecP192R1Field.reduce(tt1, Y3.x);
            SecP192R1FieldElement secP192R1FieldElement = new SecP192R1FieldElement(H);
            if (!Z1IsOne) {
                SecP192R1Field.multiply(secP192R1FieldElement.x, Z1.x, secP192R1FieldElement.x);
            }
            if (!Z2IsOne) {
                SecP192R1Field.multiply(secP192R1FieldElement.x, Z2.x, secP192R1FieldElement.x);
            }
            return new SecP192R1Point(curve, X3, Y3, new ECFieldElement[]{secP192R1FieldElement}, this.withCompression);
        } else if (Nat192.isZero(t2)) {
            return twice();
        } else {
            return curve.getInfinity();
        }
    }

    public ECPoint twice() {
        if (isInfinity()) {
            return this;
        }
        ECCurve curve = getCurve();
        SecP192R1FieldElement Y1 = this.y;
        if (Y1.isZero()) {
            return curve.getInfinity();
        }
        SecP192R1FieldElement X1 = this.x;
        SecP192R1FieldElement Z1 = this.zs[0];
        int[] t1 = Nat192.create();
        int[] t2 = Nat192.create();
        int[] Y1Squared = Nat192.create();
        SecP192R1Field.square(Y1.x, Y1Squared);
        int[] T = Nat192.create();
        SecP192R1Field.square(Y1Squared, T);
        boolean Z1IsOne = Z1.isOne();
        int[] Z1Squared = Z1.x;
        if (!Z1IsOne) {
            Z1Squared = t2;
            SecP192R1Field.square(Z1.x, t2);
        }
        SecP192R1Field.subtract(X1.x, Z1Squared, t1);
        int[] M = t2;
        SecP192R1Field.add(X1.x, Z1Squared, t2);
        SecP192R1Field.multiply(t2, t1, t2);
        SecP192R1Field.reduce32(Nat192.addBothTo(t2, t2, t2), t2);
        int[] S = Y1Squared;
        SecP192R1Field.multiply(Y1Squared, X1.x, Y1Squared);
        SecP192R1Field.reduce32(Nat.shiftUpBits(6, Y1Squared, 2, 0), Y1Squared);
        SecP192R1Field.reduce32(Nat.shiftUpBits(6, T, 3, 0, t1), t1);
        SecP192R1FieldElement X3 = new SecP192R1FieldElement(T);
        SecP192R1Field.square(t2, X3.x);
        SecP192R1Field.subtract(X3.x, Y1Squared, X3.x);
        SecP192R1Field.subtract(X3.x, Y1Squared, X3.x);
        SecP192R1FieldElement Y3 = new SecP192R1FieldElement(Y1Squared);
        SecP192R1Field.subtract(Y1Squared, X3.x, Y3.x);
        SecP192R1Field.multiply(Y3.x, t2, Y3.x);
        SecP192R1Field.subtract(Y3.x, t1, Y3.x);
        SecP192R1FieldElement secP192R1FieldElement = new SecP192R1FieldElement(t2);
        SecP192R1Field.twice(Y1.x, secP192R1FieldElement.x);
        if (!Z1IsOne) {
            SecP192R1Field.multiply(secP192R1FieldElement.x, Z1.x, secP192R1FieldElement.x);
        }
        return new SecP192R1Point(curve, X3, Y3, new ECFieldElement[]{secP192R1FieldElement}, this.withCompression);
    }

    public ECPoint twicePlus(ECPoint b) {
        if (this == b) {
            return threeTimes();
        }
        if (isInfinity()) {
            return b;
        }
        if (b.isInfinity()) {
            return twice();
        }
        if (this.y.isZero()) {
            return b;
        }
        return twice().add(b);
    }

    public ECPoint threeTimes() {
        if (isInfinity() || this.y.isZero()) {
            return this;
        }
        return twice().add(this);
    }

    public ECPoint negate() {
        if (isInfinity()) {
            return this;
        }
        return new SecP192R1Point(this.curve, this.x, this.y.negate(), this.zs, this.withCompression);
    }
}
