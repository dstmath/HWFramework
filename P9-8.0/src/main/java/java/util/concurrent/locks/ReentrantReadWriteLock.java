package java.util.concurrent.locks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer.ConditionObject;
import sun.misc.Unsafe;

public class ReentrantReadWriteLock implements ReadWriteLock, Serializable {
    private static final long TID;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = -6992448646407690164L;
    private final ReadLock readerLock;
    final Sync sync;
    private final WriteLock writerLock;

    static abstract class Sync extends AbstractQueuedSynchronizer {
        static final int EXCLUSIVE_MASK = 65535;
        static final int MAX_COUNT = 65535;
        static final int SHARED_SHIFT = 16;
        static final int SHARED_UNIT = 65536;
        private static final long serialVersionUID = 6317671515068378041L;
        private transient HoldCounter cachedHoldCounter;
        private transient Thread firstReader;
        private transient int firstReaderHoldCount;
        private transient ThreadLocalHoldCounter readHolds = new ThreadLocalHoldCounter();

        static final class HoldCounter {
            int count;
            final long tid = ReentrantReadWriteLock.getThreadId(Thread.currentThread());

            HoldCounter() {
            }
        }

        static final class ThreadLocalHoldCounter extends ThreadLocal<HoldCounter> {
            ThreadLocalHoldCounter() {
            }

            public HoldCounter initialValue() {
                return new HoldCounter();
            }
        }

        abstract boolean readerShouldBlock();

        abstract boolean writerShouldBlock();

        static int sharedCount(int c) {
            return c >>> 16;
        }

        static int exclusiveCount(int c) {
            return 65535 & c;
        }

        Sync() {
            setState(getState());
        }

        protected final boolean tryRelease(int releases) {
            if (isHeldExclusively()) {
                int nextc = getState() - releases;
                boolean free = exclusiveCount(nextc) == 0;
                if (free) {
                    setExclusiveOwnerThread(null);
                }
                setState(nextc);
                return free;
            }
            throw new IllegalMonitorStateException();
        }

        protected final boolean tryAcquire(int acquires) {
            Thread current = Thread.currentThread();
            int c = getState();
            int w = exclusiveCount(c);
            if (c != 0) {
                if (w == 0 || current != getExclusiveOwnerThread()) {
                    return false;
                }
                if (exclusiveCount(acquires) + w > 65535) {
                    throw new Error("Maximum lock count exceeded");
                }
                setState(c + acquires);
                return true;
            } else if (writerShouldBlock() || (compareAndSetState(c, c + acquires) ^ 1) != 0) {
                return false;
            } else {
                setExclusiveOwnerThread(current);
                return true;
            }
        }

        protected final boolean tryReleaseShared(int unused) {
            int nextc;
            Thread current = Thread.currentThread();
            if (this.firstReader != current) {
                HoldCounter rh = this.cachedHoldCounter;
                if (rh == null || rh.tid != ReentrantReadWriteLock.getThreadId(current)) {
                    rh = (HoldCounter) this.readHolds.get();
                }
                int count = rh.count;
                if (count <= 1) {
                    this.readHolds.remove();
                    if (count <= 0) {
                        throw unmatchedUnlockException();
                    }
                }
                rh.count--;
            } else if (this.firstReaderHoldCount == 1) {
                this.firstReader = null;
            } else {
                this.firstReaderHoldCount--;
            }
            int c;
            do {
                c = getState();
                nextc = c - 65536;
            } while (!compareAndSetState(c, nextc));
            if (nextc == 0) {
                return true;
            }
            return false;
        }

        private IllegalMonitorStateException unmatchedUnlockException() {
            return new IllegalMonitorStateException("attempt to unlock read lock, not locked by current thread");
        }

        protected final int tryAcquireShared(int unused) {
            Thread current = Thread.currentThread();
            int c = getState();
            if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current) {
                return -1;
            }
            int r = sharedCount(c);
            if (readerShouldBlock() || r >= 65535 || !compareAndSetState(c, 65536 + c)) {
                return fullTryAcquireShared(current);
            }
            if (r == 0) {
                this.firstReader = current;
                this.firstReaderHoldCount = 1;
            } else if (this.firstReader == current) {
                this.firstReaderHoldCount++;
            } else {
                HoldCounter rh = this.cachedHoldCounter;
                if (rh == null || rh.tid != ReentrantReadWriteLock.getThreadId(current)) {
                    rh = (HoldCounter) this.readHolds.get();
                    this.cachedHoldCounter = rh;
                } else if (rh.count == 0) {
                    this.readHolds.set(rh);
                }
                rh.count++;
            }
            return 1;
        }

