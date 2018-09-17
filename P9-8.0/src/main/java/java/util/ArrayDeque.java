package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Consumer;

public class ArrayDeque<E> extends AbstractCollection<E> implements Deque<E>, Cloneable, Serializable {
    static final /* synthetic */ boolean -assertionsDisabled = (ArrayDeque.class.desiredAssertionStatus() ^ 1);
    private static final int MIN_INITIAL_CAPACITY = 8;
    private static final long serialVersionUID = 2340985798034038923L;
    transient Object[] elements;
    transient int head;
    transient int tail;

    private class DeqIterator implements Iterator<E> {
        private int cursor;
        private int fence;
        private int lastRet;

        /* synthetic */ DeqIterator(ArrayDeque this$0, DeqIterator -this1) {
            this();
        }

        private DeqIterator() {
            this.cursor = ArrayDeque.this.head;
            this.fence = ArrayDeque.this.tail;
            this.lastRet = -1;
        }

        public boolean hasNext() {
            return this.cursor != this.fence ? true : ArrayDeque.-assertionsDisabled;
        }

        public E next() {
            if (this.cursor == this.fence) {
                throw new NoSuchElementException();
            }
            E result = ArrayDeque.this.elements[this.cursor];
            if (ArrayDeque.this.tail != this.fence || result == null) {
                throw new ConcurrentModificationException();
            }
            this.lastRet = this.cursor;
            this.cursor = (this.cursor + 1) & (ArrayDeque.this.elements.length - 1);
            return result;
        }

        public void remove() {
            if (this.lastRet < 0) {
                throw new IllegalStateException();
            }
            if (ArrayDeque.this.delete(this.lastRet)) {
                this.cursor = (this.cursor - 1) & (ArrayDeque.this.elements.length - 1);
                this.fence = ArrayDeque.this.tail;
            }
            this.lastRet = -1;
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Object[] a = ArrayDeque.this.elements;
            int m = a.length - 1;
            int f = this.fence;
            int i = this.cursor;
            this.cursor = f;
            while (i != f) {
                E e = a[i];
                i = (i + 1) & m;
                if (e == null) {
                    throw new ConcurrentModificationException();
                }
                action.accept(e);
            }
        }
    }

    static final class DeqSpliterator<E> implements Spliterator<E> {
        private final ArrayDeque<E> deq;
        private int fence;
        private int index;

        DeqSpliterator(ArrayDeque<E> deq, int origin, int fence) {
            this.deq = deq;
            this.index = origin;
            this.fence = fence;
        }

        private int getFence() {
            int t = this.fence;
            if (t >= 0) {
                return t;
            }
            t = this.deq.tail;
            this.fence = t;
            this.index = this.deq.head;
            return t;
        }

        public DeqSpliterator<E> trySplit() {
            int t = getFence();
            int h = this.index;
            int n = this.deq.elements.length;
            if (h == t || ((h + 1) & (n - 1)) == t) {
                return null;
            }
            if (h > t) {
                t += n;
            }
            int m = ((h + t) >>> 1) & (n - 1);
            ArrayDeque arrayDeque = this.deq;
            this.index = m;
            return new DeqSpliterator(arrayDeque, h, m);
        }

        public void forEachRemaining(Consumer<? super E> consumer) {
            if (consumer == null) {
                throw new NullPointerException();
            }
            Object[] a = this.deq.elements;
            int m = a.length - 1;
            int f = getFence();
            int i = this.index;
            this.index = f;
            while (i != f) {
                E e = a[i];
                i = (i + 1) & m;
                if (e == null) {
                    throw new ConcurrentModificationException();
                }
                consumer.accept(e);
            }
        }

        public boolean tryAdvance(Consumer<? super E> consumer) {
            if (consumer == null) {
                throw new NullPointerException();
            }
            Object[] a = this.deq.elements;
            int m = a.length - 1;
            int f = getFence();
            int i = this.index;
            if (i == f) {
                return ArrayDeque.-assertionsDisabled;
            }
            E e = a[i];
            this.index = (i + 1) & m;
            if (e == null) {
                throw new ConcurrentModificationException();
            }
            consumer.accept(e);
            return true;
        }

        public long estimateSize() {
            int n = getFence() - this.index;
            if (n < 0) {
                n += this.deq.elements.length;
            }
            return (long) n;
        }

        public int characteristics() {
            return 16720;
        }
    }

    private class DescendingIterator implements Iterator<E> {
        private int cursor;
        private int fence;
        private int lastRet;

        /* synthetic */ DescendingIterator(ArrayDeque this$0, DescendingIterator -this1) {
            this();
        }

        private DescendingIterator() {
            this.cursor = ArrayDeque.this.tail;
            this.fence = ArrayDeque.this.head;
            this.lastRet = -1;
        }

        public boolean hasNext() {
            return this.cursor != this.fence ? true : ArrayDeque.-assertionsDisabled;
        }

