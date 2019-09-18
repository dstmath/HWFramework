package java.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DelayQueue<E extends Delayed> extends AbstractQueue<E> implements BlockingQueue<E> {
    private final Condition available = this.lock.newCondition();
    private Thread leader;
    private final transient ReentrantLock lock = new ReentrantLock();
    private final PriorityQueue<E> q = new PriorityQueue<>();

    private class Itr implements Iterator<E> {
        final Object[] array;
        int cursor;
        int lastRet = -1;

        Itr(Object[] array2) {
            this.array = array2;
        }

        public boolean hasNext() {
            return this.cursor < this.array.length;
        }

        public E next() {
            if (this.cursor < this.array.length) {
                this.lastRet = this.cursor;
                E[] eArr = this.array;
                int i = this.cursor;
                this.cursor = i + 1;
                return (Delayed) eArr[i];
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (this.lastRet >= 0) {
                DelayQueue.this.removeEQ(this.array[this.lastRet]);
                this.lastRet = -1;
                return;
            }
            throw new IllegalStateException();
        }
    }

    public DelayQueue() {
    }

    public DelayQueue(Collection<? extends E> c) {
        addAll(c);
    }

    public boolean add(E e) {
        return offer(e);
    }

    public boolean offer(E e) {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            this.q.offer(e);
            if (this.q.peek() == e) {
                this.leader = null;
                this.available.signal();
            }
            return true;
        } finally {
            lock2.unlock();
        }
    }

    public void put(E e) {
        offer(e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e);
    }

    public E poll() {
        E e;
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            E first = (Delayed) this.q.peek();
            if (first != null) {
                if (first.getDelay(TimeUnit.NANOSECONDS) <= 0) {
                    e = (Delayed) this.q.poll();
                    return e;
                }
            }
            e = null;
            return e;
        } finally {
            lock2.unlock();
        }
    }

    public E take() throws InterruptedException {
        Thread thisThread;
        ReentrantLock lock2 = this.lock;
        lock2.lockInterruptibly();
        while (true) {
            try {
                E first = (Delayed) this.q.peek();
                if (first == null) {
                    this.available.await();
                } else {
                    long delay = first.getDelay(TimeUnit.NANOSECONDS);
                    if (delay <= 0) {
                        break;
                    } else if (this.leader != null) {
                        this.available.await();
                    } else {
                        thisThread = Thread.currentThread();
                        this.leader = thisThread;
                        this.available.awaitNanos(delay);
                        if (this.leader == thisThread) {
                            this.leader = null;
                        }
                    }
                }
            } catch (Throwable first2) {
                if (this.leader == null && this.q.peek() != null) {
                    this.available.signal();
                }
                lock2.unlock();
                throw first2;
            }
        }
        E e = (Delayed) this.q.poll();
        if (this.leader == null && this.q.peek() != null) {
            this.available.signal();
        }
        lock2.unlock();
        return e;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        Thread thisThread;
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock2 = this.lock;
        lock2.lockInterruptibly();
        while (true) {
            try {
                E first = (Delayed) this.q.peek();
                if (first != null) {
                    long delay = first.getDelay(TimeUnit.NANOSECONDS);
                    if (delay <= 0) {
                        E e = (Delayed) this.q.poll();
                        if (this.leader == null && this.q.peek() != null) {
                            this.available.signal();
                        }
                        lock2.unlock();
                        return e;
                    } else if (nanos <= 0) {
                        if (this.leader == null && this.q.peek() != null) {
                            this.available.signal();
                        }
                        lock2.unlock();
                        return null;
                    } else {
                        if (nanos >= delay) {
                            if (this.leader == null) {
                                thisThread = Thread.currentThread();
                                this.leader = thisThread;
                                nanos -= delay - this.available.awaitNanos(delay);
                                if (this.leader == thisThread) {
                                    this.leader = null;
                                }
                            }
                        }
                        nanos = this.available.awaitNanos(nanos);
                    }
                } else if (nanos <= 0) {
                    if (this.leader == null && this.q.peek() != null) {
                        this.available.signal();
                    }
                    lock2.unlock();
                    return null;
                } else {
                    nanos = this.available.awaitNanos(nanos);
                }
            } catch (Throwable th) {
                if (this.leader == null && this.q.peek() != null) {
                    this.available.signal();
                }
                lock2.unlock();
                throw th;
            }
        }
    }

    public E peek() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return (Delayed) this.q.peek();
        } finally {
            lock2.unlock();
        }
    }

    public int size() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.q.size();
        } finally {
            lock2.unlock();
        }
    }

    private E peekExpired() {
        E first = (Delayed) this.q.peek();
        if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
            return null;
        }
        return first;
    }

    public int drainTo(Collection<? super E> c) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c != this) {
            ReentrantLock lock2 = this.lock;
            lock2.lock();
            int n = 0;
            while (true) {
                try {
                    E peekExpired = peekExpired();
                    E e = peekExpired;
                    if (peekExpired == null) {
                        return n;
                    }
                    c.add(e);
                    this.q.poll();
                    n++;
                } finally {
                    lock2.unlock();
                }
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
            if (maxElements <= 0) {
                return 0;
            }
            ReentrantLock lock2 = this.lock;
            lock2.lock();
            while (n < maxElements) {
                try {
                    E peekExpired = peekExpired();
                    E e = peekExpired;
                    if (peekExpired == null) {
                        break;
                    }
                    c.add(e);
                    this.q.poll();
                    n++;
                } catch (Throwable th) {
                    lock2.unlock();
                    throw th;
                }
            }
            lock2.unlock();
            return n;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void clear() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            this.q.clear();
        } finally {
            lock2.unlock();
        }
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    public Object[] toArray() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.q.toArray();
        } finally {
            lock2.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.q.toArray(a);
        } finally {
            lock2.unlock();
        }
    }

    public boolean remove(Object o) {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.q.remove(o);
        } finally {
            lock2.unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public void removeEQ(Object o) {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            Iterator<E> it = this.q.iterator();
            while (true) {
                if (it.hasNext()) {
                    if (o == it.next()) {
                        it.remove();
                        break;
                    }
                } else {
                    break;
                }
            }
        } finally {
            lock2.unlock();
        }
    }

    public Iterator<E> iterator() {
        return new Itr(toArray());
    }
}