        final int fullTryAcquireShared(Thread current) {
            int c;
            HoldCounter rh = null;
            do {
                c = getState();
                if (exclusiveCount(c) != 0) {
                    if (getExclusiveOwnerThread() != current) {
                        return -1;
                    }
                } else if (readerShouldBlock() && this.firstReader != current) {
                    if (rh == null) {
                        rh = this.cachedHoldCounter;
                        if (rh == null || rh.tid != ReentrantReadWriteLock.getThreadId(current)) {
                            rh = (HoldCounter) this.readHolds.get();
                            if (rh.count == 0) {
                                this.readHolds.remove();
                            }
                        }
                    }
                    if (rh.count == 0) {
                        return -1;
                    }
                }
                if (sharedCount(c) == 65535) {
                    throw new Error("Maximum lock count exceeded");
                }
            } while (!compareAndSetState(c, 65536 + c));
            if (sharedCount(c) == 0) {
                this.firstReader = current;
                this.firstReaderHoldCount = 1;
            } else if (this.firstReader == current) {
                this.firstReaderHoldCount++;
            } else {
                if (rh == null) {
                    rh = this.cachedHoldCounter;
                }
                if (rh == null || rh.tid != ReentrantReadWriteLock.getThreadId(current)) {
                    rh = (HoldCounter) this.readHolds.get();
                } else if (rh.count == 0) {
                    this.readHolds.set(rh);
                }
                rh.count++;
                this.cachedHoldCounter = rh;
            }
            return 1;
        }

        final boolean tryWriteLock() {
            Thread current = Thread.currentThread();
            int c = getState();
            if (c != 0) {
                int w = exclusiveCount(c);
                if (w == 0 || current != getExclusiveOwnerThread()) {
                    return false;
                }
                if (w == 65535) {
                    throw new Error("Maximum lock count exceeded");
                }
            }
            if (!compareAndSetState(c, c + 1)) {
                return false;
            }
            setExclusiveOwnerThread(current);
            return true;
        }

        final boolean tryReadLock() {
            int r;
            Thread current = Thread.currentThread();
            int c;
            do {
                c = getState();
                if (exclusiveCount(c) != 0 && getExclusiveOwnerThread() != current) {
                    return false;
                }
                r = sharedCount(c);
                if (r == 65535) {
                    throw new Error("Maximum lock count exceeded");
                }
            } while (!compareAndSetState(c, 65536 + c));
            if (r == 0) {
                this.firstReader = current;
                this.firstReaderHoldCount = 1;
            } else if (this.firstReader == current) {
                this.firstReaderHoldCount++;
            } else {
                HoldCounter rh = this.cachedHoldCounter;
                if (rh == null || rh.tid != ReentrantReadWriteLock.getThreadId(current)) {
                    rh = (HoldCounter) this.readHolds.get();
                    this.cachedHoldCounter = rh;
                } else if (rh.count == 0) {
                    this.readHolds.set(rh);
                }
                rh.count++;
            }
            return true;
        }

        protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        final Thread getOwner() {
            if (exclusiveCount(getState()) == 0) {
                return null;
            }
            return getExclusiveOwnerThread();
        }

        final int getReadLockCount() {
            return sharedCount(getState());
        }

        final boolean isWriteLocked() {
            return exclusiveCount(getState()) != 0;
        }

        final int getWriteHoldCount() {
            return isHeldExclusively() ? exclusiveCount(getState()) : 0;
        }

        final int getReadHoldCount() {
            if (getReadLockCount() == 0) {
                return 0;
            }
            Thread current = Thread.currentThread();
            if (this.firstReader == current) {
                return this.firstReaderHoldCount;
            }
            HoldCounter rh = this.cachedHoldCounter;
            if (rh != null && rh.tid == ReentrantReadWriteLock.getThreadId(current)) {
                return rh.count;
            }
            int count = ((HoldCounter) this.readHolds.get()).count;
            if (count == 0) {
                this.readHolds.remove();
            }
            return count;
        }

