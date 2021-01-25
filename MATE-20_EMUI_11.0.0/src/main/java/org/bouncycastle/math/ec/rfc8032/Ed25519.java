package org.bouncycastle.math.ec.rfc8032;

import java.security.SecureRandom;
import org.bouncycastle.asn1.eac.CertificateHolderAuthorization;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.math.ec.rfc7748.X25519;
import org.bouncycastle.math.ec.rfc7748.X25519Field;
import org.bouncycastle.math.raw.Interleave;
import org.bouncycastle.math.raw.Nat;
import org.bouncycastle.math.raw.Nat256;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Strings;

public abstract class Ed25519 {
    private static final int[] B_x = {52811034, 25909283, 8072341, 50637101, 13785486, 30858332, 20483199, 20966410, 43936626, 4379245};
    private static final int[] B_y = {40265304, 26843545, 6710886, 53687091, 13421772, 40265318, 26843545, 6710886, 53687091, 13421772};
    private static final int[] C_d = {56195235, 47411844, 25868126, 40503822, 57364, 58321048, 30416477, 31930572, 57760639, 10749657};
    private static final int[] C_d2 = {45281625, 27714825, 18181821, 13898781, 114729, 49533232, 60832955, 30306712, 48412415, 4722099};
    private static final int[] C_d4 = {23454386, 55429651, 2809210, 27797563, 229458, 31957600, 54557047, 27058993, 29715967, 9444199};
    private static final byte[] DOM2_PREFIX = Strings.toByteArray("SigEd25519 no Ed25519 collisions");
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
    public static final int PREHASH_SIZE = 64;
    public static final int PUBLIC_KEY_SIZE = 32;
    private static final int SCALAR_BYTES = 32;
    private static final int SCALAR_INTS = 8;
    public static final int SECRET_KEY_SIZE = 32;
    public static final int SIGNATURE_SIZE = 64;
    private static final int WNAF_WIDTH_BASE = 7;
    private static int[] precompBase = null;
    private static PointExt[] precompBaseTable = null;
    private static final Object precompLock = new Object();

    public static final class Algorithm {
        public static final int Ed25519 = 0;
        public static final int Ed25519ctx = 1;
        public static final int Ed25519ph = 2;
    }

    /* access modifiers changed from: private */
    public static class PointAccum {
        int[] u;
        int[] v;
        int[] x;
        int[] y;
        int[] z;

        private PointAccum() {
            this.x = X25519Field.create();
            this.y = X25519Field.create();
            this.z = X25519Field.create();
            this.u = X25519Field.create();
            this.v = X25519Field.create();
        }
    }

    /* access modifiers changed from: private */
    public static class PointAffine {
        int[] x;
        int[] y;

        private PointAffine() {
            this.x = X25519Field.create();
            this.y = X25519Field.create();
        }
    }

    /* access modifiers changed from: private */
    public static class PointExt {
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

    /* access modifiers changed from: private */
    public static class PointPrecomp {
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

    private static boolean checkContextVar(byte[] bArr, byte b) {
        return (bArr == null && b == 0) || (bArr != null && bArr.length < 256);
    }

    private static int checkPoint(int[] iArr, int[] iArr2) {
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        int[] create3 = X25519Field.create();
        X25519Field.sqr(iArr, create2);
        X25519Field.sqr(iArr2, create3);
        X25519Field.mul(create2, create3, create);
        X25519Field.sub(create3, create2, create3);
        X25519Field.mul(create, C_d, create);
        X25519Field.addOne(create);
        X25519Field.sub(create, create3, create);
        X25519Field.normalize(create);
        return X25519Field.isZero(create);
    }

