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
    static final int MAX_TIMED_SPINS = 0;
    static final int MAX_UNTIMED_SPINS = 0;
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
        private static final long CLEANME = 0;
        private static final long HEAD = 0;
        private static final long TAIL = 0;
        private static final Unsafe U = null;
        volatile transient QNode cleanMe;
        volatile transient QNode head;
        volatile transient QNode tail;

        static final class QNode {
            private static final long ITEM = 0;
            private static final long NEXT = 0;
            private static final Unsafe U = null;
            final boolean isData;
            volatile Object item;
            volatile QNode next;
            volatile Thread waiter;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.SynchronousQueue.TransferQueue.QNode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.SynchronousQueue.TransferQueue.QNode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.SynchronousQueue.TransferQueue.QNode.<clinit>():void");
            }

            QNode(Object item, boolean isData) {
                this.item = item;
                this.isData = isData;
            }

            boolean casNext(QNode cmp, QNode val) {
                if (this.next == cmp) {
                    return U.compareAndSwapObject(this, NEXT, cmp, val);
                }
                return false;
            }

            boolean casItem(Object cmp, Object val) {
                if (this.item == cmp) {
                    return U.compareAndSwapObject(this, ITEM, cmp, val);
                }
                return false;
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
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.SynchronousQueue.TransferQueue.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.SynchronousQueue.TransferQueue.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.SynchronousQueue.TransferQueue.<clinit>():void");
        }

        TransferQueue() {
            QNode h = new QNode(null, false);
            this.head = h;
            this.tail = h;
        }

        void advanceHead(QNode h, QNode nh) {
            if (h == this.head && U.compareAndSwapObject(this, HEAD, h, nh)) {
                h.next = h;
            }
        }

        void advanceTail(QNode t, QNode nt) {
            if (this.tail == t) {
                U.compareAndSwapObject(this, TAIL, t, nt);
            }
        }

        boolean casCleanMe(QNode cmp, QNode val) {
            if (this.cleanMe == cmp) {
                return U.compareAndSwapObject(this, CLEANME, cmp, val);
            }
            return false;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        E transfer(E e, boolean timed, long nanos) {
            QNode h;
            QNode m;
            Object x;
            QNode s = null;
            boolean isData = e != null;
            while (true) {
                QNode t = this.tail;
                h = this.head;
                if (!(t == null || h == null)) {
                    if (h != t && t.isData != isData) {
                        m = h.next;
                        if (t == this.tail && m != null && h == this.head) {
                            x = m.item;
                            if (isData != (x != null) && x != m && m.casItem(x, e)) {
                                break;
                            }
                            advanceHead(h, m);
                        }
                    } else {
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
                                break;
                            }
                        }
                    }
                }
            }
            advanceHead(h, m);
            LockSupport.unpark(m.waiter);
            if (x == null) {
                E x2 = e;
            }
            return x;
        }

        Object awaitFulfill(QNode s, E e, boolean timed, long nanos) {
            long deadline = timed ? System.nanoTime() + nanos : 0;
            Thread w = Thread.currentThread();
            int spins = this.head.next == s ? timed ? SynchronousQueue.MAX_TIMED_SPINS : SynchronousQueue.MAX_UNTIMED_SPINS : SynchronousQueue.MAX_UNTIMED_SPINS;
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
                                if (!(d == null || d == dp || !d.isCancelled())) {
                                    if (d != t) {
                                        QNode dn = d.next;
                                        if (!(dn == null || dn == d || !dp.casNext(d, dn))) {
                                        }
                                    }
                                    if (dp == pred) {
                                        return;
                                    }
                                }
                                casCleanMe(dp, null);
                                if (dp == pred) {
                                    return;
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
    }

    static final class TransferStack<E> extends Transferer<E> {
        static final int DATA = 1;
        static final int FULFILLING = 2;
        private static final long HEAD = 0;
        static final int REQUEST = 0;
        private static final Unsafe U = null;
        volatile SNode head;

        static final class SNode {
            private static final long MATCH = 0;
            private static final long NEXT = 0;
            private static final Unsafe U = null;
            Object item;
            volatile SNode match;
            int mode;
            volatile SNode next;
            volatile Thread waiter;

            static {
                /* JADX: method processing error */
/*
                Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.SynchronousQueue.TransferStack.SNode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.SynchronousQueue.TransferStack.SNode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
                /*
                // Can't load method instructions.
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.SynchronousQueue.TransferStack.SNode.<clinit>():void");
            }

            SNode(Object item) {
                this.item = item;
            }

            boolean casNext(SNode cmp, SNode val) {
                if (cmp == this.next) {
                    return U.compareAndSwapObject(this, NEXT, cmp, val);
                }
                return false;
            }

            boolean tryMatch(SNode s) {
                if (this.match == null && U.compareAndSwapObject(this, MATCH, null, s)) {
                    Thread w = this.waiter;
                    if (w != null) {
                        this.waiter = null;
                        LockSupport.unpark(w);
                    }
                    return true;
                }
                return this.match == s;
            }

            void tryCancel() {
                U.compareAndSwapObject(this, MATCH, null, this);
            }

            boolean isCancelled() {
                return this.match == this;
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.SynchronousQueue.TransferStack.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.SynchronousQueue.TransferStack.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.SynchronousQueue.TransferStack.<clinit>():void");
        }

        TransferStack() {
        }

        static boolean isFulfilling(int m) {
            return (m & FULFILLING) != 0;
        }

        boolean casHead(SNode h, SNode nh) {
            if (h == this.head) {
                return U.compareAndSwapObject(this, HEAD, h, nh);
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        E transfer(E e, boolean timed, long nanos) {
            SNode m;
            SNode mn;
            SNode s = null;
            int mode = e == null ? SynchronousQueue.MAX_UNTIMED_SPINS : DATA;
            loop0:
            while (true) {
                SNode h = this.head;
                if (h == null || h.mode == mode) {
                    if (!timed || nanos > 0) {
                        s = snode(s, e, h, mode);
                        if (casHead(h, s)) {
                            break;
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
                    s = snode(s, e, h, mode | FULFILLING);
                    if (casHead(h, s)) {
                        while (true) {
                            m = s.next;
                            if (m == null) {
                                break;
                            }
                            mn = m.next;
                            if (m.tryMatch(s)) {
                                break loop0;
                            }
                            s.casNext(m, mn);
                        }
                        casHead(s, null);
                        s = null;
                    } else {
                        continue;
                    }
                }
            }
            casHead(s, mn);
            return mode == 0 ? m.item : s.item;
        }

        SNode awaitFulfill(SNode s, boolean timed, long nanos) {
            long deadline = timed ? System.nanoTime() + nanos : 0;
            Thread w = Thread.currentThread();
            int spins = shouldSpin(s) ? timed ? SynchronousQueue.MAX_TIMED_SPINS : SynchronousQueue.MAX_UNTIMED_SPINS : SynchronousQueue.MAX_UNTIMED_SPINS;
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
                    spins = shouldSpin(s) ? spins - 1 : SynchronousQueue.MAX_UNTIMED_SPINS;
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

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        void clean(SNode s) {
            s.item = null;
            s.waiter = null;
            SNode past = s.next;
            if (past != null && past.isCancelled()) {
                past = past.next;
            }
            while (true) {
                SNode n;
                SNode p = this.head;
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
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.SynchronousQueue.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.SynchronousQueue.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.SynchronousQueue.<clinit>():void");
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
        return MAX_UNTIMED_SPINS;
    }

    public int remainingCapacity() {
        return MAX_UNTIMED_SPINS;
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
        return new Object[MAX_UNTIMED_SPINS];
    }

    public <T> T[] toArray(T[] a) {
        if (a.length > 0) {
            a[MAX_UNTIMED_SPINS] = null;
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
            int n = MAX_UNTIMED_SPINS;
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
            int n = MAX_UNTIMED_SPINS;
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
