package org.bouncycastle.math.ec;

import java.math.BigInteger;
import java.util.Hashtable;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;

public abstract class ECPoint {
    protected static ECFieldElement[] EMPTY_ZS = new ECFieldElement[0];
    protected ECCurve curve;
    protected Hashtable preCompTable;
    protected boolean withCompression;
    protected ECFieldElement x;
    protected ECFieldElement y;
    protected ECFieldElement[] zs;

    public static abstract class AbstractF2m extends ECPoint {
        protected AbstractF2m(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
            super(eCCurve, eCFieldElement, eCFieldElement2);
        }

        protected AbstractF2m(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr) {
            super(eCCurve, eCFieldElement, eCFieldElement2, eCFieldElementArr);
        }

        /* access modifiers changed from: protected */
        public boolean satisfiesCurveEquation() {
            ECFieldElement eCFieldElement;
            ECFieldElement eCFieldElement2;
            ECCurve curve = getCurve();
            ECFieldElement eCFieldElement3 = this.x;
            ECFieldElement a = curve.getA();
            ECFieldElement b = curve.getB();
            int coordinateSystem = curve.getCoordinateSystem();
            if (coordinateSystem == 6) {
                ECFieldElement eCFieldElement4 = this.zs[0];
                boolean isOne = eCFieldElement4.isOne();
                if (eCFieldElement3.isZero()) {
                    ECFieldElement square = this.y.square();
                    if (!isOne) {
                        b = b.multiply(eCFieldElement4.square());
                    }
                    return square.equals(b);
                }
                ECFieldElement eCFieldElement5 = this.y;
                ECFieldElement square2 = eCFieldElement3.square();
                if (isOne) {
                    eCFieldElement2 = eCFieldElement5.square().add(eCFieldElement5).add(a);
                    eCFieldElement = square2.square().add(b);
                } else {
                    ECFieldElement square3 = eCFieldElement4.square();
                    ECFieldElement square4 = square3.square();
                    eCFieldElement2 = eCFieldElement5.add(eCFieldElement4).multiplyPlusProduct(eCFieldElement5, a, square3);
                    eCFieldElement = square2.squarePlusProduct(b, square4);
                }
                return eCFieldElement2.multiply(square2).equals(eCFieldElement);
            }
            ECFieldElement eCFieldElement6 = this.y;
            ECFieldElement multiply = eCFieldElement6.add(eCFieldElement3).multiply(eCFieldElement6);
            switch (coordinateSystem) {
                case 0:
                    break;
                case 1:
                    ECFieldElement eCFieldElement7 = this.zs[0];
                    if (!eCFieldElement7.isOne()) {
                        ECFieldElement multiply2 = eCFieldElement7.multiply(eCFieldElement7.square());
                        multiply = multiply.multiply(eCFieldElement7);
                        a = a.multiply(eCFieldElement7);
                        b = b.multiply(multiply2);
                        break;
                    }
                    break;
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
            return multiply.equals(eCFieldElement3.add(a).multiply(eCFieldElement3.square()).add(b));
        }

        /* access modifiers changed from: protected */
        public boolean satisfiesOrder() {
            BigInteger cofactor = this.curve.getCofactor();
            boolean z = true;
            if (ECConstants.TWO.equals(cofactor)) {
                return ((ECFieldElement.AbstractF2m) normalize().getAffineXCoord().add(this.curve.getA())).trace() == 0;
            }
            if (!ECConstants.FOUR.equals(cofactor)) {
                return ECPoint.super.satisfiesOrder();
            }
            ECPoint normalize = normalize();
            ECFieldElement affineXCoord = normalize.getAffineXCoord();
            ECFieldElement solveQuadraticEquation = ((ECCurve.AbstractF2m) this.curve).solveQuadraticEquation(affineXCoord.add(this.curve.getA()));
            if (solveQuadraticEquation == null) {
                return false;
            }
            ECFieldElement add = affineXCoord.multiply(solveQuadraticEquation).add(normalize.getAffineYCoord()).add(this.curve.getA());
            if (((ECFieldElement.AbstractF2m) add).trace() != 0) {
                if (((ECFieldElement.AbstractF2m) add.add(affineXCoord)).trace() == 0) {
                    return true;
                }
                z = false;
            }
            return z;
        }

        public ECPoint scaleX(ECFieldElement eCFieldElement) {
            if (isInfinity()) {
                return this;
            }
            switch (getCurveCoordinateSystem()) {
                case 5:
                    ECFieldElement rawXCoord = getRawXCoord();
                    ECFieldElement rawYCoord = getRawYCoord();
                    return getCurve().createRawPoint(rawXCoord, rawYCoord.add(rawXCoord).divide(eCFieldElement).add(rawXCoord.multiply(eCFieldElement)), getRawZCoords(), this.withCompression);
                case 6:
                    ECFieldElement rawXCoord2 = getRawXCoord();
                    ECFieldElement rawYCoord2 = getRawYCoord();
                    ECFieldElement eCFieldElement2 = getRawZCoords()[0];
                    ECFieldElement multiply = rawXCoord2.multiply(eCFieldElement.square());
                    ECFieldElement add = rawYCoord2.add(rawXCoord2).add(multiply);
                    ECFieldElement multiply2 = eCFieldElement2.multiply(eCFieldElement);
                    return getCurve().createRawPoint(multiply, add, new ECFieldElement[]{multiply2}, this.withCompression);
                default:
                    return ECPoint.super.scaleX(eCFieldElement);
            }
        }

        public ECPoint scaleY(ECFieldElement eCFieldElement) {
            if (isInfinity()) {
                return this;
            }
            switch (getCurveCoordinateSystem()) {
                case 5:
                case 6:
                    ECFieldElement rawXCoord = getRawXCoord();
                    return getCurve().createRawPoint(rawXCoord, getRawYCoord().add(rawXCoord).multiply(eCFieldElement).add(rawXCoord), getRawZCoords(), this.withCompression);
                default:
                    return ECPoint.super.scaleY(eCFieldElement);
            }
        }

        public ECPoint subtract(ECPoint eCPoint) {
            return eCPoint.isInfinity() ? this : add(eCPoint.negate());
        }

        public AbstractF2m tau() {
            ECPoint createRawPoint;
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            int coordinateSystem = curve.getCoordinateSystem();
            ECFieldElement eCFieldElement = this.x;
            switch (coordinateSystem) {
                case 0:
                case 5:
                    createRawPoint = curve.createRawPoint(eCFieldElement.square(), this.y.square(), this.withCompression);
                    break;
                case 1:
                case 6:
                    ECFieldElement eCFieldElement2 = this.y;
                    ECFieldElement eCFieldElement3 = this.zs[0];
                    createRawPoint = curve.createRawPoint(eCFieldElement.square(), eCFieldElement2.square(), new ECFieldElement[]{eCFieldElement3.square()}, this.withCompression);
                    break;
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
            return (AbstractF2m) createRawPoint;
        }

        public AbstractF2m tauPow(int i) {
            ECPoint createRawPoint;
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            int coordinateSystem = curve.getCoordinateSystem();
            ECFieldElement eCFieldElement = this.x;
            switch (coordinateSystem) {
                case 0:
                case 5:
                    createRawPoint = curve.createRawPoint(eCFieldElement.squarePow(i), this.y.squarePow(i), this.withCompression);
                    break;
                case 1:
                case 6:
                    ECFieldElement eCFieldElement2 = this.y;
                    ECFieldElement eCFieldElement3 = this.zs[0];
                    createRawPoint = curve.createRawPoint(eCFieldElement.squarePow(i), eCFieldElement2.squarePow(i), new ECFieldElement[]{eCFieldElement3.squarePow(i)}, this.withCompression);
                    break;
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
            return (AbstractF2m) createRawPoint;
        }
    }

    public static abstract class AbstractFp extends ECPoint {
        protected AbstractFp(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
            super(eCCurve, eCFieldElement, eCFieldElement2);
        }

        protected AbstractFp(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr) {
            super(eCCurve, eCFieldElement, eCFieldElement2, eCFieldElementArr);
        }

        /* access modifiers changed from: protected */
        public boolean getCompressionYTilde() {
            return getAffineYCoord().testBitZero();
        }

        /* access modifiers changed from: protected */
        public boolean satisfiesCurveEquation() {
            ECFieldElement eCFieldElement = this.x;
            ECFieldElement eCFieldElement2 = this.y;
            ECFieldElement a = this.curve.getA();
            ECFieldElement b = this.curve.getB();
            ECFieldElement square = eCFieldElement2.square();
            switch (getCurveCoordinateSystem()) {
                case 0:
                    break;
                case 1:
                    ECFieldElement eCFieldElement3 = this.zs[0];
                    if (!eCFieldElement3.isOne()) {
                        ECFieldElement square2 = eCFieldElement3.square();
                        ECFieldElement multiply = eCFieldElement3.multiply(square2);
                        square = square.multiply(eCFieldElement3);
                        a = a.multiply(square2);
                        b = b.multiply(multiply);
                        break;
                    }
                    break;
                case 2:
                case 3:
                case 4:
                    ECFieldElement eCFieldElement4 = this.zs[0];
                    if (!eCFieldElement4.isOne()) {
                        ECFieldElement square3 = eCFieldElement4.square();
                        ECFieldElement square4 = square3.square();
                        ECFieldElement multiply2 = square3.multiply(square4);
                        a = a.multiply(square4);
                        b = b.multiply(multiply2);
                        break;
                    }
                    break;
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
            return square.equals(eCFieldElement.square().add(a).multiply(eCFieldElement).add(b));
        }

        public ECPoint subtract(ECPoint eCPoint) {
            return eCPoint.isInfinity() ? this : add(eCPoint.negate());
        }
    }

    public static class F2m extends AbstractF2m {
        public F2m(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, boolean z) {
            super(eCCurve, eCFieldElement, eCFieldElement2);
            boolean z2 = false;
            if ((eCFieldElement == null) == (eCFieldElement2 == null ? true : z2)) {
                if (eCFieldElement != null) {
                    ECFieldElement.F2m.checkFieldElements(this.x, this.y);
                    if (eCCurve != null) {
                        ECFieldElement.F2m.checkFieldElements(this.x, this.curve.getA());
                    }
                }
                this.withCompression = z;
                return;
            }
            throw new IllegalArgumentException("Exactly one of the field elements is null");
        }

        F2m(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr, boolean z) {
            super(eCCurve, eCFieldElement, eCFieldElement2, eCFieldElementArr);
            this.withCompression = z;
        }

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
            int coordinateSystem = curve.getCoordinateSystem();
            ECFieldElement eCFieldElement7 = this.x;
            ECFieldElement eCFieldElement8 = eCPoint.x;
            if (coordinateSystem != 6) {
                switch (coordinateSystem) {
                    case 0:
                        ECFieldElement eCFieldElement9 = this.y;
                        ECFieldElement eCFieldElement10 = eCPoint.y;
                        ECFieldElement add = eCFieldElement7.add(eCFieldElement8);
                        ECFieldElement add2 = eCFieldElement9.add(eCFieldElement10);
                        if (add.isZero()) {
                            return add2.isZero() ? twice() : curve.getInfinity();
                        }
                        ECFieldElement divide = add2.divide(add);
                        ECFieldElement add3 = divide.square().add(divide).add(add).add(curve.getA());
                        return new F2m(curve, add3, divide.multiply(eCFieldElement7.add(add3)).add(add3).add(eCFieldElement9), this.withCompression);
                    case 1:
                        ECFieldElement eCFieldElement11 = this.y;
                        ECFieldElement eCFieldElement12 = this.zs[0];
                        ECFieldElement eCFieldElement13 = eCPoint.y;
                        ECFieldElement eCFieldElement14 = eCPoint.zs[0];
                        boolean isOne = eCFieldElement14.isOne();
                        ECFieldElement add4 = eCFieldElement12.multiply(eCFieldElement13).add(isOne ? eCFieldElement11 : eCFieldElement11.multiply(eCFieldElement14));
                        ECFieldElement add5 = eCFieldElement12.multiply(eCFieldElement8).add(isOne ? eCFieldElement7 : eCFieldElement7.multiply(eCFieldElement14));
                        if (add5.isZero()) {
                            return add4.isZero() ? twice() : curve.getInfinity();
                        }
                        ECFieldElement square = add5.square();
                        ECFieldElement multiply = square.multiply(add5);
                        if (!isOne) {
                            eCFieldElement12 = eCFieldElement12.multiply(eCFieldElement14);
                        }
                        ECFieldElement add6 = add4.add(add5);
                        ECFieldElement add7 = add6.multiplyPlusProduct(add4, square, curve.getA()).multiply(eCFieldElement12).add(multiply);
                        ECFieldElement multiply2 = add5.multiply(add7);
                        if (!isOne) {
                            square = square.multiply(eCFieldElement14);
                        }
                        F2m f2m = new F2m(curve, multiply2, add4.multiplyPlusProduct(eCFieldElement7, add5, eCFieldElement11).multiplyPlusProduct(square, add6, add7), new ECFieldElement[]{multiply.multiply(eCFieldElement12)}, this.withCompression);
                        return f2m;
                    default:
                        throw new IllegalStateException("unsupported coordinate system");
                }
            } else if (eCFieldElement7.isZero()) {
                return eCFieldElement8.isZero() ? curve.getInfinity() : eCPoint.add(this);
            } else {
                ECFieldElement eCFieldElement15 = this.y;
                ECFieldElement eCFieldElement16 = this.zs[0];
                ECFieldElement eCFieldElement17 = eCPoint.y;
                ECFieldElement eCFieldElement18 = eCPoint.zs[0];
                boolean isOne2 = eCFieldElement16.isOne();
                if (!isOne2) {
                    eCFieldElement2 = eCFieldElement8.multiply(eCFieldElement16);
                    eCFieldElement = eCFieldElement17.multiply(eCFieldElement16);
                } else {
                    eCFieldElement2 = eCFieldElement8;
                    eCFieldElement = eCFieldElement17;
                }
                boolean isOne3 = eCFieldElement18.isOne();
                if (!isOne3) {
                    eCFieldElement7 = eCFieldElement7.multiply(eCFieldElement18);
                    eCFieldElement3 = eCFieldElement15.multiply(eCFieldElement18);
                } else {
                    eCFieldElement3 = eCFieldElement15;
                }
                ECFieldElement add8 = eCFieldElement3.add(eCFieldElement);
                ECFieldElement add9 = eCFieldElement7.add(eCFieldElement2);
                if (add9.isZero()) {
                    return add8.isZero() ? twice() : curve.getInfinity();
                }
                if (eCFieldElement8.isZero()) {
                    ECPoint normalize = normalize();
                    ECFieldElement xCoord = normalize.getXCoord();
                    ECFieldElement yCoord = normalize.getYCoord();
                    ECFieldElement divide2 = yCoord.add(eCFieldElement17).divide(xCoord);
                    eCFieldElement6 = divide2.square().add(divide2).add(xCoord).add(curve.getA());
                    if (eCFieldElement6.isZero()) {
                        return new F2m(curve, eCFieldElement6, curve.getB().sqrt(), this.withCompression);
                    }
                    eCFieldElement5 = divide2.multiply(xCoord.add(eCFieldElement6)).add(eCFieldElement6).add(yCoord).divide(eCFieldElement6).add(eCFieldElement6);
                    eCFieldElement4 = curve.fromBigInteger(ECConstants.ONE);
                } else {
                    ECFieldElement square2 = add9.square();
                    ECFieldElement multiply3 = add8.multiply(eCFieldElement7);
                    ECFieldElement multiply4 = add8.multiply(eCFieldElement2);
                    ECFieldElement multiply5 = multiply3.multiply(multiply4);
                    if (multiply5.isZero()) {
                        return new F2m(curve, multiply5, curve.getB().sqrt(), this.withCompression);
                    }
                    ECFieldElement multiply6 = add8.multiply(square2);
                    eCFieldElement4 = !isOne3 ? multiply6.multiply(eCFieldElement18) : multiply6;
                    ECFieldElement squarePlusProduct = multiply4.add(square2).squarePlusProduct(eCFieldElement4, eCFieldElement15.add(eCFieldElement16));
                    if (!isOne2) {
                        eCFieldElement4 = eCFieldElement4.multiply(eCFieldElement16);
                    }
                    eCFieldElement5 = squarePlusProduct;
                    eCFieldElement6 = multiply5;
                }
                F2m f2m2 = new F2m(curve, eCFieldElement6, eCFieldElement5, new ECFieldElement[]{eCFieldElement4}, this.withCompression);
                return f2m2;
            }
        }

        /* access modifiers changed from: protected */
        public ECPoint detach() {
            return new F2m(null, getAffineXCoord(), getAffineYCoord(), false);
        }

        /* access modifiers changed from: protected */
        public boolean getCompressionYTilde() {
            ECFieldElement rawXCoord = getRawXCoord();
            boolean z = false;
            if (rawXCoord.isZero()) {
                return false;
            }
            ECFieldElement rawYCoord = getRawYCoord();
            switch (getCurveCoordinateSystem()) {
                case 5:
                case 6:
                    if (rawYCoord.testBitZero() != rawXCoord.testBitZero()) {
                        z = true;
                    }
                    return z;
                default:
                    return rawYCoord.divide(rawXCoord).testBitZero();
            }
        }

        public ECFieldElement getYCoord() {
            int curveCoordinateSystem = getCurveCoordinateSystem();
            switch (curveCoordinateSystem) {
                case 5:
                case 6:
                    ECFieldElement eCFieldElement = this.x;
                    ECFieldElement eCFieldElement2 = this.y;
                    if (isInfinity() || eCFieldElement.isZero()) {
                        return eCFieldElement2;
                    }
                    ECFieldElement multiply = eCFieldElement2.add(eCFieldElement).multiply(eCFieldElement);
                    if (6 == curveCoordinateSystem) {
                        ECFieldElement eCFieldElement3 = this.zs[0];
                        if (!eCFieldElement3.isOne()) {
                            multiply = multiply.divide(eCFieldElement3);
                        }
                    }
                    return multiply;
                default:
                    return this.y;
            }
        }

        public ECPoint negate() {
            if (isInfinity()) {
                return this;
            }
            ECFieldElement eCFieldElement = this.x;
            if (eCFieldElement.isZero()) {
                return this;
            }
            switch (getCurveCoordinateSystem()) {
                case 0:
                    return new F2m(this.curve, eCFieldElement, this.y.add(eCFieldElement), this.withCompression);
                case 1:
                    ECFieldElement eCFieldElement2 = this.y;
                    ECFieldElement eCFieldElement3 = this.zs[0];
                    F2m f2m = new F2m(this.curve, eCFieldElement, eCFieldElement2.add(eCFieldElement), new ECFieldElement[]{eCFieldElement3}, this.withCompression);
                    return f2m;
                case 5:
                    return new F2m(this.curve, eCFieldElement, this.y.addOne(), this.withCompression);
                case 6:
                    ECFieldElement eCFieldElement4 = this.y;
                    ECFieldElement eCFieldElement5 = this.zs[0];
                    F2m f2m2 = new F2m(this.curve, eCFieldElement, eCFieldElement4.add(eCFieldElement5), new ECFieldElement[]{eCFieldElement5}, this.withCompression);
                    return f2m2;
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
        }

        public ECPoint twice() {
            ECFieldElement eCFieldElement;
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            ECFieldElement eCFieldElement2 = this.x;
            if (eCFieldElement2.isZero()) {
                return curve.getInfinity();
            }
            int coordinateSystem = curve.getCoordinateSystem();
            if (coordinateSystem != 6) {
                switch (coordinateSystem) {
                    case 0:
                        ECFieldElement add = this.y.divide(eCFieldElement2).add(eCFieldElement2);
                        ECFieldElement add2 = add.square().add(add).add(curve.getA());
                        return new F2m(curve, add2, eCFieldElement2.squarePlusProduct(add2, add.addOne()), this.withCompression);
                    case 1:
                        ECFieldElement eCFieldElement3 = this.y;
                        ECFieldElement eCFieldElement4 = this.zs[0];
                        boolean isOne = eCFieldElement4.isOne();
                        ECFieldElement multiply = isOne ? eCFieldElement2 : eCFieldElement2.multiply(eCFieldElement4);
                        if (!isOne) {
                            eCFieldElement3 = eCFieldElement3.multiply(eCFieldElement4);
                        }
                        ECFieldElement square = eCFieldElement2.square();
                        ECFieldElement add3 = square.add(eCFieldElement3);
                        ECFieldElement square2 = multiply.square();
                        ECFieldElement add4 = add3.add(multiply);
                        ECFieldElement multiplyPlusProduct = add4.multiplyPlusProduct(add3, square2, curve.getA());
                        F2m f2m = new F2m(curve, multiply.multiply(multiplyPlusProduct), square.square().multiplyPlusProduct(multiply, multiplyPlusProduct, add4), new ECFieldElement[]{multiply.multiply(square2)}, this.withCompression);
                        return f2m;
                    default:
                        throw new IllegalStateException("unsupported coordinate system");
                }
            } else {
                ECFieldElement eCFieldElement5 = this.y;
                ECFieldElement eCFieldElement6 = this.zs[0];
                boolean isOne2 = eCFieldElement6.isOne();
                ECFieldElement multiply2 = isOne2 ? eCFieldElement5 : eCFieldElement5.multiply(eCFieldElement6);
                ECFieldElement square3 = isOne2 ? eCFieldElement6 : eCFieldElement6.square();
                ECFieldElement a = curve.getA();
                ECFieldElement multiply3 = isOne2 ? a : a.multiply(square3);
                ECFieldElement add5 = eCFieldElement5.square().add(multiply2).add(multiply3);
                if (add5.isZero()) {
                    return new F2m(curve, add5, curve.getB().sqrt(), this.withCompression);
                }
                ECFieldElement square4 = add5.square();
                ECFieldElement multiply4 = isOne2 ? add5 : add5.multiply(square3);
                ECFieldElement b = curve.getB();
                ECCurve eCCurve = curve;
                if (b.bitLength() < (curve.getFieldSize() >> 1)) {
                    ECFieldElement square5 = eCFieldElement5.add(eCFieldElement2).square();
                    eCFieldElement = square5.add(add5).add(square3).multiply(square5).add(b.isOne() ? multiply3.add(square3).square() : multiply3.squarePlusProduct(b, square3.square())).add(square4);
                    if (!a.isZero()) {
                        if (!a.isOne()) {
                            eCFieldElement = eCFieldElement.add(a.addOne().multiply(multiply4));
                        }
                        F2m f2m2 = new F2m(eCCurve, square4, eCFieldElement, new ECFieldElement[]{multiply4}, this.withCompression);
                        return f2m2;
                    }
                } else {
                    if (!isOne2) {
                        eCFieldElement2 = eCFieldElement2.multiply(eCFieldElement6);
                    }
                    eCFieldElement = eCFieldElement2.squarePlusProduct(add5, multiply2).add(square4);
                }
                eCFieldElement = eCFieldElement.add(multiply4);
                F2m f2m22 = new F2m(eCCurve, square4, eCFieldElement, new ECFieldElement[]{multiply4}, this.withCompression);
                return f2m22;
            }
        }

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
            if (curve.getCoordinateSystem() != 6) {
                return twice().add(eCPoint);
            }
            ECFieldElement eCFieldElement2 = eCPoint.x;
            ECFieldElement eCFieldElement3 = eCPoint.zs[0];
            if (eCFieldElement2.isZero() || !eCFieldElement3.isOne()) {
                return twice().add(eCPoint);
            }
            ECFieldElement eCFieldElement4 = this.y;
            ECFieldElement eCFieldElement5 = this.zs[0];
            ECFieldElement eCFieldElement6 = eCPoint.y;
            ECFieldElement square = eCFieldElement.square();
            ECFieldElement square2 = eCFieldElement4.square();
            ECFieldElement square3 = eCFieldElement5.square();
            ECFieldElement add = curve.getA().multiply(square3).add(square2).add(eCFieldElement4.multiply(eCFieldElement5));
            ECFieldElement addOne = eCFieldElement6.addOne();
            ECFieldElement multiplyPlusProduct = curve.getA().add(addOne).multiply(square3).add(square2).multiplyPlusProduct(add, square, square3);
            ECFieldElement multiply = eCFieldElement2.multiply(square3);
            ECFieldElement square4 = multiply.add(add).square();
            if (square4.isZero()) {
                return multiplyPlusProduct.isZero() ? eCPoint.twice() : curve.getInfinity();
            }
            if (multiplyPlusProduct.isZero()) {
                return new F2m(curve, multiplyPlusProduct, curve.getB().sqrt(), this.withCompression);
            }
            ECFieldElement multiply2 = multiplyPlusProduct.square().multiply(multiply);
            ECFieldElement multiply3 = multiplyPlusProduct.multiply(square4).multiply(square3);
            F2m f2m = new F2m(curve, multiply2, multiplyPlusProduct.add(square4).square().multiplyPlusProduct(add, addOne, multiply3), new ECFieldElement[]{multiply3}, this.withCompression);
            return f2m;
        }
    }

    public static class Fp extends AbstractFp {
        public Fp(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, boolean z) {
            super(eCCurve, eCFieldElement, eCFieldElement2);
            boolean z2 = false;
            if ((eCFieldElement == null) == (eCFieldElement2 == null ? true : z2)) {
                this.withCompression = z;
                return;
            }
            throw new IllegalArgumentException("Exactly one of the field elements is null");
        }

        Fp(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr, boolean z) {
            super(eCCurve, eCFieldElement, eCFieldElement2, eCFieldElementArr);
            this.withCompression = z;
        }

        /* JADX WARNING: type inference failed for: r18v0, types: [org.bouncycastle.math.ec.ECPoint] */
        /* JADX WARNING: Removed duplicated region for block: B:85:0x01f6  */
        /* JADX WARNING: Removed duplicated region for block: B:86:0x0204  */
        /* JADX WARNING: Unknown variable types count: 1 */
        public ECPoint add(ECPoint r18) {
            ECFieldElement eCFieldElement;
            ECFieldElement eCFieldElement2;
            ECFieldElement eCFieldElement3;
            ECFieldElement eCFieldElement4;
            Fp fp = r18;
            if (isInfinity()) {
                return fp;
            }
            if (r18.isInfinity()) {
                return this;
            }
            if (this == fp) {
                return twice();
            }
            ECCurve curve = getCurve();
            int coordinateSystem = curve.getCoordinateSystem();
            ECFieldElement eCFieldElement5 = this.x;
            ECFieldElement eCFieldElement6 = this.y;
            ECFieldElement eCFieldElement7 = fp.x;
            ECFieldElement eCFieldElement8 = fp.y;
            if (coordinateSystem != 4) {
                switch (coordinateSystem) {
                    case 0:
                        ECFieldElement subtract = eCFieldElement7.subtract(eCFieldElement5);
                        ECFieldElement subtract2 = eCFieldElement8.subtract(eCFieldElement6);
                        if (subtract.isZero()) {
                            return subtract2.isZero() ? twice() : curve.getInfinity();
                        }
                        ECFieldElement divide = subtract2.divide(subtract);
                        ECFieldElement subtract3 = divide.square().subtract(eCFieldElement5).subtract(eCFieldElement7);
                        return new Fp(curve, subtract3, divide.multiply(eCFieldElement5.subtract(subtract3)).subtract(eCFieldElement6), this.withCompression);
                    case 1:
                        ECFieldElement eCFieldElement9 = this.zs[0];
                        ECFieldElement eCFieldElement10 = fp.zs[0];
                        boolean isOne = eCFieldElement9.isOne();
                        boolean isOne2 = eCFieldElement10.isOne();
                        if (!isOne) {
                            eCFieldElement8 = eCFieldElement8.multiply(eCFieldElement9);
                        }
                        if (!isOne2) {
                            eCFieldElement6 = eCFieldElement6.multiply(eCFieldElement10);
                        }
                        ECFieldElement subtract4 = eCFieldElement8.subtract(eCFieldElement6);
                        if (!isOne) {
                            eCFieldElement7 = eCFieldElement7.multiply(eCFieldElement9);
                        }
                        if (!isOne2) {
                            eCFieldElement5 = eCFieldElement5.multiply(eCFieldElement10);
                        }
                        ECFieldElement subtract5 = eCFieldElement7.subtract(eCFieldElement5);
                        if (subtract5.isZero()) {
                            return subtract4.isZero() ? twice() : curve.getInfinity();
                        }
                        if (isOne) {
                            eCFieldElement9 = eCFieldElement10;
                        } else if (!isOne2) {
                            eCFieldElement9 = eCFieldElement9.multiply(eCFieldElement10);
                        }
                        ECFieldElement square = subtract5.square();
                        ECFieldElement multiply = square.multiply(subtract5);
                        ECFieldElement multiply2 = square.multiply(eCFieldElement5);
                        ECFieldElement subtract6 = subtract4.square().multiply(eCFieldElement9).subtract(multiply).subtract(two(multiply2));
                        Fp fp2 = new Fp(curve, subtract5.multiply(subtract6), multiply2.subtract(subtract6).multiplyMinusProduct(subtract4, eCFieldElement6, multiply), new ECFieldElement[]{multiply.multiply(eCFieldElement9)}, this.withCompression);
                        return fp2;
                    case 2:
                        break;
                    default:
                        throw new IllegalStateException("unsupported coordinate system");
                }
            }
            ECFieldElement eCFieldElement11 = this.zs[0];
            ECFieldElement eCFieldElement12 = fp.zs[0];
            boolean isOne3 = eCFieldElement11.isOne();
            if (isOne3 || !eCFieldElement11.equals(eCFieldElement12)) {
                if (!isOne3) {
                    ECFieldElement square2 = eCFieldElement11.square();
                    eCFieldElement7 = square2.multiply(eCFieldElement7);
                    eCFieldElement8 = square2.multiply(eCFieldElement11).multiply(eCFieldElement8);
                }
                boolean isOne4 = eCFieldElement12.isOne();
                if (!isOne4) {
                    ECFieldElement square3 = eCFieldElement12.square();
                    eCFieldElement5 = square3.multiply(eCFieldElement5);
                    eCFieldElement6 = square3.multiply(eCFieldElement12).multiply(eCFieldElement6);
                }
                ECFieldElement subtract7 = eCFieldElement5.subtract(eCFieldElement7);
                ECFieldElement subtract8 = eCFieldElement6.subtract(eCFieldElement8);
                if (subtract7.isZero()) {
                    return subtract8.isZero() ? twice() : curve.getInfinity();
                }
                ECFieldElement square4 = subtract7.square();
                ECFieldElement multiply3 = square4.multiply(subtract7);
                ECFieldElement multiply4 = square4.multiply(eCFieldElement5);
                eCFieldElement2 = subtract8.square().add(multiply3).subtract(two(multiply4));
                eCFieldElement3 = multiply4.subtract(eCFieldElement2).multiplyMinusProduct(subtract8, multiply3, eCFieldElement6);
                ECFieldElement multiply5 = !isOne3 ? subtract7.multiply(eCFieldElement11) : subtract7;
                eCFieldElement4 = !isOne4 ? multiply5.multiply(eCFieldElement12) : multiply5;
                if (eCFieldElement4 == subtract7) {
                    eCFieldElement = square4;
                    Fp fp3 = new Fp(curve, eCFieldElement2, eCFieldElement3, coordinateSystem != 4 ? new ECFieldElement[]{eCFieldElement4, calculateJacobianModifiedW(eCFieldElement4, eCFieldElement)} : new ECFieldElement[]{eCFieldElement4}, this.withCompression);
                    return fp3;
                }
            } else {
                ECFieldElement subtract9 = eCFieldElement5.subtract(eCFieldElement7);
                ECFieldElement subtract10 = eCFieldElement6.subtract(eCFieldElement8);
                if (subtract9.isZero()) {
                    return subtract10.isZero() ? twice() : curve.getInfinity();
                }
                ECFieldElement square5 = subtract9.square();
                ECFieldElement multiply6 = eCFieldElement5.multiply(square5);
                ECFieldElement multiply7 = eCFieldElement7.multiply(square5);
                ECFieldElement multiply8 = multiply6.subtract(multiply7).multiply(eCFieldElement6);
                ECFieldElement subtract11 = subtract10.square().subtract(multiply6).subtract(multiply7);
                eCFieldElement3 = multiply6.subtract(subtract11).multiply(subtract10).subtract(multiply8);
                eCFieldElement4 = subtract9.multiply(eCFieldElement11);
                eCFieldElement2 = subtract11;
            }
            eCFieldElement = null;
            Fp fp32 = new Fp(curve, eCFieldElement2, eCFieldElement3, coordinateSystem != 4 ? new ECFieldElement[]{eCFieldElement4, calculateJacobianModifiedW(eCFieldElement4, eCFieldElement)} : new ECFieldElement[]{eCFieldElement4}, this.withCompression);
            return fp32;
        }

        /* access modifiers changed from: protected */
        public ECFieldElement calculateJacobianModifiedW(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
            ECFieldElement a = getCurve().getA();
            if (a.isZero() || eCFieldElement.isOne()) {
                return a;
            }
            if (eCFieldElement2 == null) {
                eCFieldElement2 = eCFieldElement.square();
            }
            ECFieldElement square = eCFieldElement2.square();
            ECFieldElement negate = a.negate();
            return negate.bitLength() < a.bitLength() ? square.multiply(negate).negate() : square.multiply(a);
        }

        /* access modifiers changed from: protected */
        public ECPoint detach() {
            return new Fp(null, getAffineXCoord(), getAffineYCoord(), false);
        }

        /* access modifiers changed from: protected */
        public ECFieldElement doubleProductFromSquares(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement eCFieldElement3, ECFieldElement eCFieldElement4) {
            return eCFieldElement.add(eCFieldElement2).square().subtract(eCFieldElement3).subtract(eCFieldElement4);
        }

        /* access modifiers changed from: protected */
        public ECFieldElement eight(ECFieldElement eCFieldElement) {
            return four(two(eCFieldElement));
        }

        /* access modifiers changed from: protected */
        public ECFieldElement four(ECFieldElement eCFieldElement) {
            return two(two(eCFieldElement));
        }

        /* access modifiers changed from: protected */
        public ECFieldElement getJacobianModifiedW() {
            ECFieldElement eCFieldElement = this.zs[1];
            if (eCFieldElement != null) {
                return eCFieldElement;
            }
            ECFieldElement[] eCFieldElementArr = this.zs;
            ECFieldElement calculateJacobianModifiedW = calculateJacobianModifiedW(this.zs[0], null);
            eCFieldElementArr[1] = calculateJacobianModifiedW;
            return calculateJacobianModifiedW;
        }

        public ECFieldElement getZCoord(int i) {
            return (i == 1 && 4 == getCurveCoordinateSystem()) ? getJacobianModifiedW() : super.getZCoord(i);
        }

        public ECPoint negate() {
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            if (curve.getCoordinateSystem() == 0) {
                return new Fp(curve, this.x, this.y.negate(), this.withCompression);
            }
            Fp fp = new Fp(curve, this.x, this.y.negate(), this.zs, this.withCompression);
            return fp;
        }

        /* access modifiers changed from: protected */
        public ECFieldElement three(ECFieldElement eCFieldElement) {
            return two(eCFieldElement).add(eCFieldElement);
        }

        public ECPoint threeTimes() {
            if (isInfinity()) {
                return this;
            }
            ECFieldElement eCFieldElement = this.y;
            if (eCFieldElement.isZero()) {
                return this;
            }
            ECCurve curve = getCurve();
            int coordinateSystem = curve.getCoordinateSystem();
            if (coordinateSystem != 0) {
                return coordinateSystem != 4 ? twice().add(this) : twiceJacobianModified(false).add(this);
            }
            ECFieldElement eCFieldElement2 = this.x;
            ECFieldElement two = two(eCFieldElement);
            ECFieldElement square = two.square();
            ECFieldElement add = three(eCFieldElement2.square()).add(getCurve().getA());
            ECFieldElement subtract = three(eCFieldElement2).multiply(square).subtract(add.square());
            if (subtract.isZero()) {
                return getCurve().getInfinity();
            }
            ECFieldElement invert = subtract.multiply(two).invert();
            ECFieldElement multiply = subtract.multiply(invert).multiply(add);
            ECFieldElement subtract2 = square.square().multiply(invert).subtract(multiply);
            ECFieldElement add2 = subtract2.subtract(multiply).multiply(multiply.add(subtract2)).add(eCFieldElement2);
            return new Fp(curve, add2, eCFieldElement2.subtract(add2).multiply(subtract2).subtract(eCFieldElement), this.withCompression);
        }

        public ECPoint timesPow2(int i) {
            ECFieldElement eCFieldElement;
            if (i < 0) {
                throw new IllegalArgumentException("'e' cannot be negative");
            } else if (i == 0 || isInfinity()) {
                return this;
            } else {
                if (i == 1) {
                    return twice();
                }
                ECCurve curve = getCurve();
                ECFieldElement eCFieldElement2 = this.y;
                if (eCFieldElement2.isZero()) {
                    return curve.getInfinity();
                }
                int coordinateSystem = curve.getCoordinateSystem();
                ECFieldElement a = curve.getA();
                ECFieldElement eCFieldElement3 = this.x;
                ECFieldElement fromBigInteger = this.zs.length < 1 ? curve.fromBigInteger(ECConstants.ONE) : this.zs[0];
                if (!fromBigInteger.isOne()) {
                    if (coordinateSystem != 4) {
                        switch (coordinateSystem) {
                            case 0:
                                break;
                            case 1:
                                eCFieldElement = fromBigInteger.square();
                                eCFieldElement3 = eCFieldElement3.multiply(fromBigInteger);
                                eCFieldElement2 = eCFieldElement2.multiply(eCFieldElement);
                                break;
                            case 2:
                                eCFieldElement = null;
                                break;
                            default:
                                throw new IllegalStateException("unsupported coordinate system");
                        }
                        a = calculateJacobianModifiedW(fromBigInteger, eCFieldElement);
                    } else {
                        a = getJacobianModifiedW();
                    }
                }
                ECFieldElement eCFieldElement4 = fromBigInteger;
                ECFieldElement eCFieldElement5 = a;
                ECFieldElement eCFieldElement6 = eCFieldElement2;
                int i2 = 0;
                while (i2 < i) {
                    if (eCFieldElement6.isZero()) {
                        return curve.getInfinity();
                    }
                    ECFieldElement three = three(eCFieldElement3.square());
                    ECFieldElement two = two(eCFieldElement6);
                    ECFieldElement multiply = two.multiply(eCFieldElement6);
                    ECFieldElement two2 = two(eCFieldElement3.multiply(multiply));
                    ECFieldElement two3 = two(multiply.square());
                    if (!eCFieldElement5.isZero()) {
                        three = three.add(eCFieldElement5);
                        eCFieldElement5 = two(two3.multiply(eCFieldElement5));
                    }
                    ECFieldElement subtract = three.square().subtract(two(two2));
                    eCFieldElement6 = three.multiply(two2.subtract(subtract)).subtract(two3);
                    eCFieldElement4 = eCFieldElement4.isOne() ? two : two.multiply(eCFieldElement4);
                    i2++;
                    eCFieldElement3 = subtract;
                }
                if (coordinateSystem != 4) {
                    switch (coordinateSystem) {
                        case 0:
                            ECFieldElement invert = eCFieldElement4.invert();
                            ECFieldElement square = invert.square();
                            return new Fp(curve, eCFieldElement3.multiply(square), eCFieldElement6.multiply(square.multiply(invert)), this.withCompression);
                        case 1:
                            Fp fp = new Fp(curve, eCFieldElement3.multiply(eCFieldElement4), eCFieldElement6, new ECFieldElement[]{eCFieldElement4.multiply(eCFieldElement4.square())}, this.withCompression);
                            return fp;
                        case 2:
                            Fp fp2 = new Fp(curve, eCFieldElement3, eCFieldElement6, new ECFieldElement[]{eCFieldElement4}, this.withCompression);
                            return fp2;
                        default:
                            throw new IllegalStateException("unsupported coordinate system");
                    }
                } else {
                    Fp fp3 = new Fp(curve, eCFieldElement3, eCFieldElement6, new ECFieldElement[]{eCFieldElement4, eCFieldElement5}, this.withCompression);
                    return fp3;
                }
            }
        }

        public ECPoint twice() {
            ECFieldElement eCFieldElement;
            ECFieldElement multiply;
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            ECFieldElement eCFieldElement2 = this.y;
            if (eCFieldElement2.isZero()) {
                return curve.getInfinity();
            }
            int coordinateSystem = curve.getCoordinateSystem();
            ECFieldElement eCFieldElement3 = this.x;
            if (coordinateSystem == 4) {
                return twiceJacobianModified(true);
            }
            switch (coordinateSystem) {
                case 0:
                    ECFieldElement divide = three(eCFieldElement3.square()).add(getCurve().getA()).divide(two(eCFieldElement2));
                    ECFieldElement subtract = divide.square().subtract(two(eCFieldElement3));
                    return new Fp(curve, subtract, divide.multiply(eCFieldElement3.subtract(subtract)).subtract(eCFieldElement2), this.withCompression);
                case 1:
                    ECFieldElement eCFieldElement4 = this.zs[0];
                    boolean isOne = eCFieldElement4.isOne();
                    ECFieldElement a = curve.getA();
                    if (!a.isZero() && !isOne) {
                        a = a.multiply(eCFieldElement4.square());
                    }
                    ECFieldElement add = a.add(three(eCFieldElement3.square()));
                    ECFieldElement multiply2 = isOne ? eCFieldElement2 : eCFieldElement2.multiply(eCFieldElement4);
                    ECFieldElement square = isOne ? eCFieldElement2.square() : multiply2.multiply(eCFieldElement2);
                    ECFieldElement four = four(eCFieldElement3.multiply(square));
                    ECFieldElement subtract2 = add.square().subtract(two(four));
                    ECFieldElement two = two(multiply2);
                    ECFieldElement multiply3 = subtract2.multiply(two);
                    ECFieldElement two2 = two(square);
                    Fp fp = new Fp(curve, multiply3, four.subtract(subtract2).multiply(add).subtract(two(two2.square())), new ECFieldElement[]{two(isOne ? two(two2) : two.square()).multiply(multiply2)}, this.withCompression);
                    return fp;
                case 2:
                    ECFieldElement eCFieldElement5 = this.zs[0];
                    boolean isOne2 = eCFieldElement5.isOne();
                    ECFieldElement square2 = eCFieldElement2.square();
                    ECFieldElement square3 = square2.square();
                    ECFieldElement a2 = curve.getA();
                    ECFieldElement negate = a2.negate();
                    if (negate.toBigInteger().equals(BigInteger.valueOf(3))) {
                        ECFieldElement square4 = isOne2 ? eCFieldElement5 : eCFieldElement5.square();
                        eCFieldElement = three(eCFieldElement3.add(square4).multiply(eCFieldElement3.subtract(square4)));
                        multiply = square2.multiply(eCFieldElement3);
                    } else {
                        ECFieldElement three = three(eCFieldElement3.square());
                        if (!isOne2) {
                            if (!a2.isZero()) {
                                ECFieldElement square5 = eCFieldElement5.square().square();
                                if (negate.bitLength() < a2.bitLength()) {
                                    eCFieldElement = three.subtract(square5.multiply(negate));
                                } else {
                                    a2 = square5.multiply(a2);
                                }
                            } else {
                                eCFieldElement = three;
                            }
                            multiply = eCFieldElement3.multiply(square2);
                        }
                        eCFieldElement = three.add(a2);
                        multiply = eCFieldElement3.multiply(square2);
                    }
                    ECFieldElement four2 = four(multiply);
                    ECFieldElement subtract3 = eCFieldElement.square().subtract(two(four2));
                    ECFieldElement subtract4 = four2.subtract(subtract3).multiply(eCFieldElement).subtract(eight(square3));
                    ECFieldElement two3 = two(eCFieldElement2);
                    if (!isOne2) {
                        two3 = two3.multiply(eCFieldElement5);
                    }
                    Fp fp2 = new Fp(curve, subtract3, subtract4, new ECFieldElement[]{two3}, this.withCompression);
                    return fp2;
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
        }

        /* access modifiers changed from: protected */
        public Fp twiceJacobianModified(boolean z) {
            ECFieldElement eCFieldElement = this.x;
            ECFieldElement eCFieldElement2 = this.y;
            ECFieldElement eCFieldElement3 = this.zs[0];
            ECFieldElement jacobianModifiedW = getJacobianModifiedW();
            ECFieldElement add = three(eCFieldElement.square()).add(jacobianModifiedW);
            ECFieldElement two = two(eCFieldElement2);
            ECFieldElement multiply = two.multiply(eCFieldElement2);
            ECFieldElement two2 = two(eCFieldElement.multiply(multiply));
            ECFieldElement subtract = add.square().subtract(two(two2));
            ECFieldElement two3 = two(multiply.square());
            ECFieldElement subtract2 = add.multiply(two2.subtract(subtract)).subtract(two3);
            ECFieldElement two4 = z ? two(two3.multiply(jacobianModifiedW)) : null;
            if (!eCFieldElement3.isOne()) {
                two = two.multiply(eCFieldElement3);
            }
            Fp fp = new Fp(getCurve(), subtract, subtract2, new ECFieldElement[]{two, two4}, this.withCompression);
            return fp;
        }

        public ECPoint twicePlus(ECPoint eCPoint) {
            if (this == eCPoint) {
                return threeTimes();
            }
            if (isInfinity()) {
                return eCPoint;
            }
            if (eCPoint.isInfinity()) {
                return twice();
            }
            ECFieldElement eCFieldElement = this.y;
            if (eCFieldElement.isZero()) {
                return eCPoint;
            }
            ECCurve curve = getCurve();
            int coordinateSystem = curve.getCoordinateSystem();
            if (coordinateSystem != 0) {
                return coordinateSystem != 4 ? twice().add(eCPoint) : twiceJacobianModified(false).add(eCPoint);
            }
            ECFieldElement eCFieldElement2 = this.x;
            ECFieldElement eCFieldElement3 = eCPoint.x;
            ECFieldElement eCFieldElement4 = eCPoint.y;
            ECFieldElement subtract = eCFieldElement3.subtract(eCFieldElement2);
            ECFieldElement subtract2 = eCFieldElement4.subtract(eCFieldElement);
            if (subtract.isZero()) {
                return subtract2.isZero() ? threeTimes() : this;
            }
            ECFieldElement square = subtract.square();
            ECFieldElement subtract3 = square.multiply(two(eCFieldElement2).add(eCFieldElement3)).subtract(subtract2.square());
            if (subtract3.isZero()) {
                return curve.getInfinity();
            }
            ECFieldElement invert = subtract3.multiply(subtract).invert();
            ECFieldElement multiply = subtract3.multiply(invert).multiply(subtract2);
            ECFieldElement subtract4 = two(eCFieldElement).multiply(square).multiply(subtract).multiply(invert).subtract(multiply);
            ECFieldElement add = subtract4.subtract(multiply).multiply(multiply.add(subtract4)).add(eCFieldElement3);
            return new Fp(curve, add, eCFieldElement2.subtract(add).multiply(subtract4).subtract(eCFieldElement), this.withCompression);
        }

        /* access modifiers changed from: protected */
        public ECFieldElement two(ECFieldElement eCFieldElement) {
            return eCFieldElement.add(eCFieldElement);
        }
    }

    protected ECPoint(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        this(eCCurve, eCFieldElement, eCFieldElement2, getInitialZCoords(eCCurve));
    }

    protected ECPoint(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr) {
        this.preCompTable = null;
        this.curve = eCCurve;
        this.x = eCFieldElement;
        this.y = eCFieldElement2;
        this.zs = eCFieldElementArr;
    }

    protected static ECFieldElement[] getInitialZCoords(ECCurve eCCurve) {
        int coordinateSystem = eCCurve == null ? 0 : eCCurve.getCoordinateSystem();
        if (coordinateSystem == 0 || coordinateSystem == 5) {
            return EMPTY_ZS;
        }
        ECFieldElement fromBigInteger = eCCurve.fromBigInteger(ECConstants.ONE);
        if (coordinateSystem != 6) {
            switch (coordinateSystem) {
                case 1:
                case 2:
                    break;
                case 3:
                    return new ECFieldElement[]{fromBigInteger, fromBigInteger, fromBigInteger};
                case 4:
                    return new ECFieldElement[]{fromBigInteger, eCCurve.getA()};
                default:
                    throw new IllegalArgumentException("unknown coordinate system");
            }
        }
        return new ECFieldElement[]{fromBigInteger};
    }

    public abstract ECPoint add(ECPoint eCPoint);

    /* access modifiers changed from: protected */
    public void checkNormalized() {
        if (!isNormalized()) {
            throw new IllegalStateException("point not in normal form");
        }
    }

    /* access modifiers changed from: protected */
    public ECPoint createScaledPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
        return getCurve().createRawPoint(getRawXCoord().multiply(eCFieldElement), getRawYCoord().multiply(eCFieldElement2), this.withCompression);
    }

    /* access modifiers changed from: protected */
    public abstract ECPoint detach();

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ECPoint)) {
            return false;
        }
        return equals((ECPoint) obj);
    }

