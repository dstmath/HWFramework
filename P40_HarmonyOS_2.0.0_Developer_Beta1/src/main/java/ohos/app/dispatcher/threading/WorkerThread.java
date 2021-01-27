package ohos.app.dispatcher.threading;

import java.util.concurrent.atomic.AtomicLong;
import ohos.appexecfwk.utils.AppLog;

public class WorkerThread implements Runnable {
    private Delegate delegate;
    private Runnable firstTask;
    AtomicLong taskCounter = new AtomicLong(0);
    final Thread thread;

    public interface Delegate {
        void doWorks(WorkerThread workerThread);
    }

    WorkerThread(Delegate delegate2, Runnable runnable, ThreadFactory threadFactory) {
        if (threadFactory == null || delegate2 == null) {
            throw new NullPointerException("Lack of delegate or thread factory.");
        }
        this.delegate = delegate2;
        this.firstTask = runnable;
        this.thread = threadFactory.create(this);
        AppLog.i("WorkerThread create a new thread: %{public}s", this.thread.getName());
    }

    @Override // java.lang.Runnable
    public void run() {
        this.delegate.doWorks(this);
    }

    public void incTaskCount() {
        this.taskCounter.incrementAndGet();
    }

    public String getThreadName() {
        Thread thread2 = this.thread;
        if (thread2 == null) {
            return null;
        }
        return thread2.getName();
    }

    public Runnable pollFirstTask() {
        Runnable runnable = this.firstTask;
        this.firstTask = null;
        return runnable;
    }

    public long getTaskCounter() {
        return this.taskCounter.get();
    }
}
