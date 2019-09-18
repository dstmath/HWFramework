package com.android.org.bouncycastle.math.ec;

import java.math.BigInteger;

public abstract class WNafUtil {
    private static final int[] DEFAULT_WINDOW_SIZE_CUTOFFS = {13, 41, 121, 337, 897, 2305};
    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final int[] EMPTY_INTS = new int[0];
    private static final ECPoint[] EMPTY_POINTS = new ECPoint[0];
    public static final String PRECOMP_NAME = "bc_wnaf";

    public static int[] generateCompactNaf(BigInteger k) {
        if ((k.bitLength() >>> 16) != 0) {
            throw new IllegalArgumentException("'k' must have bitlength < 2^16");
        } else if (k.signum() == 0) {
            return EMPTY_INTS;
        } else {
            BigInteger _3k = k.shiftLeft(1).add(k);
            int bits = _3k.bitLength();
            int[] naf = new int[(bits >> 1)];
            BigInteger diff = _3k.xor(k);
            int highBit = bits - 1;
            int zeroes = 0;
            int zeroes2 = 0;
            int i = 1;
            while (i < highBit) {
                if (!diff.testBit(i)) {
                    zeroes++;
                } else {
                    naf[zeroes2] = ((k.testBit(i) ? -1 : 1) << 16) | zeroes;
                    i++;
                    zeroes = 1;
                    zeroes2++;
                }
                i++;
            }
            int length = zeroes2 + 1;
            naf[zeroes2] = 65536 | zeroes;
            if (naf.length > length) {
                naf = trim(naf, length);
            }
            return naf;
        }
    }

    public static int[] generateCompactWindowNaf(int width, BigInteger k) {
        if (width == 2) {
            return generateCompactNaf(k);
        }
        if (width < 2 || width > 16) {
            throw new IllegalArgumentException("'width' must be in the range [2, 16]");
        } else if ((k.bitLength() >>> 16) != 0) {
            throw new IllegalArgumentException("'k' must have bitlength < 2^16");
        } else if (k.signum() == 0) {
            return EMPTY_INTS;
        } else {
            int[] wnaf = new int[((k.bitLength() / width) + 1)];
            int pow2 = 1 << width;
            int mask = pow2 - 1;
            int sign = pow2 >>> 1;
            int length = 0;
            boolean carry = false;
            BigInteger k2 = k;
            int pos = 0;
            while (pos <= k2.bitLength()) {
                if (k2.testBit(pos) == carry) {
                    pos++;
                } else {
                    k2 = k2.shiftRight(pos);
                    int digit = k2.intValue() & mask;
                    if (carry) {
                        digit++;
                    }
                    carry = (digit & sign) != 0;
                    if (carry) {
                        digit -= pow2;
                    }
                    wnaf[length] = (digit << 16) | (length > 0 ? pos - 1 : pos);
                    pos = width;
                    length++;
                }
            }
            if (wnaf.length > length) {
                wnaf = trim(wnaf, length);
            }
            return wnaf;
        }
    }

    public static byte[] generateJSF(BigInteger g, BigInteger h) {
        byte[] jsf = new byte[(Math.max(g.bitLength(), h.bitLength()) + 1)];
        BigInteger k0 = g;
        BigInteger k1 = h;
        int j = 0;
        int d0 = 0;
        int d1 = 0;
        int offset = 0;
        while (true) {
            if ((d0 | d1) == 0 && k0.bitLength() <= offset && k1.bitLength() <= offset) {
                break;
            }
            int n0 = ((k0.intValue() >>> offset) + d0) & 7;
            int n1 = ((k1.intValue() >>> offset) + d1) & 7;
            int u0 = n0 & 1;
            if (u0 != 0) {
                u0 -= n0 & 2;
                if (n0 + u0 == 4 && (n1 & 3) == 2) {
                    u0 = -u0;
                }
            }
            int u1 = n1 & 1;
            if (u1 != 0) {
                u1 -= n1 & 2;
                if (n1 + u1 == 4 && (n0 & 3) == 2) {
                    u1 = -u1;
                }
            }
            if ((d0 << 1) == 1 + u0) {
                d0 ^= 1;
            }
            if ((d1 << 1) == 1 + u1) {
                d1 ^= 1;
            }
            offset++;
            if (offset == 30) {
                offset = 0;
                k0 = k0.shiftRight(30);
                k1 = k1.shiftRight(30);
            }
            jsf[j] = (byte) ((u0 << 4) | (u1 & 15));
            j++;
        }
        if (jsf.length > j) {
            return trim(jsf, j);
        }
        return jsf;
    }

