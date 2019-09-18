package org.bouncycastle.pqc.crypto.gmss.util;

import org.bouncycastle.crypto.Digest;

public class WinternitzOTSVerify {
    private Digest messDigestOTS;
    private int w;

    public WinternitzOTSVerify(Digest digest, int i) {
        this.w = i;
        this.messDigestOTS = digest;
    }

    public byte[] Verify(byte[] bArr, byte[] bArr2) {
        int i;
        byte[] bArr3 = bArr;
        byte[] bArr4 = bArr2;
        int digestSize = this.messDigestOTS.getDigestSize();
        byte[] bArr5 = new byte[digestSize];
        int i2 = 0;
        this.messDigestOTS.update(bArr3, 0, bArr3.length);
        byte[] bArr6 = new byte[this.messDigestOTS.getDigestSize()];
        this.messDigestOTS.doFinal(bArr6, 0);
        int i3 = digestSize << 3;
        int i4 = ((this.w - 1) + i3) / this.w;
        int log = getLog((i4 << this.w) + 1);
        int i5 = ((((this.w + log) - 1) / this.w) + i4) * digestSize;
        if (i5 != bArr4.length) {
            return null;
        }
        byte[] bArr7 = new byte[i5];
        int i6 = 8;
        if (8 % this.w == 0) {
            int i7 = 8 / this.w;
            int i8 = (1 << this.w) - 1;
            int i9 = 0;
            int i10 = 0;
            byte[] bArr8 = new byte[digestSize];
            int i11 = 0;
            while (i11 < bArr6.length) {
                byte[] bArr9 = bArr8;
                int i12 = i10;
                int i13 = i9;
                int i14 = 0;
                while (i14 < i7) {
                    int i15 = bArr6[i11] & i8;
                    i13 += i15;
                    int i16 = i7;
                    int i17 = i12 * digestSize;
                    System.arraycopy(bArr4, i17, bArr9, 0, digestSize);
                    while (i15 < i8) {
                        this.messDigestOTS.update(bArr9, 0, bArr9.length);
                        bArr9 = new byte[this.messDigestOTS.getDigestSize()];
                        this.messDigestOTS.doFinal(bArr9, 0);
                        i15++;
                        i13 = i13;
                        byte[] bArr10 = bArr2;
                    }
                    int i18 = i13;
                    System.arraycopy(bArr9, 0, bArr7, i17, digestSize);
                    bArr6[i11] = (byte) (bArr6[i11] >>> this.w);
                    i12++;
                    i14++;
                    i7 = i16;
                    bArr4 = bArr2;
                }
                int i19 = i7;
                i11++;
                i9 = i13;
                i10 = i12;
                bArr8 = bArr9;
                bArr4 = bArr2;
            }
            int i20 = (i4 << this.w) - i9;
            for (int i21 = 0; i21 < log; i21 += this.w) {
                int i22 = i10 * digestSize;
                System.arraycopy(bArr2, i22, bArr8, 0, digestSize);
                for (int i23 = i20 & i8; i23 < i8; i23++) {
                    this.messDigestOTS.update(bArr8, 0, bArr8.length);
                    bArr8 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr8, 0);
                }
                System.arraycopy(bArr8, 0, bArr7, i22, digestSize);
                i20 >>>= this.w;
                i10++;
            }
        } else {
            byte[] bArr11 = bArr4;
            if (this.w < 8) {
                int i24 = digestSize / this.w;
                int i25 = (1 << this.w) - 1;
                int i26 = 0;
                int i27 = 0;
                int i28 = 0;
                byte[] bArr12 = new byte[digestSize];
                int i29 = 0;
                while (i29 < i24) {
                    int i30 = i26;
                    long j = 0;
                    for (int i31 = 0; i31 < this.w; i31++) {
                        j ^= (long) ((bArr6[i30] & 255) << (i31 << 3));
                        i30++;
                    }
                    int i32 = 0;
                    byte[] bArr13 = bArr12;
                    while (i32 < i6) {
                        int i33 = log;
                        int i34 = (int) (j & ((long) i25));
                        i27 += i34;
                        int i35 = i28 * digestSize;
                        System.arraycopy(bArr11, i35, bArr13, 0, digestSize);
                        while (i34 < i25) {
                            this.messDigestOTS.update(bArr13, 0, bArr13.length);
                            bArr13 = new byte[this.messDigestOTS.getDigestSize()];
                            this.messDigestOTS.doFinal(bArr13, 0);
                            i34++;
                        }
                        System.arraycopy(bArr13, 0, bArr7, i35, digestSize);
                        j >>>= this.w;
                        i28++;
                        i32++;
                        log = i33;
                        i6 = 8;
                    }
                    int i36 = log;
                    i29++;
                    bArr12 = bArr13;
                    i26 = i30;
                    i6 = 8;
                }
                int i37 = log;
                int i38 = digestSize % this.w;
                long j2 = 0;
                for (int i39 = 0; i39 < i38; i39++) {
                    j2 ^= (long) ((bArr6[i26] & 255) << (i39 << 3));
                    i26++;
                }
                int i40 = i38 << 3;
                byte[] bArr14 = bArr12;
                for (int i41 = 0; i41 < i40; i41 += this.w) {
                    int i42 = (int) (j2 & ((long) i25));
                    i27 += i42;
                    int i43 = i28 * digestSize;
                    System.arraycopy(bArr11, i43, bArr14, 0, digestSize);
                    while (i42 < i25) {
                        this.messDigestOTS.update(bArr14, 0, bArr14.length);
                        bArr14 = new byte[this.messDigestOTS.getDigestSize()];
                        this.messDigestOTS.doFinal(bArr14, 0);
                        i42++;
                    }
                    System.arraycopy(bArr14, 0, bArr7, i43, digestSize);
                    j2 >>>= this.w;
                    i28++;
                }
                int i44 = (i4 << this.w) - i27;
                int i45 = i37;
                for (int i46 = 0; i46 < i45; i46 += this.w) {
                    int i47 = i28 * digestSize;
                    System.arraycopy(bArr11, i47, bArr14, 0, digestSize);
                    for (int i48 = i44 & i25; i48 < i25; i48++) {
                        this.messDigestOTS.update(bArr14, 0, bArr14.length);
                        bArr14 = new byte[this.messDigestOTS.getDigestSize()];
                        this.messDigestOTS.doFinal(bArr14, 0);
                    }
                    System.arraycopy(bArr14, 0, bArr7, i47, digestSize);
                    i44 >>>= this.w;
                    i28++;
                }
            } else {
                int i49 = log;
                if (this.w < 57) {
                    int i50 = i3 - this.w;
                    int i51 = (1 << this.w) - 1;
                    int i52 = 0;
                    int i53 = 0;
                    byte[] bArr15 = new byte[digestSize];
                    int i54 = 0;
                    while (i54 <= i50) {
                        int i55 = i54 >>> 3;
                        int i56 = i54 % 8;
                        i54 += this.w;
                        int i57 = (i54 + 7) >>> 3;
                        int i58 = i55;
                        long j3 = 0;
                        int i59 = i2;
                        while (i58 < i57) {
                            j3 ^= (long) ((bArr6[i58] & 255) << (i59 << 3));
                            i59++;
                            i58++;
                            i4 = i4;
                            i50 = i50;
                        }
                        int i60 = i50;
                        int i61 = i4;
                        long j4 = (long) i51;
                        long j5 = (j3 >>> i56) & j4;
                        int i62 = i51;
                        i52 = (int) (((long) i52) + j5);
                        int i63 = i53 * digestSize;
                        System.arraycopy(bArr11, i63, bArr15, 0, digestSize);
                        while (j5 < j4) {
                            this.messDigestOTS.update(bArr15, 0, bArr15.length);
                            bArr15 = new byte[this.messDigestOTS.getDigestSize()];
                            this.messDigestOTS.doFinal(bArr15, 0);
                            j5++;
                            i54 = i54;
                            i52 = i52;
                        }
                        int i64 = i54;
                        int i65 = i52;
                        System.arraycopy(bArr15, 0, bArr7, i63, digestSize);
                        i53++;
                        i4 = i61;
                        i50 = i60;
                        i51 = i62;
                        i2 = 0;
                    }
                    int i66 = i4;
                    int i67 = i51;
                    int i68 = i54 >>> 3;
                    if (i68 < digestSize) {
                        int i69 = i54 % 8;
                        int i70 = 0;
                        long j6 = 0;
                        while (i68 < digestSize) {
                            j6 ^= (long) ((bArr6[i68] & 255) << (i70 << 3));
                            i70++;
                            i68++;
                        }
                        int i71 = i67;
                        long j7 = (long) i71;
                        long j8 = (j6 >>> i69) & j7;
                        i = i71;
                        i52 = (int) (((long) i52) + j8);
                        int i72 = i53 * digestSize;
                        System.arraycopy(bArr11, i72, bArr15, 0, digestSize);
                        while (j8 < j7) {
                            this.messDigestOTS.update(bArr15, 0, bArr15.length);
                            bArr15 = new byte[this.messDigestOTS.getDigestSize()];
                            this.messDigestOTS.doFinal(bArr15, 0);
                            j8++;
                        }
                        System.arraycopy(bArr15, 0, bArr7, i72, digestSize);
                        i53++;
                    } else {
                        i = i67;
                    }
                    int i73 = (i66 << this.w) - i52;
                    int i74 = 0;
                    while (i74 < i49) {
                        int i75 = i53 * digestSize;
                        System.arraycopy(bArr11, i75, bArr15, 0, digestSize);
                        byte[] bArr16 = bArr7;
                        byte[] bArr17 = bArr15;
                        int i76 = i;
                        for (long j9 = (long) (i73 & i); j9 < ((long) i76); j9++) {
                            this.messDigestOTS.update(bArr17, 0, bArr17.length);
                            bArr17 = new byte[this.messDigestOTS.getDigestSize()];
                            this.messDigestOTS.doFinal(bArr17, 0);
                        }
                        byte[] bArr18 = bArr16;
                        System.arraycopy(bArr17, 0, bArr18, i75, digestSize);
                        i73 >>>= this.w;
                        i53++;
                        i74 += this.w;
                        i = i76;
                        bArr7 = bArr18;
                        bArr15 = bArr17;
                    }
                }
            }
        }
        byte[] bArr19 = bArr7;
        byte[] bArr20 = new byte[digestSize];
        this.messDigestOTS.update(bArr19, 0, bArr19.length);
        byte[] bArr21 = new byte[this.messDigestOTS.getDigestSize()];
        this.messDigestOTS.doFinal(bArr21, 0);
        return bArr21;
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

    public int getSignatureLength() {
        int digestSize = this.messDigestOTS.getDigestSize();
        int i = ((digestSize << 3) + (this.w - 1)) / this.w;
        return digestSize * (i + (((getLog((i << this.w) + 1) + this.w) - 1) / this.w));
    }
}
