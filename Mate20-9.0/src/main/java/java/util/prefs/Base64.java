package java.util.prefs;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

class Base64 {
    private static final byte[] altBase64ToInt = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, -1, 62, 9, 10, 11, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 12, 13, 14, -1, 15, 63, 16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE, -1, 18, 19, Character.START_PUNCTUATION, 20, Character.CURRENCY_SYMBOL, 27, 28, Character.INITIAL_QUOTE_PUNCTUATION, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 22, 23, 24, Character.MATH_SYMBOL};
    private static final byte[] base64ToInt = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE, 18, 19, 20, Character.START_PUNCTUATION, 22, 23, 24, Character.MATH_SYMBOL, -1, -1, -1, -1, -1, -1, Character.CURRENCY_SYMBOL, 27, 28, Character.INITIAL_QUOTE_PUNCTUATION, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};
    private static final char[] intToAltBase64 = {'!', '\"', '#', '$', '%', '&', '\'', '(', ')', ',', '-', '.', ':', ';', '<', '>', '@', '[', ']', '^', '`', '_', '{', '|', '}', '~', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', Locale.PRIVATE_USE_EXTENSION, 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '?'};
    private static final char[] intToBase64 = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', Locale.PRIVATE_USE_EXTENSION, 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

    Base64() {
    }

    static String byteArrayToBase64(byte[] a) {
        return byteArrayToBase64(a, false);
    }

    static String byteArrayToAltBase64(byte[] a) {
        return byteArrayToBase64(a, true);
    }

    private static String byteArrayToBase64(byte[] a, boolean alternate) {
        int aLen = a.length;
        int numFullGroups = aLen / 3;
        int numBytesInPartialGroup = aLen - (3 * numFullGroups);
        StringBuffer result = new StringBuffer(4 * ((aLen + 2) / 3));
        char[] intToAlpha = alternate ? intToAltBase64 : intToBase64;
        int inCursor = 0;
        int i = 0;
        while (i < numFullGroups) {
            int inCursor2 = inCursor + 1;
            int inCursor3 = a[inCursor] & 255;
            int inCursor4 = inCursor2 + 1;
            int byte1 = a[inCursor2] & 255;
            int inCursor5 = inCursor4 + 1;
            int byte2 = a[inCursor4] & 255;
            result.append(intToAlpha[inCursor3 >> 2]);
            result.append(intToAlpha[((inCursor3 << 4) & 63) | (byte1 >> 4)]);
            result.append(intToAlpha[((byte1 << 2) & 63) | (byte2 >> 6)]);
            result.append(intToAlpha[byte2 & 63]);
            i++;
            inCursor = inCursor5;
        }
        if (numBytesInPartialGroup != 0) {
            int inCursor6 = inCursor + 1;
            int inCursor7 = a[inCursor] & 255;
            result.append(intToAlpha[inCursor7 >> 2]);
            if (numBytesInPartialGroup == 1) {
                result.append(intToAlpha[(inCursor7 << 4) & 63]);
                result.append("==");
                int byte0 = inCursor6;
            } else {
                int inCursor8 = inCursor6 + 1;
                int byte12 = a[inCursor6] & 255;
                result.append(intToAlpha[((inCursor7 << 4) & 63) | (byte12 >> 4)]);
                result.append(intToAlpha[(byte12 << 2) & 63]);
                result.append('=');
                int byte02 = inCursor8;
            }
        }
        return result.toString();
    }

    static byte[] base64ToByteArray(String s) {
        return base64ToByteArray(s, false);
    }

    static byte[] altBase64ToByteArray(String s) {
        return base64ToByteArray(s, true);
    }

    private static byte[] base64ToByteArray(String s, boolean alternate) {
        String str = s;
        byte[] alphaToInt = alternate ? altBase64ToInt : base64ToInt;
        int sLen = s.length();
        int numGroups = sLen / 4;
        if (4 * numGroups == sLen) {
            int missingBytesInLastGroup = 0;
            int numFullGroups = numGroups;
            if (sLen != 0) {
                if (str.charAt(sLen - 1) == '=') {
                    missingBytesInLastGroup = 0 + 1;
                    numFullGroups--;
                }
                if (str.charAt(sLen - 2) == '=') {
                    missingBytesInLastGroup++;
                }
            }
            byte[] result = new byte[((3 * numGroups) - missingBytesInLastGroup)];
            int inCursor = 0;
            int outCursor = 0;
            int i = 0;
            while (i < numFullGroups) {
                int inCursor2 = inCursor + 1;
                int ch0 = base64toInt(str.charAt(inCursor), alphaToInt);
                int inCursor3 = inCursor2 + 1;
                int inCursor4 = base64toInt(str.charAt(inCursor2), alphaToInt);
                int inCursor5 = inCursor3 + 1;
                int ch2 = base64toInt(str.charAt(inCursor3), alphaToInt);
                int inCursor6 = inCursor5 + 1;
                int ch3 = base64toInt(str.charAt(inCursor5), alphaToInt);
                int outCursor2 = outCursor + 1;
                result[outCursor] = (byte) ((ch0 << 2) | (inCursor4 >> 4));
                int outCursor3 = outCursor2 + 1;
                result[outCursor2] = (byte) ((inCursor4 << 4) | (ch2 >> 2));
                outCursor = outCursor3 + 1;
                result[outCursor3] = (byte) ((ch2 << 6) | ch3);
                i++;
                inCursor = inCursor6;
            }
            if (missingBytesInLastGroup != 0) {
                int inCursor7 = inCursor + 1;
                int ch02 = base64toInt(str.charAt(inCursor), alphaToInt);
                int inCursor8 = inCursor7 + 1;
                int inCursor9 = base64toInt(str.charAt(inCursor7), alphaToInt);
                int outCursor4 = outCursor + 1;
                result[outCursor] = (byte) ((ch02 << 2) | (inCursor9 >> 4));
                if (missingBytesInLastGroup == 1) {
                    int i2 = outCursor4 + 1;
                    result[outCursor4] = (byte) ((inCursor9 << 4) | (base64toInt(str.charAt(inCursor8), alphaToInt) >> 2));
                    int ch03 = inCursor8 + 1;
                } else {
                    int i3 = inCursor8;
                    int i4 = outCursor4;
                }
            } else {
                int i5 = outCursor;
            }
            return result;
        }
        throw new IllegalArgumentException("String length must be a multiple of four.");
    }

    private static int base64toInt(char c, byte[] alphaToInt) {
        byte result = alphaToInt[c];
        if (result >= 0) {
            return result;
        }
        throw new IllegalArgumentException("Illegal character " + c);
    }

    public static void main(String[] args) {
        int numRuns = Integer.parseInt(args[0]);
        int numBytes = Integer.parseInt(args[1]);
        Random rnd = new Random();
        for (int i = 0; i < numRuns; i++) {
            for (int j = 0; j < numBytes; j++) {
                byte[] arr = new byte[j];
                for (int k = 0; k < j; k++) {
                    arr[k] = (byte) rnd.nextInt();
                }
                if (!Arrays.equals(arr, base64ToByteArray(byteArrayToBase64(arr)))) {
                    System.out.println("Dismal failure!");
                }
                if (!Arrays.equals(arr, altBase64ToByteArray(byteArrayToAltBase64(arr)))) {
                    System.out.println("Alternate dismal failure!");
                }
            }
        }
    }
}
