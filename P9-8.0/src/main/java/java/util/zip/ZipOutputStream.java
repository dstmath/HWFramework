package java.util.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Vector;

public class ZipOutputStream extends DeflaterOutputStream implements ZipConstants {
    public static final int DEFLATED = 8;
    public static final int STORED = 0;
    private static final boolean inhibitZip64 = false;
    private boolean closed;
    private byte[] comment;
    private CRC32 crc;
    private XEntry current;
    private boolean finished;
    private long locoff;
    private int method;
    private HashSet<String> names;
    private long written;
    private Vector<XEntry> xentries;
    private final ZipCoder zc;

    private static class XEntry {
        final ZipEntry entry;
        final long offset;

        public XEntry(ZipEntry entry, long offset) {
            this.entry = entry;
            this.offset = offset;
        }
    }

    private static int version(ZipEntry e) throws ZipException {
        switch (e.method) {
            case 0:
                return 10;
            case 8:
                return 20;
            default:
                throw new ZipException("unsupported compression method");
        }
    }

    private void ensureOpen() throws IOException {
        if (this.closed) {
            throw new IOException("Stream closed");
        }
    }

    public ZipOutputStream(OutputStream out) {
        this(out, StandardCharsets.UTF_8);
    }

    public ZipOutputStream(OutputStream out, Charset charset) {
        super(out, new Deflater(-1, true));
        this.xentries = new Vector();
        this.names = new HashSet();
        this.crc = new CRC32();
        this.written = 0;
        this.locoff = 0;
        this.method = 8;
        this.closed = false;
        if (charset == null) {
            throw new NullPointerException("charset is null");
        }
        this.zc = ZipCoder.get(charset);
        this.usesDefaultDeflater = true;
    }

    public void setComment(String comment) {
        if (comment != null) {
            this.comment = this.zc.getBytes(comment);
            if (this.comment.length > 65535) {
                throw new IllegalArgumentException("ZIP file comment too long.");
            }
        }
    }

    public void setMethod(int method) {
        if (method == 8 || method == 0) {
            this.method = method;
            return;
        }
        throw new IllegalArgumentException("invalid compression method");
    }

    public void setLevel(int level) {
        this.def.setLevel(level);
    }

    public void putNextEntry(ZipEntry e) throws IOException {
        ensureOpen();
        if (this.current != null) {
            closeEntry();
        }
        if (e.xdostime == -1) {
            e.setTime(System.currentTimeMillis());
        }
        if (e.method == -1) {
            e.method = this.method;
        }
        e.flag = 0;
        switch (e.method) {
            case 0:
                if (e.size == -1) {
                    e.size = e.csize;
                } else if (e.csize == -1) {
                    e.csize = e.size;
                } else if (e.size != e.csize) {
                    throw new ZipException("STORED entry where compressed != uncompressed size");
                }
                if (e.size == -1 || e.crc == -1) {
                    throw new ZipException("STORED entry missing size, compressed size, or crc-32");
                }
            case 8:
                if (e.size == -1 || e.csize == -1 || e.crc == -1) {
                    e.flag = 8;
                    break;
                }
            default:
                throw new ZipException("unsupported compression method");
        }
        if (this.names.add(e.name)) {
            if (this.zc.isUTF8()) {
                e.flag |= 2048;
            }
            this.current = new XEntry(e, this.written);
            this.xentries.add(this.current);
            writeLOC(this.current);
            return;
        }
        throw new ZipException("duplicate entry: " + e.name);
    }

