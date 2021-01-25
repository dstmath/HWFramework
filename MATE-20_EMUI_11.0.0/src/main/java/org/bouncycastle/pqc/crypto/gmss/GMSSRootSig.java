package org.bouncycastle.pqc.crypto.gmss;

import java.lang.reflect.Array;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.pqc.crypto.gmss.util.GMSSRandom;
import org.bouncycastle.util.encoders.Hex;

public class GMSSRootSig {
    private long big8;
    private int checksum;
    private int counter;
    private GMSSRandom gmssRandom;
    private byte[] hash;
    private int height;
    private int ii;
    private int k;
    private int keysize;
    private int mdsize;
    private Digest messDigestOTS;
    private int messagesize;
    private byte[] privateKeyOTS;
    private int r;
    private byte[] seed;
    private byte[] sign;
    private int steps;
    private int test;
    private long test8;
    private int w;

    public GMSSRootSig(Digest digest, int i, int i2) {
        this.messDigestOTS = digest;
        this.gmssRandom = new GMSSRandom(this.messDigestOTS);
        this.mdsize = this.messDigestOTS.getDigestSize();
        this.w = i;
        this.height = i2;
        this.k = (1 << i) - 1;
        this.messagesize = (int) Math.ceil(((double) (this.mdsize << 3)) / ((double) i));
    }

    public GMSSRootSig(Digest digest, byte[][] bArr, int[] iArr) {
        this.messDigestOTS = digest;
        this.gmssRandom = new GMSSRandom(this.messDigestOTS);
        this.counter = iArr[0];
        this.test = iArr[1];
        this.ii = iArr[2];
        this.r = iArr[3];
        this.steps = iArr[4];
        this.keysize = iArr[5];
        this.height = iArr[6];
        this.w = iArr[7];
        this.checksum = iArr[8];
        this.mdsize = this.messDigestOTS.getDigestSize();
        int i = this.w;
        this.k = (1 << i) - 1;
        this.messagesize = (int) Math.ceil(((double) (this.mdsize << 3)) / ((double) i));
        this.privateKeyOTS = bArr[0];
        this.seed = bArr[1];
        this.hash = bArr[2];
        this.sign = bArr[3];
        this.test8 = (((long) (bArr[4][2] & 255)) << 16) | ((long) (bArr[4][0] & 255)) | (((long) (bArr[4][1] & 255)) << 8) | (((long) (bArr[4][3] & 255)) << 24) | (((long) (bArr[4][4] & 255)) << 32) | (((long) (bArr[4][5] & 255)) << 40) | (((long) (bArr[4][6] & 255)) << 48) | (((long) (bArr[4][7] & 255)) << 56);
        this.big8 = (((long) (bArr[4][15] & 255)) << 56) | ((long) (bArr[4][8] & 255)) | (((long) (bArr[4][9] & 255)) << 8) | (((long) (bArr[4][10] & 255)) << 16) | (((long) (bArr[4][11] & 255)) << 24) | (((long) (bArr[4][12] & 255)) << 32) | (((long) (bArr[4][13] & 255)) << 40) | (((long) (bArr[4][14] & 255)) << 48);
    }

