package ohos.global.icu.impl;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ICURWLock {
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private Stats stats = null;

    public static final class Stats {
        public int _mrc;
        public int _rc;
        public int _wc;
        public int _wrc;
        public int _wwc;

        private Stats() {
        }

        private Stats(int i, int i2, int i3, int i4, int i5) {
            this._rc = i;
            this._mrc = i2;
            this._wrc = i3;
            this._wc = i4;
            this._wwc = i5;
        }

        private Stats(Stats stats) {
            this(stats._rc, stats._mrc, stats._wrc, stats._wc, stats._wwc);
        }

        public String toString() {
            return " rc: " + this._rc + " mrc: " + this._mrc + " wrc: " + this._wrc + " wc: " + this._wc + " wwc: " + this._wwc;
        }
    }

    public synchronized Stats resetStats() {
        Stats stats2;
        stats2 = this.stats;
        this.stats = new Stats();
        return stats2;
    }

    public synchronized Stats clearStats() {
        Stats stats2;
        stats2 = this.stats;
        this.stats = null;
        return stats2;
    }

    public synchronized Stats getStats() {
        return this.stats == null ? null : new Stats(this.stats);
    }

    public void acquireRead() {
        if (this.stats != null) {
            synchronized (this) {
                this.stats._rc++;
                if (this.rwl.getReadLockCount() > 0) {
                    this.stats._mrc++;
                }
                if (this.rwl.isWriteLocked()) {
                    this.stats._wrc++;
                }
            }
        }
        this.rwl.readLock().lock();
    }

    public void releaseRead() {
        this.rwl.readLock().unlock();
    }

    public void acquireWrite() {
        if (this.stats != null) {
            synchronized (this) {
                this.stats._wc++;
                if (this.rwl.getReadLockCount() > 0 || this.rwl.isWriteLocked()) {
                    this.stats._wwc++;
                }
            }
        }
        this.rwl.writeLock().lock();
    }

    public void releaseWrite() {
        this.rwl.writeLock().unlock();
    }
}