    public void closeEntry() throws IOException {
        ensureOpen();
        if (this.current != null) {
            ZipEntry e = this.current.entry;
            switch (e.method) {
                case 0:
                    if (e.size != this.written - this.locoff) {
                        throw new ZipException("invalid entry size (expected " + e.size + " but got " + (this.written - this.locoff) + " bytes)");
                    } else if (e.crc != this.crc.getValue()) {
                        throw new ZipException("invalid entry crc-32 (expected 0x" + Long.toHexString(e.crc) + " but got 0x" + Long.toHexString(this.crc.getValue()) + ")");
                    }
                    break;
                case 8:
                    this.def.finish();
                    while (!this.def.finished()) {
                        deflate();
                    }
                    if ((e.flag & 8) != 0) {
                        e.size = this.def.getBytesRead();
                        e.csize = this.def.getBytesWritten();
                        e.crc = this.crc.getValue();
                        writeEXT(e);
                    } else if (e.size != this.def.getBytesRead()) {
                        throw new ZipException("invalid entry size (expected " + e.size + " but got " + this.def.getBytesRead() + " bytes)");
                    } else if (e.csize != this.def.getBytesWritten()) {
                        throw new ZipException("invalid entry compressed size (expected " + e.csize + " but got " + this.def.getBytesWritten() + " bytes)");
                    } else if (e.crc != this.crc.getValue()) {
                        throw new ZipException("invalid entry CRC-32 (expected 0x" + Long.toHexString(e.crc) + " but got 0x" + Long.toHexString(this.crc.getValue()) + ")");
                    }
                    this.def.reset();
                    this.written += e.csize;
                    break;
                default:
                    throw new ZipException("invalid compression method");
            }
            this.crc.reset();
            this.current = null;
        }
    }

