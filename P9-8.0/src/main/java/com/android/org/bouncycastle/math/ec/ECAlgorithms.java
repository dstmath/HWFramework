package com.android.org.bouncycastle.math.ec;

import com.android.org.bouncycastle.math.ec.ECCurve.AbstractF2m;
import com.android.org.bouncycastle.math.ec.endo.ECEndomorphism;
import com.android.org.bouncycastle.math.ec.endo.GLVEndomorphism;
import com.android.org.bouncycastle.math.field.FiniteField;
import com.android.org.bouncycastle.math.field.PolynomialExtensionField;
import java.math.BigInteger;

public class ECAlgorithms {
    public static boolean isF2mCurve(ECCurve c) {
        return isF2mField(c.getField());
    }

    public static boolean isF2mField(FiniteField field) {
        if (field.getDimension() <= 1 || !field.getCharacteristic().equals(ECConstants.TWO)) {
            return false;
        }
        return field instanceof PolynomialExtensionField;
    }

    public static boolean isFpCurve(ECCurve c) {
        return isFpField(c.getField());
    }

    public static boolean isFpField(FiniteField field) {
        return field.getDimension() == 1;
    }

    public static ECPoint sumOfMultiplies(ECPoint[] ps, BigInteger[] ks) {
        if (ps == null || ks == null || ps.length != ks.length || ps.length < 1) {
            throw new IllegalArgumentException("point and scalar arrays should be non-null, and of equal, non-zero, length");
        }
        int count = ps.length;
        switch (count) {
            case 1:
                return ps[0].multiply(ks[0]);
            case 2:
                return sumOfTwoMultiplies(ps[0], ks[0], ps[1], ks[1]);
            default:
                ECPoint p = ps[0];
                ECCurve c = p.getCurve();
                ECPoint[] imported = new ECPoint[count];
                imported[0] = p;
                for (int i = 1; i < count; i++) {
                    imported[i] = importPoint(c, ps[i]);
                }
                ECEndomorphism endomorphism = c.getEndomorphism();
                if (endomorphism instanceof GLVEndomorphism) {
                    return validatePoint(implSumOfMultipliesGLV(imported, ks, (GLVEndomorphism) endomorphism));
                }
                return validatePoint(implSumOfMultiplies(imported, ks));
        }
    }

    public static ECPoint sumOfTwoMultiplies(ECPoint P, BigInteger a, ECPoint Q, BigInteger b) {
        ECCurve cp = P.getCurve();
        Q = importPoint(cp, Q);
        if ((cp instanceof AbstractF2m) && ((AbstractF2m) cp).isKoblitz()) {
            return validatePoint(P.multiply(a).add(Q.multiply(b)));
        }
        ECEndomorphism endomorphism = cp.getEndomorphism();
        if (!(endomorphism instanceof GLVEndomorphism)) {
            return validatePoint(implShamirsTrickWNaf(P, a, Q, b));
        }
        return validatePoint(implSumOfMultipliesGLV(new ECPoint[]{P, Q}, new BigInteger[]{a, b}, (GLVEndomorphism) endomorphism));
    }

    public static ECPoint shamirsTrick(ECPoint P, BigInteger k, ECPoint Q, BigInteger l) {
        return validatePoint(implShamirsTrickJsf(P, k, importPoint(P.getCurve(), Q), l));
    }

    public static ECPoint importPoint(ECCurve c, ECPoint p) {
        if (c.equals(p.getCurve())) {
            return c.importPoint(p);
        }
        throw new IllegalArgumentException("Point must be on the same curve");
    }

    public static void montgomeryTrick(ECFieldElement[] zs, int off, int len) {
        montgomeryTrick(zs, off, len, null);
    }

    public static void montgomeryTrick(ECFieldElement[] zs, int off, int len, ECFieldElement scale) {
        ECFieldElement[] c = new ECFieldElement[len];
        c[0] = zs[off];
        int i = 0;
        while (true) {
            i++;
            if (i >= len) {
                break;
            }
            c[i] = c[i - 1].multiply(zs[off + i]);
        }
        i--;
        if (scale != null) {
            c[i] = c[i].multiply(scale);
        }
        ECFieldElement u = c[i].invert();
        int i2 = i;
        while (i2 > 0) {
            i = i2 - 1;
            int j = off + i2;
            ECFieldElement tmp = zs[j];
            zs[j] = c[i].multiply(u);
            u = u.multiply(tmp);
            i2 = i;
        }
        zs[off] = u;
    }

    public static ECPoint referenceMultiply(ECPoint p, BigInteger k) {
        BigInteger x = k.abs();
        ECPoint q = p.getCurve().getInfinity();
        int t = x.bitLength();
        if (t > 0) {
            if (x.testBit(0)) {
                q = p;
            }
            for (int i = 1; i < t; i++) {
                p = p.twice();
                if (x.testBit(i)) {
                    q = q.add(p);
                }
            }
        }
        return k.signum() < 0 ? q.negate() : q;
    }

