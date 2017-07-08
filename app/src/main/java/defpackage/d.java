package defpackage;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.huawei.android.pushagent.PushService;

/* renamed from: d */
public class d extends Thread {
    private MessageQueue l;
    private WakeLock m;
    private Context mContext;
    public Handler mHandler;

    public d(Context context) {
        super("ReceiverDispatcher");
        this.mContext = context;
        this.m = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "eventloop");
    }

    public void a(o oVar, Intent intent) {
        if (this.mHandler == null) {
            aw.e("PushLog2828", "ReceiverDispatcher: the handler is null");
            PushService.c().stopService();
            return;
        }
        try {
            if (!this.m.isHeld()) {
                this.m.acquire();
            }
            if (!this.mHandler.postDelayed(new f(this, oVar, intent, null), 1)) {
                aw.w("PushLog2828", "postDelayed runnable error");
                throw new Exception("postDelayed runnable error");
            }
        } catch (Throwable e) {
            try {
                aw.d("PushLog2828", "dispatchIntent error", e);
                if (this.m.isHeld()) {
                    aw.i("PushLog2828", "release wakelock after dispatchIntent error");
                    this.m.release();
                }
            } catch (Throwable e2) {
                aw.d("PushLog2828", "release eventLooper wakelock error", e2);
            }
        }
    }

    public void run() {
        try {
            Looper.prepare();
            this.mHandler = new Handler();
            this.l = Looper.myQueue();
            this.l.addIdleHandler(new e(this));
            Looper.loop();
            if (this.m != null && this.m.isHeld()) {
                this.m.release();
            }
        } catch (Throwable th) {
            if (this.m != null && this.m.isHeld()) {
                this.m.release();
            }
        }
    }
}
