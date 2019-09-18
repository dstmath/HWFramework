package org.bouncycastle.math.ec.rfc8032;

import org.bouncycastle.crypto.digests.SHAKEDigest;
import org.bouncycastle.crypto.tls.CipherSuite;
import org.bouncycastle.math.ec.rfc7748.X448Field;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public abstract class Ed448 {
    private static final int[] B_x = {118276190, 40534716, 9670182, 135141552, 85017403, 259173222, 68333082, 171784774, 174973732, 15824510, 73756743, 57518561, 94773951, 248652241, 107736333, 82941708};
    private static final int[] B_y = {36764180, 8885695, 130592152, 20104429, 163904957, 30304195, 121295871, 5901357, 125344798, 171541512, 175338348, 209069246, 3626697, 38307682, 24032956, 110359655};
    private static final int C_d = -39081;
    private static final byte[] DOM4_PREFIX = Strings.toByteArray("SigEd448");
    private static final int[] L = {-1420278541, 595116690, -1916432555, 560775794, -1361693040, -1001465015, 2093622249, -1, -1, -1, -1, -1, -1, 1073741823};
    private static final int L4_0 = 43969588;
    private static final int L4_1 = 30366549;
    private static final int L4_2 = 163752818;
    private static final int L4_3 = 258169998;
    private static final int L4_4 = 96434764;
    private static final int L4_5 = 227822194;
    private static final int L4_6 = 149865618;
    private static final int L4_7 = 550336261;
    private static final int L_0 = 78101261;
    private static final int L_1 = 141809365;
    private static final int L_2 = 175155932;
    private static final int L_3 = 64542499;
    private static final int L_4 = 158326419;
    private static final int L_5 = 191173276;
    private static final int L_6 = 104575268;
    private static final int L_7 = 137584065;
    private static final long M26L = 67108863;
    private static final long M28L = 268435455;
    private static final long M32L = 4294967295L;
    private static final int[] P = {-1, -1, -1, -1, -1, -1, -1, -2, -1, -1, -1, -1, -1, -1};
    private static final int POINT_BYTES = 57;
    private static final int PRECOMP_BLOCKS = 5;
    private static final int PRECOMP_MASK = 15;
    private static final int PRECOMP_POINTS = 16;
    private static final int PRECOMP_SPACING = 18;
    private static final int PRECOMP_TEETH = 5;
    public static final int PUBLIC_KEY_SIZE = 57;
    private static final int SCALAR_BYTES = 57;
    private static final int SCALAR_INTS = 14;
    public static final int SECRET_KEY_SIZE = 57;
    public static final int SIGNATURE_SIZE = 114;
    private static final int WNAF_WIDTH_BASE = 7;
    private static int[] precompBase = null;
    private static PointExt[] precompBaseTable = null;

    private static class PointExt {
        int[] x;
        int[] y;
        int[] z;

        private PointExt() {
            this.x = X448Field.create();
            this.y = X448Field.create();
            this.z = X448Field.create();
        }
    }

    private static class PointPrecomp {
        int[] x;
        int[] y;

        private PointPrecomp() {
            this.x = X448Field.create();
            this.y = X448Field.create();
        }
    }

    private static byte[] calculateS(byte[] bArr, byte[] bArr2, byte[] bArr3) {
        int[] iArr = new int[28];
        decodeScalar(bArr, 0, iArr);
        int[] iArr2 = new int[14];
        decodeScalar(bArr2, 0, iArr2);
        int[] iArr3 = new int[14];
        decodeScalar(bArr3, 0, iArr3);
        Nat.mulAddTo(14, iArr2, iArr3, iArr);
        byte[] bArr4 = new byte[SIGNATURE_SIZE];
        for (int i = 0; i < iArr.length; i++) {
            encode32(iArr[i], bArr4, i * 4);
        }
        return reduceScalar(bArr4);
    }

    private static boolean checkContextVar(byte[] bArr) {
        return bArr != null && bArr.length < 256;
    }

    private static boolean checkPointVar(byte[] bArr) {
        if ((bArr[56] & Byte.MAX_VALUE) != 0) {
            return false;
        }
        int[] iArr = new int[14];
        decode32(bArr, 0, iArr, 0, 14);
        return !Nat.gte(14, iArr, P);
    }

    private static boolean checkScalarVar(byte[] bArr) {
        if (bArr[56] != 0) {
            return false;
        }
        int[] iArr = new int[14];
        decodeScalar(bArr, 0, iArr);
        return !Nat.gte(14, iArr, L);
    }

    private static int decode16(byte[] bArr, int i) {
        return ((bArr[i + 1] & 255) << 8) | (bArr[i] & 255);
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
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i, i + 57);
        boolean z2 = false;
        if (!checkPointVar(copyOfRange)) {
            return false;
        }
        int i2 = (copyOfRange[56] & 128) >>> 7;
        copyOfRange[56] = (byte) (copyOfRange[56] & Byte.MAX_VALUE);
        X448Field.decode(copyOfRange, 0, pointExt.y);
        int[] create = X448Field.create();
        int[] create2 = X448Field.create();
        X448Field.sqr(pointExt.y, create);
        X448Field.mul(create, 39081, create2);
        X448Field.negate(create, create);
        X448Field.addOne(create);
        X448Field.addOne(create2);
        if (!X448Field.sqrtRatioVar(create, create2, pointExt.x)) {
            return false;
        }
        X448Field.normalize(pointExt.x);
        if (i2 == 1 && X448Field.isZeroVar(pointExt.x)) {
            return false;
        }
        if (i2 != (pointExt.x[0] & 1)) {
            z2 = true;
        }
        if (z ^ z2) {
            X448Field.negate(pointExt.x, pointExt.x);
        }
        pointExtendXY(pointExt);
        return true;
    }

    private static void decodeScalar(byte[] bArr, int i, int[] iArr) {
        decode32(bArr, i, iArr, 0, 14);
    }

    private static void dom4(SHAKEDigest sHAKEDigest, byte b, byte[] bArr) {
        sHAKEDigest.update(DOM4_PREFIX, 0, DOM4_PREFIX.length);
        sHAKEDigest.update(b);
        sHAKEDigest.update((byte) bArr.length);
        sHAKEDigest.update(bArr, 0, bArr.length);
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
        int[] create = X448Field.create();
        int[] create2 = X448Field.create();
        X448Field.inv(pointExt.z, create2);
        X448Field.mul(pointExt.x, create2, create);
        X448Field.mul(pointExt.y, create2, create2);
        X448Field.normalize(create);
        X448Field.normalize(create2);
        X448Field.encode(create2, bArr, i);
        bArr[(i + 57) - 1] = (byte) ((create[0] & 1) << 7);
    }

    public static void generatePublicKey(byte[] bArr, int i, byte[] bArr2, int i2) {
        SHAKEDigest sHAKEDigest = new SHAKEDigest(256);
        byte[] bArr3 = new byte[SIGNATURE_SIZE];
        sHAKEDigest.update(bArr, i, 57);
        sHAKEDigest.doFinal(bArr3, 0, bArr3.length);
        byte[] bArr4 = new byte[57];
        pruneScalar(bArr3, 0, bArr4);
        scalarMultBaseEncodedVar(bArr4, bArr2, i2);
    }

    private static byte[] getWNAF(int[] iArr, int i) {
        int i2;
        int[] iArr2 = new int[28];
        int length = iArr2.length;
        int i3 = 0;
        int i4 = 14;
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
        byte[] bArr = new byte[448];
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

    private static void implSignVar(SHAKEDigest sHAKEDigest, byte[] bArr, byte[] bArr2, byte[] bArr3, int i, byte[] bArr4, byte[] bArr5, int i2, int i3, byte[] bArr6, int i4) {
        dom4(sHAKEDigest, (byte) 0, bArr4);
        sHAKEDigest.update(bArr, 57, 57);
        sHAKEDigest.update(bArr5, i2, i3);
        sHAKEDigest.doFinal(bArr, 0, bArr.length);
        byte[] reduceScalar = reduceScalar(bArr);
        byte[] bArr7 = new byte[57];
        scalarMultBaseEncodedVar(reduceScalar, bArr7, 0);
        dom4(sHAKEDigest, (byte) 0, bArr4);
        sHAKEDigest.update(bArr7, 0, 57);
        sHAKEDigest.update(bArr3, i, 57);
        sHAKEDigest.update(bArr5, i2, i3);
        sHAKEDigest.doFinal(bArr, 0, bArr.length);
        byte[] calculateS = calculateS(reduceScalar, reduceScalar(bArr), bArr2);
        System.arraycopy(bArr7, 0, bArr6, i4, 57);
        System.arraycopy(calculateS, 0, bArr6, i4 + 57, 57);
    }

    private static void pointAddPrecomp(PointPrecomp pointPrecomp, PointExt pointExt) {
        int[] create = X448Field.create();
        int[] create2 = X448Field.create();
        int[] create3 = X448Field.create();
        int[] create4 = X448Field.create();
        int[] create5 = X448Field.create();
        int[] create6 = X448Field.create();
        int[] create7 = X448Field.create();
        X448Field.sqr(pointExt.z, create);
        X448Field.mul(pointPrecomp.x, pointExt.x, create2);
        X448Field.mul(pointPrecomp.y, pointExt.y, create3);
        X448Field.mul(create2, create3, create4);
        X448Field.mul(create4, 39081, create4);
        X448Field.add(create, create4, create5);
        X448Field.sub(create, create4, create6);
        X448Field.add(pointPrecomp.x, pointPrecomp.y, create);
        X448Field.add(pointExt.x, pointExt.y, create4);
        X448Field.mul(create, create4, create7);
        X448Field.add(create3, create2, create);
        X448Field.sub(create3, create2, create4);
        X448Field.carry(create);
        X448Field.sub(create7, create, create7);
        X448Field.mul(create7, pointExt.z, create7);
        X448Field.mul(create4, pointExt.z, create4);
        X448Field.mul(create5, create7, pointExt.x);
        X448Field.mul(create4, create6, pointExt.y);
        X448Field.mul(create5, create6, pointExt.z);
    }

    private static void pointAddVar(boolean z, PointExt pointExt, PointExt pointExt2) {
        int[] iArr;
        int[] iArr2;
        int[] iArr3;
        int[] iArr4;
        int[] create = X448Field.create();
        int[] create2 = X448Field.create();
        int[] create3 = X448Field.create();
        int[] create4 = X448Field.create();
        int[] create5 = X448Field.create();
        int[] create6 = X448Field.create();
        int[] create7 = X448Field.create();
        int[] create8 = X448Field.create();
        if (z) {
            X448Field.sub(pointExt.y, pointExt.x, create8);
            iArr2 = create2;
            iArr3 = create5;
            iArr4 = create6;
            iArr = create7;
        } else {
            X448Field.add(pointExt.y, pointExt.x, create8);
            iArr3 = create2;
            iArr2 = create5;
            iArr = create6;
            iArr4 = create7;
        }
        X448Field.mul(pointExt.z, pointExt2.z, create);
        X448Field.sqr(create, create2);
        X448Field.mul(pointExt.x, pointExt2.x, create3);
        X448Field.mul(pointExt.y, pointExt2.y, create4);
        X448Field.mul(create3, create4, create5);
        X448Field.mul(create5, 39081, create5);
        X448Field.add(create2, create5, iArr);
        X448Field.sub(create2, create5, iArr4);
        X448Field.add(pointExt2.x, pointExt2.y, create5);
        X448Field.mul(create8, create5, create8);
        X448Field.add(create4, create3, iArr3);
        X448Field.sub(create4, create3, iArr2);
        X448Field.carry(iArr3);
        X448Field.sub(create8, create2, create8);
        X448Field.mul(create8, create, create8);
        X448Field.mul(create5, create, create5);
        X448Field.mul(create6, create8, pointExt2.x);
        X448Field.mul(create5, create7, pointExt2.y);
        X448Field.mul(create6, create7, pointExt2.z);
    }

    private static PointExt pointCopy(PointExt pointExt) {
        PointExt pointExt2 = new PointExt();
        X448Field.copy(pointExt.x, 0, pointExt2.x, 0);
        X448Field.copy(pointExt.y, 0, pointExt2.y, 0);
        X448Field.copy(pointExt.z, 0, pointExt2.z, 0);
        return pointExt2;
    }

    private static void pointDouble(PointExt pointExt) {
        int[] create = X448Field.create();
        int[] create2 = X448Field.create();
        int[] create3 = X448Field.create();
        int[] create4 = X448Field.create();
        int[] create5 = X448Field.create();
        int[] create6 = X448Field.create();
        X448Field.add(pointExt.x, pointExt.y, create);
        X448Field.sqr(create, create);
        X448Field.sqr(pointExt.x, create2);
        X448Field.sqr(pointExt.y, create3);
        X448Field.add(create2, create3, create4);
        X448Field.carry(create4);
        X448Field.sqr(pointExt.z, create5);
        X448Field.add(create5, create5, create5);
        X448Field.carry(create5);
        X448Field.sub(create4, create5, create6);
        X448Field.sub(create, create4, create);
        X448Field.sub(create2, create3, create2);
        X448Field.mul(create, create6, pointExt.x);
        X448Field.mul(create4, create2, pointExt.y);
        X448Field.mul(create4, create6, pointExt.z);
    }

    private static void pointExtendXY(PointExt pointExt) {
        X448Field.one(pointExt.z);
    }

    private static void pointLookup(int i, int i2, PointPrecomp pointPrecomp) {
        int i3 = i * 16 * 2 * 16;
        for (int i4 = 0; i4 < 16; i4++) {
            int i5 = ((i4 ^ i2) - 1) >> 31;
            Nat.cmov(16, i5, precompBase, i3, pointPrecomp.x, 0);
            int i6 = i3 + 16;
            Nat.cmov(16, i5, precompBase, i6, pointPrecomp.y, 0);
            i3 = i6 + 16;
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
        X448Field.zero(pointExt.x);
        X448Field.one(pointExt.y);
        X448Field.one(pointExt.z);
    }

    public static synchronized void precompute() {
        synchronized (Ed448.class) {
            if (precompBase == null) {
                PointExt pointExt = new PointExt();
                X448Field.copy(B_x, 0, pointExt.x, 0);
                X448Field.copy(B_y, 0, pointExt.y, 0);
                pointExtendXY(pointExt);
                precompBaseTable = pointPrecompVar(pointExt, 32);
                precompBase = new int[2560];
                int i = 0;
                int i2 = 0;
                while (i < 5) {
                    PointExt[] pointExtArr = new PointExt[5];
                    PointExt pointExt2 = new PointExt();
                    pointSetNeutral(pointExt2);
                    int i3 = 0;
                    while (true) {
                        if (i3 >= 5) {
                            break;
                        }
                        pointAddVar(true, pointExt, pointExt2);
                        pointDouble(pointExt);
                        pointExtArr[i3] = pointCopy(pointExt);
                        for (int i4 = 1; i4 < 18; i4++) {
                            pointDouble(pointExt);
                        }
                        i3++;
                    }
                    PointExt[] pointExtArr2 = new PointExt[16];
                    pointExtArr2[0] = pointExt2;
                    int i5 = 0;
                    int i6 = 1;
                    while (i5 < 4) {
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
                    for (int i11 = 0; i11 < 16; i11++) {
                        PointExt pointExt3 = pointExtArr2[i11];
                        X448Field.inv(pointExt3.z, pointExt3.z);
                        X448Field.mul(pointExt3.x, pointExt3.z, pointExt3.x);
                        X448Field.mul(pointExt3.y, pointExt3.z, pointExt3.y);
                        X448Field.copy(pointExt3.x, 0, precompBase, i10);
                        int i12 = i10 + 16;
                        X448Field.copy(pointExt3.y, 0, precompBase, i12);
                        i10 = i12 + 16;
                    }
                    i++;
                    i2 = i10;
                }
            }
        }
    }

    private static void pruneScalar(byte[] bArr, int i, byte[] bArr2) {
        System.arraycopy(bArr, i, bArr2, 0, 57);
        bArr2[0] = (byte) (bArr2[0] & 252);
        bArr2[55] = (byte) (bArr2[55] | 128);
        bArr2[56] = (byte) (bArr2[56] & 0);
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
        long decode245 = ((long) (decode24(bArr2, 32) << 4)) & 4294967295L;
        long decode325 = ((long) decode32(bArr2, 35)) & 4294967295L;
        long decode246 = ((long) (decode24(bArr2, 39) << 4)) & 4294967295L;
        long decode326 = ((long) decode32(bArr2, 42)) & 4294967295L;
        long decode247 = ((long) (decode24(bArr2, 46) << 4)) & 4294967295L;
        long decode327 = ((long) decode32(bArr2, 49)) & 4294967295L;
        long decode248 = ((long) (decode24(bArr2, 53) << 4)) & 4294967295L;
        long decode249 = ((long) (decode24(bArr2, 74) << 4)) & 4294967295L;
        long decode328 = ((long) decode32(bArr2, 77)) & 4294967295L;
        long decode2410 = ((long) (decode24(bArr2, 81) << 4)) & 4294967295L;
        long decode329 = ((long) decode32(bArr2, 84)) & 4294967295L;
        long decode2411 = ((long) (decode24(bArr2, 88) << 4)) & 4294967295L;
        long decode3210 = ((long) decode32(bArr2, 91)) & 4294967295L;
        long decode2412 = ((long) (decode24(bArr2, 95) << 4)) & 4294967295L;
        long decode3211 = ((long) decode32(bArr2, 98)) & 4294967295L;
        long decode2413 = ((long) (decode24(bArr2, 102) << 4)) & 4294967295L;
        long decode3212 = ((long) decode32(bArr2, CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA256)) & 4294967295L;
        long decode2414 = ((long) (decode24(bArr2, CipherSuite.TLS_DH_anon_WITH_AES_256_CBC_SHA256) << 4)) & 4294967295L;
        long decode16 = ((long) decode16(bArr2, 112)) & 4294967295L;
        long j = decode2410 + (decode16 * 550336261);
        long j2 = decode2414 + (decode3212 >>> 28);
        long j3 = decode3212 & M28L;
        long decode3213 = (((long) decode32(bArr2, 56)) & 4294967295L) + (decode16 * 43969588) + (j2 * 30366549);
        long decode2415 = (((long) (decode24(bArr2, 60) << 4)) & 4294967295L) + (decode16 * 30366549) + (j2 * 163752818);
        long decode3214 = (((long) decode32(bArr2, 63)) & 4294967295L) + (decode16 * 163752818) + (j2 * 258169998);
        long decode2416 = (((long) (decode24(bArr2, 67) << 4)) & 4294967295L) + (decode16 * 258169998) + (j2 * 96434764);
        long j4 = decode328 + (decode16 * 149865618) + (j2 * 550336261);
        long j5 = decode2413 + (decode3211 >>> 28);
        long j6 = decode3211 & M28L;
        long decode3215 = (((long) decode32(bArr2, 70)) & 4294967295L) + (decode16 * 96434764) + (j2 * 227822194) + (j3 * 149865618) + (j5 * 550336261);
        long j7 = decode2412 + (decode3210 >>> 28);
        long j8 = decode3210 & M28L;
        long j9 = decode3214 + (j3 * 96434764) + (j5 * 227822194) + (j6 * 149865618) + (j7 * 550336261);
        long j10 = decode2415 + (j3 * 258169998) + (j5 * 96434764) + (j6 * 227822194) + (j7 * 149865618) + (j8 * 550336261);
        long j11 = decode2411 + (decode329 >>> 28);
        long j12 = decode329 & M28L;
        long j13 = decode249 + (decode16 * 227822194) + (j2 * 149865618) + (j3 * 550336261) + (decode3215 >>> 28);
        long j14 = decode3215 & M28L;
        long j15 = j4 + (j13 >>> 28);
        long j16 = j13 & M28L;
        long j17 = j + (j15 >>> 28);
        long j18 = j15 & M28L;
        long j19 = j12 + (j17 >>> 28);
        long j20 = j17 & M28L;
        long j21 = decode244 + (j20 * 43969588);
        long j22 = decode324 + (j19 * 43969588) + (j20 * 30366549);
        long j23 = decode245 + (j11 * 43969588) + (j19 * 30366549) + (j20 * 163752818);
        long j24 = decode325 + (j8 * 43969588) + (j11 * 30366549) + (j19 * 163752818) + (j20 * 258169998);
        long j25 = decode246 + (j7 * 43969588) + (j8 * 30366549) + (j11 * 163752818) + (j19 * 258169998) + (j20 * 96434764);
        long j26 = decode326 + (j6 * 43969588) + (j7 * 30366549) + (j8 * 163752818) + (j11 * 258169998) + (j19 * 96434764) + (j20 * 227822194);
        long j27 = decode327 + (j3 * 43969588) + (j5 * 30366549) + (j6 * 163752818) + (j7 * 258169998) + (j8 * 96434764) + (j11 * 227822194) + (j19 * 149865618) + (j20 * 550336261);
        long j28 = j9 + (j10 >>> 28);
        long j29 = j10 & M28L;
        long j30 = decode2416 + (j3 * 227822194) + (j5 * 149865618) + (j6 * 550336261) + (j28 >>> 28);
        long j31 = j28 & M28L;
        long j32 = j14 + (j30 >>> 28);
        long j33 = j30 & M28L;
        long j34 = j16 + (j32 >>> 28);
        long j35 = j32 & M28L;
        long j36 = decode322 + (j35 * 43969588);
        long j37 = decode243 + (j34 * 43969588) + (j35 * 30366549);
        long j38 = decode323 + (j18 * 43969588) + (j34 * 30366549) + (j35 * 163752818);
        long j39 = j21 + (j18 * 30366549) + (j34 * 163752818) + (j35 * 258169998);
        long j40 = j22 + (j18 * 163752818) + (j34 * 258169998) + (j35 * 96434764);
        long j41 = j23 + (j18 * 258169998) + (j34 * 96434764) + (j35 * 227822194);
        long j42 = j25 + (j18 * 227822194) + (j34 * 149865618) + (j35 * 550336261);
        long j43 = decode242 + (j33 * 43969588);
        long j44 = j36 + (j33 * 30366549);
        long j45 = j37 + (j33 * 163752818);
        long j46 = j38 + (j33 * 258169998);
        long j47 = j39 + (j33 * 96434764);
        long j48 = j40 + (j33 * 227822194);
        long j49 = j41 + (j33 * 149865618);
        long j50 = j24 + (j18 * 96434764) + (j34 * 227822194) + (j35 * 149865618) + (j33 * 550336261);
        long j51 = decode248 + (j2 * 43969588) + (j3 * 30366549) + (j5 * 163752818) + (j6 * 258169998) + (j7 * 96434764) + (j8 * 227822194) + (j11 * 149865618) + (j19 * 550336261) + (j27 >>> 28);
        long j52 = j27 & M28L;
        long j53 = decode3213 + (j3 * 163752818) + (j5 * 258169998) + (j6 * 96434764) + (j7 * 227822194) + (j8 * 149865618) + (j11 * 550336261) + (j51 >>> 28);
        long j54 = j51 & M28L;
        long j55 = j29 + (j53 >>> 28);
        long j56 = j53 & M28L;
        long j57 = j31 + (j55 >>> 28);
        long j58 = j55 & M28L;
        long j59 = decode32 + (j57 * 43969588);
        long j60 = j43 + (j57 * 30366549);
        long j61 = j44 + (j57 * 163752818);
        long j62 = j45 + (j57 * 258169998);
        long j63 = j46 + (j57 * 96434764);
        long j64 = j47 + (j57 * 227822194);
        long j65 = j49 + (j57 * 550336261);
        long j66 = j48 + (j57 * 149865618) + (j58 * 550336261);
        long j67 = j54 & M26L;
        long j68 = (j56 * 4) + (j54 >>> 26) + 1;
        long decode3216 = (((long) decode32(bArr2, 0)) & 4294967295L) + (78101261 * j68);
        long j69 = decode24 + (43969588 * j58) + (141809365 * j68) + (decode3216 >>> 28);
        long j70 = decode3216 & M28L;
        long j71 = j59 + (30366549 * j58) + (175155932 * j68) + (j69 >>> 28);
        long j72 = j69 & M28L;
        long j73 = j60 + (163752818 * j58) + (64542499 * j68) + (j71 >>> 28);
        long j74 = j71 & M28L;
        long j75 = j61 + (258169998 * j58) + (158326419 * j68) + (j73 >>> 28);
        long j76 = j73 & M28L;
        long j77 = j62 + (96434764 * j58) + (191173276 * j68) + (j75 >>> 28);
        long j78 = j75 & M28L;
        long j79 = j63 + (227822194 * j58) + (104575268 * j68) + (j77 >>> 28);
        long j80 = j77 & M28L;
        long j81 = j64 + (149865618 * j58) + (j68 * 137584065) + (j79 >>> 28);
        long j82 = j79 & M28L;
        long j83 = j66 + (j81 >>> 28);
        long j84 = j81 & M28L;
        long j85 = j65 + (j83 >>> 28);
        long j86 = j83 & M28L;
        long j87 = j50 + (j85 >>> 28);
        long j88 = j85 & M28L;
        long j89 = j42 + (j87 >>> 28);
        long j90 = j87 & M28L;
        long j91 = j26 + (j18 * 149865618) + (j34 * 550336261) + (j89 >>> 28);
        long j92 = j89 & M28L;
        long j93 = decode247 + (j5 * 43969588) + (j6 * 30366549) + (j7 * 163752818) + (j8 * 258169998) + (j11 * 96434764) + (j19 * 227822194) + (j20 * 149865618) + (j18 * 550336261) + (j91 >>> 28);
        long j94 = j91 & M28L;
        long j95 = j52 + (j93 >>> 28);
        long j96 = j93 & M28L;
        long j97 = j67 + (j95 >>> 28);
        long j98 = j95 & M28L;
        long j99 = j97 >>> 26;
        long j100 = j97 & M26L;
        long j101 = j99 - 1;
        long j102 = j70 - (j101 & 78101261);
        long j103 = (j72 - (j101 & 141809365)) + (j102 >> 28);
        long j104 = j102 & M28L;
        long j105 = (j74 - (j101 & 175155932)) + (j103 >> 28);
        long j106 = j103 & M28L;
        long j107 = (j76 - (j101 & 64542499)) + (j105 >> 28);
        long j108 = j105 & M28L;
        long j109 = (j78 - (j101 & 158326419)) + (j107 >> 28);
        long j110 = j107 & M28L;
        long j111 = (j80 - (j101 & 191173276)) + (j109 >> 28);
        long j112 = j109 & M28L;
        long j113 = (j82 - (j101 & 104575268)) + (j111 >> 28);
        long j114 = j111 & M28L;
        long j115 = (j84 - (j101 & 137584065)) + (j113 >> 28);
        long j116 = j113 & M28L;
        long j117 = j86 + (j115 >> 28);
        long j118 = j115 & M28L;
        long j119 = j88 + (j117 >> 28);
        long j120 = j117 & M28L;
        long j121 = j90 + (j119 >> 28);
        long j122 = j119 & M28L;
        long j123 = j92 + (j121 >> 28);
        long j124 = j121 & M28L;
        long j125 = j94 + (j123 >> 28);
        long j126 = j123 & M28L;
        long j127 = j96 + (j125 >> 28);
        long j128 = j125 & M28L;
        long j129 = j98 + (j127 >> 28);
        long j130 = j127 & M28L;
        long j131 = j129 & M28L;
        byte[] bArr3 = new byte[57];
        encode56((j106 << 28) | j104, bArr3, 0);
        encode56((j110 << 28) | j108, bArr3, 7);
        encode56(j112 | (j114 << 28), bArr3, 14);
        encode56(j116 | (j118 << 28), bArr3, 21);
        encode56(j120 | (j122 << 28), bArr3, 28);
        encode56(j124 | (j126 << 28), bArr3, 35);
        encode56(j128 | (j130 << 28), bArr3, 42);
        encode56(((j100 + (j129 >> 28)) << 28) | j131, bArr3, 49);
        return bArr3;
    }

    private static void scalarMultBase(byte[] bArr, PointExt pointExt) {
        precompute();
        pointSetNeutral(pointExt);
        int[] iArr = new int[15];
        decodeScalar(bArr, 0, iArr);
        iArr[14] = 4 + Nat.cadd(14, (~iArr[0]) & 1, iArr, L, iArr);
        Nat.shiftDownBit(iArr.length, iArr, 0);
        PointPrecomp pointPrecomp = new PointPrecomp();
        int i = 17;
        while (true) {
            int i2 = 0;
            int i3 = i;
            while (i2 < 5) {
                int i4 = 0;
                int i5 = i3;
                for (int i6 = 0; i6 < 5; i6++) {
                    i4 |= ((iArr[i5 >>> 5] >>> (i5 & 31)) & 1) << i6;
                    i5 += 18;
                }
                int i7 = (i4 >>> 4) & 1;
                pointLookup(i2, ((-i7) ^ i4) & 15, pointPrecomp);
                X448Field.cnegate(i7, pointPrecomp.x);
                pointAddPrecomp(pointPrecomp, pointExt);
                i2++;
                i3 = i5;
            }
            i--;
            if (i >= 0) {
                pointDouble(pointExt);
            } else {
                return;
            }
        }
    }

    private static void scalarMultBaseEncodedVar(byte[] bArr, byte[] bArr2, int i) {
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
        int i = 447;
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

    public static void sign(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4, int i3, int i4, byte[] bArr5, int i5) {
        if (checkContextVar(bArr3)) {
            SHAKEDigest sHAKEDigest = new SHAKEDigest(256);
            byte[] bArr6 = new byte[SIGNATURE_SIZE];
            sHAKEDigest.update(bArr, i, 57);
            sHAKEDigest.doFinal(bArr6, 0, bArr6.length);
            byte[] bArr7 = new byte[57];
            pruneScalar(bArr6, 0, bArr7);
            implSignVar(sHAKEDigest, bArr6, bArr7, bArr2, i2, bArr3, bArr4, i3, i4, bArr5, i5);
            return;
        }
        throw new IllegalArgumentException("ctx");
    }

    public static void sign(byte[] bArr, int i, byte[] bArr2, byte[] bArr3, int i2, int i3, byte[] bArr4, int i4) {
        if (checkContextVar(bArr2)) {
            SHAKEDigest sHAKEDigest = new SHAKEDigest(256);
            byte[] bArr5 = new byte[SIGNATURE_SIZE];
            sHAKEDigest.update(bArr, i, 57);
            sHAKEDigest.doFinal(bArr5, 0, bArr5.length);
            byte[] bArr6 = new byte[57];
            pruneScalar(bArr5, 0, bArr6);
            byte[] bArr7 = new byte[57];
            scalarMultBaseEncodedVar(bArr6, bArr7, 0);
            implSignVar(sHAKEDigest, bArr5, bArr6, bArr7, 0, bArr2, bArr3, i2, i3, bArr4, i4);
            return;
        }
        throw new IllegalArgumentException("ctx");
    }

    public static boolean verify(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4, int i3, int i4) {
        if (checkContextVar(bArr3)) {
            int i5 = i + 57;
            byte[] copyOfRange = Arrays.copyOfRange(bArr, i, i5);
            byte[] copyOfRange2 = Arrays.copyOfRange(bArr, i5, i + SIGNATURE_SIZE);
            if (!checkPointVar(copyOfRange) || !checkScalarVar(copyOfRange2)) {
                return false;
            }
            PointExt pointExt = new PointExt();
            if (!decodePointVar(bArr2, i2, true, pointExt)) {
                return false;
            }
            SHAKEDigest sHAKEDigest = new SHAKEDigest(256);
            byte[] bArr5 = new byte[SIGNATURE_SIZE];
            dom4(sHAKEDigest, (byte) 0, bArr3);
            sHAKEDigest.update(copyOfRange, 0, 57);
            sHAKEDigest.update(bArr2, i2, 57);
            sHAKEDigest.update(bArr4, i3, i4);
            sHAKEDigest.doFinal(bArr5, 0, bArr5.length);
            byte[] reduceScalar = reduceScalar(bArr5);
            int[] iArr = new int[14];
            decodeScalar(copyOfRange2, 0, iArr);
            int[] iArr2 = new int[14];
            decodeScalar(reduceScalar, 0, iArr2);
            PointExt pointExt2 = new PointExt();
            scalarMultStraussVar(iArr, iArr2, pointExt, pointExt2);
            byte[] bArr6 = new byte[57];
            encodePoint(pointExt2, bArr6, 0);
            return Arrays.areEqual(bArr6, copyOfRange);
        }
        throw new IllegalArgumentException("ctx");
    }
}
