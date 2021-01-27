package org.bouncycastle.crypto.engines;

import java.lang.reflect.Array;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.math.ec.Tnaf;
import org.bouncycastle.pqc.crypto.rainbow.util.GF2Field;
import org.bouncycastle.util.Pack;

public class AESLightEngine implements BlockCipher {
    private static final int BLOCK_SIZE = 16;
    private static final byte[] S = {99, 124, 119, 123, -14, 107, 111, -59, 48, 1, 103, 43, -2, -41, -85, 118, -54, -126, -55, 125, -6, 89, 71, -16, -83, -44, -94, -81, -100, -92, 114, -64, -73, -3, -109, 38, 54, 63, -9, -52, 52, -91, -27, -15, 113, -40, 49, 21, 4, -57, 35, -61, 24, -106, 5, -102, 7, 18, Byte.MIN_VALUE, -30, -21, 39, -78, 117, 9, -125, 44, 26, 27, 110, 90, -96, 82, 59, -42, -77, 41, -29, 47, -124, 83, -47, 0, -19, 32, -4, -79, 91, 106, -53, -66, 57, 74, 76, 88, -49, -48, -17, -86, -5, 67, 77, 51, -123, 69, -7, 2, Byte.MAX_VALUE, 80, 60, -97, -88, 81, -93, 64, -113, -110, -99, 56, -11, PSSSigner.TRAILER_IMPLICIT, -74, -38, 33, Tnaf.POW_2_WIDTH, -1, -13, -46, -51, 12, 19, -20, 95, -105, 68, 23, -60, -89, 126, 61, 100, 93, 25, 115, 96, -127, 79, -36, 34, 42, -112, -120, 70, -18, -72, 20, -34, 94, 11, -37, -32, 50, 58, 10, 73, 6, 36, 92, -62, -45, -84, 98, -111, -107, -28, 121, -25, -56, 55, 109, -115, -43, 78, -87, 108, 86, -12, -22, 101, 122, -82, 8, -70, 120, 37, 46, 28, -90, -76, -58, -24, -35, 116, 31, 75, -67, -117, -118, 112, 62, -75, 102, 72, 3, -10, 14, 97, 53, 87, -71, -122, -63, 29, -98, -31, -8, -104, 17, 105, -39, -114, -108, -101, 30, -121, -23, -50, 85, 40, -33, -116, -95, -119, 13, -65, -26, 66, 104, 65, -103, 45, 15, -80, 84, -69, 22};
    private static final byte[] Si = {82, 9, 106, -43, 48, 54, -91, 56, -65, 64, -93, -98, -127, -13, -41, -5, 124, -29, 57, -126, -101, 47, -1, -121, 52, -114, 67, 68, -60, -34, -23, -53, 84, 123, -108, 50, -90, -62, 35, 61, -18, 76, -107, 11, 66, -6, -61, 78, 8, 46, -95, 102, 40, -39, 36, -78, 118, 91, -94, 73, 109, -117, -47, 37, 114, -8, -10, 100, -122, 104, -104, 22, -44, -92, 92, -52, 93, 101, -74, -110, 108, 112, 72, 80, -3, -19, -71, -38, 94, 21, 70, 87, -89, -115, -99, -124, -112, -40, -85, 0, -116, PSSSigner.TRAILER_IMPLICIT, -45, 10, -9, -28, 88, 5, -72, -77, 69, 6, -48, 44, 30, -113, -54, 63, 15, 2, -63, -81, -67, 3, 1, 19, -118, 107, 58, -111, 17, 65, 79, 103, -36, -22, -105, -14, -49, -50, -16, -76, -26, 115, -106, -84, 116, 34, -25, -83, 53, -123, -30, -7, 55, -24, 28, 117, -33, 110, 71, -15, 26, 113, 29, 41, -59, -119, 111, -73, 98, 14, -86, 24, -66, 27, -4, 86, 62, 75, -58, -46, 121, 32, -102, -37, -64, -2, 120, -51, 90, -12, 31, -35, -88, 51, -120, 7, -57, 49, -79, 18, Tnaf.POW_2_WIDTH, 89, 39, Byte.MIN_VALUE, -20, 95, 96, 81, Byte.MAX_VALUE, -87, 25, -75, 74, 13, 45, -27, 122, -97, -109, -55, -100, -17, -96, -32, 59, 77, -82, 42, -11, -80, -56, -21, -69, 60, -125, 83, -103, 97, 23, 43, 4, 126, -70, 119, -42, 38, -31, 105, 20, 99, 85, 33, 12, 125};
    private static final int m1 = -2139062144;
    private static final int m2 = 2139062143;
    private static final int m3 = 27;
    private static final int m4 = -1061109568;
    private static final int m5 = 1061109567;
    private static final int[] rcon = {1, 2, 4, 8, 16, 32, 64, 128, 27, 54, 108, 216, 171, 77, 154, 47, 94, 188, 99, 198, 151, 53, 106, 212, 179, 125, 250, 239, 197, 145};
    private int C0;
    private int C1;
    private int C2;
    private int C3;
    private int ROUNDS;
    private int[][] WorkingKey = null;
    private boolean forEncryption;

