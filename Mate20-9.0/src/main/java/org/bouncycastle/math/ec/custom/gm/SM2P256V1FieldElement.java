package org.bouncycastle.math.ec.custom.gm;

import java.math.BigInteger;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.raw.Mod;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.Arrays;

public class SM2P256V1FieldElement extends ECFieldElement.AbstractFp {
    public static final BigInteger Q = SM2P256V1Curve.q;
    protected int[] x;

    public SM2P256V1FieldElement() {
        this.x = Nat256.create();
    }

    public SM2P256V1FieldElement(BigInteger bigInteger) {
        if (bigInteger == null || bigInteger.signum() < 0 || bigInteger.compareTo(Q) >= 0) {
            throw new IllegalArgumentException("x value invalid for SM2P256V1FieldElement");
        }
        this.x = SM2P256V1Field.fromBigInteger(bigInteger);
    }

    protected SM2P256V1FieldElement(int[] iArr) {
        this.x = iArr;
    }

    public ECFieldElement add(ECFieldElement eCFieldElement) {
        int[] create = Nat256.create();
        SM2P256V1Field.add(this.x, ((SM2P256V1FieldElement) eCFieldElement).x, create);
        return new SM2P256V1FieldElement(create);
    }

    public ECFieldElement addOne() {
        int[] create = Nat256.create();
        SM2P256V1Field.addOne(this.x, create);
        return new SM2P256V1FieldElement(create);
    }

    public ECFieldElement divide(ECFieldElement eCFieldElement) {
        int[] create = Nat256.create();
        Mod.invert(SM2P256V1Field.P, ((SM2P256V1FieldElement) eCFieldElement).x, create);
        SM2P256V1Field.multiply(create, this.x, create);
        return new SM2P256V1FieldElement(create);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SM2P256V1FieldElement)) {
            return false;
        }
        return Nat256.eq(this.x, ((SM2P256V1FieldElement) obj).x);
    }

    public String getFieldName() {
        return "SM2P256V1Field";
    }

    public int getFieldSize() {
        return Q.bitLength();
    }

    public int hashCode() {
        return Q.hashCode() ^ Arrays.hashCode(this.x, 0, 8);
    }

    public ECFieldElement invert() {
        int[] create = Nat256.create();
        Mod.invert(SM2P256V1Field.P, this.x, create);
        return new SM2P256V1FieldElement(create);
    }

    public boolean isOne() {
        return Nat256.isOne(this.x);
    }

    public boolean isZero() {
        return Nat256.isZero(this.x);
    }

    public ECFieldElement multiply(ECFieldElement eCFieldElement) {
        int[] create = Nat256.create();
        SM2P256V1Field.multiply(this.x, ((SM2P256V1FieldElement) eCFieldElement).x, create);
        return new SM2P256V1FieldElement(create);
    }

    public ECFieldElement negate() {
        int[] create = Nat256.create();
        SM2P256V1Field.negate(this.x, create);
        return new SM2P256V1FieldElement(create);
    }

    public ECFieldElement sqrt() {
        int[] iArr = this.x;
        if (Nat256.isZero(iArr) || Nat256.isOne(iArr)) {
            return this;
        }
        int[] create = Nat256.create();
        SM2P256V1Field.square(iArr, create);
        SM2P256V1Field.multiply(create, iArr, create);
        int[] create2 = Nat256.create();
        SM2P256V1Field.squareN(create, 2, create2);
        SM2P256V1Field.multiply(create2, create, create2);
        int[] create3 = Nat256.create();
        SM2P256V1Field.squareN(create2, 2, create3);
        SM2P256V1Field.multiply(create3, create, create3);
        SM2P256V1Field.squareN(create3, 6, create);
        SM2P256V1Field.multiply(create, create3, create);
        int[] create4 = Nat256.create();
        SM2P256V1Field.squareN(create, 12, create4);
        SM2P256V1Field.multiply(create4, create, create4);
        SM2P256V1Field.squareN(create4, 6, create);
        SM2P256V1Field.multiply(create, create3, create);
        SM2P256V1Field.square(create, create3);
        SM2P256V1Field.multiply(create3, iArr, create3);
        SM2P256V1Field.squareN(create3, 31, create4);
        SM2P256V1Field.multiply(create4, create3, create);
        SM2P256V1Field.squareN(create4, 32, create4);
        SM2P256V1Field.multiply(create4, create, create4);
        SM2P256V1Field.squareN(create4, 62, create4);
        SM2P256V1Field.multiply(create4, create, create4);
        SM2P256V1Field.squareN(create4, 4, create4);
        SM2P256V1Field.multiply(create4, create2, create4);
        SM2P256V1Field.squareN(create4, 32, create4);
        SM2P256V1Field.multiply(create4, iArr, create4);
        SM2P256V1Field.squareN(create4, 62, create4);
        SM2P256V1Field.square(create4, create2);
        if (Nat256.eq(iArr, create2)) {
            return new SM2P256V1FieldElement(create4);
        }
        return null;
    }

    public ECFieldElement square() {
        int[] create = Nat256.create();
        SM2P256V1Field.square(this.x, create);
        return new SM2P256V1FieldElement(create);
    }

    public ECFieldElement subtract(ECFieldElement eCFieldElement) {
        int[] create = Nat256.create();
        SM2P256V1Field.subtract(this.x, ((SM2P256V1FieldElement) eCFieldElement).x, create);
        return new SM2P256V1FieldElement(create);
    }

    public boolean testBitZero() {
        return Nat256.getBit(this.x, 0) == 1;
    }

    public BigInteger toBigInteger() {
        return Nat256.toBigInteger(this.x);
    }
}
