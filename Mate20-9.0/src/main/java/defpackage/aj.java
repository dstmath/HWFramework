package defpackage;

import com.huawei.android.feature.tasks.OnFailureListener;
import com.huawei.android.feature.tasks.Task;
import java.util.concurrent.Executor;

/* renamed from: aj  reason: default package */
final class aj<TResult> implements ae<TResult> {
    final Object E = new Object();
    OnFailureListener I;
    private Executor mExecutor;

    public aj(Executor executor, OnFailureListener onFailureListener) {
        this.mExecutor = executor;
        this.I = onFailureListener;
    }

    public final void a(Task<TResult> task) {
        if (!task.isSuccessful()) {
            synchronized (this.E) {
                if (this.I != null) {
                    this.mExecutor.execute(new ak(this, task));
                }
            }
        }
    }
}
