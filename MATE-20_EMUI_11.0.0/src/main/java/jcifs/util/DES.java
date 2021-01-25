package jcifs.util;

import jcifs.dcerpc.msrpc.samr;
import jcifs.netbios.NbtException;
import jcifs.ntlmssp.NtlmFlags;

public class DES {
    private static int[] SP1 = {16843776, 0, 65536, 16843780, 16842756, 66564, 4, 65536, samr.ACB_AUTOLOCK, 16843776, 16843780, samr.ACB_AUTOLOCK, 16778244, 16842756, 16777216, 4, 1028, 16778240, 16778240, 66560, 66560, 16842752, 16842752, 16778244, 65540, 16777220, 16777220, 65540, 0, 1028, 66564, 16777216, 65536, 16843780, 4, 16842752, 16843776, 16777216, 16777216, samr.ACB_AUTOLOCK, 16842756, 65536, 66560, 16777220, samr.ACB_AUTOLOCK, 4, 16778244, 66564, 16843780, 65540, 16842752, 16778244, 16777220, 1028, 66564, 16843776, 1028, 16778240, 16778240, 0, 65540, 66560, 0, 16842756};
    private static int[] SP2 = {-2146402272, -2147450880, 32768, 1081376, 1048576, 32, -2146435040, -2147450848, -2147483616, -2146402272, -2146402304, Integer.MIN_VALUE, -2147450880, 1048576, 32, -2146435040, 1081344, 1048608, -2147450848, 0, Integer.MIN_VALUE, 32768, 1081376, -2146435072, 1048608, -2147483616, 0, 1081344, 32800, -2146402304, -2146435072, 32800, 0, 1081376, -2146435040, 1048576, -2147450848, -2146435072, -2146402304, 32768, -2146435072, -2147450880, 32, -2146402272, 1081376, 32, 32768, Integer.MIN_VALUE, 32800, -2146402304, 1048576, -2147483616, 1048608, -2147450848, -2147483616, 1048608, 1081344, 0, -2147450880, 32800, Integer.MIN_VALUE, -2146435040, -2146402272, 1081344};
    private static int[] SP3 = {520, 134349312, 0, 134348808, 134218240, 0, 131592, 134218240, 131080, 134217736, 134217736, 131072, 134349320, 131080, 134348800, 520, 134217728, 8, 134349312, 512, 131584, 134348800, 134348808, 131592, 134218248, 131584, 131072, 134218248, 8, 134349320, 512, 134217728, 134349312, 134217728, 131080, 520, 131072, 134349312, 134218240, 0, 512, 131080, 134349320, 134218240, 134217736, 512, 0, 134348808, 134218248, 131072, 134217728, 134349320, 8, 131592, 131584, 134217736, 134348800, 134218248, 520, 134348800, 131592, 8, 134348808, 131584};
    private static int[] SP4 = {8396801, 8321, 8321, 128, 8396928, 8388737, 8388609, 8193, 0, 8396800, 8396800, 8396929, NbtException.NOT_LISTENING_CALLING, 0, 8388736, 8388609, 1, 8192, NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO, 8396801, 128, NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO, 8193, 8320, 8388737, 1, 8320, 8388736, 8192, 8396928, 8396929, NbtException.NOT_LISTENING_CALLING, 8388736, 8388609, 8396800, 8396929, NbtException.NOT_LISTENING_CALLING, 0, 0, 8396800, 8320, 8388736, 8388737, 1, 8396801, 8321, 8321, 128, 8396929, NbtException.NOT_LISTENING_CALLING, 1, 8192, 8388609, 8193, 8396928, 8388737, 8193, 8320, NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO, 8396801, 128, NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO, 8192, 8396928};
    private static int[] SP5 = {256, 34078976, 34078720, 1107296512, 524288, 256, 1073741824, 34078720, 1074266368, 524288, 33554688, 1074266368, 1107296512, 1107820544, 524544, 1073741824, 33554432, 1074266112, 1074266112, 0, 1073742080, 1107820800, 1107820800, 33554688, 1107820544, 1073742080, 0, 1107296256, 34078976, 33554432, 1107296256, 524544, 524288, 1107296512, 256, 33554432, 1073741824, 34078720, 1107296512, 1074266368, 33554688, 1073741824, 1107820544, 34078976, 1074266368, 256, 33554432, 1107820544, 1107820800, 524544, 1107296256, 1107820800, 34078720, 0, 1074266112, 1107296256, 524544, 33554688, 1073742080, 524288, 0, 1074266112, 34078976, 1073742080};
    private static int[] SP6 = {536870928, 541065216, 16384, 541081616, 541065216, 16, 541081616, NtlmFlags.NTLMSSP_REQUEST_NON_NT_SESSION_KEY, 536887296, 4210704, NtlmFlags.NTLMSSP_REQUEST_NON_NT_SESSION_KEY, 536870928, 4194320, 536887296, 536870912, 16400, 0, 4194320, 536887312, 16384, 4210688, 536887312, 16, 541065232, 541065232, 0, 4210704, 541081600, 16400, 4210688, 541081600, 536870912, 536887296, 16, 541065232, 4210688, 541081616, NtlmFlags.NTLMSSP_REQUEST_NON_NT_SESSION_KEY, 16400, 536870928, NtlmFlags.NTLMSSP_REQUEST_NON_NT_SESSION_KEY, 536887296, 536870912, 16400, 536870928, 541081616, 4210688, 541065216, 4210704, 541081600, 0, 541065232, 16, 16384, 541065216, 4210704, 16384, 4194320, 536887312, 0, 541081600, 536870912, 4194320, 536887312};
    private static int[] SP7 = {NtlmFlags.NTLMSSP_REQUEST_ACCEPT_RESPONSE, 69206018, 67110914, 0, 2048, 67110914, 2099202, 69208064, 69208066, NtlmFlags.NTLMSSP_REQUEST_ACCEPT_RESPONSE, 0, 67108866, 2, 67108864, 69206018, 2050, 67110912, 2099202, 2097154, 67110912, 67108866, 69206016, 69208064, 2097154, 69206016, 2048, 2050, 69208066, 2099200, 2, 67108864, 2099200, 67108864, 2099200, NtlmFlags.NTLMSSP_REQUEST_ACCEPT_RESPONSE, 67110914, 67110914, 69206018, 69206018, 2, 2097154, 67108864, 67110912, NtlmFlags.NTLMSSP_REQUEST_ACCEPT_RESPONSE, 69208064, 2050, 2099202, 69208064, 2050, 67108866, 69208066, 69206016, 2099200, 0, 2, 69208066, 0, 2099202, 69206016, 2048, 67108866, 67110912, 2048, 2097154};
    private static int[] SP8 = {268439616, 4096, 262144, 268701760, 268435456, 268439616, 64, 268435456, 262208, 268697600, 268701760, 266240, 268701696, 266304, 4096, 64, 268697600, 268435520, 268439552, 4160, 266240, 262208, 268697664, 268701696, 4160, 0, 0, 268697664, 268435520, 268439552, 266304, 262144, 266304, 262144, 268701696, 4096, 64, 268697664, 4096, 266304, 268439552, 64, 268435520, 268697600, 268697664, 268435456, 262144, 268439616, 0, 268701760, 262208, 268435520, 268697600, 268439552, 268439616, 0, 268701760, 266240, 266240, 4160, 4160, 262208, 268435456, 268701696};
    private static int[] bigbyte = {NtlmFlags.NTLMSSP_NEGOTIATE_TARGET_INFO, NtlmFlags.NTLMSSP_REQUEST_NON_NT_SESSION_KEY, NtlmFlags.NTLMSSP_REQUEST_ACCEPT_RESPONSE, 1048576, 524288, 262144, 131072, 65536, 32768, 16384, 8192, 4096, 2048, samr.ACB_AUTOLOCK, 512, 256, 128, 64, 32, 16, 8, 4, 2, 1};
    private static byte[] bytebit = {Byte.MIN_VALUE, 64, 32, 16, 8, 4, 2, 1};
    private static byte[] pc1 = {56, 48, 40, 32, 24, 16, 8, 0, 57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 60, 52, 44, 36, 28, 20, 12, 4, 27, 19, 11, 3};
    private static byte[] pc2 = {13, 16, 10, 23, 0, 4, 2, 27, 14, 5, 20, 9, 22, 18, 11, 3, 25, 7, 15, 6, 26, 19, 12, 1, 40, 51, 30, 36, 46, 54, 29, 39, 50, 44, 32, 47, 43, 48, 38, 55, 33, 52, 45, 41, 49, 35, 28, 31};
    private static int[] totrot = {1, 2, 4, 6, 8, 10, 12, 14, 15, 17, 19, 21, 23, 25, 27, 28};
    private int[] decryptKeys;
    private int[] encryptKeys;
    private int[] tempInts;