    public synchronized void write(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (off >= 0 && len >= 0) {
            if (off <= b.length - len) {
                if (len != 0) {
                    if (this.current == null) {
                        throw new ZipException("no current ZIP entry");
                    }
                    ZipEntry entry = this.current.entry;
                    switch (entry.method) {
                        case 0:
                            this.written += (long) len;
                            if (this.written - this.locoff <= entry.size) {
                                this.out.write(b, off, len);
                                break;
                            }
                            throw new ZipException("attempt to write past end of STORED entry");
                        case 8:
                            super.write(b, off, len);
                            break;
                        default:
                            throw new ZipException("invalid compression method");
                    }
                    this.crc.update(b, off, len);
                    return;
                }
                return;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public void finish() throws IOException {
        ensureOpen();
        if (!this.finished) {
            if (this.xentries.isEmpty()) {
                throw new ZipException("No entries");
            }
            if (this.current != null) {
                closeEntry();
            }
            long off = this.written;
            for (XEntry xentry : this.xentries) {
                writeCEN(xentry);
            }
            writeEND(off, this.written - off);
            this.finished = true;
        }
    }

    public void close() throws IOException {
        if (!this.closed) {
            super.close();
            this.closed = true;
        }
    }

    private void writeLOC(XEntry xentry) throws IOException {
        ZipEntry e = xentry.entry;
        int flag = e.flag;
        boolean hasZip64 = false;
        int elen = getExtraLen(e.extra);
        writeInt(ZipConstants.LOCSIG);
        if ((flag & 8) == 8) {
            writeShort(version(e));
            writeShort(flag);
            writeShort(e.method);
            writeInt(e.xdostime);
            writeInt(0);
            writeInt(0);
            writeInt(0);
        } else {
            if (e.csize >= 4294967295L || e.size >= 4294967295L) {
                hasZip64 = true;
                writeShort(45);
            } else {
                writeShort(version(e));
            }
            writeShort(flag);
            writeShort(e.method);
            writeInt(e.xdostime);
            writeInt(e.crc);
            if (hasZip64) {
                writeInt(4294967295L);
                writeInt(4294967295L);
                elen += 20;
            } else {
                writeInt(e.csize);
                writeInt(e.size);
            }
        }
        byte[] nameBytes = this.zc.getBytes(e.name);
        writeShort(nameBytes.length);
        int elenEXTT = 0;
        int flagEXTT = 0;
        if (e.mtime != null) {
            elenEXTT = 4;
            flagEXTT = 1;
        }
        if (e.atime != null) {
            elenEXTT += 4;
            flagEXTT |= 2;
        }
        if (e.ctime != null) {
            elenEXTT += 4;
            flagEXTT |= 4;
        }
        if (flagEXTT != 0) {
            elen += elenEXTT + 5;
        }
        writeShort(elen);
        writeBytes(nameBytes, 0, nameBytes.length);
        if (hasZip64) {
            writeShort(1);
            writeShort(16);
            writeLong(e.size);
            writeLong(e.csize);
        }
        if (flagEXTT != 0) {
            writeShort(21589);
            writeShort(elenEXTT + 1);
            writeByte(flagEXTT);
            if (e.mtime != null) {
                writeInt(ZipUtils.fileTimeToUnixTime(e.mtime));
            }
            if (e.atime != null) {
                writeInt(ZipUtils.fileTimeToUnixTime(e.atime));
            }
            if (e.ctime != null) {
                writeInt(ZipUtils.fileTimeToUnixTime(e.ctime));
            }
        }
        writeExtra(e.extra);
        this.locoff = this.written;
    }

    private void writeEXT(ZipEntry e) throws IOException {
        writeInt(ZipConstants.EXTSIG);
        writeInt(e.crc);
        if (e.csize >= 4294967295L || e.size >= 4294967295L) {
            writeLong(e.csize);
            writeLong(e.size);
            return;
        }
        writeInt(e.csize);
        writeInt(e.size);
    }

    private void writeCEN(XEntry xentry) throws IOException {
        byte[] commentBytes;
        ZipEntry e = xentry.entry;
        int flag = e.flag;
        int version = version(e);
        long csize = e.csize;
        long size = e.size;
        long offset = xentry.offset;
        int elenZIP64 = 0;
        boolean hasZip64 = false;
        if (e.csize >= 4294967295L) {
            csize = 4294967295L;
            elenZIP64 = 8;
            hasZip64 = true;
        }
        if (e.size >= 4294967295L) {
            size = 4294967295L;
            elenZIP64 += 8;
            hasZip64 = true;
        }
        if (xentry.offset >= 4294967295L) {
            offset = 4294967295L;
            elenZIP64 += 8;
            hasZip64 = true;
        }
        writeInt(ZipConstants.CENSIG);
        if (hasZip64) {
            writeShort(45);
            writeShort(45);
        } else {
            writeShort(version);
            writeShort(version);
        }
        writeShort(flag);
        writeShort(e.method);
        writeInt(e.xdostime);
        writeInt(e.crc);
        writeInt(csize);
        writeInt(size);
        byte[] nameBytes = this.zc.getBytes(e.name);
        writeShort(nameBytes.length);
        int elen = getExtraLen(e.extra);
        if (hasZip64) {
            elen += elenZIP64 + 4;
        }
        int flagEXTT = 0;
        if (e.mtime != null) {
            elen += 4;
            flagEXTT = 1;
        }
        if (e.atime != null) {
            flagEXTT |= 2;
        }
        if (e.ctime != null) {
            flagEXTT |= 4;
        }
        if (flagEXTT != 0) {
            elen += 5;
        }
        writeShort(elen);
        if (e.comment != null) {
            commentBytes = this.zc.getBytes(e.comment);
            writeShort(Math.min(commentBytes.length, 65535));
        } else {
            commentBytes = null;
            writeShort(0);
        }
        writeShort(0);
        writeShort(0);
        writeInt(0);
        writeInt(offset);
        writeBytes(nameBytes, 0, nameBytes.length);
        if (hasZip64) {
            writeShort(1);
            writeShort(elenZIP64);
            if (size == 4294967295L) {
                writeLong(e.size);
            }
            if (csize == 4294967295L) {
                writeLong(e.csize);
            }
            if (offset == 4294967295L) {
                writeLong(xentry.offset);
            }
        }
        if (flagEXTT != 0) {
            writeShort(21589);
            if (e.mtime != null) {
                writeShort(5);
                writeByte(flagEXTT);
                writeInt(ZipUtils.fileTimeToUnixTime(e.mtime));
            } else {
                writeShort(1);
                writeByte(flagEXTT);
            }
        }
        writeExtra(e.extra);
        if (commentBytes != null) {
            writeBytes(commentBytes, 0, Math.min(commentBytes.length, 65535));
        }
    }

    private void writeEND(long off, long len) throws IOException {
        boolean hasZip64 = false;
        long xlen = len;
        long xoff = off;
        if (len >= 4294967295L) {
            xlen = 4294967295L;
            hasZip64 = true;
        }
        if (off >= 4294967295L) {
            xoff = 4294967295L;
            hasZip64 = true;
        }
        int count = this.xentries.size();
        if (count >= 65535) {
            hasZip64 |= true;
            if (hasZip64) {
                count = 65535;
            }
        }
        if (hasZip64) {
            long off64 = this.written;
            writeInt(101075792);
            writeLong(44);
            writeShort(45);
            writeShort(45);
            writeInt(0);
            writeInt(0);
            writeLong((long) this.xentries.size());
            writeLong((long) this.xentries.size());
            writeLong(len);
            writeLong(off);
            writeInt(117853008);
            writeInt(0);
            writeLong(off64);
            writeInt(1);
        }
        writeInt(ZipConstants.ENDSIG);
        writeShort(0);
        writeShort(0);
        writeShort(count);
        writeShort(count);
        writeInt(xlen);
        writeInt(xoff);
        if (this.comment != null) {
            writeShort(this.comment.length);
            writeBytes(this.comment, 0, this.comment.length);
            return;
        }
        writeShort(0);
    }

    private int getExtraLen(byte[] extra) {
        if (extra == null) {
            return 0;
        }
        int skipped = 0;
        int len = extra.length;
        int off = 0;
        while (off + 4 <= len) {
            int tag = ZipUtils.get16(extra, off);
            int sz = ZipUtils.get16(extra, off + 2);
            if (sz < 0 || (off + 4) + sz > len) {
                break;
            }
            if (tag == 21589 || tag == 1) {
                skipped += sz + 4;
            }
            off += sz + 4;
        }
        return len - skipped;
    }

    private void writeExtra(byte[] extra) throws IOException {
        if (extra != null) {
            int len = extra.length;
            int off = 0;
            while (off + 4 <= len) {
                int tag = ZipUtils.get16(extra, off);
                int sz = ZipUtils.get16(extra, off + 2);
                if (sz < 0 || (off + 4) + sz > len) {
                    writeBytes(extra, off, len - off);
                    return;
                }
                if (!(tag == 21589 || tag == 1)) {
                    writeBytes(extra, off, sz + 4);
                }
                off += sz + 4;
            }
            if (off < len) {
                writeBytes(extra, off, len - off);
            }
        }
    }

    private void writeByte(int v) throws IOException {
        this.out.write(v & 255);
        this.written++;
    }

    private void writeShort(int v) throws IOException {
        OutputStream out = this.out;
        out.write((v >>> 0) & 255);
        out.write((v >>> 8) & 255);
        this.written += 2;
    }

    private void writeInt(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int) ((v >>> null) & 255));
        out.write((int) ((v >>> 8) & 255));
        out.write((int) ((v >>> 16) & 255));
        out.write((int) ((v >>> 24) & 255));
        this.written += 4;
    }

    private void writeLong(long v) throws IOException {
        OutputStream out = this.out;
        out.write((int) ((v >>> null) & 255));
        out.write((int) ((v >>> 8) & 255));
        out.write((int) ((v >>> 16) & 255));
        out.write((int) ((v >>> 24) & 255));
        out.write((int) ((v >>> 32) & 255));
        out.write((int) ((v >>> 40) & 255));
        out.write((int) ((v >>> 48) & 255));
        out.write((int) ((v >>> 56) & 255));
        this.written += 8;
    }

    private void writeBytes(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
        this.written += (long) len;
    }
}
