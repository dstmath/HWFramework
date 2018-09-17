package com.android.okhttp.internal.framed;

import com.android.okhttp.Protocol;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.NamedRunnable;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.framed.FrameReader.Handler;
import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSink;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.ByteString;
import com.android.okhttp.okio.Okio;
import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class FramedConnection implements Closeable {
    static final /* synthetic */ boolean -assertionsDisabled = (FramedConnection.class.desiredAssertionStatus() ^ 1);
    private static final int OKHTTP_CLIENT_WINDOW_SIZE = 16777216;
    private static final ExecutorService executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp FramedConnection", true));
    long bytesLeftInWriteWindow;
    final boolean client;
    private final Set<Integer> currentPushRequests;
    final FrameWriter frameWriter;
    private final String hostName;
    private long idleStartTimeNs;
    private int lastGoodStreamId;
    private final Listener listener;
    private int nextPingId;
    private int nextStreamId;
    Settings okHttpSettings;
    final Settings peerSettings;
    private Map<Integer, Ping> pings;
    final Protocol protocol;
    private final ExecutorService pushExecutor;
    private final PushObserver pushObserver;
    final Reader readerRunnable;
    private boolean receivedInitialPeerSettings;
    private boolean shutdown;
    final Socket socket;
    private final Map<Integer, FramedStream> streams;
    long unacknowledgedBytesRead;
    final Variant variant;

    public static class Builder {
        private boolean client;
        private String hostName;
        private Listener listener = Listener.REFUSE_INCOMING_STREAMS;
        private Protocol protocol = Protocol.SPDY_3;
        private PushObserver pushObserver = PushObserver.CANCEL;
        private BufferedSink sink;
        private Socket socket;
        private BufferedSource source;

        public Builder(boolean client) throws IOException {
            this.client = client;
        }

        public Builder socket(Socket socket) throws IOException {
            return socket(socket, ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostName(), Okio.buffer(Okio.source(socket)), Okio.buffer(Okio.sink(socket)));
        }

        public Builder socket(Socket socket, String hostName, BufferedSource source, BufferedSink sink) {
            this.socket = socket;
            this.hostName = hostName;
            this.source = source;
            this.sink = sink;
            return this;
        }

        public Builder listener(Listener listener) {
            this.listener = listener;
            return this;
        }

        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder pushObserver(PushObserver pushObserver) {
            this.pushObserver = pushObserver;
            return this;
        }

        public FramedConnection build() throws IOException {
            return new FramedConnection(this, null);
        }
    }

    public static abstract class Listener {
        public static final Listener REFUSE_INCOMING_STREAMS = new Listener() {
            public void onStream(FramedStream stream) throws IOException {
                stream.close(ErrorCode.REFUSED_STREAM);
            }
        };

        public abstract void onStream(FramedStream framedStream) throws IOException;

        public void onSettings(FramedConnection connection) {
        }
    }

    class Reader extends NamedRunnable implements Handler {
        final FrameReader frameReader;

        /* synthetic */ Reader(FramedConnection this$0, FrameReader frameReader, Reader -this2) {
            this(frameReader);
        }

        private Reader(FrameReader frameReader) {
            super("OkHttp %s", this$0.hostName);
            this.frameReader = frameReader;
        }

        protected void execute() {
            ErrorCode connectionErrorCode = ErrorCode.INTERNAL_ERROR;
            ErrorCode streamErrorCode = ErrorCode.INTERNAL_ERROR;
            try {
                if (!FramedConnection.this.client) {
                    this.frameReader.readConnectionPreface();
                }
                while (true) {
                    if (!this.frameReader.nextFrame(this)) {
                        break;
                    }
                }
                connectionErrorCode = ErrorCode.NO_ERROR;
                streamErrorCode = ErrorCode.CANCEL;
            } catch (IOException e) {
                connectionErrorCode = ErrorCode.PROTOCOL_ERROR;
                streamErrorCode = ErrorCode.PROTOCOL_ERROR;
            } finally {
                try {
                    FramedConnection.this.close(connectionErrorCode, streamErrorCode);
                } catch (IOException e2) {
                }
                Util.closeQuietly(this.frameReader);
            }
        }

        public void data(boolean inFinished, int streamId, BufferedSource source, int length) throws IOException {
            if (FramedConnection.this.pushedStream(streamId)) {
                FramedConnection.this.pushDataLater(streamId, source, length, inFinished);
                return;
            }
            FramedStream dataStream = FramedConnection.this.getStream(streamId);
            if (dataStream == null) {
                FramedConnection.this.writeSynResetLater(streamId, ErrorCode.INVALID_STREAM);
                source.skip((long) length);
                return;
            }
            dataStream.receiveData(source, length);
            if (inFinished) {
                dataStream.receiveFin();
            }
        }

        /* JADX WARNING: Missing block: B:35:0x0090, code:
            if (r15.failIfStreamPresent() == false) goto L_0x00a0;
     */
        /* JADX WARNING: Missing block: B:36:0x0092, code:
            r6.closeLater(com.android.okhttp.internal.framed.ErrorCode.PROTOCOL_ERROR);
            r9.this$0.removeStream(r12);
     */
        /* JADX WARNING: Missing block: B:37:0x009c, code:
            return;
     */
        /* JADX WARNING: Missing block: B:41:0x00a0, code:
            r6.receiveHeaders(r14, r15);
     */
        /* JADX WARNING: Missing block: B:42:0x00a3, code:
            if (r11 == false) goto L_0x00a8;
     */
        /* JADX WARNING: Missing block: B:43:0x00a5, code:
            r6.receiveFin();
     */
        /* JADX WARNING: Missing block: B:44:0x00a8, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void headers(boolean outFinished, boolean inFinished, int streamId, int associatedStreamId, List<Header> headerBlock, HeadersMode headersMode) {
            if (FramedConnection.this.pushedStream(streamId)) {
                FramedConnection.this.pushHeadersLater(streamId, headerBlock, inFinished);
                return;
            }
            synchronized (FramedConnection.this) {
                if (FramedConnection.this.shutdown) {
                    return;
                }
                FramedStream stream = FramedConnection.this.getStream(streamId);
                if (stream == null) {
                    if (headersMode.failIfStreamAbsent()) {
                        FramedConnection.this.writeSynResetLater(streamId, ErrorCode.INVALID_STREAM);
                    } else if (streamId <= FramedConnection.this.lastGoodStreamId) {
                    } else if (streamId % 2 == FramedConnection.this.nextStreamId % 2) {
                    } else {
                        final FramedStream newStream = new FramedStream(streamId, FramedConnection.this, outFinished, inFinished, headerBlock);
                        FramedConnection.this.lastGoodStreamId = streamId;
                        FramedConnection.this.streams.put(Integer.valueOf(streamId), newStream);
                        FramedConnection.executor.execute(new NamedRunnable("OkHttp %s stream %d", new Object[]{FramedConnection.this.hostName, Integer.valueOf(streamId)}) {
                            public void execute() {
                                try {
                                    FramedConnection.this.listener.onStream(newStream);
                                } catch (IOException e) {
                                    Internal.logger.log(Level.INFO, "FramedConnection.Listener failure for " + FramedConnection.this.hostName, e);
                                    try {
                                        newStream.close(ErrorCode.PROTOCOL_ERROR);
                                    } catch (IOException e2) {
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }

        public void rstStream(int streamId, ErrorCode errorCode) {
            if (FramedConnection.this.pushedStream(streamId)) {
                FramedConnection.this.pushResetLater(streamId, errorCode);
                return;
            }
            FramedStream rstStream = FramedConnection.this.removeStream(streamId);
            if (rstStream != null) {
                rstStream.receiveRstStream(errorCode);
            }
        }

        public void settings(boolean clearPrevious, Settings newSettings) {
            long delta = 0;
            FramedStream[] streamsToNotify = null;
            synchronized (FramedConnection.this) {
                int priorWriteWindowSize = FramedConnection.this.peerSettings.getInitialWindowSize(65536);
                if (clearPrevious) {
                    FramedConnection.this.peerSettings.clear();
                }
                FramedConnection.this.peerSettings.merge(newSettings);
                if (FramedConnection.this.getProtocol() == Protocol.HTTP_2) {
                    ackSettingsLater(newSettings);
                }
                int peerInitialWindowSize = FramedConnection.this.peerSettings.getInitialWindowSize(65536);
                if (!(peerInitialWindowSize == -1 || peerInitialWindowSize == priorWriteWindowSize)) {
                    delta = (long) (peerInitialWindowSize - priorWriteWindowSize);
                    if (!FramedConnection.this.receivedInitialPeerSettings) {
                        FramedConnection.this.addBytesToWriteWindow(delta);
                        FramedConnection.this.receivedInitialPeerSettings = true;
                    }
                    if (!FramedConnection.this.streams.isEmpty()) {
                        streamsToNotify = (FramedStream[]) FramedConnection.this.streams.values().toArray(new FramedStream[FramedConnection.this.streams.size()]);
                    }
                }
                FramedConnection.executor.execute(new NamedRunnable("OkHttp %s settings", FramedConnection.this.hostName) {
                    public void execute() {
                        FramedConnection.this.listener.onSettings(FramedConnection.this);
                    }
                });
            }
            if (streamsToNotify != null && delta != 0) {
                for (FramedStream stream : streamsToNotify) {
                    synchronized (stream) {
                        stream.addBytesToWriteWindow(delta);
                    }
                }
            }
        }

        private void ackSettingsLater(final Settings peerSettings) {
            FramedConnection.executor.execute(new NamedRunnable("OkHttp %s ACK Settings", new Object[]{FramedConnection.this.hostName}) {
                public void execute() {
                    try {
                        FramedConnection.this.frameWriter.ackSettings(peerSettings);
                    } catch (IOException e) {
                    }
                }
            });
        }

        public void ackSettings() {
        }

        public void ping(boolean reply, int payload1, int payload2) {
            if (reply) {
                Ping ping = FramedConnection.this.removePing(payload1);
                if (ping != null) {
                    ping.receive();
                    return;
                }
                return;
            }
            FramedConnection.this.writePingLater(true, payload1, payload2, null);
        }

        public void goAway(int lastGoodStreamId, ErrorCode errorCode, ByteString debugData) {
            int size = debugData.size();
            synchronized (FramedConnection.this) {
                FramedStream[] streamsCopy = (FramedStream[]) FramedConnection.this.streams.values().toArray(new FramedStream[FramedConnection.this.streams.size()]);
                FramedConnection.this.shutdown = true;
            }
            for (FramedStream framedStream : streamsCopy) {
                if (framedStream.getId() > lastGoodStreamId && framedStream.isLocallyInitiated()) {
                    framedStream.receiveRstStream(ErrorCode.REFUSED_STREAM);
                    FramedConnection.this.removeStream(framedStream.getId());
                }
            }
        }

        public void windowUpdate(int streamId, long windowSizeIncrement) {
            if (streamId == 0) {
                synchronized (FramedConnection.this) {
                    FramedConnection framedConnection = FramedConnection.this;
                    framedConnection.bytesLeftInWriteWindow += windowSizeIncrement;
                    FramedConnection.this.notifyAll();
                }
                return;
            }
            FramedStream stream = FramedConnection.this.getStream(streamId);
            if (stream != null) {
                synchronized (stream) {
                    stream.addBytesToWriteWindow(windowSizeIncrement);
                }
            }
        }

        public void priority(int streamId, int streamDependency, int weight, boolean exclusive) {
        }

        public void pushPromise(int streamId, int promisedStreamId, List<Header> requestHeaders) {
            FramedConnection.this.pushRequestLater(promisedStreamId, requestHeaders);
        }

        public void alternateService(int streamId, String origin, ByteString protocol, String host, int port, long maxAge) {
        }
    }

    /* synthetic */ FramedConnection(Builder builder, FramedConnection -this1) {
        this(builder);
    }

    private FramedConnection(Builder builder) throws IOException {
        int i = 2;
        this.streams = new HashMap();
        this.idleStartTimeNs = System.nanoTime();
        this.unacknowledgedBytesRead = 0;
        this.okHttpSettings = new Settings();
        this.peerSettings = new Settings();
        this.receivedInitialPeerSettings = -assertionsDisabled;
        this.currentPushRequests = new LinkedHashSet();
        this.protocol = builder.protocol;
        this.pushObserver = builder.pushObserver;
        this.client = builder.client;
        this.listener = builder.listener;
        this.nextStreamId = builder.client ? 1 : 2;
        if (builder.client && this.protocol == Protocol.HTTP_2) {
            this.nextStreamId += 2;
        }
        if (builder.client) {
            i = 1;
        }
        this.nextPingId = i;
        if (builder.client) {
            this.okHttpSettings.set(7, 0, OKHTTP_CLIENT_WINDOW_SIZE);
        }
        this.hostName = builder.hostName;
        if (this.protocol == Protocol.HTTP_2) {
            this.variant = new Http2();
            this.pushExecutor = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), Util.threadFactory(String.format("OkHttp %s Push Observer", new Object[]{this.hostName}), true));
            this.peerSettings.set(7, 0, 65535);
            this.peerSettings.set(5, 0, 16384);
        } else if (this.protocol == Protocol.SPDY_3) {
            this.variant = new Spdy3();
            this.pushExecutor = null;
        } else {
            throw new AssertionError(this.protocol);
        }
        this.bytesLeftInWriteWindow = (long) this.peerSettings.getInitialWindowSize(65536);
        this.socket = builder.socket;
        this.frameWriter = this.variant.newWriter(builder.sink, this.client);
        this.readerRunnable = new Reader(this, this.variant.newReader(builder.source, this.client), null);
        new Thread(this.readerRunnable).start();
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public synchronized int openStreamCount() {
        return this.streams.size();
    }

    synchronized FramedStream getStream(int id) {
        return (FramedStream) this.streams.get(Integer.valueOf(id));
    }

    synchronized FramedStream removeStream(int streamId) {
        FramedStream stream;
        stream = (FramedStream) this.streams.remove(Integer.valueOf(streamId));
        if (stream != null && this.streams.isEmpty()) {
            setIdle(true);
        }
        notifyAll();
        return stream;
    }

    private synchronized void setIdle(boolean value) {
        this.idleStartTimeNs = value ? System.nanoTime() : Long.MAX_VALUE;
    }

    public synchronized boolean isIdle() {
        return this.idleStartTimeNs != Long.MAX_VALUE ? true : -assertionsDisabled;
    }

    public synchronized int maxConcurrentStreams() {
        return this.peerSettings.getMaxConcurrentStreams(Integer.MAX_VALUE);
    }

    public synchronized long getIdleStartTimeNs() {
        return this.idleStartTimeNs;
    }

    public FramedStream pushStream(int associatedStreamId, List<Header> requestHeaders, boolean out) throws IOException {
        if (this.client) {
            throw new IllegalStateException("Client cannot push requests.");
        } else if (this.protocol == Protocol.HTTP_2) {
            return newStream(associatedStreamId, requestHeaders, out, -assertionsDisabled);
        } else {
            throw new IllegalStateException("protocol != HTTP_2");
        }
    }

    public FramedStream newStream(List<Header> requestHeaders, boolean out, boolean in) throws IOException {
        return newStream(0, requestHeaders, out, in);
    }

    private FramedStream newStream(int associatedStreamId, List<Header> requestHeaders, boolean out, boolean in) throws IOException {
        FramedStream stream;
        boolean outFinished = out ^ 1;
        boolean inFinished = in ^ 1;
        synchronized (this.frameWriter) {
            int streamId;
            synchronized (this) {
                if (this.shutdown) {
                    throw new IOException("shutdown");
                }
                streamId = this.nextStreamId;
                this.nextStreamId += 2;
                stream = new FramedStream(streamId, this, outFinished, inFinished, requestHeaders);
                if (stream.isOpen()) {
                    this.streams.put(Integer.valueOf(streamId), stream);
                    setIdle(-assertionsDisabled);
                }
            }
            if (associatedStreamId == 0) {
                this.frameWriter.synStream(outFinished, inFinished, streamId, associatedStreamId, requestHeaders);
            } else if (this.client) {
                throw new IllegalArgumentException("client streams shouldn't have associated stream IDs");
            } else {
                this.frameWriter.pushPromise(associatedStreamId, streamId, requestHeaders);
            }
        }
        if (!out) {
            this.frameWriter.flush();
        }
        return stream;
    }

    void writeSynReply(int streamId, boolean outFinished, List<Header> alternating) throws IOException {
        this.frameWriter.synReply(outFinished, streamId, alternating);
    }

    public void writeData(int streamId, boolean outFinished, Buffer buffer, long byteCount) throws IOException {
        if (byteCount == 0) {
            this.frameWriter.data(outFinished, streamId, buffer, 0);
            return;
        }
        while (byteCount > 0) {
            int toWrite;
            synchronized (this) {
                while (this.bytesLeftInWriteWindow <= 0) {
                    try {
                        if (this.streams.containsKey(Integer.valueOf(streamId))) {
                            wait();
                        } else {
                            throw new IOException("stream closed");
                        }
                    } catch (InterruptedException e) {
                        throw new InterruptedIOException();
                    }
                }
                toWrite = Math.min((int) Math.min(byteCount, this.bytesLeftInWriteWindow), this.frameWriter.maxDataLength());
                this.bytesLeftInWriteWindow -= (long) toWrite;
            }
            byteCount -= (long) toWrite;
            FrameWriter frameWriter = this.frameWriter;
            boolean z = (outFinished && byteCount == 0) ? true : -assertionsDisabled;
            frameWriter.data(z, streamId, buffer, toWrite);
        }
    }

    void addBytesToWriteWindow(long delta) {
        this.bytesLeftInWriteWindow += delta;
        if (delta > 0) {
            notifyAll();
        }
    }

    void writeSynResetLater(int streamId, ErrorCode errorCode) {
        final int i = streamId;
        final ErrorCode errorCode2 = errorCode;
        executor.submit(new NamedRunnable("OkHttp %s stream %d", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            public void execute() {
                try {
                    FramedConnection.this.writeSynReset(i, errorCode2);
                } catch (IOException e) {
                }
            }
        });
    }

    void writeSynReset(int streamId, ErrorCode statusCode) throws IOException {
        this.frameWriter.rstStream(streamId, statusCode);
    }

    void writeWindowUpdateLater(int streamId, long unacknowledgedBytesRead) {
        final int i = streamId;
        final long j = unacknowledgedBytesRead;
        executor.execute(new NamedRunnable("OkHttp Window Update %s stream %d", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            public void execute() {
                try {
                    FramedConnection.this.frameWriter.windowUpdate(i, j);
                } catch (IOException e) {
                }
            }
        });
    }

    public Ping ping() throws IOException {
        int pingId;
        Ping ping = new Ping();
        synchronized (this) {
            if (this.shutdown) {
                throw new IOException("shutdown");
            }
            pingId = this.nextPingId;
            this.nextPingId += 2;
            if (this.pings == null) {
                this.pings = new HashMap();
            }
            this.pings.put(Integer.valueOf(pingId), ping);
        }
        writePing(-assertionsDisabled, pingId, 1330343787, ping);
        return ping;
    }

    private void writePingLater(boolean reply, int payload1, int payload2, Ping ping) {
        final boolean z = reply;
        final int i = payload1;
        final int i2 = payload2;
        final Ping ping2 = ping;
        executor.execute(new NamedRunnable("OkHttp %s ping %08x%08x", new Object[]{this.hostName, Integer.valueOf(payload1), Integer.valueOf(payload2)}) {
            public void execute() {
                try {
                    FramedConnection.this.writePing(z, i, i2, ping2);
                } catch (IOException e) {
                }
            }
        });
    }

    private void writePing(boolean reply, int payload1, int payload2, Ping ping) throws IOException {
        synchronized (this.frameWriter) {
            if (ping != null) {
                ping.send();
            }
            this.frameWriter.ping(reply, payload1, payload2);
        }
    }

    private synchronized Ping removePing(int id) {
        Ping ping = null;
        synchronized (this) {
            if (this.pings != null) {
                ping = (Ping) this.pings.remove(Integer.valueOf(id));
            }
        }
        return ping;
    }

    public void flush() throws IOException {
        this.frameWriter.flush();
    }

    public void shutdown(ErrorCode statusCode) throws IOException {
        synchronized (this.frameWriter) {
            synchronized (this) {
                if (this.shutdown) {
                    return;
                }
                this.shutdown = true;
                int lastGoodStreamId = this.lastGoodStreamId;
                this.frameWriter.goAway(lastGoodStreamId, statusCode, Util.EMPTY_BYTE_ARRAY);
            }
        }
    }

    public void close() throws IOException {
        close(ErrorCode.NO_ERROR, ErrorCode.CANCEL);
    }

    private void close(ErrorCode connectionCode, ErrorCode streamCode) throws IOException {
        if (-assertionsDisabled || !Thread.holdsLock(this)) {
            IOException thrown = null;
            try {
                shutdown(connectionCode);
            } catch (IOException e) {
                thrown = e;
            }
            FramedStream[] streamsToClose = null;
            Ping[] pingsToCancel = null;
            synchronized (this) {
                if (!this.streams.isEmpty()) {
                    streamsToClose = (FramedStream[]) this.streams.values().toArray(new FramedStream[this.streams.size()]);
                    this.streams.clear();
                    setIdle(-assertionsDisabled);
                }
                if (this.pings != null) {
                    pingsToCancel = (Ping[]) this.pings.values().toArray(new Ping[this.pings.size()]);
                    this.pings = null;
                }
            }
            if (streamsToClose != null) {
                for (FramedStream stream : streamsToClose) {
                    try {
                        stream.close(streamCode);
                    } catch (IOException e2) {
                        if (thrown != null) {
                            thrown = e2;
                        }
                    }
                }
            }
            if (pingsToCancel != null) {
                for (Ping ping : pingsToCancel) {
                    ping.cancel();
                }
            }
            try {
                this.frameWriter.close();
            } catch (IOException e22) {
                if (thrown == null) {
                    thrown = e22;
                }
            }
            try {
                this.socket.close();
            } catch (IOException e222) {
                thrown = e222;
            }
            if (thrown != null) {
                throw thrown;
            }
            return;
        }
        throw new AssertionError();
    }

    public void sendConnectionPreface() throws IOException {
        this.frameWriter.connectionPreface();
        this.frameWriter.settings(this.okHttpSettings);
        int windowSize = this.okHttpSettings.getInitialWindowSize(65536);
        if (windowSize != 65536) {
            this.frameWriter.windowUpdate(0, (long) (windowSize - 65536));
        }
    }

    public void setSettings(Settings settings) throws IOException {
        synchronized (this.frameWriter) {
            synchronized (this) {
                if (this.shutdown) {
                    throw new IOException("shutdown");
                }
                this.okHttpSettings.merge(settings);
                this.frameWriter.settings(settings);
            }
        }
    }

    private boolean pushedStream(int streamId) {
        return (this.protocol == Protocol.HTTP_2 && streamId != 0 && (streamId & 1) == 0) ? true : -assertionsDisabled;
    }

    private void pushRequestLater(int streamId, List<Header> requestHeaders) {
        synchronized (this) {
            if (this.currentPushRequests.contains(Integer.valueOf(streamId))) {
                writeSynResetLater(streamId, ErrorCode.PROTOCOL_ERROR);
                return;
            }
            this.currentPushRequests.add(Integer.valueOf(streamId));
            final int i = streamId;
            final List<Header> list = requestHeaders;
            this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Request[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
                public void execute() {
                    if (FramedConnection.this.pushObserver.onRequest(i, list)) {
                        try {
                            FramedConnection.this.frameWriter.rstStream(i, ErrorCode.CANCEL);
                            synchronized (FramedConnection.this) {
                                FramedConnection.this.currentPushRequests.remove(Integer.valueOf(i));
                            }
                        } catch (IOException e) {
                        }
                    }
                }
            });
        }
    }

    private void pushHeadersLater(int streamId, List<Header> requestHeaders, boolean inFinished) {
        final int i = streamId;
        final List<Header> list = requestHeaders;
        final boolean z = inFinished;
        this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Headers[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            public void execute() {
                boolean cancel = FramedConnection.this.pushObserver.onHeaders(i, list, z);
                if (cancel) {
                    try {
                        FramedConnection.this.frameWriter.rstStream(i, ErrorCode.CANCEL);
                    } catch (IOException e) {
                        return;
                    }
                }
                if (cancel || z) {
                    synchronized (FramedConnection.this) {
                        FramedConnection.this.currentPushRequests.remove(Integer.valueOf(i));
                    }
                }
            }
        });
    }

    private void pushDataLater(int streamId, BufferedSource source, int byteCount, boolean inFinished) throws IOException {
        final Buffer buffer = new Buffer();
        source.require((long) byteCount);
        source.read(buffer, (long) byteCount);
        if (buffer.size() != ((long) byteCount)) {
            throw new IOException(buffer.size() + " != " + byteCount);
        }
        final int i = streamId;
        final int i2 = byteCount;
        final boolean z = inFinished;
        this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Data[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            public void execute() {
                try {
                    boolean cancel = FramedConnection.this.pushObserver.onData(i, buffer, i2, z);
                    if (cancel) {
                        FramedConnection.this.frameWriter.rstStream(i, ErrorCode.CANCEL);
                    }
                    if (cancel || z) {
                        synchronized (FramedConnection.this) {
                            FramedConnection.this.currentPushRequests.remove(Integer.valueOf(i));
                        }
                    }
                } catch (IOException e) {
                }
            }
        });
    }

    private void pushResetLater(int streamId, ErrorCode errorCode) {
        final int i = streamId;
        final ErrorCode errorCode2 = errorCode;
        this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Reset[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            public void execute() {
                FramedConnection.this.pushObserver.onReset(i, errorCode2);
                synchronized (FramedConnection.this) {
                    FramedConnection.this.currentPushRequests.remove(Integer.valueOf(i));
                }
            }
        });
    }
}
