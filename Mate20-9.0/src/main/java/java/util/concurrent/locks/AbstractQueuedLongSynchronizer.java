package java.util.concurrent.locks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import sun.misc.Unsafe;

public abstract class AbstractQueuedLongSynchronizer extends AbstractOwnableSynchronizer implements Serializable {
    private static final long HEAD;
    static final long SPIN_FOR_TIMEOUT_THRESHOLD = 1000;
    private static final long STATE;
    private static final long TAIL;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = 7373984972572414692L;
    private volatile transient AbstractQueuedSynchronizer.Node head;
    private volatile long state;
    private volatile transient AbstractQueuedSynchronizer.Node tail;

    public class ConditionObject implements Condition, Serializable {
        private static final int REINTERRUPT = 1;
        private static final int THROW_IE = -1;
        private static final long serialVersionUID = 1173984872572414699L;
        private transient AbstractQueuedSynchronizer.Node firstWaiter;
        private transient AbstractQueuedSynchronizer.Node lastWaiter;

        public ConditionObject() {
        }

        private AbstractQueuedSynchronizer.Node addConditionWaiter() {
            AbstractQueuedSynchronizer.Node t = this.lastWaiter;
            if (!(t == null || t.waitStatus == -2)) {
                unlinkCancelledWaiters();
                t = this.lastWaiter;
            }
            AbstractQueuedSynchronizer.Node node = new AbstractQueuedSynchronizer.Node(-2);
            if (t == null) {
                this.firstWaiter = node;
            } else {
                t.nextWaiter = node;
            }
            this.lastWaiter = node;
            return node;
        }

        private void doSignal(AbstractQueuedSynchronizer.Node first) {
            AbstractQueuedSynchronizer.Node node;
            do {
                AbstractQueuedSynchronizer.Node node2 = first.nextWaiter;
                this.firstWaiter = node2;
                if (node2 == null) {
                    this.lastWaiter = null;
                }
                first.nextWaiter = null;
                if (!AbstractQueuedLongSynchronizer.this.transferForSignal(first)) {
                    node = this.firstWaiter;
                    first = node;
                } else {
                    return;
                }
            } while (node != null);
        }

        private void doSignalAll(AbstractQueuedSynchronizer.Node first) {
            this.firstWaiter = null;
            this.lastWaiter = null;
            do {
                AbstractQueuedSynchronizer.Node next = first.nextWaiter;
                first.nextWaiter = null;
                AbstractQueuedLongSynchronizer.this.transferForSignal(first);
                first = next;
            } while (first != null);
        }

        private void unlinkCancelledWaiters() {
            AbstractQueuedSynchronizer.Node t = this.firstWaiter;
            AbstractQueuedSynchronizer.Node trail = null;
            while (t != null) {
                AbstractQueuedSynchronizer.Node next = t.nextWaiter;
                if (t.waitStatus != -2) {
                    t.nextWaiter = null;
                    if (trail == null) {
                        this.firstWaiter = next;
                    } else {
                        trail.nextWaiter = next;
                    }
                    if (next == null) {
                        this.lastWaiter = trail;
                    }
                } else {
                    trail = t;
                }
                t = next;
            }
        }

        public final void signal() {
            if (AbstractQueuedLongSynchronizer.this.isHeldExclusively()) {
                AbstractQueuedSynchronizer.Node first = this.firstWaiter;
                if (first != null) {
                    doSignal(first);
                    return;
                }
                return;
            }
            throw new IllegalMonitorStateException();
        }

        public final void signalAll() {
            if (AbstractQueuedLongSynchronizer.this.isHeldExclusively()) {
                AbstractQueuedSynchronizer.Node first = this.firstWaiter;
                if (first != null) {
                    doSignalAll(first);
                    return;
                }
                return;
            }
            throw new IllegalMonitorStateException();
        }

        public final void awaitUninterruptibly() {
            AbstractQueuedSynchronizer.Node node = addConditionWaiter();
            long savedState = AbstractQueuedLongSynchronizer.this.fullyRelease(node);
            boolean interrupted = false;
            while (!AbstractQueuedLongSynchronizer.this.isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted()) {
                    interrupted = true;
                }
            }
            if (AbstractQueuedLongSynchronizer.this.acquireQueued(node, savedState) || interrupted) {
                AbstractQueuedLongSynchronizer.selfInterrupt();
            }
        }

