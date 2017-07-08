package com.google.android.mms.pdu;

import java.io.ByteArrayOutputStream;

public class QuotedPrintable {
    private static byte ESCAPE_CHAR;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.google.android.mms.pdu.QuotedPrintable.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.google.android.mms.pdu.QuotedPrintable.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.mms.pdu.QuotedPrintable.<clinit>():void");
    }

    public static final byte[] decodeQuotedPrintable(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int i = 0;
        while (i < bytes.length) {
            byte b = bytes[i];
            if (b == ESCAPE_CHAR) {
                try {
                    if ('\r' == ((char) bytes[i + 1]) && '\n' == ((char) bytes[i + 2])) {
                        i += 2;
                    } else {
                        i++;
                        int u = Character.digit((char) bytes[i], 16);
                        i++;
                        int l = Character.digit((char) bytes[i], 16);
                        if (u == -1 || l == -1) {
                            return null;
                        }
                        buffer.write((char) ((u << 4) + l));
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    return null;
                }
            }
            buffer.write(b);
            i++;
        }
        return buffer.toByteArray();
    }
}
