package com.android.org.bouncycastle.math.ec;

import java.math.BigInteger;

public abstract class WNafUtil {
    private static final int[] DEFAULT_WINDOW_SIZE_CUTOFFS = null;
    private static final byte[] EMPTY_BYTES = null;
    private static final int[] EMPTY_INTS = null;
    private static final ECPoint[] EMPTY_POINTS = null;
    public static final String PRECOMP_NAME = "bc_wnaf";

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.math.ec.WNafUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.math.ec.WNafUtil.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.math.ec.WNafUtil.<clinit>():void");
    }

    public static int[] generateCompactNaf(BigInteger k) {
        if ((k.bitLength() >>> 16) != 0) {
            throw new IllegalArgumentException("'k' must have bitlength < 2^16");
        } else if (k.signum() == 0) {
            return EMPTY_INTS;
        } else {
            int length;
            BigInteger _3k = k.shiftLeft(1).add(k);
            int bits = _3k.bitLength();
            int[] naf = new int[(bits >> 1)];
            BigInteger diff = _3k.xor(k);
            int highBit = bits - 1;
            int zeroes = 0;
            int i = 1;
            int length2 = 0;
            while (i < highBit) {
                if (diff.testBit(i)) {
                    int digit;
                    if (k.testBit(i)) {
                        digit = -1;
                    } else {
                        digit = 1;
                    }
                    length = length2 + 1;
                    naf[length2] = (digit << 16) | zeroes;
                    zeroes = 1;
                    i++;
                } else {
                    zeroes++;
                    length = length2;
                }
                i++;
                length2 = length;
            }
            length = length2 + 1;
            naf[length2] = 65536 | zeroes;
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
            boolean carry = false;
            int length = 0;
            int pos = 0;
            while (pos <= k.bitLength()) {
                if (k.testBit(pos) == carry) {
                    pos++;
                } else {
                    int zeroes;
                    k = k.shiftRight(pos);
                    int digit = k.intValue() & mask;
                    if (carry) {
                        digit++;
                    }
                    if ((digit & sign) != 0) {
                        carry = true;
                    } else {
                        carry = false;
                    }
                    if (carry) {
                        digit -= pow2;
                    }
                    if (length > 0) {
                        zeroes = pos - 1;
                    } else {
                        zeroes = pos;
                    }
                    int length2 = length + 1;
                    wnaf[length] = (digit << 16) | zeroes;
                    pos = width;
                    length = length2;
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
        int d0 = 0;
        int d1 = 0;
        int offset = 0;
        int j = 0;
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
            if ((d0 << 1) == u0 + 1) {
                d0 ^= 1;
            }
            if ((d1 << 1) == u1 + 1) {
                d1 ^= 1;
            }
            offset++;
            if (offset == 30) {
                offset = 0;
                k0 = k0.shiftRight(30);
                k1 = k1.shiftRight(30);
            }
            int j2 = j + 1;
            jsf[j] = (byte) ((u0 << 4) | (u1 & 15));
            j = j2;
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
                int i2;
                int i3 = i - 1;
                if (k.testBit(i)) {
                    i2 = -1;
                } else {
                    i2 = 1;
                }
                naf[i3] = (byte) i2;
                i++;
            }
            i++;
        }
        naf[digits - 1] = (byte) 1;
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
            boolean carry = false;
            int length = 0;
            int pos = 0;
            while (pos <= k.bitLength()) {
                if (k.testBit(pos) == carry) {
                    pos++;
                } else {
                    k = k.shiftRight(pos);
                    int digit = k.intValue() & mask;
                    if (carry) {
                        digit++;
                    }
                    if ((digit & sign) != 0) {
                        carry = true;
                    } else {
                        carry = false;
                    }
                    if (carry) {
                        digit -= pow2;
                    }
                    if (length > 0) {
                        pos--;
                    }
                    length += pos;
                    int length2 = length + 1;
                    wnaf[length] = (byte) digit;
                    pos = width;
                    length = length2;
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
        int i;
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
        for (i = 0; i < preCompP.length; i++) {
            preCompQ[i] = pointMap.map(preCompP[i]);
        }
        wnafPreCompQ.setPreComp(preCompQ);
        if (includeNegated) {
            ECPoint[] preCompNegQ = new ECPoint[preCompQ.length];
            for (i = 0; i < preCompNegQ.length; i++) {
                preCompNegQ[i] = preCompQ[i].negate();
            }
            wnafPreCompQ.setPreCompNeg(preCompNegQ);
        }
        c.setPreCompInfo(q, PRECOMP_NAME, wnafPreCompQ);
        return q;
    }

    public static com.android.org.bouncycastle.math.ec.WNafPreCompInfo precompute(com.android.org.bouncycastle.math.ec.ECPoint r17, int r18, boolean r19) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r1 = r17.getCurve();
        r15 = "bc_wnaf";
        r0 = r17;
        r15 = r1.getPreCompInfo(r0, r15);
        r14 = getWNafPreCompInfo(r15);
        r4 = 0;
        r15 = r18 + -2;
        r16 = 0;
        r0 = r16;
        r15 = java.lang.Math.max(r0, r15);
        r16 = 1;
        r12 = r16 << r15;
        r10 = r14.getPreComp();
        if (r10 != 0) goto L_0x0054;
    L_0x0026:
        r10 = EMPTY_POINTS;
    L_0x0028:
        if (r4 >= r12) goto L_0x0039;
    L_0x002a:
        r10 = resizeTable(r10, r12);
        r15 = 1;
        if (r12 != r15) goto L_0x0056;
    L_0x0031:
        r15 = r17.normalize();
        r16 = 0;
        r10[r16] = r15;
    L_0x0039:
        r14.setPreComp(r10);
        if (r19 == 0) goto L_0x00e6;
    L_0x003e:
        r11 = r14.getPreCompNeg();
        if (r11 != 0) goto L_0x00da;
    L_0x0044:
        r9 = 0;
        r11 = new com.android.org.bouncycastle.math.ec.ECPoint[r12];
    L_0x0047:
        if (r9 >= r12) goto L_0x00e3;
    L_0x0049:
        r15 = r10[r9];
        r15 = r15.negate();
        r11[r9] = r15;
        r9 = r9 + 1;
        goto L_0x0047;
    L_0x0054:
        r4 = r10.length;
        goto L_0x0028;
    L_0x0056:
        r2 = r4;
        if (r4 != 0) goto L_0x005d;
    L_0x0059:
        r15 = 0;
        r10[r15] = r17;
        r2 = 1;
    L_0x005d:
        r5 = 0;
        r15 = 2;
        if (r12 != r15) goto L_0x006f;
    L_0x0061:
        r15 = r17.threeTimes();
        r16 = 1;
        r10[r16] = r15;
    L_0x0069:
        r15 = r12 - r4;
        r1.normalizeAll(r10, r4, r15, r5);
        goto L_0x0039;
    L_0x006f:
        r13 = r14.getTwice();
        r15 = r2 + -1;
        r8 = r10[r15];
        if (r13 != 0) goto L_0x009a;
    L_0x0079:
        r15 = 0;
        r15 = r10[r15];
        r13 = r15.twice();
        r14.setTwice(r13);
        r15 = com.android.org.bouncycastle.math.ec.ECAlgorithms.isFpCurve(r1);
        if (r15 == 0) goto L_0x00a6;
    L_0x0089:
        r15 = r1.getFieldSize();
        r16 = 64;
        r0 = r16;
        if (r15 < r0) goto L_0x00a6;
    L_0x0093:
        r15 = r1.getCoordinateSystem();
        switch(r15) {
            case 2: goto L_0x00a8;
            case 3: goto L_0x00a8;
            case 4: goto L_0x00a8;
            default: goto L_0x009a;
        };
    L_0x009a:
        r3 = r2;
    L_0x009b:
        if (r3 >= r12) goto L_0x00ef;
    L_0x009d:
        r2 = r3 + 1;
        r8 = r8.add(r13);
        r10[r3] = r8;
        goto L_0x009a;
    L_0x00a6:
        r3 = r2;
        goto L_0x009b;
    L_0x00a8:
        r15 = 0;
        r5 = r13.getZCoord(r15);
        r15 = r13.getXCoord();
        r15 = r15.toBigInteger();
        r16 = r13.getYCoord();
        r16 = r16.toBigInteger();
        r0 = r16;
        r13 = r1.createPoint(r15, r0);
        r6 = r5.square();
        r7 = r6.multiply(r5);
        r15 = r8.scaleX(r6);
        r8 = r15.scaleY(r7);
        if (r4 != 0) goto L_0x00d8;
    L_0x00d5:
        r15 = 0;
        r10[r15] = r8;
    L_0x00d8:
        r3 = r2;
        goto L_0x009b;
    L_0x00da:
        r9 = r11.length;
        if (r9 >= r12) goto L_0x0047;
    L_0x00dd:
        r11 = resizeTable(r11, r12);
        goto L_0x0047;
    L_0x00e3:
        r14.setPreCompNeg(r11);
    L_0x00e6:
        r15 = "bc_wnaf";
        r0 = r17;
        r1.setPreCompInfo(r0, r15, r14);
        return r14;
    L_0x00ef:
        r2 = r3;
        goto L_0x0069;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.math.ec.WNafUtil.precompute(com.android.org.bouncycastle.math.ec.ECPoint, int, boolean):com.android.org.bouncycastle.math.ec.WNafPreCompInfo");
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
