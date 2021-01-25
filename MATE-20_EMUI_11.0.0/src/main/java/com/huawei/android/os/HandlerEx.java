package com.huawei.android.os;

import android.os.Handler;
import android.os.Looper;
import com.huawei.annotation.HwSystemApi;

public class HandlerEx extends Handler {
    public HandlerEx() {
    }

    public static final boolean hasCallbacks(Handler handler, Runnable r) {
        return handler.hasCallbacks(r);
    }

    @HwSystemApi
    public HandlerEx(Looper looper, Handler.Callback callback, boolean async) {
        super(looper, callback, async);
    }
}
