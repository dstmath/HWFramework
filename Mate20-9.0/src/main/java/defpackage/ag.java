package defpackage;

import com.huawei.android.feature.tasks.Task;

/* renamed from: ag  reason: default package */
final class ag implements Runnable {
    final /* synthetic */ Task G;
    final /* synthetic */ af H;

    ag(af afVar, Task task) {
        this.H = afVar;
        this.G = task;
    }

    public final void run() {
        synchronized (this.H.E) {
            if (this.H.F != null) {
                this.H.F.onComplete(this.G);
            }
        }
    }
}
