package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class LinkedBlockingDeque<E> extends AbstractQueue<E> implements BlockingDeque<E>, Serializable {
    private static final long serialVersionUID = -387911632671998426L;
    private final int capacity;
    private transient int count;
    transient Node<E> first;
    transient Node<E> last;
    final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;

    private abstract class AbstractItr implements Iterator<E> {
        private Node<E> lastRet;
        Node<E> next;
        E nextItem;

        /* access modifiers changed from: package-private */
        public abstract Node<E> firstNode();

        /* access modifiers changed from: package-private */
        public abstract Node<E> nextNode(Node<E> node);

        AbstractItr() {
            ReentrantLock lock = LinkedBlockingDeque.this.lock;
            lock.lock();
            try {
                this.next = firstNode();
                this.nextItem = this.next == null ? null : this.next.item;
            } finally {
                lock.unlock();
            }
        }

        private Node<E> succ(Node<E> n) {
            while (true) {
                Node<E> s = nextNode(n);
                if (s == null) {
                    return null;
                }
                if (s.item != null) {
                    return s;
                }
                if (s == n) {
                    return firstNode();
                }
                n = s;
            }
        }

        /* access modifiers changed from: package-private */
        public void advance() {
            ReentrantLock lock = LinkedBlockingDeque.this.lock;
            lock.lock();
            try {
                this.next = succ(this.next);
                this.nextItem = this.next == null ? null : this.next.item;
            } finally {
                lock.unlock();
            }
        }

        public boolean hasNext() {
            return this.next != null;
        }

        public E next() {
            if (this.next != null) {
                this.lastRet = this.next;
                E x = this.nextItem;
                advance();
                return x;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            Node<E> n = this.lastRet;
            if (n != null) {
                this.lastRet = null;
                ReentrantLock lock = LinkedBlockingDeque.this.lock;
                lock.lock();
                try {
                    if (n.item != null) {
                        LinkedBlockingDeque.this.unlink(n);
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private class DescendingItr extends LinkedBlockingDeque<E>.AbstractItr {
        private DescendingItr() {
            super();
        }

        /* access modifiers changed from: package-private */
        public Node<E> firstNode() {
            return LinkedBlockingDeque.this.last;
        }

        /* access modifiers changed from: package-private */
        public Node<E> nextNode(Node<E> n) {
            return n.prev;
        }
    }

    private class Itr extends LinkedBlockingDeque<E>.AbstractItr {
        private Itr() {
            super();
        }

        /* access modifiers changed from: package-private */
        public Node<E> firstNode() {
            return LinkedBlockingDeque.this.first;
        }

        /* access modifiers changed from: package-private */
        public Node<E> nextNode(Node<E> n) {
            return n.next;
        }
    }

    static final class LBDSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 33554432;
        int batch;
        Node<E> current;
        long est;
        boolean exhausted;
        final LinkedBlockingDeque<E> queue;

        LBDSpliterator(LinkedBlockingDeque<E> queue2) {
            this.queue = queue2;
            this.est = (long) queue2.size();
        }

        public long estimateSize() {
            return this.est;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x001c, code lost:
            if (r4 != null) goto L_0x001e;
         */
        /* JADX WARNING: Removed duplicated region for block: B:30:0x004f  */
        /* JADX WARNING: Removed duplicated region for block: B:31:0x0054  */
        /* JADX WARNING: Removed duplicated region for block: B:35:0x0062  */
        public Spliterator<E> trySplit() {
            LinkedBlockingDeque<E> q = this.queue;
            int b = this.batch;
            int n = MAX_BATCH;
            if (b <= 0) {
                n = 1;
            } else if (b < MAX_BATCH) {
                n = b + 1;
            }
            if (!this.exhausted) {
                Node<E> node = this.current;
                Node<E> h = node;
                if (node == null) {
                    Node<E> node2 = q.first;
                    h = node2;
                }
                if (h.next != null) {
                    Object[] a = new Object[n];
                    ReentrantLock lock = q.lock;
                    int i = 0;
                    Node<E> p = this.current;
                    lock.lock();
                    if (p == null) {
                        try {
                            Node<E> node3 = q.first;
                            p = node3;
                            if (node3 != null) {
                            }
                            lock.unlock();
                            this.current = p;
                            if (p != null) {
                                this.est = 0;
                                this.exhausted = true;
                            } else {
                                long j = this.est - ((long) i);
                                this.est = j;
                                if (j < 0) {
                                    this.est = 0;
                                }
                            }
                            if (i > 0) {
                                this.batch = i;
                                return Spliterators.spliterator(a, 0, i, 4368);
                            }
                        } catch (Throwable th) {
                            lock.unlock();
                            throw th;
                        }
                    }
                    do {
                        E e = p.item;
                        a[i] = e;
                        if (e != null) {
                            i++;
                        }
                        Node<E> node4 = p.next;
                        p = node4;
                        if (node4 == null) {
                            break;
                        }
                    } while (i < n);
                    lock.unlock();
                    this.current = p;
                    if (p != null) {
                    }
                    if (i > 0) {
                    }
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            if (action != null) {
                LinkedBlockingDeque<E> q = this.queue;
                ReentrantLock lock = q.lock;
                if (!this.exhausted) {
                    this.exhausted = true;
                    Node<E> p = this.current;
                    do {
                        E e = null;
                        lock.lock();
                        if (p == null) {
                            try {
                                p = q.first;
                            } catch (Throwable th) {
                                lock.unlock();
                                throw th;
                            }
                        }
                        while (p != null) {
                            e = p.item;
                            p = p.next;
                            if (e != null) {
                                break;
                            }
                        }
                        lock.unlock();
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

        /* JADX INFO: finally extract failed */
        public boolean tryAdvance(Consumer<? super E> action) {
            if (action != null) {
                LinkedBlockingDeque<E> q = this.queue;
                ReentrantLock lock = q.lock;
                if (!this.exhausted) {
                    E e = null;
                    lock.lock();
                    try {
                        if (this.current == null) {
                            this.current = q.first;
                        }
                        while (this.current != null) {
                            e = this.current.item;
                            this.current = this.current.next;
                            if (e != null) {
                                break;
                            }
                        }
                        lock.unlock();
                        if (this.current == null) {
                            this.exhausted = true;
                        }
                        if (e != null) {
                            action.accept(e);
                            return true;
                        }
                    } catch (Throwable th) {
                        lock.unlock();
                        throw th;
                    }
                }
                return false;
            }
            throw new NullPointerException();
        }

        public int characteristics() {
            return 4368;
        }
    }

    static final class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(E x) {
            this.item = x;
        }
    }

    public LinkedBlockingDeque() {
        this((int) Integer.MAX_VALUE);
    }

    public LinkedBlockingDeque(int capacity2) {
        this.lock = new ReentrantLock();
        this.notEmpty = this.lock.newCondition();
        this.notFull = this.lock.newCondition();
        if (capacity2 > 0) {
            this.capacity = capacity2;
            return;
        }
        throw new IllegalArgumentException();
    }

    public LinkedBlockingDeque(Collection<? extends E> c) {
        this((int) Integer.MAX_VALUE);
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            for (E e : c) {
                if (e == null) {
                    throw new NullPointerException();
                } else if (!linkLast(new Node(e))) {
                    throw new IllegalStateException("Deque full");
                }
            }
        } finally {
            lock2.unlock();
        }
    }

    private boolean linkFirst(Node<E> node) {
        if (this.count >= this.capacity) {
            return false;
        }
        Node<E> f = this.first;
        node.next = f;
        this.first = node;
        if (this.last == null) {
            this.last = node;
        } else {
            f.prev = node;
        }
        this.count++;
        this.notEmpty.signal();
        return true;
    }

    private boolean linkLast(Node<E> node) {
        if (this.count >= this.capacity) {
            return false;
        }
        Node<E> l = this.last;
        node.prev = l;
        this.last = node;
        if (this.first == null) {
            this.first = node;
        } else {
            l.next = node;
        }
        this.count++;
        this.notEmpty.signal();
        return true;
    }

    private E unlinkFirst() {
        Node<E> f = this.first;
        if (f == null) {
            return null;
        }
        Node<E> n = f.next;
        E item = f.item;
        f.item = null;
        f.next = f;
        this.first = n;
        if (n == null) {
            this.last = null;
        } else {
            n.prev = null;
        }
        this.count--;
        this.notFull.signal();
        return item;
    }

    private E unlinkLast() {
        Node<E> l = this.last;
        if (l == null) {
            return null;
        }
        Node<E> p = l.prev;
        E item = l.item;
        l.item = null;
        l.prev = l;
        this.last = p;
        if (p == null) {
            this.first = null;
        } else {
            p.next = null;
        }
        this.count--;
        this.notFull.signal();
        return item;
    }

    /* access modifiers changed from: package-private */
    public void unlink(Node<E> x) {
        Node<E> p = x.prev;
        Node<E> n = x.next;
        if (p == null) {
            unlinkFirst();
        } else if (n == null) {
            unlinkLast();
        } else {
            p.next = n;
            n.prev = p;
            x.item = null;
            this.count--;
            this.notFull.signal();
        }
    }

    public void addFirst(E e) {
        if (!offerFirst(e)) {
            throw new IllegalStateException("Deque full");
        }
    }

    public void addLast(E e) {
        if (!offerLast(e)) {
            throw new IllegalStateException("Deque full");
        }
    }

    public boolean offerFirst(E e) {
        if (e != null) {
            Node<E> node = new Node<>(e);
            ReentrantLock lock2 = this.lock;
            lock2.lock();
            try {
                return linkFirst(node);
            } finally {
                lock2.unlock();
            }
        } else {
            throw new NullPointerException();
        }
    }

    public boolean offerLast(E e) {
        if (e != null) {
            Node<E> node = new Node<>(e);
            ReentrantLock lock2 = this.lock;
            lock2.lock();
            try {
                return linkLast(node);
            } finally {
                lock2.unlock();
            }
        } else {
            throw new NullPointerException();
        }
    }

    public void putFirst(E e) throws InterruptedException {
        if (e != null) {
            Node<E> node = new Node<>(e);
            ReentrantLock lock2 = this.lock;
            lock2.lock();
            while (!linkFirst(node)) {
                try {
                    this.notFull.await();
                } finally {
                    lock2.unlock();
                }
            }
            return;
        }
        throw new NullPointerException();
    }

    public void putLast(E e) throws InterruptedException {
        if (e != null) {
            Node<E> node = new Node<>(e);
            ReentrantLock lock2 = this.lock;
            lock2.lock();
            while (!linkLast(node)) {
                try {
                    this.notFull.await();
                } finally {
                    lock2.unlock();
                }
            }
            return;
        }
        throw new NullPointerException();
    }

    public boolean offerFirst(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e != null) {
            Node<E> node = new Node<>(e);
            long nanos = unit.toNanos(timeout);
            ReentrantLock lock2 = this.lock;
            lock2.lockInterruptibly();
            while (!linkFirst(node)) {
                try {
                    if (nanos <= 0) {
                        return false;
                    }
                    nanos = this.notFull.awaitNanos(nanos);
                } finally {
                    lock2.unlock();
                }
            }
            lock2.unlock();
            return true;
        }
        throw new NullPointerException();
    }

    public boolean offerLast(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e != null) {
            Node<E> node = new Node<>(e);
            long nanos = unit.toNanos(timeout);
            ReentrantLock lock2 = this.lock;
            lock2.lockInterruptibly();
            while (!linkLast(node)) {
                try {
                    if (nanos <= 0) {
                        return false;
                    }
                    nanos = this.notFull.awaitNanos(nanos);
                } finally {
                    lock2.unlock();
                }
            }
            lock2.unlock();
            return true;
        }
        throw new NullPointerException();
    }

    public E removeFirst() {
        E x = pollFirst();
        if (x != null) {
            return x;
        }
        throw new NoSuchElementException();
    }

    public E removeLast() {
        E x = pollLast();
        if (x != null) {
            return x;
        }
        throw new NoSuchElementException();
    }

    public E pollFirst() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return unlinkFirst();
        } finally {
            lock2.unlock();
        }
    }

    public E pollLast() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return unlinkLast();
        } finally {
            lock2.unlock();
        }
    }

    public E takeFirst() throws InterruptedException {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        while (true) {
            try {
                E unlinkFirst = unlinkFirst();
                E x = unlinkFirst;
                if (unlinkFirst != null) {
                    return x;
                }
                this.notEmpty.await();
            } finally {
                lock2.unlock();
            }
        }
    }

    public E takeLast() throws InterruptedException {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        while (true) {
            try {
                E unlinkLast = unlinkLast();
                E x = unlinkLast;
                if (unlinkLast != null) {
                    return x;
                }
                this.notEmpty.await();
            } finally {
                lock2.unlock();
            }
        }
    }

    public E pollFirst(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock2 = this.lock;
        lock2.lockInterruptibly();
        while (true) {
            try {
                E unlinkFirst = unlinkFirst();
                E x = unlinkFirst;
                if (unlinkFirst != null) {
                    lock2.unlock();
                    return x;
                } else if (nanos <= 0) {
                    return null;
                } else {
                    nanos = this.notEmpty.awaitNanos(nanos);
                }
            } finally {
                lock2.unlock();
            }
        }
    }

    public E pollLast(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock2 = this.lock;
        lock2.lockInterruptibly();
        while (true) {
            try {
                E unlinkLast = unlinkLast();
                E x = unlinkLast;
                if (unlinkLast != null) {
                    lock2.unlock();
                    return x;
                } else if (nanos <= 0) {
                    return null;
                } else {
                    nanos = this.notEmpty.awaitNanos(nanos);
                }
            } finally {
                lock2.unlock();
            }
        }
    }

    public E getFirst() {
        E x = peekFirst();
        if (x != null) {
            return x;
        }
        throw new NoSuchElementException();
    }

    public E getLast() {
        E x = peekLast();
        if (x != null) {
            return x;
        }
        throw new NoSuchElementException();
    }

    public E peekFirst() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.first == null ? null : this.first.item;
        } finally {
            lock2.unlock();
        }
    }

    public E peekLast() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.last == null ? null : this.last.item;
        } finally {
            lock2.unlock();
        }
    }

    public boolean removeFirstOccurrence(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            for (Node<E> p = this.first; p != null; p = p.next) {
                if (o.equals(p.item)) {
                    unlink(p);
                    return true;
                }
            }
            lock2.unlock();
            return false;
        } finally {
            lock2.unlock();
        }
    }

    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            for (Node<E> p = this.last; p != null; p = p.prev) {
                if (o.equals(p.item)) {
                    unlink(p);
                    return true;
                }
            }
            lock2.unlock();
            return false;
        } finally {
            lock2.unlock();
        }
    }

    public boolean add(E e) {
        addLast(e);
        return true;
    }

    public boolean offer(E e) {
        return offerLast(e);
    }

    public void put(E e) throws InterruptedException {
        putLast(e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        return offerLast(e, timeout, unit);
    }

    public E remove() {
        return removeFirst();
    }

    public E poll() {
        return pollFirst();
    }

    public E take() throws InterruptedException {
        return takeFirst();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        return pollFirst(timeout, unit);
    }

    public E element() {
        return getFirst();
    }

    public E peek() {
        return peekFirst();
    }

    public int remainingCapacity() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.capacity - this.count;
        } finally {
            lock2.unlock();
        }
    }

    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c != this) {
            if (maxElements <= 0) {
                return 0;
            }
            ReentrantLock lock2 = this.lock;
            lock2.lock();
            try {
                int n = Math.min(maxElements, this.count);
                for (int i = 0; i < n; i++) {
                    c.add(this.first.item);
                    unlinkFirst();
                }
                return n;
            } finally {
                lock2.unlock();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void push(E e) {
        addFirst(e);
    }

    public E pop() {
        return removeFirst();
    }

    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    public int size() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.count;
        } finally {
            lock2.unlock();
        }
    }

    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            for (Node<E> p = this.first; p != null; p = p.next) {
                if (o.equals(p.item)) {
                    return true;
                }
            }
            lock2.unlock();
            return false;
        } finally {
            lock2.unlock();
        }
    }

    public Object[] toArray() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            Object[] a = new Object[this.count];
            int k = 0;
            Node<E> p = this.first;
            while (p != null) {
                int k2 = k + 1;
                a[k] = p.item;
                p = p.next;
                k = k2;
            }
            return a;
        } finally {
            lock2.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            int length = a.length;
            T[] a2 = a;
            if (length < this.count) {
                a2 = (Object[]) Array.newInstance(a.getClass().getComponentType(), this.count);
            }
            int k = 0;
            Node<E> p = this.first;
            while (p != null) {
                a2[k] = p.item;
                p = p.next;
                k++;
            }
            if (a2.length > k) {
                a2[k] = null;
            }
            return a2;
        } finally {
            lock2.unlock();
        }
    }

    public String toString() {
        return Helpers.collectionToString(this);
    }

    public void clear() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            Node<E> f = this.first;
            while (f != null) {
                f.item = null;
                Node<E> n = f.next;
                f.prev = null;
                f.next = null;
                f = n;
            }
            this.last = null;
            this.first = null;
            this.count = 0;
            this.notFull.signalAll();
        } finally {
            lock2.unlock();
        }
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    public Iterator<E> descendingIterator() {
        return new DescendingItr();
    }

    public Spliterator<E> spliterator() {
        return new LBDSpliterator(this);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            s.defaultWriteObject();
            for (Node<E> p = this.first; p != null; p = p.next) {
                s.writeObject(p.item);
            }
            s.writeObject(null);
        } finally {
            lock2.unlock();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.count = 0;
        this.first = null;
        this.last = null;
        while (true) {
            E item = s.readObject();
            if (item != null) {
                add(item);
            } else {
                return;
            }
        }
    }
}
