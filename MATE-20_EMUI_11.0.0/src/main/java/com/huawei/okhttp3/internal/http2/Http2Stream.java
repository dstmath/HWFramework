package com.huawei.okhttp3.internal.http2;

import com.huawei.okhttp3.Headers;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okio.AsyncTimeout;
import com.huawei.okio.Buffer;
import com.huawei.okio.BufferedSource;
import com.huawei.okio.Sink;
import com.huawei.okio.Source;
import com.huawei.okio.Timeout;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import javax.annotation.Nullable;

public final class Http2Stream {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    long bytesLeftInWriteWindow;
    final Http2Connection connection;
    @Nullable
    ErrorCode errorCode;
    @Nullable
    IOException errorException;
    private boolean hasResponseHeaders;
    private final Deque<Headers> headersQueue = new ArrayDeque();
    final int id;
    final StreamTimeout readTimeout = new StreamTimeout();
    final FramingSink sink;
    private final FramingSource source;
    long unacknowledgedBytesRead = 0;
    final StreamTimeout writeTimeout = new StreamTimeout();

    Http2Stream(int id2, Http2Connection connection2, boolean outFinished, boolean inFinished, @Nullable Headers headers) {
        if (connection2 != null) {
            this.id = id2;
            this.connection = connection2;
            this.bytesLeftInWriteWindow = (long) connection2.peerSettings.getInitialWindowSize();
            this.source = new FramingSource((long) connection2.okHttpSettings.getInitialWindowSize());
            this.sink = new FramingSink();
            this.source.finished = inFinished;
            this.sink.finished = outFinished;
            if (headers != null) {
                this.headersQueue.add(headers);
            }
            if (isLocallyInitiated() && headers != null) {
                throw new IllegalStateException("locally-initiated streams shouldn't have headers yet");
            } else if (!isLocallyInitiated() && headers == null) {
                throw new IllegalStateException("remotely-initiated streams should have headers");
            }
        } else {
            throw new NullPointerException("connection == null");
        }
    }

    public int getId() {
        return this.id;
    }

    public synchronized boolean isOpen() {
        if (this.errorCode != null) {
            return false;
        }
        if ((this.source.finished || this.source.closed) && ((this.sink.finished || this.sink.closed) && this.hasResponseHeaders)) {
            return false;
        }
        return true;
    }

    public boolean isLocallyInitiated() {
        return this.connection.client == ((this.id & 1) == 1);
    }

    public Http2Connection getConnection() {
        return this.connection;
    }

