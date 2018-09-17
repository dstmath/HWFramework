package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.params.KeyParameter;

public final class TwofishEngine implements BlockCipher {
    private static final int BLOCK_SIZE = 16;
    private static final int GF256_FDBK = 361;
    private static final int GF256_FDBK_2 = 180;
    private static final int GF256_FDBK_4 = 90;
    private static final int INPUT_WHITEN = 0;
    private static final int MAX_KEY_BITS = 256;
    private static final int MAX_ROUNDS = 16;
    private static final int OUTPUT_WHITEN = 4;
    private static final byte[][] P = new byte[][]{new byte[]{(byte) -87, (byte) 103, (byte) -77, (byte) -24, (byte) 4, (byte) -3, (byte) -93, (byte) 118, (byte) -102, (byte) -110, Byte.MIN_VALUE, (byte) 120, (byte) -28, (byte) -35, (byte) -47, (byte) 56, (byte) 13, (byte) -58, (byte) 53, (byte) -104, (byte) 24, (byte) -9, (byte) -20, (byte) 108, (byte) 67, (byte) 117, (byte) 55, (byte) 38, (byte) -6, (byte) 19, (byte) -108, (byte) 72, (byte) -14, (byte) -48, (byte) -117, (byte) 48, (byte) -124, (byte) 84, (byte) -33, (byte) 35, (byte) 25, (byte) 91, (byte) 61, (byte) 89, (byte) -13, (byte) -82, (byte) -94, (byte) -126, (byte) 99, (byte) 1, (byte) -125, (byte) 46, (byte) -39, (byte) 81, (byte) -101, (byte) 124, (byte) -90, (byte) -21, (byte) -91, (byte) -66, (byte) 22, (byte) 12, (byte) -29, (byte) 97, (byte) -64, (byte) -116, (byte) 58, (byte) -11, (byte) 115, (byte) 44, (byte) 37, (byte) 11, (byte) -69, (byte) 78, (byte) -119, (byte) 107, (byte) 83, (byte) 106, (byte) -76, (byte) -15, (byte) -31, (byte) -26, (byte) -67, (byte) 69, (byte) -30, (byte) -12, (byte) -74, (byte) 102, (byte) -52, (byte) -107, (byte) 3, (byte) 86, (byte) -44, (byte) 28, (byte) 30, (byte) -41, (byte) -5, (byte) -61, (byte) -114, (byte) -75, (byte) -23, (byte) -49, (byte) -65, (byte) -70, (byte) -22, (byte) 119, (byte) 57, (byte) -81, (byte) 51, (byte) -55, (byte) 98, (byte) 113, (byte) -127, (byte) 121, (byte) 9, (byte) -83, (byte) 36, (byte) -51, (byte) -7, (byte) -40, (byte) -27, (byte) -59, (byte) -71, (byte) 77, (byte) 68, (byte) 8, (byte) -122, (byte) -25, (byte) -95, (byte) 29, (byte) -86, (byte) -19, (byte) 6, (byte) 112, (byte) -78, (byte) -46, (byte) 65, (byte) 123, (byte) -96, (byte) 17, (byte) 49, (byte) -62, (byte) 39, (byte) -112, (byte) 32, (byte) -10, (byte) 96, (byte) -1, (byte) -106, (byte) 92, (byte) -79, (byte) -85, (byte) -98, (byte) -100, (byte) 82, (byte) 27, (byte) 95, (byte) -109, (byte) 10, (byte) -17, (byte) -111, (byte) -123, (byte) 73, (byte) -18, (byte) 45, (byte) 79, (byte) -113, (byte) 59, (byte) 71, (byte) -121, (byte) 109, (byte) 70, (byte) -42, (byte) 62, (byte) 105, (byte) 100, (byte) 42, (byte) -50, (byte) -53, (byte) 47, (byte) -4, (byte) -105, (byte) 5, (byte) 122, (byte) -84, Byte.MAX_VALUE, (byte) -43, (byte) 26, (byte) 75, (byte) 14, (byte) -89, (byte) 90, (byte) 40, (byte) 20, (byte) 63, (byte) 41, (byte) -120, (byte) 60, (byte) 76, (byte) 2, (byte) -72, (byte) -38, (byte) -80, (byte) 23, (byte) 85, (byte) 31, (byte) -118, (byte) 125, (byte) 87, (byte) -57, (byte) -115, (byte) 116, (byte) -73, (byte) -60, (byte) -97, (byte) 114, (byte) 126, (byte) 21, (byte) 34, (byte) 18, (byte) 88, (byte) 7, (byte) -103, (byte) 52, (byte) 110, (byte) 80, (byte) -34, (byte) 104, (byte) 101, (byte) -68, (byte) -37, (byte) -8, (byte) -56, (byte) -88, (byte) 43, (byte) 64, (byte) -36, (byte) -2, (byte) 50, (byte) -92, (byte) -54, Tnaf.POW_2_WIDTH, (byte) 33, (byte) -16, (byte) -45, (byte) 93, (byte) 15, (byte) 0, (byte) 111, (byte) -99, (byte) 54, (byte) 66, (byte) 74, (byte) 94, (byte) -63, (byte) -32}, new byte[]{(byte) 117, (byte) -13, (byte) -58, (byte) -12, (byte) -37, (byte) 123, (byte) -5, (byte) -56, (byte) 74, (byte) -45, (byte) -26, (byte) 107, (byte) 69, (byte) 125, (byte) -24, (byte) 75, (byte) -42, (byte) 50, (byte) -40, (byte) -3, (byte) 55, (byte) 113, (byte) -15, (byte) -31, (byte) 48, (byte) 15, (byte) -8, (byte) 27, (byte) -121, (byte) -6, (byte) 6, (byte) 63, (byte) 94, (byte) -70, (byte) -82, (byte) 91, (byte) -118, (byte) 0, (byte) -68, (byte) -99, (byte) 109, (byte) -63, (byte) -79, (byte) 14, Byte.MIN_VALUE, (byte) 93, (byte) -46, (byte) -43, (byte) -96, (byte) -124, (byte) 7, (byte) 20, (byte) -75, (byte) -112, (byte) 44, (byte) -93, (byte) -78, (byte) 115, (byte) 76, (byte) 84, (byte) -110, (byte) 116, (byte) 54, (byte) 81, (byte) 56, (byte) -80, (byte) -67, (byte) 90, (byte) -4, (byte) 96, (byte) 98, (byte) -106, (byte) 108, (byte) 66, (byte) -9, Tnaf.POW_2_WIDTH, (byte) 124, (byte) 40, (byte) 39, (byte) -116, (byte) 19, (byte) -107, (byte) -100, (byte) -57, (byte) 36, (byte) 70, (byte) 59, (byte) 112, (byte) -54, (byte) -29, (byte) -123, (byte) -53, (byte) 17, (byte) -48, (byte) -109, (byte) -72, (byte) -90, (byte) -125, (byte) 32, (byte) -1, (byte) -97, (byte) 119, (byte) -61, (byte) -52, (byte) 3, (byte) 111, (byte) 8, (byte) -65, (byte) 64, (byte) -25, (byte) 43, (byte) -30, (byte) 121, (byte) 12, (byte) -86, (byte) -126, (byte) 65, (byte) 58, (byte) -22, (byte) -71, (byte) -28, (byte) -102, (byte) -92, (byte) -105, (byte) 126, (byte) -38, (byte) 122, (byte) 23, (byte) 102, (byte) -108, (byte) -95, (byte) 29, (byte) 61, (byte) -16, (byte) -34, (byte) -77, (byte) 11, (byte) 114, (byte) -89, (byte) 28, (byte) -17, (byte) -47, (byte) 83, (byte) 62, (byte) -113, (byte) 51, (byte) 38, (byte) 95, (byte) -20, (byte) 118, (byte) 42, (byte) 73, (byte) -127, (byte) -120, (byte) -18, (byte) 33, (byte) -60, (byte) 26, (byte) -21, (byte) -39, (byte) -59, (byte) 57, (byte) -103, (byte) -51, (byte) -83, (byte) 49, (byte) -117, (byte) 1, (byte) 24, (byte) 35, (byte) -35, (byte) 31, (byte) 78, (byte) 45, (byte) -7, (byte) 72, (byte) 79, (byte) -14, (byte) 101, (byte) -114, (byte) 120, (byte) 92, (byte) 88, (byte) 25, (byte) -115, (byte) -27, (byte) -104, (byte) 87, (byte) 103, Byte.MAX_VALUE, (byte) 5, (byte) 100, (byte) -81, (byte) 99, (byte) -74, (byte) -2, (byte) -11, (byte) -73, (byte) 60, (byte) -91, (byte) -50, (byte) -23, (byte) 104, (byte) 68, (byte) -32, (byte) 77, (byte) 67, (byte) 105, (byte) 41, (byte) 46, (byte) -84, (byte) 21, (byte) 89, (byte) -88, (byte) 10, (byte) -98, (byte) 110, (byte) 71, (byte) -33, (byte) 52, (byte) 53, (byte) 106, (byte) -49, (byte) -36, (byte) 34, (byte) -55, (byte) -64, (byte) -101, (byte) -119, (byte) -44, (byte) -19, (byte) -85, (byte) 18, (byte) -94, (byte) 13, (byte) 82, (byte) -69, (byte) 2, (byte) 47, (byte) -87, (byte) -41, (byte) 97, (byte) 30, (byte) -76, (byte) 80, (byte) 4, (byte) -10, (byte) -62, (byte) 22, (byte) 37, (byte) -122, (byte) 86, (byte) 85, (byte) 9, (byte) -66, (byte) -111}};
    private static final int P_00 = 1;
    private static final int P_01 = 0;
    private static final int P_02 = 0;
    private static final int P_03 = 1;
    private static final int P_04 = 1;
    private static final int P_10 = 0;
    private static final int P_11 = 0;
    private static final int P_12 = 1;
    private static final int P_13 = 1;
    private static final int P_14 = 0;
    private static final int P_20 = 1;
    private static final int P_21 = 1;
    private static final int P_22 = 0;
    private static final int P_23 = 0;
    private static final int P_24 = 0;
    private static final int P_30 = 0;
    private static final int P_31 = 1;
    private static final int P_32 = 1;
    private static final int P_33 = 0;
    private static final int P_34 = 1;
    private static final int ROUNDS = 16;
    private static final int ROUND_SUBKEYS = 8;
    private static final int RS_GF_FDBK = 333;
    private static final int SK_BUMP = 16843009;
    private static final int SK_ROTL = 9;
    private static final int SK_STEP = 33686018;
    private static final int TOTAL_SUBKEYS = 40;
    private boolean encrypting = false;
    private int[] gMDS0 = new int[MAX_KEY_BITS];
    private int[] gMDS1 = new int[MAX_KEY_BITS];
    private int[] gMDS2 = new int[MAX_KEY_BITS];
    private int[] gMDS3 = new int[MAX_KEY_BITS];
    private int[] gSBox;
    private int[] gSubKeys;
    private int k64Cnt = 0;
    private byte[] workingKey = null;

