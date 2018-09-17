package sun.nio.cs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;

public class StreamEncoder extends Writer {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 8192;
    private ByteBuffer bb;
    private WritableByteChannel ch;
    private Charset cs;
    private CharsetEncoder encoder;
    private boolean haveLeftoverChar;
    private volatile boolean isOpen;
    private CharBuffer lcb;
    private char leftoverChar;
    private final OutputStream out;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.cs.StreamEncoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.cs.StreamEncoder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.cs.StreamEncoder.<clinit>():void");
    }

    private void ensureOpen() throws IOException {
        if (!this.isOpen) {
            throw new IOException("Stream closed");
        }
    }

    public static StreamEncoder forOutputStreamWriter(OutputStream out, Object lock, String charsetName) throws UnsupportedEncodingException {
        String csn = charsetName;
        if (charsetName == null) {
            csn = Charset.defaultCharset().name();
        }
        try {
            if (Charset.isSupported(csn)) {
                return new StreamEncoder(out, lock, Charset.forName(csn));
            }
        } catch (IllegalCharsetNameException e) {
        }
        throw new UnsupportedEncodingException(csn);
    }

    public static StreamEncoder forOutputStreamWriter(OutputStream out, Object lock, Charset cs) {
        return new StreamEncoder(out, lock, cs);
    }

    public static StreamEncoder forOutputStreamWriter(OutputStream out, Object lock, CharsetEncoder enc) {
        return new StreamEncoder(out, lock, enc);
    }

    public static StreamEncoder forEncoder(WritableByteChannel ch, CharsetEncoder enc, int minBufferCap) {
        return new StreamEncoder(ch, enc, minBufferCap);
    }

    public String getEncoding() {
        if (isOpen()) {
            return encodingName();
        }
        return null;
    }

    public void flushBuffer() throws IOException {
        synchronized (this.lock) {
            if (isOpen()) {
                implFlushBuffer();
            } else {
                throw new IOException("Stream closed");
            }
        }
    }

    public void write(int c) throws IOException {
        write(new char[]{(char) c}, 0, 1);
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            if (off >= 0 && off <= cbuf.length && len >= 0) {
                if (off + len <= cbuf.length && off + len >= 0) {
                    if (len == 0) {
                        return;
                    }
                    implWrite(cbuf, off, len);
                    return;
                }
            }
            throw new IndexOutOfBoundsException();
        }
    }

    public void write(String str, int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        char[] cbuf = new char[len];
        str.getChars(off, off + len, cbuf, 0);
        write(cbuf, 0, len);
    }

    public void flush() throws IOException {
        synchronized (this.lock) {
            ensureOpen();
            implFlush();
        }
    }

    public void close() throws IOException {
        synchronized (this.lock) {
            if (this.isOpen) {
                implClose();
                this.isOpen = -assertionsDisabled;
                return;
            }
        }
    }

    private boolean isOpen() {
        return this.isOpen;
    }

    private StreamEncoder(OutputStream out, Object lock, Charset cs) {
        this(out, lock, cs.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE));
    }

    private StreamEncoder(OutputStream out, Object lock, CharsetEncoder enc) {
        super(lock);
        this.isOpen = true;
        this.haveLeftoverChar = -assertionsDisabled;
        this.lcb = null;
        this.out = out;
        this.ch = null;
        this.cs = enc.charset();
        this.encoder = enc;
        if (this.ch == null) {
            this.bb = ByteBuffer.allocate(DEFAULT_BYTE_BUFFER_SIZE);
        }
    }

    private StreamEncoder(WritableByteChannel ch, CharsetEncoder enc, int mbc) {
        this.isOpen = true;
        this.haveLeftoverChar = -assertionsDisabled;
        this.lcb = null;
        this.out = null;
        this.ch = ch;
        this.cs = enc.charset();
        this.encoder = enc;
        if (mbc < 0) {
            mbc = DEFAULT_BYTE_BUFFER_SIZE;
        }
        this.bb = ByteBuffer.allocate(mbc);
    }

    private void writeBytes() throws IOException {
        int rem = 0;
        this.bb.flip();
        int lim = this.bb.limit();
        int pos = this.bb.position();
        if (!-assertionsDisabled) {
            if ((pos <= lim ? 1 : 0) == 0) {
                throw new AssertionError();
            }
        }
        if (pos <= lim) {
            rem = lim - pos;
        }
        if (rem > 0) {
            if (this.ch == null) {
                this.out.write(this.bb.array(), this.bb.arrayOffset() + pos, rem);
            } else if (!(this.ch.write(this.bb) == rem || -assertionsDisabled)) {
                throw new AssertionError(Integer.valueOf(rem));
            }
        }
        this.bb.clear();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void flushLeftoverChar(CharBuffer cb, boolean endOfInput) throws IOException {
        if (this.haveLeftoverChar || endOfInput) {
            if (this.lcb == null) {
                this.lcb = CharBuffer.allocate(2);
            } else {
                this.lcb.clear();
            }
            if (this.haveLeftoverChar) {
                this.lcb.put(this.leftoverChar);
            }
            if (cb != null && cb.hasRemaining()) {
                this.lcb.put(cb.get());
            }
            this.lcb.flip();
            while (true) {
                if (!this.lcb.hasRemaining() && !endOfInput) {
                    break;
                }
                CoderResult cr = this.encoder.encode(this.lcb, this.bb, endOfInput);
                if (cr.isUnderflow()) {
                    break;
                } else if (cr.isOverflow()) {
                    if (!-assertionsDisabled) {
                        if (!(this.bb.position() > 0 ? true : -assertionsDisabled)) {
                            break;
                        }
                    }
                    writeBytes();
                } else {
                    cr.throwException();
                }
            }
            this.haveLeftoverChar = -assertionsDisabled;
        }
    }

    void implWrite(char[] cbuf, int off, int len) throws IOException {
        boolean z = -assertionsDisabled;
        CharBuffer cb = CharBuffer.wrap(cbuf, off, len);
        if (this.haveLeftoverChar) {
            flushLeftoverChar(cb, -assertionsDisabled);
        }
        while (cb.hasRemaining()) {
            CoderResult cr = this.encoder.encode(cb, this.bb, -assertionsDisabled);
            if (cr.isUnderflow()) {
                if (!-assertionsDisabled) {
                    if (cb.remaining() <= 1) {
                        z = true;
                    }
                    if (!z) {
                        throw new AssertionError(Integer.valueOf(cb.remaining()));
                    }
                }
                if (cb.remaining() == 1) {
                    this.haveLeftoverChar = true;
                    this.leftoverChar = cb.get();
                    return;
                }
                return;
            } else if (cr.isOverflow()) {
                if (!-assertionsDisabled) {
                    if (!(this.bb.position() > 0 ? true : -assertionsDisabled)) {
                        throw new AssertionError();
                    }
                }
                writeBytes();
            } else {
                cr.throwException();
            }
        }
    }

    void implFlushBuffer() throws IOException {
        if (this.bb.position() > 0) {
            writeBytes();
        }
    }

    void implFlush() throws IOException {
        implFlushBuffer();
        if (this.out != null) {
            this.out.flush();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void implClose() throws IOException {
        flushLeftoverChar(null, true);
        while (true) {
            try {
                CoderResult cr = this.encoder.flush(this.bb);
                if (cr.isUnderflow()) {
                    break;
                } else if (cr.isOverflow()) {
                    if (!-assertionsDisabled) {
                        boolean z;
                        if (this.bb.position() > 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        if (!z) {
                            break;
                        }
                    }
                    writeBytes();
                } else {
                    cr.throwException();
                }
            } catch (IOException x) {
                this.encoder.reset();
                throw x;
            }
        }
        throw new AssertionError();
    }

    String encodingName() {
        if (this.cs instanceof HistoricallyNamedCharset) {
            return ((HistoricallyNamedCharset) this.cs).historicalName();
        }
        return this.cs.name();
    }
}
