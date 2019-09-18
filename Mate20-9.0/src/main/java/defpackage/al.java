package defpackage;

import com.huawei.android.feature.tasks.OnCompleteListener;
import com.huawei.android.feature.tasks.OnFailureListener;
import com.huawei.android.feature.tasks.OnSuccessListener;
import com.huawei.android.feature.tasks.RuntimeExecutionException;
import com.huawei.android.feature.tasks.Task;
import com.huawei.android.feature.tasks.TaskExecutors;
import java.util.concurrent.Executor;

/* renamed from: al  reason: default package */
public final class al<TResult> extends Task<TResult> {
    public final Object E = new Object();
    public final am<TResult> K = new am<>();
    public boolean L;
    public TResult M;
    private Exception N;

    private void a() {
        synchronized (this.E) {
            if (this.L) {
                this.K.b(this);
            }
        }
    }

    public final Task<TResult> addOnCompleteListener(OnCompleteListener<TResult> onCompleteListener) {
        return addOnCompleteListener(TaskExecutors.MAIN_THREAD, onCompleteListener);
    }

    public final Task<TResult> addOnCompleteListener(Executor executor, OnCompleteListener<TResult> onCompleteListener) {
        this.K.a(new af(executor, onCompleteListener));
        a();
        return this;
    }

    public final Task<TResult> addOnFailureListener(OnFailureListener onFailureListener) {
        return addOnFailureListener(TaskExecutors.MAIN_THREAD, onFailureListener);
    }

    public final Task<TResult> addOnFailureListener(Executor executor, OnFailureListener onFailureListener) {
        this.K.a(new aj(executor, onFailureListener));
        a();
        return this;
    }

    public final Task<TResult> addOnSuccessListener(OnSuccessListener<? super TResult> onSuccessListener) {
        return addOnSuccessListener(TaskExecutors.MAIN_THREAD, onSuccessListener);
    }

    public final Task<TResult> addOnSuccessListener(Executor executor, OnSuccessListener<? super TResult> onSuccessListener) {
        this.K.a(new an(executor, onSuccessListener));
        a();
        return this;
    }

    public final Exception getException() {
        Exception exc;
        synchronized (this.E) {
            exc = this.N;
        }
        return exc;
    }

    public final TResult getResult() {
        TResult tresult;
        synchronized (this.E) {
            if (!this.L) {
                throw new IllegalStateException("Task is not yet complete");
            } else if (this.N != null) {
                throw new RuntimeExecutionException(this.N);
            } else {
                tresult = this.M;
            }
        }
        return tresult;
    }

    public final boolean isComplete() {
        boolean z;
        synchronized (this.E) {
            z = this.L;
        }
        return z;
    }

    public final boolean isSuccessful() {
        boolean z;
        synchronized (this.E) {
            z = this.L && this.N == null;
        }
        return z;
    }

    public final boolean notifyException(Exception exc) {
        if (exc == null) {
            return false;
        }
        synchronized (this.E) {
            if (this.L) {
                return false;
            }
            this.L = true;
            this.N = exc;
            this.K.b(this);
            return true;
        }
    }

    public final boolean notifyResult(TResult tresult) {
        boolean z = true;
        synchronized (this.E) {
            if (this.L) {
                z = false;
            } else {
                this.L = true;
                this.M = tresult;
                this.K.b(this);
            }
        }
        return z;
    }
}
