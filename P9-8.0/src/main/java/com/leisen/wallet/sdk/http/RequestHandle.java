package com.leisen.wallet.sdk.http;

import java.lang.ref.WeakReference;

public class RequestHandle {
    private final WeakReference<AsyncHttpRequest> request;

    public RequestHandle(AsyncHttpRequest asyncHttpRequest) {
        this.request = new WeakReference(asyncHttpRequest);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        AsyncHttpRequest _request = (AsyncHttpRequest) this.request.get();
        if (_request == null || _request.cancel(mayInterruptIfRunning)) {
            return true;
        }
        return false;
    }

    public boolean isFinished() {
        AsyncHttpRequest _request = (AsyncHttpRequest) this.request.get();
        if (_request == null || _request.isDone()) {
            return true;
        }
        return false;
    }

    public boolean isCancelled() {
        AsyncHttpRequest _request = (AsyncHttpRequest) this.request.get();
        if (_request == null || _request.isCancelled()) {
            return true;
        }
        return false;
    }

    public boolean shouldBeGarbageCollected() {
        boolean should = false;
        if (isFinished() || isCancelled()) {
            should = true;
        }
        if (should) {
            this.request.clear();
        }
        return should;
    }
}