    public DES() {
        this.encryptKeys = new int[32];
        this.decryptKeys = new int[32];
        this.tempInts = new int[2];
    }

    public DES(byte[] key) {
        this.encryptKeys = new int[32];
        this.decryptKeys = new int[32];
        this.tempInts = new int[2];
        if (key.length == 7) {
            byte[] key8 = new byte[8];
            makeSMBKey(key, key8);
            setKey(key8);
            return;
        }
        setKey(key);
    }

    public static void makeSMBKey(byte[] key7, byte[] key8) {
        key8[0] = (byte) ((key7[0] >> 1) & 255);
        key8[1] = (byte) ((((key7[0] & 1) << 6) | (((key7[1] & 255) >> 2) & 255)) & 255);
        key8[2] = (byte) ((((key7[1] & 3) << 5) | (((key7[2] & 255) >> 3) & 255)) & 255);
        key8[3] = (byte) ((((key7[2] & 7) << 4) | (((key7[3] & 255) >> 4) & 255)) & 255);
        key8[4] = (byte) ((((key7[3] & 15) << 3) | (((key7[4] & 255) >> 5) & 255)) & 255);
        key8[5] = (byte) ((((key7[4] & 31) << 2) | (((key7[5] & 255) >> 6) & 255)) & 255);
        key8[6] = (byte) ((((key7[5] & 63) << 1) | (((key7[6] & 255) >> 7) & 255)) & 255);
        key8[7] = (byte) (key7[6] & Byte.MAX_VALUE);
        for (int i = 0; i < 8; i++) {
            key8[i] = (byte) (key8[i] << 1);
        }
    }

