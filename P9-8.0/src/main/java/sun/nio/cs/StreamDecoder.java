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
    static final /* synthetic */ boolean -assertionsDisabled = (StreamDecoder.class.desiredAssertionStatus() ^ 1);
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

    private int read0() throws IOException {
        synchronized (this.lock) {
            char c;
            if (this.haveLeftoverChar) {
                this.haveLeftoverChar = -assertionsDisabled;
                c = this.leftoverChar;
                return c;
            }
            char[] cb = new char[2];
            int n = read(cb, 0, 2);
            switch (n) {
                case -1:
                    return -1;
                case 1:
                    break;
                case 2:
                    this.leftoverChar = cb[1];
                    this.haveLeftoverChar = true;
                    break;
                default:
                    if (-assertionsDisabled) {
                        return -1;
                    }
                    throw new AssertionError(Integer.valueOf(n));
            }
            c = cb[0];
            return c;
        }
    }

    /* JADX WARNING: Missing block: B:32:0x0044, code:
            return 1;
     */
    /* JADX WARNING: Missing block: B:40:0x0051, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                        if (len == 0 || (implReady() ^ 1) != 0) {
                        }
                    }
                    int i;
                    if (len == 1) {
                        int c = read0();
                        if (c != -1) {
                            cbuf[off] = (char) c;
                            i = n + 1;
                            return i;
                        } else if (n == 0) {
                            n = -1;
                        }
                    } else {
                        i = implRead(cbuf, off, off + len) + n;
                        return i;
                    }
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
            this.bb = ByteBuffer.allocate(8192);
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
            mbc = 8192;
        } else if (mbc < 32) {
            mbc = 32;
        }
        this.bb = ByteBuffer.allocate(mbc);
        this.bb.flip();
    }

    private int readBytes() throws IOException {
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
            if (-assertionsDisabled || pos <= lim) {
                rem = pos <= lim ? lim - pos : 0;
                if (-assertionsDisabled || rem > 0) {
                    n = this.in.read(this.bb.array(), this.bb.arrayOffset() + pos, rem);
                    if (n < 0) {
                        this.bb.flip();
                        return n;
                    } else if (n == 0) {
                        throw new IOException("Underlying input stream returned zero bytes");
                    } else if (-assertionsDisabled || n <= rem) {
                        this.bb.position(pos + n);
                    } else {
                        throw new AssertionError("n = " + n + ", rem = " + rem);
                    }
                }
                throw new AssertionError();
            }
            throw new AssertionError();
            this.bb.flip();
            rem = this.bb.remaining();
            if (-assertionsDisabled || rem != 0) {
                return rem;
            }
            throw new AssertionError(Integer.valueOf(rem));
        } finally {
            this.bb.flip();
        }
    }

    int implRead(char[] cbuf, int off, int end) throws IOException {
        if (-assertionsDisabled || end - off > 1) {
            CoderResult cr;
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
                    if (eof || !cb.hasRemaining() || (cb.position() > 0 && (inReady() ^ 1) != 0)) {
                        break;
                    } else if (readBytes() < 0) {
                        eof = true;
                    }
                } else if (!cr.isOverflow()) {
                    cr.throwException();
                } else if (!-assertionsDisabled && cb.position() <= 0) {
                    throw new AssertionError();
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
        throw new AssertionError();
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
