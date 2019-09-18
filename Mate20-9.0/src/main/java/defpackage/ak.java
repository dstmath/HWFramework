package defpackage;

import com.huawei.android.feature.tasks.Task;

/* renamed from: ak  reason: default package */
final class ak implements Runnable {
    final /* synthetic */ Task G;
    final /* synthetic */ aj J;

    ak(aj ajVar, Task task) {
        this.J = ajVar;
        this.G = task;
    }

    public final void run() {
        synchronized (this.J.E) {
            if (this.J.I != null) {
                this.J.I.onFailure(this.G.getException());
            }
        }
    }
}