    private static int FFmulX(int i) {
        return (((i & m1) >>> 7) * 27) ^ ((m2 & i) << 1);
    }

    private static int FFmulX2(int i) {
        int i2 = i & m4;
        int i3 = i2 ^ (i2 >>> 1);
        return (i3 >>> 5) ^ (((m5 & i) << 2) ^ (i3 >>> 2));
    }

    private void decryptBlock(int[][] iArr) {
        int i = this.C0;
        int i2 = this.ROUNDS;
        int i3 = i ^ iArr[i2][0];
        int i4 = this.C1 ^ iArr[i2][1];
        int i5 = this.C2 ^ iArr[i2][2];
        int i6 = i2 - 1;
        int i7 = iArr[i2][3] ^ this.C3;
        while (true) {
            byte[] bArr = Si;
            int i8 = i3 & GF2Field.MASK;
            if (i6 > 1) {
                int inv_mcol = inv_mcol((bArr[(i4 >> 24) & GF2Field.MASK] << 24) ^ (((bArr[i8] & 255) ^ ((bArr[(i7 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr[(i5 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i6][0];
                byte[] bArr2 = Si;
                int inv_mcol2 = inv_mcol((bArr2[(i5 >> 24) & GF2Field.MASK] << 24) ^ (((bArr2[i4 & GF2Field.MASK] & 255) ^ ((bArr2[(i3 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr2[(i7 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i6][1];
                byte[] bArr3 = Si;
                int inv_mcol3 = inv_mcol((bArr3[(i7 >> 24) & GF2Field.MASK] << 24) ^ (((bArr3[i5 & GF2Field.MASK] & 255) ^ ((bArr3[(i4 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr3[(i3 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i6][2];
                byte[] bArr4 = Si;
                int i9 = i6 - 1;
                int inv_mcol4 = inv_mcol((bArr4[(i3 >> 24) & GF2Field.MASK] << 24) ^ (((bArr4[i7 & GF2Field.MASK] & 255) ^ ((bArr4[(i5 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr4[(i4 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i6][3];
                byte[] bArr5 = Si;
                int inv_mcol5 = inv_mcol((bArr5[(inv_mcol2 >> 24) & GF2Field.MASK] << 24) ^ (((bArr5[inv_mcol & GF2Field.MASK] & 255) ^ ((bArr5[(inv_mcol4 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr5[(inv_mcol3 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i9][0];
                byte[] bArr6 = Si;
                int inv_mcol6 = inv_mcol((bArr6[(inv_mcol3 >> 24) & GF2Field.MASK] << 24) ^ (((bArr6[inv_mcol2 & GF2Field.MASK] & 255) ^ ((bArr6[(inv_mcol >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr6[(inv_mcol4 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i9][1];
                byte[] bArr7 = Si;
                int inv_mcol7 = inv_mcol((bArr7[(inv_mcol4 >> 24) & GF2Field.MASK] << 24) ^ (((bArr7[inv_mcol3 & GF2Field.MASK] & 255) ^ ((bArr7[(inv_mcol2 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr7[(inv_mcol >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i9][2];
                byte[] bArr8 = Si;
                int inv_mcol8 = inv_mcol((((bArr8[inv_mcol4 & GF2Field.MASK] & 255) ^ ((bArr8[(inv_mcol3 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr8[(inv_mcol2 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr8[(inv_mcol >> 24) & GF2Field.MASK] << 24));
                int i10 = i9 - 1;
                i7 = iArr[i9][3] ^ inv_mcol8;
                i3 = inv_mcol5;
                i4 = inv_mcol6;
                i5 = inv_mcol7;
                i6 = i10;
            } else {
                int inv_mcol9 = inv_mcol((bArr[(i4 >> 24) & GF2Field.MASK] << 24) ^ (((bArr[i8] & 255) ^ ((bArr[(i7 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr[(i5 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i6][0];
                byte[] bArr9 = Si;
                int inv_mcol10 = inv_mcol((bArr9[(i5 >> 24) & GF2Field.MASK] << 24) ^ (((bArr9[i4 & GF2Field.MASK] & 255) ^ ((bArr9[(i3 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr9[(i7 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i6][1];
                byte[] bArr10 = Si;
                int inv_mcol11 = inv_mcol((bArr10[(i7 >> 24) & GF2Field.MASK] << 24) ^ (((bArr10[i5 & GF2Field.MASK] & 255) ^ ((bArr10[(i4 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr10[(i3 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i6][2];
                byte[] bArr11 = Si;
                int inv_mcol12 = inv_mcol((bArr11[(i3 >> 24) & GF2Field.MASK] << 24) ^ (((bArr11[i7 & GF2Field.MASK] & 255) ^ ((bArr11[(i5 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr11[(i4 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i6][3];
                byte[] bArr12 = Si;
                this.C0 = ((((bArr12[inv_mcol9 & GF2Field.MASK] & 255) ^ ((bArr12[(inv_mcol12 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr12[(inv_mcol11 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr12[(inv_mcol10 >> 24) & GF2Field.MASK] << 24)) ^ iArr[0][0];
                this.C1 = ((((bArr12[inv_mcol10 & GF2Field.MASK] & 255) ^ ((bArr12[(inv_mcol9 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr12[(inv_mcol12 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr12[(inv_mcol11 >> 24) & GF2Field.MASK] << 24)) ^ iArr[0][1];
                this.C2 = ((((bArr12[inv_mcol11 & GF2Field.MASK] & 255) ^ ((bArr12[(inv_mcol10 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr12[(inv_mcol9 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr12[(inv_mcol12 >> 24) & GF2Field.MASK] << 24)) ^ iArr[0][2];
                this.C3 = iArr[0][3] ^ ((((bArr12[inv_mcol12 & GF2Field.MASK] & 255) ^ ((bArr12[(inv_mcol11 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr12[(inv_mcol10 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr12[(inv_mcol9 >> 24) & GF2Field.MASK] << 24));
                return;
            }
        }
    }

    private void encryptBlock(int[][] iArr) {
        int i = this.C0 ^ iArr[0][0];
        int i2 = this.C1 ^ iArr[0][1];
        int i3 = this.C2 ^ iArr[0][2];
        int i4 = this.C3 ^ iArr[0][3];
        int i5 = i3;
        int i6 = i2;
        int i7 = i;
        int i8 = 1;
        while (i8 < this.ROUNDS - 1) {
            byte[] bArr = S;
            int mcol = mcol((bArr[(i4 >> 24) & GF2Field.MASK] << 24) ^ (((bArr[i7 & GF2Field.MASK] & 255) ^ ((bArr[(i6 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr[(i5 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i8][0];
            byte[] bArr2 = S;
            int mcol2 = mcol((bArr2[(i7 >> 24) & GF2Field.MASK] << 24) ^ (((bArr2[i6 & GF2Field.MASK] & 255) ^ ((bArr2[(i5 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr2[(i4 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i8][1];
            byte[] bArr3 = S;
            int mcol3 = mcol((bArr3[(i6 >> 24) & GF2Field.MASK] << 24) ^ (((bArr3[i5 & GF2Field.MASK] & 255) ^ ((bArr3[(i4 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr3[(i7 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i8][2];
            byte[] bArr4 = S;
            int mcol4 = mcol(((((bArr4[(i7 >> 8) & GF2Field.MASK] & 255) << 8) ^ (bArr4[i4 & GF2Field.MASK] & 255)) ^ ((bArr4[(i6 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr4[(i5 >> 24) & GF2Field.MASK] << 24));
            int i9 = i8 + 1;
            int i10 = iArr[i8][3] ^ mcol4;
            byte[] bArr5 = S;
            i7 = mcol((bArr5[(i10 >> 24) & GF2Field.MASK] << 24) ^ (((bArr5[mcol & GF2Field.MASK] & 255) ^ ((bArr5[(mcol2 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr5[(mcol3 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i9][0];
            byte[] bArr6 = S;
            int mcol5 = mcol((bArr6[(mcol >> 24) & GF2Field.MASK] << 24) ^ (((bArr6[mcol2 & GF2Field.MASK] & 255) ^ ((bArr6[(mcol3 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr6[(i10 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i9][1];
            byte[] bArr7 = S;
            int mcol6 = mcol((bArr7[(mcol2 >> 24) & GF2Field.MASK] << 24) ^ (((bArr7[mcol3 & GF2Field.MASK] & 255) ^ ((bArr7[(i10 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr7[(mcol >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i9][2];
            byte[] bArr8 = S;
            int i11 = i9 + 1;
            int mcol7 = mcol((((bArr8[i10 & GF2Field.MASK] & 255) ^ ((bArr8[(mcol >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr8[(mcol2 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr8[(mcol3 >> 24) & GF2Field.MASK] << 24)) ^ iArr[i9][3];
            i6 = mcol5;
            i5 = mcol6;
            i4 = mcol7;
            i8 = i11;
        }
        byte[] bArr9 = S;
        int mcol8 = mcol((bArr9[(i4 >> 24) & GF2Field.MASK] << 24) ^ (((bArr9[i7 & GF2Field.MASK] & 255) ^ ((bArr9[(i6 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr9[(i5 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i8][0];
        byte[] bArr10 = S;
        int mcol9 = mcol((bArr10[(i7 >> 24) & GF2Field.MASK] << 24) ^ (((bArr10[i6 & GF2Field.MASK] & 255) ^ ((bArr10[(i5 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr10[(i4 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i8][1];
        byte[] bArr11 = S;
        int mcol10 = mcol((bArr11[(i6 >> 24) & GF2Field.MASK] << 24) ^ (((bArr11[i5 & GF2Field.MASK] & 255) ^ ((bArr11[(i4 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr11[(i7 >> 16) & GF2Field.MASK] & 255) << 16))) ^ iArr[i8][2];
        byte[] bArr12 = S;
        int mcol11 = mcol(((((bArr12[(i7 >> 8) & GF2Field.MASK] & 255) << 8) ^ (bArr12[i4 & GF2Field.MASK] & 255)) ^ ((bArr12[(i6 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr12[(i5 >> 24) & GF2Field.MASK] << 24));
        int i12 = i8 + 1;
        int i13 = iArr[i8][3] ^ mcol11;
        byte[] bArr13 = S;
        this.C0 = iArr[i12][0] ^ ((((bArr13[mcol8 & GF2Field.MASK] & 255) ^ ((bArr13[(mcol9 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr13[(mcol10 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr13[(i13 >> 24) & GF2Field.MASK] << 24));
        this.C1 = ((((bArr13[mcol9 & GF2Field.MASK] & 255) ^ ((bArr13[(mcol10 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr13[(i13 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr13[(mcol8 >> 24) & GF2Field.MASK] << 24)) ^ iArr[i12][1];
        this.C2 = ((((bArr13[mcol10 & GF2Field.MASK] & 255) ^ ((bArr13[(i13 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr13[(mcol8 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr13[(mcol9 >> 24) & GF2Field.MASK] << 24)) ^ iArr[i12][2];
        this.C3 = iArr[i12][3] ^ ((((bArr13[i13 & GF2Field.MASK] & 255) ^ ((bArr13[(mcol8 >> 8) & GF2Field.MASK] & 255) << 8)) ^ ((bArr13[(mcol9 >> 16) & GF2Field.MASK] & 255) << 16)) ^ (bArr13[(mcol10 >> 24) & GF2Field.MASK] << 24));
    }

    private int[][] generateWorkingKey(byte[] bArr, boolean z) {
        int length = bArr.length;
        if (length < 16 || length > 32 || (length & 7) != 0) {
            throw new IllegalArgumentException("Key length not 128/192/256 bits.");
        }
        int i = length >> 2;
        this.ROUNDS = i + 6;
        int[][] iArr = (int[][]) Array.newInstance(int.class, this.ROUNDS + 1, 4);
        if (i == 4) {
            int littleEndianToInt = Pack.littleEndianToInt(bArr, 0);
            iArr[0][0] = littleEndianToInt;
            int littleEndianToInt2 = Pack.littleEndianToInt(bArr, 4);
            iArr[0][1] = littleEndianToInt2;
            int littleEndianToInt3 = Pack.littleEndianToInt(bArr, 8);
            iArr[0][2] = littleEndianToInt3;
            int littleEndianToInt4 = Pack.littleEndianToInt(bArr, 12);
            iArr[0][3] = littleEndianToInt4;
            int i2 = littleEndianToInt3;
            int i3 = littleEndianToInt;
            int i4 = littleEndianToInt4;
            for (int i5 = 1; i5 <= 10; i5++) {
                i3 ^= subWord(shift(i4, 8)) ^ rcon[i5 - 1];
                iArr[i5][0] = i3;
                littleEndianToInt2 ^= i3;
                iArr[i5][1] = littleEndianToInt2;
                i2 ^= littleEndianToInt2;
                iArr[i5][2] = i2;
                i4 ^= i2;
                iArr[i5][3] = i4;
            }
        } else if (i == 6) {
            int littleEndianToInt5 = Pack.littleEndianToInt(bArr, 0);
            iArr[0][0] = littleEndianToInt5;
            int littleEndianToInt6 = Pack.littleEndianToInt(bArr, 4);
            iArr[0][1] = littleEndianToInt6;
            int littleEndianToInt7 = Pack.littleEndianToInt(bArr, 8);
            iArr[0][2] = littleEndianToInt7;
            int littleEndianToInt8 = Pack.littleEndianToInt(bArr, 12);
            iArr[0][3] = littleEndianToInt8;
            int littleEndianToInt9 = Pack.littleEndianToInt(bArr, 16);
            iArr[1][0] = littleEndianToInt9;
            int littleEndianToInt10 = Pack.littleEndianToInt(bArr, 20);
            iArr[1][1] = littleEndianToInt10;
            int subWord = littleEndianToInt5 ^ (subWord(shift(littleEndianToInt10, 8)) ^ 1);
            iArr[1][2] = subWord;
            int i6 = littleEndianToInt6 ^ subWord;
            iArr[1][3] = i6;
            int i7 = littleEndianToInt7 ^ i6;
            iArr[2][0] = i7;
            int i8 = littleEndianToInt8 ^ i7;
            iArr[2][1] = i8;
            int i9 = littleEndianToInt9 ^ i8;
            iArr[2][2] = i9;
            int i10 = littleEndianToInt10 ^ i9;
            iArr[2][3] = i10;
            int i11 = i9;
            int i12 = 2;
            int i13 = i8;
            int i14 = i7;
            int i15 = subWord;
            int i16 = i10;
            for (int i17 = 3; i17 < 12; i17 += 3) {
                int subWord2 = subWord(shift(i16, 8)) ^ i12;
                int i18 = i12 << 1;
                int i19 = i15 ^ subWord2;
                iArr[i17][0] = i19;
                int i20 = i6 ^ i19;
                iArr[i17][1] = i20;
                int i21 = i14 ^ i20;
                iArr[i17][2] = i21;
                int i22 = i13 ^ i21;
                iArr[i17][3] = i22;
                int i23 = i11 ^ i22;
                int i24 = i17 + 1;
                iArr[i24][0] = i23;
                int i25 = i16 ^ i23;
                iArr[i24][1] = i25;
                int subWord3 = subWord(shift(i25, 8)) ^ i18;
                i12 = i18 << 1;
                i15 = i19 ^ subWord3;
                iArr[i24][2] = i15;
                i6 = i20 ^ i15;
                iArr[i24][3] = i6;
                i14 = i21 ^ i6;
                int i26 = i17 + 2;
                iArr[i26][0] = i14;
                i13 = i22 ^ i14;
                iArr[i26][1] = i13;
                i11 = i23 ^ i13;
                iArr[i26][2] = i11;
                i16 = i25 ^ i11;
                iArr[i26][3] = i16;
            }
            int subWord4 = (subWord(shift(i16, 8)) ^ i12) ^ i15;
            iArr[12][0] = subWord4;
            int i27 = subWord4 ^ i6;
            iArr[12][1] = i27;
            int i28 = i27 ^ i14;
            iArr[12][2] = i28;
            iArr[12][3] = i28 ^ i13;
        } else if (i == 8) {
            int littleEndianToInt11 = Pack.littleEndianToInt(bArr, 0);
            iArr[0][0] = littleEndianToInt11;
            int littleEndianToInt12 = Pack.littleEndianToInt(bArr, 4);
            iArr[0][1] = littleEndianToInt12;
            int littleEndianToInt13 = Pack.littleEndianToInt(bArr, 8);
            iArr[0][2] = littleEndianToInt13;
            int littleEndianToInt14 = Pack.littleEndianToInt(bArr, 12);
            iArr[0][3] = littleEndianToInt14;
            int littleEndianToInt15 = Pack.littleEndianToInt(bArr, 16);
            iArr[1][0] = littleEndianToInt15;
            int littleEndianToInt16 = Pack.littleEndianToInt(bArr, 20);
            iArr[1][1] = littleEndianToInt16;
            int littleEndianToInt17 = Pack.littleEndianToInt(bArr, 24);
            iArr[1][2] = littleEndianToInt17;
            int littleEndianToInt18 = Pack.littleEndianToInt(bArr, 28);
            iArr[1][3] = littleEndianToInt18;
            int i29 = littleEndianToInt11;
            int i30 = littleEndianToInt17;
            int i31 = littleEndianToInt18;
            int i32 = littleEndianToInt16;
            int i33 = littleEndianToInt15;
            int i34 = 1;
            for (int i35 = 2; i35 < 14; i35 += 2) {
                int subWord5 = subWord(shift(i31, 8)) ^ i34;
                i34 <<= 1;
                i29 ^= subWord5;
                iArr[i35][0] = i29;
                littleEndianToInt12 ^= i29;
                iArr[i35][1] = littleEndianToInt12;
                littleEndianToInt13 ^= littleEndianToInt12;
                iArr[i35][2] = littleEndianToInt13;
                littleEndianToInt14 ^= littleEndianToInt13;
                iArr[i35][3] = littleEndianToInt14;
                i33 ^= subWord(littleEndianToInt14);
                int i36 = i35 + 1;
                iArr[i36][0] = i33;
                i32 ^= i33;
                iArr[i36][1] = i32;
                i30 ^= i32;
                iArr[i36][2] = i30;
                i31 ^= i30;
                iArr[i36][3] = i31;
            }
            int subWord6 = (subWord(shift(i31, 8)) ^ i34) ^ i29;
            iArr[14][0] = subWord6;
            int i37 = subWord6 ^ littleEndianToInt12;
            iArr[14][1] = i37;
            int i38 = i37 ^ littleEndianToInt13;
            iArr[14][2] = i38;
            iArr[14][3] = i38 ^ littleEndianToInt14;
        } else {
            throw new IllegalStateException("Should never get here");
        }
        if (!z) {
            for (int i39 = 1; i39 < this.ROUNDS; i39++) {
                for (int i40 = 0; i40 < 4; i40++) {
                    iArr[i39][i40] = inv_mcol(iArr[i39][i40]);
                }
            }
        }
        return iArr;
    }

    private static int inv_mcol(int i) {
        int shift = shift(i, 8) ^ i;
        int FFmulX = i ^ FFmulX(shift);
        int FFmulX2 = shift ^ FFmulX2(FFmulX);
        return FFmulX ^ (FFmulX2 ^ shift(FFmulX2, 16));
    }

    private static int mcol(int i) {
        int shift = shift(i, 8);
        int i2 = i ^ shift;
        return FFmulX(i2) ^ (shift ^ shift(i2, 16));
    }

    private void packBlock(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = this.C0;
        bArr[i] = (byte) i3;
        int i4 = i2 + 1;
        bArr[i2] = (byte) (i3 >> 8);
        int i5 = i4 + 1;
        bArr[i4] = (byte) (i3 >> 16);
        int i6 = i5 + 1;
        bArr[i5] = (byte) (i3 >> 24);
        int i7 = i6 + 1;
        int i8 = this.C1;
        bArr[i6] = (byte) i8;
        int i9 = i7 + 1;
        bArr[i7] = (byte) (i8 >> 8);
        int i10 = i9 + 1;
        bArr[i9] = (byte) (i8 >> 16);
        int i11 = i10 + 1;
        bArr[i10] = (byte) (i8 >> 24);
        int i12 = i11 + 1;
        int i13 = this.C2;
        bArr[i11] = (byte) i13;
        int i14 = i12 + 1;
        bArr[i12] = (byte) (i13 >> 8);
        int i15 = i14 + 1;
        bArr[i14] = (byte) (i13 >> 16);
        int i16 = i15 + 1;
        bArr[i15] = (byte) (i13 >> 24);
        int i17 = i16 + 1;
        int i18 = this.C3;
        bArr[i16] = (byte) i18;
        int i19 = i17 + 1;
        bArr[i17] = (byte) (i18 >> 8);
        bArr[i19] = (byte) (i18 >> 16);
        bArr[i19 + 1] = (byte) (i18 >> 24);
    }

    private static int shift(int i, int i2) {
        return (i << (-i2)) | (i >>> i2);
    }

    private static int subWord(int i) {
        byte[] bArr = S;
        return (bArr[(i >> 24) & GF2Field.MASK] << 24) | (bArr[i & GF2Field.MASK] & 255) | ((bArr[(i >> 8) & GF2Field.MASK] & 255) << 8) | ((bArr[(i >> 16) & GF2Field.MASK] & 255) << 16);
    }

    private void unpackBlock(byte[] bArr, int i) {
        int i2 = i + 1;
        this.C0 = bArr[i] & 255;
        int i3 = i2 + 1;
        this.C0 |= (bArr[i2] & 255) << 8;
        int i4 = i3 + 1;
        this.C0 |= (bArr[i3] & 255) << 16;
        int i5 = i4 + 1;
        this.C0 |= bArr[i4] << 24;
        int i6 = i5 + 1;
        this.C1 = bArr[i5] & 255;
        int i7 = i6 + 1;
        this.C1 = ((bArr[i6] & 255) << 8) | this.C1;
        int i8 = i7 + 1;
        this.C1 |= (bArr[i7] & 255) << 16;
        int i9 = i8 + 1;
        this.C1 |= bArr[i8] << 24;
        int i10 = i9 + 1;
        this.C2 = bArr[i9] & 255;
        int i11 = i10 + 1;
        this.C2 = ((bArr[i10] & 255) << 8) | this.C2;
        int i12 = i11 + 1;
        this.C2 |= (bArr[i11] & 255) << 16;
        int i13 = i12 + 1;
        this.C2 |= bArr[i12] << 24;
        int i14 = i13 + 1;
        this.C3 = bArr[i13] & 255;
        int i15 = i14 + 1;
        this.C3 = ((bArr[i14] & 255) << 8) | this.C3;
        this.C3 |= (bArr[i15] & 255) << 16;
        this.C3 = (bArr[i15 + 1] << 24) | this.C3;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public String getAlgorithmName() {
        return "AES";
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int getBlockSize() {
        return 16;
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void init(boolean z, CipherParameters cipherParameters) {
        if (cipherParameters instanceof KeyParameter) {
            this.WorkingKey = generateWorkingKey(((KeyParameter) cipherParameters).getKey(), z);
            this.forEncryption = z;
            return;
        }
        throw new IllegalArgumentException("invalid parameter passed to AES init - " + cipherParameters.getClass().getName());
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) {
        if (this.WorkingKey == null) {
            throw new IllegalStateException("AES engine not initialised");
        } else if (i + 16 > bArr.length) {
            throw new DataLengthException("input buffer too short");
        } else if (i2 + 16 <= bArr2.length) {
            boolean z = this.forEncryption;
            unpackBlock(bArr, i);
            int[][] iArr = this.WorkingKey;
            if (z) {
                encryptBlock(iArr);
            } else {
                decryptBlock(iArr);
            }
            packBlock(bArr2, i2);
            return 16;
        } else {
            throw new OutputLengthException("output buffer too short");
        }
    }

    @Override // org.bouncycastle.crypto.BlockCipher
    public void reset() {
    }
}
