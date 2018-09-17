package com.huawei.android.pushagent;

import android.os.MessageQueue.IdleHandler;

final class g implements IdleHandler {
    final /* synthetic */ b jc;

    g(b bVar) {
        this.jc = bVar;
    }

    public boolean queueIdle() {
        this.jc.zr();
        return true;
    }
}
