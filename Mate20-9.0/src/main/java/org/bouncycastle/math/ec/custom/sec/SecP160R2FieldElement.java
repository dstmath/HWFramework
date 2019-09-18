package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.raw.Mod;
import org.bouncycastle.math.raw.Nat160;
import org.bouncycastle.util.Arrays;

public class SecP160R2FieldElement extends ECFieldElement.AbstractFp {
    public static final BigInteger Q = SecP160R2Curve.q;
    protected int[] x;

    public SecP160R2FieldElement() {
        this.x = Nat160.create();
    }

    public SecP160R2FieldElement(BigInteger bigInteger) {
        if (bigInteger == null || bigInteger.signum() < 0 || bigInteger.compareTo(Q) >= 0) {
            throw new IllegalArgumentException("x value invalid for SecP160R2FieldElement");
        }
        this.x = SecP160R2Field.fromBigInteger(bigInteger);
    }

    protected SecP160R2FieldElement(int[] iArr) {
        this.x = iArr;
    }

    public ECFieldElement add(ECFieldElement eCFieldElement) {
        int[] create = Nat160.create();
        SecP160R2Field.add(this.x, ((SecP160R2FieldElement) eCFieldElement).x, create);
        return new SecP160R2FieldElement(create);
    }

    public ECFieldElement addOne() {
        int[] create = Nat160.create();
        SecP160R2Field.addOne(this.x, create);
        return new SecP160R2FieldElement(create);
    }

    public ECFieldElement divide(ECFieldElement eCFieldElement) {
        int[] create = Nat160.create();
        Mod.invert(SecP160R2Field.P, ((SecP160R2FieldElement) eCFieldElement).x, create);
        SecP160R2Field.multiply(create, this.x, create);
        return new SecP160R2FieldElement(create);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SecP160R2FieldElement)) {
            return false;
        }
        return Nat160.eq(this.x, ((SecP160R2FieldElement) obj).x);
    }

    public String getFieldName() {
        return "SecP160R2Field";
    }

    public int getFieldSize() {
        return Q.bitLength();
    }

    public int hashCode() {
        return Q.hashCode() ^ Arrays.hashCode(this.x, 0, 5);
    }

    public ECFieldElement invert() {
        int[] create = Nat160.create();
        Mod.invert(SecP160R2Field.P, this.x, create);
        return new SecP160R2FieldElement(create);
    }

    public boolean isOne() {
        return Nat160.isOne(this.x);
    }

    public boolean isZero() {
        return Nat160.isZero(this.x);
    }

    public ECFieldElement multiply(ECFieldElement eCFieldElement) {
        int[] create = Nat160.create();
        SecP160R2Field.multiply(this.x, ((SecP160R2FieldElement) eCFieldElement).x, create);
        return new SecP160R2FieldElement(create);
    }

    public ECFieldElement negate() {
        int[] create = Nat160.create();
        SecP160R2Field.negate(this.x, create);
        return new SecP160R2FieldElement(create);
    }

    public ECFieldElement sqrt() {
        int[] iArr = this.x;
        if (Nat160.isZero(iArr) || Nat160.isOne(iArr)) {
            return this;
        }
        int[] create = Nat160.create();
        SecP160R2Field.square(iArr, create);
        SecP160R2Field.multiply(create, iArr, create);
        int[] create2 = Nat160.create();
        SecP160R2Field.square(create, create2);
        SecP160R2Field.multiply(create2, iArr, create2);
        int[] create3 = Nat160.create();
        SecP160R2Field.square(create2, create3);
        SecP160R2Field.multiply(create3, iArr, create3);
        int[] create4 = Nat160.create();
        SecP160R2Field.squareN(create3, 3, create4);
        SecP160R2Field.multiply(create4, create2, create4);
        SecP160R2Field.squareN(create4, 7, create3);
        SecP160R2Field.multiply(create3, create4, create3);
        SecP160R2Field.squareN(create3, 3, create4);
        SecP160R2Field.multiply(create4, create2, create4);
        int[] create5 = Nat160.create();
        SecP160R2Field.squareN(create4, 14, create5);
        SecP160R2Field.multiply(create5, create3, create5);
        SecP160R2Field.squareN(create5, 31, create3);
        SecP160R2Field.multiply(create3, create5, create3);
        SecP160R2Field.squareN(create3, 62, create5);
        SecP160R2Field.multiply(create5, create3, create5);
        SecP160R2Field.squareN(create5, 3, create3);
        SecP160R2Field.multiply(create3, create2, create3);
        SecP160R2Field.squareN(create3, 18, create3);
        SecP160R2Field.multiply(create3, create4, create3);
        SecP160R2Field.squareN(create3, 2, create3);
        SecP160R2Field.multiply(create3, iArr, create3);
        SecP160R2Field.squareN(create3, 3, create3);
        SecP160R2Field.multiply(create3, create, create3);
        SecP160R2Field.squareN(create3, 6, create3);
        SecP160R2Field.multiply(create3, create2, create3);
        SecP160R2Field.squareN(create3, 2, create3);
        SecP160R2Field.multiply(create3, iArr, create3);
        SecP160R2Field.square(create3, create);
        if (Nat160.eq(iArr, create)) {
            return new SecP160R2FieldElement(create3);
        }
        return null;
    }

    public ECFieldElement square() {
        int[] create = Nat160.create();
        SecP160R2Field.square(this.x, create);
        return new SecP160R2FieldElement(create);
    }

    public ECFieldElement subtract(ECFieldElement eCFieldElement) {
        int[] create = Nat160.create();
        SecP160R2Field.subtract(this.x, ((SecP160R2FieldElement) eCFieldElement).x, create);
        return new SecP160R2FieldElement(create);
    }

    public boolean testBitZero() {
        return Nat160.getBit(this.x, 0) == 1;
    }

    public BigInteger toBigInteger() {
        return Nat160.toBigInteger(this.x);
    }
}
