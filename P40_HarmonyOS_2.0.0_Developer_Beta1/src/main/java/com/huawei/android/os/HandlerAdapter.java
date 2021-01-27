package com.huawei.android.os;

import android.os.Handler;
import android.os.Looper;

public class HandlerAdapter extends Handler {
    public HandlerAdapter(Looper looper, Handler.Callback callback, boolean async) {
        super(looper, callback, async);
    }
}
