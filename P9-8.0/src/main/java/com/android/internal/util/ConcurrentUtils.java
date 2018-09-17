package com.android.internal.util;

import android.os.Process;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentUtils {
    private ConcurrentUtils() {
    }

    public static ExecutorService newFixedThreadPool(int nThreads, final String poolName, final int linuxThreadPriority) {
        return Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
            private final AtomicInteger threadNum = new AtomicInteger(0);

            public Thread newThread(final Runnable r) {
                String str = poolName + this.threadNum.incrementAndGet();
                final int i = linuxThreadPriority;
                return new Thread(str) {
                    public void run() {
                        Process.setThreadPriority(i);
                        r.run();
                    }
                };
            }
        });
    }

    public static <T> T waitForFutureNoInterrupt(Future<T> future, String description) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(description + " interrupted");
        } catch (ExecutionException e2) {
            throw new RuntimeException(description + " failed", e2);
        }
    }
}