    private static int checkPoint(int[] iArr, int[] iArr2, int[] iArr3) {
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        int[] create3 = X25519Field.create();
        int[] create4 = X25519Field.create();
        X25519Field.sqr(iArr, create2);
        X25519Field.sqr(iArr2, create3);
        X25519Field.sqr(iArr3, create4);
        X25519Field.mul(create2, create3, create);
        X25519Field.sub(create3, create2, create3);
        X25519Field.mul(create3, create4, create3);
        X25519Field.sqr(create4, create4);
        X25519Field.mul(create, C_d, create);
        X25519Field.add(create, create4, create);
        X25519Field.sub(create, create3, create);
        X25519Field.normalize(create);
        return X25519Field.isZero(create);
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

    private static Digest createDigest() {
        return new SHA512Digest();
    }

    public static Digest createPrehash() {
        return createDigest();
    }

    private static int decode24(byte[] bArr, int i) {
        int i2 = i + 1;
        return ((bArr[i2 + 1] & 255) << 16) | (bArr[i] & 255) | ((bArr[i2] & 255) << 8);
    }

    private static int decode32(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = i2 + 1;
        return (bArr[i3 + 1] << 24) | (bArr[i] & 255) | ((bArr[i2] & 255) << 8) | ((bArr[i3] & 255) << 16);
    }

    private static void decode32(byte[] bArr, int i, int[] iArr, int i2, int i3) {
        for (int i4 = 0; i4 < i3; i4++) {
            iArr[i2 + i4] = decode32(bArr, (i4 * 4) + i);
        }
    }

    private static boolean decodePointVar(byte[] bArr, int i, boolean z, PointAffine pointAffine) {
        byte[] copyOfRange = Arrays.copyOfRange(bArr, i, i + 32);
        boolean z2 = false;
        if (!checkPointVar(copyOfRange)) {
            return false;
        }
        int i2 = (copyOfRange[31] & 128) >>> 7;
        copyOfRange[31] = (byte) (copyOfRange[31] & Byte.MAX_VALUE);
        X25519Field.decode(copyOfRange, 0, pointAffine.y);
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        X25519Field.sqr(pointAffine.y, create);
        X25519Field.mul(C_d, create, create2);
        X25519Field.subOne(create);
        X25519Field.addOne(create2);
        if (!X25519Field.sqrtRatioVar(create, create2, pointAffine.x)) {
            return false;
        }
        X25519Field.normalize(pointAffine.x);
        if (i2 == 1 && X25519Field.isZeroVar(pointAffine.x)) {
            return false;
        }
        if (i2 != (pointAffine.x[0] & 1)) {
            z2 = true;
        }
        if (z ^ z2) {
            X25519Field.negate(pointAffine.x, pointAffine.x);
        }
        return true;
    }

    private static void decodeScalar(byte[] bArr, int i, int[] iArr) {
        decode32(bArr, i, iArr, 0, 8);
    }

    private static void dom2(Digest digest, byte b, byte[] bArr) {
        if (bArr != null) {
            byte[] bArr2 = DOM2_PREFIX;
            digest.update(bArr2, 0, bArr2.length);
            digest.update(b);
            digest.update((byte) bArr.length);
            digest.update(bArr, 0, bArr.length);
        }
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

    private static int encodePoint(PointAccum pointAccum, byte[] bArr, int i) {
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        X25519Field.inv(pointAccum.z, create2);
        X25519Field.mul(pointAccum.x, create2, create);
        X25519Field.mul(pointAccum.y, create2, create2);
        X25519Field.normalize(create);
        X25519Field.normalize(create2);
        int checkPoint = checkPoint(create, create2);
        X25519Field.encode(create2, bArr, i);
        int i2 = (i + 32) - 1;
        bArr[i2] = (byte) (((create[0] & 1) << 7) | bArr[i2]);
        return checkPoint;
    }

    public static void generatePrivateKey(SecureRandom secureRandom, byte[] bArr) {
        secureRandom.nextBytes(bArr);
    }

    public static void generatePublicKey(byte[] bArr, int i, byte[] bArr2, int i2) {
        Digest createDigest = createDigest();
        byte[] bArr3 = new byte[createDigest.getDigestSize()];
        createDigest.update(bArr, i, 32);
        createDigest.doFinal(bArr3, 0);
        byte[] bArr4 = new byte[32];
        pruneScalar(bArr3, 0, bArr4);
        scalarMultBaseEncoded(bArr4, bArr2, i2);
    }

    private static int getWindow4(int[] iArr, int i) {
        return (iArr[i >>> 3] >>> ((i & 7) << 2)) & 15;
    }

    private static byte[] getWnafVar(int[] iArr, int i) {
        int[] iArr2 = new int[16];
        int length = iArr2.length;
        int i2 = 0;
        int i3 = 8;
        int i4 = 0;
        while (true) {
            i3--;
            if (i3 < 0) {
                break;
            }
            int i5 = iArr[i3];
            int i6 = length - 1;
            iArr2[i6] = (i4 << 16) | (i5 >>> 16);
            length = i6 - 1;
            iArr2[length] = i5;
            i4 = i5;
        }
        byte[] bArr = new byte[253];
        int i7 = 1 << i;
        int i8 = i7 - 1;
        int i9 = i7 >>> 1;
        int i10 = 0;
        int i11 = 0;
        while (i2 < iArr2.length) {
            int i12 = iArr2[i2];
            while (i10 < 16) {
                int i13 = i12 >>> i10;
                if ((i13 & 1) == i11) {
                    i10++;
                } else {
                    int i14 = (i13 & i8) + i11;
                    int i15 = i14 & i9;
                    int i16 = i14 - (i15 << 1);
                    i11 = i15 >>> (i - 1);
                    bArr[(i2 << 4) + i10] = (byte) i16;
                    i10 += i;
                }
            }
            i2++;
            i10 -= 16;
        }
        return bArr;
    }

    private static void implSign(Digest digest, byte[] bArr, byte[] bArr2, byte[] bArr3, int i, byte[] bArr4, byte b, byte[] bArr5, int i2, int i3, byte[] bArr6, int i4) {
        dom2(digest, b, bArr4);
        digest.update(bArr, 32, 32);
        digest.update(bArr5, i2, i3);
        digest.doFinal(bArr, 0);
        byte[] reduceScalar = reduceScalar(bArr);
        byte[] bArr7 = new byte[32];
        scalarMultBaseEncoded(reduceScalar, bArr7, 0);
        dom2(digest, b, bArr4);
        digest.update(bArr7, 0, 32);
        digest.update(bArr3, i, 32);
        digest.update(bArr5, i2, i3);
        digest.doFinal(bArr, 0);
        byte[] calculateS = calculateS(reduceScalar, reduceScalar(bArr), bArr2);
        System.arraycopy(bArr7, 0, bArr6, i4, 32);
        System.arraycopy(calculateS, 0, bArr6, i4 + 32, 32);
    }

    private static void implSign(byte[] bArr, int i, byte[] bArr2, byte b, byte[] bArr3, int i2, int i3, byte[] bArr4, int i4) {
        if (checkContextVar(bArr2, b)) {
            Digest createDigest = createDigest();
            byte[] bArr5 = new byte[createDigest.getDigestSize()];
            createDigest.update(bArr, i, 32);
            createDigest.doFinal(bArr5, 0);
            byte[] bArr6 = new byte[32];
            pruneScalar(bArr5, 0, bArr6);
            byte[] bArr7 = new byte[32];
            scalarMultBaseEncoded(bArr6, bArr7, 0);
            implSign(createDigest, bArr5, bArr6, bArr7, 0, bArr2, b, bArr3, i2, i3, bArr4, i4);
            return;
        }
        throw new IllegalArgumentException("ctx");
    }

    private static void implSign(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, byte b, byte[] bArr4, int i3, int i4, byte[] bArr5, int i5) {
        if (checkContextVar(bArr3, b)) {
            Digest createDigest = createDigest();
            byte[] bArr6 = new byte[createDigest.getDigestSize()];
            createDigest.update(bArr, i, 32);
            createDigest.doFinal(bArr6, 0);
            byte[] bArr7 = new byte[32];
            pruneScalar(bArr6, 0, bArr7);
            implSign(createDigest, bArr6, bArr7, bArr2, i2, bArr3, b, bArr4, i3, i4, bArr5, i5);
            return;
        }
        throw new IllegalArgumentException("ctx");
    }

    private static boolean implVerify(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, byte b, byte[] bArr4, int i3, int i4) {
        if (checkContextVar(bArr3, b)) {
            int i5 = i + 32;
            byte[] copyOfRange = Arrays.copyOfRange(bArr, i, i5);
            byte[] copyOfRange2 = Arrays.copyOfRange(bArr, i5, i + 64);
            if (!checkPointVar(copyOfRange) || !checkScalarVar(copyOfRange2)) {
                return false;
            }
            PointAffine pointAffine = new PointAffine();
            if (!decodePointVar(bArr2, i2, true, pointAffine)) {
                return false;
            }
            Digest createDigest = createDigest();
            byte[] bArr5 = new byte[createDigest.getDigestSize()];
            dom2(createDigest, b, bArr3);
            createDigest.update(copyOfRange, 0, 32);
            createDigest.update(bArr2, i2, 32);
            createDigest.update(bArr4, i3, i4);
            createDigest.doFinal(bArr5, 0);
            byte[] reduceScalar = reduceScalar(bArr5);
            int[] iArr = new int[8];
            decodeScalar(copyOfRange2, 0, iArr);
            int[] iArr2 = new int[8];
            decodeScalar(reduceScalar, 0, iArr2);
            PointAccum pointAccum = new PointAccum();
            scalarMultStrausVar(iArr, iArr2, pointAffine, pointAccum);
            byte[] bArr6 = new byte[32];
            return encodePoint(pointAccum, bArr6, 0) != 0 && Arrays.areEqual(bArr6, copyOfRange);
        }
        throw new IllegalArgumentException("ctx");
    }

    private static void pointAdd(PointExt pointExt, PointAccum pointAccum) {
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        int[] create3 = X25519Field.create();
        int[] create4 = X25519Field.create();
        int[] iArr = pointAccum.u;
        int[] create5 = X25519Field.create();
        int[] create6 = X25519Field.create();
        int[] iArr2 = pointAccum.v;
        X25519Field.apm(pointAccum.y, pointAccum.x, create2, create);
        X25519Field.apm(pointExt.y, pointExt.x, create4, create3);
        X25519Field.mul(create, create3, create);
        X25519Field.mul(create2, create4, create2);
        X25519Field.mul(pointAccum.u, pointAccum.v, create3);
        X25519Field.mul(create3, pointExt.t, create3);
        X25519Field.mul(create3, C_d2, create3);
        X25519Field.mul(pointAccum.z, pointExt.z, create4);
        X25519Field.add(create4, create4, create4);
        X25519Field.apm(create2, create, iArr2, iArr);
        X25519Field.apm(create4, create3, create6, create5);
        X25519Field.carry(create6);
        X25519Field.mul(iArr, create5, pointAccum.x);
        X25519Field.mul(create6, iArr2, pointAccum.y);
        X25519Field.mul(create5, create6, pointAccum.z);
    }

    private static void pointAdd(PointExt pointExt, PointExt pointExt2) {
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        int[] create3 = X25519Field.create();
        int[] create4 = X25519Field.create();
        int[] create5 = X25519Field.create();
        int[] create6 = X25519Field.create();
        int[] create7 = X25519Field.create();
        int[] create8 = X25519Field.create();
        X25519Field.apm(pointExt.y, pointExt.x, create2, create);
        X25519Field.apm(pointExt2.y, pointExt2.x, create4, create3);
        X25519Field.mul(create, create3, create);
        X25519Field.mul(create2, create4, create2);
        X25519Field.mul(pointExt.t, pointExt2.t, create3);
        X25519Field.mul(create3, C_d2, create3);
        X25519Field.mul(pointExt.z, pointExt2.z, create4);
        X25519Field.add(create4, create4, create4);
        X25519Field.apm(create2, create, create8, create5);
        X25519Field.apm(create4, create3, create7, create6);
        X25519Field.carry(create7);
        X25519Field.mul(create5, create6, pointExt2.x);
        X25519Field.mul(create7, create8, pointExt2.y);
        X25519Field.mul(create6, create7, pointExt2.z);
        X25519Field.mul(create5, create8, pointExt2.t);
    }

    private static void pointAddPrecomp(PointPrecomp pointPrecomp, PointAccum pointAccum) {
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        int[] create3 = X25519Field.create();
        int[] iArr = pointAccum.u;
        int[] create4 = X25519Field.create();
        int[] create5 = X25519Field.create();
        int[] iArr2 = pointAccum.v;
        X25519Field.apm(pointAccum.y, pointAccum.x, create2, create);
        X25519Field.mul(create, pointPrecomp.ymx_h, create);
        X25519Field.mul(create2, pointPrecomp.ypx_h, create2);
        X25519Field.mul(pointAccum.u, pointAccum.v, create3);
        X25519Field.mul(create3, pointPrecomp.xyd, create3);
        X25519Field.apm(create2, create, iArr2, iArr);
        X25519Field.apm(pointAccum.z, create3, create5, create4);
        X25519Field.carry(create5);
        X25519Field.mul(iArr, create4, pointAccum.x);
        X25519Field.mul(create5, iArr2, pointAccum.y);
        X25519Field.mul(create4, create5, pointAccum.z);
    }

    private static void pointAddVar(boolean z, PointExt pointExt, PointAccum pointAccum) {
        int[] iArr;
        int[] iArr2;
        int[] iArr3;
        int[] iArr4;
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        int[] create3 = X25519Field.create();
        int[] create4 = X25519Field.create();
        int[] iArr5 = pointAccum.u;
        int[] create5 = X25519Field.create();
        int[] create6 = X25519Field.create();
        int[] iArr6 = pointAccum.v;
        if (z) {
            iArr = create3;
            iArr4 = create4;
            iArr3 = create5;
            iArr2 = create6;
        } else {
            iArr4 = create3;
            iArr = create4;
            iArr2 = create5;
            iArr3 = create6;
        }
        X25519Field.apm(pointAccum.y, pointAccum.x, create2, create);
        X25519Field.apm(pointExt.y, pointExt.x, iArr, iArr4);
        X25519Field.mul(create, create3, create);
        X25519Field.mul(create2, create4, create2);
        X25519Field.mul(pointAccum.u, pointAccum.v, create3);
        X25519Field.mul(create3, pointExt.t, create3);
        X25519Field.mul(create3, C_d2, create3);
        X25519Field.mul(pointAccum.z, pointExt.z, create4);
        X25519Field.add(create4, create4, create4);
        X25519Field.apm(create2, create, iArr6, iArr5);
        X25519Field.apm(create4, create3, iArr3, iArr2);
        X25519Field.carry(iArr3);
        X25519Field.mul(iArr5, create5, pointAccum.x);
        X25519Field.mul(create6, iArr6, pointAccum.y);
        X25519Field.mul(create5, create6, pointAccum.z);
    }

    private static void pointAddVar(boolean z, PointExt pointExt, PointExt pointExt2, PointExt pointExt3) {
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
            iArr4 = create3;
            iArr3 = create4;
            iArr2 = create6;
            iArr = create7;
        } else {
            iArr3 = create3;
            iArr4 = create4;
            iArr = create6;
            iArr2 = create7;
        }
        X25519Field.apm(pointExt.y, pointExt.x, create2, create);
        X25519Field.apm(pointExt2.y, pointExt2.x, iArr4, iArr3);
        X25519Field.mul(create, create3, create);
        X25519Field.mul(create2, create4, create2);
        X25519Field.mul(pointExt.t, pointExt2.t, create3);
        X25519Field.mul(create3, C_d2, create3);
        X25519Field.mul(pointExt.z, pointExt2.z, create4);
        X25519Field.add(create4, create4, create4);
        X25519Field.apm(create2, create, create8, create5);
        X25519Field.apm(create4, create3, iArr2, iArr);
        X25519Field.carry(iArr2);
        X25519Field.mul(create5, create6, pointExt3.x);
        X25519Field.mul(create7, create8, pointExt3.y);
        X25519Field.mul(create6, create7, pointExt3.z);
        X25519Field.mul(create5, create8, pointExt3.t);
    }

    private static PointExt pointCopy(PointAccum pointAccum) {
        PointExt pointExt = new PointExt();
        X25519Field.copy(pointAccum.x, 0, pointExt.x, 0);
        X25519Field.copy(pointAccum.y, 0, pointExt.y, 0);
        X25519Field.copy(pointAccum.z, 0, pointExt.z, 0);
        X25519Field.mul(pointAccum.u, pointAccum.v, pointExt.t);
        return pointExt;
    }

    private static PointExt pointCopy(PointAffine pointAffine) {
        PointExt pointExt = new PointExt();
        X25519Field.copy(pointAffine.x, 0, pointExt.x, 0);
        X25519Field.copy(pointAffine.y, 0, pointExt.y, 0);
        pointExtendXY(pointExt);
        return pointExt;
    }

    private static PointExt pointCopy(PointExt pointExt) {
        PointExt pointExt2 = new PointExt();
        pointCopy(pointExt, pointExt2);
        return pointExt2;
    }

    private static void pointCopy(PointAffine pointAffine, PointAccum pointAccum) {
        X25519Field.copy(pointAffine.x, 0, pointAccum.x, 0);
        X25519Field.copy(pointAffine.y, 0, pointAccum.y, 0);
        pointExtendXY(pointAccum);
    }

    private static void pointCopy(PointExt pointExt, PointExt pointExt2) {
        X25519Field.copy(pointExt.x, 0, pointExt2.x, 0);
        X25519Field.copy(pointExt.y, 0, pointExt2.y, 0);
        X25519Field.copy(pointExt.z, 0, pointExt2.z, 0);
        X25519Field.copy(pointExt.t, 0, pointExt2.t, 0);
    }

    private static void pointDouble(PointAccum pointAccum) {
        int[] create = X25519Field.create();
        int[] create2 = X25519Field.create();
        int[] create3 = X25519Field.create();
        int[] iArr = pointAccum.u;
        int[] create4 = X25519Field.create();
        int[] create5 = X25519Field.create();
        int[] iArr2 = pointAccum.v;
        X25519Field.sqr(pointAccum.x, create);
        X25519Field.sqr(pointAccum.y, create2);
        X25519Field.sqr(pointAccum.z, create3);
        X25519Field.add(create3, create3, create3);
        X25519Field.apm(create, create2, iArr2, create5);
        X25519Field.add(pointAccum.x, pointAccum.y, iArr);
        X25519Field.sqr(iArr, iArr);
        X25519Field.sub(iArr2, iArr, iArr);
        X25519Field.add(create3, create5, create4);
        X25519Field.carry(create4);
        X25519Field.mul(iArr, create4, pointAccum.x);
        X25519Field.mul(create5, iArr2, pointAccum.y);
        X25519Field.mul(create4, create5, pointAccum.z);
    }

    private static void pointExtendXY(PointAccum pointAccum) {
        X25519Field.one(pointAccum.z);
        X25519Field.copy(pointAccum.x, 0, pointAccum.u, 0);
        X25519Field.copy(pointAccum.y, 0, pointAccum.v, 0);
    }

    private static void pointExtendXY(PointExt pointExt) {
        X25519Field.one(pointExt.z);
        X25519Field.mul(pointExt.x, pointExt.y, pointExt.t);
    }

    private static void pointLookup(int i, int i2, PointPrecomp pointPrecomp) {
        int i3 = i * 8 * 3 * 10;
        for (int i4 = 0; i4 < 8; i4++) {
            int i5 = ((i4 ^ i2) - 1) >> 31;
            X25519Field.cmov(i5, precompBase, i3, pointPrecomp.ypx_h, 0);
            int i6 = i3 + 10;
            X25519Field.cmov(i5, precompBase, i6, pointPrecomp.ymx_h, 0);
            int i7 = i6 + 10;
            X25519Field.cmov(i5, precompBase, i7, pointPrecomp.xyd, 0);
            i3 = i7 + 10;
        }
    }

    private static void pointLookup(int[] iArr, int i, PointExt pointExt) {
        int i2 = i * 40;
        X25519Field.copy(iArr, i2, pointExt.x, 0);
        int i3 = i2 + 10;
        X25519Field.copy(iArr, i3, pointExt.y, 0);
        int i4 = i3 + 10;
        X25519Field.copy(iArr, i4, pointExt.z, 0);
        X25519Field.copy(iArr, i4 + 10, pointExt.t, 0);
    }

    private static void pointLookup(int[] iArr, int i, int[] iArr2, PointExt pointExt) {
        int window4 = getWindow4(iArr, i);
        int i2 = (window4 >>> 3) ^ 1;
        int i3 = (window4 ^ (-i2)) & 7;
        int i4 = 0;
        for (int i5 = 0; i5 < 8; i5++) {
            int i6 = ((i5 ^ i3) - 1) >> 31;
            X25519Field.cmov(i6, iArr2, i4, pointExt.x, 0);
            int i7 = i4 + 10;
            X25519Field.cmov(i6, iArr2, i7, pointExt.y, 0);
            int i8 = i7 + 10;
            X25519Field.cmov(i6, iArr2, i8, pointExt.z, 0);
            int i9 = i8 + 10;
            X25519Field.cmov(i6, iArr2, i9, pointExt.t, 0);
            i4 = i9 + 10;
        }
        X25519Field.cnegate(i2, pointExt.x);
        X25519Field.cnegate(i2, pointExt.t);
    }

    private static int[] pointPrecomp(PointAffine pointAffine, int i) {
        PointExt pointCopy = pointCopy(pointAffine);
        PointExt pointCopy2 = pointCopy(pointCopy);
        pointAdd(pointCopy, pointCopy2);
        int[] createTable = X25519Field.createTable(i * 4);
        int i2 = 0;
        int i3 = 0;
        while (true) {
            X25519Field.copy(pointCopy.x, 0, createTable, i2);
            int i4 = i2 + 10;
            X25519Field.copy(pointCopy.y, 0, createTable, i4);
            int i5 = i4 + 10;
            X25519Field.copy(pointCopy.z, 0, createTable, i5);
            int i6 = i5 + 10;
            X25519Field.copy(pointCopy.t, 0, createTable, i6);
            i2 = i6 + 10;
            i3++;
            if (i3 == i) {
                return createTable;
            }
            pointAdd(pointCopy2, pointCopy);
        }
    }

    private static PointExt[] pointPrecompVar(PointExt pointExt, int i) {
        PointExt pointExt2 = new PointExt();
        pointAddVar(false, pointExt, pointExt, pointExt2);
        PointExt[] pointExtArr = new PointExt[i];
        pointExtArr[0] = pointCopy(pointExt);
        for (int i2 = 1; i2 < i; i2++) {
            PointExt pointExt3 = pointExtArr[i2 - 1];
            PointExt pointExt4 = new PointExt();
            pointExtArr[i2] = pointExt4;
            pointAddVar(false, pointExt3, pointExt2, pointExt4);
        }
        return pointExtArr;
    }

    private static void pointSetNeutral(PointAccum pointAccum) {
        X25519Field.zero(pointAccum.x);
        X25519Field.one(pointAccum.y);
        X25519Field.one(pointAccum.z);
        X25519Field.zero(pointAccum.u);
        X25519Field.one(pointAccum.v);
    }

    private static void pointSetNeutral(PointExt pointExt) {
        X25519Field.zero(pointExt.x);
        X25519Field.one(pointExt.y);
        X25519Field.one(pointExt.z);
        X25519Field.zero(pointExt.t);
    }

    public static void precompute() {
        int i;
        synchronized (precompLock) {
            if (precompBase == null) {
                PointExt pointExt = new PointExt();
                X25519Field.copy(B_x, 0, pointExt.x, 0);
                X25519Field.copy(B_y, 0, pointExt.y, 0);
                pointExtendXY(pointExt);
                precompBaseTable = pointPrecompVar(pointExt, 32);
                PointAccum pointAccum = new PointAccum();
                X25519Field.copy(B_x, 0, pointAccum.x, 0);
                X25519Field.copy(B_y, 0, pointAccum.y, 0);
                pointExtendXY(pointAccum);
                precompBase = X25519Field.createTable(CertificateHolderAuthorization.CVCA);
                int i2 = 0;
                int i3 = 0;
                while (i2 < 8) {
                    PointExt[] pointExtArr = new PointExt[4];
                    PointExt pointExt2 = new PointExt();
                    pointSetNeutral(pointExt2);
                    int i4 = 0;
                    while (true) {
                        i = 1;
                        if (i4 >= 4) {
                            break;
                        }
                        pointAddVar(true, pointExt2, pointCopy(pointAccum), pointExt2);
                        pointDouble(pointAccum);
                        pointExtArr[i4] = pointCopy(pointAccum);
                        if (i2 + i4 != 10) {
                            while (i < 8) {
                                pointDouble(pointAccum);
                                i++;
                            }
                        }
                        i4++;
                    }
                    PointExt[] pointExtArr2 = new PointExt[8];
                    pointExtArr2[0] = pointExt2;
                    int i5 = 0;
                    int i6 = 1;
                    while (i5 < 3) {
                        int i7 = i << i5;
                        int i8 = i6;
                        int i9 = 0;
                        while (i9 < i7) {
                            PointExt pointExt3 = pointExtArr2[i8 - i7];
                            PointExt pointExt4 = pointExtArr[i5];
                            PointExt pointExt5 = new PointExt();
                            pointExtArr2[i8] = pointExt5;
                            pointAddVar(false, pointExt3, pointExt4, pointExt5);
                            i9++;
                            i8++;
                        }
                        i5++;
                        i6 = i8;
                        i = 1;
                    }
                    int i10 = i3;
                    for (int i11 = 0; i11 < 8; i11++) {
                        PointExt pointExt6 = pointExtArr2[i11];
                        int[] create = X25519Field.create();
                        int[] create2 = X25519Field.create();
                        X25519Field.add(pointExt6.z, pointExt6.z, create);
                        X25519Field.inv(create, create2);
                        X25519Field.mul(pointExt6.x, create2, create);
                        X25519Field.mul(pointExt6.y, create2, create2);
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
                    i2++;
                    i3 = i10;
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
        long decode24 = ((long) (decode24(bArr, 4) << 4)) & 4294967295L;
        long decode32 = ((long) decode32(bArr, 7)) & 4294967295L;
        long decode242 = ((long) (decode24(bArr, 11) << 4)) & 4294967295L;
        long decode322 = ((long) decode32(bArr, 14)) & 4294967295L;
        long decode243 = ((long) (decode24(bArr, 18) << 4)) & 4294967295L;
        long decode323 = ((long) decode32(bArr, 21)) & 4294967295L;
        long decode324 = ((long) decode32(bArr, 28)) & 4294967295L;
        long decode325 = ((long) decode32(bArr, 49)) & 4294967295L;
        long decode244 = ((long) (decode24(bArr, 53) << 4)) & 4294967295L;
        long decode326 = ((long) decode32(bArr, 56)) & 4294967295L;
        long decode245 = ((long) (decode24(bArr, 60) << 4)) & 4294967295L;
        long j = ((long) bArr[63]) & 255;
        long decode246 = (((long) (decode24(bArr, 46) << 4)) & 4294967295L) - (j * 5343);
        long j2 = decode245 + (decode326 >> 28);
        long j3 = decode326 & M28L;
        long decode247 = ((((long) (decode24(bArr, 32) << 4)) & 4294967295L) - (j * -50998291)) - (j2 * 19280294);
        long decode327 = ((((long) decode32(bArr, 35)) & 4294967295L) - (j * 19280294)) - (j2 * 127719000);
        long decode328 = ((((long) decode32(bArr, 42)) & 4294967295L) - (j * -6428113)) - (j2 * 5343);
        long decode248 = (((((long) (decode24(bArr, 39) << 4)) & 4294967295L) - (j * 127719000)) - (j2 * -6428113)) - (j3 * 5343);
        long j4 = decode244 + (decode325 >> 28);
        long j5 = decode325 & M28L;
        long j6 = (decode327 - (j3 * -6428113)) - (j4 * 5343);
        long j7 = ((decode247 - (j3 * 127719000)) - (j4 * -6428113)) - (j5 * 5343);
        long j8 = decode246 + (decode328 >> 28);
        long j9 = (decode328 & M28L) + (decode248 >> 28);
        long j10 = (decode322 - (j8 * -50998291)) - (j9 * 19280294);
        long j11 = ((decode243 - (j5 * -50998291)) - (j8 * 19280294)) - (j9 * 127719000);
        long decode249 = (((((((long) (decode24(bArr, 25) << 4)) & 4294967295L) - (j3 * -50998291)) - (j4 * 19280294)) - (j5 * 127719000)) - (j8 * -6428113)) - (j9 * 5343);
        long j12 = (decode248 & M28L) + (j6 >> 28);
        long j13 = (decode242 - (j9 * -50998291)) - (j12 * 19280294);
        long j14 = j10 - (j12 * 127719000);
        long j15 = ((((decode323 - (j4 * -50998291)) - (j5 * 19280294)) - (j8 * 127719000)) - (j9 * -6428113)) - (j12 * 5343);
        long j16 = (j6 & M28L) + (j7 >> 28);
        long j17 = j7 & M28L;
        long j18 = decode24 - (j16 * -50998291);
        long j19 = (decode32 - (j12 * -50998291)) - (j16 * 19280294);
        long j20 = j13 - (j16 * 127719000);
        long j21 = j14 - (j16 * -6428113);
        long j22 = (j11 - (j12 * -6428113)) - (j16 * 5343);
        long j23 = (((((decode324 - (j2 * -50998291)) - (j3 * 19280294)) - (j4 * 127719000)) - (j5 * -6428113)) - (j8 * 5343)) + (decode249 >> 28);
        long j24 = decode249 & M28L;
        long j25 = j23 & M28L;
        long j26 = j25 >>> 27;
        long j27 = j17 + (j23 >> 28) + j26;
        long decode329 = (((long) decode32(bArr, 0)) & 4294967295L) - (j27 * -50998291);
        long j28 = (j18 - (j27 * 19280294)) + (decode329 >> 28);
        long j29 = decode329 & M28L;
        long j30 = (j19 - (j27 * 127719000)) + (j28 >> 28);
        long j31 = j28 & M28L;
        long j32 = (j20 - (j27 * -6428113)) + (j30 >> 28);
        long j33 = j30 & M28L;
        long j34 = (j21 - (j27 * 5343)) + (j32 >> 28);
        long j35 = j32 & M28L;
        long j36 = j22 + (j34 >> 28);
        long j37 = j34 & M28L;
        long j38 = j15 + (j36 >> 28);
        long j39 = j36 & M28L;
        long j40 = j24 + (j38 >> 28);
        long j41 = j38 & M28L;
        long j42 = j25 + (j40 >> 28);
        long j43 = j40 & M28L;
        long j44 = j42 >> 28;
        long j45 = j42 & M28L;
        long j46 = j44 - j26;
        long j47 = j29 + (j46 & -50998291);
        long j48 = j31 + (j46 & 19280294) + (j47 >> 28);
        long j49 = j47 & M28L;
        long j50 = j33 + (j46 & 127719000) + (j48 >> 28);
        long j51 = j48 & M28L;
        long j52 = j35 + (j46 & -6428113) + (j50 >> 28);
        long j53 = j50 & M28L;
        long j54 = j37 + (j46 & 5343) + (j52 >> 28);
        long j55 = j52 & M28L;
        long j56 = j39 + (j54 >> 28);
        long j57 = j54 & M28L;
        long j58 = j41 + (j56 >> 28);
        long j59 = j56 & M28L;
        long j60 = j43 + (j58 >> 28);
        long j61 = j58 & M28L;
        long j62 = j60 & M28L;
        byte[] bArr2 = new byte[32];
        encode56(j49 | (j51 << 28), bArr2, 0);
        encode56((j55 << 28) | j53, bArr2, 7);
        encode56(j57 | (j59 << 28), bArr2, 14);
        encode56(j61 | (j62 << 28), bArr2, 21);
        encode32((int) (j45 + (j60 >> 28)), bArr2, 28);
        return bArr2;
    }

    private static void scalarMult(byte[] bArr, PointAffine pointAffine, PointAccum pointAccum) {
        int[] iArr = new int[8];
        decodeScalar(bArr, 0, iArr);
        Nat.shiftDownBits(8, iArr, 3, 1);
        Nat.cadd(8, 1 & (~iArr[0]), iArr, L, iArr);
        Nat.shiftDownBit(8, iArr, 0);
        pointCopy(pointAffine, pointAccum);
        int[] pointPrecomp = pointPrecomp(pointAffine, 8);
        PointExt pointExt = new PointExt();
        pointLookup(pointPrecomp, 7, pointExt);
        pointAdd(pointExt, pointAccum);
        int i = 62;
        while (true) {
            pointLookup(iArr, i, pointPrecomp, pointExt);
            pointAdd(pointExt, pointAccum);
            pointDouble(pointAccum);
            pointDouble(pointAccum);
            pointDouble(pointAccum);
            i--;
            if (i >= 0) {
                pointDouble(pointAccum);
            } else {
                return;
            }
        }
    }

    private static void scalarMultBase(byte[] bArr, PointAccum pointAccum) {
        precompute();
        pointSetNeutral(pointAccum);
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
                pointAddPrecomp(pointPrecomp, pointAccum);
            }
            i2 -= 4;
            if (i2 >= 0) {
                pointDouble(pointAccum);
            } else {
                return;
            }
        }
    }

    private static void scalarMultBaseEncoded(byte[] bArr, byte[] bArr2, int i) {
        PointAccum pointAccum = new PointAccum();
        scalarMultBase(bArr, pointAccum);
        if (encodePoint(pointAccum, bArr2, i) == 0) {
            throw new IllegalStateException();
        }
    }

    public static void scalarMultBaseYZ(X25519.Friend friend, byte[] bArr, int i, int[] iArr, int[] iArr2) {
        if (friend != null) {
            byte[] bArr2 = new byte[32];
            pruneScalar(bArr, i, bArr2);
            PointAccum pointAccum = new PointAccum();
            scalarMultBase(bArr2, pointAccum);
            if (checkPoint(pointAccum.x, pointAccum.y, pointAccum.z) != 0) {
                X25519Field.copy(pointAccum.y, 0, iArr, 0);
                X25519Field.copy(pointAccum.z, 0, iArr2, 0);
                return;
            }
            throw new IllegalStateException();
        }
        throw new NullPointerException("This method is only for use by X25519");
    }

    private static void scalarMultStrausVar(int[] iArr, int[] iArr2, PointAffine pointAffine, PointAccum pointAccum) {
        precompute();
        byte[] wnafVar = getWnafVar(iArr, 7);
        byte[] wnafVar2 = getWnafVar(iArr2, 5);
        PointExt[] pointPrecompVar = pointPrecompVar(pointCopy(pointAffine), 8);
        pointSetNeutral(pointAccum);
        int i = 252;
        while (true) {
            byte b = wnafVar[i];
            boolean z = false;
            if (b != 0) {
                int i2 = b >> 31;
                pointAddVar(i2 != 0, precompBaseTable[(b ^ i2) >>> 1], pointAccum);
            }
            byte b2 = wnafVar2[i];
            if (b2 != 0) {
                int i3 = b2 >> 31;
                int i4 = (b2 ^ i3) >>> 1;
                if (i3 != 0) {
                    z = true;
                }
                pointAddVar(z, pointPrecompVar[i4], pointAccum);
            }
            i--;
            if (i >= 0) {
                pointDouble(pointAccum);
            } else {
                return;
            }
        }
    }

    public static void sign(byte[] bArr, int i, byte[] bArr2, int i2, int i3, byte[] bArr3, int i4) {
        implSign(bArr, i, null, (byte) 0, bArr2, i2, i3, bArr3, i4);
    }

    public static void sign(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3, int i4, byte[] bArr4, int i5) {
        implSign(bArr, i, bArr2, i2, null, (byte) 0, bArr3, i3, i4, bArr4, i5);
    }

    public static void sign(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4, int i3, int i4, byte[] bArr5, int i5) {
        implSign(bArr, i, bArr2, i2, bArr3, (byte) 0, bArr4, i3, i4, bArr5, i5);
    }

    public static void sign(byte[] bArr, int i, byte[] bArr2, byte[] bArr3, int i2, int i3, byte[] bArr4, int i4) {
        implSign(bArr, i, bArr2, (byte) 0, bArr3, i2, i3, bArr4, i4);
    }

    public static void signPrehash(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, Digest digest, byte[] bArr4, int i3) {
        byte[] bArr5 = new byte[64];
        if (64 == digest.doFinal(bArr5, 0)) {
            implSign(bArr, i, bArr2, i2, bArr3, (byte) 1, bArr5, 0, bArr5.length, bArr4, i3);
            return;
        }
        throw new IllegalArgumentException("ph");
    }

    public static void signPrehash(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4, int i3, byte[] bArr5, int i4) {
        implSign(bArr, i, bArr2, i2, bArr3, (byte) 1, bArr4, i3, 64, bArr5, i4);
    }

    public static void signPrehash(byte[] bArr, int i, byte[] bArr2, Digest digest, byte[] bArr3, int i2) {
        byte[] bArr4 = new byte[64];
        if (64 == digest.doFinal(bArr4, 0)) {
            implSign(bArr, i, bArr2, (byte) 1, bArr4, 0, bArr4.length, bArr3, i2);
            return;
        }
        throw new IllegalArgumentException("ph");
    }

    public static void signPrehash(byte[] bArr, int i, byte[] bArr2, byte[] bArr3, int i2, byte[] bArr4, int i3) {
        implSign(bArr, i, bArr2, (byte) 1, bArr3, i2, 64, bArr4, i3);
    }

    public static boolean verify(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, int i3, int i4) {
        return implVerify(bArr, i, bArr2, i2, null, (byte) 0, bArr3, i3, i4);
    }

    public static boolean verify(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4, int i3, int i4) {
        return implVerify(bArr, i, bArr2, i2, bArr3, (byte) 0, bArr4, i3, i4);
    }

    public static boolean verifyPrehash(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, Digest digest) {
        byte[] bArr4 = new byte[64];
        if (64 == digest.doFinal(bArr4, 0)) {
            return implVerify(bArr, i, bArr2, i2, bArr3, (byte) 1, bArr4, 0, bArr4.length);
        }
        throw new IllegalArgumentException("ph");
    }

    public static boolean verifyPrehash(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3, byte[] bArr4, int i3) {
        return implVerify(bArr, i, bArr2, i2, bArr3, (byte) 1, bArr4, i3, 64);
    }
}
