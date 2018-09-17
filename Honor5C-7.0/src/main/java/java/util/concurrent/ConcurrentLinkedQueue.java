package java.util.concurrent;

import android.icu.util.AnnualTimeZoneRule;
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
    private static final long HEAD = 0;
    private static final long ITEM = 0;
    private static final long NEXT = 0;
    private static final long TAIL = 0;
    private static final Unsafe U = null;
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

        /* JADX WARNING: inconsistent code. */
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

        /* JADX WARNING: inconsistent code. */
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
            E item;
            loop0:
            while (true) {
                h = ConcurrentLinkedQueue.this.head;
                p = h;
                while (true) {
                    item = p.item;
                    if (item == null) {
                        Node<E> q = p.next;
                        if (q == null) {
                            break loop0;
                        } else if (p != q) {
                            p = q;
                        }
                    } else {
                        break loop0;
                    }
                }
            }
            this.nextNode = p;
            this.nextItem = item;
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

        private Node() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.ConcurrentLinkedQueue.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.ConcurrentLinkedQueue.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.ConcurrentLinkedQueue.<clinit>():void");
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
            if (q == null) {
                if (casNext(p, null, newNode)) {
                    break;
                }
            } else if (p == q) {
                t = this.tail;
                if (t != t) {
                    p = t;
                    t = t;
                } else {
                    p = this.head;
                    t = t;
                }
            } else {
                if (p != t) {
                    t = this.tail;
                    if (t != t) {
                        p = t;
                        t = t;
                    } else {
                        t = t;
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
        E item;
        loop0:
        while (true) {
            Node<E> h = this.head;
            Node<E> p = h;
            while (true) {
                item = p.item;
                if (item != null && casItem(p, item, null)) {
                    break loop0;
                }
                Node<E> q = p.next;
                if (q == null) {
                    updateHead(h, p);
                    return null;
                } else if (p != q) {
                    p = q;
                }
            }
        }
        if (p != h) {
            q = p.next;
            if (q == null) {
                q = p;
            }
            updateHead(h, q);
        }
        return item;
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
        loop0:
        while (true) {
            h = this.head;
            p = h;
            while (true) {
                boolean hasItem = p.item != null;
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
                    if (count == AnnualTimeZoneRule.MAX_YEAR) {
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
                if (item != null && o.equals(item)) {
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
                boolean z = false;
                E item = p.item;
                if (item != null) {
                    if (o.equals(item)) {
                        z = casItem(p, item, null);
                    } else {
                        next = succ(p);
                        pred2 = p;
                        p = next;
                    }
                }
                next = succ(p);
                if (!(pred == null || next == null)) {
                    casNext(pred, p, next);
                }
                if (z) {
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
            if (q == null) {
                if (casNext(p, null, beginningOfTheEnd)) {
                    break;
                }
            } else if (p == q) {
                t = this.tail;
                if (t != t) {
                    p = t;
                    t = t;
                } else {
                    p = this.head;
                    t = t;
                }
            } else {
                if (p != t) {
                    t = this.tail;
                    if (t != t) {
                        p = t;
                        t = t;
                    } else {
                        t = t;
                    }
                }
                p = q;
            }
        }
        if (!casTail(t, last)) {
            Node<E> t2 = this.tail;
            if (last.next == null) {
                casTail(t2, last);
            }
        }
        return true;
    }

    public String toString() {
        Object[] objArr = null;
        loop0:
        while (true) {
            int charLength = 0;
            Node<E> p = first();
            int size = 0;
            while (p != null) {
                int size2;
                E item = p.item;
                if (item != null) {
                    if (objArr == null) {
                        objArr = new String[4];
                    } else if (size == objArr.length) {
                        String[] a = (String[]) Arrays.copyOf(objArr, size * 2);
                    }
                    String s = item.toString();
                    size2 = size + 1;
                    objArr[size] = s;
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
        return Helpers.toString(objArr, size, charLength);
    }

    private Object[] toArrayInternal(Object[] a) {
        Object[] x = a;
        loop0:
        while (true) {
            Node<E> p = first();
            int size = 0;
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
            System.arraycopy(x, 0, a, 0, size);
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
        Node node = null;
        while (true) {
            Object item = s.readObject();
            if (item == null) {
                break;
            }
            Node<E> newNode = newNode(item);
            if (h == null) {
                node = newNode;
                h = newNode;
            } else {
                lazySetNext(node, newNode);
                node = newNode;
            }
        }
        if (h == null) {
            node = newNode(null);
            h = node;
        }
        this.head = h;
        this.tail = node;
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
}
