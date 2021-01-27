package defpackage;

import com.huawei.android.feature.tasks.Task;

/* renamed from: v  reason: default package */
final class v implements Runnable {
    final /* synthetic */ Task u;
    final /* synthetic */ u y;

    v(u uVar, Task task) {
        this.y = uVar;
        this.u = task;
    }

    @Override // java.lang.Runnable
    public final void run() {
        synchronized (this.y.s) {
            if (this.y.x != null) {
                this.y.x.onFailure(this.u.getException());
            }
        }
    }
}
