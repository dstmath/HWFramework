package com.huawei.android.pushagent.utils.threadpool;

public class c implements Runnable {
    private Runnable ex;

    public c(Runnable runnable) {
        this.ex = runnable;
    }

    public void run() {
        if (this.ex != null) {
            try {
                this.ex.run();
            } catch (Throwable th) {
                com.huawei.android.pushagent.utils.d.c.sf("PushLog2951", "exception in task run");
            }
        }
    }
}