        private int checkInterruptWhileWaiting(AbstractQueuedSynchronizer.Node node) {
            if (Thread.interrupted()) {
                return AbstractQueuedLongSynchronizer.this.transferAfterCancelledWait(node) ? -1 : 1;
            }
            return 0;
        }

        private void reportInterruptAfterWait(int interruptMode) throws InterruptedException {
            if (interruptMode == -1) {
                throw new InterruptedException();
            } else if (interruptMode == 1) {
                AbstractQueuedLongSynchronizer.selfInterrupt();
            }
        }

        public final void await() throws InterruptedException {
            if (!Thread.interrupted()) {
                AbstractQueuedSynchronizer.Node node = addConditionWaiter();
                long savedState = AbstractQueuedLongSynchronizer.this.fullyRelease(node);
                int interruptMode = 0;
                while (!AbstractQueuedLongSynchronizer.this.isOnSyncQueue(node)) {
                    LockSupport.park(this);
                    int checkInterruptWhileWaiting = checkInterruptWhileWaiting(node);
                    interruptMode = checkInterruptWhileWaiting;
                    if (checkInterruptWhileWaiting != 0) {
                        break;
                    }
                }
                if (AbstractQueuedLongSynchronizer.this.acquireQueued(node, savedState) && interruptMode != -1) {
                    interruptMode = 1;
                }
                if (node.nextWaiter != null) {
                    unlinkCancelledWaiters();
                }
                if (interruptMode != 0) {
                    reportInterruptAfterWait(interruptMode);
                    return;
                }
                return;
            }
            throw new InterruptedException();
        }

        public final long awaitNanos(long nanosTimeout) throws InterruptedException {
            if (!Thread.interrupted()) {
                long deadline = System.nanoTime() + nanosTimeout;
                long initialNanos = nanosTimeout;
                AbstractQueuedSynchronizer.Node node = addConditionWaiter();
                long savedState = AbstractQueuedLongSynchronizer.this.fullyRelease(node);
                int interruptMode = 0;
                while (true) {
                    if (AbstractQueuedLongSynchronizer.this.isOnSyncQueue(node)) {
                        break;
                    } else if (nanosTimeout <= 0) {
                        AbstractQueuedLongSynchronizer.this.transferAfterCancelledWait(node);
                        break;
                    } else {
                        if (nanosTimeout > AbstractQueuedLongSynchronizer.SPIN_FOR_TIMEOUT_THRESHOLD) {
                            LockSupport.parkNanos(this, nanosTimeout);
                        }
                        int checkInterruptWhileWaiting = checkInterruptWhileWaiting(node);
                        interruptMode = checkInterruptWhileWaiting;
                        if (checkInterruptWhileWaiting != 0) {
                            break;
                        }
                        nanosTimeout = deadline - System.nanoTime();
                    }
                }
                if (AbstractQueuedLongSynchronizer.this.acquireQueued(node, savedState) && interruptMode != -1) {
                    interruptMode = 1;
                }
                if (node.nextWaiter != null) {
                    unlinkCancelledWaiters();
                }
                if (interruptMode != 0) {
                    reportInterruptAfterWait(interruptMode);
                }
                long remaining = deadline - System.nanoTime();
                if (remaining <= initialNanos) {
                    return remaining;
                }
                return Long.MIN_VALUE;
            }
            throw new InterruptedException();
        }

