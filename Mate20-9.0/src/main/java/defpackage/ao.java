package defpackage;

import com.huawei.android.feature.tasks.Task;

/* renamed from: ao  reason: default package */
final class ao implements Runnable {
    final /* synthetic */ Task G;
    final /* synthetic */ an R;

    ao(an anVar, Task task) {
        this.R = anVar;
        this.G = task;
    }

    public final void run() {
        synchronized (this.R.E) {
            if (this.R.Q != null) {
                this.R.Q.onSuccess(this.G.getResult());
            }
        }
    }
}
