package com.huawei.okio;

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

public final class GzipSink implements Sink {
    private boolean closed;
    private final CRC32 crc = new CRC32();
    private final Deflater deflater;
    private final DeflaterSink deflaterSink;
    private final BufferedSink sink;

    public GzipSink(Sink sink2) {
        if (sink2 != null) {
            this.deflater = new Deflater(-1, true);
            this.sink = Okio.buffer(sink2);
            this.deflaterSink = new DeflaterSink(this.sink, this.deflater);
            writeHeader();
            return;
        }
        throw new IllegalArgumentException("sink == null");
    }

    @Override // com.huawei.okio.Sink
    public void write(Buffer source, long byteCount) throws IOException {
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        } else if (byteCount != 0) {
            updateCrc(source, byteCount);
            this.deflaterSink.write(source, byteCount);
        }
    }

    @Override // com.huawei.okio.Sink, java.io.Flushable
    public void flush() throws IOException {
        this.deflaterSink.flush();
    }

    @Override // com.huawei.okio.Sink
    public Timeout timeout() {
        return this.sink.timeout();
    }

    @Override // java.io.Closeable, com.huawei.okio.Sink, java.lang.AutoCloseable
    public void close() throws IOException {
        if (!this.closed) {
            Throwable thrown = null;
            try {
                this.deflaterSink.finishDeflate();
                writeFooter();
            } catch (Throwable e) {
                thrown = e;
            }
            try {
                this.deflater.end();
            } catch (Throwable e2) {
                if (thrown == null) {
                    thrown = e2;
                }
            }
            try {
                this.sink.close();
            } catch (Throwable e3) {
                if (thrown == null) {
                    thrown = e3;
                }
            }
            this.closed = true;
            if (thrown != null) {
                Util.sneakyRethrow(thrown);
            }
        }
    }

    public final Deflater deflater() {
        return this.deflater;
    }

    private void writeHeader() {
        Buffer buffer = this.sink.buffer();
        buffer.writeShort(8075);
        buffer.writeByte(8);
        buffer.writeByte(0);
        buffer.writeInt(0);
        buffer.writeByte(0);
        buffer.writeByte(0);
    }

    private void writeFooter() throws IOException {
        this.sink.writeIntLe((int) this.crc.getValue());
        this.sink.writeIntLe((int) this.deflater.getBytesRead());
    }

    private void updateCrc(Buffer buffer, long byteCount) {
        Segment head = buffer.head;
        while (byteCount > 0) {
            int segmentLength = (int) Math.min(byteCount, (long) (head.limit - head.pos));
            this.crc.update(head.data, head.pos, segmentLength);
            byteCount -= (long) segmentLength;
            head = head.next;
        }
    }
}
