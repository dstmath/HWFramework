package com.huawei.okhttp3.internal.ws;

import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.Callback;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Protocol;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.WebSocket;
import com.huawei.okhttp3.WebSocketListener;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.StreamAllocation;
import com.huawei.okhttp3.internal.ws.WebSocketReader.FrameCallback;
import com.huawei.okio.BufferedSink;
import com.huawei.okio.BufferedSource;
import com.huawei.okio.ByteString;
import java.io.Closeable;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class RealWebSocket implements WebSocket, FrameCallback {
    static final /* synthetic */ boolean -assertionsDisabled = (RealWebSocket.class.desiredAssertionStatus() ^ 1);
    private static final long CANCEL_AFTER_CLOSE_MILLIS = 60000;
    private static final long MAX_QUEUE_SIZE = 16777216;
    private static final List<Protocol> ONLY_HTTP1 = Collections.singletonList(Protocol.HTTP_1_1);
    private Call call;
    private ScheduledFuture<?> cancelFuture;
    private boolean enqueuedClose;
    private ScheduledExecutorService executor;
    private boolean failed;
    private final String key;
    final WebSocketListener listener;
    private final ArrayDeque<Object> messageAndCloseQueue = new ArrayDeque();
    private final Request originalRequest;
    int pingCount;
    int pongCount;
    private final ArrayDeque<ByteString> pongQueue = new ArrayDeque();
    private long queueSize;
    private final Random random;
    private WebSocketReader reader;
    private int receivedCloseCode = -1;
    private String receivedCloseReason;
    private Streams streams;
    private WebSocketWriter writer;
    private final Runnable writerRunnable;

    final class CancelRunnable implements Runnable {
        CancelRunnable() {
        }

        public void run() {
            RealWebSocket.this.cancel();
        }
    }

    public static abstract class Streams implements Closeable {
        public final boolean client;
        public final BufferedSink sink;
        public final BufferedSource source;

        public Streams(boolean client, BufferedSource source, BufferedSink sink) {
            this.client = client;
            this.source = source;
            this.sink = sink;
        }
    }

    static final class ClientStreams extends Streams {
        private final StreamAllocation streamAllocation;

        ClientStreams(StreamAllocation streamAllocation) {
            super(true, streamAllocation.connection().source, streamAllocation.connection().sink);
            this.streamAllocation = streamAllocation;
        }

        public void close() {
            this.streamAllocation.streamFinished(true, this.streamAllocation.codec());
        }
    }

    static final class Close {
        final long cancelAfterCloseMillis;
        final int code;
        final ByteString reason;

        Close(int code, ByteString reason, long cancelAfterCloseMillis) {
            this.code = code;
            this.reason = reason;
            this.cancelAfterCloseMillis = cancelAfterCloseMillis;
        }
    }

    static final class Message {
        final ByteString data;
        final int formatOpcode;

        Message(int formatOpcode, ByteString data) {
            this.formatOpcode = formatOpcode;
            this.data = data;
        }
    }

    private final class PingRunnable implements Runnable {
        /* synthetic */ PingRunnable(RealWebSocket this$0, PingRunnable -this1) {
            this();
        }

        private PingRunnable() {
        }

        public void run() {
            RealWebSocket.this.writePingFrame();
        }
    }

    public RealWebSocket(Request request, WebSocketListener listener, Random random) {
        if ("GET".equals(request.method())) {
            this.originalRequest = request;
            this.listener = listener;
            this.random = random;
            byte[] nonce = new byte[16];
            random.nextBytes(nonce);
            this.key = ByteString.of(nonce).base64();
            this.writerRunnable = new Runnable() {
                public void run() {
                    do {
                        try {
                        } catch (IOException e) {
                            RealWebSocket.this.failWebSocket(e, null);
                            return;
                        }
                    } while (RealWebSocket.this.writeOneFrame());
                }
            };
            return;
        }
        throw new IllegalArgumentException("Request must be GET: " + request.method());
    }

    public Request request() {
        return this.originalRequest;
    }

    public synchronized long queueSize() {
        return this.queueSize;
    }

    public void cancel() {
        this.call.cancel();
    }

    public void connect(OkHttpClient client) {
        client = client.newBuilder().protocols(ONLY_HTTP1).build();
        final int pingIntervalMillis = client.pingIntervalMillis();
        final Request request = this.originalRequest.newBuilder().header("Upgrade", "websocket").header("Connection", "Upgrade").header("Sec-WebSocket-Key", this.key).header("Sec-WebSocket-Version", "13").build();
        this.call = Internal.instance.newWebSocketCall(client, request);
        this.call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) {
                try {
                    RealWebSocket.this.checkResponse(response);
                    StreamAllocation streamAllocation = Internal.instance.streamAllocation(call);
                    streamAllocation.noNewStreams();
                    Streams streams = new ClientStreams(streamAllocation);
                    try {
                        RealWebSocket.this.listener.onOpen(RealWebSocket.this, response);
                        RealWebSocket.this.initReaderAndWriter("OkHttp WebSocket " + request.url().redact(), (long) pingIntervalMillis, streams);
                        streamAllocation.connection().socket().setSoTimeout(0);
                        RealWebSocket.this.loopReader();
                    } catch (Exception e) {
                        RealWebSocket.this.failWebSocket(e, null);
                    }
                } catch (ProtocolException e2) {
                    RealWebSocket.this.failWebSocket(e2, response);
                    Util.closeQuietly((Closeable) response);
                }
            }

            public void onFailure(Call call, IOException e) {
                RealWebSocket.this.failWebSocket(e, null);
            }
        });
    }

    void checkResponse(Response response) throws ProtocolException {
        if (response.code() != 101) {
            throw new ProtocolException("Expected HTTP 101 response but was '" + response.code() + " " + response.message() + "'");
        }
        String headerConnection = response.header("Connection");
        if ("Upgrade".equalsIgnoreCase(headerConnection)) {
            String headerUpgrade = response.header("Upgrade");
            if ("websocket".equalsIgnoreCase(headerUpgrade)) {
                String headerAccept = response.header("Sec-WebSocket-Accept");
                String acceptExpected = ByteString.encodeUtf8(this.key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").sha1().base64();
                if (!acceptExpected.equals(headerAccept)) {
                    throw new ProtocolException("Expected 'Sec-WebSocket-Accept' header value '" + acceptExpected + "' but was '" + headerAccept + "'");
                }
                return;
            }
            throw new ProtocolException("Expected 'Upgrade' header value 'websocket' but was '" + headerUpgrade + "'");
        }
        throw new ProtocolException("Expected 'Connection' header value 'Upgrade' but was '" + headerConnection + "'");
    }

    public void initReaderAndWriter(String name, long pingIntervalMillis, Streams streams) throws IOException {
        synchronized (this) {
            this.streams = streams;
            this.writer = new WebSocketWriter(streams.client, streams.sink, this.random);
            this.executor = new ScheduledThreadPoolExecutor(1, Util.threadFactory(name, false));
            if (pingIntervalMillis != 0) {
                this.executor.scheduleAtFixedRate(new PingRunnable(this, null), pingIntervalMillis, pingIntervalMillis, TimeUnit.MILLISECONDS);
            }
            if (!this.messageAndCloseQueue.isEmpty()) {
                runWriter();
            }
        }
        this.reader = new WebSocketReader(streams.client, streams.source, this);
    }

    public void loopReader() throws IOException {
        while (this.receivedCloseCode == -1) {
            this.reader.processNextFrame();
        }
    }

    boolean processNextFrame() throws IOException {
        boolean z = false;
        try {
            this.reader.processNextFrame();
            if (this.receivedCloseCode == -1) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            failWebSocket(e, null);
            return false;
        }
    }

    synchronized int pingCount() {
        return this.pingCount;
    }

    synchronized int pongCount() {
        return this.pongCount;
    }

    public void onReadMessage(String text) throws IOException {
        this.listener.onMessage((WebSocket) this, text);
    }

    public void onReadMessage(ByteString bytes) throws IOException {
        this.listener.onMessage((WebSocket) this, bytes);
    }

    /* JADX WARNING: Missing block: B:9:0x0012, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void onReadPing(ByteString payload) {
        if (!this.failed && (!this.enqueuedClose || !this.messageAndCloseQueue.isEmpty())) {
            this.pongQueue.add(payload);
            runWriter();
            this.pingCount++;
        }
    }

    public synchronized void onReadPong(ByteString buffer) {
        this.pongCount++;
    }

    public void onReadClose(int code, String reason) {
        if (code == -1) {
            throw new IllegalArgumentException();
        }
        Closeable toClose = null;
        synchronized (this) {
            if (this.receivedCloseCode != -1) {
                throw new IllegalStateException("already closed");
            }
            this.receivedCloseCode = code;
            this.receivedCloseReason = reason;
            if (this.enqueuedClose && this.messageAndCloseQueue.isEmpty()) {
                toClose = this.streams;
                this.streams = null;
                if (this.cancelFuture != null) {
                    this.cancelFuture.cancel(false);
                }
                this.executor.shutdown();
            }
        }
        try {
            this.listener.onClosing(this, code, reason);
            if (toClose != null) {
                this.listener.onClosed(this, code, reason);
            }
            Util.closeQuietly(toClose);
        } catch (Throwable th) {
            Util.closeQuietly(toClose);
        }
    }

    public boolean send(String text) {
        if (text != null) {
            return send(ByteString.encodeUtf8(text), 1);
        }
        throw new NullPointerException("text == null");
    }

    public boolean send(ByteString bytes) {
        if (bytes != null) {
            return send(bytes, 2);
        }
        throw new NullPointerException("bytes == null");
    }

    /* JADX WARNING: Missing block: B:8:0x000b, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean send(ByteString data, int formatOpcode) {
        if (!this.failed && !this.enqueuedClose) {
            if (this.queueSize + ((long) data.size()) > MAX_QUEUE_SIZE) {
                close(1001, null);
                return false;
            }
            this.queueSize += (long) data.size();
            this.messageAndCloseQueue.add(new Message(formatOpcode, data));
            runWriter();
            return true;
        }
    }

    synchronized boolean pong(ByteString payload) {
        if (this.failed || (this.enqueuedClose && this.messageAndCloseQueue.isEmpty())) {
            return false;
        }
        this.pongQueue.add(payload);
        runWriter();
        return true;
    }

    public boolean close(int code, String reason) {
        return close(code, reason, CANCEL_AFTER_CLOSE_MILLIS);
    }

    synchronized boolean close(int code, String reason, long cancelAfterCloseMillis) {
        WebSocketProtocol.validateCloseCode(code);
        ByteString byteString = null;
        if (reason != null) {
            byteString = ByteString.encodeUtf8(reason);
            if (((long) byteString.size()) > 123) {
                throw new IllegalArgumentException("reason.size() > 123: " + reason);
            }
        }
        if (this.failed || this.enqueuedClose) {
            return false;
        }
        this.enqueuedClose = true;
        this.messageAndCloseQueue.add(new Close(code, byteString, cancelAfterCloseMillis));
        runWriter();
        return true;
    }

    private void runWriter() {
        if (!-assertionsDisabled && !Thread.holdsLock(this)) {
            throw new AssertionError();
        } else if (this.executor != null) {
            this.executor.execute(this.writerRunnable);
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0046, code:
            if (r5 == null) goto L_0x0073;
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            r10.writePong(r5);
     */
    /* JADX WARNING: Missing block: B:19:0x004b, code:
            com.huawei.okhttp3.internal.Util.closeQuietly(r9);
     */
    /* JADX WARNING: Missing block: B:20:0x004f, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:31:0x0075, code:
            if ((r4 instanceof com.huawei.okhttp3.internal.ws.RealWebSocket.Message) == false) goto L_0x00ad;
     */
    /* JADX WARNING: Missing block: B:32:0x0077, code:
            r3 = ((com.huawei.okhttp3.internal.ws.RealWebSocket.Message) r4).data;
            r8 = com.huawei.okio.Okio.buffer(r10.newMessageSink(((com.huawei.okhttp3.internal.ws.RealWebSocket.Message) r4).formatOpcode, (long) r3.size()));
            r8.write(r3);
            r8.close();
     */
    /* JADX WARNING: Missing block: B:33:0x0094, code:
            monitor-enter(r16);
     */
    /* JADX WARNING: Missing block: B:35:?, code:
            r16.queueSize -= (long) r3.size();
     */
    /* JADX WARNING: Missing block: B:37:?, code:
            monitor-exit(r16);
     */
    /* JADX WARNING: Missing block: B:39:0x00a6, code:
            com.huawei.okhttp3.internal.Util.closeQuietly(r9);
     */
    /* JADX WARNING: Missing block: B:46:0x00af, code:
            if ((r4 instanceof com.huawei.okhttp3.internal.ws.RealWebSocket.Close) == false) goto L_0x00c8;
     */
    /* JADX WARNING: Missing block: B:47:0x00b1, code:
            r2 = (com.huawei.okhttp3.internal.ws.RealWebSocket.Close) r4;
            r10.writeClose(r2.code, r2.reason);
     */
    /* JADX WARNING: Missing block: B:48:0x00bc, code:
            if (r9 == null) goto L_0x004b;
     */
    /* JADX WARNING: Missing block: B:49:0x00be, code:
            r16.listener.onClosed(r16, r6, r7);
     */
    /* JADX WARNING: Missing block: B:51:0x00cd, code:
            throw new java.lang.AssertionError();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean writeOneFrame() throws IOException {
        Object messageOrClose = null;
        int receivedCloseCode = -1;
        String receivedCloseReason = null;
        Closeable streamsToClose = null;
        synchronized (this) {
            if (this.failed) {
                return false;
            }
            WebSocketWriter writer = this.writer;
            ByteString pong = (ByteString) this.pongQueue.poll();
            if (pong == null) {
                messageOrClose = this.messageAndCloseQueue.poll();
                if (messageOrClose instanceof Close) {
                    receivedCloseCode = this.receivedCloseCode;
                    receivedCloseReason = this.receivedCloseReason;
                    if (receivedCloseCode != -1) {
                        streamsToClose = this.streams;
                        this.streams = null;
                        this.executor.shutdown();
                    } else {
                        this.cancelFuture = this.executor.schedule(new CancelRunnable(), ((Close) messageOrClose).cancelAfterCloseMillis, TimeUnit.MILLISECONDS);
                    }
                } else if (messageOrClose == null) {
                    return false;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:?, code:
            r1.writePing(com.huawei.okio.ByteString.EMPTY);
     */
    /* JADX WARNING: Missing block: B:15:0x0013, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:16:0x0014, code:
            failWebSocket(r0, null);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writePingFrame() {
        synchronized (this) {
            if (this.failed) {
                return;
            }
            WebSocketWriter writer = this.writer;
        }
    }

    /* JADX WARNING: Missing block: B:16:?, code:
            r3.listener.onFailure(r3, r4, r5);
     */
    /* JADX WARNING: Missing block: B:22:0x0030, code:
            com.huawei.okhttp3.internal.Util.closeQuietly(r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void failWebSocket(Exception e, Response response) {
        synchronized (this) {
            if (this.failed) {
                return;
            }
            this.failed = true;
            Closeable streamsToClose = this.streams;
            this.streams = null;
            if (this.cancelFuture != null) {
                this.cancelFuture.cancel(false);
            }
            if (this.executor != null) {
                this.executor.shutdown();
            }
        }
    }
}