    public synchronized Headers takeHeaders() throws IOException {
        Throwable th;
        this.readTimeout.enter();
        while (this.headersQueue.isEmpty()) {
            try {
                try {
                    if (this.errorCode != null) {
                        break;
                    }
                    waitForIo();
                } catch (Throwable th2) {
                    th = th2;
                    this.readTimeout.exitAndThrowIfTimedOut();
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                this.readTimeout.exitAndThrowIfTimedOut();
                throw th;
            }
        }
        this.readTimeout.exitAndThrowIfTimedOut();
        if (!this.headersQueue.isEmpty()) {
        } else if (this.errorException != null) {
            throw this.errorException;
        } else {
            throw new StreamResetException(this.errorCode);
        }
        return this.headersQueue.removeFirst();
    }

    public synchronized Headers trailers() throws IOException {
        if (this.errorCode != null) {
            if (this.errorException != null) {
                throw this.errorException;
            }
            throw new StreamResetException(this.errorCode);
        } else if (!this.source.finished || !this.source.receiveBuffer.exhausted() || !this.source.readBuffer.exhausted()) {
            throw new IllegalStateException("too early; can't read the trailers yet");
        }
        return this.source.trailers != null ? this.source.trailers : Util.EMPTY_HEADERS;
    }

    public synchronized ErrorCode getErrorCode() {
        return this.errorCode;
    }

    public void writeHeaders(List<Header> responseHeaders, boolean outFinished, boolean flushHeaders) throws IOException {
        boolean z;
        if (responseHeaders != null) {
            synchronized (this) {
                z = true;
                this.hasResponseHeaders = true;
                if (outFinished) {
                    this.sink.finished = true;
                }
            }
            if (!flushHeaders) {
                synchronized (this.connection) {
                    if (this.connection.bytesLeftInWriteWindow != 0) {
                        z = false;
                    }
                    flushHeaders = z;
                }
            }
            this.connection.writeHeaders(this.id, outFinished, responseHeaders);
            if (flushHeaders) {
                this.connection.flush();
                return;
            }
            return;
        }
        throw new NullPointerException("headers == null");
    }

    public void enqueueTrailers(Headers trailers) {
        synchronized (this) {
            if (this.sink.finished) {
                throw new IllegalStateException("already finished");
            } else if (trailers.size() != 0) {
                this.sink.trailers = trailers;
            } else {
                throw new IllegalArgumentException("trailers.size() == 0");
            }
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
            if (!this.hasResponseHeaders) {
                if (!isLocallyInitiated()) {
                    throw new IllegalStateException("reply before requesting the sink");
                }
            }
        }
        return this.sink;
    }

    public void close(ErrorCode rstStatusCode, @Nullable IOException errorException2) throws IOException {
        if (closeInternal(rstStatusCode, errorException2)) {
            this.connection.writeSynReset(this.id, rstStatusCode);
        }
    }

    public void closeLater(ErrorCode errorCode2) {
        if (closeInternal(errorCode2, null)) {
            this.connection.writeSynResetLater(this.id, errorCode2);
        }
    }

    private boolean closeInternal(ErrorCode errorCode2, @Nullable IOException errorException2) {
        synchronized (this) {
            if (this.errorCode != null) {
                return false;
            }
            if (this.source.finished && this.sink.finished) {
                return false;
            }
            this.errorCode = errorCode2;
            this.errorException = errorException2;
            notifyAll();
            this.connection.removeStream(this.id);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void receiveData(BufferedSource in, int length) throws IOException {
        this.source.receive(in, (long) length);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0019  */
    public void receiveHeaders(Headers headers, boolean inFinished) {
        boolean open;
        synchronized (this) {
            if (this.hasResponseHeaders) {
                if (inFinished) {
                    this.source.trailers = headers;
                    if (inFinished) {
                        this.source.finished = true;
                    }
                    open = isOpen();
                    notifyAll();
                }
            }
            this.hasResponseHeaders = true;
            this.headersQueue.add(headers);
            if (inFinished) {
            }
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
    public final class FramingSource implements Source {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        boolean closed;
        boolean finished;
        private final long maxByteCount;
        private final Buffer readBuffer = new Buffer();
        private final Buffer receiveBuffer = new Buffer();
        private Headers trailers;

        FramingSource(long maxByteCount2) {
            this.maxByteCount = maxByteCount2;
        }

        /* JADX WARNING: Removed duplicated region for block: B:33:0x00a3  */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x00a7  */
        @Override // com.huawei.okio.Source
        public long read(Buffer sink, long byteCount) throws IOException {
            long readBytesDelivered;
            IOException iOException;
            if (byteCount >= 0) {
                while (true) {
                    readBytesDelivered = -1;
                    IOException errorExceptionToDeliver = null;
                    synchronized (Http2Stream.this) {
                        Http2Stream.this.readTimeout.enter();
                        try {
                            if (Http2Stream.this.errorCode != null) {
                                if (Http2Stream.this.errorException != null) {
                                    iOException = Http2Stream.this.errorException;
                                } else {
                                    iOException = new StreamResetException(Http2Stream.this.errorCode);
                                }
                                errorExceptionToDeliver = iOException;
                            }
                            if (!this.closed) {
                                if (this.readBuffer.size() <= 0) {
                                    if (this.finished || errorExceptionToDeliver != null) {
                                        break;
                                    }
                                    Http2Stream.this.waitForIo();
                                } else {
                                    readBytesDelivered = this.readBuffer.read(sink, Math.min(byteCount, this.readBuffer.size()));
                                    Http2Stream.this.unacknowledgedBytesRead += readBytesDelivered;
                                    if (errorExceptionToDeliver == null && Http2Stream.this.unacknowledgedBytesRead >= ((long) (Http2Stream.this.connection.okHttpSettings.getInitialWindowSize() / 2))) {
                                        Http2Stream.this.connection.writeWindowUpdateLater(Http2Stream.this.id, Http2Stream.this.unacknowledgedBytesRead);
                                        Http2Stream.this.unacknowledgedBytesRead = 0;
                                    }
                                }
                            } else {
                                throw new IOException("stream closed");
                            }
                        } finally {
                            Http2Stream.this.readTimeout.exitAndThrowIfTimedOut();
                        }
                    }
                    if (readBytesDelivered == -1) {
                        updateConnectionFlowControl(readBytesDelivered);
                        return readBytesDelivered;
                    } else if (errorExceptionToDeliver == null) {
                        return -1;
                    } else {
                        throw errorExceptionToDeliver;
                    }
                }
                Http2Stream.this.readTimeout.exitAndThrowIfTimedOut();
                if (readBytesDelivered == -1) {
                }
            } else {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            }
        }

        private void updateConnectionFlowControl(long read) {
            Http2Stream.this.connection.updateConnectionFlowControl(read);
        }

        /* access modifiers changed from: package-private */
        public void receive(BufferedSource in, long byteCount) throws IOException {
            boolean finished2;
            boolean wasEmpty;
            boolean flowControlError;
            long bytesDiscarded = byteCount;
            while (bytesDiscarded > 0) {
                synchronized (Http2Stream.this) {
                    finished2 = this.finished;
                    wasEmpty = true;
                    flowControlError = this.readBuffer.size() + bytesDiscarded > this.maxByteCount;
                }
                if (flowControlError) {
                    in.skip(bytesDiscarded);
                    Http2Stream.this.closeLater(ErrorCode.FLOW_CONTROL_ERROR);
                    return;
                } else if (finished2) {
                    in.skip(bytesDiscarded);
                    return;
                } else {
                    long read = in.read(this.receiveBuffer, bytesDiscarded);
                    if (read != -1) {
                        long byteCount2 = bytesDiscarded - read;
                        long bytesDiscarded2 = 0;
                        synchronized (Http2Stream.this) {
                            if (this.closed) {
                                bytesDiscarded2 = this.receiveBuffer.size();
                                this.receiveBuffer.clear();
                            } else {
                                if (this.readBuffer.size() != 0) {
                                    wasEmpty = false;
                                }
                                this.readBuffer.writeAll(this.receiveBuffer);
                                if (wasEmpty) {
                                    Http2Stream.this.notifyAll();
                                }
                            }
                        }
                        if (bytesDiscarded2 > 0) {
                            updateConnectionFlowControl(bytesDiscarded2);
                        }
                        bytesDiscarded = byteCount2;
                    } else {
                        throw new EOFException();
                    }
                }
            }
        }

        @Override // com.huawei.okio.Source
        public Timeout timeout() {
            return Http2Stream.this.readTimeout;
        }

        @Override // com.huawei.okio.Source, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            long bytesDiscarded;
            synchronized (Http2Stream.this) {
                this.closed = true;
                bytesDiscarded = this.readBuffer.size();
                this.readBuffer.clear();
                Http2Stream.this.notifyAll();
            }
            if (bytesDiscarded > 0) {
                updateConnectionFlowControl(bytesDiscarded);
            }
            Http2Stream.this.cancelStreamIfNecessary();
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelStreamIfNecessary() throws IOException {
        boolean cancel;
        boolean open;
        synchronized (this) {
            cancel = !this.source.finished && this.source.closed && (this.sink.finished || this.sink.closed);
            open = isOpen();
        }
        if (cancel) {
            close(ErrorCode.CANCEL, null);
        } else if (!open) {
            this.connection.removeStream(this.id);
        }
    }

    /* access modifiers changed from: package-private */
    public final class FramingSink implements Sink {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final long EMIT_BUFFER_SIZE = 16384;
        boolean closed;
        boolean finished;
        private final Buffer sendBuffer = new Buffer();
        private Headers trailers;

        FramingSink() {
        }

        @Override // com.huawei.okio.Sink
        public void write(Buffer source, long byteCount) throws IOException {
            this.sendBuffer.write(source, byteCount);
            while (this.sendBuffer.size() >= EMIT_BUFFER_SIZE) {
                emitFrame(false);
            }
        }

        /* JADX INFO: finally extract failed */
        private void emitFrame(boolean outFinishedOnLastFrame) throws IOException {
            long toWrite;
            boolean outFinished;
            synchronized (Http2Stream.this) {
                Http2Stream.this.writeTimeout.enter();
                while (Http2Stream.this.bytesLeftInWriteWindow <= 0 && !this.finished && !this.closed && Http2Stream.this.errorCode == null) {
                    try {
                        Http2Stream.this.waitForIo();
                    } catch (Throwable th) {
                        Http2Stream.this.writeTimeout.exitAndThrowIfTimedOut();
                        throw th;
                    }
                }
                Http2Stream.this.writeTimeout.exitAndThrowIfTimedOut();
                Http2Stream.this.checkOutNotClosed();
                toWrite = Math.min(Http2Stream.this.bytesLeftInWriteWindow, this.sendBuffer.size());
                Http2Stream.this.bytesLeftInWriteWindow -= toWrite;
            }
            Http2Stream.this.writeTimeout.enter();
            if (outFinishedOnLastFrame) {
                try {
                    if (toWrite == this.sendBuffer.size()) {
                        outFinished = true;
                        Http2Stream.this.connection.writeData(Http2Stream.this.id, outFinished, this.sendBuffer, toWrite);
                        Http2Stream.this.writeTimeout.exitAndThrowIfTimedOut();
                    }
                } catch (Throwable th2) {
                    Http2Stream.this.writeTimeout.exitAndThrowIfTimedOut();
                    throw th2;
                }
            }
            outFinished = false;
            Http2Stream.this.connection.writeData(Http2Stream.this.id, outFinished, this.sendBuffer, toWrite);
            Http2Stream.this.writeTimeout.exitAndThrowIfTimedOut();
        }

        @Override // com.huawei.okio.Sink, java.io.Flushable
        public void flush() throws IOException {
            synchronized (Http2Stream.this) {
                Http2Stream.this.checkOutNotClosed();
            }
            while (this.sendBuffer.size() > 0) {
                emitFrame(false);
                Http2Stream.this.connection.flush();
            }
        }

        @Override // com.huawei.okio.Sink
        public Timeout timeout() {
            return Http2Stream.this.writeTimeout;
        }

        @Override // com.huawei.okio.Sink, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            synchronized (Http2Stream.this) {
                if (this.closed) {
                    return;
                }
            }
            if (!Http2Stream.this.sink.finished) {
                boolean hasData = this.sendBuffer.size() > 0;
                if (this.trailers != null) {
                    while (this.sendBuffer.size() > 0) {
                        emitFrame(false);
                    }
                    Http2Stream.this.connection.writeHeaders(Http2Stream.this.id, true, Util.toHeaderBlock(this.trailers));
                } else if (hasData) {
                    while (this.sendBuffer.size() > 0) {
                        emitFrame(true);
                    }
                } else {
                    Http2Stream.this.connection.writeData(Http2Stream.this.id, true, null, 0);
                }
            }
            synchronized (Http2Stream.this) {
                this.closed = true;
            }
            Http2Stream.this.connection.flush();
            Http2Stream.this.cancelStreamIfNecessary();
        }
    }

    /* access modifiers changed from: package-private */
    public void addBytesToWriteWindow(long delta) {
        this.bytesLeftInWriteWindow += delta;
        if (delta > 0) {
            notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void checkOutNotClosed() throws IOException {
        if (this.sink.closed) {
            throw new IOException("stream closed");
        } else if (!this.sink.finished) {
            ErrorCode errorCode2 = this.errorCode;
            if (errorCode2 != null) {
                IOException iOException = this.errorException;
                if (iOException != null) {
                    throw iOException;
                }
                throw new StreamResetException(errorCode2);
            }
        } else {
            throw new IOException("stream finished");
        }
    }

    /* access modifiers changed from: package-private */
    public void waitForIo() throws InterruptedIOException {
        try {
            wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException();
        }
    }

    /* access modifiers changed from: package-private */
    public class StreamTimeout extends AsyncTimeout {
        StreamTimeout() {
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.okio.AsyncTimeout
        public void timedOut() {
            Http2Stream.this.closeLater(ErrorCode.CANCEL);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.okio.AsyncTimeout
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
}
