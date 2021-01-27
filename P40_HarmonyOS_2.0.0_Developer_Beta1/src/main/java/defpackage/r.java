package defpackage;

import com.huawei.android.feature.tasks.Task;

/* renamed from: r  reason: default package */
final class r implements Runnable {
    final /* synthetic */ Task u;
    final /* synthetic */ q v;

    r(q qVar, Task task) {
        this.v = qVar;
        this.u = task;
    }

    @Override // java.lang.Runnable
    public final void run() {
        synchronized (this.v.s) {
            if (this.v.t != null) {
                this.v.t.onComplete(this.u);
            }
        }
    }
}
