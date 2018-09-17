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
    private static final Node<Object> NEXT_TERMINATOR = new Node();
    private static final Node<Object> PREV_TERMINATOR = new Node();
    private static final long TAIL;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = 876323262645176354L;
    private volatile transient Node<E> head;
    private volatile transient Node<E> tail;

    private abstract class AbstractItr implements Iterator<E> {
        private Node<E> lastRet;
        private E nextItem;
        private Node<E> nextNode;

        abstract Node<E> nextNode(Node<E> node);

        abstract Node<E> startNode();

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
            if (item == null) {
                throw new NoSuchElementException();
            }
            advance();
            return item;
        }

        public void remove() {
            Node<E> l = this.lastRet;
            if (l == null) {
                throw new IllegalStateException();
            }
            l.item = null;
            ConcurrentLinkedDeque.this.unlink(l);
            this.lastRet = null;
        }
    }

    static final class CLDSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 33554432;
        int batch;
        Node<E> current;
        boolean exhausted;
        final ConcurrentLinkedDeque<E> queue;

        CLDSpliterator(ConcurrentLinkedDeque<E> queue) {
            this.queue = queue;
        }

        /* JADX WARNING: Missing block: B:8:0x0015, code:
            if (r4 != null) goto L_0x0017;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Spliterator<E> trySplit() {
            ConcurrentLinkedDeque<E> q = this.queue;
            int b = this.batch;
            int n = b <= 0 ? 1 : b >= MAX_BATCH ? MAX_BATCH : b + 1;
            if (!this.exhausted) {
                Node<E> p;
                Node<E> p2 = this.current;
                if (p2 == null) {
                    p2 = q.first();
                }
                if (p2.item == null) {
                    p = p2.next;
                    if (p2 == p) {
                        p2 = q.first();
                        this.current = p2;
                    } else {
                        p2 = p;
                    }
                }
                if (!(p2 == null || p2.next == null)) {
                    Object[] a = new Object[n];
                    int i = 0;
                    do {
                        Object obj = p2.item;
                        a[i] = obj;
                        if (obj != null) {
                            i++;
                        }
                        p = p2.next;
                        if (p2 == p) {
                            p2 = q.first();
                        } else {
                            p2 = p;
                        }
                        if (p2 == null) {
                            break;
                        }
                    } while (i < n);
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
            if (action == null) {
                throw new NullPointerException();
            }
            ConcurrentLinkedDeque<E> q = this.queue;
            if (!this.exhausted) {
                Node<E> p = this.current;
                if (p == null) {
                    p = q.first();
                    if (p == null) {
                        return;
                    }
                }
                this.exhausted = true;
                do {
                    E e = p.item;
                    Node<E> p2 = p.next;
                    if (p == p2) {
                        p = q.first();
                    } else {
                        p = p2;
                    }
                    if (e != null) {
                        action.accept(e);
                        continue;
                    }
                } while (p != null);
            }
        }

        /* JADX WARNING: Missing block: B:9:0x0017, code:
            if (r1 != null) goto L_0x0019;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            ConcurrentLinkedDeque<E> q = this.queue;
            if (!this.exhausted) {
                E e;
                Node<E> p = this.current;
                if (p == null) {
                    p = q.first();
                }
                do {
                    e = p.item;
                    Node<E> p2 = p.next;
                    if (p == p2) {
                        p = q.first();
                    } else {
                        p = p2;
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

        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        public int characteristics() {
            return 4368;
        }
    }

    private class DescendingItr extends AbstractItr {
        /* synthetic */ DescendingItr(ConcurrentLinkedDeque this$0, DescendingItr -this1) {
            this();
        }

        private DescendingItr() {
            super();
        }

        Node<E> startNode() {
            return ConcurrentLinkedDeque.this.last();
        }

        Node<E> nextNode(Node<E> p) {
            return ConcurrentLinkedDeque.this.pred(p);
        }
    }

    private class Itr extends AbstractItr {
        /* synthetic */ Itr(ConcurrentLinkedDeque this$0, Itr -this1) {
            this();
        }

        private Itr() {
            super();
        }

        Node<E> startNode() {
            return ConcurrentLinkedDeque.this.first();
        }

        Node<E> nextNode(Node<E> p) {
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

        Node(E item) {
            U.putObject(this, ITEM, item);
        }

        boolean casItem(E cmp, E val) {
            return U.compareAndSwapObject(this, ITEM, cmp, val);
        }

        void lazySetNext(Node<E> val) {
            U.putOrderedObject(this, NEXT, val);
        }

        boolean casNext(Node<E> cmp, Node<E> val) {
            return U.compareAndSwapObject(this, NEXT, cmp, val);
        }

        void lazySetPrev(Node<E> val) {
            U.putOrderedObject(this, PREV, val);
        }

        boolean casPrev(Node<E> cmp, Node<E> val) {
            return U.compareAndSwapObject(this, PREV, cmp, val);
        }

        static {
            try {
                PREV = U.objectFieldOffset(Node.class.getDeclaredField("prev"));
                ITEM = U.objectFieldOffset(Node.class.getDeclaredField("item"));
                NEXT = U.objectFieldOffset(Node.class.getDeclaredField("next"));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
    }

    Node<E> prevTerminator() {
        return PREV_TERMINATOR;
    }

    Node<E> nextTerminator() {
        return NEXT_TERMINATOR;
    }

    private void linkFirst(E e) {
        Node<E> h;
        Node<E> p;
        Node<E> newNode = new Node(Objects.requireNonNull(e));
        loop0:
        while (true) {
            h = this.head;
            p = h;
            while (true) {
                Node<E> q = p.prev;
                if (q != null) {
                    p = q;
                    q = q.prev;
                    if (q != null) {
                        Node<E> h2 = this.head;
                        p = h != h2 ? h2 : q;
                        h = h2;
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
        Node<E> t;
        Node<E> p;
        Node<E> newNode = new Node(Objects.requireNonNull(e));
        loop0:
        while (true) {
            t = this.tail;
            p = t;
            while (true) {
                Node<E> q = p.next;
                if (q != null) {
                    p = q;
                    q = q.next;
                    if (q != null) {
                        Node<E> t2 = this.tail;
                        p = t != t2 ? t2 : q;
                        t = t2;
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

    /* JADX WARNING: Removed duplicated region for block: B:28:0x003a  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0022  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void unlink(Node<E> x) {
        Node<E> prev = x.prev;
        Node<E> next = x.next;
        if (prev == null) {
            unlinkFirst(x, next);
        } else if (next == null) {
            unlinkLast(x, prev);
        } else {
            Node<E> activePred;
            boolean isFirst;
            Node<E> activeSucc;
            boolean isLast;
            int hops = 1;
            Node<E> p = prev;
            while (p.item == null) {
                Node<E> q = p.prev;
                if (q == null) {
                    if (p.next != p) {
                        activePred = p;
                        isFirst = true;
                        p = next;
                        while (p.item == null) {
                            q = p.next;
                            if (q == null) {
                                if (p.prev != p) {
                                    activeSucc = p;
                                    isLast = true;
                                    if (hops < 2 || (isFirst | isLast) == 0) {
                                        skipDeletedSuccessors(activePred);
                                        skipDeletedPredecessors(activeSucc);
                                        if ((isFirst | isLast) != 0 && activePred.next == activeSucc && activeSucc.prev == activePred && (isFirst ? activePred.prev != null : activePred.item == null) && (isLast ? activeSucc.next != null : activeSucc.item == null)) {
                                            Node prevTerminator;
                                            Node<E> prevTerminator2;
                                            updateHead();
                                            updateTail();
                                            if (isFirst) {
                                                prevTerminator2 = prevTerminator();
                                            } else {
                                                prevTerminator2 = x;
                                            }
                                            x.lazySetPrev(prevTerminator2);
                                            if (isLast) {
                                                prevTerminator2 = nextTerminator();
                                            } else {
                                                prevTerminator2 = x;
                                            }
                                            x.lazySetNext(prevTerminator2);
                                        }
                                    } else {
                                        return;
                                    }
                                }
                                return;
                            } else if (p != q) {
                                p = q;
                                hops++;
                            } else {
                                return;
                            }
                        }
                        activeSucc = p;
                        isLast = false;
                        if (hops < 2) {
                        }
                        skipDeletedSuccessors(activePred);
                        skipDeletedPredecessors(activeSucc);
                    } else {
                        return;
                    }
                } else if (p != q) {
                    p = q;
                    hops++;
                } else {
                    return;
                }
            }
            activePred = p;
            isFirst = false;
            p = next;
            while (p.item == null) {
            }
            activeSucc = p;
            isLast = false;
            if (hops < 2) {
            }
            skipDeletedSuccessors(activePred);
            skipDeletedPredecessors(activeSucc);
        }
    }

    private void unlinkFirst(Node<E> first, Node<E> next) {
        Node o = null;
        Node<E> p = next;
        while (p.item == null) {
            Node<E> q = p.next;
            if (q == null) {
                break;
            } else if (p != q) {
                Node<E> o2 = p;
                p = q;
            } else {
                return;
            }
        }
        if (!(o2 == null || p.prev == p || !first.casNext(next, p))) {
            skipDeletedPredecessors(p);
            if (first.prev == null && ((p.next == null || p.item != null) && p.prev == first)) {
                updateHead();
                updateTail();
                o2.lazySetNext(o2);
                o2.lazySetPrev(prevTerminator());
            }
        }
    }

    private void unlinkLast(Node<E> last, Node<E> prev) {
        Node o = null;
        Node<E> p = prev;
        while (p.item == null) {
            Node<E> q = p.prev;
            if (q == null) {
                break;
            } else if (p != q) {
                Node<E> o2 = p;
                p = q;
            } else {
                return;
            }
        }
        if (!(o2 == null || p.next == p || !last.casPrev(prev, p))) {
            skipDeletedSuccessors(p);
            if (last.next == null && ((p.prev == null || p.item != null) && p.next == last)) {
                updateHead();
                updateTail();
                o2.lazySetPrev(o2);
                o2.lazySetNext(nextTerminator());
            }
        }
    }

    private final void updateHead() {
        while (true) {
            Node<E> h = this.head;
            if (h.item != null) {
                break;
            }
            Node<E> p = h.prev;
            if (p == null) {
                break;
            }
            while (true) {
                Node<E> q = p.prev;
                if (q == null) {
                    break;
                }
                p = q;
                q = q.prev;
                if (q != null) {
                    if (h != this.head) {
                        break;
                    }
                    p = q;
                } else {
                    break;
                }
            }
            if (casHead(h, p)) {
                return;
            }
        }
    }

    private final void updateTail() {
        while (true) {
            Node<E> t = this.tail;
            if (t.item != null) {
                break;
            }
            Node<E> p = t.next;
            if (p == null) {
                break;
            }
            while (true) {
                Node<E> q = p.next;
                if (q == null) {
                    break;
                }
                p = q;
                q = q.next;
                if (q != null) {
                    if (t != this.tail) {
                        break;
                    }
                    p = q;
                } else {
                    break;
                }
            }
            if (casTail(t, p)) {
                return;
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0016, code:
            if (r0.next == r0) goto L_0x0018;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void skipDeletedPredecessors(Node<E> x) {
        while (true) {
            Node<E> prev = x.prev;
            Node<E> p = prev;
            while (p.item == null) {
                Node<E> q = p.prev;
                if (q != null) {
                    if (p == q) {
                        break;
                    }
                    p = q;
                }
            }
            if (prev == p || x.casPrev(prev, p)) {
            }
            if (x.item == null && x.next != null) {
                return;
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0016, code:
            if (r1.prev == r1) goto L_0x0018;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void skipDeletedSuccessors(Node<E> x) {
        while (true) {
            Node<E> next = x.next;
            Node<E> p = next;
            while (p.item == null) {
                Node<E> q = p.next;
                if (q != null) {
                    if (p == q) {
                        break;
                    }
                    p = q;
                }
            }
            if (next == p || x.casNext(next, p)) {
            }
            if (x.item == null && x.prev != null) {
                return;
            }
        }
    }

    final Node<E> succ(Node<E> p) {
        Node<E> q = p.next;
        return p == q ? first() : q;
    }

    final Node<E> pred(Node<E> p) {
        Node<E> q = p.prev;
        return p == q ? last() : q;
    }

    Node<E> first() {
        Node<E> p;
        Node<E> h;
        do {
            h = this.head;
            p = h;
            while (true) {
                Node<E> q = p.prev;
                if (q == null) {
                    break;
                }
                p = q;
                q = q.prev;
                if (q == null) {
                    break;
                }
                Node<E> h2 = this.head;
                p = h != h2 ? h2 : q;
                h = h2;
            }
            if (p == h) {
                break;
            }
        } while (!casHead(h, p));
        return p;
    }

    Node<E> last() {
        Node<E> p;
        Node<E> t;
        do {
            t = this.tail;
            p = t;
            while (true) {
                Node<E> q = p.next;
                if (q == null) {
                    break;
                }
                p = q;
                q = q.next;
                if (q == null) {
                    break;
                }
                Node<E> t2 = this.tail;
                p = t != t2 ? t2 : q;
                t = t2;
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
        Node node = new Node(null);
        this.tail = node;
        this.head = node;
    }

    public ConcurrentLinkedDeque(Collection<? extends E> c) {
        Node h = null;
        Node<E> t = null;
        for (E e : c) {
            Node<E> newNode = new Node(Objects.requireNonNull(e));
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
                t = new Node(null);
                h = t;
            } else {
                Node<E> newNode = new Node(null);
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
            if (item != null && o.lambda$-java_util_function_Predicate_4628(item) && p.casItem(item, null)) {
                unlink(p);
                return true;
            }
            p = succ(p);
        }
        return false;
    }

    public boolean removeLastOccurrence(Object o) {
        Objects.requireNonNull(o);
        Node<E> p = last();
        while (p != null) {
            E item = p.item;
            if (item != null && o.lambda$-java_util_function_Predicate_4628(item) && p.casItem(item, null)) {
                unlink(p);
                return true;
            }
            p = pred(p);
        }
        return false;
    }

    public boolean contains(Object o) {
        if (o != null) {
            Node<E> p = first();
            while (p != null) {
                E item = p.item;
                if (item != null && o.lambda$-java_util_function_Predicate_4628(item)) {
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
            while (p != null) {
                if (p.item != null) {
                    count++;
                    if (count == Integer.MAX_VALUE) {
                        break loop0;
                    }
                }
                Node<E> p2 = p.next;
                if (p != p2) {
                    p = p2;
                }
            }
            break loop0;
        }
        return count;
    }

    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    public boolean addAll(Collection<? extends E> c) {
        if (c == this) {
            throw new IllegalArgumentException();
        }
        Node<E> t;
        Node beginningOfTheEnd = null;
        Node last = null;
        for (E e : c) {
            Node<E> newNode = new Node(Objects.requireNonNull(e));
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
            t = this.tail;
            Node<E> p = t;
            while (true) {
                Node<E> q = p.next;
                if (q != null) {
                    p = q;
                    q = q.next;
                    if (q != null) {
                        Node<E> t2 = this.tail;
                        p = t != t2 ? t2 : q;
                        t = t2;
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
            t = this.tail;
            if (last.next == null) {
                casTail(t, last);
            }
        }
        return true;
    }

    public void clear() {
        do {
        } while (pollFirst() != null);
    }

    public String toString() {
        int charLength;
        int size;
        Object[] a = null;
        loop0:
        while (true) {
            charLength = 0;
            Node<E> p = first();
            size = 0;
            while (p != null) {
                int size2;
                E item = p.item;
                if (item != null) {
                    if (a == null) {
                        a = new String[4];
                    } else if (size == a.length) {
                        String[] a2 = (String[]) Arrays.copyOf(a, size * 2);
                    }
                    String s = item.toString();
                    size2 = size + 1;
                    a[size] = s;
                    charLength += s.length();
                } else {
                    size2 = size;
                }
                Node<E> p2 = p.next;
                if (p != p2) {
                    p = p2;
                    size = size2;
                }
            }
            break loop0;
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
            Node<E> p = first();
            size = 0;
            while (p != null) {
                int size2;
                E item = p.item;
                if (item != null) {
                    if (x == null) {
                        x = new Object[4];
                    } else if (size == x.length) {
                        x = Arrays.copyOf(x, (size + 4) * 2);
                    }
                    size2 = size + 1;
                    x[size] = item;
                } else {
                    size2 = size;
                }
                Node<E> p2 = p.next;
                if (p != p2) {
                    p = p2;
                    size = size2;
                }
            }
            break loop0;
        }
        if (x == null) {
            return new Object[0];
        }
        if (a == null || size > a.length) {
            if (size != x.length) {
                x = Arrays.copyOf(x, size);
            }
            return x;
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
        return new Itr(this, null);
    }

    public Iterator<E> descendingIterator() {
        return new DescendingItr(this, null);
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
        Node h = null;
        Node<E> t = null;
        while (true) {
            Object item = s.readObject();
            if (item != null) {
                Node<E> newNode = new Node(item);
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
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
