package com.android.org.bouncycastle.util;

import com.android.org.bouncycastle.asn1.x509.ReasonFlags;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

public final class Strings {
    private static String LINE_SEPARATOR;

    private static class StringListImpl extends ArrayList<String> implements StringList {
        private StringListImpl() {
        }

        public boolean add(String s) {
            return super.add(s);
        }

        public String set(int index, String element) {
            return (String) super.set(index, element);
        }

        public void add(int index, String element) {
            super.add(index, element);
        }

        public String[] toStringArray() {
            String[] strs = new String[size()];
            for (int i = 0; i != strs.length; i++) {
                strs[i] = (String) get(i);
            }
            return strs;
        }

        public String[] toStringArray(int from, int to) {
            String[] strs = new String[(to - from)];
            int i = from;
            while (i != size() && i != to) {
                strs[i - from] = (String) get(i);
                i++;
            }
            return strs;
        }

        public /* bridge */ /* synthetic */ String get(int index) {
            return (String) get(index);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.util.Strings.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.util.Strings.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.util.Strings.<clinit>():void");
    }

    public static String fromUTF8ByteArray(byte[] bytes) {
        int i = 0;
        int length = 0;
        while (i < bytes.length) {
            length++;
            if ((bytes[i] & 240) == 240) {
                length++;
                i += 4;
            } else if ((bytes[i] & 224) == 224) {
                i += 3;
            } else if ((bytes[i] & 192) == 192) {
                i += 2;
            } else {
                i++;
            }
        }
        char[] cs = new char[length];
        i = 0;
        length = 0;
        while (i < bytes.length) {
            int length2;
            char ch;
            if ((bytes[i] & 240) == 240) {
                int U = (((((bytes[i] & 3) << 18) | ((bytes[i + 1] & 63) << 12)) | ((bytes[i + 2] & 63) << 6)) | (bytes[i + 3] & 63)) - 65536;
                char W2 = (char) ((U & 1023) | 56320);
                length2 = length + 1;
                cs[length] = (char) ((U >> 10) | 55296);
                ch = W2;
                i += 4;
                length = length2;
            } else if ((bytes[i] & 224) == 224) {
                ch = (char) ((((bytes[i] & 15) << 12) | ((bytes[i + 1] & 63) << 6)) | (bytes[i + 2] & 63));
                i += 3;
            } else if ((bytes[i] & 208) == 208) {
                ch = (char) (((bytes[i] & 31) << 6) | (bytes[i + 1] & 63));
                i += 2;
            } else if ((bytes[i] & 192) == 192) {
                ch = (char) (((bytes[i] & 31) << 6) | (bytes[i + 1] & 63));
                i += 2;
            } else {
                ch = (char) (bytes[i] & 255);
                i++;
            }
            length2 = length + 1;
            cs[length] = ch;
            length = length2;
        }
        return new String(cs);
    }

    public static byte[] toUTF8ByteArray(String string) {
        return toUTF8ByteArray(string.toCharArray());
    }

    public static byte[] toUTF8ByteArray(char[] string) {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        try {
            toUTF8ByteArray(string, bOut);
            return bOut.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("cannot encode string to byte array!");
        }
    }

    public static void toUTF8ByteArray(char[] string, OutputStream sOut) throws IOException {
        char[] c = string;
        int i = 0;
        while (i < string.length) {
            char ch = string[i];
            if (ch < '\u0080') {
                sOut.write(ch);
            } else if (ch < '\u0800') {
                sOut.write((ch >> 6) | 192);
                sOut.write((ch & 63) | ReasonFlags.unused);
            } else if (ch < '\ud800' || ch > '\udfff') {
                sOut.write((ch >> 12) | 224);
                sOut.write(((ch >> 6) & 63) | ReasonFlags.unused);
                sOut.write((ch & 63) | ReasonFlags.unused);
            } else if (i + 1 >= string.length) {
                throw new IllegalStateException("invalid UTF-16 codepoint");
            } else {
                char W1 = ch;
                i++;
                ch = string[i];
                char W2 = ch;
                if (W1 > '\udbff') {
                    throw new IllegalStateException("invalid UTF-16 codepoint");
                }
                int codePoint = (((W1 & 1023) << 10) | (ch & 1023)) + 65536;
                sOut.write((codePoint >> 18) | 240);
                sOut.write(((codePoint >> 12) & 63) | ReasonFlags.unused);
                sOut.write(((codePoint >> 6) & 63) | ReasonFlags.unused);
                sOut.write((codePoint & 63) | ReasonFlags.unused);
            }
            i++;
        }
    }

    public static String toUpperCase(String string) {
        boolean changed = false;
        char[] chars = string.toCharArray();
        for (int i = 0; i != chars.length; i++) {
            char ch = chars[i];
            if ('a' <= ch && 'z' >= ch) {
                changed = true;
                chars[i] = (char) ((ch - 97) + 65);
            }
        }
        if (changed) {
            return new String(chars);
        }
        return string;
    }

    public static String toLowerCase(String string) {
        boolean changed = false;
        char[] chars = string.toCharArray();
        for (int i = 0; i != chars.length; i++) {
            char ch = chars[i];
            if ('A' <= ch && 'Z' >= ch) {
                changed = true;
                chars[i] = (char) ((ch - 65) + 97);
            }
        }
        if (changed) {
            return new String(chars);
        }
        return string;
    }

    public static byte[] toByteArray(char[] chars) {
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i != bytes.length; i++) {
            bytes[i] = (byte) chars[i];
        }
        return bytes;
    }

    public static byte[] toByteArray(String string) {
        byte[] bytes = new byte[string.length()];
        for (int i = 0; i != bytes.length; i++) {
            bytes[i] = (byte) string.charAt(i);
        }
        return bytes;
    }

    public static int toByteArray(String s, byte[] buf, int off) {
        int count = s.length();
        for (int i = 0; i < count; i++) {
            buf[off + i] = (byte) s.charAt(i);
        }
        return count;
    }

    public static String fromByteArray(byte[] bytes) {
        return new String(asCharArray(bytes));
    }

    public static char[] asCharArray(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0; i != chars.length; i++) {
            chars[i] = (char) (bytes[i] & 255);
        }
        return chars;
    }

    public static String[] split(String input, char delimiter) {
        Vector v = new Vector();
        boolean moreTokens = true;
        while (moreTokens) {
            int tokenLocation = input.indexOf(delimiter);
            if (tokenLocation > 0) {
                v.addElement(input.substring(0, tokenLocation));
                input = input.substring(tokenLocation + 1);
            } else {
                moreTokens = false;
                v.addElement(input);
            }
        }
        String[] res = new String[v.size()];
        for (int i = 0; i != res.length; i++) {
            res[i] = (String) v.elementAt(i);
        }
        return res;
    }

    public static StringList newList() {
        return new StringListImpl();
    }

    public static String lineSeparator() {
        return LINE_SEPARATOR;
    }
}
