package org.bouncycastle.math.ec.custom.sec;

import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

public class SecT193R2Point extends ECPoint.AbstractF2m {
    SecT193R2Point(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        super(eCCurve, eCFieldElement, eCFieldElement2);
    }

    SecT193R2Point(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr) {
        super(eCCurve, eCFieldElement, eCFieldElement2, eCFieldElementArr);
    }

    @Override // org.bouncycastle.math.ec.ECPoint
    public ECPoint add(ECPoint eCPoint) {
        ECFieldElement eCFieldElement;
        ECFieldElement eCFieldElement2;
        ECFieldElement eCFieldElement3;
        ECFieldElement eCFieldElement4;
        ECFieldElement eCFieldElement5;
        ECFieldElement eCFieldElement6;
        if (isInfinity()) {
            return eCPoint;
        }
        if (eCPoint.isInfinity()) {
            return this;
        }
        ECCurve curve = getCurve();
        ECFieldElement eCFieldElement7 = this.x;
        ECFieldElement rawXCoord = eCPoint.getRawXCoord();
        if (eCFieldElement7.isZero()) {
            return rawXCoord.isZero() ? curve.getInfinity() : eCPoint.add(this);
        }
        ECFieldElement eCFieldElement8 = this.y;
        ECFieldElement eCFieldElement9 = this.zs[0];
        ECFieldElement rawYCoord = eCPoint.getRawYCoord();
        ECFieldElement zCoord = eCPoint.getZCoord(0);
        boolean isOne = eCFieldElement9.isOne();
        if (!isOne) {
            eCFieldElement2 = rawXCoord.multiply(eCFieldElement9);
            eCFieldElement = rawYCoord.multiply(eCFieldElement9);
        } else {
            eCFieldElement2 = rawXCoord;
            eCFieldElement = rawYCoord;
        }
        boolean isOne2 = zCoord.isOne();
        if (!isOne2) {
            eCFieldElement7 = eCFieldElement7.multiply(zCoord);
            eCFieldElement3 = eCFieldElement8.multiply(zCoord);
        } else {
            eCFieldElement3 = eCFieldElement8;
        }
        ECFieldElement add = eCFieldElement3.add(eCFieldElement);
        ECFieldElement add2 = eCFieldElement7.add(eCFieldElement2);
        if (add2.isZero()) {
            return add.isZero() ? twice() : curve.getInfinity();
        }
        if (rawXCoord.isZero()) {
            ECPoint normalize = normalize();
            ECFieldElement xCoord = normalize.getXCoord();
            ECFieldElement yCoord = normalize.getYCoord();
            ECFieldElement divide = yCoord.add(rawYCoord).divide(xCoord);
            ECFieldElement add3 = divide.square().add(divide).add(xCoord).add(curve.getA());
            if (add3.isZero()) {
                return new SecT193R2Point(curve, add3, curve.getB().sqrt());
            }
            eCFieldElement5 = divide.multiply(xCoord.add(add3)).add(add3).add(yCoord).divide(add3).add(add3);
            eCFieldElement4 = curve.fromBigInteger(ECConstants.ONE);
            eCFieldElement6 = add3;
        } else {
            ECFieldElement square = add2.square();
            ECFieldElement multiply = add.multiply(eCFieldElement7);
            ECFieldElement multiply2 = add.multiply(eCFieldElement2);
            eCFieldElement6 = multiply.multiply(multiply2);
            if (eCFieldElement6.isZero()) {
                return new SecT193R2Point(curve, eCFieldElement6, curve.getB().sqrt());
            }
            ECFieldElement multiply3 = add.multiply(square);
            eCFieldElement4 = !isOne2 ? multiply3.multiply(zCoord) : multiply3;
            eCFieldElement5 = multiply2.add(square).squarePlusProduct(eCFieldElement4, eCFieldElement8.add(eCFieldElement9));
            if (!isOne) {
                eCFieldElement4 = eCFieldElement4.multiply(eCFieldElement9);
            }
        }
        return new SecT193R2Point(curve, eCFieldElement6, eCFieldElement5, new ECFieldElement[]{eCFieldElement4});
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.math.ec.ECPoint
    public ECPoint detach() {
        return new SecT193R2Point(null, getAffineXCoord(), getAffineYCoord());
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.math.ec.ECPoint
    public boolean getCompressionYTilde() {
        ECFieldElement rawXCoord = getRawXCoord();
        return !rawXCoord.isZero() && getRawYCoord().testBitZero() != rawXCoord.testBitZero();
    }

    @Override // org.bouncycastle.math.ec.ECPoint
    public ECFieldElement getYCoord() {
        ECFieldElement eCFieldElement = this.x;
        ECFieldElement eCFieldElement2 = this.y;
        if (isInfinity() || eCFieldElement.isZero()) {
            return eCFieldElement2;
        }
        ECFieldElement multiply = eCFieldElement2.add(eCFieldElement).multiply(eCFieldElement);
        ECFieldElement eCFieldElement3 = this.zs[0];
        return !eCFieldElement3.isOne() ? multiply.divide(eCFieldElement3) : multiply;
    }

    @Override // org.bouncycastle.math.ec.ECPoint
    public ECPoint negate() {
        if (isInfinity()) {
            return this;
        }
        ECFieldElement eCFieldElement = this.x;
        if (eCFieldElement.isZero()) {
            return this;
        }
        ECFieldElement eCFieldElement2 = this.y;
        ECFieldElement eCFieldElement3 = this.zs[0];
        return new SecT193R2Point(this.curve, eCFieldElement, eCFieldElement2.add(eCFieldElement3), new ECFieldElement[]{eCFieldElement3});
    }

    @Override // org.bouncycastle.math.ec.ECPoint
    public ECPoint twice() {
        if (isInfinity()) {
            return this;
        }
        ECCurve curve = getCurve();
        ECFieldElement eCFieldElement = this.x;
        if (eCFieldElement.isZero()) {
            return curve.getInfinity();
        }
        ECFieldElement eCFieldElement2 = this.y;
        ECFieldElement eCFieldElement3 = this.zs[0];
        boolean isOne = eCFieldElement3.isOne();
        ECFieldElement multiply = isOne ? eCFieldElement2 : eCFieldElement2.multiply(eCFieldElement3);
        ECFieldElement square = isOne ? eCFieldElement3 : eCFieldElement3.square();
        ECFieldElement a = curve.getA();
        if (!isOne) {
            a = a.multiply(square);
        }
        ECFieldElement add = eCFieldElement2.square().add(multiply).add(a);
        if (add.isZero()) {
            return new SecT193R2Point(curve, add, curve.getB().sqrt());
        }
        ECFieldElement square2 = add.square();
        ECFieldElement multiply2 = isOne ? add : add.multiply(square);
        if (!isOne) {
            eCFieldElement = eCFieldElement.multiply(eCFieldElement3);
        }
        return new SecT193R2Point(curve, square2, eCFieldElement.squarePlusProduct(add, multiply).add(square2).add(multiply2), new ECFieldElement[]{multiply2});
    }

    @Override // org.bouncycastle.math.ec.ECPoint
    public ECPoint twicePlus(ECPoint eCPoint) {
        if (isInfinity()) {
            return eCPoint;
        }
        if (eCPoint.isInfinity()) {
            return twice();
        }
        ECCurve curve = getCurve();
        ECFieldElement eCFieldElement = this.x;
        if (eCFieldElement.isZero()) {
            return eCPoint;
        }
        ECFieldElement rawXCoord = eCPoint.getRawXCoord();
        ECFieldElement zCoord = eCPoint.getZCoord(0);
        if (rawXCoord.isZero() || !zCoord.isOne()) {
            return twice().add(eCPoint);
        }
        ECFieldElement eCFieldElement2 = this.y;
        ECFieldElement eCFieldElement3 = this.zs[0];
        ECFieldElement rawYCoord = eCPoint.getRawYCoord();
        ECFieldElement square = eCFieldElement.square();
        ECFieldElement square2 = eCFieldElement2.square();
        ECFieldElement square3 = eCFieldElement3.square();
        ECFieldElement add = curve.getA().multiply(square3).add(square2).add(eCFieldElement2.multiply(eCFieldElement3));
        ECFieldElement addOne = rawYCoord.addOne();
        ECFieldElement multiplyPlusProduct = curve.getA().add(addOne).multiply(square3).add(square2).multiplyPlusProduct(add, square, square3);
        ECFieldElement multiply = rawXCoord.multiply(square3);
        ECFieldElement square4 = multiply.add(add).square();
        if (square4.isZero()) {
            return multiplyPlusProduct.isZero() ? eCPoint.twice() : curve.getInfinity();
        }
        if (multiplyPlusProduct.isZero()) {
            return new SecT193R2Point(curve, multiplyPlusProduct, curve.getB().sqrt());
        }
        ECFieldElement multiply2 = multiplyPlusProduct.square().multiply(multiply);
        ECFieldElement multiply3 = multiplyPlusProduct.multiply(square4).multiply(square3);
        return new SecT193R2Point(curve, multiply2, multiplyPlusProduct.add(square4).square().multiplyPlusProduct(add, addOne, multiply3), new ECFieldElement[]{multiply3});
    }
}
