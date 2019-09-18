package org.bouncycastle.crypto.digests;

import org.bouncycastle.asn1.eac.CertificateBody;
import org.bouncycastle.asn1.x509.DisplayText;
import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.crypto.tls.CipherSuite;
import org.bouncycastle.crypto.tls.ExtensionType;
import org.bouncycastle.math.Primes;
import org.bouncycastle.math.ec.rfc8032.Ed448;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Memoable;

public final class WhirlpoolDigest implements ExtendedDigest, Memoable {
    private static final int BITCOUNT_ARRAY_SIZE = 32;
    private static final int BYTE_LENGTH = 64;
    private static final long[] C0 = new long[256];
    private static final long[] C1 = new long[256];
    private static final long[] C2 = new long[256];
    private static final long[] C3 = new long[256];
    private static final long[] C4 = new long[256];
    private static final long[] C5 = new long[256];
    private static final long[] C6 = new long[256];
    private static final long[] C7 = new long[256];
    private static final int DIGEST_LENGTH_BYTES = 64;
    private static final short[] EIGHT = new short[32];
    private static final int REDUCTION_POLYNOMIAL = 285;
    private static final int ROUNDS = 10;
    private static final int[] SBOX = {24, 35, 198, 232, CipherSuite.TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA, CipherSuite.TLS_RSA_PSK_WITH_NULL_SHA256, 1, 79, 54, CipherSuite.TLS_DH_anon_WITH_AES_128_GCM_SHA256, 210, 245, 121, 111, CipherSuite.TLS_DHE_PSK_WITH_AES_256_CBC_SHA, 82, 96, 188, CipherSuite.TLS_DH_anon_WITH_SEED_CBC_SHA, CipherSuite.TLS_DHE_PSK_WITH_RC4_128_SHA, CipherSuite.TLS_DHE_DSS_WITH_AES_256_GCM_SHA384, 12, 123, 53, 29, 224, 215, CipherSuite.TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA256, 46, 75, 254, 87, 21, 119, 55, 229, CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384, 240, 74, 218, 88, 201, 41, 10, CipherSuite.TLS_PSK_WITH_NULL_SHA384, CipherSuite.TLS_DH_RSA_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256, CipherSuite.TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA, CipherSuite.TLS_DHE_DSS_WITH_CAMELLIA_128_CBC_SHA256, 93, 16, 244, 203, 62, 5, 103, 228, 39, 65, CipherSuite.TLS_PSK_WITH_3DES_EDE_CBC_SHA, CipherSuite.TLS_DH_anon_WITH_AES_256_GCM_SHA384, 125, CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA, 216, 251, 238, 124, 102, 221, 23, 71, CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256, 202, 45, CipherSuite.TLS_DH_anon_WITH_CAMELLIA_128_CBC_SHA256, 7, CipherSuite.TLS_RSA_PSK_WITH_AES_256_GCM_SHA384, 90, 131, 51, 99, 2, CipherSuite.TLS_DHE_PSK_WITH_AES_128_GCM_SHA256, 113, DisplayText.DISPLAY_TEXT_MAXIMUM_SIZE, 25, 73, 217, 242, 227, 91, CipherSuite.TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA, CipherSuite.TLS_DHE_RSA_WITH_SEED_CBC_SHA, 38, 50, CipherSuite.TLS_PSK_WITH_NULL_SHA256, 233, 15, 213, 128, CipherSuite.TLS_DHE_RSA_WITH_CAMELLIA_128_CBC_SHA256, 205, 52, 72, 255, 122, CipherSuite.TLS_DHE_PSK_WITH_AES_128_CBC_SHA, 95, 32, 104, 26, CipherSuite.TLS_PSK_WITH_AES_128_CBC_SHA256, CipherSuite.TLS_DHE_PSK_WITH_NULL_SHA256, 84, CipherSuite.TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA, 34, 100, 241, 115, 18, 64, 8, CipherSuite.TLS_DHE_DSS_WITH_CAMELLIA_256_CBC_SHA256, 236, 219, CipherSuite.TLS_DH_RSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_PSK_WITH_AES_256_CBC_SHA, 61, CipherSuite.TLS_DH_DSS_WITH_SEED_CBC_SHA, 0, 207, 43, 118, 130, 214, 27, CipherSuite.TLS_DHE_PSK_WITH_NULL_SHA384, CipherSuite.TLS_PSK_WITH_AES_256_CBC_SHA384, CipherSuite.TLS_DHE_DSS_WITH_AES_256_CBC_SHA256, 80, 69, 243, 48, 239, 63, 85, CipherSuite.TLS_DHE_DSS_WITH_AES_128_GCM_SHA256, 234, ExtensionType.negotiated_ff_dhe_groups, CipherSuite.TLS_RSA_WITH_CAMELLIA_128_CBC_SHA256, 47, 192, 222, 28, 253, 77, CipherSuite.TLS_RSA_PSK_WITH_RC4_128_SHA, 117, 6, CipherSuite.TLS_PSK_WITH_RC4_128_SHA, CipherSuite.TLS_DHE_PSK_WITH_AES_128_CBC_SHA256, 230, 14, 31, 98, 212, CipherSuite.TLS_PSK_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_RSA_WITH_SEED_CBC_SHA, 249, CipherSuite.TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA256, 37, 89, CipherSuite.TLS_RSA_WITH_CAMELLIA_256_CBC_SHA, Ed448.SIGNATURE_SIZE, 57, 76, 94, 120, 56, CipherSuite.TLS_PSK_WITH_AES_128_CBC_SHA, 209, CipherSuite.TLS_DH_DSS_WITH_AES_256_GCM_SHA384, 226, 97, CipherSuite.TLS_DHE_PSK_WITH_AES_256_CBC_SHA384, 33, CipherSuite.TLS_RSA_WITH_AES_128_GCM_SHA256, 30, 67, 199, 252, 4, 81, CipherSuite.TLS_DHE_DSS_WITH_SEED_CBC_SHA, CipherSuite.TLS_DH_anon_WITH_AES_256_CBC_SHA256, 13, 250, 223, 126, 36, 59, CipherSuite.TLS_DHE_PSK_WITH_AES_256_GCM_SHA384, 206, 17, CipherSuite.TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA, 78, CipherSuite.TLS_RSA_PSK_WITH_AES_256_CBC_SHA384, 235, 60, 129, CipherSuite.TLS_RSA_PSK_WITH_AES_128_CBC_SHA, 247, CipherSuite.TLS_RSA_PSK_WITH_NULL_SHA384, 19, 44, Primes.SMALL_FACTOR_LIMIT, 231, 110, CipherSuite.TLS_DHE_RSA_WITH_CAMELLIA_256_CBC_SHA256, 3, 86, 68, CertificateBody.profileType, CipherSuite.TLS_PSK_WITH_AES_256_GCM_SHA384, 42, CipherSuite.TLS_DH_DSS_WITH_CAMELLIA_128_CBC_SHA256, CipherSuite.TLS_DH_DSS_WITH_CAMELLIA_256_CBC_SHA256, 83, 220, 11, CipherSuite.TLS_RSA_WITH_AES_256_GCM_SHA384, CipherSuite.TLS_DH_anon_WITH_AES_128_CBC_SHA256, 49, 116, 246, 70, CipherSuite.TLS_RSA_PSK_WITH_AES_128_GCM_SHA256, CipherSuite.TLS_DH_anon_WITH_CAMELLIA_256_CBC_SHA, 20, 225, 22, 58, CipherSuite.TLS_DH_RSA_WITH_AES_256_CBC_SHA256, 9, 112, CipherSuite.TLS_RSA_PSK_WITH_AES_128_CBC_SHA256, 208, 237, 204, 66, CipherSuite.TLS_DH_RSA_WITH_SEED_CBC_SHA, CipherSuite.TLS_DH_DSS_WITH_AES_128_GCM_SHA256, 40, 92, 248, CipherSuite.TLS_DH_RSA_WITH_CAMELLIA_256_CBC_SHA};
    private long[] _K = new long[8];
    private long[] _L = new long[8];
    private short[] _bitCount = new short[32];
    private long[] _block = new long[8];
    private byte[] _buffer = new byte[64];
    private int _bufferPos = 0;
    private long[] _hash = new long[8];
    private final long[] _rc = new long[11];
    private long[] _state = new long[8];

