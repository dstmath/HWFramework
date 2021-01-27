package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.NamedRunnable;
import com.huawei.okhttp3.internal.Util;
import com.huawei.okhttp3.internal.cache.CacheInterceptor;
import com.huawei.okhttp3.internal.connection.ConnectInterceptor;
import com.huawei.okhttp3.internal.connection.Transmitter;
import com.huawei.okhttp3.internal.http.BridgeInterceptor;
import com.huawei.okhttp3.internal.http.CallServerInterceptor;
import com.huawei.okhttp3.internal.http.RealInterceptorChain;
import com.huawei.okhttp3.internal.http.RetryAndFollowUpInterceptor;
import com.huawei.okhttp3.internal.platform.Platform;
import com.huawei.okio.Timeout;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@Deprecated
final class RealCall implements Call {
    final OkHttpClient client;
    private boolean executed;
    final boolean forWebSocket;
    final Request originalRequest;
    private Transmitter transmitter;

    private RealCall(OkHttpClient client2, Request originalRequest2, boolean forWebSocket2) {
        this.client = client2;
        this.originalRequest = originalRequest2;
        this.forWebSocket = forWebSocket2;
    }

    static RealCall newRealCall(OkHttpClient client2, Request originalRequest2, boolean forWebSocket2) {
        RealCall call = new RealCall(client2, originalRequest2, forWebSocket2);
        call.transmitter = new Transmitter(client2, call);
        return call;
    }

    @Override // com.huawei.okhttp3.Call
    public Request request() {
        return this.originalRequest;
    }

    @Override // com.huawei.okhttp3.Call
    public Response execute() throws IOException {
        synchronized (this) {
            if (!this.executed) {
                this.executed = true;
            } else {
                throw new IllegalStateException("Already Executed");
            }
        }
        this.transmitter.timeoutEnter();
        this.transmitter.callStart();
        try {
            this.client.dispatcher().executed(this);
            return getResponseWithInterceptorChain();
        } finally {
            this.client.dispatcher().finished(this);
        }
    }

    @Override // com.huawei.okhttp3.Call
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (!this.executed) {
                this.executed = true;
            } else {
                throw new IllegalStateException("Already Executed");
            }
        }
        this.transmitter.callStart();
        this.client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    @Override // com.huawei.okhttp3.Call
    public void cancel() {
        this.transmitter.cancel();
    }

    @Override // com.huawei.okhttp3.Call
    public Timeout timeout() {
        return this.transmitter.timeout();
    }

    @Override // com.huawei.okhttp3.Call
    public synchronized boolean isExecuted() {
        return this.executed;
    }

    @Override // com.huawei.okhttp3.Call
    public boolean isCanceled() {
        return this.transmitter.isCanceled();
    }

    @Override // com.huawei.okhttp3.Call, java.lang.Object
    public RealCall clone() {
        return newRealCall(this.client, this.originalRequest, this.forWebSocket);
    }

    final class AsyncCall extends NamedRunnable {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private volatile AtomicInteger callsPerHost = new AtomicInteger(0);
        private final Callback responseCallback;

        AsyncCall(Callback responseCallback2) {
            super("OkHttp %s", RealCall.this.redactedUrl());
            this.responseCallback = responseCallback2;
        }

        /* access modifiers changed from: package-private */
        public AtomicInteger callsPerHost() {
            return this.callsPerHost;
        }

        /* access modifiers changed from: package-private */
        public void reuseCallsPerHostFrom(AsyncCall other) {
            this.callsPerHost = other.callsPerHost;
        }

        /* access modifiers changed from: package-private */
        public String host() {
            return RealCall.this.originalRequest.url().host();
        }

        /* access modifiers changed from: package-private */
        public Request request() {
            return RealCall.this.originalRequest;
        }

        /* access modifiers changed from: package-private */
        public RealCall get() {
            return RealCall.this;
        }

        /* access modifiers changed from: package-private */
        public void executeOn(ExecutorService executorService) {
            try {
                executorService.execute(this);
                if (1 != 0) {
                    return;
                }
            } catch (RejectedExecutionException e) {
                InterruptedIOException ioException = new InterruptedIOException("executor rejected");
                ioException.initCause(e);
                RealCall.this.transmitter.noMoreExchanges(ioException);
                this.responseCallback.onFailure(RealCall.this, ioException);
                if (0 != 0) {
                    return;
                }
            } catch (Throwable th) {
                if (0 == 0) {
                    RealCall.this.client.dispatcher().finished(this);
                }
                throw th;
            }
            RealCall.this.client.dispatcher().finished(this);
        }

        /* access modifiers changed from: protected */
        @Override // com.huawei.okhttp3.internal.NamedRunnable
        public void execute() {
            boolean signalledCallback = false;
            RealCall.this.transmitter.timeoutEnter();
            try {
                signalledCallback = true;
                this.responseCallback.onResponse(RealCall.this, RealCall.this.getResponseWithInterceptorChain());
            } catch (IOException e) {
                if (signalledCallback) {
                    Platform platform = Platform.get();
                    platform.log(4, "Callback failure for " + RealCall.this.toLoggableString(), e);
                } else {
                    this.responseCallback.onFailure(RealCall.this, e);
                }
            } catch (Throwable t) {
                RealCall.this.client.dispatcher().finished(this);
                throw t;
            }
            RealCall.this.client.dispatcher().finished(this);
        }
    }

    /* access modifiers changed from: package-private */
    public String toLoggableString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isCanceled() ? "canceled " : "");
        sb.append(this.forWebSocket ? "web socket" : "call");
        sb.append(" to ");
        sb.append(redactedUrl());
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public String redactedUrl() {
        return this.originalRequest.url().redact();
    }

    /* access modifiers changed from: package-private */
    public Response getResponseWithInterceptorChain() throws IOException {
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(this.client.interceptors());
        interceptors.add(new RetryAndFollowUpInterceptor(this.client));
        interceptors.add(new BridgeInterceptor(this.client.cookieJar()));
        interceptors.add(new CacheInterceptor(this.client.internalCache()));
        interceptors.add(new ConnectInterceptor(this.client));
        if (!this.forWebSocket) {
            interceptors.addAll(this.client.networkInterceptors());
        }
        interceptors.add(new CallServerInterceptor(this.forWebSocket));
        try {
            Response response = new RealInterceptorChain(interceptors, this.transmitter, null, 0, this.originalRequest, this, this.client.connectTimeoutMillis(), this.client.readTimeoutMillis(), this.client.writeTimeoutMillis()).proceed(this.originalRequest);
            if (!this.transmitter.isCanceled()) {
                if (0 == 0) {
                    this.transmitter.noMoreExchanges(null);
                }
                return response;
            }
            Util.closeQuietly(response);
            throw new IOException("Canceled");
        } catch (IOException e) {
            throw this.transmitter.noMoreExchanges(e);
        } catch (Throwable th) {
            if (1 == 0) {
                this.transmitter.noMoreExchanges(null);
            }
            throw th;
        }
    }
}
