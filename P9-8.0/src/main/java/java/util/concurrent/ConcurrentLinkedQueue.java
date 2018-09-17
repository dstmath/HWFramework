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

        CLQSpliterator(ConcurrentLinkedQueue<E> queue) {
            this.queue = queue;
        }

        /* JADX WARNING: Missing block: B:8:0x0015, code:
            if (r4 != null) goto L_0x0017;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Spliterator<E> trySplit() {
            ConcurrentLinkedQueue<E> q = this.queue;
            int b = this.batch;
            int n = b <= 0 ? 1 : b >= MAX_BATCH ? MAX_BATCH : b + 1;
            if (!this.exhausted) {
                Node<E> p = this.current;
                if (p == null) {
                    p = q.first();
                }
                if (p.next != null) {
                    Object[] a = new Object[n];
                    int i = 0;
                    do {
                        Object obj = p.item;
                        a[i] = obj;
                        if (obj != null) {
                            i++;
                        }
                        Node<E> p2 = p.next;
                        if (p == p2) {
                            p = q.first();
                        } else {
                            p = p2;
                        }
                        if (p == null) {
                            break;
                        }
                    } while (i < n);
                    this.current = p;
                    if (p == null) {
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
            ConcurrentLinkedQueue<E> q = this.queue;
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
            ConcurrentLinkedQueue<E> q = this.queue;
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

    private class Itr implements Iterator<E> {
        private Node<E> lastRet;
        private E nextItem;
        private Node<E> nextNode;

        Itr() {
            Node<E> h;
            Node<E> p;
            loop0:
            while (true) {
                h = ConcurrentLinkedQueue.this.head;
                p = h;
                while (true) {
                    E item = p.item;
                    if (item == null) {
                        Node<E> q = p.next;
                        if (q == null) {
                            break loop0;
                        } else if (p != q) {
                            p = q;
                        }
                    } else {
                        this.nextNode = p;
                        this.nextItem = item;
                        break loop0;
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
            if (pred == null) {
                throw new NoSuchElementException();
            }
            this.lastRet = pred;
            Object item = null;
            Node<E> p = ConcurrentLinkedQueue.this.succ(pred);
            while (p != null) {
                item = p.item;
                if (item != null) {
                    break;
                }
                Node<E> q = ConcurrentLinkedQueue.this.succ(p);
                if (q != null) {
                    ConcurrentLinkedQueue.casNext(pred, p, q);
                }
                p = q;
            }
            this.nextNode = p;
            E x = this.nextItem;
            this.nextItem = item;
            return x;
        }

        public void remove() {
            Node<E> l = this.lastRet;
            if (l == null) {
                throw new IllegalStateException();
            }
            l.item = null;
            this.lastRet = null;
        }
    }

    private static class Node<E> {
        volatile E item;
        volatile Node<E> next;

        /* synthetic */ Node(Node -this0) {
            this();
        }

        private Node() {
        }
    }

    static <E> Node<E> newNode(E item) {
        Node<E> node = new Node();
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
        Node newNode = newNode(null);
        this.tail = newNode;
        this.head = newNode;
    }

    public ConcurrentLinkedQueue(Collection<? extends E> c) {
        Node h = null;
        Node t = null;
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
            t = newNode(null);
            h = t;
        }
        this.head = h;
        this.tail = t;
    }

    public boolean add(E e) {
        return offer(e);
    }

    final void updateHead(Node<E> h, Node<E> p) {
        if (h != p && casHead(h, p)) {
            lazySetNext(h, h);
        }
    }

    final Node<E> succ(Node<E> p) {
        Node<E> next = p.next;
        return p == next ? this.head : next;
    }

    public boolean offer(E e) {
        Node<E> newNode = newNode(Objects.requireNonNull(e));
        Node t = this.tail;
        Node<E> p = t;
        while (true) {
            Node<E> q = p.next;
            Node<E> t2;
            if (q == null) {
                if (casNext(p, null, newNode)) {
                    break;
                }
            } else if (p == q) {
                t2 = this.tail;
                if (t != t2) {
                    p = t2;
                    t = t2;
                } else {
                    p = this.head;
                    t = t2;
                }
            } else {
                if (p != t) {
                    t2 = this.tail;
                    if (t != t2) {
                        p = t2;
                        t = t2;
                    } else {
                        t = t2;
                    }
                }
                p = q;
            }
        }
        if (p != t) {
            casTail(t, newNode);
        }
        return true;
    }

    public E poll() {
        while (true) {
            Node<E> h = this.head;
            Node<E> p = h;
            while (true) {
                E item = p.item;
                Node<E> q;
                if (item == null || !casItem(p, item, null)) {
                    q = p.next;
                    if (q == null) {
                        updateHead(h, p);
                        return null;
                    } else if (p != q) {
                        p = q;
                    }
                } else {
                    if (p != h) {
                        q = p.next;
                        if (q == null) {
                            q = p;
                        }
                        updateHead(h, q);
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
                Node<E> q = p.next;
                if (q == null) {
                    break loop0;
                } else if (p != q) {
                    p = q;
                }
            }
        }
        updateHead(h, p);
        return item;
    }

    Node<E> first() {
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
                Node<E> q = p.next;
                if (q == null) {
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

    public boolean remove(Object o) {
        if (o != null) {
            Node pred = null;
            Node<E> p = first();
            while (p != null) {
                Node<E> next;
                Node<E> pred2;
                boolean removed = false;
                E item = p.item;
                if (item != null) {
                    if (o.lambda$-java_util_function_Predicate_4628(item)) {
                        removed = casItem(p, item, null);
                    } else {
                        next = succ(p);
                        pred2 = p;
                        p = next;
                    }
                }
                next = succ(p);
                if (!(pred2 == null || next == null)) {
                    casNext(pred2, p, next);
                }
                if (removed) {
                    return true;
                }
                pred2 = p;
                p = next;
            }
        }
        return false;
    }

    public boolean addAll(Collection<? extends E> c) {
        if (c == this) {
            throw new IllegalArgumentException();
        }
        Node beginningOfTheEnd = null;
        Node last = null;
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
        Node t = this.tail;
        Node<E> p = t;
        while (true) {
            Node<E> q = p.next;
            Node<E> t2;
            if (q == null) {
                if (casNext(p, null, beginningOfTheEnd)) {
                    break;
                }
            } else if (p == q) {
                t2 = this.tail;
                if (t != t2) {
                    p = t2;
                    t = t2;
                } else {
                    p = this.head;
                    t = t2;
                }
            } else {
                if (p != t) {
                    t2 = this.tail;
                    if (t != t2) {
                        p = t2;
                        t = t2;
                    } else {
                        t = t2;
                    }
                }
                p = q;
            }
        }
        if (!casTail(t, last)) {
            Node<E> t3 = this.tail;
            if (last.next == null) {
                casTail(t3, last);
            }
        }
        return true;
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
        Node h = null;
        Node t = null;
        while (true) {
            Object item = s.readObject();
            if (item == null) {
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
            t = newNode(null);
            h = t;
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
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
