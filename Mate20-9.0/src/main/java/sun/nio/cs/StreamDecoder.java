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

public class StreamDecoder extends Reader {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 8192;
    private static final int MIN_BYTE_BUFFER_SIZE = 32;
    private static volatile boolean channelsAvailable = true;
    private ByteBuffer bb;
    private ReadableByteChannel ch;
    private Charset cs;
    private CharsetDecoder decoder;
    private boolean haveLeftoverChar;
    private InputStream in;
    private volatile boolean isOpen;
    private char leftoverChar;
    private boolean needsFlush;

    private void ensureOpen() throws IOException {
        if (!this.isOpen) {
            throw new IOException("Stream closed");
        }
    }

    public static StreamDecoder forInputStreamReader(InputStream in2, Object lock, String charsetName) throws UnsupportedEncodingException {
        String csn = charsetName;
        if (csn == null) {
            csn = Charset.defaultCharset().name();
        }
        try {
            if (Charset.isSupported(csn)) {
                return new StreamDecoder(in2, lock, Charset.forName(csn));
            }
        } catch (IllegalCharsetNameException e) {
        }
        throw new UnsupportedEncodingException(csn);
    }

    public static StreamDecoder forInputStreamReader(InputStream in2, Object lock, Charset cs2) {
        return new StreamDecoder(in2, lock, cs2);
    }

    public static StreamDecoder forInputStreamReader(InputStream in2, Object lock, CharsetDecoder dec) {
        return new StreamDecoder(in2, lock, dec);
    }

