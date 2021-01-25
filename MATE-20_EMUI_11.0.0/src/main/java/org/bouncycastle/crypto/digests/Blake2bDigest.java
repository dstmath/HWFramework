package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class Blake2bDigest implements ExtendedDigest {
    private static final int BLOCK_LENGTH_BYTES = 128;
    private static int ROUNDS = 12;
    private static final long[] blake2b_IV = {7640891576956012808L, -4942790177534073029L, 4354685564936845355L, -6534734903238641935L, 5840696475078001361L, -7276294671716946913L, 2270897969802886507L, 6620516959819538809L};
    private static final byte[][] blake2b_sigma = {new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, new byte[]{14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3}, new byte[]{11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4}, new byte[]{7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8}, new byte[]{9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13}, new byte[]{2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9}, new byte[]{12, 5, 1, 15, 14, 13, 4, 10, 0, 7, 6, 3, 9, 2, 8, 11}, new byte[]{13, 11, 7, 14, 12, 1, 3, 9, 5, 0, 15, 4, 8, 6, 2, 10}, new byte[]{6, 15, 14, 9, 11, 3, 0, 8, 12, 2, 13, 7, 1, 4, 10, 5}, new byte[]{10, 2, 8, 4, 7, 6, 1, 5, 15, 11, 9, 14, 3, 12, 13, 0}, new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, new byte[]{14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3}};
    private byte[] buffer;
    private int bufferPos;
    private long[] chainValue;
    private int digestLength;
    private long f0;
    private long[] internalState;
    private byte[] key;
    private int keyLength;
    private byte[] personalization;
    private byte[] salt;
    private long t0;
    private long t1;

    public Blake2bDigest() {
        this(512);
    }

    public Blake2bDigest(int i) {
        this.digestLength = 64;
        this.keyLength = 0;
        this.salt = null;
        this.personalization = null;
        this.key = null;
        this.buffer = null;
        this.bufferPos = 0;
        this.internalState = new long[16];
        this.chainValue = null;
        this.t0 = 0;
        this.t1 = 0;
        this.f0 = 0;
        if (i < 8 || i > 512 || i % 8 != 0) {
            throw new IllegalArgumentException("BLAKE2b digest bit length must be a multiple of 8 and not greater than 512");
        }
        this.buffer = new byte[128];
        this.keyLength = 0;
        this.digestLength = i / 8;
        init();
    }

    public Blake2bDigest(Blake2bDigest blake2bDigest) {
        this.digestLength = 64;
        this.keyLength = 0;
        this.salt = null;
        this.personalization = null;
        this.key = null;
        this.buffer = null;
        this.bufferPos = 0;
        this.internalState = new long[16];
        this.chainValue = null;
        this.t0 = 0;
        this.t1 = 0;
        this.f0 = 0;
        this.bufferPos = blake2bDigest.bufferPos;
        this.buffer = Arrays.clone(blake2bDigest.buffer);
        this.keyLength = blake2bDigest.keyLength;
        this.key = Arrays.clone(blake2bDigest.key);
        this.digestLength = blake2bDigest.digestLength;
        this.chainValue = Arrays.clone(blake2bDigest.chainValue);
        this.personalization = Arrays.clone(blake2bDigest.personalization);
        this.salt = Arrays.clone(blake2bDigest.salt);
        this.t0 = blake2bDigest.t0;
        this.t1 = blake2bDigest.t1;
        this.f0 = blake2bDigest.f0;
    }

    public Blake2bDigest(byte[] bArr) {
        this.digestLength = 64;
        this.keyLength = 0;
        this.salt = null;
        this.personalization = null;
        this.key = null;
        this.buffer = null;
        this.bufferPos = 0;
        this.internalState = new long[16];
        this.chainValue = null;
        this.t0 = 0;
        this.t1 = 0;
        this.f0 = 0;
        this.buffer = new byte[128];
        if (bArr != null) {
            this.key = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.key, 0, bArr.length);
            if (bArr.length <= 64) {
                this.keyLength = bArr.length;
                System.arraycopy(bArr, 0, this.buffer, 0, bArr.length);
                this.bufferPos = 128;
            } else {
                throw new IllegalArgumentException("Keys > 64 are not supported");
            }
        }
        this.digestLength = 64;
        init();
    }

    public Blake2bDigest(byte[] bArr, int i, byte[] bArr2, byte[] bArr3) {
        this.digestLength = 64;
        this.keyLength = 0;
        this.salt = null;
        this.personalization = null;
        this.key = null;
        this.buffer = null;
        this.bufferPos = 0;
        this.internalState = new long[16];
        this.chainValue = null;
        this.t0 = 0;
        this.t1 = 0;
        this.f0 = 0;
        this.buffer = new byte[128];
        if (i < 1 || i > 64) {
            throw new IllegalArgumentException("Invalid digest length (required: 1 - 64)");
        }
        this.digestLength = i;
        if (bArr2 != null) {
            if (bArr2.length == 16) {
                this.salt = new byte[16];
                System.arraycopy(bArr2, 0, this.salt, 0, bArr2.length);
            } else {
                throw new IllegalArgumentException("salt length must be exactly 16 bytes");
            }
        }
        if (bArr3 != null) {
            if (bArr3.length == 16) {
                this.personalization = new byte[16];
                System.arraycopy(bArr3, 0, this.personalization, 0, bArr3.length);
            } else {
                throw new IllegalArgumentException("personalization length must be exactly 16 bytes");
            }
        }
        if (bArr != null) {
            this.key = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.key, 0, bArr.length);
            if (bArr.length <= 64) {
                this.keyLength = bArr.length;
                System.arraycopy(bArr, 0, this.buffer, 0, bArr.length);
                this.bufferPos = 128;
            } else {
                throw new IllegalArgumentException("Keys > 64 are not supported");
            }
        }
        init();
    }

    private void G(long j, long j2, int i, int i2, int i3, int i4) {
        long[] jArr = this.internalState;
        jArr[i] = jArr[i] + jArr[i2] + j;
        jArr[i4] = rotr64(jArr[i4] ^ jArr[i], 32);
        long[] jArr2 = this.internalState;
        jArr2[i3] = jArr2[i3] + jArr2[i4];
        jArr2[i2] = rotr64(jArr2[i2] ^ jArr2[i3], 24);
        long[] jArr3 = this.internalState;
        jArr3[i] = jArr3[i] + jArr3[i2] + j2;
        jArr3[i4] = rotr64(jArr3[i4] ^ jArr3[i], 16);
        long[] jArr4 = this.internalState;
        jArr4[i3] = jArr4[i3] + jArr4[i4];
        jArr4[i2] = rotr64(jArr4[i2] ^ jArr4[i3], 63);
    }

    private void compress(byte[] bArr, int i) {
        initializeInternalState();
        long[] jArr = new long[16];
        int i2 = 0;
        for (int i3 = 0; i3 < 16; i3++) {
            jArr[i3] = Pack.littleEndianToLong(bArr, (i3 * 8) + i);
        }
        for (int i4 = 0; i4 < ROUNDS; i4++) {
            byte[][] bArr2 = blake2b_sigma;
            G(jArr[bArr2[i4][0]], jArr[bArr2[i4][1]], 0, 4, 8, 12);
            byte[][] bArr3 = blake2b_sigma;
            G(jArr[bArr3[i4][2]], jArr[bArr3[i4][3]], 1, 5, 9, 13);
            byte[][] bArr4 = blake2b_sigma;
            G(jArr[bArr4[i4][4]], jArr[bArr4[i4][5]], 2, 6, 10, 14);
            byte[][] bArr5 = blake2b_sigma;
            G(jArr[bArr5[i4][6]], jArr[bArr5[i4][7]], 3, 7, 11, 15);
            byte[][] bArr6 = blake2b_sigma;
            G(jArr[bArr6[i4][8]], jArr[bArr6[i4][9]], 0, 5, 10, 15);
            byte[][] bArr7 = blake2b_sigma;
            G(jArr[bArr7[i4][10]], jArr[bArr7[i4][11]], 1, 6, 11, 12);
            byte[][] bArr8 = blake2b_sigma;
            G(jArr[bArr8[i4][12]], jArr[bArr8[i4][13]], 2, 7, 8, 13);
            byte[][] bArr9 = blake2b_sigma;
            G(jArr[bArr9[i4][14]], jArr[bArr9[i4][15]], 3, 4, 9, 14);
        }
        while (true) {
            long[] jArr2 = this.chainValue;
            if (i2 < jArr2.length) {
                long j = jArr2[i2];
                long[] jArr3 = this.internalState;
                jArr2[i2] = (j ^ jArr3[i2]) ^ jArr3[i2 + 8];
                i2++;
            } else {
                return;
            }
        }
    }

    private void init() {
        if (this.chainValue == null) {
            this.chainValue = new long[8];
            long[] jArr = this.chainValue;
            long[] jArr2 = blake2b_IV;
            jArr[0] = jArr2[0] ^ ((long) ((this.digestLength | (this.keyLength << 8)) | 16842752));
            jArr[1] = jArr2[1];
            jArr[2] = jArr2[2];
            jArr[3] = jArr2[3];
            jArr[4] = jArr2[4];
            jArr[5] = jArr2[5];
            byte[] bArr = this.salt;
            if (bArr != null) {
                jArr[4] = jArr[4] ^ Pack.littleEndianToLong(bArr, 0);
                long[] jArr3 = this.chainValue;
                jArr3[5] = jArr3[5] ^ Pack.littleEndianToLong(this.salt, 8);
            }
            long[] jArr4 = this.chainValue;
            long[] jArr5 = blake2b_IV;
            jArr4[6] = jArr5[6];
            jArr4[7] = jArr5[7];
            byte[] bArr2 = this.personalization;
            if (bArr2 != null) {
                jArr4[6] = Pack.littleEndianToLong(bArr2, 0) ^ jArr4[6];
                long[] jArr6 = this.chainValue;
                jArr6[7] = jArr6[7] ^ Pack.littleEndianToLong(this.personalization, 8);
            }
        }
    }

    private void initializeInternalState() {
        long[] jArr = this.chainValue;
        System.arraycopy(jArr, 0, this.internalState, 0, jArr.length);
        System.arraycopy(blake2b_IV, 0, this.internalState, this.chainValue.length, 4);
        long[] jArr2 = this.internalState;
        long j = this.t0;
        long[] jArr3 = blake2b_IV;
        jArr2[12] = j ^ jArr3[4];
        jArr2[13] = this.t1 ^ jArr3[5];
        jArr2[14] = this.f0 ^ jArr3[6];
        jArr2[15] = jArr3[7];
    }

    private static long rotr64(long j, int i) {
        return (j << (64 - i)) | (j >>> i);
    }

    public void clearKey() {
        byte[] bArr = this.key;
        if (bArr != null) {
            Arrays.fill(bArr, (byte) 0);
            Arrays.fill(this.buffer, (byte) 0);
        }
    }

    public void clearSalt() {
        byte[] bArr = this.salt;
        if (bArr != null) {
            Arrays.fill(bArr, (byte) 0);
        }
    }

    @Override // org.bouncycastle.crypto.Digest
    public int doFinal(byte[] bArr, int i) {
        int i2;
        this.f0 = -1;
        long j = this.t0;
        int i3 = this.bufferPos;
        this.t0 = j + ((long) i3);
        if (i3 > 0 && this.t0 == 0) {
            this.t1++;
        }
        compress(this.buffer, 0);
        Arrays.fill(this.buffer, (byte) 0);
        Arrays.fill(this.internalState, 0);
        int i4 = 0;
        while (true) {
            long[] jArr = this.chainValue;
            if (i4 >= jArr.length || (i2 = i4 * 8) >= this.digestLength) {
                break;
            }
            byte[] longToLittleEndian = Pack.longToLittleEndian(jArr[i4]);
            int i5 = this.digestLength;
            if (i2 < i5 - 8) {
                System.arraycopy(longToLittleEndian, 0, bArr, i2 + i, 8);
            } else {
                System.arraycopy(longToLittleEndian, 0, bArr, i + i2, i5 - i2);
            }
            i4++;
        }
        Arrays.fill(this.chainValue, 0);
        reset();
        return this.digestLength;
    }

    @Override // org.bouncycastle.crypto.Digest
    public String getAlgorithmName() {
        return "BLAKE2b";
    }

    @Override // org.bouncycastle.crypto.ExtendedDigest
    public int getByteLength() {
        return 128;
    }

    @Override // org.bouncycastle.crypto.Digest
    public int getDigestSize() {
        return this.digestLength;
    }

    @Override // org.bouncycastle.crypto.Digest
    public void reset() {
        this.bufferPos = 0;
        this.f0 = 0;
        this.t0 = 0;
        this.t1 = 0;
        this.chainValue = null;
        Arrays.fill(this.buffer, (byte) 0);
        byte[] bArr = this.key;
        if (bArr != null) {
            System.arraycopy(bArr, 0, this.buffer, 0, bArr.length);
            this.bufferPos = 128;
        }
        init();
    }

    @Override // org.bouncycastle.crypto.Digest
    public void update(byte b) {
        int i = this.bufferPos;
        if (128 - i == 0) {
            this.t0 += 128;
            if (this.t0 == 0) {
                this.t1++;
            }
            compress(this.buffer, 0);
            Arrays.fill(this.buffer, (byte) 0);
            this.buffer[0] = b;
            this.bufferPos = 1;
            return;
        }
        this.buffer[i] = b;
        this.bufferPos = i + 1;
    }

    /* JADX WARNING: Removed duplicated region for block: B:16:0x0048  */
    @Override // org.bouncycastle.crypto.Digest
    public void update(byte[] bArr, int i, int i2) {
        int i3;
        int i4;
        int i5;
        if (bArr != null && i2 != 0) {
            int i6 = this.bufferPos;
            if (i6 != 0) {
                i3 = 128 - i6;
                if (i3 < i2) {
                    System.arraycopy(bArr, i, this.buffer, i6, i3);
                    this.t0 += 128;
                    if (this.t0 == 0) {
                        this.t1++;
                    }
                    compress(this.buffer, 0);
                    this.bufferPos = 0;
                    Arrays.fill(this.buffer, (byte) 0);
                    int i7 = i2 + i;
                    i4 = i7 - 128;
                    i5 = i + i3;
                    while (i5 < i4) {
                        this.t0 += 128;
                        if (this.t0 == 0) {
                            this.t1++;
                        }
                        compress(bArr, i5);
                        i5 += 128;
                    }
                    i2 = i7 - i5;
                    System.arraycopy(bArr, i5, this.buffer, 0, i2);
                } else {
                    System.arraycopy(bArr, i, this.buffer, i6, i2);
                }
            } else {
                i3 = 0;
                int i72 = i2 + i;
                i4 = i72 - 128;
                i5 = i + i3;
                while (i5 < i4) {
                }
                i2 = i72 - i5;
                System.arraycopy(bArr, i5, this.buffer, 0, i2);
            }
            this.bufferPos += i2;
        }
    }
}
