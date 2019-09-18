package com.android.okhttp.internal.framed;

import com.android.okhttp.Protocol;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.NamedRunnable;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.framed.FrameReader;
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
import java.util.logging.Logger;

public final class FramedConnection implements Closeable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int OKHTTP_CLIENT_WINDOW_SIZE = 16777216;
    /* access modifiers changed from: private */
    public static final ExecutorService executor;
    long bytesLeftInWriteWindow;
    final boolean client;
    /* access modifiers changed from: private */
    public final Set<Integer> currentPushRequests;
    final FrameWriter frameWriter;
    /* access modifiers changed from: private */
    public final String hostName;
    private long idleStartTimeNs;
    /* access modifiers changed from: private */
    public int lastGoodStreamId;
    /* access modifiers changed from: private */
    public final Listener listener;
    private int nextPingId;
    /* access modifiers changed from: private */
    public int nextStreamId;
    Settings okHttpSettings;
    final Settings peerSettings;
    private Map<Integer, Ping> pings;
    final Protocol protocol;
    private final ExecutorService pushExecutor;
    /* access modifiers changed from: private */
    public final PushObserver pushObserver;
    final Reader readerRunnable;
    /* access modifiers changed from: private */
    public boolean receivedInitialPeerSettings;
    /* access modifiers changed from: private */
    public boolean shutdown;
    final Socket socket;
    /* access modifiers changed from: private */
    public final Map<Integer, FramedStream> streams;
    long unacknowledgedBytesRead;
    final Variant variant;

    public static class Builder {
        /* access modifiers changed from: private */
        public boolean client;
        /* access modifiers changed from: private */
        public String hostName;
        /* access modifiers changed from: private */
        public Listener listener = Listener.REFUSE_INCOMING_STREAMS;
        /* access modifiers changed from: private */
        public Protocol protocol = Protocol.SPDY_3;
        /* access modifiers changed from: private */
        public PushObserver pushObserver = PushObserver.CANCEL;
        /* access modifiers changed from: private */
        public BufferedSink sink;
        /* access modifiers changed from: private */
        public Socket socket;
        /* access modifiers changed from: private */
        public BufferedSource source;

        public Builder(boolean client2) throws IOException {
            this.client = client2;
        }

        public Builder socket(Socket socket2) throws IOException {
            return socket(socket2, ((InetSocketAddress) socket2.getRemoteSocketAddress()).getHostName(), Okio.buffer(Okio.source(socket2)), Okio.buffer(Okio.sink(socket2)));
        }

        public Builder socket(Socket socket2, String hostName2, BufferedSource source2, BufferedSink sink2) {
            this.socket = socket2;
            this.hostName = hostName2;
            this.source = source2;
            this.sink = sink2;
            return this;
        }

        public Builder listener(Listener listener2) {
            this.listener = listener2;
            return this;
        }

        public Builder protocol(Protocol protocol2) {
            this.protocol = protocol2;
            return this;
        }

        public Builder pushObserver(PushObserver pushObserver2) {
            this.pushObserver = pushObserver2;
            return this;
        }

        public FramedConnection build() throws IOException {
            return new FramedConnection(this);
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

    class Reader extends NamedRunnable implements FrameReader.Handler {
        final FrameReader frameReader;

        private Reader(FrameReader frameReader2) {
            super("OkHttp %s", FramedConnection.this.hostName);
            this.frameReader = frameReader2;
        }

        /* access modifiers changed from: protected */
        public void execute() {
            ErrorCode connectionErrorCode = ErrorCode.INTERNAL_ERROR;
            ErrorCode streamErrorCode = ErrorCode.INTERNAL_ERROR;
            try {
                if (!FramedConnection.this.client) {
                    this.frameReader.readConnectionPreface();
                }
                while (this.frameReader.nextFrame(this)) {
                }
                try {
                    FramedConnection.this.close(ErrorCode.NO_ERROR, ErrorCode.CANCEL);
                } catch (IOException e) {
                }
            } catch (IOException e2) {
                connectionErrorCode = ErrorCode.PROTOCOL_ERROR;
                try {
                    FramedConnection.this.close(connectionErrorCode, ErrorCode.PROTOCOL_ERROR);
                } catch (IOException e3) {
                }
            } catch (Throwable th) {
                try {
                    FramedConnection.this.close(connectionErrorCode, streamErrorCode);
                } catch (IOException e4) {
                }
                Util.closeQuietly((Closeable) this.frameReader);
                throw th;
            }
            Util.closeQuietly((Closeable) this.frameReader);
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

        /* JADX INFO: finally extract failed */
        /* JADX WARNING: Code restructure failed: missing block: B:31:0x0097, code lost:
            if (r20.failIfStreamPresent() == false) goto L_0x00a4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0099, code lost:
            r0.closeLater(com.android.okhttp.internal.framed.ErrorCode.PROTOCOL_ERROR);
            r1.this$0.removeStream(r9);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a3, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a4, code lost:
            r0.receiveHeaders(r10, r20);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a9, code lost:
            if (r8 == false) goto L_0x00ae;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ab, code lost:
            r0.receiveFin();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x00ae, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x00b4, code lost:
            r0 = th;
         */
        public void headers(boolean outFinished, boolean inFinished, int streamId, int associatedStreamId, List<Header> headerBlock, HeadersMode headersMode) {
            boolean z = inFinished;
            int i = streamId;
            List<Header> list = headerBlock;
            if (FramedConnection.this.pushedStream(i)) {
                FramedConnection.this.pushHeadersLater(i, list, z);
                return;
            }
            synchronized (FramedConnection.this) {
                try {
                    if (!FramedConnection.this.shutdown) {
                        FramedStream stream = FramedConnection.this.getStream(i);
                        if (stream == null) {
                            if (headersMode.failIfStreamAbsent()) {
                                FramedConnection.this.writeSynResetLater(i, ErrorCode.INVALID_STREAM);
                            } else if (i > FramedConnection.this.lastGoodStreamId) {
                                if (i % 2 != FramedConnection.this.nextStreamId % 2) {
                                    FramedStream framedStream = new FramedStream(i, FramedConnection.this, outFinished, z, list);
                                    final FramedStream newStream = framedStream;
                                    int unused = FramedConnection.this.lastGoodStreamId = i;
                                    FramedConnection.this.streams.put(Integer.valueOf(streamId), newStream);
                                    FramedConnection.executor.execute(new NamedRunnable("OkHttp %s stream %d", new Object[]{FramedConnection.this.hostName, Integer.valueOf(streamId)}) {
                                        public void execute() {
                                            try {
                                                FramedConnection.this.listener.onStream(newStream);
                                            } catch (IOException e) {
                                                Logger logger = Internal.logger;
                                                Level level = Level.INFO;
                                                logger.log(level, "FramedConnection.Listener failure for " + FramedConnection.this.hostName, e);
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
                } catch (Throwable th) {
                    th = th;
                    HeadersMode headersMode2 = headersMode;
                    while (true) {
                        throw th;
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
            int i;
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
                        boolean unused = FramedConnection.this.receivedInitialPeerSettings = true;
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
            FramedStream[] streamsCopy;
            debugData.size();
            synchronized (FramedConnection.this) {
                streamsCopy = (FramedStream[]) FramedConnection.this.streams.values().toArray(new FramedStream[FramedConnection.this.streams.size()]);
                boolean unused = FramedConnection.this.shutdown = true;
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
                    FramedConnection.this.bytesLeftInWriteWindow += windowSizeIncrement;
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

    static {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp FramedConnection", true));
        executor = threadPoolExecutor;
    }

    private FramedConnection(Builder builder) throws IOException {
        this.streams = new HashMap();
        this.idleStartTimeNs = System.nanoTime();
        this.unacknowledgedBytesRead = 0;
        this.okHttpSettings = new Settings();
        this.peerSettings = new Settings();
        this.receivedInitialPeerSettings = $assertionsDisabled;
        this.currentPushRequests = new LinkedHashSet();
        this.protocol = builder.protocol;
        this.pushObserver = builder.pushObserver;
        this.client = builder.client;
        this.listener = builder.listener;
        int i = 2;
        this.nextStreamId = builder.client ? 1 : 2;
        if (builder.client && this.protocol == Protocol.HTTP_2) {
            this.nextStreamId += 2;
        }
        this.nextPingId = builder.client ? 1 : i;
        if (builder.client) {
            this.okHttpSettings.set(7, 0, OKHTTP_CLIENT_WINDOW_SIZE);
        }
        this.hostName = builder.hostName;
        if (this.protocol == Protocol.HTTP_2) {
            this.variant = new Http2();
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), Util.threadFactory(String.format("OkHttp %s Push Observer", new Object[]{this.hostName}), true));
            this.pushExecutor = threadPoolExecutor;
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
        this.readerRunnable = new Reader(this.variant.newReader(builder.source, this.client));
        new Thread(this.readerRunnable).start();
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public synchronized int openStreamCount() {
        return this.streams.size();
    }

    /* access modifiers changed from: package-private */
    public synchronized FramedStream getStream(int id) {
        return this.streams.get(Integer.valueOf(id));
    }

    /* access modifiers changed from: package-private */
    public synchronized FramedStream removeStream(int streamId) {
        FramedStream stream;
        stream = this.streams.remove(Integer.valueOf(streamId));
        if (stream != null && this.streams.isEmpty()) {
            setIdle(true);
        }
        notifyAll();
        return stream;
    }

    private synchronized void setIdle(boolean value) {
        long j;
        if (value) {
            try {
                j = System.nanoTime();
            } catch (Throwable th) {
                throw th;
            }
        } else {
            j = Long.MAX_VALUE;
        }
        this.idleStartTimeNs = j;
    }

    public synchronized boolean isIdle() {
        return this.idleStartTimeNs != Long.MAX_VALUE ? true : $assertionsDisabled;
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
            return newStream(associatedStreamId, requestHeaders, out, $assertionsDisabled);
        } else {
            throw new IllegalStateException("protocol != HTTP_2");
        }
    }

    public FramedStream newStream(List<Header> requestHeaders, boolean out, boolean in) throws IOException {
        return newStream(0, requestHeaders, out, in);
    }

    /* JADX WARNING: type inference failed for: r2v8, types: [boolean] */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0037, code lost:
        if (r14 != 0) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0039, code lost:
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003b, code lost:
        r2 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:?, code lost:
        r7.frameWriter.synStream(r4, r5, r0, r14, r18);
        r6 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0048, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0049, code lost:
        r2 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x004a, code lost:
        r6 = r18;
        r2 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004d, code lost:
        r2 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0050, code lost:
        if (r7.client != false) goto L_0x0062;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r7.frameWriter.pushPromise(r14, r0, r18);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0059, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005a, code lost:
        if (r19 != false) goto L_0x0061;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005c, code lost:
        r7.frameWriter.flush();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0061, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0062, code lost:
        r6 = r18;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006b, code lost:
        throw new java.lang.IllegalArgumentException("client streams shouldn't have associated stream IDs");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x006c, code lost:
        r0 = th;
        r2 = r2;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    private FramedStream newStream(int associatedStreamId, List<Header> requestHeaders, boolean out, boolean in) throws IOException {
        FrameWriter frameWriter2;
        FrameWriter frameWriter3;
        int i = associatedStreamId;
        boolean outFinished = !out;
        boolean inFinished = !in;
        FrameWriter frameWriter4 = this.frameWriter;
        synchronized (frameWriter4) {
            try {
                synchronized (this) {
                    try {
                        if (!this.shutdown) {
                            int streamId = this.nextStreamId;
                            this.nextStreamId += 2;
                            FramedStream framedStream = new FramedStream(streamId, this, outFinished, inFinished, requestHeaders);
                            FramedStream stream = framedStream;
                            ? isOpen = stream.isOpen();
                            FrameWriter frameWriter5 = isOpen;
                            if (isOpen != 0) {
                                this.streams.put(Integer.valueOf(streamId), stream);
                                frameWriter5 = null;
                                setIdle($assertionsDisabled);
                            }
                        } else {
                            List<Header> list = requestHeaders;
                            frameWriter2 = frameWriter4;
                            throw new IOException("shutdown");
                        }
                    } catch (Throwable th) {
                        th = th;
                        frameWriter2 = frameWriter2;
                        throw th;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                frameWriter3 = frameWriter2;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeSynReply(int streamId, boolean outFinished, List<Header> alternating) throws IOException {
        this.frameWriter.synReply(outFinished, streamId, alternating);
    }

    public void writeData(int streamId, boolean outFinished, Buffer buffer, long byteCount) throws IOException {
        int toWrite;
        if (byteCount == 0) {
            this.frameWriter.data(outFinished, streamId, buffer, 0);
            return;
        }
        while (byteCount > 0) {
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
            this.frameWriter.data(outFinished && byteCount == 0, streamId, buffer, toWrite);
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
    public void writeSynResetLater(int streamId, ErrorCode errorCode) {
        ExecutorService executorService = executor;
        final int i = streamId;
        final ErrorCode errorCode2 = errorCode;
        AnonymousClass1 r1 = new NamedRunnable("OkHttp %s stream %d", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            public void execute() {
                try {
                    FramedConnection.this.writeSynReset(i, errorCode2);
                } catch (IOException e) {
                }
            }
        };
        executorService.submit(r1);
    }

    /* access modifiers changed from: package-private */
    public void writeSynReset(int streamId, ErrorCode statusCode) throws IOException {
        this.frameWriter.rstStream(streamId, statusCode);
    }

    /* access modifiers changed from: package-private */
    public void writeWindowUpdateLater(int streamId, long unacknowledgedBytesRead2) {
        ExecutorService executorService = executor;
        final int i = streamId;
        final long j = unacknowledgedBytesRead2;
        AnonymousClass2 r1 = new NamedRunnable("OkHttp Window Update %s stream %d", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            public void execute() {
                try {
                    FramedConnection.this.frameWriter.windowUpdate(i, j);
                } catch (IOException e) {
                }
            }
        };
        executorService.execute(r1);
    }

    public Ping ping() throws IOException {
        int pingId;
        Ping ping = new Ping();
        synchronized (this) {
            if (!this.shutdown) {
                pingId = this.nextPingId;
                this.nextPingId += 2;
                if (this.pings == null) {
                    this.pings = new HashMap();
                }
                this.pings.put(Integer.valueOf(pingId), ping);
            } else {
                throw new IOException("shutdown");
            }
        }
        writePing($assertionsDisabled, pingId, 1330343787, ping);
        return ping;
    }

    /* access modifiers changed from: private */
    public void writePingLater(boolean reply, int payload1, int payload2, Ping ping) {
        ExecutorService executorService = executor;
        final boolean z = reply;
        final int i = payload1;
        final int i2 = payload2;
        final Ping ping2 = ping;
        AnonymousClass3 r1 = new NamedRunnable("OkHttp %s ping %08x%08x", new Object[]{this.hostName, Integer.valueOf(payload1), Integer.valueOf(payload2)}) {
            public void execute() {
                try {
                    FramedConnection.this.writePing(z, i, i2, ping2);
                } catch (IOException e) {
                }
            }
        };
        executorService.execute(r1);
    }

    /* access modifiers changed from: private */
    public void writePing(boolean reply, int payload1, int payload2, Ping ping) throws IOException {
        synchronized (this.frameWriter) {
            if (ping != null) {
                try {
                    ping.send();
                } catch (Throwable th) {
                    throw th;
                }
            }
            this.frameWriter.ping(reply, payload1, payload2);
        }
    }

    /* access modifiers changed from: private */
    public synchronized Ping removePing(int id) {
        return this.pings != null ? this.pings.remove(Integer.valueOf(id)) : null;
    }

    public void flush() throws IOException {
        this.frameWriter.flush();
    }

    public void shutdown(ErrorCode statusCode) throws IOException {
        synchronized (this.frameWriter) {
            synchronized (this) {
                if (!this.shutdown) {
                    this.shutdown = true;
                    int lastGoodStreamId2 = this.lastGoodStreamId;
                    this.frameWriter.goAway(lastGoodStreamId2, statusCode, Util.EMPTY_BYTE_ARRAY);
                }
            }
        }
    }

    public void close() throws IOException {
        close(ErrorCode.NO_ERROR, ErrorCode.CANCEL);
    }

    /* access modifiers changed from: private */
    public void close(ErrorCode connectionCode, ErrorCode streamCode) throws IOException {
        int i;
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
                setIdle($assertionsDisabled);
            }
            if (this.pings != null) {
                pingsToCancel = (Ping[]) this.pings.values().toArray(new Ping[this.pings.size()]);
                this.pings = null;
            }
        }
        if (streamsToClose != null) {
            IOException thrown2 = thrown;
            for (FramedStream stream : streamsToClose) {
                try {
                    stream.close(streamCode);
                } catch (IOException e2) {
                    if (thrown2 != null) {
                        thrown2 = e2;
                    }
                }
            }
            thrown = thrown2;
        }
        if (pingsToCancel != null) {
            for (Ping ping : pingsToCancel) {
                ping.cancel();
            }
        }
        try {
            this.frameWriter.close();
        } catch (IOException e3) {
            if (thrown == null) {
                thrown = e3;
            }
        }
        try {
            this.socket.close();
        } catch (IOException e4) {
            thrown = e4;
        }
        if (thrown != null) {
            throw thrown;
        }
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
                if (!this.shutdown) {
                    this.okHttpSettings.merge(settings);
                    this.frameWriter.settings(settings);
                } else {
                    throw new IOException("shutdown");
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean pushedStream(int streamId) {
        if (this.protocol == Protocol.HTTP_2 && streamId != 0 && (streamId & 1) == 0) {
            return true;
        }
        return $assertionsDisabled;
    }

    /* access modifiers changed from: private */
    public void pushRequestLater(int streamId, List<Header> requestHeaders) {
        synchronized (this) {
            if (this.currentPushRequests.contains(Integer.valueOf(streamId))) {
                writeSynResetLater(streamId, ErrorCode.PROTOCOL_ERROR);
                return;
            }
            this.currentPushRequests.add(Integer.valueOf(streamId));
            ExecutorService executorService = this.pushExecutor;
            final int i = streamId;
            final List<Header> list = requestHeaders;
            AnonymousClass4 r1 = new NamedRunnable("OkHttp %s Push Request[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
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
            };
            executorService.execute(r1);
        }
    }

    /* access modifiers changed from: private */
    public void pushHeadersLater(int streamId, List<Header> requestHeaders, boolean inFinished) {
        ExecutorService executorService = this.pushExecutor;
        final int i = streamId;
        final List<Header> list = requestHeaders;
        final boolean z = inFinished;
        AnonymousClass5 r1 = new NamedRunnable("OkHttp %s Push Headers[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
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
        };
        executorService.execute(r1);
    }

    /* access modifiers changed from: private */
    public void pushDataLater(int streamId, BufferedSource source, int byteCount, boolean inFinished) throws IOException {
        Buffer buffer = new Buffer();
        source.require((long) byteCount);
        source.read(buffer, (long) byteCount);
        if (buffer.size() == ((long) byteCount)) {
            ExecutorService executorService = this.pushExecutor;
            final int i = streamId;
            final Buffer buffer2 = buffer;
            final int i2 = byteCount;
            final boolean z = inFinished;
            AnonymousClass6 r1 = new NamedRunnable("OkHttp %s Push Data[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
                public void execute() {
                    try {
                        boolean cancel = FramedConnection.this.pushObserver.onData(i, buffer2, i2, z);
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
            };
            executorService.execute(r1);
            return;
        }
        throw new IOException(buffer.size() + " != " + byteCount);
    }

    /* access modifiers changed from: private */
    public void pushResetLater(int streamId, ErrorCode errorCode) {
        ExecutorService executorService = this.pushExecutor;
        final int i = streamId;
        final ErrorCode errorCode2 = errorCode;
        AnonymousClass7 r1 = new NamedRunnable("OkHttp %s Push Reset[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}) {
            public void execute() {
                FramedConnection.this.pushObserver.onReset(i, errorCode2);
                synchronized (FramedConnection.this) {
                    FramedConnection.this.currentPushRequests.remove(Integer.valueOf(i));
                }
            }
        };
        executorService.execute(r1);
    }
}
