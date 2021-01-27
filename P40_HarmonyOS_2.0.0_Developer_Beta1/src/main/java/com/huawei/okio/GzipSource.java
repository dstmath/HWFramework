package com.huawei.okio;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Inflater;

@Deprecated
public final class GzipSource implements Source {
    private static final byte FCOMMENT = 4;
    private static final byte FEXTRA = 2;
    private static final byte FHCRC = 1;
    private static final byte FNAME = 3;
    private static final byte SECTION_BODY = 1;
    private static final byte SECTION_DONE = 3;
    private static final byte SECTION_HEADER = 0;
    private static final byte SECTION_TRAILER = 2;
    private final CRC32 crc = new CRC32();
    private final Inflater inflater;
    private final InflaterSource inflaterSource;
    private int section = 0;
    private final BufferedSource source;

    public GzipSource(Source source2) {
        if (source2 != null) {
            this.inflater = new Inflater(true);
            this.source = Okio.buffer(source2);
            this.inflaterSource = new InflaterSource(this.source, this.inflater);
            return;
        }
        throw new IllegalArgumentException("source == null");
    }

    @Override // com.huawei.okio.Source
    public long read(Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        } else if (byteCount == 0) {
            return 0;
        } else {
            if (this.section == 0) {
                consumeHeader();
                this.section = 1;
            }
            if (this.section == 1) {
                long offset = sink.size;
                long result = this.inflaterSource.read(sink, byteCount);
                if (result != -1) {
                    updateCrc(sink, offset, result);
                    return result;
                }
                this.section = 2;
            }
            if (this.section == 2) {
                consumeTrailer();
                this.section = 3;
                if (!this.source.exhausted()) {
                    throw new IOException("gzip finished without exhausting source");
                }
            }
            return -1;
        }
    }

    private void consumeHeader() throws IOException {
        this.source.require(10);
        byte flags = this.source.buffer().getByte(3);
        boolean fhcrc = ((flags >> 1) & 1) == 1;
        if (fhcrc) {
            updateCrc(this.source.buffer(), 0, 10);
        }
        checkEqual("ID1ID2", 8075, this.source.readShort());
        this.source.skip(8);
        if (((flags >> 2) & 1) == 1) {
            this.source.require(2);
            if (fhcrc) {
                updateCrc(this.source.buffer(), 0, 2);
            }
            int xlen = this.source.buffer().readShortLe();
            this.source.require((long) xlen);
            if (fhcrc) {
                updateCrc(this.source.buffer(), 0, (long) xlen);
            }
            this.source.skip((long) xlen);
        }
        if (((flags >> 3) & 1) == 1) {
            long index = this.source.indexOf(SECTION_HEADER);
            if (index != -1) {
                if (fhcrc) {
                    updateCrc(this.source.buffer(), 0, index + 1);
                }
                this.source.skip(index + 1);
            } else {
                throw new EOFException();
            }
        }
        if (((flags >> FCOMMENT) & 1) == 1) {
            long index2 = this.source.indexOf(SECTION_HEADER);
            if (index2 != -1) {
                if (fhcrc) {
                    updateCrc(this.source.buffer(), 0, index2 + 1);
                }
                this.source.skip(1 + index2);
            } else {
                throw new EOFException();
            }
        }
        if (fhcrc) {
            checkEqual("FHCRC", this.source.readShortLe(), (short) ((int) this.crc.getValue()));
            this.crc.reset();
        }
    }

    private void consumeTrailer() throws IOException {
        checkEqual("CRC", this.source.readIntLe(), (int) this.crc.getValue());
        checkEqual("ISIZE", this.source.readIntLe(), (int) this.inflater.getBytesWritten());
    }

    @Override // com.huawei.okio.Source
    public Timeout timeout() {
        return this.source.timeout();
    }

    @Override // com.huawei.okio.Source, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.inflaterSource.close();
    }

    private void updateCrc(Buffer buffer, long offset, long byteCount) {
        Segment s = buffer.head;
        while (offset >= ((long) (s.limit - s.pos))) {
            offset -= (long) (s.limit - s.pos);
            s = s.next;
        }
        while (byteCount > 0) {
            int pos = (int) (((long) s.pos) + offset);
            int toUpdate = (int) Math.min((long) (s.limit - pos), byteCount);
            this.crc.update(s.data, pos, toUpdate);
            byteCount -= (long) toUpdate;
            offset = 0;
            s = s.next;
        }
    }

    private void checkEqual(String name, int expected, int actual) throws IOException {
        if (actual != expected) {
            throw new IOException(String.format("%s: actual 0x%08x != expected 0x%08x", name, Integer.valueOf(actual), Integer.valueOf(expected)));
        }
    }
}
