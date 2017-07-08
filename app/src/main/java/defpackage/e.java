package defpackage;

import android.os.MessageQueue.IdleHandler;

/* renamed from: e */
class e implements IdleHandler {
    final /* synthetic */ d n;

    e(d dVar) {
        this.n = dVar;
    }

    public boolean queueIdle() {
        if (this.n.m.isHeld()) {
            this.n.m.release();
        }
        return true;
    }
}