    public boolean equals(ECPoint eCPoint) {
        ECPoint eCPoint2;
        ECPoint eCPoint3;
        boolean z = false;
        if (eCPoint == null) {
            return false;
        }
        ECCurve curve2 = getCurve();
        ECCurve curve3 = eCPoint.getCurve();
        boolean z2 = curve2 == null;
        boolean z3 = curve3 == null;
        boolean isInfinity = isInfinity();
        boolean isInfinity2 = eCPoint.isInfinity();
        if (isInfinity || isInfinity2) {
            if (isInfinity && isInfinity2 && (z2 || z3 || curve2.equals(curve3))) {
                z = true;
            }
            return z;
        }
        if (!z2 || !z3) {
            if (z2) {
                eCPoint = eCPoint.normalize();
            } else {
                if (z3) {
                    eCPoint3 = eCPoint;
                    eCPoint2 = normalize();
                } else if (!curve2.equals(curve3)) {
                    return false;
                } else {
                    ECPoint[] eCPointArr = {this, curve2.importPoint(eCPoint)};
                    curve2.normalizeAll(eCPointArr);
                    eCPoint2 = eCPointArr[0];
                    eCPoint3 = eCPointArr[1];
                }
                if (eCPoint2.getXCoord().equals(eCPoint3.getXCoord()) && eCPoint2.getYCoord().equals(eCPoint3.getYCoord())) {
                    z = true;
                }
                return z;
            }
        }
        eCPoint3 = eCPoint;
        eCPoint2 = this;
        z = true;
        return z;
    }

