package org.bouncycastle.pqc.crypto.gmss.util;

import org.bouncycastle.crypto.Digest;

public class WinternitzOTSVerify {
    private int mdsize = this.messDigestOTS.getDigestSize();
    private Digest messDigestOTS;
    private int w;

    public WinternitzOTSVerify(Digest digest, int i) {
        this.w = i;
        this.messDigestOTS = digest;
    }

    private void hashSignatureBlock(byte[] bArr, int i, int i2, byte[] bArr2, int i3) {
        if (i2 < 1) {
            System.arraycopy(bArr, i, bArr2, i3, this.mdsize);
            return;
        }
        this.messDigestOTS.update(bArr, i, this.mdsize);
        while (true) {
            this.messDigestOTS.doFinal(bArr2, i3);
            i2--;
            if (i2 > 0) {
                this.messDigestOTS.update(bArr2, i3, this.mdsize);
            } else {
                return;
            }
        }
    }

    public byte[] Verify(byte[] bArr, byte[] bArr2) {
        int i;
        int i2;
        byte[] bArr3 = new byte[this.mdsize];
        int i3 = 0;
        this.messDigestOTS.update(bArr, 0, bArr.length);
        this.messDigestOTS.doFinal(bArr3, 0);
        int i4 = this.w;
        int i5 = ((this.mdsize << 3) + (i4 - 1)) / i4;
        int log = getLog((i5 << i4) + 1);
        int i6 = this.w;
        int i7 = this.mdsize;
        int i8 = ((((log + i6) - 1) / i6) + i5) * i7;
        if (i8 != bArr2.length) {
            return null;
        }
        byte[] bArr4 = new byte[i8];
        int i9 = 8;
        if (8 % i6 == 0) {
            int i10 = 8 / i6;
            int i11 = (1 << i6) - 1;
            int i12 = 0;
            int i13 = 0;
            int i14 = 0;
            while (i14 < bArr3.length) {
                int i15 = i13;
                int i16 = 0;
                while (i16 < i10) {
                    int i17 = bArr3[i14] & i11;
                    int i18 = i12 + i17;
                    int i19 = this.mdsize;
                    hashSignatureBlock(bArr2, i15 * i19, i11 - i17, bArr4, i15 * i19);
                    bArr3[i14] = (byte) (bArr3[i14] >>> this.w);
                    i15++;
                    i16++;
                    i12 = i18;
                    i14 = i14;
                }
                i14++;
                i13 = i15;
            }
            int i20 = i13;
            int i21 = (i5 << this.w) - i12;
            int i22 = 0;
            while (i22 < log) {
                int i23 = this.mdsize;
                hashSignatureBlock(bArr2, i20 * i23, i11 - (i21 & i11), bArr4, i20 * i23);
                int i24 = this.w;
                i21 >>>= i24;
                i20++;
                i22 += i24;
            }
            i = 0;
        } else {
            if (i6 < 8) {
                int i25 = i7 / i6;
                int i26 = (1 << i6) - 1;
                int i27 = 0;
                int i28 = 0;
                int i29 = 0;
                int i30 = 0;
                while (i30 < i25) {
                    int i31 = i27;
                    int i32 = i3;
                    long j = 0;
                    while (i32 < this.w) {
                        j ^= (long) ((bArr3[i31] & 255) << (i32 << 3));
                        i31++;
                        i32++;
                        i5 = i5;
                    }
                    int i33 = i29;
                    int i34 = 0;
                    while (i34 < i9) {
                        int i35 = (int) (j & ((long) i26));
                        int i36 = i28 + i35;
                        int i37 = this.mdsize;
                        hashSignatureBlock(bArr2, i33 * i37, i26 - i35, bArr4, i33 * i37);
                        j >>>= this.w;
                        i33++;
                        i34++;
                        i28 = i36;
                        i26 = i26;
                        i30 = i30;
                        i25 = i25;
                        i9 = 8;
                    }
                    i30++;
                    i29 = i33;
                    i27 = i31;
                    i3 = 0;
                    i9 = 8;
                    i5 = i5;
                }
                int i38 = this.mdsize % this.w;
                int i39 = i27;
                long j2 = 0;
                for (int i40 = 0; i40 < i38; i40++) {
                    j2 ^= (long) ((bArr3[i39] & 255) << (i40 << 3));
                    i39++;
                }
                int i41 = i38 << 3;
                int i42 = i29;
                int i43 = 0;
                while (i43 < i41) {
                    int i44 = (int) (j2 & ((long) i26));
                    int i45 = i28 + i44;
                    int i46 = this.mdsize;
                    hashSignatureBlock(bArr2, i42 * i46, i26 - i44, bArr4, i42 * i46);
                    int i47 = this.w;
                    j2 >>>= i47;
                    i42++;
                    i43 += i47;
                    i28 = i45;
                }
                int i48 = (i5 << this.w) - i28;
                int i49 = 0;
                while (i49 < log) {
                    int i50 = this.mdsize;
                    hashSignatureBlock(bArr2, i42 * i50, i26 - (i48 & i26), bArr4, i42 * i50);
                    int i51 = this.w;
                    i48 >>>= i51;
                    i42++;
                    i49 += i51;
                }
            } else if (i6 < 57) {
                int i52 = (i7 << 3) - i6;
                int i53 = (1 << i6) - 1;
                byte[] bArr5 = new byte[i7];
                int i54 = 0;
                int i55 = 0;
                int i56 = 0;
                while (i54 <= i52) {
                    int i57 = i54 >>> 3;
                    int i58 = i54 % 8;
                    i54 += this.w;
                    int i59 = (i54 + 7) >>> 3;
                    int i60 = 0;
                    long j3 = 0;
                    while (i57 < i59) {
                        j3 ^= (long) ((bArr3[i57] & 255) << (i60 << 3));
                        i60++;
                        i57++;
                        log = log;
                    }
                    long j4 = (long) i53;
                    long j5 = (j3 >>> i58) & j4;
                    i55 = (int) (((long) i55) + j5);
                    int i61 = this.mdsize;
                    System.arraycopy(bArr2, i56 * i61, bArr5, 0, i61);
                    while (j5 < j4) {
                        this.messDigestOTS.update(bArr5, 0, bArr5.length);
                        this.messDigestOTS.doFinal(bArr5, 0);
                        j5++;
                    }
                    int i62 = this.mdsize;
                    System.arraycopy(bArr5, 0, bArr4, i56 * i62, i62);
                    i56++;
                    i52 = i52;
                    log = log;
                }
                int i63 = i54 >>> 3;
                if (i63 < this.mdsize) {
                    int i64 = i54 % 8;
                    int i65 = 0;
                    long j6 = 0;
                    while (true) {
                        i2 = this.mdsize;
                        if (i63 >= i2) {
                            break;
                        }
                        j6 ^= (long) ((bArr3[i63] & 255) << (i65 << 3));
                        i65++;
                        i63++;
                    }
                    long j7 = (long) i53;
                    long j8 = (j6 >>> i64) & j7;
                    i55 = (int) (((long) i55) + j8);
                    System.arraycopy(bArr2, i56 * i2, bArr5, 0, i2);
                    while (j8 < j7) {
                        this.messDigestOTS.update(bArr5, 0, bArr5.length);
                        this.messDigestOTS.doFinal(bArr5, 0);
                        j8++;
                    }
                    int i66 = this.mdsize;
                    System.arraycopy(bArr5, 0, bArr4, i56 * i66, i66);
                    i56++;
                }
                int i67 = (i5 << this.w) - i55;
                int i68 = 0;
                while (i68 < log) {
                    int i69 = this.mdsize;
                    System.arraycopy(bArr2, i56 * i69, bArr5, 0, i69);
                    for (long j9 = (long) (i67 & i53); j9 < ((long) i53); j9++) {
                        this.messDigestOTS.update(bArr5, 0, bArr5.length);
                        this.messDigestOTS.doFinal(bArr5, 0);
                    }
                    int i70 = this.mdsize;
                    System.arraycopy(bArr5, 0, bArr4, i56 * i70, i70);
                    int i71 = this.w;
                    i67 >>>= i71;
                    i56++;
                    i68 += i71;
                }
            }
            i = 0;
        }
        this.messDigestOTS.update(bArr4, i, bArr4.length);
        byte[] bArr6 = new byte[this.mdsize];
        this.messDigestOTS.doFinal(bArr6, i);
        return bArr6;
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