    private void oneStep() {
        long j;
        int i;
        int i2 = this.w;
        if (8 % i2 == 0) {
            int i3 = this.test;
            if (i3 == 0) {
                this.privateKeyOTS = this.gmssRandom.nextSeed(this.seed);
                int i4 = this.ii;
                if (i4 < this.mdsize) {
                    byte[] bArr = this.hash;
                    this.test = bArr[i4] & this.k;
                    bArr[i4] = (byte) (bArr[i4] >>> this.w);
                } else {
                    int i5 = this.checksum;
                    this.test = this.k & i5;
                    this.checksum = i5 >>> this.w;
                }
            } else if (i3 > 0) {
                Digest digest = this.messDigestOTS;
                byte[] bArr2 = this.privateKeyOTS;
                digest.update(bArr2, 0, bArr2.length);
                this.privateKeyOTS = new byte[this.messDigestOTS.getDigestSize()];
                this.messDigestOTS.doFinal(this.privateKeyOTS, 0);
                this.test--;
            }
            if (this.test == 0) {
                byte[] bArr3 = this.privateKeyOTS;
                byte[] bArr4 = this.sign;
                int i6 = this.counter;
                int i7 = this.mdsize;
                System.arraycopy(bArr3, 0, bArr4, i6 * i7, i7);
                this.counter++;
                if (this.counter % (8 / this.w) == 0) {
                    this.ii++;
                    return;
                }
                return;
            }
            return;
        }
        if (i2 < 8) {
            int i8 = this.test;
            if (i8 == 0) {
                int i9 = this.counter;
                if (i9 % 8 == 0 && this.ii < (i = this.mdsize)) {
                    this.big8 = 0;
                    if (i9 < ((i / i2) << 3)) {
                        for (int i10 = 0; i10 < this.w; i10++) {
                            long j2 = this.big8;
                            byte[] bArr5 = this.hash;
                            int i11 = this.ii;
                            this.big8 = j2 ^ ((long) ((bArr5[i11] & 255) << (i10 << 3)));
                            this.ii = i11 + 1;
                        }
                    } else {
                        for (int i12 = 0; i12 < this.mdsize % this.w; i12++) {
                            long j3 = this.big8;
                            byte[] bArr6 = this.hash;
                            int i13 = this.ii;
                            this.big8 = j3 ^ ((long) ((bArr6[i13] & 255) << (i12 << 3)));
                            this.ii = i13 + 1;
                        }
                    }
                }
                if (this.counter == this.messagesize) {
                    this.big8 = (long) this.checksum;
                }
                this.test = (int) (this.big8 & ((long) this.k));
                this.privateKeyOTS = this.gmssRandom.nextSeed(this.seed);
            } else if (i8 > 0) {
                Digest digest2 = this.messDigestOTS;
                byte[] bArr7 = this.privateKeyOTS;
                digest2.update(bArr7, 0, bArr7.length);
                this.privateKeyOTS = new byte[this.messDigestOTS.getDigestSize()];
                this.messDigestOTS.doFinal(this.privateKeyOTS, 0);
                this.test--;
            }
            if (this.test == 0) {
                byte[] bArr8 = this.privateKeyOTS;
                byte[] bArr9 = this.sign;
                int i14 = this.counter;
                int i15 = this.mdsize;
                System.arraycopy(bArr8, 0, bArr9, i14 * i15, i15);
                this.big8 >>>= this.w;
            } else {
                return;
            }
        } else if (i2 < 57) {
            long j4 = this.test8;
            if (j4 == 0) {
                this.big8 = 0;
                this.ii = 0;
                int i16 = this.r;
                int i17 = i16 % 8;
                int i18 = i16 >>> 3;
                int i19 = this.mdsize;
                if (i18 < i19) {
                    if (i16 <= (i19 << 3) - i2) {
                        this.r = i16 + i2;
                        i19 = (this.r + 7) >>> 3;
                    } else {
                        this.r = i16 + i2;
                    }
                    while (true) {
                        j = this.big8;
                        if (i18 >= i19) {
                            break;
                        }
                        int i20 = this.ii;
                        this.big8 = j ^ ((long) ((this.hash[i18] & 255) << (i20 << 3)));
                        this.ii = i20 + 1;
                        i18++;
                    }
                    this.big8 = j >>> i17;
                    this.test8 = this.big8 & ((long) this.k);
                } else {
                    int i21 = this.checksum;
                    this.test8 = (long) (this.k & i21);
                    this.checksum = i21 >>> i2;
                }
                this.privateKeyOTS = this.gmssRandom.nextSeed(this.seed);
            } else if (j4 > 0) {
                Digest digest3 = this.messDigestOTS;
                byte[] bArr10 = this.privateKeyOTS;
                digest3.update(bArr10, 0, bArr10.length);
                this.privateKeyOTS = new byte[this.messDigestOTS.getDigestSize()];
                this.messDigestOTS.doFinal(this.privateKeyOTS, 0);
                this.test8--;
            }
            if (this.test8 == 0) {
                byte[] bArr11 = this.privateKeyOTS;
                byte[] bArr12 = this.sign;
                int i22 = this.counter;
                int i23 = this.mdsize;
                System.arraycopy(bArr11, 0, bArr12, i22 * i23, i23);
            } else {
                return;
            }
        } else {
            return;
        }
        this.counter++;
    }

