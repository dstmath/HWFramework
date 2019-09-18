package org.bouncycastle.crypto.engines;

import org.bouncycastle.asn1.cmc.BodyPartID;
import org.bouncycastle.util.Pack;

public class ChaChaEngine extends Salsa20Engine {
    public ChaChaEngine() {
    }

    public ChaChaEngine(int i) {
        super(i);
    }

    public static void chachaCore(int i, int[] iArr, int[] iArr2) {
        int[] iArr3 = iArr;
        int[] iArr4 = iArr2;
        int i2 = 16;
        if (iArr3.length != 16) {
            throw new IllegalArgumentException();
        } else if (iArr4.length != 16) {
            throw new IllegalArgumentException();
        } else if (i % 2 == 0) {
            boolean z = false;
            int i3 = iArr3[0];
            int i4 = iArr3[1];
            int i5 = iArr3[2];
            int i6 = iArr3[3];
            int i7 = iArr3[4];
            int i8 = iArr3[5];
            int i9 = iArr3[6];
            int i10 = 7;
            int i11 = iArr3[7];
            int i12 = 8;
            int i13 = iArr3[8];
            int i14 = iArr3[9];
            int i15 = iArr3[10];
            int i16 = iArr3[11];
            int i17 = 12;
            int i18 = iArr3[12];
            int i19 = iArr3[13];
            int i20 = iArr3[14];
            int i21 = iArr3[15];
            int i22 = i;
            while (i22 > 0) {
                int i23 = i3 + i7;
                int rotl = rotl(i18 ^ i23, i2);
                int i24 = i13 + rotl;
                int rotl2 = rotl(i7 ^ i24, i17);
                int i25 = i23 + rotl2;
                int rotl3 = rotl(rotl ^ i25, i12);
                int i26 = i24 + rotl3;
                int rotl4 = rotl(rotl2 ^ i26, i10);
                int i27 = i4 + i8;
                int rotl5 = rotl(i19 ^ i27, i2);
                int i28 = i14 + rotl5;
                int rotl6 = rotl(i8 ^ i28, i17);
                int i29 = i27 + rotl6;
                int rotl7 = rotl(rotl5 ^ i29, i12);
                int i30 = i28 + rotl7;
                int rotl8 = rotl(rotl6 ^ i30, i10);
                int i31 = i5 + i9;
                int rotl9 = rotl(i20 ^ i31, i2);
                int i32 = i15 + rotl9;
                int rotl10 = rotl(i9 ^ i32, i17);
                int i33 = i31 + rotl10;
                int rotl11 = rotl(rotl9 ^ i33, i12);
                int i34 = i32 + rotl11;
                int rotl12 = rotl(rotl10 ^ i34, i10);
                int i35 = i6 + i11;
                int rotl13 = rotl(i21 ^ i35, 16);
                int i36 = i16 + rotl13;
                int rotl14 = rotl(i11 ^ i36, i17);
                int i37 = i35 + rotl14;
                int rotl15 = rotl(rotl13 ^ i37, 8);
                int i38 = i36 + rotl15;
                int rotl16 = rotl(rotl14 ^ i38, 7);
                int i39 = i25 + rotl8;
                int rotl17 = rotl(rotl15 ^ i39, 16);
                int i40 = i34 + rotl17;
                int rotl18 = rotl(rotl8 ^ i40, 12);
                i3 = i39 + rotl18;
                i21 = rotl(rotl17 ^ i3, 8);
                i15 = i40 + i21;
                i8 = rotl(rotl18 ^ i15, 7);
                int i41 = i29 + rotl12;
                int rotl19 = rotl(rotl3 ^ i41, 16);
                int i42 = i38 + rotl19;
                int rotl20 = rotl(rotl12 ^ i42, 12);
                i4 = i41 + rotl20;
                i18 = rotl(rotl19 ^ i4, 8);
                i16 = i42 + i18;
                i9 = rotl(rotl20 ^ i16, 7);
                int i43 = i33 + rotl16;
                int rotl21 = rotl(rotl7 ^ i43, 16);
                int i44 = i26 + rotl21;
                int rotl22 = rotl(rotl16 ^ i44, 12);
                i5 = i43 + rotl22;
                i19 = rotl(rotl21 ^ i5, 8);
                i13 = i44 + i19;
                i11 = rotl(rotl22 ^ i13, 7);
                int i45 = i37 + rotl4;
                int rotl23 = rotl(rotl11 ^ i45, 16);
                int i46 = i30 + rotl23;
                int rotl24 = rotl(rotl4 ^ i46, 12);
                i6 = i45 + rotl24;
                i20 = rotl(rotl23 ^ i6, 8);
                i14 = i46 + i20;
                i7 = rotl(rotl24 ^ i14, 7);
                i22 -= 2;
                i2 = 16;
                z = false;
                i17 = 12;
                i12 = 8;
                i10 = 7;
            }
            char c = z;
            iArr4[c] = i3 + iArr3[c];
            iArr4[1] = i4 + iArr3[1];
            iArr4[2] = i5 + iArr3[2];
            iArr4[3] = i6 + iArr3[3];
            iArr4[4] = i7 + iArr3[4];
            iArr4[5] = i8 + iArr3[5];
            iArr4[6] = i9 + iArr3[6];
            iArr4[7] = i11 + iArr3[7];
            iArr4[8] = i13 + iArr3[8];
            iArr4[9] = i14 + iArr3[9];
            iArr4[10] = i15 + iArr3[10];
            iArr4[11] = i16 + iArr3[11];
            iArr4[12] = i18 + iArr3[12];
            iArr4[13] = i19 + iArr3[13];
            iArr4[14] = i20 + iArr3[14];
            iArr4[15] = i21 + iArr3[15];
        } else {
            throw new IllegalArgumentException("Number of rounds must be even");
        }
    }

