package tmsdkobf;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: Unknown */
public class kc implements ThreadFactory, ki {
    private final ThreadGroup vi;
    private final AtomicInteger vj;
    private final String vk;

    kc() {
        this.vj = new AtomicInteger(1);
        this.vi = new ThreadGroup("TMS-COMMON");
        this.vk = "Common Thread Pool-" + vI.getAndIncrement() + "-Thread-";
    }

    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(this.vi, runnable, this.vk + this.vj.getAndIncrement(), 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != 5) {
            thread.setPriority(5);
        }
        return thread;
    }
}
