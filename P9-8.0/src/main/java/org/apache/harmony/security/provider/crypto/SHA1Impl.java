package org.apache.harmony.security.provider.crypto;

public class SHA1Impl {
    static void computeHash(int[] arrW) {
        int t;
        int temp;
        int a = arrW[82];
        int b = arrW[83];
        int c = arrW[84];
        int d = arrW[85];
        int e = arrW[86];
        for (t = 16; t < 80; t++) {
            temp = ((arrW[t - 3] ^ arrW[t - 8]) ^ arrW[t - 14]) ^ arrW[t - 16];
            arrW[t] = (temp << 1) | (temp >>> 31);
        }
        for (t = 0; t < 20; t++) {
            temp = (((a << 5) | (a >>> 27)) + ((b & c) | ((~b) & d))) + ((arrW[t] + e) + 1518500249);
            e = d;
            d = c;
            c = (b << 30) | (b >>> 2);
            b = a;
            a = temp;
        }
        for (t = 20; t < 40; t++) {
            temp = (((a << 5) | (a >>> 27)) + ((b ^ c) ^ d)) + ((arrW[t] + e) + 1859775393);
            e = d;
            d = c;
            c = (b << 30) | (b >>> 2);
            b = a;
            a = temp;
        }
        for (t = 40; t < 60; t++) {
            temp = (((a << 5) | (a >>> 27)) + (((b & c) | (b & d)) | (c & d))) + ((arrW[t] + e) - 1894007588);
            e = d;
            d = c;
            c = (b << 30) | (b >>> 2);
            b = a;
            a = temp;
        }
        for (t = 60; t < 80; t++) {
            temp = (((a << 5) | (a >>> 27)) + ((b ^ c) ^ d)) + ((arrW[t] + e) - 899497514);
            e = d;
            d = c;
            c = (b << 30) | (b >>> 2);
            b = a;
            a = temp;
        }
        arrW[82] = arrW[82] + a;
        arrW[83] = arrW[83] + b;
        arrW[84] = arrW[84] + c;
        arrW[85] = arrW[85] + d;
        arrW[86] = arrW[86] + e;
    }

    static void updateHash(int[] intArray, byte[] byteInput, int fromByte, int toByte) {
        int index = intArray[81];
        int i = fromByte;
        int wordIndex = index >> 2;
        int byteIndex = index & 3;
        intArray[81] = (((index + toByte) - fromByte) + 1) & 63;
        if (byteIndex != 0) {
            while (i <= toByte && byteIndex < 4) {
                intArray[wordIndex] = intArray[wordIndex] | ((byteInput[i] & 255) << ((3 - byteIndex) << 3));
                byteIndex++;
                i++;
            }
            if (byteIndex == 4) {
                wordIndex++;
                if (wordIndex == 16) {
                    computeHash(intArray);
                    wordIndex = 0;
                }
            }
            if (i > toByte) {
                return;
            }
        }
        int maxWord = ((toByte - i) + 1) >> 2;
        for (int k = 0; k < maxWord; k++) {
            intArray[wordIndex] = ((((byteInput[i] & 255) << 24) | ((byteInput[i + 1] & 255) << 16)) | ((byteInput[i + 2] & 255) << 8)) | (byteInput[i + 3] & 255);
            i += 4;
            wordIndex++;
            if (wordIndex >= 16) {
                computeHash(intArray);
                wordIndex = 0;
            }
        }
        int nBytes = (toByte - i) + 1;
        if (nBytes != 0) {
            int w = (byteInput[i] & 255) << 24;
            if (nBytes != 1) {
                w |= (byteInput[i + 1] & 255) << 16;
                if (nBytes != 2) {
                    w |= (byteInput[i + 2] & 255) << 8;
                }
            }
            intArray[wordIndex] = w;
        }
    }
}
