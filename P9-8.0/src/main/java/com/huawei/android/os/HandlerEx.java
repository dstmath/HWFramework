package com.huawei.android.os;

import android.os.Handler;

public class HandlerEx extends Handler {
    public static final boolean hasCallbacks(Handler handler, Runnable r) {
        return handler.hasCallbacks(r);
    }
}