    public void setKey(byte[] key) {
        deskey(key, true, this.encryptKeys);
        deskey(key, false, this.decryptKeys);
    }

    private void deskey(byte[] keyBlock, boolean encrypting, int[] KnL) {
        int m;
        int[] pc1m = new int[56];
        int[] pcr = new int[56];
        int[] kn = new int[32];
        for (int j = 0; j < 56; j++) {
            byte b = pc1[j];
            pc1m[j] = (keyBlock[b >>> 3] & bytebit[b & 7]) != 0 ? 1 : 0;
        }
        for (int i = 0; i < 16; i++) {
            if (encrypting) {
                m = i << 1;
            } else {
                m = (15 - i) << 1;
            }
            int n = m + 1;
            kn[n] = 0;
            kn[m] = 0;
            for (int j2 = 0; j2 < 28; j2++) {
                int l = j2 + totrot[i];
                if (l < 28) {
                    pcr[j2] = pc1m[l];
                } else {
                    pcr[j2] = pc1m[l - 28];
                }
            }
            for (int j3 = 28; j3 < 56; j3++) {
                int l2 = j3 + totrot[i];
                if (l2 < 56) {
                    pcr[j3] = pc1m[l2];
                } else {
                    pcr[j3] = pc1m[l2 - 28];
                }
            }
            for (int j4 = 0; j4 < 24; j4++) {
                if (pcr[pc2[j4]] != 0) {
                    kn[m] = kn[m] | bigbyte[j4];
                }
                if (pcr[pc2[j4 + 24]] != 0) {
                    kn[n] = kn[n] | bigbyte[j4];
                }
            }
        }
        cookey(kn, KnL);
    }