        public E next() {
            if (this.cursor == this.fence) {
                throw new NoSuchElementException();
            }
            this.cursor = (this.cursor - 1) & (ArrayDeque.this.elements.length - 1);
            E result = ArrayDeque.this.elements[this.cursor];
            if (ArrayDeque.this.head != this.fence || result == null) {
                throw new ConcurrentModificationException();
            }
            this.lastRet = this.cursor;
            return result;
        }

        public void remove() {
            if (this.lastRet < 0) {
                throw new IllegalStateException();
            }
            if (!ArrayDeque.this.delete(this.lastRet)) {
                this.cursor = (this.cursor + 1) & (ArrayDeque.this.elements.length - 1);
                this.fence = ArrayDeque.this.head;
            }
            this.lastRet = -1;
        }
    }

    private void allocateElements(int numElements) {
        int initialCapacity = 8;
        if (numElements >= 8) {
            initialCapacity = numElements;
            initialCapacity = numElements | (numElements >>> 1);
            initialCapacity |= initialCapacity >>> 2;
            initialCapacity |= initialCapacity >>> 4;
            initialCapacity |= initialCapacity >>> 8;
            initialCapacity = (initialCapacity | (initialCapacity >>> 16)) + 1;
            if (initialCapacity < 0) {
                initialCapacity >>>= 1;
            }
        }
        this.elements = new Object[initialCapacity];
    }

    private void doubleCapacity() {
        if (-assertionsDisabled || this.head == this.tail) {
            int p = this.head;
            int n = this.elements.length;
            int r = n - p;
            int newCapacity = n << 1;
            if (newCapacity < 0) {
                throw new IllegalStateException("Sorry, deque too big");
            }
            Object a = new Object[newCapacity];
            System.arraycopy(this.elements, p, a, 0, r);
            System.arraycopy(this.elements, 0, a, r, p);
            this.elements = a;
            this.head = 0;
            this.tail = n;
            return;
        }
        throw new AssertionError();
    }

    public ArrayDeque() {
        this.elements = new Object[16];
    }

    public ArrayDeque(int numElements) {
        allocateElements(numElements);
    }

    public ArrayDeque(Collection<? extends E> c) {
        allocateElements(c.size());
        addAll(c);
    }

    public void addFirst(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        Object[] objArr = this.elements;
        int length = (this.head - 1) & (this.elements.length - 1);
        this.head = length;
        objArr[length] = e;
        if (this.head == this.tail) {
            doubleCapacity();
        }
    }

    public void addLast(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        this.elements[this.tail] = e;
        int length = (this.tail + 1) & (this.elements.length - 1);
        this.tail = length;
        if (length == this.head) {
            doubleCapacity();
        }
    }

    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    public boolean offerLast(E e) {
        addLast(e);
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
        Object[] elements = this.elements;
        int h = this.head;
        E result = elements[h];
        if (result != null) {
            elements[h] = null;
            this.head = (h + 1) & (elements.length - 1);
        }
        return result;
    }

    public E pollLast() {
        Object[] elements = this.elements;
        int t = (this.tail - 1) & (elements.length - 1);
        E result = elements[t];
        if (result != null) {
            elements[t] = null;
            this.tail = t;
        }
        return result;
    }

    public E getFirst() {
        E result = this.elements[this.head];
        if (result != null) {
            return result;
        }
        throw new NoSuchElementException();
    }

    public E getLast() {
        E result = this.elements[(this.tail - 1) & (this.elements.length - 1)];
        if (result != null) {
            return result;
        }
        throw new NoSuchElementException();
    }

    public E peekFirst() {
        return this.elements[this.head];
    }

    public E peekLast() {
        return this.elements[(this.tail - 1) & (this.elements.length - 1)];
    }

    public boolean removeFirstOccurrence(Object o) {
        if (o != null) {
            int mask = this.elements.length - 1;
            int i = this.head;
            while (true) {
                Object x = this.elements[i];
                if (x == null) {
                    break;
                } else if (o.lambda$-java_util_function_Predicate_4628(x)) {
                    delete(i);
                    return true;
                } else {
                    i = (i + 1) & mask;
                }
            }
        }
        return -assertionsDisabled;
    }

    public boolean removeLastOccurrence(Object o) {
        if (o != null) {
            int mask = this.elements.length - 1;
            int i = this.tail - 1;
            while (true) {
                int i2 = i & mask;
                Object x = this.elements[i2];
                if (x == null) {
                    break;
                } else if (o.lambda$-java_util_function_Predicate_4628(x)) {
                    delete(i2);
                    return true;
                } else {
                    i = i2 - 1;
                }
            }
        }
        return -assertionsDisabled;
    }

    public boolean add(E e) {
        addLast(e);
        return true;
    }

    public boolean offer(E e) {
        return offerLast(e);
    }

    public E remove() {
        return removeFirst();
    }

    public E poll() {
        return pollFirst();
    }

    public E element() {
        return getFirst();
    }

    public E peek() {
        return peekFirst();
    }

    public void push(E e) {
        addFirst(e);
    }

