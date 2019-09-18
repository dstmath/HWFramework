package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import sun.misc.Unsafe;

public class ConcurrentLinkedDeque<E> extends AbstractCollection<E> implements Deque<E>, Serializable {
    private static final long HEAD;
    private static final int HOPS = 2;
    private static final Node<Object> NEXT_TERMINATOR = new Node<>();
    private static final Node<Object> PREV_TERMINATOR = new Node<>();
    private static final long TAIL;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = 876323262645176354L;
    private volatile transient Node<E> head;
    private volatile transient Node<E> tail;

    private abstract class AbstractItr implements Iterator<E> {
        private Node<E> lastRet;
        private E nextItem;
        private Node<E> nextNode;

        /* access modifiers changed from: package-private */
        public abstract Node<E> nextNode(Node<E> node);

        /* access modifiers changed from: package-private */
        public abstract Node<E> startNode();

        AbstractItr() {
            advance();
        }

        private void advance() {
            this.lastRet = this.nextNode;
            Node<E> p = this.nextNode == null ? startNode() : nextNode(this.nextNode);
            while (p != null) {
                E item = p.item;
                if (item != null) {
                    this.nextNode = p;
                    this.nextItem = item;
                    return;
                }
                p = nextNode(p);
            }
            this.nextNode = null;
            this.nextItem = null;
        }

        public boolean hasNext() {
            return this.nextItem != null;
        }

        public E next() {
            E item = this.nextItem;
            if (item != null) {
                advance();
                return item;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            Node<E> l = this.lastRet;
            if (l != null) {
                l.item = null;
                ConcurrentLinkedDeque.this.unlink(l);
                this.lastRet = null;
                return;
            }
            throw new IllegalStateException();
        }
    }

    static final class CLDSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 33554432;
        int batch;
        Node<E> current;
        boolean exhausted;
        final ConcurrentLinkedDeque<E> queue;

