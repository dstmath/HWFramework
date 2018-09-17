package java.util.zip;

import java.nio.ByteBuffer;
import sun.nio.ch.DirectBuffer;

public class Adler32 implements Checksum {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private int adler;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.zip.Adler32.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.zip.Adler32.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.zip.Adler32.<clinit>():void");
    }

    private static native int update(int i, int i2);

    private static native int updateByteBuffer(int i, long j, int i2, int i3);

    private static native int updateBytes(int i, byte[] bArr, int i2, int i3);

    public Adler32() {
        this.adler = 1;
    }

    public void update(int b) {
        this.adler = update(this.adler, b);
    }

    public void update(byte[] b, int off, int len) {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || off > b.length - len) {
            throw new ArrayIndexOutOfBoundsException();
        } else {
            this.adler = updateBytes(this.adler, b, off, len);
        }
    }

    public void update(byte[] b) {
        this.adler = updateBytes(this.adler, b, 0, b.length);
    }

    private void update(ByteBuffer buffer) {
        int pos = buffer.position();
        int limit = buffer.limit();
        if (!-assertionsDisabled) {
            if ((pos <= limit ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        int rem = limit - pos;
        if (rem > 0) {
            if (buffer instanceof DirectBuffer) {
                this.adler = updateByteBuffer(this.adler, ((DirectBuffer) buffer).address(), pos, rem);
            } else if (buffer.hasArray()) {
                this.adler = updateBytes(this.adler, buffer.array(), buffer.arrayOffset() + pos, rem);
            } else {
                byte[] b = new byte[rem];
                buffer.get(b);
                this.adler = updateBytes(this.adler, b, 0, b.length);
            }
            buffer.position(limit);
        }
    }

    public void reset() {
        this.adler = 1;
    }

    public long getValue() {
        return ((long) this.adler) & 4294967295L;
    }
}
