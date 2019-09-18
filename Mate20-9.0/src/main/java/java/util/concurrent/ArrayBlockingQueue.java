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
    transient ArrayBlockingQueue<E>.Itrs itrs;
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
            } finally {
                lock.unlock();
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isDetached() {
            return this.prevTakeIndex < 0;
        }

        private int incCursor(int index) {
            int index2 = index + 1;
            if (index2 == ArrayBlockingQueue.this.items.length) {
                index2 = 0;
            }
            if (index2 == ArrayBlockingQueue.this.putIndex) {
                return -1;
            }
            return index2;
        }

        private boolean invalidated(int index, int prevTakeIndex2, long dequeues, int length) {
            boolean z = false;
            if (index < 0) {
                return false;
            }
            int distance = index - prevTakeIndex2;
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
            int prevCycles2 = this.prevCycles;
            int prevTakeIndex2 = this.prevTakeIndex;
            if (cycles != prevCycles2 || takeIndex != prevTakeIndex2) {
                int len = ArrayBlockingQueue.this.items.length;
                long dequeues = (long) (((cycles - prevCycles2) * len) + (takeIndex - prevTakeIndex2));
                if (invalidated(this.lastRet, prevTakeIndex2, dequeues, len)) {
                    this.lastRet = -2;
                }
                if (invalidated(this.nextIndex, prevTakeIndex2, dequeues, len)) {
                    this.nextIndex = -2;
                }
                if (invalidated(this.cursor, prevTakeIndex2, dequeues, len)) {
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
            } finally {
                lock.unlock();
            }
        }

        public E next() {
            E x = this.nextItem;
            if (x != null) {
                ReentrantLock lock = ArrayBlockingQueue.this.lock;
                lock.lock();
                try {
                    if (!isDetached()) {
                        incorporateDequeues();
                    }
                    this.lastRet = this.nextIndex;
                    int cursor2 = this.cursor;
                    if (cursor2 >= 0) {
                        ArrayBlockingQueue arrayBlockingQueue = ArrayBlockingQueue.this;
                        this.nextIndex = cursor2;
                        this.nextItem = arrayBlockingQueue.itemAt(cursor2);
                        this.cursor = incCursor(cursor2);
                    } else {
                        this.nextIndex = -1;
                        this.nextItem = null;
                    }
                    return x;
                } finally {
                    lock.unlock();
                }
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            ReentrantLock lock = ArrayBlockingQueue.this.lock;
            lock.lock();
            try {
                if (!isDetached()) {
                    incorporateDequeues();
                }
                int lastRet2 = this.lastRet;
                this.lastRet = -1;
                if (lastRet2 >= 0) {
                    if (!isDetached()) {
                        ArrayBlockingQueue.this.removeAt(lastRet2);
                    } else {
                        E lastItem2 = this.lastItem;
                        this.lastItem = null;
                        if (ArrayBlockingQueue.this.itemAt(lastRet2) == lastItem2) {
                            ArrayBlockingQueue.this.removeAt(lastRet2);
                        }
                    }
                } else if (lastRet2 == -1) {
                    throw new IllegalStateException();
                }
                if (this.cursor < 0 && this.nextIndex < 0) {
                    detach();
                }
            } finally {
                lock.unlock();
            }
        }

        /* access modifiers changed from: package-private */
        public void shutdown() {
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

        private int distance(int index, int prevTakeIndex2, int length) {
            int distance = index - prevTakeIndex2;
            if (distance < 0) {
                return distance + length;
            }
            return distance;
        }

        /* access modifiers changed from: package-private */
        public boolean removedAt(int removedIndex) {
            if (isDetached()) {
                return true;
            }
            int takeIndex = ArrayBlockingQueue.this.takeIndex;
            int prevTakeIndex2 = this.prevTakeIndex;
            int len = ArrayBlockingQueue.this.items.length;
            int removedDistance = (((ArrayBlockingQueue.this.itrs.cycles - this.prevCycles) + (removedIndex < takeIndex ? 1 : 0)) * len) + (removedIndex - prevTakeIndex2);
            int cursor2 = this.cursor;
            if (cursor2 >= 0) {
                int x = distance(cursor2, prevTakeIndex2, len);
                if (x == removedDistance) {
                    if (cursor2 == ArrayBlockingQueue.this.putIndex) {
                        cursor2 = -1;
                        this.cursor = -1;
                    }
                } else if (x > removedDistance) {
                    int dec = ArrayBlockingQueue.this.dec(cursor2);
                    cursor2 = dec;
                    this.cursor = dec;
                }
            }
            int lastRet2 = this.lastRet;
            if (lastRet2 >= 0) {
                int x2 = distance(lastRet2, prevTakeIndex2, len);
                if (x2 == removedDistance) {
                    lastRet2 = -2;
                    this.lastRet = -2;
                } else if (x2 > removedDistance) {
                    int dec2 = ArrayBlockingQueue.this.dec(lastRet2);
                    lastRet2 = dec2;
                    this.lastRet = dec2;
                }
            }
            int nextIndex2 = this.nextIndex;
            if (nextIndex2 >= 0) {
                int x3 = distance(nextIndex2, prevTakeIndex2, len);
                if (x3 == removedDistance) {
                    nextIndex2 = -2;
                    this.nextIndex = -2;
                } else if (x3 > removedDistance) {
                    int dec3 = ArrayBlockingQueue.this.dec(nextIndex2);
                    nextIndex2 = dec3;
                    this.nextIndex = dec3;
                }
            }
            if (cursor2 >= 0 || nextIndex2 >= 0 || lastRet2 >= 0) {
                return false;
            }
            this.prevTakeIndex = -3;
            return true;
        }

        /* access modifiers changed from: package-private */
        public boolean takeIndexWrapped() {
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
        private ArrayBlockingQueue<E>.Itrs.Node head;
        private ArrayBlockingQueue<E>.Itrs.Node sweeper;

        private class Node extends WeakReference<ArrayBlockingQueue<E>.Itr> {
            ArrayBlockingQueue<E>.Itrs.Node next;

            Node(ArrayBlockingQueue<E>.Itr iterator, ArrayBlockingQueue<E>.Itrs.Node next2) {
                super(iterator);
                this.next = next2;
            }
        }

        Itrs(ArrayBlockingQueue<E>.Itr initial) {
            register(initial);
        }

        /* access modifiers changed from: package-private */
        public void doSomeSweeping(boolean tryHarder) {
            boolean passedGo;
            ArrayBlockingQueue<E>.Itrs.Node p;
            ArrayBlockingQueue<E>.Itrs.Node o;
            ArrayBlockingQueue<E>.Itrs.Node node;
            int probes = tryHarder ? 16 : 4;
            ArrayBlockingQueue<E>.Itrs.Node sweeper2 = this.sweeper;
            if (sweeper2 == null) {
                o = null;
                p = this.head;
                passedGo = true;
            } else {
                o = sweeper2;
                p = o.next;
                passedGo = false;
            }
            while (true) {
                node = null;
                if (probes <= 0) {
                    break;
                }
                if (p == null) {
                    if (passedGo) {
                        break;
                    }
                    o = null;
                    p = this.head;
                    passedGo = true;
                }
                ArrayBlockingQueue<E>.Itr it = (Itr) p.get();
                ArrayBlockingQueue<E>.Itrs.Node next = p.next;
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
            if (p != null) {
                node = o;
            }
            this.sweeper = node;
        }

        /* access modifiers changed from: package-private */
        public void register(ArrayBlockingQueue<E>.Itr itr) {
            this.head = new Node(itr, this.head);
        }

        /* access modifiers changed from: package-private */
        public void takeIndexWrapped() {
            this.cycles++;
            ArrayBlockingQueue<E>.Itrs.Node o = null;
            ArrayBlockingQueue<E>.Itrs.Node p = this.head;
            while (p != null) {
                ArrayBlockingQueue<E>.Itr it = (Itr) p.get();
                ArrayBlockingQueue<E>.Itrs.Node next = p.next;
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

        /* access modifiers changed from: package-private */
        public void removedAt(int removedIndex) {
            ArrayBlockingQueue<E>.Itrs.Node o = null;
            ArrayBlockingQueue<E>.Itrs.Node p = this.head;
            while (p != null) {
                ArrayBlockingQueue<E>.Itr it = (Itr) p.get();
                ArrayBlockingQueue<E>.Itrs.Node next = p.next;
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

        /* access modifiers changed from: package-private */
        public void queueIsEmpty() {
            for (ArrayBlockingQueue<E>.Itrs.Node p = this.head; p != null; p = p.next) {
                ArrayBlockingQueue<E>.Itr it = (Itr) p.get();
                if (it != null) {
                    p.clear();
                    it.shutdown();
                }
            }
            this.head = null;
            ArrayBlockingQueue.this.itrs = null;
        }

        /* access modifiers changed from: package-private */
        public void elementDequeued() {
            if (ArrayBlockingQueue.this.count == 0) {
                queueIsEmpty();
            } else if (ArrayBlockingQueue.this.takeIndex == 0) {
                takeIndexWrapped();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final int dec(int i) {
        return (i == 0 ? this.items.length : i) - 1;
    }

    /* access modifiers changed from: package-private */
    public final E itemAt(int i) {
        return this.items[i];
    }

    private void enqueue(E x) {
        Object[] items2 = this.items;
        items2[this.putIndex] = x;
        int i = this.putIndex + 1;
        this.putIndex = i;
        if (i == items2.length) {
            this.putIndex = 0;
        }
        this.count++;
        this.notEmpty.signal();
    }

    private E dequeue() {
        E[] items2 = this.items;
        E x = items2[this.takeIndex];
        items2[this.takeIndex] = null;
        int i = this.takeIndex + 1;
        this.takeIndex = i;
        if (i == items2.length) {
            this.takeIndex = 0;
        }
        this.count--;
        if (this.itrs != null) {
            this.itrs.elementDequeued();
        }
        this.notFull.signal();
        return x;
    }

    /* access modifiers changed from: package-private */
    public void removeAt(int removeIndex) {
        int pred;
        Object[] items2 = this.items;
        if (removeIndex == this.takeIndex) {
            items2[this.takeIndex] = null;
            int i = this.takeIndex + 1;
            this.takeIndex = i;
            if (i == items2.length) {
                this.takeIndex = 0;
            }
            this.count--;
            if (this.itrs != null) {
                this.itrs.elementDequeued();
            }
        } else {
            int i2 = removeIndex;
            int putIndex2 = this.putIndex;
            while (true) {
                pred = i2;
                i2++;
                if (i2 == items2.length) {
                    i2 = 0;
                }
                if (i2 == putIndex2) {
                    break;
                }
                items2[pred] = items2[i2];
            }
            items2[pred] = null;
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
        if (capacity > 0) {
            this.items = new Object[capacity];
            this.lock = new ReentrantLock(fair);
            this.notEmpty = this.lock.newCondition();
            this.notFull = this.lock.newCondition();
            return;
        }
        throw new IllegalArgumentException();
    }

    public ArrayBlockingQueue(int capacity, boolean fair, Collection<? extends E> c) {
        this(capacity, fair);
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        int i = 0;
        int i2 = 0;
        try {
            for (E e : c) {
                int i3 = i2 + 1;
                try {
                    this.items[i2] = Objects.requireNonNull(e);
                    i2 = i3;
                } catch (ArrayIndexOutOfBoundsException e2) {
                    int i4 = i3;
                    throw new IllegalArgumentException();
                }
            }
            try {
                this.count = i2;
                if (i2 != capacity) {
                    i = i2;
                }
                this.putIndex = i;
            } finally {
                lock2.unlock();
            }
        } catch (ArrayIndexOutOfBoundsException e3) {
            throw new IllegalArgumentException();
        }
    }

    public boolean add(E e) {
        return super.add(e);
    }

    public boolean offer(E e) {
        Objects.requireNonNull(e);
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            if (this.count == this.items.length) {
                return false;
            }
            enqueue(e);
            lock2.unlock();
            return true;
        } finally {
            lock2.unlock();
        }
    }

    public void put(E e) throws InterruptedException {
        Objects.requireNonNull(e);
        ReentrantLock lock2 = this.lock;
        lock2.lockInterruptibly();
        while (this.count == this.items.length) {
            try {
                this.notFull.await();
            } finally {
                lock2.unlock();
            }
        }
        enqueue(e);
    }

    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(e);
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock2 = this.lock;
        lock2.lockInterruptibly();
        while (this.count == this.items.length) {
            try {
                if (nanos <= 0) {
                    return false;
                }
                nanos = this.notFull.awaitNanos(nanos);
            } finally {
                lock2.unlock();
            }
        }
        enqueue(e);
        lock2.unlock();
        return true;
    }

    public E poll() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.count == 0 ? null : dequeue();
        } finally {
            lock2.unlock();
        }
    }

    public E take() throws InterruptedException {
        ReentrantLock lock2 = this.lock;
        lock2.lockInterruptibly();
        while (this.count == 0) {
            try {
                this.notEmpty.await();
            } finally {
                lock2.unlock();
            }
        }
        return dequeue();
    }

    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock2 = this.lock;
        lock2.lockInterruptibly();
        while (this.count == 0) {
            try {
                if (nanos <= 0) {
                    return null;
                }
                nanos = this.notEmpty.awaitNanos(nanos);
            } finally {
                lock2.unlock();
            }
        }
        E dequeue = dequeue();
        lock2.unlock();
        return dequeue;
    }

    public E peek() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return itemAt(this.takeIndex);
        } finally {
            lock2.unlock();
        }
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

    public int remainingCapacity() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            return this.items.length - this.count;
        } finally {
            lock2.unlock();
        }
    }

    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            if (this.count > 0) {
                Object[] items2 = this.items;
                int putIndex2 = this.putIndex;
                int i = this.takeIndex;
                while (!o.equals(items2[i])) {
                    i++;
                    if (i == items2.length) {
                        i = 0;
                        continue;
                    }
                    if (i == putIndex2) {
                    }
                }
                removeAt(i);
                lock2.unlock();
                return true;
            }
            return false;
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
            if (this.count > 0) {
                Object[] items2 = this.items;
                int putIndex2 = this.putIndex;
                int i = this.takeIndex;
                while (!o.equals(items2[i])) {
                    i++;
                    if (i == items2.length) {
                        i = 0;
                        continue;
                    }
                    if (i == putIndex2) {
                    }
                }
                lock2.unlock();
                return true;
            }
            return false;
        } finally {
            lock2.unlock();
        }
    }

    public Object[] toArray() {
        ReentrantLock lock2 = this.lock;
        lock2.lock();
        try {
            Object[] items2 = this.items;
            int end = this.takeIndex + this.count;
            Object[] a = Arrays.copyOfRange((T[]) items2, this.takeIndex, end);
            if (end != this.putIndex) {
                System.arraycopy((Object) items2, 0, (Object) a, items2.length - this.takeIndex, this.putIndex);
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
            Object[] items2 = this.items;
            int count2 = this.count;
            int firstLeg = Math.min(items2.length - this.takeIndex, count2);
            if (a.length < count2) {
                a = Arrays.copyOfRange(items2, this.takeIndex, this.takeIndex + count2, a.getClass());
            } else {
                System.arraycopy((Object) items2, this.takeIndex, (Object) a, 0, firstLeg);
                if (a.length > count2) {
                    a[count2] = null;
                }
            }
            if (firstLeg < count2) {
                System.arraycopy((Object) items2, 0, (Object) a, firstLeg, this.putIndex);
            }
            return a;
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
            int k = this.count;
            if (k > 0) {
                Object[] items2 = this.items;
                int putIndex2 = this.putIndex;
                int i = this.takeIndex;
                do {
                    items2[i] = null;
                    i++;
                    if (i == items2.length) {
                        i = 0;
                        continue;
                    }
                } while (i != putIndex2);
                this.takeIndex = putIndex2;
                this.count = 0;
                if (this.itrs != null) {
                    this.itrs.queueIsEmpty();
                }
                while (k > 0 && lock2.hasWaiters(this.notFull)) {
                    this.notFull.signal();
                    k--;
                }
            }
        } finally {
            lock2.unlock();
        }
    }

    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        int take;
        Objects.requireNonNull(c);
        if (c != this) {
            int i = 0;
            if (maxElements <= 0) {
                return 0;
            }
            E[] items2 = this.items;
            ReentrantLock lock2 = this.lock;
            lock2.lock();
            try {
                int n = Math.min(maxElements, this.count);
                take = this.takeIndex;
                while (i < n) {
                    c.add(items2[take]);
                    items2[take] = null;
                    take++;
                    if (take == items2.length) {
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
                    while (i > 0 && lock2.hasWaiters(this.notFull)) {
                        this.notFull.signal();
                        i--;
                    }
                }
                lock2.unlock();
                return n;
            } catch (Throwable th) {
                lock2.unlock();
                throw th;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, 4368);
    }
}