    public ECFieldElement getAffineXCoord() {
        checkNormalized();
        return getXCoord();
    }

    public ECFieldElement getAffineYCoord() {
        checkNormalized();
        return getYCoord();
    }

    /* access modifiers changed from: protected */
    public abstract boolean getCompressionYTilde();

    public ECCurve getCurve() {
        return this.curve;
    }

    /* access modifiers changed from: protected */
    public int getCurveCoordinateSystem() {
        if (this.curve == null) {
            return 0;
        }
        return this.curve.getCoordinateSystem();
    }

    public final ECPoint getDetachedPoint() {
        return normalize().detach();
    }

    public byte[] getEncoded() {
        return getEncoded(this.withCompression);
    }

    public byte[] getEncoded(boolean z) {
        if (isInfinity()) {
            return new byte[1];
        }
        ECPoint normalize = normalize();
        byte[] encoded = normalize.getXCoord().getEncoded();
        if (z) {
            byte[] bArr = new byte[(encoded.length + 1)];
            bArr[0] = (byte) (normalize.getCompressionYTilde() ? 3 : 2);
            System.arraycopy(encoded, 0, bArr, 1, encoded.length);
            return bArr;
        }
        byte[] encoded2 = normalize.getYCoord().getEncoded();
        byte[] bArr2 = new byte[(encoded.length + encoded2.length + 1)];
        bArr2[0] = 4;
        System.arraycopy(encoded, 0, bArr2, 1, encoded.length);
        System.arraycopy(encoded2, 0, bArr2, encoded.length + 1, encoded2.length);
        return bArr2;
    }

