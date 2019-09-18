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

            QNode(Object item2, boolean isData2) {
                this.item = item2;
                this.isData = isData2;
            }

            /* access modifiers changed from: package-private */
            public boolean casNext(QNode cmp, QNode val) {
                if (this.next == cmp) {
                    if (U.compareAndSwapObject(this, NEXT, cmp, val)) {
                        return true;
                    }
                }
                return false;
            }

            /* access modifiers changed from: package-private */
            public boolean casItem(Object cmp, Object val) {
                if (this.item == cmp) {
                    if (U.compareAndSwapObject(this, ITEM, cmp, val)) {
                        return true;
                    }
                }
                return false;
            }

            /* access modifiers changed from: package-private */
            public void tryCancel(Object cmp) {
                U.compareAndSwapObject(this, ITEM, cmp, this);
            }

            /* access modifiers changed from: package-private */
            public boolean isCancelled() {
                return this.item == this;
            }

            /* access modifiers changed from: package-private */
            public boolean isOffList() {
                return this.next == this;
            }

            static {
                try {
                    ITEM = U.objectFieldOffset(QNode.class.getDeclaredField("item"));
                    NEXT = U.objectFieldOffset(QNode.class.getDeclaredField("next"));
                } catch (ReflectiveOperationException e) {
                    throw new Error((Throwable) e);
                }
            }
        }

        TransferQueue() {
            QNode h = new QNode(null, false);
            this.head = h;
            this.tail = h;
        }

        /* access modifiers changed from: package-private */
        public void advanceHead(QNode h, QNode nh) {
            if (h == this.head) {
                if (U.compareAndSwapObject(this, HEAD, h, nh)) {
                    h.next = h;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void advanceTail(QNode t, QNode nt) {
            if (this.tail == t) {
                U.compareAndSwapObject(this, TAIL, t, nt);
            }
        }

        /* access modifiers changed from: package-private */
        public boolean casCleanMe(QNode cmp, QNode val) {
            if (this.cleanMe == cmp) {
                if (U.compareAndSwapObject(this, CLEANME, cmp, val)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public E transfer(E e, boolean timed, long nanos) {
            Object obj = e;
            QNode s = null;
            boolean isData = obj != null;
            while (true) {
                boolean isData2 = isData;
                QNode t = this.tail;
                QNode h = this.head;
                if (!(t == null || h == null)) {
                    if (h == t || t.isData == isData2) {
                        QNode tn = t.next;
                        if (t != this.tail) {
                            continue;
                        } else if (tn != null) {
                            advanceTail(t, tn);
                        } else if (timed && nanos <= 0) {
                            return null;
                        } else {
                            if (s == null) {
                                s = new QNode(obj, isData2);
                            }
                            QNode s2 = s;
                            if (!t.casNext(null, s2)) {
                                s = s2;
                            } else {
                                advanceTail(t, s2);
                                QNode s3 = s2;
                                Object x = awaitFulfill(s2, obj, timed, nanos);
                                if (x == s3) {
                                    clean(t, s3);
                                    return null;
                                }
                                if (!s3.isOffList()) {
                                    advanceHead(t, s3);
                                    if (x != null) {
                                        s3.item = s3;
                                    }
                                    s3.waiter = null;
                                }
                                return x != null ? x : obj;
                            }
                        }
                    } else {
                        QNode m = h.next;
                        if (t == this.tail && m != null && h == this.head) {
                            Object x2 = m.item;
                            if (isData2 == (x2 != null) || x2 == m || !m.casItem(x2, obj)) {
                                advanceHead(h, m);
                            } else {
                                advanceHead(h, m);
                                LockSupport.unpark(m.waiter);
                                return x2 != null ? x2 : obj;
                            }
                        }
                    }
                }
                isData = isData2;
            }
        }

        /* access modifiers changed from: package-private */
        public Object awaitFulfill(QNode s, E e, boolean timed, long nanos) {
            int spins;
            long deadline = timed ? System.nanoTime() + nanos : 0;
            Thread w = Thread.currentThread();
            if (this.head.next == s) {
                spins = timed ? SynchronousQueue.MAX_TIMED_SPINS : SynchronousQueue.MAX_UNTIMED_SPINS;
            } else {
                spins = 0;
            }
            while (true) {
                if (w.isInterrupted()) {
                    s.tryCancel(e);
                }
                Object x = s.item;
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

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x0053, code lost:
            if (r5.casNext(r6, r8) != false) goto L_0x0055;
         */
        /* JADX WARNING: Removed duplicated region for block: B:43:0x005a A[SYNTHETIC] */
        /* JADX WARNING: Removed duplicated region for block: B:51:0x0003 A[SYNTHETIC] */
        public void clean(QNode pred, QNode s) {
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
                                if (!(d == null || d == dp || !d.isCancelled())) {
                                    if (d != t) {
                                        QNode qNode = d.next;
                                        QNode dn = qNode;
                                        if (qNode != null) {
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
                } else {
                    advanceHead(h, hn);
                }
            }
        }

        static {
            try {
                HEAD = U.objectFieldOffset(TransferQueue.class.getDeclaredField("head"));
                TAIL = U.objectFieldOffset(TransferQueue.class.getDeclaredField("tail"));
                CLEANME = U.objectFieldOffset(TransferQueue.class.getDeclaredField("cleanMe"));
            } catch (ReflectiveOperationException e) {
                throw new Error((Throwable) e);
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

            SNode(Object item2) {
                this.item = item2;
            }

            /* access modifiers changed from: package-private */
            public boolean casNext(SNode cmp, SNode val) {
                if (cmp == this.next) {
                    if (U.compareAndSwapObject(this, NEXT, cmp, val)) {
                        return true;
                    }
                }
                return false;
            }

            /* access modifiers changed from: package-private */
            public boolean tryMatch(SNode s) {
                boolean z = true;
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
                if (this.match != s) {
                    z = false;
                }
                return z;
            }

            /* access modifiers changed from: package-private */
            public void tryCancel() {
                U.compareAndSwapObject(this, MATCH, null, this);
            }

            /* access modifiers changed from: package-private */
            public boolean isCancelled() {
                return this.match == this;
            }

            static {
                try {
                    MATCH = U.objectFieldOffset(SNode.class.getDeclaredField("match"));
                    NEXT = U.objectFieldOffset(SNode.class.getDeclaredField("next"));
                } catch (ReflectiveOperationException e) {
                    throw new Error((Throwable) e);
                }
            }
        }

        TransferStack() {
        }

        static boolean isFulfilling(int m) {
            return (m & 2) != 0;
        }

        /* access modifiers changed from: package-private */
        public boolean casHead(SNode h, SNode nh) {
            if (h == this.head) {
                if (U.compareAndSwapObject(this, HEAD, h, nh)) {
                    return true;
                }
            }
            return false;
        }

        static SNode snode(SNode s, Object e, SNode next, int mode) {
            if (s == null) {
                s = new SNode(e);
            }
            s.mode = mode;
            s.next = next;
            return s;
        }

        /* access modifiers changed from: package-private */
        public E transfer(E e, boolean timed, long nanos) {
            SNode s = null;
            int mode = e == null ? 0 : 1;
            while (true) {
                SNode h = this.head;
                if (h == null || h.mode == mode) {
                    if (!timed || nanos > 0) {
                        SNode snode = snode(s, e, h, mode);
                        s = snode;
                        if (casHead(h, snode)) {
                            SNode m = awaitFulfill(s, timed, nanos);
                            if (m == s) {
                                clean(s);
                                return null;
                            }
                            SNode sNode = this.head;
                            SNode h2 = sNode;
                            if (sNode != null && h2.next == s) {
                                casHead(h2, s.next);
                            }
                            return mode == 0 ? m.item : s.item;
                        }
                    } else if (h == null || !h.isCancelled()) {
                        return null;
                    } else {
                        casHead(h, h.next);
                    }
                } else if (isFulfilling(h.mode)) {
                    SNode m2 = h.next;
                    if (m2 == null) {
                        casHead(h, null);
                    } else {
                        SNode mn = m2.next;
                        if (m2.tryMatch(h)) {
                            casHead(h, mn);
                        } else {
                            h.casNext(m2, mn);
                        }
                    }
                } else if (h.isCancelled()) {
                    casHead(h, h.next);
                } else {
                    SNode snode2 = snode(s, e, h, 2 | mode);
                    s = snode2;
                    if (casHead(h, snode2)) {
                        while (true) {
                            SNode m3 = s.next;
                            if (m3 == null) {
                                casHead(s, null);
                                s = null;
                                break;
                            }
                            SNode mn2 = m3.next;
                            if (m3.tryMatch(s)) {
                                casHead(s, mn2);
                                return mode == 0 ? m3.item : s.item;
                            }
                            s.casNext(m3, mn2);
                        }
                    } else {
                        continue;
                    }
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public SNode awaitFulfill(SNode s, boolean timed, long nanos) {
            int spins;
            long deadline = timed ? System.nanoTime() + nanos : 0;
            Thread w = Thread.currentThread();
            if (shouldSpin(s)) {
                spins = timed ? SynchronousQueue.MAX_TIMED_SPINS : SynchronousQueue.MAX_UNTIMED_SPINS;
            } else {
                spins = 0;
            }
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

        /* access modifiers changed from: package-private */
        public boolean shouldSpin(SNode s) {
            SNode h = this.head;
            return h == s || h == null || isFulfilling(h.mode);
        }

        /* access modifiers changed from: package-private */
        public void clean(SNode s) {
            SNode p;
            s.item = null;
            s.waiter = null;
            SNode past = s.next;
            if (past != null && past.isCancelled()) {
                past = past.next;
            }
            while (true) {
                SNode sNode = this.head;
                p = sNode;
                if (sNode != null && p != past && p.isCancelled()) {
                    casHead(p, p.next);
                }
            }
            while (p != null && p != past) {
                SNode p2 = p.next;
                if (p2 == null || !p2.isCancelled()) {
                    p = p2;
                } else {
                    p.casNext(p2, p2.next);
                }
            }
        }

        static {
            try {
                HEAD = U.objectFieldOffset(TransferStack.class.getDeclaredField("head"));
            } catch (ReflectiveOperationException e) {
                throw new Error((Throwable) e);
            }
        }
    }

    static abstract class Transferer<E> {
        /* access modifiers changed from: package-private */
        public abstract E transfer(E e, boolean z, long j);

        Transferer() {
        }
    }

    static class WaitQueue implements Serializable {
        WaitQueue() {
        }
    }

    static {
        Class<LockSupport> cls = LockSupport.class;
    }

    public SynchronousQueue() {
        this(false);
    }

    public SynchronousQueue(boolean fair) {
        this.transferer = fair ? new TransferQueue<>() : new TransferStack<>();
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
        if (e != null) {
            return this.transferer.transfer(e, true, 0) != null;
        }
        throw new NullPointerException();
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
        if (e != null || !Thread.interrupted()) {
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
        } else if (c != this) {
            int n = 0;
            while (true) {
                E poll = poll();
                E e = poll;
                if (poll == null) {
                    return n;
                }
                c.add(e);
                n++;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c != this) {
            int n = 0;
            while (n < maxElements) {
                E poll = poll();
                E e = poll;
                if (poll == null) {
                    break;
                }
                c.add(e);
                n++;
            }
            return n;
        } else {
            throw new IllegalArgumentException();
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
