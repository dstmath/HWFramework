package java.util.concurrent;

import android.icu.util.AnnualTimeZoneRule;
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
        final /* synthetic */ LinkedBlockingDeque this$0;

        abstract Node<E> firstNode();

        abstract Node<E> nextNode(Node<E> node);

        AbstractItr(LinkedBlockingDeque this$0) {
            Object obj = null;
            this.this$0 = this$0;
            ReentrantLock lock = this$0.lock;
            lock.lock();
            try {
                this.next = firstNode();
                if (this.next != null) {
                    obj = this.next.item;
                }
                this.nextItem = obj;
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

        void advance() {
            Object obj = null;
            ReentrantLock lock = this.this$0.lock;
            lock.lock();
            try {
                this.next = succ(this.next);
                if (this.next != null) {
                    obj = this.next.item;
                }
                this.nextItem = obj;
            } finally {
                lock.unlock();
            }
        }

        public boolean hasNext() {
            return this.next != null;
        }

        public E next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            this.lastRet = this.next;
            E x = this.nextItem;
            advance();
            return x;
        }

        public void remove() {
            Node<E> n = this.lastRet;
            if (n == null) {
                throw new IllegalStateException();
            }
            this.lastRet = null;
            ReentrantLock lock = this.this$0.lock;
            lock.lock();
            try {
                if (n.item != null) {
                    this.this$0.unlink(n);
                }
                lock.unlock();
            } catch (Throwable th) {
                lock.unlock();
            }
        }
    }

    private class DescendingItr extends AbstractItr {
        private DescendingItr() {
            super(LinkedBlockingDeque.this);
        }

        Node<E> firstNode() {
            return LinkedBlockingDeque.this.last;
        }

        Node<E> nextNode(Node<E> n) {
            return n.prev;
        }
    }

    private class Itr extends AbstractItr {
        private Itr() {
            super(LinkedBlockingDeque.this);
        }

        Node<E> firstNode() {
            return LinkedBlockingDeque.this.first;
        }

        Node<E> nextNode(Node<E> n) {
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

        LBDSpliterator(LinkedBlockingDeque<E> queue) {
            this.queue = queue;
            this.est = (long) queue.size();
        }

        public long estimateSize() {
            return this.est;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Spliterator<E> trySplit() {
            LinkedBlockingDeque<E> q = this.queue;
            int b = this.batch;
            int n = b <= 0 ? 1 : b >= MAX_BATCH ? MAX_BATCH : b + 1;
            if (!this.exhausted) {
                Node<E> h = this.current;
                if (h == null) {
                    h = q.first;
                }
                if (h.next != null) {
                    Object[] a = new Object[n];
                    ReentrantLock lock = q.lock;
                    int i = 0;
                    Node<E> p = this.current;
                    lock.lock();
                    if (p == null) {
                        try {
                            p = q.first;
                        } catch (Throwable th) {
                            lock.unlock();
                        }
                    }
                    do {
                        Object obj = p.item;
                        a[i] = obj;
                        if (obj != null) {
                            i++;
                        }
                        p = p.next;
                        if (p == null) {
                            break;
                        }
                    } while (i < n);
                    lock.unlock();
                    this.current = p;
                    if (p == null) {
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
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            LinkedBlockingDeque<E> q = this.queue;
            ReentrantLock lock = q.lock;
            if (!this.exhausted) {
                this.exhausted = true;
                Node<E> p = this.current;
                do {
                    Object e = null;
                    lock.lock();
                    if (p == null) {
                        try {
                            p = q.first;
                        } catch (Throwable th) {
                            lock.unlock();
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
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            LinkedBlockingDeque<E> q = this.queue;
            ReentrantLock lock = q.lock;
            if (!this.exhausted) {
                Object e = null;
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
                }
            }
            return false;
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
        this((int) AnnualTimeZoneRule.MAX_YEAR);
    }

    public LinkedBlockingDeque(int capacity) {
        this.lock = new ReentrantLock();
        this.notEmpty = this.lock.newCondition();
        this.notFull = this.lock.newCondition();
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
    }

    public LinkedBlockingDeque(Collection<? extends E> c) {
        this((int) AnnualTimeZoneRule.MAX_YEAR);
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (E e : c) {
                if (e == null) {
                    throw new NullPointerException();
                } else if (!linkLast(new Node(e))) {
                    throw new IllegalStateException("Deque full");
                }
            }
        } finally {
            lock.unlock();
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

    void unlink(Node<E> x) {
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
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node(e);
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean linkFirst = linkFirst(node);
            return linkFirst;
        } finally {
            lock.unlock();
        }
    }

    public boolean offerLast(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node(e);
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean linkLast = linkLast(node);
            return linkLast;
        } finally {
            lock.unlock();
        }
    }

    public void putFirst(E e) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node(e);
        ReentrantLock lock = this.lock;
        lock.lock();
        while (!linkFirst(node)) {
            try {
                this.notFull.await();
            } finally {
                lock.unlock();
            }
        }
    }

    public void putLast(E e) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node(e);
        ReentrantLock lock = this.lock;
        lock.lock();
        while (!linkLast(node)) {
            try {
                this.notFull.await();
            } finally {
                lock.unlock();
            }
        }
    }

    public boolean offerFirst(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node(e);
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (!linkFirst(node)) {
            try {
                if (nanos <= 0) {
                    return false;
                }
                nanos = this.notFull.awaitNanos(nanos);
            } finally {
                lock.unlock();
            }
        }
        lock.unlock();
        return true;
    }

    public boolean offerLast(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node(e);
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (!linkLast(node)) {
            try {
                if (nanos <= 0) {
                    return false;
                }
                nanos = this.notFull.awaitNanos(nanos);
            } finally {
                lock.unlock();
            }
        }
        lock.unlock();
        return true;
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
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E unlinkFirst = unlinkFirst();
            return unlinkFirst;
        } finally {
            lock.unlock();
        }
    }

    public E pollLast() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E unlinkLast = unlinkLast();
            return unlinkLast;
        } finally {
            lock.unlock();
        }
    }

    public E takeFirst() throws InterruptedException {
        E x;
        ReentrantLock lock = this.lock;
        lock.lock();
        while (true) {
            try {
                x = unlinkFirst();
                if (x != null) {
                    break;
                }
                this.notEmpty.await();
            } finally {
                lock.unlock();
            }
        }
        return x;
    }

    public E takeLast() throws InterruptedException {
        E x;
        ReentrantLock lock = this.lock;
        lock.lock();
        while (true) {
            try {
                x = unlinkLast();
                if (x != null) {
                    break;
                }
                this.notEmpty.await();
            } finally {
                lock.unlock();
            }
        }
        return x;
    }

    public E pollFirst(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (true) {
            try {
                E x = unlinkFirst();
                if (x != null) {
                    lock.unlock();
                    return x;
                } else if (nanos <= 0) {
                    break;
                } else {
                    nanos = this.notEmpty.awaitNanos(nanos);
                }
            } finally {
                lock.unlock();
            }
        }
        return null;
    }

    public E pollLast(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (true) {
            try {
                E x = unlinkLast();
                if (x != null) {
                    lock.unlock();
                    return x;
                } else if (nanos <= 0) {
                    break;
                } else {
                    nanos = this.notEmpty.awaitNanos(nanos);
                }
            } finally {
                lock.unlock();
            }
        }
        return null;
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
        E e = null;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.first != null) {
                e = this.first.item;
            }
            lock.unlock();
            return e;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public E peekLast() {
        E e = null;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.last != null) {
                e = this.last.item;
            }
            lock.unlock();
            return e;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public boolean removeFirstOccurrence(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Node<E> p = this.first; p != null; p = p.next) {
                if (o.equals(p.item)) {
                    unlink(p);
                    return true;
                }
            }
            lock.unlock();
            return false;
        } finally {
            lock.unlock();
        }
    }

    public boolean removeLastOccurrence(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Node<E> p = this.last; p != null; p = p.prev) {
                if (o.equals(p.item)) {
                    unlink(p);
                    return true;
                }
            }
            lock.unlock();
            return false;
        } finally {
            lock.unlock();
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
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = this.capacity - this.count;
            return i;
        } finally {
            lock.unlock();
        }
    }

    public int drainTo(Collection<? super E> c) {
        return drainTo(c, AnnualTimeZoneRule.MAX_YEAR);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c == this) {
            throw new IllegalArgumentException();
        } else if (maxElements <= 0) {
            return 0;
        } else {
            ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int n = Math.min(maxElements, this.count);
                for (int i = 0; i < n; i++) {
                    c.add(this.first.item);
                    unlinkFirst();
                }
                return n;
            } finally {
                lock.unlock();
            }
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
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = this.count;
            return i;
        } finally {
            lock.unlock();
        }
    }

    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Node<E> p = this.first; p != null; p = p.next) {
                if (o.equals(p.item)) {
                    return true;
                }
            }
            lock.unlock();
            return false;
        } finally {
            lock.unlock();
        }
    }

    public Object[] toArray() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] a = new Object[this.count];
            Node<E> p = this.first;
            int k = 0;
            while (p != null) {
                int k2 = k + 1;
                a[k] = p.item;
                p = p.next;
                k = k2;
            }
            return a;
        } finally {
            lock.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (a.length < this.count) {
                a = (Object[]) Array.newInstance(a.getClass().getComponentType(), this.count);
            }
            Node<E> p = this.first;
            int k = 0;
            while (p != null) {
                int k2 = k + 1;
                a[k] = p.item;
                p = p.next;
                k = k2;
            }
            if (a.length > k) {
                a[k] = null;
            }
            lock.unlock();
            return a;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public String toString() {
        return Helpers.collectionToString(this);
    }

    public void clear() {
        ReentrantLock lock = this.lock;
        lock.lock();
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
            lock.unlock();
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
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            s.defaultWriteObject();
            for (Node<E> p = this.first; p != null; p = p.next) {
                s.writeObject(p.item);
            }
            s.writeObject(null);
        } finally {
            lock.unlock();
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