    static {
        EIGHT[31] = 8;
    }

    public WhirlpoolDigest() {
        for (int i = 0; i < 256; i++) {
            int i2 = SBOX[i];
            int maskWithReductionPolynomial = maskWithReductionPolynomial(i2 << 1);
            int maskWithReductionPolynomial2 = maskWithReductionPolynomial(maskWithReductionPolynomial << 1);
            int i3 = maskWithReductionPolynomial2 ^ i2;
            int maskWithReductionPolynomial3 = maskWithReductionPolynomial(maskWithReductionPolynomial2 << 1);
            int i4 = maskWithReductionPolynomial3 ^ i2;
            int i5 = i2;
            C0[i] = packIntoLong(i2, i5, maskWithReductionPolynomial2, i2, maskWithReductionPolynomial3, i3, maskWithReductionPolynomial, i4);
            int i6 = i2;
            C1[i] = packIntoLong(i4, i5, i6, maskWithReductionPolynomial2, i2, maskWithReductionPolynomial3, i3, maskWithReductionPolynomial);
            int i7 = i2;
            C2[i] = packIntoLong(maskWithReductionPolynomial, i4, i6, i7, maskWithReductionPolynomial2, i2, maskWithReductionPolynomial3, i3);
            int i8 = i2;
            C3[i] = packIntoLong(i3, maskWithReductionPolynomial, i4, i7, i8, maskWithReductionPolynomial2, i2, maskWithReductionPolynomial3);
            int i9 = i2;
            C4[i] = packIntoLong(maskWithReductionPolynomial3, i3, maskWithReductionPolynomial, i4, i8, i9, maskWithReductionPolynomial2, i2);
            int i10 = i2;
            C5[i] = packIntoLong(i2, maskWithReductionPolynomial3, i3, maskWithReductionPolynomial, i4, i9, i10, maskWithReductionPolynomial2);
            int i11 = i2;
            C6[i] = packIntoLong(maskWithReductionPolynomial2, i2, maskWithReductionPolynomial3, i3, maskWithReductionPolynomial, i4, i10, i11);
            C7[i] = packIntoLong(i2, maskWithReductionPolynomial2, i2, maskWithReductionPolynomial3, i3, maskWithReductionPolynomial, i4, i11);
        }
        this._rc[0] = 0;
        for (int i12 = 1; i12 <= 10; i12++) {
            int i13 = (i12 - 1) * 8;
            this._rc[i12] = (((((((C0[i13] & -72057594037927936L) ^ (C1[i13 + 1] & 71776119061217280L)) ^ (C2[i13 + 2] & 280375465082880L)) ^ (C3[i13 + 3] & 1095216660480L)) ^ (C4[i13 + 4] & 4278190080L)) ^ (C5[i13 + 5] & 16711680)) ^ (C6[i13 + 6] & 65280)) ^ (C7[i13 + 7] & 255);
        }
    }

