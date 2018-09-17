package android.util;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Base64InputStream extends FilterInputStream {
    private static final int BUFFER_SIZE = 2048;
    private static byte[] EMPTY;
    private final Coder coder;
    private boolean eof;
    private byte[] inputBuffer;
    private int outputEnd;
    private int outputStart;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.Base64InputStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.Base64InputStream.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.util.Base64InputStream.<clinit>():void");
    }

    public Base64InputStream(InputStream in, int flags) {
        this(in, flags, false);
    }

    public Base64InputStream(InputStream in, int flags, boolean encode) {
        super(in);
        this.eof = false;
        this.inputBuffer = new byte[BUFFER_SIZE];
        if (encode) {
            this.coder = new Encoder(flags, null);
        } else {
            this.coder = new Decoder(flags, null);
        }
        this.coder.output = new byte[this.coder.maxOutputSize(BUFFER_SIZE)];
        this.outputStart = 0;
        this.outputEnd = 0;
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    public void reset() {
        throw new UnsupportedOperationException();
    }

    public void close() throws IOException {
        this.in.close();
        this.inputBuffer = null;
    }

    public int available() {
        return this.outputEnd - this.outputStart;
    }

    public long skip(long n) throws IOException {
        if (this.outputStart >= this.outputEnd) {
            refill();
        }
        if (this.outputStart >= this.outputEnd) {
            return 0;
        }
        long bytes = Math.min(n, (long) (this.outputEnd - this.outputStart));
        this.outputStart = (int) (((long) this.outputStart) + bytes);
        return bytes;
    }

    public int read() throws IOException {
        if (this.outputStart >= this.outputEnd) {
            refill();
        }
        if (this.outputStart >= this.outputEnd) {
            return -1;
        }
        byte[] bArr = this.coder.output;
        int i = this.outputStart;
        this.outputStart = i + 1;
        return bArr[i] & MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (this.outputStart >= this.outputEnd) {
            refill();
        }
        if (this.outputStart >= this.outputEnd) {
            return -1;
        }
        int bytes = Math.min(len, this.outputEnd - this.outputStart);
        System.arraycopy(this.coder.output, this.outputStart, b, off, bytes);
        this.outputStart += bytes;
        return bytes;
    }

    private void refill() throws IOException {
        if (!this.eof) {
            boolean success;
            int bytesRead = this.in.read(this.inputBuffer);
            if (bytesRead == -1) {
                this.eof = true;
                success = this.coder.process(EMPTY, 0, 0, true);
            } else {
                success = this.coder.process(this.inputBuffer, 0, bytesRead, false);
            }
            if (success) {
                this.outputEnd = this.coder.op;
                this.outputStart = 0;
                return;
            }
            throw new Base64DataException("bad base-64");
        }
    }
}
