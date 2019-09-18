package android.telecom;

import android.os.Handler;

class CallbackRecord<T> {
    private final T mCallback;
    private final Handler mHandler;

    public CallbackRecord(T callback, Handler handler) {
        this.mCallback = callback;
        this.mHandler = handler;
    }

    public T getCallback() {
        return this.mCallback;
    }

    public Handler getHandler() {
        return this.mHandler;
    }
}