    public final ECFieldElement getRawXCoord() {
        return this.x;
    }

    public final ECFieldElement getRawYCoord() {
        return this.y;
    }

    /* access modifiers changed from: protected */
    public final ECFieldElement[] getRawZCoords() {
        return this.zs;
    }

    public ECFieldElement getXCoord() {
        return this.x;
    }

    public ECFieldElement getYCoord() {
        return this.y;
    }

    public ECFieldElement getZCoord(int i) {
        if (i < 0 || i >= this.zs.length) {
            return null;
        }
        return this.zs[i];
    }

    public ECFieldElement[] getZCoords() {
        int length = this.zs.length;
        if (length == 0) {
            return EMPTY_ZS;
        }
        ECFieldElement[] eCFieldElementArr = new ECFieldElement[length];
        System.arraycopy(this.zs, 0, eCFieldElementArr, 0, length);
        return eCFieldElementArr;
    }

    public int hashCode() {
        ECCurve curve2 = getCurve();
        int i = curve2 == null ? 0 : ~curve2.hashCode();
        if (isInfinity()) {
            return i;
        }
        ECPoint normalize = normalize();
        return (i ^ (normalize.getXCoord().hashCode() * 17)) ^ (normalize.getYCoord().hashCode() * 257);
    }

    /* access modifiers changed from: package-private */
    public boolean implIsValid(final boolean z) {
        if (isInfinity()) {
            return true;
        }
        return !((ValidityPrecompInfo) getCurve().precompute(this, "bc_validity", new PreCompCallback() {
            public PreCompInfo precompute(PreCompInfo preCompInfo) {
                ValidityPrecompInfo validityPrecompInfo = preCompInfo instanceof ValidityPrecompInfo ? (ValidityPrecompInfo) preCompInfo : null;
                if (validityPrecompInfo == null) {
                    validityPrecompInfo = new ValidityPrecompInfo();
                }
                if (validityPrecompInfo.hasFailed()) {
                    return validityPrecompInfo;
                }
                if (!validityPrecompInfo.hasCurveEquationPassed()) {
                    if (!ECPoint.this.satisfiesCurveEquation()) {
                        validityPrecompInfo.reportFailed();
                        return validityPrecompInfo;
                    }
                    validityPrecompInfo.reportCurveEquationPassed();
                }
                if (z && !validityPrecompInfo.hasOrderPassed()) {
                    if (!ECPoint.this.satisfiesOrder()) {
                        validityPrecompInfo.reportFailed();
                        return validityPrecompInfo;
                    }
                    validityPrecompInfo.reportOrderPassed();
                }
                return validityPrecompInfo;
            }
        })).hasFailed();
    }

