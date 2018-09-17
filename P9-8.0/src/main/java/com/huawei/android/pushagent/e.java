package com.huawei.android.pushagent;

import com.huawei.android.pushagent.utils.d.c;
import com.huawei.android.pushagent.utils.f.a;
import java.lang.Thread.UncaughtExceptionHandler;

final class e implements UncaughtExceptionHandler {
    final /* synthetic */ PushService ja;

    e(PushService pushService) {
        this.ja = pushService;
    }

    public void uncaughtException(Thread thread, Throwable th) {
        c.se(PushService.TAG, "uncaughtException:" + th.toString(), th);
        a.tf(this.ja.ih, th.toString());
        this.ja.stopSelf();
    }
}
