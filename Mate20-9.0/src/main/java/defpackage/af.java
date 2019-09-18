package defpackage;

import com.huawei.android.feature.tasks.OnCompleteListener;
import com.huawei.android.feature.tasks.Task;
import java.util.concurrent.Executor;

/* renamed from: af  reason: default package */
final class af<TResult> implements ae<TResult> {
    final Object E = new Object();
    OnCompleteListener<TResult> F;
    private final Executor mExecutor;

    public af(Executor executor, OnCompleteListener<TResult> onCompleteListener) {
        this.mExecutor = executor;
        this.F = onCompleteListener;
    }

    public final void a(Task<TResult> task) {
        synchronized (this.E) {
            if (this.F != null) {
                this.mExecutor.execute(new ag(this, task));
            }
        }
    }
}
