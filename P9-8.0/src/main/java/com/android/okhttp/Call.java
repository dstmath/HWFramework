package com.android.okhttp;

import com.android.okhttp.Interceptor.Chain;
import com.android.okhttp.Request.Builder;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.NamedRunnable;
import com.android.okhttp.internal.http.HttpEngine;
import com.android.okhttp.internal.http.RequestException;
import com.android.okhttp.internal.http.RouteException;
import com.android.okhttp.internal.http.StreamAllocation;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.logging.Level;

public class Call {
    volatile boolean canceled;
    private final OkHttpClient client;
    HttpEngine engine;
    private boolean executed;
    Request originalRequest;

    class ApplicationInterceptorChain implements Chain {
        private final boolean forWebSocket;
        private final int index;
        private final Request request;

        ApplicationInterceptorChain(int index, Request request, boolean forWebSocket) {
            this.index = index;
            this.request = request;
            this.forWebSocket = forWebSocket;
        }

        public Connection connection() {
            return null;
        }

        public Request request() {
            return this.request;
        }

        public Response proceed(Request request) throws IOException {
            if (this.index >= Call.this.client.interceptors().size()) {
                return Call.this.getResponse(request, this.forWebSocket);
            }
            Interceptor interceptor = (Interceptor) Call.this.client.interceptors().get(this.index);
            Response interceptedResponse = interceptor.intercept(new ApplicationInterceptorChain(this.index + 1, request, this.forWebSocket));
            if (interceptedResponse != null) {
                return interceptedResponse;
            }
            throw new NullPointerException("application interceptor " + interceptor + " returned null");
        }
    }

    final class AsyncCall extends NamedRunnable {
        private final boolean forWebSocket;
        private final Callback responseCallback;

        /* synthetic */ AsyncCall(Call this$0, Callback responseCallback, boolean forWebSocket, AsyncCall -this3) {
            this(responseCallback, forWebSocket);
        }

        private AsyncCall(Callback responseCallback, boolean forWebSocket) {
            super("OkHttp %s", this$0.originalRequest.urlString());
            this.responseCallback = responseCallback;
            this.forWebSocket = forWebSocket;
        }

        String host() {
            return Call.this.originalRequest.httpUrl().host();
        }

        Request request() {
            return Call.this.originalRequest;
        }

        Object tag() {
            return Call.this.originalRequest.tag();
        }

        void cancel() {
            Call.this.cancel();
        }

        Call get() {
            return Call.this;
        }

        protected void execute() {
            boolean signalledCallback = false;
            try {
                Response response = Call.this.getResponseWithInterceptorChain(this.forWebSocket);
                if (Call.this.canceled) {
                    this.responseCallback.onFailure(Call.this.originalRequest, new IOException("Canceled"));
                } else {
                    signalledCallback = true;
                    this.responseCallback.onResponse(response);
                }
                Call.this.client.getDispatcher().finished(this);
            } catch (IOException e) {
                if (signalledCallback) {
                    Internal.logger.log(Level.INFO, "Callback failure for " + Call.this.toLoggableString(), e);
                } else {
                    this.responseCallback.onFailure(Call.this.engine == null ? Call.this.originalRequest : Call.this.engine.getRequest(), e);
                }
                Call.this.client.getDispatcher().finished(this);
            } catch (Throwable th) {
                Call.this.client.getDispatcher().finished(this);
            }
        }
    }

    protected Call(OkHttpClient client, Request originalRequest) {
        this.client = client.copyWithDefaults();
        this.originalRequest = originalRequest;
    }

    public Response execute() throws IOException {
        synchronized (this) {
            if (this.executed) {
                throw new IllegalStateException("Already Executed");
            }
            this.executed = true;
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

    Object tag() {
        return this.originalRequest.tag();
    }

    public void enqueue(Callback responseCallback) {
        enqueue(responseCallback, false);
    }

    void enqueue(Callback responseCallback, boolean forWebSocket) {
        synchronized (this) {
            if (this.executed) {
                throw new IllegalStateException("Already Executed");
            }
            this.executed = true;
        }
        this.client.getDispatcher().enqueue(new AsyncCall(this, responseCallback, forWebSocket, null));
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

    private String toLoggableString() {
        return (this.canceled ? "canceled call" : "call") + " to " + this.originalRequest.httpUrl().resolve("/...");
    }

    private Response getResponseWithInterceptorChain(boolean forWebSocket) throws IOException {
        return new ApplicationInterceptorChain(0, this.originalRequest, forWebSocket).proceed(this.originalRequest);
    }

    Response getResponse(Request request, boolean forWebSocket) throws IOException {
        HttpEngine retryEngine;
        RequestBody body = request.body();
        if (body != null) {
            Builder requestBuilder = request.newBuilder();
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
            request = requestBuilder.build();
        }
        this.engine = new HttpEngine(this.client, request, false, false, forWebSocket, null, null, null);
        int followUpCount = 0;
        while (!this.canceled) {
            try {
                this.engine.sendRequest();
                this.engine.readResponse();
                if (false) {
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
                if (followUpCount > 20) {
                    streamAllocation.release();
                    throw new ProtocolException("Too many follow-up requests: " + followUpCount);
                }
                if (!this.engine.sameConnection(followUp.httpUrl())) {
                    streamAllocation.release();
                    streamAllocation = null;
                }
                request = followUp;
                this.engine = new HttpEngine(this.client, followUp, false, false, forWebSocket, streamAllocation, null, response);
            } catch (RequestException e) {
                throw e.getCause();
            } catch (RouteException e2) {
                retryEngine = this.engine.recover(e2);
                if (retryEngine != null) {
                    this.engine = retryEngine;
                    if (false) {
                        this.engine.close().release();
                    }
                } else {
                    throw e2.getLastConnectException();
                }
            } catch (IOException e3) {
                retryEngine = this.engine.recover(e3, null);
                if (retryEngine != null) {
                    this.engine = retryEngine;
                    if (false) {
                        this.engine.close().release();
                    }
                } else {
                    throw e3;
                }
            } catch (Throwable th) {
                if (true) {
                    this.engine.close().release();
                }
            }
        }
        this.engine.releaseStreamAllocation();
        throw new IOException("Canceled");
    }
}