    public int getLog(int i) {
        int i2 = 1;
        int i3 = 2;
        while (i3 < i) {
            i3 <<= 1;
            i2++;
        }
        return i2;
    }

    public byte[] getSig() {
        return this.sign;
    }

    public byte[][] getStatByte() {
        byte[][] bArr = (byte[][]) Array.newInstance(byte.class, 5, this.mdsize);
        bArr[0] = this.privateKeyOTS;
        bArr[1] = this.seed;
        bArr[2] = this.hash;
        bArr[3] = this.sign;
        bArr[4] = getStatLong();
        return bArr;
    }

    public int[] getStatInt() {
        return new int[]{this.counter, this.test, this.ii, this.r, this.steps, this.keysize, this.height, this.w, this.checksum};
    }

    public byte[] getStatLong() {
        long j = this.test8;
        long j2 = this.big8;
        return new byte[]{(byte) ((int) (j & 255)), (byte) ((int) ((j >> 8) & 255)), (byte) ((int) ((j >> 16) & 255)), (byte) ((int) ((j >> 24) & 255)), (byte) ((int) ((j >> 32) & 255)), (byte) ((int) ((j >> 40) & 255)), (byte) ((int) ((j >> 48) & 255)), (byte) ((int) ((j >> 56) & 255)), (byte) ((int) (j2 & 255)), (byte) ((int) ((j2 >> 8) & 255)), (byte) ((int) ((j2 >> 16) & 255)), (byte) ((int) ((j2 >> 24) & 255)), (byte) ((int) ((j2 >> 32) & 255)), (byte) ((int) ((j2 >> 40) & 255)), (byte) ((int) ((j2 >> 48) & 255)), (byte) ((int) ((j2 >> 56) & 255))};
    }

