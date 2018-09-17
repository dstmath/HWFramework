package libcore.io;

import dalvik.bytecode.Opcodes;
import dalvik.system.VMDebug;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import libcore.icu.DateUtilsBridge;
import org.w3c.dom.traversal.NodeFilter;

public final class Streams {
    private static AtomicReference<byte[]> skipBuffer;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: libcore.io.Streams.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: libcore.io.Streams.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: libcore.io.Streams.<clinit>():void");
    }

    private Streams() {
    }

    public static int readSingleByte(InputStream in) throws IOException {
        byte[] buffer = new byte[1];
        if (in.read(buffer, 0, 1) != -1) {
            return buffer[0] & Opcodes.OP_CONST_CLASS_JUMBO;
        }
        return -1;
    }

    public static void writeSingleByte(OutputStream out, int b) throws IOException {
        out.write(new byte[]{(byte) (b & Opcodes.OP_CONST_CLASS_JUMBO)});
    }

    public static void readFully(InputStream in, byte[] dst) throws IOException {
        readFully(in, dst, 0, dst.length);
    }

    public static void readFully(InputStream in, byte[] dst, int offset, int byteCount) throws IOException {
        if (byteCount != 0) {
            if (in == null) {
                throw new NullPointerException("in == null");
            } else if (dst == null) {
                throw new NullPointerException("dst == null");
            } else {
                Arrays.checkOffsetAndCount(dst.length, offset, byteCount);
                while (byteCount > 0) {
                    int bytesRead = in.read(dst, offset, byteCount);
                    if (bytesRead < 0) {
                        throw new EOFException();
                    }
                    offset += bytesRead;
                    byteCount -= bytesRead;
                }
            }
        }
    }

    public static byte[] readFully(InputStream in) throws IOException {
        try {
            byte[] readFullyNoClose = readFullyNoClose(in);
            return readFullyNoClose;
        } finally {
            in.close();
        }
    }

    public static byte[] readFullyNoClose(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[NodeFilter.SHOW_DOCUMENT_FRAGMENT];
        while (true) {
            int count = in.read(buffer);
            if (count == -1) {
                return bytes.toByteArray();
            }
            bytes.write(buffer, 0, count);
        }
    }

    public static String readFully(Reader reader) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[NodeFilter.SHOW_DOCUMENT_FRAGMENT];
            while (true) {
                int count = reader.read(buffer);
                if (count == -1) {
                    break;
                }
                writer.write(buffer, 0, count);
            }
            String stringWriter = writer.toString();
            return stringWriter;
        } finally {
            reader.close();
        }
    }

    public static void skipAll(InputStream in) throws IOException {
        do {
            in.skip(Long.MAX_VALUE);
        } while (in.read() != -1);
    }

    public static long skipByReading(InputStream in, long byteCount) throws IOException {
        byte[] buffer = (byte[]) skipBuffer.getAndSet(null);
        if (buffer == null) {
            buffer = new byte[VMDebug.KIND_GLOBAL_EXT_ALLOCATED_OBJECTS];
        }
        long skipped = 0;
        while (skipped < byteCount) {
            int toRead = (int) Math.min(byteCount - skipped, (long) buffer.length);
            int read = in.read(buffer, 0, toRead);
            if (read != -1) {
                skipped += (long) read;
                if (read < toRead) {
                    break;
                }
            }
            break;
        }
        skipBuffer.set(buffer);
        return skipped;
    }

    public static int copy(InputStream in, OutputStream out) throws IOException {
        int total = 0;
        byte[] buffer = new byte[DateUtilsBridge.FORMAT_UTC];
        while (true) {
            int c = in.read(buffer);
            if (c == -1) {
                return total;
            }
            total += c;
            out.write(buffer, 0, c);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String readAsciiLine(InputStream in) throws IOException {
        StringBuilder result = new StringBuilder(80);
        while (true) {
            int c = in.read();
            if (c == -1) {
                break;
            } else if (c == 10) {
                break;
            } else {
                result.append((char) c);
            }
        }
        throw new EOFException();
    }
}
