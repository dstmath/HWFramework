package org.bouncycastle.math.ec;

import java.math.BigInteger;
import java.util.Hashtable;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;

public abstract class ECPoint {
    protected static final ECFieldElement[] EMPTY_ZS = new ECFieldElement[0];
    protected ECCurve curve;
    protected Hashtable preCompTable;
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
        @Override // org.bouncycastle.math.ec.ECPoint
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
            if (coordinateSystem != 0) {
                if (coordinateSystem == 1) {
                    ECFieldElement eCFieldElement7 = this.zs[0];
                    if (!eCFieldElement7.isOne()) {
                        ECFieldElement multiply2 = eCFieldElement7.multiply(eCFieldElement7.square());
                        multiply = multiply.multiply(eCFieldElement7);
                        a = a.multiply(eCFieldElement7);
                        b = b.multiply(multiply2);
                    }
                } else {
                    throw new IllegalStateException("unsupported coordinate system");
                }
            }
            return multiply.equals(eCFieldElement3.add(a).multiply(eCFieldElement3.square()).add(b));
        }

        /* access modifiers changed from: protected */
        @Override // org.bouncycastle.math.ec.ECPoint
        public boolean satisfiesOrder() {
            BigInteger cofactor = this.curve.getCofactor();
            if (ECConstants.TWO.equals(cofactor)) {
                return ((ECFieldElement.AbstractF2m) normalize().getAffineXCoord()).trace() != 0;
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
            return ((ECFieldElement.AbstractF2m) affineXCoord.multiply(solveQuadraticEquation).add(normalize.getAffineYCoord())).trace() == 0;
        }

        @Override // org.bouncycastle.math.ec.ECPoint
        public ECPoint scaleX(ECFieldElement eCFieldElement) {
            if (isInfinity()) {
                return this;
            }
            int curveCoordinateSystem = getCurveCoordinateSystem();
            if (curveCoordinateSystem == 5) {
                ECFieldElement rawXCoord = getRawXCoord();
                return getCurve().createRawPoint(rawXCoord, getRawYCoord().add(rawXCoord).divide(eCFieldElement).add(rawXCoord.multiply(eCFieldElement)), getRawZCoords());
            } else if (curveCoordinateSystem != 6) {
                return ECPoint.super.scaleX(eCFieldElement);
            } else {
                ECFieldElement rawXCoord2 = getRawXCoord();
                ECFieldElement rawYCoord = getRawYCoord();
                ECFieldElement eCFieldElement2 = getRawZCoords()[0];
                ECFieldElement multiply = rawXCoord2.multiply(eCFieldElement.square());
                return getCurve().createRawPoint(multiply, rawYCoord.add(rawXCoord2).add(multiply), new ECFieldElement[]{eCFieldElement2.multiply(eCFieldElement)});
            }
        }

        @Override // org.bouncycastle.math.ec.ECPoint
        public ECPoint scaleXNegateY(ECFieldElement eCFieldElement) {
            return scaleX(eCFieldElement);
        }

        @Override // org.bouncycastle.math.ec.ECPoint
        public ECPoint scaleY(ECFieldElement eCFieldElement) {
            if (isInfinity()) {
                return this;
            }
            int curveCoordinateSystem = getCurveCoordinateSystem();
            if (curveCoordinateSystem != 5 && curveCoordinateSystem != 6) {
                return ECPoint.super.scaleY(eCFieldElement);
            }
            ECFieldElement rawXCoord = getRawXCoord();
            return getCurve().createRawPoint(rawXCoord, getRawYCoord().add(rawXCoord).multiply(eCFieldElement).add(rawXCoord), getRawZCoords());
        }

        @Override // org.bouncycastle.math.ec.ECPoint
        public ECPoint scaleYNegateX(ECFieldElement eCFieldElement) {
            return scaleY(eCFieldElement);
        }

        @Override // org.bouncycastle.math.ec.ECPoint
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
            if (coordinateSystem != 0) {
                if (coordinateSystem != 1) {
                    if (coordinateSystem != 5) {
                        if (coordinateSystem != 6) {
                            throw new IllegalStateException("unsupported coordinate system");
                        }
                    }
                }
                createRawPoint = curve.createRawPoint(eCFieldElement.square(), this.y.square(), new ECFieldElement[]{this.zs[0].square()});
                return (AbstractF2m) createRawPoint;
            }
            createRawPoint = curve.createRawPoint(eCFieldElement.square(), this.y.square());
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
            if (coordinateSystem != 0) {
                if (coordinateSystem != 1) {
                    if (coordinateSystem != 5) {
                        if (coordinateSystem != 6) {
                            throw new IllegalStateException("unsupported coordinate system");
                        }
                    }
                }
                createRawPoint = curve.createRawPoint(eCFieldElement.squarePow(i), this.y.squarePow(i), new ECFieldElement[]{this.zs[0].squarePow(i)});
                return (AbstractF2m) createRawPoint;
            }
            createRawPoint = curve.createRawPoint(eCFieldElement.squarePow(i), this.y.squarePow(i));
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
        @Override // org.bouncycastle.math.ec.ECPoint
        public boolean getCompressionYTilde() {
            return getAffineYCoord().testBitZero();
        }

        /* access modifiers changed from: protected */
        @Override // org.bouncycastle.math.ec.ECPoint
        public boolean satisfiesCurveEquation() {
            ECFieldElement eCFieldElement = this.x;
            ECFieldElement eCFieldElement2 = this.y;
            ECFieldElement a = this.curve.getA();
            ECFieldElement b = this.curve.getB();
            ECFieldElement square = eCFieldElement2.square();
            int curveCoordinateSystem = getCurveCoordinateSystem();
            if (curveCoordinateSystem != 0) {
                if (curveCoordinateSystem == 1) {
                    ECFieldElement eCFieldElement3 = this.zs[0];
                    if (!eCFieldElement3.isOne()) {
                        ECFieldElement square2 = eCFieldElement3.square();
                        ECFieldElement multiply = eCFieldElement3.multiply(square2);
                        square = square.multiply(eCFieldElement3);
                        a = a.multiply(square2);
                        b = b.multiply(multiply);
                    }
                } else if (curveCoordinateSystem == 2 || curveCoordinateSystem == 3 || curveCoordinateSystem == 4) {
                    ECFieldElement eCFieldElement4 = this.zs[0];
                    if (!eCFieldElement4.isOne()) {
                        ECFieldElement square3 = eCFieldElement4.square();
                        ECFieldElement square4 = square3.square();
                        ECFieldElement multiply2 = square3.multiply(square4);
                        a = a.multiply(square4);
                        b = b.multiply(multiply2);
                    }
                } else {
                    throw new IllegalStateException("unsupported coordinate system");
                }
            }
            return square.equals(eCFieldElement.square().add(a).multiply(eCFieldElement).add(b));
        }

        @Override // org.bouncycastle.math.ec.ECPoint
        public ECPoint subtract(ECPoint eCPoint) {
            return eCPoint.isInfinity() ? this : add(eCPoint.negate());
        }
    }

    public static class F2m extends AbstractF2m {
        F2m(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
            super(eCCurve, eCFieldElement, eCFieldElement2);
        }

        F2m(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr) {
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
            int coordinateSystem = curve.getCoordinateSystem();
            ECFieldElement eCFieldElement7 = this.x;
            ECFieldElement eCFieldElement8 = eCPoint.x;
            if (coordinateSystem == 0) {
                ECFieldElement eCFieldElement9 = this.y;
                ECFieldElement eCFieldElement10 = eCPoint.y;
                ECFieldElement add = eCFieldElement7.add(eCFieldElement8);
                ECFieldElement add2 = eCFieldElement9.add(eCFieldElement10);
                if (add.isZero()) {
                    return add2.isZero() ? twice() : curve.getInfinity();
                }
                ECFieldElement divide = add2.divide(add);
                ECFieldElement add3 = divide.square().add(divide).add(add).add(curve.getA());
                return new F2m(curve, add3, divide.multiply(eCFieldElement7.add(add3)).add(add3).add(eCFieldElement9));
            } else if (coordinateSystem == 1) {
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
                return new F2m(curve, multiply2, add4.multiplyPlusProduct(eCFieldElement7, add5, eCFieldElement11).multiplyPlusProduct(square, add6, add7), new ECFieldElement[]{multiply.multiply(eCFieldElement12)});
            } else if (coordinateSystem != 6) {
                throw new IllegalStateException("unsupported coordinate system");
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
                    eCFieldElement4 = divide2.square().add(divide2).add(xCoord).add(curve.getA());
                    if (eCFieldElement4.isZero()) {
                        return new F2m(curve, eCFieldElement4, curve.getB().sqrt());
                    }
                    eCFieldElement6 = divide2.multiply(xCoord.add(eCFieldElement4)).add(eCFieldElement4).add(yCoord).divide(eCFieldElement4).add(eCFieldElement4);
                    eCFieldElement5 = curve.fromBigInteger(ECConstants.ONE);
                } else {
                    ECFieldElement square2 = add9.square();
                    ECFieldElement multiply3 = add8.multiply(eCFieldElement7);
                    ECFieldElement multiply4 = add8.multiply(eCFieldElement2);
                    eCFieldElement4 = multiply3.multiply(multiply4);
                    if (eCFieldElement4.isZero()) {
                        return new F2m(curve, eCFieldElement4, curve.getB().sqrt());
                    }
                    ECFieldElement multiply5 = add8.multiply(square2);
                    eCFieldElement5 = !isOne3 ? multiply5.multiply(eCFieldElement18) : multiply5;
                    eCFieldElement6 = multiply4.add(square2).squarePlusProduct(eCFieldElement5, eCFieldElement15.add(eCFieldElement16));
                    if (!isOne2) {
                        eCFieldElement5 = eCFieldElement5.multiply(eCFieldElement16);
                    }
                }
                return new F2m(curve, eCFieldElement4, eCFieldElement6, new ECFieldElement[]{eCFieldElement5});
            }
        }

        /* access modifiers changed from: protected */
        @Override // org.bouncycastle.math.ec.ECPoint
        public ECPoint detach() {
            return new F2m(null, getAffineXCoord(), getAffineYCoord());
        }

        /* access modifiers changed from: protected */
        @Override // org.bouncycastle.math.ec.ECPoint
        public boolean getCompressionYTilde() {
            ECFieldElement rawXCoord = getRawXCoord();
            if (rawXCoord.isZero()) {
                return false;
            }
            ECFieldElement rawYCoord = getRawYCoord();
            int curveCoordinateSystem = getCurveCoordinateSystem();
            return (curveCoordinateSystem == 5 || curveCoordinateSystem == 6) ? rawYCoord.testBitZero() != rawXCoord.testBitZero() : rawYCoord.divide(rawXCoord).testBitZero();
        }

        @Override // org.bouncycastle.math.ec.ECPoint
        public ECFieldElement getYCoord() {
            int curveCoordinateSystem = getCurveCoordinateSystem();
            if (curveCoordinateSystem != 5 && curveCoordinateSystem != 6) {
                return this.y;
            }
            ECFieldElement eCFieldElement = this.x;
            ECFieldElement eCFieldElement2 = this.y;
            if (isInfinity() || eCFieldElement.isZero()) {
                return eCFieldElement2;
            }
            ECFieldElement multiply = eCFieldElement2.add(eCFieldElement).multiply(eCFieldElement);
            if (6 != curveCoordinateSystem) {
                return multiply;
            }
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
            int curveCoordinateSystem = getCurveCoordinateSystem();
            if (curveCoordinateSystem == 0) {
                return new F2m(this.curve, eCFieldElement, this.y.add(eCFieldElement));
            }
            if (curveCoordinateSystem == 1) {
                return new F2m(this.curve, eCFieldElement, this.y.add(eCFieldElement), new ECFieldElement[]{this.zs[0]});
            } else if (curveCoordinateSystem == 5) {
                return new F2m(this.curve, eCFieldElement, this.y.addOne());
            } else {
                if (curveCoordinateSystem == 6) {
                    ECFieldElement eCFieldElement2 = this.y;
                    ECFieldElement eCFieldElement3 = this.zs[0];
                    return new F2m(this.curve, eCFieldElement, eCFieldElement2.add(eCFieldElement3), new ECFieldElement[]{eCFieldElement3});
                }
                throw new IllegalStateException("unsupported coordinate system");
            }
        }

        @Override // org.bouncycastle.math.ec.ECPoint
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
            if (coordinateSystem == 0) {
                ECFieldElement add = this.y.divide(eCFieldElement2).add(eCFieldElement2);
                ECFieldElement add2 = add.square().add(add).add(curve.getA());
                return new F2m(curve, add2, eCFieldElement2.squarePlusProduct(add2, add.addOne()));
            } else if (coordinateSystem == 1) {
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
                return new F2m(curve, multiply.multiply(multiplyPlusProduct), square.square().multiplyPlusProduct(multiply, multiplyPlusProduct, add4), new ECFieldElement[]{multiply.multiply(square2)});
            } else if (coordinateSystem == 6) {
                ECFieldElement eCFieldElement5 = this.y;
                ECFieldElement eCFieldElement6 = this.zs[0];
                boolean isOne2 = eCFieldElement6.isOne();
                ECFieldElement multiply2 = isOne2 ? eCFieldElement5 : eCFieldElement5.multiply(eCFieldElement6);
                ECFieldElement square3 = isOne2 ? eCFieldElement6 : eCFieldElement6.square();
                ECFieldElement a = curve.getA();
                ECFieldElement multiply3 = isOne2 ? a : a.multiply(square3);
                ECFieldElement add5 = eCFieldElement5.square().add(multiply2).add(multiply3);
                if (add5.isZero()) {
                    return new F2m(curve, add5, curve.getB().sqrt());
                }
                ECFieldElement square4 = add5.square();
                ECFieldElement multiply4 = isOne2 ? add5 : add5.multiply(square3);
                ECFieldElement b = curve.getB();
                if (b.bitLength() < (curve.getFieldSize() >> 1)) {
                    ECFieldElement square5 = eCFieldElement5.add(eCFieldElement2).square();
                    eCFieldElement = square5.add(add5).add(square3).multiply(square5).add(b.isOne() ? multiply3.add(square3).square() : multiply3.squarePlusProduct(b, square3.square())).add(square4);
                    if (!a.isZero()) {
                        if (!a.isOne()) {
                            eCFieldElement = eCFieldElement.add(a.addOne().multiply(multiply4));
                        }
                        return new F2m(curve, square4, eCFieldElement, new ECFieldElement[]{multiply4});
                    }
                } else {
                    if (!isOne2) {
                        eCFieldElement2 = eCFieldElement2.multiply(eCFieldElement6);
                    }
                    eCFieldElement = eCFieldElement2.squarePlusProduct(add5, multiply2).add(square4);
                }
                eCFieldElement = eCFieldElement.add(multiply4);
                return new F2m(curve, square4, eCFieldElement, new ECFieldElement[]{multiply4});
            } else {
                throw new IllegalStateException("unsupported coordinate system");
            }
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
                return new F2m(curve, multiplyPlusProduct, curve.getB().sqrt());
            }
            ECFieldElement multiply2 = multiplyPlusProduct.square().multiply(multiply);
            ECFieldElement multiply3 = multiplyPlusProduct.multiply(square4).multiply(square3);
            return new F2m(curve, multiply2, multiplyPlusProduct.add(square4).square().multiplyPlusProduct(add, addOne, multiply3), new ECFieldElement[]{multiply3});
        }
    }

    public static class Fp extends AbstractFp {
        Fp(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2) {
            super(eCCurve, eCFieldElement, eCFieldElement2);
        }

        Fp(ECCurve eCCurve, ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr) {
            super(eCCurve, eCFieldElement, eCFieldElement2, eCFieldElementArr);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:52:0x0120, code lost:
            if (r1 == r6) goto L_0x0122;
         */
        @Override // org.bouncycastle.math.ec.ECPoint
        public ECPoint add(ECPoint eCPoint) {
            ECFieldElement eCFieldElement;
            ECFieldElement eCFieldElement2;
            ECFieldElement eCFieldElement3;
            ECFieldElement eCFieldElement4;
            if (isInfinity()) {
                return eCPoint;
            }
            if (eCPoint.isInfinity()) {
                return this;
            }
            if (this == eCPoint) {
                return twice();
            }
            ECCurve curve = getCurve();
            int coordinateSystem = curve.getCoordinateSystem();
            ECFieldElement eCFieldElement5 = this.x;
            ECFieldElement eCFieldElement6 = this.y;
            ECFieldElement eCFieldElement7 = eCPoint.x;
            ECFieldElement eCFieldElement8 = eCPoint.y;
            if (coordinateSystem == 0) {
                ECFieldElement subtract = eCFieldElement7.subtract(eCFieldElement5);
                ECFieldElement subtract2 = eCFieldElement8.subtract(eCFieldElement6);
                if (subtract.isZero()) {
                    return subtract2.isZero() ? twice() : curve.getInfinity();
                }
                ECFieldElement divide = subtract2.divide(subtract);
                ECFieldElement subtract3 = divide.square().subtract(eCFieldElement5).subtract(eCFieldElement7);
                return new Fp(curve, subtract3, divide.multiply(eCFieldElement5.subtract(subtract3)).subtract(eCFieldElement6));
            } else if (coordinateSystem == 1) {
                ECFieldElement eCFieldElement9 = this.zs[0];
                ECFieldElement eCFieldElement10 = eCPoint.zs[0];
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
                return new Fp(curve, subtract5.multiply(subtract6), multiply2.subtract(subtract6).multiplyMinusProduct(subtract4, eCFieldElement6, multiply), new ECFieldElement[]{multiply.multiply(eCFieldElement9)});
            } else if (coordinateSystem == 2 || coordinateSystem == 4) {
                ECFieldElement eCFieldElement11 = this.zs[0];
                ECFieldElement eCFieldElement12 = eCPoint.zs[0];
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
                    eCFieldElement = subtract7.square();
                    ECFieldElement multiply3 = eCFieldElement.multiply(subtract7);
                    ECFieldElement multiply4 = eCFieldElement.multiply(eCFieldElement5);
                    eCFieldElement2 = subtract8.square().add(multiply3).subtract(two(multiply4));
                    eCFieldElement3 = multiply4.subtract(eCFieldElement2).multiplyMinusProduct(subtract8, multiply3, eCFieldElement6);
                    ECFieldElement multiply5 = !isOne3 ? subtract7.multiply(eCFieldElement11) : subtract7;
                    eCFieldElement4 = !isOne4 ? multiply5.multiply(eCFieldElement12) : multiply5;
                } else {
                    ECFieldElement subtract9 = eCFieldElement5.subtract(eCFieldElement7);
                    ECFieldElement subtract10 = eCFieldElement6.subtract(eCFieldElement8);
                    if (subtract9.isZero()) {
                        return subtract10.isZero() ? twice() : curve.getInfinity();
                    }
                    ECFieldElement square4 = subtract9.square();
                    ECFieldElement multiply6 = eCFieldElement5.multiply(square4);
                    ECFieldElement multiply7 = eCFieldElement7.multiply(square4);
                    ECFieldElement multiply8 = multiply6.subtract(multiply7).multiply(eCFieldElement6);
                    ECFieldElement subtract11 = subtract10.square().subtract(multiply6).subtract(multiply7);
                    eCFieldElement3 = multiply6.subtract(subtract11).multiply(subtract10).subtract(multiply8);
                    eCFieldElement4 = subtract9.multiply(eCFieldElement11);
                    eCFieldElement2 = subtract11;
                }
                eCFieldElement = null;
                return new Fp(curve, eCFieldElement2, eCFieldElement3, coordinateSystem == 4 ? new ECFieldElement[]{eCFieldElement4, calculateJacobianModifiedW(eCFieldElement4, eCFieldElement)} : new ECFieldElement[]{eCFieldElement4});
            } else {
                throw new IllegalStateException("unsupported coordinate system");
            }
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
        @Override // org.bouncycastle.math.ec.ECPoint
        public ECPoint detach() {
            return new Fp(null, getAffineXCoord(), getAffineYCoord());
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

        @Override // org.bouncycastle.math.ec.ECPoint
        public ECFieldElement getZCoord(int i) {
            return (i == 1 && 4 == getCurveCoordinateSystem()) ? getJacobianModifiedW() : super.getZCoord(i);
        }

        @Override // org.bouncycastle.math.ec.ECPoint
        public ECPoint negate() {
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            return curve.getCoordinateSystem() != 0 ? new Fp(curve, this.x, this.y.negate(), this.zs) : new Fp(curve, this.x, this.y.negate());
        }

        /* access modifiers changed from: protected */
        public ECFieldElement three(ECFieldElement eCFieldElement) {
            return two(eCFieldElement).add(eCFieldElement);
        }

        @Override // org.bouncycastle.math.ec.ECPoint
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
            return new Fp(curve, add2, eCFieldElement2.subtract(add2).multiply(subtract2).subtract(eCFieldElement));
        }

        @Override // org.bouncycastle.math.ec.ECPoint
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
                if (!fromBigInteger.isOne() && coordinateSystem != 0) {
                    if (coordinateSystem == 1) {
                        eCFieldElement = fromBigInteger.square();
                        eCFieldElement3 = eCFieldElement3.multiply(fromBigInteger);
                        eCFieldElement2 = eCFieldElement2.multiply(eCFieldElement);
                    } else if (coordinateSystem == 2) {
                        eCFieldElement = null;
                    } else if (coordinateSystem == 4) {
                        a = getJacobianModifiedW();
                    } else {
                        throw new IllegalStateException("unsupported coordinate system");
                    }
                    a = calculateJacobianModifiedW(fromBigInteger, eCFieldElement);
                }
                ECFieldElement eCFieldElement4 = a;
                ECFieldElement eCFieldElement5 = eCFieldElement2;
                int i2 = 0;
                while (i2 < i) {
                    if (eCFieldElement5.isZero()) {
                        return curve.getInfinity();
                    }
                    ECFieldElement three = three(eCFieldElement3.square());
                    ECFieldElement two = two(eCFieldElement5);
                    ECFieldElement multiply = two.multiply(eCFieldElement5);
                    ECFieldElement two2 = two(eCFieldElement3.multiply(multiply));
                    ECFieldElement two3 = two(multiply.square());
                    if (!eCFieldElement4.isZero()) {
                        three = three.add(eCFieldElement4);
                        eCFieldElement4 = two(two3.multiply(eCFieldElement4));
                    }
                    ECFieldElement subtract = three.square().subtract(two(two2));
                    eCFieldElement5 = three.multiply(two2.subtract(subtract)).subtract(two3);
                    fromBigInteger = fromBigInteger.isOne() ? two : two.multiply(fromBigInteger);
                    i2++;
                    eCFieldElement3 = subtract;
                }
                if (coordinateSystem == 0) {
                    ECFieldElement invert = fromBigInteger.invert();
                    ECFieldElement square = invert.square();
                    return new Fp(curve, eCFieldElement3.multiply(square), eCFieldElement5.multiply(square.multiply(invert)));
                } else if (coordinateSystem == 1) {
                    return new Fp(curve, eCFieldElement3.multiply(fromBigInteger), eCFieldElement5, new ECFieldElement[]{fromBigInteger.multiply(fromBigInteger.square())});
                } else {
                    if (coordinateSystem == 2) {
                        return new Fp(curve, eCFieldElement3, eCFieldElement5, new ECFieldElement[]{fromBigInteger});
                    }
                    if (coordinateSystem == 4) {
                        return new Fp(curve, eCFieldElement3, eCFieldElement5, new ECFieldElement[]{fromBigInteger, eCFieldElement4});
                    }
                    throw new IllegalStateException("unsupported coordinate system");
                }
            }
        }

        @Override // org.bouncycastle.math.ec.ECPoint
        public ECPoint twice() {
            ECFieldElement eCFieldElement;
            ECFieldElement eCFieldElement2;
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            ECFieldElement eCFieldElement3 = this.y;
            if (eCFieldElement3.isZero()) {
                return curve.getInfinity();
            }
            int coordinateSystem = curve.getCoordinateSystem();
            ECFieldElement eCFieldElement4 = this.x;
            if (coordinateSystem == 0) {
                ECFieldElement divide = three(eCFieldElement4.square()).add(getCurve().getA()).divide(two(eCFieldElement3));
                ECFieldElement subtract = divide.square().subtract(two(eCFieldElement4));
                return new Fp(curve, subtract, divide.multiply(eCFieldElement4.subtract(subtract)).subtract(eCFieldElement3));
            } else if (coordinateSystem == 1) {
                ECFieldElement eCFieldElement5 = this.zs[0];
                boolean isOne = eCFieldElement5.isOne();
                ECFieldElement a = curve.getA();
                if (!a.isZero() && !isOne) {
                    a = a.multiply(eCFieldElement5.square());
                }
                ECFieldElement add = a.add(three(eCFieldElement4.square()));
                ECFieldElement multiply = isOne ? eCFieldElement3 : eCFieldElement3.multiply(eCFieldElement5);
                ECFieldElement square = isOne ? eCFieldElement3.square() : multiply.multiply(eCFieldElement3);
                ECFieldElement four = four(eCFieldElement4.multiply(square));
                ECFieldElement subtract2 = add.square().subtract(two(four));
                ECFieldElement two = two(multiply);
                ECFieldElement multiply2 = subtract2.multiply(two);
                ECFieldElement two2 = two(square);
                return new Fp(curve, multiply2, four.subtract(subtract2).multiply(add).subtract(two(two2.square())), new ECFieldElement[]{two(isOne ? two(two2) : two.square()).multiply(multiply)});
            } else if (coordinateSystem == 2) {
                ECFieldElement eCFieldElement6 = this.zs[0];
                boolean isOne2 = eCFieldElement6.isOne();
                ECFieldElement square2 = eCFieldElement3.square();
                ECFieldElement square3 = square2.square();
                ECFieldElement a2 = curve.getA();
                ECFieldElement negate = a2.negate();
                if (negate.toBigInteger().equals(BigInteger.valueOf(3))) {
                    ECFieldElement square4 = isOne2 ? eCFieldElement6 : eCFieldElement6.square();
                    eCFieldElement = three(eCFieldElement4.add(square4).multiply(eCFieldElement4.subtract(square4)));
                    eCFieldElement2 = square2.multiply(eCFieldElement4);
                } else {
                    ECFieldElement three = three(eCFieldElement4.square());
                    if (!isOne2) {
                        if (!a2.isZero()) {
                            ECFieldElement square5 = eCFieldElement6.square().square();
                            if (negate.bitLength() < a2.bitLength()) {
                                eCFieldElement = three.subtract(square5.multiply(negate));
                            } else {
                                a2 = square5.multiply(a2);
                            }
                        } else {
                            eCFieldElement = three;
                        }
                        eCFieldElement2 = eCFieldElement4.multiply(square2);
                    }
                    eCFieldElement = three.add(a2);
                    eCFieldElement2 = eCFieldElement4.multiply(square2);
                }
                ECFieldElement four2 = four(eCFieldElement2);
                ECFieldElement subtract3 = eCFieldElement.square().subtract(two(four2));
                ECFieldElement subtract4 = four2.subtract(subtract3).multiply(eCFieldElement).subtract(eight(square3));
                ECFieldElement two3 = two(eCFieldElement3);
                if (!isOne2) {
                    two3 = two3.multiply(eCFieldElement6);
                }
                return new Fp(curve, subtract3, subtract4, new ECFieldElement[]{two3});
            } else if (coordinateSystem == 4) {
                return twiceJacobianModified(true);
            } else {
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
            return new Fp(getCurve(), subtract, subtract2, new ECFieldElement[]{two, two4});
        }

        @Override // org.bouncycastle.math.ec.ECPoint
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
            return new Fp(curve, add, eCFieldElement2.subtract(add).multiply(subtract4).subtract(eCFieldElement));
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
        if (!(coordinateSystem == 1 || coordinateSystem == 2)) {
            if (coordinateSystem == 3) {
                return new ECFieldElement[]{fromBigInteger, fromBigInteger, fromBigInteger};
            }
            if (coordinateSystem == 4) {
                return new ECFieldElement[]{fromBigInteger, eCCurve.getA()};
            }
            if (coordinateSystem != 6) {
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
        return getCurve().createRawPoint(getRawXCoord().multiply(eCFieldElement), getRawYCoord().multiply(eCFieldElement2));
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
        if (eCPoint == null) {
            return false;
        }
        ECCurve curve2 = getCurve();
        ECCurve curve3 = eCPoint.getCurve();
        boolean z = curve2 == null;
        boolean z2 = curve3 == null;
        boolean isInfinity = isInfinity();
        boolean isInfinity2 = eCPoint.isInfinity();
        if (!isInfinity && !isInfinity2) {
            if (!z || !z2) {
                if (z) {
                    eCPoint = eCPoint.normalize();
                } else {
                    if (z2) {
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
                    return eCPoint2.getXCoord().equals(eCPoint3.getXCoord()) && eCPoint2.getYCoord().equals(eCPoint3.getYCoord());
                }
            }
            eCPoint3 = eCPoint;
            eCPoint2 = this;
            if (eCPoint2.getXCoord().equals(eCPoint3.getXCoord())) {
                return false;
            }
        } else if (!isInfinity || !isInfinity2) {
            return false;
        } else {
            return z || z2 || curve2.equals(curve3);
        }
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
        ECCurve eCCurve = this.curve;
        if (eCCurve == null) {
            return 0;
        }
        return eCCurve.getCoordinateSystem();
    }

    public final ECPoint getDetachedPoint() {
        return normalize().detach();
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
        if (i >= 0) {
            ECFieldElement[] eCFieldElementArr = this.zs;
            if (i < eCFieldElementArr.length) {
                return eCFieldElementArr[i];
            }
        }
        return null;
    }

    public ECFieldElement[] getZCoords() {
        ECFieldElement[] eCFieldElementArr = this.zs;
        int length = eCFieldElementArr.length;
        if (length == 0) {
            return EMPTY_ZS;
        }
        ECFieldElement[] eCFieldElementArr2 = new ECFieldElement[length];
        System.arraycopy(eCFieldElementArr, 0, eCFieldElementArr2, 0, length);
        return eCFieldElementArr2;
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
    public boolean implIsValid(final boolean z, final boolean z2) {
        if (isInfinity()) {
            return true;
        }
        return !((ValidityPrecompInfo) getCurve().precompute(this, "bc_validity", new PreCompCallback() {
            /* class org.bouncycastle.math.ec.ECPoint.AnonymousClass1 */

            @Override // org.bouncycastle.math.ec.PreCompCallback
            public PreCompInfo precompute(PreCompInfo preCompInfo) {
                ValidityPrecompInfo validityPrecompInfo = preCompInfo instanceof ValidityPrecompInfo ? (ValidityPrecompInfo) preCompInfo : null;
                if (validityPrecompInfo == null) {
                    validityPrecompInfo = new ValidityPrecompInfo();
                }
                if (validityPrecompInfo.hasFailed()) {
                    return validityPrecompInfo;
                }
                if (!validityPrecompInfo.hasCurveEquationPassed()) {
                    if (z || ECPoint.this.satisfiesCurveEquation()) {
                        validityPrecompInfo.reportCurveEquationPassed();
                    } else {
                        validityPrecompInfo.reportFailed();
                        return validityPrecompInfo;
                    }
                }
                if (z2 && !validityPrecompInfo.hasOrderPassed()) {
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

    public boolean isInfinity() {
        if (!(this.x == null || this.y == null)) {
            ECFieldElement[] eCFieldElementArr = this.zs;
            if (eCFieldElementArr.length <= 0 || !eCFieldElementArr[0].isZero()) {
                return false;
            }
        }
        return true;
    }

    public boolean isNormalized() {
        int curveCoordinateSystem = getCurveCoordinateSystem();
        return curveCoordinateSystem == 0 || curveCoordinateSystem == 5 || isInfinity() || this.zs[0].isOne();
    }

    public boolean isValid() {
        return implIsValid(false, true);
    }

    /* access modifiers changed from: package-private */
    public boolean isValidPartial() {
        return implIsValid(false, false);
    }

    public ECPoint multiply(BigInteger bigInteger) {
        return getCurve().getMultiplier().multiply(this, bigInteger);
    }

    public abstract ECPoint negate();

    public ECPoint normalize() {
        int curveCoordinateSystem;
        if (isInfinity() || (curveCoordinateSystem = getCurveCoordinateSystem()) == 0 || curveCoordinateSystem == 5) {
            return this;
        }
        ECFieldElement zCoord = getZCoord(0);
        if (zCoord.isOne()) {
            return this;
        }
        if (this.curve != null) {
            ECFieldElement randomFieldElementMult = this.curve.randomFieldElementMult(CryptoServicesRegistrar.getSecureRandom());
            return normalize(zCoord.multiply(randomFieldElementMult).invert().multiply(randomFieldElementMult));
        }
        throw new IllegalStateException("Detached points must be in affine coordinates");
    }

    /* access modifiers changed from: package-private */
    public ECPoint normalize(ECFieldElement eCFieldElement) {
        int curveCoordinateSystem = getCurveCoordinateSystem();
        if (curveCoordinateSystem != 1) {
            if (curveCoordinateSystem == 2 || curveCoordinateSystem == 3 || curveCoordinateSystem == 4) {
                ECFieldElement square = eCFieldElement.square();
                return createScaledPoint(square, square.multiply(eCFieldElement));
            } else if (curveCoordinateSystem != 6) {
                throw new IllegalStateException("not a projective coordinate system");
            }
        }
        return createScaledPoint(eCFieldElement, eCFieldElement);
    }

    /* access modifiers changed from: protected */
    public abstract boolean satisfiesCurveEquation();

    /* access modifiers changed from: protected */
    public boolean satisfiesOrder() {
        BigInteger order;
        return ECConstants.ONE.equals(this.curve.getCofactor()) || (order = this.curve.getOrder()) == null || ECAlgorithms.referenceMultiply(this, order).isInfinity();
    }

    public ECPoint scaleX(ECFieldElement eCFieldElement) {
        return isInfinity() ? this : getCurve().createRawPoint(getRawXCoord().multiply(eCFieldElement), getRawYCoord(), getRawZCoords());
    }

    public ECPoint scaleXNegateY(ECFieldElement eCFieldElement) {
        return isInfinity() ? this : getCurve().createRawPoint(getRawXCoord().multiply(eCFieldElement), getRawYCoord().negate(), getRawZCoords());
    }

    public ECPoint scaleY(ECFieldElement eCFieldElement) {
        return isInfinity() ? this : getCurve().createRawPoint(getRawXCoord(), getRawYCoord().multiply(eCFieldElement), getRawZCoords());
    }

    public ECPoint scaleYNegateX(ECFieldElement eCFieldElement) {
        return isInfinity() ? this : getCurve().createRawPoint(getRawXCoord().negate(), getRawYCoord().multiply(eCFieldElement), getRawZCoords());
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
        for (int i = 0; i < this.zs.length; i++) {
            stringBuffer.append(',');
            stringBuffer.append(this.zs[i]);
        }
        stringBuffer.append(')');
        return stringBuffer.toString();
    }

    public abstract ECPoint twice();

    public ECPoint twicePlus(ECPoint eCPoint) {
        return twice().add(eCPoint);
    }
}
