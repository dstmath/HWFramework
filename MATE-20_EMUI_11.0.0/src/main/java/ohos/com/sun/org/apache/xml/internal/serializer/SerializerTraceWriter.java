package ohos.com.sun.org.apache.xml.internal.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

final class SerializerTraceWriter extends Writer implements WriterChain {
    private byte[] buf;
    private int buf_length;
    private int count;
    private final SerializerTrace m_tracer;
    private final Writer m_writer;

    private void setBufferSize(int i) {
        this.buf = new byte[(i + 3)];
        this.buf_length = i;
        this.count = 0;
    }

    public SerializerTraceWriter(Writer writer, SerializerTrace serializerTrace) {
        this.m_writer = writer;
        this.m_tracer = serializerTrace;
        setBufferSize(1024);
    }

    private void flushBuffer() throws IOException {
        int i = this.count;
        if (i > 0) {
            char[] cArr = new char[i];
            for (int i2 = 0; i2 < this.count; i2++) {
                cArr[i2] = (char) this.buf[i2];
            }
            SerializerTrace serializerTrace = this.m_tracer;
            if (serializerTrace != null) {
                serializerTrace.fireGenerateEvent(12, cArr, 0, cArr.length);
            }
            this.count = 0;
        }
    }

    @Override // java.io.Writer, java.io.Flushable, ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    public void flush() throws IOException {
        Writer writer = this.m_writer;
        if (writer != null) {
            writer.flush();
        }
        flushBuffer();
    }

    @Override // java.io.Writer, java.io.Closeable, java.lang.AutoCloseable, ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    public void close() throws IOException {
        Writer writer = this.m_writer;
        if (writer != null) {
            writer.close();
        }
        flushBuffer();
    }

    @Override // java.io.Writer, ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    public void write(int i) throws IOException {
        Writer writer = this.m_writer;
        if (writer != null) {
            writer.write(i);
        }
        if (this.count >= this.buf_length) {
            flushBuffer();
        }
        if (i < 128) {
            byte[] bArr = this.buf;
            int i2 = this.count;
            this.count = i2 + 1;
            bArr[i2] = (byte) i;
        } else if (i < 2048) {
            byte[] bArr2 = this.buf;
            int i3 = this.count;
            this.count = i3 + 1;
            bArr2[i3] = (byte) ((i >> 6) + 192);
            int i4 = this.count;
            this.count = i4 + 1;
            bArr2[i4] = (byte) ((i & 63) + 128);
        } else {
            byte[] bArr3 = this.buf;
            int i5 = this.count;
            this.count = i5 + 1;
            bArr3[i5] = (byte) ((i >> 12) + 224);
            int i6 = this.count;
            this.count = i6 + 1;
            bArr3[i6] = (byte) (((i >> 6) & 63) + 128);
            int i7 = this.count;
            this.count = i7 + 1;
            bArr3[i7] = (byte) ((i & 63) + 128);
        }
    }

    @Override // java.io.Writer, ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    public void write(char[] cArr, int i, int i2) throws IOException {
        Writer writer = this.m_writer;
        if (writer != null) {
            writer.write(cArr, i, i2);
        }
        int i3 = (i2 << 1) + i2;
        if (i3 >= this.buf_length) {
            flushBuffer();
            setBufferSize(i3 * 2);
        }
        if (i3 > this.buf_length - this.count) {
            flushBuffer();
        }
        int i4 = i2 + i;
        while (i < i4) {
            char c = cArr[i];
            if (c < 128) {
                byte[] bArr = this.buf;
                int i5 = this.count;
                this.count = i5 + 1;
                bArr[i5] = (byte) c;
            } else if (c < 2048) {
                byte[] bArr2 = this.buf;
                int i6 = this.count;
                this.count = i6 + 1;
                bArr2[i6] = (byte) ((c >> 6) + 192);
                int i7 = this.count;
                this.count = i7 + 1;
                bArr2[i7] = (byte) ((c & '?') + 128);
            } else {
                byte[] bArr3 = this.buf;
                int i8 = this.count;
                this.count = i8 + 1;
                bArr3[i8] = (byte) ((c >> '\f') + 224);
                int i9 = this.count;
                this.count = i9 + 1;
                bArr3[i9] = (byte) (((c >> 6) & 63) + 128);
                int i10 = this.count;
                this.count = i10 + 1;
                bArr3[i10] = (byte) ((c & '?') + 128);
            }
            i++;
        }
    }

    @Override // java.io.Writer, ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    public void write(String str) throws IOException {
        Writer writer = this.m_writer;
        if (writer != null) {
            writer.write(str);
        }
        int length = str.length();
        int i = (length << 1) + length;
        if (i >= this.buf_length) {
            flushBuffer();
            setBufferSize(i * 2);
        }
        if (i > this.buf_length - this.count) {
            flushBuffer();
        }
        for (int i2 = 0; i2 < length; i2++) {
            char charAt = str.charAt(i2);
            if (charAt < 128) {
                byte[] bArr = this.buf;
                int i3 = this.count;
                this.count = i3 + 1;
                bArr[i3] = (byte) charAt;
            } else if (charAt < 2048) {
                byte[] bArr2 = this.buf;
                int i4 = this.count;
                this.count = i4 + 1;
                bArr2[i4] = (byte) ((charAt >> 6) + 192);
                int i5 = this.count;
                this.count = i5 + 1;
                bArr2[i5] = (byte) ((charAt & '?') + 128);
            } else {
                byte[] bArr3 = this.buf;
                int i6 = this.count;
                this.count = i6 + 1;
                bArr3[i6] = (byte) ((charAt >> '\f') + 224);
                int i7 = this.count;
                this.count = i7 + 1;
                bArr3[i7] = (byte) (((charAt >> 6) & 63) + 128);
                int i8 = this.count;
                this.count = i8 + 1;
                bArr3[i8] = (byte) ((charAt & '?') + 128);
            }
        }
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    public Writer getWriter() {
        return this.m_writer;
    }

    @Override // ohos.com.sun.org.apache.xml.internal.serializer.WriterChain
    public OutputStream getOutputStream() {
        Writer writer = this.m_writer;
        if (writer instanceof WriterChain) {
            return ((WriterChain) writer).getOutputStream();
        }
        return null;
    }
}
