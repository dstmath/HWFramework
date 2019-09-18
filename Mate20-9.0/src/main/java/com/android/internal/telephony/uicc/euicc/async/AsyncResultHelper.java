package com.android.internal.telephony.uicc.euicc.async;

import android.os.Handler;

public final class AsyncResultHelper {
    public static <T> void returnResult(final T result, final AsyncResultCallback<T> callback, Handler handler) {
        if (handler == null) {
            callback.onResult(result);
        } else {
            handler.post(new Runnable() {
                public void run() {
                    AsyncResultCallback.this.onResult(result);
                }
            });
        }
    }

    public static void throwException(final Throwable e, final AsyncResultCallback<?> callback, Handler handler) {
        if (handler == null) {
            callback.onException(e);
        } else {
            handler.post(new Runnable() {
                public void run() {
                    AsyncResultCallback.this.onException(e);
                }
            });
        }
    }

    private AsyncResultHelper() {
    }
}