    /* access modifiers changed from: protected */
    public void advanceCounter() {
        int[] iArr = this.engineState;
        int i = iArr[12] + 1;
        iArr[12] = i;
        if (i == 0) {
            int[] iArr2 = this.engineState;
            iArr2[13] = iArr2[13] + 1;
        }
    }

    /* access modifiers changed from: protected */
    public void advanceCounter(long j) {
        int i = (int) (j >>> 32);
        int i2 = (int) j;
        if (i > 0) {
            int[] iArr = this.engineState;
            iArr[13] = iArr[13] + i;
        }
        int i3 = this.engineState[12];
        int[] iArr2 = this.engineState;
        iArr2[12] = iArr2[12] + i2;
        if (i3 != 0 && this.engineState[12] < i3) {
            int[] iArr3 = this.engineState;
            iArr3[13] = iArr3[13] + 1;
        }
    }

    /* access modifiers changed from: protected */
    public void generateKeyStream(byte[] bArr) {
        chachaCore(this.rounds, this.engineState, this.x);
        Pack.intToLittleEndian(this.x, bArr, 0);
    }

    public String getAlgorithmName() {
        return "ChaCha" + this.rounds;
    }

    /* access modifiers changed from: protected */
    public long getCounter() {
        return (((long) this.engineState[13]) << 32) | (((long) this.engineState[12]) & BodyPartID.bodyIdMax);
    }

    /* access modifiers changed from: protected */
    public void resetCounter() {
        int[] iArr = this.engineState;
        this.engineState[13] = 0;
        iArr[12] = 0;
    }

    /* access modifiers changed from: protected */
    public void retreatCounter() {
        int[] iArr;
        if (this.engineState[12] == 0 && this.engineState[13] == 0) {
            throw new IllegalStateException("attempt to reduce counter past zero.");
        }
        int[] iArr2 = this.engineState;
        int i = iArr2[12] - 1;
        iArr2[12] = i;
        if (i == -1) {
            iArr[13] = this.engineState[13] - 1;
        }
    }

    /* access modifiers changed from: protected */
    public void retreatCounter(long j) {
        int i = (int) (j >>> 32);
        int i2 = (int) j;
        if (i != 0) {
            if ((((long) this.engineState[13]) & BodyPartID.bodyIdMax) >= (((long) i) & BodyPartID.bodyIdMax)) {
                int[] iArr = this.engineState;
                iArr[13] = iArr[13] - i;
            } else {
                throw new IllegalStateException("attempt to reduce counter past zero.");
            }
        }
        if ((((long) this.engineState[12]) & BodyPartID.bodyIdMax) >= (((long) i2) & BodyPartID.bodyIdMax)) {
            int[] iArr2 = this.engineState;
            iArr2[12] = iArr2[12] - i2;
        } else if (this.engineState[13] != 0) {
            int[] iArr3 = this.engineState;
            iArr3[13] = iArr3[13] - 1;
            int[] iArr4 = this.engineState;
            iArr4[12] = iArr4[12] - i2;
        } else {
            throw new IllegalStateException("attempt to reduce counter past zero.");
        }
    }

    /* access modifiers changed from: protected */
    public void setKey(byte[] bArr, byte[] bArr2) {
        if (bArr != null) {
            if (bArr.length == 16 || bArr.length == 32) {
                packTauOrSigma(bArr.length, this.engineState, 0);
                Pack.littleEndianToInt(bArr, 0, this.engineState, 4, 4);
                Pack.littleEndianToInt(bArr, bArr.length - 16, this.engineState, 8, 4);
            } else {
                throw new IllegalArgumentException(getAlgorithmName() + " requires 128 bit or 256 bit key");
            }
        }
        Pack.littleEndianToInt(bArr2, 0, this.engineState, 14, 2);
    }
}
