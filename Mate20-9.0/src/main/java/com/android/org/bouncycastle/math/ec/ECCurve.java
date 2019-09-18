package com.android.org.bouncycastle.math.ec;

import com.android.org.bouncycastle.math.ec.ECFieldElement;
import com.android.org.bouncycastle.math.ec.ECPoint;
import com.android.org.bouncycastle.math.ec.endo.ECEndomorphism;
import com.android.org.bouncycastle.math.ec.endo.GLVEndomorphism;
import com.android.org.bouncycastle.math.field.FiniteField;
import com.android.org.bouncycastle.math.field.FiniteFields;
import com.android.org.bouncycastle.util.BigIntegers;
import com.android.org.bouncycastle.util.Integers;
import java.math.BigInteger;
import java.util.Hashtable;
import java.util.Random;

public abstract class ECCurve {
    public static final int COORD_AFFINE = 0;
    public static final int COORD_HOMOGENEOUS = 1;
    public static final int COORD_JACOBIAN = 2;
    public static final int COORD_JACOBIAN_CHUDNOVSKY = 3;
    public static final int COORD_JACOBIAN_MODIFIED = 4;
    public static final int COORD_LAMBDA_AFFINE = 5;
    public static final int COORD_LAMBDA_PROJECTIVE = 6;
    public static final int COORD_SKEWED = 7;
    protected ECFieldElement a;
    protected ECFieldElement b;
    protected BigInteger cofactor;
    protected int coord = 0;
    protected ECEndomorphism endomorphism = null;
    protected FiniteField field;
    protected ECMultiplier multiplier = null;
    protected BigInteger order;

    public static abstract class AbstractF2m extends ECCurve {
        private BigInteger[] si = null;

        public static BigInteger inverse(int m, int[] ks, BigInteger x) {
            return new LongArray(x).modInverse(m, ks).toBigInteger();
        }

        private static FiniteField buildField(int m, int k1, int k2, int k3) {
            if (k1 == 0) {
                throw new IllegalArgumentException("k1 must be > 0");
            } else if (k2 == 0) {
                if (k3 == 0) {
                    return FiniteFields.getBinaryExtensionField(new int[]{0, k1, m});
                }
                throw new IllegalArgumentException("k3 must be 0 if k2 == 0");
            } else if (k2 <= k1) {
                throw new IllegalArgumentException("k2 must be > k1");
            } else if (k3 > k2) {
                return FiniteFields.getBinaryExtensionField(new int[]{0, k1, k2, k3, m});
            } else {
                throw new IllegalArgumentException("k3 must be > k2");
            }
        }

        protected AbstractF2m(int m, int k1, int k2, int k3) {
            super(buildField(m, k1, k2, k3));
        }

        public boolean isValidFieldElement(BigInteger x) {
            return x != null && x.signum() >= 0 && x.bitLength() <= getFieldSize();
        }

        public ECPoint createPoint(BigInteger x, BigInteger y, boolean withCompression) {
            ECFieldElement X = fromBigInteger(x);
            ECFieldElement Y = fromBigInteger(y);
            switch (getCoordinateSystem()) {
                case 5:
                case 6:
                    if (!X.isZero()) {
                        Y = Y.divide(X).add(X);
                        break;
                    } else if (!Y.square().equals(getB())) {
                        throw new IllegalArgumentException();
                    }
                    break;
            }
            return createRawPoint(X, Y, withCompression);
        }

        /* access modifiers changed from: protected */
        public ECPoint decompressPoint(int yTilde, BigInteger X1) {
            ECFieldElement x = fromBigInteger(X1);
            ECFieldElement y = null;
            if (!x.isZero()) {
                ECFieldElement z = solveQuadraticEquation(x.square().invert().multiply(getB()).add(getA()).add(x));
                if (z != null) {
                    if (z.testBitZero() != (yTilde == 1)) {
                        z = z.addOne();
                    }
                    switch (getCoordinateSystem()) {
                        case 5:
                        case 6:
                            y = z.add(x);
                            break;
                        default:
                            y = z.multiply(x);
                            break;
                    }
                }
            } else {
                y = getB().sqrt();
            }
            if (y != null) {
                return createRawPoint(x, y, true);
            }
            throw new IllegalArgumentException("Invalid point compression");
        }

