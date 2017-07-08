package defpackage;

import android.content.Intent;

/* renamed from: f */
final class f implements Runnable {
    private Intent mIntent;
    final /* synthetic */ d n;
    private o o;

    private f(d dVar, o oVar, Intent intent) {
        this.n = dVar;
        this.o = oVar;
        this.mIntent = intent;
    }

    public void run() {
        try {
            this.o.onReceive(this.n.mContext, this.mIntent);
        } catch (Throwable e) {
            aw.d("PushLog2828", "ReceiverDispatcher: call Receiver:" + this.o.getClass().getSimpleName() + ", intent:" + this.mIntent + " failed:" + e.toString(), e);
        }
    }
}
