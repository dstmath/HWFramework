package java.util.concurrent;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class CountDownLatch {
    private final Sync sync;

    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        protected int tryAcquireShared(int acquires) {
            return getState() == 0 ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            int nextc;
            boolean z = false;
            int c;
            do {
                c = getState();
                if (c == 0) {
                    return false;
                }
                nextc = c - 1;
            } while (!compareAndSetState(c, nextc));
            if (nextc == 0) {
                z = true;
            }
            return z;
        }
    }

    public CountDownLatch(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        }
        this.sync = new Sync(count);
    }

    public void await() throws InterruptedException {
        this.sync.acquireSharedInterruptibly(1);
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return this.sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    public void countDown() {
        this.sync.releaseShared(1);
    }

    public long getCount() {
        return (long) this.sync.getCount();
    }

    public String toString() {
        return super.toString() + "[Count = " + this.sync.getCount() + "]";
    }
}
