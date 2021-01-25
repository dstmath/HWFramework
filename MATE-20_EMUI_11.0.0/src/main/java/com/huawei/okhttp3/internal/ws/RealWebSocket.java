package com.huawei.okhttp3.internal.ws;

import com.huawei.okhttp3.Call;
import com.huawei.okhttp3.Callback;
import com.huawei.okhttp3.EventListener;
import com.huawei.okhttp3.OkHttpClient;
import com.huawei.okhttp3.Protocol;
import com.huawei.okhttp3.Request;
import com.huawei.okhttp3.Response;
import com.huawei.okhttp3.WebSocket;
import com.huawei.okhttp3.WebSocketListener;
import com.huawei.okhttp3.internal.Internal;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.connection.Exchange;
import com.huawei.okhttp3.internal.ws.WebSocketReader;
import com.huawei.okio.BufferedSink;
import com.huawei.okio.BufferedSource;
import com.huawei.okio.ByteString;
import com.huawei.okio.Okio;
import java.io.Closeable;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

public final class RealWebSocket implements WebSocket, WebSocketReader.FrameCallback {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final long CANCEL_AFTER_CLOSE_MILLIS = 60000;
    private static final long MAX_QUEUE_SIZE = 16777216;
    private static final List<Protocol> ONLY_HTTP1 = Collections.singletonList(Protocol.HTTP_1_1);
    private boolean awaitingPong;
    private Call call;
    private ScheduledFuture<?> cancelFuture;
    private boolean enqueuedClose;
    private ScheduledExecutorService executor;
    private boolean failed;
    private final String key;
    final WebSocketListener listener;
    private final ArrayDeque<Object> messageAndCloseQueue = new ArrayDeque<>();
    private final Request originalRequest;
    private final long pingIntervalMillis;
    private final ArrayDeque<ByteString> pongQueue = new ArrayDeque<>();
    private long queueSize;
    private final Random random;
    private WebSocketReader reader;
    private int receivedCloseCode = -1;
    private String receivedCloseReason;
    private int receivedPingCount;
    private int receivedPongCount;
    private int sentPingCount;
    private Streams streams;
    private WebSocketWriter writer;
    private final Runnable writerRunnable;

    public RealWebSocket(Request request, WebSocketListener listener2, Random random2, long pingIntervalMillis2) {
        if ("GET".equals(request.method())) {
            this.originalRequest = request;
            this.listener = listener2;
            this.random = random2;
            this.pingIntervalMillis = pingIntervalMillis2;
            byte[] nonce = new byte[16];
            random2.nextBytes(nonce);
            this.key = ByteString.of(nonce).base64();
            this.writerRunnable = new Runnable() {
                /* class com.huawei.okhttp3.internal.ws.$$Lambda$RealWebSocket$rKT1oXby4tBse6rjUzjrToUYh4I */

                @Override // java.lang.Runnable
                public final void run() {
                    RealWebSocket.this.lambda$new$0$RealWebSocket();
                }
            };
            return;
        }
        throw new IllegalArgumentException("Request must be GET: " + request.method());
    }

    public /* synthetic */ void lambda$new$0$RealWebSocket() {
        do {
            try {
            } catch (IOException e) {
                failWebSocket(e, null);
                return;
            }
        } while (writeOneFrame());
    }

    @Override // com.huawei.okhttp3.WebSocket
    public Request request() {
        return this.originalRequest;
    }

    @Override // com.huawei.okhttp3.WebSocket
    public synchronized long queueSize() {
        return this.queueSize;
    }

    @Override // com.huawei.okhttp3.WebSocket
    public void cancel() {
        this.call.cancel();
    }

