package defpackage;

import com.huawei.android.feature.tasks.OnCompleteListener;
import com.huawei.android.feature.tasks.OnFailureListener;
import com.huawei.android.feature.tasks.OnSuccessListener;
import com.huawei.android.feature.tasks.RuntimeExecutionException;
import com.huawei.android.feature.tasks.Task;
import com.huawei.android.feature.tasks.TaskExecutors;
import java.util.concurrent.Executor;

/* renamed from: w  reason: default package */
public final class w<TResult> extends Task<TResult> {
    public boolean A;
    public TResult B;
    private Exception C;
    public final Object s = new Object();
    public final x<TResult> z = new x<>();

    private void a() {
        synchronized (this.s) {
            if (this.A) {
                this.z.b(this);
            }
        }
    }

    @Override // com.huawei.android.feature.tasks.Task
    public final Task<TResult> addOnCompleteListener(OnCompleteListener<TResult> onCompleteListener) {
        return addOnCompleteListener(TaskExecutors.MAIN_THREAD, onCompleteListener);
    }

    @Override // com.huawei.android.feature.tasks.Task
    public final Task<TResult> addOnCompleteListener(Executor executor, OnCompleteListener<TResult> onCompleteListener) {
        this.z.a(new q(executor, onCompleteListener));
        a();
        return this;
    }

    @Override // com.huawei.android.feature.tasks.Task
    public final Task<TResult> addOnFailureListener(OnFailureListener onFailureListener) {
        return addOnFailureListener(TaskExecutors.MAIN_THREAD, onFailureListener);
    }

    @Override // com.huawei.android.feature.tasks.Task
    public final Task<TResult> addOnFailureListener(Executor executor, OnFailureListener onFailureListener) {
        this.z.a(new u(executor, onFailureListener));
        a();
        return this;
    }

    @Override // com.huawei.android.feature.tasks.Task
    public final Task<TResult> addOnSuccessListener(OnSuccessListener<? super TResult> onSuccessListener) {
        return addOnSuccessListener(TaskExecutors.MAIN_THREAD, onSuccessListener);
    }

    @Override // com.huawei.android.feature.tasks.Task
    public final Task<TResult> addOnSuccessListener(Executor executor, OnSuccessListener<? super TResult> onSuccessListener) {
        this.z.a(new y(executor, onSuccessListener));
        a();
        return this;
    }

    @Override // com.huawei.android.feature.tasks.Task
    public final Exception getException() {
        Exception exc;
        synchronized (this.s) {
            exc = this.C;
        }
        return exc;
    }

    @Override // com.huawei.android.feature.tasks.Task
    public final TResult getResult() {
        TResult tresult;
        synchronized (this.s) {
            if (!this.A) {
                throw new IllegalStateException("Task is not yet complete");
            } else if (this.C != null) {
                throw new RuntimeExecutionException(this.C);
            } else {
                tresult = this.B;
            }
        }
        return tresult;
    }

    @Override // com.huawei.android.feature.tasks.Task
    public final boolean isComplete() {
        boolean z2;
        synchronized (this.s) {
            z2 = this.A;
        }
        return z2;
    }

    @Override // com.huawei.android.feature.tasks.Task
    public final boolean isSuccessful() {
        boolean z2;
        synchronized (this.s) {
            z2 = this.A && this.C == null;
        }
        return z2;
    }

    public final boolean notifyException(Exception exc) {
        if (exc == null) {
            return false;
        }
        synchronized (this.s) {
            if (this.A) {
                return false;
            }
            this.A = true;
            this.C = exc;
            this.z.b(this);
            return true;
        }
    }

    public final boolean notifyResult(TResult tresult) {
        boolean z2 = true;
        synchronized (this.s) {
            if (this.A) {
                z2 = false;
            } else {
                this.A = true;
                this.B = tresult;
                this.z.b(this);
            }
        }
        return z2;
    }
}
