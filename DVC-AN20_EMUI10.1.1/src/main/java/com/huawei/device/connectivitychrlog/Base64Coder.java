package com.huawei.device.connectivitychrlog;

import java.nio.ByteBuffer;

public class Base64Coder {
    static final char[] base64_alphabet = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '$', '='};

    public static String encode(byte[] data) {
        int length = data.length;
        byte[] char_array_3 = {0, 0, 0};
        byte[] char_array_4 = {61, 61, 61, 61};
        StringBuilder retContent = new StringBuilder();
        int i = 0;
        int reversePos = 0;
        while (length > 0) {
            length--;
            int i2 = i + 1;
            int reversePos2 = reversePos + 1;
            char_array_3[i] = data[reversePos];
            if (i2 == 3) {
                char_array_4[0] = (byte) ((char_array_3[0] & 252) >> 2);
                char_array_4[1] = (byte) (((char_array_3[0] & 3) << 4) + ((char_array_3[1] & 240) >> 4));
                char_array_4[2] = (byte) (((char_array_3[1] & 15) << 2) + ((char_array_3[2] & 192) >> 6));
                char_array_4[3] = (byte) (char_array_3[2] & 63);
                for (int i3 = 0; i3 < 4; i3++) {
                    retContent.append(base64_alphabet[char_array_4[i3]]);
                }
                i = 0;
                reversePos = reversePos2;
            } else {
                i = i2;
                reversePos = reversePos2;
            }
        }
        if (i > 0) {
            for (int j = i; j < 3; j++) {
                char_array_3[j] = 0;
            }
            char_array_4[0] = (byte) ((char_array_3[0] & 252) >> 2);
            char_array_4[1] = (byte) (((char_array_3[0] & 3) << 4) + ((char_array_3[1] & 240) >> 4));
            char_array_4[2] = (byte) (((char_array_3[1] & 15) << 2) + ((char_array_3[2] & 192) >> 6));
            char_array_4[3] = (byte) (char_array_3[2] & 63);
            for (int j2 = 0; j2 < i + 1; j2++) {
                retContent.append(base64_alphabet[char_array_4[j2]]);
            }
            while (true) {
                int i4 = i + 1;
                if (i >= 3) {
                    break;
                }
                retContent.append('=');
                i = i4;
            }
        }
        return retContent.toString();
    }

    public static byte[] decode(byte[] data) {
        int i = 0;
        int enCode = 0;
        int mLength = data.length;
        byte[] char_array_4 = new byte[4];
        byte[] char_array_3 = new byte[3];
        ByteBuffer retContent = ByteBuffer.wrap(new byte[mLength]);
        while (mLength > 0 && ((char) data[enCode]) != '=' && isBase64((char) data[enCode])) {
            mLength--;
            int i2 = i + 1;
            int enCode2 = enCode + 1;
            char_array_4[i] = data[enCode];
            if (i2 == 4) {
                for (int i3 = 0; i3 < 4; i3++) {
                    char_array_4[i3] = findChar((char) char_array_4[i3]);
                }
                char_array_3[0] = (byte) ((char_array_4[0] << 2) + ((char_array_4[1] & 48) >> 4));
                char_array_3[1] = (byte) (((char_array_4[1] & 15) << 4) + ((char_array_4[2] & 60) >> 2));
                char_array_3[2] = (byte) (((char_array_4[2] & 3) << 6) + char_array_4[3]);
                for (int i4 = 0; i4 < 3; i4++) {
                    retContent.put(char_array_3[i4]);
                }
                i = 0;
                enCode = enCode2;
            } else {
                i = i2;
                enCode = enCode2;
            }
        }
        if (i > 0) {
            for (int j = i; j < 4; j++) {
                char_array_4[j] = 0;
            }
            for (int j2 = 0; j2 < 4; j2++) {
                char_array_4[j2] = findChar((char) char_array_4[j2]);
            }
            char_array_3[0] = (byte) ((char_array_4[0] << 2) + ((char_array_4[1] & 48) >> 4));
            char_array_3[1] = (byte) (((char_array_4[1] & 15) << 4) + ((char_array_4[2] & 60) >> 2));
            char_array_3[2] = (byte) (((char_array_4[2] & 3) << 6) + char_array_4[3]);
            for (int j3 = 0; j3 < i - 1; j3++) {
                retContent.put(char_array_3[j3]);
            }
        }
        retContent.flip();
        byte[] retArray = new byte[retContent.limit()];
        retContent.get(retArray, 0, retContent.limit());
        return retArray;
    }

    public static boolean isBase64(char c) {
        for (int i = 0; i < 64; i++) {
            if (c == base64_alphabet[i]) {
                return true;
            }
        }
        return false;
    }

    public static byte findChar(char x) {
        for (int i = 0; i < 64; i++) {
            if (x == base64_alphabet[i]) {
                return (byte) i;
            }
        }
        return 64;
    }
}
