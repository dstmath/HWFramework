package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat320;
import org.bouncycastle.util.Arrays;

public class SecT283FieldElement extends ECFieldElement.AbstractF2m {
    protected long[] x;

    public SecT283FieldElement() {
        this.x = Nat320.create64();
    }

    public SecT283FieldElement(BigInteger bigInteger) {
        if (bigInteger == null || bigInteger.signum() < 0 || bigInteger.bitLength() > 283) {
            throw new IllegalArgumentException("x value invalid for SecT283FieldElement");
        }
        this.x = SecT283Field.fromBigInteger(bigInteger);
    }

    protected SecT283FieldElement(long[] jArr) {
        this.x = jArr;
    }

    public ECFieldElement add(ECFieldElement eCFieldElement) {
        long[] create64 = Nat320.create64();
        SecT283Field.add(this.x, ((SecT283FieldElement) eCFieldElement).x, create64);
        return new SecT283FieldElement(create64);
    }

    public ECFieldElement addOne() {
        long[] create64 = Nat320.create64();
        SecT283Field.addOne(this.x, create64);
        return new SecT283FieldElement(create64);
    }

    public ECFieldElement divide(ECFieldElement eCFieldElement) {
        return multiply(eCFieldElement.invert());
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SecT283FieldElement)) {
            return false;
        }
        return Nat320.eq64(this.x, ((SecT283FieldElement) obj).x);
    }

    public String getFieldName() {
        return "SecT283Field";
    }

    public int getFieldSize() {
        return 283;
    }

    public int getK1() {
        return 5;
    }

    public int getK2() {
        return 7;
    }

    public int getK3() {
        return 12;
    }

    public int getM() {
        return 283;
    }

    public int getRepresentation() {
        return 3;
    }

    public int hashCode() {
        return Arrays.hashCode(this.x, 0, 5) ^ 2831275;
    }

    public ECFieldElement invert() {
        long[] create64 = Nat320.create64();
        SecT283Field.invert(this.x, create64);
        return new SecT283FieldElement(create64);
    }

    public boolean isOne() {
        return Nat320.isOne64(this.x);
    }

    public boolean isZero() {
        return Nat320.isZero64(this.x);
    }

    public ECFieldElement multiply(ECFieldElement eCFieldElement) {
        long[] create64 = Nat320.create64();
        SecT283Field.multiply(this.x, ((SecT283FieldElement) eCFieldElement).x, create64);
        return new SecT283FieldElement(create64);
    }

    public ECFieldElement multiplyMinusProduct(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement eCFieldElement3) {
        return multiplyPlusProduct(eCFieldElement, eCFieldElement2, eCFieldElement3);
    }

    public ECFieldElement multiplyPlusProduct(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement eCFieldElement3) {
        long[] jArr = this.x;
        long[] jArr2 = ((SecT283FieldElement) eCFieldElement).x;
        long[] jArr3 = ((SecT283FieldElement) eCFieldElement2).x;
        long[] jArr4 = ((SecT283FieldElement) eCFieldElement3).x;
        long[] create64 = Nat.create64(9);
        SecT283Field.multiplyAddToExt(jArr, jArr2, create64);
        SecT283Field.multiplyAddToExt(jArr3, jArr4, create64);
        long[] create642 = Nat320.create64();
        SecT283Field.reduce(create64, create642);
        return new SecT283FieldElement(create642);
    }

    public ECFieldElement negate() {
        return this;
    }

    public ECFieldElement sqrt() {
        long[] create64 = Nat320.create64();
        SecT283Field.sqrt(this.x, create64);
        return new SecT283FieldElement(create64);
    }

    public ECFieldElement square() {
        long[] create64 = Nat320.create64();
        SecT283Field.square(this.x, create64);
        return new SecT283FieldElement(create64);
    }

    public ECFieldElement squareMinusProduct(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        return squarePlusProduct(eCFieldElement, eCFieldElement2);
    }

    public ECFieldElement squarePlusProduct(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        long[] jArr = this.x;
        long[] jArr2 = ((SecT283FieldElement) eCFieldElement).x;
        long[] jArr3 = ((SecT283FieldElement) eCFieldElement2).x;
        long[] create64 = Nat.create64(9);
        SecT283Field.squareAddToExt(jArr, create64);
        SecT283Field.multiplyAddToExt(jArr2, jArr3, create64);
        long[] create642 = Nat320.create64();
        SecT283Field.reduce(create64, create642);
        return new SecT283FieldElement(create642);
    }

    public ECFieldElement squarePow(int i) {
        if (i < 1) {
            return this;
        }
        long[] create64 = Nat320.create64();
        SecT283Field.squareN(this.x, i, create64);
        return new SecT283FieldElement(create64);
    }

    public ECFieldElement subtract(ECFieldElement eCFieldElement) {
        return add(eCFieldElement);
    }

    public boolean testBitZero() {
        return (this.x[0] & 1) != 0;
    }

    public BigInteger toBigInteger() {
        return Nat320.toBigInteger64(this.x);
    }

    public int trace() {
        return SecT283Field.trace(this.x);
    }
}
