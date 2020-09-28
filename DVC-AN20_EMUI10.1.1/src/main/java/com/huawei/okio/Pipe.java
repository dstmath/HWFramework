package com.huawei.okio;

import java.io.IOException;

public final class Pipe {
    final Buffer buffer = new Buffer();
    final long maxBufferSize;
    private final Sink sink = new PipeSink();
    boolean sinkClosed;
    private final Source source = new PipeSource();
    boolean sourceClosed;

    public Pipe(long maxBufferSize2) {
        if (maxBufferSize2 >= 1) {
            this.maxBufferSize = maxBufferSize2;
            return;
        }
        throw new IllegalArgumentException("maxBufferSize < 1: " + maxBufferSize2);
    }

    public final Source source() {
        return this.source;
    }

    public final Sink sink() {
        return this.sink;
    }

    final class PipeSink implements Sink {
        final Timeout timeout = new Timeout();

        PipeSink() {
        }

        @Override // com.huawei.okio.Sink
        public void write(Buffer source, long byteCount) throws IOException {
            synchronized (Pipe.this.buffer) {
                if (!Pipe.this.sinkClosed) {
                    while (byteCount > 0) {
                        if (!Pipe.this.sourceClosed) {
                            long bufferSpaceAvailable = Pipe.this.maxBufferSize - Pipe.this.buffer.size();
                            if (bufferSpaceAvailable == 0) {
                                this.timeout.waitUntilNotified(Pipe.this.buffer);
                            } else {
                                long bytesToWrite = Math.min(bufferSpaceAvailable, byteCount);
                                Pipe.this.buffer.write(source, bytesToWrite);
                                byteCount -= bytesToWrite;
                                Pipe.this.buffer.notifyAll();
                            }
                        } else {
                            throw new IOException("source is closed");
                        }
                    }
                } else {
                    throw new IllegalStateException("closed");
                }
            }
        }

        @Override // com.huawei.okio.Sink, java.io.Flushable
        public void flush() throws IOException {
            synchronized (Pipe.this.buffer) {
                if (Pipe.this.sinkClosed) {
                    throw new IllegalStateException("closed");
                } else if (Pipe.this.sourceClosed) {
                    if (Pipe.this.buffer.size() > 0) {
                        throw new IOException("source is closed");
                    }
                }
            }
        }

        @Override // java.io.Closeable, com.huawei.okio.Sink, java.lang.AutoCloseable
        public void close() throws IOException {
            synchronized (Pipe.this.buffer) {
                if (!Pipe.this.sinkClosed) {
                    if (Pipe.this.sourceClosed) {
                        if (Pipe.this.buffer.size() > 0) {
                            throw new IOException("source is closed");
                        }
                    }
                    Pipe.this.sinkClosed = true;
                    Pipe.this.buffer.notifyAll();
                }
            }
        }

        @Override // com.huawei.okio.Sink
        public Timeout timeout() {
            return this.timeout;
        }
    }

    final class PipeSource implements Source {
        final Timeout timeout = new Timeout();

        PipeSource() {
        }

        @Override // com.huawei.okio.Source
        public long read(Buffer sink, long byteCount) throws IOException {
            synchronized (Pipe.this.buffer) {
                if (!Pipe.this.sourceClosed) {
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
                throw new IllegalStateException("closed");
            }
        }

        @Override // java.io.Closeable, com.huawei.okio.Source, java.lang.AutoCloseable
        public void close() throws IOException {
            synchronized (Pipe.this.buffer) {
                Pipe.this.sourceClosed = true;
                Pipe.this.buffer.notifyAll();
            }
        }

        @Override // com.huawei.okio.Source
        public Timeout timeout() {
            return this.timeout;
        }
    }
}
