package org.bouncycastle.math.ec.rfc8032;

import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.math.ec.rfc7748.X25519Field;
import org.bouncycastle.math.raw.Interleave;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.Arrays;

public abstract class Ed25519 {
    private static final int[] B_x = {52811034, 25909283, 8072341, 50637101, 13785486, 30858332, 20483199, 20966410, 43936626, 4379245};
    private static final int[] B_y = {40265304, 26843545, 6710886, 53687091, 13421772, 40265318, 26843545, 6710886, 53687091, 13421772};
    private static final int[] C_d = {56195235, 47411844, 25868126, 40503822, 57364, 58321048, 30416477, 31930572, 57760639, 10749657};
    private static final int[] C_d2 = {45281625, 27714825, 18181821, 13898781, 114729, 49533232, 60832955, 30306712, 48412415, 4722099};
    private static final int[] C_d4 = {23454386, 55429651, 2809210, 27797563, 229458, 31957600, 54557047, 27058993, 29715967, 9444199};
    private static final int[] L = {1559614445, 1477600026, -1560830762, 350157278, 0, 0, 0, 268435456};
    private static final int L0 = -50998291;
    private static final int L1 = 19280294;
    private static final int L2 = 127719000;
    private static final int L3 = -6428113;
    private static final int L4 = 5343;
    private static final long M28L = 268435455;
    private static final long M32L = 4294967295L;
    private static final int[] P = {-19, -1, -1, -1, -1, -1, -1, Integer.MAX_VALUE};
    private static final int POINT_BYTES = 32;
    private static final int PRECOMP_BLOCKS = 8;
    private static final int PRECOMP_MASK = 7;
    private static final int PRECOMP_POINTS = 8;
    private static final int PRECOMP_SPACING = 8;
    private static final int PRECOMP_TEETH = 4;
    public static final int PUBLIC_KEY_SIZE = 32;
    private static final int SCALAR_BYTES = 32;
    private static final int SCALAR_INTS = 8;
    public static final int SECRET_KEY_SIZE = 32;
    public static final int SIGNATURE_SIZE = 64;
    private static final int WNAF_WIDTH_BASE = 7;
    private static int[] precompBase = null;
    private static PointExt[] precompBaseTable = null;

    private static class PointExt {
        int[] t;
        int[] x;
        int[] y;
        int[] z;

        private PointExt() {
            this.x = X25519Field.create();
            this.y = X25519Field.create();
            this.z = X25519Field.create();
            this.t = X25519Field.create();
        }
    }

    private static class PointPrecomp {
        int[] xyd;
        int[] ymx_h;
        int[] ypx_h;

        private PointPrecomp() {
            this.ypx_h = X25519Field.create();
            this.ymx_h = X25519Field.create();
            this.xyd = X25519Field.create();
        }
    }

    private static byte[] calculateS(byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int[] iArr = new int[16];
        decodeScalar(bArr, 0, iArr);
        int[] iArr2 = new int[8];
        decodeScalar(bArr2, 0, iArr2);
        int[] iArr3 = new int[8];
        decodeScalar(bArr3, 0, iArr3);
        Nat256.mulAddTo(iArr2, iArr3, iArr);
        byte[] bArr4 = new byte[64];
        for (int i = 0; i < iArr.length; i++) {
            encode32(iArr[i], bArr4, i * 4);
        }
        return reduceScalar(bArr4);
    }

    private static boolean checkPointVar(byte[] bArr) {
        int[] iArr = new int[8];
        decode32(bArr, 0, iArr, 0, 8);
        iArr[7] = iArr[7] & Integer.MAX_VALUE;
        return !Nat256.gte(iArr, P);
    }

    private static boolean checkScalarVar(byte[] bArr) {
        int[] iArr = new int[8];
        decodeScalar(bArr, 0, iArr);
        return !Nat256.gte(iArr, L);
    }

