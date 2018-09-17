package java.util.concurrent;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ArrayBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, Serializable {
    private static final long serialVersionUID = -817911632652898426L;
    int count;
    final Object[] items;
    transient Itrs itrs;
    final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;
    int putIndex;
    int takeIndex;

    private class Itr implements Iterator<E> {
        private static final int DETACHED = -3;
        private static final int NONE = -1;
        private static final int REMOVED = -2;
        private int cursor;
        private E lastItem;
        private int lastRet = -1;
        private int nextIndex;
        private E nextItem;
        private int prevCycles;
        private int prevTakeIndex;

        Itr() {
            ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (ArrayBlockingQueue.this.count == 0) {
                    this.cursor = -1;
                    this.nextIndex = -1;
                    this.prevTakeIndex = -3;
                } else {
                    int takeIndex = ArrayBlockingQueue.this.takeIndex;
                    this.prevTakeIndex = takeIndex;
                    this.nextIndex = takeIndex;
                    this.nextItem = ArrayBlockingQueue.this.itemAt(takeIndex);
                    this.cursor = incCursor(takeIndex);
                    if (ArrayBlockingQueue.this.itrs == null) {
                        ArrayBlockingQueue.this.itrs = new Itrs(this);
                    } else {
                        ArrayBlockingQueue.this.itrs.register(this);
                        ArrayBlockingQueue.this.itrs.doSomeSweeping(false);
                    }
                    this.prevCycles = ArrayBlockingQueue.this.itrs.cycles;
                }
                lock.unlock();
            } catch (Throwable th) {
                lock.unlock();
            }
        }

        boolean isDetached() {
            return this.prevTakeIndex < 0;
        }

        private int incCursor(int index) {
            index++;
            if (index == ArrayBlockingQueue.this.items.length) {
                index = 0;
            }
            if (index == ArrayBlockingQueue.this.putIndex) {
                return -1;
            }
            return index;
        }

        private boolean invalidated(int index, int prevTakeIndex, long dequeues, int length) {
            boolean z = false;
            if (index < 0) {
                return false;
            }
            int distance = index - prevTakeIndex;
            if (distance < 0) {
                distance += length;
            }
            if (dequeues > ((long) distance)) {
                z = true;
            }
            return z;
        }

        private void incorporateDequeues() {
            int cycles = ArrayBlockingQueue.this.itrs.cycles;
            int takeIndex = ArrayBlockingQueue.this.takeIndex;
            int prevCycles = this.prevCycles;
            int prevTakeIndex = this.prevTakeIndex;
            if (cycles != prevCycles || takeIndex != prevTakeIndex) {
                int len = ArrayBlockingQueue.this.items.length;
                long dequeues = (long) (((cycles - prevCycles) * len) + (takeIndex - prevTakeIndex));
                if (invalidated(this.lastRet, prevTakeIndex, dequeues, len)) {
                    this.lastRet = -2;
                }
                if (invalidated(this.nextIndex, prevTakeIndex, dequeues, len)) {
                    this.nextIndex = -2;
                }
                if (invalidated(this.cursor, prevTakeIndex, dequeues, len)) {
                    this.cursor = takeIndex;
                }
                if (this.cursor >= 0 || this.nextIndex >= 0 || this.lastRet >= 0) {
                    this.prevCycles = cycles;
                    this.prevTakeIndex = takeIndex;
                    return;
                }
                detach();
            }
        }

        private void detach() {
            if (this.prevTakeIndex >= 0) {
                this.prevTakeIndex = -3;
                ArrayBlockingQueue.this.itrs.doSomeSweeping(true);
            }
        }

        public boolean hasNext() {
            if (this.nextItem != null) {
                return true;
            }
            noNext();
            return false;
        }

        private void noNext() {
            ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (!isDetached()) {
                    incorporateDequeues();
                    if (this.lastRet >= 0) {
                        this.lastItem = ArrayBlockingQueue.this.itemAt(this.lastRet);
                        detach();
                    }
                }
                lock.unlock();
            } catch (Throwable th) {
                lock.unlock();
            }
        }

        public E next() {
            E x = this.nextItem;
            if (x == null) {
                throw new NoSuchElementException();
            }
            ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (!isDetached()) {
                    incorporateDequeues();
                }
                this.lastRet = this.nextIndex;
                int cursor = this.cursor;
                if (cursor >= 0) {
                    ArrayBlockingQueue arrayBlockingQueue = ArrayBlockingQueue.this;
                    this.nextIndex = cursor;
                    this.nextItem = arrayBlockingQueue.itemAt(cursor);
                    this.cursor = incCursor(cursor);
                } else {
                    this.nextIndex = -1;
                    this.nextItem = null;
                }
                lock.unlock();
                return x;
            } catch (Throwable th) {
                lock.unlock();
            }
        }

        public void remove() {
            ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (!isDetached()) {
                    incorporateDequeues();
                }
                int lastRet = this.lastRet;
                this.lastRet = -1;
                if (lastRet >= 0) {
                    if (isDetached()) {
                        E lastItem = this.lastItem;
                        this.lastItem = null;
                        if (ArrayBlockingQueue.this.itemAt(lastRet) == lastItem) {
                            ArrayBlockingQueue.this.removeAt(lastRet);
                        }
                    } else {
                        ArrayBlockingQueue.this.removeAt(lastRet);
                    }
                } else if (lastRet == -1) {
                    throw new IllegalStateException();
                }
                if (this.cursor < 0 && this.nextIndex < 0) {
                    detach();
                }
                lock.unlock();
            } catch (Throwable th) {
                lock.unlock();
            }
        }

        void shutdown() {
            this.cursor = -1;
            if (this.nextIndex >= 0) {
                this.nextIndex = -2;
            }
            if (this.lastRet >= 0) {
                this.lastRet = -2;
                this.lastItem = null;
            }
            this.prevTakeIndex = -3;
        }

        private int distance(int index, int prevTakeIndex, int length) {
            int distance = index - prevTakeIndex;
            if (distance < 0) {
                return distance + length;
            }
            return distance;
        }

        boolean removedAt(int removedIndex) {
            if (isDetached()) {
                return true;
            }
            int i;
            int x;
            int takeIndex = ArrayBlockingQueue.this.takeIndex;
            int prevTakeIndex = this.prevTakeIndex;
            int len = ArrayBlockingQueue.this.items.length;
            int i2 = ArrayBlockingQueue.this.itrs.cycles - this.prevCycles;
            if (removedIndex < takeIndex) {
                i = 1;
            } else {
                i = 0;
            }
            int removedDistance = ((i + i2) * len) + (removedIndex - prevTakeIndex);
            int cursor = this.cursor;
            if (cursor >= 0) {
                x = distance(cursor, prevTakeIndex, len);
                if (x == removedDistance) {
                    if (cursor == ArrayBlockingQueue.this.putIndex) {
                        cursor = -1;
                        this.cursor = -1;
                    }
                } else if (x > removedDistance) {
                    cursor = ArrayBlockingQueue.this.dec(cursor);
                    this.cursor = cursor;
                }
            }
            int lastRet = this.lastRet;
            if (lastRet >= 0) {
                x = distance(lastRet, prevTakeIndex, len);
                if (x == removedDistance) {
                    lastRet = -2;
                    this.lastRet = -2;
                } else if (x > removedDistance) {
                    lastRet = ArrayBlockingQueue.this.dec(lastRet);
                    this.lastRet = lastRet;
                }
            }
            int nextIndex = this.nextIndex;
            if (nextIndex >= 0) {
                x = distance(nextIndex, prevTakeIndex, len);
                if (x == removedDistance) {
                    nextIndex = -2;
                    this.nextIndex = -2;
                } else if (x > removedDistance) {
                    nextIndex = ArrayBlockingQueue.this.dec(nextIndex);
                    this.nextIndex = nextIndex;
                }
            }
            if (cursor >= 0 || nextIndex >= 0 || lastRet >= 0) {
                return false;
            }
            this.prevTakeIndex = -3;
            return true;
        }

        boolean takeIndexWrapped() {
            if (isDetached()) {
                return true;
            }
            if (ArrayBlockingQueue.this.itrs.cycles - this.prevCycles <= 1) {
                return false;
            }
            shutdown();
            return true;
        }
    }

    class Itrs {
        private static final int LONG_SWEEP_PROBES = 16;
        private static final int SHORT_SWEEP_PROBES = 4;
        int cycles;
        private java.util.concurrent.ArrayBlockingQueue$Itrs.Node head;
        private java.util.concurrent.ArrayBlockingQueue$Itrs.Node sweeper;

        private class Node extends WeakReference<Itr> {
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node next;

            Node(Itr iterator, java.util.concurrent.ArrayBlockingQueue$Itrs.Node next) {
                super(iterator);
                this.next = next;
            }
        }

        Itrs(Itr initial) {
            register(initial);
        }

        void doSomeSweeping(boolean tryHarder) {
            Node o;
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node p;
            boolean passedGo;
            int probes = tryHarder ? 16 : 4;
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node sweeper = this.sweeper;
            if (sweeper == null) {
                o = null;
                p = this.head;
                passedGo = true;
            } else {
                o = sweeper;
                p = sweeper.next;
                passedGo = false;
            }
            while (probes > 0) {
                if (p == null) {
                    if (passedGo) {
                        break;
                    }
                    java.util.concurrent.ArrayBlockingQueue$Itrs.Node o2 = null;
                    p = this.head;
                    passedGo = true;
                }
                Itr it = (Itr) p.get();
                java.util.concurrent.ArrayBlockingQueue$Itrs.Node next = p.next;
                if (it == null || it.isDetached()) {
                    probes = 16;
                    p.clear();
                    p.next = null;
                    if (o == null) {
                        this.head = next;
                        if (next == null) {
                            ArrayBlockingQueue.this.itrs = null;
                            return;
                        }
                    } else {
                        o.next = next;
                    }
                } else {
                    o = p;
                }
                p = next;
                probes--;
            }
            if (p == null) {
                o = null;
            }
            this.sweeper = o;
        }

        void register(Itr itr) {
            this.head = new Node(itr, this.head);
        }

        void takeIndexWrapped() {
            this.cycles++;
            Node o = null;
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node p = this.head;
            while (p != null) {
                Itr it = (Itr) p.get();
                java.util.concurrent.ArrayBlockingQueue$Itrs.Node next = p.next;
                if (it == null || it.takeIndexWrapped()) {
                    p.clear();
                    p.next = null;
                    if (o == null) {
                        this.head = next;
                    } else {
                        o.next = next;
                    }
                } else {
                    o = p;
                }
                p = next;
            }
            if (this.head == null) {
                ArrayBlockingQueue.this.itrs = null;
            }
        }

        void removedAt(int removedIndex) {
            Node o = null;
            java.util.concurrent.ArrayBlockingQueue$Itrs.Node p = this.head;
            while (p != null) {
                Itr it = (Itr) p.get();
                java.util.concurrent.ArrayBlockingQueue$Itrs.Node next = p.next;
                if (it == null || it.removedAt(removedIndex)) {
                    p.clear();
                    p.next = null;
                    if (o == null) {
                        this.head = next;
                    } else {
                        o.next = next;
                    }
                } else {
                    o = p;
                }
                p = next;
            }
            if (this.head == null) {
                ArrayBlockingQueue.this.itrs = null;
            }
        }

        void queueIsEmpty() {
            for (java.util.concurrent.ArrayBlockingQueue$Itrs.Node p = this.head; p != null; p = p.next) {
                Itr it = (Itr) p.get();
                if (it != null) {
                    p.clear();
                    it.shutdown();
                }
            }
            this.head = null;
            ArrayBlockingQueue.this.itrs = null;
        }

        void elementDequeued() {
            if (ArrayBlockingQueue.this.count == 0) {
                queueIsEmpty();
            } else if (ArrayBlockingQueue.this.takeIndex == 0) {
                takeIndexWrapped();
            }
        }
    }

    final int dec(int i) {
        if (i == 0) {
            i = this.items.length;
        }
        return i - 1;
    }

    final E itemAt(int i) {
        return this.items[i];
    }

    private void enqueue(E x) {
        Object[] items = this.items;
        items[this.putIndex] = x;
        int i = this.putIndex + 1;
        this.putIndex = i;
        if (i == items.length) {
            this.putIndex = 0;
        }
        this.count++;
        this.notEmpty.signal();
    }

    private E dequeue() {
        Object[] items = this.items;
        E x = items[this.takeIndex];
        items[this.takeIndex] = null;
        int i = this.takeIndex + 1;
        this.takeIndex = i;
        if (i == items.length) {
            this.takeIndex = 0;
        }
        this.count--;
        if (this.itrs != null) {
            this.itrs.elementDequeued();
        }
        this.notFull.signal();
        return x;
    }

    void removeAt(int removeIndex) {
        Object[] items = this.items;
        if (removeIndex == this.takeIndex) {
            items[this.takeIndex] = null;
            int i = this.takeIndex + 1;
            this.takeIndex = i;
            if (i == items.length) {
                this.takeIndex = 0;
            }
            this.count--;
            if (this.itrs != null) {
                this.itrs.elementDequeued();
            }
        } else {
            int pred;
            int i2 = removeIndex;
            int putIndex = this.putIndex;
            while (true) {
                pred = i2;
                i2++;
                if (i2 == items.length) {
                    i2 = 0;
                }
                if (i2 == putIndex) {
                    break;
                }
                items[pred] = items[i2];
            }
            items[pred] = null;
            this.putIndex = pred;
            this.count--;
            if (this.itrs != null) {
                this.itrs.removedAt(removeIndex);
            }
        }
        this.notFull.signal();
    }

    public ArrayBlockingQueue(int capacity) {
        this(capacity, false);
    }

    public ArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.items = new Object[capacity];
        this.lock = new ReentrantLock(fair);
        this.notEmpty = this.lock.newCondition();
        this.notFull = this.lock.newCondition();
    }

    /* JADX WARNING: Missing block: B:19:?, code:
            r8.count = r4;
     */
    /* JADX WARNING: Missing block: B:20:0x0032, code:
            if (r4 != r9) goto L_0x003b;
     */
    /* JADX WARNING: Missing block: B:21:0x0034, code:
            r6 = 0;
     */
    /* JADX WARNING: Missing block: B:22:0x0035, code:
            r8.putIndex = r6;
     */
    /* JADX WARNING: Missing block: B:23:0x0037, code:
            r5.unlock();
     */
    /* JADX WARNING: Missing block: B:24:0x003a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:25:0x003b, code:
            r6 = r4;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public ArrayBlockingQueue(int capacity, boolean fair, Collection<? extends E> c) {
        Throwable th;
        this(capacity, fair);
        ReentrantLock lock = this.lock;
        lock.lock();
        int i = 0;
        try {
            Iterator e$iterator = c.iterator();
            while (true) {
                int i2;
                try {
                    i2 = i;
                    if (e$iterator.hasNext()) {
                        E e = e$iterator.next();
                        i = i2 + 1;
                        this.items[i2] = Objects.requireNonNull(e);
                    } else {
                        try {
                            break;
                        } catch (Throwable th2) {
                            th = th2;
                            i = i2;
                            lock.unlock();
                            throw th;
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e2) {
                    i = i2;
                    try {
                        throw new IllegalArgumentException();
                    } catch (Throwable th3) {
                        th = th3;
                        lock.unlock();
                        throw th;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e3) {
        }
    }

    public boolean add(E e) {
        return super.add(e);
    }

    public boolean offer(E e) {
        Objects.requireNonNull(e);
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.count == this.items.length) {
                return false;
            }
            enqueue(e);
            lock.unlock();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void put(E e) throws InterruptedException {
        Objects.requireNonNull(e);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (this.count == this.items.length) {
            try {
                this.notFull.await();
            } finally {
                lock.unlock();
            }
        }
        enqueue(e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(e);
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (this.count == this.items.length) {
            try {
                if (nanos <= 0) {
                    return false;
                }
                nanos = this.notFull.awaitNanos(nanos);
            } finally {
                lock.unlock();
            }
        }
        enqueue(e);
        lock.unlock();
        return true;
    }

    public E poll() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E dequeue = this.count == 0 ? null : dequeue();
            lock.unlock();
            return dequeue;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (this.count == 0) {
            try {
                this.notEmpty.await();
            } finally {
                lock.unlock();
            }
        }
        E dequeue = dequeue();
        return dequeue;
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        while (this.count == 0) {
            try {
                if (nanos <= 0) {
                    return null;
                }
                nanos = this.notEmpty.awaitNanos(nanos);
            } finally {
                lock.unlock();
            }
        }
        E dequeue = dequeue();
        lock.unlock();
        return dequeue;
    }

    public E peek() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            E itemAt = itemAt(this.takeIndex);
            return itemAt;
        } finally {
            lock.unlock();
        }
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

    public int remainingCapacity() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int length = this.items.length - this.count;
            return length;
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (this.count > 0) {
                Object[] items = this.items;
                int putIndex = this.putIndex;
                int i = this.takeIndex;
                while (!o.lambda$-java_util_function_Predicate_4628(items[i])) {
                    i++;
                    if (i == items.length) {
                        i = 0;
                        continue;
                    }
                    if (i == putIndex) {
                    }
                }
                removeAt(i);
                lock.unlock();
                return true;
            }
            lock.unlock();
            return false;
        } catch (Throwable th) {
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
            if (this.count > 0) {
                Object[] items = this.items;
                int putIndex = this.putIndex;
                int i = this.takeIndex;
                while (!o.lambda$-java_util_function_Predicate_4628(items[i])) {
                    i++;
                    if (i == items.length) {
                        i = 0;
                        continue;
                    }
                    if (i == putIndex) {
                    }
                }
                lock.unlock();
                return true;
            }
            lock.unlock();
            return false;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public Object[] toArray() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object items = this.items;
            int end = this.takeIndex + this.count;
            Object a = Arrays.copyOfRange((Object[]) items, this.takeIndex, end);
            if (end != this.putIndex) {
                System.arraycopy(items, 0, a, items.length - this.takeIndex, this.putIndex);
            }
            lock.unlock();
            return a;
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public <T> T[] toArray(T[] a) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object a2;
            Object items = this.items;
            int count = this.count;
            int firstLeg = Math.min(items.length - this.takeIndex, count);
            if (a2.length < count) {
                a2 = Arrays.copyOfRange(items, this.takeIndex, this.takeIndex + count, a2.getClass());
            } else {
                System.arraycopy(items, this.takeIndex, (Object) a2, 0, firstLeg);
                if (a2.length > count) {
                    a2[count] = null;
                }
            }
            if (firstLeg < count) {
                System.arraycopy(items, 0, a2, firstLeg, this.putIndex);
            }
            lock.unlock();
            return a2;
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
            int k = this.count;
            if (k > 0) {
                Object[] items = this.items;
                int putIndex = this.putIndex;
                int i = this.takeIndex;
                do {
                    items[i] = null;
                    i++;
                    if (i == items.length) {
                        i = 0;
                        continue;
                    }
                } while (i != putIndex);
                this.takeIndex = putIndex;
                this.count = 0;
                if (this.itrs != null) {
                    this.itrs.queueIsEmpty();
                }
                while (k > 0 && lock.hasWaiters(this.notFull)) {
                    this.notFull.signal();
                    k--;
                }
            }
            lock.unlock();
        } catch (Throwable th) {
            lock.unlock();
        }
    }

    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        Objects.requireNonNull(c);
        if (c == this) {
            throw new IllegalArgumentException();
        } else if (maxElements <= 0) {
            return 0;
        } else {
            Object[] items = this.items;
            ReentrantLock lock = this.lock;
            lock.lock();
            int take;
            int i;
            try {
                int n = Math.min(maxElements, this.count);
                take = this.takeIndex;
                i = 0;
                while (i < n) {
                    c.add(items[take]);
                    items[take] = null;
                    take++;
                    if (take == items.length) {
                        take = 0;
                    }
                    i++;
                }
                if (i > 0) {
                    this.count -= i;
                    this.takeIndex = take;
                    if (this.itrs != null) {
                        if (this.count == 0) {
                            this.itrs.queueIsEmpty();
                        } else if (i > take) {
                            this.itrs.takeIndexWrapped();
                        }
                    }
                    while (i > 0 && lock.hasWaiters(this.notFull)) {
                        this.notFull.signal();
                        i--;
                    }
                }
                lock.unlock();
                return n;
            } catch (Throwable th) {
                lock.unlock();
            }
        }
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator((Collection) this, 4368);
    }
}