    public TwofishEngine() {
        int[] m1 = new int[2];
        int[] mX = new int[2];
        int[] mY = new int[2];
        for (int i = 0; i < MAX_KEY_BITS; i++) {
            int j = P[0][i] & 255;
            m1[0] = j;
            mX[0] = Mx_X(j) & 255;
            mY[0] = Mx_Y(j) & 255;
            j = P[1][i] & 255;
            m1[1] = j;
            mX[1] = Mx_X(j) & 255;
            mY[1] = Mx_Y(j) & 255;
            this.gMDS0[i] = ((m1[1] | (mX[1] << 8)) | (mY[1] << 16)) | (mY[1] << 24);
            this.gMDS1[i] = ((mY[0] | (mY[0] << 8)) | (mX[0] << 16)) | (m1[0] << 24);
            this.gMDS2[i] = ((mX[1] | (mY[1] << 8)) | (m1[1] << 16)) | (mY[1] << 24);
            this.gMDS3[i] = ((mX[0] | (m1[0] << 8)) | (mY[0] << 16)) | (mX[0] << 24);
        }
    }

    public void init(boolean encrypting, CipherParameters params) {
        if (params instanceof KeyParameter) {
            this.encrypting = encrypting;
            this.workingKey = ((KeyParameter) params).getKey();
            this.k64Cnt = this.workingKey.length / 8;
            setKey(this.workingKey);
            return;
        }
        throw new IllegalArgumentException("invalid parameter passed to Twofish init - " + params.getClass().getName());
    }

