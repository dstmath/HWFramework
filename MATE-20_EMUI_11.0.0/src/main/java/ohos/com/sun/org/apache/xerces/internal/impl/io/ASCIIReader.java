package ohos.com.sun.org.apache.xerces.internal.impl.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Locale;
import ohos.com.sun.org.apache.xerces.internal.util.MessageFormatter;
import ohos.com.sun.xml.internal.stream.util.ThreadLocalBufferAllocator;

public class ASCIIReader extends Reader {
    public static final int DEFAULT_BUFFER_SIZE = 2048;
    protected byte[] fBuffer;
    private MessageFormatter fFormatter;
    protected InputStream fInputStream;
    private Locale fLocale;

    @Override // java.io.Reader
    public boolean ready() throws IOException {
        return false;
    }

    public ASCIIReader(InputStream inputStream, MessageFormatter messageFormatter, Locale locale) {
        this(inputStream, 2048, messageFormatter, locale);
    }

    public ASCIIReader(InputStream inputStream, int i, MessageFormatter messageFormatter, Locale locale) {
        this.fFormatter = null;
        this.fLocale = null;
        this.fInputStream = inputStream;
        this.fBuffer = ThreadLocalBufferAllocator.getBufferAllocator().getByteBuffer(i);
        if (this.fBuffer == null) {
            this.fBuffer = new byte[i];
        }
        this.fFormatter = messageFormatter;
        this.fLocale = locale;
    }

    @Override // java.io.Reader
    public int read() throws IOException {
        int read = this.fInputStream.read();
        if (read < 128) {
            return read;
        }
        throw new MalformedByteSequenceException(this.fFormatter, this.fLocale, "http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidASCII", new Object[]{Integer.toString(read)});
    }

    @Override // java.io.Reader
    public int read(char[] cArr, int i, int i2) throws IOException {
        byte[] bArr = this.fBuffer;
        if (i2 > bArr.length) {
            i2 = bArr.length;
        }
        int read = this.fInputStream.read(this.fBuffer, 0, i2);
        for (int i3 = 0; i3 < read; i3++) {
            byte b = this.fBuffer[i3];
            if (b >= 0) {
                cArr[i + i3] = (char) b;
            } else {
                throw new MalformedByteSequenceException(this.fFormatter, this.fLocale, "http://www.w3.org/TR/1998/REC-xml-19980210", "InvalidASCII", new Object[]{Integer.toString(b & 255)});
            }
        }
        return read;
    }

    @Override // java.io.Reader
    public long skip(long j) throws IOException {
        return this.fInputStream.skip(j);
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