    public boolean isCompressed() {
        return this.withCompression;
    }

    public boolean isInfinity() {
        return this.x == null || this.y == null || (this.zs.length > 0 && this.zs[0].isZero());
    }

    public boolean isNormalized() {
        int curveCoordinateSystem = getCurveCoordinateSystem();
        return curveCoordinateSystem == 0 || curveCoordinateSystem == 5 || isInfinity() || this.zs[0].isOne();
    }

    public boolean isValid() {
        return implIsValid(true);
    }

    /* access modifiers changed from: package-private */
    public boolean isValidPartial() {
        return implIsValid(false);
    }

    public ECPoint multiply(BigInteger bigInteger) {
        return getCurve().getMultiplier().multiply(this, bigInteger);
    }

    public abstract ECPoint negate();

    public ECPoint normalize() {
        if (isInfinity()) {
            return this;
        }
        int curveCoordinateSystem = getCurveCoordinateSystem();
        if (curveCoordinateSystem == 0 || curveCoordinateSystem == 5) {
            return this;
        }
        ECFieldElement zCoord = getZCoord(0);
        return zCoord.isOne() ? this : normalize(zCoord.invert());
    }

    /* access modifiers changed from: package-private */
    public ECPoint normalize(ECFieldElement eCFieldElement) {
        int curveCoordinateSystem = getCurveCoordinateSystem();
        if (curveCoordinateSystem != 6) {
            switch (curveCoordinateSystem) {
                case 1:
                    break;
                case 2:
                case 3:
                case 4:
                    ECFieldElement square = eCFieldElement.square();
                    return createScaledPoint(square, square.multiply(eCFieldElement));
                default:
                    throw new IllegalStateException("not a projective coordinate system");
            }
        }
        return createScaledPoint(eCFieldElement, eCFieldElement);
    }

