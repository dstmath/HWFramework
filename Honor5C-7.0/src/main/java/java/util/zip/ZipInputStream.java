package java.util.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import sun.security.x509.GeneralNameInterface;

public class ZipInputStream extends InflaterInputStream implements ZipConstants {
    private static final int DEFLATED = 8;
    private static final int STORED = 0;
    private byte[] b;
    private boolean closed;
    private CRC32 crc;
    private ZipEntry entry;
    private boolean entryEOF;
    private int flag;
    private long remaining;
    private byte[] tmpbuf;
    private ZipCoder zc;

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("Stream closed");
        }
    }

    public ZipInputStream(InputStream in) {
        this(in, StandardCharsets.UTF_8);
    }

    public ZipInputStream(InputStream in, Charset charset) {
        super(new PushbackInputStream(in, Modifier.INTERFACE), new Inflater(true), Modifier.INTERFACE);
        this.crc = new CRC32();
        this.tmpbuf = new byte[Modifier.INTERFACE];
        this.closed = false;
        this.entryEOF = false;
        this.b = new byte[Record.maxPadding];
        if (in == null) {
            throw new NullPointerException("in is null");
        } else if (charset == null) {
            throw new NullPointerException("charset is null");
        } else {
            this.zc = ZipCoder.get(charset);
        }
    }

    public ZipEntry getNextEntry() throws IOException {
        ensureOpen();
        if (this.entry != null) {
            closeEntry();
        }
        this.crc.reset();
        this.inf.reset();
        ZipEntry readLOC = readLOC();
        this.entry = readLOC;
        if (readLOC == null) {
            return null;
        }
        if (this.entry.method == 0 || this.entry.method == DEFLATED) {
            this.remaining = this.entry.size;
        }
        this.entryEOF = false;
        return this.entry;
    }

    public void closeEntry() throws IOException {
        ensureOpen();
        do {
        } while (read(this.tmpbuf, 0, this.tmpbuf.length) != -1);
        this.entryEOF = true;
    }

    public int available() throws IOException {
        ensureOpen();
        if (this.entryEOF || (this.entry != null && this.remaining == 0)) {
            return 0;
        }
        return 1;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (off < 0 || len < 0 || off > b.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            if (this.entry == null) {
                return -1;
            }
            switch (this.entry.method) {
                case GeneralNameInterface.NAME_MATCH /*0*/:
                    if (this.remaining <= 0) {
                        this.entryEOF = true;
                        this.entry = null;
                        return -1;
                    }
                    if (((long) len) > this.remaining) {
                        len = (int) this.remaining;
                    }
                    len = this.in.read(b, off, len);
                    if (len == -1) {
                        throw new ZipException("unexpected EOF");
                    }
                    this.crc.update(b, off, len);
                    this.remaining -= (long) len;
                    if (this.remaining != 0 || this.entry.crc == this.crc.getValue()) {
                        return len;
                    }
                    throw new ZipException("invalid entry CRC (expected 0x" + Long.toHexString(this.entry.crc) + " but got 0x" + Long.toHexString(this.crc.getValue()) + ")");
                case DEFLATED /*8*/:
                    len = super.read(b, off, len);
                    if (len == -1) {
                        readEnd(this.entry);
                        this.entryEOF = true;
                        this.entry = null;
                    } else {
                        this.crc.update(b, off, len);
                        this.remaining -= (long) len;
                    }
                    return len;
                default:
                    throw new ZipException("invalid compression method");
            }
        }
    }

    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
        ensureOpen();
        int max = (int) Math.min(n, 2147483647L);
        int total = 0;
        while (total < max) {
            int len = max - total;
            if (len > this.tmpbuf.length) {
                len = this.tmpbuf.length;
            }
            len = read(this.tmpbuf, 0, len);
            if (len == -1) {
                this.entryEOF = true;
                break;
            }
            total += len;
        }
        return (long) total;
    }

    public void close() throws IOException {
        if (!this.closed) {
            super.close();
            this.closed = true;
        }
    }

    private ZipEntry readLOC() throws IOException {
        try {
            readFully(this.tmpbuf, 0, 30);
            if (get32(this.tmpbuf, 0) != ZipConstants.LOCSIG) {
                return null;
            }
            String toStringUTF8;
            this.flag = get16(this.tmpbuf, 6);
            int len = get16(this.tmpbuf, 26);
            int blen = this.b.length;
            if (len > blen) {
                do {
                    blen *= 2;
                } while (len > blen);
                this.b = new byte[blen];
            }
            readFully(this.b, 0, len);
            if ((this.flag & Modifier.STRICT) != 0) {
                toStringUTF8 = this.zc.toStringUTF8(this.b, len);
            } else {
                toStringUTF8 = this.zc.toString(this.b, len);
            }
            ZipEntry e = createZipEntry(toStringUTF8);
            if ((this.flag & 1) == 1) {
                throw new ZipException("encrypted ZIP entry not supported");
            }
            e.method = get16(this.tmpbuf, DEFLATED);
            e.time = get32(this.tmpbuf, 10);
            if ((this.flag & DEFLATED) != DEFLATED) {
                e.crc = get32(this.tmpbuf, 14);
                e.csize = get32(this.tmpbuf, 18);
                e.size = get32(this.tmpbuf, 22);
            } else if (e.method != DEFLATED) {
                throw new ZipException("only DEFLATED entries can have EXT descriptor");
            }
            len = get16(this.tmpbuf, 28);
            if (len > 0) {
                byte[] bb = new byte[len];
                readFully(bb, 0, len);
                e.setExtra(bb);
                if (e.csize == 4294967295L || e.size == 4294967295L) {
                    int off = 0;
                    while (off + 4 < len) {
                        int sz = get16(bb, off + 2);
                        if (get16(bb, off) == 1) {
                            off += 4;
                            if (sz < 16 || off + sz > len) {
                                return e;
                            }
                            e.size = get64(bb, off);
                            e.csize = get64(bb, off + DEFLATED);
                        } else {
                            off += sz + 4;
                        }
                    }
                }
            }
            return e;
        } catch (EOFException e2) {
            return null;
        }
    }

    protected ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }

    private void readEnd(ZipEntry e) throws IOException {
        int n = this.inf.getRemaining();
        if (n > 0) {
            ((PushbackInputStream) this.in).unread(this.buf, this.len - n, n);
        }
        if ((this.flag & DEFLATED) == DEFLATED) {
            long sig;
            if (this.inf.getBytesWritten() > 4294967295L || this.inf.getBytesRead() > 4294967295L) {
                readFully(this.tmpbuf, 0, 24);
                sig = get32(this.tmpbuf, 0);
                if (sig != ZipConstants.EXTSIG) {
                    e.crc = sig;
                    e.csize = get64(this.tmpbuf, 4);
                    e.size = get64(this.tmpbuf, 12);
                    ((PushbackInputStream) this.in).unread(this.tmpbuf, 19, 4);
                } else {
                    e.crc = get32(this.tmpbuf, 4);
                    e.csize = get64(this.tmpbuf, DEFLATED);
                    e.size = get64(this.tmpbuf, 16);
                }
            } else {
                readFully(this.tmpbuf, 0, 16);
                sig = get32(this.tmpbuf, 0);
                if (sig != ZipConstants.EXTSIG) {
                    e.crc = sig;
                    e.csize = get32(this.tmpbuf, 4);
                    e.size = get32(this.tmpbuf, DEFLATED);
                    ((PushbackInputStream) this.in).unread(this.tmpbuf, 11, 4);
                } else {
                    e.crc = get32(this.tmpbuf, 4);
                    e.csize = get32(this.tmpbuf, DEFLATED);
                    e.size = get32(this.tmpbuf, 12);
                }
            }
        }
        if (e.size != this.inf.getBytesWritten()) {
            throw new ZipException("invalid entry size (expected " + e.size + " but got " + this.inf.getBytesWritten() + " bytes)");
        } else if (e.csize != this.inf.getBytesRead()) {
            throw new ZipException("invalid entry compressed size (expected " + e.csize + " but got " + this.inf.getBytesRead() + " bytes)");
        } else if (e.crc != this.crc.getValue()) {
            throw new ZipException("invalid entry CRC (expected 0x" + Long.toHexString(e.crc) + " but got 0x" + Long.toHexString(this.crc.getValue()) + ")");
        }
    }

    private void readFully(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int n = this.in.read(b, off, len);
            if (n == -1) {
                throw new EOFException();
            }
            off += n;
            len -= n;
        }
    }

    private static final int get16(byte[] b, int off) {
        return (b[off] & 255) | ((b[off + 1] & 255) << DEFLATED);
    }

    private static final long get32(byte[] b, int off) {
        return (((long) get16(b, off)) | (((long) get16(b, off + 2)) << 16)) & 4294967295L;
    }

    private static final long get64(byte[] b, int off) {
        return get32(b, off) | (get32(b, off + 4) << 32);
    }
}
