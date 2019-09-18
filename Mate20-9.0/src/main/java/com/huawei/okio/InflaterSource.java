package com.huawei.okio;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public final class InflaterSource implements Source {
    private int bufferBytesHeldByInflater;
    private boolean closed;
    private final Inflater inflater;
    private final BufferedSource source;

    public InflaterSource(Source source2, Inflater inflater2) {
        this(Okio.buffer(source2), inflater2);
    }

    InflaterSource(BufferedSource source2, Inflater inflater2) {
        if (source2 == null) {
            throw new IllegalArgumentException("source == null");
        } else if (inflater2 != null) {
            this.source = source2;
            this.inflater = inflater2;
        } else {
            throw new IllegalArgumentException("inflater == null");
        }
    }

    public long read(Buffer sink, long byteCount) throws IOException {
        Segment tail;
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        } else if (this.closed) {
            throw new IllegalStateException("closed");
        } else if (byteCount == 0) {
            return 0;
        } else {
            while (true) {
                boolean sourceExhausted = refill();
                try {
                    tail = sink.writableSegment(1);
                    int bytesInflated = this.inflater.inflate(tail.data, tail.limit, (int) Math.min(byteCount, (long) (8192 - tail.limit)));
                    if (bytesInflated > 0) {
                        tail.limit += bytesInflated;
                        sink.size += (long) bytesInflated;
                        return (long) bytesInflated;
                    } else if (this.inflater.finished()) {
                        break;
                    } else if (this.inflater.needsDictionary()) {
                        break;
                    } else if (sourceExhausted) {
                        throw new EOFException("source exhausted prematurely");
                    }
                } catch (DataFormatException e) {
                    throw new IOException(e);
                }
            }
            releaseInflatedBytes();
            if (tail.pos == tail.limit) {
                sink.head = tail.pop();
                SegmentPool.recycle(tail);
            }
            return -1;
        }
    }

    public final boolean refill() throws IOException {
        if (!this.inflater.needsInput()) {
            return false;
        }
        releaseInflatedBytes();
        if (this.inflater.getRemaining() != 0) {
            throw new IllegalStateException("?");
        } else if (this.source.exhausted()) {
            return true;
        } else {
            Segment head = this.source.buffer().head;
            this.bufferBytesHeldByInflater = head.limit - head.pos;
            this.inflater.setInput(head.data, head.pos, this.bufferBytesHeldByInflater);
            return false;
        }
    }

    private void releaseInflatedBytes() throws IOException {
        if (this.bufferBytesHeldByInflater != 0) {
            int toRelease = this.bufferBytesHeldByInflater - this.inflater.getRemaining();
            this.bufferBytesHeldByInflater -= toRelease;
            this.source.skip((long) toRelease);
        }
    }

    public Timeout timeout() {
        return this.source.timeout();
    }

    public void close() throws IOException {
        if (!this.closed) {
            this.inflater.end();
            this.closed = true;
            this.source.close();
        }
    }
}
