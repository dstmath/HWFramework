package com.android.internal.telephony.uicc.euicc.async;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;

public abstract class AsyncMessageInvocation<Request, Response> implements Handler.Callback {
    /* access modifiers changed from: protected */
    public abstract Response parseResult(AsyncResult asyncResult) throws Throwable;

    /* access modifiers changed from: protected */
    public abstract void sendRequestMessage(Request request, Message message);

    public final void invoke(Request request, AsyncResultCallback<Response> resultCallback, Handler handler) {
        sendRequestMessage(request, new Handler(handler.getLooper(), this).obtainMessage(0, resultCallback));
    }

    public boolean handleMessage(Message msg) {
        AsyncResult result = (AsyncResult) msg.obj;
        AsyncResultCallback<Response> resultCallback = (AsyncResultCallback) result.userObj;
        try {
            resultCallback.onResult(parseResult(result));
            return true;
        } catch (Throwable t) {
            resultCallback.onException(t);
            return true;
        }
    }
}
