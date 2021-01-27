package ohos.utils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionHelper {
    private static final int MS_TO_NS = 1000000;
    private final Condition condition;
    private volatile boolean isWakeup;
    private final Lock lock;

    public ConditionHelper() {
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
        this.isWakeup = false;
    }

    public ConditionHelper(boolean z) {
        this.lock = new ReentrantLock();
        this.condition = this.lock.newCondition();
        this.isWakeup = z;
    }

    public void wakeup() {
        this.lock.lock();
        try {
            this.isWakeup = true;
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    public void resetCondition() {
        this.isWakeup = false;
    }

    public void await() {
        this.lock.lock();
        boolean z = false;
        while (!this.isWakeup) {
            try {
                try {
                    this.condition.await();
                } catch (InterruptedException unused) {
                    z = true;
                }
            } finally {
                this.lock.unlock();
                if (z) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public boolean await(long j) {
        if (j != 0) {
            boolean z = false;
            long currentTimeMillis = System.currentTimeMillis();
            try {
                this.lock.lock();
                long j2 = j;
                while (!this.isWakeup && j2 > 0) {
                    try {
                        this.condition.awaitNanos(j2 * 1000000);
                    } catch (InterruptedException unused) {
                        z = true;
                    }
                    j2 = (currentTimeMillis + j) - System.currentTimeMillis();
                }
                return this.isWakeup;
            } finally {
                this.lock.unlock();
                if (z) {
                    Thread.currentThread().interrupt();
                }
            }
        } else {
            await();
            return true;
        }
    }
}
