package com.android.okhttp;

import com.android.okhttp.Interceptor;
import com.android.okhttp.Request;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.NamedRunnable;
import com.android.okhttp.internal.http.HttpEngine;
import com.android.okhttp.internal.http.RequestException;
import com.android.okhttp.internal.http.RouteException;
import com.android.okhttp.internal.http.StreamAllocation;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Call {
    volatile boolean canceled;
    /* access modifiers changed from: private */
    public final OkHttpClient client;
    HttpEngine engine;
    private boolean executed;
    Request originalRequest;

    class ApplicationInterceptorChain implements Interceptor.Chain {
        private final boolean forWebSocket;
        private final int index;
        private final Request request;

        ApplicationInterceptorChain(int index2, Request request2, boolean forWebSocket2) {
            this.index = index2;
            this.request = request2;
            this.forWebSocket = forWebSocket2;
        }

        public Connection connection() {
            return null;
        }

        public Request request() {
            return this.request;
        }

        public Response proceed(Request request2) throws IOException {
            if (this.index >= Call.this.client.interceptors().size()) {
                return Call.this.getResponse(request2, this.forWebSocket);
            }
            Interceptor.Chain chain = new ApplicationInterceptorChain(this.index + 1, request2, this.forWebSocket);
            Interceptor interceptor = Call.this.client.interceptors().get(this.index);
            Response interceptedResponse = interceptor.intercept(chain);
            if (interceptedResponse != null) {
                return interceptedResponse;
            }
            throw new NullPointerException("application interceptor " + interceptor + " returned null");
        }
    }

    final class AsyncCall extends NamedRunnable {
        private final boolean forWebSocket;
        private final Callback responseCallback;

        private AsyncCall(Callback responseCallback2, boolean forWebSocket2) {
            super("OkHttp %s", Call.this.originalRequest.urlString());
            this.responseCallback = responseCallback2;
            this.forWebSocket = forWebSocket2;
        }

        /* access modifiers changed from: package-private */
        public String host() {
            return Call.this.originalRequest.httpUrl().host();
        }

        /* access modifiers changed from: package-private */
        public Request request() {
            return Call.this.originalRequest;
        }

        /* access modifiers changed from: package-private */
        public Object tag() {
            return Call.this.originalRequest.tag();
        }

        /* access modifiers changed from: package-private */
        public void cancel() {
            Call.this.cancel();
        }

        /* access modifiers changed from: package-private */
        public Call get() {
            return Call.this;
        }

        /* access modifiers changed from: protected */
        public void execute() {
            try {
                Response response = Call.this.getResponseWithInterceptorChain(this.forWebSocket);
                if (Call.this.canceled) {
                    this.responseCallback.onFailure(Call.this.originalRequest, new IOException("Canceled"));
                } else {
                    this.responseCallback.onResponse(response);
                }
            } catch (IOException e) {
                if (0 != 0) {
                    Logger logger = Internal.logger;
                    Level level = Level.INFO;
                    logger.log(level, "Callback failure for " + Call.this.toLoggableString(), e);
                } else {
                    this.responseCallback.onFailure(Call.this.engine == null ? Call.this.originalRequest : Call.this.engine.getRequest(), e);
                }
            } catch (Throwable th) {
                Call.this.client.getDispatcher().finished(this);
                throw th;
            }
            Call.this.client.getDispatcher().finished(this);
        }
    }

    protected Call(OkHttpClient client2, Request originalRequest2) {
        this.client = client2.copyWithDefaults();
        this.originalRequest = originalRequest2;
    }

    public Response execute() throws IOException {
        synchronized (this) {
            if (!this.executed) {
                this.executed = true;
            } else {
                throw new IllegalStateException("Already Executed");
            }
        }
        try {
            this.client.getDispatcher().executed(this);
            Response result = getResponseWithInterceptorChain(false);
            if (result != null) {
                return result;
            }
            throw new IOException("Canceled");
        } finally {
            this.client.getDispatcher().finished(this);
        }
    }

    /* access modifiers changed from: package-private */
    public Object tag() {
        return this.originalRequest.tag();
    }

    public void enqueue(Callback responseCallback) {
        enqueue(responseCallback, false);
    }

    /* access modifiers changed from: package-private */
    public void enqueue(Callback responseCallback, boolean forWebSocket) {
        synchronized (this) {
            if (!this.executed) {
                this.executed = true;
            } else {
                throw new IllegalStateException("Already Executed");
            }
        }
        this.client.getDispatcher().enqueue(new AsyncCall(responseCallback, forWebSocket));
    }

    public void cancel() {
        this.canceled = true;
        if (this.engine != null) {
            this.engine.cancel();
        }
    }

    public synchronized boolean isExecuted() {
        return this.executed;
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    /* access modifiers changed from: private */
    public String toLoggableString() {
        String string = this.canceled ? "canceled call" : "call";
        HttpUrl redactedUrl = this.originalRequest.httpUrl().resolve("/...");
        return string + " to " + redactedUrl;
    }

    /* access modifiers changed from: private */
    public Response getResponseWithInterceptorChain(boolean forWebSocket) throws IOException {
        return new ApplicationInterceptorChain(0, this.originalRequest, forWebSocket).proceed(this.originalRequest);
    }

    /* access modifiers changed from: package-private */
    public Response getResponse(Request request, boolean forWebSocket) throws IOException {
        Request request2;
        RequestBody body = request.body();
        if (body != null) {
            Request.Builder requestBuilder = request.newBuilder();
            MediaType contentType = body.contentType();
            if (contentType != null) {
                requestBuilder.header("Content-Type", contentType.toString());
            }
            long contentLength = body.contentLength();
            if (contentLength != -1) {
                requestBuilder.header("Content-Length", Long.toString(contentLength));
                requestBuilder.removeHeader("Transfer-Encoding");
            } else {
                requestBuilder.header("Transfer-Encoding", "chunked");
                requestBuilder.removeHeader("Content-Length");
            }
            request2 = requestBuilder.build();
        } else {
            request2 = request;
        }
        HttpEngine httpEngine = new HttpEngine(this.client, request2, false, false, forWebSocket, null, null, null);
        this.engine = httpEngine;
        int followUpCount = 0;
        Request request3 = request2;
        while (!this.canceled) {
            try {
                this.engine.sendRequest();
                this.engine.readResponse();
                if (0 != 0) {
                    this.engine.close().release();
                }
                Response response = this.engine.getResponse();
                Request followUp = this.engine.followUpRequest();
                if (followUp == null) {
                    if (!forWebSocket) {
                        this.engine.releaseStreamAllocation();
                    }
                    return response;
                }
                StreamAllocation streamAllocation = this.engine.close();
                followUpCount++;
                if (followUpCount <= 20) {
                    if (!this.engine.sameConnection(followUp.httpUrl())) {
                        streamAllocation.release();
                        streamAllocation = null;
                    }
                    Request request4 = followUp;
                    HttpEngine httpEngine2 = new HttpEngine(this.client, request4, false, false, forWebSocket, streamAllocation, null, response);
                    this.engine = httpEngine2;
                    Request request5 = request4;
                } else {
                    streamAllocation.release();
                    throw new ProtocolException("Too many follow-up requests: " + followUpCount);
                }
            } catch (RequestException e) {
                throw e.getCause();
            } catch (RouteException e2) {
                HttpEngine retryEngine = this.engine.recover(e2);
                if (retryEngine != null) {
                    this.engine = retryEngine;
                    if (0 == 0) {
                    }
                    this.engine.close().release();
                } else {
                    throw e2.getLastConnectException();
                }
            } catch (IOException e3) {
                HttpEngine retryEngine2 = this.engine.recover(e3, null);
                if (retryEngine2 != null) {
                    this.engine = retryEngine2;
                    if (0 != 0) {
                        this.engine.close().release();
                    }
                } else {
                    throw e3;
                }
            } catch (Throwable th) {
                if (1 != 0) {
                    this.engine.close().release();
                }
                throw th;
            }
        }
        this.engine.releaseStreamAllocation();
        throw new IOException("Canceled");
    }
}
