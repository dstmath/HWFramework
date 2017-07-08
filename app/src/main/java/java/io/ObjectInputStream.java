package java.io;

import dalvik.system.VMStack;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import sun.reflect.misc.ReflectUtil;
import sun.security.x509.GeneralNameInterface;
import sun.util.calendar.BaseCalendar;
import sun.util.logging.PlatformLogger;

public class ObjectInputStream extends InputStream implements ObjectInput, ObjectStreamConstants {
    private static final int NULL_HANDLE = -1;
    private static final HashMap<String, Class<?>> primClasses = null;
    private static final Object unsharedMarker = null;
    private final BlockDataInputStream bin;
    private boolean closed;
    private SerialCallbackContext curContext;
    private boolean defaultDataEnd;
    private int depth;
    private final boolean enableOverride;
    private boolean enableResolve;
    private final HandleTable handles;
    private int passHandle;
    private byte[] primVals;
    private final ValidationList vlist;

    /* renamed from: java.io.ObjectInputStream.1 */
    static class AnonymousClass1 implements PrivilegedAction<Boolean> {
        final /* synthetic */ Class val$subcl;

        AnonymousClass1(Class val$subcl) {
            this.val$subcl = val$subcl;
        }

        public Boolean run() {
            Class<?> cl = this.val$subcl;
            while (cl != ObjectInputStream.class) {
                try {
                    cl.getDeclaredMethod("readUnshared", (Class[]) null);
                    return Boolean.FALSE;
                } catch (NoSuchMethodException e) {
                    try {
                        cl.getDeclaredMethod("readFields", (Class[]) null);
                        return Boolean.FALSE;
                    } catch (NoSuchMethodException e2) {
                        cl = cl.getSuperclass();
                    }
                }
            }
            return Boolean.TRUE;
        }
    }

    private class BlockDataInputStream extends InputStream implements DataInput {
        private static final int CHAR_BUF_SIZE = 256;
        private static final int HEADER_BLOCKED = -2;
        private static final int MAX_BLOCK_SIZE = 1024;
        private static final int MAX_HEADER_SIZE = 5;
        private boolean blkmode;
        private final byte[] buf;
        private final char[] cbuf;
        private final DataInputStream din;
        private int end;
        private final byte[] hbuf;
        private final PeekInputStream in;
        private int pos;
        private int unread;

        BlockDataInputStream(InputStream in) {
            this.buf = new byte[MAX_BLOCK_SIZE];
            this.hbuf = new byte[MAX_HEADER_SIZE];
            this.cbuf = new char[CHAR_BUF_SIZE];
            this.blkmode = false;
            this.pos = 0;
            this.end = ObjectInputStream.NULL_HANDLE;
            this.unread = 0;
            this.in = new PeekInputStream(in);
            this.din = new DataInputStream(this);
        }

        boolean setBlockDataMode(boolean newmode) throws IOException {
            boolean z = false;
            if (this.blkmode == newmode) {
                return this.blkmode;
            }
            if (newmode) {
                this.pos = 0;
                this.end = 0;
                this.unread = 0;
            } else if (this.pos < this.end) {
                throw new IllegalStateException("unread block data");
            }
            this.blkmode = newmode;
            if (!this.blkmode) {
                z = true;
            }
            return z;
        }

        boolean getBlockDataMode() {
            return this.blkmode;
        }

        void skipBlockData() throws IOException {
            if (this.blkmode) {
                while (this.end >= 0) {
                    refill();
                }
                return;
            }
            throw new IllegalStateException("not in block data mode");
        }

        private int readBlockHeader(boolean canBlock) throws IOException {
            if (ObjectInputStream.this.defaultDataEnd) {
                return ObjectInputStream.NULL_HANDLE;
            }
            while (true) {
                int avail = canBlock ? PlatformLogger.OFF : this.in.available();
                if (avail == 0) {
                    return HEADER_BLOCKED;
                }
                int tc = this.in.peek();
                switch (tc) {
                    case 119:
                        if (avail < 2) {
                            return HEADER_BLOCKED;
                        }
                        this.in.readFully(this.hbuf, 0, 2);
                        return this.hbuf[1] & 255;
                    case 121:
                        try {
                            this.in.read();
                            ObjectInputStream.this.handleReset();
                        } catch (EOFException e) {
                            throw new StreamCorruptedException("unexpected EOF while reading block data header");
                        }
                    case 122:
                        if (avail < MAX_HEADER_SIZE) {
                            return HEADER_BLOCKED;
                        }
                        this.in.readFully(this.hbuf, 0, MAX_HEADER_SIZE);
                        int len = Bits.getInt(this.hbuf, 1);
                        if (len >= 0) {
                            return len;
                        }
                        throw new StreamCorruptedException("illegal block data header length: " + len);
                    default:
                        if (tc < 0 || (tc >= 112 && tc <= 126)) {
                            return ObjectInputStream.NULL_HANDLE;
                        }
                        throw new StreamCorruptedException(String.format("invalid type code: %02X", Integer.valueOf(tc)));
                }
                throw new StreamCorruptedException("unexpected EOF while reading block data header");
            }
        }

        private void refill() throws IOException {
            do {
                try {
                    this.pos = 0;
                    int n;
                    if (this.unread > 0) {
                        n = this.in.read(this.buf, 0, Math.min(this.unread, (int) MAX_BLOCK_SIZE));
                        if (n >= 0) {
                            this.end = n;
                            this.unread -= n;
                        } else {
                            throw new StreamCorruptedException("unexpected EOF in middle of data block");
                        }
                    }
                    n = readBlockHeader(true);
                    if (n >= 0) {
                        this.end = 0;
                        this.unread = n;
                    } else {
                        this.end = ObjectInputStream.NULL_HANDLE;
                        this.unread = 0;
                    }
                } catch (IOException ex) {
                    this.pos = 0;
                    this.end = ObjectInputStream.NULL_HANDLE;
                    this.unread = 0;
                    throw ex;
                }
            } while (this.pos == this.end);
        }

        int currentBlockRemaining() {
            if (!this.blkmode) {
                throw new IllegalStateException();
            } else if (this.end >= 0) {
                return (this.end - this.pos) + this.unread;
            } else {
                return 0;
            }
        }

        int peek() throws IOException {
            if (!this.blkmode) {
                return this.in.peek();
            }
            if (this.pos == this.end) {
                refill();
            }
            return this.end >= 0 ? this.buf[this.pos] & 255 : ObjectInputStream.NULL_HANDLE;
        }

        byte peekByte() throws IOException {
            int val = peek();
            if (val >= 0) {
                return (byte) val;
            }
            throw new EOFException();
        }

        public int read() throws IOException {
            if (!this.blkmode) {
                return this.in.read();
            }
            int i;
            if (this.pos == this.end) {
                refill();
            }
            if (this.end >= 0) {
                byte[] bArr = this.buf;
                int i2 = this.pos;
                this.pos = i2 + 1;
                i = bArr[i2] & 255;
            } else {
                i = ObjectInputStream.NULL_HANDLE;
            }
            return i;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            return read(b, off, len, false);
        }

        public long skip(long len) throws IOException {
            long remain = len;
            while (remain > 0) {
                int nread;
                if (!this.blkmode) {
                    nread = this.in.read(this.buf, 0, (int) Math.min(remain, 1024));
                    if (nread < 0) {
                        break;
                    }
                    remain -= (long) nread;
                } else {
                    if (this.pos == this.end) {
                        refill();
                    }
                    if (this.end < 0) {
                        break;
                    }
                    nread = (int) Math.min(remain, (long) (this.end - this.pos));
                    remain -= (long) nread;
                    this.pos += nread;
                }
            }
            return len - remain;
        }

        public int available() throws IOException {
            int i = 0;
            if (!this.blkmode) {
                return this.in.available();
            }
            if (this.pos == this.end && this.unread == 0) {
                int n;
                do {
                    n = readBlockHeader(false);
                } while (n == 0);
                switch (n) {
                    case HEADER_BLOCKED /*-2*/:
                        break;
                    case ObjectInputStream.NULL_HANDLE /*-1*/:
                        this.pos = 0;
                        this.end = ObjectInputStream.NULL_HANDLE;
                        break;
                    default:
                        this.pos = 0;
                        this.end = 0;
                        this.unread = n;
                        break;
                }
            }
            int unreadAvail = this.unread > 0 ? Math.min(this.in.available(), this.unread) : 0;
            if (this.end >= 0) {
                i = (this.end - this.pos) + unreadAvail;
            }
            return i;
        }

