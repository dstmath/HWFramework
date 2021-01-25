package org.bouncycastle.math.ec.custom.sec;

import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat576;

public class SecT571R1Point extends ECPoint.AbstractF2m {
    SecT571R1Point(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        super(eCCurve, eCFieldElement, eCFieldElement2);
    }

    SecT571R1Point(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr) {
        super(eCCurve, eCFieldElement, eCFieldElement2, eCFieldElementArr);
    }

    @Override // org.bouncycastle.math.ec.ECPoint
    public ECPoint add(ECPoint eCPoint) {
        long[] jArr;
        long[] jArr2;
        long[] jArr3;
        SecT571FieldElement secT571FieldElement;
        SecT571FieldElement secT571FieldElement2;
        SecT571FieldElement secT571FieldElement3;
        if (isInfinity()) {
            return eCPoint;
        }
        if (eCPoint.isInfinity()) {
            return this;
        }
        ECCurve curve = getCurve();
        SecT571FieldElement secT571FieldElement4 = (SecT571FieldElement) this.x;
        SecT571FieldElement secT571FieldElement5 = (SecT571FieldElement) eCPoint.getRawXCoord();
        if (secT571FieldElement4.isZero()) {
            return secT571FieldElement5.isZero() ? curve.getInfinity() : eCPoint.add(this);
        }
        SecT571FieldElement secT571FieldElement6 = (SecT571FieldElement) this.y;
        SecT571FieldElement secT571FieldElement7 = (SecT571FieldElement) this.zs[0];
        SecT571FieldElement secT571FieldElement8 = (SecT571FieldElement) eCPoint.getRawYCoord();
        SecT571FieldElement secT571FieldElement9 = (SecT571FieldElement) eCPoint.getZCoord(0);
        long[] create64 = Nat576.create64();
        long[] create642 = Nat576.create64();
        long[] create643 = Nat576.create64();
        long[] create644 = Nat576.create64();
        long[] precompMultiplicand = secT571FieldElement7.isOne() ? null : SecT571Field.precompMultiplicand(secT571FieldElement7.x);
        if (precompMultiplicand == null) {
            jArr = secT571FieldElement5.x;
            jArr2 = secT571FieldElement8.x;
        } else {
            SecT571Field.multiplyPrecomp(secT571FieldElement5.x, precompMultiplicand, create642);
            SecT571Field.multiplyPrecomp(secT571FieldElement8.x, precompMultiplicand, create644);
            jArr = create642;
            jArr2 = create644;
        }
        long[] precompMultiplicand2 = secT571FieldElement9.isOne() ? null : SecT571Field.precompMultiplicand(secT571FieldElement9.x);
        long[] jArr4 = secT571FieldElement4.x;
        if (precompMultiplicand2 == null) {
            jArr3 = secT571FieldElement6.x;
        } else {
            SecT571Field.multiplyPrecomp(jArr4, precompMultiplicand2, create64);
            SecT571Field.multiplyPrecomp(secT571FieldElement6.x, precompMultiplicand2, create643);
            jArr4 = create64;
            jArr3 = create643;
        }
        SecT571Field.add(jArr3, jArr2, create643);
        SecT571Field.add(jArr4, jArr, create644);
        if (Nat576.isZero64(create644)) {
            return Nat576.isZero64(create643) ? twice() : curve.getInfinity();
        }
        if (secT571FieldElement5.isZero()) {
            ECPoint normalize = normalize();
            SecT571FieldElement secT571FieldElement10 = (SecT571FieldElement) normalize.getXCoord();
            ECFieldElement yCoord = normalize.getYCoord();
            ECFieldElement divide = yCoord.add(secT571FieldElement8).divide(secT571FieldElement10);
            secT571FieldElement = (SecT571FieldElement) divide.square().add(divide).add(secT571FieldElement10).addOne();
            if (secT571FieldElement.isZero()) {
                return new SecT571R1Point(curve, secT571FieldElement, SecT571R1Curve.SecT571R1_B_SQRT);
            }
            secT571FieldElement3 = (SecT571FieldElement) divide.multiply(secT571FieldElement10.add(secT571FieldElement)).add(secT571FieldElement).add(yCoord).divide(secT571FieldElement).add(secT571FieldElement);
            secT571FieldElement2 = (SecT571FieldElement) curve.fromBigInteger(ECConstants.ONE);
        } else {
            SecT571Field.square(create644, create644);
            long[] precompMultiplicand3 = SecT571Field.precompMultiplicand(create643);
            SecT571Field.multiplyPrecomp(jArr4, precompMultiplicand3, create64);
            SecT571Field.multiplyPrecomp(jArr, precompMultiplicand3, create642);
            secT571FieldElement = new SecT571FieldElement(create64);
            SecT571Field.multiply(create64, create642, secT571FieldElement.x);
            if (secT571FieldElement.isZero()) {
                return new SecT571R1Point(curve, secT571FieldElement, SecT571R1Curve.SecT571R1_B_SQRT);
            }
            SecT571FieldElement secT571FieldElement11 = new SecT571FieldElement(create643);
            SecT571Field.multiplyPrecomp(create644, precompMultiplicand3, secT571FieldElement11.x);
            if (precompMultiplicand2 != null) {
                SecT571Field.multiplyPrecomp(secT571FieldElement11.x, precompMultiplicand2, secT571FieldElement11.x);
            }
            long[] createExt64 = Nat576.createExt64();
            SecT571Field.add(create642, create644, create644);
            SecT571Field.squareAddToExt(create644, createExt64);
            SecT571Field.add(secT571FieldElement6.x, secT571FieldElement7.x, create644);
            SecT571Field.multiplyAddToExt(create644, secT571FieldElement11.x, createExt64);
            SecT571FieldElement secT571FieldElement12 = new SecT571FieldElement(create644);
            SecT571Field.reduce(createExt64, secT571FieldElement12.x);
            if (precompMultiplicand != null) {
                SecT571Field.multiplyPrecomp(secT571FieldElement11.x, precompMultiplicand, secT571FieldElement11.x);
            }
            secT571FieldElement3 = secT571FieldElement12;
            secT571FieldElement2 = secT571FieldElement11;
        }
        return new SecT571R1Point(curve, secT571FieldElement, secT571FieldElement3, new ECFieldElement[]{secT571FieldElement2});
    }