    public static StreamDecoder forDecoder(ReadableByteChannel ch2, CharsetDecoder dec, int minBufferCap) {
        return new StreamDecoder(ch2, dec, minBufferCap);
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

    private int read0() throws IOException {
        synchronized (this.lock) {
            if (this.haveLeftoverChar) {
                this.haveLeftoverChar = $assertionsDisabled;
                char c = this.leftoverChar;
                return c;
            }
            char[] cb = new char[2];
            int n = read(cb, 0, 2);
            if (n == -1) {
                return -1;
            }
            switch (n) {
                case 1:
                    break;
                case 2:
                    this.leftoverChar = cb[1];
                    this.haveLeftoverChar = true;
                    break;
                default:
                    return -1;
            }
            char c2 = cb[0];
            return c2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0036, code lost:
        return 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0046, code lost:
        return r5;
     */
    public int read(char[] cbuf, int offset, int length) throws IOException {
        int off = offset;
        int len = length;
        synchronized (this.lock) {
            ensureOpen();
            if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            } else {
                int n = 0;
                if (this.haveLeftoverChar) {
                    cbuf[off] = this.leftoverChar;
                    off++;
                    len--;
                    this.haveLeftoverChar = $assertionsDisabled;
                    n = 1;
                    if (len == 0 || !implReady()) {
                    }
                }
                if (len == 1) {
                    int c = read0();
                    int i = -1;
                    if (c != -1) {
                        cbuf[off] = (char) c;
                        int i2 = n + 1;
                        return i2;
                    } else if (n != 0) {
                        i = n;
                    }
                } else {
                    int implRead = implRead(cbuf, off, off + len) + n;
                    return implRead;
                }
            }
        }
    }

    public boolean ready() throws IOException {
        boolean z;
        synchronized (this.lock) {
            ensureOpen();
            if (!this.haveLeftoverChar) {
                if (!implReady()) {
                    z = $assertionsDisabled;
                }
            }
            z = true;
        }
        return z;
    }

    public void close() throws IOException {
        synchronized (this.lock) {
            if (this.isOpen) {
                implClose();
                this.isOpen = $assertionsDisabled;
            }
        }
    }

    private boolean isOpen() {
        return this.isOpen;
    }

    private static FileChannel getChannel(FileInputStream in2) {
        if (!channelsAvailable) {
            return null;
        }
        try {
            return in2.getChannel();
        } catch (UnsatisfiedLinkError e) {
            channelsAvailable = $assertionsDisabled;
            return null;
        }
    }

    StreamDecoder(InputStream in2, Object lock, Charset cs2) {
        this(in2, lock, cs2.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE));
    }

    StreamDecoder(InputStream in2, Object lock, CharsetDecoder dec) {
        super(lock);
        this.isOpen = true;
        this.haveLeftoverChar = $assertionsDisabled;
        this.needsFlush = $assertionsDisabled;
        this.cs = dec.charset();
        this.decoder = dec;
        if (this.ch == null) {
            this.in = in2;
            this.ch = null;
            this.bb = ByteBuffer.allocate(8192);
        }
        this.bb.flip();
    }

    StreamDecoder(ReadableByteChannel ch2, CharsetDecoder dec, int mbc) {
        this.isOpen = true;
        this.haveLeftoverChar = $assertionsDisabled;
        this.needsFlush = $assertionsDisabled;
        this.in = null;
        this.ch = ch2;
        this.decoder = dec;
        this.cs = dec.charset();
        int i = 32;
        if (mbc < 0) {
            i = 8192;
        } else if (mbc >= 32) {
            i = mbc;
        }
        this.bb = ByteBuffer.allocate(i);
        this.bb.flip();
    }

    private int readBytes() throws IOException {
        this.bb.compact();
        try {
            if (this.ch != null) {
                int n = ChannelInputStream.read(this.ch, this.bb);
                if (n < 0) {
                    return n;
                }
            } else {
                int lim = this.bb.limit();
                int pos = this.bb.position();
                int n2 = this.in.read(this.bb.array(), this.bb.arrayOffset() + pos, pos <= lim ? lim - pos : 0);
                if (n2 < 0) {
                    this.bb.flip();
                    return n2;
                } else if (n2 != 0) {
                    this.bb.position(pos + n2);
                } else {
                    throw new IOException("Underlying input stream returned zero bytes");
                }
            }
            this.bb.flip();
            return this.bb.remaining();
        } finally {
            this.bb.flip();
        }
    }

    /* access modifiers changed from: package-private */
    public int implRead(char[] cbuf, int off, int end) throws IOException {
        CharBuffer cb = CharBuffer.wrap(cbuf, off, end - off);
        if (cb.position() != 0) {
            cb = cb.slice();
        }
        if (this.needsFlush) {
            CoderResult cr = this.decoder.flush(cb);
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
        boolean eof = $assertionsDisabled;
        while (true) {
            CoderResult cr2 = this.decoder.decode(this.bb, cb, eof);
            if (cr2.isUnderflow()) {
                if (eof || !cb.hasRemaining() || (cb.position() > 0 && !inReady())) {
                    break;
                } else if (readBytes() < 0) {
                    eof = true;
                }
            } else if (cr2.isOverflow() != 0) {
                break;
            } else {
                cr2.throwException();
            }
        }
        if (eof) {
            CoderResult cr3 = this.decoder.flush(cb);
            if (cr3.isOverflow()) {
                this.needsFlush = true;
                return cb.position();
            }
            this.decoder.reset();
            if (!cr3.isUnderflow()) {
                cr3.throwException();
            }
        }
        if (cb.position() != 0 || !eof) {
            return cb.position();
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public String encodingName() {
        if (this.cs instanceof HistoricallyNamedCharset) {
            return ((HistoricallyNamedCharset) this.cs).historicalName();
        }
        return this.cs.name();
    }

    private boolean inReady() {
        boolean z = $assertionsDisabled;
        try {
            if ((this.in != null && this.in.available() > 0) || (this.ch instanceof FileChannel)) {
                z = true;
            }
            return z;
        } catch (IOException e) {
            return $assertionsDisabled;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean implReady() {
        if (this.bb.hasRemaining() || inReady()) {
            return true;
        }
        return $assertionsDisabled;
    }

    /* access modifiers changed from: package-private */
    public void implClose() throws IOException {
        if (this.ch != null) {
            this.ch.close();
        } else {
            this.in.close();
        }
    }
}
