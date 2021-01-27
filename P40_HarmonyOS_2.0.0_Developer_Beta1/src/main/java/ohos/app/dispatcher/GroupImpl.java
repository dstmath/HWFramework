package ohos.app.dispatcher;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import ohos.appexecfwk.utils.AppLog;

/* access modifiers changed from: package-private */
public class GroupImpl implements Group {
    private static final int MAX_TASK = 1000;
    private final Condition condition = this.waitLock.newCondition();
    private final AtomicInteger count = new AtomicInteger(0);
    private final Object countLock = new Object();
    private final Queue<Runnable> notifications = new LinkedList();
    private final ReentrantLock waitLock = new ReentrantLock(true);

    GroupImpl() {
    }

    public boolean awaitAllTasks(long j) {
        if (this.count.get() == 0) {
            return true;
        }
        boolean z = false;
        if (j <= 0) {
            return false;
        }
        this.waitLock.lock();
        while (true) {
            try {
                if (this.count.get() <= 0) {
                    z = true;
                    break;
                } else if (!this.condition.await(j, TimeUnit.MILLISECONDS)) {
                    AppLog.d("GroupImpl::awaitAllTasks timeout", new Object[0]);
                    break;
                } else {
                    AppLog.d("GroupImpl::awaitAllTasks success", new Object[0]);
                }
            } catch (InterruptedException unused) {
                AppLog.w("GroupImpl::awaitAllTasks has been interrupted", new Object[0]);
            } catch (Throwable th) {
                this.waitLock.unlock();
                throw th;
            }
        }
        this.waitLock.unlock();
        return z;
    }

    public void associate() {
        this.count.incrementAndGet();
    }

    /* JADX INFO: finally extract failed */
    public void notifyTaskDone() {
        if (this.count.decrementAndGet() <= 0) {
            this.waitLock.lock();
            try {
                this.condition.signalAll();
                this.waitLock.unlock();
                drainNotifications();
            } catch (Throwable th) {
                this.waitLock.unlock();
                throw th;
            }
        }
    }

    public void addNotification(Runnable runnable) {
        if (this.count.get() != 0) {
            synchronized (this.notifications) {
                if (this.count.get() != 0) {
                    this.notifications.add(runnable);
                    return;
                }
            }
        }
        runnable.run();
    }

    private void drainNotifications() {
        synchronized (this.notifications) {
            int i = 1000;
            while (true) {
                Runnable poll = this.notifications.poll();
                if (poll == null) {
                    break;
                }
                i--;
                if (i <= 0) {
                    break;
                }
                poll.run();
            }
        }
    }
}
