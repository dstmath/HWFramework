package com.android.server.location.gnsschrlog;

import java.nio.ByteBuffer;

public class Base64Coder {
    static final char[] base64_alphabet = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.gnsschrlog.Base64Coder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.gnsschrlog.Base64Coder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.gnsschrlog.Base64Coder.<clinit>():void");
    }

    public static String encode(byte[] data) {
        int length = data.length;
        byte[] char_array_3 = new byte[]{(byte) 0, (byte) 0, (byte) 0};
        byte[] char_array_4 = new byte[]{(byte) 61, (byte) 61, (byte) 61, (byte) 61};
        StringBuilder retContent = new StringBuilder();
        int reversePos = 0;
        int i = 0;
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
                for (i2 = 0; i2 < 4; i2++) {
                    retContent.append(base64_alphabet[char_array_4[i2]]);
                }
                i2 = 0;
            }
            reversePos = reversePos2;
            i = i2;
        }
        if (i > 0) {
            int j;
            for (j = i; j < 3; j++) {
                char_array_3[j] = (byte) 0;
            }
            char_array_4[0] = (byte) ((char_array_3[0] & 252) >> 2);
            char_array_4[1] = (byte) (((char_array_3[0] & 3) << 4) + ((char_array_3[1] & 240) >> 4));
            char_array_4[2] = (byte) (((char_array_3[1] & 15) << 2) + ((char_array_3[2] & 192) >> 6));
            char_array_4[3] = (byte) (char_array_3[2] & 63);
            for (j = 0; j < i + 1; j++) {
                retContent.append(base64_alphabet[char_array_4[j]]);
            }
            while (true) {
                i2 = i + 1;
                if (i >= 3) {
                    break;
                }
                retContent.append('=');
                i = i2;
            }
        } else {
            i2 = i;
        }
        return retContent.toString();
    }

    public static byte[] decode(byte[] data) {
        int mLength = data.length;
        byte[] char_array_4 = new byte[4];
        byte[] char_array_3 = new byte[3];
        ByteBuffer retContent = ByteBuffer.wrap(new byte[mLength]);
        int enCode = 0;
        int i = 0;
        while (mLength > 0 && ((char) data[enCode]) != '=' && isBase64((char) data[enCode])) {
            mLength--;
            int i2 = i + 1;
            int enCode2 = enCode + 1;
            char_array_4[i] = data[enCode];
            if (i2 == 4) {
                for (i2 = 0; i2 < 4; i2++) {
                    char_array_4[i2] = findChar((char) char_array_4[i2]);
                }
                char_array_3[0] = (byte) ((char_array_4[0] << 2) + ((char_array_4[1] & 48) >> 4));
                char_array_3[1] = (byte) (((char_array_4[1] & 15) << 4) + ((char_array_4[2] & 60) >> 2));
                char_array_3[2] = (byte) (((char_array_4[2] & 3) << 6) + char_array_4[3]);
                for (i2 = 0; i2 < 3; i2++) {
                    retContent.put(char_array_3[i2]);
                }
                i2 = 0;
            }
            enCode = enCode2;
            i = i2;
        }
        if (i > 0) {
            int j;
            for (j = i; j < 4; j++) {
                char_array_4[j] = (byte) 0;
            }
            for (j = 0; j < 4; j++) {
                char_array_4[j] = findChar((char) char_array_4[j]);
            }
            char_array_3[0] = (byte) ((char_array_4[0] << 2) + ((char_array_4[1] & 48) >> 4));
            char_array_3[1] = (byte) (((char_array_4[1] & 15) << 4) + ((char_array_4[2] & 60) >> 2));
            char_array_3[2] = (byte) (((char_array_4[2] & 3) << 6) + char_array_4[3]);
            for (j = 0; j < i - 1; j++) {
                retContent.put(char_array_3[j]);
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
        return (byte) 64;
    }
}
