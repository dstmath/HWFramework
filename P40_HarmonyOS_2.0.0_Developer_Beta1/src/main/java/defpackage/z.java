package defpackage;

import com.huawei.android.feature.tasks.Task;

/* renamed from: z  reason: default package */
final class z implements Runnable {
    final /* synthetic */ y G;
    final /* synthetic */ Task u;

    z(y yVar, Task task) {
        this.G = yVar;
        this.u = task;
    }

    @Override // java.lang.Runnable
    public final void run() {
        synchronized (this.G.s) {
            if (this.G.F != null) {
                this.G.F.onSuccess((Object) this.u.getResult());
            }
        }
    }
}
