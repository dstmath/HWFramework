package com.android.org.bouncycastle.math.ec;

import com.android.org.bouncycastle.math.ec.ECFieldElement;
import java.math.BigInteger;
import java.util.Hashtable;

public abstract class ECPoint {
    protected static ECFieldElement[] EMPTY_ZS = new ECFieldElement[0];
    protected ECCurve curve;
    protected Hashtable preCompTable;
    protected boolean withCompression;
    protected ECFieldElement x;
    protected ECFieldElement y;
    protected ECFieldElement[] zs;

    public static abstract class AbstractF2m extends ECPoint {
        protected AbstractF2m(ECCurve curve, ECFieldElement x, ECFieldElement y) {
            super(curve, x, y);
        }

        protected AbstractF2m(ECCurve curve, ECFieldElement x, ECFieldElement y, ECFieldElement[] zs) {
            super(curve, x, y, zs);
        }

        /* access modifiers changed from: protected */
        public boolean satisfiesCurveEquation() {
            ECFieldElement rhs;
            ECFieldElement lhs;
            ECCurve curve = getCurve();
            ECFieldElement X = this.x;
            ECFieldElement A = curve.getA();
            ECFieldElement B = curve.getB();
            int coord = curve.getCoordinateSystem();
            if (coord == 6) {
                ECFieldElement Z = this.zs[0];
                boolean ZIsOne = Z.isOne();
                if (X.isZero()) {
                    ECFieldElement lhs2 = this.y.square();
                    ECFieldElement rhs2 = B;
                    if (!ZIsOne) {
                        rhs2 = rhs2.multiply(Z.square());
                    }
                    return lhs2.equals(rhs2);
                }
                ECFieldElement Y = this.y;
                ECFieldElement X2 = X.square();
                if (ZIsOne) {
                    lhs = Y.square().add(Y).add(A);
                    rhs = X2.square().add(B);
                } else {
                    ECFieldElement lhs3 = Z.square();
                    ECFieldElement Z4 = lhs3.square();
                    ECFieldElement lhs4 = Y.add(Z).multiplyPlusProduct(Y, A, lhs3);
                    rhs = X2.squarePlusProduct(B, Z4);
                    lhs = lhs4;
                }
                return lhs.multiply(X2).equals(rhs);
            }
            ECFieldElement Y2 = this.y;
            ECFieldElement lhs5 = Y2.add(X).multiply(Y2);
            switch (coord) {
                case 0:
                    break;
                case 1:
                    ECFieldElement Z2 = this.zs[0];
                    if (!Z2.isOne()) {
                        ECFieldElement Z3 = Z2.multiply(Z2.square());
                        lhs5 = lhs5.multiply(Z2);
                        A = A.multiply(Z2);
                        B = B.multiply(Z3);
                        break;
                    }
                    break;
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
            return lhs5.equals(X.add(A).multiply(X.square()).add(B));
        }

        public ECPoint scaleX(ECFieldElement scale) {
            if (isInfinity()) {
                return this;
            }
            switch (getCurveCoordinateSystem()) {
                case 5:
                    ECFieldElement X = getRawXCoord();
                    ECFieldElement L = getRawYCoord();
                    return getCurve().createRawPoint(X, L.add(X).divide(scale).add(X.multiply(scale)), getRawZCoords(), this.withCompression);
                case 6:
                    ECFieldElement X2 = getRawXCoord();
                    ECFieldElement L2 = getRawYCoord();
                    ECFieldElement Z = getRawZCoords()[0];
                    ECFieldElement X22 = X2.multiply(scale.square());
                    ECFieldElement L22 = L2.add(X2).add(X22);
                    ECFieldElement Z2 = Z.multiply(scale);
                    return getCurve().createRawPoint(X22, L22, new ECFieldElement[]{Z2}, this.withCompression);
                default:
                    return ECPoint.super.scaleX(scale);
            }
        }

        public ECPoint scaleY(ECFieldElement scale) {
            if (isInfinity()) {
                return this;
            }
            switch (getCurveCoordinateSystem()) {
                case 5:
                case 6:
                    ECFieldElement X = getRawXCoord();
                    return getCurve().createRawPoint(X, getRawYCoord().add(X).multiply(scale).add(X), getRawZCoords(), this.withCompression);
                default:
                    return ECPoint.super.scaleY(scale);
            }
        }

        public ECPoint subtract(ECPoint b) {
            if (b.isInfinity()) {
                return this;
            }
            return add(b.negate());
        }

        public AbstractF2m tau() {
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            int coord = curve.getCoordinateSystem();
            ECFieldElement X1 = this.x;
            switch (coord) {
                case 0:
                case 5:
                    return (AbstractF2m) curve.createRawPoint(X1.square(), this.y.square(), this.withCompression);
                case 1:
                case 6:
                    ECFieldElement Y1 = this.y;
                    ECFieldElement Z1 = this.zs[0];
                    return (AbstractF2m) curve.createRawPoint(X1.square(), Y1.square(), new ECFieldElement[]{Z1.square()}, this.withCompression);
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
        }

        public AbstractF2m tauPow(int pow) {
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            int coord = curve.getCoordinateSystem();
            ECFieldElement X1 = this.x;
            switch (coord) {
                case 0:
                case 5:
                    return (AbstractF2m) curve.createRawPoint(X1.squarePow(pow), this.y.squarePow(pow), this.withCompression);
                case 1:
                case 6:
                    ECFieldElement Y1 = this.y;
                    ECFieldElement Z1 = this.zs[0];
                    return (AbstractF2m) curve.createRawPoint(X1.squarePow(pow), Y1.squarePow(pow), new ECFieldElement[]{Z1.squarePow(pow)}, this.withCompression);
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
        }
    }

    public static abstract class AbstractFp extends ECPoint {
        protected AbstractFp(ECCurve curve, ECFieldElement x, ECFieldElement y) {
            super(curve, x, y);
        }

        protected AbstractFp(ECCurve curve, ECFieldElement x, ECFieldElement y, ECFieldElement[] zs) {
            super(curve, x, y, zs);
        }

        /* access modifiers changed from: protected */
        public boolean getCompressionYTilde() {
            return getAffineYCoord().testBitZero();
        }

        /* access modifiers changed from: protected */
        public boolean satisfiesCurveEquation() {
            ECFieldElement X = this.x;
            ECFieldElement Y = this.y;
            ECFieldElement A = this.curve.getA();
            ECFieldElement B = this.curve.getB();
            ECFieldElement lhs = Y.square();
            switch (getCurveCoordinateSystem()) {
                case 0:
                    break;
                case 1:
                    ECFieldElement Z = this.zs[0];
                    if (!Z.isOne()) {
                        ECFieldElement Z2 = Z.square();
                        ECFieldElement Z3 = Z.multiply(Z2);
                        lhs = lhs.multiply(Z);
                        A = A.multiply(Z2);
                        B = B.multiply(Z3);
                        break;
                    }
                    break;
                case 2:
                case 3:
                case 4:
                    ECFieldElement Z4 = this.zs[0];
                    if (!Z4.isOne()) {
                        ECFieldElement Z22 = Z4.square();
                        ECFieldElement Z42 = Z22.square();
                        ECFieldElement Z6 = Z22.multiply(Z42);
                        A = A.multiply(Z42);
                        B = B.multiply(Z6);
                        break;
                    }
                    break;
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
            return lhs.equals(X.square().add(A).multiply(X).add(B));
        }

        public ECPoint subtract(ECPoint b) {
            if (b.isInfinity()) {
                return this;
            }
            return add(b.negate());
        }
    }

    public static class F2m extends AbstractF2m {
        public F2m(ECCurve curve, ECFieldElement x, ECFieldElement y) {
            this(curve, x, y, false);
        }

        public F2m(ECCurve curve, ECFieldElement x, ECFieldElement y, boolean withCompression) {
            super(curve, x, y);
            boolean z = false;
            if ((x == null) == (y == null ? true : z)) {
                if (x != null) {
                    ECFieldElement.F2m.checkFieldElements(this.x, this.y);
                    if (curve != null) {
                        ECFieldElement.F2m.checkFieldElements(this.x, this.curve.getA());
                    }
                }
                this.withCompression = withCompression;
                return;
            }
            throw new IllegalArgumentException("Exactly one of the field elements is null");
        }

        F2m(ECCurve curve, ECFieldElement x, ECFieldElement y, ECFieldElement[] zs, boolean withCompression) {
            super(curve, x, y, zs);
            this.withCompression = withCompression;
        }

        /* access modifiers changed from: protected */
        public ECPoint detach() {
            return new F2m(null, getAffineXCoord(), getAffineYCoord());
        }

        public ECFieldElement getYCoord() {
            int coord = getCurveCoordinateSystem();
            switch (coord) {
                case 5:
                case 6:
                    ECFieldElement X = this.x;
                    ECFieldElement L = this.y;
                    if (isInfinity() || X.isZero()) {
                        return L;
                    }
                    ECFieldElement Y = L.add(X).multiply(X);
                    if (6 == coord) {
                        ECFieldElement Z = this.zs[0];
                        if (!Z.isOne()) {
                            Y = Y.divide(Z);
                        }
                    }
                    return Y;
                default:
                    return this.y;
            }
        }

        /* access modifiers changed from: protected */
        public boolean getCompressionYTilde() {
            ECFieldElement X = getRawXCoord();
            boolean z = false;
            if (X.isZero()) {
                return false;
            }
            ECFieldElement Y = getRawYCoord();
            switch (getCurveCoordinateSystem()) {
                case 5:
                case 6:
                    if (Y.testBitZero() != X.testBitZero()) {
                        z = true;
                    }
                    return z;
                default:
                    return Y.divide(X).testBitZero();
            }
        }

        public ECPoint add(ECPoint b) {
            ECFieldElement S2;
            ECFieldElement S1;
            ECFieldElement Z3;
            ECFieldElement L3;
            ECFieldElement X3;
            ECPoint eCPoint = b;
            if (isInfinity()) {
                return eCPoint;
            }
            if (b.isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            int coord = curve.getCoordinateSystem();
            ECFieldElement X1 = this.x;
            ECFieldElement X2 = eCPoint.x;
            if (coord != 6) {
                switch (coord) {
                    case 0:
                        ECFieldElement Y1 = this.y;
                        ECFieldElement Y2 = eCPoint.y;
                        ECFieldElement dx = X1.add(X2);
                        ECFieldElement dy = Y1.add(Y2);
                        if (!dx.isZero()) {
                            ECFieldElement L = dy.divide(dx);
                            ECFieldElement X32 = L.square().add(L).add(dx).add(curve.getA());
                            return new F2m(curve, X32, L.multiply(X1.add(X32)).add(X32).add(Y1), this.withCompression);
                        } else if (dy.isZero()) {
                            return twice();
                        } else {
                            return curve.getInfinity();
                        }
                    case 1:
                        ECFieldElement Y12 = this.y;
                        ECFieldElement Z1 = this.zs[0];
                        ECFieldElement Y22 = eCPoint.y;
                        ECFieldElement Z2 = eCPoint.zs[0];
                        boolean Z2IsOne = Z2.isOne();
                        ECFieldElement U1 = Z1.multiply(Y22);
                        ECFieldElement U2 = Z2IsOne ? Y12 : Y12.multiply(Z2);
                        ECFieldElement U = U1.add(U2);
                        ECFieldElement V1 = Z1.multiply(X2);
                        ECFieldElement V2 = Z2IsOne ? X1 : X1.multiply(Z2);
                        ECFieldElement V = V1.add(V2);
                        if (!V.isZero()) {
                            int i = coord;
                            ECFieldElement VSq = V.square();
                            ECFieldElement eCFieldElement = Y22;
                            ECFieldElement Y23 = VSq.multiply(V);
                            ECFieldElement W = Z2IsOne ? Z1 : Z1.multiply(Z2);
                            ECFieldElement eCFieldElement2 = Z1;
                            ECFieldElement uv = U.add(V);
                            ECFieldElement V12 = V1;
                            ECFieldElement eCFieldElement3 = X2;
                            ECFieldElement X22 = W;
                            ECFieldElement A = uv.multiplyPlusProduct(U, VSq, curve.getA()).multiply(X22).add(Y23);
                            ECFieldElement eCFieldElement4 = V2;
                            ECFieldElement eCFieldElement5 = VSq;
                            ECFieldElement eCFieldElement6 = U1;
                            ECFieldElement eCFieldElement7 = A;
                            ECFieldElement eCFieldElement8 = V12;
                            ECFieldElement V13 = V;
                            ECFieldElement eCFieldElement9 = U;
                            ECFieldElement eCFieldElement10 = U2;
                            F2m f2m = new F2m(curve, V.multiply(A), U.multiplyPlusProduct(X1, V, Y12).multiplyPlusProduct(Z2IsOne ? VSq : VSq.multiply(Z2), uv, A), new ECFieldElement[]{Y23.multiply(X22)}, this.withCompression);
                            return f2m;
                        } else if (U.isZero()) {
                            return twice();
                        } else {
                            return curve.getInfinity();
                        }
                    default:
                        throw new IllegalStateException("unsupported coordinate system");
                }
            } else {
                ECFieldElement X23 = X2;
                if (!X1.isZero()) {
                    ECFieldElement L1 = this.y;
                    ECFieldElement Z12 = this.zs[0];
                    ECFieldElement L2 = eCPoint.y;
                    ECFieldElement Z22 = eCPoint.zs[0];
                    boolean Z1IsOne = Z12.isOne();
                    ECFieldElement U22 = X23;
                    ECFieldElement S22 = L2;
                    if (!Z1IsOne) {
                        U22 = U22.multiply(Z12);
                        S22 = S22.multiply(Z12);
                    }
                    ECFieldElement U23 = U22;
                    ECFieldElement S23 = S22;
                    boolean Z2IsOne2 = Z22.isOne();
                    ECFieldElement U12 = X1;
                    ECFieldElement S12 = L1;
                    if (!Z2IsOne2) {
                        U12 = U12.multiply(Z22);
                        S12 = S12.multiply(Z22);
                    }
                    ECFieldElement U13 = U12;
                    ECFieldElement S13 = S12;
                    ECFieldElement A2 = S13.add(S23);
                    ECFieldElement B = U13.add(U23);
                    if (!B.isZero()) {
                        if (X23.isZero()) {
                            ECPoint p = normalize();
                            ECFieldElement X12 = p.getXCoord();
                            S1 = S13;
                            ECFieldElement S14 = p.getYCoord();
                            ECPoint eCPoint2 = p;
                            S2 = S23;
                            ECFieldElement Y24 = L2;
                            ECFieldElement S24 = S14.add(Y24).divide(X12);
                            ECFieldElement eCFieldElement11 = Y24;
                            ECFieldElement eCFieldElement12 = X23;
                            X3 = S24.square().add(S24).add(X12).add(curve.getA());
                            if (X3.isZero()) {
                                ECFieldElement eCFieldElement13 = L2;
                                boolean z = Z1IsOne;
                                return new F2m(curve, X3, curve.getB().sqrt(), this.withCompression);
                            }
                            boolean z2 = Z1IsOne;
                            L3 = S24.multiply(X12.add(X3)).add(X3).add(S14).divide(X3).add(X3);
                            ECFieldElement eCFieldElement14 = B;
                            Z3 = curve.fromBigInteger(ECConstants.ONE);
                        } else {
                            S1 = S13;
                            S2 = S23;
                            ECFieldElement eCFieldElement15 = X23;
                            ECFieldElement eCFieldElement16 = L2;
                            boolean Z1IsOne2 = Z1IsOne;
                            ECFieldElement B2 = B.square();
                            ECFieldElement AU1 = A2.multiply(U13);
                            ECFieldElement AU2 = A2.multiply(U23);
                            ECFieldElement X33 = AU1.multiply(AU2);
                            if (X33.isZero()) {
                                return new F2m(curve, X33, curve.getB().sqrt(), this.withCompression);
                            }
                            ECFieldElement ABZ2 = A2.multiply(B2);
                            if (!Z2IsOne2) {
                                ABZ2 = ABZ2.multiply(Z22);
                            }
                            L3 = AU2.add(B2).squarePlusProduct(ABZ2, L1.add(Z12));
                            Z3 = ABZ2;
                            if (!Z1IsOne2) {
                                ECFieldElement ABZ22 = B2;
                                Z3 = Z3.multiply(Z12);
                            } else {
                                ECFieldElement eCFieldElement17 = B2;
                            }
                            X3 = X33;
                        }
                        ECFieldElement eCFieldElement18 = A2;
                        ECFieldElement eCFieldElement19 = U13;
                        ECFieldElement eCFieldElement20 = S1;
                        ECFieldElement eCFieldElement21 = U23;
                        ECFieldElement eCFieldElement22 = S2;
                        F2m f2m2 = new F2m(curve, X3, L3, new ECFieldElement[]{Z3}, this.withCompression);
                        return f2m2;
                    } else if (A2.isZero()) {
                        return twice();
                    } else {
                        return curve.getInfinity();
                    }
                } else if (X23.isZero()) {
                    return curve.getInfinity();
                } else {
                    return eCPoint.add(this);
                }
            }
        }

        public ECPoint twice() {
            ECFieldElement b;
            ECFieldElement L3;
            ECFieldElement t2;
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            ECFieldElement X1 = this.x;
            if (X1.isZero()) {
                return curve.getInfinity();
            }
            int coord = curve.getCoordinateSystem();
            if (coord != 6) {
                switch (coord) {
                    case 0:
                        ECFieldElement L1 = this.y.divide(X1).add(X1);
                        ECFieldElement X3 = L1.square().add(L1).add(curve.getA());
                        return new F2m(curve, X3, X1.squarePlusProduct(X3, L1.addOne()), this.withCompression);
                    case 1:
                        ECFieldElement Y1 = this.y;
                        ECFieldElement Z1 = this.zs[0];
                        boolean Z1IsOne = Z1.isOne();
                        ECFieldElement X1Z1 = Z1IsOne ? X1 : X1.multiply(Z1);
                        ECFieldElement Y1Z1 = Z1IsOne ? Y1 : Y1.multiply(Z1);
                        ECFieldElement X1Sq = X1.square();
                        ECFieldElement S = X1Sq.add(Y1Z1);
                        ECFieldElement V = X1Z1;
                        ECFieldElement vSquared = V.square();
                        ECFieldElement sv = S.add(V);
                        ECFieldElement h = sv.multiplyPlusProduct(S, vSquared, curve.getA());
                        ECFieldElement eCFieldElement = sv;
                        ECFieldElement eCFieldElement2 = h;
                        ECFieldElement eCFieldElement3 = vSquared;
                        ECFieldElement eCFieldElement4 = V;
                        ECFieldElement eCFieldElement5 = S;
                        F2m f2m = new F2m(curve, V.multiply(h), X1Sq.square().multiplyPlusProduct(V, h, sv), new ECFieldElement[]{V.multiply(vSquared)}, this.withCompression);
                        return f2m;
                    default:
                        throw new IllegalStateException("unsupported coordinate system");
                }
            } else {
                ECFieldElement L12 = this.y;
                ECFieldElement Z12 = this.zs[0];
                boolean Z1IsOne2 = Z12.isOne();
                ECFieldElement L1Z1 = Z1IsOne2 ? L12 : L12.multiply(Z12);
                ECFieldElement Z1Sq = Z1IsOne2 ? Z12 : Z12.square();
                ECFieldElement a = curve.getA();
                ECFieldElement aZ1Sq = Z1IsOne2 ? a : a.multiply(Z1Sq);
                ECFieldElement T = L12.square().add(L1Z1).add(aZ1Sq);
                if (T.isZero()) {
                    return new F2m(curve, T, curve.getB().sqrt(), this.withCompression);
                }
                ECFieldElement X32 = T.square();
                ECFieldElement Z3 = Z1IsOne2 ? T : T.multiply(Z1Sq);
                ECFieldElement b2 = curve.getB();
                int i = coord;
                if (b2.bitLength() < (curve.getFieldSize() >> 1)) {
                    ECFieldElement t1 = L12.add(X1).square();
                    if (b2.isOne()) {
                        t2 = aZ1Sq.add(Z1Sq).square();
                    } else {
                        t2 = aZ1Sq.squarePlusProduct(b2, Z1Sq.square());
                    }
                    b = b2;
                    L3 = t1.add(T).add(Z1Sq).multiply(t1).add(t2).add(X32);
                    if (a.isZero()) {
                        L3 = L3.add(Z3);
                    } else if (!a.isOne()) {
                        ECFieldElement eCFieldElement6 = t1;
                        L3 = L3.add(a.addOne().multiply(Z3));
                    }
                } else {
                    b = b2;
                    L3 = (Z1IsOne2 ? X1 : X1.multiply(Z12)).squarePlusProduct(T, L1Z1).add(X32).add(Z3);
                }
                ECFieldElement eCFieldElement7 = b;
                ECFieldElement eCFieldElement8 = Z3;
                ECFieldElement eCFieldElement9 = X32;
                ECFieldElement eCFieldElement10 = T;
                ECFieldElement eCFieldElement11 = aZ1Sq;
                F2m f2m2 = new F2m(curve, X32, L3, new ECFieldElement[]{Z3}, this.withCompression);
                return f2m2;
            }
        }

        public ECPoint twicePlus(ECPoint b) {
            ECPoint eCPoint = b;
            if (isInfinity()) {
                return eCPoint;
            }
            if (b.isInfinity()) {
                return twice();
            }
            ECCurve curve = getCurve();
            ECFieldElement X1 = this.x;
            if (X1.isZero()) {
                return eCPoint;
            }
            int coord = curve.getCoordinateSystem();
            if (coord != 6) {
                return twice().add(eCPoint);
            }
            ECFieldElement X2 = eCPoint.x;
            ECFieldElement Z2 = eCPoint.zs[0];
            if (X2.isZero()) {
                int i = coord;
                ECFieldElement eCFieldElement = X2;
            } else if (!Z2.isOne()) {
                ECFieldElement eCFieldElement2 = X1;
                int i2 = coord;
                ECFieldElement eCFieldElement3 = X2;
            } else {
                ECFieldElement L1 = this.y;
                ECFieldElement Z1 = this.zs[0];
                ECFieldElement L2 = eCPoint.y;
                ECFieldElement X1Sq = X1.square();
                ECFieldElement L1Sq = L1.square();
                ECFieldElement Z1Sq = Z1.square();
                ECFieldElement L1Z1 = L1.multiply(Z1);
                ECFieldElement T = curve.getA().multiply(Z1Sq).add(L1Sq).add(L1Z1);
                ECFieldElement L2plus1 = L2.addOne();
                ECFieldElement eCFieldElement4 = L1Z1;
                ECFieldElement A = curve.getA().add(L2plus1).multiply(Z1Sq).add(L1Sq).multiplyPlusProduct(T, X1Sq, Z1Sq);
                ECFieldElement eCFieldElement5 = X1;
                ECFieldElement X12 = X2.multiply(Z1Sq);
                ECFieldElement eCFieldElement6 = L1Sq;
                ECFieldElement L1Sq2 = X12.add(T).square();
                if (L1Sq2.isZero()) {
                    if (A.isZero()) {
                        return b.twice();
                    }
                    return curve.getInfinity();
                } else if (A.isZero()) {
                    ECFieldElement eCFieldElement7 = X1Sq;
                    int i3 = coord;
                    ECFieldElement eCFieldElement8 = X2;
                    return new F2m(curve, A, curve.getB().sqrt(), this.withCompression);
                } else {
                    int i4 = coord;
                    ECFieldElement eCFieldElement9 = X2;
                    ECFieldElement X3 = A.square().multiply(X12);
                    ECFieldElement Z3 = A.multiply(L1Sq2).multiply(Z1Sq);
                    ECFieldElement eCFieldElement10 = X12;
                    ECFieldElement eCFieldElement11 = T;
                    ECFieldElement eCFieldElement12 = L2plus1;
                    ECFieldElement eCFieldElement13 = A;
                    ECFieldElement eCFieldElement14 = Z1Sq;
                    ECFieldElement eCFieldElement15 = L1Sq2;
                    F2m f2m = new F2m(curve, X3, A.add(L1Sq2).square().multiplyPlusProduct(T, L2plus1, Z3), new ECFieldElement[]{Z3}, this.withCompression);
                    return f2m;
                }
            }
            return twice().add(eCPoint);
        }

        public ECPoint negate() {
            if (isInfinity()) {
                return this;
            }
            ECFieldElement X = this.x;
            if (X.isZero()) {
                return this;
            }
            switch (getCurveCoordinateSystem()) {
                case 0:
                    return new F2m(this.curve, X, this.y.add(X), this.withCompression);
                case 1:
                    ECFieldElement Y = this.y;
                    ECFieldElement Z = this.zs[0];
                    F2m f2m = new F2m(this.curve, X, Y.add(X), new ECFieldElement[]{Z}, this.withCompression);
                    return f2m;
                case 5:
                    return new F2m(this.curve, X, this.y.addOne(), this.withCompression);
                case 6:
                    ECFieldElement L = this.y;
                    ECFieldElement Z2 = this.zs[0];
                    F2m f2m2 = new F2m(this.curve, X, L.add(Z2), new ECFieldElement[]{Z2}, this.withCompression);
                    return f2m2;
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
        }
    }

    public static class Fp extends AbstractFp {
        public Fp(ECCurve curve, ECFieldElement x, ECFieldElement y) {
            this(curve, x, y, false);
        }

        public Fp(ECCurve curve, ECFieldElement x, ECFieldElement y, boolean withCompression) {
            super(curve, x, y);
            boolean z = false;
            if ((x == null) == (y == null ? true : z)) {
                this.withCompression = withCompression;
                return;
            }
            throw new IllegalArgumentException("Exactly one of the field elements is null");
        }

        Fp(ECCurve curve, ECFieldElement x, ECFieldElement y, ECFieldElement[] zs, boolean withCompression) {
            super(curve, x, y, zs);
            this.withCompression = withCompression;
        }

        /* access modifiers changed from: protected */
        public ECPoint detach() {
            return new Fp(null, getAffineXCoord(), getAffineYCoord());
        }

        public ECFieldElement getZCoord(int index) {
            if (index == 1 && 4 == getCurveCoordinateSystem()) {
                return getJacobianModifiedW();
            }
            return super.getZCoord(index);
        }

        public ECPoint add(ECPoint b) {
            int coord;
            ECFieldElement Y3;
            ECFieldElement Y1;
            ECFieldElement X1;
            ECFieldElement A1;
            ECFieldElement Z1Cubed;
            ECFieldElement U2;
            ECFieldElement Z1Squared;
            ECFieldElement Z2Cubed;
            ECFieldElement U1;
            ECFieldElement Z2Squared;
            ECFieldElement R;
            ECPoint eCPoint = b;
            if (isInfinity()) {
                return eCPoint;
            }
            if (b.isInfinity()) {
                return this;
            }
            if (this == eCPoint) {
                return twice();
            }
            ECCurve curve = getCurve();
            int coord2 = curve.getCoordinateSystem();
            ECFieldElement X12 = this.x;
            ECFieldElement Y12 = this.y;
            ECFieldElement X2 = eCPoint.x;
            ECFieldElement Y2 = eCPoint.y;
            if (coord2 != 4) {
                switch (coord2) {
                    case 0:
                        ECFieldElement dx = X2.subtract(X12);
                        ECFieldElement dy = Y2.subtract(Y12);
                        if (!dx.isZero()) {
                            ECFieldElement gamma = dy.divide(dx);
                            ECFieldElement X3 = gamma.square().subtract(X12).subtract(X2);
                            return new Fp(curve, X3, gamma.multiply(X12.subtract(X3)).subtract(Y12), this.withCompression);
                        } else if (dy.isZero()) {
                            return twice();
                        } else {
                            return curve.getInfinity();
                        }
                    case 1:
                        ECFieldElement Z1 = this.zs[0];
                        ECFieldElement Z2 = eCPoint.zs[0];
                        boolean Z1IsOne = Z1.isOne();
                        boolean Z2IsOne = Z2.isOne();
                        ECFieldElement u1 = Z1IsOne ? Y2 : Y2.multiply(Z1);
                        ECFieldElement u2 = Z2IsOne ? Y12 : Y12.multiply(Z2);
                        ECFieldElement u = u1.subtract(u2);
                        ECFieldElement v1 = Z1IsOne ? X2 : X2.multiply(Z1);
                        ECFieldElement v2 = Z2IsOne ? X12 : X12.multiply(Z2);
                        ECFieldElement v = v1.subtract(v2);
                        if (!v.isZero()) {
                            ECFieldElement w = Z1IsOne ? Z2 : Z2IsOne ? Z1 : Z1.multiply(Z2);
                            ECFieldElement eCFieldElement = Z1;
                            ECFieldElement vSquared = v.square();
                            ECFieldElement eCFieldElement2 = Z2;
                            ECFieldElement vCubed = vSquared.multiply(v);
                            int i = coord2;
                            ECFieldElement vSquaredV2 = vSquared.multiply(v2);
                            ECFieldElement eCFieldElement3 = vSquared;
                            ECFieldElement w2 = w;
                            ECFieldElement eCFieldElement4 = v2;
                            ECFieldElement A = u.square().multiply(w2).subtract(vCubed).subtract(two(vSquaredV2));
                            ECFieldElement eCFieldElement5 = v1;
                            ECFieldElement v12 = v;
                            ECFieldElement eCFieldElement6 = A;
                            ECFieldElement eCFieldElement7 = u;
                            ECFieldElement eCFieldElement8 = u2;
                            ECFieldElement eCFieldElement9 = u1;
                            Fp fp = new Fp(curve, v.multiply(A), vSquaredV2.subtract(A).multiplyMinusProduct(u, u2, vCubed), new ECFieldElement[]{vCubed.multiply(w2)}, this.withCompression);
                            return fp;
                        } else if (u.isZero()) {
                            return twice();
                        } else {
                            return curve.getInfinity();
                        }
                    case 2:
                        coord = coord2;
                        break;
                    default:
                        throw new IllegalStateException("unsupported coordinate system");
                }
            } else {
                coord = coord2;
            }
            ECFieldElement Z12 = this.zs[0];
            ECFieldElement Z22 = eCPoint.zs[0];
            boolean Z1IsOne2 = Z12.isOne();
            if (Z1IsOne2 || !Z12.equals(Z22)) {
                if (Z1IsOne2) {
                    Z1Squared = Z12;
                    U2 = X2;
                    Z1Cubed = Y2;
                } else {
                    Z1Squared = Z12.square();
                    U2 = Z1Squared.multiply(X2);
                    Z1Cubed = Z1Squared.multiply(Z12).multiply(Y2);
                }
                boolean Z2IsOne2 = Z22.isOne();
                if (Z2IsOne2) {
                    Z2Squared = Z22;
                    U1 = X12;
                    Z2Cubed = Y12;
                } else {
                    Z2Squared = Z22.square();
                    U1 = Z2Squared.multiply(X12);
                    Z2Cubed = Z2Squared.multiply(Z22).multiply(Y12);
                }
                ECFieldElement eCFieldElement10 = Z1Squared;
                ECFieldElement H = U1.subtract(U2);
                ECFieldElement eCFieldElement11 = U2;
                ECFieldElement U22 = Z2Cubed.subtract(Z1Cubed);
                if (!H.isZero()) {
                    ECFieldElement eCFieldElement12 = Z1Cubed;
                    ECFieldElement Z3Squared = H.square();
                    ECFieldElement eCFieldElement13 = Z2Squared;
                    ECFieldElement Z2Squared2 = Z3Squared.multiply(H);
                    ECFieldElement eCFieldElement14 = X12;
                    ECFieldElement X13 = Z3Squared.multiply(U1);
                    ECFieldElement eCFieldElement15 = U1;
                    ECFieldElement eCFieldElement16 = Y12;
                    ECFieldElement X32 = U22.square().add(Z2Squared2).subtract(two(X13));
                    ECFieldElement Y32 = X13.subtract(X32).multiplyMinusProduct(U22, Z2Squared2, Z2Cubed);
                    ECFieldElement Z3 = H;
                    if (!Z1IsOne2) {
                        ECFieldElement eCFieldElement17 = U22;
                        R = Z3.multiply(Z12);
                    } else {
                        ECFieldElement R2 = U22;
                        R = Z3;
                    }
                    if (!Z2IsOne2) {
                        R = R.multiply(Z22);
                    }
                    if (R == H) {
                        A1 = R;
                        X1 = Z3Squared;
                        Y3 = Y32;
                    } else {
                        A1 = R;
                        Y3 = Y32;
                        X1 = null;
                    }
                    Y1 = X32;
                } else if (U22.isZero()) {
                    return twice();
                } else {
                    return curve.getInfinity();
                }
            } else {
                ECFieldElement dx2 = X12.subtract(X2);
                ECFieldElement dy2 = Y12.subtract(Y2);
                if (!dx2.isZero()) {
                    ECFieldElement C = dx2.square();
                    ECFieldElement W1 = X12.multiply(C);
                    ECFieldElement W2 = X2.multiply(C);
                    ECFieldElement A12 = W1.subtract(W2).multiply(Y12);
                    ECFieldElement X33 = dy2.square().subtract(W1).subtract(W2);
                    ECFieldElement eCFieldElement18 = W2;
                    ECFieldElement Y33 = W1.subtract(X33).multiply(dy2).subtract(A12);
                    ECFieldElement eCFieldElement19 = A12;
                    A1 = dx2.multiply(Z12);
                    Y3 = Y33;
                    ECFieldElement eCFieldElement20 = X12;
                    ECFieldElement eCFieldElement21 = Y12;
                    X1 = null;
                    Y1 = X33;
                } else if (dy2.isZero()) {
                    return twice();
                } else {
                    return curve.getInfinity();
                }
            }
            int coord3 = coord;
            int i2 = coord3;
            Fp fp2 = new Fp(curve, Y1, Y3, coord3 == 4 ? new ECFieldElement[]{A1, calculateJacobianModifiedW(A1, X1)} : new ECFieldElement[]{A1}, this.withCompression);
            return fp2;
        }

        public ECPoint twice() {
            ECFieldElement M;
            ECFieldElement Z1Squared;
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            ECFieldElement Y1 = this.y;
            if (Y1.isZero()) {
                return curve.getInfinity();
            }
            int coord = curve.getCoordinateSystem();
            ECFieldElement X1 = this.x;
            if (coord == 4) {
                return twiceJacobianModified(true);
            }
            switch (coord) {
                case 0:
                    ECFieldElement gamma = three(X1.square()).add(getCurve().getA()).divide(two(Y1));
                    ECFieldElement X3 = gamma.square().subtract(two(X1));
                    return new Fp(curve, X3, gamma.multiply(X1.subtract(X3)).subtract(Y1), this.withCompression);
                case 1:
                    ECFieldElement Z1 = this.zs[0];
                    boolean Z1IsOne = Z1.isOne();
                    ECFieldElement w = curve.getA();
                    if (!w.isZero() && !Z1IsOne) {
                        w = w.multiply(Z1.square());
                    }
                    ECFieldElement w2 = w.add(three(X1.square()));
                    ECFieldElement s = Z1IsOne ? Y1 : Y1.multiply(Z1);
                    ECFieldElement t = Z1IsOne ? Y1.square() : s.multiply(Y1);
                    ECFieldElement B = X1.multiply(t);
                    ECFieldElement _4B = four(B);
                    ECFieldElement h = w2.square().subtract(two(_4B));
                    ECFieldElement _2s = two(s);
                    ECFieldElement X32 = h.multiply(_2s);
                    ECFieldElement _2t = two(t);
                    ECFieldElement h2 = h;
                    ECFieldElement Y3 = _4B.subtract(h).multiply(w2).subtract(two(_2t.square()));
                    ECFieldElement _4sSquared = Z1IsOne ? two(_2t) : _2s.square();
                    ECFieldElement eCFieldElement = _2t;
                    ECFieldElement eCFieldElement2 = _2s;
                    ECFieldElement eCFieldElement3 = h2;
                    ECFieldElement h3 = _4sSquared;
                    ECFieldElement eCFieldElement4 = _4B;
                    ECFieldElement eCFieldElement5 = B;
                    Fp fp = new Fp(curve, X32, Y3, new ECFieldElement[]{two(_4sSquared).multiply(s)}, this.withCompression);
                    return fp;
                case 2:
                    ECFieldElement Z12 = this.zs[0];
                    boolean Z1IsOne2 = Z12.isOne();
                    ECFieldElement Y1Squared = Y1.square();
                    ECFieldElement T = Y1Squared.square();
                    ECFieldElement a4 = curve.getA();
                    ECFieldElement a4Neg = a4.negate();
                    if (a4Neg.toBigInteger().equals(BigInteger.valueOf(3))) {
                        ECFieldElement Z1Squared2 = Z1IsOne2 ? Z12 : Z12.square();
                        M = three(X1.add(Z1Squared2).multiply(X1.subtract(Z1Squared2)));
                        Z1Squared = four(Y1Squared.multiply(X1));
                    } else {
                        ECFieldElement X1Squared = X1.square();
                        M = three(X1Squared);
                        if (Z1IsOne2) {
                            M = M.add(a4);
                            ECFieldElement eCFieldElement6 = X1Squared;
                        } else if (!a4.isZero()) {
                            ECFieldElement Z1Pow4 = Z12.square().square();
                            ECFieldElement eCFieldElement7 = X1Squared;
                            if (a4Neg.bitLength() < a4.bitLength()) {
                                M = M.subtract(Z1Pow4.multiply(a4Neg));
                            } else {
                                M = M.add(Z1Pow4.multiply(a4));
                            }
                        }
                        Z1Squared = four(X1.multiply(Y1Squared));
                    }
                    ECFieldElement M2 = M;
                    ECFieldElement M3 = Z1Squared;
                    ECFieldElement X33 = M2.square().subtract(two(M3));
                    ECFieldElement Y32 = M3.subtract(X33).multiply(M2).subtract(eight(T));
                    ECFieldElement Z3 = two(Y1);
                    if (!Z1IsOne2) {
                        Z3 = Z3.multiply(Z12);
                    }
                    ECFieldElement eCFieldElement8 = X33;
                    ECFieldElement eCFieldElement9 = M3;
                    ECFieldElement eCFieldElement10 = M2;
                    ECFieldElement eCFieldElement11 = a4Neg;
                    Fp fp2 = new Fp(curve, X33, Y32, new ECFieldElement[]{Z3}, this.withCompression);
                    return fp2;
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
        }

        public ECPoint twicePlus(ECPoint b) {
            ECPoint eCPoint = b;
            if (this == eCPoint) {
                return threeTimes();
            }
            if (isInfinity()) {
                return eCPoint;
            }
            if (b.isInfinity()) {
                return twice();
            }
            ECFieldElement Y1 = this.y;
            if (Y1.isZero()) {
                return eCPoint;
            }
            ECCurve curve = getCurve();
            int coord = curve.getCoordinateSystem();
            if (coord == 0) {
                ECFieldElement X1 = this.x;
                ECFieldElement X2 = eCPoint.x;
                ECFieldElement Y2 = eCPoint.y;
                ECFieldElement dx = X2.subtract(X1);
                ECFieldElement dy = Y2.subtract(Y1);
                if (!dx.isZero()) {
                    ECFieldElement X = dx.square();
                    ECFieldElement d = X.multiply(two(X1).add(X2)).subtract(dy.square());
                    if (d.isZero()) {
                        return curve.getInfinity();
                    }
                    ECFieldElement I = d.multiply(dx).invert();
                    ECFieldElement L1 = d.multiply(I).multiply(dy);
                    ECFieldElement L2 = two(Y1).multiply(X).multiply(dx).multiply(I).subtract(L1);
                    int i = coord;
                    ECFieldElement eCFieldElement = Y2;
                    ECFieldElement X4 = L2.subtract(L1).multiply(L1.add(L2)).add(X2);
                    ECFieldElement eCFieldElement2 = L2;
                    ECFieldElement eCFieldElement3 = Y1;
                    return new Fp(curve, X4, X1.subtract(X4).multiply(L2).subtract(Y1), this.withCompression);
                } else if (dy.isZero()) {
                    return threeTimes();
                } else {
                    return this;
                }
            } else if (coord != 4) {
                return twice().add(eCPoint);
            } else {
                return twiceJacobianModified(false).add(eCPoint);
            }
        }

        public ECPoint threeTimes() {
            if (isInfinity()) {
                return this;
            }
            ECFieldElement Y1 = this.y;
            if (Y1.isZero()) {
                return this;
            }
            ECCurve curve = getCurve();
            int coord = curve.getCoordinateSystem();
            if (coord == 0) {
                ECFieldElement X1 = this.x;
                ECFieldElement _2Y1 = two(Y1);
                ECFieldElement X = _2Y1.square();
                ECFieldElement Z = three(X1.square()).add(getCurve().getA());
                ECFieldElement d = three(X1).multiply(X).subtract(Z.square());
                if (d.isZero()) {
                    return getCurve().getInfinity();
                }
                ECFieldElement I = d.multiply(_2Y1).invert();
                ECFieldElement L1 = d.multiply(I).multiply(Z);
                ECFieldElement L2 = X.square().multiply(I).subtract(L1);
                ECFieldElement X4 = L2.subtract(L1).multiply(L1.add(L2)).add(X1);
                ECFieldElement eCFieldElement = Y1;
                int i = coord;
                return new Fp(curve, X4, X1.subtract(X4).multiply(L2).subtract(Y1), this.withCompression);
            } else if (coord != 4) {
                return twice().add(this);
            } else {
                return twiceJacobianModified(false).add(this);
            }
        }

        public ECPoint timesPow2(int e) {
            int i = e;
            if (i < 0) {
                throw new IllegalArgumentException("'e' cannot be negative");
            } else if (i == 0 || isInfinity()) {
                return this;
            } else {
                if (i == 1) {
                    return twice();
                }
                ECCurve curve = getCurve();
                ECFieldElement Y1 = this.y;
                if (Y1.isZero()) {
                    return curve.getInfinity();
                }
                int coord = curve.getCoordinateSystem();
                ECFieldElement W1 = curve.getA();
                ECFieldElement X1 = this.x;
                ECFieldElement Z1 = this.zs.length < 1 ? curve.fromBigInteger(ECConstants.ONE) : this.zs[0];
                if (!Z1.isOne()) {
                    if (coord != 4) {
                        switch (coord) {
                            case 0:
                                break;
                            case 1:
                                ECFieldElement Z1Sq = Z1.square();
                                X1 = X1.multiply(Z1);
                                Y1 = Y1.multiply(Z1Sq);
                                W1 = calculateJacobianModifiedW(Z1, Z1Sq);
                                break;
                            case 2:
                                W1 = calculateJacobianModifiedW(Z1, null);
                                break;
                            default:
                                throw new IllegalStateException("unsupported coordinate system");
                        }
                    } else {
                        W1 = getJacobianModifiedW();
                    }
                }
                ECFieldElement Y12 = Y1;
                ECFieldElement W12 = W1;
                ECFieldElement X12 = X1;
                ECFieldElement Z12 = Z1;
                int i2 = 0;
                while (i2 < i) {
                    if (Y12.isZero()) {
                        return curve.getInfinity();
                    }
                    ECFieldElement M = three(X12.square());
                    ECFieldElement _2Y1 = two(Y12);
                    ECFieldElement _2Y1Squared = _2Y1.multiply(Y12);
                    ECFieldElement S = two(X12.multiply(_2Y1Squared));
                    ECFieldElement _4T = _2Y1Squared.square();
                    ECFieldElement _8T = two(_4T);
                    if (!W12.isZero()) {
                        M = M.add(W12);
                        W12 = two(_8T.multiply(W12));
                    }
                    ECFieldElement eCFieldElement = _4T;
                    X12 = M.square().subtract(two(S));
                    Y12 = M.multiply(S.subtract(X12)).subtract(_8T);
                    Z12 = Z12.isOne() ? _2Y1 : _2Y1.multiply(Z12);
                    i2++;
                    i = e;
                }
                if (coord != 4) {
                    switch (coord) {
                        case 0:
                            ECFieldElement X13 = Z12.invert();
                            ECFieldElement zInv2 = X13.square();
                            return new Fp(curve, X12.multiply(zInv2), Y12.multiply(zInv2.multiply(X13)), this.withCompression);
                        case 1:
                            Fp fp = new Fp(curve, X12.multiply(Z12), Y12, new ECFieldElement[]{Z12.multiply(Z12.square())}, this.withCompression);
                            return fp;
                        case 2:
                            Fp fp2 = new Fp(curve, X12, Y12, new ECFieldElement[]{Z12}, this.withCompression);
                            return fp2;
                        default:
                            throw new IllegalStateException("unsupported coordinate system");
                    }
                } else {
                    Fp fp3 = new Fp(curve, X12, Y12, new ECFieldElement[]{Z12, W12}, this.withCompression);
                    return fp3;
                }
            }
        }

        /* access modifiers changed from: protected */
        public ECFieldElement two(ECFieldElement x) {
            return x.add(x);
        }

        /* access modifiers changed from: protected */
        public ECFieldElement three(ECFieldElement x) {
            return two(x).add(x);
        }

        /* access modifiers changed from: protected */
        public ECFieldElement four(ECFieldElement x) {
            return two(two(x));
        }

        /* access modifiers changed from: protected */
        public ECFieldElement eight(ECFieldElement x) {
            return four(two(x));
        }

        /* access modifiers changed from: protected */
        public ECFieldElement doubleProductFromSquares(ECFieldElement a, ECFieldElement b, ECFieldElement aSquared, ECFieldElement bSquared) {
            return a.add(b).square().subtract(aSquared).subtract(bSquared);
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
        public ECFieldElement calculateJacobianModifiedW(ECFieldElement Z, ECFieldElement ZSquared) {
            ECFieldElement W;
            ECFieldElement a4 = getCurve().getA();
            if (a4.isZero() || Z.isOne()) {
                return a4;
            }
            if (ZSquared == null) {
                ZSquared = Z.square();
            }
            ECFieldElement W2 = ZSquared.square();
            ECFieldElement a4Neg = a4.negate();
            if (a4Neg.bitLength() < a4.bitLength()) {
                W = W2.multiply(a4Neg).negate();
            } else {
                W = W2.multiply(a4);
            }
            return W;
        }

        /* access modifiers changed from: protected */
        public ECFieldElement getJacobianModifiedW() {
            ECFieldElement W = this.zs[1];
            if (W != null) {
                return W;
            }
            ECFieldElement[] eCFieldElementArr = this.zs;
            ECFieldElement calculateJacobianModifiedW = calculateJacobianModifiedW(this.zs[0], null);
            ECFieldElement W2 = calculateJacobianModifiedW;
            eCFieldElementArr[1] = calculateJacobianModifiedW;
            return W2;
        }

        /* access modifiers changed from: protected */
        public Fp twiceJacobianModified(boolean calculateW) {
            ECFieldElement X1 = this.x;
            ECFieldElement Y1 = this.y;
            ECFieldElement Z1 = this.zs[0];
            ECFieldElement W1 = getJacobianModifiedW();
            ECFieldElement M = three(X1.square()).add(W1);
            ECFieldElement _2Y1 = two(Y1);
            ECFieldElement _2Y1Squared = _2Y1.multiply(Y1);
            ECFieldElement S = two(X1.multiply(_2Y1Squared));
            ECFieldElement X3 = M.square().subtract(two(S));
            ECFieldElement _8T = two(_2Y1Squared.square());
            ECFieldElement eCFieldElement = _8T;
            Fp fp = new Fp(getCurve(), X3, M.multiply(S.subtract(X3)).subtract(_8T), new ECFieldElement[]{Z1.isOne() ? _2Y1 : _2Y1.multiply(Z1), calculateW ? two(_8T.multiply(W1)) : null}, this.withCompression);
            return fp;
        }
    }

    public abstract ECPoint add(ECPoint eCPoint);

    /* access modifiers changed from: protected */
    public abstract ECPoint detach();

    /* access modifiers changed from: protected */
    public abstract boolean getCompressionYTilde();

    public abstract ECPoint negate();

    /* access modifiers changed from: protected */
    public abstract boolean satisfiesCurveEquation();

    public abstract ECPoint subtract(ECPoint eCPoint);

    public abstract ECPoint twice();

    protected static ECFieldElement[] getInitialZCoords(ECCurve curve2) {
        int coord = curve2 == null ? 0 : curve2.getCoordinateSystem();
        if (coord == 0 || coord == 5) {
            return EMPTY_ZS;
        }
        ECFieldElement one = curve2.fromBigInteger(ECConstants.ONE);
        if (coord != 6) {
            switch (coord) {
                case 1:
                case 2:
                    break;
                case 3:
                    return new ECFieldElement[]{one, one, one};
                case 4:
                    return new ECFieldElement[]{one, curve2.getA()};
                default:
                    throw new IllegalArgumentException("unknown coordinate system");
            }
        }
        return new ECFieldElement[]{one};
    }

    protected ECPoint(ECCurve curve2, ECFieldElement x2, ECFieldElement y2) {
        this(curve2, x2, y2, getInitialZCoords(curve2));
    }

    protected ECPoint(ECCurve curve2, ECFieldElement x2, ECFieldElement y2, ECFieldElement[] zs2) {
        this.preCompTable = null;
        this.curve = curve2;
        this.x = x2;
        this.y = y2;
        this.zs = zs2;
    }

    /* access modifiers changed from: protected */
    public boolean satisfiesCofactor() {
        BigInteger h = this.curve.getCofactor();
        return h == null || h.equals(ECConstants.ONE) || !ECAlgorithms.referenceMultiply(this, h).isInfinity();
    }

    public final ECPoint getDetachedPoint() {
        return normalize().detach();
    }

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

    public ECFieldElement getX() {
        return normalize().getXCoord();
    }

    public ECFieldElement getY() {
        return normalize().getYCoord();
    }

    public ECFieldElement getAffineXCoord() {
        checkNormalized();
        return getXCoord();
    }

    public ECFieldElement getAffineYCoord() {
        checkNormalized();
        return getYCoord();
    }

    public ECFieldElement getXCoord() {
        return this.x;
    }

    public ECFieldElement getYCoord() {
        return this.y;
    }

    public ECFieldElement getZCoord(int index) {
        if (index < 0 || index >= this.zs.length) {
            return null;
        }
        return this.zs[index];
    }

    public ECFieldElement[] getZCoords() {
        int zsLen = this.zs.length;
        if (zsLen == 0) {
            return EMPTY_ZS;
        }
        ECFieldElement[] copy = new ECFieldElement[zsLen];
        System.arraycopy(this.zs, 0, copy, 0, zsLen);
        return copy;
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

    /* access modifiers changed from: protected */
    public void checkNormalized() {
        if (!isNormalized()) {
            throw new IllegalStateException("point not in normal form");
        }
    }

    public boolean isNormalized() {
        int coord = getCurveCoordinateSystem();
        if (coord == 0 || coord == 5 || isInfinity() || this.zs[0].isOne()) {
            return true;
        }
        return false;
    }

    public ECPoint normalize() {
        if (isInfinity()) {
            return this;
        }
        int curveCoordinateSystem = getCurveCoordinateSystem();
        if (curveCoordinateSystem == 0 || curveCoordinateSystem == 5) {
            return this;
        }
        ECFieldElement Z1 = getZCoord(0);
        if (Z1.isOne()) {
            return this;
        }
        return normalize(Z1.invert());
    }

    /* access modifiers changed from: package-private */
    public ECPoint normalize(ECFieldElement zInv) {
        int curveCoordinateSystem = getCurveCoordinateSystem();
        if (curveCoordinateSystem != 6) {
            switch (curveCoordinateSystem) {
                case 1:
                    break;
                case 2:
                case 3:
                case 4:
                    ECFieldElement zInv2 = zInv.square();
                    return createScaledPoint(zInv2, zInv2.multiply(zInv));
                default:
                    throw new IllegalStateException("not a projective coordinate system");
            }
        }
        return createScaledPoint(zInv, zInv);
    }

    /* access modifiers changed from: protected */
    public ECPoint createScaledPoint(ECFieldElement sx, ECFieldElement sy) {
        return getCurve().createRawPoint(getRawXCoord().multiply(sx), getRawYCoord().multiply(sy), this.withCompression);
    }

    public boolean isInfinity() {
        return this.x == null || this.y == null || (this.zs.length > 0 && this.zs[0].isZero());
    }

    public boolean isCompressed() {
        return this.withCompression;
    }

    public boolean isValid() {
        if (!isInfinity() && getCurve() != null && (!satisfiesCurveEquation() || !satisfiesCofactor())) {
            return false;
        }
        return true;
    }

    public ECPoint scaleX(ECFieldElement scale) {
        if (isInfinity()) {
            return this;
        }
        return getCurve().createRawPoint(getRawXCoord().multiply(scale), getRawYCoord(), getRawZCoords(), this.withCompression);
    }

    public ECPoint scaleY(ECFieldElement scale) {
        if (isInfinity()) {
            return this;
        }
        return getCurve().createRawPoint(getRawXCoord(), getRawYCoord().multiply(scale), getRawZCoords(), this.withCompression);
    }

    public boolean equals(ECPoint other) {
        boolean z = false;
        if (other == null) {
            return false;
        }
        ECCurve c1 = getCurve();
        ECCurve c2 = other.getCurve();
        boolean n1 = c1 == null;
        boolean n2 = c2 == null;
        boolean i1 = isInfinity();
        boolean i2 = other.isInfinity();
        if (i1 || i2) {
            if (i1 && i2 && (n1 || n2 || c1.equals(c2))) {
                z = true;
            }
            return z;
        }
        ECPoint p1 = this;
        ECPoint p2 = other;
        if (!n1 || !n2) {
            if (n1) {
                p2 = p2.normalize();
            } else if (n2) {
                p1 = p1.normalize();
            } else if (!c1.equals(c2)) {
                return false;
            } else {
                ECPoint[] points = {this, c1.importPoint(p2)};
                c1.normalizeAll(points);
                p1 = points[0];
                p2 = points[1];
            }
        }
        if (p1.getXCoord().equals(p2.getXCoord()) && p1.getYCoord().equals(p2.getYCoord())) {
            z = true;
        }
        return z;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ECPoint)) {
            return false;
        }
        return equals((ECPoint) other);
    }

    public int hashCode() {
        ECCurve c = getCurve();
        int hc = c == null ? 0 : ~c.hashCode();
        if (isInfinity()) {
            return hc;
        }
        ECPoint p = normalize();
        return (hc ^ (p.getXCoord().hashCode() * 17)) ^ (p.getYCoord().hashCode() * 257);
    }

    public String toString() {
        if (isInfinity()) {
            return "INF";
        }
        StringBuffer sb = new StringBuffer();
        sb.append('(');
        sb.append(getRawXCoord());
        sb.append(',');
        sb.append(getRawYCoord());
        for (ECFieldElement append : this.zs) {
            sb.append(',');
            sb.append(append);
        }
        sb.append(')');
        return sb.toString();
    }

    public byte[] getEncoded() {
        return getEncoded(this.withCompression);
    }

    public byte[] getEncoded(boolean compressed) {
        if (isInfinity()) {
            return new byte[1];
        }
        ECPoint normed = normalize();
        byte[] X = normed.getXCoord().getEncoded();
        if (compressed) {
            byte[] PO = new byte[(X.length + 1)];
            PO[0] = (byte) (normed.getCompressionYTilde() ? 3 : 2);
            System.arraycopy(X, 0, PO, 1, X.length);
            return PO;
        }
        byte[] Y = normed.getYCoord().getEncoded();
        byte[] PO2 = new byte[(X.length + Y.length + 1)];
        PO2[0] = 4;
        System.arraycopy(X, 0, PO2, 1, X.length);
        System.arraycopy(Y, 0, PO2, X.length + 1, Y.length);
        return PO2;
    }

    public ECPoint timesPow2(int e) {
        if (e >= 0) {
            int e2 = e;
            ECPoint p = this;
            while (true) {
                e2--;
                if (e2 < 0) {
                    return p;
                }
                p = p.twice();
            }
        } else {
            throw new IllegalArgumentException("'e' cannot be negative");
        }
    }

    public ECPoint twicePlus(ECPoint b) {
        return twice().add(b);
    }

    public ECPoint threeTimes() {
        return twicePlus(this);
    }

    public ECPoint multiply(BigInteger k) {
        return getCurve().getMultiplier().multiply(this, k);
    }
}
