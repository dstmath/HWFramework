package com.huawei.android.os;

import android.os.AsyncResult;
import android.os.Message;

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

    public void setUserObj(Object obj) {
        this.mResult.userObj = obj;
    }

    public static AsyncResultEx forMessage(Message m, Object r, Throwable ex) {
        return from(AsyncResult.forMessage(m, r, ex));
    }
}
