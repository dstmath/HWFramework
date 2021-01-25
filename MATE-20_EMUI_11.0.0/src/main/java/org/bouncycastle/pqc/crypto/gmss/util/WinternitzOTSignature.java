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
        this.privateKeyOTS = (byte[][]) Array.newInstance(byte.class, this.keysize, this.mdsize);
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
        int i = this.keysize;
        int i2 = this.mdsize;
        byte[] bArr = new byte[(i * i2)];
        byte[] bArr2 = new byte[i2];
        int i3 = 1 << this.w;
        for (int i4 = 0; i4 < this.keysize; i4++) {
            Digest digest = this.messDigestOTS;
            byte[][] bArr3 = this.privateKeyOTS;
            digest.update(bArr3[i4], 0, bArr3[i4].length);
            byte[] bArr4 = new byte[this.messDigestOTS.getDigestSize()];
            this.messDigestOTS.doFinal(bArr4, 0);
            for (int i5 = 2; i5 < i3; i5++) {
                this.messDigestOTS.update(bArr4, 0, bArr4.length);
                bArr4 = new byte[this.messDigestOTS.getDigestSize()];
                this.messDigestOTS.doFinal(bArr4, 0);
            }
            int i6 = this.mdsize;
            System.arraycopy(bArr4, 0, bArr, i6 * i4, i6);
        }
        this.messDigestOTS.update(bArr, 0, bArr.length);
        byte[] bArr5 = new byte[this.messDigestOTS.getDigestSize()];
        this.messDigestOTS.doFinal(bArr5, 0);
        return bArr5;
    }

    public byte[] getSignature(byte[] bArr) {
        int i;
        int i2 = this.keysize;
        int i3 = this.mdsize;
        byte[] bArr2 = new byte[(i2 * i3)];
        byte[] bArr3 = new byte[i3];
        this.messDigestOTS.update(bArr, 0, bArr.length);
        byte[] bArr4 = new byte[this.messDigestOTS.getDigestSize()];
        this.messDigestOTS.doFinal(bArr4, 0);
        int i4 = this.w;
        if (8 % i4 == 0) {
            int i5 = 8 / i4;
            int i6 = (1 << i4) - 1;
            int i7 = 0;
            int i8 = 0;
            byte[] bArr5 = new byte[this.mdsize];
            int i9 = 0;
            while (i9 < bArr4.length) {
                byte[] bArr6 = bArr5;
                int i10 = i8;
                int i11 = i7;
                for (int i12 = 0; i12 < i5; i12++) {
                    int i13 = bArr4[i9] & i6;
                    i11 += i13;
                    System.arraycopy(this.privateKeyOTS[i10], 0, bArr6, 0, this.mdsize);
                    while (i13 > 0) {
                        this.messDigestOTS.update(bArr6, 0, bArr6.length);
                        bArr6 = new byte[this.messDigestOTS.getDigestSize()];
                        this.messDigestOTS.doFinal(bArr6, 0);
                        i13--;
                    }
                    int i14 = this.mdsize;
                    System.arraycopy(bArr6, 0, bArr2, i10 * i14, i14);
                    bArr4[i9] = (byte) (bArr4[i9] >>> this.w);
                    i10++;
                }
                i9++;
                i7 = i11;
                i8 = i10;
                bArr5 = bArr6;
            }
            int i15 = (this.messagesize << this.w) - i7;
            int i16 = 0;
            while (i16 < this.checksumsize) {
                System.arraycopy(this.privateKeyOTS[i8], 0, bArr5, 0, this.mdsize);
                for (int i17 = i15 & i6; i17 > 0; i17--) {
                    this.messDigestOTS.update(bArr5, 0, bArr5.length);
                    bArr5 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr5, 0);
                }
                int i18 = this.mdsize;
                System.arraycopy(bArr5, 0, bArr2, i8 * i18, i18);
                int i19 = this.w;
                i15 >>>= i19;
                i8++;
                i16 += i19;
            }
        } else if (i4 < 8) {
            int i20 = this.mdsize;
            int i21 = i20 / i4;
            int i22 = (1 << i4) - 1;
            int i23 = 0;
            int i24 = 0;
            int i25 = 0;
            byte[] bArr7 = new byte[i20];
            int i26 = 0;
            while (i26 < i21) {
                int i27 = i23;
                long j = 0;
                for (int i28 = 0; i28 < this.w; i28++) {
                    j ^= (long) ((bArr4[i27] & 255) << (i28 << 3));
                    i27++;
                }
                for (int i29 = 0; i29 < 8; i29++) {
                    int i30 = (int) (((long) i22) & j);
                    i25 += i30;
                    System.arraycopy(this.privateKeyOTS[i24], 0, bArr7, 0, this.mdsize);
                    while (i30 > 0) {
                        this.messDigestOTS.update(bArr7, 0, bArr7.length);
                        bArr7 = new byte[this.messDigestOTS.getDigestSize()];
                        this.messDigestOTS.doFinal(bArr7, 0);
                        i30--;
                    }
                    int i31 = this.mdsize;
                    System.arraycopy(bArr7, 0, bArr2, i24 * i31, i31);
                    j >>>= this.w;
                    i24++;
                }
                i26++;
                i23 = i27;
            }
            int i32 = this.mdsize % this.w;
            long j2 = 0;
            for (int i33 = 0; i33 < i32; i33++) {
                j2 ^= (long) ((bArr4[i23] & 255) << (i33 << 3));
                i23++;
            }
            int i34 = i32 << 3;
            int i35 = 0;
            while (i35 < i34) {
                int i36 = (int) (j2 & ((long) i22));
                i25 += i36;
                System.arraycopy(this.privateKeyOTS[i24], 0, bArr7, 0, this.mdsize);
                while (i36 > 0) {
                    this.messDigestOTS.update(bArr7, 0, bArr7.length);
                    bArr7 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr7, 0);
                    i36--;
                }
                int i37 = this.mdsize;
                System.arraycopy(bArr7, 0, bArr2, i24 * i37, i37);
                int i38 = this.w;
                j2 >>>= i38;
                i24++;
                i35 += i38;
            }
            int i39 = (this.messagesize << this.w) - i25;
            int i40 = 0;
            while (i40 < this.checksumsize) {
                System.arraycopy(this.privateKeyOTS[i24], 0, bArr7, 0, this.mdsize);
                for (int i41 = i39 & i22; i41 > 0; i41--) {
                    this.messDigestOTS.update(bArr7, 0, bArr7.length);
                    bArr7 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr7, 0);
                }
                int i42 = this.mdsize;
                System.arraycopy(bArr7, 0, bArr2, i24 * i42, i42);
                int i43 = this.w;
                i39 >>>= i43;
                i24++;
                i40 += i43;
            }
        } else if (i4 < 57) {
            int i44 = this.mdsize;
            int i45 = (i44 << 3) - i4;
            int i46 = (1 << i4) - 1;
            int i47 = 0;
            int i48 = 0;
            byte[] bArr8 = new byte[i44];
            int i49 = 0;
            while (i49 <= i45) {
                int i50 = i49 % 8;
                i49 += this.w;
                int i51 = (i49 + 7) >>> 3;
                int i52 = 0;
                long j3 = 0;
                for (int i53 = i49 >>> 3; i53 < i51; i53++) {
                    j3 ^= (long) ((bArr4[i53] & 255) << (i52 << 3));
                    i52++;
                }
                long j4 = (j3 >>> i50) & ((long) i46);
                i48 = (int) (((long) i48) + j4);
                System.arraycopy(this.privateKeyOTS[i47], 0, bArr8, 0, this.mdsize);
                while (j4 > 0) {
                    this.messDigestOTS.update(bArr8, 0, bArr8.length);
                    bArr8 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr8, 0);
                    j4--;
                }
                int i54 = this.mdsize;
                System.arraycopy(bArr8, 0, bArr2, i47 * i54, i54);
                i47++;
            }
            int i55 = i49 >>> 3;
            if (i55 < this.mdsize) {
                int i56 = i49 % 8;
                int i57 = 0;
                long j5 = 0;
                while (true) {
                    i = this.mdsize;
                    if (i55 >= i) {
                        break;
                    }
                    j5 ^= (long) ((bArr4[i55] & 255) << (i57 << 3));
                    i57++;
                    i55++;
                }
                long j6 = (j5 >>> i56) & ((long) i46);
                i48 = (int) (((long) i48) + j6);
                System.arraycopy(this.privateKeyOTS[i47], 0, bArr8, 0, i);
                while (j6 > 0) {
                    this.messDigestOTS.update(bArr8, 0, bArr8.length);
                    bArr8 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr8, 0);
                    j6--;
                }
                int i58 = this.mdsize;
                System.arraycopy(bArr8, 0, bArr2, i47 * i58, i58);
                i47++;
            }
            int i59 = (this.messagesize << this.w) - i48;
            int i60 = 0;
            while (i60 < this.checksumsize) {
                System.arraycopy(this.privateKeyOTS[i47], 0, bArr8, 0, this.mdsize);
                for (long j7 = (long) (i59 & i46); j7 > 0; j7--) {
                    this.messDigestOTS.update(bArr8, 0, bArr8.length);
                    bArr8 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr8, 0);
                }
                int i61 = this.mdsize;
                System.arraycopy(bArr8, 0, bArr2, i47 * i61, i61);
                int i62 = this.w;
                i59 >>>= i62;
                i47++;
                i60 += i62;
            }
        }
        return bArr2;
    }
}