    public String getAlgorithmName() {
        return "Twofish";
    }

    public int processBlock(byte[] in, int inOff, byte[] out, int outOff) {
        if (this.workingKey == null) {
            throw new IllegalStateException("Twofish not initialised");
        } else if (inOff + 16 > in.length) {
            throw new DataLengthException("input buffer too short");
        } else if (outOff + 16 > out.length) {
            throw new OutputLengthException("output buffer too short");
        } else {
            if (this.encrypting) {
                encryptBlock(in, inOff, out, outOff);
            } else {
                decryptBlock(in, inOff, out, outOff);
            }
            return 16;
        }
    }

    public void reset() {
        if (this.workingKey != null) {
            setKey(this.workingKey);
        }
    }

    public int getBlockSize() {
        return 16;
    }

    private void setKey(byte[] key) {
        int[] k32e = new int[4];
        int[] k32o = new int[4];
        int[] sBoxKeys = new int[4];
        this.gSubKeys = new int[TOTAL_SUBKEYS];
        if (this.k64Cnt < 1) {
            throw new IllegalArgumentException("Key size less than 64 bits");
        } else if (this.k64Cnt > 4) {
            throw new IllegalArgumentException("Key size larger than 256 bits");
        } else {
            int i;
            for (i = 0; i < this.k64Cnt; i++) {
                int p = i * 8;
                k32e[i] = BytesTo32Bits(key, p);
                k32o[i] = BytesTo32Bits(key, p + 4);
                sBoxKeys[(this.k64Cnt - 1) - i] = RS_MDS_Encode(k32e[i], k32o[i]);
            }
            for (i = 0; i < 20; i++) {
                int q = i * SK_STEP;
                int A = F32(q, k32e);
                int B = F32(SK_BUMP + q, k32o);
                B = (B << 8) | (B >>> 24);
                A += B;
                this.gSubKeys[i * 2] = A;
                A += B;
                this.gSubKeys[(i * 2) + 1] = (A << 9) | (A >>> 23);
            }
            int k0 = sBoxKeys[0];
            int k1 = sBoxKeys[1];
            int k2 = sBoxKeys[2];
            int k3 = sBoxKeys[3];
            this.gSBox = new int[1024];
            for (i = 0; i < MAX_KEY_BITS; i++) {
                int b3 = i;
                int b2 = i;
                int b1 = i;
                int b0 = i;
                switch (this.k64Cnt & 3) {
                    case 0:
                        b0 = (P[1][b0] & 255) ^ b0(k3);
                        b1 = (P[0][b1] & 255) ^ b1(k3);
                        b2 = (P[0][b2] & 255) ^ b2(k3);
                        b3 = (P[1][b3] & 255) ^ b3(k3);
                        break;
                    case 1:
                        this.gSBox[i * 2] = this.gMDS0[(P[0][b0] & 255) ^ b0(k0)];
                        this.gSBox[(i * 2) + 1] = this.gMDS1[(P[0][b1] & 255) ^ b1(k0)];
                        this.gSBox[(i * 2) + 512] = this.gMDS2[(P[1][b2] & 255) ^ b2(k0)];
                        this.gSBox[(i * 2) + 513] = this.gMDS3[(P[1][b3] & 255) ^ b3(k0)];
                        continue;
                    case 2:
                        break;
                    case 3:
                        break;
                    default:
                        break;
                }
                b0 = (P[1][b0] & 255) ^ b0(k2);
                b1 = (P[1][b1] & 255) ^ b1(k2);
                b2 = (P[0][b2] & 255) ^ b2(k2);
                b3 = (P[0][b3] & 255) ^ b3(k2);
                this.gSBox[i * 2] = this.gMDS0[(P[0][(P[0][b0] & 255) ^ b0(k1)] & 255) ^ b0(k0)];
                this.gSBox[(i * 2) + 1] = this.gMDS1[(P[0][(P[1][b1] & 255) ^ b1(k1)] & 255) ^ b1(k0)];
                this.gSBox[(i * 2) + 512] = this.gMDS2[(P[1][(P[0][b2] & 255) ^ b2(k1)] & 255) ^ b2(k0)];
                this.gSBox[(i * 2) + 513] = this.gMDS3[(P[1][(P[1][b3] & 255) ^ b3(k1)] & 255) ^ b3(k0)];
                continue;
            }
        }
    }

