package com.huawei.okio;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/* access modifiers changed from: package-private */
public final class RealBufferedSink implements BufferedSink {
    public final Buffer buffer = new Buffer();
    boolean closed;
    public final Sink sink;

    RealBufferedSink(Sink sink2) {
        if (sink2 != null) {
            this.sink = sink2;
            return;
        }
        throw new NullPointerException("sink == null");
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer buffer() {
        return this.buffer;
    }

    @Override // com.huawei.okio.Sink
    public void write(Buffer source, long byteCount) throws IOException {
        if (!this.closed) {
            this.buffer.write(source, byteCount);
            emitCompleteSegments();
            return;
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink write(ByteString byteString) throws IOException {
        if (!this.closed) {
            this.buffer.write(byteString);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeUtf8(String string) throws IOException {
        if (!this.closed) {
            this.buffer.writeUtf8(string);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeUtf8(String string, int beginIndex, int endIndex) throws IOException {
        if (!this.closed) {
            this.buffer.writeUtf8(string, beginIndex, endIndex);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeUtf8CodePoint(int codePoint) throws IOException {
        if (!this.closed) {
            this.buffer.writeUtf8CodePoint(codePoint);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeString(String string, Charset charset) throws IOException {
        if (!this.closed) {
            this.buffer.writeString(string, charset);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeString(String string, int beginIndex, int endIndex, Charset charset) throws IOException {
        if (!this.closed) {
            this.buffer.writeString(string, beginIndex, endIndex, charset);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink write(byte[] source) throws IOException {
        if (!this.closed) {
            this.buffer.write(source);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink write(byte[] source, int offset, int byteCount) throws IOException {
        if (!this.closed) {
            this.buffer.write(source, offset, byteCount);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // java.nio.channels.WritableByteChannel
    public int write(ByteBuffer source) throws IOException {
        if (!this.closed) {
            int result = this.buffer.write(source);
            emitCompleteSegments();
            return result;
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public long writeAll(Source source) throws IOException {
        if (source != null) {
            long totalBytesRead = 0;
            while (true) {
                long readCount = source.read(this.buffer, 8192);
                if (readCount == -1) {
                    return totalBytesRead;
                }
                totalBytesRead += readCount;
                emitCompleteSegments();
            }
        } else {
            throw new IllegalArgumentException("source == null");
        }
    }

    @Override // com.huawei.okio.BufferedSink
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

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeByte(int b) throws IOException {
        if (!this.closed) {
            this.buffer.writeByte(b);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeShort(int s) throws IOException {
        if (!this.closed) {
            this.buffer.writeShort(s);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeShortLe(int s) throws IOException {
        if (!this.closed) {
            this.buffer.writeShortLe(s);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeInt(int i) throws IOException {
        if (!this.closed) {
            this.buffer.writeInt(i);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeIntLe(int i) throws IOException {
        if (!this.closed) {
            this.buffer.writeIntLe(i);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeLong(long v) throws IOException {
        if (!this.closed) {
            this.buffer.writeLong(v);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeLongLe(long v) throws IOException {
        if (!this.closed) {
            this.buffer.writeLongLe(v);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeDecimalLong(long v) throws IOException {
        if (!this.closed) {
            this.buffer.writeDecimalLong(v);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink writeHexadecimalUnsignedLong(long v) throws IOException {
        if (!this.closed) {
            this.buffer.writeHexadecimalUnsignedLong(v);
            return emitCompleteSegments();
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSink
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

    @Override // com.huawei.okio.BufferedSink
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

    @Override // com.huawei.okio.BufferedSink
    public OutputStream outputStream() {
        return new OutputStream() {
            /* class com.huawei.okio.RealBufferedSink.AnonymousClass1 */

            @Override // java.io.OutputStream
            public void write(int b) throws IOException {
                if (!RealBufferedSink.this.closed) {
                    RealBufferedSink.this.buffer.writeByte((int) ((byte) b));
                    RealBufferedSink.this.emitCompleteSegments();
                    return;
                }
                throw new IOException("closed");
            }

            @Override // java.io.OutputStream
            public void write(byte[] data, int offset, int byteCount) throws IOException {
                if (!RealBufferedSink.this.closed) {
                    RealBufferedSink.this.buffer.write(data, offset, byteCount);
                    RealBufferedSink.this.emitCompleteSegments();
                    return;
                }
                throw new IOException("closed");
            }

            @Override // java.io.OutputStream, java.io.Flushable
            public void flush() throws IOException {
                if (!RealBufferedSink.this.closed) {
                    RealBufferedSink.this.flush();
                }
            }

            @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
            public void close() throws IOException {
                RealBufferedSink.this.close();
            }

            @Override // java.lang.Object
            public String toString() {
                return RealBufferedSink.this + ".outputStream()";
            }
        };
    }

    @Override // com.huawei.okio.BufferedSink, com.huawei.okio.Sink, java.io.Flushable
    public void flush() throws IOException {
        if (!this.closed) {
            if (this.buffer.size > 0) {
                Sink sink2 = this.sink;
                Buffer buffer2 = this.buffer;
                sink2.write(buffer2, buffer2.size);
            }
            this.sink.flush();
            return;
        }
        throw new IllegalStateException("closed");
    }

    @Override // java.nio.channels.Channel
    public boolean isOpen() {
        return !this.closed;
    }

    @Override // com.huawei.okio.Sink, java.io.Closeable, java.lang.AutoCloseable
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

    @Override // com.huawei.okio.Sink
    public Timeout timeout() {
        return this.sink.timeout();
    }

    @Override // java.lang.Object
    public String toString() {
        return "buffer(" + this.sink + ")";
    }
}
