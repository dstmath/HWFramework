package com.android.org.bouncycastle.math.ec;

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
                if (k3 != 0) {
                    throw new IllegalArgumentException("k3 must be 0 if k2 == 0");
                }
                return FiniteFields.getBinaryExtensionField(new int[]{0, k1, m});
            } else if (k2 <= k1) {
                throw new IllegalArgumentException("k2 must be > k1");
            } else if (k3 <= k2) {
                throw new IllegalArgumentException("k3 must be > k2");
            } else {
                return FiniteFields.getBinaryExtensionField(new int[]{0, k1, k2, k3, m});
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

        protected ECPoint decompressPoint(int yTilde, BigInteger X1) {
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
            }
            y = getB().sqrt();
            if (y != null) {
                return createRawPoint(x, y, true);
            }
            throw new IllegalArgumentException("Invalid point compression");
        }

        private ECFieldElement solveQuadraticEquation(ECFieldElement beta) {
            if (beta.isZero()) {
                return beta;
            }
            ECFieldElement z;
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
                if (!w.isZero()) {
                    return null;
                }
            } while (z.square().add(z).isZero());
            return z;
        }

        synchronized BigInteger[] getSi() {
            if (this.si == null) {
                this.si = Tnaf.getSi(this);
            }
            return this.si;
        }

        public boolean isKoblitz() {
            if (this.order == null || this.cofactor == null || !this.b.isOne()) {
                return false;
            }
            return !this.a.isZero() ? this.a.isOne() : true;
        }
    }

    public static abstract class AbstractFp extends ECCurve {
        protected AbstractFp(BigInteger q) {
            super(FiniteFields.getPrimeField(q));
        }

        public boolean isValidFieldElement(BigInteger x) {
            return x != null && x.signum() >= 0 && x.compareTo(getField().getCharacteristic()) < 0;
        }

        protected ECPoint decompressPoint(int yTilde, BigInteger X1) {
            ECFieldElement x = fromBigInteger(X1);
            ECFieldElement y = x.square().add(this.a).multiply(x).add(this.b).sqrt();
            if (y == null) {
                throw new IllegalArgumentException("Invalid point compression");
            }
            if (y.testBitZero() != (yTilde == 1)) {
                y = y.negate();
            }
            return createRawPoint(x, y, true);
        }
    }

    public class Config {
        protected int coord;
        protected ECEndomorphism endomorphism;
        protected ECMultiplier multiplier;

        Config(int coord, ECEndomorphism endomorphism, ECMultiplier multiplier) {
            this.coord = coord;
            this.endomorphism = endomorphism;
            this.multiplier = multiplier;
        }

        public Config setCoordinateSystem(int coord) {
            this.coord = coord;
            return this;
        }

        public Config setEndomorphism(ECEndomorphism endomorphism) {
            this.endomorphism = endomorphism;
            return this;
        }

        public Config setMultiplier(ECMultiplier multiplier) {
            this.multiplier = multiplier;
            return this;
        }

        public ECCurve create() {
            if (ECCurve.this.supportsCoordinateSystem(this.coord)) {
                ECCurve c = ECCurve.this.cloneCurve();
                if (c == ECCurve.this) {
                    throw new IllegalStateException("implementation returned current curve");
                }
                synchronized (c) {
                    c.coord = this.coord;
                    c.endomorphism = this.endomorphism;
                    c.multiplier = this.multiplier;
                }
                return c;
            }
            throw new IllegalStateException("unsupported coordinate system");
        }
    }

    public static class F2m extends AbstractF2m {
        private static final int F2M_DEFAULT_COORDS = 6;
        private com.android.org.bouncycastle.math.ec.ECPoint.F2m infinity;
        private int k1;
        private int k2;
        private int k3;
        private int m;

        public F2m(int m, int k, BigInteger a, BigInteger b) {
            this(m, k, 0, 0, a, b, null, null);
        }

        public F2m(int m, int k, BigInteger a, BigInteger b, BigInteger order, BigInteger cofactor) {
            this(m, k, 0, 0, a, b, order, cofactor);
        }

        public F2m(int m, int k1, int k2, int k3, BigInteger a, BigInteger b) {
            this(m, k1, k2, k3, a, b, null, null);
        }

        public F2m(int m, int k1, int k2, int k3, BigInteger a, BigInteger b, BigInteger order, BigInteger cofactor) {
            super(m, k1, k2, k3);
            this.m = m;
            this.k1 = k1;
            this.k2 = k2;
            this.k3 = k3;
            this.order = order;
            this.cofactor = cofactor;
            this.infinity = new com.android.org.bouncycastle.math.ec.ECPoint.F2m(this, null, null);
            this.a = fromBigInteger(a);
            this.b = fromBigInteger(b);
            this.coord = 6;
        }

        protected F2m(int m, int k1, int k2, int k3, ECFieldElement a, ECFieldElement b, BigInteger order, BigInteger cofactor) {
            super(m, k1, k2, k3);
            this.m = m;
            this.k1 = k1;
            this.k2 = k2;
            this.k3 = k3;
            this.order = order;
            this.cofactor = cofactor;
            this.infinity = new com.android.org.bouncycastle.math.ec.ECPoint.F2m(this, null, null);
            this.a = a;
            this.b = b;
            this.coord = 6;
        }

        protected ECCurve cloneCurve() {
            return new F2m(this.m, this.k1, this.k2, this.k3, this.a, this.b, this.order, this.cofactor);
        }

        public boolean supportsCoordinateSystem(int coord) {
            switch (coord) {
                case 0:
                case 1:
                case 6:
                    return true;
                default:
                    return false;
            }
        }

        protected ECMultiplier createDefaultMultiplier() {
            if (isKoblitz()) {
                return new WTauNafMultiplier();
            }
            return super.createDefaultMultiplier();
        }

        public int getFieldSize() {
            return this.m;
        }

        public ECFieldElement fromBigInteger(BigInteger x) {
            return new com.android.org.bouncycastle.math.ec.ECFieldElement.F2m(this.m, this.k1, this.k2, this.k3, x);
        }

        protected ECPoint createRawPoint(ECFieldElement x, ECFieldElement y, boolean withCompression) {
            return new com.android.org.bouncycastle.math.ec.ECPoint.F2m(this, x, y, withCompression);
        }

        protected ECPoint createRawPoint(ECFieldElement x, ECFieldElement y, ECFieldElement[] zs, boolean withCompression) {
            return new com.android.org.bouncycastle.math.ec.ECPoint.F2m(this, x, y, zs, withCompression);
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
        com.android.org.bouncycastle.math.ec.ECPoint.Fp infinity;
        BigInteger q;
        BigInteger r;

        public Fp(BigInteger q, BigInteger a, BigInteger b) {
            this(q, a, b, null, null);
        }

        public Fp(BigInteger q, BigInteger a, BigInteger b, BigInteger order, BigInteger cofactor) {
            super(q);
            this.q = q;
            this.r = com.android.org.bouncycastle.math.ec.ECFieldElement.Fp.calculateResidue(q);
            this.infinity = new com.android.org.bouncycastle.math.ec.ECPoint.Fp(this, null, null);
            this.a = fromBigInteger(a);
            this.b = fromBigInteger(b);
            this.order = order;
            this.cofactor = cofactor;
            this.coord = 4;
        }

        protected Fp(BigInteger q, BigInteger r, ECFieldElement a, ECFieldElement b) {
            this(q, r, a, b, null, null);
        }

        protected Fp(BigInteger q, BigInteger r, ECFieldElement a, ECFieldElement b, BigInteger order, BigInteger cofactor) {
            super(q);
            this.q = q;
            this.r = r;
            this.infinity = new com.android.org.bouncycastle.math.ec.ECPoint.Fp(this, null, null);
            this.a = a;
            this.b = b;
            this.order = order;
            this.cofactor = cofactor;
            this.coord = 4;
        }

        protected ECCurve cloneCurve() {
            return new Fp(this.q, this.r, this.a, this.b, this.order, this.cofactor);
        }

        public boolean supportsCoordinateSystem(int coord) {
            switch (coord) {
                case 0:
                case 1:
                case 2:
                case 4:
                    return true;
                default:
                    return false;
            }
        }

        public BigInteger getQ() {
            return this.q;
        }

        public int getFieldSize() {
            return this.q.bitLength();
        }

        public ECFieldElement fromBigInteger(BigInteger x) {
            return new com.android.org.bouncycastle.math.ec.ECFieldElement.Fp(this.q, this.r, x);
        }

        protected ECPoint createRawPoint(ECFieldElement x, ECFieldElement y, boolean withCompression) {
            return new com.android.org.bouncycastle.math.ec.ECPoint.Fp(this, x, y, withCompression);
        }

        protected ECPoint createRawPoint(ECFieldElement x, ECFieldElement y, ECFieldElement[] zs, boolean withCompression) {
            return new com.android.org.bouncycastle.math.ec.ECPoint.Fp(this, x, y, zs, withCompression);
        }

        public ECPoint importPoint(ECPoint p) {
            if (!(this == p.getCurve() || getCoordinateSystem() != 2 || (p.isInfinity() ^ 1) == 0)) {
                switch (p.getCurve().getCoordinateSystem()) {
                    case 2:
                    case 3:
                    case 4:
                        return new com.android.org.bouncycastle.math.ec.ECPoint.Fp(this, fromBigInteger(p.x.toBigInteger()), fromBigInteger(p.y.toBigInteger()), new ECFieldElement[]{fromBigInteger(p.zs[0].toBigInteger())}, p.withCompression);
                }
            }
            return super.importPoint(p);
        }

        public ECPoint getInfinity() {
            return this.infinity;
        }
    }

    protected abstract ECCurve cloneCurve();

    protected abstract ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, boolean z);

    protected abstract ECPoint createRawPoint(ECFieldElement eCFieldElement, ECFieldElement eCFieldElement2, ECFieldElement[] eCFieldElementArr, boolean z);

    protected abstract ECPoint decompressPoint(int i, BigInteger bigInteger);

    public abstract ECFieldElement fromBigInteger(BigInteger bigInteger);

    public abstract int getFieldSize();

    public abstract ECPoint getInfinity();

    public abstract boolean isValidFieldElement(BigInteger bigInteger);

    public static int[] getAllCoordinateSystems() {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7};
    }

    protected ECCurve(FiniteField field) {
        this.field = field;
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

    protected ECMultiplier createDefaultMultiplier() {
        if (this.endomorphism instanceof GLVEndomorphism) {
            return new GLVMultiplier(this, (GLVEndomorphism) this.endomorphism);
        }
        return new WNafL2RMultiplier();
    }

    public boolean supportsCoordinateSystem(int coord) {
        return coord == 0;
    }

    public PreCompInfo getPreCompInfo(ECPoint point, String name) {
        PreCompInfo preCompInfo = null;
        checkPoint(point);
        synchronized (point) {
            Hashtable table = point.preCompTable;
            if (table != null) {
                preCompInfo = (PreCompInfo) table.get(name);
            }
        }
        return preCompInfo;
    }

    public void setPreCompInfo(ECPoint point, String name, PreCompInfo preCompInfo) {
        checkPoint(point);
        synchronized (point) {
            Hashtable table = point.preCompTable;
            if (table == null) {
                table = new Hashtable(4);
                point.preCompTable = table;
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
        p = p.normalize();
        return validatePoint(p.getXCoord().toBigInteger(), p.getYCoord().toBigInteger(), p.withCompression);
    }

    public void normalizeAll(ECPoint[] points) {
        normalizeAll(points, 0, points.length, null);
    }

    public void normalizeAll(ECPoint[] points, int off, int len, ECFieldElement iso) {
        checkPoints(points, off, len);
        switch (getCoordinateSystem()) {
            case 0:
            case 5:
                if (iso != null) {
                    throw new IllegalArgumentException("'iso' not valid for affine coordinates");
                }
                return;
            default:
                ECFieldElement[] zs = new ECFieldElement[len];
                int[] indices = new int[len];
                int i = 0;
                int count = 0;
                while (i < len) {
                    int count2;
                    ECPoint p = points[off + i];
                    if (p == null) {
                        count2 = count;
                    } else if (iso == null && (p.isNormalized() ^ 1) == 0) {
                        count2 = count;
                    } else {
                        zs[count] = p.getZCoord(0);
                        count2 = count + 1;
                        indices[count] = off + i;
                    }
                    i++;
                    count = count2;
                }
                if (count != 0) {
                    ECAlgorithms.montgomeryTrick(zs, 0, count, iso);
                    for (int j = 0; j < count; j++) {
                        int index = indices[j];
                        points[index] = points[index].normalize(zs[j]);
                    }
                    return;
                }
                return;
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
        boolean z = true;
        int expectedLength = (getFieldSize() + 7) / 8;
        byte type = encoded[0];
        switch (type) {
            case (byte) 0:
                if (encoded.length == 1) {
                    p = getInfinity();
                    break;
                }
                throw new IllegalArgumentException("Incorrect length for infinity encoding");
            case (byte) 2:
            case (byte) 3:
                if (encoded.length != expectedLength + 1) {
                    throw new IllegalArgumentException("Incorrect length for compressed encoding");
                }
                p = decompressPoint(type & 1, BigIntegers.fromUnsignedByteArray(encoded, 1, expectedLength));
                if (!p.satisfiesCofactor()) {
                    throw new IllegalArgumentException("Invalid point");
                }
                break;
            case (byte) 4:
                if (encoded.length == (expectedLength * 2) + 1) {
                    p = validatePoint(BigIntegers.fromUnsignedByteArray(encoded, 1, expectedLength), BigIntegers.fromUnsignedByteArray(encoded, expectedLength + 1, expectedLength));
                    break;
                }
                throw new IllegalArgumentException("Incorrect length for uncompressed encoding");
            case (byte) 6:
            case (byte) 7:
                if (encoded.length == (expectedLength * 2) + 1) {
                    BigInteger X = BigIntegers.fromUnsignedByteArray(encoded, 1, expectedLength);
                    BigInteger Y = BigIntegers.fromUnsignedByteArray(encoded, expectedLength + 1, expectedLength);
                    boolean testBit = Y.testBit(0);
                    if (type != (byte) 7) {
                        z = false;
                    }
                    if (testBit == z) {
                        p = validatePoint(X, Y);
                        break;
                    }
                    throw new IllegalArgumentException("Inconsistent Y coordinate in hybrid encoding");
                }
                throw new IllegalArgumentException("Incorrect length for hybrid encoding");
            default:
                throw new IllegalArgumentException("Invalid point encoding 0x" + Integer.toString(type, 16));
        }
        if (type == (byte) 0 || !p.isInfinity()) {
            return p;
        }
        throw new IllegalArgumentException("Invalid infinity encoding");
    }

    protected void checkPoint(ECPoint point) {
        if (point == null || this != point.getCurve()) {
            throw new IllegalArgumentException("'point' must be non-null and on this curve");
        }
    }

    protected void checkPoints(ECPoint[] points) {
        checkPoints(points, 0, points.length);
    }

    protected void checkPoints(ECPoint[] points, int off, int len) {
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
        if (this == other) {
            return true;
        }
        if (other != null && getField().equals(other.getField()) && getA().toBigInteger().equals(other.getA().toBigInteger())) {
            return getB().toBigInteger().equals(other.getB().toBigInteger());
        }
        return false;
    }

    public boolean equals(Object obj) {
        if (this != obj) {
            return obj instanceof ECCurve ? equals((ECCurve) obj) : false;
        } else {
            return true;
        }
    }

    public int hashCode() {
        return (getField().hashCode() ^ Integers.rotateLeft(getA().toBigInteger().hashCode(), 8)) ^ Integers.rotateLeft(getB().toBigInteger().hashCode(), 16);
    }
}
