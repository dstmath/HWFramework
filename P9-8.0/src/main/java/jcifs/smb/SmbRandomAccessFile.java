package jcifs.smb;

import java.io.DataInput;
import java.io.DataOutput;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import jcifs.netbios.NbtException;
import jcifs.util.Encdec;

public class SmbRandomAccessFile implements DataOutput, DataInput {
    private static final int WRITE_OPTIONS = 2114;
    private int access;
    private int ch;
    private SmbFile file;
    private long fp;
    private int openFlags;
    private int options;
    private int readSize;
    private byte[] tmp;
    private int writeSize;
    private SmbComWriteAndXResponse write_andx_resp;

    public SmbRandomAccessFile(String url, String mode, int shareAccess) throws SmbException, MalformedURLException, UnknownHostException {
        this(new SmbFile(url, "", null, shareAccess), mode);
    }

    public SmbRandomAccessFile(SmbFile file, String mode) throws SmbException, MalformedURLException, UnknownHostException {
        this.access = 0;
        this.options = 0;
        this.tmp = new byte[8];
        this.write_andx_resp = null;
        this.file = file;
        if (mode.equals("r")) {
            this.openFlags = 17;
        } else if (mode.equals("rw")) {
            this.openFlags = 23;
            this.write_andx_resp = new SmbComWriteAndXResponse();
            this.options = WRITE_OPTIONS;
            this.access = 3;
        } else {
            throw new IllegalArgumentException("Invalid mode");
        }
        file.open(this.openFlags, this.access, 128, this.options);
        this.readSize = file.tree.session.transport.rcv_buf_size - 70;
        this.writeSize = file.tree.session.transport.snd_buf_size - 70;
        this.fp = 0;
    }

    public int read() throws SmbException {
        if (read(this.tmp, 0, 1) == -1) {
            return -1;
        }
        return this.tmp[0] & 255;
    }

    public int read(byte[] b) throws SmbException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws SmbException {
        if (len <= 0) {
            return 0;
        }
        long start = this.fp;
        if (!this.file.isOpen()) {
            this.file.open(this.openFlags, 0, 128, this.options);
        }
        SmbComReadAndXResponse response = new SmbComReadAndXResponse(b, off);
        int n;
        int r;
        do {
            if (len > this.readSize) {
                r = this.readSize;
            } else {
                r = len;
            }
            this.file.send(new SmbComReadAndX(this.file.fid, this.fp, r, null), response);
            n = response.dataLength;
            if (n > 0) {
                this.fp += (long) n;
                len -= n;
                response.off += n;
                if (len <= 0) {
                    break;
                }
            } else {
                return (int) (this.fp - start > 0 ? this.fp - start : -1);
            }
        } while (n == r);
        return (int) (this.fp - start);
    }

    public final void readFully(byte[] b) throws SmbException {
        readFully(b, 0, b.length);
    }

    public final void readFully(byte[] b, int off, int len) throws SmbException {
        int n = 0;
        do {
            int count = read(b, off + n, len - n);
            if (count < 0) {
                throw new SmbException("EOF");
            }
            n += count;
            this.fp += (long) count;
        } while (n < len);
    }

    public int skipBytes(int n) throws SmbException {
        if (n <= 0) {
            return 0;
        }
        this.fp += (long) n;
        return n;
    }

    public void write(int b) throws SmbException {
        this.tmp[0] = (byte) b;
        write(this.tmp, 0, 1);
    }

