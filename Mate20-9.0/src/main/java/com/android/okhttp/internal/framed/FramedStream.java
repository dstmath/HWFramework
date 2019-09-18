package com.android.okhttp.internal.framed;

import com.android.okhttp.okio.AsyncTimeout;
import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.Sink;
import com.android.okhttp.okio.Source;
import com.android.okhttp.okio.Timeout;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public final class FramedStream {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    long bytesLeftInWriteWindow;
    /* access modifiers changed from: private */
    public final FramedConnection connection;
    /* access modifiers changed from: private */
    public ErrorCode errorCode = null;
    /* access modifiers changed from: private */
    public final int id;
    /* access modifiers changed from: private */
    public final StreamTimeout readTimeout = new StreamTimeout();
    private final List<Header> requestHeaders;
    private List<Header> responseHeaders;
    final FramedDataSink sink;
    private final FramedDataSource source;
    long unacknowledgedBytesRead = 0;
    /* access modifiers changed from: private */
    public final StreamTimeout writeTimeout = new StreamTimeout();

    final class FramedDataSink implements Sink {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final long EMIT_BUFFER_SIZE = 16384;
        /* access modifiers changed from: private */
        public boolean closed;
        /* access modifiers changed from: private */
        public boolean finished;
        private final Buffer sendBuffer = new Buffer();

        static {
            Class<FramedStream> cls = FramedStream.class;
        }

        FramedDataSink() {
        }

        public void write(Buffer source, long byteCount) throws IOException {
            this.sendBuffer.write(source, byteCount);
            while (this.sendBuffer.size() >= EMIT_BUFFER_SIZE) {
                emitDataFrame($assertionsDisabled);
            }
        }

        /* JADX INFO: finally extract failed */
        private void emitDataFrame(boolean outFinished) throws IOException {
            long toWrite;
            synchronized (FramedStream.this) {
                FramedStream.this.writeTimeout.enter();
                while (FramedStream.this.bytesLeftInWriteWindow <= 0 && !this.finished && !this.closed && FramedStream.this.errorCode == null) {
                    try {
                        FramedStream.this.waitForIo();
                    } catch (Throwable th) {
                        FramedStream.this.writeTimeout.exitAndThrowIfTimedOut();
                        throw th;
                    }
                }
                FramedStream.this.writeTimeout.exitAndThrowIfTimedOut();
                FramedStream.this.checkOutNotClosed();
                toWrite = Math.min(FramedStream.this.bytesLeftInWriteWindow, this.sendBuffer.size());
                FramedStream.this.bytesLeftInWriteWindow -= toWrite;
            }
            FramedStream.this.writeTimeout.enter();
            try {
                FramedStream.this.connection.writeData(FramedStream.this.id, (!outFinished || toWrite != this.sendBuffer.size()) ? $assertionsDisabled : true, this.sendBuffer, toWrite);
            } finally {
                FramedStream.this.writeTimeout.exitAndThrowIfTimedOut();
            }
        }

        public void flush() throws IOException {
            synchronized (FramedStream.this) {
                FramedStream.this.checkOutNotClosed();
            }
            while (this.sendBuffer.size() > 0) {
                emitDataFrame($assertionsDisabled);
                FramedStream.this.connection.flush();
            }
        }

        public Timeout timeout() {
            return FramedStream.this.writeTimeout;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x001e, code lost:
            if (r8.sendBuffer.size() <= 0) goto L_0x002e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0028, code lost:
            if (r8.sendBuffer.size() <= 0) goto L_0x0041;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
            emitDataFrame(true);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x002e, code lost:
            com.android.okhttp.internal.framed.FramedStream.access$500(r8.this$0).writeData(com.android.okhttp.internal.framed.FramedStream.access$600(r8.this$0), true, null, 0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0041, code lost:
            r2 = r8.this$0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0043, code lost:
            monitor-enter(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            r8.closed = true;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x0046, code lost:
            monitor-exit(r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0047, code lost:
            com.android.okhttp.internal.framed.FramedStream.access$500(r8.this$0).flush();
            com.android.okhttp.internal.framed.FramedStream.access$1000(r8.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0055, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
            if (r8.this$0.sink.finished != false) goto L_0x0041;
         */
        public void close() throws IOException {
            synchronized (FramedStream.this) {
                if (this.closed) {
                }
            }
        }
    }

    private final class FramedDataSource implements Source {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        /* access modifiers changed from: private */
        public boolean closed;
        /* access modifiers changed from: private */
        public boolean finished;
        private final long maxByteCount;
        private final Buffer readBuffer;
        private final Buffer receiveBuffer;

        static {
            Class<FramedStream> cls = FramedStream.class;
        }

        private FramedDataSource(long maxByteCount2) {
            this.receiveBuffer = new Buffer();
            this.readBuffer = new Buffer();
            this.maxByteCount = maxByteCount2;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0065, code lost:
            r5 = com.android.okhttp.internal.framed.FramedStream.access$500(r11.this$0);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x006b, code lost:
            monitor-enter(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
            com.android.okhttp.internal.framed.FramedStream.access$500(r11.this$0).unacknowledgedBytesRead += r3;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0090, code lost:
            if (com.android.okhttp.internal.framed.FramedStream.access$500(r11.this$0).unacknowledgedBytesRead < ((long) (com.android.okhttp.internal.framed.FramedStream.access$500(r11.this$0).okHttpSettings.getInitialWindowSize(65536) / 2))) goto L_0x00ac;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0092, code lost:
            com.android.okhttp.internal.framed.FramedStream.access$500(r11.this$0).writeWindowUpdateLater(0, com.android.okhttp.internal.framed.FramedStream.access$500(r11.this$0).unacknowledgedBytesRead);
            com.android.okhttp.internal.framed.FramedStream.access$500(r11.this$0).unacknowledgedBytesRead = 0;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x00ac, code lost:
            monitor-exit(r5);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x00ad, code lost:
            return r3;
         */
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount >= 0) {
                synchronized (FramedStream.this) {
                    waitUntilReadable();
                    checkNotClosed();
                    if (this.readBuffer.size() == 0) {
                        return -1;
                    }
                    long read = this.readBuffer.read(sink, Math.min(byteCount, this.readBuffer.size()));
                    FramedStream.this.unacknowledgedBytesRead += read;
                    if (FramedStream.this.unacknowledgedBytesRead >= ((long) (FramedStream.this.connection.okHttpSettings.getInitialWindowSize(65536) / 2))) {
                        FramedStream.this.connection.writeWindowUpdateLater(FramedStream.this.id, FramedStream.this.unacknowledgedBytesRead);
                        FramedStream.this.unacknowledgedBytesRead = 0;
                    }
                }
            } else {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            }
        }

        private void waitUntilReadable() throws IOException {
            FramedStream.this.readTimeout.enter();
            while (this.readBuffer.size() == 0 && !this.finished && !this.closed && FramedStream.this.errorCode == null) {
                try {
                    FramedStream.this.waitForIo();
                } finally {
                    FramedStream.this.readTimeout.exitAndThrowIfTimedOut();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void receive(BufferedSource in, long byteCount) throws IOException {
            boolean finished2;
            boolean z;
            boolean flowControlError;
            while (byteCount > 0) {
                synchronized (FramedStream.this) {
                    finished2 = this.finished;
                    z = false;
                    flowControlError = this.readBuffer.size() + byteCount > this.maxByteCount;
                }
                if (flowControlError) {
                    in.skip(byteCount);
                    FramedStream.this.closeLater(ErrorCode.FLOW_CONTROL_ERROR);
                    return;
                } else if (finished2) {
                    in.skip(byteCount);
                    return;
                } else {
                    long read = in.read(this.receiveBuffer, byteCount);
                    if (read != -1) {
                        long byteCount2 = byteCount - read;
                        synchronized (FramedStream.this) {
                            if (this.readBuffer.size() == 0) {
                                z = true;
                            }
                            boolean wasEmpty = z;
                            this.readBuffer.writeAll(this.receiveBuffer);
                            if (wasEmpty) {
                                FramedStream.this.notifyAll();
                            }
                        }
                        byteCount = byteCount2;
                    } else {
                        throw new EOFException();
                    }
                }
            }
        }

        public Timeout timeout() {
            return FramedStream.this.readTimeout;
        }

        public void close() throws IOException {
            synchronized (FramedStream.this) {
                this.closed = true;
                this.readBuffer.clear();
                FramedStream.this.notifyAll();
            }
            FramedStream.this.cancelStreamIfNecessary();
        }

        private void checkNotClosed() throws IOException {
            if (this.closed) {
                throw new IOException("stream closed");
            } else if (FramedStream.this.errorCode != null) {
                throw new IOException("stream was reset: " + FramedStream.this.errorCode);
            }
        }
    }

    class StreamTimeout extends AsyncTimeout {
        StreamTimeout() {
        }

        /* access modifiers changed from: protected */
        public void timedOut() {
            FramedStream.this.closeLater(ErrorCode.CANCEL);
        }

        /* access modifiers changed from: protected */
        public IOException newTimeoutException(IOException cause) {
            SocketTimeoutException socketTimeoutException = new SocketTimeoutException("timeout");
            if (cause != null) {
                socketTimeoutException.initCause(cause);
            }
            return socketTimeoutException;
        }

        public void exitAndThrowIfTimedOut() throws IOException {
            if (exit()) {
                throw newTimeoutException(null);
            }
        }
    }

    FramedStream(int id2, FramedConnection connection2, boolean outFinished, boolean inFinished, List<Header> requestHeaders2) {
        if (connection2 == null) {
            throw new NullPointerException("connection == null");
        } else if (requestHeaders2 != null) {
            this.id = id2;
            this.connection = connection2;
            this.bytesLeftInWriteWindow = (long) connection2.peerSettings.getInitialWindowSize(65536);
            this.source = new FramedDataSource((long) connection2.okHttpSettings.getInitialWindowSize(65536));
            this.sink = new FramedDataSink();
            boolean unused = this.source.finished = inFinished;
            boolean unused2 = this.sink.finished = outFinished;
            this.requestHeaders = requestHeaders2;
        } else {
            throw new NullPointerException("requestHeaders == null");
        }
    }

    public int getId() {
        return this.id;
    }

    public synchronized boolean isOpen() {
        if (this.errorCode != null) {
            return false;
        }
        if ((this.source.finished || this.source.closed) && ((this.sink.finished || this.sink.closed) && this.responseHeaders != null)) {
            return false;
        }
        return true;
    }

    public boolean isLocallyInitiated() {
        if (this.connection.client == ((this.id & 1) == 1)) {
            return true;
        }
        return false;
    }

    public FramedConnection getConnection() {
        return this.connection;
    }

    public List<Header> getRequestHeaders() {
        return this.requestHeaders;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0039, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003a, code lost:
        r3.readTimeout.exitAndThrowIfTimedOut();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x003f, code lost:
        throw r0;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
    public synchronized List<Header> getResponseHeaders() throws IOException {
        this.readTimeout.enter();
        while (this.responseHeaders == null && this.errorCode == null) {
            waitForIo();
        }
        this.readTimeout.exitAndThrowIfTimedOut();
        if (this.responseHeaders != null) {
        } else {
            throw new IOException("stream was reset: " + this.errorCode);
        }
        return this.responseHeaders;
    }

    public synchronized ErrorCode getErrorCode() {
        return this.errorCode;
    }

    public void reply(List<Header> responseHeaders2, boolean out) throws IOException {
        boolean outFinished = false;
        synchronized (this) {
            if (responseHeaders2 == null) {
                throw new NullPointerException("responseHeaders == null");
            } else if (this.responseHeaders == null) {
                this.responseHeaders = responseHeaders2;
                if (!out) {
                    boolean unused = this.sink.finished = true;
                    outFinished = true;
                }
            } else {
                throw new IllegalStateException("reply already sent");
            }
        }
        this.connection.writeSynReply(this.id, outFinished, responseHeaders2);
        if (outFinished) {
            this.connection.flush();
        }
    }

    public Timeout readTimeout() {
        return this.readTimeout;
    }

    public Timeout writeTimeout() {
        return this.writeTimeout;
    }

    public Source getSource() {
        return this.source;
    }

    public Sink getSink() {
        synchronized (this) {
            if (this.responseHeaders == null) {
                if (!isLocallyInitiated()) {
                    throw new IllegalStateException("reply before requesting the sink");
                }
            }
        }
        return this.sink;
    }

    public void close(ErrorCode rstStatusCode) throws IOException {
        if (closeInternal(rstStatusCode)) {
            this.connection.writeSynReset(this.id, rstStatusCode);
        }
    }

    public void closeLater(ErrorCode errorCode2) {
        if (closeInternal(errorCode2)) {
            this.connection.writeSynResetLater(this.id, errorCode2);
        }
    }

    private boolean closeInternal(ErrorCode errorCode2) {
        synchronized (this) {
            if (this.errorCode != null) {
                return false;
            }
            if (this.source.finished && this.sink.finished) {
                return false;
            }
            this.errorCode = errorCode2;
            notifyAll();
            this.connection.removeStream(this.id);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void receiveHeaders(List<Header> headers, HeadersMode headersMode) {
        ErrorCode errorCode2 = null;
        boolean open = true;
        synchronized (this) {
            if (this.responseHeaders == null) {
                if (headersMode.failIfHeadersAbsent()) {
                    errorCode2 = ErrorCode.PROTOCOL_ERROR;
                } else {
                    this.responseHeaders = headers;
                    open = isOpen();
                    notifyAll();
                }
            } else if (headersMode.failIfHeadersPresent()) {
                errorCode2 = ErrorCode.STREAM_IN_USE;
            } else {
                List<Header> newHeaders = new ArrayList<>();
                newHeaders.addAll(this.responseHeaders);
                newHeaders.addAll(headers);
                this.responseHeaders = newHeaders;
            }
        }
        if (errorCode2 != null) {
            closeLater(errorCode2);
        } else if (!open) {
            this.connection.removeStream(this.id);
        }
    }

    /* access modifiers changed from: package-private */
    public void receiveData(BufferedSource in, int length) throws IOException {
        this.source.receive(in, (long) length);
    }

    /* access modifiers changed from: package-private */
    public void receiveFin() {
        boolean open;
        synchronized (this) {
            boolean unused = this.source.finished = true;
            open = isOpen();
            notifyAll();
        }
        if (!open) {
            this.connection.removeStream(this.id);
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void receiveRstStream(ErrorCode errorCode2) {
        if (this.errorCode == null) {
            this.errorCode = errorCode2;
            notifyAll();
        }
    }

    /* access modifiers changed from: private */
    public void cancelStreamIfNecessary() throws IOException {
        boolean cancel;
        boolean open;
        synchronized (this) {
            cancel = !this.source.finished && this.source.closed && (this.sink.finished || this.sink.closed);
            open = isOpen();
        }
        if (cancel) {
            close(ErrorCode.CANCEL);
        } else if (!open) {
            this.connection.removeStream(this.id);
        }
    }

    /* access modifiers changed from: package-private */
    public void addBytesToWriteWindow(long delta) {
        this.bytesLeftInWriteWindow += delta;
        if (delta > 0) {
            notifyAll();
        }
    }

    /* access modifiers changed from: private */
    public void checkOutNotClosed() throws IOException {
        if (this.sink.closed) {
            throw new IOException("stream closed");
        } else if (this.sink.finished) {
            throw new IOException("stream finished");
        } else if (this.errorCode != null) {
            throw new IOException("stream was reset: " + this.errorCode);
        }
    }

    /* access modifiers changed from: private */
    public void waitForIo() throws InterruptedIOException {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
    }
}
