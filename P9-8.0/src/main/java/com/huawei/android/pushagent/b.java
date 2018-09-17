package com.huawei.android.pushagent;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.huawei.android.pushagent.model.c.e;
import com.huawei.android.pushagent.utils.d.c;

class b extends Thread {
    private static long is = 2000;
    public Handler ir;
    private Context it;
    private MessageQueue iu;
    private WakeLock iv = ((PowerManager) this.it.getSystemService("power")).newWakeLock(1, "eventloop");

    public b(Context context) {
        super("ReceiverDispatcher");
        this.it = context;
    }

    public void run() {
        try {
            Looper.prepare();
            this.ir = new Handler();
            this.iu = Looper.myQueue();
            this.iu.addIdleHandler(new g(this));
            Looper.loop();
            c.sh("PushLog2951", "ReceiverDispatcher thread exit!");
        } catch (Throwable th) {
            c.sf("PushLog2951", c.sm(th));
        } finally {
            zr();
        }
    }

    void zq(e eVar, Intent intent) {
        if (this.ir == null) {
            c.sf("PushLog2951", "ReceiverDispatcher: the handler is null");
            PushService.yy().stopSelf();
            return;
        }
        try {
            if (!this.iv.isHeld()) {
                this.iv.acquire(is);
            }
            if (!this.ir.postDelayed(new c(this, eVar, intent, null), 1)) {
                c.sj("PushLog2951", "postDelayed runnable error");
                throw new Exception("postDelayed runnable error");
            }
        } catch (Throwable e) {
            c.se("PushLog2951", "dispatchIntent error," + e.toString(), e);
            zr();
        }
    }

    private void zr() {
        try {
            if (this.iv != null && this.iv.isHeld()) {
                this.iv.release();
            }
        } catch (Exception e) {
            c.sf("PushLog2951", e.toString());
        }
    }
}
