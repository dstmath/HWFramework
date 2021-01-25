package com.android.server.backup.remote;

import android.app.backup.IBackupCallback;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import com.android.internal.util.Preconditions;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RemoteCall {
    private final RemoteCallable<IBackupCallback> mCallable;
    private final CompletableFuture<RemoteResult> mFuture;
    private final long mTimeoutMs;

    public static RemoteResult execute(RemoteCallable<IBackupCallback> callable, long timeoutMs) throws RemoteException {
        return new RemoteCall(callable, timeoutMs).call();
    }

    public RemoteCall(RemoteCallable<IBackupCallback> callable, long timeoutMs) {
        this(false, callable, timeoutMs);
    }

    public RemoteCall(boolean cancelled, RemoteCallable<IBackupCallback> callable, long timeoutMs) {
        this.mCallable = callable;
        this.mTimeoutMs = timeoutMs;
        this.mFuture = new CompletableFuture<>();
        if (cancelled) {
            cancel();
        }
    }

    public RemoteResult call() throws RemoteException {
        Preconditions.checkState(!Looper.getMainLooper().isCurrentThread(), "Can't call call() on main thread");
        if (!this.mFuture.isDone()) {
            if (this.mTimeoutMs == 0) {
                timeOut();
            } else {
                Handler.getMain().postDelayed(new Runnable() {
                    /* class com.android.server.backup.remote.$$Lambda$RemoteCall$UZaEiTGjS9e2j04YYkGl3Y2ltU4 */

                    @Override // java.lang.Runnable
                    public final void run() {
                        RemoteCall.this.timeOut();
                    }
                }, this.mTimeoutMs);
                this.mCallable.call(new FutureBackupCallback(this.mFuture));
            }
        }
        try {
            return this.mFuture.get();
        } catch (InterruptedException e) {
            return RemoteResult.FAILED_THREAD_INTERRUPTED;
        } catch (ExecutionException e2) {
            throw new IllegalStateException("Future unexpectedly completed with an exception");
        }
    }

    public void cancel() {
        this.mFuture.complete(RemoteResult.FAILED_CANCELLED);
    }

    /* access modifiers changed from: private */
    public void timeOut() {
        this.mFuture.complete(RemoteResult.FAILED_TIMED_OUT);
    }
}