    public static ECPoint validatePoint(ECPoint p) {
        if (p.isValid()) {
            return p;
        }
        throw new IllegalArgumentException("Invalid point");
    }

    static ECPoint implShamirsTrickJsf(ECPoint P, BigInteger k, ECPoint Q, BigInteger l) {
        ECCurve curve = P.getCurve();
        ECPoint infinity = curve.getInfinity();
        ECPoint PaddQ = P.add(Q);
        ECPoint PsubQ = P.subtract(Q);
        curve.normalizeAll(new ECPoint[]{Q, PsubQ, P, PaddQ});
        ECPoint[] table = new ECPoint[]{points[3].negate(), points[2].negate(), points[1].negate(), points[0].negate(), infinity, points[0], points[1], points[2], points[3]};
        byte[] jsf = WNafUtil.generateJSF(k, l);
        ECPoint R = infinity;
        int i = jsf.length;
        while (true) {
            i--;
            if (i < 0) {
                return R;
            }
            int jsfi = jsf[i];
            int lDigit = (jsfi << 28) >> 28;
            R = R.twicePlus(table[((((jsfi << 24) >> 28) * 3) + 4) + lDigit]);
        }
    }

    static ECPoint implShamirsTrickWNaf(ECPoint P, BigInteger k, ECPoint Q, BigInteger l) {
        boolean negK = k.signum() < 0;
        boolean negL = l.signum() < 0;
        k = k.abs();
        l = l.abs();
        int widthP = Math.max(2, Math.min(16, WNafUtil.getWindowSize(k.bitLength())));
        int widthQ = Math.max(2, Math.min(16, WNafUtil.getWindowSize(l.bitLength())));
        WNafPreCompInfo infoP = WNafUtil.precompute(P, widthP, true);
        WNafPreCompInfo infoQ = WNafUtil.precompute(Q, widthQ, true);
        return implShamirsTrickWNaf(negK ? infoP.getPreCompNeg() : infoP.getPreComp(), negK ? infoP.getPreComp() : infoP.getPreCompNeg(), WNafUtil.generateWindowNaf(widthP, k), negL ? infoQ.getPreCompNeg() : infoQ.getPreComp(), negL ? infoQ.getPreComp() : infoQ.getPreCompNeg(), WNafUtil.generateWindowNaf(widthQ, l));
    }

    static ECPoint implShamirsTrickWNaf(ECPoint P, BigInteger k, ECPointMap pointMapQ, BigInteger l) {
        boolean negK = k.signum() < 0;
        boolean negL = l.signum() < 0;
        k = k.abs();
        l = l.abs();
        int width = Math.max(2, Math.min(16, WNafUtil.getWindowSize(Math.max(k.bitLength(), l.bitLength()))));
        ECPoint Q = WNafUtil.mapPointWithPrecomp(P, width, true, pointMapQ);
        WNafPreCompInfo infoP = WNafUtil.getWNafPreCompInfo(P);
        WNafPreCompInfo infoQ = WNafUtil.getWNafPreCompInfo(Q);
        return implShamirsTrickWNaf(negK ? infoP.getPreCompNeg() : infoP.getPreComp(), negK ? infoP.getPreComp() : infoP.getPreCompNeg(), WNafUtil.generateWindowNaf(width, k), negL ? infoQ.getPreCompNeg() : infoQ.getPreComp(), negL ? infoQ.getPreComp() : infoQ.getPreCompNeg(), WNafUtil.generateWindowNaf(width, l));
    }

    private static ECPoint implShamirsTrickWNaf(ECPoint[] preCompP, ECPoint[] preCompNegP, byte[] wnafP, ECPoint[] preCompQ, ECPoint[] preCompNegQ, byte[] wnafQ) {
        int len = Math.max(wnafP.length, wnafQ.length);
        ECPoint infinity = preCompP[0].getCurve().getInfinity();
        ECPoint R = infinity;
        int zeroes = 0;
        int i = len - 1;
        while (i >= 0) {
            int wiP = i < wnafP.length ? wnafP[i] : 0;
            int wiQ = i < wnafQ.length ? wnafQ[i] : 0;
            if ((wiP | wiQ) == 0) {
                zeroes++;
            } else {
                ECPoint r = infinity;
                if (wiP != 0) {
                    r = infinity.add((wiP < 0 ? preCompNegP : preCompP)[Math.abs(wiP) >>> 1]);
                }
                if (wiQ != 0) {
                    r = r.add((wiQ < 0 ? preCompNegQ : preCompQ)[Math.abs(wiQ) >>> 1]);
                }
                if (zeroes > 0) {
                    R = R.timesPow2(zeroes);
                    zeroes = 0;
                }
                R = R.twicePlus(r);
            }
            i--;
        }
        if (zeroes > 0) {
            return R.timesPow2(zeroes);
        }
        return R;
    }

