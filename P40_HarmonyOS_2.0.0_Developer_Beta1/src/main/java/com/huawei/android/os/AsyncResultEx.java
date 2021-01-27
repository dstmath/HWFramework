package com.huawei.android.os;

import android.os.AsyncResult;
import android.os.Message;
import com.android.internal.telephony.uicc.IccException;
import com.huawei.annotation.HwSystemApi;

public class AsyncResultEx {
    private AsyncResult mResult;

    @HwSystemApi
    public AsyncResultEx() {
    }

    @HwSystemApi
    public AsyncResultEx(Object userObject, Object result, Throwable exception) {
        this.mResult = new AsyncResult(userObject, result, exception);
    }

    private void setAsyncResult(AsyncResult result) {
        this.mResult = result;
    }

    public AsyncResult getAsyncResult() {
        return this.mResult;
    }

    public static AsyncResultEx from(Object result) {
        if (result == null || !(result instanceof AsyncResult)) {
            return null;
        }
        AsyncResultEx resultEx = new AsyncResultEx();
        resultEx.setAsyncResult((AsyncResult) result);
        return resultEx;
    }

    public Throwable getException() {
        AsyncResult asyncResult = this.mResult;
        if (asyncResult != null) {
            return asyncResult.exception;
        }
        return null;
    }

    @HwSystemApi
    public void setException(Throwable exception) {
        AsyncResult asyncResult = this.mResult;
        if (asyncResult != null) {
            asyncResult.exception = exception;
        }
    }

    public Object getUserObj() {
        AsyncResult asyncResult = this.mResult;
        if (asyncResult != null) {
            return asyncResult.userObj;
        }
        return null;
    }

    public Object getResult() {
        AsyncResult asyncResult = this.mResult;
        if (asyncResult != null) {
            return asyncResult.result;
        }
        return null;
    }

    @HwSystemApi
    public void setResult(Object obj) {
        AsyncResult asyncResult = this.mResult;
        if (asyncResult != null) {
            asyncResult.result = obj;
        }
    }

    public void setUserObj(Object obj) {
        AsyncResult asyncResult = this.mResult;
        if (asyncResult != null) {
            asyncResult.userObj = obj;
        }
    }

    public static AsyncResultEx forMessage(Message message, Object carryObject, Throwable ex) {
        return from(AsyncResult.forMessage(message, carryObject, ex));
    }

    @HwSystemApi
    public static AsyncResultEx forMessage(Message message) {
        return from(AsyncResult.forMessage(message));
    }

    @HwSystemApi
    public boolean isInstanceIccException() {
        AsyncResult asyncResult = this.mResult;
        if (asyncResult != null) {
            return asyncResult.exception instanceof IccException;
        }
        return false;
    }
}