    private void cookey(int[] raw, int[] KnL) {
        int KnLi = 0;
        int rawi = 0;
        for (int i = 0; i < 16; i++) {
            int rawi2 = rawi + 1;
            int raw0 = raw[rawi];
            rawi = rawi2 + 1;
            int raw1 = raw[rawi2];
            KnL[KnLi] = (raw0 & 16515072) << 6;
            KnL[KnLi] = KnL[KnLi] | ((raw0 & 4032) << 10);
            KnL[KnLi] = KnL[KnLi] | ((raw1 & 16515072) >>> 10);
            KnL[KnLi] = KnL[KnLi] | ((raw1 & 4032) >>> 6);
            int KnLi2 = KnLi + 1;
            KnL[KnLi2] = (raw0 & 258048) << 12;
            KnL[KnLi2] = KnL[KnLi2] | ((raw0 & 63) << 16);
            KnL[KnLi2] = KnL[KnLi2] | ((raw1 & 258048) >>> 4);
            KnL[KnLi2] = KnL[KnLi2] | (raw1 & 63);
            KnLi = KnLi2 + 1;
        }
    }

    private void encrypt(byte[] clearText, int clearOff, byte[] cipherText, int cipherOff) {
        squashBytesToInts(clearText, clearOff, this.tempInts, 0, 2);
        des(this.tempInts, this.tempInts, this.encryptKeys);
        spreadIntsToBytes(this.tempInts, 0, cipherText, cipherOff, 2);
    }

    private void decrypt(byte[] cipherText, int cipherOff, byte[] clearText, int clearOff) {
        squashBytesToInts(cipherText, cipherOff, this.tempInts, 0, 2);
        des(this.tempInts, this.tempInts, this.decryptKeys);
        spreadIntsToBytes(this.tempInts, 0, clearText, clearOff, 2);
    }

