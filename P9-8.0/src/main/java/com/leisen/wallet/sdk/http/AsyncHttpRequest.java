package com.leisen.wallet.sdk.http;

import android.util.Log;
import com.leisen.wallet.sdk.util.LogUtil;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

public class AsyncHttpRequest implements Runnable {
    private boolean cancelIsNotified = false;
    private int executionCount;
    private final AbstractHttpClient httpClient;
    private final HttpContext httpContext;
    private final HttpUriRequest httpUriRequest;
    private boolean isCancelled = false;
    private boolean isFinished = false;
    private final ResponseHandlerInterface responseHandler;

    public AsyncHttpRequest(AbstractHttpClient httpClient, HttpContext httpContext, HttpUriRequest httpUriRequest, ResponseHandlerInterface responseHandler) {
        this.httpClient = httpClient;
        this.httpContext = httpContext;
        this.httpUriRequest = httpUriRequest;
        this.responseHandler = responseHandler;
    }

    public void run() {
        if (!isCancelled()) {
            if (this.responseHandler != null) {
                this.responseHandler.sendStartMessage();
            }
            if (!isCancelled()) {
                try {
                    makeRequestWithRetries();
                } catch (Exception e) {
                    if (isCancelled() || this.responseHandler == null) {
                        Log.e("AsyncHttpRequest", "makeRequestWithRetries returned error, but handler is null" + e);
                    } else {
                        this.responseHandler.sendFailureMessage(0, null, null, e);
                    }
                }
                if (!isCancelled()) {
                    if (this.responseHandler != null) {
                        this.responseHandler.sendFinishMessage();
                    }
                    this.isFinished = true;
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:30:0x0080 A:{Catch:{ Exception -> 0x00b5 }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0080 A:{Catch:{ Exception -> 0x00b5 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void makeRequestWithRetries() throws IOException {
        IOException cause;
        int i;
        Exception e;
        boolean retry = true;
        HttpRequestRetryHandler retryHandler = this.httpClient.getHttpRequestRetryHandler();
        IOException cause2 = null;
        while (retry) {
            try {
                makeRequest();
                return;
            } catch (UnknownHostException e2) {
                try {
                    cause = new IOException("UnknownHostException exception:" + e2.getMessage());
                    try {
                        if (this.executionCount > 0) {
                            i = this.executionCount + 1;
                            this.executionCount = i;
                            if (retryHandler.retryRequest(cause, i, this.httpContext)) {
                                retry = true;
                                if (retry && this.responseHandler != null) {
                                    this.responseHandler.sendRetryMessage(this.executionCount);
                                    cause2 = cause;
                                } else {
                                    cause2 = cause;
                                }
                            }
                        }
                        retry = false;
                        if (retry) {
                            this.responseHandler.sendRetryMessage(this.executionCount);
                            cause2 = cause;
                        }
                        cause2 = cause;
                    } catch (Exception e3) {
                        e = e3;
                    }
                } catch (Exception e4) {
                    e = e4;
                    cause = cause2;
                }
            } catch (NullPointerException e5) {
                cause = new IOException("NPE in HttpClient: " + e5.getMessage());
                i = this.executionCount + 1;
                this.executionCount = i;
                retry = retryHandler.retryRequest(cause, i, this.httpContext);
                if (retry) {
                }
                cause2 = cause;
            } catch (IOException e6) {
                if (!isCancelled()) {
                    cause = e6;
                    i = this.executionCount + 1;
                    this.executionCount = i;
                    retry = retryHandler.retryRequest(e6, i, this.httpContext);
                    if (retry) {
                    }
                    cause2 = cause;
                } else {
                    return;
                }
            }
        }
        cause = cause2;
        throw cause;
        Log.e("AsyncHttpRequest", "Unhandled exception origin cause", e);
        cause = new IOException("Unhandled exception: " + e.getMessage());
        throw cause;
    }

    private void makeRequest() throws IOException {
        if (!isCancelled()) {
            if (this.httpUriRequest.getURI().getScheme() != null) {
                LogUtil.e("AysncHttpClient", "==>get response before");
                HttpResponse httpResponse = this.httpClient.execute(this.httpUriRequest);
                LogUtil.e("AysncHttpClient", "==>get response after" + httpResponse.getStatusLine().getStatusCode());
                if (!(isCancelled() || this.responseHandler == null)) {
                    this.responseHandler.sendResponseMessage(httpResponse);
                }
                return;
            }
            throw new MalformedURLException("No valid URI scheme was provided");
        }
    }

    public boolean isCancelled() {
        if (this.isCancelled) {
            sendCancelNotification();
        }
        return this.isCancelled;
    }

    private synchronized void sendCancelNotification() {
        if (!this.isFinished) {
            if (this.isCancelled && !this.cancelIsNotified) {
                this.cancelIsNotified = true;
                if (this.responseHandler != null) {
                    this.responseHandler.sendCancelMessage();
                }
            }
        }
    }

    public boolean isDone() {
        return isCancelled() || this.isFinished;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        this.isCancelled = true;
        this.httpUriRequest.abort();
        return isCancelled();
    }
}
