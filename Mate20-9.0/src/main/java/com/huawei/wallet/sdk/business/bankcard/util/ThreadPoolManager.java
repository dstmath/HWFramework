package com.huawei.wallet.sdk.business.bankcard.util;

import com.huawei.wallet.sdk.common.log.LogC;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ThreadPoolManager {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "commonbase:ThreadPoolManager";
    private static volatile ThreadPoolManager instance;
    private final ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();

    public ExecutorService getThreadPoolExecutor() {
        return this.threadPoolExecutor;
    }

    private ThreadPoolManager() {
        LogC.i(TAG, "ThreadPool init!", false);
    }

    public static ThreadPoolManager getInstance() {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }

    public void execute(Runnable run) {
        this.threadPoolExecutor.execute(run);
    }

    public Future<?> submit(Runnable run) {
        return this.threadPoolExecutor.submit(run);
    }

    public void release() {
        LogC.i(TAG, "ThreadPool release!", false);
        if (this.threadPoolExecutor != null && !this.threadPoolExecutor.isShutdown()) {
            this.threadPoolExecutor.shutdown();
        }
    }
}