        public void close() throws IOException {
            if (this.blkmode) {
                this.pos = 0;
                this.end = ObjectInputStream.NULL_HANDLE;
                this.unread = 0;
            }
            this.in.close();
        }

        int read(byte[] b, int off, int len, boolean copy) throws IOException {
            if (len == 0) {
                return 0;
            }
            int nread;
            if (this.blkmode) {
                if (this.pos == this.end) {
                    refill();
                }
                if (this.end < 0) {
                    return ObjectInputStream.NULL_HANDLE;
                }
                nread = Math.min(len, this.end - this.pos);
                System.arraycopy(this.buf, this.pos, b, off, nread);
                this.pos += nread;
                return nread;
            } else if (!copy) {
                return this.in.read(b, off, len);
            } else {
                nread = this.in.read(this.buf, 0, Math.min(len, (int) MAX_BLOCK_SIZE));
                if (nread > 0) {
                    System.arraycopy(this.buf, 0, b, off, nread);
                }
                return nread;
            }
        }

        public void readFully(byte[] b) throws IOException {
            readFully(b, 0, b.length, false);
        }

        public void readFully(byte[] b, int off, int len) throws IOException {
            readFully(b, off, len, false);
        }

        public void readFully(byte[] b, int off, int len, boolean copy) throws IOException {
            while (len > 0) {
                int n = read(b, off, len, copy);
                if (n < 0) {
                    throw new EOFException();
                }
                off += n;
                len -= n;
            }
        }

        public int skipBytes(int n) throws IOException {
            return this.din.skipBytes(n);
        }

        public boolean readBoolean() throws IOException {
            int v = read();
            if (v < 0) {
                throw new EOFException();
            } else if (v != 0) {
                return true;
            } else {
                return false;
            }
        }

        public byte readByte() throws IOException {
            int v = read();
            if (v >= 0) {
                return (byte) v;
            }
            throw new EOFException();
        }

        public int readUnsignedByte() throws IOException {
            int v = read();
            if (v >= 0) {
                return v;
            }
            throw new EOFException();
        }

