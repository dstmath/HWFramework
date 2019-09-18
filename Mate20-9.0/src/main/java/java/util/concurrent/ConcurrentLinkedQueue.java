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
import java.util.Objects;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import sun.misc.Unsafe;

public class ConcurrentLinkedQueue<E> extends AbstractQueue<E> implements Queue<E>, Serializable {
    private static final long HEAD;
    private static final long ITEM;
    private static final long NEXT;
    private static final long TAIL;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = 196745693267521676L;
    volatile transient Node<E> head;
    private volatile transient Node<E> tail;

    static final class CLQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 33554432;
        int batch;
        Node<E> current;
        boolean exhausted;
        final ConcurrentLinkedQueue<E> queue;

        CLQSpliterator(ConcurrentLinkedQueue<E> queue2) {
            this.queue = queue2;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
            if (r4 != null) goto L_0x0020;
         */
        public Spliterator<E> trySplit() {
            ConcurrentLinkedQueue<E> q = this.queue;
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
                if (p.next != null) {
                    Object[] a = new Object[n];
                    Node<E> p2 = p;
                    int i = 0;
                    do {
                        E e = p2.item;
                        a[i] = e;
                        if (e != null) {
                            i++;
                        }
                        Node<E> node2 = p2.next;
                        Node<E> p3 = node2;
                        if (p2 == node2) {
                            p2 = q.first();
                        } else {
                            p2 = p3;
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
            Node<E> p;
            if (action != null) {
                ConcurrentLinkedQueue<E> q = this.queue;
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
                ConcurrentLinkedQueue<E> q = this.queue;
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

    private class Itr implements Iterator<E> {
        private Node<E> lastRet;
        private E nextItem;
        private Node<E> nextNode;

        Itr() {
            Node<E> p;
            Node<E> h;
            loop0:
            while (true) {
                p = ConcurrentLinkedQueue.this.head;
                h = p;
                while (true) {
                    E e = p.item;
                    E item = e;
                    if (e != null) {
                        this.nextNode = p;
                        this.nextItem = item;
                        break loop0;
                    }
                    Node<E> node = p.next;
                    Node<E> q = node;
                    if (node == null) {
                        break loop0;
                    } else if (p != q) {
                        p = q;
                    }
                }
            }
            ConcurrentLinkedQueue.this.updateHead(h, p);
        }

        public boolean hasNext() {
            return this.nextItem != null;
        }

        public E next() {
            Node<E> pred = this.nextNode;
            if (pred != null) {
                this.lastRet = pred;
                E item = null;
                Node<E> p = ConcurrentLinkedQueue.this.succ(pred);
                while (p != null) {
                    E e = p.item;
                    item = e;
                    if (e != null) {
                        break;
                    }
                    Node<E> succ = ConcurrentLinkedQueue.this.succ(p);
                    Node<E> q = succ;
                    if (succ != null) {
                        ConcurrentLinkedQueue.casNext(pred, p, q);
                    }
                    p = q;
                }
                this.nextNode = p;
                E x = this.nextItem;
                this.nextItem = item;
                return x;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            Node<E> l = this.lastRet;
            if (l != null) {
                l.item = null;
                this.lastRet = null;
                return;
            }
            throw new IllegalStateException();
        }
    }

    private static class Node<E> {
        volatile E item;
        volatile Node<E> next;

        private Node() {
        }
    }

    static <E> Node<E> newNode(E item) {
        Node<E> node = new Node<>();
        U.putObject(node, ITEM, item);
        return node;
    }

    static <E> boolean casItem(Node<E> node, E cmp, E val) {
        return U.compareAndSwapObject(node, ITEM, cmp, val);
    }

    static <E> void lazySetNext(Node<E> node, Node<E> val) {
        U.putOrderedObject(node, NEXT, val);
    }

    static <E> boolean casNext(Node<E> node, Node<E> cmp, Node<E> val) {
        return U.compareAndSwapObject(node, NEXT, cmp, val);
    }

    public ConcurrentLinkedQueue() {
        Node<E> newNode = newNode(null);
        this.tail = newNode;
        this.head = newNode;
    }

    public ConcurrentLinkedQueue(Collection<? extends E> c) {
        Node<E> h = null;
        Node<E> t = null;
        for (E e : c) {
            Node<E> newNode = newNode(Objects.requireNonNull(e));
            if (h == null) {
                t = newNode;
                h = newNode;
            } else {
                lazySetNext(t, newNode);
                t = newNode;
            }
        }
        if (h == null) {
            Node<E> newNode2 = newNode(null);
            t = newNode2;
            h = newNode2;
        }
        this.head = h;
        this.tail = t;
    }

    public boolean add(E e) {
        return offer(e);
    }

    /* access modifiers changed from: package-private */
    public final void updateHead(Node<E> h, Node<E> p) {
        if (h != p && casHead(h, p)) {
            lazySetNext(h, h);
        }
    }

    /* access modifiers changed from: package-private */
    public final Node<E> succ(Node<E> p) {
        Node<E> next = p.next;
        return p == next ? this.head : next;
    }

    public boolean offer(E e) {
        Node<E> t;
        Node<E> newNode = newNode(Objects.requireNonNull(e));
        Node<E> p = this.tail;
        Node<E> t2 = p;
        while (true) {
            Node<E> q = p.next;
            if (q == null) {
                if (casNext(p, null, newNode)) {
                    break;
                }
            } else if (p == q) {
                Node<E> node = this.tail;
                Node<E> t3 = node;
                p = t2 != node ? t3 : this.head;
                t2 = t3;
            } else {
                if (p != t2) {
                    Node<E> node2 = this.tail;
                    t = node2;
                    if (t2 != node2) {
                        t2 = t;
                        p = t;
                    }
                } else {
                    t = t2;
                }
                t2 = t;
                t = q;
                p = t;
            }
        }
        if (p != t2) {
            casTail(t2, newNode);
        }
        return true;
    }

    public E poll() {
        while (true) {
            Node<E> h = this.head;
            Node<E> p = h;
            while (true) {
                E item = p.item;
                if (item == null || !casItem(p, item, null)) {
                    Node<E> node = p.next;
                    Node<E> q = node;
                    if (node == null) {
                        updateHead(h, p);
                        return null;
                    } else if (p != q) {
                        p = q;
                    }
                } else {
                    if (p != h) {
                        Node<E> q2 = p.next;
                        updateHead(h, q2 != null ? q2 : p);
                    }
                    return item;
                }
            }
        }
    }

    public E peek() {
        Node<E> h;
        Node<E> p;
        E item;
        loop0:
        while (true) {
            h = this.head;
            p = h;
            while (true) {
                item = p.item;
                if (item != null) {
                    break loop0;
                }
                Node<E> node = p.next;
                Node<E> q = node;
                if (node == null) {
                    break loop0;
                } else if (p != q) {
                    p = q;
                }
            }
        }
        updateHead(h, p);
        return item;
    }

    /* access modifiers changed from: package-private */
    public Node<E> first() {
        Node<E> h;
        Node<E> p;
        boolean hasItem;
        loop0:
        while (true) {
            h = this.head;
            p = h;
            while (true) {
                hasItem = p.item != null;
                if (hasItem) {
                    break loop0;
                }
                Node<E> node = p.next;
                Node<E> q = node;
                if (node == null) {
                    break loop0;
                } else if (p != q) {
                    p = q;
                }
            }
        }
        updateHead(h, p);
        if (hasItem) {
            return p;
        }
        return null;
    }

    public boolean isEmpty() {
        return first() == null;
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

    public boolean remove(Object o) {
        Node<E> next;
        if (o != null) {
            Node<E> pred = null;
            Node<E> p = first();
            while (p != null) {
                boolean removed = false;
                E item = p.item;
                if (item != null) {
                    if (!o.equals(item)) {
                        next = succ(p);
                        pred = p;
                        p = next;
                    } else {
                        removed = casItem(p, item, null);
                    }
                }
                next = succ(p);
                if (!(pred == null || next == null)) {
                    casNext(pred, p, next);
                }
                if (removed) {
                    return true;
                }
                pred = p;
                p = next;
            }
        }
        return false;
    }

    public boolean addAll(Collection<? extends E> c) {
        Node<E> t;
        if (c != this) {
            Node<E> beginningOfTheEnd = null;
            Node<E> last = null;
            for (E e : c) {
                Node<E> newNode = newNode(Objects.requireNonNull(e));
                if (beginningOfTheEnd == null) {
                    last = newNode;
                    beginningOfTheEnd = newNode;
                } else {
                    lazySetNext(last, newNode);
                    last = newNode;
                }
            }
            if (beginningOfTheEnd == null) {
                return false;
            }
            Node<E> p = this.tail;
            Node<E> t2 = p;
            while (true) {
                Node<E> q = p.next;
                if (q == null) {
                    if (casNext(p, null, beginningOfTheEnd)) {
                        break;
                    }
                } else if (p == q) {
                    Node<E> node = this.tail;
                    Node<E> t3 = node;
                    p = t2 != node ? t3 : this.head;
                    t2 = t3;
                } else {
                    if (p != t2) {
                        Node<E> node2 = this.tail;
                        t = node2;
                        if (t2 != node2) {
                            t2 = t;
                            p = t;
                        }
                    } else {
                        t = t2;
                    }
                    t2 = t;
                    t = q;
                    p = t;
                }
            }
            if (!casTail(t2, last)) {
                Node<E> t4 = this.tail;
                if (last.next == null) {
                    casTail(t4, last);
                }
            }
            return true;
        }
        throw new IllegalArgumentException();
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

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Node<E> p = first();
        while (p != null) {
            Object item = p.item;
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
            if (readObject == null) {
                break;
            }
            Node<E> newNode = newNode(item);
            if (h == null) {
                t = newNode;
                h = newNode;
            } else {
                lazySetNext(t, newNode);
                t = newNode;
            }
        }
        if (h == null) {
            Node<E> newNode2 = newNode(null);
            t = newNode2;
            h = newNode2;
        }
        this.head = h;
        this.tail = t;
    }

    public Spliterator<E> spliterator() {
        return new CLQSpliterator(this);
    }

    private boolean casTail(Node<E> cmp, Node<E> val) {
        return U.compareAndSwapObject(this, TAIL, cmp, val);
    }

    private boolean casHead(Node<E> cmp, Node<E> val) {
        return U.compareAndSwapObject(this, HEAD, cmp, val);
    }

    static {
        try {
            HEAD = U.objectFieldOffset(ConcurrentLinkedQueue.class.getDeclaredField("head"));
            TAIL = U.objectFieldOffset(ConcurrentLinkedQueue.class.getDeclaredField("tail"));
            ITEM = U.objectFieldOffset(Node.class.getDeclaredField("item"));
            NEXT = U.objectFieldOffset(Node.class.getDeclaredField("next"));
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }
}