        public final boolean awaitUntil(Date deadline) throws InterruptedException {
            long abstime = deadline.getTime();
            if (!Thread.interrupted()) {
                AbstractQueuedSynchronizer.Node node = addConditionWaiter();
                long savedState = AbstractQueuedLongSynchronizer.this.fullyRelease(node);
                boolean timedout = false;
                int interruptMode = 0;
                while (true) {
                    if (!AbstractQueuedLongSynchronizer.this.isOnSyncQueue(node)) {
                        if (System.currentTimeMillis() < abstime) {
                            LockSupport.parkUntil(this, abstime);
                            int checkInterruptWhileWaiting = checkInterruptWhileWaiting(node);
                            interruptMode = checkInterruptWhileWaiting;
                            if (checkInterruptWhileWaiting != 0) {
                                break;
                            }
                        } else {
                            timedout = AbstractQueuedLongSynchronizer.this.transferAfterCancelledWait(node);
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (AbstractQueuedLongSynchronizer.this.acquireQueued(node, savedState) && interruptMode != -1) {
                    interruptMode = 1;
                }
                if (node.nextWaiter != null) {
                    unlinkCancelledWaiters();
                }
                if (interruptMode != 0) {
                    reportInterruptAfterWait(interruptMode);
                }
                if (!timedout) {
                    return true;
                }
                return false;
            }
            throw new InterruptedException();
        }

        public final boolean await(long time, TimeUnit unit) throws InterruptedException {
            long nanosTimeout = unit.toNanos(time);
            if (!Thread.interrupted()) {
                long deadline = System.nanoTime() + nanosTimeout;
                AbstractQueuedSynchronizer.Node node = addConditionWaiter();
                long savedState = AbstractQueuedLongSynchronizer.this.fullyRelease(node);
                boolean timedout = false;
                long nanosTimeout2 = nanosTimeout;
                int interruptMode = 0;
                while (true) {
                    if (AbstractQueuedLongSynchronizer.this.isOnSyncQueue(node)) {
                        break;
                    } else if (nanosTimeout2 <= 0) {
                        timedout = AbstractQueuedLongSynchronizer.this.transferAfterCancelledWait(node);
                        break;
                    } else {
                        if (nanosTimeout2 > AbstractQueuedLongSynchronizer.SPIN_FOR_TIMEOUT_THRESHOLD) {
                            LockSupport.parkNanos(this, nanosTimeout2);
                        }
                        int checkInterruptWhileWaiting = checkInterruptWhileWaiting(node);
                        interruptMode = checkInterruptWhileWaiting;
                        if (checkInterruptWhileWaiting != 0) {
                            break;
                        }
                        nanosTimeout2 = deadline - System.nanoTime();
                    }
                }
                if (AbstractQueuedLongSynchronizer.this.acquireQueued(node, savedState) && interruptMode != -1) {
                    interruptMode = 1;
                }
                if (node.nextWaiter != null) {
                    unlinkCancelledWaiters();
                }
                if (interruptMode != 0) {
                    reportInterruptAfterWait(interruptMode);
                }
                if (!timedout) {
                    return true;
                }
                return false;
            }
            throw new InterruptedException();
        }

        /* access modifiers changed from: package-private */
        public final boolean isOwnedBy(AbstractQueuedLongSynchronizer sync) {
            return sync == AbstractQueuedLongSynchronizer.this;
        }

        /* access modifiers changed from: protected */
        public final boolean hasWaiters() {
            if (AbstractQueuedLongSynchronizer.this.isHeldExclusively()) {
                for (AbstractQueuedSynchronizer.Node w = this.firstWaiter; w != null; w = w.nextWaiter) {
                    if (w.waitStatus == -2) {
                        return true;
                    }
                }
                return false;
            }
            throw new IllegalMonitorStateException();
        }

        /* access modifiers changed from: protected */
        public final int getWaitQueueLength() {
            if (AbstractQueuedLongSynchronizer.this.isHeldExclusively()) {
                int n = 0;
                for (AbstractQueuedSynchronizer.Node w = this.firstWaiter; w != null; w = w.nextWaiter) {
                    if (w.waitStatus == -2) {
                        n++;
                    }
                }
                return n;
            }
            throw new IllegalMonitorStateException();
        }

        /* access modifiers changed from: protected */
        public final Collection<Thread> getWaitingThreads() {
            if (AbstractQueuedLongSynchronizer.this.isHeldExclusively()) {
                ArrayList<Thread> list = new ArrayList<>();
                for (AbstractQueuedSynchronizer.Node w = this.firstWaiter; w != null; w = w.nextWaiter) {
                    if (w.waitStatus == -2) {
                        Thread t = w.thread;
                        if (t != null) {
                            list.add(t);
                        }
                    }
                }
                return list;
            }
            throw new IllegalMonitorStateException();
        }
    }

    protected AbstractQueuedLongSynchronizer() {
    }

    /* access modifiers changed from: protected */
    public final long getState() {
        return this.state;
    }

    /* access modifiers changed from: protected */
    public final void setState(long newState) {
        U.putLongVolatile(this, STATE, newState);
    }

    /* access modifiers changed from: protected */
    public final boolean compareAndSetState(long expect, long update) {
        return U.compareAndSwapLong(this, STATE, expect, update);
    }

    private AbstractQueuedSynchronizer.Node enq(AbstractQueuedSynchronizer.Node node) {
        while (true) {
            AbstractQueuedSynchronizer.Node oldTail = this.tail;
            if (oldTail != null) {
                U.putObject(node, AbstractQueuedSynchronizer.Node.PREV, oldTail);
                if (compareAndSetTail(oldTail, node)) {
                    oldTail.next = node;
                    return oldTail;
                }
            } else {
                initializeSyncQueue();
            }
        }
    }

    private AbstractQueuedSynchronizer.Node addWaiter(AbstractQueuedSynchronizer.Node mode) {
        AbstractQueuedSynchronizer.Node node = new AbstractQueuedSynchronizer.Node(mode);
        while (true) {
            AbstractQueuedSynchronizer.Node oldTail = this.tail;
            if (oldTail != null) {
                U.putObject(node, AbstractQueuedSynchronizer.Node.PREV, oldTail);
                if (compareAndSetTail(oldTail, node)) {
                    oldTail.next = node;
                    return node;
                }
            } else {
                initializeSyncQueue();
            }
        }
    }

    private void setHead(AbstractQueuedSynchronizer.Node node) {
        this.head = node;
        node.thread = null;
        node.prev = null;
    }

    private void unparkSuccessor(AbstractQueuedSynchronizer.Node node) {
        int ws = node.waitStatus;
        if (ws < 0) {
            node.compareAndSetWaitStatus(ws, 0);
        }
        AbstractQueuedSynchronizer.Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            AbstractQueuedSynchronizer.Node p = this.tail;
            while (p != node && p != null) {
                if (p.waitStatus <= 0) {
                    s = p;
                }
                p = p.prev;
            }
        }
        if (s != null) {
            LockSupport.unpark(s.thread);
        }
    }

    private void doReleaseShared() {
        while (true) {
            AbstractQueuedSynchronizer.Node h = this.head;
            if (!(h == null || h == this.tail)) {
                int ws = h.waitStatus;
                if (ws == -1) {
                    if (!h.compareAndSetWaitStatus(-1, 0)) {
                        continue;
                    } else {
                        unparkSuccessor(h);
                    }
                } else if (ws == 0 && !h.compareAndSetWaitStatus(0, -3)) {
                }
            }
            if (h == this.head) {
                return;
            }
        }
    }

    private void setHeadAndPropagate(AbstractQueuedSynchronizer.Node node, long propagate) {
        AbstractQueuedSynchronizer.Node h = this.head;
        setHead(node);
        if (propagate <= 0 && h != null && h.waitStatus >= 0) {
            AbstractQueuedSynchronizer.Node node2 = this.head;
            AbstractQueuedSynchronizer.Node h2 = node2;
            if (node2 != null && h2.waitStatus >= 0) {
                return;
            }
        }
        AbstractQueuedSynchronizer.Node s = node.next;
        if (s == null || s.isShared()) {
            doReleaseShared();
        }
    }

    private void cancelAcquire(AbstractQueuedSynchronizer.Node node) {
        if (node != null) {
            node.thread = null;
            AbstractQueuedSynchronizer.Node pred = node.prev;
            while (pred.waitStatus > 0) {
                AbstractQueuedSynchronizer.Node node2 = pred.prev;
                pred = node2;
                node.prev = node2;
            }
            AbstractQueuedSynchronizer.Node predNext = pred.next;
            node.waitStatus = 1;
            if (node != this.tail || !compareAndSetTail(node, pred)) {
                if (pred != this.head) {
                    int i = pred.waitStatus;
                    int ws = i;
                    if ((i == -1 || (ws <= 0 && pred.compareAndSetWaitStatus(ws, -1))) && pred.thread != null) {
                        AbstractQueuedSynchronizer.Node next = node.next;
                        if (next != null && next.waitStatus <= 0) {
                            pred.compareAndSetNext(predNext, next);
                        }
                        node.next = node;
                    }
                }
                unparkSuccessor(node);
                node.next = node;
            } else {
                pred.compareAndSetNext(predNext, null);
            }
        }
    }

    private static boolean shouldParkAfterFailedAcquire(AbstractQueuedSynchronizer.Node pred, AbstractQueuedSynchronizer.Node node) {
        int ws = pred.waitStatus;
        if (ws == -1) {
            return true;
        }
        if (ws > 0) {
            do {
                AbstractQueuedSynchronizer.Node node2 = pred.prev;
                pred = node2;
                node.prev = node2;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            pred.compareAndSetWaitStatus(ws, -1);
        }
        return false;
    }

    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }

    /* access modifiers changed from: package-private */
    public final boolean acquireQueued(AbstractQueuedSynchronizer.Node node, long arg) {
        boolean interrupted = false;
        while (true) {
            try {
                AbstractQueuedSynchronizer.Node p = node.predecessor();
                if (p == this.head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null;
                    return interrupted;
                } else if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) {
                    interrupted = true;
                }
            } catch (Throwable t) {
                cancelAcquire(node);
                throw t;
            }
        }
    }

