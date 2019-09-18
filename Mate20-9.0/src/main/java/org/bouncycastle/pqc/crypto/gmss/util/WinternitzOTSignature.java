package org.bouncycastle.pqc.crypto.gmss.util;

import java.lang.reflect.Array;
import org.bouncycastle.crypto.Digest;

public class WinternitzOTSignature {
    private int checksumsize;
    private GMSSRandom gmssRandom = new GMSSRandom(this.messDigestOTS);
    private int keysize;
    private int mdsize = this.messDigestOTS.getDigestSize();
    private Digest messDigestOTS;
    private int messagesize;
    private byte[][] privateKeyOTS;
    private int w;

    public WinternitzOTSignature(byte[] bArr, Digest digest, int i) {
        this.w = i;
        this.messDigestOTS = digest;
        double d = (double) i;
        this.messagesize = (int) Math.ceil(((double) (this.mdsize << 3)) / d);
        this.checksumsize = getLog((this.messagesize << i) + 1);
        this.keysize = this.messagesize + ((int) Math.ceil(((double) this.checksumsize) / d));
        this.privateKeyOTS = (byte[][]) Array.newInstance(byte.class, new int[]{this.keysize, this.mdsize});
        byte[] bArr2 = new byte[this.mdsize];
        System.arraycopy(bArr, 0, bArr2, 0, bArr2.length);
        for (int i2 = 0; i2 < this.keysize; i2++) {
            this.privateKeyOTS[i2] = this.gmssRandom.nextSeed(bArr2);
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

    public byte[][] getPrivateKey() {
        return this.privateKeyOTS;
    }

    public byte[] getPublicKey() {
        byte[] bArr = new byte[(this.keysize * this.mdsize)];
        byte[] bArr2 = new byte[this.mdsize];
        int i = 1 << this.w;
        for (int i2 = 0; i2 < this.keysize; i2++) {
            this.messDigestOTS.update(this.privateKeyOTS[i2], 0, this.privateKeyOTS[i2].length);
            byte[] bArr3 = new byte[this.messDigestOTS.getDigestSize()];
            this.messDigestOTS.doFinal(bArr3, 0);
            for (int i3 = 2; i3 < i; i3++) {
                this.messDigestOTS.update(bArr3, 0, bArr3.length);
                bArr3 = new byte[this.messDigestOTS.getDigestSize()];
                this.messDigestOTS.doFinal(bArr3, 0);
            }
            System.arraycopy(bArr3, 0, bArr, this.mdsize * i2, this.mdsize);
        }
        this.messDigestOTS.update(bArr, 0, bArr.length);
        byte[] bArr4 = new byte[this.messDigestOTS.getDigestSize()];
        this.messDigestOTS.doFinal(bArr4, 0);
        return bArr4;
    }

    public byte[] getSignature(byte[] bArr) {
        byte[] bArr2 = bArr;
        byte[] bArr3 = new byte[(this.keysize * this.mdsize)];
        byte[] bArr4 = new byte[this.mdsize];
        this.messDigestOTS.update(bArr2, 0, bArr2.length);
        byte[] bArr5 = new byte[this.messDigestOTS.getDigestSize()];
        this.messDigestOTS.doFinal(bArr5, 0);
        if (8 % this.w == 0) {
            int i = 8 / this.w;
            int i2 = (1 << this.w) - 1;
            int i3 = 0;
            int i4 = 0;
            byte[] bArr6 = new byte[this.mdsize];
            int i5 = 0;
            while (i5 < bArr5.length) {
                byte[] bArr7 = bArr6;
                int i6 = i4;
                int i7 = i3;
                for (int i8 = 0; i8 < i; i8++) {
                    int i9 = bArr5[i5] & i2;
                    i7 += i9;
                    System.arraycopy(this.privateKeyOTS[i6], 0, bArr7, 0, this.mdsize);
                    while (i9 > 0) {
                        this.messDigestOTS.update(bArr7, 0, bArr7.length);
                        bArr7 = new byte[this.messDigestOTS.getDigestSize()];
                        this.messDigestOTS.doFinal(bArr7, 0);
                        i9--;
                    }
                    System.arraycopy(bArr7, 0, bArr3, this.mdsize * i6, this.mdsize);
                    bArr5[i5] = (byte) (bArr5[i5] >>> this.w);
                    i6++;
                }
                i5++;
                i3 = i7;
                i4 = i6;
                bArr6 = bArr7;
            }
            int i10 = (this.messagesize << this.w) - i3;
            for (int i11 = 0; i11 < this.checksumsize; i11 += this.w) {
                System.arraycopy(this.privateKeyOTS[i4], 0, bArr6, 0, this.mdsize);
                for (int i12 = i10 & i2; i12 > 0; i12--) {
                    this.messDigestOTS.update(bArr6, 0, bArr6.length);
                    bArr6 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr6, 0);
                }
                System.arraycopy(bArr6, 0, bArr3, this.mdsize * i4, this.mdsize);
                i10 >>>= this.w;
                i4++;
            }
        } else if (this.w < 8) {
            int i13 = this.mdsize / this.w;
            int i14 = (1 << this.w) - 1;
            int i15 = 0;
            int i16 = 0;
            int i17 = 0;
            byte[] bArr8 = new byte[this.mdsize];
            int i18 = 0;
            while (i18 < i13) {
                int i19 = i15;
                long j = 0;
                for (int i20 = 0; i20 < this.w; i20++) {
                    j ^= (long) ((bArr5[i19] & 255) << (i20 << 3));
                    i19++;
                }
                int i21 = 0;
                while (i21 < 8) {
                    int i22 = i21;
                    int i23 = (int) (((long) i14) & j);
                    i17 += i23;
                    System.arraycopy(this.privateKeyOTS[i16], 0, bArr8, 0, this.mdsize);
                    while (i23 > 0) {
                        this.messDigestOTS.update(bArr8, 0, bArr8.length);
                        bArr8 = new byte[this.messDigestOTS.getDigestSize()];
                        this.messDigestOTS.doFinal(bArr8, 0);
                        i23--;
                    }
                    System.arraycopy(bArr8, 0, bArr3, this.mdsize * i16, this.mdsize);
                    j >>>= this.w;
                    i16++;
                    i21 = i22 + 1;
                }
                i18++;
                i15 = i19;
            }
            int i24 = this.mdsize % this.w;
            long j2 = 0;
            for (int i25 = 0; i25 < i24; i25++) {
                j2 ^= (long) ((bArr5[i15] & 255) << (i25 << 3));
                i15++;
            }
            int i26 = i24 << 3;
            for (int i27 = 0; i27 < i26; i27 += this.w) {
                int i28 = (int) (j2 & ((long) i14));
                i17 += i28;
                System.arraycopy(this.privateKeyOTS[i16], 0, bArr8, 0, this.mdsize);
                while (i28 > 0) {
                    this.messDigestOTS.update(bArr8, 0, bArr8.length);
                    bArr8 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr8, 0);
                    i28--;
                }
                System.arraycopy(bArr8, 0, bArr3, this.mdsize * i16, this.mdsize);
                j2 >>>= this.w;
                i16++;
            }
            int i29 = (this.messagesize << this.w) - i17;
            for (int i30 = 0; i30 < this.checksumsize; i30 += this.w) {
                System.arraycopy(this.privateKeyOTS[i16], 0, bArr8, 0, this.mdsize);
                for (int i31 = i29 & i14; i31 > 0; i31--) {
                    this.messDigestOTS.update(bArr8, 0, bArr8.length);
                    bArr8 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr8, 0);
                }
                System.arraycopy(bArr8, 0, bArr3, this.mdsize * i16, this.mdsize);
                i29 >>>= this.w;
                i16++;
            }
        } else if (this.w < 57) {
            int i32 = (this.mdsize << 3) - this.w;
            int i33 = (1 << this.w) - 1;
            int i34 = 0;
            int i35 = 0;
            byte[] bArr9 = new byte[this.mdsize];
            int i36 = 0;
            while (i36 <= i32) {
                int i37 = i36 % 8;
                i36 += this.w;
                int i38 = (i36 + 7) >>> 3;
                int i39 = 0;
                long j3 = 0;
                for (int i40 = i36 >>> 3; i40 < i38; i40++) {
                    j3 ^= (long) ((bArr5[i40] & 255) << (i39 << 3));
                    i39++;
                }
                long j4 = (j3 >>> i37) & ((long) i33);
                i34 = (int) (((long) i34) + j4);
                System.arraycopy(this.privateKeyOTS[i35], 0, bArr9, 0, this.mdsize);
                while (j4 > 0) {
                    this.messDigestOTS.update(bArr9, 0, bArr9.length);
                    bArr9 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr9, 0);
                    j4--;
                }
                System.arraycopy(bArr9, 0, bArr3, this.mdsize * i35, this.mdsize);
                i35++;
            }
            int i41 = i36 >>> 3;
            if (i41 < this.mdsize) {
                int i42 = i36 % 8;
                int i43 = 0;
                long j5 = 0;
                while (i41 < this.mdsize) {
                    j5 ^= (long) ((bArr5[i41] & 255) << (i43 << 3));
                    i43++;
                    i41++;
                }
                long j6 = (j5 >>> i42) & ((long) i33);
                i34 = (int) (((long) i34) + j6);
                System.arraycopy(this.privateKeyOTS[i35], 0, bArr9, 0, this.mdsize);
                while (j6 > 0) {
                    this.messDigestOTS.update(bArr9, 0, bArr9.length);
                    bArr9 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr9, 0);
                    j6--;
                }
                System.arraycopy(bArr9, 0, bArr3, this.mdsize * i35, this.mdsize);
                i35++;
            }
            int i44 = (this.messagesize << this.w) - i34;
            for (int i45 = 0; i45 < this.checksumsize; i45 += this.w) {
                System.arraycopy(this.privateKeyOTS[i35], 0, bArr9, 0, this.mdsize);
                for (long j7 = (long) (i44 & i33); j7 > 0; j7--) {
                    this.messDigestOTS.update(bArr9, 0, bArr9.length);
                    bArr9 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr9, 0);
                }
                System.arraycopy(bArr9, 0, bArr3, this.mdsize * i35, this.mdsize);
                i44 >>>= this.w;
                i35++;
            }
        }
        return bArr3;
    }
}
