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
        this.k = (1 << this.w) - 1;
        this.messagesize = (int) Math.ceil(((double) (this.mdsize << 3)) / ((double) this.w));
        this.privateKeyOTS = bArr[0];
        this.seed = bArr[1];
        this.hash = bArr[2];
        this.sign = bArr[3];
        this.test8 = (((long) (bArr[4][2] & 255)) << 16) | ((long) (bArr[4][0] & 255)) | (((long) (bArr[4][1] & 255)) << 8) | (((long) (bArr[4][3] & 255)) << 24) | (((long) (bArr[4][4] & 255)) << 32) | (((long) (bArr[4][5] & 255)) << 40) | (((long) (bArr[4][6] & 255)) << 48) | (((long) (bArr[4][7] & 255)) << 56);
        this.big8 = (((long) (bArr[4][15] & 255)) << 56) | ((long) (bArr[4][8] & 255)) | (((long) (bArr[4][9] & 255)) << 8) | (((long) (bArr[4][10] & 255)) << 16) | (((long) (bArr[4][11] & 255)) << 24) | (((long) (bArr[4][12] & 255)) << 32) | (((long) (bArr[4][13] & 255)) << 40) | (((long) (bArr[4][14] & 255)) << 48);
    }

    private void oneStep() {
        int i;
        if (8 % this.w == 0) {
            if (this.test == 0) {
                this.privateKeyOTS = this.gmssRandom.nextSeed(this.seed);
                if (this.ii < this.mdsize) {
                    this.test = this.hash[this.ii] & this.k;
                    this.hash[this.ii] = (byte) (this.hash[this.ii] >>> this.w);
                } else {
                    this.test = this.checksum & this.k;
                    this.checksum >>>= this.w;
                }
            } else if (this.test > 0) {
                this.messDigestOTS.update(this.privateKeyOTS, 0, this.privateKeyOTS.length);
                this.privateKeyOTS = new byte[this.messDigestOTS.getDigestSize()];
                this.messDigestOTS.doFinal(this.privateKeyOTS, 0);
                this.test--;
            }
            if (this.test == 0) {
                System.arraycopy(this.privateKeyOTS, 0, this.sign, this.counter * this.mdsize, this.mdsize);
                this.counter++;
                if (this.counter % (8 / this.w) == 0) {
                    this.ii++;
                }
            }
        } else {
            if (this.w < 8) {
                if (this.test == 0) {
                    if (this.counter % 8 == 0 && this.ii < this.mdsize) {
                        this.big8 = 0;
                        if (this.counter < ((this.mdsize / this.w) << 3)) {
                            for (int i2 = 0; i2 < this.w; i2++) {
                                this.big8 ^= (long) ((this.hash[this.ii] & 255) << (i2 << 3));
                                this.ii++;
                            }
                        } else {
                            for (int i3 = 0; i3 < this.mdsize % this.w; i3++) {
                                this.big8 ^= (long) ((this.hash[this.ii] & 255) << (i3 << 3));
                                this.ii++;
                            }
                        }
                    }
                    if (this.counter == this.messagesize) {
                        this.big8 = (long) this.checksum;
                    }
                    this.test = (int) (this.big8 & ((long) this.k));
                    this.privateKeyOTS = this.gmssRandom.nextSeed(this.seed);
                } else if (this.test > 0) {
                    this.messDigestOTS.update(this.privateKeyOTS, 0, this.privateKeyOTS.length);
                    this.privateKeyOTS = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(this.privateKeyOTS, 0);
                    this.test--;
                }
                if (this.test == 0) {
                    System.arraycopy(this.privateKeyOTS, 0, this.sign, this.counter * this.mdsize, this.mdsize);
                    this.big8 >>>= this.w;
                }
            } else if (this.w < 57) {
                if (this.test8 == 0) {
                    this.big8 = 0;
                    this.ii = 0;
                    int i4 = this.r % 8;
                    int i5 = this.r >>> 3;
                    if (i5 < this.mdsize) {
                        if (this.r <= (this.mdsize << 3) - this.w) {
                            this.r += this.w;
                            i = (this.r + 7) >>> 3;
                        } else {
                            i = this.mdsize;
                            this.r += this.w;
                        }
                        while (i5 < i) {
                            this.big8 ^= (long) ((this.hash[i5] & 255) << (this.ii << 3));
                            this.ii++;
                            i5++;
                        }
                        this.big8 >>>= i4;
                        this.test8 = this.big8 & ((long) this.k);
                    } else {
                        this.test8 = (long) (this.checksum & this.k);
                        this.checksum >>>= this.w;
                    }
                    this.privateKeyOTS = this.gmssRandom.nextSeed(this.seed);
                } else if (this.test8 > 0) {
                    this.messDigestOTS.update(this.privateKeyOTS, 0, this.privateKeyOTS.length);
                    this.privateKeyOTS = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(this.privateKeyOTS, 0);
                    this.test8--;
                }
                if (this.test8 == 0) {
                    System.arraycopy(this.privateKeyOTS, 0, this.sign, this.counter * this.mdsize, this.mdsize);
                }
            }
            this.counter++;
        }
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
        byte[][] bArr = (byte[][]) Array.newInstance(byte.class, new int[]{5, this.mdsize});
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
        return new byte[]{(byte) ((int) (this.test8 & 255)), (byte) ((int) ((this.test8 >> 8) & 255)), (byte) ((int) ((this.test8 >> 16) & 255)), (byte) ((int) ((this.test8 >> 24) & 255)), (byte) ((int) ((this.test8 >> 32) & 255)), (byte) ((int) ((this.test8 >> 40) & 255)), (byte) ((int) ((this.test8 >> 48) & 255)), (byte) ((int) ((this.test8 >> 56) & 255)), (byte) ((int) (this.big8 & 255)), (byte) ((int) ((this.big8 >> 8) & 255)), (byte) ((int) ((this.big8 >> 16) & 255)), (byte) ((int) ((this.big8 >> 24) & 255)), (byte) ((int) ((this.big8 >> 32) & 255)), (byte) ((int) ((this.big8 >> 40) & 255)), (byte) ((int) ((this.big8 >> 48) & 255)), (byte) ((int) ((this.big8 >> 56) & 255))};
    }

    public void initSign(byte[] bArr, byte[] bArr2) {
        int i;
        byte[] bArr3 = bArr2;
        this.hash = new byte[this.mdsize];
        int i2 = 0;
        this.messDigestOTS.update(bArr3, 0, bArr3.length);
        this.hash = new byte[this.messDigestOTS.getDigestSize()];
        this.messDigestOTS.doFinal(this.hash, 0);
        byte[] bArr4 = new byte[this.mdsize];
        System.arraycopy(this.hash, 0, bArr4, 0, this.mdsize);
        int log = getLog((this.messagesize << this.w) + 1);
        if (8 % this.w == 0) {
            int i3 = 8 / this.w;
            int i4 = 0;
            int i5 = 0;
            while (i4 < this.mdsize) {
                int i6 = i5;
                for (int i7 = 0; i7 < i3; i7++) {
                    i6 += bArr4[i4] & this.k;
                    bArr4[i4] = (byte) (bArr4[i4] >>> this.w);
                }
                i4++;
                i5 = i6;
            }
            this.checksum = (this.messagesize << this.w) - i5;
            int i8 = this.checksum;
            for (int i9 = 0; i9 < log; i9 += this.w) {
                i5 += this.k & i8;
                i8 >>>= this.w;
            }
            i = i5;
        } else if (this.w < 8) {
            int i10 = this.mdsize / this.w;
            int i11 = 0;
            int i12 = 0;
            int i13 = 0;
            while (i11 < i10) {
                int i14 = i12;
                long j = 0;
                for (int i15 = i2; i15 < this.w; i15++) {
                    j ^= (long) ((bArr4[i14] & 255) << (i15 << 3));
                    i14++;
                }
                int i16 = i2;
                while (i16 < 8) {
                    i13 += (int) (((long) this.k) & j);
                    j >>>= this.w;
                    i16++;
                    i10 = i10;
                }
                int i17 = i10;
                i11++;
                i12 = i14;
                i2 = 0;
            }
            int i18 = this.mdsize % this.w;
            long j2 = 0;
            for (int i19 = 0; i19 < i18; i19++) {
                j2 ^= (long) ((bArr4[i12] & 255) << (i19 << 3));
                i12++;
            }
            int i20 = i18 << 3;
            for (int i21 = 0; i21 < i20; i21 += this.w) {
                i13 += (int) (((long) this.k) & j2);
                j2 >>>= this.w;
            }
            this.checksum = (this.messagesize << this.w) - i13;
            int i22 = this.checksum;
            i = i13;
            for (int i23 = 0; i23 < log; i23 += this.w) {
                i += this.k & i22;
                i22 >>>= this.w;
            }
        } else if (this.w < 57) {
            int i24 = 0;
            int i25 = 0;
            while (i24 <= (this.mdsize << 3) - this.w) {
                int i26 = i24 % 8;
                i24 += this.w;
                int i27 = (i24 + 7) >>> 3;
                long j3 = 0;
                int i28 = 0;
                for (int i29 = i24 >>> 3; i29 < i27; i29++) {
                    j3 ^= (long) ((bArr4[i29] & 255) << (i28 << 3));
                    i28++;
                }
                i25 = (int) (((long) i25) + ((j3 >>> i26) & ((long) this.k)));
            }
            int i30 = i24 >>> 3;
            if (i30 < this.mdsize) {
                int i31 = i24 % 8;
                int i32 = 0;
                long j4 = 0;
                while (i30 < this.mdsize) {
                    j4 ^= (long) ((bArr4[i30] & 255) << (i32 << 3));
                    i32++;
                    i30++;
                }
                i25 = (int) (((long) i25) + ((j4 >>> i31) & ((long) this.k)));
            }
            this.checksum = (this.messagesize << this.w) - i25;
            int i33 = i25;
            int i34 = this.checksum;
            for (int i35 = 0; i35 < log; i35 += this.w) {
                i33 = i + (this.k & i34);
                i34 >>>= this.w;
            }
        } else {
            i = 0;
        }
        this.keysize = this.messagesize + ((int) Math.ceil(((double) log) / ((double) this.w)));
        this.steps = (int) Math.ceil(((double) (this.keysize + i)) / ((double) (1 << this.height)));
        this.sign = new byte[(this.keysize * this.mdsize)];
        this.counter = 0;
        this.test = 0;
        this.ii = 0;
        this.test8 = 0;
        this.r = 0;
        this.privateKeyOTS = new byte[this.mdsize];
        this.seed = new byte[this.mdsize];
        System.arraycopy(bArr, 0, this.seed, 0, this.mdsize);
    }

    public String toString() {
        int[] iArr = new int[9];
        int[] statInt = getStatInt();
        byte[][] bArr = (byte[][]) Array.newInstance(byte.class, new int[]{5, this.mdsize});
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