        CLDSpliterator(ConcurrentLinkedDeque<E> queue2) {
            this.queue = queue2;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
            if (r4 != null) goto L_0x0020;
         */
        public Spliterator<E> trySplit() {
            ConcurrentLinkedDeque<E> q = this.queue;
            int b = this.batch;
            int n = MAX_BATCH;
            if (b <= 0) {
                n = 1;
            } else if (b < MAX_BATCH) {
                n = b + 1;
            }
            if (!this.exhausted) {
                Node<E> node = this.current;
                Node<E> p = node;
                if (node == null) {
                    Node<E> first = q.first();
                    p = first;
                }
                if (p.item == null) {
                    Node<E> node2 = p.next;
                    Node<E> p2 = node2;
                    if (p == node2) {
                        Node<E> first2 = q.first();
                        p = first2;
                        this.current = first2;
                    } else {
                        p = p2;
                    }
                }
                if (!(p == null || p.next == null)) {
                    Object[] a = new Object[n];
                    Node<E> p3 = p;
                    int i = 0;
                    do {
                        E e = p3.item;
                        a[i] = e;
                        if (e != null) {
                            i++;
                        }
                        Node<E> node3 = p3.next;
                        Node<E> p4 = node3;
                        if (p3 == node3) {
                            p3 = q.first();
                        } else {
                            p3 = p4;
                        }
                        if (p3 == null) {
                            break;
                        }
                    } while (i < n);
                    this.current = p3;
                    if (p3 == null) {
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
            Node<E> p;
            if (action != null) {
                ConcurrentLinkedDeque<E> q = this.queue;
                if (!this.exhausted) {
                    Node<E> node = this.current;
                    Node<E> p2 = node;
                    if (node == null) {
                        Node<E> first = q.first();
                        p2 = first;
                        if (first == null) {
                            return;
                        }
                    }
                    this.exhausted = true;
                    do {
                        E e = p.item;
                        Node<E> node2 = p.next;
                        Node<E> p3 = node2;
                        if (p == node2) {
                            p = q.first();
                        } else {
                            p = p3;
                        }
                        if (e != null) {
                            action.accept(e);
                            continue;
                        }
                    } while (p != null);
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }

        /* JADX WARNING: Code restructure failed: missing block: B:6:0x0012, code lost:
            if (r1 != null) goto L_0x0014;
         */
        public boolean tryAdvance(Consumer<? super E> action) {
            Node<E> p;
            E e;
            if (action != null) {
                ConcurrentLinkedDeque<E> q = this.queue;
                if (!this.exhausted) {
                    Node<E> node = this.current;
                    Node<E> p2 = node;
                    if (node == null) {
                        Node<E> first = q.first();
                        p2 = first;
                    }
                    do {
                        e = p.item;
                        Node<E> node2 = p.next;
                        Node<E> p3 = node2;
                        if (p == node2) {
                            p = q.first();
                        } else {
                            p = p3;
                        }
                        if (e != null) {
                            break;
                        }
                    } while (p != null);
                    this.current = p;
                    if (p == null) {
                        this.exhausted = true;
                    }
                    if (e != null) {
                        action.accept(e);
                        return true;
                    }
                }
                return false;
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

    private class DescendingItr extends ConcurrentLinkedDeque<E>.AbstractItr {
        private DescendingItr() {
            super();
        }

        /* access modifiers changed from: package-private */
        public Node<E> startNode() {
            return ConcurrentLinkedDeque.this.last();
        }

        /* access modifiers changed from: package-private */
        public Node<E> nextNode(Node<E> p) {
            return ConcurrentLinkedDeque.this.pred(p);
        }
    }

    private class Itr extends ConcurrentLinkedDeque<E>.AbstractItr {
        private Itr() {
            super();
        }

        /* access modifiers changed from: package-private */
        public Node<E> startNode() {
            return ConcurrentLinkedDeque.this.first();
        }

        /* access modifiers changed from: package-private */
        public Node<E> nextNode(Node<E> p) {
            return ConcurrentLinkedDeque.this.succ(p);
        }
    }

    static final class Node<E> {
        private static final long ITEM;
        private static final long NEXT;
        private static final long PREV;
        private static final Unsafe U = Unsafe.getUnsafe();
        volatile E item;
        volatile Node<E> next;
        volatile Node<E> prev;

        Node() {
        }

        Node(E item2) {
            U.putObject(this, ITEM, item2);
        }

        /* access modifiers changed from: package-private */
        public boolean casItem(E cmp, E val) {
            return U.compareAndSwapObject(this, ITEM, cmp, val);
        }

        /* access modifiers changed from: package-private */
        public void lazySetNext(Node<E> val) {
            U.putOrderedObject(this, NEXT, val);
        }

        /* access modifiers changed from: package-private */
        public boolean casNext(Node<E> cmp, Node<E> val) {
            return U.compareAndSwapObject(this, NEXT, cmp, val);
        }

        /* access modifiers changed from: package-private */
        public void lazySetPrev(Node<E> val) {
            U.putOrderedObject(this, PREV, val);
        }

        /* access modifiers changed from: package-private */
        public boolean casPrev(Node<E> cmp, Node<E> val) {
            return U.compareAndSwapObject(this, PREV, cmp, val);
        }

        static {
            try {
                PREV = U.objectFieldOffset(Node.class.getDeclaredField("prev"));
                ITEM = U.objectFieldOffset(Node.class.getDeclaredField("item"));
                NEXT = U.objectFieldOffset(Node.class.getDeclaredField("next"));
            } catch (ReflectiveOperationException e) {
                throw new Error((Throwable) e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Node<E> prevTerminator() {
        return PREV_TERMINATOR;
    }

    /* access modifiers changed from: package-private */
    public Node<E> nextTerminator() {
        return NEXT_TERMINATOR;
    }

    private void linkFirst(E e) {
        Node<E> p;
        Node<E> h;
        Node<E> h2;
        Node<E> newNode = new Node<>(Objects.requireNonNull(e));
        loop0:
        while (true) {
            p = this.head;
            h = p;
            while (true) {
                Node<E> node = p.prev;
                Node<E> q = node;
                if (node != null) {
                    p = q;
                    Node<E> node2 = q.prev;
                    Node<E> q2 = node2;
                    if (node2 != null) {
                        Node<E> node3 = this.head;
                        Node<E> h3 = node3;
                        if (h != node3) {
                            h2 = h3;
                        } else {
                            h2 = q2;
                        }
                        p = h2;
                        h = h3;
                    }
                }
                if (p.next != p) {
                    newNode.lazySetNext(p);
                    if (p.casPrev(null, newNode)) {
                        break loop0;
                    }
                }
            }
        }
        if (p != h) {
            casHead(h, newNode);
        }
    }

    private void linkLast(E e) {
        Node<E> p;
        Node<E> t;
        Node<E> t2;
        Node<E> newNode = new Node<>(Objects.requireNonNull(e));
        loop0:
        while (true) {
            p = this.tail;
            t = p;
            while (true) {
                Node<E> node = p.next;
                Node<E> q = node;
                if (node != null) {
                    p = q;
                    Node<E> node2 = q.next;
                    Node<E> q2 = node2;
                    if (node2 != null) {
                        Node<E> node3 = this.tail;
                        Node<E> t3 = node3;
                        if (t != node3) {
                            t2 = t3;
                        } else {
                            t2 = q2;
                        }
                        p = t2;
                        t = t3;
                    }
                }
                if (p.prev != p) {
                    newNode.lazySetPrev(p);
                    if (p.casNext(null, newNode)) {
                        break loop0;
                    }
                }
            }
        }
        if (p != t) {
            casTail(t, newNode);
        }
    }

    /* access modifiers changed from: package-private */
    public void unlink(Node<E> x) {
        Node<E> q;
        boolean isFirst;
        Node<E> q2;
        boolean isLast;
        Node<E> prev = x.prev;
        Node<E> next = x.next;
        if (prev == null) {
            unlinkFirst(x, next);
        } else if (next == null) {
            unlinkLast(x, prev);
        } else {
            int hops = 1;
            Node<E> p = prev;
            while (true) {
                if (p.item != null) {
                    q = p;
                    isFirst = false;
                    break;
                }
                Node<E> q3 = p.prev;
                if (q3 == null) {
                    if (p.next != p) {
                        q = p;
                        isFirst = true;
                    } else {
                        return;
                    }
                } else if (p != q3) {
                    p = q3;
                    hops++;
                } else {
                    return;
                }
            }
            Node<E> activePred = q;
            Node<E> p2 = next;
            while (true) {
                if (p2.item != null) {
                    q2 = p2;
                    isLast = false;
                    break;
                }
                Node<E> activeSucc = p2.next;
                if (activeSucc == null) {
                    if (p2.prev != p2) {
                        q2 = p2;
                        isLast = true;
                    } else {
                        return;
                    }
                } else if (p2 != activeSucc) {
                    p2 = activeSucc;
                    hops++;
                } else {
                    return;
                }
            }
            Node<E> activeSucc2 = q2;
            boolean isLast2 = isLast;
            if (hops >= 2 || (!isFirst && !isLast2)) {
                skipDeletedSuccessors(activePred);
                skipDeletedPredecessors(activeSucc2);
                if ((isFirst || isLast2) && activePred.next == activeSucc2 && activeSucc2.prev == activePred && (!isFirst ? activePred.item != null : activePred.prev == null) && (!isLast2 ? activeSucc2.item != null : activeSucc2.next == null)) {
                    updateHead();
                    updateTail();
                    x.lazySetPrev(isFirst ? prevTerminator() : x);
                    x.lazySetNext(isLast2 ? nextTerminator() : x);
                }
            }
        }
    }

    private void unlinkFirst(Node<E> first, Node<E> next) {
        Node<E> o = null;
        Node<E> p = next;
        while (p.item == null) {
            Node<E> node = p.next;
            Node<E> q = node;
            if (node == null) {
                break;
            } else if (p != q) {
                o = p;
                p = q;
            } else {
                return;
            }
        }
        if (!(o == null || p.prev == p || !first.casNext(next, p))) {
            skipDeletedPredecessors(p);
            if (first.prev == null && ((p.next == null || p.item != null) && p.prev == first)) {
                updateHead();
                updateTail();
                o.lazySetNext(o);
                o.lazySetPrev(prevTerminator());
            }
        }
    }

    private void unlinkLast(Node<E> last, Node<E> prev) {
        Node<E> o = null;
        Node<E> p = prev;
        while (p.item == null) {
            Node<E> node = p.prev;
            Node<E> q = node;
            if (node == null) {
                break;
            } else if (p != q) {
                o = p;
                p = q;
            } else {
                return;
            }
        }
        if (!(o == null || p.next == p || !last.casPrev(prev, p))) {
            skipDeletedSuccessors(p);
            if (last.next == null && ((p.prev == null || p.item != null) && p.next == last)) {
                updateHead();
                updateTail();
                o.lazySetPrev(o);
                o.lazySetNext(nextTerminator());
            }
        }
    }

    private final void updateHead() {
        while (true) {
            Node<E> node = this.head;
            Node<E> h = node;
            if (node.item != null) {
                break;
            }
            Node<E> node2 = h.prev;
            Node<E> p = node2;
            if (node2 == null) {
                break;
            }
            while (true) {
                Node<E> node3 = p.prev;
                Node<E> q = node3;
                if (node3 == null) {
                    break;
                }
                p = q;
                Node<E> node4 = q.prev;
                Node<E> q2 = node4;
                if (node4 == null) {
                    break;
                } else if (h != this.head) {
                    break;
                } else {
                    p = q2;
                }
            }
            if (casHead(h, p)) {
                return;
            }
        }
    }

    private final void updateTail() {
        while (true) {
            Node<E> node = this.tail;
            Node<E> t = node;
            if (node.item != null) {
                break;
            }
            Node<E> node2 = t.next;
            Node<E> p = node2;
            if (node2 == null) {
                break;
            }
            while (true) {
                Node<E> node3 = p.next;
                Node<E> q = node3;
                if (node3 == null) {
                    break;
                }
                p = q;
                Node<E> node4 = q.next;
                Node<E> q2 = node4;
                if (node4 == null) {
                    break;
                } else if (t != this.tail) {
                    break;
                } else {
                    p = q2;
                }
            }
            if (casTail(t, p)) {
                return;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000e, code lost:
        if (r1.next == r1) goto L_0x001d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0011, code lost:
        if (r0 == r1) goto L_0x0019;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0017, code lost:
        if (r5.casPrev(r0, r1) == false) goto L_0x001d;
     */
    private void skipDeletedPredecessors(Node<E> x) {
        while (true) {
            Node<E> prev = x.prev;
            Node<E> p = prev;
            while (true) {
                if (p.item != null) {
                    break;
                }
                Node<E> q = p.prev;
                if (q != null) {
                    if (p == q) {
                        break;
                    }
                    p = q;
                }
            }
            if (x.item == null && x.next != null) {
                return;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000e, code lost:
        if (r1.prev == r1) goto L_0x001d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x0011, code lost:
        if (r0 == r1) goto L_0x0019;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0017, code lost:
        if (r5.casNext(r0, r1) == false) goto L_0x001d;
     */
    private void skipDeletedSuccessors(Node<E> x) {
        while (true) {
            Node<E> next = x.next;
            Node<E> p = next;
            while (true) {
                if (p.item != null) {
                    break;
                }
                Node<E> q = p.next;
                if (q != null) {
                    if (p == q) {
                        break;
                    }
                    p = q;
                }
            }
            if (x.item == null && x.prev != null) {
                return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final Node<E> succ(Node<E> p) {
        Node<E> q = p.next;
        return p == q ? first() : q;
    }

    /* access modifiers changed from: package-private */
    public final Node<E> pred(Node<E> p) {
        Node<E> q = p.prev;
        return p == q ? last() : q;
    }

    /* access modifiers changed from: package-private */
    public Node<E> first() {
        Node<E> p;
        Node<E> h;
        Node<E> h2;
        do {
            p = this.head;
            h = p;
            while (true) {
                Node<E> node = p.prev;
                Node<E> q = node;
                if (node == null) {
                    break;
                }
                p = q;
                Node<E> node2 = q.prev;
                Node<E> q2 = node2;
                if (node2 == null) {
                    break;
                }
                Node<E> node3 = this.head;
                Node<E> h3 = node3;
                if (h != node3) {
                    h2 = h3;
                } else {
                    h2 = q2;
                }
                p = h2;
                h = h3;
            }
            if (p == h) {
                break;
            }
        } while (!casHead(h, p));
        return p;
    }

    /* access modifiers changed from: package-private */
    public Node<E> last() {
        Node<E> p;
        Node<E> t;
        Node<E> t2;
        do {
            p = this.tail;
            t = p;
            while (true) {
                Node<E> node = p.next;
                Node<E> q = node;
                if (node == null) {
                    break;
                }
                p = q;
                Node<E> node2 = q.next;
                Node<E> q2 = node2;
                if (node2 == null) {
                    break;
                }
                Node<E> node3 = this.tail;
                Node<E> t3 = node3;
                if (t != node3) {
                    t2 = t3;
                } else {
                    t2 = q2;
                }
                p = t2;
                t = t3;
            }
            if (p == t) {
                break;
            }
        } while (!casTail(t, p));
        return p;
    }

    private E screenNullResult(E v) {
        if (v != null) {
            return v;
        }
        throw new NoSuchElementException();
    }

    public ConcurrentLinkedDeque() {
        Node<E> node = new Node<>(null);
        this.tail = node;
        this.head = node;
    }

    public ConcurrentLinkedDeque(Collection<? extends E> c) {
        Node<E> h = null;
        Node<E> t = null;
        for (E e : c) {
            Node<E> newNode = new Node<>(Objects.requireNonNull(e));
            if (h == null) {
                t = newNode;
                h = newNode;
            } else {
                t.lazySetNext(newNode);
                newNode.lazySetPrev(t);
                t = newNode;
            }
        }
        initHeadTail(h, t);
    }

    private void initHeadTail(Node<E> h, Node<E> t) {
        if (h == t) {
            if (h == null) {
                Node<E> node = new Node<>(null);
                t = node;
                h = node;
            } else {
                Node<E> newNode = new Node<>(null);
                t.lazySetNext(newNode);
                newNode.lazySetPrev(t);
                t = newNode;
            }
        }
        this.head = h;
        this.tail = t;
    }

    public void addFirst(E e) {
        linkFirst(e);
    }

    public void addLast(E e) {
        linkLast(e);
    }

    public boolean offerFirst(E e) {
        linkFirst(e);
        return true;
    }

    public boolean offerLast(E e) {
        linkLast(e);
        return true;
    }

    public E peekFirst() {
        Node<E> p = first();
        while (p != null) {
            E item = p.item;
            if (item != null) {
                return item;
            }
            p = succ(p);
        }
        return null;
    }

    public E peekLast() {
        Node<E> p = last();
        while (p != null) {
            E item = p.item;
            if (item != null) {
                return item;
            }
            p = pred(p);
        }
        return null;
    }

    public E getFirst() {
        return screenNullResult(peekFirst());
    }

    public E getLast() {
        return screenNullResult(peekLast());
    }

    public E pollFirst() {
        Node<E> p = first();
        while (p != null) {
            E item = p.item;
            if (item == null || !p.casItem(item, null)) {
                p = succ(p);
            } else {
                unlink(p);
                return item;
            }
        }
        return null;
    }

    public E pollLast() {
        Node<E> p = last();
        while (p != null) {
            E item = p.item;
            if (item == null || !p.casItem(item, null)) {
                p = pred(p);
            } else {
                unlink(p);
                return item;
            }
        }
        return null;
    }

    public E removeFirst() {
        return screenNullResult(pollFirst());
    }

    public E removeLast() {
        return screenNullResult(pollLast());
    }

    public boolean offer(E e) {
        return offerLast(e);
    }

    public boolean add(E e) {
        return offerLast(e);
    }

    public E poll() {
        return pollFirst();
    }

    public E peek() {
        return peekFirst();
    }

    public E remove() {
        return removeFirst();
    }

    public E pop() {
        return removeFirst();
    }

    public E element() {
        return getFirst();
    }

    public void push(E e) {
        addFirst(e);
    }

    public boolean removeFirstOccurrence(Object o) {
        Objects.requireNonNull(o);
        Node<E> p = first();
        while (p != null) {
            E item = p.item;
            if (item == null || !o.equals(item) || !p.casItem(item, null)) {
                p = succ(p);
            } else {
                unlink(p);
                return true;
            }
        }
        return false;
    }

    public boolean removeLastOccurrence(Object o) {
        Objects.requireNonNull(o);
        Node<E> p = last();
        while (p != null) {
            E item = p.item;
            if (item == null || !o.equals(item) || !p.casItem(item, null)) {
                p = pred(p);
            } else {
                unlink(p);
                return true;
            }
        }
        return false;
    }

    public boolean contains(Object o) {
        if (o != null) {
            Node<E> p = first();
            while (p != null) {
                E item = p.item;
                if (item != null && o.equals(item)) {
                    return true;
                }
                p = succ(p);
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return peekFirst() == null;
    }

    public int size() {
        int count;
        loop0:
        while (true) {
            count = 0;
            Node<E> p = first();
            while (true) {
                if (p == null) {
                    break loop0;
                }
                if (p.item != null) {
                    count++;
                    if (count == Integer.MAX_VALUE) {
                        break loop0;
                    }
                }
                Node<E> node = p.next;
                Node<E> p2 = node;
                if (p != node) {
                    p = p2;
                }
            }
        }
        return count;
    }

    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    public boolean addAll(Collection<? extends E> c) {
        Node<E> t;
        Node<E> t2;
        if (c != this) {
            Node<E> beginningOfTheEnd = null;
            Node<E> last = null;
            for (E e : c) {
                Node<E> newNode = new Node<>(Objects.requireNonNull(e));
                if (beginningOfTheEnd == null) {
                    last = newNode;
                    beginningOfTheEnd = newNode;
                } else {
                    last.lazySetNext(newNode);
                    newNode.lazySetPrev(last);
                    last = newNode;
                }
            }
            if (beginningOfTheEnd == null) {
                return false;
            }
            loop1:
            while (true) {
                Node<E> p = this.tail;
                t = p;
                while (true) {
                    Node<E> node = p.next;
                    Node<E> q = node;
                    if (node != null) {
                        p = q;
                        Node<E> node2 = q.next;
                        Node<E> q2 = node2;
                        if (node2 != null) {
                            Node<E> node3 = this.tail;
                            Node<E> t3 = node3;
                            if (t != node3) {
                                t2 = t3;
                            } else {
                                t2 = q2;
                            }
                            p = t2;
                            t = t3;
                        }
                    }
                    if (p.prev != p) {
                        beginningOfTheEnd.lazySetPrev(p);
                        if (p.casNext(null, beginningOfTheEnd)) {
                            break loop1;
                        }
                    }
                }
            }
            if (!casTail(t, last)) {
                Node<E> t4 = this.tail;
                if (last.next == null) {
                    casTail(t4, last);
                }
            }
            return true;
        }
        throw new IllegalArgumentException();
    }

    public void clear() {
        do {
        } while (pollFirst() != null);
    }

    /* JADX WARNING: type inference failed for: r5v5, types: [java.lang.Object[]] */
    /* JADX WARNING: Multi-variable type inference failed */
    public String toString() {
        int charLength;
        int size;
        String[] a = null;
        loop0:
        while (true) {
            charLength = 0;
            size = 0;
            Node<E> p = first();
            while (true) {
                if (p == null) {
                    break loop0;
                }
                E item = p.item;
                if (item != null) {
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
                Node<E> node = p.next;
                Node<E> p2 = node;
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
            Node<E> p = first();
            while (true) {
                if (p == null) {
                    break loop0;
                }
                E item = p.item;
                if (item != null) {
                    if (x == null) {
                        x = new Object[4];
                    } else if (size == x.length) {
                        x = Arrays.copyOf((T[]) x, 2 * (size + 4));
                    }
                    x[size] = item;
                    size++;
                }
                Node<E> node = p.next;
                Node<E> p2 = node;
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

    public Iterator<E> iterator() {
        return new Itr();
    }

    public Iterator<E> descendingIterator() {
        return new DescendingItr();
    }

    public Spliterator<E> spliterator() {
        return new CLDSpliterator(this);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Node<E> p = first();
        while (p != null) {
            E item = p.item;
            if (item != null) {
                s.writeObject(item);
            }
            p = succ(p);
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        Node<E> h = null;
        Node<E> t = null;
        while (true) {
            Object readObject = s.readObject();
            Object item = readObject;
            if (readObject != null) {
                Node<E> newNode = new Node<>(item);
                if (h == null) {
                    t = newNode;
                    h = newNode;
                } else {
                    t.lazySetNext(newNode);
                    newNode.lazySetPrev(t);
                    t = newNode;
                }
            } else {
                initHeadTail(h, t);
                return;
            }
        }
    }

    private boolean casHead(Node<E> cmp, Node<E> val) {
        return U.compareAndSwapObject(this, HEAD, cmp, val);
    }

    private boolean casTail(Node<E> cmp, Node<E> val) {
        return U.compareAndSwapObject(this, TAIL, cmp, val);
    }

    static {
        PREV_TERMINATOR.next = PREV_TERMINATOR;
        NEXT_TERMINATOR.prev = NEXT_TERMINATOR;
        try {
            HEAD = U.objectFieldOffset(ConcurrentLinkedDeque.class.getDeclaredField("head"));
            TAIL = U.objectFieldOffset(ConcurrentLinkedDeque.class.getDeclaredField("tail"));
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }
}
