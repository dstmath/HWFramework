package org.apache.xml.serializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

final class SerializerTraceWriter extends Writer implements WriterChain {
    private byte[] buf;
    private int buf_length;
    private int count;
    private final SerializerTrace m_tracer;
    private final Writer m_writer;

    private void setBufferSize(int size) {
        this.buf = new byte[(size + 3)];
        this.buf_length = size;
        this.count = 0;
    }

    public SerializerTraceWriter(Writer out, SerializerTrace tracer) {
        this.m_writer = out;
        this.m_tracer = tracer;
        setBufferSize(1024);
    }

    private void flushBuffer() throws IOException {
        if (this.count > 0) {
            char[] chars = new char[this.count];
            for (int i = 0; i < this.count; i++) {
                chars[i] = (char) this.buf[i];
            }
            if (this.m_tracer != null) {
                this.m_tracer.fireGenerateEvent(12, chars, 0, chars.length);
            }
            this.count = 0;
        }
    }

    public void flush() throws IOException {
        if (this.m_writer != null) {
            this.m_writer.flush();
        }
        flushBuffer();
    }

    public void close() throws IOException {
        if (this.m_writer != null) {
            this.m_writer.close();
        }
        flushBuffer();
    }

    public void write(int c) throws IOException {
        if (this.m_writer != null) {
            this.m_writer.write(c);
        }
        if (this.count >= this.buf_length) {
            flushBuffer();
        }
        if (c < 128) {
            byte[] bArr = this.buf;
            int i = this.count;
            this.count = i + 1;
            bArr[i] = (byte) c;
        } else if (c < 2048) {
            byte[] bArr2 = this.buf;
            int i2 = this.count;
            this.count = i2 + 1;
            bArr2[i2] = (byte) (192 + (c >> 6));
            byte[] bArr3 = this.buf;
            int i3 = this.count;
            this.count = i3 + 1;
            bArr3[i3] = (byte) (128 + (c & 63));
        } else {
            byte[] bArr4 = this.buf;
            int i4 = this.count;
            this.count = i4 + 1;
            bArr4[i4] = (byte) (224 + (c >> 12));
            byte[] bArr5 = this.buf;
            int i5 = this.count;
            this.count = i5 + 1;
            bArr5[i5] = (byte) (((c >> 6) & 63) + 128);
            byte[] bArr6 = this.buf;
            int i6 = this.count;
            this.count = i6 + 1;
            bArr6[i6] = (byte) (128 + (c & 63));
        }
    }

    public void write(char[] chars, int start, int length) throws IOException {
        if (this.m_writer != null) {
            this.m_writer.write(chars, start, length);
        }
        int lengthx3 = (length << 1) + length;
        if (lengthx3 >= this.buf_length) {
            flushBuffer();
            setBufferSize(2 * lengthx3);
        }
        if (lengthx3 > this.buf_length - this.count) {
            flushBuffer();
        }
        int n = length + start;
        for (int i = start; i < n; i++) {
            char c = chars[i];
            if (c < 128) {
                byte[] bArr = this.buf;
                int i2 = this.count;
                this.count = i2 + 1;
                bArr[i2] = (byte) c;
            } else if (c < 2048) {
                byte[] bArr2 = this.buf;
                int i3 = this.count;
                this.count = i3 + 1;
                bArr2[i3] = (byte) (192 + (c >> 6));
                byte[] bArr3 = this.buf;
                int i4 = this.count;
                this.count = i4 + 1;
                bArr3[i4] = (byte) (128 + (c & '?'));
            } else {
                byte[] bArr4 = this.buf;
                int i5 = this.count;
                this.count = i5 + 1;
                bArr4[i5] = (byte) (224 + (c >> 12));
                byte[] bArr5 = this.buf;
                int i6 = this.count;
                this.count = i6 + 1;
                bArr5[i6] = (byte) (((c >> 6) & 63) + 128);
                byte[] bArr6 = this.buf;
                int i7 = this.count;
                this.count = i7 + 1;
                bArr6[i7] = (byte) (128 + (c & '?'));
            }
        }
    }

    public void write(String s) throws IOException {
        if (this.m_writer != null) {
            this.m_writer.write(s);
        }
        int length = s.length();
        int lengthx3 = (length << 1) + length;
        if (lengthx3 >= this.buf_length) {
            flushBuffer();
            setBufferSize(2 * lengthx3);
        }
        if (lengthx3 > this.buf_length - this.count) {
            flushBuffer();
        }
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            if (c < 128) {
                byte[] bArr = this.buf;
                int i2 = this.count;
                this.count = i2 + 1;
                bArr[i2] = (byte) c;
            } else if (c < 2048) {
                byte[] bArr2 = this.buf;
                int i3 = this.count;
                this.count = i3 + 1;
                bArr2[i3] = (byte) (192 + (c >> 6));
                byte[] bArr3 = this.buf;
                int i4 = this.count;
                this.count = i4 + 1;
                bArr3[i4] = (byte) (128 + (c & '?'));
            } else {
                byte[] bArr4 = this.buf;
                int i5 = this.count;
                this.count = i5 + 1;
                bArr4[i5] = (byte) (224 + (c >> 12));
                byte[] bArr5 = this.buf;
                int i6 = this.count;
                this.count = i6 + 1;
                bArr5[i6] = (byte) (((c >> 6) & 63) + 128);
                byte[] bArr6 = this.buf;
                int i7 = this.count;
                this.count = i7 + 1;
                bArr6[i7] = (byte) (128 + (c & '?'));
            }
        }
    }

    public Writer getWriter() {
        return this.m_writer;
    }

    public OutputStream getOutputStream() {
        if (this.m_writer instanceof WriterChain) {
            return ((WriterChain) this.m_writer).getOutputStream();
        }
        return null;
    }
}
