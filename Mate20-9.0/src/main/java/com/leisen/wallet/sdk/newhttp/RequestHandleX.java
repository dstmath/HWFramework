package com.leisen.wallet.sdk.newhttp;

import java.lang.ref.WeakReference;

public class RequestHandleX {
    private final WeakReference<AsyncHttpRequestX> request;

    public RequestHandleX(AsyncHttpRequestX asyncHttpRequest) {
        this.request = new WeakReference<>(asyncHttpRequest);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        AsyncHttpRequestX _request = (AsyncHttpRequestX) this.request.get();
        return _request == null || _request.cancel(mayInterruptIfRunning);
    }

    public boolean isFinished() {
        AsyncHttpRequestX _request = (AsyncHttpRequestX) this.request.get();
        return _request == null || _request.isDone();
    }

    public boolean isCancelled() {
        AsyncHttpRequestX _request = (AsyncHttpRequestX) this.request.get();
        return _request == null || _request.isCancelled();
    }

    public boolean shouldBeGarbageCollected() {
        boolean should = isFinished() || isCancelled();
        if (should) {
            this.request.clear();
        }
        return should;
    }
}