        private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
            s.defaultReadObject();
            this.readHolds = new ThreadLocalHoldCounter();
            setState(0);
        }

        final int getCount() {
            return getState();
        }
    }

    static final class FairSync extends Sync {
        private static final long serialVersionUID = -2274990926593161451L;

        FairSync() {
        }

        final boolean writerShouldBlock() {
            return hasQueuedPredecessors();
        }

        final boolean readerShouldBlock() {
            return hasQueuedPredecessors();
        }
    }

    static final class NonfairSync extends Sync {
        private static final long serialVersionUID = -8159625535654395037L;

        NonfairSync() {
        }

        final boolean writerShouldBlock() {
            return false;
        }

        final boolean readerShouldBlock() {
            return apparentlyFirstQueuedIsExclusive();
        }
    }

    public static class ReadLock implements Lock, Serializable {
        private static final long serialVersionUID = -5992448646407690164L;
        private final Sync sync;

        protected ReadLock(ReentrantReadWriteLock lock) {
            this.sync = lock.sync;
        }

        public void lock() {
            this.sync.acquireShared(1);
        }

        public void lockInterruptibly() throws InterruptedException {
            this.sync.acquireSharedInterruptibly(1);
        }

        public boolean tryLock() {
            return this.sync.tryReadLock();
        }

        public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
            return this.sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
        }

        public void unlock() {
            this.sync.releaseShared(1);
        }

        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            return super.toString() + "[Read locks = " + this.sync.getReadLockCount() + "]";
        }
    }

    public static class WriteLock implements Lock, Serializable {
        private static final long serialVersionUID = -4992448646407690164L;
        private final Sync sync;

        protected WriteLock(ReentrantReadWriteLock lock) {
            this.sync = lock.sync;
        }

        public void lock() {
            this.sync.acquire(1);
        }

        public void lockInterruptibly() throws InterruptedException {
            this.sync.acquireInterruptibly(1);
        }

        public boolean tryLock() {
            return this.sync.tryWriteLock();
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

        public String toString() {
            String str;
            Thread o = this.sync.getOwner();
            StringBuilder append = new StringBuilder().append(super.toString());
            if (o == null) {
                str = "[Unlocked]";
            } else {
                str = "[Locked by thread " + o.getName() + "]";
            }
            return append.append(str).toString();
        }

        public boolean isHeldByCurrentThread() {
            return this.sync.isHeldExclusively();
        }

        public int getHoldCount() {
            return this.sync.getWriteHoldCount();
        }
    }

    public ReentrantReadWriteLock() {
        this(false);
    }

    public ReentrantReadWriteLock(boolean fair) {
        this.sync = fair ? new FairSync() : new NonfairSync();
        this.readerLock = new ReadLock(this);
        this.writerLock = new WriteLock(this);
    }

    public WriteLock writeLock() {
        return this.writerLock;
    }

    public ReadLock readLock() {
        return this.readerLock;
    }

    public final boolean isFair() {
        return this.sync instanceof FairSync;
    }

    protected Thread getOwner() {
        return this.sync.getOwner();
    }

    public int getReadLockCount() {
        return this.sync.getReadLockCount();
    }

    public boolean isWriteLocked() {
        return this.sync.isWriteLocked();
    }

    public boolean isWriteLockedByCurrentThread() {
        return this.sync.isHeldExclusively();
    }

    public int getWriteHoldCount() {
        return this.sync.getWriteHoldCount();
    }

    public int getReadHoldCount() {
        return this.sync.getReadHoldCount();
    }

    protected Collection<Thread> getQueuedWriterThreads() {
        return this.sync.getExclusiveQueuedThreads();
    }

    protected Collection<Thread> getQueuedReaderThreads() {
        return this.sync.getSharedQueuedThreads();
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

    protected Collection<Thread> getQueuedThreads() {
        return this.sync.getQueuedThreads();
    }

    public boolean hasWaiters(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        } else if (condition instanceof ConditionObject) {
            return this.sync.hasWaiters((ConditionObject) condition);
        } else {
            throw new IllegalArgumentException("not owner");
        }
    }

    public int getWaitQueueLength(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        } else if (condition instanceof ConditionObject) {
            return this.sync.getWaitQueueLength((ConditionObject) condition);
        } else {
            throw new IllegalArgumentException("not owner");
        }
    }

    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        } else if (condition instanceof ConditionObject) {
            return this.sync.getWaitingThreads((ConditionObject) condition);
        } else {
            throw new IllegalArgumentException("not owner");
        }
    }

    public String toString() {
        int c = this.sync.getCount();
        int w = Sync.exclusiveCount(c);
        return super.toString() + "[Write locks = " + w + ", Read locks = " + Sync.sharedCount(c) + "]";
    }

    static final long getThreadId(Thread thread) {
        return U.getLongVolatile(thread, TID);
    }

    static {
        try {
            TID = U.objectFieldOffset(Thread.class.getDeclaredField("tid"));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
