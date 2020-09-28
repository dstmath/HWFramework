package com.huawei.okio;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.annotation.Nullable;

/* access modifiers changed from: package-private */
public final class RealBufferedSource implements BufferedSource {
    public final Buffer buffer = new Buffer();
    boolean closed;
    public final Source source;

    RealBufferedSource(Source source2) {
        if (source2 != null) {
            this.source = source2;
            return;
        }
        throw new NullPointerException("source == null");
    }

    @Override // com.huawei.okio.BufferedSource
    public Buffer buffer() {
        return this.buffer;
    }

    @Override // com.huawei.okio.Source
    public long read(Buffer sink, long byteCount) throws IOException {
        if (sink == null) {
            throw new IllegalArgumentException("sink == null");
        } else if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        } else if (this.closed) {
            throw new IllegalStateException("closed");
        } else if (this.buffer.size == 0 && this.source.read(this.buffer, 8192) == -1) {
            return -1;
        } else {
            return this.buffer.read(sink, Math.min(byteCount, this.buffer.size));
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public boolean exhausted() throws IOException {
        if (!this.closed) {
            return this.buffer.exhausted() && this.source.read(this.buffer, 8192) == -1;
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSource
    public void require(long byteCount) throws IOException {
        if (!request(byteCount)) {
            throw new EOFException();
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public boolean request(long byteCount) throws IOException {
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        } else if (!this.closed) {
            while (this.buffer.size < byteCount) {
                if (this.source.read(this.buffer, 8192) == -1) {
                    return false;
                }
            }
            return true;
        } else {
            throw new IllegalStateException("closed");
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public byte readByte() throws IOException {
        require(1);
        return this.buffer.readByte();
    }

    @Override // com.huawei.okio.BufferedSource
    public ByteString readByteString() throws IOException {
        this.buffer.writeAll(this.source);
        return this.buffer.readByteString();
    }

    @Override // com.huawei.okio.BufferedSource
    public ByteString readByteString(long byteCount) throws IOException {
        require(byteCount);
        return this.buffer.readByteString(byteCount);
    }

    @Override // com.huawei.okio.BufferedSource
    public int select(Options options) throws IOException {
        if (!this.closed) {
            do {
                int index = this.buffer.selectPrefix(options);
                if (index == -1) {
                    return -1;
                }
                int selectedSize = options.byteStrings[index].size();
                if (((long) selectedSize) <= this.buffer.size) {
                    this.buffer.skip((long) selectedSize);
                    return index;
                }
            } while (this.source.read(this.buffer, 8192) != -1);
            return -1;
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSource
    public byte[] readByteArray() throws IOException {
        this.buffer.writeAll(this.source);
        return this.buffer.readByteArray();
    }

    @Override // com.huawei.okio.BufferedSource
    public byte[] readByteArray(long byteCount) throws IOException {
        require(byteCount);
        return this.buffer.readByteArray(byteCount);
    }

    @Override // com.huawei.okio.BufferedSource
    public int read(byte[] sink) throws IOException {
        return read(sink, 0, sink.length);
    }

    @Override // com.huawei.okio.BufferedSource
    public void readFully(byte[] sink) throws IOException {
        try {
            require((long) sink.length);
            this.buffer.readFully(sink);
        } catch (EOFException e) {
            int offset = 0;
            while (this.buffer.size > 0) {
                Buffer buffer2 = this.buffer;
                int read = buffer2.read(sink, offset, (int) buffer2.size);
                if (read != -1) {
                    offset += read;
                } else {
                    throw new AssertionError();
                }
            }
            throw e;
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public int read(byte[] sink, int offset, int byteCount) throws IOException {
        Util.checkOffsetAndCount((long) sink.length, (long) offset, (long) byteCount);
        if (this.buffer.size == 0 && this.source.read(this.buffer, 8192) == -1) {
            return -1;
        }
        return this.buffer.read(sink, offset, (int) Math.min((long) byteCount, this.buffer.size));
    }

    @Override // java.nio.channels.ReadableByteChannel
    public int read(ByteBuffer sink) throws IOException {
        if (this.buffer.size == 0 && this.source.read(this.buffer, 8192) == -1) {
            return -1;
        }
        return this.buffer.read(sink);
    }

    @Override // com.huawei.okio.BufferedSource
    public void readFully(Buffer sink, long byteCount) throws IOException {
        try {
            require(byteCount);
            this.buffer.readFully(sink, byteCount);
        } catch (EOFException e) {
            sink.writeAll(this.buffer);
            throw e;
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public long readAll(Sink sink) throws IOException {
        if (sink != null) {
            long totalBytesWritten = 0;
            while (this.source.read(this.buffer, 8192) != -1) {
                long emitByteCount = this.buffer.completeSegmentByteCount();
                if (emitByteCount > 0) {
                    totalBytesWritten += emitByteCount;
                    sink.write(this.buffer, emitByteCount);
                }
            }
            if (this.buffer.size() <= 0) {
                return totalBytesWritten;
            }
            long totalBytesWritten2 = totalBytesWritten + this.buffer.size();
            Buffer buffer2 = this.buffer;
            sink.write(buffer2, buffer2.size());
            return totalBytesWritten2;
        }
        throw new IllegalArgumentException("sink == null");
    }

    @Override // com.huawei.okio.BufferedSource
    public String readUtf8() throws IOException {
        this.buffer.writeAll(this.source);
        return this.buffer.readUtf8();
    }

    @Override // com.huawei.okio.BufferedSource
    public String readUtf8(long byteCount) throws IOException {
        require(byteCount);
        return this.buffer.readUtf8(byteCount);
    }

    @Override // com.huawei.okio.BufferedSource
    public String readString(Charset charset) throws IOException {
        if (charset != null) {
            this.buffer.writeAll(this.source);
            return this.buffer.readString(charset);
        }
        throw new IllegalArgumentException("charset == null");
    }

    @Override // com.huawei.okio.BufferedSource
    public String readString(long byteCount, Charset charset) throws IOException {
        require(byteCount);
        if (charset != null) {
            return this.buffer.readString(byteCount, charset);
        }
        throw new IllegalArgumentException("charset == null");
    }

    @Override // com.huawei.okio.BufferedSource
    @Nullable
    public String readUtf8Line() throws IOException {
        long newline = indexOf((byte) 10);
        if (newline != -1) {
            return this.buffer.readUtf8Line(newline);
        }
        if (this.buffer.size != 0) {
            return readUtf8(this.buffer.size);
        }
        return null;
    }

    @Override // com.huawei.okio.BufferedSource
    public String readUtf8LineStrict() throws IOException {
        return readUtf8LineStrict(Long.MAX_VALUE);
    }

    @Override // com.huawei.okio.BufferedSource
    public String readUtf8LineStrict(long limit) throws IOException {
        if (limit >= 0) {
            long scanLength = limit == Long.MAX_VALUE ? Long.MAX_VALUE : limit + 1;
            long newline = indexOf((byte) 10, 0, scanLength);
            if (newline != -1) {
                return this.buffer.readUtf8Line(newline);
            }
            if (scanLength < Long.MAX_VALUE && request(scanLength) && this.buffer.getByte(scanLength - 1) == 13 && request(1 + scanLength) && this.buffer.getByte(scanLength) == 10) {
                return this.buffer.readUtf8Line(scanLength);
            }
            Buffer data = new Buffer();
            Buffer buffer2 = this.buffer;
            buffer2.copyTo(data, 0, Math.min(32L, buffer2.size()));
            throw new EOFException("\\n not found: limit=" + Math.min(this.buffer.size(), limit) + " content=" + data.readByteString().hex() + (char) 8230);
        }
        throw new IllegalArgumentException("limit < 0: " + limit);
    }

    @Override // com.huawei.okio.BufferedSource
    public int readUtf8CodePoint() throws IOException {
        require(1);
        byte b0 = this.buffer.getByte(0);
        if ((b0 & 224) == 192) {
            require(2);
        } else if ((b0 & 240) == 224) {
            require(3);
        } else if ((b0 & 248) == 240) {
            require(4);
        }
        return this.buffer.readUtf8CodePoint();
    }

    @Override // com.huawei.okio.BufferedSource
    public short readShort() throws IOException {
        require(2);
        return this.buffer.readShort();
    }

    @Override // com.huawei.okio.BufferedSource
    public short readShortLe() throws IOException {
        require(2);
        return this.buffer.readShortLe();
    }

    @Override // com.huawei.okio.BufferedSource
    public int readInt() throws IOException {
        require(4);
        return this.buffer.readInt();
    }

    @Override // com.huawei.okio.BufferedSource
    public int readIntLe() throws IOException {
        require(4);
        return this.buffer.readIntLe();
    }

    @Override // com.huawei.okio.BufferedSource
    public long readLong() throws IOException {
        require(8);
        return this.buffer.readLong();
    }

    @Override // com.huawei.okio.BufferedSource
    public long readLongLe() throws IOException {
        require(8);
        return this.buffer.readLongLe();
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x002b  */
    @Override // com.huawei.okio.BufferedSource
    public long readDecimalLong() throws IOException {
        require(1);
        int pos = 0;
        while (true) {
            if (!request((long) (pos + 1))) {
                break;
            }
            byte b = this.buffer.getByte((long) pos);
            if ((b >= 48 && b <= 57) || (pos == 0 && b == 45)) {
                pos++;
            } else if (pos == 0) {
                throw new NumberFormatException(String.format("Expected leading [0-9] or '-' character but was %#x", Byte.valueOf(b)));
            }
        }
        if (pos == 0) {
        }
        return this.buffer.readDecimalLong();
    }

    @Override // com.huawei.okio.BufferedSource
    public long readHexadecimalUnsignedLong() throws IOException {
        require(1);
        int pos = 0;
        while (true) {
            if (!request((long) (pos + 1))) {
                break;
            }
            byte b = this.buffer.getByte((long) pos);
            if ((b >= 48 && b <= 57) || ((b >= 97 && b <= 102) || (b >= 65 && b <= 70))) {
                pos++;
            } else if (pos == 0) {
                throw new NumberFormatException(String.format("Expected leading [0-9a-fA-F] character but was %#x", Byte.valueOf(b)));
            }
        }
        return this.buffer.readHexadecimalUnsignedLong();
    }

    @Override // com.huawei.okio.BufferedSource
    public void skip(long byteCount) throws IOException {
        if (!this.closed) {
            while (byteCount > 0) {
                if (this.buffer.size == 0 && this.source.read(this.buffer, 8192) == -1) {
                    throw new EOFException();
                }
                long toSkip = Math.min(byteCount, this.buffer.size());
                this.buffer.skip(toSkip);
                byteCount -= toSkip;
            }
            return;
        }
        throw new IllegalStateException("closed");
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOf(byte b) throws IOException {
        return indexOf(b, 0, Long.MAX_VALUE);
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOf(byte b, long fromIndex) throws IOException {
        return indexOf(b, fromIndex, Long.MAX_VALUE);
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOf(byte b, long fromIndex, long toIndex) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        } else if (fromIndex < 0 || toIndex < fromIndex) {
            throw new IllegalArgumentException(String.format("fromIndex=%s toIndex=%s", Long.valueOf(fromIndex), Long.valueOf(toIndex)));
        } else {
            long fromIndex2 = fromIndex;
            while (fromIndex2 < toIndex) {
                long result = this.buffer.indexOf(b, fromIndex2, toIndex);
                if (result != -1) {
                    return result;
                }
                long lastBufferSize = this.buffer.size;
                if (lastBufferSize >= toIndex || this.source.read(this.buffer, 8192) == -1) {
                    return -1;
                }
                fromIndex2 = Math.max(fromIndex2, lastBufferSize);
            }
            return -1;
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOf(ByteString bytes) throws IOException {
        return indexOf(bytes, 0);
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOf(ByteString bytes, long fromIndex) throws IOException {
        if (!this.closed) {
            while (true) {
                long result = this.buffer.indexOf(bytes, fromIndex);
                if (result != -1) {
                    return result;
                }
                long lastBufferSize = this.buffer.size;
                if (this.source.read(this.buffer, 8192) == -1) {
                    return -1;
                }
                fromIndex = Math.max(fromIndex, (lastBufferSize - ((long) bytes.size())) + 1);
            }
        } else {
            throw new IllegalStateException("closed");
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOfElement(ByteString targetBytes) throws IOException {
        return indexOfElement(targetBytes, 0);
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOfElement(ByteString targetBytes, long fromIndex) throws IOException {
        if (!this.closed) {
            while (true) {
                long result = this.buffer.indexOfElement(targetBytes, fromIndex);
                if (result != -1) {
                    return result;
                }
                long lastBufferSize = this.buffer.size;
                if (this.source.read(this.buffer, 8192) == -1) {
                    return -1;
                }
                fromIndex = Math.max(fromIndex, lastBufferSize);
            }
        } else {
            throw new IllegalStateException("closed");
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public boolean rangeEquals(long offset, ByteString bytes) throws IOException {
        return rangeEquals(offset, bytes, 0, bytes.size());
    }

    @Override // com.huawei.okio.BufferedSource
    public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        } else if (offset < 0 || bytesOffset < 0 || byteCount < 0 || bytes.size() - bytesOffset < byteCount) {
            return false;
        } else {
            for (int i = 0; i < byteCount; i++) {
                long bufferOffset = ((long) i) + offset;
                if (!(request(1 + bufferOffset) && this.buffer.getByte(bufferOffset) == bytes.getByte(bytesOffset + i))) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public InputStream inputStream() {
        return new InputStream() {
            /* class com.huawei.okio.RealBufferedSource.AnonymousClass1 */

            @Override // java.io.InputStream
            public int read() throws IOException {
                if (RealBufferedSource.this.closed) {
                    throw new IOException("closed");
                } else if (RealBufferedSource.this.buffer.size == 0 && RealBufferedSource.this.source.read(RealBufferedSource.this.buffer, 8192) == -1) {
                    return -1;
                } else {
                    return RealBufferedSource.this.buffer.readByte() & 255;
                }
            }

            @Override // java.io.InputStream
            public int read(byte[] data, int offset, int byteCount) throws IOException {
                if (!RealBufferedSource.this.closed) {
                    Util.checkOffsetAndCount((long) data.length, (long) offset, (long) byteCount);
                    if (RealBufferedSource.this.buffer.size == 0 && RealBufferedSource.this.source.read(RealBufferedSource.this.buffer, 8192) == -1) {
                        return -1;
                    }
                    return RealBufferedSource.this.buffer.read(data, offset, byteCount);
                }
                throw new IOException("closed");
            }

            @Override // java.io.InputStream
            public int available() throws IOException {
                if (!RealBufferedSource.this.closed) {
                    return (int) Math.min(RealBufferedSource.this.buffer.size, 2147483647L);
                }
                throw new IOException("closed");
            }

            @Override // java.io.Closeable, java.lang.AutoCloseable, java.io.InputStream
            public void close() throws IOException {
                RealBufferedSource.this.close();
            }

            public String toString() {
                return RealBufferedSource.this + ".inputStream()";
            }
        };
    }

    public boolean isOpen() {
        return !this.closed;
    }

    @Override // java.io.Closeable, com.huawei.okio.Source, java.lang.AutoCloseable, java.nio.channels.Channel
    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.source.close();
            this.buffer.clear();
        }
    }

    @Override // com.huawei.okio.Source
    public Timeout timeout() {
        return this.source.timeout();
    }

    public String toString() {
        return "buffer(" + this.source + ")";
    }
}
