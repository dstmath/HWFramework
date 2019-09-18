package com.android.server;

import android.os.Build;
import android.util.Slog;
import com.android.internal.util.ConcurrentUtils;
import com.android.internal.util.Preconditions;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SystemServerInitThreadPool {
    private static final boolean IS_DEBUGGABLE = Build.IS_DEBUGGABLE;
    private static final int SHUTDOWN_TIMEOUT_MILLIS = 20000;
    private static final String TAG = SystemServerInitThreadPool.class.getSimpleName();
    private static SystemServerInitThreadPool sInstance;
    private ExecutorService mService = ConcurrentUtils.newFixedThreadPool(4, "system-server-init-thread", -2);

    public static synchronized SystemServerInitThreadPool get() {
        SystemServerInitThreadPool systemServerInitThreadPool;
        synchronized (SystemServerInitThreadPool.class) {
            if (sInstance == null) {
                sInstance = new SystemServerInitThreadPool();
            }
            boolean z = sInstance.mService != null;
            Preconditions.checkState(z, "Cannot get " + TAG + " - it has been shut down");
            systemServerInitThreadPool = sInstance;
        }
        return systemServerInitThreadPool;
    }

    public Future<?> submit(Runnable runnable, String description) {
        return IS_DEBUGGABLE ? this.mService.submit(new Runnable(description, runnable) {
            private final /* synthetic */ String f$0;
            private final /* synthetic */ Runnable f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run() {
                SystemServerInitThreadPool.lambda$submit$0(this.f$0, this.f$1);
            }
        }) : this.mService.submit(runnable);
    }

    static /* synthetic */ void lambda$submit$0(String description, Runnable runnable) {
        String str = TAG;
        Slog.d(str, "Started executing " + description);
        try {
            runnable.run();
            String str2 = TAG;
            Slog.d(str2, "Finished executing " + description);
        } catch (RuntimeException e) {
            String str3 = TAG;
            Slog.e(str3, "Failure in " + description + ": " + e, e);
            throw e;
        }
    }

    static synchronized void shutdown() {
        synchronized (SystemServerInitThreadPool.class) {
            if (!(sInstance == null || sInstance.mService == null)) {
                sInstance.mService.shutdown();
                try {
                    boolean terminated = sInstance.mService.awaitTermination(20000, TimeUnit.MILLISECONDS);
                    List<Runnable> unstartedRunnables = sInstance.mService.shutdownNow();
                    if (terminated) {
                        sInstance.mService = null;
                        Slog.d(TAG, "Shutdown successful");
                    } else {
                        throw new IllegalStateException("Cannot shutdown. Unstarted tasks " + unstartedRunnables);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(TAG + " init interrupted");
                }
            }
        }
    }
}
