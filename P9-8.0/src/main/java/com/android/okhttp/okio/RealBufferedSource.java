package com.android.okhttp.okio;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

final class RealBufferedSource implements BufferedSource {
    public final Buffer buffer;
    private boolean closed;
    public final Source source;

    public RealBufferedSource(Source source, Buffer buffer) {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        this.buffer = buffer;
        this.source = source;
    }

    public RealBufferedSource(Source source) {
        this(source, new Buffer());
    }

    public Buffer buffer() {
        return this.buffer;
    }

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

    public boolean exhausted() throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        } else if (this.buffer.exhausted() && this.source.read(this.buffer, 8192) == -1) {
            return true;
        } else {
            return false;
        }
    }

    public void require(long byteCount) throws IOException {
        if (!request(byteCount)) {
            throw new EOFException();
        }
    }

    public boolean request(long byteCount) throws IOException {
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        } else if (this.closed) {
            throw new IllegalStateException("closed");
        } else {
            while (this.buffer.size < byteCount) {
                if (this.source.read(this.buffer, 8192) == -1) {
                    return false;
                }
            }
            return true;
        }
    }

    public byte readByte() throws IOException {
        require(1);
        return this.buffer.readByte();
    }

    public ByteString readByteString() throws IOException {
        this.buffer.writeAll(this.source);
        return this.buffer.readByteString();
    }

    public ByteString readByteString(long byteCount) throws IOException {
        require(byteCount);
        return this.buffer.readByteString(byteCount);
    }

    public byte[] readByteArray() throws IOException {
        this.buffer.writeAll(this.source);
        return this.buffer.readByteArray();
    }

    public byte[] readByteArray(long byteCount) throws IOException {
        require(byteCount);
        return this.buffer.readByteArray(byteCount);
    }

    public int read(byte[] sink) throws IOException {
        return read(sink, 0, sink.length);
    }

    public void readFully(byte[] sink) throws IOException {
        try {
            require((long) sink.length);
            this.buffer.readFully(sink);
        } catch (EOFException e) {
            int offset = 0;
            while (this.buffer.size > 0) {
                int read = this.buffer.read(sink, offset, (int) this.buffer.size);
                if (read == -1) {
                    throw new AssertionError();
                }
                offset += read;
            }
            throw e;
        }
    }

    public int read(byte[] sink, int offset, int byteCount) throws IOException {
        Util.checkOffsetAndCount((long) sink.length, (long) offset, (long) byteCount);
        if (this.buffer.size == 0 && this.source.read(this.buffer, 8192) == -1) {
            return -1;
        }
        return this.buffer.read(sink, offset, (int) Math.min((long) byteCount, this.buffer.size));
    }

    public void readFully(Buffer sink, long byteCount) throws IOException {
        try {
            require(byteCount);
            this.buffer.readFully(sink, byteCount);
        } catch (EOFException e) {
            sink.writeAll(this.buffer);
            throw e;
        }
    }

    public long readAll(Sink sink) throws IOException {
        if (sink == null) {
            throw new IllegalArgumentException("sink == null");
        }
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
        totalBytesWritten += this.buffer.size();
        sink.write(this.buffer, this.buffer.size());
        return totalBytesWritten;
    }

    public String readUtf8() throws IOException {
        this.buffer.writeAll(this.source);
        return this.buffer.readUtf8();
    }

    public String readUtf8(long byteCount) throws IOException {
        require(byteCount);
        return this.buffer.readUtf8(byteCount);
    }

    public String readString(Charset charset) throws IOException {
        if (charset == null) {
            throw new IllegalArgumentException("charset == null");
        }
        this.buffer.writeAll(this.source);
        return this.buffer.readString(charset);
    }

    public String readString(long byteCount, Charset charset) throws IOException {
        require(byteCount);
        if (charset != null) {
            return this.buffer.readString(byteCount, charset);
        }
        throw new IllegalArgumentException("charset == null");
    }

    public String readUtf8Line() throws IOException {
        long newline = indexOf((byte) 10);
        if (newline != -1) {
            return this.buffer.readUtf8Line(newline);
        }
        return this.buffer.size != 0 ? readUtf8(this.buffer.size) : null;
    }

    public String readUtf8LineStrict() throws IOException {
        long newline = indexOf((byte) 10);
        if (newline != -1) {
            return this.buffer.readUtf8Line(newline);
        }
        Buffer data = new Buffer();
        this.buffer.copyTo(data, 0, Math.min(32, this.buffer.size()));
        throw new EOFException("\\n not found: size=" + this.buffer.size() + " content=" + data.readByteString().hex() + "...");
    }

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

    public short readShort() throws IOException {
        require(2);
        return this.buffer.readShort();
    }

    public short readShortLe() throws IOException {
        require(2);
        return this.buffer.readShortLe();
    }

    public int readInt() throws IOException {
        require(4);
        return this.buffer.readInt();
    }

    public int readIntLe() throws IOException {
        require(4);
        return this.buffer.readIntLe();
    }

    public long readLong() throws IOException {
        require(8);
        return this.buffer.readLong();
    }

    public long readLongLe() throws IOException {
        require(8);
        return this.buffer.readLongLe();
    }

    public long readDecimalLong() throws IOException {
        require(1);
        int pos = 0;
        while (request((long) (pos + 1))) {
            byte b = this.buffer.getByte((long) pos);
            if ((b < (byte) 48 || b > (byte) 57) && !(pos == 0 && b == (byte) 45)) {
                if (pos == 0) {
                    throw new NumberFormatException(String.format("Expected leading [0-9] or '-' character but was %#x", new Object[]{Byte.valueOf(b)}));
                }
                return this.buffer.readDecimalLong();
            }
            pos++;
        }
        return this.buffer.readDecimalLong();
    }

    public long readHexadecimalUnsignedLong() throws IOException {
        require(1);
        for (int pos = 0; request((long) (pos + 1)); pos++) {
            byte b = this.buffer.getByte((long) pos);
            if ((b < (byte) 48 || b > (byte) 57) && ((b < (byte) 97 || b > (byte) 102) && (b < (byte) 65 || b > (byte) 70))) {
                if (pos == 0) {
                    throw new NumberFormatException(String.format("Expected leading [0-9a-fA-F] character but was %#x", new Object[]{Byte.valueOf(b)}));
                }
                return this.buffer.readHexadecimalUnsignedLong();
            }
        }
        return this.buffer.readHexadecimalUnsignedLong();
    }

    public void skip(long byteCount) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        while (byteCount > 0) {
            if (this.buffer.size == 0 && this.source.read(this.buffer, 8192) == -1) {
                throw new EOFException();
            }
            long toSkip = Math.min(byteCount, this.buffer.size());
            this.buffer.skip(toSkip);
            byteCount -= toSkip;
        }
    }

    public long indexOf(byte b) throws IOException {
        return indexOf(b, 0);
    }

    public long indexOf(byte b, long fromIndex) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        while (fromIndex >= this.buffer.size) {
            if (this.source.read(this.buffer, 8192) == -1) {
                return -1;
            }
        }
        do {
            long index = this.buffer.indexOf(b, fromIndex);
            if (index != -1) {
                return index;
            }
            fromIndex = this.buffer.size;
        } while (this.source.read(this.buffer, 8192) != -1);
        return -1;
    }

    public long indexOf(ByteString bytes) throws IOException {
        return indexOf(bytes, 0);
    }

    public long indexOf(ByteString bytes, long fromIndex) throws IOException {
        if (bytes.size() == 0) {
            throw new IllegalArgumentException("bytes is empty");
        }
        while (true) {
            fromIndex = indexOf(bytes.getByte(0), fromIndex);
            if (fromIndex == -1) {
                return -1;
            }
            if (rangeEquals(fromIndex, bytes)) {
                return fromIndex;
            }
            fromIndex++;
        }
    }

    public long indexOfElement(ByteString targetBytes) throws IOException {
        return indexOfElement(targetBytes, 0);
    }

    public long indexOfElement(ByteString targetBytes, long fromIndex) throws IOException {
        if (this.closed) {
            throw new IllegalStateException("closed");
        }
        while (fromIndex >= this.buffer.size) {
            if (this.source.read(this.buffer, 8192) == -1) {
                return -1;
            }
        }
        do {
            long index = this.buffer.indexOfElement(targetBytes, fromIndex);
            if (index != -1) {
                return index;
            }
            fromIndex = this.buffer.size;
        } while (this.source.read(this.buffer, 8192) != -1);
        return -1;
    }

    private boolean rangeEquals(long offset, ByteString bytes) throws IOException {
        return request(((long) bytes.size()) + offset) ? this.buffer.rangeEquals(offset, bytes) : false;
    }

    public InputStream inputStream() {
        return new InputStream() {
            public int read() throws IOException {
                if (RealBufferedSource.this.closed) {
                    throw new IOException("closed");
                } else if (RealBufferedSource.this.buffer.size == 0 && RealBufferedSource.this.source.read(RealBufferedSource.this.buffer, 8192) == -1) {
                    return -1;
                } else {
                    return RealBufferedSource.this.buffer.readByte() & 255;
                }
            }

            public int read(byte[] data, int offset, int byteCount) throws IOException {
                if (RealBufferedSource.this.closed) {
                    throw new IOException("closed");
                }
                Util.checkOffsetAndCount((long) data.length, (long) offset, (long) byteCount);
                if (RealBufferedSource.this.buffer.size == 0 && RealBufferedSource.this.source.read(RealBufferedSource.this.buffer, 8192) == -1) {
                    return -1;
                }
                return RealBufferedSource.this.buffer.read(data, offset, byteCount);
            }

            public int available() throws IOException {
                if (!RealBufferedSource.this.closed) {
                    return (int) Math.min(RealBufferedSource.this.buffer.size, 2147483647L);
                }
                throw new IOException("closed");
            }

            public void close() throws IOException {
                RealBufferedSource.this.close();
            }

            public String toString() {
                return RealBufferedSource.this + ".inputStream()";
            }
        };
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.closed = true;
            this.source.close();
            this.buffer.clear();
        }
    }

    public Timeout timeout() {
        return this.source.timeout();
    }

    public String toString() {
        return "buffer(" + this.source + ")";
    }
}