    public void initSign(byte[] bArr, byte[] bArr2) {
        int i;
        int i2;
        this.hash = new byte[this.mdsize];
        this.messDigestOTS.update(bArr2, 0, bArr2.length);
        this.hash = new byte[this.messDigestOTS.getDigestSize()];
        this.messDigestOTS.doFinal(this.hash, 0);
        int i3 = this.mdsize;
        byte[] bArr3 = new byte[i3];
        System.arraycopy(this.hash, 0, bArr3, 0, i3);
        int log = getLog((this.messagesize << this.w) + 1);
        int i4 = this.w;
        if (8 % i4 == 0) {
            int i5 = 8 / i4;
            int i6 = 0;
            i = 0;
            while (i6 < this.mdsize) {
                int i7 = i;
                for (int i8 = 0; i8 < i5; i8++) {
                    i7 += bArr3[i6] & this.k;
                    bArr3[i6] = (byte) (bArr3[i6] >>> this.w);
                }
                i6++;
                i = i7;
            }
            this.checksum = (this.messagesize << this.w) - i;
            int i9 = this.checksum;
            int i10 = 0;
            while (i10 < log) {
                i += this.k & i9;
                int i11 = this.w;
                i9 >>>= i11;
                i10 += i11;
            }
        } else if (i4 < 8) {
            int i12 = this.mdsize / i4;
            int i13 = 0;
            int i14 = 0;
            int i15 = 0;
            while (i13 < i12) {
                int i16 = i14;
                long j = 0;
                for (int i17 = 0; i17 < this.w; i17++) {
                    j ^= (long) ((bArr3[i16] & 255) << (i17 << 3));
                    i16++;
                }
                for (int i18 = 0; i18 < 8; i18++) {
                    i15 += (int) (((long) this.k) & j);
                    j >>>= this.w;
                }
                i13++;
                i14 = i16;
            }
            int i19 = this.mdsize % this.w;
            long j2 = 0;
            for (int i20 = 0; i20 < i19; i20++) {
                j2 ^= (long) ((bArr3[i14] & 255) << (i20 << 3));
                i14++;
            }
            int i21 = i19 << 3;
            int i22 = 0;
            while (i22 < i21) {
                i15 += (int) (((long) this.k) & j2);
                int i23 = this.w;
                j2 >>>= i23;
                i22 += i23;
            }
            this.checksum = (this.messagesize << this.w) - i15;
            int i24 = this.checksum;
            int i25 = 0;
            while (i25 < log) {
                i15 += this.k & i24;
                int i26 = this.w;
                i24 >>>= i26;
                i25 += i26;
            }
            i = i15;
        } else if (i4 < 57) {
            int i27 = 0;
            i = 0;
            while (true) {
                i2 = this.mdsize;
                int i28 = this.w;
                if (i27 > (i2 << 3) - i28) {
                    break;
                }
                int i29 = i27 % 8;
                i27 += i28;
                int i30 = (i27 + 7) >>> 3;
                int i31 = 0;
                long j3 = 0;
                for (int i32 = i27 >>> 3; i32 < i30; i32++) {
                    j3 ^= (long) ((bArr3[i32] & 255) << (i31 << 3));
                    i31++;
                }
                i = (int) (((long) i) + ((j3 >>> i29) & ((long) this.k)));
            }
            int i33 = i27 >>> 3;
            if (i33 < i2) {
                int i34 = i27 % 8;
                int i35 = 0;
                long j4 = 0;
                while (i33 < this.mdsize) {
                    j4 ^= (long) ((bArr3[i33] & 255) << (i35 << 3));
                    i35++;
                    i33++;
                }
                i = (int) (((long) i) + ((j4 >>> i34) & ((long) this.k)));
            }
            this.checksum = (this.messagesize << this.w) - i;
            int i36 = this.checksum;
            int i37 = 0;
            while (i37 < log) {
                i += this.k & i36;
                int i38 = this.w;
                i36 >>>= i38;
                i37 += i38;
            }
        } else {
            i = 0;
        }
        this.keysize = this.messagesize + ((int) Math.ceil(((double) log) / ((double) this.w)));
        this.steps = (int) Math.ceil(((double) (this.keysize + i)) / ((double) (1 << this.height)));
        int i39 = this.keysize;
        int i40 = this.mdsize;
        this.sign = new byte[(i39 * i40)];
        this.counter = 0;
        this.test = 0;
        this.ii = 0;
        this.test8 = 0;
        this.r = 0;
        this.privateKeyOTS = new byte[i40];
        this.seed = new byte[i40];
        System.arraycopy(bArr, 0, this.seed, 0, i40);
    }

    public String toString() {
        int[] statInt = getStatInt();
        byte[][] bArr = (byte[][]) Array.newInstance(byte.class, 5, this.mdsize);
        byte[][] statByte = getStatByte();
        String str = "" + this.big8 + "  ";
        for (int i = 0; i < 9; i++) {
            str = str + statInt[i] + " ";
        }
        for (int i2 = 0; i2 < 5; i2++) {
            str = str + new String(Hex.encode(statByte[i2])) + " ";
        }
        return str;
    }

    public boolean updateSign() {
        for (int i = 0; i < this.steps; i++) {
            if (this.counter < this.keysize) {
                oneStep();
            }
            if (this.counter == this.keysize) {
                return true;
            }
        }
        return false;
    }
}
