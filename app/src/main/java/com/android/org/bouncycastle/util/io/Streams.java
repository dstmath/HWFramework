package com.android.org.bouncycastle.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class Streams {
    private static int BUFFER_SIZE;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.util.io.Streams.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.util.io.Streams.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.util.io.Streams.<clinit>():void");
    }

    public static void drain(InputStream inStr) throws IOException {
        byte[] bs = new byte[BUFFER_SIZE];
        do {
        } while (inStr.read(bs, 0, bs.length) >= 0);
    }

    public static byte[] readAll(InputStream inStr) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        pipeAll(inStr, buf);
        return buf.toByteArray();
    }

    public static byte[] readAllLimited(InputStream inStr, int limit) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        pipeAllLimited(inStr, (long) limit, buf);
        return buf.toByteArray();
    }

    public static int readFully(InputStream inStr, byte[] buf) throws IOException {
        return readFully(inStr, buf, 0, buf.length);
    }

    public static int readFully(InputStream inStr, byte[] buf, int off, int len) throws IOException {
        int totalRead = 0;
        while (totalRead < len) {
            int numRead = inStr.read(buf, off + totalRead, len - totalRead);
            if (numRead < 0) {
                break;
            }
            totalRead += numRead;
        }
        return totalRead;
    }

    public static void pipeAll(InputStream inStr, OutputStream outStr) throws IOException {
        byte[] bs = new byte[BUFFER_SIZE];
        while (true) {
            int numRead = inStr.read(bs, 0, bs.length);
            if (numRead >= 0) {
                outStr.write(bs, 0, numRead);
            } else {
                return;
            }
        }
    }

    public static long pipeAllLimited(InputStream inStr, long limit, OutputStream outStr) throws IOException {
        long total = 0;
        byte[] bs = new byte[BUFFER_SIZE];
        while (true) {
            int numRead = inStr.read(bs, 0, bs.length);
            if (numRead < 0) {
                return total;
            }
            if (limit - total < ((long) numRead)) {
                break;
            }
            total += (long) numRead;
            outStr.write(bs, 0, numRead);
        }
        throw new StreamOverflowException("Data Overflow");
    }
}