    public WhirlpoolDigest(WhirlpoolDigest whirlpoolDigest) {
        reset(whirlpoolDigest);
    }

    private long bytesToLongFromBuffer(byte[] bArr, int i) {
        return (((long) bArr[i + 7]) & 255) | ((((long) bArr[i + 0]) & 255) << 56) | ((((long) bArr[i + 1]) & 255) << 48) | ((((long) bArr[i + 2]) & 255) << 40) | ((((long) bArr[i + 3]) & 255) << 32) | ((((long) bArr[i + 4]) & 255) << 24) | ((((long) bArr[i + 5]) & 255) << 16) | ((((long) bArr[i + 6]) & 255) << 8);
    }

    private void convertLongToByteArray(long j, byte[] bArr, int i) {
        for (int i2 = 0; i2 < 8; i2++) {
            bArr[i + i2] = (byte) ((int) ((j >> (56 - (i2 * 8))) & 255));
        }
    }

    private byte[] copyBitLength() {
        byte[] bArr = new byte[32];
        for (int i = 0; i < bArr.length; i++) {
            bArr[i] = (byte) (this._bitCount[i] & 255);
        }
        return bArr;
    }

    private void finish() {
        byte[] copyBitLength = copyBitLength();
        byte[] bArr = this._buffer;
        int i = this._bufferPos;
        this._bufferPos = i + 1;
        bArr[i] = (byte) (bArr[i] | 128);
        if (this._bufferPos == this._buffer.length) {
            processFilledBuffer(this._buffer, 0);
        }
        if (this._bufferPos > 32) {
            while (this._bufferPos != 0) {
                update((byte) 0);
            }
        }
        while (this._bufferPos <= 32) {
            update((byte) 0);
        }
        System.arraycopy(copyBitLength, 0, this._buffer, 32, copyBitLength.length);
        processFilledBuffer(this._buffer, 0);
    }