    private void encryptBlock(byte[] src, int srcIndex, byte[] dst, int dstIndex) {
        int x0 = BytesTo32Bits(src, srcIndex) ^ this.gSubKeys[0];
        int x1 = BytesTo32Bits(src, srcIndex + 4) ^ this.gSubKeys[1];
        int x2 = BytesTo32Bits(src, srcIndex + 8) ^ this.gSubKeys[2];
        int x3 = BytesTo32Bits(src, srcIndex + 12) ^ this.gSubKeys[3];
        int k = 8;
        for (int r = 0; r < 16; r += 2) {
            int t0 = Fe32_0(x0);
            int t1 = Fe32_3(x1);
            int k2 = k + 1;
            x2 ^= (t0 + t1) + this.gSubKeys[k];
            x2 = (x2 >>> 1) | (x2 << 31);
            k = k2 + 1;
            x3 = ((x3 << 1) | (x3 >>> 31)) ^ (((t1 * 2) + t0) + this.gSubKeys[k2]);
            t0 = Fe32_0(x2);
            t1 = Fe32_3(x3);
            k2 = k + 1;
            x0 ^= (t0 + t1) + this.gSubKeys[k];
            x0 = (x0 >>> 1) | (x0 << 31);
            k = k2 + 1;
            x1 = ((x1 << 1) | (x1 >>> 31)) ^ (((t1 * 2) + t0) + this.gSubKeys[k2]);
        }
        Bits32ToBytes(this.gSubKeys[4] ^ x2, dst, dstIndex);
        Bits32ToBytes(this.gSubKeys[5] ^ x3, dst, dstIndex + 4);
        Bits32ToBytes(this.gSubKeys[6] ^ x0, dst, dstIndex + 8);
        Bits32ToBytes(this.gSubKeys[7] ^ x1, dst, dstIndex + 12);
    }

