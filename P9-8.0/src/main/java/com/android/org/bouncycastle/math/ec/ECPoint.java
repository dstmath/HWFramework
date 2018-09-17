package com.android.org.bouncycastle.math.ec;

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

        protected boolean satisfiesCurveEquation() {
            ECCurve curve = getCurve();
            ECFieldElement X = this.x;
            ECFieldElement A = curve.getA();
            ECFieldElement B = curve.getB();
            int coord = curve.getCoordinateSystem();
            ECFieldElement Z;
            ECFieldElement lhs;
            if (coord == 6) {
                Z = this.zs[0];
                boolean ZIsOne = Z.isOne();
                ECFieldElement rhs;
                if (X.isZero()) {
                    lhs = this.y.square();
                    rhs = B;
                    if (!ZIsOne) {
                        rhs = B.multiply(Z.square());
                    }
                    return lhs.equals(rhs);
                }
                ECFieldElement L = this.y;
                ECFieldElement X2 = X.square();
                if (ZIsOne) {
                    lhs = L.square().add(L).add(A);
                    rhs = X2.square().add(B);
                } else {
                    ECFieldElement Z2 = Z.square();
                    ECFieldElement Z4 = Z2.square();
                    lhs = L.add(Z).multiplyPlusProduct(L, A, Z2);
                    rhs = X2.squarePlusProduct(B, Z4);
                }
                return lhs.multiply(X2).equals(rhs);
            }
            ECFieldElement Y = this.y;
            lhs = Y.add(X).multiply(Y);
            switch (coord) {
                case 0:
                    break;
                case 1:
                    Z = this.zs[0];
                    if (!Z.isOne()) {
                        ECFieldElement Z3 = Z.multiply(Z.square());
                        lhs = lhs.multiply(Z);
                        A = A.multiply(Z);
                        B = B.multiply(Z3);
                        break;
                    }
                    break;
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
            return lhs.equals(X.add(A).multiply(X.square()).add(B));
        }

        public ECPoint scaleX(ECFieldElement scale) {
            if (isInfinity()) {
                return this;
            }
            ECFieldElement X;
            ECFieldElement L;
            switch (getCurveCoordinateSystem()) {
                case 5:
                    X = getRawXCoord();
                    L = getRawYCoord();
                    return getCurve().createRawPoint(X, L.add(X).divide(scale).add(X.multiply(scale)), getRawZCoords(), this.withCompression);
                case 6:
                    X = getRawXCoord();
                    L = getRawYCoord();
                    ECFieldElement Z = getRawZCoords()[0];
                    ECFieldElement X2 = X.multiply(scale.square());
                    ECFieldElement L2 = L.add(X).add(X2);
                    ECFieldElement Z2 = Z.multiply(scale);
                    return getCurve().createRawPoint(X2, L2, new ECFieldElement[]{Z2}, this.withCompression);
                default:
                    return super.scaleX(scale);
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
                    return super.scaleY(scale);
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

        protected boolean getCompressionYTilde() {
            return getAffineYCoord().testBitZero();
        }

        protected boolean satisfiesCurveEquation() {
            ECFieldElement X = this.x;
            ECFieldElement Y = this.y;
            ECFieldElement A = this.curve.getA();
            ECFieldElement B = this.curve.getB();
            ECFieldElement lhs = Y.square();
            ECFieldElement Z;
            ECFieldElement Z2;
            switch (getCurveCoordinateSystem()) {
                case 0:
                    break;
                case 1:
                    Z = this.zs[0];
                    if (!Z.isOne()) {
                        Z2 = Z.square();
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
                    Z = this.zs[0];
                    if (!Z.isOne()) {
                        Z2 = Z.square();
                        ECFieldElement Z4 = Z2.square();
                        ECFieldElement Z6 = Z2.multiply(Z4);
                        A = A.multiply(Z4);
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
            if (x != null) {
                com.android.org.bouncycastle.math.ec.ECFieldElement.F2m.checkFieldElements(this.x, this.y);
                if (curve != null) {
                    com.android.org.bouncycastle.math.ec.ECFieldElement.F2m.checkFieldElements(this.x, this.curve.getA());
                }
            }
            this.withCompression = withCompression;
        }

        F2m(ECCurve curve, ECFieldElement x, ECFieldElement y, ECFieldElement[] zs, boolean withCompression) {
            super(curve, x, y, zs);
            this.withCompression = withCompression;
        }

        protected ECPoint detach() {
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

        protected boolean getCompressionYTilde() {
            boolean z = false;
            ECFieldElement X = getRawXCoord();
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
            if (isInfinity()) {
                return b;
            }
            if (b.isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            int coord = curve.getCoordinateSystem();
            ECFieldElement X1 = this.x;
            ECFieldElement X2 = b.x;
            ECFieldElement Y1;
            ECFieldElement Y2;
            ECFieldElement L;
            ECFieldElement X3;
            ECFieldElement Z1;
            ECFieldElement Z2;
            boolean Z2IsOne;
            ECFieldElement A;
            switch (coord) {
                case 0:
                    Y1 = this.y;
                    Y2 = b.y;
                    ECFieldElement dx = X1.add(X2);
                    ECFieldElement dy = Y1.add(Y2);
                    if (!dx.isZero()) {
                        L = dy.divide(dx);
                        X3 = L.square().add(L).add(dx).add(curve.getA());
                        return new F2m(curve, X3, L.multiply(X1.add(X3)).add(X3).add(Y1), this.withCompression);
                    } else if (dy.isZero()) {
                        return twice();
                    } else {
                        return curve.getInfinity();
                    }
                case 1:
                    Y1 = this.y;
                    Z1 = this.zs[0];
                    Y2 = b.y;
                    Z2 = b.zs[0];
                    Z2IsOne = Z2.isOne();
                    ECFieldElement U = Z1.multiply(Y2).add(Z2IsOne ? Y1 : Y1.multiply(Z2));
                    ECFieldElement V = Z1.multiply(X2).add(Z2IsOne ? X1 : X1.multiply(Z2));
                    if (!V.isZero()) {
                        ECFieldElement VSq = V.square();
                        ECFieldElement VCu = VSq.multiply(V);
                        ECFieldElement W = Z2IsOne ? Z1 : Z1.multiply(Z2);
                        ECFieldElement uv = U.add(V);
                        A = uv.multiplyPlusProduct(U, VSq, curve.getA()).multiply(W).add(VCu);
                        return new F2m(curve, V.multiply(A), U.multiplyPlusProduct(X1, V, Y1).multiplyPlusProduct(Z2IsOne ? VSq : VSq.multiply(Z2), uv, A), new ECFieldElement[]{VCu.multiply(W)}, this.withCompression);
                    } else if (U.isZero()) {
                        return twice();
                    } else {
                        return curve.getInfinity();
                    }
                case 6:
                    if (!X1.isZero()) {
                        ECFieldElement L1 = this.y;
                        Z1 = this.zs[0];
                        ECFieldElement L2 = b.y;
                        Z2 = b.zs[0];
                        boolean Z1IsOne = Z1.isOne();
                        ECFieldElement U2 = X2;
                        ECFieldElement S2 = L2;
                        if (!Z1IsOne) {
                            U2 = X2.multiply(Z1);
                            S2 = L2.multiply(Z1);
                        }
                        Z2IsOne = Z2.isOne();
                        ECFieldElement U1 = X1;
                        ECFieldElement S1 = L1;
                        if (!Z2IsOne) {
                            U1 = X1.multiply(Z2);
                            S1 = L1.multiply(Z2);
                        }
                        A = S1.add(S2);
                        ECFieldElement B = U1.add(U2);
                        if (!B.isZero()) {
                            ECFieldElement L3;
                            ECFieldElement Z3;
                            if (X2.isZero()) {
                                ECPoint p = normalize();
                                X1 = p.getXCoord();
                                Y1 = p.getYCoord();
                                Y2 = L2;
                                L = Y1.add(L2).divide(X1);
                                X3 = L.square().add(L).add(X1).add(curve.getA());
                                if (X3.isZero()) {
                                    return new F2m(curve, X3, curve.getB().sqrt(), this.withCompression);
                                }
                                L3 = L.multiply(X1.add(X3)).add(X3).add(Y1).divide(X3).add(X3);
                                Z3 = curve.fromBigInteger(ECConstants.ONE);
                            } else {
                                B = B.square();
                                ECFieldElement AU1 = A.multiply(U1);
                                ECFieldElement AU2 = A.multiply(U2);
                                X3 = AU1.multiply(AU2);
                                if (X3.isZero()) {
                                    return new F2m(curve, X3, curve.getB().sqrt(), this.withCompression);
                                }
                                ECFieldElement ABZ2 = A.multiply(B);
                                if (!Z2IsOne) {
                                    ABZ2 = ABZ2.multiply(Z2);
                                }
                                L3 = AU2.add(B).squarePlusProduct(ABZ2, L1.add(Z1));
                                Z3 = ABZ2;
                                if (!Z1IsOne) {
                                    Z3 = Z3.multiply(Z1);
                                }
                            }
                            return new F2m(curve, X3, L3, new ECFieldElement[]{Z3}, this.withCompression);
                        } else if (A.isZero()) {
                            return twice();
                        } else {
                            return curve.getInfinity();
                        }
                    } else if (X2.isZero()) {
                        return curve.getInfinity();
                    } else {
                        return b.add(this);
                    }
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
        }

        public ECPoint twice() {
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            ECFieldElement X1 = this.x;
            if (X1.isZero()) {
                return curve.getInfinity();
            }
            ECFieldElement L1;
            ECFieldElement X3;
            ECFieldElement Z1;
            boolean Z1IsOne;
            switch (curve.getCoordinateSystem()) {
                case 0:
                    L1 = this.y.divide(X1).add(X1);
                    X3 = L1.square().add(L1).add(curve.getA());
                    return new F2m(curve, X3, X1.squarePlusProduct(X3, L1.addOne()), this.withCompression);
                case 1:
                    ECFieldElement Y1 = this.y;
                    Z1 = this.zs[0];
                    Z1IsOne = Z1.isOne();
                    ECFieldElement X1Z1 = Z1IsOne ? X1 : X1.multiply(Z1);
                    ECFieldElement Y1Z1 = Z1IsOne ? Y1 : Y1.multiply(Z1);
                    ECFieldElement X1Sq = X1.square();
                    ECFieldElement S = X1Sq.add(Y1Z1);
                    ECFieldElement V = X1Z1;
                    ECFieldElement vSquared = X1Z1.square();
                    ECFieldElement sv = S.add(V);
                    ECFieldElement h = sv.multiplyPlusProduct(S, vSquared, curve.getA());
                    return new F2m(curve, V.multiply(h), X1Sq.square().multiplyPlusProduct(V, h, sv), new ECFieldElement[]{V.multiply(vSquared)}, this.withCompression);
                case 6:
                    L1 = this.y;
                    Z1 = this.zs[0];
                    Z1IsOne = Z1.isOne();
                    ECFieldElement L1Z1 = Z1IsOne ? L1 : L1.multiply(Z1);
                    ECFieldElement Z1Sq = Z1IsOne ? Z1 : Z1.square();
                    ECFieldElement a = curve.getA();
                    ECFieldElement aZ1Sq = Z1IsOne ? a : a.multiply(Z1Sq);
                    ECFieldElement T = L1.square().add(L1Z1).add(aZ1Sq);
                    if (T.isZero()) {
                        return new F2m(curve, T, curve.getB().sqrt(), this.withCompression);
                    }
                    ECFieldElement L3;
                    X3 = T.square();
                    ECFieldElement Z3 = Z1IsOne ? T : T.multiply(Z1Sq);
                    ECFieldElement b = curve.getB();
                    if (b.bitLength() < (curve.getFieldSize() >> 1)) {
                        ECFieldElement t2;
                        ECFieldElement t1 = L1.add(X1).square();
                        if (b.isOne()) {
                            t2 = aZ1Sq.add(Z1Sq).square();
                        } else {
                            t2 = aZ1Sq.squarePlusProduct(b, Z1Sq.square());
                        }
                        L3 = t1.add(T).add(Z1Sq).multiply(t1).add(t2).add(X3);
                        if (a.isZero()) {
                            L3 = L3.add(Z3);
                        } else if (!a.isOne()) {
                            L3 = L3.add(a.addOne().multiply(Z3));
                        }
                    } else {
                        L3 = (Z1IsOne ? X1 : X1.multiply(Z1)).squarePlusProduct(T, L1Z1).add(X3).add(Z3);
                    }
                    return new F2m(curve, X3, L3, new ECFieldElement[]{Z3}, this.withCompression);
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
        }

        public ECPoint twicePlus(ECPoint b) {
            if (isInfinity()) {
                return b;
            }
            if (b.isInfinity()) {
                return twice();
            }
            ECCurve curve = getCurve();
            ECFieldElement X1 = this.x;
            if (X1.isZero()) {
                return b;
            }
            switch (curve.getCoordinateSystem()) {
                case 6:
                    ECFieldElement X2 = b.x;
                    ECFieldElement Z2 = b.zs[0];
                    if (X2.isZero() || (Z2.isOne() ^ 1) != 0) {
                        return twice().add(b);
                    }
                    ECFieldElement L1 = this.y;
                    ECFieldElement Z1 = this.zs[0];
                    ECFieldElement L2 = b.y;
                    ECFieldElement X1Sq = X1.square();
                    ECFieldElement L1Sq = L1.square();
                    ECFieldElement Z1Sq = Z1.square();
                    ECFieldElement T = curve.getA().multiply(Z1Sq).add(L1Sq).add(L1.multiply(Z1));
                    ECFieldElement L2plus1 = L2.addOne();
                    ECFieldElement A = curve.getA().add(L2plus1).multiply(Z1Sq).add(L1Sq).multiplyPlusProduct(T, X1Sq, Z1Sq);
                    ECFieldElement X2Z1Sq = X2.multiply(Z1Sq);
                    ECFieldElement B = X2Z1Sq.add(T).square();
                    if (B.isZero()) {
                        if (A.isZero()) {
                            return b.twice();
                        }
                        return curve.getInfinity();
                    } else if (A.isZero()) {
                        return new F2m(curve, A, curve.getB().sqrt(), this.withCompression);
                    } else {
                        return new F2m(curve, A.square().multiply(X2Z1Sq), A.add(B).square().multiplyPlusProduct(T, L2plus1, A.multiply(B).multiply(Z1Sq)), new ECFieldElement[]{Z3}, this.withCompression);
                    }
                default:
                    return twice().add(b);
            }
        }

        public ECPoint negate() {
            if (isInfinity()) {
                return this;
            }
            ECFieldElement X = this.x;
            if (X.isZero()) {
                return this;
            }
            ECFieldElement Z;
            switch (getCurveCoordinateSystem()) {
                case 0:
                    return new F2m(this.curve, X, this.y.add(X), this.withCompression);
                case 1:
                    ECFieldElement Y = this.y;
                    Z = this.zs[0];
                    return new F2m(this.curve, X, Y.add(X), new ECFieldElement[]{Z}, this.withCompression);
                case 5:
                    return new F2m(this.curve, X, this.y.addOne(), this.withCompression);
                case 6:
                    ECFieldElement L = this.y;
                    Z = this.zs[0];
                    return new F2m(this.curve, X, L.add(Z), new ECFieldElement[]{Z}, this.withCompression);
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

        Fp(ECCurve curve, ECFieldElement x, ECFieldElement y, ECFieldElement[] zs, boolean withCompression) {
            super(curve, x, y, zs);
            this.withCompression = withCompression;
        }

        protected ECPoint detach() {
            return new Fp(null, getAffineXCoord(), getAffineYCoord());
        }

        public ECFieldElement getZCoord(int index) {
            if (index == 1 && 4 == getCurveCoordinateSystem()) {
                return getJacobianModifiedW();
            }
            return super.getZCoord(index);
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
            ECCurve curve = getCurve();
            int coord = curve.getCoordinateSystem();
            ECFieldElement X1 = this.x;
            ECFieldElement Y1 = this.y;
            ECFieldElement X2 = b.x;
            ECFieldElement Y2 = b.y;
            ECFieldElement dx;
            ECFieldElement dy;
            ECFieldElement X3;
            ECFieldElement Z1;
            ECFieldElement Z2;
            boolean Z1IsOne;
            boolean Z2IsOne;
            switch (coord) {
                case 0:
                    dx = X2.subtract(X1);
                    dy = Y2.subtract(Y1);
                    if (!dx.isZero()) {
                        ECFieldElement gamma = dy.divide(dx);
                        X3 = gamma.square().subtract(X1).subtract(X2);
                        return new Fp(curve, X3, gamma.multiply(X1.subtract(X3)).subtract(Y1), this.withCompression);
                    } else if (dy.isZero()) {
                        return twice();
                    } else {
                        return curve.getInfinity();
                    }
                case 1:
                    Z1 = this.zs[0];
                    Z2 = b.zs[0];
                    Z1IsOne = Z1.isOne();
                    Z2IsOne = Z2.isOne();
                    ECFieldElement u1 = Z1IsOne ? Y2 : Y2.multiply(Z1);
                    ECFieldElement u2 = Z2IsOne ? Y1 : Y1.multiply(Z2);
                    ECFieldElement u = u1.subtract(u2);
                    ECFieldElement v1 = Z1IsOne ? X2 : X2.multiply(Z1);
                    ECFieldElement v2 = Z2IsOne ? X1 : X1.multiply(Z2);
                    ECFieldElement v = v1.subtract(v2);
                    if (!v.isZero()) {
                        ECFieldElement w = Z1IsOne ? Z2 : Z2IsOne ? Z1 : Z1.multiply(Z2);
                        ECFieldElement vSquared = v.square();
                        ECFieldElement vCubed = vSquared.multiply(v);
                        ECFieldElement vSquaredV2 = vSquared.multiply(v2);
                        ECFieldElement A = u.square().multiply(w).subtract(vCubed).subtract(two(vSquaredV2));
                        return new Fp(curve, v.multiply(A), vSquaredV2.subtract(A).multiplyMinusProduct(u, u2, vCubed), new ECFieldElement[]{vCubed.multiply(w)}, this.withCompression);
                    } else if (u.isZero()) {
                        return twice();
                    } else {
                        return curve.getInfinity();
                    }
                case 2:
                case 4:
                    ECFieldElement Y3;
                    ECFieldElement Z3;
                    ECFieldElement[] zs;
                    Z1 = this.zs[0];
                    Z2 = b.zs[0];
                    Z1IsOne = Z1.isOne();
                    ECFieldElement Z3Squared = null;
                    if (Z1IsOne || !Z1.equals(Z2)) {
                        ECFieldElement U2;
                        ECFieldElement S2;
                        ECFieldElement U1;
                        ECFieldElement S1;
                        ECFieldElement Z1Squared;
                        if (Z1IsOne) {
                            Z1Squared = Z1;
                            U2 = X2;
                            S2 = Y2;
                        } else {
                            Z1Squared = Z1.square();
                            U2 = Z1Squared.multiply(X2);
                            S2 = Z1Squared.multiply(Z1).multiply(Y2);
                        }
                        Z2IsOne = Z2.isOne();
                        ECFieldElement Z2Squared;
                        if (Z2IsOne) {
                            Z2Squared = Z2;
                            U1 = X1;
                            S1 = Y1;
                        } else {
                            Z2Squared = Z2.square();
                            U1 = Z2Squared.multiply(X1);
                            S1 = Z2Squared.multiply(Z2).multiply(Y1);
                        }
                        ECFieldElement H = U1.subtract(U2);
                        ECFieldElement R = S1.subtract(S2);
                        if (!H.isZero()) {
                            ECFieldElement HSquared = H.square();
                            ECFieldElement G = HSquared.multiply(H);
                            ECFieldElement V = HSquared.multiply(U1);
                            X3 = R.square().add(G).subtract(two(V));
                            Y3 = V.subtract(X3).multiplyMinusProduct(R, G, S1);
                            Z3 = H;
                            if (!Z1IsOne) {
                                Z3 = H.multiply(Z1);
                            }
                            if (!Z2IsOne) {
                                Z3 = Z3.multiply(Z2);
                            }
                            if (Z3 == H) {
                                Z3Squared = HSquared;
                            }
                        } else if (R.isZero()) {
                            return twice();
                        } else {
                            return curve.getInfinity();
                        }
                    }
                    dx = X1.subtract(X2);
                    dy = Y1.subtract(Y2);
                    if (!dx.isZero()) {
                        ECFieldElement C = dx.square();
                        ECFieldElement W1 = X1.multiply(C);
                        ECFieldElement W2 = X2.multiply(C);
                        ECFieldElement A1 = W1.subtract(W2).multiply(Y1);
                        X3 = dy.square().subtract(W1).subtract(W2);
                        Y3 = W1.subtract(X3).multiply(dy).subtract(A1);
                        Z3 = dx;
                        Z3 = dx.multiply(Z1);
                    } else if (dy.isZero()) {
                        return twice();
                    } else {
                        return curve.getInfinity();
                    }
                    if (coord == 4) {
                        ECFieldElement W3 = calculateJacobianModifiedW(Z3, Z3Squared);
                        zs = new ECFieldElement[]{Z3, W3};
                    } else {
                        zs = new ECFieldElement[]{Z3};
                    }
                    return new Fp(curve, X3, Y3, zs, this.withCompression);
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
        }

        public ECPoint twice() {
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
            ECFieldElement X3;
            ECFieldElement Z1;
            boolean Z1IsOne;
            ECFieldElement Y3;
            switch (coord) {
                case 0:
                    ECFieldElement gamma = three(X1.square()).add(getCurve().getA()).divide(two(Y1));
                    X3 = gamma.square().subtract(two(X1));
                    return new Fp(curve, X3, gamma.multiply(X1.subtract(X3)).subtract(Y1), this.withCompression);
                case 1:
                    Z1 = this.zs[0];
                    Z1IsOne = Z1.isOne();
                    ECFieldElement w = curve.getA();
                    if (!(w.isZero() || (Z1IsOne ^ 1) == 0)) {
                        w = w.multiply(Z1.square());
                    }
                    w = w.add(three(X1.square()));
                    ECFieldElement s = Z1IsOne ? Y1 : Y1.multiply(Z1);
                    ECFieldElement t = Z1IsOne ? Y1.square() : s.multiply(Y1);
                    ECFieldElement _4B = four(X1.multiply(t));
                    ECFieldElement h = w.square().subtract(two(_4B));
                    ECFieldElement _2s = two(s);
                    X3 = h.multiply(_2s);
                    ECFieldElement _2t = two(t);
                    Y3 = _4B.subtract(h).multiply(w).subtract(two(_2t.square()));
                    ECFieldElement _4sSquared = Z1IsOne ? two(_2t) : _2s.square();
                    return new Fp(curve, X3, Y3, new ECFieldElement[]{two(_4sSquared).multiply(s)}, this.withCompression);
                case 2:
                    ECFieldElement M;
                    ECFieldElement S;
                    Z1 = this.zs[0];
                    Z1IsOne = Z1.isOne();
                    ECFieldElement Y1Squared = Y1.square();
                    ECFieldElement T = Y1Squared.square();
                    ECFieldElement a4 = curve.getA();
                    ECFieldElement a4Neg = a4.negate();
                    if (a4Neg.toBigInteger().equals(BigInteger.valueOf(3))) {
                        ECFieldElement Z1Squared = Z1IsOne ? Z1 : Z1.square();
                        M = three(X1.add(Z1Squared).multiply(X1.subtract(Z1Squared)));
                        S = four(Y1Squared.multiply(X1));
                    } else {
                        M = three(X1.square());
                        if (Z1IsOne) {
                            M = M.add(a4);
                        } else if (!a4.isZero()) {
                            ECFieldElement Z1Pow4 = Z1.square().square();
                            if (a4Neg.bitLength() < a4.bitLength()) {
                                M = M.subtract(Z1Pow4.multiply(a4Neg));
                            } else {
                                M = M.add(Z1Pow4.multiply(a4));
                            }
                        }
                        S = four(X1.multiply(Y1Squared));
                    }
                    X3 = M.square().subtract(two(S));
                    Y3 = S.subtract(X3).multiply(M).subtract(eight(T));
                    ECFieldElement Z3 = two(Y1);
                    if (!Z1IsOne) {
                        Z3 = Z3.multiply(Z1);
                    }
                    return new Fp(curve, X3, Y3, new ECFieldElement[]{Z3}, this.withCompression);
                case 4:
                    return twiceJacobianModified(true);
                default:
                    throw new IllegalStateException("unsupported coordinate system");
            }
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
            ECFieldElement Y1 = this.y;
            if (Y1.isZero()) {
                return b;
            }
            ECCurve curve = getCurve();
            switch (curve.getCoordinateSystem()) {
                case 0:
                    ECFieldElement X1 = this.x;
                    ECFieldElement X2 = b.x;
                    ECFieldElement Y2 = b.y;
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
                        ECFieldElement X4 = L2.subtract(L1).multiply(L1.add(L2)).add(X2);
                        return new Fp(curve, X4, X1.subtract(X4).multiply(L2).subtract(Y1), this.withCompression);
                    } else if (dy.isZero()) {
                        return threeTimes();
                    } else {
                        return this;
                    }
                case 4:
                    return twiceJacobianModified(false).add(b);
                default:
                    return twice().add(b);
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
            switch (curve.getCoordinateSystem()) {
                case 0:
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
                    return new Fp(curve, X4, X1.subtract(X4).multiply(L2).subtract(Y1), this.withCompression);
                case 4:
                    return twiceJacobianModified(false).add(this);
                default:
                    return twice().add(this);
            }
        }

        public ECPoint timesPow2(int e) {
            if (e < 0) {
                throw new IllegalArgumentException("'e' cannot be negative");
            } else if (e == 0 || isInfinity()) {
                return this;
            } else {
                if (e == 1) {
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
                        case 4:
                            W1 = getJacobianModifiedW();
                            break;
                        default:
                            throw new IllegalStateException("unsupported coordinate system");
                    }
                }
                for (int i = 0; i < e; i++) {
                    if (Y1.isZero()) {
                        return curve.getInfinity();
                    }
                    ECFieldElement M = three(X1.square());
                    ECFieldElement _2Y1 = two(Y1);
                    ECFieldElement _2Y1Squared = _2Y1.multiply(Y1);
                    ECFieldElement S = two(X1.multiply(_2Y1Squared));
                    ECFieldElement _8T = two(_2Y1Squared.square());
                    if (!W1.isZero()) {
                        M = M.add(W1);
                        W1 = two(_8T.multiply(W1));
                    }
                    X1 = M.square().subtract(two(S));
                    Y1 = M.multiply(S.subtract(X1)).subtract(_8T);
                    if (Z1.isOne()) {
                        Z1 = _2Y1;
                    } else {
                        Z1 = _2Y1.multiply(Z1);
                    }
                }
                switch (coord) {
                    case 0:
                        ECFieldElement zInv = Z1.invert();
                        ECFieldElement zInv2 = zInv.square();
                        return new Fp(curve, X1.multiply(zInv2), Y1.multiply(zInv2.multiply(zInv)), this.withCompression);
                    case 1:
                        return new Fp(curve, X1.multiply(Z1), Y1, new ECFieldElement[]{Z1.multiply(Z1.square())}, this.withCompression);
                    case 2:
                        return new Fp(curve, X1, Y1, new ECFieldElement[]{Z1}, this.withCompression);
                    case 4:
                        return new Fp(curve, X1, Y1, new ECFieldElement[]{Z1, W1}, this.withCompression);
                    default:
                        throw new IllegalStateException("unsupported coordinate system");
                }
            }
        }

        protected ECFieldElement two(ECFieldElement x) {
            return x.add(x);
        }

        protected ECFieldElement three(ECFieldElement x) {
            return two(x).add(x);
        }

        protected ECFieldElement four(ECFieldElement x) {
            return two(two(x));
        }

        protected ECFieldElement eight(ECFieldElement x) {
            return four(two(x));
        }

        protected ECFieldElement doubleProductFromSquares(ECFieldElement a, ECFieldElement b, ECFieldElement aSquared, ECFieldElement bSquared) {
            return a.add(b).square().subtract(aSquared).subtract(bSquared);
        }

        public ECPoint negate() {
            if (isInfinity()) {
                return this;
            }
            ECCurve curve = getCurve();
            if (curve.getCoordinateSystem() != 0) {
                return new Fp(curve, this.x, this.y.negate(), this.zs, this.withCompression);
            }
            return new Fp(curve, this.x, this.y.negate(), this.withCompression);
        }

        protected ECFieldElement calculateJacobianModifiedW(ECFieldElement Z, ECFieldElement ZSquared) {
            ECFieldElement a4 = getCurve().getA();
            if (a4.isZero() || Z.isOne()) {
                return a4;
            }
            if (ZSquared == null) {
                ZSquared = Z.square();
            }
            ECFieldElement W = ZSquared.square();
            ECFieldElement a4Neg = a4.negate();
            if (a4Neg.bitLength() < a4.bitLength()) {
                W = W.multiply(a4Neg).negate();
            } else {
                W = W.multiply(a4);
            }
            return W;
        }

        protected ECFieldElement getJacobianModifiedW() {
            ECFieldElement W = this.zs[1];
            if (W != null) {
                return W;
            }
            ECFieldElement[] eCFieldElementArr = this.zs;
            W = calculateJacobianModifiedW(this.zs[0], null);
            eCFieldElementArr[1] = W;
            return W;
        }

        protected Fp twiceJacobianModified(boolean calculateW) {
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
            ECFieldElement Y3 = M.multiply(S.subtract(X3)).subtract(_8T);
            ECFieldElement W3 = calculateW ? two(_8T.multiply(W1)) : null;
            ECFieldElement Z3 = Z1.isOne() ? _2Y1 : _2Y1.multiply(Z1);
            return new Fp(getCurve(), X3, Y3, new ECFieldElement[]{Z3, W3}, this.withCompression);
        }
    }

    public abstract ECPoint add(ECPoint eCPoint);

    protected abstract ECPoint detach();

    protected abstract boolean getCompressionYTilde();

    public abstract ECPoint negate();

    protected abstract boolean satisfiesCurveEquation();

    public abstract ECPoint subtract(ECPoint eCPoint);

    public abstract ECPoint twice();

    protected static ECFieldElement[] getInitialZCoords(ECCurve curve) {
        int coord = curve == null ? 0 : curve.getCoordinateSystem();
        switch (coord) {
            case 0:
            case 5:
                return EMPTY_ZS;
            default:
                ECFieldElement one = curve.fromBigInteger(ECConstants.ONE);
                switch (coord) {
                    case 1:
                    case 2:
                    case 6:
                        return new ECFieldElement[]{one};
                    case 3:
                        return new ECFieldElement[]{one, one, one};
                    case 4:
                        return new ECFieldElement[]{one, curve.getA()};
                    default:
                        throw new IllegalArgumentException("unknown coordinate system");
                }
        }
    }

    protected ECPoint(ECCurve curve, ECFieldElement x, ECFieldElement y) {
        this(curve, x, y, getInitialZCoords(curve));
    }

    protected ECPoint(ECCurve curve, ECFieldElement x, ECFieldElement y, ECFieldElement[] zs) {
        this.preCompTable = null;
        this.curve = curve;
        this.x = x;
        this.y = y;
        this.zs = zs;
    }

    protected boolean satisfiesCofactor() {
        BigInteger h = this.curve.getCofactor();
        return (h == null || h.equals(ECConstants.ONE)) ? true : ECAlgorithms.referenceMultiply(this, h).isInfinity() ^ 1;
    }

    public final ECPoint getDetachedPoint() {
        return normalize().detach();
    }

    public ECCurve getCurve() {
        return this.curve;
    }

    protected int getCurveCoordinateSystem() {
        return this.curve == null ? 0 : this.curve.getCoordinateSystem();
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
        return (index < 0 || index >= this.zs.length) ? null : this.zs[index];
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

    protected final ECFieldElement[] getRawZCoords() {
        return this.zs;
    }

    protected void checkNormalized() {
        if (!isNormalized()) {
            throw new IllegalStateException("point not in normal form");
        }
    }

    public boolean isNormalized() {
        int coord = getCurveCoordinateSystem();
        if (coord == 0 || coord == 5 || isInfinity()) {
            return true;
        }
        return this.zs[0].isOne();
    }

    public ECPoint normalize() {
        if (isInfinity()) {
            return this;
        }
        switch (getCurveCoordinateSystem()) {
            case 0:
            case 5:
                return this;
            default:
                ECFieldElement Z1 = getZCoord(0);
                if (Z1.isOne()) {
                    return this;
                }
                return normalize(Z1.invert());
        }
    }

    ECPoint normalize(ECFieldElement zInv) {
        switch (getCurveCoordinateSystem()) {
            case 1:
            case 6:
                return createScaledPoint(zInv, zInv);
            case 2:
            case 3:
            case 4:
                ECFieldElement zInv2 = zInv.square();
                return createScaledPoint(zInv2, zInv2.multiply(zInv));
            default:
                throw new IllegalStateException("not a projective coordinate system");
        }
    }

    protected ECPoint createScaledPoint(ECFieldElement sx, ECFieldElement sy) {
        return getCurve().createRawPoint(getRawXCoord().multiply(sx), getRawYCoord().multiply(sy), this.withCompression);
    }

    public boolean isInfinity() {
        if (this.x == null || this.y == null) {
            return true;
        }
        return this.zs.length > 0 ? this.zs[0].isZero() : false;
    }

    public boolean isCompressed() {
        return this.withCompression;
    }

    public boolean isValid() {
        return isInfinity() || getCurve() == null || (satisfiesCurveEquation() && satisfiesCofactor());
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
        boolean z = true;
        boolean z2 = false;
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
            if (!i1 || !i2) {
                z = false;
            } else if (!(n1 || n2)) {
                z = c1.equals(c2);
            }
            return z;
        }
        ECPoint p1 = this;
        ECPoint p2 = other;
        if (!(n1 && n2)) {
            if (n1) {
                p2 = other.normalize();
            } else if (n2) {
                p1 = normalize();
            } else if (!c1.equals(c2)) {
                return false;
            } else {
                ECPoint[] points = new ECPoint[]{this, c1.importPoint(other)};
                c1.normalizeAll(points);
                p1 = points[0];
                p2 = points[1];
            }
        }
        if (p1.getXCoord().equals(p2.getXCoord())) {
            z2 = p1.getYCoord().equals(p2.getYCoord());
        }
        return z2;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof ECPoint) {
            return equals((ECPoint) other);
        }
        return false;
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
        for (Object append : this.zs) {
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
        byte[] PO;
        if (compressed) {
            PO = new byte[(X.length + 1)];
            PO[0] = (byte) (normed.getCompressionYTilde() ? 3 : 2);
            System.arraycopy(X, 0, PO, 1, X.length);
            return PO;
        }
        byte[] Y = normed.getYCoord().getEncoded();
        PO = new byte[((X.length + Y.length) + 1)];
        PO[0] = (byte) 4;
        System.arraycopy(X, 0, PO, 1, X.length);
        System.arraycopy(Y, 0, PO, X.length + 1, Y.length);
        return PO;
    }

    public ECPoint timesPow2(int e) {
        if (e < 0) {
            throw new IllegalArgumentException("'e' cannot be negative");
        }
        ECPoint p = this;
        while (true) {
            e--;
            if (e < 0) {
                return p;
            }
            p = p.twice();
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
