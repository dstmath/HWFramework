package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.io.Streams;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

class DefiniteLengthInputStream extends LimitedInputStream {
    private static final byte[] EMPTY_BYTES = null;
    private final int _originalLength;
    private int _remaining;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.DefiniteLengthInputStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.DefiniteLengthInputStream.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.DefiniteLengthInputStream.<clinit>():void");
    }

    DefiniteLengthInputStream(InputStream in, int length) {
        super(in, length);
        if (length < 0) {
            throw new IllegalArgumentException("negative lengths not allowed");
        }
        this._originalLength = length;
        this._remaining = length;
        if (length == 0) {
            setParentEofDetect(true);
        }
    }

    int getRemaining() {
        return this._remaining;
    }

    public int read() throws IOException {
        if (this._remaining == 0) {
            return -1;
        }
        int b = this._in.read();
        if (b < 0) {
            throw new EOFException("DEF length " + this._originalLength + " object truncated by " + this._remaining);
        }
        int i = this._remaining - 1;
        this._remaining = i;
        if (i == 0) {
            setParentEofDetect(true);
        }
        return b;
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        if (this._remaining == 0) {
            return -1;
        }
        int numRead = this._in.read(buf, off, Math.min(len, this._remaining));
        if (numRead < 0) {
            throw new EOFException("DEF length " + this._originalLength + " object truncated by " + this._remaining);
        }
        int i = this._remaining - numRead;
        this._remaining = i;
        if (i == 0) {
            setParentEofDetect(true);
        }
        return numRead;
    }

    byte[] toByteArray() throws IOException {
        if (this._remaining == 0) {
            return EMPTY_BYTES;
        }
        byte[] bytes = new byte[this._remaining];
        int readFully = this._remaining - Streams.readFully(this._in, bytes);
        this._remaining = readFully;
        if (readFully != 0) {
            throw new EOFException("DEF length " + this._originalLength + " object truncated by " + this._remaining);
        }
        setParentEofDetect(true);
        return bytes;
    }
}
