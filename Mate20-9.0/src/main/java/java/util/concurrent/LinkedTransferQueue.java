package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import sun.misc.Unsafe;

public class LinkedTransferQueue<E> extends AbstractQueue<E> implements TransferQueue<E>, Serializable {
    private static final int ASYNC = 1;
    private static final int CHAINED_SPINS = 64;
    private static final int FRONT_SPINS = 128;
    private static final long HEAD;
    private static final boolean MP;
    private static final int NOW = 0;
    private static final long SWEEPVOTES;
    static final int SWEEP_THRESHOLD = 32;
    private static final int SYNC = 2;
    private static final long TAIL;
    private static final int TIMED = 3;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = -3223113410248163686L;
    volatile transient Node head;
    private volatile transient int sweepVotes;
    private volatile transient Node tail;

    final class Itr implements Iterator<E> {
        private Node lastPred;
        private Node lastRet;
        private E nextItem;
        private Node nextNode;

        private void advance(Node prev) {
            Node node = this.lastRet;
            Node r = node;
            if (node == null || r.isMatched()) {
                Node node2 = this.lastPred;
                Node b = node2;
                if (node2 != null && !b.isMatched()) {
                    while (true) {
                        Node node3 = b.next;
                        Node s = node3;
                        if (node3 != null && s != b && s.isMatched()) {
                            Node node4 = s.next;
                            Node n = node4;
                            if (node4 == null || n == s) {
                                break;
                            }
                            b.casNext(s, n);
                        } else {
                            break;
                        }
                    }
                } else {
                    this.lastPred = null;
                }
            } else {
                this.lastPred = r;
            }
            this.lastRet = prev;
            Node p = prev;
            while (true) {
                Node s2 = p == null ? LinkedTransferQueue.this.head : p.next;
                if (s2 == null) {
                    break;
                } else if (s2 == p) {
                    p = null;
                } else {
                    E item = s2.item;
                    if (!s2.isData) {
                        if (item == null) {
                            break;
                        }
                    } else if (!(item == null || item == s2)) {
                        this.nextItem = item;
                        this.nextNode = s2;
                        return;
                    }
                    if (p == null) {
                        p = s2;
                    } else {
                        Node node5 = s2.next;
                        Node n2 = node5;
                        if (node5 == null) {
                            break;
                        } else if (s2 == n2) {
                            p = null;
                        } else {
                            p.casNext(s2, n2);
                        }
                    }
                }
            }
            this.nextNode = null;
            this.nextItem = null;
        }

        Itr() {
            advance(null);
        }

        public final boolean hasNext() {
            if (this.nextNode != null) {
                return true;
            }
            return LinkedTransferQueue.MP;
        }

        public final E next() {
            Node p = this.nextNode;
            if (p != null) {
                E e = this.nextItem;
                advance(p);
                return e;
            }
            throw new NoSuchElementException();
        }

        public final void remove() {
            Node lastRet2 = this.lastRet;
            if (lastRet2 != null) {
                this.lastRet = null;
                if (lastRet2.tryMatchData()) {
                    LinkedTransferQueue.this.unsplice(this.lastPred, lastRet2);
                    return;
                }
                return;
            }
            throw new IllegalStateException();
        }
    }

    final class LTQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 33554432;
        int batch;
        Node current;
        boolean exhausted;

