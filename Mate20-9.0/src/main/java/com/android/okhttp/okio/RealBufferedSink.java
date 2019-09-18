package com.android.okhttp.okio;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

final class RealBufferedSink implements BufferedSink {
    public final Buffer buffer;
    /* access modifiers changed from: private */
    public boolean closed;
    public final Sink sink;

    public RealBufferedSink(Sink sink2, Buffer buffer2) {
        if (sink2 != null) {
            this.buffer = buffer2;
            this.sink = sink2;
            return;
        }
        throw new IllegalArgumentException("sink == null");
    }

    public RealBufferedSink(Sink sink2) {
        this(sink2, new Buffer());
    }

    public Buffer buffer() {
        return this.buffer;
    }

    public void write(Buffer source, long byteCount) throws IOException {
        if (!this.closed) {
            this.buffer.write(source, byteCount);
            emitCompleteSegments();
            return;
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink write(ByteString byteString) throws IOException {
        if (!this.closed) {
            this.buffer.write(byteString);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeUtf8(String string) throws IOException {
        if (!this.closed) {
            this.buffer.writeUtf8(string);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeUtf8(String string, int beginIndex, int endIndex) throws IOException {
        if (!this.closed) {
            this.buffer.writeUtf8(string, beginIndex, endIndex);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeUtf8CodePoint(int codePoint) throws IOException {
        if (!this.closed) {
            this.buffer.writeUtf8CodePoint(codePoint);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeString(String string, Charset charset) throws IOException {
        if (!this.closed) {
            this.buffer.writeString(string, charset);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeString(String string, int beginIndex, int endIndex, Charset charset) throws IOException {
        if (!this.closed) {
            this.buffer.writeString(string, beginIndex, endIndex, charset);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink write(byte[] source) throws IOException {
        if (!this.closed) {
            this.buffer.write(source);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink write(byte[] source, int offset, int byteCount) throws IOException {
        if (!this.closed) {
            this.buffer.write(source, offset, byteCount);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public long writeAll(Source source) throws IOException {
        if (source != null) {
            long totalBytesRead = 0;
            while (true) {
                long read = source.read(this.buffer, 8192);
                long readCount = read;
                if (read == -1) {
                    return totalBytesRead;
                }
                totalBytesRead += readCount;
                emitCompleteSegments();
            }
        } else {
            throw new IllegalArgumentException("source == null");
        }
    }

    public BufferedSink write(Source source, long byteCount) throws IOException {
        while (byteCount > 0) {
            long read = source.read(this.buffer, byteCount);
            if (read != -1) {
                byteCount -= read;
                emitCompleteSegments();
            } else {
                throw new EOFException();
            }
        }
        return this;
    }

    public BufferedSink writeByte(int b) throws IOException {
        if (!this.closed) {
            this.buffer.writeByte(b);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeShort(int s) throws IOException {
        if (!this.closed) {
            this.buffer.writeShort(s);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeShortLe(int s) throws IOException {
        if (!this.closed) {
            this.buffer.writeShortLe(s);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeInt(int i) throws IOException {
        if (!this.closed) {
            this.buffer.writeInt(i);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeIntLe(int i) throws IOException {
        if (!this.closed) {
            this.buffer.writeIntLe(i);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeLong(long v) throws IOException {
        if (!this.closed) {
            this.buffer.writeLong(v);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeLongLe(long v) throws IOException {
        if (!this.closed) {
            this.buffer.writeLongLe(v);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeDecimalLong(long v) throws IOException {
        if (!this.closed) {
            this.buffer.writeDecimalLong(v);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink writeHexadecimalUnsignedLong(long v) throws IOException {
        if (!this.closed) {
            this.buffer.writeHexadecimalUnsignedLong(v);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink emitCompleteSegments() throws IOException {
        if (!this.closed) {
            long byteCount = this.buffer.completeSegmentByteCount();
            if (byteCount > 0) {
                this.sink.write(this.buffer, byteCount);
            }
            return this;
        }
        throw new IllegalStateException("closed");
    }

    public BufferedSink emit() throws IOException {
        if (!this.closed) {
            long byteCount = this.buffer.size();
            if (byteCount > 0) {
                this.sink.write(this.buffer, byteCount);
            }
            return this;
        }
        throw new IllegalStateException("closed");
    }

    public OutputStream outputStream() {
        return new OutputStream() {
            public void write(int b) throws IOException {
                if (!RealBufferedSink.this.closed) {
                    RealBufferedSink.this.buffer.writeByte((int) (byte) b);
                    RealBufferedSink.this.emitCompleteSegments();
                    return;
                }
                throw new IOException("closed");
            }

            public void write(byte[] data, int offset, int byteCount) throws IOException {
                if (!RealBufferedSink.this.closed) {
                    RealBufferedSink.this.buffer.write(data, offset, byteCount);
                    RealBufferedSink.this.emitCompleteSegments();
                    return;
                }
                throw new IOException("closed");
            }

            public void flush() throws IOException {
                if (!RealBufferedSink.this.closed) {
                    RealBufferedSink.this.flush();
                }
            }

            public void close() throws IOException {
                RealBufferedSink.this.close();
            }

            public String toString() {
                return RealBufferedSink.this + ".outputStream()";
            }
        };
    }

    public void flush() throws IOException {
        if (!this.closed) {
            if (this.buffer.size > 0) {
                this.sink.write(this.buffer, this.buffer.size);
            }
            this.sink.flush();
            return;
        }
        throw new IllegalStateException("closed");
    }

    public void close() throws IOException {
        if (!this.closed) {
            Throwable thrown = null;
            try {
                if (this.buffer.size > 0) {
                    this.sink.write(this.buffer, this.buffer.size);
                }
            } catch (Throwable e) {
                thrown = e;
            }
            try {
                this.sink.close();
            } catch (Throwable e2) {
                if (thrown == null) {
                    thrown = e2;
                }
            }
            this.closed = true;
            if (thrown != null) {
                Util.sneakyRethrow(thrown);
            }
        }
    }

    public Timeout timeout() {
        return this.sink.timeout();
    }

    public String toString() {
        return "buffer(" + this.sink + ")";
    }
}
