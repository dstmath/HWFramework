package tmsdkobf;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class iv implements ThreadFactory, jb {
    private final ThreadGroup so = new ThreadGroup("TMS-COMMON");
    private final AtomicInteger sp = new AtomicInteger(1);
    private final String sq = ("Common Thread Pool-" + sO.getAndIncrement() + "-Thread-");

    iv() {
    }

    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(this.so, runnable, this.sq + this.sp.getAndIncrement(), 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != 5) {
            thread.setPriority(5);
        }
        return thread;
    }
}