        LTQSpliterator() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
            if (r3 != null) goto L_0x0020;
         */
        public Spliterator<E> trySplit() {
            int b = this.batch;
            int n = MAX_BATCH;
            if (b <= 0) {
                n = 1;
            } else if (b < MAX_BATCH) {
                n = b + 1;
            }
            if (!this.exhausted) {
                Node node = this.current;
                Node p = node;
                if (node == null) {
                    Node firstDataNode = LinkedTransferQueue.this.firstDataNode();
                    p = firstDataNode;
                }
                if (p.next != null) {
                    Object[] a = new Object[n];
                    Node p2 = p;
                    int i = 0;
                    do {
                        Object e = p2.item;
                        if (e != p2) {
                            a[i] = e;
                            if (e != null) {
                                i++;
                            }
                        }
                        Node node2 = p2.next;
                        Node p3 = node2;
                        if (p2 == node2) {
                            p2 = LinkedTransferQueue.this.firstDataNode();
                        } else {
                            p2 = p3;
                        }
                        if (p2 == null || i >= n) {
                            this.current = p2;
                        }
                    } while (p2.isData);
                    this.current = p2;
                    if (p2 == null) {
                        this.exhausted = true;
                    }
                    if (i > 0) {
                        this.batch = i;
                        return Spliterators.spliterator(a, 0, i, 4368);
                    }
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Node p;
            if (action == null) {
                throw new NullPointerException();
            } else if (!this.exhausted) {
                Node node = this.current;
                Node p2 = node;
                if (node == null) {
                    Node firstDataNode = LinkedTransferQueue.this.firstDataNode();
                    p2 = firstDataNode;
                    if (firstDataNode == null) {
                        return;
                    }
                }
                this.exhausted = true;
                do {
                    Object e = p.item;
                    if (!(e == null || e == p)) {
                        action.accept(e);
                    }
                    Node node2 = p.next;
                    Node p3 = node2;
                    if (p == node2) {
                        p = LinkedTransferQueue.this.firstDataNode();
                    } else {
                        p = p3;
                    }
                    if (p == null) {
                        return;
                    }
                } while (p.isData);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:6:0x0012, code lost:
            if (r0 != null) goto L_0x0014;
         */
        public boolean tryAdvance(Consumer<? super E> action) {
            Node p;
            Object e;
            if (action != null) {
                if (!this.exhausted) {
                    Node node = this.current;
                    Node p2 = node;
                    if (node == null) {
                        Node firstDataNode = LinkedTransferQueue.this.firstDataNode();
                        p2 = firstDataNode;
                    }
                    do {
                        Object obj = p.item;
                        e = obj;
                        if (obj == p) {
                            e = null;
                        }
                        Node node2 = p.next;
                        Node p3 = node2;
                        if (p == node2) {
                            p = LinkedTransferQueue.this.firstDataNode();
                        } else {
                            p = p3;
                        }
                        if (e != null || p == null) {
                            this.current = p;
                        }
                    } while (p.isData);
                    this.current = p;
                    if (p == null) {
                        this.exhausted = true;
                    }
                    if (e != null) {
                        action.accept(e);
                        return true;
                    }
                }
                return LinkedTransferQueue.MP;
            }
            throw new NullPointerException();
        }

        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        public int characteristics() {
            return 4368;
        }
    }

    static final class Node {
        private static final long ITEM;
        private static final long NEXT;
        private static final Unsafe U = Unsafe.getUnsafe();
        private static final long WAITER;
        private static final long serialVersionUID = -3375979862319811754L;
        final boolean isData;
        volatile Object item;
        volatile Node next;
        volatile Thread waiter;

        /* access modifiers changed from: package-private */
        public final boolean casNext(Node cmp, Node val) {
            return U.compareAndSwapObject(this, NEXT, cmp, val);
        }

        /* access modifiers changed from: package-private */
        public final boolean casItem(Object cmp, Object val) {
            return U.compareAndSwapObject(this, ITEM, cmp, val);
        }

        Node(Object item2, boolean isData2) {
            U.putObject(this, ITEM, item2);
            this.isData = isData2;
        }

        /* access modifiers changed from: package-private */
        public final void forgetNext() {
            U.putObject(this, NEXT, this);
        }

        /* access modifiers changed from: package-private */
        public final void forgetContents() {
            U.putObject(this, ITEM, this);
            U.putObject(this, WAITER, null);
        }

        /* access modifiers changed from: package-private */
        public final boolean isMatched() {
            Object x = this.item;
            if (x != this) {
                if ((x == null) != this.isData) {
                    return LinkedTransferQueue.MP;
                }
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public final boolean isUnmatchedRequest() {
            if (this.isData || this.item != null) {
                return LinkedTransferQueue.MP;
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public final boolean cannotPrecede(boolean haveData) {
            boolean d = this.isData;
            if (d == haveData) {
                return LinkedTransferQueue.MP;
            }
            Object obj = this.item;
            Object x = obj;
            if (obj == this) {
                return LinkedTransferQueue.MP;
            }
            if ((x != null) == d) {
                return true;
            }
            return LinkedTransferQueue.MP;
        }

        /* access modifiers changed from: package-private */
        public final boolean tryMatchData() {
            Object x = this.item;
            if (x == null || x == this || !casItem(x, null)) {
                return LinkedTransferQueue.MP;
            }
            LockSupport.unpark(this.waiter);
            return true;
        }

        static {
            try {
                ITEM = U.objectFieldOffset(Node.class.getDeclaredField("item"));
                NEXT = U.objectFieldOffset(Node.class.getDeclaredField("next"));
                WAITER = U.objectFieldOffset(Node.class.getDeclaredField("waiter"));
            } catch (ReflectiveOperationException e) {
                throw new Error((Throwable) e);
            }
        }
    }

    static {
        boolean z = true;
        if (Runtime.getRuntime().availableProcessors() <= 1) {
            z = MP;
        }
        MP = z;
        try {
            HEAD = U.objectFieldOffset(LinkedTransferQueue.class.getDeclaredField("head"));
            TAIL = U.objectFieldOffset(LinkedTransferQueue.class.getDeclaredField("tail"));
            SWEEPVOTES = U.objectFieldOffset(LinkedTransferQueue.class.getDeclaredField("sweepVotes"));
            Class<LockSupport> cls = LockSupport.class;
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }

    private boolean casTail(Node cmp, Node val) {
        return U.compareAndSwapObject(this, TAIL, cmp, val);
    }

    private boolean casHead(Node cmp, Node val) {
        return U.compareAndSwapObject(this, HEAD, cmp, val);
    }

    private boolean casSweepVotes(int cmp, int val) {
        return U.compareAndSwapInt(this, SWEEPVOTES, cmp, val);
    }

    private E xfer(E e, boolean haveData, int how, long nanos) {
        boolean z;
        Node node;
        E e2 = e;
        boolean z2 = haveData;
        int i = how;
        if (!z2 || e2 != null) {
            Node s = null;
            while (true) {
                Node p = this.head;
                Node h = p;
                while (true) {
                    z = true;
                    if (p == null) {
                        break;
                    }
                    boolean isData = p.isData;
                    E item = p.item;
                    if (item != p) {
                        if ((item != null) == isData) {
                            if (isData == z2) {
                                break;
                            } else if (p.casItem(item, e2)) {
                                Node h2 = h;
                                Node q = p;
                                while (true) {
                                    if (q == h2) {
                                        break;
                                    }
                                    Node n = q.next;
                                    if (this.head == h2) {
                                        if (casHead(h2, n == null ? q : n)) {
                                            h2.forgetNext();
                                            break;
                                        }
                                    }
                                    Node node2 = this.head;
                                    h2 = node2;
                                    if (node2 == null) {
                                        break;
                                    }
                                    Node node3 = h2.next;
                                    q = node3;
                                    if (node3 != null) {
                                        if (!q.isMatched()) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                LockSupport.unpark(p.waiter);
                                return item;
                            }
                        }
                    }
                    Node n2 = p.next;
                    if (p != n2) {
                        node = n2;
                    } else {
                        node = this.head;
                        h = node;
                    }
                    p = node;
                }
                if (i == 0) {
                    break;
                }
                if (s == null) {
                    s = new Node(e2, z2);
                }
                Node s2 = s;
                Node pred = tryAppend(s2, z2);
                if (pred == null) {
                    s = s2;
                } else if (i != 1) {
                    if (i != 3) {
                        z = false;
                    }
                    return awaitMatch(s2, pred, e2, z, nanos);
                }
            }
            return e2;
        }
        throw new NullPointerException();
    }

    private Node tryAppend(Node s, boolean haveData) {
        Node p = this.tail;
        Node t = p;
        while (true) {
            Node node = null;
            if (p == null) {
                Node node2 = this.head;
                p = node2;
                if (node2 == null) {
                    if (casHead(null, s)) {
                        return s;
                    }
                }
            }
            if (p.cannotPrecede(haveData)) {
                return null;
            }
            Node node3 = p.next;
            Node n = node3;
            if (node3 != null) {
                if (p != t) {
                    Node node4 = this.tail;
                    Node u = node4;
                    if (t != node4) {
                        t = u;
                        node = u;
                        p = node;
                    }
                }
                if (p != n) {
                    node = n;
                }
                p = node;
            } else if (!p.casNext(null, s)) {
                p = p.next;
            } else {
                if (p != t) {
                    do {
                        if (this.tail == t && casTail(t, s)) {
                            break;
                        }
                        Node node5 = this.tail;
                        t = node5;
                        if (node5 == null) {
                            break;
                        }
                        Node node6 = t.next;
                        Node s2 = node6;
                        if (node6 == null) {
                            break;
                        }
                        Node node7 = s2.next;
                        s = node7;
                        if (node7 == null) {
                            break;
                        }
                    } while (s != t);
                }
                return p;
            }
        }
    }

    private E awaitMatch(Node s, Node pred, E e, boolean timed, long nanos) {
        Node node = s;
        Node node2 = pred;
        E e2 = e;
        long deadline = timed ? System.nanoTime() + nanos : 0;
        Thread w = Thread.currentThread();
        int spins = -1;
        ThreadLocalRandom randomYields = null;
        long nanos2 = nanos;
        while (true) {
            E item = node.item;
            if (item != e2) {
                s.forgetContents();
                return item;
            } else if (w.isInterrupted() || (timed && nanos2 <= 0)) {
                unsplice(node2, node);
                if (node.casItem(e2, node)) {
                    return e2;
                }
            } else if (spins < 0) {
                int spinsFor = spinsFor(node2, node.isData);
                spins = spinsFor;
                if (spinsFor > 0) {
                    randomYields = ThreadLocalRandom.current();
                }
            } else if (spins > 0) {
                spins--;
                if (randomYields.nextInt(64) == 0) {
                    Thread.yield();
                }
            } else if (node.waiter == null) {
                node.waiter = w;
            } else if (timed) {
                nanos2 = deadline - System.nanoTime();
                if (nanos2 > 0) {
                    LockSupport.parkNanos(this, nanos2);
                }
            } else {
                LockSupport.park(this);
            }
        }
    }

    private static int spinsFor(Node pred, boolean haveData) {
        if (MP && pred != null) {
            if (pred.isData != haveData) {
                return 192;
            }
            if (pred.isMatched()) {
                return 128;
            }
            if (pred.waiter == null) {
                return 64;
            }
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public final Node succ(Node p) {
        Node next = p.next;
        return p == next ? this.head : next;
    }

    /* access modifiers changed from: package-private */
    public final Node firstDataNode() {
        loop0:
        while (true) {
            Node p = this.head;
            while (true) {
                if (p == null) {
                    break loop0;
                }
                Object item = p.item;
                if (!p.isData) {
                    if (item == null) {
                        break loop0;
                    }
                } else if (!(item == null || item == p)) {
                    return p;
                }
                Node node = p.next;
                Node p2 = node;
                if (p != node) {
                    p = p2;
                }
            }
        }
        return null;
    }

    private int countOfMode(boolean data) {
        int count;
        loop0:
        while (true) {
            count = 0;
            Node p = this.head;
            while (true) {
                if (p == null) {
                    break loop0;
                }
                if (!p.isMatched()) {
                    if (p.isData == data) {
                        count++;
                        if (count == Integer.MAX_VALUE) {
                            break loop0;
                        }
                    } else {
                        return 0;
                    }
                }
                Node node = p.next;
                Node p2 = node;
                if (p != node) {
                    p = p2;
                }
            }
        }
        return count;
    }

    /* JADX WARNING: type inference failed for: r5v6, types: [java.lang.Object[]] */
    /* JADX WARNING: Multi-variable type inference failed */
    public String toString() {
        int charLength;
        int size;
        String[] a = null;
        loop0:
        while (true) {
            charLength = 0;
            size = 0;
            Node p = this.head;
            while (true) {
                if (p == null) {
                    break loop0;
                }
                Object item = p.item;
                if (!p.isData) {
                    if (item == null) {
                        break loop0;
                    }
                } else if (!(item == null || item == p)) {
                    if (a == null) {
                        a = new String[4];
                    } else if (size == a.length) {
                        a = Arrays.copyOf((T[]) a, 2 * size);
                    }
                    String s = item.toString();
                    a[size] = s;
                    charLength += s.length();
                    size++;
                }
                Node node = p.next;
                Node p2 = node;
                if (p != node) {
                    p = p2;
                }
            }
        }
        if (size == 0) {
            return "[]";
        }
        return Helpers.toString(a, size, charLength);
    }

    private Object[] toArrayInternal(Object[] a) {
        int size;
        Object[] x = a;
        loop0:
        while (true) {
            size = 0;
            Node p = this.head;
            while (true) {
                if (p == null) {
                    break loop0;
                }
                Object item = p.item;
                if (!p.isData) {
                    if (item == null) {
                        break loop0;
                    }
                } else if (!(item == null || item == p)) {
                    if (x == null) {
                        x = new Object[4];
                    } else if (size == x.length) {
                        x = Arrays.copyOf((T[]) x, 2 * (size + 4));
                    }
                    x[size] = item;
                    size++;
                }
                Node node = p.next;
                Node p2 = node;
                if (p != node) {
                    p = p2;
                }
            }
        }
        if (x == null) {
            return new Object[0];
        }
        if (a == null || size > a.length) {
            return size == x.length ? x : Arrays.copyOf((T[]) x, size);
        }
        if (a != x) {
            System.arraycopy((Object) x, 0, (Object) a, 0, size);
        }
        if (size < a.length) {
            a[size] = null;
        }
        return a;
    }

    public Object[] toArray() {
        return toArrayInternal(null);
    }

    public <T> T[] toArray(T[] a) {
        if (a != null) {
            return toArrayInternal(a);
        }
        throw new NullPointerException();
    }

    public Spliterator<E> spliterator() {
        return new LTQSpliterator();
    }

    /* access modifiers changed from: package-private */
    public final void unsplice(Node pred, Node s) {
        s.waiter = null;
        if (pred != null && pred != s && pred.next == s) {
            Node n = s.next;
            if (n == null || (n != s && pred.casNext(s, n) && pred.isMatched())) {
                while (true) {
                    Node h = this.head;
                    if (h != pred && h != s && h != null) {
                        if (h.isMatched()) {
                            Node hn = h.next;
                            if (hn != null) {
                                if (hn != h && casHead(h, hn)) {
                                    h.forgetNext();
                                }
                            } else {
                                return;
                            }
                        } else if (pred.next != pred && s.next != s) {
                            while (true) {
                                int v = this.sweepVotes;
                                if (v < 32) {
                                    if (casSweepVotes(v, v + 1)) {
                                        break;
                                    }
                                } else if (casSweepVotes(v, 0)) {
                                    sweep();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void sweep() {
        Node p = this.head;
        while (p != null) {
            Node node = p.next;
            Node s = node;
            if (node == null) {
                return;
            }
            if (!s.isMatched()) {
                p = s;
            } else {
                Node node2 = s.next;
                Node n = node2;
                if (node2 != null) {
                    if (s == n) {
                        p = this.head;
                    } else {
                        p.casNext(s, n);
                    }
                } else {
                    return;
                }
            }
        }
    }

    private boolean findAndRemove(Object e) {
        if (e != null) {
            Node pred = null;
            Node p = this.head;
            while (p != null) {
                Object item = p.item;
                if (!p.isData) {
                    if (item == null) {
                        break;
                    }
                } else if (item != null && item != p && e.equals(item) && p.tryMatchData()) {
                    unsplice(pred, p);
                    return true;
                }
                pred = p;
                Node node = p.next;
                p = node;
                if (node == pred) {
                    pred = null;
                    p = this.head;
                }
            }
        }
        return MP;
    }

    public LinkedTransferQueue() {
    }

    public LinkedTransferQueue(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    public void put(E e) {
        xfer(e, true, 1, 0);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) {
        xfer(e, true, 1, 0);
        return true;
    }

    public boolean offer(E e) {
        xfer(e, true, 1, 0);
        return true;
    }

    public boolean add(E e) {
        xfer(e, true, 1, 0);
        return true;
    }

    public boolean tryTransfer(E e) {
        if (xfer(e, true, 0, 0) == null) {
            return true;
        }
        return MP;
    }

    public void transfer(E e) throws InterruptedException {
        if (xfer(e, true, 2, 0) != null) {
            Thread.interrupted();
            throw new InterruptedException();
        }
    }

    public boolean tryTransfer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (xfer(e, true, 3, unit.toNanos(timeout)) == null) {
            return true;
        }
        if (!Thread.interrupted()) {
            return MP;
        }
        throw new InterruptedException();
    }

    public E take() throws InterruptedException {
        E e = xfer(null, MP, 2, 0);
        if (e != null) {
            return e;
        }
        Thread.interrupted();
        throw new InterruptedException();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E e = xfer(null, MP, 3, unit.toNanos(timeout));
        if (e != null || !Thread.interrupted()) {
            return e;
        }
        throw new InterruptedException();
    }

    public E poll() {
        return xfer(null, MP, 0, 0);
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

    public Iterator<E> iterator() {
        return new Itr();
    }

    public E peek() {
        loop0:
        while (true) {
            E p = this.head;
            while (true) {
                if (p == null) {
                    break loop0;
                }
                E item = p.item;
                if (!p.isData) {
                    if (item == null) {
                        break loop0;
                    }
                } else if (!(item == null || item == p)) {
                    return item;
                }
                E e = p.next;
                E p2 = e;
                if (p != e) {
                    p = p2;
                }
            }
        }
        return null;
    }

    public boolean isEmpty() {
        if (firstDataNode() == null) {
            return true;
        }
        return MP;
    }

    public boolean hasWaitingConsumer() {
        loop0:
        while (true) {
            Node p = this.head;
            while (true) {
                if (p == null) {
                    break loop0;
                }
                Object item = p.item;
                if (p.isData) {
                    if (!(item == null || item == p)) {
                        break loop0;
                    }
                } else if (item == null) {
                    return true;
                }
                Node node = p.next;
                Node p2 = node;
                if (p != node) {
                    p = p2;
                }
            }
        }
        return MP;
    }

    public int size() {
        return countOfMode(true);
    }

    public int getWaitingConsumerCount() {
        return countOfMode(MP);
    }

    public boolean remove(Object o) {
        return findAndRemove(o);
    }

    public boolean contains(Object o) {
        if (o != null) {
            Node p = this.head;
            while (p != null) {
                Object item = p.item;
                if (p.isData) {
                    if (!(item == null || item == p || !o.equals(item))) {
                        return true;
                    }
                } else if (item == null) {
                    break;
                }
                p = succ(p);
            }
        }
        return MP;
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Iterator it = iterator();
        while (it.hasNext()) {
            s.writeObject(it.next());
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        while (true) {
            E item = s.readObject();
            if (item != null) {
                offer(item);
            } else {
                return;
            }
        }
    }
}
