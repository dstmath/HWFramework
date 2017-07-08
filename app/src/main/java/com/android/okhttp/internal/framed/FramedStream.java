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
    static final /* synthetic */ boolean -assertionsDisabled = false;
    long bytesLeftInWriteWindow;
    private final FramedConnection connection;
    private ErrorCode errorCode;
    private final int id;
    private final StreamTimeout readTimeout;
    private final List<Header> requestHeaders;
    private List<Header> responseHeaders;
    final FramedDataSink sink;
    private final FramedDataSource source;
    long unacknowledgedBytesRead;
    private final StreamTimeout writeTimeout;

    final class FramedDataSink implements Sink {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        private static final long EMIT_BUFFER_SIZE = 16384;
        final /* synthetic */ boolean $assertionsDisabled;
        private boolean closed;
        private boolean finished;
        private final Buffer sendBuffer;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.framed.FramedStream.FramedDataSink.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.framed.FramedStream.FramedDataSink.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.framed.FramedStream.FramedDataSink.<clinit>():void");
        }

        FramedDataSink() {
            this.sendBuffer = new Buffer();
        }

        public void write(Buffer source, long byteCount) throws IOException {
            if (!-assertionsDisabled) {
                if (!(Thread.holdsLock(FramedStream.this) ? -assertionsDisabled : true)) {
                    throw new AssertionError();
                }
            }
            this.sendBuffer.write(source, byteCount);
            while (this.sendBuffer.size() >= EMIT_BUFFER_SIZE) {
                emitDataFrame(-assertionsDisabled);
            }
        }

        private void emitDataFrame(boolean outFinished) throws IOException {
            boolean z = -assertionsDisabled;
            synchronized (FramedStream.this) {
                FramedStream.this.writeTimeout.enter();
                while (FramedStream.this.bytesLeftInWriteWindow <= 0 && !this.finished) {
                    try {
                        if (!this.closed && FramedStream.this.errorCode == null) {
                            FramedStream.this.waitForIo();
                        }
                    } catch (Throwable th) {
                        FramedStream.this.writeTimeout.exitAndThrowIfTimedOut();
                    }
                }
                FramedStream.this.writeTimeout.exitAndThrowIfTimedOut();
                FramedStream.this.checkOutNotClosed();
                long toWrite = Math.min(FramedStream.this.bytesLeftInWriteWindow, this.sendBuffer.size());
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
            if (!-assertionsDisabled) {
                if (!(Thread.holdsLock(FramedStream.this) ? -assertionsDisabled : true)) {
                    throw new AssertionError();
                }
            }
            synchronized (FramedStream.this) {
                FramedStream.this.checkOutNotClosed();
            }
            while (this.sendBuffer.size() > 0) {
                emitDataFrame(-assertionsDisabled);
                FramedStream.this.connection.flush();
            }
        }

        public Timeout timeout() {
            return FramedStream.this.writeTimeout;
        }

        public void close() throws IOException {
            if (!-assertionsDisabled) {
                if (!(Thread.holdsLock(FramedStream.this) ? -assertionsDisabled : true)) {
                    throw new AssertionError();
                }
            }
            synchronized (FramedStream.this) {
                if (this.closed) {
                    return;
                }
                if (!FramedStream.this.sink.finished) {
                    if (this.sendBuffer.size() > 0) {
                        while (this.sendBuffer.size() > 0) {
                            emitDataFrame(true);
                        }
                    } else {
                        FramedStream.this.connection.writeData(FramedStream.this.id, true, null, 0);
                    }
                }
                synchronized (FramedStream.this) {
                    this.closed = true;
                }
                FramedStream.this.connection.flush();
                FramedStream.this.cancelStreamIfNecessary();
            }
        }
    }

    private final class FramedDataSource implements Source {
        static final /* synthetic */ boolean -assertionsDisabled = false;
        final /* synthetic */ boolean $assertionsDisabled;
        private boolean closed;
        private boolean finished;
        private final long maxByteCount;
        private final Buffer readBuffer;
        private final Buffer receiveBuffer;
        final /* synthetic */ FramedStream this$0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.framed.FramedStream.FramedDataSource.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.framed.FramedStream.FramedDataSource.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.framed.FramedStream.FramedDataSource.<clinit>():void");
        }

        /* synthetic */ FramedDataSource(FramedStream this$0, long maxByteCount, FramedDataSource framedDataSource) {
            this(this$0, maxByteCount);
        }

        private FramedDataSource(FramedStream this$0, long maxByteCount) {
            this.this$0 = this$0;
            this.receiveBuffer = new Buffer();
            this.readBuffer = new Buffer();
            this.maxByteCount = maxByteCount;
        }

        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            }
            synchronized (this.this$0) {
                waitUntilReadable();
                checkNotClosed();
                if (this.readBuffer.size() == 0) {
                    return -1;
                }
                long read = this.readBuffer.read(sink, Math.min(byteCount, this.readBuffer.size()));
                FramedStream framedStream = this.this$0;
                framedStream.unacknowledgedBytesRead += read;
                if (this.this$0.unacknowledgedBytesRead >= ((long) (this.this$0.connection.okHttpSettings.getInitialWindowSize(65536) / 2))) {
                    this.this$0.connection.writeWindowUpdateLater(this.this$0.id, this.this$0.unacknowledgedBytesRead);
                    this.this$0.unacknowledgedBytesRead = 0;
                }
                synchronized (this.this$0.connection) {
                    FramedConnection -get0 = this.this$0.connection;
                    -get0.unacknowledgedBytesRead += read;
                    if (this.this$0.connection.unacknowledgedBytesRead >= ((long) (this.this$0.connection.okHttpSettings.getInitialWindowSize(65536) / 2))) {
                        this.this$0.connection.writeWindowUpdateLater(0, this.this$0.connection.unacknowledgedBytesRead);
                        this.this$0.connection.unacknowledgedBytesRead = 0;
                    }
                }
                return read;
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void waitUntilReadable() throws IOException {
            this.this$0.readTimeout.enter();
            while (this.readBuffer.size() == 0 && !this.finished) {
                try {
                    if (!this.closed && this.this$0.errorCode == null) {
                        this.this$0.waitForIo();
                    }
                } catch (Throwable th) {
                    this.this$0.readTimeout.exitAndThrowIfTimedOut();
                }
            }
            this.this$0.readTimeout.exitAndThrowIfTimedOut();
        }

        void receive(BufferedSource in, long byteCount) throws IOException {
            if (!-assertionsDisabled) {
                if ((Thread.holdsLock(this.this$0) ? null : 1) == null) {
                    throw new AssertionError();
                }
            }
            while (byteCount > 0) {
                synchronized (this.this$0) {
                    boolean finished = this.finished;
                    boolean flowControlError = this.readBuffer.size() + byteCount > this.maxByteCount;
                }
                if (flowControlError) {
                    in.skip(byteCount);
                    this.this$0.closeLater(ErrorCode.FLOW_CONTROL_ERROR);
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
                    synchronized (this.this$0) {
                        boolean wasEmpty = this.readBuffer.size() == 0;
                        this.readBuffer.writeAll(this.receiveBuffer);
                        if (wasEmpty) {
                            this.this$0.notifyAll();
                        }
                    }
                }
            }
        }

        public Timeout timeout() {
            return this.this$0.readTimeout;
        }

        public void close() throws IOException {
            synchronized (this.this$0) {
                this.closed = true;
                this.readBuffer.clear();
                this.this$0.notifyAll();
            }
            this.this$0.cancelStreamIfNecessary();
        }

        private void checkNotClosed() throws IOException {
            if (this.closed) {
                throw new IOException("stream closed");
            } else if (this.this$0.errorCode != null) {
                throw new IOException("stream was reset: " + this.this$0.errorCode);
            }
        }
    }

    class StreamTimeout extends AsyncTimeout {
        final /* synthetic */ FramedStream this$0;

        StreamTimeout(FramedStream this$0) {
            this.this$0 = this$0;
        }

        protected void timedOut() {
            this.this$0.closeLater(ErrorCode.CANCEL);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.framed.FramedStream.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.framed.FramedStream.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.framed.FramedStream.<clinit>():void");
    }

    FramedStream(int id, FramedConnection connection, boolean outFinished, boolean inFinished, List<Header> requestHeaders) {
        this.unacknowledgedBytesRead = 0;
        this.readTimeout = new StreamTimeout(this);
        this.writeTimeout = new StreamTimeout(this);
        this.errorCode = null;
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
        Object obj = 1;
        if (!-assertionsDisabled) {
            if (Thread.holdsLock(this)) {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
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
            if (this.responseHeaders != null || isLocallyInitiated()) {
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
        if (!-assertionsDisabled) {
            if (!(!Thread.holdsLock(this))) {
                throw new AssertionError();
            }
        }
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

    void receiveHeaders(List<Header> headers, HeadersMode headersMode) {
        if (!-assertionsDisabled) {
            if ((Thread.holdsLock(this) ? null : 1) == null) {
                throw new AssertionError();
            }
        }
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
        } else if (!open) {
            this.connection.removeStream(this.id);
        }
    }

    void receiveData(BufferedSource in, int length) throws IOException {
        if (!-assertionsDisabled) {
            if ((Thread.holdsLock(this) ? null : 1) == null) {
                throw new AssertionError();
            }
        }
        this.source.receive(in, (long) length);
    }

    void receiveFin() {
        Object obj = 1;
        if (!-assertionsDisabled) {
            if (Thread.holdsLock(this)) {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        synchronized (this) {
            this.source.finished = true;
            boolean open = isOpen();
            notifyAll();
        }
        if (!open) {
            this.connection.removeStream(this.id);
        }
    }

    synchronized void receiveRstStream(ErrorCode errorCode) {
        if (this.errorCode == null) {
            this.errorCode = errorCode;
            notifyAll();
        }
    }

    private void cancelStreamIfNecessary() throws IOException {
        if (!-assertionsDisabled) {
            if ((Thread.holdsLock(this) ? null : 1) == null) {
                throw new AssertionError();
            }
        }
        synchronized (this) {
            boolean cancel = (this.source.finished || !this.source.closed) ? false : !this.sink.finished ? this.sink.closed : true;
            boolean open = isOpen();
        }
        if (cancel) {
            close(ErrorCode.CANCEL);
        } else if (!open) {
            this.connection.removeStream(this.id);
        }
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