    private void decryptBlock(byte[] src, int srcIndex, byte[] dst, int dstIndex) {
        int x2 = BytesTo32Bits(src, srcIndex) ^ this.gSubKeys[4];
        int x3 = BytesTo32Bits(src, srcIndex + 4) ^ this.gSubKeys[5];
        int x0 = BytesTo32Bits(src, srcIndex + 8) ^ this.gSubKeys[6];
        int x1 = BytesTo32Bits(src, srcIndex + 12) ^ this.gSubKeys[7];
        int k = 39;
        for (int r = 0; r < 16; r += 2) {
            int t0 = Fe32_0(x2);
            int t1 = Fe32_3(x3);
            int k2 = k - 1;
            x1 ^= ((t1 * 2) + t0) + this.gSubKeys[k];
            k = k2 - 1;
            x0 = ((x0 << 1) | (x0 >>> 31)) ^ ((t0 + t1) + this.gSubKeys[k2]);
            x1 = (x1 >>> 1) | (x1 << 31);
            t0 = Fe32_0(x0);
            t1 = Fe32_3(x1);
            k2 = k - 1;
            x3 ^= ((t1 * 2) + t0) + this.gSubKeys[k];
            k = k2 - 1;
            x2 = ((x2 << 1) | (x2 >>> 31)) ^ ((t0 + t1) + this.gSubKeys[k2]);
            x3 = (x3 >>> 1) | (x3 << 31);
        }
        Bits32ToBytes(this.gSubKeys[0] ^ x0, dst, dstIndex);
        Bits32ToBytes(this.gSubKeys[1] ^ x1, dst, dstIndex + 4);
        Bits32ToBytes(this.gSubKeys[2] ^ x2, dst, dstIndex + 8);
        Bits32ToBytes(this.gSubKeys[3] ^ x3, dst, dstIndex + 12);
    }