        public char readChar() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 2);
            } else if (this.end - this.pos < 2) {
                return this.din.readChar();
            }
            char v = Bits.getChar(this.buf, this.pos);
            this.pos += 2;
            return v;
        }

        public short readShort() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 2);
            } else if (this.end - this.pos < 2) {
                return this.din.readShort();
            }
            short v = Bits.getShort(this.buf, this.pos);
            this.pos += 2;
            return v;
        }

        public int readUnsignedShort() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 2);
            } else if (this.end - this.pos < 2) {
                return this.din.readUnsignedShort();
            }
            int v = Bits.getShort(this.buf, this.pos) & 65535;
            this.pos += 2;
            return v;
        }

        public int readInt() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 4);
            } else if (this.end - this.pos < 4) {
                return this.din.readInt();
            }
            int v = Bits.getInt(this.buf, this.pos);
            this.pos += 4;
            return v;
        }

        public float readFloat() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 4);
            } else if (this.end - this.pos < 4) {
                return this.din.readFloat();
            }
            float v = Bits.getFloat(this.buf, this.pos);
            this.pos += 4;
            return v;
        }

        public long readLong() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 8);
            } else if (this.end - this.pos < 8) {
                return this.din.readLong();
            }
            long v = Bits.getLong(this.buf, this.pos);
            this.pos += 8;
            return v;
        }

        public double readDouble() throws IOException {
            if (!this.blkmode) {
                this.pos = 0;
                this.in.readFully(this.buf, 0, 8);
            } else if (this.end - this.pos < 8) {
                return this.din.readDouble();
            }
            double v = Bits.getDouble(this.buf, this.pos);
            this.pos += 8;
            return v;
        }

        public String readUTF() throws IOException {
            return readUTFBody((long) readUnsignedShort());
        }

        public String readLine() throws IOException {
            return this.din.readLine();
        }

        void readBooleans(boolean[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int stop;
                if (!this.blkmode) {
                    int span = Math.min(endoff - off2, (int) MAX_BLOCK_SIZE);
                    this.in.readFully(this.buf, 0, span);
                    stop = off2 + span;
                    this.pos = 0;
                } else if (this.end - this.pos < 1) {
                    off = off2 + 1;
                    v[off2] = this.din.readBoolean();
                    off2 = off;
                } else {
                    stop = Math.min(endoff, (this.end + off2) - this.pos);
                }
                while (off2 < stop) {
                    off = off2 + 1;
                    byte[] bArr = this.buf;
                    int i = this.pos;
                    this.pos = i + 1;
                    v[off2] = Bits.getBoolean(bArr, i);
                    off2 = off;
                }
            }
        }

        void readChars(char[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int stop;
                if (!this.blkmode) {
                    int span = Math.min(endoff - off2, (int) Modifier.INTERFACE);
                    this.in.readFully(this.buf, 0, span << 1);
                    stop = off2 + span;
                    this.pos = 0;
                } else if (this.end - this.pos < 2) {
                    off = off2 + 1;
                    v[off2] = this.din.readChar();
                    off2 = off;
                } else {
                    stop = Math.min(endoff, ((this.end - this.pos) >> 1) + off2);
                }
                while (off2 < stop) {
                    off = off2 + 1;
                    v[off2] = Bits.getChar(this.buf, this.pos);
                    this.pos += 2;
                    off2 = off;
                }
            }
        }

        void readShorts(short[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int stop;
                if (!this.blkmode) {
                    int span = Math.min(endoff - off2, (int) Modifier.INTERFACE);
                    this.in.readFully(this.buf, 0, span << 1);
                    stop = off2 + span;
                    this.pos = 0;
                } else if (this.end - this.pos < 2) {
                    off = off2 + 1;
                    v[off2] = this.din.readShort();
                    off2 = off;
                } else {
                    stop = Math.min(endoff, ((this.end - this.pos) >> 1) + off2);
                }
                while (off2 < stop) {
                    off = off2 + 1;
                    v[off2] = Bits.getShort(this.buf, this.pos);
                    this.pos += 2;
                    off2 = off;
                }
            }
        }

        void readInts(int[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int stop;
                if (!this.blkmode) {
                    int span = Math.min(endoff - off2, (int) CHAR_BUF_SIZE);
                    this.in.readFully(this.buf, 0, span << 2);
                    stop = off2 + span;
                    this.pos = 0;
                } else if (this.end - this.pos < 4) {
                    off = off2 + 1;
                    v[off2] = this.din.readInt();
                    off2 = off;
                } else {
                    stop = Math.min(endoff, ((this.end - this.pos) >> 2) + off2);
                }
                while (off2 < stop) {
                    off = off2 + 1;
                    v[off2] = Bits.getInt(this.buf, this.pos);
                    this.pos += 4;
                    off2 = off;
                }
            }
        }

        void readFloats(float[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int span;
                if (!this.blkmode) {
                    span = Math.min(endoff - off2, (int) CHAR_BUF_SIZE);
                    this.in.readFully(this.buf, 0, span << 2);
                    this.pos = 0;
                } else if (this.end - this.pos < 4) {
                    off = off2 + 1;
                    v[off2] = this.din.readFloat();
                    off2 = off;
                } else {
                    span = Math.min(endoff - off2, (this.end - this.pos) >> 2);
                }
                ObjectInputStream.bytesToFloats(this.buf, this.pos, v, off2, span);
                off = off2 + span;
                this.pos += span << 2;
                off2 = off;
            }
        }

        void readLongs(long[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int stop;
                if (!this.blkmode) {
                    int span = Math.min(endoff - off2, (int) Pattern.CANON_EQ);
                    this.in.readFully(this.buf, 0, span << 3);
                    stop = off2 + span;
                    this.pos = 0;
                } else if (this.end - this.pos < 8) {
                    off = off2 + 1;
                    v[off2] = this.din.readLong();
                    off2 = off;
                } else {
                    stop = Math.min(endoff, ((this.end - this.pos) >> 3) + off2);
                }
                while (off2 < stop) {
                    off = off2 + 1;
                    v[off2] = Bits.getLong(this.buf, this.pos);
                    this.pos += 8;
                    off2 = off;
                }
            }
        }

        void readDoubles(double[] v, int off, int len) throws IOException {
            int endoff = off + len;
            int off2 = off;
            while (off2 < endoff) {
                int span;
                if (!this.blkmode) {
                    span = Math.min(endoff - off2, (int) Pattern.CANON_EQ);
                    this.in.readFully(this.buf, 0, span << 3);
                    this.pos = 0;
                } else if (this.end - this.pos < 8) {
                    off = off2 + 1;
                    v[off2] = this.din.readDouble();
                    off2 = off;
                } else {
                    span = Math.min(endoff - off2, (this.end - this.pos) >> 3);
                }
                ObjectInputStream.bytesToDoubles(this.buf, this.pos, v, off2, span);
                off = off2 + span;
                this.pos += span << 3;
                off2 = off;
            }
        }

        String readLongUTF() throws IOException {
            return readUTFBody(readLong());
        }

        private String readUTFBody(long utflen) throws IOException {
            StringBuilder sbuf = new StringBuilder();
            if (!this.blkmode) {
                this.pos = 0;
                this.end = 0;
            }
            while (utflen > 0) {
                int avail = this.end - this.pos;
                if (avail >= 3 || ((long) avail) == utflen) {
                    utflen -= readUTFSpan(sbuf, utflen);
                } else if (this.blkmode) {
                    utflen -= (long) readUTFChar(sbuf, utflen);
                } else {
                    if (avail > 0) {
                        System.arraycopy(this.buf, this.pos, this.buf, 0, avail);
                    }
                    this.pos = 0;
                    this.end = (int) Math.min(1024, utflen);
                    this.in.readFully(this.buf, avail, this.end - avail);
                }
            }
            return sbuf.toString();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private long readUTFSpan(StringBuilder sbuf, long utflen) throws IOException {
            int i;
            int i2;
            Throwable th;
            int start = this.pos;
            int avail = Math.min(this.end - this.pos, (int) CHAR_BUF_SIZE);
            int i3 = this.pos;
            if (utflen > ((long) avail)) {
                i = avail + HEADER_BLOCKED;
            } else {
                i = (int) utflen;
            }
            int stop = i3 + i;
            int cpos = 0;
            while (this.pos < stop) {
                try {
                    byte[] bArr = this.buf;
                    i3 = this.pos;
                    this.pos = i3 + 1;
                    int b1 = bArr[i3] & 255;
                    int b2;
                    switch (b1 >> 4) {
                        case GeneralNameInterface.NAME_MATCH /*0*/:
                        case BaseCalendar.SUNDAY /*1*/:
                        case BaseCalendar.MONDAY /*2*/:
                        case BaseCalendar.TUESDAY /*3*/:
                        case BaseCalendar.WEDNESDAY /*4*/:
                        case MAX_HEADER_SIZE /*5*/:
                        case BaseCalendar.JUNE /*6*/:
                        case BaseCalendar.SATURDAY /*7*/:
                            i2 = cpos + 1;
                            try {
                                this.cbuf[cpos] = (char) b1;
                                break;
                            } catch (ArrayIndexOutOfBoundsException e) {
                                break;
                            } catch (Throwable th2) {
                                th = th2;
                                break;
                            }
                        case BaseCalendar.DECEMBER /*12*/:
                        case Calendar.SECOND /*13*/:
                            bArr = this.buf;
                            i3 = this.pos;
                            this.pos = i3 + 1;
                            b2 = bArr[i3];
                            if ((b2 & 192) == Pattern.CANON_EQ) {
                                i2 = cpos + 1;
                                this.cbuf[cpos] = (char) (((b1 & 31) << 6) | ((b2 & 63) << 0));
                                break;
                            }
                            throw new UTFDataFormatException();
                        case ZipConstants.LOCCRC /*14*/:
                            int b3 = this.buf[this.pos + 1];
                            b2 = this.buf[this.pos + 0];
                            this.pos += 2;
                            if ((b2 & 192) == Pattern.CANON_EQ && (b3 & 192) == Pattern.CANON_EQ) {
                                i2 = cpos + 1;
                                this.cbuf[cpos] = (char) ((((b1 & 15) << 12) | ((b2 & 63) << 6)) | ((b3 & 63) << 0));
                                break;
                            }
                            throw new UTFDataFormatException();
                        default:
                            throw new UTFDataFormatException();
                    }
                } catch (ArrayIndexOutOfBoundsException e2) {
                    i2 = cpos;
                } catch (Throwable th3) {
                    th = th3;
                    i2 = cpos;
                }
            }
            if (null != null || ((long) (this.pos - start)) > utflen) {
                this.pos = ((int) utflen) + start;
                throw new UTFDataFormatException();
            }
            i2 = cpos;
            sbuf.append(this.cbuf, 0, i2);
            return (long) (this.pos - start);
        }

        private int readUTFChar(StringBuilder sbuf, long utflen) throws IOException {
            int b1 = readByte() & 255;
            int b2;
            switch (b1 >> 4) {
                case GeneralNameInterface.NAME_MATCH /*0*/:
                case BaseCalendar.SUNDAY /*1*/:
                case BaseCalendar.MONDAY /*2*/:
                case BaseCalendar.TUESDAY /*3*/:
                case BaseCalendar.WEDNESDAY /*4*/:
                case MAX_HEADER_SIZE /*5*/:
                case BaseCalendar.JUNE /*6*/:
                case BaseCalendar.SATURDAY /*7*/:
                    sbuf.append((char) b1);
                    return 1;
                case BaseCalendar.DECEMBER /*12*/:
                case Calendar.SECOND /*13*/:
                    if (utflen < 2) {
                        throw new UTFDataFormatException();
                    }
                    b2 = readByte();
                    if ((b2 & 192) != Pattern.CANON_EQ) {
                        throw new UTFDataFormatException();
                    }
                    sbuf.append((char) (((b1 & 31) << 6) | ((b2 & 63) << 0)));
                    return 2;
                case ZipConstants.LOCCRC /*14*/:
                    if (utflen < 3) {
                        if (utflen == 2) {
                            readByte();
                        }
                        throw new UTFDataFormatException();
                    }
                    b2 = readByte();
                    int b3 = readByte();
                    if ((b2 & 192) == Pattern.CANON_EQ && (b3 & 192) == Pattern.CANON_EQ) {
                        sbuf.append((char) ((((b1 & 15) << 12) | ((b2 & 63) << 6)) | ((b3 & 63) << 0)));
                        return 3;
                    }
                    throw new UTFDataFormatException();
                default:
                    throw new UTFDataFormatException();
            }
        }
    }

    private static class Caches {
        static final ConcurrentMap<WeakClassKey, Boolean> subclassAudits = null;
        static final ReferenceQueue<Class<?>> subclassAuditsQueue = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.ObjectInputStream.Caches.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.ObjectInputStream.Caches.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.Caches.<clinit>():void");
        }

        private Caches() {
        }
    }

    public static abstract class GetField {
        public abstract boolean defaulted(String str) throws IOException;

        public abstract byte get(String str, byte b) throws IOException;

        public abstract char get(String str, char c) throws IOException;

        public abstract double get(String str, double d) throws IOException;

        public abstract float get(String str, float f) throws IOException;

        public abstract int get(String str, int i) throws IOException;

        public abstract long get(String str, long j) throws IOException;

        public abstract Object get(String str, Object obj) throws IOException;

        public abstract short get(String str, short s) throws IOException;

        public abstract boolean get(String str, boolean z) throws IOException;

        public abstract ObjectStreamClass getObjectStreamClass();

        public GetField() {
        }
    }

    private class GetFieldImpl extends GetField {
        private final ObjectStreamClass desc;
        private final int[] objHandles;
        private final Object[] objVals;
        private final byte[] primVals;
        final /* synthetic */ ObjectInputStream this$0;

        GetFieldImpl(ObjectInputStream this$0, ObjectStreamClass desc) {
            this.this$0 = this$0;
            this.desc = desc;
            this.primVals = new byte[desc.getPrimDataSize()];
            this.objVals = new Object[desc.getNumObjFields()];
            this.objHandles = new int[this.objVals.length];
        }

        public ObjectStreamClass getObjectStreamClass() {
            return this.desc;
        }

        public boolean defaulted(String name) throws IOException {
            return getFieldOffset(name, null) < 0;
        }

        public boolean get(String name, boolean val) throws IOException {
            int off = getFieldOffset(name, Boolean.TYPE);
            return off >= 0 ? Bits.getBoolean(this.primVals, off) : val;
        }

        public byte get(String name, byte val) throws IOException {
            int off = getFieldOffset(name, Byte.TYPE);
            return off >= 0 ? this.primVals[off] : val;
        }

        public char get(String name, char val) throws IOException {
            int off = getFieldOffset(name, Character.TYPE);
            return off >= 0 ? Bits.getChar(this.primVals, off) : val;
        }

        public short get(String name, short val) throws IOException {
            int off = getFieldOffset(name, Short.TYPE);
            return off >= 0 ? Bits.getShort(this.primVals, off) : val;
        }

        public int get(String name, int val) throws IOException {
            int off = getFieldOffset(name, Integer.TYPE);
            return off >= 0 ? Bits.getInt(this.primVals, off) : val;
        }

        public float get(String name, float val) throws IOException {
            int off = getFieldOffset(name, Float.TYPE);
            return off >= 0 ? Bits.getFloat(this.primVals, off) : val;
        }

        public long get(String name, long val) throws IOException {
            int off = getFieldOffset(name, Long.TYPE);
            return off >= 0 ? Bits.getLong(this.primVals, off) : val;
        }

        public double get(String name, double val) throws IOException {
            int off = getFieldOffset(name, Double.TYPE);
            return off >= 0 ? Bits.getDouble(this.primVals, off) : val;
        }

        public Object get(String name, Object val) throws IOException {
            Object obj = null;
            int off = getFieldOffset(name, Object.class);
            if (off < 0) {
                return val;
            }
            int objHandle = this.objHandles[off];
            this.this$0.handles.markDependency(this.this$0.passHandle, objHandle);
            if (this.this$0.handles.lookupException(objHandle) == null) {
                obj = this.objVals[off];
            }
            return obj;
        }

        void readFields() throws IOException {
            this.this$0.bin.readFully(this.primVals, 0, this.primVals.length, false);
            int oldHandle = this.this$0.passHandle;
            ObjectStreamField[] fields = this.desc.getFields(false);
            int numPrimFields = fields.length - this.objVals.length;
            for (int i = 0; i < this.objVals.length; i++) {
                this.objVals[i] = this.this$0.readObject0(fields[numPrimFields + i].isUnshared());
                this.objHandles[i] = this.this$0.passHandle;
            }
            this.this$0.passHandle = oldHandle;
        }

        private int getFieldOffset(String name, Class type) {
            ObjectStreamField field = this.desc.getField(name, type);
            if (field != null) {
                return field.getOffset();
            }
            if (this.desc.getLocalDesc().getField(name, type) != null) {
                return ObjectInputStream.NULL_HANDLE;
            }
            throw new IllegalArgumentException("no such field " + name + " with type " + type);
        }
    }

    private static class HandleTable {
        private static final byte STATUS_EXCEPTION = (byte) 3;
        private static final byte STATUS_OK = (byte) 1;
        private static final byte STATUS_UNKNOWN = (byte) 2;
        HandleList[] deps;
        Object[] entries;
        int lowDep;
        int size;
        byte[] status;

        private static class HandleList {
            private int[] list;
            private int size;

            public HandleList() {
                this.list = new int[4];
                this.size = 0;
            }

            public void add(int handle) {
                if (this.size >= this.list.length) {
                    int[] newList = new int[(this.list.length << 1)];
                    System.arraycopy(this.list, 0, newList, 0, this.list.length);
                    this.list = newList;
                }
                int[] iArr = this.list;
                int i = this.size;
                this.size = i + 1;
                iArr[i] = handle;
            }

            public int get(int index) {
                if (index < this.size) {
                    return this.list[index];
                }
                throw new ArrayIndexOutOfBoundsException();
            }

            public int size() {
                return this.size;
            }
        }

        HandleTable(int initialCapacity) {
            this.lowDep = ObjectInputStream.NULL_HANDLE;
            this.size = 0;
            this.status = new byte[initialCapacity];
            this.entries = new Object[initialCapacity];
            this.deps = new HandleList[initialCapacity];
        }

        int assign(Object obj) {
            if (this.size >= this.entries.length) {
                grow();
            }
            this.status[this.size] = STATUS_UNKNOWN;
            this.entries[this.size] = obj;
            int i = this.size;
            this.size = i + 1;
            return i;
        }

        void markDependency(int dependent, int target) {
            if (dependent != ObjectInputStream.NULL_HANDLE && target != ObjectInputStream.NULL_HANDLE) {
                switch (this.status[dependent]) {
                    case BaseCalendar.MONDAY /*2*/:
                        switch (this.status[target]) {
                            case BaseCalendar.SUNDAY /*1*/:
                                break;
                            case BaseCalendar.MONDAY /*2*/:
                                if (this.deps[target] == null) {
                                    this.deps[target] = new HandleList();
                                }
                                this.deps[target].add(dependent);
                                if (this.lowDep < 0 || this.lowDep > target) {
                                    this.lowDep = target;
                                    break;
                                }
                            case BaseCalendar.TUESDAY /*3*/:
                                markException(dependent, (ClassNotFoundException) this.entries[target]);
                                break;
                            default:
                                throw new InternalError();
                        }
                        break;
                    case BaseCalendar.TUESDAY /*3*/:
                        break;
                    default:
                        throw new InternalError();
                }
            }
        }

        void markException(int handle, ClassNotFoundException ex) {
            switch (this.status[handle]) {
                case BaseCalendar.MONDAY /*2*/:
                    this.status[handle] = STATUS_EXCEPTION;
                    this.entries[handle] = ex;
                    HandleList dlist = this.deps[handle];
                    if (dlist != null) {
                        int ndeps = dlist.size();
                        for (int i = 0; i < ndeps; i++) {
                            markException(dlist.get(i), ex);
                        }
                        this.deps[handle] = null;
                    }
                case BaseCalendar.TUESDAY /*3*/:
                default:
                    throw new InternalError();
            }
        }

        void finish(int handle) {
            int end;
            if (this.lowDep < 0) {
                end = handle + 1;
            } else if (this.lowDep >= handle) {
                end = this.size;
                this.lowDep = ObjectInputStream.NULL_HANDLE;
            } else {
                return;
            }
            for (int i = handle; i < end; i++) {
                switch (this.status[i]) {
                    case BaseCalendar.SUNDAY /*1*/:
                    case BaseCalendar.TUESDAY /*3*/:
                        break;
                    case BaseCalendar.MONDAY /*2*/:
                        this.status[i] = STATUS_OK;
                        this.deps[i] = null;
                        break;
                    default:
                        throw new InternalError();
                }
            }
        }

        void setObject(int handle, Object obj) {
            switch (this.status[handle]) {
                case BaseCalendar.SUNDAY /*1*/:
                case BaseCalendar.MONDAY /*2*/:
                    this.entries[handle] = obj;
                case BaseCalendar.TUESDAY /*3*/:
                default:
                    throw new InternalError();
            }
        }

        Object lookupObject(int handle) {
            return (handle == ObjectInputStream.NULL_HANDLE || this.status[handle] == 3) ? null : this.entries[handle];
        }

        ClassNotFoundException lookupException(int handle) {
            return (handle == ObjectInputStream.NULL_HANDLE || this.status[handle] != 3) ? null : (ClassNotFoundException) this.entries[handle];
        }

        void clear() {
            Arrays.fill(this.status, 0, this.size, (byte) 0);
            Arrays.fill(this.entries, 0, this.size, null);
            Arrays.fill(this.deps, 0, this.size, null);
            this.lowDep = ObjectInputStream.NULL_HANDLE;
            this.size = 0;
        }

        int size() {
            return this.size;
        }

        private void grow() {
            int newCapacity = (this.entries.length << 1) + 1;
            byte[] newStatus = new byte[newCapacity];
            Object newEntries = new Object[newCapacity];
            Object newDeps = new HandleList[newCapacity];
            System.arraycopy(this.status, 0, newStatus, 0, this.size);
            System.arraycopy(this.entries, 0, newEntries, 0, this.size);
            System.arraycopy(this.deps, 0, newDeps, 0, this.size);
            this.status = newStatus;
            this.entries = newEntries;
            this.deps = newDeps;
        }
    }

    private static class PeekInputStream extends InputStream {
        private final InputStream in;
        private int peekb;

        PeekInputStream(InputStream in) {
            this.peekb = ObjectInputStream.NULL_HANDLE;
            this.in = in;
        }

        int peek() throws IOException {
            if (this.peekb >= 0) {
                return this.peekb;
            }
            int read = this.in.read();
            this.peekb = read;
            return read;
        }

        public int read() throws IOException {
            if (this.peekb < 0) {
                return this.in.read();
            }
            int v = this.peekb;
            this.peekb = ObjectInputStream.NULL_HANDLE;
            return v;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (len == 0) {
                return 0;
            }
            if (this.peekb < 0) {
                return this.in.read(b, off, len);
            }
            int off2 = off + 1;
            b[off] = (byte) this.peekb;
            len += ObjectInputStream.NULL_HANDLE;
            this.peekb = ObjectInputStream.NULL_HANDLE;
            int n = this.in.read(b, off2, len);
            return n >= 0 ? n + 1 : 1;
        }

        void readFully(byte[] b, int off, int len) throws IOException {
            int n = 0;
            while (n < len) {
                int count = read(b, off + n, len - n);
                if (count < 0) {
                    throw new EOFException();
                }
                n += count;
            }
        }

        public long skip(long n) throws IOException {
            if (n <= 0) {
                return 0;
            }
            int skipped = 0;
            if (this.peekb >= 0) {
                this.peekb = ObjectInputStream.NULL_HANDLE;
                skipped = 1;
                n--;
            }
            return ((long) skipped) + skip(n);
        }

        public int available() throws IOException {
            int i = 0;
            int available = this.in.available();
            if (this.peekb >= 0) {
                i = 1;
            }
            return i + available;
        }

        public void close() throws IOException {
            this.in.close();
        }
    }

    private static class ValidationList {
        private Callback list;

        /* renamed from: java.io.ObjectInputStream.ValidationList.1 */
        class AnonymousClass1 implements PrivilegedExceptionAction<Void> {
            final /* synthetic */ ValidationList this$1;

            AnonymousClass1(ValidationList this$1) {
                this.this$1 = this$1;
            }

            public /* bridge */ /* synthetic */ Object run() throws Exception {
                return run();
            }

            public Void m0run() throws InvalidObjectException {
                this.this$1.list.obj.validateObject();
                return null;
            }
        }

        private static class Callback {
            final AccessControlContext acc;
            Callback next;
            final ObjectInputValidation obj;
            final int priority;

            Callback(ObjectInputValidation obj, int priority, Callback next, AccessControlContext acc) {
                this.obj = obj;
                this.priority = priority;
                this.next = next;
                this.acc = acc;
            }
        }

        ValidationList() {
        }

        void register(ObjectInputValidation obj, int priority) throws InvalidObjectException {
            if (obj == null) {
                throw new InvalidObjectException("null callback");
            }
            Callback prev = null;
            Callback cur = this.list;
            while (cur != null && priority < cur.priority) {
                prev = cur;
                cur = cur.next;
            }
            AccessControlContext acc = AccessController.getContext();
            if (prev != null) {
                prev.next = new Callback(obj, priority, cur, acc);
            } else {
                this.list = new Callback(obj, priority, this.list, acc);
            }
        }

        void doCallbacks() throws InvalidObjectException {
            while (this.list != null) {
                try {
                    AccessController.doPrivileged(new AnonymousClass1(this), this.list.acc);
                    this.list = this.list.next;
                } catch (PrivilegedActionException ex) {
                    this.list = null;
                    throw ((InvalidObjectException) ex.getException());
                }
            }
        }

        public void clear() {
            this.list = null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.io.ObjectInputStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.io.ObjectInputStream.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.io.ObjectInputStream.<clinit>():void");
    }

    private static native void bytesToDoubles(byte[] bArr, int i, double[] dArr, int i2, int i3);

    private static native void bytesToFloats(byte[] bArr, int i, float[] fArr, int i2, int i3);

    public ObjectInputStream(InputStream in) throws IOException {
        this.passHandle = NULL_HANDLE;
        this.defaultDataEnd = false;
        verifySubclass();
        this.bin = new BlockDataInputStream(in);
        this.handles = new HandleTable(10);
        this.vlist = new ValidationList();
        this.enableOverride = false;
        readStreamHeader();
        this.bin.setBlockDataMode(true);
    }

    protected ObjectInputStream() throws IOException, SecurityException {
        this.passHandle = NULL_HANDLE;
        this.defaultDataEnd = false;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        this.bin = null;
        this.handles = null;
        this.vlist = null;
        this.enableOverride = true;
    }

    public final Object readObject() throws IOException, ClassNotFoundException {
        if (this.enableOverride) {
            return readObjectOverride();
        }
        int outerHandle = this.passHandle;
        try {
            Object obj = readObject0(false);
            this.handles.markDependency(outerHandle, this.passHandle);
            ClassNotFoundException ex = this.handles.lookupException(this.passHandle);
            if (ex != null) {
                throw ex;
            }
            if (this.depth == 0) {
                this.vlist.doCallbacks();
            }
            this.passHandle = outerHandle;
            if (this.closed && this.depth == 0) {
                clear();
            }
            return obj;
        } catch (Throwable th) {
            this.passHandle = outerHandle;
            if (this.closed && this.depth == 0) {
                clear();
            }
        }
    }

    protected Object readObjectOverride() throws IOException, ClassNotFoundException {
        return null;
    }

    public Object readUnshared() throws IOException, ClassNotFoundException {
        int outerHandle = this.passHandle;
        try {
            Object obj = readObject0(true);
            this.handles.markDependency(outerHandle, this.passHandle);
            ClassNotFoundException ex = this.handles.lookupException(this.passHandle);
            if (ex != null) {
                throw ex;
            }
            if (this.depth == 0) {
                this.vlist.doCallbacks();
            }
            this.passHandle = outerHandle;
            if (this.closed && this.depth == 0) {
                clear();
            }
            return obj;
        } catch (Throwable th) {
            this.passHandle = outerHandle;
            if (this.closed && this.depth == 0) {
                clear();
            }
        }
    }

    public void defaultReadObject() throws IOException, ClassNotFoundException {
        if (this.curContext == null) {
            throw new NotActiveException("not in call to readObject");
        }
        Object curObj = this.curContext.getObj();
        ObjectStreamClass curDesc = this.curContext.getDesc();
        this.bin.setBlockDataMode(false);
        defaultReadFields(curObj, curDesc);
        this.bin.setBlockDataMode(true);
        if (!curDesc.hasWriteObjectData()) {
            this.defaultDataEnd = true;
        }
        ClassNotFoundException ex = this.handles.lookupException(this.passHandle);
        if (ex != null) {
            throw ex;
        }
    }

    public GetField readFields() throws IOException, ClassNotFoundException {
        if (this.curContext == null) {
            throw new NotActiveException("not in call to readObject");
        }
        Object curObj = this.curContext.getObj();
        ObjectStreamClass curDesc = this.curContext.getDesc();
        this.bin.setBlockDataMode(false);
        GetFieldImpl getField = new GetFieldImpl(this, curDesc);
        getField.readFields();
        this.bin.setBlockDataMode(true);
        if (!curDesc.hasWriteObjectData()) {
            this.defaultDataEnd = true;
        }
        return getField;
    }

    public void registerValidation(ObjectInputValidation obj, int prio) throws NotActiveException, InvalidObjectException {
        if (this.depth == 0) {
            throw new NotActiveException("stream inactive");
        }
        this.vlist.register(obj, prio);
    }

    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String name = desc.getName();
        try {
            return Class.forName(name, false, latestUserDefinedLoader());
        } catch (ClassNotFoundException ex) {
            Class<?> cl = (Class) primClasses.get(name);
            if (cl != null) {
                return cl;
            }
            throw ex;
        }
    }

    protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
        ClassLoader latestLoader = latestUserDefinedLoader();
        ClassLoader nonPublicLoader = null;
        boolean hasNonPublicInterface = false;
        Class[] classObjs = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class cl = Class.forName(interfaces[i], false, latestLoader);
            if ((cl.getModifiers() & 1) == 0) {
                if (!hasNonPublicInterface) {
                    nonPublicLoader = cl.getClassLoader();
                    hasNonPublicInterface = true;
                } else if (nonPublicLoader != cl.getClassLoader()) {
                    throw new IllegalAccessError("conflicting non-public interface class loaders");
                }
            }
            classObjs[i] = cl;
        }
        if (!hasNonPublicInterface) {
            nonPublicLoader = latestLoader;
        }
        try {
            return Proxy.getProxyClass(nonPublicLoader, classObjs);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

    protected Object resolveObject(Object obj) throws IOException {
        return obj;
    }

    protected boolean enableResolveObject(boolean enable) throws SecurityException {
        if (enable == this.enableResolve) {
            return enable;
        }
        if (enable) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        this.enableResolve = enable;
        return !this.enableResolve;
    }

    protected void readStreamHeader() throws IOException, StreamCorruptedException {
        short s0 = this.bin.readShort();
        short s1 = this.bin.readShort();
        if (s0 != ObjectStreamConstants.STREAM_MAGIC || s1 != (short) 5) {
            throw new StreamCorruptedException(String.format("invalid stream header: %04X%04X", Short.valueOf(s0), Short.valueOf(s1)));
        }
    }

    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass desc = new ObjectStreamClass();
        desc.readNonProxy(this);
        return desc;
    }

    public int read() throws IOException {
        return this.bin.read();
    }

    public int read(byte[] buf, int off, int len) throws IOException {
        if (buf == null) {
            throw new NullPointerException();
        }
        int endoff = off + len;
        if (off >= 0 && len >= 0 && endoff <= buf.length && endoff >= 0) {
            return this.bin.read(buf, off, len, false);
        }
        throw new IndexOutOfBoundsException();
    }

    public int available() throws IOException {
        return this.bin.available();
    }

    public void close() throws IOException {
        this.closed = true;
        if (this.depth == 0) {
            clear();
        }
        this.bin.close();
    }

    public boolean readBoolean() throws IOException {
        return this.bin.readBoolean();
    }

    public byte readByte() throws IOException {
        return this.bin.readByte();
    }

    public int readUnsignedByte() throws IOException {
        return this.bin.readUnsignedByte();
    }

    public char readChar() throws IOException {
        return this.bin.readChar();
    }

    public short readShort() throws IOException {
        return this.bin.readShort();
    }

    public int readUnsignedShort() throws IOException {
        return this.bin.readUnsignedShort();
    }

    public int readInt() throws IOException {
        return this.bin.readInt();
    }

    public long readLong() throws IOException {
        return this.bin.readLong();
    }

    public float readFloat() throws IOException {
        return this.bin.readFloat();
    }

    public double readDouble() throws IOException {
        return this.bin.readDouble();
    }

    public void readFully(byte[] buf) throws IOException {
        this.bin.readFully(buf, 0, buf.length, false);
    }

    public void readFully(byte[] buf, int off, int len) throws IOException {
        int endoff = off + len;
        if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
            throw new IndexOutOfBoundsException();
        }
        this.bin.readFully(buf, off, len, false);
    }

    public int skipBytes(int len) throws IOException {
        return this.bin.skipBytes(len);
    }

    @Deprecated
    public String readLine() throws IOException {
        return this.bin.readLine();
    }

    public String readUTF() throws IOException {
        return this.bin.readUTF();
    }

    private void verifySubclass() {
        Class cl = getClass();
        if (cl != ObjectInputStream.class) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                ObjectStreamClass.processQueue(Caches.subclassAuditsQueue, Caches.subclassAudits);
                WeakClassKey key = new WeakClassKey(cl, Caches.subclassAuditsQueue);
                Boolean result = (Boolean) Caches.subclassAudits.get(key);
                if (result == null) {
                    result = Boolean.valueOf(auditSubclass(cl));
                    Caches.subclassAudits.putIfAbsent(key, result);
                }
                if (!result.booleanValue()) {
                    sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
                }
            }
        }
    }

    private static boolean auditSubclass(Class<?> subcl) {
        return ((Boolean) AccessController.doPrivileged(new AnonymousClass1(subcl))).booleanValue();
    }

    private void clear() {
        this.handles.clear();
        this.vlist.clear();
    }

    private Object readObject0(boolean unshared) throws IOException {
        boolean oldMode = this.bin.getBlockDataMode();
        if (oldMode) {
            int remain = this.bin.currentBlockRemaining();
            if (remain > 0) {
                throw new OptionalDataException(remain);
            } else if (this.defaultDataEnd) {
                throw new OptionalDataException(true);
            } else {
                this.bin.setBlockDataMode(false);
            }
        }
        while (true) {
            byte tc = this.bin.peekByte();
            if (tc != 121) {
                break;
            }
            this.bin.readByte();
            handleReset();
        }
        this.depth++;
        Object readNull;
        switch (tc) {
            case (byte) 112:
                readNull = readNull();
                this.depth += NULL_HANDLE;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            case (byte) 113:
                readNull = readHandle(unshared);
                this.depth += NULL_HANDLE;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            case (byte) 114:
            case (byte) 125:
                ObjectStreamClass readClassDesc = readClassDesc(unshared);
                this.depth += NULL_HANDLE;
                this.bin.setBlockDataMode(oldMode);
                return readClassDesc;
            case (byte) 115:
                readNull = checkResolve(readOrdinaryObject(unshared));
                this.depth += NULL_HANDLE;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            case (byte) 116:
            case (byte) 124:
                readNull = checkResolve(readString(unshared));
                this.depth += NULL_HANDLE;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            case (byte) 117:
                readNull = checkResolve(readArray(unshared));
                this.depth += NULL_HANDLE;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            case (byte) 118:
                Class readClass = readClass(unshared);
                this.depth += NULL_HANDLE;
                this.bin.setBlockDataMode(oldMode);
                return readClass;
            case (byte) 119:
            case (byte) 122:
                if (oldMode) {
                    this.bin.setBlockDataMode(true);
                    this.bin.peek();
                    throw new OptionalDataException(this.bin.currentBlockRemaining());
                }
                throw new StreamCorruptedException("unexpected block data");
            case (byte) 120:
                if (oldMode) {
                    throw new OptionalDataException(true);
                }
                throw new StreamCorruptedException("unexpected end of block data");
            case (byte) 123:
                throw new WriteAbortedException("writing aborted", readFatalException());
            case (byte) 126:
                readNull = checkResolve(readEnum(unshared));
                this.depth += NULL_HANDLE;
                this.bin.setBlockDataMode(oldMode);
                return readNull;
            default:
                try {
                    throw new StreamCorruptedException(String.format("invalid type code: %02X", Byte.valueOf(tc)));
                } catch (Throwable th) {
                    this.depth += NULL_HANDLE;
                    this.bin.setBlockDataMode(oldMode);
                }
        }
        this.depth += NULL_HANDLE;
        this.bin.setBlockDataMode(oldMode);
    }

    private Object checkResolve(Object obj) throws IOException {
        if (!this.enableResolve || this.handles.lookupException(this.passHandle) != null) {
            return obj;
        }
        Object rep = resolveObject(obj);
        if (rep != obj) {
            this.handles.setObject(this.passHandle, rep);
        }
        return rep;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    String readTypeString() throws IOException {
        int oldHandle = this.passHandle;
        try {
            String str;
            switch (this.bin.peekByte()) {
                case (byte) 112:
                    str = (String) readNull();
                    break;
                case (byte) 113:
                    str = (String) readHandle(false);
                    this.passHandle = oldHandle;
                    return str;
                case (byte) 116:
                case (byte) 124:
                    str = readString(false);
                    this.passHandle = oldHandle;
                    return str;
                default:
                    throw new StreamCorruptedException(String.format("invalid type code: %02X", Byte.valueOf(this.bin.peekByte())));
            }
        } finally {
            this.passHandle = oldHandle;
        }
        this.passHandle = oldHandle;
    }

    private Object readNull() throws IOException {
        if (this.bin.readByte() != 112) {
            throw new InternalError();
        }
        this.passHandle = NULL_HANDLE;
        return null;
    }

    private Object readHandle(boolean unshared) throws IOException {
        if (this.bin.readByte() != 113) {
            throw new InternalError();
        }
        this.passHandle = this.bin.readInt() - ObjectStreamConstants.baseWireHandle;
        if (this.passHandle < 0 || this.passHandle >= this.handles.size()) {
            throw new StreamCorruptedException(String.format("invalid handle value: %08X", Integer.valueOf(this.passHandle + ObjectStreamConstants.baseWireHandle)));
        } else if (unshared) {
            throw new InvalidObjectException("cannot read back reference as unshared");
        } else {
            Object obj = this.handles.lookupObject(this.passHandle);
            if (obj != unsharedMarker) {
                return obj;
            }
            throw new InvalidObjectException("cannot read back reference to unshared object");
        }
    }

    private Class readClass(boolean unshared) throws IOException {
        if (this.bin.readByte() != 118) {
            throw new InternalError();
        }
        Object obj;
        ObjectStreamClass desc = readClassDesc(false);
        Class cl = desc.forClass();
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = unsharedMarker;
        } else {
            Class cls = cl;
        }
        this.passHandle = handleTable.assign(obj);
        ClassNotFoundException resolveEx = desc.getResolveException();
        if (resolveEx != null) {
            this.handles.markException(this.passHandle, resolveEx);
        }
        this.handles.finish(this.passHandle);
        return cl;
    }

    private ObjectStreamClass readClassDesc(boolean unshared) throws IOException {
        switch (this.bin.peekByte()) {
            case (byte) 112:
                return (ObjectStreamClass) readNull();
            case (byte) 113:
                return (ObjectStreamClass) readHandle(unshared);
            case (byte) 114:
                return readNonProxyDesc(unshared);
            case (byte) 125:
                return readProxyDesc(unshared);
            default:
                throw new StreamCorruptedException(String.format("invalid type code: %02X", Byte.valueOf(this.bin.peekByte())));
        }
    }

    private boolean isCustomSubclass() {
        return getClass().getClassLoader() != ObjectInputStream.class.getClassLoader();
    }

    private ObjectStreamClass readProxyDesc(boolean unshared) throws IOException {
        if (this.bin.readByte() != 125) {
            throw new InternalError();
        }
        Object obj;
        ObjectStreamClass desc = new ObjectStreamClass();
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = unsharedMarker;
        } else {
            ObjectStreamClass objectStreamClass = desc;
        }
        int descHandle = handleTable.assign(obj);
        this.passHandle = NULL_HANDLE;
        int numIfaces = this.bin.readInt();
        String[] ifaces = new String[numIfaces];
        for (int i = 0; i < numIfaces; i++) {
            ifaces[i] = this.bin.readUTF();
        }
        Class cls = null;
        ClassNotFoundException classNotFoundException = null;
        this.bin.setBlockDataMode(true);
        try {
            cls = resolveProxyClass(ifaces);
            if (cls == null) {
                classNotFoundException = new ClassNotFoundException("null class");
            } else if (Proxy.isProxyClass(cls)) {
                ReflectUtil.checkProxyPackageAccess(getClass().getClassLoader(), cls.getInterfaces());
            } else {
                throw new InvalidClassException("Not a proxy");
            }
        } catch (ClassNotFoundException ex) {
            classNotFoundException = ex;
        }
        skipCustomData();
        desc.initProxy(cls, classNotFoundException, readClassDesc(false));
        this.handles.finish(descHandle);
        this.passHandle = descHandle;
        return desc;
    }

    private ObjectStreamClass readNonProxyDesc(boolean unshared) throws IOException {
        if (this.bin.readByte() != 114) {
            throw new InternalError();
        }
        Object obj;
        ObjectStreamClass desc = new ObjectStreamClass();
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = unsharedMarker;
        } else {
            ObjectStreamClass objectStreamClass = desc;
        }
        int descHandle = handleTable.assign(obj);
        this.passHandle = NULL_HANDLE;
        try {
            ObjectStreamClass readDesc = readClassDescriptor();
            Class cls = null;
            ClassNotFoundException classNotFoundException = null;
            this.bin.setBlockDataMode(true);
            boolean checksRequired = isCustomSubclass();
            try {
                cls = resolveClass(readDesc);
                if (cls == null) {
                    classNotFoundException = new ClassNotFoundException("null class");
                } else if (checksRequired) {
                    ReflectUtil.checkPackageAccess(cls);
                }
            } catch (ClassNotFoundException ex) {
                classNotFoundException = ex;
            }
            skipCustomData();
            desc.initNonProxy(readDesc, cls, classNotFoundException, readClassDesc(false));
            this.handles.finish(descHandle);
            this.passHandle = descHandle;
            return desc;
        } catch (ClassNotFoundException ex2) {
            throw ((IOException) new InvalidClassException("failed to read class descriptor").initCause(ex2));
        }
    }

    private String readString(boolean unshared) throws IOException {
        String str;
        Object obj;
        switch (this.bin.readByte()) {
            case (byte) 116:
                str = this.bin.readUTF();
                break;
            case (byte) 124:
                str = this.bin.readLongUTF();
                break;
            default:
                throw new StreamCorruptedException(String.format("invalid type code: %02X", Byte.valueOf(this.bin.readByte())));
        }
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = unsharedMarker;
        } else {
            String str2 = str;
        }
        this.passHandle = handleTable.assign(obj);
        this.handles.finish(this.passHandle);
        return str;
    }

    private Object readArray(boolean unshared) throws IOException {
        if (this.bin.readByte() != 117) {
            throw new InternalError();
        }
        Object obj;
        ObjectStreamClass desc = readClassDesc(false);
        int len = this.bin.readInt();
        Object array = null;
        Class cls = null;
        Class cl = desc.forClass();
        if (cl != null) {
            cls = cl.getComponentType();
            array = Array.newInstance(cls, len);
        }
        HandleTable handleTable = this.handles;
        if (unshared) {
            obj = unsharedMarker;
        } else {
            obj = array;
        }
        int arrayHandle = handleTable.assign(obj);
        ClassNotFoundException resolveEx = desc.getResolveException();
        if (resolveEx != null) {
            this.handles.markException(arrayHandle, resolveEx);
        }
        int i;
        if (cls == null) {
            for (i = 0; i < len; i++) {
                readObject0(false);
            }
        } else if (!cls.isPrimitive()) {
            Object[] oa = (Object[]) array;
            for (i = 0; i < len; i++) {
                oa[i] = readObject0(false);
                this.handles.markDependency(arrayHandle, this.passHandle);
            }
        } else if (cls == Integer.TYPE) {
            this.bin.readInts((int[]) array, 0, len);
        } else if (cls == Byte.TYPE) {
            this.bin.readFully((byte[]) array, 0, len, true);
        } else if (cls == Long.TYPE) {
            this.bin.readLongs((long[]) array, 0, len);
        } else if (cls == Float.TYPE) {
            this.bin.readFloats((float[]) array, 0, len);
        } else if (cls == Double.TYPE) {
            this.bin.readDoubles((double[]) array, 0, len);
        } else if (cls == Short.TYPE) {
            this.bin.readShorts((short[]) array, 0, len);
        } else if (cls == Character.TYPE) {
            this.bin.readChars((char[]) array, 0, len);
        } else if (cls == Boolean.TYPE) {
            this.bin.readBooleans((boolean[]) array, 0, len);
        } else {
            throw new InternalError();
        }
        this.handles.finish(arrayHandle);
        this.passHandle = arrayHandle;
        return array;
    }

    private Enum readEnum(boolean unshared) throws IOException {
        Object obj = null;
        if (this.bin.readByte() != 126) {
            throw new InternalError();
        }
        Object desc = readClassDesc(false);
        if (desc.isEnum()) {
            HandleTable handleTable = this.handles;
            if (unshared) {
                obj = unsharedMarker;
            }
            int enumHandle = handleTable.assign(obj);
            ClassNotFoundException resolveEx = desc.getResolveException();
            if (resolveEx != null) {
                this.handles.markException(enumHandle, resolveEx);
            }
            String name = readString(false);
            Enum en = null;
            Object cl = desc.forClass();
            if (cl != null) {
                try {
                    en = Enum.valueOf(cl, name);
                    if (!unshared) {
                        this.handles.setObject(enumHandle, en);
                    }
                } catch (IllegalArgumentException ex) {
                    throw ((IOException) new InvalidObjectException("enum constant " + name + " does not exist in " + cl).initCause(ex));
                }
            }
            this.handles.finish(enumHandle);
            this.passHandle = enumHandle;
            return en;
        }
        throw new InvalidClassException("non-enum class: " + desc);
    }

    private Object readOrdinaryObject(boolean unshared) throws IOException {
        if (this.bin.readByte() != 115) {
            throw new InternalError();
        }
        ObjectStreamClass desc = readClassDesc(false);
        desc.checkDeserialize();
        Class<?> cl = desc.forClass();
        if (cl == String.class || cl == Class.class || cl == ObjectStreamClass.class) {
            throw new InvalidClassException("invalid class descriptor");
        }
        try {
            Object obj;
            Object obj2 = desc.isInstantiable() ? desc.newInstance() : null;
            HandleTable handleTable = this.handles;
            if (unshared) {
                obj = unsharedMarker;
            } else {
                obj = obj2;
            }
            this.passHandle = handleTable.assign(obj);
            ClassNotFoundException resolveEx = desc.getResolveException();
            if (resolveEx != null) {
                this.handles.markException(this.passHandle, resolveEx);
            }
            if (desc.isExternalizable()) {
                readExternalData((Externalizable) obj2, desc);
            } else {
                readSerialData(obj2, desc);
            }
            this.handles.finish(this.passHandle);
            if (obj2 == null || this.handles.lookupException(this.passHandle) != null || !desc.hasReadResolveMethod()) {
                return obj2;
            }
            Object rep = desc.invokeReadResolve(obj2);
            if (unshared && rep.getClass().isArray()) {
                rep = cloneArray(rep);
            }
            if (rep == obj2) {
                return obj2;
            }
            obj2 = rep;
            this.handles.setObject(this.passHandle, rep);
            return obj2;
        } catch (Exception ex) {
            throw ((IOException) new InvalidClassException(desc.forClass().getName(), "unable to create instance").initCause(ex));
        }
    }

    private void readExternalData(Externalizable obj, ObjectStreamClass desc) throws IOException {
        boolean blocked;
        SerialCallbackContext oldContext = this.curContext;
        this.curContext = null;
        try {
            blocked = desc.hasBlockExternalData();
            if (blocked) {
                this.bin.setBlockDataMode(true);
            }
            if (obj != null) {
                obj.readExternal(this);
            }
        } catch (ClassNotFoundException ex) {
            this.handles.markException(this.passHandle, ex);
        } catch (Throwable th) {
            this.curContext = oldContext;
        }
        if (blocked) {
            skipCustomData();
        }
        this.curContext = oldContext;
    }

    private void readSerialData(Object obj, ObjectStreamClass desc) throws IOException {
        ClassDataSlot[] slots = desc.getClassDataLayout();
        for (int i = 0; i < slots.length; i++) {
            ObjectStreamClass slotDesc = slots[i].desc;
            if (slots[i].hasData) {
                if (obj != null && slotDesc.hasReadObjectMethod() && this.handles.lookupException(this.passHandle) == null) {
                    SerialCallbackContext oldContext = this.curContext;
                    try {
                        this.curContext = new SerialCallbackContext(obj, slotDesc);
                        this.bin.setBlockDataMode(true);
                        slotDesc.invokeReadObject(obj, this);
                        this.curContext.setUsed();
                    } catch (ClassNotFoundException ex) {
                        this.handles.markException(this.passHandle, ex);
                        this.curContext.setUsed();
                    } catch (Throwable th) {
                        this.curContext.setUsed();
                        this.curContext = oldContext;
                    }
                    this.curContext = oldContext;
                    this.defaultDataEnd = false;
                } else {
                    defaultReadFields(obj, slotDesc);
                }
                if (slotDesc.hasWriteObjectData()) {
                    skipCustomData();
                } else {
                    this.bin.setBlockDataMode(false);
                }
            } else if (obj != null && slotDesc.hasReadObjectNoDataMethod() && this.handles.lookupException(this.passHandle) == null) {
                slotDesc.invokeReadObjectNoData(obj);
            }
        }
    }

    private void skipCustomData() throws IOException {
        int oldHandle = this.passHandle;
        while (true) {
            if (this.bin.getBlockDataMode()) {
                this.bin.skipBlockData();
                this.bin.setBlockDataMode(false);
            }
            switch (this.bin.peekByte()) {
                case (byte) 119:
                case (byte) 122:
                    this.bin.setBlockDataMode(true);
                    break;
                case (byte) 120:
                    this.bin.readByte();
                    this.passHandle = oldHandle;
                    return;
                default:
                    readObject0(false);
                    break;
            }
        }
    }

    private void defaultReadFields(Object obj, ObjectStreamClass desc) throws IOException {
        Class cl = desc.forClass();
        if (cl == null || obj == null || cl.isInstance(obj)) {
            int primDataSize = desc.getPrimDataSize();
            if (this.primVals == null || this.primVals.length < primDataSize) {
                this.primVals = new byte[primDataSize];
            }
            this.bin.readFully(this.primVals, 0, primDataSize, false);
            if (obj != null) {
                desc.setPrimFieldValues(obj, this.primVals);
            }
            int objHandle = this.passHandle;
            ObjectStreamField[] fields = desc.getFields(false);
            Object[] objVals = new Object[desc.getNumObjFields()];
            int numPrimFields = fields.length - objVals.length;
            for (int i = 0; i < objVals.length; i++) {
                ObjectStreamField f = fields[numPrimFields + i];
                objVals[i] = readObject0(f.isUnshared());
                if (f.getField() != null) {
                    this.handles.markDependency(objHandle, this.passHandle);
                }
            }
            if (obj != null) {
                desc.setObjFieldValues(obj, objVals);
            }
            this.passHandle = objHandle;
            return;
        }
        throw new ClassCastException();
    }

    private IOException readFatalException() throws IOException {
        if (this.bin.readByte() != 123) {
            throw new InternalError();
        }
        clear();
        IOException e = (IOException) readObject0(false);
        clear();
        return e;
    }

    private void handleReset() throws StreamCorruptedException {
        if (this.depth > 0) {
            throw new StreamCorruptedException("unexpected reset; recursion depth: " + this.depth);
        }
        clear();
    }

    private static ClassLoader latestUserDefinedLoader() {
        return VMStack.getClosestUserClassLoader();
    }

    private static Object cloneArray(Object array) {
        if (array instanceof Object[]) {
            return ((Object[]) array).clone();
        }
        if (array instanceof boolean[]) {
            return ((boolean[]) array).clone();
        }
        if (array instanceof byte[]) {
            return ((byte[]) array).clone();
        }
        if (array instanceof char[]) {
            return ((char[]) array).clone();
        }
        if (array instanceof double[]) {
            return ((double[]) array).clone();
        }
        if (array instanceof float[]) {
            return ((float[]) array).clone();
        }
        if (array instanceof int[]) {
            return ((int[]) array).clone();
        }
        if (array instanceof long[]) {
            return ((long[]) array).clone();
        }
        if (array instanceof short[]) {
            return ((short[]) array).clone();
        }
        throw new AssertionError();
    }
}
