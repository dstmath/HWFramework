package com.android.okhttp.okio;

import java.io.UnsupportedEncodingException;

final class Base64 {
    private static final byte[] MAP = null;
    private static final byte[] URL_MAP = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.okio.Base64.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.okio.Base64.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.okio.Base64.<clinit>():void");
    }

    private Base64() {
    }

    public static byte[] decode(String in) {
        int limit = in.length();
        while (limit > 0) {
            char c = in.charAt(limit - 1);
            if (c != '=' && c != '\n' && c != '\r' && c != ' ' && c != '\t') {
                break;
            }
            limit--;
        }
        byte[] out = new byte[((int) ((((long) limit) * 6) / 8))];
        int inCount = 0;
        int word = 0;
        int pos = 0;
        int outCount = 0;
        while (pos < limit) {
            int bits;
            int outCount2;
            c = in.charAt(pos);
            if (c >= 'A' && c <= 'Z') {
                bits = c - 65;
            } else if (c >= 'a' && c <= 'z') {
                bits = c - 71;
            } else if (c >= '0' && c <= '9') {
                bits = c + 4;
            } else if (c == '+' || c == '-') {
                bits = 62;
            } else if (c == '/' || c == '_') {
                bits = 63;
            } else if (c != '\n' && c != '\r' && c != ' ' && c != '\t') {
                return null;
            } else {
                outCount2 = outCount;
                pos++;
                outCount = outCount2;
            }
            word = (word << 6) | ((byte) bits);
            inCount++;
            if (inCount % 4 == 0) {
                outCount2 = outCount + 1;
                out[outCount] = (byte) (word >> 16);
                outCount = outCount2 + 1;
                out[outCount2] = (byte) (word >> 8);
                outCount2 = outCount + 1;
                out[outCount] = (byte) word;
            } else {
                outCount2 = outCount;
            }
            pos++;
            outCount = outCount2;
        }
        int lastWordChars = inCount % 4;
        if (lastWordChars == 1) {
            return null;
        }
        if (lastWordChars == 2) {
            outCount2 = outCount + 1;
            out[outCount] = (byte) ((word << 12) >> 16);
        } else if (lastWordChars == 3) {
            word <<= 6;
            outCount2 = outCount + 1;
            out[outCount] = (byte) (word >> 16);
            outCount = outCount2 + 1;
            out[outCount2] = (byte) (word >> 8);
            outCount2 = outCount;
        } else {
            outCount2 = outCount;
        }
        if (outCount2 == out.length) {
            return out;
        }
        byte[] prefix = new byte[outCount2];
        System.arraycopy(out, 0, prefix, 0, outCount2);
        return prefix;
    }

    public static String encode(byte[] in) {
        return encode(in, MAP);
    }

    public static String encodeUrl(byte[] in) {
        return encode(in, URL_MAP);
    }

    private static String encode(byte[] in, byte[] map) {
        byte[] out = new byte[(((in.length + 2) * 4) / 3)];
        int end = in.length - (in.length % 3);
        int index = 0;
        for (int i = 0; i < end; i += 3) {
            int i2 = index + 1;
            out[index] = map[(in[i] & 255) >> 2];
            index = i2 + 1;
            out[i2] = map[((in[i] & 3) << 4) | ((in[i + 1] & 255) >> 4)];
            i2 = index + 1;
            out[index] = map[((in[i + 1] & 15) << 2) | ((in[i + 2] & 255) >> 6)];
            index = i2 + 1;
            out[i2] = map[in[i + 2] & 63];
        }
        switch (in.length % 3) {
            case 1:
                i2 = index + 1;
                out[index] = map[(in[end] & 255) >> 2];
                index = i2 + 1;
                out[i2] = map[(in[end] & 3) << 4];
                i2 = index + 1;
                out[index] = (byte) 61;
                index = i2 + 1;
                out[i2] = (byte) 61;
                i2 = index;
                break;
            case 2:
                i2 = index + 1;
                out[index] = map[(in[end] & 255) >> 2];
                index = i2 + 1;
                out[i2] = map[((in[end] & 3) << 4) | ((in[end + 1] & 255) >> 4)];
                i2 = index + 1;
                out[index] = map[(in[end + 1] & 15) << 2];
                index = i2 + 1;
                out[i2] = (byte) 61;
                i2 = index;
                break;
            default:
                i2 = index;
                break;
        }
        try {
            return new String(out, 0, i2, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
}
