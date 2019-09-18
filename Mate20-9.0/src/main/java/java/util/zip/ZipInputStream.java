package java.util.zip;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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
        super(new PushbackInputStream(in, 512), new Inflater(true), 512);
        this.crc = new CRC32();
        this.tmpbuf = new byte[512];
        this.closed = false;
        this.entryEOF = false;
        this.b = new byte[256];
        if (in == null) {
            throw new NullPointerException("in is null");
        } else if (charset != null) {
            this.zc = ZipCoder.get(charset);
        } else {
            throw new NullPointerException("charset is null");
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
        if (this.entry.method == 0 || this.entry.method == 8) {
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

    public int read(byte[] b2, int off, int len) throws IOException {
        ensureOpen();
        if (off < 0 || len < 0 || off > b2.length - len) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        } else {
            if (this.entry == null) {
                return -1;
            }
            int i = this.entry.method;
            if (i != 0) {
                if (i == 8) {
                    int len2 = super.read(b2, off, len);
                    if (len2 == -1) {
                        readEnd(this.entry);
                        this.entryEOF = true;
                        this.entry = null;
                    } else {
                        this.crc.update(b2, off, len2);
                        this.remaining -= (long) len2;
                    }
                    return len2;
                }
                throw new ZipException("invalid compression method");
            } else if (this.remaining <= 0) {
                this.entryEOF = true;
                this.entry = null;
                return -1;
            } else {
                if (((long) len) > this.remaining) {
                    len = (int) this.remaining;
                }
                int len3 = this.in.read(b2, off, len);
                if (len3 != -1) {
                    this.crc.update(b2, off, len3);
                    this.remaining -= (long) len3;
                    if (this.remaining != 0 || this.entry.crc == this.crc.getValue()) {
                        return len3;
                    }
                    throw new ZipException("invalid entry CRC (expected 0x" + Long.toHexString(this.entry.crc) + " but got 0x" + Long.toHexString(this.crc.getValue()) + ")");
                }
                throw new ZipException("unexpected EOF");
            }
        }
    }

    public long skip(long n) throws IOException {
        if (n >= 0) {
            ensureOpen();
            int max = (int) Math.min(n, 2147483647L);
            int total = 0;
            while (true) {
                if (total >= max) {
                    break;
                }
                int len = max - total;
                if (len > this.tmpbuf.length) {
                    len = this.tmpbuf.length;
                }
                int len2 = read(this.tmpbuf, 0, len);
                if (len2 == -1) {
                    this.entryEOF = true;
                    break;
                }
                total += len2;
            }
            return (long) total;
        }
        throw new IllegalArgumentException("negative skip length");
    }

    public void close() throws IOException {
        if (!this.closed) {
            super.close();
            this.closed = true;
        }
    }

    private ZipEntry readLOC() throws IOException {
        String str;
        try {
            boolean z = false;
            readFully(this.tmpbuf, 0, 30);
            if (ZipUtils.get32(this.tmpbuf, 0) != ZipConstants.LOCSIG) {
                return null;
            }
            this.flag = ZipUtils.get16(this.tmpbuf, 6);
            int len = ZipUtils.get16(this.tmpbuf, 26);
            int blen = this.b.length;
            if (len > blen) {
                do {
                    blen *= 2;
                } while (len > blen);
                this.b = new byte[blen];
            }
            readFully(this.b, 0, len);
            if ((this.flag & 2048) != 0) {
                str = this.zc.toStringUTF8(this.b, len);
            } else {
                str = this.zc.toString(this.b, len);
            }
            ZipEntry e = createZipEntry(str);
            if ((this.flag & 1) != 1) {
                e.method = ZipUtils.get16(this.tmpbuf, 8);
                e.xdostime = ZipUtils.get32(this.tmpbuf, 10);
                if ((this.flag & 8) != 8) {
                    e.crc = ZipUtils.get32(this.tmpbuf, 14);
                    e.csize = ZipUtils.get32(this.tmpbuf, 18);
                    e.size = ZipUtils.get32(this.tmpbuf, 22);
                } else if (e.method != 8) {
                    throw new ZipException("only DEFLATED entries can have EXT descriptor");
                }
                int len2 = ZipUtils.get16(this.tmpbuf, 28);
                if (len2 > 0) {
                    byte[] extra = new byte[len2];
                    readFully(extra, 0, len2);
                    if (e.csize == 4294967295L || e.size == 4294967295L) {
                        z = true;
                    }
                    e.setExtra0(extra, z);
                }
                return e;
            }
            throw new ZipException("encrypted ZIP entry not supported");
        } catch (EOFException e2) {
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }

    private void readEnd(ZipEntry e) throws IOException {
        int n = this.inf.getRemaining();
        if (n > 0) {
            ((PushbackInputStream) this.in).unread(this.buf, this.len - n, n);
        }
        if ((this.flag & 8) == 8) {
            if (this.inf.getBytesWritten() > 4294967295L || this.inf.getBytesRead() > 4294967295L) {
                readFully(this.tmpbuf, 0, 24);
                long sig = ZipUtils.get32(this.tmpbuf, 0);
                if (sig != ZipConstants.EXTSIG) {
                    e.crc = sig;
                    e.csize = ZipUtils.get64(this.tmpbuf, 4);
                    e.size = ZipUtils.get64(this.tmpbuf, 12);
                    ((PushbackInputStream) this.in).unread(this.tmpbuf, 19, 4);
                } else {
                    e.crc = ZipUtils.get32(this.tmpbuf, 4);
                    e.csize = ZipUtils.get64(this.tmpbuf, 8);
                    e.size = ZipUtils.get64(this.tmpbuf, 16);
                }
            } else {
                readFully(this.tmpbuf, 0, 16);
                long sig2 = ZipUtils.get32(this.tmpbuf, 0);
                if (sig2 != ZipConstants.EXTSIG) {
                    e.crc = sig2;
                    e.csize = ZipUtils.get32(this.tmpbuf, 4);
                    e.size = ZipUtils.get32(this.tmpbuf, 8);
                    ((PushbackInputStream) this.in).unread(this.tmpbuf, 11, 4);
                } else {
                    e.crc = ZipUtils.get32(this.tmpbuf, 4);
                    e.csize = ZipUtils.get32(this.tmpbuf, 8);
                    e.size = ZipUtils.get32(this.tmpbuf, 12);
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

    private void readFully(byte[] b2, int off, int len) throws IOException {
        while (len > 0) {
            int n = this.in.read(b2, off, len);
            if (n != -1) {
                off += n;
                len -= n;
            } else {
                throw new EOFException();
            }
        }
    }
}
