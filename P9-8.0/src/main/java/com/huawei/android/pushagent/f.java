package com.huawei.android.pushagent;

final class f implements Runnable {
    final /* synthetic */ PushService jb;

    f(PushService pushService) {
        this.jb = pushService;
    }

    public void run() {
        this.jb.zf();
    }
}
