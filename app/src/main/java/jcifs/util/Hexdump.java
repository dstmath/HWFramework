package jcifs.util;

import java.io.PrintStream;

public class Hexdump {
    public static final char[] HEX_DIGITS = null;
    private static final String NL = null;
    private static final int NL_LENGTH = 0;
    private static final char[] SPACE_CHARS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: jcifs.util.Hexdump.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: jcifs.util.Hexdump.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: jcifs.util.Hexdump.<clinit>():void");
    }

    public static void hexdump(PrintStream ps, byte[] src, int srcIndex, int length) {
        if (length != 0) {
            int r;
            int s = length % 16;
            if (s == 0) {
                r = length / 16;
            } else {
                r = (length / 16) + 1;
            }
            char[] c = new char[((NL_LENGTH + 74) * r)];
            char[] d = new char[16];
            int si = 0;
            int ci = 0;
            do {
                toHexChars(si, c, ci, 5);
                ci += 5;
                int ci2 = ci + 1;
                c[ci] = ':';
                while (si != length) {
                    ci = ci2 + 1;
                    c[ci2] = ' ';
                    int i = src[srcIndex + si] & 255;
                    toHexChars(i, c, ci, 2);
                    ci += 2;
                    if (i < 0 || Character.isISOControl((char) i)) {
                        d[si % 16] = '.';
                    } else {
                        d[si % 16] = (char) i;
                    }
                    si++;
                    if (si % 16 == 0) {
                        break;
                    }
                    ci2 = ci;
                }
                int n = 16 - s;
                System.arraycopy(SPACE_CHARS, 0, c, ci2, n * 3);
                ci = ci2 + (n * 3);
                System.arraycopy(SPACE_CHARS, 0, d, s, n);
                ci2 = ci + 1;
                c[ci] = ' ';
                ci = ci2 + 1;
                c[ci2] = ' ';
                ci2 = ci + 1;
                c[ci] = '|';
                System.arraycopy(d, 0, c, ci2, 16);
                ci = ci2 + 16;
                ci2 = ci + 1;
                c[ci] = '|';
                NL.getChars(0, NL_LENGTH, c, ci2);
                ci = ci2 + NL_LENGTH;
            } while (si < length);
            ps.println(c);
        }
    }

    public static String toHexString(int val, int size) {
        char[] c = new char[size];
        toHexChars(val, c, 0, size);
        return new String(c);
    }

    public static String toHexString(long val, int size) {
        char[] c = new char[size];
        toHexChars(val, c, 0, size);
        return new String(c);
    }

    public static String toHexString(byte[] src, int srcIndex, int size) {
        int i;
        char[] c = new char[size];
        size = size % 2 == 0 ? size / 2 : (size / 2) + 1;
        int j = 0;
        for (int i2 = 0; i2 < size; i2++) {
            i = j + 1;
            c[j] = HEX_DIGITS[(src[i2] >> 4) & 15];
            if (i == c.length) {
                break;
            }
            j = i + 1;
            c[i] = HEX_DIGITS[src[i2] & 15];
        }
        i = j;
        return new String(c);
    }

    public static void toHexChars(int val, char[] dst, int dstIndex, int size) {
        while (size > 0) {
            int i = (dstIndex + size) - 1;
            if (i < dst.length) {
                dst[i] = HEX_DIGITS[val & 15];
            }
            if (val != 0) {
                val >>>= 4;
            }
            size--;
        }
    }

    public static void toHexChars(long val, char[] dst, int dstIndex, int size) {
        while (size > 0) {
            dst[(dstIndex + size) - 1] = HEX_DIGITS[(int) (15 & val)];
            if (val != 0) {
                val >>>= 4;
            }
            size--;
        }
    }
}