    private int F32(int x, int[] k32) {
        int b0 = b0(x);
        int b1 = b1(x);
        int b2 = b2(x);
        int b3 = b3(x);
        int k0 = k32[0];
        int k1 = k32[1];
        int k2 = k32[2];
        int k3 = k32[3];
        switch (this.k64Cnt & 3) {
            case 0:
                b0 = (P[1][b0] & 255) ^ b0(k3);
                b1 = (P[0][b1] & 255) ^ b1(k3);
                b2 = (P[0][b2] & 255) ^ b2(k3);
                b3 = (P[1][b3] & 255) ^ b3(k3);
                break;
            case 1:
                return ((this.gMDS0[(P[0][b0] & 255) ^ b0(k0)] ^ this.gMDS1[(P[0][b1] & 255) ^ b1(k0)]) ^ this.gMDS2[(P[1][b2] & 255) ^ b2(k0)]) ^ this.gMDS3[(P[1][b3] & 255) ^ b3(k0)];
            case 2:
                break;
            case 3:
                break;
            default:
                return 0;
        }
        b0 = (P[1][b0] & 255) ^ b0(k2);
        b1 = (P[1][b1] & 255) ^ b1(k2);
        b2 = (P[0][b2] & 255) ^ b2(k2);
        b3 = (P[0][b3] & 255) ^ b3(k2);
        return ((this.gMDS0[(P[0][(P[0][b0] & 255) ^ b0(k1)] & 255) ^ b0(k0)] ^ this.gMDS1[(P[0][(P[1][b1] & 255) ^ b1(k1)] & 255) ^ b1(k0)]) ^ this.gMDS2[(P[1][(P[0][b2] & 255) ^ b2(k1)] & 255) ^ b2(k0)]) ^ this.gMDS3[(P[1][(P[1][b3] & 255) ^ b3(k1)] & 255) ^ b3(k0)];
    }

    private int RS_MDS_Encode(int k0, int k1) {
        int i;
        int r = k1;
        for (i = 0; i < 4; i++) {
            r = RS_rem(r);
        }
        r ^= k0;
        for (i = 0; i < 4; i++) {
            r = RS_rem(r);
        }
        return r;
    }

    private int RS_rem(int x) {
        int i;
        int i2 = 0;
        int b = (x >>> 24) & 255;
        int i3 = b << 1;
        if ((b & 128) != 0) {
            i = RS_GF_FDBK;
        } else {
            i = 0;
        }
        int g2 = (i ^ i3) & 255;
        i = b >>> 1;
        if ((b & 1) != 0) {
            i2 = 166;
        }
        int g3 = (i2 ^ i) ^ g2;
        return ((((x << 8) ^ (g3 << 24)) ^ (g2 << 16)) ^ (g3 << 8)) ^ b;
    }

    private int LFSR1(int x) {
        int i = 0;
        int i2 = x >> 1;
        if ((x & 1) != 0) {
            i = GF256_FDBK_2;
        }
        return i ^ i2;
    }

    private int LFSR2(int x) {
        int i;
        int i2 = 0;
        int i3 = x >> 2;
        if ((x & 2) != 0) {
            i = GF256_FDBK_2;
        } else {
            i = 0;
        }
        i ^= i3;
        if ((x & 1) != 0) {
            i2 = GF256_FDBK_4;
        }
        return i2 ^ i;
    }

    private int Mx_X(int x) {
        return LFSR2(x) ^ x;
    }

    private int Mx_Y(int x) {
        return (LFSR1(x) ^ x) ^ LFSR2(x);
    }

    private int b0(int x) {
        return x & 255;
    }

    private int b1(int x) {
        return (x >>> 8) & 255;
    }

    private int b2(int x) {
        return (x >>> 16) & 255;
    }

    private int b3(int x) {
        return (x >>> 24) & 255;
    }

    private int Fe32_0(int x) {
        return ((this.gSBox[((x & 255) * 2) + 0] ^ this.gSBox[(((x >>> 8) & 255) * 2) + 1]) ^ this.gSBox[(((x >>> 16) & 255) * 2) + 512]) ^ this.gSBox[(((x >>> 24) & 255) * 2) + 513];
    }

    private int Fe32_3(int x) {
        return ((this.gSBox[(((x >>> 24) & 255) * 2) + 0] ^ this.gSBox[((x & 255) * 2) + 1]) ^ this.gSBox[(((x >>> 8) & 255) * 2) + 512]) ^ this.gSBox[(((x >>> 16) & 255) * 2) + 513];
    }

    private int BytesTo32Bits(byte[] b, int p) {
        return (((b[p] & 255) | ((b[p + 1] & 255) << 8)) | ((b[p + 2] & 255) << 16)) | ((b[p + 3] & 255) << 24);
    }

    private void Bits32ToBytes(int in, byte[] b, int offset) {
        b[offset] = (byte) in;
        b[offset + 1] = (byte) (in >> 8);
        b[offset + 2] = (byte) (in >> 16);
        b[offset + 3] = (byte) (in >> 24);
    }
}
