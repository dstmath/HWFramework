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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class LinkedBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, Serializable {
    private static final long serialVersionUID = -6903933977591709194L;
    private final int capacity;
    private final AtomicInteger count;
    transient Node<E> head;
    private transient Node<E> last;
    private final Condition notEmpty;
    private final Condition notFull;
    private final ReentrantLock putLock;
    private final ReentrantLock takeLock;

    private class Itr implements Iterator<E> {
        private Node<E> current;
        private E currentElement;
        private Node<E> lastRet;

        Itr() {
            LinkedBlockingQueue.this.fullyLock();
            try {
                this.current = LinkedBlockingQueue.this.head.next;
                if (this.current != null) {
                    this.currentElement = this.current.item;
                }
                LinkedBlockingQueue.this.fullyUnlock();
            } catch (Throwable th) {
                LinkedBlockingQueue.this.fullyUnlock();
            }
        }

        public boolean hasNext() {
            return this.current != null;
        }

        public E next() {
            LinkedBlockingQueue.this.fullyLock();
            try {
                if (this.current == null) {
                    throw new NoSuchElementException();
                }
                Node<E> q;
                this.lastRet = this.current;
                Object item = null;
                Node<E> p = this.current;
                while (true) {
                    q = p.next;
                    if (q == p) {
                        q = LinkedBlockingQueue.this.head.next;
                    }
                    if (q == null) {
                        break;
                    }
                    item = q.item;
                    if (item != null) {
                        break;
                    }
                    p = q;
                }
                this.current = q;
                E x = this.currentElement;
                this.currentElement = item;
                return x;
            } finally {
                LinkedBlockingQueue.this.fullyUnlock();
            }
        }

        public void remove() {
            if (this.lastRet == null) {
                throw new IllegalStateException();
            }
            LinkedBlockingQueue.this.fullyLock();
            try {
                Node<E> node = this.lastRet;
                this.lastRet = null;
                Node<E> trail = LinkedBlockingQueue.this.head;
                Node<E> p = trail.next;
                while (p != null) {
                    if (p == node) {
                        LinkedBlockingQueue.this.unlink(p, trail);
                        break;
                    } else {
                        trail = p;
                        p = p.next;
                    }
                }
                LinkedBlockingQueue.this.fullyUnlock();
            } catch (Throwable th) {
                LinkedBlockingQueue.this.fullyUnlock();
            }
        }
    }

    static final class LBQSpliterator<E> implements Spliterator<E> {
        static final int MAX_BATCH = 33554432;
        int batch;
        Node<E> current;
        long est;
        boolean exhausted;
        final LinkedBlockingQueue<E> queue;

        LBQSpliterator(LinkedBlockingQueue<E> queue) {
            this.queue = queue;
            this.est = (long) queue.size();
        }

        public long estimateSize() {
            return this.est;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Spliterator<E> trySplit() {
            LinkedBlockingQueue<E> q = this.queue;
            int b = this.batch;
            int n = b <= 0 ? 1 : b >= MAX_BATCH ? MAX_BATCH : b + 1;
            if (!this.exhausted) {
                Node<E> h = this.current;
                if (h == null) {
                    h = q.head.next;
                }
                if (h.next != null) {
                    Object[] a = new Object[n];
                    int i = 0;
                    Node<E> p = this.current;
                    q.fullyLock();
                    if (p == null) {
                        try {
                            p = q.head.next;
                        } catch (Throwable th) {
                            q.fullyUnlock();
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
                    q.fullyUnlock();
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
            LinkedBlockingQueue<E> q = this.queue;
            if (!this.exhausted) {
                this.exhausted = true;
                Node<E> p = this.current;
                do {
                    Object e = null;
                    q.fullyLock();
                    if (p == null) {
                        try {
                            p = q.head.next;
                        } catch (Throwable th) {
                            q.fullyUnlock();
                        }
                    }
                    while (p != null) {
                        e = p.item;
                        p = p.next;
                        if (e != null) {
                            break;
                        }
                    }
                    q.fullyUnlock();
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
            LinkedBlockingQueue<E> q = this.queue;
            if (!this.exhausted) {
                Object e = null;
                q.fullyLock();
                try {
                    if (this.current == null) {
                        this.current = q.head.next;
                    }
                    while (this.current != null) {
                        e = this.current.item;
                        this.current = this.current.next;
                        if (e != null) {
                            break;
                        }
                    }
                    q.fullyUnlock();
                    if (this.current == null) {
                        this.exhausted = true;
                    }
                    if (e != null) {
                        action.accept(e);
                        return true;
                    }
                } catch (Throwable th) {
                    q.fullyUnlock();
                }
            }
            return false;
        }

        public int characteristics() {
            return 4368;
        }
    }

    static class Node<E> {
        E item;
        Node<E> next;

        Node(E x) {
            this.item = x;
        }
    }

    private void signalNotEmpty() {
        ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            this.notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    private void signalNotFull() {
        ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            this.notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    private void enqueue(Node<E> node) {
        this.last.next = node;
        this.last = node;
    }

    private E dequeue() {
        Node<E> h = this.head;
        Node<E> first = h.next;
        h.next = h;
        this.head = first;
        E x = first.item;
        first.item = null;
        return x;
    }

    void fullyLock() {
        this.putLock.lock();
        this.takeLock.lock();
    }

    void fullyUnlock() {
        this.takeLock.unlock();
        this.putLock.unlock();
    }

    public LinkedBlockingQueue() {
        this((int) AnnualTimeZoneRule.MAX_YEAR);
    }

    public LinkedBlockingQueue(int capacity) {
        this.count = new AtomicInteger();
        this.takeLock = new ReentrantLock();
        this.notEmpty = this.takeLock.newCondition();
        this.putLock = new ReentrantLock();
        this.notFull = this.putLock.newCondition();
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
        Node node = new Node(null);
        this.head = node;
        this.last = node;
    }

    public LinkedBlockingQueue(Collection<? extends E> c) {
        this((int) AnnualTimeZoneRule.MAX_YEAR);
        ReentrantLock putLock = this.putLock;
        putLock.lock();
        int n = 0;
        try {
            for (E e : c) {
                if (e == null) {
                    throw new NullPointerException();
                } else if (n == this.capacity) {
                    throw new IllegalStateException("Queue full");
                } else {
                    enqueue(new Node(e));
                    n++;
                }
            }
            this.count.set(n);
        } finally {
            putLock.unlock();
        }
    }

    public int size() {
        return this.count.get();
    }

    public int remainingCapacity() {
        return this.capacity - this.count.get();
    }

    public void put(E e) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node(e);
        ReentrantLock putLock = this.putLock;
        AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        while (count.get() == this.capacity) {
            try {
                this.notFull.await();
            } catch (Throwable th) {
                putLock.unlock();
            }
        }
        enqueue(node);
        int c = count.getAndIncrement();
        if (c + 1 < this.capacity) {
            this.notFull.signal();
        }
        putLock.unlock();
        if (c == 0) {
            signalNotEmpty();
        }
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos(timeout);
        ReentrantLock putLock = this.putLock;
        AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        while (count.get() == this.capacity) {
            try {
                if (nanos <= 0) {
                    return false;
                }
                nanos = this.notFull.awaitNanos(nanos);
            } finally {
                putLock.unlock();
            }
        }
        enqueue(new Node(e));
        int c = count.getAndIncrement();
        if (c + 1 < this.capacity) {
            this.notFull.signal();
        }
        putLock.unlock();
        if (c == 0) {
            signalNotEmpty();
        }
        return true;
    }

    public boolean offer(E e) {
        boolean z = false;
        if (e == null) {
            throw new NullPointerException();
        }
        AtomicInteger count = this.count;
        if (count.get() == this.capacity) {
            return false;
        }
        int c = -1;
        Node<E> node = new Node(e);
        ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            if (count.get() < this.capacity) {
                enqueue(node);
                c = count.getAndIncrement();
                if (c + 1 < this.capacity) {
                    this.notFull.signal();
                }
            }
            putLock.unlock();
            if (c == 0) {
                signalNotEmpty();
            }
            if (c >= 0) {
                z = true;
            }
            return z;
        } catch (Throwable th) {
            putLock.unlock();
        }
    }

    public E take() throws InterruptedException {
        AtomicInteger count = this.count;
        ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        while (count.get() == 0) {
            try {
                this.notEmpty.await();
            } catch (Throwable th) {
                takeLock.unlock();
            }
        }
        E x = dequeue();
        int c = count.getAndDecrement();
        if (c > 1) {
            this.notEmpty.signal();
        }
        takeLock.unlock();
        if (c == this.capacity) {
            signalNotFull();
        }
        return x;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        AtomicInteger count = this.count;
        ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        while (count.get() == 0) {
            try {
                if (nanos <= 0) {
                    return null;
                }
                nanos = this.notEmpty.awaitNanos(nanos);
            } finally {
                takeLock.unlock();
            }
        }
        E x = dequeue();
        int c = count.getAndDecrement();
        if (c > 1) {
            this.notEmpty.signal();
        }
        takeLock.unlock();
        if (c == this.capacity) {
            signalNotFull();
        }
        return x;
    }

    public E poll() {
        AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        }
        E x = null;
        int c = -1;
        ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            if (count.get() > 0) {
                x = dequeue();
                c = count.getAndDecrement();
                if (c > 1) {
                    this.notEmpty.signal();
                }
            }
            takeLock.unlock();
            if (c == this.capacity) {
                signalNotFull();
            }
            return x;
        } catch (Throwable th) {
            takeLock.unlock();
        }
    }

    public E peek() {
        E e = null;
        if (this.count.get() == 0) {
            return null;
        }
        ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            if (this.count.get() > 0) {
                e = this.head.next.item;
            }
            takeLock.unlock();
            return e;
        } catch (Throwable th) {
            takeLock.unlock();
        }
    }

    void unlink(Node<E> p, Node<E> trail) {
        p.item = null;
        trail.next = p.next;
        if (this.last == p) {
            this.last = trail;
        }
        if (this.count.getAndDecrement() == this.capacity) {
            this.notFull.signal();
        }
    }

    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        fullyLock();
        try {
            Node<E> trail = this.head;
            for (Node<E> p = trail.next; p != null; p = p.next) {
                if (o.equals(p.item)) {
                    unlink(p, trail);
                    return true;
                }
                trail = p;
            }
            fullyUnlock();
            return false;
        } finally {
            fullyUnlock();
        }
    }

    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        fullyLock();
        try {
            for (Node<E> p = this.head.next; p != null; p = p.next) {
                if (o.equals(p.item)) {
                    return true;
                }
            }
            fullyUnlock();
            return false;
        } finally {
            fullyUnlock();
        }
    }

    public Object[] toArray() {
        fullyLock();
        try {
            Object[] a = new Object[this.count.get()];
            Node<E> p = this.head.next;
            int k = 0;
            while (p != null) {
                int k2 = k + 1;
                a[k] = p.item;
                p = p.next;
                k = k2;
            }
            return a;
        } finally {
            fullyUnlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        fullyLock();
        try {
            int size = this.count.get();
            if (a.length < size) {
                a = (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
            }
            Node<E> p = this.head.next;
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
            fullyUnlock();
            return a;
        } catch (Throwable th) {
            fullyUnlock();
        }
    }

    public String toString() {
        return Helpers.collectionToString(this);
    }

    public void clear() {
        fullyLock();
        try {
            Node<E> h = this.head;
            while (true) {
                Node<E> p = h.next;
                if (p == null) {
                    break;
                }
                h.next = h;
                p.item = null;
                h = p;
            }
            this.head = this.last;
            if (this.count.getAndSet(0) == this.capacity) {
                this.notFull.signal();
            }
            fullyUnlock();
        } catch (Throwable th) {
            fullyUnlock();
        }
    }

    public int drainTo(Collection<? super E> c) {
        return drainTo(c, AnnualTimeZoneRule.MAX_YEAR);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        int i;
        if (c == null) {
            throw new NullPointerException();
        } else if (c == this) {
            throw new IllegalArgumentException();
        } else if (maxElements <= 0) {
            return 0;
        } else {
            boolean signalNotFull = false;
            ReentrantLock takeLock = this.takeLock;
            takeLock.lock();
            Node<E> h;
            try {
                int n = Math.min(maxElements, this.count.get());
                h = this.head;
                i = 0;
                while (i < n) {
                    Node<E> p = h.next;
                    c.add(p.item);
                    p.item = null;
                    h.next = h;
                    h = p;
                    i++;
                }
                if (i > 0) {
                    this.head = h;
                    signalNotFull = this.count.getAndAdd(-i) == this.capacity;
                }
                takeLock.unlock();
                if (signalNotFull) {
                    signalNotFull();
                }
                return n;
            } catch (Throwable th) {
                takeLock.unlock();
                if (null != null) {
                    signalNotFull();
                }
            }
        }
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    public Spliterator<E> spliterator() {
        return new LBQSpliterator(this);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        fullyLock();
        try {
            s.defaultWriteObject();
            for (Node<E> p = this.head.next; p != null; p = p.next) {
                s.writeObject(p.item);
            }
            s.writeObject(null);
        } finally {
            fullyUnlock();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.count.set(0);
        Node node = new Node(null);
        this.head = node;
        this.last = node;
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
