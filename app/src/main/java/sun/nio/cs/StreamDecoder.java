package sun.nio.cs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import sun.nio.ch.ChannelInputStream;
import sun.security.x509.GeneralNameInterface;
import sun.util.calendar.BaseCalendar;

public class StreamDecoder extends Reader {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 8192;
    private static final int MIN_BYTE_BUFFER_SIZE = 32;
    private static volatile boolean channelsAvailable;
    private ByteBuffer bb;
    private ReadableByteChannel ch;
    private Charset cs;
    private CharsetDecoder decoder;
    private boolean haveLeftoverChar;
    private InputStream in;
    private volatile boolean isOpen;
    private char leftoverChar;
    private boolean needsFlush;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.nio.cs.StreamDecoder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.nio.cs.StreamDecoder.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.nio.cs.StreamDecoder.<clinit>():void");
    }

    private void ensureOpen() throws IOException {
        if (!this.isOpen) {
            throw new IOException("Stream closed");
        }
    }

    public static StreamDecoder forInputStreamReader(InputStream in, Object lock, String charsetName) throws UnsupportedEncodingException {
        String csn = charsetName;
        if (charsetName == null) {
            csn = Charset.defaultCharset().name();
        }
        try {
            if (Charset.isSupported(csn)) {
                return new StreamDecoder(in, lock, Charset.forName(csn));
            }
        } catch (IllegalCharsetNameException e) {
        }
        throw new UnsupportedEncodingException(csn);
    }

    public static StreamDecoder forInputStreamReader(InputStream in, Object lock, Charset cs) {
        return new StreamDecoder(in, lock, cs);
    }

    public static StreamDecoder forInputStreamReader(InputStream in, Object lock, CharsetDecoder dec) {
        return new StreamDecoder(in, lock, dec);
    }

    public static StreamDecoder forDecoder(ReadableByteChannel ch, CharsetDecoder dec, int minBufferCap) {
        return new StreamDecoder(ch, dec, minBufferCap);
    }

    public String getEncoding() {
        if (isOpen()) {
            return encodingName();
        }
        return null;
    }

    public int read() throws IOException {
        return read0();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int read0() throws IOException {
        synchronized (this.lock) {
            if (this.haveLeftoverChar) {
                this.haveLeftoverChar = -assertionsDisabled;
                char c = this.leftoverChar;
                return c;
            }
            char[] cb = new char[2];
            int n = read(cb, 0, 2);
            switch (n) {
                case GeneralNameInterface.NAME_DIFF_TYPE /*-1*/:
                    return -1;
                case BaseCalendar.SUNDAY /*1*/:
                    break;
                case BaseCalendar.MONDAY /*2*/:
                    this.leftoverChar = cb[1];
                    this.haveLeftoverChar = true;
                    break;
                default:
                    if (-assertionsDisabled) {
                        return -1;
                    }
                    throw new AssertionError(Integer.valueOf(n));
            }
        }
    }

    public int read(char[] cbuf, int offset, int length) throws IOException {
        int off = offset;
        int len = length;
        synchronized (this.lock) {
            ensureOpen();
            if (offset >= 0 && offset <= cbuf.length && length >= 0) {
                if (offset + length <= cbuf.length && offset + length >= 0) {
                    if (length == 0) {
                        return 0;
                    }
                    int n = 0;
                    if (this.haveLeftoverChar) {
                        cbuf[offset] = this.leftoverChar;
                        off = offset + 1;
                        len = length - 1;
                        this.haveLeftoverChar = -assertionsDisabled;
                        n = 1;
                        if (len == 0 || !implReady()) {
                            return 1;
                        }
                    }
                    int i;
                    if (len == 1) {
                        int c = read0();
                        if (c == -1) {
                            if (n == 0) {
                                n = -1;
                            }
                            return n;
                        }
                        cbuf[off] = (char) c;
                        i = n + 1;
                        return i;
                    }
                    i = implRead(cbuf, off, off + len) + n;
                    return i;
                }
            }
            throw new IndexOutOfBoundsException();
        }
    }

    public boolean ready() throws IOException {
        boolean implReady;
        synchronized (this.lock) {
            ensureOpen();
            implReady = !this.haveLeftoverChar ? implReady() : true;
        }
        return implReady;
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

    private static FileChannel getChannel(FileInputStream in) {
        if (!channelsAvailable) {
            return null;
        }
        try {
            return in.getChannel();
        } catch (UnsatisfiedLinkError e) {
            channelsAvailable = -assertionsDisabled;
            return null;
        }
    }

    StreamDecoder(InputStream in, Object lock, Charset cs) {
        this(in, lock, cs.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE));
    }

    StreamDecoder(InputStream in, Object lock, CharsetDecoder dec) {
        super(lock);
        this.isOpen = true;
        this.haveLeftoverChar = -assertionsDisabled;
        this.needsFlush = -assertionsDisabled;
        this.cs = dec.charset();
        this.decoder = dec;
        if (this.ch == null) {
            this.in = in;
            this.ch = null;
            this.bb = ByteBuffer.allocate(DEFAULT_BYTE_BUFFER_SIZE);
        }
        this.bb.flip();
    }

    StreamDecoder(ReadableByteChannel ch, CharsetDecoder dec, int mbc) {
        this.isOpen = true;
        this.haveLeftoverChar = -assertionsDisabled;
        this.needsFlush = -assertionsDisabled;
        this.in = null;
        this.ch = ch;
        this.decoder = dec;
        this.cs = dec.charset();
        if (mbc < 0) {
            mbc = DEFAULT_BYTE_BUFFER_SIZE;
        } else if (mbc < MIN_BYTE_BUFFER_SIZE) {
            mbc = MIN_BYTE_BUFFER_SIZE;
        }
        this.bb = ByteBuffer.allocate(mbc);
        this.bb.flip();
    }

    private int readBytes() throws IOException {
        Object obj = 1;
        this.bb.compact();
        try {
            int rem;
            int n;
            if (this.ch != null) {
                n = ChannelInputStream.read(this.ch, this.bb);
                if (n < 0) {
                    return n;
                }
            }
            int lim = this.bb.limit();
            int pos = this.bb.position();
            if (!-assertionsDisabled) {
                if ((pos <= lim ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            rem = pos <= lim ? lim - pos : 0;
            if (!-assertionsDisabled) {
                Object obj2;
                if (rem > 0) {
                    obj2 = 1;
                } else {
                    obj2 = null;
                }
                if (obj2 == null) {
                    throw new AssertionError();
                }
            }
            n = this.in.read(this.bb.array(), this.bb.arrayOffset() + pos, rem);
            if (n < 0) {
                this.bb.flip();
                return n;
            } else if (n == 0) {
                throw new IOException("Underlying input stream returned zero bytes");
            } else {
                if (!-assertionsDisabled) {
                    if ((n <= rem ? 1 : null) == null) {
                        throw new AssertionError("n = " + n + ", rem = " + rem);
                    }
                }
                this.bb.position(pos + n);
            }
            this.bb.flip();
            rem = this.bb.remaining();
            if (!-assertionsDisabled) {
                if (rem == 0) {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError(Integer.valueOf(rem));
                }
            }
            return rem;
        } finally {
            this.bb.flip();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int implRead(char[] cbuf, int off, int end) throws IOException {
        CoderResult cr;
        boolean z = -assertionsDisabled;
        if (!-assertionsDisabled) {
            if (!(end - off > 1)) {
                throw new AssertionError();
            }
        }
        CharBuffer cb = CharBuffer.wrap(cbuf, off, end - off);
        if (cb.position() != 0) {
            cb = cb.slice();
        }
        if (this.needsFlush) {
            cr = this.decoder.flush(cb);
            if (cr.isOverflow()) {
                return cb.position();
            }
            if (!cr.isUnderflow()) {
                cr.throwException();
            } else if (cb.position() == 0) {
                return -1;
            } else {
                return cb.position();
            }
        }
        boolean eof = -assertionsDisabled;
        while (true) {
            cr = this.decoder.decode(this.bb, cb, eof);
            if (cr.isUnderflow()) {
                if (!eof && cb.hasRemaining() && (cb.position() <= 0 || inReady())) {
                    if (readBytes() < 0) {
                        eof = true;
                    }
                }
            } else if (cr.isOverflow()) {
                break;
            } else {
                cr.throwException();
            }
        }
        if (eof) {
            cr = this.decoder.flush(cb);
            if (cr.isOverflow()) {
                this.needsFlush = true;
                return cb.position();
            }
            this.decoder.reset();
            if (!cr.isUnderflow()) {
                cr.throwException();
            }
        }
        if (cb.position() == 0) {
            if (eof) {
                return -1;
            }
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        return cb.position();
    }

    String encodingName() {
        if (this.cs instanceof HistoricallyNamedCharset) {
            return ((HistoricallyNamedCharset) this.cs).historicalName();
        }
        return this.cs.name();
    }

    private boolean inReady() {
        try {
            boolean z;
            if (this.in == null || this.in.available() <= 0) {
                z = this.ch instanceof FileChannel;
            } else {
                z = true;
            }
            return z;
        } catch (IOException e) {
            return -assertionsDisabled;
        }
    }

    boolean implReady() {
        return !this.bb.hasRemaining() ? inReady() : true;
    }

    void implClose() throws IOException {
        if (this.ch != null) {
            this.ch.close();
        } else {
            this.in.close();
        }
    }
}