        private ECFieldElement solveQuadraticEquation(ECFieldElement beta) {
            ECFieldElement z;
            if (beta.isZero()) {
                return beta;
            }
            ECFieldElement zeroElement = fromBigInteger(ECConstants.ZERO);
            int m = getFieldSize();
            Random rand = new Random();
            do {
                ECFieldElement t = fromBigInteger(new BigInteger(m, rand));
                z = zeroElement;
                ECFieldElement w = beta;
                for (int i = 1; i < m; i++) {
                    ECFieldElement w2 = w.square();
                    z = z.square().add(w2.multiply(t));
                    w = w2.add(beta);
                }
                if (w.isZero() == 0) {
                    return null;
                }
            } while (z.square().add(z).isZero());
            return z;
        }

        /* access modifiers changed from: package-private */
        public synchronized BigInteger[] getSi() {
            if (this.si == null) {
                this.si = Tnaf.getSi(this);
            }
            return this.si;
        }

        public boolean isKoblitz() {
            return this.order != null && this.cofactor != null && this.b.isOne() && (this.a.isZero() || this.a.isOne());
        }
    }

    public static abstract class AbstractFp extends ECCurve {
        protected AbstractFp(BigInteger q) {
            super(FiniteFields.getPrimeField(q));
        }

        public boolean isValidFieldElement(BigInteger x) {
            return x != null && x.signum() >= 0 && x.compareTo(getField().getCharacteristic()) < 0;
        }

        /* access modifiers changed from: protected */
        public ECPoint decompressPoint(int yTilde, BigInteger X1) {
            ECFieldElement x = fromBigInteger(X1);
            ECFieldElement y = x.square().add(this.a).multiply(x).add(this.b).sqrt();
            if (y != null) {
                if (y.testBitZero() != (yTilde == 1)) {
                    y = y.negate();
                }
                return createRawPoint(x, y, true);
            }
            throw new IllegalArgumentException("Invalid point compression");
        }
    }

    public class Config {
        protected int coord;
        protected ECEndomorphism endomorphism;
        protected ECMultiplier multiplier;

        Config(int coord2, ECEndomorphism endomorphism2, ECMultiplier multiplier2) {
            this.coord = coord2;
            this.endomorphism = endomorphism2;
            this.multiplier = multiplier2;
        }

        public Config setCoordinateSystem(int coord2) {
            this.coord = coord2;
            return this;
        }

        public Config setEndomorphism(ECEndomorphism endomorphism2) {
            this.endomorphism = endomorphism2;
            return this;
        }

        public Config setMultiplier(ECMultiplier multiplier2) {
            this.multiplier = multiplier2;
            return this;
        }

        public ECCurve create() {
            if (ECCurve.this.supportsCoordinateSystem(this.coord)) {
                ECCurve c = ECCurve.this.cloneCurve();
                if (c != ECCurve.this) {
                    synchronized (c) {
                        c.coord = this.coord;
                        c.endomorphism = this.endomorphism;
                        c.multiplier = this.multiplier;
                    }
                    return c;
                }
                throw new IllegalStateException("implementation returned current curve");
            }
            throw new IllegalStateException("unsupported coordinate system");
        }
    }

    public static class F2m extends AbstractF2m {
        private static final int F2M_DEFAULT_COORDS = 6;
        private ECPoint.F2m infinity;
        private int k1;
        private int k2;
        private int k3;
        private int m;

        public F2m(int m2, int k, BigInteger a, BigInteger b) {
            this(m2, k, 0, 0, a, b, (BigInteger) null, (BigInteger) null);
        }

        public F2m(int m2, int k, BigInteger a, BigInteger b, BigInteger order, BigInteger cofactor) {
            this(m2, k, 0, 0, a, b, order, cofactor);
        }

        public F2m(int m2, int k12, int k22, int k32, BigInteger a, BigInteger b) {
            this(m2, k12, k22, k32, a, b, (BigInteger) null, (BigInteger) null);
        }

        public F2m(int m2, int k12, int k22, int k32, BigInteger a, BigInteger b, BigInteger order, BigInteger cofactor) {
            super(m2, k12, k22, k32);
            this.m = m2;
            this.k1 = k12;
            this.k2 = k22;
            this.k3 = k32;
            this.order = order;
            this.cofactor = cofactor;
            this.infinity = new ECPoint.F2m(this, null, null);
            this.a = fromBigInteger(a);
            this.b = fromBigInteger(b);
            this.coord = 6;
        }

        protected F2m(int m2, int k12, int k22, int k32, ECFieldElement a, ECFieldElement b, BigInteger order, BigInteger cofactor) {
            super(m2, k12, k22, k32);
            this.m = m2;
            this.k1 = k12;
            this.k2 = k22;
            this.k3 = k32;
            this.order = order;
            this.cofactor = cofactor;
            this.infinity = new ECPoint.F2m(this, null, null);
            this.a = a;
            this.b = b;
            this.coord = 6;
        }

