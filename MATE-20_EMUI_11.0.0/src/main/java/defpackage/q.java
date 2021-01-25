package defpackage;

import com.huawei.android.feature.tasks.OnCompleteListener;
import com.huawei.android.feature.tasks.Task;
import java.util.concurrent.Executor;

/* access modifiers changed from: package-private */
/* renamed from: q  reason: default package */
public final class q<TResult> implements p<TResult> {
    private final Executor r;
    final Object s = new Object();
    OnCompleteListener<TResult> t;

    public q(Executor executor, OnCompleteListener<TResult> onCompleteListener) {
        this.r = executor;
        this.t = onCompleteListener;
    }

    @Override // defpackage.p
    public final void a(Task<TResult> task) {
        synchronized (this.s) {
            if (this.t != null) {
                this.r.execute(new r(this, task));
            }
        }
    }
}
