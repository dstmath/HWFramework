package org.bouncycastle.pqc.crypto.gmss.util;

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
        this.messagesize = (((this.mdsize << 3) + i) - 1) / i;
        this.checksumsize = getLog((this.messagesize << i) + 1);
        this.keysize = this.messagesize + (((this.checksumsize + i) - 1) / i);
        this.privateKeyOTS = new byte[this.keysize][];
        byte[] bArr2 = new byte[this.mdsize];
        System.arraycopy(bArr, 0, bArr2, 0, bArr2.length);
        for (int i2 = 0; i2 < this.keysize; i2++) {
            this.privateKeyOTS[i2] = this.gmssRandom.nextSeed(bArr2);
        }
    }

    private void hashPrivateKeyBlock(int i, int i2, byte[] bArr, int i3) {
        if (i2 < 1) {
            System.arraycopy(this.privateKeyOTS[i], 0, bArr, i3, this.mdsize);
            return;
        }
        this.messDigestOTS.update(this.privateKeyOTS[i], 0, this.mdsize);
        while (true) {
            this.messDigestOTS.doFinal(bArr, i3);
            i2--;
            if (i2 > 0) {
                this.messDigestOTS.update(bArr, i3, this.mdsize);
            } else {
                return;
            }
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
        int i = (1 << this.w) - 1;
        int i2 = 0;
        for (int i3 = 0; i3 < this.keysize; i3++) {
            hashPrivateKeyBlock(i3, i, bArr, i2);
            i2 += this.mdsize;
        }
        this.messDigestOTS.update(bArr, 0, bArr.length);
        byte[] bArr2 = new byte[this.mdsize];
        this.messDigestOTS.doFinal(bArr2, 0);
        return bArr2;
    }

    public byte[] getSignature(byte[] bArr) {
        int i;
        int i2 = this.keysize;
        int i3 = this.mdsize;
        byte[] bArr2 = new byte[(i2 * i3)];
        byte[] bArr3 = new byte[i3];
        int i4 = 0;
        this.messDigestOTS.update(bArr, 0, bArr.length);
        this.messDigestOTS.doFinal(bArr3, 0);
        int i5 = this.w;
        if (8 % i5 == 0) {
            int i6 = 8 / i5;
            int i7 = (1 << i5) - 1;
            int i8 = 0;
            int i9 = 0;
            int i10 = 0;
            while (i8 < bArr3.length) {
                int i11 = i9;
                for (int i12 = 0; i12 < i6; i12++) {
                    int i13 = bArr3[i8] & i7;
                    i11 += i13;
                    hashPrivateKeyBlock(i10, i13, bArr2, this.mdsize * i10);
                    bArr3[i8] = (byte) (bArr3[i8] >>> this.w);
                    i10++;
                }
                i8++;
                i9 = i11;
            }
            int i14 = (this.messagesize << this.w) - i9;
            while (i4 < this.checksumsize) {
                hashPrivateKeyBlock(i10, i14 & i7, bArr2, this.mdsize * i10);
                int i15 = this.w;
                i14 >>>= i15;
                i10++;
                i4 += i15;
            }
        } else if (i5 < 8) {
            int i16 = this.mdsize / i5;
            int i17 = (1 << i5) - 1;
            int i18 = 0;
            int i19 = 0;
            int i20 = 0;
            int i21 = 0;
            while (i18 < i16) {
                int i22 = i19;
                long j = 0;
                for (int i23 = 0; i23 < this.w; i23++) {
                    j ^= (long) ((bArr3[i22] & 255) << (i23 << 3));
                    i22++;
                }
                for (int i24 = 0; i24 < 8; i24++) {
                    int i25 = ((int) j) & i17;
                    i21 += i25;
                    hashPrivateKeyBlock(i20, i25, bArr2, this.mdsize * i20);
                    j >>>= this.w;
                    i20++;
                }
                i18++;
                i19 = i22;
            }
            int i26 = this.mdsize % this.w;
            long j2 = 0;
            for (int i27 = 0; i27 < i26; i27++) {
                j2 ^= (long) ((bArr3[i19] & 255) << (i27 << 3));
                i19++;
            }
            int i28 = i26 << 3;
            int i29 = 0;
            long j3 = j2;
            while (i29 < i28) {
                int i30 = ((int) j3) & i17;
                i21 += i30;
                hashPrivateKeyBlock(i20, i30, bArr2, this.mdsize * i20);
                int i31 = this.w;
                j3 >>>= i31;
                i20++;
                i29 += i31;
            }
            int i32 = (this.messagesize << this.w) - i21;
            while (i4 < this.checksumsize) {
                hashPrivateKeyBlock(i20, i32 & i17, bArr2, this.mdsize * i20);
                int i33 = this.w;
                i32 >>>= i33;
                i20++;
                i4 += i33;
            }
        } else if (i5 < 57) {
            int i34 = this.mdsize;
            int i35 = (i34 << 3) - i5;
            int i36 = (1 << i5) - 1;
            byte[] bArr4 = new byte[i34];
            int i37 = 0;
            int i38 = 0;
            int i39 = 0;
            while (i37 <= i35) {
                int i40 = i37 % 8;
                i37 += this.w;
                int i41 = (i37 + 7) >>> 3;
                int i42 = 0;
                long j4 = 0;
                for (int i43 = i37 >>> 3; i43 < i41; i43++) {
                    j4 ^= (long) ((bArr3[i43] & 255) << (i42 << 3));
                    i42++;
                }
                long j5 = (j4 >>> i40) & ((long) i36);
                i39 = (int) (((long) i39) + j5);
                System.arraycopy(this.privateKeyOTS[i38], 0, bArr4, 0, this.mdsize);
                while (j5 > 0) {
                    this.messDigestOTS.update(bArr4, 0, bArr4.length);
                    this.messDigestOTS.doFinal(bArr4, 0);
                    j5--;
                }
                int i44 = this.mdsize;
                System.arraycopy(bArr4, 0, bArr2, i38 * i44, i44);
                i38++;
            }
            int i45 = i37 >>> 3;
            if (i45 < this.mdsize) {
                int i46 = i37 % 8;
                int i47 = 0;
                long j6 = 0;
                while (true) {
                    i = this.mdsize;
                    if (i45 >= i) {
                        break;
                    }
                    j6 ^= (long) ((bArr3[i45] & 255) << (i47 << 3));
                    i47++;
                    i45++;
                }
                long j7 = (j6 >>> i46) & ((long) i36);
                i39 = (int) (((long) i39) + j7);
                System.arraycopy(this.privateKeyOTS[i38], 0, bArr4, 0, i);
                while (j7 > 0) {
                    this.messDigestOTS.update(bArr4, 0, bArr4.length);
                    this.messDigestOTS.doFinal(bArr4, 0);
                    j7--;
                }
                int i48 = this.mdsize;
                System.arraycopy(bArr4, 0, bArr2, i38 * i48, i48);
                i38++;
            }
            int i49 = (this.messagesize << this.w) - i39;
            int i50 = 0;
            while (i50 < this.checksumsize) {
                System.arraycopy(this.privateKeyOTS[i38], 0, bArr4, 0, this.mdsize);
                for (long j8 = (long) (i49 & i36); j8 > 0; j8--) {
                    this.messDigestOTS.update(bArr4, 0, bArr4.length);
                    this.messDigestOTS.doFinal(bArr4, 0);
                }
                int i51 = this.mdsize;
                System.arraycopy(bArr4, 0, bArr2, i38 * i51, i51);
                int i52 = this.w;
                i49 >>>= i52;
                i38++;
                i50 += i52;
            }
        }
        return bArr2;
    }
}