    public static byte[] generateNaf(BigInteger k) {
        if (k.signum() == 0) {
            return EMPTY_BYTES;
        }
        BigInteger _3k = k.shiftLeft(1).add(k);
        int digits = _3k.bitLength() - 1;
        byte[] naf = new byte[digits];
        BigInteger diff = _3k.xor(k);
        int i = 1;
        while (i < digits) {
            if (diff.testBit(i)) {
                naf[i - 1] = (byte) (k.testBit(i) ? -1 : 1);
                i++;
            }
            i++;
        }
        naf[digits - 1] = 1;
        return naf;
    }

    public static byte[] generateWindowNaf(int width, BigInteger k) {
        if (width == 2) {
            return generateNaf(k);
        }
        if (width < 2 || width > 8) {
            throw new IllegalArgumentException("'width' must be in the range [2, 8]");
        } else if (k.signum() == 0) {
            return EMPTY_BYTES;
        } else {
            byte[] wnaf = new byte[(k.bitLength() + 1)];
            int pow2 = 1 << width;
            int mask = pow2 - 1;
            int sign = pow2 >>> 1;
            int length = 0;
            boolean carry = false;
            BigInteger k2 = k;
            int pos = 0;
            while (pos <= k2.bitLength()) {
                if (k2.testBit(pos) == carry) {
                    pos++;
                } else {
                    k2 = k2.shiftRight(pos);
                    int digit = k2.intValue() & mask;
                    if (carry) {
                        digit++;
                    }
                    carry = (digit & sign) != 0;
                    if (carry) {
                        digit -= pow2;
                    }
                    int length2 = length + (length > 0 ? pos - 1 : pos);
                    wnaf[length2] = (byte) digit;
                    pos = width;
                    length = length2 + 1;
                }
            }
            if (wnaf.length > length) {
                wnaf = trim(wnaf, length);
            }
            return wnaf;
        }
    }

    public static int getNafWeight(BigInteger k) {
        if (k.signum() == 0) {
            return 0;
        }
        return k.shiftLeft(1).add(k).xor(k).bitCount();
    }

    public static WNafPreCompInfo getWNafPreCompInfo(ECPoint p) {
        return getWNafPreCompInfo(p.getCurve().getPreCompInfo(p, PRECOMP_NAME));
    }

    public static WNafPreCompInfo getWNafPreCompInfo(PreCompInfo preCompInfo) {
        if (preCompInfo == null || !(preCompInfo instanceof WNafPreCompInfo)) {
            return new WNafPreCompInfo();
        }
        return (WNafPreCompInfo) preCompInfo;
    }

    public static int getWindowSize(int bits) {
        return getWindowSize(bits, DEFAULT_WINDOW_SIZE_CUTOFFS);
    }

    public static int getWindowSize(int bits, int[] windowSizeCutoffs) {
        int w = 0;
        while (w < windowSizeCutoffs.length && bits >= windowSizeCutoffs[w]) {
            w++;
        }
        return w + 2;
    }

    public static ECPoint mapPointWithPrecomp(ECPoint p, int width, boolean includeNegated, ECPointMap pointMap) {
        ECCurve c = p.getCurve();
        WNafPreCompInfo wnafPreCompP = precompute(p, width, includeNegated);
        ECPoint q = pointMap.map(p);
        WNafPreCompInfo wnafPreCompQ = getWNafPreCompInfo(c.getPreCompInfo(q, PRECOMP_NAME));
        ECPoint twiceP = wnafPreCompP.getTwice();
        if (twiceP != null) {
            wnafPreCompQ.setTwice(pointMap.map(twiceP));
        }
        ECPoint[] preCompP = wnafPreCompP.getPreComp();
        ECPoint[] preCompQ = new ECPoint[preCompP.length];
        for (int i = 0; i < preCompP.length; i++) {
            preCompQ[i] = pointMap.map(preCompP[i]);
        }
        wnafPreCompQ.setPreComp(preCompQ);
        if (includeNegated) {
            ECPoint[] preCompNegQ = new ECPoint[preCompQ.length];
            for (int i2 = 0; i2 < preCompNegQ.length; i2++) {
                preCompNegQ[i2] = preCompQ[i2].negate();
            }
            wnafPreCompQ.setPreCompNeg(preCompNegQ);
        }
        c.setPreCompInfo(q, PRECOMP_NAME, wnafPreCompQ);
        return q;
    }

