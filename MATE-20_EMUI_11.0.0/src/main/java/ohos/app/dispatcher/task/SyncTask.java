package ohos.app.dispatcher.task;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import ohos.appexecfwk.utils.AppLog;

public class SyncTask extends Task {
    private boolean executed = false;
    private final Lock lock = new ReentrantLock();
    private final Condition runCondition = this.lock.newCondition();

    public SyncTask(Runnable runnable, TaskPriority taskPriority) {
        super(runnable, taskPriority);
    }

    @Override // ohos.app.dispatcher.task.Task, java.lang.Runnable
    public void run() {
        this.lock.lock();
        try {
            super.run();
            this.executed = true;
            this.runCondition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    public void waitTask() {
        if (this.runnable != null) {
            this.lock.lock();
            while (!this.executed) {
                try {
                    this.runCondition.await();
                } catch (InterruptedException unused) {
                    AppLog.w("SyncTask::waitTask has been interrupted", new Object[0]);
                } catch (Throwable th) {
                    this.lock.unlock();
                    throw th;
                }
            }
            this.lock.unlock();
        }
    }
}
