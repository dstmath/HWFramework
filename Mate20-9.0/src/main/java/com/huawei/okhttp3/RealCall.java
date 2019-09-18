package com.huawei.okhttp3;

import com.huawei.okhttp3.internal.NamedRunnable;
import com.huawei.okhttp3.internal.cache.CacheInterceptor;
import com.huawei.okhttp3.internal.connection.ConnectInterceptor;
import com.huawei.okhttp3.internal.connection.Http2ConnectionInterceptor;
import com.huawei.okhttp3.internal.connection.StreamAllocation;
import com.huawei.okhttp3.internal.http.BridgeInterceptor;
import com.huawei.okhttp3.internal.http.CallServerInterceptor;
import com.huawei.okhttp3.internal.http.RealInterceptorChain;
import com.huawei.okhttp3.internal.http.RetryAndFollowUpInterceptor;
import com.huawei.okhttp3.internal.platform.Platform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class RealCall implements Call {
    final OkHttpClient client;
    /* access modifiers changed from: private */
    public EventListener eventListener;
    private boolean executed;
    final boolean forWebSocket;
    final Request originalRequest;
    final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;

    final class AsyncCall extends NamedRunnable {
        private boolean forCreateConnectionOnly = false;
        private final Callback responseCallback;

        AsyncCall(Callback responseCallback2) {
            super("OkHttp %s", RealCall.this.redactedUrl());
            this.responseCallback = responseCallback2;
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
        public OkHttpClient client() {
            return RealCall.this.client;
        }

        /* access modifiers changed from: package-private */
        public boolean forCreateConnectionOnly() {
            return this.forCreateConnectionOnly;
        }

        public void setForCreateConnectionOnly() {
            this.forCreateConnectionOnly = true;
        }

        /* access modifiers changed from: protected */
        public void execute() {
            Response response;
            try {
                if (this.forCreateConnectionOnly) {
                    response = RealCall.this.getResponseForCreateConnectionOnly();
                } else {
                    response = RealCall.this.getResponseWithInterceptorChain();
                }
                if (RealCall.this.retryAndFollowUpInterceptor.isCanceled()) {
                    this.responseCallback.onFailure(RealCall.this, new IOException("Canceled"));
                } else {
                    this.responseCallback.onResponse(RealCall.this, response);
                }
            } catch (IOException e) {
                if (0 != 0) {
                    Platform platform = Platform.get();
                    platform.log(4, "Callback failure for " + RealCall.this.toLoggableString(), e);
                } else {
                    RealCall.this.eventListener.callFailed(RealCall.this, e);
                    this.responseCallback.onFailure(RealCall.this, e);
                }
            } catch (Exception e2) {
                if (0 != 0) {
                    Platform platform2 = Platform.get();
                    platform2.log(4, "Callback failure for " + RealCall.this.toLoggableString(), e2);
                } else {
                    RealCall.this.eventListener.callFailed(RealCall.this, new IOException(e2));
                    this.responseCallback.onFailure(RealCall.this, new IOException(e2));
                }
            } catch (Throwable th) {
                RealCall.this.client.dispatcher().finished(this);
                throw th;
            }
            RealCall.this.client.dispatcher().finished(this);
        }
    }

    private RealCall(OkHttpClient client2, Request originalRequest2, boolean forWebSocket2) {
        this.client = client2;
        this.originalRequest = originalRequest2;
        this.forWebSocket = forWebSocket2;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(client2, forWebSocket2);
    }

    static RealCall newRealCall(OkHttpClient client2, Request originalRequest2, boolean forWebSocket2) {
        RealCall call = new RealCall(client2, originalRequest2, forWebSocket2);
        call.eventListener = client2.eventListenerFactory().create(call);
        return call;
    }

    public Request request() {
        return this.originalRequest;
    }

    public Response execute() throws IOException {
        synchronized (this) {
            if (!this.executed) {
                this.executed = true;
            } else {
                throw new IllegalStateException("Already Executed");
            }
        }
        captureCallStackTrace();
        this.eventListener.callStart(this);
        try {
            this.client.dispatcher().executed(this);
            Response result = getResponseWithInterceptorChain();
            if (result != null) {
                this.client.dispatcher().finished(this);
                return result;
            }
            throw new IOException("Canceled");
        } catch (IOException ioe) {
            this.eventListener.callFailed(this, ioe);
            throw ioe;
        } catch (Exception e) {
            this.eventListener.callFailed(this, new IOException(e));
            throw new IOException(e);
        } catch (Throwable th) {
            this.client.dispatcher().finished(this);
            throw th;
        }
    }

    private void captureCallStackTrace() {
        this.retryAndFollowUpInterceptor.setCallStackTrace(Platform.get().getStackTraceForCloseable("response.body().close()"));
    }

    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (!this.executed) {
                this.executed = true;
            } else {
                throw new IllegalStateException("Already Executed");
            }
        }
        captureCallStackTrace();
        this.eventListener.callStart(this);
        this.client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    public void cancel() {
        this.retryAndFollowUpInterceptor.cancel();
    }

    public synchronized boolean isExecuted() {
        return this.executed;
    }

    public boolean isCanceled() {
        return this.retryAndFollowUpInterceptor.isCanceled();
    }

    public RealCall clone() {
        return newRealCall(this.client, this.originalRequest, this.forWebSocket);
    }

    /* access modifiers changed from: package-private */
    public StreamAllocation streamAllocation() {
        return this.retryAndFollowUpInterceptor.streamAllocation();
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
        interceptors.add(this.retryAndFollowUpInterceptor);
        interceptors.add(new BridgeInterceptor(this.client.cookieJar()));
        interceptors.add(new CacheInterceptor(this.client.internalCache()));
        interceptors.add(new ConnectInterceptor(this.client));
        if (!this.forWebSocket) {
            interceptors.addAll(this.client.networkInterceptors());
        }
        interceptors.add(new CallServerInterceptor(this.forWebSocket));
        RealInterceptorChain realInterceptorChain = new RealInterceptorChain(interceptors, null, null, null, 0, this.originalRequest, this, this.eventListener, this.client.connectTimeoutMillis(), this.client.readTimeoutMillis(), this.client.writeTimeoutMillis());
        return realInterceptorChain.proceed(this.originalRequest);
    }

    /* access modifiers changed from: package-private */
    public Response getResponseForCreateConnectionOnly() throws IOException {
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.add(this.retryAndFollowUpInterceptor);
        interceptors.add(new Http2ConnectionInterceptor(this.client));
        RealInterceptorChain realInterceptorChain = new RealInterceptorChain(interceptors, null, null, null, 0, this.originalRequest, this, this.eventListener, this.client.connectTimeoutMillis(), this.client.readTimeoutMillis(), this.client.writeTimeoutMillis());
        return realInterceptorChain.proceed(this.originalRequest);
    }
}
