package com.android.okhttp.internal.framed;

import com.android.okhttp.okio.AsyncTimeout;
import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.Sink;
import com.android.okhttp.okio.Source;
import com.android.okhttp.okio.Timeout;
import com.squareup.okhttp.internal.framed.Header;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public final class FramedStream {
    static final /* synthetic */ boolean -assertionsDisabled = (FramedStream.class.desiredAssertionStatus() ^ 1);
    long bytesLeftInWriteWindow;
    private final FramedConnection connection;
    private ErrorCode errorCode = null;
    private final int id;
    private final StreamTimeout readTimeout = new StreamTimeout();
    private final List<Header> requestHeaders;
    private List<Header> responseHeaders;
    final FramedDataSink sink;
    private final FramedDataSource source;
    long unacknowledgedBytesRead = 0;
    private final StreamTimeout writeTimeout = new StreamTimeout();

    final class FramedDataSink implements Sink {
        static final /* synthetic */ boolean -assertionsDisabled = (FramedDataSink.class.desiredAssertionStatus() ^ 1);
        private static final long EMIT_BUFFER_SIZE = 16384;
        final /* synthetic */ boolean $assertionsDisabled;
        private boolean closed;
        private boolean finished;
        private final Buffer sendBuffer = new Buffer();

        FramedDataSink() {
        }

        public void write(Buffer source, long byteCount) throws IOException {
            if (-assertionsDisabled || !Thread.holdsLock(FramedStream.this)) {
                this.sendBuffer.write(source, byteCount);
                while (this.sendBuffer.size() >= EMIT_BUFFER_SIZE) {
                    emitDataFrame(-assertionsDisabled);
                }
                return;
            }
            throw new AssertionError();
        }

        private void emitDataFrame(boolean outFinished) throws IOException {
            long toWrite;
            boolean z = -assertionsDisabled;
            synchronized (FramedStream.this) {
                FramedStream.this.writeTimeout.enter();
                while (FramedStream.this.bytesLeftInWriteWindow <= 0 && (this.finished ^ 1) != 0 && (this.closed ^ 1) != 0 && FramedStream.this.errorCode == null) {
                    try {
                        FramedStream.this.waitForIo();
                    } catch (Throwable th) {
                        FramedStream.this.writeTimeout.exitAndThrowIfTimedOut();
                    }
                }
                FramedStream.this.writeTimeout.exitAndThrowIfTimedOut();
                FramedStream.this.checkOutNotClosed();
                toWrite = Math.min(FramedStream.this.bytesLeftInWriteWindow, this.sendBuffer.size());
                FramedStream framedStream = FramedStream.this;
                framedStream.bytesLeftInWriteWindow -= toWrite;
            }
            FramedStream.this.writeTimeout.enter();
            try {
                FramedConnection -get0 = FramedStream.this.connection;
                int -get2 = FramedStream.this.id;
                if (outFinished && toWrite == this.sendBuffer.size()) {
                    z = true;
                }
                -get0.writeData(-get2, z, this.sendBuffer, toWrite);
            } finally {
                FramedStream.this.writeTimeout.exitAndThrowIfTimedOut();
            }
        }

        public void flush() throws IOException {
            if (-assertionsDisabled || !Thread.holdsLock(FramedStream.this)) {
                synchronized (FramedStream.this) {
                    FramedStream.this.checkOutNotClosed();
                }
                while (this.sendBuffer.size() > 0) {
                    emitDataFrame(-assertionsDisabled);
                    FramedStream.this.connection.flush();
                }
                return;
            }
            throw new AssertionError();
        }

        public Timeout timeout() {
            return FramedStream.this.writeTimeout;
        }

        /* JADX WARNING: Missing block: B:15:0x0025, code:
            if (r6.this$0.sink.finished != false) goto L_0x0052;
     */
        /* JADX WARNING: Missing block: B:17:0x002f, code:
            if (r6.sendBuffer.size() <= 0) goto L_0x0042;
     */
        /* JADX WARNING: Missing block: B:19:0x0039, code:
            if (r6.sendBuffer.size() <= 0) goto L_0x0052;
     */
        /* JADX WARNING: Missing block: B:20:0x003b, code:
            emitDataFrame(true);
     */
        /* JADX WARNING: Missing block: B:24:0x0042, code:
            com.android.okhttp.internal.framed.FramedStream.-get0(r6.this$0).writeData(com.android.okhttp.internal.framed.FramedStream.-get2(r6.this$0), true, null, 0);
     */
        /* JADX WARNING: Missing block: B:25:0x0052, code:
            r0 = r6.this$0;
     */
        /* JADX WARNING: Missing block: B:26:0x0054, code:
            monitor-enter(r0);
     */
        /* JADX WARNING: Missing block: B:29:?, code:
            r6.closed = true;
     */
        /* JADX WARNING: Missing block: B:30:0x0058, code:
            monitor-exit(r0);
     */
        /* JADX WARNING: Missing block: B:31:0x0059, code:
            com.android.okhttp.internal.framed.FramedStream.-get0(r6.this$0).flush();
            com.android.okhttp.internal.framed.FramedStream.-wrap0(r6.this$0);
     */
        /* JADX WARNING: Missing block: B:32:0x0067, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void close() throws IOException {
            if (-assertionsDisabled || !Thread.holdsLock(FramedStream.this)) {
                synchronized (FramedStream.this) {
                    if (this.closed) {
                        return;
                    }
                }
            }
            throw new AssertionError();
        }
    }

    private final class FramedDataSource implements Source {
        static final /* synthetic */ boolean -assertionsDisabled = (FramedDataSource.class.desiredAssertionStatus() ^ 1);
        final /* synthetic */ boolean $assertionsDisabled;
        private boolean closed;
        private boolean finished;
        private final long maxByteCount;
        private final Buffer readBuffer;
        private final Buffer receiveBuffer;

        /* synthetic */ FramedDataSource(FramedStream this$0, long maxByteCount, FramedDataSource -this2) {
            this(maxByteCount);
        }

        private FramedDataSource(long maxByteCount) {
            this.receiveBuffer = new Buffer();
            this.readBuffer = new Buffer();
            this.maxByteCount = maxByteCount;
        }

        /* JADX WARNING: Missing block: B:18:0x0081, code:
            r3 = com.android.okhttp.internal.framed.FramedStream.-get0(r8.this$0);
     */
        /* JADX WARNING: Missing block: B:19:0x0087, code:
            monitor-enter(r3);
     */
        /* JADX WARNING: Missing block: B:21:?, code:
            r2 = com.android.okhttp.internal.framed.FramedStream.-get0(r8.this$0);
            r2.unacknowledgedBytesRead += r0;
     */
        /* JADX WARNING: Missing block: B:22:0x00ae, code:
            if (com.android.okhttp.internal.framed.FramedStream.-get0(r8.this$0).unacknowledgedBytesRead < ((long) (com.android.okhttp.internal.framed.FramedStream.-get0(r8.this$0).okHttpSettings.getInitialWindowSize(65536) / 2))) goto L_0x00cc;
     */
        /* JADX WARNING: Missing block: B:23:0x00b0, code:
            com.android.okhttp.internal.framed.FramedStream.-get0(r8.this$0).writeWindowUpdateLater(0, com.android.okhttp.internal.framed.FramedStream.-get0(r8.this$0).unacknowledgedBytesRead);
            com.android.okhttp.internal.framed.FramedStream.-get0(r8.this$0).unacknowledgedBytesRead = 0;
     */
        /* JADX WARNING: Missing block: B:24:0x00cc, code:
            monitor-exit(r3);
     */
        /* JADX WARNING: Missing block: B:25:0x00cd, code:
            return r0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            }
            synchronized (FramedStream.this) {
                waitUntilReadable();
                checkNotClosed();
                if (this.readBuffer.size() == 0) {
                    return -1;
                }
                long read = this.readBuffer.read(sink, Math.min(byteCount, this.readBuffer.size()));
                FramedStream framedStream = FramedStream.this;
                framedStream.unacknowledgedBytesRead += read;
                if (FramedStream.this.unacknowledgedBytesRead >= ((long) (FramedStream.this.connection.okHttpSettings.getInitialWindowSize(65536) / 2))) {
                    FramedStream.this.connection.writeWindowUpdateLater(FramedStream.this.id, FramedStream.this.unacknowledgedBytesRead);
                    FramedStream.this.unacknowledgedBytesRead = 0;
                }
            }
        }

        private void waitUntilReadable() throws IOException {
            FramedStream.this.readTimeout.enter();
            while (this.readBuffer.size() == 0 && (this.finished ^ 1) != 0 && (this.closed ^ 1) != 0 && FramedStream.this.errorCode == null) {
                try {
                    FramedStream.this.waitForIo();
                } catch (Throwable th) {
                    FramedStream.this.readTimeout.exitAndThrowIfTimedOut();
                }
            }
            FramedStream.this.readTimeout.exitAndThrowIfTimedOut();
        }

        void receive(BufferedSource in, long byteCount) throws IOException {
            if (-assertionsDisabled || !Thread.holdsLock(FramedStream.this)) {
                while (byteCount > 0) {
                    boolean finished;
                    boolean flowControlError;
                    synchronized (FramedStream.this) {
                        finished = this.finished;
                        flowControlError = this.readBuffer.size() + byteCount > this.maxByteCount;
                    }
                    if (flowControlError) {
                        in.skip(byteCount);
                        FramedStream.this.closeLater(ErrorCode.FLOW_CONTROL_ERROR);
                        return;
                    } else if (finished) {
                        in.skip(byteCount);
                        return;
                    } else {
                        long read = in.read(this.receiveBuffer, byteCount);
                        if (read == -1) {
                            throw new EOFException();
                        }
                        byteCount -= read;
                        synchronized (FramedStream.this) {
                            boolean wasEmpty = this.readBuffer.size() == 0;
                            this.readBuffer.writeAll(this.receiveBuffer);
                            if (wasEmpty) {
                                FramedStream.this.notifyAll();
                            }
                        }
                    }
                }
                return;
            }
            throw new AssertionError();
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

        protected void timedOut() {
            FramedStream.this.closeLater(ErrorCode.CANCEL);
        }

        protected IOException newTimeoutException(IOException cause) {
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

    FramedStream(int id, FramedConnection connection, boolean outFinished, boolean inFinished, List<Header> requestHeaders) {
        if (connection == null) {
            throw new NullPointerException("connection == null");
        } else if (requestHeaders == null) {
            throw new NullPointerException("requestHeaders == null");
        } else {
            this.id = id;
            this.connection = connection;
            this.bytesLeftInWriteWindow = (long) connection.peerSettings.getInitialWindowSize(65536);
            this.source = new FramedDataSource(this, (long) connection.okHttpSettings.getInitialWindowSize(65536), null);
            this.sink = new FramedDataSink();
            this.source.finished = inFinished;
            this.sink.finished = outFinished;
            this.requestHeaders = requestHeaders;
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

    public synchronized List<Header> getResponseHeaders() throws IOException {
        this.readTimeout.enter();
        while (this.responseHeaders == null && this.errorCode == null) {
            try {
                waitForIo();
            } catch (Throwable th) {
                this.readTimeout.exitAndThrowIfTimedOut();
            }
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

    public void reply(List<Header> responseHeaders, boolean out) throws IOException {
        if (-assertionsDisabled || !Thread.holdsLock(this)) {
            boolean outFinished = false;
            synchronized (this) {
                if (responseHeaders == null) {
                    throw new NullPointerException("responseHeaders == null");
                } else if (this.responseHeaders != null) {
                    throw new IllegalStateException("reply already sent");
                } else {
                    this.responseHeaders = responseHeaders;
                    if (!out) {
                        this.sink.finished = true;
                        outFinished = true;
                    }
                }
            }
            this.connection.writeSynReply(this.id, outFinished, responseHeaders);
            if (outFinished) {
                this.connection.flush();
                return;
            }
            return;
        }
        throw new AssertionError();
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
            if (this.responseHeaders != null || (isLocallyInitiated() ^ 1) == 0) {
            } else {
                throw new IllegalStateException("reply before requesting the sink");
            }
        }
        return this.sink;
    }

    public void close(ErrorCode rstStatusCode) throws IOException {
        if (closeInternal(rstStatusCode)) {
            this.connection.writeSynReset(this.id, rstStatusCode);
        }
    }

    public void closeLater(ErrorCode errorCode) {
        if (closeInternal(errorCode)) {
            this.connection.writeSynResetLater(this.id, errorCode);
        }
    }

    private boolean closeInternal(ErrorCode errorCode) {
        if (-assertionsDisabled || !Thread.holdsLock(this)) {
            synchronized (this) {
                if (this.errorCode != null) {
                    return false;
                } else if (this.source.finished && this.sink.finished) {
                    return false;
                } else {
                    this.errorCode = errorCode;
                    notifyAll();
                    this.connection.removeStream(this.id);
                    return true;
                }
            }
        }
        throw new AssertionError();
    }

    void receiveHeaders(List<Header> headers, HeadersMode headersMode) {
        if (-assertionsDisabled || !Thread.holdsLock(this)) {
            ErrorCode errorCode = null;
            boolean open = true;
            synchronized (this) {
                if (this.responseHeaders == null) {
                    if (headersMode.failIfHeadersAbsent()) {
                        errorCode = ErrorCode.PROTOCOL_ERROR;
                    } else {
                        this.responseHeaders = headers;
                        open = isOpen();
                        notifyAll();
                    }
                } else if (headersMode.failIfHeadersPresent()) {
                    errorCode = ErrorCode.STREAM_IN_USE;
                } else {
                    List<Header> newHeaders = new ArrayList();
                    newHeaders.addAll(this.responseHeaders);
                    newHeaders.addAll(headers);
                    this.responseHeaders = newHeaders;
                }
            }
            if (errorCode != null) {
                closeLater(errorCode);
                return;
            } else if (!open) {
                this.connection.removeStream(this.id);
                return;
            } else {
                return;
            }
        }
        throw new AssertionError();
    }

    void receiveData(BufferedSource in, int length) throws IOException {
        if (-assertionsDisabled || !Thread.holdsLock(this)) {
            this.source.receive(in, (long) length);
            return;
        }
        throw new AssertionError();
    }

    void receiveFin() {
        if (-assertionsDisabled || !Thread.holdsLock(this)) {
            boolean open;
            synchronized (this) {
                this.source.finished = true;
                open = isOpen();
                notifyAll();
            }
            if (!open) {
                this.connection.removeStream(this.id);
                return;
            }
            return;
        }
        throw new AssertionError();
    }

    synchronized void receiveRstStream(ErrorCode errorCode) {
        if (this.errorCode == null) {
            this.errorCode = errorCode;
            notifyAll();
        }
    }

    private void cancelStreamIfNecessary() throws IOException {
        if (-assertionsDisabled || !Thread.holdsLock(this)) {
            boolean cancel;
            boolean open;
            synchronized (this) {
                cancel = (this.source.finished || !this.source.closed) ? false : !this.sink.finished ? this.sink.closed : true;
                open = isOpen();
            }
            if (cancel) {
                close(ErrorCode.CANCEL);
                return;
            } else if (!open) {
                this.connection.removeStream(this.id);
                return;
            } else {
                return;
            }
        }
        throw new AssertionError();
    }

    void addBytesToWriteWindow(long delta) {
        this.bytesLeftInWriteWindow += delta;
        if (delta > 0) {
            notifyAll();
        }
    }

    private void checkOutNotClosed() throws IOException {
        if (this.sink.closed) {
            throw new IOException("stream closed");
        } else if (this.sink.finished) {
            throw new IOException("stream finished");
        } else if (this.errorCode != null) {
            throw new IOException("stream was reset: " + this.errorCode);
        }
    }

    private void waitForIo() throws InterruptedIOException {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new InterruptedIOException();
        }
    }
}
