package java.util.concurrent.locks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class ReentrantLock implements Lock, Serializable {
    private static final long serialVersionUID = 7373984872572414699L;
    private final Sync sync;

    static final class FairSync extends Sync {
        private static final long serialVersionUID = -3000897897090466540L;

        FairSync() {
        }

        /* access modifiers changed from: package-private */
        public final void lock() {
            acquire(1);
        }

        /* access modifiers changed from: protected */
        public final boolean tryAcquire(int acquires) {
            Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc >= 0) {
                    setState(nextc);
                    return true;
                }
                throw new Error("Maximum lock count exceeded");
            }
            return false;
        }
    }

    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = 7316153563782823691L;

        NonfairSync() {
        }

        /* access modifiers changed from: package-private */
        public final void lock() {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
            } else {
                acquire(1);
            }
        }

        /* access modifiers changed from: protected */
        public final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    static abstract class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = -5179523762034025860L;

        /* access modifiers changed from: package-private */
        public abstract void lock();

        Sync() {
        }

        /* access modifiers changed from: package-private */
        public final boolean nonfairTryAcquire(int acquires) {
            Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc >= 0) {
                    setState(nextc);
                    return true;
                }
                throw new Error("Maximum lock count exceeded");
            }
            return false;
        }

        /* access modifiers changed from: protected */
        public final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() == getExclusiveOwnerThread()) {
                boolean free = false;
                if (c == 0) {
                    free = true;
                    setExclusiveOwnerThread(null);
                }
                setState(c);
                return free;
            }
            throw new IllegalMonitorStateException();
        }

        /* access modifiers changed from: protected */
        public final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        /* access modifiers changed from: package-private */
        public final AbstractQueuedSynchronizer.ConditionObject newCondition() {
            return new AbstractQueuedSynchronizer.ConditionObject();
        }

        /* access modifiers changed from: package-private */
        public final Thread getOwner() {
            if (getState() == 0) {
                return null;
            }
            return getExclusiveOwnerThread();
        }

        /* access modifiers changed from: package-private */
        public final int getHoldCount() {
            if (isHeldExclusively()) {
                return getState();
            }
            return 0;
        }

        /* access modifiers changed from: package-private */
        public final boolean isLocked() {
            return getState() != 0;
        }

        private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0);
        }
    }

    public ReentrantLock() {
        this.sync = new NonfairSync();
    }

    public ReentrantLock(boolean fair) {
        this.sync = fair ? new FairSync() : new NonfairSync();
    }

    public void lock() {
        this.sync.lock();
    }

    public void lockInterruptibly() throws InterruptedException {
        this.sync.acquireInterruptibly(1);
    }

    public boolean tryLock() {
        return this.sync.nonfairTryAcquire(1);
    }

    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return this.sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    public void unlock() {
        this.sync.release(1);
    }

    public Condition newCondition() {
        return this.sync.newCondition();
    }

    public int getHoldCount() {
        return this.sync.getHoldCount();
    }

    public boolean isHeldByCurrentThread() {
        return this.sync.isHeldExclusively();
    }

    public boolean isLocked() {
        return this.sync.isLocked();
    }

    public final boolean isFair() {
        return this.sync instanceof FairSync;
    }

    /* access modifiers changed from: protected */
    public Thread getOwner() {
        return this.sync.getOwner();
    }

    public final boolean hasQueuedThreads() {
        return this.sync.hasQueuedThreads();
    }

    public final boolean hasQueuedThread(Thread thread) {
        return this.sync.isQueued(thread);
    }

    public final int getQueueLength() {
        return this.sync.getQueueLength();
    }

    /* access modifiers changed from: protected */
    public Collection<Thread> getQueuedThreads() {
        return this.sync.getQueuedThreads();
    }

    public boolean hasWaiters(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        } else if (condition instanceof AbstractQueuedSynchronizer.ConditionObject) {
            return this.sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject) condition);
        } else {
            throw new IllegalArgumentException("not owner");
        }
    }

    public int getWaitQueueLength(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        } else if (condition instanceof AbstractQueuedSynchronizer.ConditionObject) {
            return this.sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject) condition);
        } else {
            throw new IllegalArgumentException("not owner");
        }
    }

    /* access modifiers changed from: protected */
    public Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        } else if (condition instanceof AbstractQueuedSynchronizer.ConditionObject) {
            return this.sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject) condition);
        } else {
            throw new IllegalArgumentException("not owner");
        }
    }

    public String toString() {
        String str;
        Thread o = this.sync.getOwner();
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        if (o == null) {
            str = "[Unlocked]";
        } else {
            str = "[Locked by thread " + o.getName() + "]";
        }
        sb.append(str);
        return sb.toString();
    }
}
