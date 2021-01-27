package com.huawei.okio;

import android.bluetooth.BluetoothAdvFilterParamEx;
import com.huawei.networkit.grs.utils.Encrypt;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Deprecated
public final class Buffer implements BufferedSource, BufferedSink, Cloneable, ByteChannel {
    private static final byte[] DIGITS = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
    static final int REPLACEMENT_CHARACTER = 65533;
    @Nullable
    Segment head;
    long size;

    public final long size() {
        return this.size;
    }

    @Override // com.huawei.okio.BufferedSource, com.huawei.okio.BufferedSink
    public Buffer buffer() {
        return this;
    }

    @Override // com.huawei.okio.BufferedSource
    public Buffer getBuffer() {
        return this;
    }

    @Override // com.huawei.okio.BufferedSink
    public OutputStream outputStream() {
        return new OutputStream() {
            /* class com.huawei.okio.Buffer.AnonymousClass1 */

            @Override // java.io.OutputStream
            public void write(int b) {
                Buffer.this.writeByte((int) ((byte) b));
            }

            @Override // java.io.OutputStream
            public void write(byte[] data, int offset, int byteCount) {
                Buffer.this.write(data, offset, byteCount);
            }

            @Override // java.io.OutputStream, java.io.Flushable
            public void flush() {
            }

            @Override // java.io.OutputStream, java.io.Closeable, java.lang.AutoCloseable
            public void close() {
            }

            @Override // java.lang.Object
            public String toString() {
                return Buffer.this + ".outputStream()";
            }
        };
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer emitCompleteSegments() {
        return this;
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink emit() {
        return this;
    }

    @Override // com.huawei.okio.BufferedSource
    public boolean exhausted() {
        return this.size == 0;
    }

    @Override // com.huawei.okio.BufferedSource
    public void require(long byteCount) throws EOFException {
        if (this.size < byteCount) {
            throw new EOFException();
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public boolean request(long byteCount) {
        return this.size >= byteCount;
    }

    @Override // com.huawei.okio.BufferedSource
    public BufferedSource peek() {
        return Okio.buffer(new PeekSource(this));
    }

    @Override // com.huawei.okio.BufferedSource
    public InputStream inputStream() {
        return new InputStream() {
            /* class com.huawei.okio.Buffer.AnonymousClass2 */

            @Override // java.io.InputStream
            public int read() {
                if (Buffer.this.size > 0) {
                    return Buffer.this.readByte() & 255;
                }
                return -1;
            }

            @Override // java.io.InputStream
            public int read(byte[] sink, int offset, int byteCount) {
                return Buffer.this.read(sink, offset, byteCount);
            }

            @Override // java.io.InputStream
            public int available() {
                return (int) Math.min(Buffer.this.size, 2147483647L);
            }

            @Override // java.io.InputStream, java.io.Closeable, java.lang.AutoCloseable
            public void close() {
            }

            @Override // java.lang.Object
            public String toString() {
                return Buffer.this + ".inputStream()";
            }
        };
    }

    public final Buffer copyTo(OutputStream out) throws IOException {
        return copyTo(out, 0, this.size);
    }

    public final Buffer copyTo(OutputStream out, long offset, long byteCount) throws IOException {
        if (out != null) {
            Util.checkOffsetAndCount(this.size, offset, byteCount);
            if (byteCount == 0) {
                return this;
            }
            Segment s = this.head;
            while (offset >= ((long) (s.limit - s.pos))) {
                offset -= (long) (s.limit - s.pos);
                s = s.next;
            }
            while (byteCount > 0) {
                int pos = (int) (((long) s.pos) + offset);
                int toCopy = (int) Math.min((long) (s.limit - pos), byteCount);
                out.write(s.data, pos, toCopy);
                byteCount -= (long) toCopy;
                offset = 0;
                s = s.next;
            }
            return this;
        }
        throw new IllegalArgumentException("out == null");
    }

    public final Buffer copyTo(Buffer out, long offset, long byteCount) {
        if (out != null) {
            Util.checkOffsetAndCount(this.size, offset, byteCount);
            if (byteCount == 0) {
                return this;
            }
            out.size += byteCount;
            Segment s = this.head;
            while (offset >= ((long) (s.limit - s.pos))) {
                offset -= (long) (s.limit - s.pos);
                s = s.next;
            }
            while (byteCount > 0) {
                Segment copy = s.sharedCopy();
                copy.pos = (int) (((long) copy.pos) + offset);
                copy.limit = Math.min(copy.pos + ((int) byteCount), copy.limit);
                Segment segment = out.head;
                if (segment == null) {
                    copy.prev = copy;
                    copy.next = copy;
                    out.head = copy;
                } else {
                    segment.prev.push(copy);
                }
                byteCount -= (long) (copy.limit - copy.pos);
                offset = 0;
                s = s.next;
            }
            return this;
        }
        throw new IllegalArgumentException("out == null");
    }

    public final Buffer writeTo(OutputStream out) throws IOException {
        return writeTo(out, this.size);
    }

    public final Buffer writeTo(OutputStream out, long byteCount) throws IOException {
        if (out != null) {
            Util.checkOffsetAndCount(this.size, 0, byteCount);
            Segment s = this.head;
            while (byteCount > 0) {
                int toCopy = (int) Math.min(byteCount, (long) (s.limit - s.pos));
                out.write(s.data, s.pos, toCopy);
                s.pos += toCopy;
                this.size -= (long) toCopy;
                byteCount -= (long) toCopy;
                if (s.pos == s.limit) {
                    Segment pop = s.pop();
                    s = pop;
                    this.head = pop;
                    SegmentPool.recycle(s);
                }
            }
            return this;
        }
        throw new IllegalArgumentException("out == null");
    }

    public final Buffer readFrom(InputStream in) throws IOException {
        readFrom(in, Long.MAX_VALUE, true);
        return this;
    }

    public final Buffer readFrom(InputStream in, long byteCount) throws IOException {
        if (byteCount >= 0) {
            readFrom(in, byteCount, false);
            return this;
        }
        throw new IllegalArgumentException("byteCount < 0: " + byteCount);
    }

    private void readFrom(InputStream in, long byteCount, boolean forever) throws IOException {
        if (in != null) {
            while (true) {
                if (byteCount > 0 || forever) {
                    Segment tail = writableSegment(1);
                    int bytesRead = in.read(tail.data, tail.limit, (int) Math.min(byteCount, (long) (8192 - tail.limit)));
                    if (bytesRead != -1) {
                        tail.limit += bytesRead;
                        this.size += (long) bytesRead;
                        byteCount -= (long) bytesRead;
                    } else if (!forever) {
                        throw new EOFException();
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        } else {
            throw new IllegalArgumentException("in == null");
        }
    }

    public final long completeSegmentByteCount() {
        long result = this.size;
        if (result == 0) {
            return 0;
        }
        Segment tail = this.head.prev;
        if (tail.limit >= 8192 || !tail.owner) {
            return result;
        }
        return result - ((long) (tail.limit - tail.pos));
    }

    /* JADX INFO: Multiple debug info for r1v2 byte: [D('b' byte), D('pos' int)] */
    @Override // com.huawei.okio.BufferedSource
    public byte readByte() {
        if (this.size != 0) {
            Segment segment = this.head;
            int pos = segment.pos;
            int limit = segment.limit;
            int pos2 = pos + 1;
            byte b = segment.data[pos];
            this.size--;
            if (pos2 == limit) {
                this.head = segment.pop();
                SegmentPool.recycle(segment);
            } else {
                segment.pos = pos2;
            }
            return b;
        }
        throw new IllegalStateException("size == 0");
    }

    public final byte getByte(long pos) {
        Util.checkOffsetAndCount(this.size, pos, 1);
        long j = this.size;
        if (j - pos > pos) {
            Segment s = this.head;
            while (true) {
                int segmentByteCount = s.limit - s.pos;
                if (pos < ((long) segmentByteCount)) {
                    return s.data[s.pos + ((int) pos)];
                }
                pos -= (long) segmentByteCount;
                s = s.next;
            }
        } else {
            long pos2 = pos - j;
            Segment s2 = this.head.prev;
            while (true) {
                pos2 += (long) (s2.limit - s2.pos);
                if (pos2 >= 0) {
                    return s2.data[s2.pos + ((int) pos2)];
                }
                s2 = s2.prev;
            }
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public short readShort() {
        if (this.size >= 2) {
            Segment segment = this.head;
            int pos = segment.pos;
            int limit = segment.limit;
            if (limit - pos < 2) {
                return (short) (((readByte() & 255) << 8) | (readByte() & 255));
            }
            byte[] data = segment.data;
            int pos2 = pos + 1;
            int pos3 = pos2 + 1;
            int s = ((data[pos] & 255) << 8) | (data[pos2] & 255);
            this.size -= 2;
            if (pos3 == limit) {
                this.head = segment.pop();
                SegmentPool.recycle(segment);
            } else {
                segment.pos = pos3;
            }
            return (short) s;
        }
        throw new IllegalStateException("size < 2: " + this.size);
    }

    @Override // com.huawei.okio.BufferedSource
    public int readInt() {
        if (this.size >= 4) {
            Segment segment = this.head;
            int pos = segment.pos;
            int limit = segment.limit;
            if (limit - pos < 4) {
                return ((readByte() & 255) << 24) | ((readByte() & 255) << 16) | ((readByte() & 255) << 8) | (readByte() & 255);
            }
            byte[] data = segment.data;
            int pos2 = pos + 1;
            int pos3 = pos2 + 1;
            int i = ((data[pos] & 255) << 24) | ((data[pos2] & 255) << 16);
            int pos4 = pos3 + 1;
            int i2 = i | ((data[pos3] & 255) << 8);
            int pos5 = pos4 + 1;
            int i3 = i2 | (data[pos4] & 255);
            this.size -= 4;
            if (pos5 == limit) {
                this.head = segment.pop();
                SegmentPool.recycle(segment);
            } else {
                segment.pos = pos5;
            }
            return i3;
        }
        throw new IllegalStateException("size < 4: " + this.size);
    }

    @Override // com.huawei.okio.BufferedSource
    public long readLong() {
        if (this.size >= 8) {
            Segment segment = this.head;
            int pos = segment.pos;
            int limit = segment.limit;
            if (limit - pos < 8) {
                return ((((long) readInt()) & 4294967295L) << 32) | (((long) readInt()) & 4294967295L);
            }
            byte[] data = segment.data;
            int pos2 = pos + 1;
            int pos3 = pos2 + 1;
            int pos4 = pos3 + 1;
            int pos5 = pos4 + 1;
            int pos6 = pos5 + 1;
            int pos7 = pos6 + 1;
            int pos8 = pos7 + 1;
            int pos9 = pos8 + 1;
            long v = ((((long) data[pos]) & 255) << 56) | ((((long) data[pos2]) & 255) << 48) | ((((long) data[pos3]) & 255) << 40) | ((((long) data[pos4]) & 255) << 32) | ((((long) data[pos5]) & 255) << 24) | ((((long) data[pos6]) & 255) << 16) | ((((long) data[pos7]) & 255) << 8) | (((long) data[pos8]) & 255);
            this.size -= 8;
            if (pos9 == limit) {
                this.head = segment.pop();
                SegmentPool.recycle(segment);
            } else {
                segment.pos = pos9;
            }
            return v;
        }
        throw new IllegalStateException("size < 8: " + this.size);
    }

    @Override // com.huawei.okio.BufferedSource
    public short readShortLe() {
        return Util.reverseBytesShort(readShort());
    }

    @Override // com.huawei.okio.BufferedSource
    public int readIntLe() {
        return Util.reverseBytesInt(readInt());
    }

    @Override // com.huawei.okio.BufferedSource
    public long readLongLe() {
        return Util.reverseBytesLong(readLong());
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x009a, code lost:
        if (r3 == 0) goto L_0x009e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x009c, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b8, code lost:
        throw new java.lang.NumberFormatException("Expected leading [0-9] or '-' character but was 0x" + java.lang.Integer.toHexString(r14));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00d8, code lost:
        r20.size -= (long) r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00de, code lost:
        if (r4 == false) goto L_0x00e2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:?, code lost:
        return -r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
        return r1;
     */
    @Override // com.huawei.okio.BufferedSource
    public long readDecimalLong() {
        long overflowZone;
        byte b;
        byte[] data;
        boolean done;
        boolean done2;
        if (this.size != 0) {
            long value = 0;
            int seen = 0;
            boolean negative = false;
            boolean done3 = false;
            long overflowZone2 = -922337203685477580L;
            long overflowDigit = -7;
            loop0:
            while (true) {
                Segment segment = this.head;
                byte[] data2 = segment.data;
                int pos = segment.pos;
                int limit = segment.limit;
                while (true) {
                    if (pos >= limit) {
                        overflowZone = overflowZone2;
                        break;
                    }
                    b = data2[pos];
                    if (b >= 48 && b <= 57) {
                        int digit = 48 - b;
                        if (value < overflowZone2) {
                            done2 = done3;
                            break loop0;
                        }
                        if (value == overflowZone2) {
                            overflowZone = overflowZone2;
                            done2 = done3;
                            if (((long) digit) < overflowDigit) {
                                break loop0;
                            }
                        } else {
                            overflowZone = overflowZone2;
                            done2 = done3;
                        }
                        value = (value * 10) + ((long) digit);
                        done = done2;
                        data = data2;
                    } else {
                        done = done3;
                        overflowZone = overflowZone2;
                        data = data2;
                        if (b != 45 || seen != 0) {
                            break;
                        }
                        negative = true;
                        overflowDigit--;
                    }
                    pos++;
                    seen++;
                    overflowZone2 = overflowZone;
                    done3 = done;
                    data2 = data;
                }
                if (pos == limit) {
                    this.head = segment.pop();
                    SegmentPool.recycle(segment);
                } else {
                    segment.pos = pos;
                }
                if (done3 || this.head == null) {
                    break;
                }
                overflowZone2 = overflowZone;
            }
            Buffer buffer = new Buffer().writeDecimalLong(value).writeByte((int) b);
            if (!negative) {
                buffer.readByte();
            }
            throw new NumberFormatException("Number too large: " + buffer.readUtf8());
        }
        throw new IllegalStateException("size == 0");
    }

    @Override // com.huawei.okio.BufferedSource
    public long readHexadecimalUnsignedLong() {
        byte b;
        int digit;
        if (this.size != 0) {
            long value = 0;
            int seen = 0;
            boolean done = false;
            do {
                Segment segment = this.head;
                byte[] data = segment.data;
                int pos = segment.pos;
                int limit = segment.limit;
                while (true) {
                    if (pos >= limit) {
                        break;
                    }
                    b = data[pos];
                    if (b >= 48 && b <= 57) {
                        digit = b - 48;
                    } else if (b >= 97 && b <= 102) {
                        digit = (b - 97) + 10;
                    } else if (b < 65 || b > 70) {
                        break;
                    } else {
                        digit = (b - 65) + 10;
                    }
                    if ((-1152921504606846976L & value) == 0) {
                        value = (value << 4) | ((long) digit);
                        pos++;
                        seen++;
                    } else {
                        Buffer buffer = new Buffer().writeHexadecimalUnsignedLong(value).writeByte((int) b);
                        throw new NumberFormatException("Number too large: " + buffer.readUtf8());
                    }
                }
                if (seen != 0) {
                    done = true;
                    if (pos == limit) {
                        this.head = segment.pop();
                        SegmentPool.recycle(segment);
                    } else {
                        segment.pos = pos;
                    }
                    if (done) {
                        break;
                    }
                } else {
                    throw new NumberFormatException("Expected leading [0-9a-fA-F] character but was 0x" + Integer.toHexString(b));
                }
            } while (this.head != null);
            this.size -= (long) seen;
            return value;
        }
        throw new IllegalStateException("size == 0");
    }

    @Override // com.huawei.okio.BufferedSource
    public ByteString readByteString() {
        return new ByteString(readByteArray());
    }

    @Override // com.huawei.okio.BufferedSource
    public ByteString readByteString(long byteCount) throws EOFException {
        return new ByteString(readByteArray(byteCount));
    }

    @Override // com.huawei.okio.BufferedSource
    public int select(Options options) {
        int index = selectPrefix(options, false);
        if (index == -1) {
            return -1;
        }
        try {
            skip((long) options.byteStrings[index].size());
            return index;
        } catch (EOFException e) {
            throw new AssertionError();
        }
    }

    /* JADX INFO: Multiple debug info for r9v2 int: [D('triePos' int), D('scanOrSelect' int)] */
    /* JADX INFO: Multiple debug info for r11v1 int: [D('triePos' int), D('possiblePrefixIndex' int)] */
    /* access modifiers changed from: package-private */
    public int selectPrefix(Options options, boolean selectTruncated) {
        int i;
        int triePos;
        Segment head2 = this.head;
        int nextStep = -2;
        if (head2 != null) {
            Segment s = head2;
            byte[] data = head2.data;
            int pos = head2.pos;
            int limit = head2.limit;
            int[] trie = options.trie;
            int scanOrSelect = 0;
            int prefixIndex = -1;
            loop0:
            while (true) {
                int triePos2 = scanOrSelect + 1;
                int scanOrSelect2 = trie[scanOrSelect];
                int triePos3 = triePos2 + 1;
                int possiblePrefixIndex = trie[triePos2];
                if (possiblePrefixIndex != -1) {
                    prefixIndex = possiblePrefixIndex;
                }
                if (s == null) {
                    break;
                }
                if (scanOrSelect2 < 0) {
                    int trieLimit = triePos3 + (scanOrSelect2 * -1);
                    while (true) {
                        int pos2 = pos + 1;
                        int triePos4 = triePos3 + 1;
                        if ((data[pos] & 255) != trie[triePos3]) {
                            return prefixIndex;
                        }
                        boolean scanComplete = triePos4 == trieLimit;
                        if (pos2 == limit) {
                            s = s.next;
                            pos2 = s.pos;
                            data = s.data;
                            limit = s.limit;
                            if (s != head2) {
                                i = -2;
                            } else if (!scanComplete) {
                                break loop0;
                            } else {
                                i = -2;
                                s = null;
                            }
                        } else {
                            i = -2;
                        }
                        if (scanComplete) {
                            pos = pos2;
                            triePos = trie[triePos4];
                            break;
                        }
                        triePos3 = triePos4;
                        pos = pos2;
                    }
                } else {
                    i = nextStep;
                    int pos3 = pos + 1;
                    int b = data[pos] & 255;
                    int selectLimit = triePos3 + scanOrSelect2;
                    while (triePos3 != selectLimit) {
                        if (b == trie[triePos3]) {
                            int nextStep2 = trie[triePos3 + scanOrSelect2];
                            if (pos3 == limit) {
                                s = s.next;
                                int pos4 = s.pos;
                                data = s.data;
                                limit = s.limit;
                                if (s == head2) {
                                    s = null;
                                    pos = pos4;
                                    triePos = nextStep2;
                                } else {
                                    pos = pos4;
                                    triePos = nextStep2;
                                }
                            } else {
                                pos = pos3;
                                triePos = nextStep2;
                            }
                        } else {
                            triePos3++;
                        }
                    }
                    return prefixIndex;
                }
                if (triePos >= 0) {
                    return triePos;
                }
                scanOrSelect = -triePos;
                nextStep = i;
            }
            if (selectTruncated) {
                return -2;
            }
            return prefixIndex;
        } else if (selectTruncated) {
            return -2;
        } else {
            return options.indexOf(ByteString.EMPTY);
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public void readFully(Buffer sink, long byteCount) throws EOFException {
        long j = this.size;
        if (j >= byteCount) {
            sink.write(this, byteCount);
        } else {
            sink.write(this, j);
            throw new EOFException();
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public long readAll(Sink sink) throws IOException {
        long byteCount = this.size;
        if (byteCount > 0) {
            sink.write(this, byteCount);
        }
        return byteCount;
    }

    @Override // com.huawei.okio.BufferedSource
    public String readUtf8() {
        try {
            return readString(this.size, Util.UTF_8);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public String readUtf8(long byteCount) throws EOFException {
        return readString(byteCount, Util.UTF_8);
    }

    @Override // com.huawei.okio.BufferedSource
    public String readString(Charset charset) {
        try {
            return readString(this.size, charset);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public String readString(long byteCount, Charset charset) throws EOFException {
        Util.checkOffsetAndCount(this.size, 0, byteCount);
        if (charset == null) {
            throw new IllegalArgumentException("charset == null");
        } else if (byteCount > 2147483647L) {
            throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
        } else if (byteCount == 0) {
            return "";
        } else {
            Segment s = this.head;
            if (((long) s.pos) + byteCount > ((long) s.limit)) {
                return new String(readByteArray(byteCount), charset);
            }
            String result = new String(s.data, s.pos, (int) byteCount, charset);
            s.pos = (int) (((long) s.pos) + byteCount);
            this.size -= byteCount;
            if (s.pos == s.limit) {
                this.head = s.pop();
                SegmentPool.recycle(s);
            }
            return result;
        }
    }

    @Override // com.huawei.okio.BufferedSource
    @Nullable
    public String readUtf8Line() throws EOFException {
        long newline = indexOf((byte) 10);
        if (newline != -1) {
            return readUtf8Line(newline);
        }
        long j = this.size;
        if (j != 0) {
            return readUtf8(j);
        }
        return null;
    }

    @Override // com.huawei.okio.BufferedSource
    public String readUtf8LineStrict() throws EOFException {
        return readUtf8LineStrict(Long.MAX_VALUE);
    }

    @Override // com.huawei.okio.BufferedSource
    public String readUtf8LineStrict(long limit) throws EOFException {
        if (limit >= 0) {
            long scanLength = Long.MAX_VALUE;
            if (limit != Long.MAX_VALUE) {
                scanLength = limit + 1;
            }
            long newline = indexOf((byte) 10, 0, scanLength);
            if (newline != -1) {
                return readUtf8Line(newline);
            }
            if (scanLength < size() && getByte(scanLength - 1) == 13 && getByte(scanLength) == 10) {
                return readUtf8Line(scanLength);
            }
            Buffer data = new Buffer();
            copyTo(data, 0, Math.min(32L, size()));
            throw new EOFException("\\n not found: limit=" + Math.min(size(), limit) + " content=" + data.readByteString().hex() + (char) 8230);
        }
        throw new IllegalArgumentException("limit < 0: " + limit);
    }

    /* access modifiers changed from: package-private */
    public String readUtf8Line(long newline) throws EOFException {
        if (newline <= 0 || getByte(newline - 1) != 13) {
            String result = readUtf8(newline);
            skip(1);
            return result;
        }
        String result2 = readUtf8(newline - 1);
        skip(2);
        return result2;
    }

    @Override // com.huawei.okio.BufferedSource
    public int readUtf8CodePoint() throws EOFException {
        int min;
        int byteCount;
        int codePoint;
        if (this.size != 0) {
            byte b0 = getByte(0);
            if ((b0 & 128) == 0) {
                codePoint = b0 & Byte.MAX_VALUE;
                byteCount = 1;
                min = 0;
            } else if ((b0 & 224) == 192) {
                codePoint = b0 & 31;
                byteCount = 2;
                min = 128;
            } else if ((b0 & BluetoothAdvFilterParamEx.DELIVERY_MODE_REPLY) == 224) {
                codePoint = b0 & 15;
                byteCount = 3;
                min = 2048;
            } else if ((b0 & 248) == 240) {
                codePoint = b0 & 7;
                byteCount = 4;
                min = 65536;
            } else {
                skip(1);
                return REPLACEMENT_CHARACTER;
            }
            if (this.size >= ((long) byteCount)) {
                for (int i = 1; i < byteCount; i++) {
                    byte b = getByte((long) i);
                    if ((b & 192) == 128) {
                        codePoint = (codePoint << 6) | (b & 63);
                    } else {
                        skip((long) i);
                        return REPLACEMENT_CHARACTER;
                    }
                }
                skip((long) byteCount);
                if (codePoint > 1114111) {
                    return REPLACEMENT_CHARACTER;
                }
                if ((codePoint < 55296 || codePoint > 57343) && codePoint >= min) {
                    return codePoint;
                }
                return REPLACEMENT_CHARACTER;
            }
            throw new EOFException("size < " + byteCount + ": " + this.size + " (to read code point prefixed 0x" + Integer.toHexString(b0) + ")");
        }
        throw new EOFException();
    }

    @Override // com.huawei.okio.BufferedSource
    public byte[] readByteArray() {
        try {
            return readByteArray(this.size);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public byte[] readByteArray(long byteCount) throws EOFException {
        Util.checkOffsetAndCount(this.size, 0, byteCount);
        if (byteCount <= 2147483647L) {
            byte[] result = new byte[((int) byteCount)];
            readFully(result);
            return result;
        }
        throw new IllegalArgumentException("byteCount > Integer.MAX_VALUE: " + byteCount);
    }

    @Override // com.huawei.okio.BufferedSource
    public int read(byte[] sink) {
        return read(sink, 0, sink.length);
    }

    @Override // com.huawei.okio.BufferedSource
    public void readFully(byte[] sink) throws EOFException {
        int offset = 0;
        while (offset < sink.length) {
            int read = read(sink, offset, sink.length - offset);
            if (read != -1) {
                offset += read;
            } else {
                throw new EOFException();
            }
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public int read(byte[] sink, int offset, int byteCount) {
        Util.checkOffsetAndCount((long) sink.length, (long) offset, (long) byteCount);
        Segment s = this.head;
        if (s == null) {
            return -1;
        }
        int toCopy = Math.min(byteCount, s.limit - s.pos);
        System.arraycopy(s.data, s.pos, sink, offset, toCopy);
        s.pos += toCopy;
        this.size -= (long) toCopy;
        if (s.pos == s.limit) {
            this.head = s.pop();
            SegmentPool.recycle(s);
        }
        return toCopy;
    }

    @Override // java.nio.channels.ReadableByteChannel
    public int read(ByteBuffer sink) throws IOException {
        Segment s = this.head;
        if (s == null) {
            return -1;
        }
        int toCopy = Math.min(sink.remaining(), s.limit - s.pos);
        sink.put(s.data, s.pos, toCopy);
        s.pos += toCopy;
        this.size -= (long) toCopy;
        if (s.pos == s.limit) {
            this.head = s.pop();
            SegmentPool.recycle(s);
        }
        return toCopy;
    }

    public final void clear() {
        try {
            skip(this.size);
        } catch (EOFException e) {
            throw new AssertionError(e);
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public void skip(long byteCount) throws EOFException {
        while (byteCount > 0) {
            Segment segment = this.head;
            if (segment != null) {
                int toSkip = (int) Math.min(byteCount, (long) (segment.limit - this.head.pos));
                this.size -= (long) toSkip;
                byteCount -= (long) toSkip;
                this.head.pos += toSkip;
                if (this.head.pos == this.head.limit) {
                    Segment toRecycle = this.head;
                    this.head = toRecycle.pop();
                    SegmentPool.recycle(toRecycle);
                }
            } else {
                throw new EOFException();
            }
        }
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer write(ByteString byteString) {
        if (byteString != null) {
            byteString.write(this);
            return this;
        }
        throw new IllegalArgumentException("byteString == null");
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeUtf8(String string) {
        return writeUtf8(string, 0, string.length());
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeUtf8(String string, int beginIndex, int endIndex) {
        if (string == null) {
            throw new IllegalArgumentException("string == null");
        } else if (beginIndex < 0) {
            throw new IllegalArgumentException("beginIndex < 0: " + beginIndex);
        } else if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        } else if (endIndex <= string.length()) {
            int runSize = beginIndex;
            while (runSize < endIndex) {
                int c = string.charAt(runSize);
                if (c < 128) {
                    Segment tail = writableSegment(1);
                    byte[] data = tail.data;
                    int segmentOffset = tail.limit - runSize;
                    int runLimit = Math.min(endIndex, 8192 - segmentOffset);
                    int i = runSize + 1;
                    data[runSize + segmentOffset] = (byte) c;
                    while (i < runLimit) {
                        int c2 = string.charAt(i);
                        if (c2 >= 128) {
                            break;
                        }
                        data[i + segmentOffset] = (byte) c2;
                        i++;
                    }
                    int runSize2 = (i + segmentOffset) - tail.limit;
                    tail.limit += runSize2;
                    this.size += (long) runSize2;
                    runSize = i;
                } else if (c < 2048) {
                    writeByte((c >> 6) | 192);
                    writeByte(128 | (c & 63));
                    runSize++;
                } else if (c < 55296 || c > 57343) {
                    writeByte((c >> 12) | 224);
                    writeByte(((c >> 6) & 63) | 128);
                    writeByte(128 | (c & 63));
                    runSize++;
                } else {
                    int low = runSize + 1 < endIndex ? string.charAt(runSize + 1) : 0;
                    if (c > 56319 || low < 56320 || low > 57343) {
                        writeByte(63);
                        runSize++;
                    } else {
                        int codePoint = (((-55297 & c) << 10) | (-56321 & low)) + 65536;
                        writeByte((codePoint >> 18) | 240);
                        writeByte(((codePoint >> 12) & 63) | 128);
                        writeByte(((codePoint >> 6) & 63) | 128);
                        writeByte(128 | (codePoint & 63));
                        runSize += 2;
                    }
                }
            }
            return this;
        } else {
            throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string.length());
        }
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeUtf8CodePoint(int codePoint) {
        if (codePoint < 128) {
            writeByte(codePoint);
        } else if (codePoint < 2048) {
            writeByte((codePoint >> 6) | 192);
            writeByte(128 | (codePoint & 63));
        } else if (codePoint < 65536) {
            if (codePoint < 55296 || codePoint > 57343) {
                writeByte((codePoint >> 12) | 224);
                writeByte(((codePoint >> 6) & 63) | 128);
                writeByte(128 | (codePoint & 63));
            } else {
                writeByte(63);
            }
        } else if (codePoint <= 1114111) {
            writeByte((codePoint >> 18) | 240);
            writeByte(((codePoint >> 12) & 63) | 128);
            writeByte(((codePoint >> 6) & 63) | 128);
            writeByte(128 | (codePoint & 63));
        } else {
            throw new IllegalArgumentException("Unexpected code point: " + Integer.toHexString(codePoint));
        }
        return this;
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeString(String string, Charset charset) {
        return writeString(string, 0, string.length(), charset);
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeString(String string, int beginIndex, int endIndex, Charset charset) {
        if (string == null) {
            throw new IllegalArgumentException("string == null");
        } else if (beginIndex < 0) {
            throw new IllegalAccessError("beginIndex < 0: " + beginIndex);
        } else if (endIndex < beginIndex) {
            throw new IllegalArgumentException("endIndex < beginIndex: " + endIndex + " < " + beginIndex);
        } else if (endIndex > string.length()) {
            throw new IllegalArgumentException("endIndex > string.length: " + endIndex + " > " + string.length());
        } else if (charset == null) {
            throw new IllegalArgumentException("charset == null");
        } else if (charset.equals(Util.UTF_8)) {
            return writeUtf8(string, beginIndex, endIndex);
        } else {
            byte[] data = string.substring(beginIndex, endIndex).getBytes(charset);
            return write(data, 0, data.length);
        }
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer write(byte[] source) {
        if (source != null) {
            return write(source, 0, source.length);
        }
        throw new IllegalArgumentException("source == null");
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer write(byte[] source, int offset, int byteCount) {
        if (source != null) {
            Util.checkOffsetAndCount((long) source.length, (long) offset, (long) byteCount);
            int limit = offset + byteCount;
            while (offset < limit) {
                Segment tail = writableSegment(1);
                int toCopy = Math.min(limit - offset, 8192 - tail.limit);
                System.arraycopy(source, offset, tail.data, tail.limit, toCopy);
                offset += toCopy;
                tail.limit += toCopy;
            }
            this.size += (long) byteCount;
            return this;
        }
        throw new IllegalArgumentException("source == null");
    }

    @Override // java.nio.channels.WritableByteChannel
    public int write(ByteBuffer source) throws IOException {
        if (source != null) {
            int byteCount = source.remaining();
            int remaining = byteCount;
            while (remaining > 0) {
                Segment tail = writableSegment(1);
                int toCopy = Math.min(remaining, 8192 - tail.limit);
                source.get(tail.data, tail.limit, toCopy);
                remaining -= toCopy;
                tail.limit += toCopy;
            }
            this.size += (long) byteCount;
            return byteCount;
        }
        throw new IllegalArgumentException("source == null");
    }

    @Override // com.huawei.okio.BufferedSink
    public long writeAll(Source source) throws IOException {
        if (source != null) {
            long totalBytesRead = 0;
            while (true) {
                long readCount = source.read(this, 8192);
                if (readCount == -1) {
                    return totalBytesRead;
                }
                totalBytesRead += readCount;
            }
        } else {
            throw new IllegalArgumentException("source == null");
        }
    }

    @Override // com.huawei.okio.BufferedSink
    public BufferedSink write(Source source, long byteCount) throws IOException {
        while (byteCount > 0) {
            long read = source.read(this, byteCount);
            if (read != -1) {
                byteCount -= read;
            } else {
                throw new EOFException();
            }
        }
        return this;
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeByte(int b) {
        Segment tail = writableSegment(1);
        byte[] bArr = tail.data;
        int i = tail.limit;
        tail.limit = i + 1;
        bArr[i] = (byte) b;
        this.size++;
        return this;
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeShort(int s) {
        Segment tail = writableSegment(2);
        byte[] data = tail.data;
        int limit = tail.limit;
        int limit2 = limit + 1;
        data[limit] = (byte) ((s >>> 8) & 255);
        data[limit2] = (byte) (s & 255);
        tail.limit = limit2 + 1;
        this.size += 2;
        return this;
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeShortLe(int s) {
        return writeShort((int) Util.reverseBytesShort((short) s));
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeInt(int i) {
        Segment tail = writableSegment(4);
        byte[] data = tail.data;
        int limit = tail.limit;
        int limit2 = limit + 1;
        data[limit] = (byte) ((i >>> 24) & 255);
        int limit3 = limit2 + 1;
        data[limit2] = (byte) ((i >>> 16) & 255);
        int limit4 = limit3 + 1;
        data[limit3] = (byte) ((i >>> 8) & 255);
        data[limit4] = (byte) (i & 255);
        tail.limit = limit4 + 1;
        this.size += 4;
        return this;
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeIntLe(int i) {
        return writeInt(Util.reverseBytesInt(i));
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeLong(long v) {
        Segment tail = writableSegment(8);
        byte[] data = tail.data;
        int limit = tail.limit;
        int limit2 = limit + 1;
        data[limit] = (byte) ((int) ((v >>> 56) & 255));
        int limit3 = limit2 + 1;
        data[limit2] = (byte) ((int) ((v >>> 48) & 255));
        int limit4 = limit3 + 1;
        data[limit3] = (byte) ((int) ((v >>> 40) & 255));
        int limit5 = limit4 + 1;
        data[limit4] = (byte) ((int) ((v >>> 32) & 255));
        int limit6 = limit5 + 1;
        data[limit5] = (byte) ((int) ((v >>> 24) & 255));
        int limit7 = limit6 + 1;
        data[limit6] = (byte) ((int) ((v >>> 16) & 255));
        int limit8 = limit7 + 1;
        data[limit7] = (byte) ((int) ((v >>> 8) & 255));
        data[limit8] = (byte) ((int) (v & 255));
        tail.limit = limit8 + 1;
        this.size += 8;
        return this;
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeLongLe(long v) {
        return writeLong(Util.reverseBytesLong(v));
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeDecimalLong(long v) {
        int width;
        if (v == 0) {
            return writeByte(48);
        }
        boolean negative = false;
        if (v < 0) {
            v = -v;
            if (v < 0) {
                return writeUtf8("-9223372036854775808");
            }
            negative = true;
        }
        if (v < 100000000) {
            width = v < 10000 ? v < 100 ? v < 10 ? 1 : 2 : v < 1000 ? 3 : 4 : v < 1000000 ? v < 100000 ? 5 : 6 : v < 10000000 ? 7 : 8;
        } else if (v < 1000000000000L) {
            width = v < 10000000000L ? v < 1000000000 ? 9 : 10 : v < 100000000000L ? 11 : 12;
        } else if (v < 1000000000000000L) {
            width = v < 10000000000000L ? 13 : v < 100000000000000L ? 14 : 15;
        } else if (v < 100000000000000000L) {
            width = v < 10000000000000000L ? 16 : 17;
        } else {
            width = v < 1000000000000000000L ? 18 : 19;
        }
        if (negative) {
            width++;
        }
        Segment tail = writableSegment(width);
        byte[] data = tail.data;
        int pos = tail.limit + width;
        while (v != 0) {
            pos--;
            data[pos] = DIGITS[(int) (v % 10)];
            v /= 10;
        }
        if (negative) {
            data[pos - 1] = 45;
        }
        tail.limit += width;
        this.size += (long) width;
        return this;
    }

    @Override // com.huawei.okio.BufferedSink
    public Buffer writeHexadecimalUnsignedLong(long v) {
        if (v == 0) {
            return writeByte(48);
        }
        int width = (Long.numberOfTrailingZeros(Long.highestOneBit(v)) / 4) + 1;
        Segment tail = writableSegment(width);
        byte[] data = tail.data;
        int start = tail.limit;
        for (int pos = (tail.limit + width) - 1; pos >= start; pos--) {
            data[pos] = DIGITS[(int) (15 & v)];
            v >>>= 4;
        }
        tail.limit += width;
        this.size += (long) width;
        return this;
    }

    /* access modifiers changed from: package-private */
    public Segment writableSegment(int minimumCapacity) {
        if (minimumCapacity < 1 || minimumCapacity > 8192) {
            throw new IllegalArgumentException();
        }
        Segment segment = this.head;
        if (segment == null) {
            this.head = SegmentPool.take();
            Segment segment2 = this.head;
            segment2.prev = segment2;
            segment2.next = segment2;
            return segment2;
        }
        Segment tail = segment.prev;
        if (tail.limit + minimumCapacity > 8192 || !tail.owner) {
            return tail.push(SegmentPool.take());
        }
        return tail;
    }

    /* JADX INFO: Multiple debug info for r0v10 com.huawei.okio.Segment: [D('tail' com.huawei.okio.Segment), D('segmentToMove' com.huawei.okio.Segment)] */
    @Override // com.huawei.okio.Sink
    public void write(Buffer source, long byteCount) {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        } else if (source != this) {
            Util.checkOffsetAndCount(source.size, 0, byteCount);
            while (byteCount > 0) {
                if (byteCount < ((long) (source.head.limit - source.head.pos))) {
                    Segment segment = this.head;
                    Segment tail = segment != null ? segment.prev : null;
                    if (tail != null && tail.owner) {
                        if ((((long) tail.limit) + byteCount) - ((long) (tail.shared ? 0 : tail.pos)) <= 8192) {
                            source.head.writeTo(tail, (int) byteCount);
                            source.size -= byteCount;
                            this.size += byteCount;
                            return;
                        }
                    }
                    source.head = source.head.split((int) byteCount);
                }
                Segment segmentToMove = source.head;
                long movedByteCount = (long) (segmentToMove.limit - segmentToMove.pos);
                source.head = segmentToMove.pop();
                Segment segment2 = this.head;
                if (segment2 == null) {
                    this.head = segmentToMove;
                    Segment segment3 = this.head;
                    segment3.prev = segment3;
                    segment3.next = segment3;
                } else {
                    segment2.prev.push(segmentToMove).compact();
                }
                source.size -= movedByteCount;
                this.size += movedByteCount;
                byteCount -= movedByteCount;
            }
        } else {
            throw new IllegalArgumentException("source == this");
        }
    }

    @Override // com.huawei.okio.Source
    public long read(Buffer sink, long byteCount) {
        if (sink == null) {
            throw new IllegalArgumentException("sink == null");
        } else if (byteCount >= 0) {
            long j = this.size;
            if (j == 0) {
                return -1;
            }
            if (byteCount > j) {
                byteCount = this.size;
            }
            sink.write(this, byteCount);
            return byteCount;
        } else {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOf(byte b) {
        return indexOf(b, 0, Long.MAX_VALUE);
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOf(byte b, long fromIndex) {
        return indexOf(b, fromIndex, Long.MAX_VALUE);
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOf(byte b, long fromIndex, long toIndex) {
        Segment s;
        long offset;
        if (fromIndex < 0 || toIndex < fromIndex) {
            throw new IllegalArgumentException(String.format("size=%s fromIndex=%s toIndex=%s", Long.valueOf(this.size), Long.valueOf(fromIndex), Long.valueOf(toIndex)));
        }
        if (toIndex > this.size) {
            toIndex = this.size;
        }
        if (fromIndex == toIndex || (s = this.head) == null) {
            return -1;
        }
        if (this.size - fromIndex >= fromIndex) {
            offset = 0;
            while (true) {
                long nextOffset = ((long) (s.limit - s.pos)) + offset;
                if (nextOffset >= fromIndex) {
                    break;
                }
                s = s.next;
                offset = nextOffset;
            }
        } else {
            offset = this.size;
            while (offset > fromIndex) {
                s = s.prev;
                offset -= (long) (s.limit - s.pos);
            }
        }
        while (offset < toIndex) {
            byte[] data = s.data;
            int limit = (int) Math.min((long) s.limit, (((long) s.pos) + toIndex) - offset);
            for (int pos = (int) ((((long) s.pos) + fromIndex) - offset); pos < limit; pos++) {
                if (data[pos] == b) {
                    return ((long) (pos - s.pos)) + offset;
                }
            }
            offset += (long) (s.limit - s.pos);
            fromIndex = offset;
            s = s.next;
        }
        return -1;
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOf(ByteString bytes) throws IOException {
        return indexOf(bytes, 0);
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOf(ByteString bytes, long fromIndex) throws IOException {
        long offset;
        byte[] data;
        int pos;
        int segmentLimit;
        Segment s;
        if (bytes.size() == 0) {
            throw new IllegalArgumentException("bytes is empty");
        } else if (fromIndex >= 0) {
            Segment s2 = this.head;
            if (s2 == null) {
                return -1;
            }
            if (this.size - fromIndex >= fromIndex) {
                offset = 0;
                while (true) {
                    long nextOffset = ((long) (s2.limit - s2.pos)) + offset;
                    if (nextOffset >= fromIndex) {
                        break;
                    }
                    s2 = s2.next;
                    offset = nextOffset;
                }
            } else {
                offset = this.size;
                while (offset > fromIndex) {
                    s2 = s2.prev;
                    offset -= (long) (s2.limit - s2.pos);
                }
            }
            byte b0 = bytes.getByte(0);
            int bytesSize = bytes.size();
            long resultLimit = 1 + (this.size - ((long) bytesSize));
            long fromIndex2 = fromIndex;
            Segment s3 = s2;
            long offset2 = offset;
            while (offset2 < resultLimit) {
                byte[] data2 = s3.data;
                int segmentLimit2 = (int) Math.min((long) s3.limit, (((long) s3.pos) + resultLimit) - offset2);
                int pos2 = (int) ((((long) s3.pos) + fromIndex2) - offset2);
                while (pos2 < segmentLimit2) {
                    if (data2[pos2] == b0) {
                        pos = pos2;
                        segmentLimit = segmentLimit2;
                        data = data2;
                        s = s3;
                        if (rangeEquals(s3, pos2 + 1, bytes, 1, bytesSize)) {
                            return ((long) (pos - s.pos)) + offset2;
                        }
                    } else {
                        pos = pos2;
                        segmentLimit = segmentLimit2;
                        data = data2;
                        s = s3;
                    }
                    pos2 = pos + 1;
                    s3 = s;
                    segmentLimit2 = segmentLimit;
                    data2 = data;
                }
                offset2 += (long) (s3.limit - s3.pos);
                fromIndex2 = offset2;
                s3 = s3.next;
            }
            return -1;
        } else {
            throw new IllegalArgumentException("fromIndex < 0");
        }
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOfElement(ByteString targetBytes) {
        return indexOfElement(targetBytes, 0);
    }

    @Override // com.huawei.okio.BufferedSource
    public long indexOfElement(ByteString targetBytes, long fromIndex) {
        long offset;
        Buffer buffer = this;
        if (fromIndex >= 0) {
            Segment s = buffer.head;
            if (s == null) {
                return -1;
            }
            if (buffer.size - fromIndex >= fromIndex) {
                offset = 0;
                while (true) {
                    long nextOffset = ((long) (s.limit - s.pos)) + offset;
                    if (nextOffset >= fromIndex) {
                        break;
                    }
                    s = s.next;
                    offset = nextOffset;
                }
            } else {
                offset = buffer.size;
                while (offset > fromIndex) {
                    s = s.prev;
                    offset -= (long) (s.limit - s.pos);
                }
            }
            int i = 0;
            if (targetBytes.size() == 2) {
                byte b0 = targetBytes.getByte(0);
                byte b1 = targetBytes.getByte(1);
                long fromIndex2 = fromIndex;
                while (offset < buffer.size) {
                    byte[] data = s.data;
                    int limit = s.limit;
                    for (int pos = (int) ((((long) s.pos) + fromIndex2) - offset); pos < limit; pos++) {
                        byte b = data[pos];
                        if (b == b0 || b == b1) {
                            return ((long) (pos - s.pos)) + offset;
                        }
                    }
                    offset += (long) (s.limit - s.pos);
                    fromIndex2 = offset;
                    s = s.next;
                }
                return -1;
            }
            byte[] targetByteArray = targetBytes.internalArray();
            long fromIndex3 = fromIndex;
            while (offset < buffer.size) {
                byte[] data2 = s.data;
                int pos2 = (int) ((((long) s.pos) + fromIndex3) - offset);
                int limit2 = s.limit;
                while (pos2 < limit2) {
                    byte b2 = data2[pos2];
                    int length = targetByteArray.length;
                    while (i < length) {
                        if (b2 == targetByteArray[i]) {
                            return ((long) (pos2 - s.pos)) + offset;
                        }
                        i++;
                    }
                    pos2++;
                    i = 0;
                }
                offset += (long) (s.limit - s.pos);
                fromIndex3 = offset;
                s = s.next;
                i = 0;
                buffer = this;
            }
            return -1;
        }
        throw new IllegalArgumentException("fromIndex < 0");
    }

    @Override // com.huawei.okio.BufferedSource
    public boolean rangeEquals(long offset, ByteString bytes) {
        return rangeEquals(offset, bytes, 0, bytes.size());
    }

    @Override // com.huawei.okio.BufferedSource
    public boolean rangeEquals(long offset, ByteString bytes, int bytesOffset, int byteCount) {
        if (offset < 0 || bytesOffset < 0 || byteCount < 0 || this.size - offset < ((long) byteCount) || bytes.size() - bytesOffset < byteCount) {
            return false;
        }
        for (int i = 0; i < byteCount; i++) {
            if (getByte(((long) i) + offset) != bytes.getByte(bytesOffset + i)) {
                return false;
            }
        }
        return true;
    }

    private boolean rangeEquals(Segment segment, int segmentPos, ByteString bytes, int bytesOffset, int bytesLimit) {
        int segmentLimit = segment.limit;
        byte[] data = segment.data;
        for (int i = bytesOffset; i < bytesLimit; i++) {
            if (segmentPos == segmentLimit) {
                segment = segment.next;
                data = segment.data;
                segmentPos = segment.pos;
                segmentLimit = segment.limit;
            }
            if (data[segmentPos] != bytes.getByte(i)) {
                return false;
            }
            segmentPos++;
        }
        return true;
    }

    @Override // com.huawei.okio.BufferedSink, com.huawei.okio.Sink, java.io.Flushable
    public void flush() {
    }

    @Override // java.nio.channels.Channel
    public boolean isOpen() {
        return true;
    }

    @Override // com.huawei.okio.Source, java.io.Closeable, java.lang.AutoCloseable
    public void close() {
    }

    @Override // com.huawei.okio.Source
    public Timeout timeout() {
        return Timeout.NONE;
    }

    /* access modifiers changed from: package-private */
    public List<Integer> segmentSizes() {
        if (this.head == null) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>();
        result.add(Integer.valueOf(this.head.limit - this.head.pos));
        for (Segment s = this.head.next; s != this.head; s = s.next) {
            result.add(Integer.valueOf(s.limit - s.pos));
        }
        return result;
    }

    public final ByteString md5() {
        return digest("MD5");
    }

    public final ByteString sha1() {
        return digest("SHA-1");
    }

    public final ByteString sha256() {
        return digest(Encrypt.ALGORITHM_SHA256);
    }

    public final ByteString sha512() {
        return digest("SHA-512");
    }

    private ByteString digest(String algorithm) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            if (this.head != null) {
                messageDigest.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);
                for (Segment s = this.head.next; s != this.head; s = s.next) {
                    messageDigest.update(s.data, s.pos, s.limit - s.pos);
                }
            }
            return ByteString.of(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        }
    }

    public final ByteString hmacSha1(ByteString key) {
        return hmac("HmacSHA1", key);
    }

    public final ByteString hmacSha256(ByteString key) {
        return hmac("HmacSHA256", key);
    }

    public final ByteString hmacSha512(ByteString key) {
        return hmac("HmacSHA512", key);
    }

    private ByteString hmac(String algorithm, ByteString key) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key.toByteArray(), algorithm));
            if (this.head != null) {
                mac.update(this.head.data, this.head.pos, this.head.limit - this.head.pos);
                for (Segment s = this.head.next; s != this.head; s = s.next) {
                    mac.update(s.data, s.pos, s.limit - s.pos);
                }
            }
            return ByteString.of(mac.doFinal());
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        } catch (InvalidKeyException e2) {
            throw new IllegalArgumentException(e2);
        }
    }

    @Override // java.lang.Object
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Buffer)) {
            return false;
        }
        Buffer that = (Buffer) o;
        long j = this.size;
        if (j != that.size) {
            return false;
        }
        if (j == 0) {
            return true;
        }
        Segment sa = this.head;
        Segment sb = that.head;
        int posA = sa.pos;
        int posB = sb.pos;
        long pos = 0;
        while (pos < this.size) {
            long count = (long) Math.min(sa.limit - posA, sb.limit - posB);
            int i = 0;
            while (((long) i) < count) {
                int posA2 = posA + 1;
                int posB2 = posB + 1;
                if (sa.data[posA] != sb.data[posB]) {
                    return false;
                }
                i++;
                posA = posA2;
                posB = posB2;
            }
            if (posA == sa.limit) {
                sa = sa.next;
                posA = sa.pos;
            }
            if (posB == sb.limit) {
                sb = sb.next;
                posB = sb.pos;
            }
            pos += count;
        }
        return true;
    }

    @Override // java.lang.Object
    public int hashCode() {
        Segment s = this.head;
        if (s == null) {
            return 0;
        }
        int result = 1;
        do {
            int limit = s.limit;
            for (int pos = s.pos; pos < limit; pos++) {
                result = (result * 31) + s.data[pos];
            }
            s = s.next;
        } while (s != this.head);
        return result;
    }

    @Override // java.lang.Object
    public String toString() {
        return snapshot().toString();
    }

    @Override // java.lang.Object
    public Buffer clone() {
        Buffer result = new Buffer();
        if (this.size == 0) {
            return result;
        }
        result.head = this.head.sharedCopy();
        Segment segment = result.head;
        segment.prev = segment;
        segment.next = segment;
        for (Segment s = this.head.next; s != this.head; s = s.next) {
            result.head.prev.push(s.sharedCopy());
        }
        result.size = this.size;
        return result;
    }

    public final ByteString snapshot() {
        long j = this.size;
        if (j <= 2147483647L) {
            return snapshot((int) j);
        }
        throw new IllegalArgumentException("size > Integer.MAX_VALUE: " + this.size);
    }

    public final ByteString snapshot(int byteCount) {
        if (byteCount == 0) {
            return ByteString.EMPTY;
        }
        return new SegmentedByteString(this, byteCount);
    }

    public final UnsafeCursor readUnsafe() {
        return readUnsafe(new UnsafeCursor());
    }

    public final UnsafeCursor readUnsafe(UnsafeCursor unsafeCursor) {
        if (unsafeCursor.buffer == null) {
            unsafeCursor.buffer = this;
            unsafeCursor.readWrite = false;
            return unsafeCursor;
        }
        throw new IllegalStateException("already attached to a buffer");
    }

    public final UnsafeCursor readAndWriteUnsafe() {
        return readAndWriteUnsafe(new UnsafeCursor());
    }

    public final UnsafeCursor readAndWriteUnsafe(UnsafeCursor unsafeCursor) {
        if (unsafeCursor.buffer == null) {
            unsafeCursor.buffer = this;
            unsafeCursor.readWrite = true;
            return unsafeCursor;
        }
        throw new IllegalStateException("already attached to a buffer");
    }

    public static final class UnsafeCursor implements Closeable {
        public Buffer buffer;
        public byte[] data;
        public int end = -1;
        public long offset = -1;
        public boolean readWrite;
        private Segment segment;
        public int start = -1;

        public final int next() {
            if (this.offset != this.buffer.size) {
                long j = this.offset;
                if (j == -1) {
                    return seek(0);
                }
                return seek(j + ((long) (this.end - this.start)));
            }
            throw new IllegalStateException();
        }

        public final int seek(long offset2) {
            long nextOffset;
            Segment next;
            if (offset2 < -1 || offset2 > this.buffer.size) {
                throw new ArrayIndexOutOfBoundsException(String.format("offset=%s > size=%s", Long.valueOf(offset2), Long.valueOf(this.buffer.size)));
            } else if (offset2 == -1 || offset2 == this.buffer.size) {
                this.segment = null;
                this.offset = offset2;
                this.data = null;
                this.start = -1;
                this.end = -1;
                return -1;
            } else {
                long min = 0;
                long max = this.buffer.size;
                Segment head = this.buffer.head;
                Segment tail = this.buffer.head;
                Segment segment2 = this.segment;
                if (segment2 != null) {
                    long segmentOffset = this.offset - ((long) (this.start - segment2.pos));
                    if (segmentOffset > offset2) {
                        max = segmentOffset;
                        tail = this.segment;
                    } else {
                        min = segmentOffset;
                        head = this.segment;
                    }
                }
                if (max - offset2 > offset2 - min) {
                    next = head;
                    nextOffset = min;
                    while (offset2 >= ((long) (next.limit - next.pos)) + nextOffset) {
                        nextOffset += (long) (next.limit - next.pos);
                        next = next.next;
                    }
                } else {
                    next = tail;
                    nextOffset = max;
                    while (nextOffset > offset2) {
                        next = next.prev;
                        nextOffset -= (long) (next.limit - next.pos);
                    }
                }
                if (this.readWrite && next.shared) {
                    Segment unsharedNext = next.unsharedCopy();
                    if (this.buffer.head == next) {
                        this.buffer.head = unsharedNext;
                    }
                    next = next.push(unsharedNext);
                    next.prev.pop();
                }
                this.segment = next;
                this.offset = offset2;
                this.data = next.data;
                this.start = next.pos + ((int) (offset2 - nextOffset));
                this.end = next.limit;
                return this.end - this.start;
            }
        }

        public final long resizeBuffer(long newSize) {
            Buffer buffer2 = this.buffer;
            if (buffer2 == null) {
                throw new IllegalStateException("not attached to a buffer");
            } else if (this.readWrite) {
                long oldSize = buffer2.size;
                if (newSize <= oldSize) {
                    if (newSize >= 0) {
                        long bytesToSubtract = oldSize - newSize;
                        while (true) {
                            if (bytesToSubtract <= 0) {
                                break;
                            }
                            Segment tail = this.buffer.head.prev;
                            int tailSize = tail.limit - tail.pos;
                            if (((long) tailSize) > bytesToSubtract) {
                                tail.limit = (int) (((long) tail.limit) - bytesToSubtract);
                                break;
                            }
                            this.buffer.head = tail.pop();
                            SegmentPool.recycle(tail);
                            bytesToSubtract -= (long) tailSize;
                        }
                        this.segment = null;
                        this.offset = newSize;
                        this.data = null;
                        this.start = -1;
                        this.end = -1;
                    } else {
                        throw new IllegalArgumentException("newSize < 0: " + newSize);
                    }
                } else if (newSize > oldSize) {
                    boolean needsToSeek = true;
                    long bytesToAdd = newSize - oldSize;
                    while (bytesToAdd > 0) {
                        Segment tail2 = this.buffer.writableSegment(1);
                        int segmentBytesToAdd = (int) Math.min(bytesToAdd, (long) (8192 - tail2.limit));
                        tail2.limit += segmentBytesToAdd;
                        bytesToAdd -= (long) segmentBytesToAdd;
                        if (needsToSeek) {
                            this.segment = tail2;
                            this.offset = oldSize;
                            this.data = tail2.data;
                            this.start = tail2.limit - segmentBytesToAdd;
                            this.end = tail2.limit;
                            needsToSeek = false;
                        }
                    }
                }
                this.buffer.size = newSize;
                return oldSize;
            } else {
                throw new IllegalStateException("resizeBuffer() only permitted for read/write buffers");
            }
        }

        public final long expandBuffer(int minByteCount) {
            if (minByteCount <= 0) {
                throw new IllegalArgumentException("minByteCount <= 0: " + minByteCount);
            } else if (minByteCount <= 8192) {
                Buffer buffer2 = this.buffer;
                if (buffer2 == null) {
                    throw new IllegalStateException("not attached to a buffer");
                } else if (this.readWrite) {
                    long oldSize = buffer2.size;
                    Segment tail = this.buffer.writableSegment(minByteCount);
                    int result = 8192 - tail.limit;
                    tail.limit = 8192;
                    this.buffer.size = ((long) result) + oldSize;
                    this.segment = tail;
                    this.offset = oldSize;
                    this.data = tail.data;
                    this.start = 8192 - result;
                    this.end = 8192;
                    return (long) result;
                } else {
                    throw new IllegalStateException("expandBuffer() only permitted for read/write buffers");
                }
            } else {
                throw new IllegalArgumentException("minByteCount > Segment.SIZE: " + minByteCount);
            }
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            if (this.buffer != null) {
                this.buffer = null;
                this.segment = null;
                this.offset = -1;
                this.data = null;
                this.start = -1;
                this.end = -1;
                return;
            }
            throw new IllegalStateException("not attached to a buffer");
        }
    }
}
