package org.bouncycastle.math.ec.custom.sec;

import java.math.BigInteger;
import org.bouncycastle.crypto.tls.CipherSuite;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.Arrays;

public class SecT193FieldElement extends ECFieldElement.AbstractF2m {
    protected long[] x;

    public SecT193FieldElement() {
        this.x = Nat256.create64();
    }

    public SecT193FieldElement(BigInteger bigInteger) {
        if (bigInteger == null || bigInteger.signum() < 0 || bigInteger.bitLength() > 193) {
            throw new IllegalArgumentException("x value invalid for SecT193FieldElement");
        }
        this.x = SecT193Field.fromBigInteger(bigInteger);
    }

    protected SecT193FieldElement(long[] jArr) {
        this.x = jArr;
    }

    public ECFieldElement add(ECFieldElement eCFieldElement) {
        long[] create64 = Nat256.create64();
        SecT193Field.add(this.x, ((SecT193FieldElement) eCFieldElement).x, create64);
        return new SecT193FieldElement(create64);
    }

    public ECFieldElement addOne() {
        long[] create64 = Nat256.create64();
        SecT193Field.addOne(this.x, create64);
        return new SecT193FieldElement(create64);
    }

    public ECFieldElement divide(ECFieldElement eCFieldElement) {
        return multiply(eCFieldElement.invert());
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SecT193FieldElement)) {
            return false;
        }
        return Nat256.eq64(this.x, ((SecT193FieldElement) obj).x);
    }

    public String getFieldName() {
        return "SecT193Field";
    }

    public int getFieldSize() {
        return CipherSuite.TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA256;
    }

    public int getK1() {
        return 15;
    }

    public int getK2() {
        return 0;
    }

    public int getK3() {
        return 0;
    }

    public int getM() {
        return CipherSuite.TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA256;
    }

    public int getRepresentation() {
        return 2;
    }

    public int hashCode() {
        return Arrays.hashCode(this.x, 0, 4) ^ 1930015;
    }

    public ECFieldElement invert() {
        long[] create64 = Nat256.create64();
        SecT193Field.invert(this.x, create64);
        return new SecT193FieldElement(create64);
    }

    public boolean isOne() {
        return Nat256.isOne64(this.x);
    }

    public boolean isZero() {
        return Nat256.isZero64(this.x);
    }

    public ECFieldElement multiply(ECFieldElement eCFieldElement) {
        long[] create64 = Nat256.create64();
        SecT193Field.multiply(this.x, ((SecT193FieldElement) eCFieldElement).x, create64);
        return new SecT193FieldElement(create64);
    }

    public ECFieldElement multiplyMinusProduct(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement eCFieldElement3) {
        return multiplyPlusProduct(eCFieldElement, eCFieldElement2, eCFieldElement3);
    }

    public ECFieldElement multiplyPlusProduct(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement eCFieldElement3) {
        long[] jArr = this.x;
        long[] jArr2 = ((SecT193FieldElement) eCFieldElement).x;
        long[] jArr3 = ((SecT193FieldElement) eCFieldElement2).x;
        long[] jArr4 = ((SecT193FieldElement) eCFieldElement3).x;
        long[] createExt64 = Nat256.createExt64();
        SecT193Field.multiplyAddToExt(jArr, jArr2, createExt64);
        SecT193Field.multiplyAddToExt(jArr3, jArr4, createExt64);
        long[] create64 = Nat256.create64();
        SecT193Field.reduce(createExt64, create64);
        return new SecT193FieldElement(create64);
    }

    public ECFieldElement negate() {
        return this;
    }

    public ECFieldElement sqrt() {
        long[] create64 = Nat256.create64();
        SecT193Field.sqrt(this.x, create64);
        return new SecT193FieldElement(create64);
    }

    public ECFieldElement square() {
        long[] create64 = Nat256.create64();
        SecT193Field.square(this.x, create64);
        return new SecT193FieldElement(create64);
    }

    public ECFieldElement squareMinusProduct(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        return squarePlusProduct(eCFieldElement, eCFieldElement2);
    }

    public ECFieldElement squarePlusProduct(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        long[] jArr = this.x;
        long[] jArr2 = ((SecT193FieldElement) eCFieldElement).x;
        long[] jArr3 = ((SecT193FieldElement) eCFieldElement2).x;
        long[] createExt64 = Nat256.createExt64();
        SecT193Field.squareAddToExt(jArr, createExt64);
        SecT193Field.multiplyAddToExt(jArr2, jArr3, createExt64);
        long[] create64 = Nat256.create64();
        SecT193Field.reduce(createExt64, create64);
        return new SecT193FieldElement(create64);
    }

    public ECFieldElement squarePow(int i) {
        if (i < 1) {
            return this;
        }
        long[] create64 = Nat256.create64();
        SecT193Field.squareN(this.x, i, create64);
        return new SecT193FieldElement(create64);
    }

    public ECFieldElement subtract(ECFieldElement eCFieldElement) {
        return add(eCFieldElement);
    }

    public boolean testBitZero() {
        return (this.x[0] & 1) != 0;
    }

    public BigInteger toBigInteger() {
        return Nat256.toBigInteger64(this.x);
    }

    public int trace() {
        return SecT193Field.trace(this.x);
    }
}
