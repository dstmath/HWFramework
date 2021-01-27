package ohos.miscservices.httpaccess;

import java.lang.Thread;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.LongAdder;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class HttpThreadFactory implements ThreadFactory {
    private static final Object LOCK = new Object();
    private static final int MAX_THREAD_NUM = 1;
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "HttpThreadFactory");
    private LongAdder incrementer = new LongAdder();
    private String threadNamePrefix;

    public HttpThreadFactory(String str) {
        this.threadNamePrefix = str + "_";
    }

    @Override // java.util.concurrent.ThreadFactory
    public Thread newThread(Runnable runnable) {
        synchronized (LOCK) {
            if (this.incrementer.intValue() > 1) {
                HiLog.error(TAG, "thread exceed max number!", new Object[0]);
                return null;
            }
            Thread thread = new Thread(runnable, this.threadNamePrefix + System.currentTimeMillis());
            thread.setUncaughtExceptionHandler(new HttpUncaughtExceptionHandler());
            this.incrementer.increment();
            return thread;
        }
    }

    private static class HttpUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        private HttpUncaughtExceptionHandler() {
        }

        @Override // java.lang.Thread.UncaughtExceptionHandler
        public void uncaughtException(Thread thread, Throwable th) {
            HiLog.error(HttpThreadFactory.TAG, "thread : %{public}s caught exception!", thread.getName());
        }
    }
}