    /* access modifiers changed from: protected */
    @Override // org.bouncycastle.math.ec.ECPoint
    public ECPoint detach() {
        return new SecT571R1Point(null, getAffineXCoord(), getAffineYCoord());
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
        return new SecT571R1Point(this.curve, eCFieldElement, eCFieldElement2.add(eCFieldElement3), new ECFieldElement[]{eCFieldElement3});
    }

    @Override // org.bouncycastle.math.ec.ECPoint
    public ECPoint twice() {
        long[] jArr;
        if (isInfinity()) {
            return this;
        }
        ECCurve curve = getCurve();
        SecT571FieldElement secT571FieldElement = (SecT571FieldElement) this.x;
        if (secT571FieldElement.isZero()) {
            return curve.getInfinity();
        }
        SecT571FieldElement secT571FieldElement2 = (SecT571FieldElement) this.y;
        SecT571FieldElement secT571FieldElement3 = (SecT571FieldElement) this.zs[0];
        long[] create64 = Nat576.create64();
        long[] create642 = Nat576.create64();
        long[] precompMultiplicand = secT571FieldElement3.isOne() ? null : SecT571Field.precompMultiplicand(secT571FieldElement3.x);
        long[] jArr2 = secT571FieldElement2.x;
        if (precompMultiplicand == null) {
            jArr = secT571FieldElement3.x;
        } else {
            SecT571Field.multiplyPrecomp(jArr2, precompMultiplicand, create64);
            SecT571Field.square(secT571FieldElement3.x, create642);
            jArr2 = create64;
            jArr = create642;
        }
        long[] create643 = Nat576.create64();
        SecT571Field.square(secT571FieldElement2.x, create643);
        SecT571Field.addBothTo(jArr2, jArr, create643);
        if (Nat576.isZero64(create643)) {
            return new SecT571R1Point(curve, new SecT571FieldElement(create643), SecT571R1Curve.SecT571R1_B_SQRT);
        }
        long[] createExt64 = Nat576.createExt64();
        SecT571Field.multiplyAddToExt(create643, jArr2, createExt64);
        SecT571FieldElement secT571FieldElement4 = new SecT571FieldElement(create64);
        SecT571Field.square(create643, secT571FieldElement4.x);
        SecT571FieldElement secT571FieldElement5 = new SecT571FieldElement(create643);
        if (precompMultiplicand != null) {
            SecT571Field.multiply(secT571FieldElement5.x, jArr, secT571FieldElement5.x);
        }
        long[] jArr3 = secT571FieldElement.x;
        if (precompMultiplicand != null) {
            SecT571Field.multiplyPrecomp(jArr3, precompMultiplicand, create642);
            jArr3 = create642;
        }
        SecT571Field.squareAddToExt(jArr3, createExt64);
        SecT571Field.reduce(createExt64, create642);
        SecT571Field.addBothTo(secT571FieldElement4.x, secT571FieldElement5.x, create642);
        return new SecT571R1Point(curve, secT571FieldElement4, new SecT571FieldElement(create642), new ECFieldElement[]{secT571FieldElement5});
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
        SecT571FieldElement secT571FieldElement = (SecT571FieldElement) this.x;
        if (secT571FieldElement.isZero()) {
            return eCPoint;
        }
        SecT571FieldElement secT571FieldElement2 = (SecT571FieldElement) eCPoint.getRawXCoord();
        SecT571FieldElement secT571FieldElement3 = (SecT571FieldElement) eCPoint.getZCoord(0);
        if (secT571FieldElement2.isZero() || !secT571FieldElement3.isOne()) {
            return twice().add(eCPoint);
        }
        SecT571FieldElement secT571FieldElement4 = (SecT571FieldElement) this.y;
        SecT571FieldElement secT571FieldElement5 = (SecT571FieldElement) this.zs[0];
        SecT571FieldElement secT571FieldElement6 = (SecT571FieldElement) eCPoint.getRawYCoord();
        long[] create64 = Nat576.create64();
        long[] create642 = Nat576.create64();
        long[] create643 = Nat576.create64();
        long[] create644 = Nat576.create64();
        SecT571Field.square(secT571FieldElement.x, create64);
        SecT571Field.square(secT571FieldElement4.x, create642);
        SecT571Field.square(secT571FieldElement5.x, create643);
        SecT571Field.multiply(secT571FieldElement4.x, secT571FieldElement5.x, create644);
        SecT571Field.addBothTo(create643, create642, create644);
        long[] precompMultiplicand = SecT571Field.precompMultiplicand(create643);
        SecT571Field.multiplyPrecomp(secT571FieldElement6.x, precompMultiplicand, create643);
        SecT571Field.add(create643, create642, create643);
        long[] createExt64 = Nat576.createExt64();
        SecT571Field.multiplyAddToExt(create643, create644, createExt64);
        SecT571Field.multiplyPrecompAddToExt(create64, precompMultiplicand, createExt64);
        SecT571Field.reduce(createExt64, create643);
        SecT571Field.multiplyPrecomp(secT571FieldElement2.x, precompMultiplicand, create64);
        SecT571Field.add(create64, create644, create642);
        SecT571Field.square(create642, create642);
        if (Nat576.isZero64(create642)) {
            return Nat576.isZero64(create643) ? eCPoint.twice() : curve.getInfinity();
        }
        if (Nat576.isZero64(create643)) {
            return new SecT571R1Point(curve, new SecT571FieldElement(create643), SecT571R1Curve.SecT571R1_B_SQRT);
        }
        SecT571FieldElement secT571FieldElement7 = new SecT571FieldElement();
        SecT571Field.square(create643, secT571FieldElement7.x);
        SecT571Field.multiply(secT571FieldElement7.x, create64, secT571FieldElement7.x);
        SecT571FieldElement secT571FieldElement8 = new SecT571FieldElement(create64);
        SecT571Field.multiply(create643, create642, secT571FieldElement8.x);
        SecT571Field.multiplyPrecomp(secT571FieldElement8.x, precompMultiplicand, secT571FieldElement8.x);
        SecT571FieldElement secT571FieldElement9 = new SecT571FieldElement(create642);
        SecT571Field.add(create643, create642, secT571FieldElement9.x);
        SecT571Field.square(secT571FieldElement9.x, secT571FieldElement9.x);
        Nat.zero64(18, createExt64);
        SecT571Field.multiplyAddToExt(secT571FieldElement9.x, create644, createExt64);
        SecT571Field.addOne(secT571FieldElement6.x, create644);
        SecT571Field.multiplyAddToExt(create644, secT571FieldElement8.x, createExt64);
        SecT571Field.reduce(createExt64, secT571FieldElement9.x);
        return new SecT571R1Point(curve, secT571FieldElement7, secT571FieldElement9, new ECFieldElement[]{secT571FieldElement8});
    }
}
