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
            Preconditions.checkState(sInstance.mService != null, "Cannot get " + TAG + " - it has been shut down");
            systemServerInitThreadPool = sInstance;
        }
        return systemServerInitThreadPool;
    }

    public Future<?> submit(Runnable runnable, String description) {
        return IS_DEBUGGABLE ? this.mService.submit(new -$Lambda$HTEfptnAsu3OjkqY28AzP7WiEyA(description, runnable)) : this.mService.submit(runnable);
    }

    static /* synthetic */ void lambda$-com_android_server_SystemServerInitThreadPool_2249(String description, Runnable runnable) {
        Slog.d(TAG, "Started executing " + description);
        try {
            runnable.run();
            Slog.d(TAG, "Finished executing " + description);
        } catch (RuntimeException e) {
            Slog.e(TAG, "Failure in " + description + ": " + e, e);
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
