package defpackage;

import com.huawei.android.pushagent.PushService;
import java.lang.Thread.UncaughtExceptionHandler;

/* renamed from: a */
public class a implements UncaughtExceptionHandler {
    final /* synthetic */ PushService k;

    public a(PushService pushService) {
        this.k = pushService;
    }

    public void uncaughtException(Thread thread, Throwable th) {
        aw.b(PushService.TAG, "uncaughtException:" + th.toString(), th);
        bl.v(this.k.context, th.toString());
        this.k.stopService();
    }
}
