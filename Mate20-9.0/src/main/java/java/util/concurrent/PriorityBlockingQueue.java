package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import sun.misc.Unsafe;

public class PriorityBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, Serializable {
    private static final long ALLOCATIONSPINLOCK;
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    private static final int MAX_ARRAY_SIZE = 2147483639;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = 5595510919245408276L;
    private volatile transient int allocationSpinLock;
    private transient Comparator<? super E> comparator;
    private final ReentrantLock lock;
    private final Condition notEmpty;
    private PriorityQueue<E> q;
    private transient Object[] queue;
    private transient int size;

    final class Itr implements Iterator<E> {
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
                return eArr[i];
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (this.lastRet >= 0) {
                PriorityBlockingQueue.this.removeEQ(this.array[this.lastRet]);
                this.lastRet = -1;
                return;
            }
            throw new IllegalStateException();
        }
    }

    static final class PBQSpliterator<E> implements Spliterator<E> {
        Object[] array;
        int fence;
        int index;
        final PriorityBlockingQueue<E> queue;

        PBQSpliterator(PriorityBlockingQueue<E> queue2, Object[] array2, int index2, int fence2) {
            this.queue = queue2;
            this.array = array2;
            this.index = index2;
            this.fence = fence2;
        }

        /* access modifiers changed from: package-private */
        public final int getFence() {
            int i = this.fence;
            int hi = i;
            if (i >= 0) {
                return hi;
            }
            Object[] array2 = this.queue.toArray();
            this.array = array2;
            int hi2 = array2.length;
            this.fence = hi2;
            return hi2;
        }

        public PBQSpliterator<E> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = (lo + hi) >>> 1;
            if (lo >= mid) {
                return null;
            }
            PriorityBlockingQueue<E> priorityBlockingQueue = this.queue;
            Object[] objArr = this.array;
            this.index = mid;
            return new PBQSpliterator<>(priorityBlockingQueue, objArr, lo, mid);
        }

        public void forEachRemaining(Consumer<? super E> action) {
            if (action != null) {
                Object[] objArr = this.array;
                Object[] a = objArr;
                if (objArr == null) {
                    Object[] array2 = this.queue.toArray();
                    a = array2;
                    this.fence = array2.length;
                }
                int i = this.fence;
                int hi = i;
                if (i <= a.length) {
                    int i2 = this.index;
                    int i3 = i2;
                    if (i2 >= 0) {
                        this.index = hi;
                        if (i3 < hi) {
                            do {
                                action.accept(a[i3]);
                                i3++;
                            } while (i3 < hi);
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (getFence() <= this.index || this.index < 0) {
                return false;
            } else {
                E[] eArr = this.array;
                int i = this.index;
                this.index = i + 1;
                action.accept(eArr[i]);
                return true;
            }
        }

        public long estimateSize() {
            return (long) (getFence() - this.index);
        }

        public int characteristics() {
            return 16704;
        }
    }

    public PriorityBlockingQueue() {
        this(11, null);
    }

    public PriorityBlockingQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    public PriorityBlockingQueue(int initialCapacity, Comparator<? super E> comparator2) {
        if (initialCapacity >= 1) {
            this.lock = new ReentrantLock();
            this.notEmpty = this.lock.newCondition();
            this.comparator = comparator2;
            this.queue = new Object[initialCapacity];
            return;
        }
        throw new IllegalArgumentException();
    }

    public PriorityBlockingQueue(Collection<? extends E> c) {
        this.lock = new ReentrantLock();
        this.notEmpty = this.lock.newCondition();
        boolean heapify = true;
        boolean screen = true;
        if (c instanceof SortedSet) {
            this.comparator = ((SortedSet) c).comparator();
            heapify = false;
        } else if (c instanceof PriorityBlockingQueue) {
            PriorityBlockingQueue<? extends E> pq = (PriorityBlockingQueue) c;
            this.comparator = pq.comparator();
            screen = false;
            if (pq.getClass() == PriorityBlockingQueue.class) {
                heapify = false;
            }
        }
        Object[] a = c.toArray();
        int n = a.length;
        a = a.getClass() != Object[].class ? Arrays.copyOf(a, n, Object[].class) : a;
        if (screen && (n == 1 || this.comparator != null)) {
            int i = 0;
            while (i < n) {
                if (a[i] != null) {
                    i++;
                } else {
                    throw new NullPointerException();
                }
            }
        }
        this.queue = a;
        this.size = n;
        if (heapify) {
            heapify();
        }
    }

    private void tryGrow(Object[] array, int oldCap) {
        int i;
        this.lock.unlock();
        Object[] newArray = null;
        if (this.allocationSpinLock == 0) {
            if (U.compareAndSwapInt(this, ALLOCATIONSPINLOCK, 0, 1)) {
                if (oldCap < 64) {
                    i = oldCap + 2;
                } else {
                    i = oldCap >> 1;
                }
                int newCap = i + oldCap;
                if (newCap - MAX_ARRAY_SIZE > 0) {
                    int minCap = oldCap + 1;
                    if (minCap < 0 || minCap > MAX_ARRAY_SIZE) {
                        try {
                            throw new OutOfMemoryError();
                        } catch (Throwable th) {
                            this.allocationSpinLock = 0;
                            throw th;
                        }
                    } else {
                        newCap = MAX_ARRAY_SIZE;
                    }
                }
                if (newCap > oldCap && this.queue == array) {
                    newArray = new Object[newCap];
                }
                this.allocationSpinLock = 0;
            }
        }
        if (newArray == null) {
            Thread.yield();
        }
        this.lock.lock();
        if (newArray != null && this.queue == array) {
            this.queue = newArray;
            System.arraycopy((Object) array, 0, (Object) newArray, 0, oldCap);
        }
    }

    private E dequeue() {
        int n = this.size - 1;
        if (n < 0) {
            return null;
        }
        E[] array = this.queue;
        E result = array[0];
        E x = array[n];
        array[n] = null;
        Comparator<? super E> cmp = this.comparator;
        if (cmp == null) {
            siftDownComparable(0, x, array, n);
        } else {
            siftDownUsingComparator(0, x, array, n, cmp);
        }
        this.size = n;
        return result;
    }

    private static <T> void siftUpComparable(int k, T x, Object[] array) {
        Comparable<? super T> key = x;
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = array[parent];
            if (key.compareTo(e) >= 0) {
                break;
            }
            array[k] = e;
            k = parent;
        }
        array[k] = key;
    }

    private static <T> void siftUpUsingComparator(int k, T x, Object[] array, Comparator<? super T> cmp) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = array[parent];
            if (cmp.compare(x, e) >= 0) {
                break;
            }
            array[k] = e;
            k = parent;
        }
        array[k] = x;
    }

    private static <T> void siftDownComparable(int k, T x, Object[] array, int n) {
        if (n > 0) {
            Comparable<? super T> key = x;
            int half = n >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                Comparable comparable = array[child];
                int right = child + 1;
                if (right < n && comparable.compareTo(array[right]) > 0) {
                    child = right;
                    comparable = array[right];
                }
                if (key.compareTo(comparable) <= 0) {
                    break;
                }
                array[k] = comparable;
                k = child;
            }
            array[k] = key;
        }
    }

    private static <T> void siftDownUsingComparator(int k, T x, Object[] array, int n, Comparator<? super T> cmp) {
        if (n > 0) {
            int half = n >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                Object c = array[child];
                int right = child + 1;
                if (right < n && cmp.compare(c, array[right]) > 0) {
                    child = right;
                    c = array[right];
                }
                if (cmp.compare(x, c) <= 0) {
                    break;
                }
                array[k] = c;
                k = child;
            }
            array[k] = x;
        }
    }

    private void heapify() {
        Object[] array = this.queue;
        int n = this.size;
        int half = (n >>> 1) - 1;
        Comparator<? super E> cmp = this.comparator;
        if (cmp == null) {
            for (int i = half; i >= 0; i--) {
                siftDownComparable(i, array[i], array, n);
            }
            return;
        }
        for (int i2 = half; i2 >= 0; i2--) {
            siftDownUsingComparator(i2, array[i2], array, n, cmp);
        }
    }

    public boolean add(E e) {
        return offer(e);
    }

    /* JADX INFO: finally extract failed */
    public boolean offer(E e) {
        int n;
        Object[] array;
        if (e != null) {
            ReentrantLock lock2 = this.lock;
            lock2.lock();
            while (true) {
                int i = this.size;
                n = i;
                Object[] objArr = this.queue;
                array = objArr;
                int length = objArr.length;
                int cap = length;
                if (i >= length) {
                    tryGrow(array, cap);
                } else {
                    try {
                        break;
                    } catch (Throwable th) {
                        lock2.unlock();
                        throw th;
                    }
                }
            }
            Comparator<? super E> cmp = this.comparator;
            if (cmp == null) {
                siftUpComparable(n, e, array);
            } else {
                siftUpUsingComparator(n, e, array, cmp);
            }
            this.size = n + 1;
            this.notEmpty.signal();
            lock2.unlock();
            return true;
        }
        throw new NullPointerException();
    }

    public void put(E e) {
        offer(e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e);
    }

    public E poll() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return dequeue();
        } finally {
            lock2.unlock();
        }
    }

    public E take() throws InterruptedException {
        ReentrantLock lock2 = this.lock;
        lock2.lockInterruptibly();
        while (true) {
            try {
                E dequeue = dequeue();
                E result = dequeue;
                if (dequeue != null) {
                    return result;
                }
                this.notEmpty.await();
            } finally {
                lock2.unlock();
            }
        }
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E result;
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock2 = this.lock;
        lock2.lockInterruptibly();
        while (true) {
            try {
                E dequeue = dequeue();
                result = dequeue;
                if (dequeue == null && nanos > ALLOCATIONSPINLOCK) {
                    nanos = this.notEmpty.awaitNanos(nanos);
                }
            } finally {
                lock2.unlock();
            }
        }
        return result;
    }

    public E peek() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.size == 0 ? null : this.queue[0];
        } finally {
            lock2.unlock();
        }
    }

    public Comparator<? super E> comparator() {
        return this.comparator;
    }

    public int size() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.size;
        } finally {
            lock2.unlock();
        }
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    private int indexOf(Object o) {
        if (o != null) {
            Object[] array = this.queue;
            int n = this.size;
            for (int i = 0; i < n; i++) {
                if (o.equals(array[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void removeAt(int i) {
        E[] array = this.queue;
        int n = this.size - 1;
        if (n == i) {
            array[i] = null;
        } else {
            E moved = array[n];
            array[n] = null;
            Comparator<? super E> cmp = this.comparator;
            if (cmp == null) {
                siftDownComparable(i, moved, array, n);
            } else {
                siftDownUsingComparator(i, moved, array, n, cmp);
            }
            if (array[i] == moved) {
                if (cmp == null) {
                    siftUpComparable(i, moved, array);
                } else {
                    siftUpUsingComparator(i, moved, array, cmp);
                }
            }
        }
        this.size = n;
    }

    public boolean remove(Object o) {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            int i = indexOf(o);
            if (i == -1) {
                return false;
            }
            removeAt(i);
            lock2.unlock();
            return true;
        } finally {
            lock2.unlock();
        }
    }

    /* access modifiers changed from: package-private */
    public void removeEQ(Object o) {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            Object[] array = this.queue;
            int i = 0;
            int n = this.size;
            while (true) {
                if (i >= n) {
                    break;
                } else if (o == array[i]) {
                    removeAt(i);
                    break;
                } else {
                    i++;
                }
            }
        } finally {
            lock2.unlock();
        }
    }

    public boolean contains(Object o) {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return indexOf(o) != -1;
        } finally {
            lock2.unlock();
        }
    }

    public String toString() {
        return Helpers.collectionToString(this);
    }

    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c == this) {
            throw new IllegalArgumentException();
        } else if (maxElements <= 0) {
            return 0;
        } else {
            ReentrantLock lock2 = this.lock;
            lock2.lock();
            try {
                int n = Math.min(this.size, maxElements);
                for (int i = 0; i < n; i++) {
                    c.add(this.queue[0]);
                    dequeue();
                }
                return n;
            } finally {
                lock2.unlock();
            }
        }
    }

    public void clear() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            Object[] array = this.queue;
            int n = this.size;
            this.size = 0;
            for (int i = 0; i < n; i++) {
                array[i] = null;
            }
        } finally {
            lock2.unlock();
        }
    }

    public Object[] toArray() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return Arrays.copyOf((T[]) this.queue, this.size);
        } finally {
            lock2.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            int n = this.size;
            if (a.length < n) {
                return Arrays.copyOf(this.queue, this.size, a.getClass());
            }
            System.arraycopy((Object) this.queue, 0, (Object) a, 0, n);
            if (a.length > n) {
                a[n] = null;
            }
            lock2.unlock();
            return a;
        } finally {
            lock2.unlock();
        }
    }

    public Iterator<E> iterator() {
        return new Itr(toArray());
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        this.lock.lock();
        try {
            this.q = new PriorityQueue<>(Math.max(this.size, 1), this.comparator);
            this.q.addAll(this);
            s.defaultWriteObject();
        } finally {
            this.q = null;
            this.lock.unlock();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        try {
            s.defaultReadObject();
            this.queue = new Object[this.q.size()];
            this.comparator = this.q.comparator();
            addAll(this.q);
        } finally {
            this.q = null;
        }
    }

    public Spliterator<E> spliterator() {
        return new PBQSpliterator(this, null, 0, -1);
    }

    static {
        try {
            ALLOCATIONSPINLOCK = U.objectFieldOffset(PriorityBlockingQueue.class.getDeclaredField("allocationSpinLock"));
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }
}