    public void write(byte[] b) throws SmbException {
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws SmbException {
        if (len > 0) {
            if (!this.file.isOpen()) {
                this.file.open(this.openFlags, 0, 128, this.options);
            }
            do {
                int w;
                if (len > this.writeSize) {
                    w = this.writeSize;
                } else {
                    w = len;
                }
                this.file.send(new SmbComWriteAndX(this.file.fid, this.fp, len - w, b, off, w, null), this.write_andx_resp);
                this.fp += this.write_andx_resp.count;
                len = (int) (((long) len) - this.write_andx_resp.count);
                off = (int) (((long) off) + this.write_andx_resp.count);
            } while (len > 0);
        }
    }

    public long getFilePointer() throws SmbException {
        return this.fp;
    }

    public void seek(long pos) throws SmbException {
        this.fp = pos;
    }

    public long length() throws SmbException {
        return this.file.length();
    }

    public void setLength(long newLength) throws SmbException {
        if (!this.file.isOpen()) {
            this.file.open(this.openFlags, 0, 128, this.options);
        }
        this.file.send(new SmbComWrite(this.file.fid, (int) (4294967295L & newLength), 0, this.tmp, 0, 0), new SmbComWriteResponse());
    }

    public void close() throws SmbException {
        this.file.close();
    }

    public final boolean readBoolean() throws SmbException {
        if (read(this.tmp, 0, 1) < 0) {
            throw new SmbException("EOF");
        } else if (this.tmp[0] != (byte) 0) {
            return true;
        } else {
            return false;
        }
    }

    public final byte readByte() throws SmbException {
        if (read(this.tmp, 0, 1) >= 0) {
            return this.tmp[0];
        }
        throw new SmbException("EOF");
    }

    public final int readUnsignedByte() throws SmbException {
        if (read(this.tmp, 0, 1) >= 0) {
            return this.tmp[0] & 255;
        }
        throw new SmbException("EOF");
    }

    public final short readShort() throws SmbException {
        if (read(this.tmp, 0, 2) >= 0) {
            return Encdec.dec_uint16be(this.tmp, 0);
        }
        throw new SmbException("EOF");
    }

    public final int readUnsignedShort() throws SmbException {
        if (read(this.tmp, 0, 2) >= 0) {
            return Encdec.dec_uint16be(this.tmp, 0) & 65535;
        }
        throw new SmbException("EOF");
    }

    public final char readChar() throws SmbException {
        if (read(this.tmp, 0, 2) >= 0) {
            return (char) Encdec.dec_uint16be(this.tmp, 0);
        }
        throw new SmbException("EOF");
    }

    public final int readInt() throws SmbException {
        if (read(this.tmp, 0, 4) >= 0) {
            return Encdec.dec_uint32be(this.tmp, 0);
        }
        throw new SmbException("EOF");
    }

    public final long readLong() throws SmbException {
        if (read(this.tmp, 0, 8) >= 0) {
            return Encdec.dec_uint64be(this.tmp, 0);
        }
        throw new SmbException("EOF");
    }

    public final float readFloat() throws SmbException {
        if (read(this.tmp, 0, 4) >= 0) {
            return Encdec.dec_floatbe(this.tmp, 0);
        }
        throw new SmbException("EOF");
    }

    public final double readDouble() throws SmbException {
        if (read(this.tmp, 0, 8) >= 0) {
            return Encdec.dec_doublebe(this.tmp, 0);
        }
        throw new SmbException("EOF");
    }

    public final String readLine() throws SmbException {
        StringBuffer input = new StringBuffer();
        int c = -1;
        boolean eol = false;
        while (!eol) {
            c = read();
            switch (c) {
                case NbtException.CONNECTION_REFUSED /*-1*/:
                case SmbConstants.DEFAULT_MAX_MPX_COUNT /*10*/:
                    eol = true;
                    break;
                case 13:
                    eol = true;
                    long cur = this.fp;
                    if (read() == 10) {
                        break;
                    }
                    this.fp = cur;
                    break;
                default:
                    input.append((char) c);
                    break;
            }
        }
        if (c == -1 && input.length() == 0) {
            return null;
        }
        return input.toString();
    }

    public final String readUTF() throws SmbException {
        int size = readUnsignedShort();
        byte[] b = new byte[size];
        read(b, 0, size);
        try {
            return Encdec.dec_utf8(b, 0, size);
        } catch (Throwable ioe) {
            throw new SmbException("", ioe);
        }
    }

    public final void writeBoolean(boolean v) throws SmbException {
        int i;
        byte[] bArr = this.tmp;
        if (v) {
            i = 1;
        } else {
            i = 0;
        }
        bArr[0] = (byte) i;
        write(this.tmp, 0, 1);
    }

    public final void writeByte(int v) throws SmbException {
        this.tmp[0] = (byte) v;
        write(this.tmp, 0, 1);
    }

    public final void writeShort(int v) throws SmbException {
        Encdec.enc_uint16be((short) v, this.tmp, 0);
        write(this.tmp, 0, 2);
    }

    public final void writeChar(int v) throws SmbException {
        Encdec.enc_uint16be((short) v, this.tmp, 0);
        write(this.tmp, 0, 2);
    }

    public final void writeInt(int v) throws SmbException {
        Encdec.enc_uint32be(v, this.tmp, 0);
        write(this.tmp, 0, 4);
    }

    public final void writeLong(long v) throws SmbException {
        Encdec.enc_uint64be(v, this.tmp, 0);
        write(this.tmp, 0, 8);
    }

    public final void writeFloat(float v) throws SmbException {
        Encdec.enc_floatbe(v, this.tmp, 0);
        write(this.tmp, 0, 4);
    }

    public final void writeDouble(double v) throws SmbException {
        Encdec.enc_doublebe(v, this.tmp, 0);
        write(this.tmp, 0, 8);
    }

    public final void writeBytes(String s) throws SmbException {
        byte[] b = s.getBytes();
        write(b, 0, b.length);
    }

    public final void writeChars(String s) throws SmbException {
        int clen = s.length();
        int blen = clen * 2;
        byte[] b = new byte[blen];
        char[] c = new char[clen];
        s.getChars(0, clen, c, 0);
        int j = 0;
        for (int i = 0; i < clen; i++) {
            int i2 = j + 1;
            b[j] = (byte) (c[i] >>> 8);
            j = i2 + 1;
            b[i2] = (byte) (c[i] >>> 0);
        }
        write(b, 0, blen);
    }

    public final void writeUTF(String str) throws SmbException {
        int len = str.length();
        int size = 0;
        for (int i = 0; i < len; i++) {
            int ch = str.charAt(i);
            int i2 = ch > 127 ? ch > 2047 ? 3 : 2 : 1;
            size += i2;
        }
        byte[] dst = new byte[size];
        writeShort(size);
        try {
            Encdec.enc_utf8(str, dst, 0, size);
            write(dst, 0, size);
        } catch (Throwable ioe) {
            throw new SmbException("", ioe);
        }
    }
}
