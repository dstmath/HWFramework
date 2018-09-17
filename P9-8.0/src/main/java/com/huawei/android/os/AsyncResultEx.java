package com.huawei.android.os;

import android.os.AsyncResult;

public class AsyncResultEx {
    private AsyncResult mResult;

    private AsyncResultEx(AsyncResult result) {
        this.mResult = result;
    }

    public static AsyncResultEx from(Object result) {
        if (result == null || !(result instanceof AsyncResult)) {
            return null;
        }
        return new AsyncResultEx((AsyncResult) result);
    }

    public Throwable getException() {
        return this.mResult.exception;
    }

    public Object getUserObj() {
        return this.mResult.userObj;
    }

    public Object getResult() {
        return this.mResult.result;
    }
}