    private void doAcquireInterruptibly(long arg) throws InterruptedException {
        AbstractQueuedSynchronizer.Node node = addWaiter(AbstractQueuedSynchronizer.Node.EXCLUSIVE);
        while (true) {
            try {
                AbstractQueuedSynchronizer.Node p = node.predecessor();
                if (p == this.head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null;
                    return;
                } else if (shouldParkAfterFailedAcquire(p, node)) {
                    if (parkAndCheckInterrupt()) {
                        throw new InterruptedException();
                    }
                }
            } catch (Throwable t) {
                cancelAcquire(node);
                throw t;
            }
        }
    }

    private boolean doAcquireNanos(long arg, long nanosTimeout) throws InterruptedException {
        if (nanosTimeout <= 0) {
            return false;
        }
        long deadline = System.nanoTime() + nanosTimeout;
        AbstractQueuedSynchronizer.Node node = addWaiter(AbstractQueuedSynchronizer.Node.EXCLUSIVE);
        while (true) {
            try {
                AbstractQueuedSynchronizer.Node p = node.predecessor();
                if (p != this.head || !tryAcquire(arg)) {
                    long nanosTimeout2 = deadline - System.nanoTime();
                    if (nanosTimeout2 <= 0) {
                        return false;
                    }
                    if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout2 > SPIN_FOR_TIMEOUT_THRESHOLD) {
                        LockSupport.parkNanos(this, nanosTimeout2);
                    }
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                } else {
                    setHead(node);
                    p.next = null;
                    return true;
                }
            } finally {
                cancelAcquire(node);
            }
        }
    }

    private void doAcquireShared(long arg) {
        AbstractQueuedSynchronizer.Node p;
        long r;
        AbstractQueuedSynchronizer.Node node = addWaiter(AbstractQueuedSynchronizer.Node.SHARED);
        boolean interrupted = false;
        while (true) {
            try {
                p = node.predecessor();
                if (p == this.head) {
                    r = tryAcquireShared(arg);
                    if (r >= 0) {
                        break;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) {
                    interrupted = true;
                }
            } catch (Throwable t) {
                cancelAcquire(node);
                throw t;
            }
        }
        setHeadAndPropagate(node, r);
        p.next = null;
        if (interrupted) {
            selfInterrupt();
        }
    }

    private void doAcquireSharedInterruptibly(long arg) throws InterruptedException {
        AbstractQueuedSynchronizer.Node node = addWaiter(AbstractQueuedSynchronizer.Node.SHARED);
        while (true) {
            try {
                AbstractQueuedSynchronizer.Node p = node.predecessor();
                if (p == this.head) {
                    long r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node)) {
                    if (parkAndCheckInterrupt()) {
                        throw new InterruptedException();
                    }
                }
            } catch (Throwable t) {
                cancelAcquire(node);
                throw t;
            }
        }
    }

    private boolean doAcquireSharedNanos(long arg, long nanosTimeout) throws InterruptedException {
        if (nanosTimeout <= 0) {
            return false;
        }
        long deadline = System.nanoTime() + nanosTimeout;
        AbstractQueuedSynchronizer.Node node = addWaiter(AbstractQueuedSynchronizer.Node.SHARED);
        while (true) {
            try {
                AbstractQueuedSynchronizer.Node p = node.predecessor();
                if (p == this.head) {
                    long r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null;
                        return true;
                    }
                }
                long nanosTimeout2 = deadline - System.nanoTime();
                if (nanosTimeout2 <= 0) {
                    return false;
                }
                if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout2 > SPIN_FOR_TIMEOUT_THRESHOLD) {
                    LockSupport.parkNanos(this, nanosTimeout2);
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            } finally {
                cancelAcquire(node);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean tryAcquire(long arg) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public boolean tryRelease(long arg) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public long tryAcquireShared(long arg) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public boolean tryReleaseShared(long arg) {
        throw new UnsupportedOperationException();
    }

    /* access modifiers changed from: protected */
    public boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

    public final void acquire(long arg) {
        if (!tryAcquire(arg) && acquireQueued(addWaiter(AbstractQueuedSynchronizer.Node.EXCLUSIVE), arg)) {
            selfInterrupt();
        }
    }

    public final void acquireInterruptibly(long arg) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        } else if (!tryAcquire(arg)) {
            doAcquireInterruptibly(arg);
        }
    }

    public final boolean tryAcquireNanos(long arg, long nanosTimeout) throws InterruptedException {
        if (!Thread.interrupted()) {
            return tryAcquire(arg) || doAcquireNanos(arg, nanosTimeout);
        }
        throw new InterruptedException();
    }

    public final boolean release(long arg) {
        if (!tryRelease(arg)) {
            return false;
        }
        AbstractQueuedSynchronizer.Node h = this.head;
        if (!(h == null || h.waitStatus == 0)) {
            unparkSuccessor(h);
        }
        return true;
    }

    public final void acquireShared(long arg) {
        if (tryAcquireShared(arg) < 0) {
            doAcquireShared(arg);
        }
    }

    public final void acquireSharedInterruptibly(long arg) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        } else if (tryAcquireShared(arg) < 0) {
            doAcquireSharedInterruptibly(arg);
        }
    }

    public final boolean tryAcquireSharedNanos(long arg, long nanosTimeout) throws InterruptedException {
        if (!Thread.interrupted()) {
            return tryAcquireShared(arg) >= 0 || doAcquireSharedNanos(arg, nanosTimeout);
        }
        throw new InterruptedException();
    }

    public final boolean releaseShared(long arg) {
        if (!tryReleaseShared(arg)) {
            return false;
        }
        doReleaseShared();
        return true;
    }

    public final boolean hasQueuedThreads() {
        return this.head != this.tail;
    }

    public final boolean hasContended() {
        return this.head != null;
    }

    public final Thread getFirstQueuedThread() {
        if (this.head == this.tail) {
            return null;
        }
        return fullGetFirstQueuedThread();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0028, code lost:
        if (r0 != null) goto L_0x002a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002a, code lost:
        return r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0013, code lost:
        if (r0 == null) goto L_0x0015;
     */
    private Thread fullGetFirstQueuedThread() {
        Thread st;
        AbstractQueuedSynchronizer.Node node = this.head;
        AbstractQueuedSynchronizer.Node h = node;
        if (node != null) {
            AbstractQueuedSynchronizer.Node node2 = h.next;
            AbstractQueuedSynchronizer.Node s = node2;
            if (node2 != null && s.prev == this.head) {
                Thread thread = s.thread;
                st = thread;
            }
        }
        AbstractQueuedSynchronizer.Node node3 = this.head;
        AbstractQueuedSynchronizer.Node h2 = node3;
        if (node3 != null) {
            AbstractQueuedSynchronizer.Node node4 = h2.next;
            AbstractQueuedSynchronizer.Node s2 = node4;
            if (node4 != null && s2.prev == this.head) {
                Thread thread2 = s2.thread;
                st = thread2;
            }
        }
        Thread firstThread = null;
        AbstractQueuedSynchronizer.Node p = this.tail;
        while (p != null && p != this.head) {
            Thread t = p.thread;
            if (t != null) {
                firstThread = t;
            }
            p = p.prev;
        }
        return firstThread;
    }

    public final boolean isQueued(Thread thread) {
        if (thread != null) {
            for (AbstractQueuedSynchronizer.Node p = this.tail; p != null; p = p.prev) {
                if (p.thread == thread) {
                    return true;
                }
            }
            return false;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final boolean apparentlyFirstQueuedIsExclusive() {
        AbstractQueuedSynchronizer.Node node = this.head;
        AbstractQueuedSynchronizer.Node h = node;
        if (node != null) {
            AbstractQueuedSynchronizer.Node node2 = h.next;
            AbstractQueuedSynchronizer.Node s = node2;
            if (!(node2 == null || s.isShared() || s.thread == null)) {
                return true;
            }
        }
        return false;
    }

    public final boolean hasQueuedPredecessors() {
        AbstractQueuedSynchronizer.Node t = this.tail;
        AbstractQueuedSynchronizer.Node h = this.head;
        if (h != t) {
            AbstractQueuedSynchronizer.Node node = h.next;
            AbstractQueuedSynchronizer.Node s = node;
            if (node == null || s.thread != Thread.currentThread()) {
                return true;
            }
        }
        return false;
    }

    public final int getQueueLength() {
        int n = 0;
        for (AbstractQueuedSynchronizer.Node p = this.tail; p != null; p = p.prev) {
            if (p.thread != null) {
                n++;
            }
        }
        return n;
    }

    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<>();
        for (AbstractQueuedSynchronizer.Node p = this.tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null) {
                list.add(t);
            }
        }
        return list;
    }

    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<>();
        for (AbstractQueuedSynchronizer.Node p = this.tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<>();
        for (AbstractQueuedSynchronizer.Node p = this.tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("[State = ");
        sb.append(getState());
        sb.append(", ");
        sb.append(hasQueuedThreads() ? "non" : "");
        sb.append("empty queue]");
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public final boolean isOnSyncQueue(AbstractQueuedSynchronizer.Node node) {
        if (node.waitStatus == -2 || node.prev == null) {
            return false;
        }
        if (node.next != null) {
            return true;
        }
        return findNodeFromTail(node);
    }

    private boolean findNodeFromTail(AbstractQueuedSynchronizer.Node node) {
        for (AbstractQueuedSynchronizer.Node p = this.tail; p != node; p = p.prev) {
            if (p == null) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public final boolean transferForSignal(AbstractQueuedSynchronizer.Node node) {
        if (!node.compareAndSetWaitStatus(-2, 0)) {
            return false;
        }
        AbstractQueuedSynchronizer.Node p = enq(node);
        int ws = p.waitStatus;
        if (ws > 0 || !p.compareAndSetWaitStatus(ws, -1)) {
            LockSupport.unpark(node.thread);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public final boolean transferAfterCancelledWait(AbstractQueuedSynchronizer.Node node) {
        if (node.compareAndSetWaitStatus(-2, 0)) {
            enq(node);
            return true;
        }
        while (!isOnSyncQueue(node)) {
            Thread.yield();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final long fullyRelease(AbstractQueuedSynchronizer.Node node) {
        try {
            long savedState = getState();
            if (release(savedState)) {
                return savedState;
            }
            throw new IllegalMonitorStateException();
        } catch (Throwable t) {
            node.waitStatus = 1;
            throw t;
        }
    }

    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    public final boolean hasWaiters(ConditionObject condition) {
        if (owns(condition)) {
            return condition.hasWaiters();
        }
        throw new IllegalArgumentException("Not owner");
    }

    public final int getWaitQueueLength(ConditionObject condition) {
        if (owns(condition)) {
            return condition.getWaitQueueLength();
        }
        throw new IllegalArgumentException("Not owner");
    }

    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (owns(condition)) {
            return condition.getWaitingThreads();
        }
        throw new IllegalArgumentException("Not owner");
    }

    static {
        try {
            STATE = U.objectFieldOffset(AbstractQueuedLongSynchronizer.class.getDeclaredField("state"));
            HEAD = U.objectFieldOffset(AbstractQueuedLongSynchronizer.class.getDeclaredField("head"));
            TAIL = U.objectFieldOffset(AbstractQueuedLongSynchronizer.class.getDeclaredField("tail"));
            Class<LockSupport> cls = LockSupport.class;
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }

    private final void initializeSyncQueue() {
        Unsafe unsafe = U;
        long j = HEAD;
        AbstractQueuedSynchronizer.Node node = new AbstractQueuedSynchronizer.Node();
        AbstractQueuedSynchronizer.Node h = node;
        if (unsafe.compareAndSwapObject(this, j, null, node)) {
            this.tail = h;
        }
    }

    private final boolean compareAndSetTail(AbstractQueuedSynchronizer.Node expect, AbstractQueuedSynchronizer.Node update) {
        return U.compareAndSwapObject(this, TAIL, expect, update);
    }
}
