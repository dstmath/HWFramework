package com.android.okhttp.okio;

import java.io.IOException;
import java.util.zip.Deflater;

public final class DeflaterSink implements Sink {
    private boolean closed;
    private final Deflater deflater;
    private final BufferedSink sink;

    public DeflaterSink(Sink sink2, Deflater deflater2) {
        this(Okio.buffer(sink2), deflater2);
    }

    DeflaterSink(BufferedSink sink2, Deflater deflater2) {
        if (sink2 == null) {
            throw new IllegalArgumentException("source == null");
        } else if (deflater2 != null) {
            this.sink = sink2;
            this.deflater = deflater2;
        } else {
            throw new IllegalArgumentException("inflater == null");
        }
    }

    public void write(Buffer source, long byteCount) throws IOException {
        Util.checkOffsetAndCount(source.size, 0, byteCount);
        while (byteCount > 0) {
            Segment head = source.head;
            int toDeflate = (int) Math.min(byteCount, (long) (head.limit - head.pos));
            this.deflater.setInput(head.data, head.pos, toDeflate);
            deflate(false);
            source.size -= (long) toDeflate;
            head.pos += toDeflate;
            if (head.pos == head.limit) {
                source.head = head.pop();
                SegmentPool.recycle(head);
            }
            byteCount -= (long) toDeflate;
        }
    }

    private void deflate(boolean syncFlush) throws IOException {
        Segment s;
        int deflated;
        Buffer buffer = this.sink.buffer();
        while (true) {
            s = buffer.writableSegment(1);
            if (syncFlush) {
                deflated = this.deflater.deflate(s.data, s.limit, 8192 - s.limit, 2);
            } else {
                deflated = this.deflater.deflate(s.data, s.limit, 8192 - s.limit);
            }
            if (deflated > 0) {
                s.limit += deflated;
                buffer.size += (long) deflated;
                this.sink.emitCompleteSegments();
            } else if (this.deflater.needsInput()) {
                break;
            }
        }
        if (s.pos == s.limit) {
            buffer.head = s.pop();
            SegmentPool.recycle(s);
        }
    }

    public void flush() throws IOException {
        deflate(true);
        this.sink.flush();
    }

    /* access modifiers changed from: package-private */
    public void finishDeflate() throws IOException {
        this.deflater.finish();
        deflate(false);
    }

    public void close() throws IOException {
        if (!this.closed) {
            Throwable thrown = null;
            try {
                finishDeflate();
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

    public Timeout timeout() {
        return this.sink.timeout();
    }

    public String toString() {
        return "DeflaterSink(" + this.sink + ")";
    }
}
