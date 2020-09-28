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
import com.huawei.okio.AsyncTimeout;
import com.huawei.okio.Timeout;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

final class RealCall implements Call {
    final OkHttpClient client;
    @Nullable
    private EventListener eventListener;
    private boolean executed;
    final boolean forWebSocket;
    final Request originalRequest;
    final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;
    final AsyncTimeout timeout = new AsyncTimeout() {
        /* class com.huawei.okhttp3.RealCall.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // com.huawei.okio.AsyncTimeout
        public void timedOut() {
            RealCall.this.cancel();
        }
    };

    private RealCall(OkHttpClient client2, Request originalRequest2, boolean forWebSocket2) {
        this.client = client2;
        this.originalRequest = originalRequest2;
        this.forWebSocket = forWebSocket2;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(client2, forWebSocket2);
        this.timeout.timeout((long) client2.callTimeoutMillis(), TimeUnit.MILLISECONDS);
    }

    static RealCall newRealCall(OkHttpClient client2, Request originalRequest2, boolean forWebSocket2) {
        RealCall call = new RealCall(client2, originalRequest2, forWebSocket2);
        call.eventListener = client2.eventListenerFactory().create(call);
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
        captureCallStackTrace();
        this.timeout.enter();
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
            IOException ioe2 = timeoutExit(ioe);
            this.eventListener.callFailed(this, ioe2);
            throw ioe2;
        } catch (Exception e) {
            this.eventListener.callFailed(this, new IOException(e));
            throw new IOException(e);
        } catch (Throwable th) {
            this.client.dispatcher().finished(this);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public IOException timeoutExit(@Nullable IOException cause) {
        if (!this.timeout.exit()) {
            return cause;
        }
        InterruptedIOException e = new InterruptedIOException("timeout");
        if (cause != null) {
            e.initCause(cause);
        }
        return e;
    }

    private void captureCallStackTrace() {
        this.retryAndFollowUpInterceptor.setCallStackTrace(Platform.get().getStackTraceForCloseable("response.body().close()"));
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
        captureCallStackTrace();
        this.eventListener.callStart(this);
        this.client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    @Override // com.huawei.okhttp3.Call
    public void cancel() {
        this.retryAndFollowUpInterceptor.cancel();
    }

    @Override // com.huawei.okhttp3.Call
    public Timeout timeout() {
        return this.timeout;
    }

    @Override // com.huawei.okhttp3.Call
    public synchronized boolean isExecuted() {
        return this.executed;
    }

    @Override // com.huawei.okhttp3.Call
    public boolean isCanceled() {
        return this.retryAndFollowUpInterceptor.isCanceled();
    }

    @Override // java.lang.Object, com.huawei.okhttp3.Call
    public RealCall clone() {
        return newRealCall(this.client, this.originalRequest, this.forWebSocket);
    }

    /* access modifiers changed from: package-private */
    public StreamAllocation streamAllocation() {
        return this.retryAndFollowUpInterceptor.streamAllocation();
    }

    final class AsyncCall extends NamedRunnable {
        static final /* synthetic */ boolean $assertionsDisabled = false;
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
        public void executeOn(ExecutorService executorService) {
            try {
                executorService.execute(this);
                if (1 != 0) {
                    return;
                }
            } catch (RejectedExecutionException e) {
                InterruptedIOException ioException = new InterruptedIOException("executor rejected");
                ioException.initCause(e);
                RealCall.this.eventListener.callFailed(RealCall.this, ioException);
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
        @Override // com.huawei.okhttp3.internal.NamedRunnable
        public void execute() {
            Response response;
            RealCall.this.timeout.enter();
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
                IOException e2 = RealCall.this.timeoutExit(e);
                if (0 != 0) {
                    Platform platform = Platform.get();
                    platform.log(4, "Callback failure for " + RealCall.this.toLoggableString(), e2);
                } else {
                    RealCall.this.eventListener.callFailed(RealCall.this, e2);
                    this.responseCallback.onFailure(RealCall.this, e2);
                }
            } catch (Exception e3) {
                if (0 != 0) {
                    Platform platform2 = Platform.get();
                    platform2.log(4, "Callback failure for " + RealCall.this.toLoggableString(), e3);
                } else {
                    RealCall.this.eventListener.callFailed(RealCall.this, new IOException(e3));
                    this.responseCallback.onFailure(RealCall.this, new IOException(e3));
                }
            } catch (Throwable th) {
                RealCall.this.client.dispatcher().finished(this);
                throw th;
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
        interceptors.add(this.retryAndFollowUpInterceptor);
        interceptors.add(new BridgeInterceptor(this.client.cookieJar()));
        interceptors.add(new CacheInterceptor(this.client.internalCache()));
        interceptors.add(new ConnectInterceptor(this.client));
        if (!this.forWebSocket) {
            interceptors.addAll(this.client.networkInterceptors());
        }
        interceptors.add(new CallServerInterceptor(this.forWebSocket));
        return new RealInterceptorChain(interceptors, null, null, null, 0, this.originalRequest, this, this.eventListener, this.client.connectTimeoutMillis(), this.client.readTimeoutMillis(), this.client.writeTimeoutMillis()).proceed(this.originalRequest);
    }

    /* access modifiers changed from: package-private */
    public Response getResponseForCreateConnectionOnly() throws IOException {
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.add(this.retryAndFollowUpInterceptor);
        interceptors.add(new Http2ConnectionInterceptor(this.client));
        return new RealInterceptorChain(interceptors, null, null, null, 0, this.originalRequest, this, this.eventListener, this.client.connectTimeoutMillis(), this.client.readTimeoutMillis(), this.client.writeTimeoutMillis()).proceed(this.originalRequest);
    }
}
