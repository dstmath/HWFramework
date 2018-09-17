package com.android.org.conscrypt;

public class Hex {
    private static final char[] DIGITS = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.conscrypt.Hex.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.conscrypt.Hex.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.Hex.<clinit>():void");
    }

    private Hex() {
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] buf = new char[(bytes.length * 2)];
        int c = 0;
        for (byte b : bytes) {
            int i = c + 1;
            buf[c] = DIGITS[(b >> 4) & 15];
            c = i + 1;
            buf[i] = DIGITS[b & 15];
        }
        return new String(buf);
    }

    public static String intToHexString(int i, int minWidth) {
        char[] buf = new char[8];
        int cursor = 8;
        while (true) {
            cursor--;
            buf[cursor] = DIGITS[i & 15];
            i >>>= 4;
            if (i == 0 && 8 - cursor >= minWidth) {
                return new String(buf, cursor, 8 - cursor);
            }
        }
    }
}
