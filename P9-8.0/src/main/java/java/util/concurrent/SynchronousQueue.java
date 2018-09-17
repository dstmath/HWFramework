package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import sun.misc.Unsafe;

public class SynchronousQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, Serializable {
    static final int MAX_TIMED_SPINS = (Runtime.getRuntime().availableProcessors() < 2 ? 0 : 32);
    static final int MAX_UNTIMED_SPINS = (MAX_TIMED_SPINS * 16);
    static final long SPIN_FOR_TIMEOUT_THRESHOLD = 1000;
    private static final long serialVersionUID = -3223113410248163686L;
    private ReentrantLock qlock;
    private volatile transient Transferer<E> transferer;
    private WaitQueue waitingConsumers;
    private WaitQueue waitingProducers;

    static class WaitQueue implements Serializable {
        WaitQueue() {
        }
    }

    static class FifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3623113410248163686L;

        FifoWaitQueue() {
        }
    }

    static class LifoWaitQueue extends WaitQueue {
        private static final long serialVersionUID = -3633113410248163686L;

        LifoWaitQueue() {
        }
    }

    static abstract class Transferer<E> {
        abstract E transfer(E e, boolean z, long j);

        Transferer() {
        }
    }

    static final class TransferQueue<E> extends Transferer<E> {
        private static final long CLEANME;
        private static final long HEAD;
        private static final long TAIL;
        private static final Unsafe U = Unsafe.getUnsafe();
        volatile transient QNode cleanMe;
        volatile transient QNode head;
        volatile transient QNode tail;

        static final class QNode {
            private static final long ITEM;
            private static final long NEXT;
            private static final Unsafe U = Unsafe.getUnsafe();
            final boolean isData;
            volatile Object item;
            volatile QNode next;
            volatile Thread waiter;

            QNode(Object item, boolean isData) {
                this.item = item;
                this.isData = isData;
            }

            boolean casNext(QNode cmp, QNode val) {
                if (this.next != cmp) {
                    return false;
                }
                return U.compareAndSwapObject(this, NEXT, cmp, val);
            }

            boolean casItem(Object cmp, Object val) {
                if (this.item != cmp) {
                    return false;
                }
                return U.compareAndSwapObject(this, ITEM, cmp, val);
            }

            void tryCancel(Object cmp) {
                U.compareAndSwapObject(this, ITEM, cmp, this);
            }

            boolean isCancelled() {
                return this.item == this;
            }

            boolean isOffList() {
                return this.next == this;
            }

            static {
                try {
                    ITEM = U.objectFieldOffset(QNode.class.getDeclaredField("item"));
                    NEXT = U.objectFieldOffset(QNode.class.getDeclaredField("next"));
                } catch (Throwable e) {
                    throw new Error(e);
                }
            }
        }

        TransferQueue() {
            QNode h = new QNode(null, false);
            this.head = h;
            this.tail = h;
        }

        void advanceHead(QNode h, QNode nh) {
            if (h == this.head) {
                if (U.compareAndSwapObject(this, HEAD, h, nh)) {
                    h.next = h;
                }
            }
        }

        void advanceTail(QNode t, QNode nt) {
            if (this.tail == t) {
                U.compareAndSwapObject(this, TAIL, t, nt);
            }
        }

        boolean casCleanMe(QNode cmp, QNode val) {
            if (this.cleanMe != cmp) {
                return false;
            }
            return U.compareAndSwapObject(this, CLEANME, cmp, val);
        }

        E transfer(E e, boolean timed, long nanos) {
            QNode s = null;
            boolean isData = e != null;
            while (true) {
                QNode t = this.tail;
                QNode h = this.head;
                if (!(t == null || h == null)) {
                    Object x;
                    E x2;
                    if (h == t || t.isData == isData) {
                        QNode tn = t.next;
                        if (t != this.tail) {
                            continue;
                        } else if (tn != null) {
                            advanceTail(t, tn);
                        } else if (timed && nanos <= 0) {
                            return null;
                        } else {
                            if (s == null) {
                                s = new QNode(e, isData);
                            }
                            if (t.casNext(null, s)) {
                                advanceTail(t, s);
                                x2 = awaitFulfill(s, e, timed, nanos);
                                if (x2 == s) {
                                    clean(t, s);
                                    return null;
                                }
                                if (!s.isOffList()) {
                                    advanceHead(t, s);
                                    if (x2 != null) {
                                        s.item = s;
                                    }
                                    s.waiter = null;
                                }
                                if (x2 == null) {
                                    x2 = e;
                                }
                                return x2;
                            }
                        }
                    } else {
                        QNode m = h.next;
                        if (t == this.tail && m != null && h == this.head) {
                            x2 = m.item;
                            if (isData == (x2 != null) || x2 == m || (m.casItem(x2, e) ^ 1) != 0) {
                                advanceHead(h, m);
                            } else {
                                advanceHead(h, m);
                                LockSupport.unpark(m.waiter);
                                if (x2 == null) {
                                    x2 = e;
                                }
                                return x2;
                            }
                        }
                    }
                }
            }
        }

        Object awaitFulfill(QNode s, E e, boolean timed, long nanos) {
            long deadline = timed ? System.nanoTime() + nanos : 0;
            Thread w = Thread.currentThread();
            int spins = this.head.next == s ? timed ? SynchronousQueue.MAX_TIMED_SPINS : SynchronousQueue.MAX_UNTIMED_SPINS : 0;
            while (true) {
                if (w.isInterrupted()) {
                    s.tryCancel(e);
                }
                E x = s.item;
                if (x != e) {
                    return x;
                }
                if (timed) {
                    nanos = deadline - System.nanoTime();
                    if (nanos <= 0) {
                        s.tryCancel(e);
                    }
                }
                if (spins > 0) {
                    spins--;
                } else if (s.waiter == null) {
                    s.waiter = w;
                } else if (!timed) {
                    LockSupport.park(this);
                } else if (nanos > SynchronousQueue.SPIN_FOR_TIMEOUT_THRESHOLD) {
                    LockSupport.parkNanos(this, nanos);
                }
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:51:0x0003 A:{SYNTHETIC} */
        /* JADX WARNING: Removed duplicated region for block: B:42:0x0044 A:{SYNTHETIC} */
        /* JADX WARNING: Missing block: B:36:0x0059, code:
            if (r2.casNext(r0, r1) != false) goto L_0x003f;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void clean(QNode pred, QNode s) {
            s.waiter = null;
            while (pred.next == s) {
                QNode h = this.head;
                QNode hn = h.next;
                if (hn == null || !hn.isCancelled()) {
                    QNode t = this.tail;
                    if (t != h) {
                        QNode tn = t.next;
                        if (t != this.tail) {
                            continue;
                        } else if (tn != null) {
                            advanceTail(t, tn);
                        } else {
                            if (s != t) {
                                QNode sn = s.next;
                                if (sn == s || pred.casNext(s, sn)) {
                                    return;
                                }
                            }
                            QNode dp = this.cleanMe;
                            if (dp != null) {
                                QNode d = dp.next;
                                if (!(d == null || d == dp || (d.isCancelled() ^ 1) != 0)) {
                                    if (d != t) {
                                        QNode dn = d.next;
                                        if (dn != null) {
                                            if (dn != d) {
                                            }
                                        }
                                    }
                                    if (dp != pred) {
                                        return;
                                    }
                                }
                                casCleanMe(dp, null);
                                if (dp != pred) {
                                }
                            } else if (casCleanMe(null, pred)) {
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                }
                advanceHead(h, hn);
            }
        }

        static {
            try {
                HEAD = U.objectFieldOffset(TransferQueue.class.getDeclaredField("head"));
                TAIL = U.objectFieldOffset(TransferQueue.class.getDeclaredField("tail"));
                CLEANME = U.objectFieldOffset(TransferQueue.class.getDeclaredField("cleanMe"));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
    }

    static final class TransferStack<E> extends Transferer<E> {
        static final int DATA = 1;
        static final int FULFILLING = 2;
        private static final long HEAD;
        static final int REQUEST = 0;
        private static final Unsafe U = Unsafe.getUnsafe();
        volatile SNode head;

        static final class SNode {
            private static final long MATCH;
            private static final long NEXT;
            private static final Unsafe U = Unsafe.getUnsafe();
            Object item;
            volatile SNode match;
            int mode;
            volatile SNode next;
            volatile Thread waiter;

            SNode(Object item) {
                this.item = item;
            }

            boolean casNext(SNode cmp, SNode val) {
                if (cmp != this.next) {
                    return false;
                }
                return U.compareAndSwapObject(this, NEXT, cmp, val);
            }

            boolean tryMatch(SNode s) {
                if (this.match == null) {
                    if (U.compareAndSwapObject(this, MATCH, null, s)) {
                        Thread w = this.waiter;
                        if (w != null) {
                            this.waiter = null;
                            LockSupport.unpark(w);
                        }
                        return true;
                    }
                }
                return this.match == s;
            }

            void tryCancel() {
                U.compareAndSwapObject(this, MATCH, null, this);
            }

            boolean isCancelled() {
                return this.match == this;
            }

            static {
                try {
                    MATCH = U.objectFieldOffset(SNode.class.getDeclaredField("match"));
                    NEXT = U.objectFieldOffset(SNode.class.getDeclaredField("next"));
                } catch (Throwable e) {
                    throw new Error(e);
                }
            }
        }

        TransferStack() {
        }

        static boolean isFulfilling(int m) {
            return (m & 2) != 0;
        }

        boolean casHead(SNode h, SNode nh) {
            if (h != this.head) {
                return false;
            }
            return U.compareAndSwapObject(this, HEAD, h, nh);
        }

        static SNode snode(SNode s, Object e, SNode next, int mode) {
            if (s == null) {
                s = new SNode(e);
            }
            s.mode = mode;
            s.next = next;
            return s;
        }

        E transfer(E e, boolean timed, long nanos) {
            SNode s = null;
            int mode = e == null ? 0 : 1;
            while (true) {
                SNode h = this.head;
                SNode m;
                SNode mn;
                if (h == null || h.mode == mode) {
                    if (!timed || nanos > 0) {
                        s = snode(s, e, h, mode);
                        if (casHead(h, s)) {
                            m = awaitFulfill(s, timed, nanos);
                            if (m == s) {
                                clean(s);
                                return null;
                            }
                            h = this.head;
                            if (h != null && h.next == s) {
                                casHead(h, s.next);
                            }
                            return mode == 0 ? m.item : s.item;
                        }
                    } else if (h == null || !h.isCancelled()) {
                        return null;
                    } else {
                        casHead(h, h.next);
                    }
                } else if (isFulfilling(h.mode)) {
                    m = h.next;
                    if (m == null) {
                        casHead(h, null);
                    } else {
                        mn = m.next;
                        if (m.tryMatch(h)) {
                            casHead(h, mn);
                        } else {
                            h.casNext(m, mn);
                        }
                    }
                } else if (h.isCancelled()) {
                    casHead(h, h.next);
                } else {
                    s = snode(s, e, h, mode | 2);
                    if (casHead(h, s)) {
                        while (true) {
                            m = s.next;
                            if (m == null) {
                                casHead(s, null);
                                s = null;
                                break;
                            }
                            mn = m.next;
                            if (m.tryMatch(s)) {
                                casHead(s, mn);
                                return mode == 0 ? m.item : s.item;
                            }
                            s.casNext(m, mn);
                        }
                    } else {
                        continue;
                    }
                }
            }
            return null;
        }

        SNode awaitFulfill(SNode s, boolean timed, long nanos) {
            long deadline = timed ? System.nanoTime() + nanos : 0;
            Thread w = Thread.currentThread();
            int spins = shouldSpin(s) ? timed ? SynchronousQueue.MAX_TIMED_SPINS : SynchronousQueue.MAX_UNTIMED_SPINS : 0;
            while (true) {
                if (w.isInterrupted()) {
                    s.tryCancel();
                }
                SNode m = s.match;
                if (m != null) {
                    return m;
                }
                if (timed) {
                    nanos = deadline - System.nanoTime();
                    if (nanos <= 0) {
                        s.tryCancel();
                    }
                }
                if (spins > 0) {
                    spins = shouldSpin(s) ? spins - 1 : 0;
                } else if (s.waiter == null) {
                    s.waiter = w;
                } else if (!timed) {
                    LockSupport.park(this);
                } else if (nanos > SynchronousQueue.SPIN_FOR_TIMEOUT_THRESHOLD) {
                    LockSupport.parkNanos(this, nanos);
                }
            }
        }

        boolean shouldSpin(SNode s) {
            SNode h = this.head;
            return (h == s || h == null) ? true : isFulfilling(h.mode);
        }

        /* JADX WARNING: Missing block: B:28:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void clean(SNode s) {
            SNode p;
            SNode n;
            s.item = null;
            s.waiter = null;
            SNode past = s.next;
            if (past != null && past.isCancelled()) {
                past = past.next;
            }
            while (true) {
                p = this.head;
                if (p == null || p == past || !p.isCancelled()) {
                    while (p != null && p != past) {
                        n = p.next;
                        if (n == null && n.isCancelled()) {
                            p.casNext(n, n.next);
                        } else {
                            p = n;
                        }
                    }
                } else {
                    casHead(p, p.next);
                }
            }
            while (p != null) {
                n = p.next;
                if (n == null) {
                }
                p = n;
            }
        }

        static {
            try {
                HEAD = U.objectFieldOffset(TransferStack.class.getDeclaredField("head"));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
    }

    static {
        Class<?> ensureLoaded = LockSupport.class;
    }

    public SynchronousQueue() {
        this(false);
    }

    public SynchronousQueue(boolean fair) {
        this.transferer = fair ? new TransferQueue() : new TransferStack();
    }

    public void put(E e) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        } else if (this.transferer.transfer(e, false, 0) == null) {
            Thread.interrupted();
            throw new InterruptedException();
        }
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        } else if (this.transferer.transfer(e, true, unit.toNanos(timeout)) != null) {
            return true;
        } else {
            if (!Thread.interrupted()) {
                return false;
            }
            throw new InterruptedException();
        }
    }

    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        } else if (this.transferer.transfer(e, true, 0) != null) {
            return true;
        } else {
            return false;
        }
    }

    public E take() throws InterruptedException {
        E e = this.transferer.transfer(null, false, 0);
        if (e != null) {
            return e;
        }
        Thread.interrupted();
        throw new InterruptedException();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = this.transferer.transfer(null, true, unit.toNanos(timeout));
        if (e != null || (Thread.interrupted() ^ 1) != 0) {
            return e;
        }
        throw new InterruptedException();
    }

    public E poll() {
        return this.transferer.transfer(null, true, 0);
    }

    public boolean isEmpty() {
        return true;
    }

    public int size() {
        return 0;
    }

    public int remainingCapacity() {
        return 0;
    }

    public void clear() {
    }

    public boolean contains(Object o) {
        return false;
    }

    public boolean remove(Object o) {
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        return c.isEmpty();
    }

    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    public E peek() {
        return null;
    }

    public Iterator<E> iterator() {
        return Collections.emptyIterator();
    }

    public Spliterator<E> spliterator() {
        return Spliterators.emptySpliterator();
    }

    public Object[] toArray() {
        return new Object[0];
    }

    public <T> T[] toArray(T[] a) {
        if (a.length > 0) {
            a[0] = null;
        }
        return a;
    }

    public String toString() {
        return "[]";
    }

    public int drainTo(Collection<? super E> c) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c == this) {
            throw new IllegalArgumentException();
        } else {
            int n = 0;
            while (true) {
                E e = poll();
                if (e == null) {
                    return n;
                }
                c.add(e);
                n++;
            }
        }
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c == this) {
            throw new IllegalArgumentException();
        } else {
            int n = 0;
            while (n < maxElements) {
                E e = poll();
                if (e == null) {
                    break;
                }
                c.add(e);
                n++;
            }
            return n;
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        if (this.transferer instanceof TransferQueue) {
            this.qlock = new ReentrantLock(true);
            this.waitingProducers = new FifoWaitQueue();
            this.waitingConsumers = new FifoWaitQueue();
        } else {
            this.qlock = new ReentrantLock();
            this.waitingProducers = new LifoWaitQueue();
            this.waitingConsumers = new LifoWaitQueue();
        }
        s.defaultWriteObject();
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (this.waitingProducers instanceof FifoWaitQueue) {
            this.transferer = new TransferQueue();
        } else {
            this.transferer = new TransferStack();
        }
    }
}