    public E pop() {
        return removeFirst();
    }

    private void checkInvariants() {
        Object obj = 1;
        if (-assertionsDisabled || this.elements[this.tail] == null) {
            if (!-assertionsDisabled) {
                if (this.head == this.tail) {
                    if (this.elements[this.head] != null) {
                        obj = null;
                    }
                } else if (this.elements[this.head] == null) {
                    obj = null;
                } else if (this.elements[(this.tail - 1) & (this.elements.length - 1)] == null) {
                    obj = null;
                }
                if (obj == null) {
                    throw new AssertionError();
                }
            }
            if (!-assertionsDisabled && this.elements[(this.head - 1) & (this.elements.length - 1)] != null) {
                throw new AssertionError();
            }
            return;
        }
        throw new AssertionError();
    }

    boolean delete(int i) {
        checkInvariants();
        Object elements = this.elements;
        int mask = elements.length - 1;
        int h = this.head;
        int t = this.tail;
        int front = (i - h) & mask;
        int back = (t - i) & mask;
        if (front >= ((t - h) & mask)) {
            throw new ConcurrentModificationException();
        } else if (front < back) {
            if (h <= i) {
                System.arraycopy(elements, h, elements, h + 1, front);
            } else {
                System.arraycopy(elements, 0, elements, 1, i);
                elements[0] = elements[mask];
                System.arraycopy(elements, h, elements, h + 1, mask - h);
            }
            elements[h] = null;
            this.head = (h + 1) & mask;
            return -assertionsDisabled;
        } else {
            if (i < t) {
                System.arraycopy(elements, i + 1, elements, i, back);
                this.tail = t - 1;
            } else {
                System.arraycopy(elements, i + 1, elements, i, mask - i);
                elements[mask] = elements[0];
                System.arraycopy(elements, 1, elements, 0, t);
                this.tail = (t - 1) & mask;
            }
            return true;
        }
    }

    public int size() {
        return (this.tail - this.head) & (this.elements.length - 1);
    }

    public boolean isEmpty() {
        return this.head == this.tail ? true : -assertionsDisabled;
    }

    public Iterator<E> iterator() {
        return new DeqIterator(this, null);
    }

    public Iterator<E> descendingIterator() {
        return new DescendingIterator(this, null);
    }

    public boolean contains(Object o) {
        if (o != null) {
            int mask = this.elements.length - 1;
            int i = this.head;
            while (true) {
                Object x = this.elements[i];
                if (x == null) {
                    break;
                } else if (o.lambda$-java_util_function_Predicate_4628(x)) {
                    return true;
                } else {
                    i = (i + 1) & mask;
                }
            }
        }
        return -assertionsDisabled;
    }

    public boolean remove(Object o) {
        return removeFirstOccurrence(o);
    }

    public void clear() {
        int h = this.head;
        int t = this.tail;
        if (h != t) {
            this.tail = 0;
            this.head = 0;
            int i = h;
            int mask = this.elements.length - 1;
            do {
                this.elements[i] = null;
                i = (i + 1) & mask;
            } while (i != t);
        }
    }

    public Object[] toArray() {
        int head = this.head;
        int tail = this.tail;
        boolean wrap = tail < head ? true : -assertionsDisabled;
        Object a = Arrays.copyOfRange(this.elements, head, wrap ? tail + this.elements.length : tail);
        if (wrap) {
            System.arraycopy(this.elements, 0, a, this.elements.length - head, tail);
        }
        return a;
    }

    public <T> T[] toArray(T[] a) {
        int length;
        Object a2;
        int head = this.head;
        int tail = this.tail;
        boolean wrap = tail < head ? true : -assertionsDisabled;
        int i = tail - head;
        if (wrap) {
            length = this.elements.length;
        } else {
            length = 0;
        }
        int size = i + length;
        if (wrap) {
            length = tail;
        } else {
            length = 0;
        }
        int firstLeg = size - length;
        int len = a2.length;
        if (size > len) {
            a2 = Arrays.copyOfRange(this.elements, head, head + size, a2.getClass());
        } else {
            System.arraycopy(this.elements, head, (Object) a2, 0, firstLeg);
            if (size < len) {
                a2[size] = null;
            }
        }
        if (wrap) {
            System.arraycopy(this.elements, 0, a2, firstLeg, tail);
        }
        return a2;
    }

    public ArrayDeque<E> clone() {
        try {
            ArrayDeque<E> result = (ArrayDeque) super.clone();
            result.elements = Arrays.copyOf(this.elements, this.elements.length);
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(size());
        int mask = this.elements.length - 1;
        for (int i = this.head; i != this.tail; i = (i + 1) & mask) {
            s.writeObject(this.elements[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int size = s.readInt();
        allocateElements(size);
        this.head = 0;
        this.tail = size;
        for (int i = 0; i < size; i++) {
            this.elements[i] = s.readObject();
        }
    }

    public Spliterator<E> spliterator() {
        return new DeqSpliterator(this, -1, -1);
    }
}
