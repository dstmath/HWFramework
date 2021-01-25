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
        byte[] bArr3 = bArr2;
        int digestSize = this.messDigestOTS.getDigestSize();
        byte[] bArr4 = new byte[digestSize];
        int i2 = 0;
        this.messDigestOTS.update(bArr, 0, bArr.length);
        byte[] bArr5 = new byte[this.messDigestOTS.getDigestSize()];
        this.messDigestOTS.doFinal(bArr5, 0);
        int i3 = digestSize << 3;
        int i4 = this.w;
        int i5 = ((i4 - 1) + i3) / i4;
        int log = getLog((i5 << i4) + 1);
        int i6 = this.w;
        int i7 = ((((log + i6) - 1) / i6) + i5) * digestSize;
        if (i7 != bArr3.length) {
            return null;
        }
        byte[] bArr6 = new byte[i7];
        int i8 = 8;
        if (8 % i6 == 0) {
            int i9 = 8 / i6;
            int i10 = (1 << i6) - 1;
            int i11 = 0;
            int i12 = 0;
            byte[] bArr7 = new byte[digestSize];
            int i13 = 0;
            while (i13 < bArr5.length) {
                byte[] bArr8 = bArr7;
                int i14 = i12;
                int i15 = i11;
                int i16 = 0;
                while (i16 < i9) {
                    int i17 = bArr5[i13] & i10;
                    i15 += i17;
                    int i18 = i14 * digestSize;
                    System.arraycopy(bArr3, i18, bArr8, 0, digestSize);
                    while (i17 < i10) {
                        this.messDigestOTS.update(bArr8, 0, bArr8.length);
                        bArr8 = new byte[this.messDigestOTS.getDigestSize()];
                        this.messDigestOTS.doFinal(bArr8, 0);
                        i17++;
                        i15 = i15;
                    }
                    System.arraycopy(bArr8, 0, bArr6, i18, digestSize);
                    bArr5[i13] = (byte) (bArr5[i13] >>> this.w);
                    i14++;
                    i16++;
                    i9 = i9;
                    bArr3 = bArr2;
                }
                i13++;
                bArr3 = bArr2;
                i11 = i15;
                i12 = i14;
                bArr7 = bArr8;
            }
            int i19 = (i5 << this.w) - i11;
            int i20 = 0;
            while (i20 < log) {
                int i21 = i12 * digestSize;
                System.arraycopy(bArr2, i21, bArr7, 0, digestSize);
                for (int i22 = i19 & i10; i22 < i10; i22++) {
                    this.messDigestOTS.update(bArr7, 0, bArr7.length);
                    bArr7 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr7, 0);
                }
                System.arraycopy(bArr7, 0, bArr6, i21, digestSize);
                int i23 = this.w;
                i19 >>>= i23;
                i12++;
                i20 += i23;
            }
        } else if (i6 < 8) {
            int i24 = digestSize / i6;
            int i25 = (1 << i6) - 1;
            int i26 = 0;
            int i27 = 0;
            int i28 = 0;
            byte[] bArr9 = new byte[digestSize];
            int i29 = 0;
            while (i29 < i24) {
                int i30 = i26;
                long j = 0;
                for (int i31 = 0; i31 < this.w; i31++) {
                    j ^= (long) ((bArr5[i30] & 255) << (i31 << 3));
                    i30++;
                }
                int i32 = 0;
                byte[] bArr10 = bArr9;
                while (i32 < i8) {
                    int i33 = (int) (j & ((long) i25));
                    i27 += i33;
                    int i34 = i28 * digestSize;
                    System.arraycopy(bArr3, i34, bArr10, 0, digestSize);
                    while (i33 < i25) {
                        this.messDigestOTS.update(bArr10, 0, bArr10.length);
                        bArr10 = new byte[this.messDigestOTS.getDigestSize()];
                        this.messDigestOTS.doFinal(bArr10, 0);
                        i33++;
                    }
                    System.arraycopy(bArr10, 0, bArr6, i34, digestSize);
                    j >>>= this.w;
                    i28++;
                    i32++;
                    i29 = i29;
                    i8 = 8;
                }
                i29++;
                bArr9 = bArr10;
                i26 = i30;
                i8 = 8;
            }
            int i35 = digestSize % this.w;
            long j2 = 0;
            for (int i36 = 0; i36 < i35; i36++) {
                j2 ^= (long) ((bArr5[i26] & 255) << (i36 << 3));
                i26++;
            }
            int i37 = i35 << 3;
            int i38 = 0;
            byte[] bArr11 = bArr9;
            while (i38 < i37) {
                int i39 = (int) (j2 & ((long) i25));
                i27 += i39;
                int i40 = i28 * digestSize;
                System.arraycopy(bArr3, i40, bArr11, 0, digestSize);
                while (i39 < i25) {
                    this.messDigestOTS.update(bArr11, 0, bArr11.length);
                    bArr11 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr11, 0);
                    i39++;
                }
                System.arraycopy(bArr11, 0, bArr6, i40, digestSize);
                int i41 = this.w;
                j2 >>>= i41;
                i28++;
                i38 += i41;
            }
            int i42 = (i5 << this.w) - i27;
            int i43 = 0;
            while (i43 < log) {
                int i44 = i28 * digestSize;
                System.arraycopy(bArr3, i44, bArr11, 0, digestSize);
                for (int i45 = i42 & i25; i45 < i25; i45++) {
                    this.messDigestOTS.update(bArr11, 0, bArr11.length);
                    bArr11 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr11, 0);
                }
                System.arraycopy(bArr11, 0, bArr6, i44, digestSize);
                int i46 = this.w;
                i42 >>>= i46;
                i28++;
                i43 += i46;
            }
        } else if (i6 < 57) {
            int i47 = i3 - i6;
            int i48 = (1 << i6) - 1;
            byte[] bArr12 = new byte[digestSize];
            int i49 = 0;
            int i50 = 0;
            int i51 = 0;
            while (i49 <= i47) {
                int i52 = i49 >>> 3;
                int i53 = i49 % 8;
                int i54 = i49 + this.w;
                int i55 = (i54 + 7) >>> 3;
                int i56 = i2;
                long j3 = 0;
                while (i52 < i55) {
                    j3 ^= (long) ((bArr5[i52] & 255) << (i56 << 3));
                    i56++;
                    i52++;
                    log = log;
                    i47 = i47;
                }
                long j4 = (long) i48;
                long j5 = (j3 >>> i53) & j4;
                i50 = (int) (((long) i50) + j5);
                int i57 = i51 * digestSize;
                System.arraycopy(bArr3, i57, bArr12, 0, digestSize);
                while (j5 < j4) {
                    this.messDigestOTS.update(bArr12, 0, bArr12.length);
                    bArr12 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr12, 0);
                    j5++;
                    j4 = j4;
                }
                System.arraycopy(bArr12, 0, bArr6, i57, digestSize);
                i51++;
                i5 = i5;
                i48 = i48;
                i49 = i54;
                log = log;
                i47 = i47;
                i2 = 0;
            }
            int i58 = i49 >>> 3;
            if (i58 < digestSize) {
                int i59 = i49 % 8;
                int i60 = 0;
                long j6 = 0;
                while (i58 < digestSize) {
                    j6 ^= (long) ((bArr5[i58] & 255) << (i60 << 3));
                    i60++;
                    i58++;
                }
                i = i48;
                long j7 = (long) i;
                long j8 = (j6 >>> i59) & j7;
                i50 = (int) (((long) i50) + j8);
                int i61 = i51 * digestSize;
                System.arraycopy(bArr3, i61, bArr12, 0, digestSize);
                while (j8 < j7) {
                    this.messDigestOTS.update(bArr12, 0, bArr12.length);
                    bArr12 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr12, 0);
                    j8++;
                    j7 = j7;
                }
                System.arraycopy(bArr12, 0, bArr6, i61, digestSize);
                i51++;
            } else {
                i = i48;
            }
            int i62 = (i5 << this.w) - i50;
            int i63 = 0;
            while (i63 < log) {
                int i64 = i51 * digestSize;
                System.arraycopy(bArr3, i64, bArr12, 0, digestSize);
                for (long j9 = (long) (i62 & i); j9 < ((long) i); j9++) {
                    this.messDigestOTS.update(bArr12, 0, bArr12.length);
                    bArr12 = new byte[this.messDigestOTS.getDigestSize()];
                    this.messDigestOTS.doFinal(bArr12, 0);
                }
                System.arraycopy(bArr12, 0, bArr6, i64, digestSize);
                int i65 = this.w;
                i62 >>>= i65;
                i51++;
                i63 += i65;
                bArr6 = bArr6;
            }
        }
        byte[] bArr13 = new byte[digestSize];
        this.messDigestOTS.update(bArr6, 0, bArr6.length);
        byte[] bArr14 = new byte[this.messDigestOTS.getDigestSize()];
        this.messDigestOTS.doFinal(bArr14, 0);
        return bArr14;
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
        int i = this.w;
        int i2 = ((digestSize << 3) + (i - 1)) / i;
        int log = getLog((i2 << i) + 1);
        int i3 = this.w;
        return digestSize * (i2 + (((log + i3) - 1) / i3));
    }
}