    private static int decode24(byte[] bArr, int i) {
        int i2 = i + 1;
        return ((bArr[i2 + 1] & 255) << Tnaf.POW_2_WIDTH) | (bArr[i] & 255) | ((bArr[i2] & 255) << 8);
    }

    private static int decode32(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = i2 + 1;
        return (bArr[i3 + 1] << 24) | (bArr[i] & 255) | ((bArr[i2] & 255) << 8) | ((bArr[i3] & 255) << Tnaf.POW_2_WIDTH);
    }

    private static void decode32(byte[] bArr, int i, int[] iArr, int i2, int i3) {
        for (int i4 = 0; i4 < i3; i4++) {
            iArr[i2 + i4] = decode32(bArr, (i4 * 4) + i);
        }
    }

    private static boolean decodePointVar(byte[] bArr, int i, boolean z, PointExt pointExt) {
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i, i + 32);
        boolean z2 = false;
        if (!checkPointVar(copyOfRange)) {
            return false;
        }
        int i2 = (copyOfRange[31] & 128) >>> 7;
        copyOfRange[31] = (byte) (copyOfRange[31] & Byte.MAX_VALUE);
        X25519Field.decode(copyOfRange, 0, pointExt.y);
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        X25519Field.sqr(pointExt.y, create);
        X25519Field.mul(C_d, create, create2);
        X25519Field.subOne(create);
        X25519Field.addOne(create2);
        if (!X25519Field.sqrtRatioVar(create, create2, pointExt.x)) {
            return false;
        }
        X25519Field.normalize(pointExt.x);
        if (i2 == 1 && X25519Field.isZeroVar(pointExt.x)) {
            return false;
        }
        if (i2 != (pointExt.x[0] & 1)) {
            z2 = true;
        }
        if (z ^ z2) {
            X25519Field.negate(pointExt.x, pointExt.x);
        }
        pointExtendXY(pointExt);
        return true;
    }

    private static void decodeScalar(byte[] bArr, int i, int[] iArr) {
        decode32(bArr, i, iArr, 0, 8);
    }

    private static void encode24(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) i;
        int i3 = i2 + 1;
        bArr[i3] = (byte) (i >>> 8);
        bArr[i3 + 1] = (byte) (i >>> 16);
    }

    private static void encode32(int i, byte[] bArr, int i2) {
        bArr[i2] = (byte) i;
        int i3 = i2 + 1;
        bArr[i3] = (byte) (i >>> 8);
        int i4 = i3 + 1;
        bArr[i4] = (byte) (i >>> 16);
        bArr[i4 + 1] = (byte) (i >>> 24);
    }

    private static void encode56(long j, byte[] bArr, int i) {
        encode32((int) j, bArr, i);
        encode24((int) (j >>> 32), bArr, i + 4);
    }

    private static void encodePoint(PointExt pointExt, byte[] bArr, int i) {
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        X25519Field.inv(pointExt.z, create2);
        X25519Field.mul(pointExt.x, create2, create);
        X25519Field.mul(pointExt.y, create2, create2);
        X25519Field.normalize(create);
        X25519Field.normalize(create2);
        X25519Field.encode(create2, bArr, i);
        int i2 = (i + 32) - 1;
        bArr[i2] = (byte) (bArr[i2] | ((create[0] & 1) << 7));
    }

    public static void generatePublicKey(byte[] bArr, int i, byte[] bArr2, int i2) {
        SHA512Digest sHA512Digest = new SHA512Digest();
        byte[] bArr3 = new byte[sHA512Digest.getDigestSize()];
        sHA512Digest.update(bArr, i, 32);
        sHA512Digest.doFinal(bArr3, 0);
        byte[] bArr4 = new byte[32];
        pruneScalar(bArr3, 0, bArr4);
        scalarMultBaseEncoded(bArr4, bArr2, i2);
    }

    private static byte[] getWNAF(int[] iArr, int i) {
        int i2;
        int[] iArr2 = new int[16];
        int length = iArr2.length;
        int i3 = 0;
        int i4 = 8;
        int i5 = 0;
        while (true) {
            i4--;
            if (i4 < 0) {
                break;
            }
            int i6 = iArr[i4];
            int i7 = length - 1;
            iArr2[i7] = (i5 << 16) | (i6 >>> 16);
            length = i7 - 1;
            iArr2[length] = i6;
            i5 = i6;
        }
        byte[] bArr = new byte[256];
        int i8 = 1 << i;
        int i9 = i8 - 1;
        int i10 = i8 >>> 1;
        int i11 = 0;
        int i12 = 0;
        while (i3 < iArr2.length) {
            int i13 = iArr2[i3];
            while (i2 < 16) {
                int i14 = i13 >>> i2;
                if ((i14 & 1) == i12) {
                    i2++;
                } else {
                    int i15 = (i14 & i9) + i12;
                    int i16 = i15 & i10;
                    int i17 = i15 - (i16 << 1);
                    i12 = i16 >>> (i - 1);
                    bArr[(i3 << 4) + i2] = (byte) i17;
                    i2 += i;
                }
            }
            i3++;
            i11 = i2 - 16;
        }
        return bArr;
    }

    private static void implSign(SHA512Digest sHA512Digest, byte[] bArr, byte[] bArr2, byte[] bArr3, int i, byte[] bArr4, int i2, int i3, byte[] bArr5, int i4) {
        sHA512Digest.update(bArr, 32, 32);
        sHA512Digest.update(bArr4, i2, i3);
        sHA512Digest.doFinal(bArr, 0);
        byte[] reduceScalar = reduceScalar(bArr);
        byte[] bArr6 = new byte[32];
        scalarMultBaseEncoded(reduceScalar, bArr6, 0);
        sHA512Digest.update(bArr6, 0, 32);
        sHA512Digest.update(bArr3, 0, 32);
        sHA512Digest.update(bArr4, i2, i3);
        sHA512Digest.doFinal(bArr, 0);
        byte[] calculateS = calculateS(reduceScalar, reduceScalar(bArr), bArr2);
        System.arraycopy(bArr6, 0, bArr5, i4, 32);
        System.arraycopy(calculateS, 0, bArr5, i4 + 32, 32);
    }

    private static void pointAddPrecomp(PointPrecomp pointPrecomp, PointExt pointExt) {
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        int[] create3 = X25519Field.create();
        int[] create4 = X25519Field.create();
        int[] create5 = X25519Field.create();
        int[] create6 = X25519Field.create();
        int[] create7 = X25519Field.create();
        X25519Field.apm(pointExt.y, pointExt.x, create2, create);
        X25519Field.mul(create, pointPrecomp.ymx_h, create);
        X25519Field.mul(create2, pointPrecomp.ypx_h, create2);
        X25519Field.mul(pointExt.t, pointPrecomp.xyd, create3);
        X25519Field.apm(create2, create, create7, create4);
        X25519Field.apm(pointExt.z, create3, create6, create5);
        X25519Field.carry(create6);
        X25519Field.mul(create4, create5, pointExt.x);
        X25519Field.mul(create6, create7, pointExt.y);
        X25519Field.mul(create5, create6, pointExt.z);
        X25519Field.mul(create4, create7, pointExt.t);
    }

    private static void pointAddVar(boolean z, PointExt pointExt, PointExt pointExt2) {
        int[] iArr;
        int[] iArr2;
        int[] iArr3;
        int[] iArr4;
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        int[] create3 = X25519Field.create();
        int[] create4 = X25519Field.create();
        int[] create5 = X25519Field.create();
        int[] create6 = X25519Field.create();
        int[] create7 = X25519Field.create();
        int[] create8 = X25519Field.create();
        if (z) {
            iArr = create3;
            iArr4 = create4;
            iArr3 = create6;
            iArr2 = create7;
        } else {
            iArr4 = create3;
            iArr = create4;
            iArr2 = create6;
            iArr3 = create7;
        }
        X25519Field.apm(pointExt2.y, pointExt2.x, create2, create);
        X25519Field.apm(pointExt.y, pointExt.x, iArr, iArr4);
        X25519Field.mul(create, create3, create);
        X25519Field.mul(create2, create4, create2);
        X25519Field.mul(pointExt2.t, pointExt.t, create3);
        X25519Field.mul(create3, C_d2, create3);
        X25519Field.mul(pointExt2.z, pointExt.z, create4);
        X25519Field.add(create4, create4, create4);
        X25519Field.apm(create2, create, create8, create5);
        X25519Field.apm(create4, create3, iArr3, iArr2);
        X25519Field.carry(iArr3);
        X25519Field.mul(create5, create6, pointExt2.x);
        X25519Field.mul(create7, create8, pointExt2.y);
        X25519Field.mul(create6, create7, pointExt2.z);
        X25519Field.mul(create5, create8, pointExt2.t);
    }

    private static PointExt pointCopy(PointExt pointExt) {
        PointExt pointExt2 = new PointExt();
        X25519Field.copy(pointExt.x, 0, pointExt2.x, 0);
        X25519Field.copy(pointExt.y, 0, pointExt2.y, 0);
        X25519Field.copy(pointExt.z, 0, pointExt2.z, 0);
        X25519Field.copy(pointExt.t, 0, pointExt2.t, 0);
        return pointExt2;
    }

    private static void pointDouble(PointExt pointExt) {
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        int[] create3 = X25519Field.create();
        int[] create4 = X25519Field.create();
        int[] create5 = X25519Field.create();
        int[] create6 = X25519Field.create();
        int[] create7 = X25519Field.create();
        X25519Field.sqr(pointExt.x, create);
        X25519Field.sqr(pointExt.y, create2);
        X25519Field.sqr(pointExt.z, create3);
        X25519Field.add(create3, create3, create3);
        X25519Field.apm(create, create2, create7, create6);
        X25519Field.add(pointExt.x, pointExt.y, create4);
        X25519Field.sqr(create4, create4);
        X25519Field.sub(create7, create4, create4);
        X25519Field.add(create3, create6, create5);
        X25519Field.carry(create5);
        X25519Field.mul(create4, create5, pointExt.x);
        X25519Field.mul(create6, create7, pointExt.y);
        X25519Field.mul(create5, create6, pointExt.z);
        X25519Field.mul(create4, create7, pointExt.t);
    }

    private static void pointExtendXY(PointExt pointExt) {
        X25519Field.one(pointExt.z);
        X25519Field.mul(pointExt.x, pointExt.y, pointExt.t);
    }

    private static void pointLookup(int i, int i2, PointPrecomp pointPrecomp) {
        int i3 = i * 8 * 3 * 10;
        for (int i4 = 0; i4 < 8; i4++) {
            int i5 = ((i4 ^ i2) - 1) >> 31;
            Nat.cmov(10, i5, precompBase, i3, pointPrecomp.ypx_h, 0);
            int i6 = i3 + 10;
            int i7 = i5;
            Nat.cmov(10, i7, precompBase, i6, pointPrecomp.ymx_h, 0);
            int i8 = i6 + 10;
            Nat.cmov(10, i7, precompBase, i8, pointPrecomp.xyd, 0);
            i3 = i8 + 10;
        }
    }

    private static PointExt[] pointPrecompVar(PointExt pointExt, int i) {
        PointExt pointCopy = pointCopy(pointExt);
        pointDouble(pointCopy);
        PointExt[] pointExtArr = new PointExt[i];
        pointExtArr[0] = pointCopy(pointExt);
        for (int i2 = 1; i2 < i; i2++) {
            pointExtArr[i2] = pointCopy(pointExtArr[i2 - 1]);
            pointAddVar(false, pointCopy, pointExtArr[i2]);
        }
        return pointExtArr;
    }

    private static void pointSetNeutral(PointExt pointExt) {
        X25519Field.zero(pointExt.x);
        X25519Field.one(pointExt.y);
        X25519Field.one(pointExt.z);
        X25519Field.zero(pointExt.t);
    }

    public static synchronized void precompute() {
        synchronized (Ed25519.class) {
            if (precompBase == null) {
                PointExt pointExt = new PointExt();
                X25519Field.copy(B_x, 0, pointExt.x, 0);
                X25519Field.copy(B_y, 0, pointExt.y, 0);
                pointExtendXY(pointExt);
                precompBaseTable = pointPrecompVar(pointExt, 32);
                precompBase = new int[1920];
                int i = 0;
                int i2 = 0;
                while (i < 8) {
                    PointExt[] pointExtArr = new PointExt[4];
                    PointExt pointExt2 = new PointExt();
                    pointSetNeutral(pointExt2);
                    int i3 = 0;
                    while (true) {
                        if (i3 >= 4) {
                            break;
                        }
                        pointAddVar(true, pointExt, pointExt2);
                        pointDouble(pointExt);
                        pointExtArr[i3] = pointCopy(pointExt);
                        for (int i4 = 1; i4 < 8; i4++) {
                            pointDouble(pointExt);
                        }
                        i3++;
                    }
                    PointExt[] pointExtArr2 = new PointExt[8];
                    pointExtArr2[0] = pointExt2;
                    int i5 = 0;
                    int i6 = 1;
                    while (i5 < 3) {
                        int i7 = 1 << i5;
                        int i8 = i6;
                        int i9 = 0;
                        while (i9 < i7) {
                            pointExtArr2[i8] = pointCopy(pointExtArr2[i8 - i7]);
                            pointAddVar(false, pointExtArr[i5], pointExtArr2[i8]);
                            i9++;
                            i8++;
                        }
                        i5++;
                        i6 = i8;
                    }
                    int i10 = i2;
                    for (int i11 = 0; i11 < 8; i11++) {
                        PointExt pointExt3 = pointExtArr2[i11];
                        int[] create = X25519Field.create();
                        int[] create2 = X25519Field.create();
                        X25519Field.add(pointExt3.z, pointExt3.z, create);
                        X25519Field.inv(create, create2);
                        X25519Field.mul(pointExt3.x, create2, create);
                        X25519Field.mul(pointExt3.y, create2, create2);
                        PointPrecomp pointPrecomp = new PointPrecomp();
                        X25519Field.apm(create2, create, pointPrecomp.ypx_h, pointPrecomp.ymx_h);
                        X25519Field.mul(create, create2, pointPrecomp.xyd);
                        X25519Field.mul(pointPrecomp.xyd, C_d4, pointPrecomp.xyd);
                        X25519Field.normalize(pointPrecomp.ypx_h);
                        X25519Field.normalize(pointPrecomp.ymx_h);
                        X25519Field.copy(pointPrecomp.ypx_h, 0, precompBase, i10);
                        int i12 = i10 + 10;
                        X25519Field.copy(pointPrecomp.ymx_h, 0, precompBase, i12);
                        int i13 = i12 + 10;
                        X25519Field.copy(pointPrecomp.xyd, 0, precompBase, i13);
                        i10 = i13 + 10;
                    }
                    i++;
                    i2 = i10;
                }
            }
        }
    }

    private static void pruneScalar(byte[] bArr, int i, byte[] bArr2) {
        System.arraycopy(bArr, i, bArr2, 0, 32);
        bArr2[0] = (byte) (bArr2[0] & 248);
        bArr2[31] = (byte) (bArr2[31] & Byte.MAX_VALUE);
        bArr2[31] = (byte) (bArr2[31] | 64);
    }

    private static byte[] reduceScalar(byte[] bArr) {
        byte[] bArr2 = bArr;
        long decode24 = ((long) (decode24(bArr2, 4) << 4)) & 4294967295L;
        long decode32 = ((long) decode32(bArr2, 7)) & 4294967295L;
        long decode242 = ((long) (decode24(bArr2, 11) << 4)) & 4294967295L;
        long decode322 = ((long) decode32(bArr2, 14)) & 4294967295L;
        long decode243 = ((long) (decode24(bArr2, 18) << 4)) & 4294967295L;
        long decode323 = ((long) decode32(bArr2, 21)) & 4294967295L;
        long decode244 = ((long) (decode24(bArr2, 25) << 4)) & 4294967295L;
        long decode324 = ((long) decode32(bArr2, 28)) & 4294967295L;
        long decode325 = ((long) decode32(bArr2, 49)) & 4294967295L;
        long decode245 = ((long) (decode24(bArr2, 53) << 4)) & 4294967295L;
        long decode326 = ((long) decode32(bArr2, 56)) & 4294967295L;
        long decode246 = ((long) (decode24(bArr2, 60) << 4)) & 4294967295L;
        long j = ((long) bArr2[63]) & 255;
        long decode247 = (((long) (decode24(bArr2, 46) << 4)) & 4294967295L) - (j * 5343);
        long j2 = decode246 + (decode326 >> 28);
        long j3 = decode326 & M28L;
        long j4 = decode324 - (j2 * -50998291);
        long decode248 = ((((long) (decode24(bArr2, 32) << 4)) & 4294967295L) - (j * -50998291)) - (j2 * 19280294);
        long decode327 = ((((long) decode32(bArr2, 35)) & 4294967295L) - (j * 19280294)) - (j2 * 127719000);
        long decode328 = ((((long) decode32(bArr2, 42)) & 4294967295L) - (j * -6428113)) - (j2 * 5343);
        long j5 = decode244 - (j3 * -50998291);
        long decode249 = (((((long) (decode24(bArr2, 39) << 4)) & 4294967295L) - (j * 127719000)) - (j2 * -6428113)) - (j3 * 5343);
        long j6 = decode245 + (decode325 >> 28);
        long j7 = decode325 & M28L;
        long j8 = (decode327 - (j3 * -6428113)) - (j6 * 5343);
        long j9 = ((decode248 - (j3 * 127719000)) - (j6 * -6428113)) - (j7 * 5343);
        long j10 = decode247 + (decode328 >> 28);
        long j11 = (decode328 & M28L) + (decode249 >> 28);
        long j12 = decode242 - (j11 * -50998291);
        long j13 = (decode322 - (j10 * -50998291)) - (j11 * 19280294);
        long j14 = ((decode243 - (j7 * -50998291)) - (j10 * 19280294)) - (j11 * 127719000);
        long j15 = (((j5 - (j6 * 19280294)) - (j7 * 127719000)) - (j10 * -6428113)) - (j11 * 5343);
        long j16 = (decode249 & M28L) + (j8 >> 28);
        long j17 = j8 & M28L;
        long j18 = decode32 - (j16 * -50998291);
        long j19 = j12 - (j16 * 19280294);
        long j20 = j13 - (j16 * 127719000);
        long j21 = ((((decode323 - (j6 * -50998291)) - (j7 * 19280294)) - (j10 * 127719000)) - (j11 * -6428113)) - (j16 * 5343);
        long j22 = j17 + (j9 >> 28);
        long j23 = j9 & M28L;
        long j24 = j18 - (j22 * 19280294);
        long j25 = j19 - (j22 * 127719000);
        long j26 = j20 - (j22 * -6428113);
        long j27 = (j14 - (j16 * -6428113)) - (j22 * 5343);
        long j28 = ((((j4 - (j3 * 19280294)) - (j6 * 127719000)) - (j7 * -6428113)) - (j10 * 5343)) + (j15 >> 28);
        long j29 = j15 & M28L;
        long j30 = j28 & M28L;
        long j31 = j30 >>> 27;
        long j32 = j23 + (j28 >> 28) + j31;
        long decode329 = (((long) decode32(bArr2, 0)) & 4294967295L) - (j32 * -50998291);
        long j33 = ((decode24 - (j22 * -50998291)) - (j32 * 19280294)) + (decode329 >> 28);
        long j34 = decode329 & M28L;
        long j35 = (j24 - (j32 * 127719000)) + (j33 >> 28);
        long j36 = j33 & M28L;
        long j37 = (j25 - (j32 * -6428113)) + (j35 >> 28);
        long j38 = j35 & M28L;
        long j39 = (j26 - (j32 * 5343)) + (j37 >> 28);
        long j40 = j37 & M28L;
        long j41 = j27 + (j39 >> 28);
        long j42 = j39 & M28L;
        long j43 = j21 + (j41 >> 28);
        long j44 = j41 & M28L;
        long j45 = j29 + (j43 >> 28);
        long j46 = j43 & M28L;
        long j47 = j30 + (j45 >> 28);
        long j48 = j45 & M28L;
        long j49 = j47 >> 28;
        long j50 = j47 & M28L;
        long j51 = j49 - j31;
        long j52 = j34 + (j51 & -50998291);
        long j53 = j36 + (j51 & 19280294) + (j52 >> 28);
        long j54 = j52 & M28L;
        long j55 = j38 + (j51 & 127719000) + (j53 >> 28);
        long j56 = j53 & M28L;
        long j57 = j40 + (j51 & -6428113) + (j55 >> 28);
        long j58 = j55 & M28L;
        long j59 = j42 + (j51 & 5343) + (j57 >> 28);
        long j60 = j57 & M28L;
        long j61 = j44 + (j59 >> 28);
        long j62 = j59 & M28L;
        long j63 = j46 + (j61 >> 28);
        long j64 = j61 & M28L;
        long j65 = j48 + (j63 >> 28);
        long j66 = j63 & M28L;
        long j67 = j65 & M28L;
        byte[] bArr3 = new byte[32];
        encode56(j54 | (j56 << 28), bArr3, 0);
        encode56((j60 << 28) | j58, bArr3, 7);
        encode56(j62 | (j64 << 28), bArr3, 14);
        encode56(j66 | (j67 << 28), bArr3, 21);
        encode32((int) (j50 + (j65 >> 28)), bArr3, 28);
        return bArr3;
    }

    private static void scalarMultBase(byte[] bArr, PointExt pointExt) {
        precompute();
        pointSetNeutral(pointExt);
        int[] iArr = new int[8];
        decodeScalar(bArr, 0, iArr);
        Nat.cadd(8, (~iArr[0]) & 1, iArr, L, iArr);
        Nat.shiftDownBit(8, iArr, 1);
        for (int i = 0; i < 8; i++) {
            iArr[i] = Interleave.shuffle2(iArr[i]);
        }
        PointPrecomp pointPrecomp = new PointPrecomp();
        int i2 = 28;
        while (true) {
            for (int i3 = 0; i3 < 8; i3++) {
                int i4 = iArr[i3] >>> i2;
                int i5 = (i4 >>> 3) & 1;
                pointLookup(i3, (i4 ^ (-i5)) & 7, pointPrecomp);
                X25519Field.cswap(i5, pointPrecomp.ypx_h, pointPrecomp.ymx_h);
                X25519Field.cnegate(i5, pointPrecomp.xyd);
                pointAddPrecomp(pointPrecomp, pointExt);
            }
            i2 -= 4;
            if (i2 >= 0) {
                pointDouble(pointExt);
            } else {
                return;
            }
        }
    }

    private static void scalarMultBaseEncoded(byte[] bArr, byte[] bArr2, int i) {
        PointExt pointExt = new PointExt();
        scalarMultBase(bArr, pointExt);
        encodePoint(pointExt, bArr2, i);
    }

    private static void scalarMultStraussVar(int[] iArr, int[] iArr2, PointExt pointExt, PointExt pointExt2) {
        precompute();
        byte[] wnaf = getWNAF(iArr, 7);
        byte[] wnaf2 = getWNAF(iArr2, 5);
        PointExt[] pointPrecompVar = pointPrecompVar(pointExt, 8);
        pointSetNeutral(pointExt2);
        int i = 255;
        while (i > 0 && (wnaf[i] | wnaf2[i]) == 0) {
            i--;
        }
        while (true) {
            byte b = wnaf[i];
            boolean z = false;
            if (b != 0) {
                int i2 = b >> 31;
                pointAddVar(i2 != 0, precompBaseTable[(b ^ i2) >>> 1], pointExt2);
            }
            byte b2 = wnaf2[i];
            if (b2 != 0) {
                int i3 = b2 >> 31;
                int i4 = (b2 ^ i3) >>> 1;
                if (i3 != 0) {
                    z = true;
                }
                pointAddVar(z, pointPrecompVar[i4], pointExt2);
            }
            i--;
            if (i >= 0) {
                pointDouble(pointExt2);
            } else {
                return;
            }
        }
    }

    public static void sign(byte[] bArr, int i, byte[] bArr2, int i2, int i3, byte[] bArr3, int i4) {
        SHA512Digest sHA512Digest = new SHA512Digest();
        byte[] bArr4 = new byte[sHA512Digest.getDigestSize()];
        sHA512Digest.update(bArr, i, 32);
        sHA512Digest.doFinal(bArr4, 0);
        byte[] bArr5 = new byte[32];
        pruneScalar(bArr4, 0, bArr5);
        byte[] bArr6 = new byte[32];
        scalarMultBaseEncoded(bArr5, bArr6, 0);
        implSign(sHA512Digest, bArr4, bArr5, bArr6, 0, bArr2, i2, i3, bArr3, i4);
    }

    public static void sign(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3, int i4, byte[] bArr4, int i5) {
        SHA512Digest sHA512Digest = new SHA512Digest();
        byte[] bArr5 = new byte[sHA512Digest.getDigestSize()];
        sHA512Digest.update(bArr, i, 32);
        sHA512Digest.doFinal(bArr5, 0);
        byte[] bArr6 = new byte[32];
        pruneScalar(bArr5, 0, bArr6);
        implSign(sHA512Digest, bArr5, bArr6, bArr2, i2, bArr3, i3, i4, bArr4, i5);
    }

    public static boolean verify(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3, int i4) {
        int i5 = i + 32;
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i, i5);
        byte[] copyOfRange2 = Arrays.copyOfRange(bArr, i5, i + 64);
        if (!checkPointVar(copyOfRange) || !checkScalarVar(copyOfRange2)) {
            return false;
        }
        PointExt pointExt = new PointExt();
        if (!decodePointVar(bArr2, i2, true, pointExt)) {
            return false;
        }
        SHA512Digest sHA512Digest = new SHA512Digest();
        byte[] bArr4 = new byte[sHA512Digest.getDigestSize()];
        sHA512Digest.update(copyOfRange, 0, 32);
        sHA512Digest.update(bArr2, i2, 32);
        sHA512Digest.update(bArr3, i3, i4);
        sHA512Digest.doFinal(bArr4, 0);
        byte[] reduceScalar = reduceScalar(bArr4);
        int[] iArr = new int[8];
        decodeScalar(copyOfRange2, 0, iArr);
        int[] iArr2 = new int[8];
        decodeScalar(reduceScalar, 0, iArr2);
        PointExt pointExt2 = new PointExt();
        scalarMultStraussVar(iArr, iArr2, pointExt, pointExt2);
        byte[] bArr5 = new byte[32];
        encodePoint(pointExt2, bArr5, 0);
        return Arrays.areEqual(bArr5, copyOfRange);
    }
}