    public static WNafPreCompInfo precompute(ECPoint p, int width, boolean includeNegated) {
        int pos;
        ECCurve c = p.getCurve();
        WNafPreCompInfo wnafPreCompInfo = getWNafPreCompInfo(c.getPreCompInfo(p, PRECOMP_NAME));
        int iniPreCompLen = 0;
        int reqPreCompLen = 1 << Math.max(0, width - 2);
        ECPoint[] preComp = wnafPreCompInfo.getPreComp();
        if (preComp == null) {
            preComp = EMPTY_POINTS;
        } else {
            iniPreCompLen = preComp.length;
        }
        if (iniPreCompLen < reqPreCompLen) {
            preComp = resizeTable(preComp, reqPreCompLen);
            if (reqPreCompLen == 1) {
                preComp[0] = p.normalize();
            } else {
                int curPreCompLen = iniPreCompLen;
                if (curPreCompLen == 0) {
                    preComp[0] = p;
                    curPreCompLen = 1;
                }
                ECFieldElement iso = null;
                if (reqPreCompLen == 2) {
                    preComp[1] = p.threeTimes();
                } else {
                    ECPoint twiceP = wnafPreCompInfo.getTwice();
                    ECPoint last = preComp[curPreCompLen - 1];
                    if (twiceP == null) {
                        twiceP = preComp[0].twice();
                        wnafPreCompInfo.setTwice(twiceP);
                        if (!twiceP.isInfinity() && ECAlgorithms.isFpCurve(c) && c.getFieldSize() >= 64) {
                            switch (c.getCoordinateSystem()) {
                                case 2:
                                case 3:
                                case 4:
                                    iso = twiceP.getZCoord(0);
                                    twiceP = c.createPoint(twiceP.getXCoord().toBigInteger(), twiceP.getYCoord().toBigInteger());
                                    ECFieldElement iso2 = iso.square();
                                    last = last.scaleX(iso2).scaleY(iso2.multiply(iso));
                                    if (iniPreCompLen == 0) {
                                        preComp[0] = last;
                                        break;
                                    }
                                    break;
                            }
                        }
                    }
                    while (curPreCompLen < reqPreCompLen) {
                        ECPoint add = last.add(twiceP);
                        last = add;
                        preComp[curPreCompLen] = add;
                        curPreCompLen++;
                    }
                }
                c.normalizeAll(preComp, iniPreCompLen, reqPreCompLen - iniPreCompLen, iso);
            }
        }
        wnafPreCompInfo.setPreComp(preComp);
        if (includeNegated) {
            ECPoint[] preCompNeg = wnafPreCompInfo.getPreCompNeg();
            if (preCompNeg == null) {
                pos = 0;
                preCompNeg = new ECPoint[reqPreCompLen];
            } else {
                pos = preCompNeg.length;
                if (pos < reqPreCompLen) {
                    preCompNeg = resizeTable(preCompNeg, reqPreCompLen);
                }
            }
            while (pos < reqPreCompLen) {
                preCompNeg[pos] = preComp[pos].negate();
                pos++;
            }
            wnafPreCompInfo.setPreCompNeg(preCompNeg);
        }
        c.setPreCompInfo(p, PRECOMP_NAME, wnafPreCompInfo);
        return wnafPreCompInfo;
    }

    private static byte[] trim(byte[] a, int length) {
        byte[] result = new byte[length];
        System.arraycopy(a, 0, result, 0, result.length);
        return result;
    }

    private static int[] trim(int[] a, int length) {
        int[] result = new int[length];
        System.arraycopy(a, 0, result, 0, result.length);
        return result;
    }

    private static ECPoint[] resizeTable(ECPoint[] a, int length) {
        ECPoint[] result = new ECPoint[length];
        System.arraycopy(a, 0, result, 0, a.length);
        return result;
    }
}