    private void increment() {
        int i = 0;
        for (int length = this._bitCount.length - 1; length >= 0; length--) {
            int i2 = (this._bitCount[length] & 255) + EIGHT[length] + i;
            i = i2 >>> 8;
            this._bitCount[length] = (short) (i2 & 255);
        }
    }

    private int maskWithReductionPolynomial(int i) {
        return ((long) i) >= 256 ? i ^ REDUCTION_POLYNOMIAL : i;
    }

    private long packIntoLong(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        return (((((((((long) i2) << 48) ^ (((long) i) << 56)) ^ (((long) i3) << 40)) ^ (((long) i4) << 32)) ^ (((long) i5) << 24)) ^ (((long) i6) << 16)) ^ (((long) i7) << 8)) ^ ((long) i8);
    }

    private void processFilledBuffer(byte[] bArr, int i) {
        for (int i2 = 0; i2 < this._state.length; i2++) {
            this._block[i2] = bytesToLongFromBuffer(this._buffer, i2 * 8);
        }
        processBlock();
        this._bufferPos = 0;
        Arrays.fill(this._buffer, (byte) 0);
    }

    public Memoable copy() {
        return new WhirlpoolDigest(this);
    }

    public int doFinal(byte[] bArr, int i) {
        finish();
        for (int i2 = 0; i2 < 8; i2++) {
            convertLongToByteArray(this._hash[i2], bArr, (i2 * 8) + i);
        }
        reset();
        return getDigestSize();
    }

    public String getAlgorithmName() {
        return "Whirlpool";
    }

    public int getByteLength() {
        return 64;
    }

    public int getDigestSize() {
        return 64;
    }

