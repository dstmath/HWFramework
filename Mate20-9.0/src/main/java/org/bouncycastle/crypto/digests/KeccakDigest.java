package org.bouncycastle.crypto.digests;

import org.bouncycastle.crypto.ExtendedDigest;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class KeccakDigest implements ExtendedDigest {
    private static long[] KeccakRoundConstants = {1, 32898, -9223372036854742902L, -9223372034707259392L, 32907, 2147483649L, -9223372034707259263L, -9223372036854743031L, 138, 136, 2147516425L, 2147483658L, 2147516555L, -9223372036854775669L, -9223372036854742903L, -9223372036854743037L, -9223372036854743038L, -9223372036854775680L, 32778, -9223372034707292150L, -9223372034707259263L, -9223372036854742912L, 2147483649L, -9223372034707259384L};
    protected int bitsInQueue;
    protected byte[] dataQueue;
    protected int fixedOutputLength;
    protected int rate;
    protected boolean squeezing;
    protected long[] state;

    public KeccakDigest() {
        this(288);
    }

    public KeccakDigest(int i) {
        this.state = new long[25];
        this.dataQueue = new byte[192];
        init(i);
    }

    public KeccakDigest(KeccakDigest keccakDigest) {
        this.state = new long[25];
        this.dataQueue = new byte[192];
        System.arraycopy(keccakDigest.state, 0, this.state, 0, keccakDigest.state.length);
        System.arraycopy(keccakDigest.dataQueue, 0, this.dataQueue, 0, keccakDigest.dataQueue.length);
        this.rate = keccakDigest.rate;
        this.bitsInQueue = keccakDigest.bitsInQueue;
        this.fixedOutputLength = keccakDigest.fixedOutputLength;
        this.squeezing = keccakDigest.squeezing;
    }

    private void KeccakAbsorb(byte[] bArr, int i) {
        int i2 = this.rate >> 6;
        for (int i3 = 0; i3 < i2; i3++) {
            long[] jArr = this.state;
            jArr[i3] = jArr[i3] ^ Pack.littleEndianToLong(bArr, i);
            i += 8;
        }
        KeccakPermutation();
    }

    private void KeccakExtract() {
        Pack.longToLittleEndian(this.state, 0, this.rate >> 6, this.dataQueue, 0);
    }

    private void KeccakPermutation() {
        long[] jArr = this.state;
        char c = 0;
        long j = jArr[0];
        boolean z = true;
        long j2 = jArr[1];
        long j3 = jArr[2];
        char c2 = 3;
        long j4 = jArr[3];
        long j5 = jArr[4];
        long j6 = jArr[5];
        long j7 = jArr[6];
        long j8 = jArr[7];
        long j9 = jArr[8];
        long j10 = jArr[9];
        long j11 = jArr[10];
        long j12 = jArr[11];
        long j13 = jArr[12];
        long j14 = jArr[13];
        long j15 = jArr[14];
        long j16 = jArr[15];
        long j17 = jArr[16];
        long j18 = jArr[17];
        long j19 = jArr[18];
        long j20 = jArr[19];
        long j21 = jArr[20];
        long j22 = jArr[21];
        long j23 = jArr[22];
        long j24 = jArr[23];
        long j25 = jArr[24];
        long j26 = j20;
        long j27 = j15;
        long j28 = j10;
        long j29 = j5;
        long j30 = j4;
        long j31 = j3;
        long j32 = j2;
        long j33 = j;
        int i = 0;
        while (i < 24) {
            long j34 = (((j33 ^ j6) ^ j11) ^ j16) ^ j21;
            long j35 = (((j32 ^ j7) ^ j12) ^ j17) ^ j22;
            long j36 = (((j31 ^ j8) ^ j13) ^ j18) ^ j23;
            long j37 = (((j30 ^ j9) ^ j14) ^ j19) ^ j24;
            long j38 = (((j29 ^ j28) ^ j27) ^ j26) ^ j25;
            long j39 = ((j35 << (z ? 1 : 0)) | (j35 >>> -1)) ^ j38;
            long j40 = ((j36 << z) | (j36 >>> -1)) ^ j34;
            long j41 = ((j37 << z) | (j37 >>> -1)) ^ j35;
            long j42 = ((j38 << z) | (j38 >>> -1)) ^ j36;
            long j43 = ((j34 << z) | (j34 >>> -1)) ^ j37;
            long j44 = j6 ^ j39;
            long j45 = j11 ^ j39;
            long j46 = j16 ^ j39;
            long j47 = j21 ^ j39;
            long j48 = j32 ^ j40;
            long j49 = j7 ^ j40;
            long j50 = j12 ^ j40;
            long j51 = j17 ^ j40;
            long j52 = j22 ^ j40;
            long j53 = j8 ^ j41;
            long j54 = j13 ^ j41;
            long j55 = j18 ^ j41;
            long j56 = j23 ^ j41;
            long j57 = j30 ^ j42;
            long j58 = j9 ^ j42;
            long j59 = j14 ^ j42;
            long j60 = j19 ^ j42;
            long j61 = j24 ^ j42;
            long j62 = j29 ^ j43;
            long j63 = j28 ^ j43;
            long j64 = j27 ^ j43;
            long j65 = j26 ^ j43;
            long j66 = j25 ^ j43;
            long j67 = (j48 << z) | (j48 >>> 63);
            int i2 = i;
            long j68 = (j49 << 44) | (j49 >>> 20);
            long j69 = j33 ^ j39;
            long j70 = (j63 << 20) | (j63 >>> 44);
            long j71 = j67;
            long j72 = j31 ^ j41;
            long j73 = (j64 << 39) | (j64 >>> 25);
            long j74 = (j72 << 62) | (j72 >>> 2);
            long j75 = (j54 << 43) | (j54 >>> 21);
            long j76 = (j47 << 18) | (j47 >>> 46);
            long j77 = (j59 << 25) | (j59 >>> 39);
            long j78 = (j65 << 8) | (j65 >>> 56);
            long j79 = (j61 << 56) | (j61 >>> 8);
            long j80 = (j46 << 41) | (j46 >>> 23);
            long j81 = (j62 << 27) | (j62 >>> 37);
            long j82 = (j66 << 14) | (j66 >>> 50);
            long j83 = (j56 << 61) | (j56 >>> c2);
            long j84 = (j52 << 2) | (j52 >>> 62);
            long j85 = (j58 << 55) | (j58 >>> 9);
            long j86 = (j51 << 45) | (j51 >>> 19);
            long j87 = (j44 << 36) | (j44 >>> 28);
            long j88 = (j60 << 21) | (j60 >>> 43);
            long j89 = (j57 >>> 36) | (j57 << 28);
            long j90 = (j55 << 15) | (j55 >>> 49);
            long j91 = (j50 << 10) | (j50 >>> 54);
            long j92 = (j53 << 6) | (j53 >>> 58);
            long j93 = (j45 << 3) | (j45 >>> 61);
            long j94 = j69 ^ ((~j68) & j75);
            long j95 = ((~j75) & j88) ^ j68;
            j31 = j75 ^ ((~j88) & j82);
            j30 = ((~j82) & j69) ^ j88;
            j29 = j82 ^ (j68 & (~j69));
            j6 = j89 ^ ((~j70) & j93);
            long j96 = j93;
            j7 = j70 ^ ((~j96) & j86);
            long j97 = j86;
            j8 = j96 ^ ((~j97) & j83);
            long j98 = j83;
            j9 = j97 ^ ((~j98) & j89);
            j28 = j98 ^ ((~j89) & j70);
            long j99 = j92;
            j11 = j71 ^ ((~j99) & j77);
            long j100 = j77;
            j12 = j99 ^ ((~j100) & j78);
            long j101 = j78;
            j13 = j100 ^ ((~j101) & j76);
            long j102 = j76;
            j14 = j101 ^ ((~j102) & j71);
            j27 = j102 ^ (j99 & (~j71));
            long j103 = j87;
            j16 = j81 ^ ((~j103) & j91);
            long j104 = j91;
            j17 = j103 ^ ((~j104) & j90);
            long j105 = j90;
            j18 = j104 ^ ((~j105) & j79);
            long j106 = j79;
            j19 = j105 ^ ((~j106) & j81);
            j26 = j106 ^ (j103 & (~j81));
            long j107 = j85;
            j21 = j74 ^ ((~j107) & j73);
            long j108 = j73;
            j22 = j107 ^ ((~j108) & j80);
            long j109 = j80;
            j23 = j108 ^ ((~j109) & j84);
            long j110 = j84;
            j24 = j109 ^ ((~j110) & j74);
            j25 = j110 ^ (j107 & (~j74));
            j33 = j94 ^ KeccakRoundConstants[i2];
            i = i2 + 1;
            j32 = j95;
            c = 0;
            z = true;
            c2 = 3;
        }
        jArr[c] = j33;
        jArr[1] = j32;
        jArr[2] = j31;
        jArr[3] = j30;
        jArr[4] = j29;
        jArr[5] = j6;
        jArr[6] = j7;
        jArr[7] = j8;
        jArr[8] = j9;
        jArr[9] = j28;
        jArr[10] = j11;
        jArr[11] = j12;
        jArr[12] = j13;
        jArr[13] = j14;
        jArr[14] = j27;
        jArr[15] = j16;
        jArr[16] = j17;
        jArr[17] = j18;
        jArr[18] = j19;
        jArr[19] = j26;
        jArr[20] = j21;
        jArr[21] = j22;
        jArr[22] = j23;
        jArr[23] = j24;
        jArr[24] = j25;
    }

    private void init(int i) {
        if (i == 128 || i == 224 || i == 256 || i == 288 || i == 384 || i == 512) {
            initSponge(1600 - (i << 1));
            return;
        }
        throw new IllegalArgumentException("bitLength must be one of 128, 224, 256, 288, 384, or 512.");
    }

    private void initSponge(int i) {
        if (i <= 0 || i >= 1600 || i % 64 != 0) {
            throw new IllegalStateException("invalid rate value");
        }
        this.rate = i;
        for (int i2 = 0; i2 < this.state.length; i2++) {
            this.state[i2] = 0;
        }
        Arrays.fill(this.dataQueue, (byte) 0);
        this.bitsInQueue = 0;
        this.squeezing = false;
        this.fixedOutputLength = (1600 - i) / 2;
    }

    private void padAndSwitchToSqueezingPhase() {
        byte[] bArr = this.dataQueue;
        int i = this.bitsInQueue >> 3;
        bArr[i] = (byte) (bArr[i] | ((byte) ((int) (1 << (this.bitsInQueue & 7)))));
        int i2 = this.bitsInQueue + 1;
        this.bitsInQueue = i2;
        if (i2 == this.rate) {
            KeccakAbsorb(this.dataQueue, 0);
            this.bitsInQueue = 0;
        }
        int i3 = this.bitsInQueue >> 6;
        int i4 = this.bitsInQueue & 63;
        int i5 = 0;
        for (int i6 = 0; i6 < i3; i6++) {
            long[] jArr = this.state;
            jArr[i6] = jArr[i6] ^ Pack.littleEndianToLong(this.dataQueue, i5);
            i5 += 8;
        }
        if (i4 > 0) {
            long[] jArr2 = this.state;
            jArr2[i3] = (((1 << i4) - 1) & Pack.littleEndianToLong(this.dataQueue, i5)) ^ jArr2[i3];
        }
        long[] jArr3 = this.state;
        int i7 = (this.rate - 1) >> 6;
        jArr3[i7] = jArr3[i7] ^ Long.MIN_VALUE;
        KeccakPermutation();
        KeccakExtract();
        this.bitsInQueue = this.rate;
        this.squeezing = true;
    }

    /* access modifiers changed from: protected */
    public void absorb(byte[] bArr, int i, int i2) {
        if (this.bitsInQueue % 8 != 0) {
            throw new IllegalStateException("attempt to absorb with odd length queue");
        } else if (!this.squeezing) {
            int i3 = this.rate >> 3;
            int i4 = this.bitsInQueue >> 3;
            int i5 = 0;
            while (i5 < i2) {
                if (i4 == 0) {
                    int i6 = i2 - i3;
                    if (i5 <= i6) {
                        do {
                            KeccakAbsorb(bArr, i + i5);
                            i5 += i3;
                        } while (i5 <= i6);
                    }
                }
                int min = Math.min(i3 - i4, i2 - i5);
                System.arraycopy(bArr, i + i5, this.dataQueue, i4, min);
                i4 += min;
                i5 += min;
                if (i4 == i3) {
                    KeccakAbsorb(this.dataQueue, 0);
                    i4 = 0;
                }
            }
            this.bitsInQueue = i4 << 3;
        } else {
            throw new IllegalStateException("attempt to absorb while squeezing");
        }
    }

    /* access modifiers changed from: protected */
    public void absorbBits(int i, int i2) {
        if (i2 < 1 || i2 > 7) {
            throw new IllegalArgumentException("'bits' must be in the range 1 to 7");
        } else if (this.bitsInQueue % 8 != 0) {
            throw new IllegalStateException("attempt to absorb with odd length queue");
        } else if (!this.squeezing) {
            this.dataQueue[this.bitsInQueue >> 3] = (byte) (i & ((1 << i2) - 1));
            this.bitsInQueue += i2;
        } else {
            throw new IllegalStateException("attempt to absorb while squeezing");
        }
    }

    public int doFinal(byte[] bArr, int i) {
        squeeze(bArr, i, (long) this.fixedOutputLength);
        reset();
        return getDigestSize();
    }

    /* access modifiers changed from: protected */
    public int doFinal(byte[] bArr, int i, byte b, int i2) {
        if (i2 > 0) {
            absorbBits(b, i2);
        }
        squeeze(bArr, i, (long) this.fixedOutputLength);
        reset();
        return getDigestSize();
    }

    public String getAlgorithmName() {
        return "Keccak-" + this.fixedOutputLength;
    }

    public int getByteLength() {
        return this.rate / 8;
    }

    public int getDigestSize() {
        return this.fixedOutputLength / 8;
    }

    public void reset() {
        init(this.fixedOutputLength);
    }

    /* access modifiers changed from: protected */
    public void squeeze(byte[] bArr, int i, long j) {
        if (!this.squeezing) {
            padAndSwitchToSqueezingPhase();
        }
        long j2 = 0;
        if (j % 8 == 0) {
            while (j2 < j) {
                if (this.bitsInQueue == 0) {
                    KeccakPermutation();
                    KeccakExtract();
                    this.bitsInQueue = this.rate;
                }
                int min = (int) Math.min((long) this.bitsInQueue, j - j2);
                System.arraycopy(this.dataQueue, (this.rate - this.bitsInQueue) / 8, bArr, ((int) (j2 / 8)) + i, min / 8);
                this.bitsInQueue -= min;
                j2 += (long) min;
            }
            return;
        }
        throw new IllegalStateException("outputLength not a multiple of 8");
    }

    public void update(byte b) {
        absorb(new byte[]{b}, 0, 1);
    }

    public void update(byte[] bArr, int i, int i2) {
        absorb(bArr, i, i2);
    }
}
