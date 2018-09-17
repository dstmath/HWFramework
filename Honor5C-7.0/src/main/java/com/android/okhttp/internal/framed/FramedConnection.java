package com.android.okhttp.internal.framed;

import com.android.okhttp.Protocol;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.NamedRunnable;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.framed.FrameReader.Handler;
import com.android.okhttp.okio.Buffer;
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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public final class FramedConnection implements Closeable {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final int OKHTTP_CLIENT_WINDOW_SIZE = 16777216;
    private static final ExecutorService executor = null;
    long bytesLeftInWriteWindow;
    final boolean client;
    private final Set<Integer> currentPushRequests;
    final FrameWriter frameWriter;
    private final IncomingStreamHandler handler;
    private final String hostName;
    private long idleStartTimeNs;
    private int lastGoodStreamId;
    private int nextPingId;
    private int nextStreamId;
    final Settings okHttpSettings;
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

    /* renamed from: com.android.okhttp.internal.framed.FramedConnection.1 */
    class AnonymousClass1 extends NamedRunnable {
        final /* synthetic */ ErrorCode val$errorCode;
        final /* synthetic */ int val$streamId;

        AnonymousClass1(String $anonymous0, Object[] $anonymous1, int val$streamId, ErrorCode val$errorCode) {
            this.val$streamId = val$streamId;
            this.val$errorCode = val$errorCode;
            super($anonymous0, $anonymous1);
        }

        public void execute() {
            try {
                FramedConnection.this.writeSynReset(this.val$streamId, this.val$errorCode);
            } catch (IOException e) {
            }
        }
    }

    /* renamed from: com.android.okhttp.internal.framed.FramedConnection.2 */
    class AnonymousClass2 extends NamedRunnable {
        final /* synthetic */ int val$streamId;
        final /* synthetic */ long val$unacknowledgedBytesRead;

        AnonymousClass2(String $anonymous0, Object[] $anonymous1, int val$streamId, long val$unacknowledgedBytesRead) {
            this.val$streamId = val$streamId;
            this.val$unacknowledgedBytesRead = val$unacknowledgedBytesRead;
            super($anonymous0, $anonymous1);
        }

        public void execute() {
            try {
                FramedConnection.this.frameWriter.windowUpdate(this.val$streamId, this.val$unacknowledgedBytesRead);
            } catch (IOException e) {
            }
        }
    }

    /* renamed from: com.android.okhttp.internal.framed.FramedConnection.3 */
    class AnonymousClass3 extends NamedRunnable {
        final /* synthetic */ int val$payload1;
        final /* synthetic */ int val$payload2;
        final /* synthetic */ Ping val$ping;
        final /* synthetic */ boolean val$reply;

        AnonymousClass3(String $anonymous0, Object[] $anonymous1, boolean val$reply, int val$payload1, int val$payload2, Ping val$ping) {
            this.val$reply = val$reply;
            this.val$payload1 = val$payload1;
            this.val$payload2 = val$payload2;
            this.val$ping = val$ping;
            super($anonymous0, $anonymous1);
        }

        public void execute() {
            try {
                FramedConnection.this.writePing(this.val$reply, this.val$payload1, this.val$payload2, this.val$ping);
            } catch (IOException e) {
            }
        }
    }

    /* renamed from: com.android.okhttp.internal.framed.FramedConnection.4 */
    class AnonymousClass4 extends NamedRunnable {
        final /* synthetic */ List val$requestHeaders;
        final /* synthetic */ int val$streamId;

        AnonymousClass4(String $anonymous0, Object[] $anonymous1, int val$streamId, List val$requestHeaders) {
            this.val$streamId = val$streamId;
            this.val$requestHeaders = val$requestHeaders;
            super($anonymous0, $anonymous1);
        }

        public void execute() {
            if (FramedConnection.this.pushObserver.onRequest(this.val$streamId, this.val$requestHeaders)) {
                try {
                    FramedConnection.this.frameWriter.rstStream(this.val$streamId, ErrorCode.CANCEL);
                    synchronized (FramedConnection.this) {
                        FramedConnection.this.currentPushRequests.remove(Integer.valueOf(this.val$streamId));
                    }
                } catch (IOException e) {
                }
            }
        }
    }

    /* renamed from: com.android.okhttp.internal.framed.FramedConnection.5 */
    class AnonymousClass5 extends NamedRunnable {
        final /* synthetic */ boolean val$inFinished;
        final /* synthetic */ List val$requestHeaders;
        final /* synthetic */ int val$streamId;

        AnonymousClass5(String $anonymous0, Object[] $anonymous1, int val$streamId, List val$requestHeaders, boolean val$inFinished) {
            this.val$streamId = val$streamId;
            this.val$requestHeaders = val$requestHeaders;
            this.val$inFinished = val$inFinished;
            super($anonymous0, $anonymous1);
        }

        public void execute() {
            boolean cancel = FramedConnection.this.pushObserver.onHeaders(this.val$streamId, this.val$requestHeaders, this.val$inFinished);
            if (cancel) {
                try {
                    FramedConnection.this.frameWriter.rstStream(this.val$streamId, ErrorCode.CANCEL);
                } catch (IOException e) {
                    return;
                }
            }
            if (cancel || this.val$inFinished) {
                synchronized (FramedConnection.this) {
                    FramedConnection.this.currentPushRequests.remove(Integer.valueOf(this.val$streamId));
                }
            }
        }
    }

    /* renamed from: com.android.okhttp.internal.framed.FramedConnection.6 */
    class AnonymousClass6 extends NamedRunnable {
        final /* synthetic */ Buffer val$buffer;
        final /* synthetic */ int val$byteCount;
        final /* synthetic */ boolean val$inFinished;
        final /* synthetic */ int val$streamId;

        AnonymousClass6(String $anonymous0, Object[] $anonymous1, int val$streamId, Buffer val$buffer, int val$byteCount, boolean val$inFinished) {
            this.val$streamId = val$streamId;
            this.val$buffer = val$buffer;
            this.val$byteCount = val$byteCount;
            this.val$inFinished = val$inFinished;
            super($anonymous0, $anonymous1);
        }

        public void execute() {
            try {
                boolean cancel = FramedConnection.this.pushObserver.onData(this.val$streamId, this.val$buffer, this.val$byteCount, this.val$inFinished);
                if (cancel) {
                    FramedConnection.this.frameWriter.rstStream(this.val$streamId, ErrorCode.CANCEL);
                }
                if (cancel || this.val$inFinished) {
                    synchronized (FramedConnection.this) {
                        FramedConnection.this.currentPushRequests.remove(Integer.valueOf(this.val$streamId));
                    }
                }
            } catch (IOException e) {
            }
        }
    }

    /* renamed from: com.android.okhttp.internal.framed.FramedConnection.7 */
    class AnonymousClass7 extends NamedRunnable {
        final /* synthetic */ ErrorCode val$errorCode;
        final /* synthetic */ int val$streamId;

        AnonymousClass7(String $anonymous0, Object[] $anonymous1, int val$streamId, ErrorCode val$errorCode) {
            this.val$streamId = val$streamId;
            this.val$errorCode = val$errorCode;
            super($anonymous0, $anonymous1);
        }

        public void execute() {
            FramedConnection.this.pushObserver.onReset(this.val$streamId, this.val$errorCode);
            synchronized (FramedConnection.this) {
                FramedConnection.this.currentPushRequests.remove(Integer.valueOf(this.val$streamId));
            }
        }
    }

    public static class Builder {
        private boolean client;
        private IncomingStreamHandler handler;
        private String hostName;
        private Protocol protocol;
        private PushObserver pushObserver;
        private Socket socket;

        public Builder(boolean client, Socket socket) throws IOException {
            this(((InetSocketAddress) socket.getRemoteSocketAddress()).getHostName(), client, socket);
        }

        public Builder(String hostName, boolean client, Socket socket) throws IOException {
            this.handler = IncomingStreamHandler.REFUSE_INCOMING_STREAMS;
            this.protocol = Protocol.SPDY_3;
            this.pushObserver = PushObserver.CANCEL;
            this.hostName = hostName;
            this.client = client;
            this.socket = socket;
        }

        public Builder handler(IncomingStreamHandler handler) {
            this.handler = handler;
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
            return new FramedConnection();
        }
    }

    class Reader extends NamedRunnable implements Handler {
        FrameReader frameReader;

        /* renamed from: com.android.okhttp.internal.framed.FramedConnection.Reader.1 */
        class AnonymousClass1 extends NamedRunnable {
            final /* synthetic */ FramedStream val$newStream;

            AnonymousClass1(String $anonymous0, Object[] $anonymous1, FramedStream val$newStream) {
                this.val$newStream = val$newStream;
                super($anonymous0, $anonymous1);
            }

            public void execute() {
                try {
                    FramedConnection.this.handler.receive(this.val$newStream);
                } catch (IOException e) {
                    Internal.logger.log(Level.INFO, "StreamHandler failure for " + FramedConnection.this.hostName, e);
                    try {
                        this.val$newStream.close(ErrorCode.PROTOCOL_ERROR);
                    } catch (IOException e2) {
                    }
                }
            }
        }

        /* renamed from: com.android.okhttp.internal.framed.FramedConnection.Reader.2 */
        class AnonymousClass2 extends NamedRunnable {
            final /* synthetic */ Settings val$peerSettings;

            AnonymousClass2(String $anonymous0, Object[] $anonymous1, Settings val$peerSettings) {
                this.val$peerSettings = val$peerSettings;
                super($anonymous0, $anonymous1);
            }

            public void execute() {
                try {
                    FramedConnection.this.frameWriter.ackSettings(this.val$peerSettings);
                } catch (IOException e) {
                }
            }
        }

        private Reader() {
            super("OkHttp %s", this$0.hostName);
        }

        protected void execute() {
            ErrorCode connectionErrorCode = ErrorCode.INTERNAL_ERROR;
            ErrorCode streamErrorCode = ErrorCode.INTERNAL_ERROR;
            try {
                this.frameReader = FramedConnection.this.variant.newReader(Okio.buffer(Okio.source(FramedConnection.this.socket)), FramedConnection.this.client);
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
                if (stream != null) {
                    if (headersMode.failIfStreamPresent()) {
                        stream.closeLater(ErrorCode.PROTOCOL_ERROR);
                        FramedConnection.this.removeStream(streamId);
                        return;
                    }
                    stream.receiveHeaders(headerBlock, headersMode);
                    if (inFinished) {
                        stream.receiveFin();
                    }
                } else if (headersMode.failIfStreamAbsent()) {
                    FramedConnection.this.writeSynResetLater(streamId, ErrorCode.INVALID_STREAM);
                } else if (streamId <= FramedConnection.this.lastGoodStreamId) {
                } else if (streamId % 2 == FramedConnection.this.nextStreamId % 2) {
                } else {
                    FramedStream newStream = new FramedStream(streamId, FramedConnection.this, outFinished, inFinished, headerBlock);
                    FramedConnection.this.lastGoodStreamId = streamId;
                    FramedConnection.this.streams.put(Integer.valueOf(streamId), newStream);
                    FramedConnection.executor.execute(new AnonymousClass1("OkHttp %s stream %d", new Object[]{FramedConnection.this.hostName, Integer.valueOf(streamId)}, newStream));
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
            FramedStream[] framedStreamArr = null;
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
                        framedStreamArr = (FramedStream[]) FramedConnection.this.streams.values().toArray(new FramedStream[FramedConnection.this.streams.size()]);
                    }
                }
            }
            if (framedStreamArr != null && delta != 0) {
                for (FramedStream stream : framedStreamArr) {
                    synchronized (stream) {
                        stream.addBytesToWriteWindow(delta);
                    }
                }
            }
        }

        private void ackSettingsLater(Settings peerSettings) {
            FramedConnection.executor.execute(new AnonymousClass2("OkHttp %s ACK Settings", new Object[]{FramedConnection.this.hostName}, peerSettings));
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
            if (debugData.size() > 0) {
            }
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.okhttp.internal.framed.FramedConnection.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.okhttp.internal.framed.FramedConnection.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.framed.FramedConnection.<clinit>():void");
    }

    private FramedConnection(Builder builder) throws IOException {
        int i;
        int i2 = 2;
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
        this.handler = builder.handler;
        if (builder.client) {
            i = 1;
        } else {
            i = 2;
        }
        this.nextStreamId = i;
        if (builder.client && this.protocol == Protocol.HTTP_2) {
            this.nextStreamId += 2;
        }
        if (builder.client) {
            i2 = 1;
        }
        this.nextPingId = i2;
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
        this.frameWriter = this.variant.newWriter(Okio.buffer(Okio.sink(builder.socket)), this.client);
        this.readerRunnable = new Reader();
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
        boolean outFinished = out ? -assertionsDisabled : true;
        boolean inFinished = in ? -assertionsDisabled : true;
        synchronized (this.frameWriter) {
            synchronized (this) {
                if (this.shutdown) {
                    throw new IOException("shutdown");
                }
                int streamId = this.nextStreamId;
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
            boolean z;
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
            if (outFinished && byteCount == 0) {
                z = true;
            } else {
                z = -assertionsDisabled;
            }
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
        executor.submit(new AnonymousClass1("OkHttp %s stream %d", new Object[]{this.hostName, Integer.valueOf(streamId)}, streamId, errorCode));
    }

    void writeSynReset(int streamId, ErrorCode statusCode) throws IOException {
        this.frameWriter.rstStream(streamId, statusCode);
    }

    void writeWindowUpdateLater(int streamId, long unacknowledgedBytesRead) {
        executor.execute(new AnonymousClass2("OkHttp Window Update %s stream %d", new Object[]{this.hostName, Integer.valueOf(streamId)}, streamId, unacknowledgedBytesRead));
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
        executor.execute(new AnonymousClass3("OkHttp %s ping %08x%08x", new Object[]{this.hostName, Integer.valueOf(payload1), Integer.valueOf(payload2)}, reply, payload1, payload2, ping));
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
                return;
            }
        }
    }

    public void close() throws IOException {
        close(ErrorCode.NO_ERROR, ErrorCode.CANCEL);
    }

    private void close(ErrorCode connectionCode, ErrorCode streamCode) throws IOException {
        if (!-assertionsDisabled) {
            if ((Thread.holdsLock(this) ? null : 1) == null) {
                throw new AssertionError();
            }
        }
        IOException thrown = null;
        try {
            shutdown(connectionCode);
        } catch (IOException e) {
            thrown = e;
        }
        FramedStream[] framedStreamArr = null;
        Ping[] pingArr = null;
        synchronized (this) {
            if (!this.streams.isEmpty()) {
                framedStreamArr = (FramedStream[]) this.streams.values().toArray(new FramedStream[this.streams.size()]);
                this.streams.clear();
                setIdle(-assertionsDisabled);
            }
            if (this.pings != null) {
                pingArr = (Ping[]) this.pings.values().toArray(new Ping[this.pings.size()]);
                this.pings = null;
            }
        }
        if (framedStreamArr != null) {
            for (FramedStream stream : framedStreamArr) {
                try {
                    stream.close(streamCode);
                } catch (IOException e2) {
                    if (thrown != null) {
                        thrown = e2;
                    }
                }
            }
        }
        if (pingArr != null) {
            for (Ping ping : pingArr) {
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
    }

    public void sendConnectionPreface() throws IOException {
        this.frameWriter.connectionPreface();
        this.frameWriter.settings(this.okHttpSettings);
        int windowSize = this.okHttpSettings.getInitialWindowSize(65536);
        if (windowSize != 65536) {
            this.frameWriter.windowUpdate(0, (long) (windowSize - 65536));
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
            this.pushExecutor.execute(new AnonymousClass4("OkHttp %s Push Request[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}, streamId, requestHeaders));
        }
    }

    private void pushHeadersLater(int streamId, List<Header> requestHeaders, boolean inFinished) {
        this.pushExecutor.execute(new AnonymousClass5("OkHttp %s Push Headers[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}, streamId, requestHeaders, inFinished));
    }

    private void pushDataLater(int streamId, BufferedSource source, int byteCount, boolean inFinished) throws IOException {
        Buffer buffer = new Buffer();
        source.require((long) byteCount);
        source.read(buffer, (long) byteCount);
        if (buffer.size() != ((long) byteCount)) {
            throw new IOException(buffer.size() + " != " + byteCount);
        }
        this.pushExecutor.execute(new AnonymousClass6("OkHttp %s Push Data[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}, streamId, buffer, byteCount, inFinished));
    }

    private void pushResetLater(int streamId, ErrorCode errorCode) {
        this.pushExecutor.execute(new AnonymousClass7("OkHttp %s Push Reset[%s]", new Object[]{this.hostName, Integer.valueOf(streamId)}, streamId, errorCode));
    }
}
