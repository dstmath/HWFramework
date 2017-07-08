package tmsdkobf;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
public class kd extends ThreadPoolExecutor {
    private a vl;

    /* compiled from: Unknown */
    public interface a {
        void afterExecute(Runnable runnable, Throwable th);

        void beforeExecute(Thread thread, Runnable runnable);
    }

    public kd(int i, int i2, long j, TimeUnit timeUnit, BlockingQueue<Runnable> blockingQueue, RejectedExecutionHandler rejectedExecutionHandler) {
        super(i, i2, j, timeUnit, blockingQueue, new kc(), rejectedExecutionHandler);
        this.vl = null;
    }

    public void a(a aVar) {
        this.vl = aVar;
    }

    protected void afterExecute(Runnable runnable, Throwable th) {
        super.afterExecute(runnable, th);
        if (this.vl != null) {
            this.vl.afterExecute(runnable, th);
        }
    }

    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable);
        if (this.vl != null) {
            this.vl.beforeExecute(thread, runnable);
        }
    }

    public void execute(Runnable runnable) {
        super.execute(runnable);
    }
}