    /* access modifiers changed from: protected */
    public abstract boolean satisfiesCurveEquation();

    /* access modifiers changed from: protected */
    public boolean satisfiesOrder() {
        boolean z = true;
        if (ECConstants.ONE.equals(this.curve.getCofactor())) {
            return true;
        }
        BigInteger order = this.curve.getOrder();
        if (order != null) {
            if (ECAlgorithms.referenceMultiply(this, order).isInfinity()) {
                return true;
            }
            z = false;
        }
        return z;
    }

    public ECPoint scaleX(ECFieldElement eCFieldElement) {
        return isInfinity() ? this : getCurve().createRawPoint(getRawXCoord().multiply(eCFieldElement), getRawYCoord(), getRawZCoords(), this.withCompression);
    }

    public ECPoint scaleY(ECFieldElement eCFieldElement) {
        return isInfinity() ? this : getCurve().createRawPoint(getRawXCoord(), getRawYCoord().multiply(eCFieldElement), getRawZCoords(), this.withCompression);
    }

    public abstract ECPoint subtract(ECPoint eCPoint);

    public ECPoint threeTimes() {
        return twicePlus(this);
    }

    public ECPoint timesPow2(int i) {
        if (i >= 0) {
            ECPoint eCPoint = this;
            while (true) {
                i--;
                if (i < 0) {
                    return eCPoint;
                }
                eCPoint = eCPoint.twice();
            }
        } else {
            throw new IllegalArgumentException("'e' cannot be negative");
        }
    }

    public String toString() {
        if (isInfinity()) {
            return "INF";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append('(');
        stringBuffer.append(getRawXCoord());
        stringBuffer.append(',');
        stringBuffer.append(getRawYCoord());
        for (ECFieldElement append : this.zs) {
            stringBuffer.append(',');
            stringBuffer.append(append);
        }
        stringBuffer.append(')');
        return stringBuffer.toString();
    }

    public abstract ECPoint twice();

    public ECPoint twicePlus(ECPoint eCPoint) {
        return twice().add(eCPoint);
    }
}
