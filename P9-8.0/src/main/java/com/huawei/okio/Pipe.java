package com.huawei.okio;

import java.io.IOException;

public final class Pipe {
    final Buffer buffer = new Buffer();
    final long maxBufferSize;
    private final Sink sink = new PipeSink();
    boolean sinkClosed;
    private final Source source = new PipeSource();
    boolean sourceClosed;

    final class PipeSink implements Sink {
        final Timeout timeout = new Timeout();

        PipeSink() {
        }

        public void write(Buffer source, long byteCount) throws IOException {
            synchronized (Pipe.this.buffer) {
                if (Pipe.this.sinkClosed) {
                    throw new IllegalStateException("closed");
                }
                while (byteCount > 0) {
                    if (Pipe.this.sourceClosed) {
                        throw new IOException("source is closed");
                    }
                    long bufferSpaceAvailable = Pipe.this.maxBufferSize - Pipe.this.buffer.size();
                    if (bufferSpaceAvailable == 0) {
                        this.timeout.waitUntilNotified(Pipe.this.buffer);
                    } else {
                        long bytesToWrite = Math.min(bufferSpaceAvailable, byteCount);
                        Pipe.this.buffer.write(source, bytesToWrite);
                        byteCount -= bytesToWrite;
                        Pipe.this.buffer.notifyAll();
                    }
                }
            }
        }

        public void flush() throws IOException {
            synchronized (Pipe.this.buffer) {
                if (Pipe.this.sinkClosed) {
                    throw new IllegalStateException("closed");
                }
                while (Pipe.this.buffer.size() > 0) {
                    if (Pipe.this.sourceClosed) {
                        throw new IOException("source is closed");
                    }
                    this.timeout.waitUntilNotified(Pipe.this.buffer);
                }
            }
        }

        public void close() throws IOException {
            synchronized (Pipe.this.buffer) {
                if (Pipe.this.sinkClosed) {
                    return;
                }
                try {
                    flush();
                    Pipe.this.sinkClosed = true;
                    Pipe.this.buffer.notifyAll();
                } catch (Throwable th) {
                    Pipe.this.sinkClosed = true;
                    Pipe.this.buffer.notifyAll();
                }
            }
        }

        public Timeout timeout() {
            return this.timeout;
        }
    }

    final class PipeSource implements Source {
        final Timeout timeout = new Timeout();

        PipeSource() {
        }

        public long read(Buffer sink, long byteCount) throws IOException {
            synchronized (Pipe.this.buffer) {
                if (Pipe.this.sourceClosed) {
                    throw new IllegalStateException("closed");
                }
                while (Pipe.this.buffer.size() == 0) {
                    if (Pipe.this.sinkClosed) {
                        return -1;
                    }
                    this.timeout.waitUntilNotified(Pipe.this.buffer);
                }
                long result = Pipe.this.buffer.read(sink, byteCount);
                Pipe.this.buffer.notifyAll();
                return result;
            }
        }

        public void close() throws IOException {
            synchronized (Pipe.this.buffer) {
                Pipe.this.sourceClosed = true;
                Pipe.this.buffer.notifyAll();
            }
        }

        public Timeout timeout() {
            return this.timeout;
        }
    }

    public Pipe(long maxBufferSize) {
        if (maxBufferSize < 1) {
            throw new IllegalArgumentException("maxBufferSize < 1: " + maxBufferSize);
        }
        this.maxBufferSize = maxBufferSize;
    }

    public Source source() {
        return this.source;
    }

    public Sink sink() {
        return this.sink;
    }
}
