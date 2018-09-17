package com.android.org.bouncycastle.crypto.engines;

import com.android.org.bouncycastle.crypto.BlockCipher;
import com.android.org.bouncycastle.crypto.CipherParameters;
import com.android.org.bouncycastle.crypto.DataLengthException;
import com.android.org.bouncycastle.crypto.OutputLengthException;
import com.android.org.bouncycastle.crypto.params.KeyParameter;
import com.android.org.bouncycastle.crypto.params.RC2Parameters;

public class RC2Engine implements BlockCipher {
    private static final int BLOCK_SIZE = 8;
    private static byte[] piTable = new byte[]{(byte) -39, (byte) 120, (byte) -7, (byte) -60, (byte) 25, (byte) -35, (byte) -75, (byte) -19, (byte) 40, (byte) -23, (byte) -3, (byte) 121, (byte) 74, (byte) -96, (byte) -40, (byte) -99, (byte) -58, (byte) 126, (byte) 55, (byte) -125, (byte) 43, (byte) 118, (byte) 83, (byte) -114, (byte) 98, (byte) 76, (byte) 100, (byte) -120, (byte) 68, (byte) -117, (byte) -5, (byte) -94, (byte) 23, (byte) -102, (byte) 89, (byte) -11, (byte) -121, (byte) -77, (byte) 79, (byte) 19, (byte) 97, (byte) 69, (byte) 109, (byte) -115, (byte) 9, (byte) -127, (byte) 125, (byte) 50, (byte) -67, (byte) -113, (byte) 64, (byte) -21, (byte) -122, (byte) -73, (byte) 123, (byte) 11, (byte) -16, (byte) -107, (byte) 33, (byte) 34, (byte) 92, (byte) 107, (byte) 78, (byte) -126, (byte) 84, (byte) -42, (byte) 101, (byte) -109, (byte) -50, (byte) 96, (byte) -78, (byte) 28, (byte) 115, (byte) 86, (byte) -64, (byte) 20, (byte) -89, (byte) -116, (byte) -15, (byte) -36, (byte) 18, (byte) 117, (byte) -54, (byte) 31, (byte) 59, (byte) -66, (byte) -28, (byte) -47, (byte) 66, (byte) 61, (byte) -44, (byte) 48, (byte) -93, (byte) 60, (byte) -74, (byte) 38, (byte) 111, (byte) -65, (byte) 14, (byte) -38, (byte) 70, (byte) 105, (byte) 7, (byte) 87, (byte) 39, (byte) -14, (byte) 29, (byte) -101, (byte) -68, (byte) -108, (byte) 67, (byte) 3, (byte) -8, (byte) 17, (byte) -57, (byte) -10, (byte) -112, (byte) -17, (byte) 62, (byte) -25, (byte) 6, (byte) -61, (byte) -43, (byte) 47, (byte) -56, (byte) 102, (byte) 30, (byte) -41, (byte) 8, (byte) -24, (byte) -22, (byte) -34, Byte.MIN_VALUE, (byte) 82, (byte) -18, (byte) -9, (byte) -124, (byte) -86, (byte) 114, (byte) -84, (byte) 53, (byte) 77, (byte) 106, (byte) 42, (byte) -106, (byte) 26, (byte) -46, (byte) 113, (byte) 90, (byte) 21, (byte) 73, (byte) 116, (byte) 75, (byte) -97, (byte) -48, (byte) 94, (byte) 4, (byte) 24, (byte) -92, (byte) -20, (byte) -62, (byte) -32, (byte) 65, (byte) 110, (byte) 15, (byte) 81, (byte) -53, (byte) -52, (byte) 36, (byte) -111, (byte) -81, (byte) 80, (byte) -95, (byte) -12, (byte) 112, (byte) 57, (byte) -103, (byte) 124, (byte) 58, (byte) -123, (byte) 35, (byte) -72, (byte) -76, (byte) 122, (byte) -4, (byte) 2, (byte) 54, (byte) 91, (byte) 37, (byte) 85, (byte) -105, (byte) 49, (byte) 45, (byte) 93, (byte) -6, (byte) -104, (byte) -29, (byte) -118, (byte) -110, (byte) -82, (byte) 5, (byte) -33, (byte) 41, Tnaf.POW_2_WIDTH, (byte) 103, (byte) 108, (byte) -70, (byte) -55, (byte) -45, (byte) 0, (byte) -26, (byte) -49, (byte) -31, (byte) -98, (byte) -88, (byte) 44, (byte) 99, (byte) 22, (byte) 1, (byte) 63, (byte) 88, (byte) -30, (byte) -119, (byte) -87, (byte) 13, (byte) 56, (byte) 52, (byte) 27, (byte) -85, (byte) 51, (byte) -1, (byte) -80, (byte) -69, (byte) 72, (byte) 12, (byte) 95, (byte) -71, (byte) -79, (byte) -51, (byte) 46, (byte) -59, (byte) -13, (byte) -37, (byte) 71, (byte) -27, (byte) -91, (byte) -100, (byte) 119, (byte) 10, (byte) -90, (byte) 32, (byte) 104, (byte) -2, Byte.MAX_VALUE, (byte) -63, (byte) -83};
    private boolean encrypting;
    private int[] workingKey;

