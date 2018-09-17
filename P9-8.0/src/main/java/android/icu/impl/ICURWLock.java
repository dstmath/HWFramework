package android.icu.impl;

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

        /* synthetic */ Stats(Stats rhs, Stats -this1) {
            this(rhs);
        }

        /* synthetic */ Stats(Stats -this0, Stats -this1, Stats -this2) {
            this();
        }

        private Stats() {
        }

        private Stats(int rc, int mrc, int wrc, int wc, int wwc) {
            this._rc = rc;
            this._mrc = mrc;
            this._wrc = wrc;
            this._wc = wc;
            this._wwc = wwc;
        }

        private Stats(Stats rhs) {
            this(rhs._rc, rhs._mrc, rhs._wrc, rhs._wc, rhs._wwc);
        }

        public String toString() {
            return " rc: " + this._rc + " mrc: " + this._mrc + " wrc: " + this._wrc + " wc: " + this._wc + " wwc: " + this._wwc;
        }
    }

    public synchronized Stats resetStats() {
        Stats result;
        result = this.stats;
        this.stats = new Stats();
        return result;
    }

    public synchronized Stats clearStats() {
        Stats result;
        result = this.stats;
        this.stats = null;
        return result;
    }

    public synchronized Stats getStats() {
        Stats stats = null;
        synchronized (this) {
            if (this.stats != null) {
                stats = new Stats(this.stats, null);
            }
        }
        return stats;
    }

    public void acquireRead() {
        if (this.stats != null) {
            synchronized (this) {
                Stats stats = this.stats;
                stats._rc++;
                if (this.rwl.getReadLockCount() > 0) {
                    stats = this.stats;
                    stats._mrc++;
                }
                if (this.rwl.isWriteLocked()) {
                    stats = this.stats;
                    stats._wrc++;
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
                Stats stats = this.stats;
                stats._wc++;
                if (this.rwl.getReadLockCount() > 0 || this.rwl.isWriteLocked()) {
                    stats = this.stats;
                    stats._wwc++;
                }
            }
        }
        this.rwl.writeLock().lock();
    }

    public void releaseWrite() {
        this.rwl.writeLock().unlock();
    }
}