    /* access modifiers changed from: protected */
    public void processBlock() {
        for (int i = 0; i < 8; i++) {
            long[] jArr = this._state;
            long j = this._block[i];
            long[] jArr2 = this._K;
            long j2 = this._hash[i];
            jArr2[i] = j2;
            jArr[i] = j ^ j2;
        }
        int i2 = 1;
        while (i2 <= 10) {
            int i3 = 0;
            while (i3 < 8) {
                this._L[i3] = 0;
                long[] jArr3 = this._L;
                jArr3[i3] = C0[((int) (this._K[(i3 + 0) & 7] >>> 56)) & 255] ^ jArr3[i3];
                long[] jArr4 = this._L;
                jArr4[i3] = jArr4[i3] ^ C1[((int) (this._K[(i3 - 1) & 7] >>> 48)) & 255];
                long[] jArr5 = this._L;
                jArr5[i3] = jArr5[i3] ^ C2[((int) (this._K[(i3 - 2) & 7] >>> 40)) & 255];
                long[] jArr6 = this._L;
                jArr6[i3] = jArr6[i3] ^ C3[((int) (this._K[(i3 - 3) & 7] >>> 32)) & 255];
                long[] jArr7 = this._L;
                jArr7[i3] = jArr7[i3] ^ C4[((int) (this._K[(i3 - 4) & 7] >>> 24)) & 255];
                long[] jArr8 = this._L;
                jArr8[i3] = jArr8[i3] ^ C5[((int) (this._K[(i3 - 5) & 7] >>> 16)) & 255];
                long[] jArr9 = this._L;
                jArr9[i3] = jArr9[i3] ^ C6[((int) (this._K[(i3 - 6) & 7] >>> 8)) & 255];
                long[] jArr10 = this._L;
                jArr10[i3] = jArr10[i3] ^ C7[((int) this._K[(i3 - 7) & 7]) & 255];
                i3++;
                i2 = i2;
            }
            int i4 = i2;
            System.arraycopy(this._L, 0, this._K, 0, this._K.length);
            long[] jArr11 = this._K;
            jArr11[0] = jArr11[0] ^ this._rc[i4];
            for (int i5 = 0; i5 < 8; i5++) {
                this._L[i5] = this._K[i5];
                long[] jArr12 = this._L;
                jArr12[i5] = jArr12[i5] ^ C0[((int) (this._state[(i5 + 0) & 7] >>> 56)) & 255];
                long[] jArr13 = this._L;
                jArr13[i5] = jArr13[i5] ^ C1[((int) (this._state[(i5 - 1) & 7] >>> 48)) & 255];
                long[] jArr14 = this._L;
                jArr14[i5] = jArr14[i5] ^ C2[((int) (this._state[(i5 - 2) & 7] >>> 40)) & 255];
                long[] jArr15 = this._L;
                jArr15[i5] = jArr15[i5] ^ C3[((int) (this._state[(i5 - 3) & 7] >>> 32)) & 255];
                long[] jArr16 = this._L;
                jArr16[i5] = jArr16[i5] ^ C4[((int) (this._state[(i5 - 4) & 7] >>> 24)) & 255];
                long[] jArr17 = this._L;
                jArr17[i5] = jArr17[i5] ^ C5[((int) (this._state[(i5 - 5) & 7] >>> 16)) & 255];
                long[] jArr18 = this._L;
                jArr18[i5] = jArr18[i5] ^ C6[((int) (this._state[(i5 - 6) & 7] >>> 8)) & 255];
                long[] jArr19 = this._L;
                jArr19[i5] = jArr19[i5] ^ C7[((int) this._state[(i5 - 7) & 7]) & 255];
            }
            System.arraycopy(this._L, 0, this._state, 0, this._state.length);
            i2 = i4 + 1;
        }
        for (int i6 = 0; i6 < 8; i6++) {
            long[] jArr20 = this._hash;
            jArr20[i6] = jArr20[i6] ^ (this._state[i6] ^ this._block[i6]);
        }
    }

    public void reset() {
        this._bufferPos = 0;
        Arrays.fill(this._bitCount, 0);
        Arrays.fill(this._buffer, (byte) 0);
        Arrays.fill(this._hash, 0);
        Arrays.fill(this._K, 0);
        Arrays.fill(this._L, 0);
        Arrays.fill(this._block, 0);
        Arrays.fill(this._state, 0);
    }

    public void reset(Memoable memoable) {
        WhirlpoolDigest whirlpoolDigest = (WhirlpoolDigest) memoable;
        System.arraycopy(whirlpoolDigest._rc, 0, this._rc, 0, this._rc.length);
        System.arraycopy(whirlpoolDigest._buffer, 0, this._buffer, 0, this._buffer.length);
        this._bufferPos = whirlpoolDigest._bufferPos;
        System.arraycopy(whirlpoolDigest._bitCount, 0, this._bitCount, 0, this._bitCount.length);
        System.arraycopy(whirlpoolDigest._hash, 0, this._hash, 0, this._hash.length);
        System.arraycopy(whirlpoolDigest._K, 0, this._K, 0, this._K.length);
        System.arraycopy(whirlpoolDigest._L, 0, this._L, 0, this._L.length);
        System.arraycopy(whirlpoolDigest._block, 0, this._block, 0, this._block.length);
        System.arraycopy(whirlpoolDigest._state, 0, this._state, 0, this._state.length);
    }

    public void update(byte b) {
        this._buffer[this._bufferPos] = b;
        this._bufferPos++;
        if (this._bufferPos == this._buffer.length) {
            processFilledBuffer(this._buffer, 0);
        }
        increment();
    }

    public void update(byte[] bArr, int i, int i2) {
        while (i2 > 0) {
            update(bArr[i]);
            i++;
            i2--;
        }
    }
}