    public void connect(OkHttpClient client) {
        OkHttpClient client2 = client.newBuilder().eventListener(EventListener.NONE).protocols(ONLY_HTTP1).build();
        final Request request = this.originalRequest.newBuilder().header("Upgrade", "websocket").header("Connection", "Upgrade").header("Sec-WebSocket-Key", this.key).header("Sec-WebSocket-Version", "13").build();
        this.call = Internal.instance.newWebSocketCall(client2, request);
        this.call.enqueue(new Callback() {
            /* class com.huawei.okhttp3.internal.ws.RealWebSocket.AnonymousClass1 */

            @Override // com.huawei.okhttp3.Callback
            public void onResponse(Call call, Response response) {
                Exchange exchange = Internal.instance.exchange(response);
                try {
                    RealWebSocket.this.checkUpgradeSuccess(response, exchange);
                    Streams streams = exchange.newWebSocketStreams();
                    try {
                        RealWebSocket.this.initReaderAndWriter("OkHttp WebSocket " + request.url().redact(), streams);
                        RealWebSocket.this.listener.onOpen(RealWebSocket.this, response);
                        RealWebSocket.this.loopReader();
                    } catch (Exception e) {
                        RealWebSocket.this.failWebSocket(e, null);
                    }
                } catch (IOException e2) {
                    if (exchange != null) {
                        exchange.webSocketUpgradeFailed();
                    }
                    RealWebSocket.this.failWebSocket(e2, response);
                    Util.closeQuietly(response);
                }
            }

            @Override // com.huawei.okhttp3.Callback
            public void onFailure(Call call, IOException e) {
                RealWebSocket.this.failWebSocket(e, null);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void checkUpgradeSuccess(Response response, @Nullable Exchange exchange) throws IOException {
        if (response.code() == 101) {
            String headerConnection = response.header("Connection");
            if ("Upgrade".equalsIgnoreCase(headerConnection)) {
                String headerUpgrade = response.header("Upgrade");
                if ("websocket".equalsIgnoreCase(headerUpgrade)) {
                    String headerAccept = response.header("Sec-WebSocket-Accept");
                    String acceptExpected = ByteString.encodeUtf8(this.key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").sha1().base64();
                    if (!acceptExpected.equals(headerAccept)) {
                        throw new ProtocolException("Expected 'Sec-WebSocket-Accept' header value '" + acceptExpected + "' but was '" + headerAccept + "'");
                    } else if (exchange == null) {
                        throw new ProtocolException("Web Socket exchange missing: bad interceptor?");
                    }
                } else {
                    throw new ProtocolException("Expected 'Upgrade' header value 'websocket' but was '" + headerUpgrade + "'");
                }
            } else {
                throw new ProtocolException("Expected 'Connection' header value 'Upgrade' but was '" + headerConnection + "'");
            }
        } else {
            throw new ProtocolException("Expected HTTP 101 response but was '" + response.code() + " " + response.message() + "'");
        }
    }

    public void initReaderAndWriter(String name, Streams streams2) throws IOException {
        synchronized (this) {
            this.streams = streams2;
            this.writer = new WebSocketWriter(streams2.client, streams2.sink, this.random);
            this.executor = new ScheduledThreadPoolExecutor(1, Util.threadFactory(name, false));
            if (this.pingIntervalMillis != 0) {
                this.executor.scheduleAtFixedRate(new PingRunnable(), this.pingIntervalMillis, this.pingIntervalMillis, TimeUnit.MILLISECONDS);
            }
            if (!this.messageAndCloseQueue.isEmpty()) {
                runWriter();
            }
        }
        this.reader = new WebSocketReader(streams2.client, streams2.source, this);
    }

    public void loopReader() throws IOException {
        while (this.receivedCloseCode == -1) {
            this.reader.processNextFrame();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean processNextFrame() throws IOException {
        try {
            this.reader.processNextFrame();
            if (this.receivedCloseCode == -1) {
                return true;
            }
            return false;
        } catch (Exception e) {
            failWebSocket(e, null);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException {
        this.executor.awaitTermination((long) timeout, timeUnit);
    }

    /* access modifiers changed from: package-private */
    public void tearDown() throws InterruptedException {
        ScheduledFuture<?> scheduledFuture = this.cancelFuture;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
        this.executor.shutdown();
        this.executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    /* access modifiers changed from: package-private */
    public synchronized int sentPingCount() {
        return this.sentPingCount;
    }

    /* access modifiers changed from: package-private */
    public synchronized int receivedPingCount() {
        return this.receivedPingCount;
    }

    /* access modifiers changed from: package-private */
    public synchronized int receivedPongCount() {
        return this.receivedPongCount;
    }

    @Override // com.huawei.okhttp3.internal.ws.WebSocketReader.FrameCallback
    public void onReadMessage(String text) throws IOException {
        this.listener.onMessage(this, text);
    }

    @Override // com.huawei.okhttp3.internal.ws.WebSocketReader.FrameCallback
    public void onReadMessage(ByteString bytes) throws IOException {
        this.listener.onMessage(this, bytes);
    }

    @Override // com.huawei.okhttp3.internal.ws.WebSocketReader.FrameCallback
    public synchronized void onReadPing(ByteString payload) {
        if (!this.failed) {
            if (!this.enqueuedClose || !this.messageAndCloseQueue.isEmpty()) {
                this.pongQueue.add(payload);
                runWriter();
                this.receivedPingCount++;
            }
        }
    }

    @Override // com.huawei.okhttp3.internal.ws.WebSocketReader.FrameCallback
    public synchronized void onReadPong(ByteString buffer) {
        this.receivedPongCount++;
        this.awaitingPong = false;
    }

    @Override // com.huawei.okhttp3.internal.ws.WebSocketReader.FrameCallback
    public void onReadClose(int code, String reason) {
        if (code != -1) {
            Streams toClose = null;
            synchronized (this) {
                if (this.receivedCloseCode == -1) {
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
                } else {
                    throw new IllegalStateException("already closed");
                }
            }
            try {
                this.listener.onClosing(this, code, reason);
                if (toClose != null) {
                    this.listener.onClosed(this, code, reason);
                }
            } finally {
                Util.closeQuietly(toClose);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override // com.huawei.okhttp3.WebSocket
    public boolean send(String text) {
        if (text != null) {
            return send(ByteString.encodeUtf8(text), 1);
        }
        throw new NullPointerException("text == null");
    }

    @Override // com.huawei.okhttp3.WebSocket
    public boolean send(ByteString bytes) {
        if (bytes != null) {
            return send(bytes, 2);
        }
        throw new NullPointerException("bytes == null");
    }

    private synchronized boolean send(ByteString data, int formatOpcode) {
        if (!this.failed) {
            if (!this.enqueuedClose) {
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
        return false;
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean pong(ByteString payload) {
        if (!this.failed) {
            if (!this.enqueuedClose || !this.messageAndCloseQueue.isEmpty()) {
                this.pongQueue.add(payload);
                runWriter();
                return true;
            }
        }
        return false;
    }

    @Override // com.huawei.okhttp3.WebSocket
    public boolean close(int code, String reason) {
        return close(code, reason, CANCEL_AFTER_CLOSE_MILLIS);
    }

    /* access modifiers changed from: package-private */
    public synchronized boolean close(int code, String reason, long cancelAfterCloseMillis) {
        WebSocketProtocol.validateCloseCode(code);
        ByteString reasonBytes = null;
        if (reason != null) {
            reasonBytes = ByteString.encodeUtf8(reason);
            if (((long) reasonBytes.size()) > 123) {
                throw new IllegalArgumentException("reason.size() > 123: " + reason);
            }
        }
        if (!this.failed) {
            if (!this.enqueuedClose) {
                this.enqueuedClose = true;
                this.messageAndCloseQueue.add(new Close(code, reasonBytes, cancelAfterCloseMillis));
                runWriter();
                return true;
            }
        }
        return false;
    }

    private void runWriter() {
        ScheduledExecutorService scheduledExecutorService = this.executor;
        if (scheduledExecutorService != null) {
            scheduledExecutorService.execute(this.writerRunnable);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean writeOneFrame() throws IOException {
        WebSocketWriter writer2;
        ByteString pong;
        Object messageOrClose = null;
        int receivedCloseCode2 = -1;
        String receivedCloseReason2 = null;
        Streams streamsToClose = null;
        synchronized (this) {
            if (this.failed) {
                return false;
            }
            writer2 = this.writer;
            pong = this.pongQueue.poll();
            if (pong == null) {
                messageOrClose = this.messageAndCloseQueue.poll();
                if (messageOrClose instanceof Close) {
                    receivedCloseCode2 = this.receivedCloseCode;
                    receivedCloseReason2 = this.receivedCloseReason;
                    if (receivedCloseCode2 != -1) {
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
        if (pong != null) {
            try {
                writer2.writePong(pong);
            } catch (Throwable th) {
                Util.closeQuietly(streamsToClose);
                throw th;
            }
        } else if (messageOrClose instanceof Message) {
            ByteString data = ((Message) messageOrClose).data;
            BufferedSink sink = Okio.buffer(writer2.newMessageSink(((Message) messageOrClose).formatOpcode, (long) data.size()));
            sink.write(data);
            sink.close();
            synchronized (this) {
                this.queueSize -= (long) data.size();
            }
        } else if (messageOrClose instanceof Close) {
            Close close = (Close) messageOrClose;
            writer2.writeClose(close.code, close.reason);
            if (streamsToClose != null) {
                this.listener.onClosed(this, receivedCloseCode2, receivedCloseReason2);
            }
        } else {
            throw new AssertionError();
        }
        Util.closeQuietly(streamsToClose);
        return true;
    }

    /* access modifiers changed from: private */
    public final class PingRunnable implements Runnable {
        PingRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            RealWebSocket.this.writePingFrame();
        }
    }

    /* access modifiers changed from: package-private */
    public void writePingFrame() {
        WebSocketWriter writer2;
        int failedPing;
        synchronized (this) {
            if (!this.failed) {
                writer2 = this.writer;
                failedPing = this.awaitingPong ? this.sentPingCount : -1;
                this.sentPingCount++;
                this.awaitingPong = true;
            } else {
                return;
            }
        }
        if (failedPing != -1) {
            StringBuilder sb = new StringBuilder();
            sb.append("sent ping but didn't receive pong within ");
            sb.append(this.pingIntervalMillis);
            sb.append("ms (after ");
            sb.append(failedPing - 1);
            sb.append(" successful ping/pongs)");
            failWebSocket(new SocketTimeoutException(sb.toString()), null);
            return;
        }
        try {
            writer2.writePing(ByteString.EMPTY);
        } catch (IOException e) {
            failWebSocket(e, null);
        }
    }

    public void failWebSocket(Exception e, @Nullable Response response) {
        synchronized (this) {
            if (!this.failed) {
                this.failed = true;
                Streams streamsToClose = this.streams;
                this.streams = null;
                if (this.cancelFuture != null) {
                    this.cancelFuture.cancel(false);
                }
                if (this.executor != null) {
                    this.executor.shutdown();
                }
                try {
                    this.listener.onFailure(this, e, response);
                } finally {
                    Util.closeQuietly(streamsToClose);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class Message {
        final ByteString data;
        final int formatOpcode;

        Message(int formatOpcode2, ByteString data2) {
            this.formatOpcode = formatOpcode2;
            this.data = data2;
        }
    }

    /* access modifiers changed from: package-private */
    public static final class Close {
        final long cancelAfterCloseMillis;
        final int code;
        final ByteString reason;

        Close(int code2, ByteString reason2, long cancelAfterCloseMillis2) {
            this.code = code2;
            this.reason = reason2;
            this.cancelAfterCloseMillis = cancelAfterCloseMillis2;
        }
    }

    public static abstract class Streams implements Closeable {
        public final boolean client;
        public final BufferedSink sink;
        public final BufferedSource source;

        public Streams(boolean client2, BufferedSource source2, BufferedSink sink2) {
            this.client = client2;
            this.source = source2;
            this.sink = sink2;
        }
    }

    /* access modifiers changed from: package-private */
    public final class CancelRunnable implements Runnable {
        CancelRunnable() {
        }

        @Override // java.lang.Runnable
        public void run() {
            RealWebSocket.this.cancel();
        }
    }
}
