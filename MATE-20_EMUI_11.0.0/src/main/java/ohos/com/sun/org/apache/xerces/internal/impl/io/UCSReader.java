package ohos.com.sun.org.apache.xerces.internal.impl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import ohos.com.sun.xml.internal.stream.util.ThreadLocalBufferAllocator;

public class UCSReader extends Reader {
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final short UCS2BE = 2;
    public static final short UCS2LE = 1;
    public static final short UCS4BE = 8;
    public static final short UCS4LE = 4;
    protected byte[] fBuffer;
    protected short fEncoding;
    protected InputStream fInputStream;

    @Override // java.io.Reader
    public boolean ready() throws IOException {
        return false;
    }

    public UCSReader(InputStream inputStream, short s) {
        this(inputStream, 8192, s);
    }

    public UCSReader(InputStream inputStream, int i, short s) {
        this.fInputStream = inputStream;
        this.fBuffer = ThreadLocalBufferAllocator.getBufferAllocator().getByteBuffer(i);
        if (this.fBuffer == null) {
            this.fBuffer = new byte[i];
        }
        this.fEncoding = s;
    }

    @Override // java.io.Reader
    public int read() throws IOException {
        int read;
        int read2;
        int read3 = this.fInputStream.read() & 255;
        if (read3 == 255 || (read = this.fInputStream.read() & 255) == 255) {
            return -1;
        }
        short s = this.fEncoding;
        if (s < 4) {
            return s == 2 ? (read3 << 8) + read : (read << 8) + read3;
        }
        int read4 = this.fInputStream.read() & 255;
        if (read4 == 255 || (read2 = this.fInputStream.read() & 255) == 255) {
            return -1;
        }
        PrintStream printStream = System.err;
        printStream.println("b0 is " + (read3 & 255) + " b1 " + (read & 255) + " b2 " + (read4 & 255) + " b3 " + (read2 & 255));
        return this.fEncoding == 8 ? (read3 << 24) + (read << 16) + (read4 << 8) + read2 : (read2 << 24) + (read4 << 16) + (read << 8) + read3;
    }

    @Override // java.io.Reader
    public int read(char[] cArr, int i, int i2) throws IOException {
        int i3 = 1;
        int i4 = i2 << (this.fEncoding >= 4 ? 2 : 1);
        byte[] bArr = this.fBuffer;
        if (i4 > bArr.length) {
            i4 = bArr.length;
        }
        int read = this.fInputStream.read(this.fBuffer, 0, i4);
        if (read == -1) {
            return -1;
        }
        if (this.fEncoding >= 4) {
            int i5 = (4 - (read & 3)) & 3;
            int i6 = 0;
            while (true) {
                if (i6 >= i5) {
                    break;
                }
                int read2 = this.fInputStream.read();
                if (read2 == -1) {
                    while (i6 < i5) {
                        this.fBuffer[read + i6] = 0;
                        i6++;
                    }
                } else {
                    this.fBuffer[read + i6] = (byte) read2;
                    i6++;
                }
            }
            read += i5;
        } else if ((read & 1) != 0) {
            read++;
            int read3 = this.fInputStream.read();
            if (read3 == -1) {
                this.fBuffer[read] = 0;
            } else {
                this.fBuffer[read] = (byte) read3;
            }
        }
        if (this.fEncoding >= 4) {
            i3 = 2;
        }
        int i7 = read >> i3;
        int i8 = 0;
        for (int i9 = 0; i9 < i7; i9++) {
            byte[] bArr2 = this.fBuffer;
            int i10 = i8 + 1;
            int i11 = bArr2[i8] & 255;
            int i12 = i10 + 1;
            int i13 = bArr2[i10] & 255;
            short s = this.fEncoding;
            if (s >= 4) {
                int i14 = i12 + 1;
                int i15 = bArr2[i12] & 255;
                int i16 = i14 + 1;
                int i17 = bArr2[i14] & 255;
                if (s == 8) {
                    cArr[i + i9] = (char) ((i11 << 24) + (i13 << 16) + (i15 << 8) + i17);
                } else {
                    cArr[i + i9] = (char) ((i17 << 24) + (i15 << 16) + (i13 << 8) + i11);
                }
                i8 = i16;
            } else {
                if (s == 2) {
                    cArr[i + i9] = (char) ((i11 << 8) + i13);
                } else {
                    cArr[i + i9] = (char) ((i13 << 8) + i11);
                }
                i8 = i12;
            }
        }
        return i7;
    }

    @Override // java.io.Reader
    public long skip(long j) throws IOException {
        int i = this.fEncoding >= 4 ? 2 : 1;
        long skip = this.fInputStream.skip(j << i);
        return (((long) (i | 1)) & skip) == 0 ? skip >> i : (skip >> i) + 1;
    }

    @Override // java.io.Reader
    public boolean markSupported() {
        return this.fInputStream.markSupported();
    }

    @Override // java.io.Reader
    public void mark(int i) throws IOException {
        this.fInputStream.mark(i);
    }

    @Override // java.io.Reader
    public void reset() throws IOException {
        this.fInputStream.reset();
    }

    @Override // java.io.Reader, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        ThreadLocalBufferAllocator.getBufferAllocator().returnByteBuffer(this.fBuffer);
        this.fBuffer = null;
        this.fInputStream.close();
    }
}
