package defpackage;

import com.huawei.android.feature.tasks.OnFailureListener;
import com.huawei.android.feature.tasks.Task;
import java.util.concurrent.Executor;

/* access modifiers changed from: package-private */
/* renamed from: u  reason: default package */
public final class u<TResult> implements p<TResult> {
    private Executor r;
    final Object s = new Object();
    OnFailureListener x;

    public u(Executor executor, OnFailureListener onFailureListener) {
        this.r = executor;
        this.x = onFailureListener;
    }

    @Override // defpackage.p
    public final void a(Task<TResult> task) {
        if (!task.isSuccessful()) {
            synchronized (this.s) {
                if (this.x != null) {
                    this.r.execute(new v(this, task));
                }
            }
        }
    }
}