    private int[] generateWorkingKey(byte[] key, int bits) {
        int i;
        int x;
        int[] xKey = new int[128];
        for (i = 0; i != key.length; i++) {
            xKey[i] = key[i] & 255;
        }
        int len = key.length;
        if (len < 128) {
            int index = 0;
            x = xKey[len - 1];
            while (true) {
                int index2 = index + 1;
                x = piTable[(xKey[index] + x) & 255] & 255;
                int len2 = len + 1;
                xKey[len] = x;
                if (len2 >= 128) {
                    break;
                }
                index = index2;
                len = len2;
            }
        }
        len = (bits + 7) >> 3;
        x = piTable[xKey[128 - len] & (255 >> ((-bits) & 7))] & 255;
        xKey[128 - len] = x;
        for (i = (128 - len) - 1; i >= 0; i--) {
            x = piTable[xKey[i + len] ^ x] & 255;
            xKey[i] = x;
        }
        int[] newKey = new int[64];
        for (i = 0; i != newKey.length; i++) {
            newKey[i] = xKey[i * 2] + (xKey[(i * 2) + 1] << 8);
        }
        return newKey;
    }

    public void init(boolean encrypting, CipherParameters params) {
        this.encrypting = encrypting;
        if (params instanceof RC2Parameters) {
            RC2Parameters param = (RC2Parameters) params;
            this.workingKey = generateWorkingKey(param.getKey(), param.getEffectiveKeyBits());
        } else if (params instanceof KeyParameter) {
            byte[] key = ((KeyParameter) params).getKey();
            this.workingKey = generateWorkingKey(key, key.length * 8);
        } else {
            throw new IllegalArgumentException("invalid parameter passed to RC2 init - " + params.getClass().getName());
        }
    }

    public void reset() {
    }

    public String getAlgorithmName() {
        return "RC2";
    }

    public int getBlockSize() {
        return 8;
    }

    public final int processBlock(byte[] in, int inOff, byte[] out, int outOff) {
        if (this.workingKey == null) {
            throw new IllegalStateException("RC2 engine not initialised");
        } else if (inOff + 8 > in.length) {
            throw new DataLengthException("input buffer too short");
        } else if (outOff + 8 > out.length) {
            throw new OutputLengthException("output buffer too short");
        } else {
            if (this.encrypting) {
                encryptBlock(in, inOff, out, outOff);
            } else {
                decryptBlock(in, inOff, out, outOff);
            }
            return 8;
        }
    }

    private int rotateWordLeft(int x, int y) {
        x &= 65535;
        return (x << y) | (x >> (16 - y));
    }

    private void encryptBlock(byte[] in, int inOff, byte[] out, int outOff) {
        int i;
        int x76 = ((in[inOff + 7] & 255) << 8) + (in[inOff + 6] & 255);
        int x54 = ((in[inOff + 5] & 255) << 8) + (in[inOff + 4] & 255);
        int x32 = ((in[inOff + 3] & 255) << 8) + (in[inOff + 2] & 255);
        int x10 = ((in[inOff + 1] & 255) << 8) + (in[inOff + 0] & 255);
        for (i = 0; i <= 16; i += 4) {
            x10 = rotateWordLeft(((((~x76) & x32) + x10) + (x54 & x76)) + this.workingKey[i], 1);
            x32 = rotateWordLeft(((((~x10) & x54) + x32) + (x76 & x10)) + this.workingKey[i + 1], 2);
            x54 = rotateWordLeft(((((~x32) & x76) + x54) + (x10 & x32)) + this.workingKey[i + 2], 3);
            x76 = rotateWordLeft(((((~x54) & x10) + x76) + (x32 & x54)) + this.workingKey[i + 3], 5);
        }
        x10 += this.workingKey[x76 & 63];
        x32 += this.workingKey[x10 & 63];
        x54 += this.workingKey[x32 & 63];
        x76 += this.workingKey[x54 & 63];
        for (i = 20; i <= 40; i += 4) {
            x10 = rotateWordLeft(((((~x76) & x32) + x10) + (x54 & x76)) + this.workingKey[i], 1);
            x32 = rotateWordLeft(((((~x10) & x54) + x32) + (x76 & x10)) + this.workingKey[i + 1], 2);
            x54 = rotateWordLeft(((((~x32) & x76) + x54) + (x10 & x32)) + this.workingKey[i + 2], 3);
            x76 = rotateWordLeft(((((~x54) & x10) + x76) + (x32 & x54)) + this.workingKey[i + 3], 5);
        }
        x10 += this.workingKey[x76 & 63];
        x32 += this.workingKey[x10 & 63];
        x54 += this.workingKey[x32 & 63];
        x76 += this.workingKey[x54 & 63];
        for (i = 44; i < 64; i += 4) {
            x10 = rotateWordLeft(((((~x76) & x32) + x10) + (x54 & x76)) + this.workingKey[i], 1);
            x32 = rotateWordLeft(((((~x10) & x54) + x32) + (x76 & x10)) + this.workingKey[i + 1], 2);
            x54 = rotateWordLeft(((((~x32) & x76) + x54) + (x10 & x32)) + this.workingKey[i + 2], 3);
            x76 = rotateWordLeft(((((~x54) & x10) + x76) + (x32 & x54)) + this.workingKey[i + 3], 5);
        }
        out[outOff + 0] = (byte) x10;
        out[outOff + 1] = (byte) (x10 >> 8);
        out[outOff + 2] = (byte) x32;
        out[outOff + 3] = (byte) (x32 >> 8);
        out[outOff + 4] = (byte) x54;
        out[outOff + 5] = (byte) (x54 >> 8);
        out[outOff + 6] = (byte) x76;
        out[outOff + 7] = (byte) (x76 >> 8);
    }

