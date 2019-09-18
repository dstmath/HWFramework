package com.google.android.mms.pdu;

import com.android.internal.telephony.uicc.AnswerToReset;

public class Base64 {
    static final int BASELENGTH = 255;
    static final int FOURBYTE = 4;
    static final byte PAD = 61;
    private static byte[] base64Alphabet = new byte[255];

    static {
        for (int i = 0; i < 255; i++) {
            base64Alphabet[i] = -1;
        }
        for (int i2 = 90; i2 >= 65; i2--) {
            base64Alphabet[i2] = (byte) (i2 - 65);
        }
        for (int i3 = 122; i3 >= 97; i3--) {
            base64Alphabet[i3] = (byte) ((i3 - 97) + 26);
        }
        for (int i4 = 57; i4 >= 48; i4--) {
            base64Alphabet[i4] = (byte) ((i4 - 48) + 52);
        }
        base64Alphabet[43] = 62;
        base64Alphabet[47] = AnswerToReset.INVERSE_CONVENTION;
    }

    public static byte[] decodeBase64(byte[] base64Data) {
        byte[] base64Data2 = discardNonBase64(base64Data);
        if (base64Data2.length == 0) {
            return new byte[0];
        }
        int numberQuadruple = base64Data2.length / 4;
        int encodedIndex = 0;
        int lastData = base64Data2.length;
        while (base64Data2[lastData - 1] == 61) {
            lastData--;
            if (lastData == 0) {
                return new byte[0];
            }
        }
        byte[] decodedData = new byte[(lastData - numberQuadruple)];
        for (int i = 0; i < numberQuadruple; i++) {
            int dataIndex = i * 4;
            byte marker0 = base64Data2[dataIndex + 2];
            byte marker1 = base64Data2[dataIndex + 3];
            byte b1 = base64Alphabet[base64Data2[dataIndex]];
            byte b2 = base64Alphabet[base64Data2[dataIndex + 1]];
            if (marker0 != 61 && marker1 != 61) {
                byte b3 = base64Alphabet[marker0];
                byte b4 = base64Alphabet[marker1];
                decodedData[encodedIndex] = (byte) ((b1 << 2) | (b2 >> 4));
                decodedData[encodedIndex + 1] = (byte) (((b2 & 15) << 4) | ((b3 >> 2) & 15));
                decodedData[encodedIndex + 2] = (byte) ((b3 << 6) | b4);
            } else if (marker0 == 61) {
                decodedData[encodedIndex] = (byte) ((b1 << 2) | (b2 >> 4));
            } else if (marker1 == 61) {
                byte b32 = base64Alphabet[marker0];
                decodedData[encodedIndex] = (byte) ((b1 << 2) | (b2 >> 4));
                decodedData[encodedIndex + 1] = (byte) (((b2 & 15) << 4) | ((b32 >> 2) & 15));
            }
            encodedIndex += 3;
        }
        return decodedData;
    }

    private static boolean isBase64(byte octect) {
        if (octect != 61 && base64Alphabet[octect] == -1) {
            return false;
        }
        return true;
    }

    static byte[] discardNonBase64(byte[] data) {
        byte[] groomedData = new byte[data.length];
        int bytesCopied = 0;
        for (int i = 0; i < data.length; i++) {
            if (isBase64(data[i])) {
                groomedData[bytesCopied] = data[i];
                bytesCopied++;
            }
        }
        byte[] packedData = new byte[bytesCopied];
        System.arraycopy(groomedData, 0, packedData, 0, bytesCopied);
        return packedData;
    }
}
