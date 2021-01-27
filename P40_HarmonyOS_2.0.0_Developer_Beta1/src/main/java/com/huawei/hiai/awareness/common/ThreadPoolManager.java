package com.huawei.hiai.awareness.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    private static final int CORE_POOL_SIZE;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int KEEP_ALIVE = 1;
    private static final int MAXIMUM_POOL_SIZE;
    private static final String THREAD_FACTORY_NAME = "thread pool";
    private static ThreadPoolManager sThreadPoolManager;
    private ExecutorService mExecutorCacheService;
    private ExecutorService mExecutorService;
    private BlockingQueue<Runnable> mPoolWorkQueue = new LinkedBlockingQueue(128);

    static {
        int i = CPU_COUNT;
        CORE_POOL_SIZE = i + 1;
        MAXIMUM_POOL_SIZE = (i * 2) + 1;
    }

    private ThreadPoolManager() {
    }

    public synchronized ExecutorService executorService() {
        if (this.mExecutorService == null) {
            this.mExecutorService = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, 1, TimeUnit.SECONDS, this.mPoolWorkQueue, threadFactory(THREAD_FACTORY_NAME, false));
        }
        return this.mExecutorService;
    }

    public static ThreadFactory threadFactory(final String name, final boolean isDaemon) {
        return new ThreadFactory() {
            /* class com.huawei.hiai.awareness.common.ThreadPoolManager.AnonymousClass1 */

            @Override // java.util.concurrent.ThreadFactory
            public Thread newThread(Runnable runnable) {
                Thread result = new Thread(runnable, name);
                result.setDaemon(isDaemon);
                return result;
            }
        };
    }

    public static ThreadPoolManager getInstance() {
        ThreadPoolManager threadPoolManager;
        synchronized (ThreadPoolManager.class) {
            if (sThreadPoolManager == null) {
                sThreadPoolManager = new ThreadPoolManager();
            }
            threadPoolManager = sThreadPoolManager;
        }
        return threadPoolManager;
    }

    public void startInChildThread(Runnable runnable) {
        executorService().execute(runnable);
    }

    public void startInCacheChildThread(Runnable runnable) {
        executorCacheService().execute(runnable);
    }

    public synchronized ExecutorService executorCacheService() {
        if (this.mExecutorCacheService == null) {
            this.mExecutorCacheService = Executors.newCachedThreadPool();
        }
        return this.mExecutorCacheService;
    }
}