        /* access modifiers changed from: protected */
        public ECCurve cloneCurve() {
            F2m f2m = new F2m(this.m, this.k1, this.k2, this.k3, this.a, this.b, this.order, this.cofactor);
            return f2m;
        }

        public boolean supportsCoordinateSystem(int coord) {
            if (coord != 6) {
                switch (coord) {
                    case 0:
                    case 1:
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public ECMultiplier createDefaultMultiplier() {
            if (isKoblitz()) {
                return new WTauNafMultiplier();
            }
            return super.createDefaultMultiplier();
        }

        public int getFieldSize() {
            return this.m;
        }

        public ECFieldElement fromBigInteger(BigInteger x) {
            ECFieldElement.F2m f2m = new ECFieldElement.F2m(this.m, this.k1, this.k2, this.k3, x);
            return f2m;
        }

        /* access modifiers changed from: protected */
        public ECPoint createRawPoint(ECFieldElement x, ECFieldElement y, boolean withCompression) {
            return new ECPoint.F2m(this, x, y, withCompression);
        }

        /* access modifiers changed from: protected */
        public ECPoint createRawPoint(ECFieldElement x, ECFieldElement y, ECFieldElement[] zs, boolean withCompression) {
            ECPoint.F2m f2m = new ECPoint.F2m(this, x, y, zs, withCompression);
            return f2m;
        }

        public ECPoint getInfinity() {
            return this.infinity;
        }

        public int getM() {
            return this.m;
        }

        public boolean isTrinomial() {
            return this.k2 == 0 && this.k3 == 0;
        }

        public int getK1() {
            return this.k1;
        }

        public int getK2() {
            return this.k2;
        }

        public int getK3() {
            return this.k3;
        }

        public BigInteger getN() {
            return this.order;
        }

        public BigInteger getH() {
            return this.cofactor;
        }
    }

    public static class Fp extends AbstractFp {
        private static final int FP_DEFAULT_COORDS = 4;
        ECPoint.Fp infinity;
        BigInteger q;
        BigInteger r;

        public Fp(BigInteger q2, BigInteger a, BigInteger b) {
            this(q2, a, b, null, null);
        }

        public Fp(BigInteger q2, BigInteger a, BigInteger b, BigInteger order, BigInteger cofactor) {
            super(q2);
            this.q = q2;
            this.r = ECFieldElement.Fp.calculateResidue(q2);
            this.infinity = new ECPoint.Fp(this, null, null);
            this.a = fromBigInteger(a);
            this.b = fromBigInteger(b);
            this.order = order;
            this.cofactor = cofactor;
            this.coord = 4;
        }

        protected Fp(BigInteger q2, BigInteger r2, ECFieldElement a, ECFieldElement b) {
            this(q2, r2, a, b, null, null);
        }

        protected Fp(BigInteger q2, BigInteger r2, ECFieldElement a, ECFieldElement b, BigInteger order, BigInteger cofactor) {
            super(q2);
            this.q = q2;
            this.r = r2;
            this.infinity = new ECPoint.Fp(this, null, null);
            this.a = a;
            this.b = b;
            this.order = order;
            this.cofactor = cofactor;
            this.coord = 4;
        }

        /* access modifiers changed from: protected */
        public ECCurve cloneCurve() {
            Fp fp = new Fp(this.q, this.r, this.a, this.b, this.order, this.cofactor);
            return fp;
        }

        public boolean supportsCoordinateSystem(int coord) {
            if (coord != 4) {
                switch (coord) {
                    case 0:
                    case 1:
                    case 2:
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }

        public BigInteger getQ() {
            return this.q;
        }

        public int getFieldSize() {
            return this.q.bitLength();
        }

        public ECFieldElement fromBigInteger(BigInteger x) {
            return new ECFieldElement.Fp(this.q, this.r, x);
        }

        /* access modifiers changed from: protected */
        public ECPoint createRawPoint(ECFieldElement x, ECFieldElement y, boolean withCompression) {
            return new ECPoint.Fp(this, x, y, withCompression);
        }

        /* access modifiers changed from: protected */
        public ECPoint createRawPoint(ECFieldElement x, ECFieldElement y, ECFieldElement[] zs, boolean withCompression) {
            ECPoint.Fp fp = new ECPoint.Fp(this, x, y, zs, withCompression);
            return fp;
        }

        public ECPoint importPoint(ECPoint p) {
            if (this != p.getCurve() && getCoordinateSystem() == 2 && !p.isInfinity()) {
                switch (p.getCurve().getCoordinateSystem()) {
                    case 2:
                    case 3:
                    case 4:
                        ECPoint.Fp fp = new ECPoint.Fp(this, fromBigInteger(p.x.toBigInteger()), fromBigInteger(p.y.toBigInteger()), new ECFieldElement[]{fromBigInteger(p.zs[0].toBigInteger())}, p.withCompression);
                        return fp;
                }
            }
            return super.importPoint(p);
        }

        public ECPoint getInfinity() {
            return this.infinity;
        }
    }

    /* access modifiers changed from: protected */
    public abstract ECCurve cloneCurve();

    /* access modifiers changed from: protected */
    public abstract ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, boolean z);

    /* access modifiers changed from: protected */
    public abstract ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr, boolean z);

    /* access modifiers changed from: protected */
    public abstract ECPoint decompressPoint(int i, BigInteger bigInteger);

    public abstract ECFieldElement fromBigInteger(BigInteger bigInteger);

    public abstract int getFieldSize();

    public abstract ECPoint getInfinity();

    public abstract boolean isValidFieldElement(BigInteger bigInteger);

    public static int[] getAllCoordinateSystems() {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7};
    }

    protected ECCurve(FiniteField field2) {
        this.field = field2;
    }

    public synchronized Config configure() {
        return new Config(this.coord, this.endomorphism, this.multiplier);
    }

    public ECPoint validatePoint(BigInteger x, BigInteger y) {
        ECPoint p = createPoint(x, y);
        if (p.isValid()) {
            return p;
        }
        throw new IllegalArgumentException("Invalid point coordinates");
    }

    public ECPoint validatePoint(BigInteger x, BigInteger y, boolean withCompression) {
        ECPoint p = createPoint(x, y, withCompression);
        if (p.isValid()) {
            return p;
        }
        throw new IllegalArgumentException("Invalid point coordinates");
    }

    public ECPoint createPoint(BigInteger x, BigInteger y) {
        return createPoint(x, y, false);
    }

    public ECPoint createPoint(BigInteger x, BigInteger y, boolean withCompression) {
        return createRawPoint(fromBigInteger(x), fromBigInteger(y), withCompression);
    }

    /* access modifiers changed from: protected */
    public ECMultiplier createDefaultMultiplier() {
        if (this.endomorphism instanceof GLVEndomorphism) {
            return new GLVMultiplier(this, (GLVEndomorphism) this.endomorphism);
        }
        return new WNafL2RMultiplier();
    }

    public boolean supportsCoordinateSystem(int coord2) {
        return coord2 == 0;
    }

    public PreCompInfo getPreCompInfo(ECPoint point, String name) {
        PreCompInfo preCompInfo;
        checkPoint(point);
        synchronized (point) {
            Hashtable table = point.preCompTable;
            preCompInfo = table == null ? null : (PreCompInfo) table.get(name);
        }
        return preCompInfo;
    }

    public void setPreCompInfo(ECPoint point, String name, PreCompInfo preCompInfo) {
        checkPoint(point);
        synchronized (point) {
            Hashtable table = point.preCompTable;
            if (table == null) {
                Hashtable hashtable = new Hashtable(4);
                table = hashtable;
                point.preCompTable = hashtable;
            }
            table.put(name, preCompInfo);
        }
    }

    public ECPoint importPoint(ECPoint p) {
        if (this == p.getCurve()) {
            return p;
        }
        if (p.isInfinity()) {
            return getInfinity();
        }
        ECPoint p2 = p.normalize();
        return validatePoint(p2.getXCoord().toBigInteger(), p2.getYCoord().toBigInteger(), p2.withCompression);
    }

    public void normalizeAll(ECPoint[] points) {
        normalizeAll(points, 0, points.length, null);
    }

    public void normalizeAll(ECPoint[] points, int off, int len, ECFieldElement iso) {
        checkPoints(points, off, len);
        int coordinateSystem = getCoordinateSystem();
        if (coordinateSystem != 0 && coordinateSystem != 5) {
            ECFieldElement[] zs = new ECFieldElement[len];
            int[] indices = new int[len];
            int index = 0;
            int count = 0;
            for (int i = 0; i < len; i++) {
                ECPoint p = points[off + i];
                if (p != null && (iso != null || !p.isNormalized())) {
                    zs[count] = p.getZCoord(0);
                    indices[count] = off + i;
                    count++;
                }
            }
            if (count != 0) {
                ECAlgorithms.montgomeryTrick(zs, 0, count, iso);
                while (true) {
                    int j = index;
                    if (j < count) {
                        int index2 = indices[j];
                        points[index2] = points[index2].normalize(zs[j]);
                        index = j + 1;
                    } else {
                        return;
                    }
                }
            }
        } else if (iso != null) {
            throw new IllegalArgumentException("'iso' not valid for affine coordinates");
        }
    }

    public FiniteField getField() {
        return this.field;
    }

    public ECFieldElement getA() {
        return this.a;
    }

    public ECFieldElement getB() {
        return this.b;
    }

    public BigInteger getOrder() {
        return this.order;
    }

    public BigInteger getCofactor() {
        return this.cofactor;
    }

    public int getCoordinateSystem() {
        return this.coord;
    }

    public ECEndomorphism getEndomorphism() {
        return this.endomorphism;
    }

    public synchronized ECMultiplier getMultiplier() {
        if (this.multiplier == null) {
            this.multiplier = createDefaultMultiplier();
        }
        return this.multiplier;
    }

    public ECPoint decodePoint(byte[] encoded) {
        ECPoint p;
        int expectedLength = (getFieldSize() + 7) / 8;
        boolean z = false;
        byte type = encoded[0];
        switch (type) {
            case 0:
                if (encoded.length == 1) {
                    p = getInfinity();
                    break;
                } else {
                    throw new IllegalArgumentException("Incorrect length for infinity encoding");
                }
            case 2:
            case 3:
                if (encoded.length == expectedLength + 1) {
                    p = decompressPoint(type & 1, BigIntegers.fromUnsignedByteArray(encoded, 1, expectedLength));
                    if (!p.satisfiesCofactor()) {
                        throw new IllegalArgumentException("Invalid point");
                    }
                } else {
                    throw new IllegalArgumentException("Incorrect length for compressed encoding");
                }
                break;
            case 4:
                if (encoded.length == (2 * expectedLength) + 1) {
                    p = validatePoint(BigIntegers.fromUnsignedByteArray(encoded, 1, expectedLength), BigIntegers.fromUnsignedByteArray(encoded, 1 + expectedLength, expectedLength));
                    break;
                } else {
                    throw new IllegalArgumentException("Incorrect length for uncompressed encoding");
                }
            case 6:
            case 7:
                if (encoded.length == (2 * expectedLength) + 1) {
                    BigInteger X = BigIntegers.fromUnsignedByteArray(encoded, 1, expectedLength);
                    BigInteger Y = BigIntegers.fromUnsignedByteArray(encoded, 1 + expectedLength, expectedLength);
                    boolean testBit = Y.testBit(0);
                    if (type == 7) {
                        z = true;
                    }
                    if (testBit == z) {
                        p = validatePoint(X, Y);
                        break;
                    } else {
                        throw new IllegalArgumentException("Inconsistent Y coordinate in hybrid encoding");
                    }
                } else {
                    throw new IllegalArgumentException("Incorrect length for hybrid encoding");
                }
            default:
                throw new IllegalArgumentException("Invalid point encoding 0x" + Integer.toString(type, 16));
        }
        if (type == 0 || !p.isInfinity()) {
            return p;
        }
        throw new IllegalArgumentException("Invalid infinity encoding");
    }

    /* access modifiers changed from: protected */
    public void checkPoint(ECPoint point) {
        if (point == null || this != point.getCurve()) {
            throw new IllegalArgumentException("'point' must be non-null and on this curve");
        }
    }

    /* access modifiers changed from: protected */
    public void checkPoints(ECPoint[] points) {
        checkPoints(points, 0, points.length);
    }

    /* access modifiers changed from: protected */
    public void checkPoints(ECPoint[] points, int off, int len) {
        if (points == null) {
            throw new IllegalArgumentException("'points' cannot be null");
        } else if (off < 0 || len < 0 || off > points.length - len) {
            throw new IllegalArgumentException("invalid range specified for 'points'");
        } else {
            int i = 0;
            while (i < len) {
                ECPoint point = points[off + i];
                if (point == null || this == point.getCurve()) {
                    i++;
                } else {
                    throw new IllegalArgumentException("'points' entries must be null or on this curve");
                }
            }
        }
    }

    public boolean equals(ECCurve other) {
        return this == other || (other != null && getField().equals(other.getField()) && getA().toBigInteger().equals(other.getA().toBigInteger()) && getB().toBigInteger().equals(other.getB().toBigInteger()));
    }

    public boolean equals(Object obj) {
        return this == obj || ((obj instanceof ECCurve) && equals((ECCurve) obj));
    }

    public int hashCode() {
        return (getField().hashCode() ^ Integers.rotateLeft(getA().toBigInteger().hashCode(), 8)) ^ Integers.rotateLeft(getB().toBigInteger().hashCode(), 16);
    }
}
