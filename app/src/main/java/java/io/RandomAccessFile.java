package java.io;

import android.system.ErrnoException;
import android.system.OsConstants;
import dalvik.system.CloseGuard;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import libcore.io.IoBridge;
import libcore.io.Libcore;
import sun.nio.ch.FileChannelImpl;
import sun.security.x509.GeneralNameInterface;
import sun.util.calendar.BaseCalendar;

public class RandomAccessFile implements DataOutput, DataInput, Closeable {
    private FileChannel channel;
    private Object closeLock;
    private volatile boolean closed;
    private FileDescriptor fd;
    private final CloseGuard guard;
    private int mode;
    private final String path;
    private boolean rw;
    private final byte[] scratch;
    private boolean syncMetadata;

    public RandomAccessFile(String name, String mode) throws FileNotFoundException {
        File file = null;
        if (name != null) {
            file = new File(name);
        }
        this(file, mode);
    }

    public RandomAccessFile(File file, String mode) throws FileNotFoundException {
        this.guard = CloseGuard.get();
        this.scratch = new byte[8];
        this.syncMetadata = false;
        this.channel = null;
        this.closeLock = new Object();
        this.closed = false;
        String path = file != null ? file.getPath() : null;
        this.mode = -1;
        if (mode.equals("r")) {
            this.mode = OsConstants.O_RDONLY;
        } else if (mode.startsWith("rw")) {
            this.mode = OsConstants.O_RDWR | OsConstants.O_CREAT;
            this.rw = true;
            if (mode.length() > 2) {
                if (mode.equals("rws")) {
                    this.syncMetadata = true;
                } else if (mode.equals("rwd")) {
                    this.mode |= OsConstants.O_SYNC;
                } else {
                    this.mode = -1;
                }
            }
        }
        if (this.mode < 0) {
            throw new IllegalArgumentException("Illegal mode \"" + mode + "\" must be one of " + "\"r\", \"rw\", \"rws\"," + " or \"rwd\"");
        } else if (path == null) {
            throw new NullPointerException("file == null");
        } else if (file.isInvalid()) {
            throw new FileNotFoundException("Invalid file path");
        } else {
            this.path = path;
            this.fd = IoBridge.open(file.getPath(), this.mode);
            if (this.syncMetadata) {
                try {
                    this.fd.sync();
                } catch (IOException e) {
                }
            }
            this.guard.open("close");
        }
    }

    public final FileDescriptor getFD() throws IOException {
        if (this.fd != null) {
            return this.fd;
        }
        throw new IOException();
    }

    public final FileChannel getChannel() {
        FileChannel fileChannel;
        synchronized (this) {
            if (this.channel == null) {
                this.channel = FileChannelImpl.open(this.fd, this.path, true, this.rw, this);
            }
            fileChannel = this.channel;
        }
        return fileChannel;
    }

    public int read() throws IOException {
        return read(this.scratch, 0, 1) != -1 ? this.scratch[0] & 255 : -1;
    }