    private void decryptBlock(byte[] in, int inOff, byte[] out, int outOff) {
        int i;
        int x76 = ((in[inOff + 7] & 255) << 8) + (in[inOff + 6] & 255);
        int x54 = ((in[inOff + 5] & 255) << 8) + (in[inOff + 4] & 255);
        int x32 = ((in[inOff + 3] & 255) << 8) + (in[inOff + 2] & 255);
        int x10 = ((in[inOff + 1] & 255) << 8) + (in[inOff + 0] & 255);
        for (i = 60; i >= 44; i -= 4) {
            x76 = rotateWordLeft(x76, 11) - ((((~x54) & x10) + (x32 & x54)) + this.workingKey[i + 3]);
            x54 = rotateWordLeft(x54, 13) - ((((~x32) & x76) + (x10 & x32)) + this.workingKey[i + 2]);
            x32 = rotateWordLeft(x32, 14) - ((((~x10) & x54) + (x76 & x10)) + this.workingKey[i + 1]);
            x10 = rotateWordLeft(x10, 15) - ((((~x76) & x32) + (x54 & x76)) + this.workingKey[i]);
        }
        x76 -= this.workingKey[x54 & 63];
        x54 -= this.workingKey[x32 & 63];
        x32 -= this.workingKey[x10 & 63];
        x10 -= this.workingKey[x76 & 63];
        for (i = 40; i >= 20; i -= 4) {
            x76 = rotateWordLeft(x76, 11) - ((((~x54) & x10) + (x32 & x54)) + this.workingKey[i + 3]);
            x54 = rotateWordLeft(x54, 13) - ((((~x32) & x76) + (x10 & x32)) + this.workingKey[i + 2]);
            x32 = rotateWordLeft(x32, 14) - ((((~x10) & x54) + (x76 & x10)) + this.workingKey[i + 1]);
            x10 = rotateWordLeft(x10, 15) - ((((~x76) & x32) + (x54 & x76)) + this.workingKey[i]);
        }
        x76 -= this.workingKey[x54 & 63];
        x54 -= this.workingKey[x32 & 63];
        x32 -= this.workingKey[x10 & 63];
        x10 -= this.workingKey[x76 & 63];
        for (i = 16; i >= 0; i -= 4) {
            x76 = rotateWordLeft(x76, 11) - ((((~x54) & x10) + (x32 & x54)) + this.workingKey[i + 3]);
            x54 = rotateWordLeft(x54, 13) - ((((~x32) & x76) + (x10 & x32)) + this.workingKey[i + 2]);
            x32 = rotateWordLeft(x32, 14) - ((((~x10) & x54) + (x76 & x10)) + this.workingKey[i + 1]);
            x10 = rotateWordLeft(x10, 15) - ((((~x76) & x32) + (x54 & x76)) + this.workingKey[i]);
        }
        out[outOff + 0] = (byte) x10;
        out[outOff + 1] = (byte) (x10 >> 8);
        out[outOff + 2] = (byte) x32;
        out[outOff + 3] = (byte) (x32 >> 8);
        out[outOff + 4] = (byte) x54;
        out[outOff + 5] = (byte) (x54 >> 8);
        out[outOff + 6] = (byte) x76;
        out[outOff + 7] = (byte) (x76 >> 8);
    }
}
