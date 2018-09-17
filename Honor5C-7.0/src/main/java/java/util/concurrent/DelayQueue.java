package java.util.concurrent;

import android.icu.util.AnnualTimeZoneRule;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DelayQueue<E extends Delayed> extends AbstractQueue<E> implements BlockingQueue<E> {
    private final Condition available;
    private Thread leader;
    private final transient ReentrantLock lock;
    private final PriorityQueue<E> q;

    private class Itr implements Iterator<E> {
        final Object[] array;
        int cursor;
        int lastRet;

        Itr(Object[] array) {
            this.lastRet = -1;
            this.array = array;
        }

        public boolean hasNext() {
            return this.cursor < this.array.length;
        }

        public E next() {
            if (this.cursor >= this.array.length) {
                throw new NoSuchElementException();
            }
            this.lastRet = this.cursor;
            Object[] objArr = this.array;
            int i = this.cursor;
            this.cursor = i + 1;
            return (Delayed) objArr[i];
        }

        public void remove() {
            if (this.lastRet < 0) {
                throw new IllegalStateException();
            }
            DelayQueue.this.removeEQ(this.array[this.lastRet]);
            this.lastRet = -1;
        }
    }

    public DelayQueue() {
        this.lock = new ReentrantLock();
        this.q = new PriorityQueue();
        this.available = this.lock.newCondition();
    }

    public DelayQueue(Collection<? extends E> c) {
        this.lock = new ReentrantLock();
        this.q = new PriorityQueue();
        this.available = this.lock.newCondition();
        addAll(c);
    }

    public boolean add(E e) {
        return offer((Delayed) e);
    }

    public boolean offer(E e) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.q.offer(e);
            if (this.q.peek() == e) {
                this.leader = null;
                this.available.signal();
            }
            lock.unlock();
            return true;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public void put(E e) {
        offer((Delayed) e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer((Delayed) e);
    }

    public E poll() {
        E e = null;
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Delayed first = (Delayed) this.q.peek();
            if (first != null && first.getDelay(TimeUnit.NANOSECONDS) <= 0) {
                Delayed delayed = (Delayed) this.q.poll();
            }
            lock.unlock();
            return e;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (true) {
            Thread thisThread;
            try {
                Delayed first = (Delayed) this.q.peek();
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
            } catch (Throwable th) {
                if (this.leader == null && this.q.peek() != null) {
                    this.available.signal();
                }
                lock.unlock();
            }
        }
        Delayed delayed = (Delayed) this.q.poll();
        if (this.leader == null && this.q.peek() != null) {
            this.available.signal();
        }
        lock.unlock();
        return delayed;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (true) {
            Delayed first = (Delayed) this.q.peek();
            if (first != null) {
                long delay = first.getDelay(TimeUnit.NANOSECONDS);
                if (delay <= 0) {
                    break;
                } else if (nanos <= 0) {
                    break;
                } else {
                    if (nanos >= delay) {
                        if (this.leader == null) {
                            Thread thisThread = Thread.currentThread();
                            this.leader = thisThread;
                            try {
                                nanos -= delay - this.available.awaitNanos(delay);
                                if (this.leader == thisThread) {
                                    this.leader = null;
                                }
                            } catch (Throwable th) {
                                if (this.leader == null && this.q.peek() != null) {
                                    this.available.signal();
                                }
                                lock.unlock();
                            }
                        }
                    }
                    nanos = this.available.awaitNanos(nanos);
                }
            } else if (nanos <= 0) {
                break;
            } else {
                nanos = this.available.awaitNanos(nanos);
            }
        }
        if (this.leader == null && this.q.peek() != null) {
            this.available.signal();
        }
        lock.unlock();
        return null;
    }

    public E peek() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Delayed delayed = (Delayed) this.q.peek();
            return delayed;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int size = this.q.size();
            return size;
        } finally {
            lock.unlock();
        }
    }

    private E peekExpired() {
        Delayed first = (Delayed) this.q.peek();
        return (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) ? null : first;
    }

    public int drainTo(Collection<? super E> c) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c == this) {
            throw new IllegalArgumentException();
        } else {
            ReentrantLock lock = this.lock;
            lock.lock();
            int n = 0;
            while (true) {
                try {
                    E e = peekExpired();
                    if (e == null) {
                        break;
                    }
                    c.add(e);
                    this.q.poll();
                    n++;
                } finally {
                    lock.unlock();
                }
            }
            return n;
        }
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
            int n = 0;
            while (n < maxElements) {
                try {
                    E e = peekExpired();
                    if (e == null) {
                        break;
                    }
                    c.add(e);
                    this.q.poll();
                    n++;
                } catch (Throwable th) {
                    lock.unlock();
                }
            }
            lock.unlock();
            return n;
        }
    }

    public void clear() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.q.clear();
        } finally {
            lock.unlock();
        }
    }

    public int remainingCapacity() {
        return AnnualTimeZoneRule.MAX_YEAR;
    }

    public Object[] toArray() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] toArray = this.q.toArray();
            return toArray;
        } finally {
            lock.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            T[] toArray = this.q.toArray(a);
            return toArray;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(Object o) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean remove = this.q.remove(o);
            return remove;
        } finally {
            lock.unlock();
        }
    }

    void removeEQ(Object o) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Iterator<E> it = this.q.iterator();
            while (it.hasNext()) {
                if (o == it.next()) {
                    it.remove();
                    break;
                }
            }
            lock.unlock();
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public Iterator<E> iterator() {
        return new Itr(toArray());
    }
}
