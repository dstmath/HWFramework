package defpackage;

import com.huawei.android.feature.tasks.OnSuccessListener;
import com.huawei.android.feature.tasks.Task;
import java.util.concurrent.Executor;

/* renamed from: an  reason: default package */
final class an<TResult> implements ae<TResult> {
    final Object E = new Object();
    OnSuccessListener<? super TResult> Q;
    private final Executor mExecutor;

    public an(Executor executor, OnSuccessListener<? super TResult> onSuccessListener) {
        this.mExecutor = executor;
        this.Q = onSuccessListener;
    }

    public final void a(Task<TResult> task) {
        if (task.isSuccessful()) {
            synchronized (this.E) {
                if (this.Q != null) {
                    this.mExecutor.execute(new ao(this, task));
                }
            }
        }
    }
}
