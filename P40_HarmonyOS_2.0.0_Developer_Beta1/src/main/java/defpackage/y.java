package defpackage;

import com.huawei.android.feature.tasks.OnSuccessListener;
import com.huawei.android.feature.tasks.Task;
import java.util.concurrent.Executor;

/* access modifiers changed from: package-private */
/* renamed from: y  reason: default package */
public final class y<TResult> implements p<TResult> {
    OnSuccessListener<? super TResult> F;
    private final Executor r;
    final Object s = new Object();

    public y(Executor executor, OnSuccessListener<? super TResult> onSuccessListener) {
        this.r = executor;
        this.F = onSuccessListener;
    }

    @Override // defpackage.p
    public final void a(Task<TResult> task) {
        if (task.isSuccessful()) {
            synchronized (this.s) {
                if (this.F != null) {
                    this.r.execute(new z(this, task));
                }
            }
        }
    }
}
