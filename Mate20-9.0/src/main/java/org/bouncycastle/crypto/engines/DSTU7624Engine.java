package org.bouncycastle.crypto.engines;

import org.bouncycastle.asn1.cmc.BodyPartID;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.OutputLengthException;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class DSTU7624Engine implements BlockCipher {
    private static final int ROUNDS_128 = 10;
    private static final int ROUNDS_256 = 14;
    private static final int ROUNDS_512 = 18;
    private static final byte[] S0 = {-88, 67, 95, 6, 107, 117, 108, 89, 113, -33, -121, -107, 23, -16, -40, 9, 109, -13, 29, -53, -55, 77, 44, -81, 121, -32, -105, -3, 111, 75, 69, 57, 62, -35, -93, 79, -76, -74, -102, 14, 31, -65, 21, -31, 73, -46, -109, -58, -110, 114, -98, 97, -47, 99, -6, -18, -12, 25, -43, -83, 88, -92, -69, -95, -36, -14, -125, 55, 66, -28, 122, 50, -100, -52, -85, 74, -113, 110, 4, 39, 46, -25, -30, 90, -106, 22, 35, 43, -62, 101, 102, 15, PSSSigner.TRAILER_IMPLICIT, -87, 71, 65, 52, 72, -4, -73, 106, -120, -91, 83, -122, -7, 91, -37, 56, 123, -61, 30, 34, 51, 36, 40, 54, -57, -78, 59, -114, 119, -70, -11, 20, -97, 8, 85, -101, 76, -2, 96, 92, -38, 24, 70, -51, 125, 33, -80, 63, 27, -119, -1, -21, -124, 105, 58, -99, -41, -45, 112, 103, 64, -75, -34, 93, 48, -111, -79, 120, 17, 1, -27, 0, 104, -104, -96, -59, 2, -90, 116, 45, 11, -94, 118, -77, -66, -50, -67, -82, -23, -118, 49, 28, -20, -15, -103, -108, -86, -10, 38, 47, -17, -24, -116, 53, 3, -44, Byte.MAX_VALUE, -5, 5, -63, 94, -112, 32, 61, -126, -9, -22, 10, 13, 126, -8, 80, 26, -60, 7, 87, -72, 60, 98, -29, -56, -84, 82, 100, Tnaf.POW_2_WIDTH, -48, -39, 19, 12, 18, 41, 81, -71, -49, -42, 115, -115, -127, 84, -64, -19, 78, 68, -89, 42, -123, 37, -26, -54, 124, -117, 86, Byte.MIN_VALUE};
    private static final byte[] S1 = {-50, -69, -21, -110, -22, -53, 19, -63, -23, 58, -42, -78, -46, -112, 23, -8, 66, 21, 86, -76, 101, 28, -120, 67, -59, 92, 54, -70, -11, 87, 103, -115, 49, -10, 100, 88, -98, -12, 34, -86, 117, 15, 2, -79, -33, 109, 115, 77, 124, 38, 46, -9, 8, 93, 68, 62, -97, 20, -56, -82, 84, Tnaf.POW_2_WIDTH, -40, PSSSigner.TRAILER_IMPLICIT, 26, 107, 105, -13, -67, 51, -85, -6, -47, -101, 104, 78, 22, -107, -111, -18, 76, 99, -114, 91, -52, 60, 25, -95, -127, 73, 123, -39, 111, 55, 96, -54, -25, 43, 72, -3, -106, 69, -4, 65, 18, 13, 121, -27, -119, -116, -29, 32, 48, -36, -73, 108, 74, -75, 63, -105, -44, 98, 45, 6, -92, -91, -125, 95, 42, -38, -55, 0, 126, -94, 85, -65, 17, -43, -100, -49, 14, 10, 61, 81, 125, -109, 27, -2, -60, 71, 9, -122, 11, -113, -99, 106, 7, -71, -80, -104, 24, 50, 113, 75, -17, 59, 112, -96, -28, 64, -1, -61, -87, -26, 120, -7, -117, 70, Byte.MIN_VALUE, 30, 56, -31, -72, -88, -32, 12, 35, 118, 29, 37, 36, 5, -15, 110, -108, 40, -102, -124, -24, -93, 79, 119, -45, -123, -30, 82, -14, -126, 80, 122, 47, 116, 83, -77, 97, -81, 57, 53, -34, -51, 31, -103, -84, -83, 114, 44, -35, -48, -121, -66, 94, -90, -20, 4, -58, 3, 52, -5, -37, 89, -74, -62, 1, -16, 90, -19, -89, 102, 33, Byte.MAX_VALUE, -118, 39, -57, -64, 41, -41};
    private static final byte[] S2 = {-109, -39, -102, -75, -104, 34, 69, -4, -70, 106, -33, 2, -97, -36, 81, 89, 74, 23, 43, -62, -108, -12, -69, -93, 98, -28, 113, -44, -51, 112, 22, -31, 73, 60, -64, -40, 92, -101, -83, -123, 83, -95, 122, -56, 45, -32, -47, 114, -90, 44, -60, -29, 118, 120, -73, -76, 9, 59, 14, 65, 76, -34, -78, -112, 37, -91, -41, 3, 17, 0, -61, 46, -110, -17, 78, 18, -99, 125, -53, 53, Tnaf.POW_2_WIDTH, -43, 79, -98, 77, -87, 85, -58, -48, 123, 24, -105, -45, 54, -26, 72, 86, -127, -113, 119, -52, -100, -71, -30, -84, -72, 47, 21, -92, 124, -38, 56, 30, 11, 5, -42, 20, 110, 108, 126, 102, -3, -79, -27, 96, -81, 94, 51, -121, -55, -16, 93, 109, 63, -120, -115, -57, -9, 29, -23, -20, -19, Byte.MIN_VALUE, 41, 39, -49, -103, -88, 80, 15, 55, 36, 40, 48, -107, -46, 62, 91, 64, -125, -77, 105, 87, 31, 7, 28, -118, PSSSigner.TRAILER_IMPLICIT, 32, -21, -50, -114, -85, -18, 49, -94, 115, -7, -54, 58, 26, -5, 13, -63, -2, -6, -14, 111, -67, -106, -35, 67, 82, -74, 8, -13, -82, -66, 25, -119, 50, 38, -80, -22, 75, 100, -124, -126, 107, -11, 121, -65, 1, 95, 117, 99, 27, 35, 61, 104, 42, 101, -24, -111, -10, -1, 19, 88, -15, 71, 10, Byte.MAX_VALUE, -59, -89, -25, 97, 90, 6, 70, 68, 66, 4, -96, -37, 57, -122, 84, -86, -116, 52, 33, -117, -8, 12, 116, 103};
    private static final byte[] S3 = {104, -115, -54, 77, 115, 75, 78, 42, -44, 82, 38, -77, 84, 30, 25, 31, 34, 3, 70, 61, 45, 74, 83, -125, 19, -118, -73, -43, 37, 121, -11, -67, 88, 47, 13, 2, -19, 81, -98, 17, -14, 62, 85, 94, -47, 22, 60, 102, 112, 93, -13, 69, 64, -52, -24, -108, 86, 8, -50, 26, 58, -46, -31, -33, -75, 56, 110, 14, -27, -12, -7, -122, -23, 79, -42, -123, 35, -49, 50, -103, 49, 20, -82, -18, -56, 72, -45, 48, -95, -110, 65, -79, 24, -60, 44, 113, 114, 68, 21, -3, 55, -66, 95, -86, -101, -120, -40, -85, -119, -100, -6, 96, -22, PSSSigner.TRAILER_IMPLICIT, 98, 12, 36, -90, -88, -20, 103, 32, -37, 124, 40, -35, -84, 91, 52, 126, Tnaf.POW_2_WIDTH, -15, 123, -113, 99, -96, 5, -102, 67, 119, 33, -65, 39, 9, -61, -97, -74, -41, 41, -62, -21, -64, -92, -117, -116, 29, -5, -1, -63, -78, -105, 46, -8, 101, -10, 117, 7, 4, 73, 51, -28, -39, -71, -48, 66, -57, 108, -112, 0, -114, 111, 80, 1, -59, -38, 71, 63, -51, 105, -94, -30, 122, -89, -58, -109, 15, 10, 6, -26, 43, -106, -93, 28, -81, 106, 18, -124, 57, -25, -80, -126, -9, -2, -99, -121, 92, -127, 53, -34, -76, -91, -4, Byte.MIN_VALUE, -17, -53, -69, 107, 118, -70, 90, 125, 120, 11, -107, -29, -83, 116, -104, 59, 54, 100, 109, -36, -16, 89, -87, 76, 23, Byte.MAX_VALUE, -111, -72, -55, 87, 27, -32, 97};
    private static final byte[] T0 = {-92, -94, -87, -59, 78, -55, 3, -39, 126, 15, -46, -83, -25, -45, 39, 91, -29, -95, -24, -26, 124, 42, 85, 12, -122, 57, -41, -115, -72, 18, 111, 40, -51, -118, 112, 86, 114, -7, -65, 79, 115, -23, -9, 87, 22, -84, 80, -64, -99, -73, 71, 113, 96, -60, 116, 67, 108, 31, -109, 119, -36, -50, 32, -116, -103, 95, 68, 1, -11, 30, -121, 94, 97, 44, 75, 29, -127, 21, -12, 35, -42, -22, -31, 103, -15, Byte.MAX_VALUE, -2, -38, 60, 7, 83, 106, -124, -100, -53, 2, -125, 51, -35, 53, -30, 89, 90, -104, -91, -110, 100, 4, 6, Tnaf.POW_2_WIDTH, 77, 28, -105, 8, 49, -18, -85, 5, -81, 121, -96, 24, 70, 109, -4, -119, -44, -57, -1, -16, -49, 66, -111, -8, 104, 10, 101, -114, -74, -3, -61, -17, 120, 76, -52, -98, 48, 46, PSSSigner.TRAILER_IMPLICIT, 11, 84, 26, -90, -69, 38, Byte.MIN_VALUE, 72, -108, 50, 125, -89, 63, -82, 34, 61, 102, -86, -10, 0, 93, -67, 74, -32, 59, -76, 23, -117, -97, 118, -80, 36, -102, 37, 99, -37, -21, 122, 62, 92, -77, -79, 41, -14, -54, 88, 110, -40, -88, 47, 117, -33, 20, -5, 19, 73, -120, -78, -20, -28, 52, 45, -106, -58, 58, -19, -107, 14, -27, -123, 107, 64, 33, -101, 9, 25, 43, 82, -34, 69, -93, -6, 81, -62, -75, -47, -112, -71, -13, 55, -63, 13, -70, 65, 17, 56, 123, -66, -48, -43, 105, 54, -56, 98, 27, -126, -113};
    private static final byte[] T1 = {-125, -14, 42, -21, -23, -65, 123, -100, 52, -106, -115, -104, -71, 105, -116, 41, 61, -120, 104, 6, 57, 17, 76, 14, -96, 86, 64, -110, 21, PSSSigner.TRAILER_IMPLICIT, -77, -36, 111, -8, 38, -70, -66, -67, 49, -5, -61, -2, Byte.MIN_VALUE, 97, -31, 122, 50, -46, 112, 32, -95, 69, -20, -39, 26, 93, -76, -40, 9, -91, 85, -114, 55, 118, -87, 103, Tnaf.POW_2_WIDTH, 23, 54, 101, -79, -107, 98, 89, 116, -93, 80, 47, 75, -56, -48, -113, -51, -44, 60, -122, 18, 29, 35, -17, -12, 83, 25, 53, -26, Byte.MAX_VALUE, 94, -42, 121, 81, 34, 20, -9, 30, 74, 66, -101, 65, 115, 45, -63, 92, -90, -94, -32, 46, -45, 40, -69, -55, -82, 106, -47, 90, 48, -112, -124, -7, -78, 88, -49, 126, -59, -53, -105, -28, 22, 108, -6, -80, 109, 31, 82, -103, 13, 78, 3, -111, -62, 77, 100, 119, -97, -35, -60, 73, -118, -102, 36, 56, -89, 87, -123, -57, 124, 125, -25, -10, -73, -84, 39, 70, -34, -33, 59, -41, -98, 43, 11, -43, 19, 117, -16, 114, -74, -99, 27, 1, 63, 68, -27, -121, -3, 7, -15, -85, -108, 24, -22, -4, 58, -126, 95, 5, 84, -37, 0, -117, -29, 72, 12, -54, 120, -119, 10, -1, 62, 91, -127, -18, 113, -30, -38, 44, -72, -75, -52, 110, -88, 107, -83, 96, -58, 8, 4, 2, -24, -11, 79, -92, -13, -64, -50, 67, 37, 28, 33, 51, 15, -81, 71, -19, 102, 99, -109, -86};
    private static final byte[] T2 = {69, -44, 11, 67, -15, 114, -19, -92, -62, 56, -26, 113, -3, -74, 58, -107, 80, 68, 75, -30, 116, 107, 30, 17, 90, -58, -76, -40, -91, -118, 112, -93, -88, -6, 5, -39, -105, 64, -55, -112, -104, -113, -36, 18, 49, 44, 71, 106, -103, -82, -56, Byte.MAX_VALUE, -7, 79, 93, -106, 111, -12, -77, 57, 33, -38, -100, -123, -98, 59, -16, -65, -17, 6, -18, -27, 95, 32, Tnaf.POW_2_WIDTH, -52, 60, 84, 74, 82, -108, 14, -64, 40, -10, 86, 96, -94, -29, 15, -20, -99, 36, -125, 126, -43, 124, -21, 24, -41, -51, -35, 120, -1, -37, -95, 9, -48, 118, -124, 117, -69, 29, 26, 47, -80, -2, -42, 52, 99, 53, -46, 42, 89, 109, 77, 119, -25, -114, 97, -49, -97, -50, 39, -11, Byte.MIN_VALUE, -122, -57, -90, -5, -8, -121, -85, 98, 63, -33, 72, 0, 20, -102, -67, 91, 4, -110, 2, 37, 101, 76, 83, 12, -14, 41, -81, 23, 108, 65, 48, -23, -109, 85, -9, -84, 104, 38, -60, 125, -54, 122, 62, -96, 55, 3, -63, 54, 105, 102, 8, 22, -89, PSSSigner.TRAILER_IMPLICIT, -59, -45, 34, -73, 19, 70, 50, -24, 87, -120, 43, -127, -78, 78, 100, 28, -86, -111, 88, 46, -101, 92, 27, 81, 115, 66, 35, 1, 110, -13, 13, -66, 61, 10, 45, 31, 103, 51, 25, 123, 94, -22, -34, -117, -53, -87, -116, -115, -83, 73, -126, -28, -70, -61, 21, -47, -32, -119, -4, -79, -71, -75, 7, 121, -72, -31};
    private static final byte[] T3 = {-78, -74, 35, 17, -89, -120, -59, -90, 57, -113, -60, -24, 115, 34, 67, -61, -126, 39, -51, 24, 81, 98, 45, -9, 92, 14, 59, -3, -54, -101, 13, 15, 121, -116, Tnaf.POW_2_WIDTH, 76, 116, 28, 10, -114, 124, -108, 7, -57, 94, 20, -95, 33, 87, 80, 78, -87, Byte.MIN_VALUE, -39, -17, 100, 65, -49, 60, -18, 46, 19, 41, -70, 52, 90, -82, -118, 97, 51, 18, -71, 85, -88, 21, 5, -10, 3, 6, 73, -75, 37, 9, 22, 12, 42, 56, -4, 32, -12, -27, Byte.MAX_VALUE, -41, 49, 43, 102, 111, -1, 114, -122, -16, -93, 47, 120, 0, PSSSigner.TRAILER_IMPLICIT, -52, -30, -80, -15, 66, -76, 48, 95, 96, 4, -20, -91, -29, -117, -25, 29, -65, -124, 123, -26, -127, -8, -34, -40, -46, 23, -50, 75, 71, -42, 105, 108, 25, -103, -102, 1, -77, -123, -79, -7, 89, -62, 55, -23, -56, -96, -19, 79, -119, 104, 109, -43, 38, -111, -121, 88, -67, -55, -104, -36, 117, -64, 118, -11, 103, 107, 126, -21, 82, -53, -47, 91, -97, 11, -37, 64, -110, 26, -6, -84, -28, -31, 113, 31, 101, -115, -105, -98, -107, -112, 93, -73, -63, -81, 84, -5, 2, -32, 53, -69, 58, 77, -83, 44, 61, 86, 8, 27, 74, -109, 106, -85, -72, 122, -14, 125, -38, 63, -2, 62, -66, -22, -86, 68, -58, -48, 54, 72, 112, -106, 119, 36, 83, -33, -13, -125, 40, 50, 69, 30, -92, -45, -94, 70, 110, -100, -35, 99, -44, -99};
    private boolean forEncryption;
    private long[] internalState;
    private long[][] roundKeys;
    private int roundsAmount;
    private int wordsInBlock;
    private int wordsInKey;
    private long[] workingKey;

    public DSTU7624Engine(int i) throws IllegalArgumentException {
        if (i == 128 || i == 256 || i == 512) {
            this.wordsInBlock = i >>> 6;
            this.internalState = new long[this.wordsInBlock];
            return;
        }
        throw new IllegalArgumentException("unsupported block length: only 128/256/512 are allowed");
    }

    private void addRoundKey(int i) {
        long[] jArr = this.roundKeys[i];
        for (int i2 = 0; i2 < this.wordsInBlock; i2++) {
            long[] jArr2 = this.internalState;
            jArr2[i2] = jArr2[i2] + jArr[i2];
        }
    }

    private void decryptBlock_128(byte[] bArr, int i, byte[] bArr2, int i2) {
        byte[] bArr3 = bArr2;
        int i3 = i2;
        long littleEndianToLong = Pack.littleEndianToLong(bArr, i);
        long littleEndianToLong2 = Pack.littleEndianToLong(bArr, i + 8);
        long[] jArr = this.roundKeys[this.roundsAmount];
        long j = littleEndianToLong - jArr[0];
        long j2 = littleEndianToLong2 - jArr[1];
        int i4 = this.roundsAmount;
        while (true) {
            long mixColumnInv = mixColumnInv(j);
            long mixColumnInv2 = mixColumnInv(j2);
            int i5 = (int) mixColumnInv;
            int i6 = (int) (mixColumnInv >>> 32);
            int i7 = (int) mixColumnInv2;
            int i8 = (int) (mixColumnInv2 >>> 32);
            byte b = (T0[i5 & 255] & 255) | ((T1[(i5 >>> 8) & 255] & 255) << 8) | ((T2[(i5 >>> 16) & 255] & 255) << Tnaf.POW_2_WIDTH) | (T3[i5 >>> 24] << 24);
            long j3 = (((long) ((T3[i8 >>> 24] << 24) | (((T0[i8 & 255] & 255) | ((T1[(i8 >>> 8) & 255] & 255) << 8)) | ((T2[(i8 >>> 16) & 255] & 255) << Tnaf.POW_2_WIDTH)))) << 32) | (((long) b) & BodyPartID.bodyIdMax);
            byte b2 = T0[i7 & 255];
            byte b3 = T1[(i7 >>> 8) & 255];
            byte b4 = T2[(i7 >>> 16) & 255];
            byte b5 = (T3[i7 >>> 24] << 24) | ((b4 & 255) << Tnaf.POW_2_WIDTH) | (b2 & 255) | ((b3 & 255) << 8);
            long j4 = (((long) ((T3[i6 >>> 24] << 24) | (((T0[i6 & 255] & 255) | ((T1[(i6 >>> 8) & 255] & 255) << 8)) | ((T2[(i6 >>> 16) & 255] & 255) << Tnaf.POW_2_WIDTH)))) << 32) | (((long) b5) & BodyPartID.bodyIdMax);
            i4--;
            if (i4 == 0) {
                long[] jArr2 = this.roundKeys[0];
                Pack.longToLittleEndian(j3 - jArr2[0], bArr3, i3);
                Pack.longToLittleEndian(j4 - jArr2[1], bArr3, i3 + 8);
                return;
            }
            long[] jArr3 = this.roundKeys[i4];
            long j5 = j4 ^ jArr3[1];
            j = j3 ^ jArr3[0];
            j2 = j5;
        }
    }

    private void encryptBlock_128(byte[] bArr, int i, byte[] bArr2, int i2) {
        byte[] bArr3 = bArr2;
        int i3 = i2;
        long littleEndianToLong = Pack.littleEndianToLong(bArr, i);
        long littleEndianToLong2 = Pack.littleEndianToLong(bArr, i + 8);
        long[] jArr = this.roundKeys[0];
        long j = littleEndianToLong + jArr[0];
        long j2 = littleEndianToLong2 + jArr[1];
        int i4 = 0;
        while (true) {
            int i5 = (int) j;
            int i6 = (int) (j >>> 32);
            int i7 = (int) j2;
            int i8 = (int) (j2 >>> 32);
            byte b = (S0[i5 & 255] & 255) | ((S1[(i5 >>> 8) & 255] & 255) << 8) | ((S2[(i5 >>> 16) & 255] & 255) << Tnaf.POW_2_WIDTH) | (S3[i5 >>> 24] << 24);
            long j3 = (((long) ((S3[i8 >>> 24] << 24) | (((S0[i8 & 255] & 255) | ((S1[(i8 >>> 8) & 255] & 255) << 8)) | ((S2[(i8 >>> 16) & 255] & 255) << Tnaf.POW_2_WIDTH)))) << 32) | (((long) b) & BodyPartID.bodyIdMax);
            byte b2 = S0[i7 & 255];
            byte b3 = S1[(i7 >>> 8) & 255];
            byte b4 = S2[(i7 >>> 16) & 255];
            byte b5 = (S3[i7 >>> 24] << 24) | ((b4 & 255) << Tnaf.POW_2_WIDTH) | (b2 & 255) | ((b3 & 255) << 8);
            long j4 = ((long) ((S3[i6 >>> 24] << 24) | (((S0[i6 & 255] & 255) | ((S1[(i6 >>> 8) & 255] & 255) << 8)) | ((S2[(i6 >>> 16) & 255] & 255) << Tnaf.POW_2_WIDTH)))) << 32;
            long mixColumn = mixColumn(j3);
            long mixColumn2 = mixColumn(j4 | (((long) b5) & BodyPartID.bodyIdMax));
            i4++;
            if (i4 == this.roundsAmount) {
                long[] jArr2 = this.roundKeys[this.roundsAmount];
                Pack.longToLittleEndian(mixColumn + jArr2[0], bArr3, i3);
                Pack.longToLittleEndian(mixColumn2 + jArr2[1], bArr3, i3 + 8);
                return;
            }
            long[] jArr3 = this.roundKeys[i4];
            long j5 = mixColumn2 ^ jArr3[1];
            j = mixColumn ^ jArr3[0];
            j2 = j5;
        }
    }

    private void invShiftRows() {
        int i = this.wordsInBlock;
        if (i == 2) {
            long j = this.internalState[0];
            long j2 = this.internalState[1];
            long j3 = (j ^ j2) & -4294967296L;
            this.internalState[0] = j ^ j3;
            this.internalState[1] = j3 ^ j2;
        } else if (i == 4) {
            long j4 = this.internalState[0];
            long j5 = this.internalState[1];
            long j6 = this.internalState[2];
            long j7 = this.internalState[3];
            long j8 = (j4 ^ j5) & -281470681808896L;
            long j9 = j4 ^ j8;
            long j10 = j5 ^ j8;
            long j11 = (j6 ^ j7) & -281470681808896L;
            long j12 = j6 ^ j11;
            long j13 = j7 ^ j11;
            long j14 = (j9 ^ j12) & -4294967296L;
            long j15 = j9 ^ j14;
            long j16 = (j10 ^ j13) & 281474976645120L;
            this.internalState[0] = j15;
            this.internalState[1] = j10 ^ j16;
            this.internalState[2] = j12 ^ j14;
            this.internalState[3] = j16 ^ j13;
        } else if (i == 8) {
            long j17 = this.internalState[0];
            long j18 = this.internalState[1];
            long j19 = this.internalState[2];
            long j20 = this.internalState[3];
            long j21 = this.internalState[4];
            long j22 = this.internalState[5];
            long j23 = this.internalState[6];
            long j24 = this.internalState[7];
            long j25 = (j17 ^ j18) & -71777214294589696L;
            long j26 = j17 ^ j25;
            long j27 = j18 ^ j25;
            long j28 = (j19 ^ j20) & -71777214294589696L;
            long j29 = j19 ^ j28;
            long j30 = j20 ^ j28;
            long j31 = (j21 ^ j22) & -71777214294589696L;
            long j32 = j21 ^ j31;
            long j33 = j22 ^ j31;
            long j34 = (j23 ^ j24) & -71777214294589696L;
            long j35 = j23 ^ j34;
            long j36 = j24 ^ j34;
            long j37 = (j26 ^ j29) & -281470681808896L;
            long j38 = j26 ^ j37;
            long j39 = j29 ^ j37;
            long j40 = (j27 ^ j30) & 72056494543077120L;
            long j41 = j27 ^ j40;
            long j42 = j30 ^ j40;
            long j43 = (j32 ^ j35) & -281470681808896L;
            long j44 = j32 ^ j43;
            long j45 = j35 ^ j43;
            long j46 = (j33 ^ j36) & 72056494543077120L;
            long j47 = j33 ^ j46;
            long j48 = j36 ^ j46;
            long j49 = (j38 ^ j44) & -4294967296L;
            long j50 = j38 ^ j49;
            long j51 = j44 ^ j49;
            long j52 = (j41 ^ j47) & 72057594021150720L;
            long j53 = j41 ^ j52;
            long j54 = (j39 ^ j45) & 281474976645120L;
            long j55 = j39 ^ j54;
            long j56 = j54 ^ j45;
            long j57 = (j42 ^ j48) & 1099511627520L;
            this.internalState[0] = j50;
            this.internalState[1] = j53;
            this.internalState[2] = j55;
            this.internalState[3] = j42 ^ j57;
            this.internalState[4] = j51;
            this.internalState[5] = j47 ^ j52;
            this.internalState[6] = j56;
            this.internalState[7] = j48 ^ j57;
        } else {
            throw new IllegalStateException("unsupported block length: only 128/256/512 are allowed");
        }
    }

    private void invSubBytes() {
        for (int i = 0; i < this.wordsInBlock; i++) {
            long j = this.internalState[i];
            int i2 = (int) j;
            int i3 = (int) (j >>> 32);
            byte b = (T0[i2 & 255] & 255) | ((T1[(i2 >>> 8) & 255] & 255) << 8) | ((T2[(i2 >>> 16) & 255] & 255) << Tnaf.POW_2_WIDTH) | (T3[i2 >>> 24] << 24);
            byte b2 = T0[i3 & 255];
            byte b3 = T1[(i3 >>> 8) & 255];
            byte b4 = T2[(i3 >>> 16) & 255];
            this.internalState[i] = (((long) ((T3[i3 >>> 24] << 24) | (((b2 & 255) | ((b3 & 255) << 8)) | ((b4 & 255) << Tnaf.POW_2_WIDTH)))) << 32) | (((long) b) & BodyPartID.bodyIdMax);
        }
    }

    private static long mixColumn(long j) {
        long mulX = mulX(j);
        long rotate = rotate(8, j) ^ j;
        long rotate2 = (rotate ^ rotate(16, rotate)) ^ rotate(48, j);
        return ((rotate(32, mulX2((j ^ rotate2) ^ mulX)) ^ rotate2) ^ rotate(40, mulX)) ^ rotate(48, mulX);
    }

    private static long mixColumnInv(long j) {
        long j2 = j;
        long rotate = rotate(8, j2) ^ j2;
        long rotate2 = (rotate ^ rotate(32, rotate)) ^ rotate(48, j2);
        long j3 = rotate2 ^ j2;
        long rotate3 = rotate(48, j2);
        long rotate4 = rotate(56, j2);
        long rotate5 = rotate(16, rotate2);
        return mulX(rotate(40, ((j2 ^ rotate(32, j3)) ^ rotate4) ^ mulX(((rotate3 ^ (rotate(24, j2) ^ j3)) ^ rotate4) ^ mulX(mulX(mulX(rotate(40, mulX(mulX(j3 ^ rotate4) ^ rotate(56, j3)) ^ j2) ^ (rotate(16, j3) ^ j2)) ^ (j3 ^ rotate3)) ^ rotate5)))) ^ rotate2;
    }

    private void mixColumns() {
        for (int i = 0; i < this.wordsInBlock; i++) {
            this.internalState[i] = mixColumn(this.internalState[i]);
        }
    }

    private void mixColumnsInv() {
        for (int i = 0; i < this.wordsInBlock; i++) {
            this.internalState[i] = mixColumnInv(this.internalState[i]);
        }
    }

    private static long mulX(long j) {
        return (((j & -9187201950435737472L) >>> 7) * 29) ^ ((9187201950435737471L & j) << 1);
    }

    private static long mulX2(long j) {
        return (((j & 4629771061636907072L) >>> 6) * 29) ^ (((4557430888798830399L & j) << 2) ^ (((-9187201950435737472L & j) >>> 6) * 29));
    }

    private static long rotate(int i, long j) {
        return (j << (-i)) | (j >>> i);
    }

    private void rotateLeft(long[] jArr, long[] jArr2) {
        int i = this.wordsInBlock;
        if (i == 2) {
            long j = jArr[0];
            long j2 = jArr[1];
            jArr2[0] = (j >>> 56) | (j2 << 8);
            jArr2[1] = (j2 >>> 56) | (j << 8);
        } else if (i == 4) {
            long j3 = jArr[0];
            long j4 = jArr[1];
            long j5 = jArr[2];
            long j6 = jArr[3];
            jArr2[0] = (j4 >>> 24) | (j5 << 40);
            jArr2[1] = (j5 >>> 24) | (j6 << 40);
            jArr2[2] = (j6 >>> 24) | (j3 << 40);
            jArr2[3] = (j3 >>> 24) | (j4 << 40);
        } else if (i == 8) {
            long j7 = jArr[0];
            long j8 = jArr[1];
            long j9 = jArr[2];
            long j10 = jArr[3];
            long j11 = jArr[4];
            long j12 = jArr[5];
            long j13 = jArr[6];
            long j14 = jArr[7];
            jArr2[0] = (j9 >>> 24) | (j10 << 40);
            jArr2[1] = (j10 >>> 24) | (j11 << 40);
            jArr2[2] = (j11 >>> 24) | (j12 << 40);
            jArr2[3] = (j12 >>> 24) | (j13 << 40);
            jArr2[4] = (j13 >>> 24) | (j14 << 40);
            jArr2[5] = (j14 >>> 24) | (j7 << 40);
            jArr2[6] = (j7 >>> 24) | (j8 << 40);
            jArr2[7] = (j8 >>> 24) | (j9 << 40);
        } else {
            throw new IllegalStateException("unsupported block length: only 128/256/512 are allowed");
        }
    }

    private void shiftRows() {
        int i = this.wordsInBlock;
        if (i == 2) {
            long j = this.internalState[0];
            long j2 = this.internalState[1];
            long j3 = (j ^ j2) & -4294967296L;
            this.internalState[0] = j ^ j3;
            this.internalState[1] = j3 ^ j2;
        } else if (i == 4) {
            long j4 = this.internalState[0];
            long j5 = this.internalState[1];
            long j6 = this.internalState[2];
            long j7 = this.internalState[3];
            long j8 = (j4 ^ j6) & -4294967296L;
            long j9 = j4 ^ j8;
            long j10 = j6 ^ j8;
            long j11 = (j5 ^ j7) & 281474976645120L;
            long j12 = j5 ^ j11;
            long j13 = j7 ^ j11;
            long j14 = (j9 ^ j12) & -281470681808896L;
            long j15 = (j10 ^ j13) & -281470681808896L;
            this.internalState[0] = j9 ^ j14;
            this.internalState[1] = j12 ^ j14;
            this.internalState[2] = j10 ^ j15;
            this.internalState[3] = j13 ^ j15;
        } else if (i == 8) {
            long j16 = this.internalState[0];
            long j17 = this.internalState[1];
            long j18 = this.internalState[2];
            long j19 = this.internalState[3];
            long j20 = this.internalState[4];
            long j21 = this.internalState[5];
            long j22 = this.internalState[6];
            long j23 = this.internalState[7];
            long j24 = (j16 ^ j20) & -4294967296L;
            long j25 = j16 ^ j24;
            long j26 = j20 ^ j24;
            long j27 = (j17 ^ j21) & 72057594021150720L;
            long j28 = j17 ^ j27;
            long j29 = j21 ^ j27;
            long j30 = (j18 ^ j22) & 281474976645120L;
            long j31 = j18 ^ j30;
            long j32 = j22 ^ j30;
            long j33 = (j19 ^ j23) & 1099511627520L;
            long j34 = j19 ^ j33;
            long j35 = j23 ^ j33;
            long j36 = (j25 ^ j31) & -281470681808896L;
            long j37 = j25 ^ j36;
            long j38 = j31 ^ j36;
            long j39 = (j28 ^ j34) & 72056494543077120L;
            long j40 = j28 ^ j39;
            long j41 = j34 ^ j39;
            long j42 = (j26 ^ j32) & -281470681808896L;
            long j43 = j26 ^ j42;
            long j44 = j32 ^ j42;
            long j45 = (j29 ^ j35) & 72056494543077120L;
            long j46 = j29 ^ j45;
            long j47 = j35 ^ j45;
            long j48 = (j37 ^ j40) & -71777214294589696L;
            long j49 = j37 ^ j48;
            long j50 = j40 ^ j48;
            long j51 = (j38 ^ j41) & -71777214294589696L;
            long j52 = j38 ^ j51;
            long j53 = j41 ^ j51;
            long j54 = (j43 ^ j46) & -71777214294589696L;
            long j55 = j43 ^ j54;
            long j56 = j46 ^ j54;
            long j57 = (j44 ^ j47) & -71777214294589696L;
            this.internalState[0] = j49;
            this.internalState[1] = j50;
            this.internalState[2] = j52;
            this.internalState[3] = j53;
            this.internalState[4] = j55;
            this.internalState[5] = j56;
            this.internalState[6] = j44 ^ j57;
            this.internalState[7] = j47 ^ j57;
        } else {
            throw new IllegalStateException("unsupported block length: only 128/256/512 are allowed");
        }
    }

    private void subBytes() {
        for (int i = 0; i < this.wordsInBlock; i++) {
            long j = this.internalState[i];
            int i2 = (int) j;
            int i3 = (int) (j >>> 32);
            byte b = (S0[i2 & 255] & 255) | ((S1[(i2 >>> 8) & 255] & 255) << 8) | ((S2[(i2 >>> 16) & 255] & 255) << Tnaf.POW_2_WIDTH) | (S3[i2 >>> 24] << 24);
            byte b2 = S0[i3 & 255];
            byte b3 = S1[(i3 >>> 8) & 255];
            byte b4 = S2[(i3 >>> 16) & 255];
            this.internalState[i] = (((long) ((S3[i3 >>> 24] << 24) | (((b2 & 255) | ((b3 & 255) << 8)) | ((b4 & 255) << Tnaf.POW_2_WIDTH)))) << 32) | (((long) b) & BodyPartID.bodyIdMax);
        }
    }

    private void subRoundKey(int i) {
        long[] jArr = this.roundKeys[i];
        for (int i2 = 0; i2 < this.wordsInBlock; i2++) {
            long[] jArr2 = this.internalState;
            jArr2[i2] = jArr2[i2] - jArr[i2];
        }
    }

    private void workingKeyExpandEven(long[] jArr, long[] jArr2) {
        long[] jArr3 = new long[this.wordsInKey];
        long[] jArr4 = new long[this.wordsInBlock];
        System.arraycopy(jArr, 0, jArr3, 0, this.wordsInKey);
        long j = 281479271743489L;
        int i = 0;
        while (true) {
            for (int i2 = 0; i2 < this.wordsInBlock; i2++) {
                jArr4[i2] = jArr2[i2] + j;
            }
            for (int i3 = 0; i3 < this.wordsInBlock; i3++) {
                this.internalState[i3] = jArr3[i3] + jArr4[i3];
            }
            subBytes();
            shiftRows();
            mixColumns();
            for (int i4 = 0; i4 < this.wordsInBlock; i4++) {
                long[] jArr5 = this.internalState;
                jArr5[i4] = jArr5[i4] ^ jArr4[i4];
            }
            subBytes();
            shiftRows();
            mixColumns();
            for (int i5 = 0; i5 < this.wordsInBlock; i5++) {
                long[] jArr6 = this.internalState;
                jArr6[i5] = jArr6[i5] + jArr4[i5];
            }
            System.arraycopy(this.internalState, 0, this.roundKeys[i], 0, this.wordsInBlock);
            if (this.roundsAmount != i) {
                if (this.wordsInBlock != this.wordsInKey) {
                    i += 2;
                    j <<= 1;
                    for (int i6 = 0; i6 < this.wordsInBlock; i6++) {
                        jArr4[i6] = jArr2[i6] + j;
                    }
                    for (int i7 = 0; i7 < this.wordsInBlock; i7++) {
                        this.internalState[i7] = jArr3[this.wordsInBlock + i7] + jArr4[i7];
                    }
                    subBytes();
                    shiftRows();
                    mixColumns();
                    for (int i8 = 0; i8 < this.wordsInBlock; i8++) {
                        long[] jArr7 = this.internalState;
                        jArr7[i8] = jArr7[i8] ^ jArr4[i8];
                    }
                    subBytes();
                    shiftRows();
                    mixColumns();
                    for (int i9 = 0; i9 < this.wordsInBlock; i9++) {
                        long[] jArr8 = this.internalState;
                        jArr8[i9] = jArr8[i9] + jArr4[i9];
                    }
                    System.arraycopy(this.internalState, 0, this.roundKeys[i], 0, this.wordsInBlock);
                    if (this.roundsAmount == i) {
                        return;
                    }
                }
                i += 2;
                j <<= 1;
                long j2 = jArr3[0];
                for (int i10 = 1; i10 < jArr3.length; i10++) {
                    jArr3[i10 - 1] = jArr3[i10];
                }
                jArr3[jArr3.length - 1] = j2;
            } else {
                return;
            }
        }
    }

    private void workingKeyExpandKT(long[] jArr, long[] jArr2) {
        long[] jArr3 = new long[this.wordsInBlock];
        long[] jArr4 = new long[this.wordsInBlock];
        this.internalState = new long[this.wordsInBlock];
        long[] jArr5 = this.internalState;
        jArr5[0] = jArr5[0] + ((long) (this.wordsInBlock + this.wordsInKey + 1));
        if (this.wordsInBlock == this.wordsInKey) {
            System.arraycopy(jArr, 0, jArr3, 0, jArr3.length);
            System.arraycopy(jArr, 0, jArr4, 0, jArr4.length);
        } else {
            System.arraycopy(jArr, 0, jArr3, 0, this.wordsInBlock);
            System.arraycopy(jArr, this.wordsInBlock, jArr4, 0, this.wordsInBlock);
        }
        for (int i = 0; i < this.internalState.length; i++) {
            long[] jArr6 = this.internalState;
            jArr6[i] = jArr6[i] + jArr3[i];
        }
        subBytes();
        shiftRows();
        mixColumns();
        for (int i2 = 0; i2 < this.internalState.length; i2++) {
            long[] jArr7 = this.internalState;
            jArr7[i2] = jArr7[i2] ^ jArr4[i2];
        }
        subBytes();
        shiftRows();
        mixColumns();
        for (int i3 = 0; i3 < this.internalState.length; i3++) {
            long[] jArr8 = this.internalState;
            jArr8[i3] = jArr8[i3] + jArr3[i3];
        }
        subBytes();
        shiftRows();
        mixColumns();
        System.arraycopy(this.internalState, 0, jArr2, 0, this.wordsInBlock);
    }

    private void workingKeyExpandOdd() {
        for (int i = 1; i < this.roundsAmount; i += 2) {
            rotateLeft(this.roundKeys[i - 1], this.roundKeys[i]);
        }
    }

    private void xorRoundKey(int i) {
        long[] jArr = this.roundKeys[i];
        for (int i2 = 0; i2 < this.wordsInBlock; i2++) {
            long[] jArr2 = this.internalState;
            jArr2[i2] = jArr2[i2] ^ jArr[i2];
        }
    }

    public String getAlgorithmName() {
        return "DSTU7624";
    }

    public int getBlockSize() {
        return this.wordsInBlock << 3;
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x005b A[LOOP:0: B:21:0x0056->B:23:0x005b, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0071  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0088  */
    public void init(boolean z, CipherParameters cipherParameters) throws IllegalArgumentException {
        int i;
        int i2;
        if (cipherParameters instanceof KeyParameter) {
            this.forEncryption = z;
            byte[] key = ((KeyParameter) cipherParameters).getKey();
            int length = key.length << 3;
            int i3 = this.wordsInBlock << 6;
            if (length != 128 && length != 256 && length != 512) {
                throw new IllegalArgumentException("unsupported key length: only 128/256/512 are allowed");
            } else if (length == i3 || length == 2 * i3) {
                if (length == 128) {
                    i2 = 10;
                } else if (length != 256) {
                    if (length == 512) {
                        i2 = 18;
                    }
                    this.wordsInKey = length >>> 6;
                    this.roundKeys = new long[(this.roundsAmount + 1)][];
                    for (i = 0; i < this.roundKeys.length; i++) {
                        this.roundKeys[i] = new long[this.wordsInBlock];
                    }
                    this.workingKey = new long[this.wordsInKey];
                    if (key.length != (length >>> 3)) {
                        Pack.littleEndianToLong(key, 0, this.workingKey);
                        long[] jArr = new long[this.wordsInBlock];
                        workingKeyExpandKT(this.workingKey, jArr);
                        workingKeyExpandEven(this.workingKey, jArr);
                        workingKeyExpandOdd();
                        return;
                    }
                    throw new IllegalArgumentException("Invalid key parameter passed to DSTU7624Engine init");
                } else {
                    i2 = 14;
                }
                this.roundsAmount = i2;
                this.wordsInKey = length >>> 6;
                this.roundKeys = new long[(this.roundsAmount + 1)][];
                while (i < this.roundKeys.length) {
                }
                this.workingKey = new long[this.wordsInKey];
                if (key.length != (length >>> 3)) {
                }
            } else {
                throw new IllegalArgumentException("Unsupported key length");
            }
        } else {
            throw new IllegalArgumentException("Invalid parameter passed to DSTU7624Engine init");
        }
    }

    public int processBlock(byte[] bArr, int i, byte[] bArr2, int i2) throws DataLengthException, IllegalStateException {
        if (this.workingKey == null) {
            throw new IllegalStateException("DSTU7624Engine not initialised");
        } else if (getBlockSize() + i > bArr.length) {
            throw new DataLengthException("Input buffer too short");
        } else if (getBlockSize() + i2 <= bArr2.length) {
            int i3 = 0;
            if (this.forEncryption) {
                if (this.wordsInBlock != 2) {
                    Pack.littleEndianToLong(bArr, i, this.internalState);
                    addRoundKey(0);
                    while (true) {
                        subBytes();
                        shiftRows();
                        mixColumns();
                        i3++;
                        if (i3 == this.roundsAmount) {
                            break;
                        }
                        xorRoundKey(i3);
                    }
                    addRoundKey(this.roundsAmount);
                } else {
                    encryptBlock_128(bArr, i, bArr2, i2);
                    return getBlockSize();
                }
            } else if (this.wordsInBlock != 2) {
                Pack.littleEndianToLong(bArr, i, this.internalState);
                subRoundKey(this.roundsAmount);
                int i4 = this.roundsAmount;
                while (true) {
                    mixColumnsInv();
                    invShiftRows();
                    invSubBytes();
                    i4--;
                    if (i4 == 0) {
                        break;
                    }
                    xorRoundKey(i4);
                }
                subRoundKey(0);
            } else {
                decryptBlock_128(bArr, i, bArr2, i2);
                return getBlockSize();
            }
            Pack.longToLittleEndian(this.internalState, bArr2, i2);
            return getBlockSize();
        } else {
            throw new OutputLengthException("Output buffer too short");
        }
    }

    public void reset() {
        Arrays.fill(this.internalState, 0);
    }
}
