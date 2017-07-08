package java.util.concurrent;

import android.icu.util.AnnualTimeZoneRule;
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
    private static final long ALLOCATIONSPINLOCK = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    private static final int MAX_ARRAY_SIZE = 2147483639;
    private static final Unsafe U = null;
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
            return objArr[i];
        }

        public void remove() {
            if (this.lastRet < 0) {
                throw new IllegalStateException();
            }
            PriorityBlockingQueue.this.removeEQ(this.array[this.lastRet]);
            this.lastRet = -1;
        }
    }

    static final class PBQSpliterator<E> implements Spliterator<E> {
        Object[] array;
        int fence;
        int index;
        final PriorityBlockingQueue<E> queue;

        PBQSpliterator(PriorityBlockingQueue<E> queue, Object[] array, int index, int fence) {
            this.queue = queue;
            this.array = array;
            this.index = index;
            this.fence = fence;
        }

        final int getFence() {
            int hi = this.fence;
            if (hi >= 0) {
                return hi;
            }
            Object[] toArray = this.queue.toArray();
            this.array = toArray;
            hi = toArray.length;
            this.fence = hi;
            return hi;
        }

        public PBQSpliterator<E> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = (lo + hi) >>> 1;
            if (lo >= mid) {
                return null;
            }
            PriorityBlockingQueue priorityBlockingQueue = this.queue;
            Object[] objArr = this.array;
            this.index = mid;
            return new PBQSpliterator(priorityBlockingQueue, objArr, lo, mid);
        }

        public void forEachRemaining(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Object[] a = this.array;
            if (a == null) {
                a = this.queue.toArray();
                this.fence = a.length;
            }
            int hi = this.fence;
            if (hi <= a.length) {
                int i = this.index;
                if (i >= 0) {
                    this.index = hi;
                    if (i < hi) {
                        do {
                            action.accept(a[i]);
                            i++;
                        } while (i < hi);
                    }
                }
            }
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (getFence() <= this.index || this.index < 0) {
                return false;
            } else {
                Object[] objArr = this.array;
                int i = this.index;
                this.index = i + 1;
                action.accept(objArr[i]);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.PriorityBlockingQueue.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.PriorityBlockingQueue.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.PriorityBlockingQueue.<clinit>():void");
    }

    public PriorityBlockingQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }

    public PriorityBlockingQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    public PriorityBlockingQueue(int initialCapacity, Comparator<? super E> comparator) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException();
        }
        this.lock = new ReentrantLock();
        this.notEmpty = this.lock.newCondition();
        this.comparator = comparator;
        this.queue = new Object[initialCapacity];
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
        if (a.getClass() != Object[].class) {
            a = Arrays.copyOf(a, n, Object[].class);
        }
        if (screen && (n == 1 || this.comparator != null)) {
            for (int i = 0; i < n; i++) {
                if (a[i] == null) {
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
        this.lock.unlock();
        Object obj = null;
        if (this.allocationSpinLock == 0 && U.compareAndSwapInt(this, ALLOCATIONSPINLOCK, 0, 1)) {
            int i;
            if (oldCap < 64) {
                i = oldCap + 2;
            } else {
                i = oldCap >> 1;
            }
            int newCap = oldCap + i;
            if (newCap - MAX_ARRAY_SIZE > 0) {
                int minCap = oldCap + 1;
                if (minCap < 0 || minCap > MAX_ARRAY_SIZE) {
                    try {
                        throw new OutOfMemoryError();
                    } catch (Throwable th) {
                        this.allocationSpinLock = 0;
                    }
                } else {
                    newCap = MAX_ARRAY_SIZE;
                }
            }
            if (newCap > oldCap) {
                if (this.queue == array) {
                    obj = new Object[newCap];
                }
            }
            this.allocationSpinLock = 0;
        }
        if (obj == null) {
            Thread.yield();
        }
        this.lock.lock();
        if (obj != null && this.queue == array) {
            this.queue = obj;
            System.arraycopy(array, 0, obj, 0, oldCap);
        }
    }

    private E dequeue() {
        int n = this.size - 1;
        if (n < 0) {
            return null;
        }
        Object[] array = this.queue;
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
        Comparable<? super T> key = (Comparable) x;
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
            Comparable<? super T> key = (Comparable) x;
            int half = n >>> 1;
            while (k < half) {
                int child = (k << 1) + 1;
                Object c = array[child];
                int right = child + 1;
                if (right < n && ((Comparable) c).compareTo(array[right]) > 0) {
                    child = right;
                    c = array[right];
                }
                if (key.compareTo(c) <= 0) {
                    break;
                }
                array[k] = c;
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
        int i;
        if (cmp == null) {
            for (i = half; i >= 0; i--) {
                siftDownComparable(i, array[i], array, n);
            }
            return;
        }
        for (i = half; i >= 0; i--) {
            siftDownUsingComparator(i, array[i], array, n, cmp);
        }
    }

    public boolean add(E e) {
        return offer(e);
    }

    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        int n;
        ReentrantLock lock = this.lock;
        lock.lock();
        while (true) {
            n = this.size;
            Object[] array = this.queue;
            int cap = array.length;
            if (n >= cap) {
                tryGrow(array, cap);
            } else {
                try {
                    break;
                } finally {
                    lock.unlock();
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
        return true;
    }

    public void put(E e) {
        offer(e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e);
    }

    public E poll() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E dequeue = dequeue();
            return dequeue;
        } finally {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        E result;
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (true) {
            try {
                result = dequeue();
                if (result != null) {
                    break;
                }
                this.notEmpty.await();
            } finally {
                lock.unlock();
            }
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E result;
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (true) {
            try {
                result = dequeue();
                if (result != null || nanos <= ALLOCATIONSPINLOCK) {
                    lock.unlock();
                } else {
                    nanos = this.notEmpty.awaitNanos(nanos);
                }
            } catch (Throwable th) {
                lock.unlock();
            }
        }
        lock.unlock();
        return result;
    }

    public E peek() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E e = this.size == 0 ? null : this.queue[0];
            lock.unlock();
            return e;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public Comparator<? super E> comparator() {
        return this.comparator;
    }

    public int size() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = this.size;
            return i;
        } finally {
            lock.unlock();
        }
    }

    public int remainingCapacity() {
        return AnnualTimeZoneRule.MAX_YEAR;
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
        Object[] array = this.queue;
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
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int i = indexOf(o);
            if (i == -1) {
                return false;
            }
            removeAt(i);
            lock.unlock();
            return true;
        } finally {
            lock.unlock();
        }
    }

    void removeEQ(Object o) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] array = this.queue;
            int n = this.size;
            for (int i = 0; i < n; i++) {
                if (o == array[i]) {
                    removeAt(i);
                    break;
                }
            }
            lock.unlock();
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public boolean contains(Object o) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            boolean z = indexOf(o) != -1;
            lock.unlock();
            return z;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public String toString() {
        return Helpers.collectionToString(this);
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
                int n = Math.min(this.size, maxElements);
                for (int i = 0; i < n; i++) {
                    c.add(this.queue[0]);
                    dequeue();
                }
                return n;
            } finally {
                lock.unlock();
            }
        }
    }

    public void clear() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] array = this.queue;
            int n = this.size;
            this.size = 0;
            for (int i = 0; i < n; i++) {
                array[i] = null;
            }
        } finally {
            lock.unlock();
        }
    }

    public Object[] toArray() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] copyOf = Arrays.copyOf(this.queue, this.size);
            return copyOf;
        } finally {
            lock.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = this.size;
            if (a.length < n) {
                T[] copyOf = Arrays.copyOf(this.queue, this.size, a.getClass());
                return copyOf;
            }
            System.arraycopy(this.queue, 0, a, 0, n);
            if (a.length > n) {
                a[n] = null;
            }
            lock.unlock();
            return a;
        } finally {
            lock.unlock();
        }
    }

    public Iterator<E> iterator() {
        return new Itr(toArray());
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        this.lock.lock();
        try {
            this.q = new PriorityQueue(Math.max(this.size, 1), this.comparator);
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
}
