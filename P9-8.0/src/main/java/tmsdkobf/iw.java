package tmsdkobf;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class iw extends ThreadPoolExecutor {
    private a sr = null;

    public interface a {
        void afterExecute(Runnable runnable, Throwable th);

        void beforeExecute(Thread thread, Runnable runnable);
    }

    public iw(int i, int i2, long j, TimeUnit timeUnit, BlockingQueue<Runnable> blockingQueue, RejectedExecutionHandler rejectedExecutionHandler) {
        super(i, i2, j, timeUnit, blockingQueue, new iv(), rejectedExecutionHandler);
    }

    public void a(a aVar) {
        this.sr = aVar;
    }

    protected void afterExecute(Runnable runnable, Throwable th) {
        super.afterExecute(runnable, th);
        if (this.sr != null) {
            this.sr.afterExecute(runnable, th);
        }
    }

    protected void beforeExecute(Thread thread, Runnable runnable) {
        super.beforeExecute(thread, runnable);
        if (this.sr != null) {
            this.sr.beforeExecute(thread, runnable);
        }
    }

    public void execute(Runnable runnable) {
        super.execute(runnable);
    }
}