    private void des(int[] inInts, int[] outInts, int[] keys) {
        int leftt = inInts[0];
        int right = inInts[1];
        int work = ((leftt >>> 4) ^ right) & 252645135;
        int right2 = right ^ work;
        int leftt2 = leftt ^ (work << 4);
        int work2 = ((leftt2 >>> 16) ^ right2) & 65535;
        int right3 = right2 ^ work2;
        int leftt3 = leftt2 ^ (work2 << 16);
        int work3 = ((right3 >>> 2) ^ leftt3) & 858993459;
        int leftt4 = leftt3 ^ work3;
        int right4 = right3 ^ (work3 << 2);
        int work4 = ((right4 >>> 8) ^ leftt4) & 16711935;
        int leftt5 = leftt4 ^ work4;
        int right5 = right4 ^ (work4 << 8);
        int right6 = (right5 << 1) | ((right5 >>> 31) & 1);
        int work5 = (leftt5 ^ right6) & -1431655766;
        int leftt6 = leftt5 ^ work5;
        int right7 = right6 ^ work5;
        int leftt7 = (leftt6 << 1) | ((leftt6 >>> 31) & 1);
        int keysi = 0;
        for (int round = 0; round < 8; round++) {
            int keysi2 = keysi + 1;
            int work6 = ((right7 << 28) | (right7 >>> 4)) ^ keys[keysi];
            int fval = SP7[work6 & 63] | SP5[(work6 >>> 8) & 63] | SP3[(work6 >>> 16) & 63] | SP1[(work6 >>> 24) & 63];
            int keysi3 = keysi2 + 1;
            int work7 = right7 ^ keys[keysi2];
            leftt7 ^= (((fval | SP8[work7 & 63]) | SP6[(work7 >>> 8) & 63]) | SP4[(work7 >>> 16) & 63]) | SP2[(work7 >>> 24) & 63];
            int keysi4 = keysi3 + 1;
            int work8 = ((leftt7 << 28) | (leftt7 >>> 4)) ^ keys[keysi3];
            int fval2 = SP7[work8 & 63] | SP5[(work8 >>> 8) & 63] | SP3[(work8 >>> 16) & 63] | SP1[(work8 >>> 24) & 63];
            keysi = keysi4 + 1;
            int work9 = leftt7 ^ keys[keysi4];
            right7 ^= (((fval2 | SP8[work9 & 63]) | SP6[(work9 >>> 8) & 63]) | SP4[(work9 >>> 16) & 63]) | SP2[(work9 >>> 24) & 63];
        }
        int right8 = (right7 << 31) | (right7 >>> 1);
        int work10 = (leftt7 ^ right8) & -1431655766;
        int leftt8 = leftt7 ^ work10;
        int right9 = right8 ^ work10;
        int leftt9 = (leftt8 << 31) | (leftt8 >>> 1);
        int work11 = ((leftt9 >>> 8) ^ right9) & 16711935;
        int right10 = right9 ^ work11;
        int leftt10 = leftt9 ^ (work11 << 8);
        int work12 = ((leftt10 >>> 2) ^ right10) & 858993459;
        int right11 = right10 ^ work12;
        int leftt11 = leftt10 ^ (work12 << 2);
        int work13 = ((right11 >>> 16) ^ leftt11) & 65535;
        int leftt12 = leftt11 ^ work13;
        int right12 = right11 ^ (work13 << 16);
        int work14 = ((right12 >>> 4) ^ leftt12) & 252645135;
        outInts[0] = right12 ^ (work14 << 4);
        outInts[1] = leftt12 ^ work14;
    }

    public void encrypt(byte[] clearText, byte[] cipherText) {
        encrypt(clearText, 0, cipherText, 0);
    }

    public void decrypt(byte[] cipherText, byte[] clearText) {
        decrypt(cipherText, 0, clearText, 0);
    }

    public byte[] encrypt(byte[] clearText) {
        int length = clearText.length;
        if (length % 8 != 0) {
            System.out.println("Array must be a multiple of 8");
            return null;
        }
        byte[] cipherText = new byte[length];
        int count = length / 8;
        for (int i = 0; i < count; i++) {
            encrypt(clearText, i * 8, cipherText, i * 8);
        }
        return cipherText;
    }

    public byte[] decrypt(byte[] cipherText) {
        int length = cipherText.length;
        if (length % 8 != 0) {
            System.out.println("Array must be a multiple of 8");
            return null;
        }
        byte[] clearText = new byte[length];
        int count = length / 8;
        for (int i = 0; i < count; i++) {
            encrypt(cipherText, i * 8, clearText, i * 8);
        }
        return clearText;
    }

    public static void squashBytesToInts(byte[] inBytes, int inOff, int[] outInts, int outOff, int intLen) {
        for (int i = 0; i < intLen; i++) {
            outInts[outOff + i] = ((inBytes[(i * 4) + inOff] & 255) << 24) | ((inBytes[((i * 4) + inOff) + 1] & 255) << 16) | ((inBytes[((i * 4) + inOff) + 2] & 255) << 8) | (inBytes[(i * 4) + inOff + 3] & 255);
        }
    }

    public static void spreadIntsToBytes(int[] inInts, int inOff, byte[] outBytes, int outOff, int intLen) {
        for (int i = 0; i < intLen; i++) {
            outBytes[(i * 4) + outOff] = (byte) (inInts[inOff + i] >>> 24);
            outBytes[(i * 4) + outOff + 1] = (byte) (inInts[inOff + i] >>> 16);
            outBytes[(i * 4) + outOff + 2] = (byte) (inInts[inOff + i] >>> 8);
            outBytes[(i * 4) + outOff + 3] = (byte) inInts[inOff + i];
        }
    }
}
