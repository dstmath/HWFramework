package java.util.concurrent;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class CountDownLatch {
    private final Sync sync;

    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        /* access modifiers changed from: package-private */
        public int getCount() {
            return getState();
        }

        /* access modifiers changed from: protected */
        public int tryAcquireShared(int acquires) {
            return getState() == 0 ? 1 : -1;
        }

        /* access modifiers changed from: protected */
        public boolean tryReleaseShared(int releases) {
            int c;
            boolean z;
            int nextc;
            do {
                c = getState();
                z = false;
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
        if (count >= 0) {
            this.sync = new Sync(count);
            return;
        }
        throw new IllegalArgumentException("count < 0");
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
