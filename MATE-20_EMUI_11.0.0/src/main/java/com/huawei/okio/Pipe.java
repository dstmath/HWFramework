package com.huawei.okio;

import java.io.IOException;
import javax.annotation.Nullable;

public final class Pipe {
    final Buffer buffer = new Buffer();
    @Nullable
    private Sink foldedSink;
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

    public void fold(Sink sink2) throws IOException {
        boolean closed;
        Buffer sinkBuffer;
        while (true) {
            synchronized (this.buffer) {
                if (this.foldedSink != null) {
                    throw new IllegalStateException("sink already folded");
                } else if (this.buffer.exhausted()) {
                    this.sourceClosed = true;
                    this.foldedSink = sink2;
                    return;
                } else {
                    closed = this.sinkClosed;
                    sinkBuffer = new Buffer();
                    sinkBuffer.write(this.buffer, this.buffer.size);
                    this.buffer.notifyAll();
                }
            }
            try {
                sink2.write(sinkBuffer, sinkBuffer.size);
                if (closed) {
                    sink2.close();
                } else {
                    sink2.flush();
                }
                if (1 == 0) {
                    synchronized (this.buffer) {
                        this.sourceClosed = true;
                        this.buffer.notifyAll();
                    }
                }
            } catch (Throwable th) {
                if (0 == 0) {
                    synchronized (this.buffer) {
                        this.sourceClosed = true;
                        this.buffer.notifyAll();
                    }
                }
                throw th;
            }
        }
    }

    final class PipeSink implements Sink {
        final PushableTimeout timeout = new PushableTimeout();

        PipeSink() {
        }

        @Override // com.huawei.okio.Sink
        public void write(Buffer source, long byteCount) throws IOException {
            Sink delegate = null;
            synchronized (Pipe.this.buffer) {
                if (!Pipe.this.sinkClosed) {
                    while (true) {
                        if (byteCount <= 0) {
                            break;
                        } else if (Pipe.this.foldedSink != null) {
                            delegate = Pipe.this.foldedSink;
                            break;
                        } else if (!Pipe.this.sourceClosed) {
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
            if (delegate != null) {
                this.timeout.push(delegate.timeout());
                try {
                    delegate.write(source, byteCount);
                } finally {
                    this.timeout.pop();
                }
            }
        }

        @Override // com.huawei.okio.Sink, java.io.Flushable
        public void flush() throws IOException {
            Sink delegate = null;
            synchronized (Pipe.this.buffer) {
                if (Pipe.this.sinkClosed) {
                    throw new IllegalStateException("closed");
                } else if (Pipe.this.foldedSink != null) {
                    delegate = Pipe.this.foldedSink;
                } else if (Pipe.this.sourceClosed) {
                    if (Pipe.this.buffer.size() > 0) {
                        throw new IOException("source is closed");
                    }
                }
            }
            if (delegate != null) {
                this.timeout.push(delegate.timeout());
                try {
                    delegate.flush();
                } finally {
                    this.timeout.pop();
                }
            }
        }

        @Override // com.huawei.okio.Sink, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            Sink delegate = null;
            synchronized (Pipe.this.buffer) {
                if (!Pipe.this.sinkClosed) {
                    if (Pipe.this.foldedSink != null) {
                        delegate = Pipe.this.foldedSink;
                    } else {
                        if (Pipe.this.sourceClosed) {
                            if (Pipe.this.buffer.size() > 0) {
                                throw new IOException("source is closed");
                            }
                        }
                        Pipe.this.sinkClosed = true;
                        Pipe.this.buffer.notifyAll();
                    }
                } else {
                    return;
                }
            }
            if (delegate != null) {
                this.timeout.push(delegate.timeout());
                try {
                    delegate.close();
                } finally {
                    this.timeout.pop();
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

        @Override // com.huawei.okio.Source, java.io.Closeable, java.lang.AutoCloseable
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
