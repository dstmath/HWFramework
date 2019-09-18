package com.android.internal.util;

import android.os.Process;
import android.util.Slog;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentUtils {
    private ConcurrentUtils() {
    }

    public static ExecutorService newFixedThreadPool(int nThreads, final String poolName, final int linuxThreadPriority) {
        return Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
            private final AtomicInteger threadNum = new AtomicInteger(0);

            public Thread newThread(final Runnable r) {
                return new Thread(poolName + this.threadNum.incrementAndGet()) {
                    public void run() {
                        Process.setThreadPriority(linuxThreadPriority);
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

    public static void waitForCountDownNoInterrupt(CountDownLatch countDownLatch, long timeoutMs, String description) {
        try {
            if (!countDownLatch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException(description + " timed out.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(description + " interrupted.");
        }
    }

    public static void wtfIfLockHeld(String tag, Object lock) {
        if (Thread.holdsLock(lock)) {
            Slog.wtf(tag, "Lock mustn't be held");
        }
    }

    public static void wtfIfLockNotHeld(String tag, Object lock) {
        if (!Thread.holdsLock(lock)) {
            Slog.wtf(tag, "Lock must be held");
        }
    }
}
