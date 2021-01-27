package com.huawei.okio;

import java.io.IOException;

@Deprecated
final class PeekSource implements Source {
    private final Buffer buffer;
    private boolean closed;
    private int expectedPos;
    private Segment expectedSegment = this.buffer.head;
    private long pos;
    private final BufferedSource upstream;

    PeekSource(BufferedSource upstream2) {
        this.upstream = upstream2;
        this.buffer = upstream2.buffer();
        Segment segment = this.expectedSegment;
        this.expectedPos = segment != null ? segment.pos : -1;
    }

    @Override // com.huawei.okio.Source
    public long read(Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        } else if (!this.closed) {
            Segment segment = this.expectedSegment;
            if (segment != null && (segment != this.buffer.head || this.expectedPos != this.buffer.head.pos)) {
                throw new IllegalStateException("Peek source is invalid because upstream source was used");
            } else if (byteCount == 0) {
                return 0;
            } else {
                if (!this.upstream.request(this.pos + 1)) {
                    return -1;
                }
                if (this.expectedSegment == null && this.buffer.head != null) {
                    this.expectedSegment = this.buffer.head;
                    this.expectedPos = this.buffer.head.pos;
                }
                long toCopy = Math.min(byteCount, this.buffer.size - this.pos);
                this.buffer.copyTo(sink, this.pos, toCopy);
                this.pos += toCopy;
                return toCopy;
            }
        } else {
            throw new IllegalStateException("closed");
        }
    }

    @Override // com.huawei.okio.Source
    public Timeout timeout() {
        return this.upstream.timeout();
    }

    @Override // com.huawei.okio.Source, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.closed = true;
    }
}