    private int readBytes(byte[] b, int off, int len) throws IOException {
        return IoBridge.read(this.fd, b, off, len);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return readBytes(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        return readBytes(b, 0, b.length);
    }

    public final void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public final void readFully(byte[] b, int off, int len) throws IOException {
        int n = 0;
        do {
            int count = read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        } while (n < len);
    }

    public int skipBytes(int n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        long pos = getFilePointer();
        long len = length();
        long newpos = pos + ((long) n);
        if (newpos > len) {
            newpos = len;
        }
        seek(newpos);
        return (int) (newpos - pos);
    }

    public void write(int b) throws IOException {
        this.scratch[0] = (byte) (b & 255);
        write(this.scratch, 0, 1);
    }

    private void writeBytes(byte[] b, int off, int len) throws IOException {
        IoBridge.write(this.fd, b, off, len);
        if (this.syncMetadata) {
            this.fd.sync();
        }
    }

    public void write(byte[] b) throws IOException {
        writeBytes(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        writeBytes(b, off, len);
    }

    public long getFilePointer() throws IOException {
        try {
            return Libcore.os.lseek(this.fd, 0, OsConstants.SEEK_CUR);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    public void seek(long offset) throws IOException {
        if (offset < 0) {
            throw new IOException("offset < 0: " + offset);
        }
        try {
            Libcore.os.lseek(this.fd, offset, OsConstants.SEEK_SET);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    public long length() throws IOException {
        try {
            return Libcore.os.fstat(this.fd).st_size;
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    public void setLength(long newLength) throws IOException {
        if (newLength < 0) {
            throw new IllegalArgumentException("newLength < 0");
        }
        try {
            Libcore.os.ftruncate(this.fd, newLength);
            if (getFilePointer() > newLength) {
                seek(newLength);
            }
            if (this.syncMetadata) {
                this.fd.sync();
            }
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    public void close() throws IOException {
        this.guard.close();
        synchronized (this.closeLock) {
            if (this.closed) {
                return;
            }
            this.closed = true;
            if (this.channel != null && this.channel.isOpen()) {
                this.channel.close();
            }
            IoBridge.closeAndSignalBlockedThreads(this.fd);
        }
    }

    public final boolean readBoolean() throws IOException {
        int ch = read();
        if (ch < 0) {
            throw new EOFException();
        } else if (ch != 0) {
            return true;
        } else {
            return false;
        }
    }

    public final byte readByte() throws IOException {
        int ch = read();
        if (ch >= 0) {
            return (byte) ch;
        }
        throw new EOFException();
    }

    public final int readUnsignedByte() throws IOException {
        int ch = read();
        if (ch >= 0) {
            return ch;
        }
        throw new EOFException();
    }

    public final short readShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) >= 0) {
            return (short) ((ch1 << 8) + (ch2 << 0));
        }
        throw new EOFException();
    }

    public final int readUnsignedShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) >= 0) {
            return (ch1 << 8) + (ch2 << 0);
        }
        throw new EOFException();
    }

    public final char readChar() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) >= 0) {
            return (char) ((ch1 << 8) + (ch2 << 0));
        }
        throw new EOFException();
    }

    public final int readInt() throws IOException {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        if ((((ch1 | ch2) | ch3) | ch4) >= 0) {
            return (((ch1 << 24) + (ch2 << 16)) + (ch3 << 8)) + (ch4 << 0);
        }
        throw new EOFException();
    }

    public final long readLong() throws IOException {
        return (((long) readInt()) << 32) + (((long) readInt()) & 4294967295L);
    }

    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public final String readLine() throws IOException {
        StringBuffer input = new StringBuffer();
        int c = -1;
        boolean eol = false;
        while (!eol) {
            c = read();
            switch (c) {
                case GeneralNameInterface.NAME_DIFF_TYPE /*-1*/:
                case BaseCalendar.OCTOBER /*10*/:
                    eol = true;
                    break;
                case Calendar.SECOND /*13*/:
                    eol = true;
                    long cur = getFilePointer();
                    if (read() == 10) {
                        break;
                    }
                    seek(cur);
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

    public final String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    public final void writeBoolean(boolean v) throws IOException {
        write(v ? 1 : 0);
    }

    public final void writeByte(int v) throws IOException {
        write(v);
    }

    public final void writeShort(int v) throws IOException {
        write((v >>> 8) & 255);
        write((v >>> 0) & 255);
    }

    public final void writeChar(int v) throws IOException {
        write((v >>> 8) & 255);
        write((v >>> 0) & 255);
    }

    public final void writeInt(int v) throws IOException {
        write((v >>> 24) & 255);
        write((v >>> 16) & 255);
        write((v >>> 8) & 255);
        write((v >>> 0) & 255);
    }

    public final void writeLong(long v) throws IOException {
        write(((int) (v >>> 56)) & 255);
        write(((int) (v >>> 48)) & 255);
        write(((int) (v >>> 40)) & 255);
        write(((int) (v >>> 32)) & 255);
        write(((int) (v >>> 24)) & 255);
        write(((int) (v >>> 16)) & 255);
        write(((int) (v >>> 8)) & 255);
        write(((int) (v >>> null)) & 255);
    }

    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    public final void writeBytes(String s) throws IOException {
        int len = s.length();
        byte[] b = new byte[len];
        s.getBytes(0, len, b, 0);
        writeBytes(b, 0, len);
    }

    public final void writeChars(String s) throws IOException {
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
        writeBytes(b, 0, blen);
    }

    public final void writeUTF(String str) throws IOException {
        DataOutputStream.writeUTF(str, this);
    }

    protected void finalize() throws Throwable {
        try {
            if (this.guard != null) {
                this.guard.warnIfOpen();
            }
            close();
        } finally {
            super.finalize();
        }
    }
}
