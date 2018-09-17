package com.huawei.android.pushagent.utils.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class b implements ThreadFactory {
    private final ThreadGroup et;
    private final String eu;
    private final int ev;
    private final AtomicInteger ew;

    public b(String str, int i) {
        this.ew = new AtomicInteger(1);
        this.ev = i;
        SecurityManager securityManager = System.getSecurityManager();
        this.et = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.eu = str + "-pool-thread-";
    }

    public b(String str) {
        this(str, 5);
    }

    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(this.et, runnable, this.eu + this.ew.getAndIncrement(), 0);
        if (thread.isDaemon()) {
            thread.setDaemon(false);
        }
        if (thread.getPriority() != this.ev) {
            thread.setPriority(this.ev);
        }
        return thread;
    }
}
