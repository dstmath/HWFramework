package java.util.prefs;

import java.util.Arrays;
import java.util.Random;

class Base64 {
    private static final byte[] altBase64ToInt = null;
    private static final byte[] base64ToInt = null;
    private static final char[] intToAltBase64 = null;
    private static final char[] intToBase64 = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.prefs.Base64.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.prefs.Base64.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.prefs.Base64.<clinit>():void");
    }

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
        int numBytesInPartialGroup = aLen - (numFullGroups * 3);
        StringBuffer result = new StringBuffer(((aLen + 2) / 3) * 4);
        char[] intToAlpha = alternate ? intToAltBase64 : intToBase64;
        int i = 0;
        int inCursor = 0;
        while (i < numFullGroups) {
            int inCursor2 = inCursor + 1;
            int byte0 = a[inCursor] & 255;
            inCursor = inCursor2 + 1;
            int byte1 = a[inCursor2] & 255;
            inCursor2 = inCursor + 1;
            int byte2 = a[inCursor] & 255;
            result.append(intToAlpha[byte0 >> 2]);
            result.append(intToAlpha[((byte0 << 4) & 63) | (byte1 >> 4)]);
            result.append(intToAlpha[((byte1 << 2) & 63) | (byte2 >> 6)]);
            result.append(intToAlpha[byte2 & 63]);
            i++;
            inCursor = inCursor2;
        }
        if (numBytesInPartialGroup != 0) {
            inCursor2 = inCursor + 1;
            byte0 = a[inCursor] & 255;
            result.append(intToAlpha[byte0 >> 2]);
            if (numBytesInPartialGroup == 1) {
                result.append(intToAlpha[(byte0 << 4) & 63]);
                result.append("==");
            } else {
                inCursor = inCursor2 + 1;
                byte1 = a[inCursor2] & 255;
                result.append(intToAlpha[((byte0 << 4) & 63) | (byte1 >> 4)]);
                result.append(intToAlpha[(byte1 << 2) & 63]);
                result.append('=');
                inCursor2 = inCursor;
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
        byte[] alphaToInt = alternate ? altBase64ToInt : base64ToInt;
        int sLen = s.length();
        int numGroups = sLen / 4;
        if (numGroups * 4 != sLen) {
            throw new IllegalArgumentException("String length must be a multiple of four.");
        }
        int missingBytesInLastGroup = 0;
        int numFullGroups = numGroups;
        if (sLen != 0) {
            if (s.charAt(sLen - 1) == '=') {
                missingBytesInLastGroup = 1;
                numFullGroups = numGroups - 1;
            }
            if (s.charAt(sLen - 2) == '=') {
                missingBytesInLastGroup++;
            }
        }
        byte[] result = new byte[((numGroups * 3) - missingBytesInLastGroup)];
        int i = 0;
        int outCursor = 0;
        int inCursor = 0;
        while (i < numFullGroups) {
            int i2 = inCursor + 1;
            int ch0 = base64toInt(s.charAt(inCursor), alphaToInt);
            inCursor = i2 + 1;
            int ch1 = base64toInt(s.charAt(i2), alphaToInt);
            i2 = inCursor + 1;
            int ch2 = base64toInt(s.charAt(inCursor), alphaToInt);
            inCursor = i2 + 1;
            int ch3 = base64toInt(s.charAt(i2), alphaToInt);
            int i3 = outCursor + 1;
            result[outCursor] = (byte) ((ch0 << 2) | (ch1 >> 4));
            outCursor = i3 + 1;
            result[i3] = (byte) ((ch1 << 4) | (ch2 >> 2));
            i3 = outCursor + 1;
            result[outCursor] = (byte) ((ch2 << 6) | ch3);
            i++;
            outCursor = i3;
        }
        if (missingBytesInLastGroup != 0) {
            i2 = inCursor + 1;
            ch0 = base64toInt(s.charAt(inCursor), alphaToInt);
            inCursor = i2 + 1;
            ch1 = base64toInt(s.charAt(i2), alphaToInt);
            i3 = outCursor + 1;
            result[outCursor] = (byte) ((ch0 << 2) | (ch1 >> 4));
            if (missingBytesInLastGroup == 1) {
                i2 = inCursor + 1;
                outCursor = i3 + 1;
                result[i3] = (byte) ((ch1 << 4) | (base64toInt(s.charAt(inCursor), alphaToInt) >> 2));
                i3 = outCursor;
            }
        } else {
            i2 = inCursor;
        }
        return result;
    }

    private static int base64toInt(char c, byte[] alphaToInt) {
        int result = alphaToInt[c];
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