    static ECPoint implSumOfMultiplies(ECPoint[] ps, BigInteger[] ks) {
        int count = ps.length;
        boolean[] negs = new boolean[count];
        WNafPreCompInfo[] infos = new WNafPreCompInfo[count];
        byte[][] wnafs = new byte[count][];
        for (int i = 0; i < count; i++) {
            boolean z;
            BigInteger ki = ks[i];
            if (ki.signum() < 0) {
                z = true;
            } else {
                z = false;
            }
            negs[i] = z;
            ki = ki.abs();
            int width = Math.max(2, Math.min(16, WNafUtil.getWindowSize(ki.bitLength())));
            infos[i] = WNafUtil.precompute(ps[i], width, true);
            wnafs[i] = WNafUtil.generateWindowNaf(width, ki);
        }
        return implSumOfMultiplies(negs, infos, wnafs);
    }

    static ECPoint implSumOfMultipliesGLV(ECPoint[] ps, BigInteger[] ks, GLVEndomorphism glvEndomorphism) {
        int i;
        int i2;
        BigInteger n = ps[0].getCurve().getOrder();
        BigInteger[] abs = new BigInteger[(len << 1)];
        int j = 0;
        for (i = 0; i < len; i++) {
            BigInteger[] ab = glvEndomorphism.decomposeScalar(ks[i].mod(n));
            i2 = j + 1;
            abs[j] = ab[0];
            j = i2 + 1;
            abs[i2] = ab[1];
        }
        ECPointMap pointMap = glvEndomorphism.getPointMap();
        if (glvEndomorphism.hasEfficientPointMap()) {
            return implSumOfMultiplies(ps, pointMap, abs);
        }
        ECPoint[] pqs = new ECPoint[(len << 1)];
        j = 0;
        for (ECPoint p : ps) {
            ECPoint q = pointMap.map(p);
            i2 = j + 1;
            pqs[j] = p;
            j = i2 + 1;
            pqs[i2] = q;
        }
        return implSumOfMultiplies(pqs, abs);
    }

    static ECPoint implSumOfMultiplies(ECPoint[] ps, ECPointMap pointMap, BigInteger[] ks) {
        int halfCount = ps.length;
        int fullCount = halfCount << 1;
        boolean[] negs = new boolean[fullCount];
        WNafPreCompInfo[] infos = new WNafPreCompInfo[fullCount];
        byte[][] wnafs = new byte[fullCount][];
        for (int i = 0; i < halfCount; i++) {
            int j0 = i << 1;
            int j1 = j0 + 1;
            BigInteger kj0 = ks[j0];
            negs[j0] = kj0.signum() < 0;
            kj0 = kj0.abs();
            BigInteger kj1 = ks[j1];
            negs[j1] = kj1.signum() < 0;
            kj1 = kj1.abs();
            int width = Math.max(2, Math.min(16, WNafUtil.getWindowSize(Math.max(kj0.bitLength(), kj1.bitLength()))));
            ECPoint P = ps[i];
            ECPoint Q = WNafUtil.mapPointWithPrecomp(P, width, true, pointMap);
            infos[j0] = WNafUtil.getWNafPreCompInfo(P);
            infos[j1] = WNafUtil.getWNafPreCompInfo(Q);
            wnafs[j0] = WNafUtil.generateWindowNaf(width, kj0);
            wnafs[j1] = WNafUtil.generateWindowNaf(width, kj1);
        }
        return implSumOfMultiplies(negs, infos, wnafs);
    }

    private static ECPoint implSumOfMultiplies(boolean[] negs, WNafPreCompInfo[] infos, byte[][] wnafs) {
        int len = 0;
        for (byte[] length : wnafs) {
            len = Math.max(len, length.length);
        }
        ECPoint infinity = infos[0].getPreComp()[0].getCurve().getInfinity();
        ECPoint R = infinity;
        int zeroes = 0;
        int i = len - 1;
        while (i >= 0) {
            ECPoint r = infinity;
            for (int j = 0; j < count; j++) {
                byte[] wnaf = wnafs[j];
                int wi = i < wnaf.length ? wnaf[i] : 0;
                if (wi != 0) {
                    int n = Math.abs(wi);
                    WNafPreCompInfo info = infos[j];
                    r = r.add(((wi < 0) == negs[j] ? info.getPreComp() : info.getPreCompNeg())[n >>> 1]);
                }
            }
            if (r == infinity) {
                zeroes++;
            } else {
                if (zeroes > 0) {
                    R = R.timesPow2(zeroes);
                    zeroes = 0;
                }
                R = R.twicePlus(r);
            }
            i--;
        }
        if (zeroes > 0) {
            return R.timesPow2(zeroes);
        }
        return R;
    }
}
