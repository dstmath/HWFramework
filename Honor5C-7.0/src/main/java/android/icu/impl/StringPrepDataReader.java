package android.icu.impl;

import android.icu.impl.ICUBinary.Authenticate;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class StringPrepDataReader implements Authenticate {
    private static final int DATA_FORMAT_ID = 1397772880;
    private static final byte[] DATA_FORMAT_VERSION = null;
    private static final boolean debug = false;
    private ByteBuffer byteBuffer;
    private int unicodeVersion;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.impl.StringPrepDataReader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.impl.StringPrepDataReader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.StringPrepDataReader.<clinit>():void");
    }

    public StringPrepDataReader(ByteBuffer bytes) throws IOException {
        if (debug) {
            System.out.println("Bytes in buffer " + bytes.remaining());
        }
        this.byteBuffer = bytes;
        this.unicodeVersion = ICUBinary.readHeader(this.byteBuffer, DATA_FORMAT_ID, this);
        if (debug) {
            System.out.println("Bytes left in byteBuffer " + this.byteBuffer.remaining());
        }
    }

    public char[] read(int length) throws IOException {
        return ICUBinary.getChars(this.byteBuffer, length, 0);
    }

    public byte[] getDataFormatVersion() {
        return DATA_FORMAT_VERSION;
    }

    public boolean isDataVersionAcceptable(byte[] version) {
        if (version[0] == DATA_FORMAT_VERSION[0] && version[2] == DATA_FORMAT_VERSION[2] && version[3] == DATA_FORMAT_VERSION[3]) {
            return true;
        }
        return false;
    }

    public int[] readIndexes(int length) throws IOException {
        int[] indexes = new int[length];
        for (int i = 0; i < length; i++) {
            indexes[i] = this.byteBuffer.getInt();
        }
        return indexes;
    }

    public byte[] getUnicodeVersion() {
        return ICUBinary.getVersionByteArrayFromCompactInt(this.unicodeVersion);
    }
}
