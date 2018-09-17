package java.util.concurrent.locks;

import java.util.concurrent.TimeUnit;

public interface Lock {
    void lock();

    void lockInterruptibly() throws InterruptedException;

    Condition newCondition();

    boolean tryLock();

    boolean tryLock(long j, TimeUnit timeUnit) throws InterruptedException;

    void unlock();
}
