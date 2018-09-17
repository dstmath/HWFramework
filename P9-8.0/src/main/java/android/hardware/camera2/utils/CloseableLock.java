package android.hardware.camera2.utils;

import android.net.ProxyInfo;
import android.util.Log;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CloseableLock implements AutoCloseable {
    private static final boolean VERBOSE = false;
    private final String TAG;
    private volatile boolean mClosed;
    private final Condition mCondition;
    private boolean mExclusive;
    private final ReentrantLock mLock;
    private final ThreadLocal<Integer> mLockCount;
    private final String mName;
    private int mSharedLocks;

    public class ScopedLock implements AutoCloseable {
        /* synthetic */ ScopedLock(CloseableLock this$0, ScopedLock -this1) {
            this();
        }

        private ScopedLock() {
        }

        public void close() {
            CloseableLock.this.releaseLock();
        }
    }

    public CloseableLock() {
        this.TAG = "CloseableLock";
        this.mClosed = false;
        this.mExclusive = false;
        this.mSharedLocks = 0;
        this.mLock = new ReentrantLock();
        this.mCondition = this.mLock.newCondition();
        this.mLockCount = new ThreadLocal<Integer>() {
            protected Integer initialValue() {
                return Integer.valueOf(0);
            }
        };
        this.mName = ProxyInfo.LOCAL_EXCL_LIST;
    }

    public CloseableLock(String name) {
        this.TAG = "CloseableLock";
        this.mClosed = false;
        this.mExclusive = false;
        this.mSharedLocks = 0;
        this.mLock = new ReentrantLock();
        this.mCondition = this.mLock.newCondition();
        this.mLockCount = /* anonymous class already generated */;
        this.mName = name;
    }

    public void close() {
        if (!this.mClosed && acquireExclusiveLock() != null) {
            if (((Integer) this.mLockCount.get()).intValue() != 1) {
                throw new IllegalStateException("Cannot close while one or more acquired locks are being held by this thread; release all other locks first");
            }
            try {
                this.mLock.lock();
                this.mClosed = true;
                this.mExclusive = false;
                this.mSharedLocks = 0;
                this.mLockCount.remove();
                this.mCondition.signalAll();
            } finally {
                this.mLock.unlock();
            }
        }
    }

    public ScopedLock acquireLock() {
        try {
            this.mLock.lock();
            if (this.mClosed) {
                return null;
            }
            int ownedLocks = ((Integer) this.mLockCount.get()).intValue();
            if (!this.mExclusive || ownedLocks <= 0) {
                while (this.mExclusive) {
                    this.mCondition.awaitUninterruptibly();
                    if (this.mClosed) {
                        this.mLock.unlock();
                        return null;
                    }
                }
                this.mSharedLocks++;
                this.mLockCount.set(Integer.valueOf(((Integer) this.mLockCount.get()).intValue() + 1));
                this.mLock.unlock();
                return new ScopedLock(this, null);
            }
            throw new IllegalStateException("Cannot acquire shared lock while holding exclusive lock");
        } finally {
            this.mLock.unlock();
        }
    }

    public ScopedLock acquireExclusiveLock() {
        try {
            this.mLock.lock();
            if (this.mClosed) {
                return null;
            }
            int ownedLocks = ((Integer) this.mLockCount.get()).intValue();
            if (this.mExclusive || ownedLocks <= 0) {
                while (ownedLocks == 0) {
                    if (!this.mExclusive && this.mSharedLocks <= 0) {
                        break;
                    }
                    this.mCondition.awaitUninterruptibly();
                    if (this.mClosed) {
                        this.mLock.unlock();
                        return null;
                    }
                }
                this.mExclusive = true;
                this.mLockCount.set(Integer.valueOf(((Integer) this.mLockCount.get()).intValue() + 1));
                this.mLock.unlock();
                return new ScopedLock(this, null);
            }
            throw new IllegalStateException("Cannot acquire exclusive lock while holding shared lock");
        } finally {
            this.mLock.unlock();
        }
    }

    public void releaseLock() {
        if (((Integer) this.mLockCount.get()).intValue() <= 0) {
            throw new IllegalStateException("Cannot release lock that was not acquired by this thread");
        }
        try {
            this.mLock.lock();
            if (this.mClosed) {
                throw new IllegalStateException("Do not release after the lock has been closed");
            }
            if (!this.mExclusive) {
                this.mSharedLocks--;
            } else if (this.mSharedLocks != 0) {
                throw new AssertionError("Too many shared locks " + this.mSharedLocks);
            }
            int ownedLocks = ((Integer) this.mLockCount.get()).intValue() - 1;
            this.mLockCount.set(Integer.valueOf(ownedLocks));
            if (ownedLocks == 0 && this.mExclusive) {
                this.mExclusive = false;
                this.mCondition.signalAll();
            } else if (ownedLocks == 0 && this.mSharedLocks == 0) {
                this.mCondition.signalAll();
            }
            this.mLock.unlock();
        } catch (Throwable th) {
            this.mLock.unlock();
        }
    }

    private void log(String what) {
        Log.v("CloseableLock[" + this.mName + "]", what);
    }
}
