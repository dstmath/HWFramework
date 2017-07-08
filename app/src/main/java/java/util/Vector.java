package java.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import sun.util.logging.PlatformLogger;

public class Vector<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, Serializable {
    private static final int MAX_ARRAY_SIZE = 2147483639;
    private static final long serialVersionUID = -2767605614048989439L;
    protected int capacityIncrement;
    protected int elementCount;
    protected Object[] elementData;

    private class Itr implements Iterator<E> {
        int cursor;
        int expectedModCount;
        int lastRet;
        protected int limit;

        private Itr() {
            this.limit = Vector.this.elementCount;
            this.lastRet = -1;
            this.expectedModCount = Vector.this.modCount;
        }

        public boolean hasNext() {
            return this.cursor < this.limit;
        }

        public E next() {
            E elementData;
            synchronized (Vector.this) {
                checkForComodification();
                int i = this.cursor;
                if (i >= this.limit) {
                    throw new NoSuchElementException();
                }
                this.cursor = i + 1;
                Vector vector = Vector.this;
                this.lastRet = i;
                elementData = vector.elementData(i);
            }
            return elementData;
        }

        public void remove() {
            if (this.lastRet == -1) {
                throw new IllegalStateException();
            }
            synchronized (Vector.this) {
                checkForComodification();
                Vector.this.remove(this.lastRet);
                this.expectedModCount = Vector.this.modCount;
                this.limit--;
            }
            this.cursor = this.lastRet;
            this.lastRet = -1;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            synchronized (Vector.this) {
                int size = this.limit;
                int i = this.cursor;
                if (i >= size) {
                    return;
                }
                E[] elementData = Vector.this.elementData;
                if (i >= elementData.length) {
                    throw new ConcurrentModificationException();
                }
                int i2;
                while (true) {
                    i2 = i;
                    if (i2 != size) {
                        if (Vector.this.modCount != this.expectedModCount) {
                            break;
                        }
                        i = i2 + 1;
                        action.accept(elementData[i2]);
                    } else {
                        break;
                    }
                }
                this.cursor = i2;
                this.lastRet = i2 - 1;
                checkForComodification();
            }
        }

        final void checkForComodification() {
            if (Vector.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    final class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super(null);
            this.cursor = index;
        }

        public boolean hasPrevious() {
            return this.cursor != 0;
        }

        public int nextIndex() {
            return this.cursor;
        }

        public int previousIndex() {
            return this.cursor - 1;
        }

        public E previous() {
            E elementData;
            synchronized (Vector.this) {
                checkForComodification();
                int i = this.cursor - 1;
                if (i < 0) {
                    throw new NoSuchElementException();
                }
                this.cursor = i;
                Vector vector = Vector.this;
                this.lastRet = i;
                elementData = vector.elementData(i);
            }
            return elementData;
        }

        public void set(E e) {
            if (this.lastRet == -1) {
                throw new IllegalStateException();
            }
            synchronized (Vector.this) {
                checkForComodification();
                Vector.this.set(this.lastRet, e);
            }
        }

        public void add(E e) {
            int i = this.cursor;
            synchronized (Vector.this) {
                checkForComodification();
                Vector.this.add(i, e);
                this.expectedModCount = Vector.this.modCount;
            }
            this.cursor = i + 1;
            this.lastRet = -1;
        }
    }

    static final class VectorSpliterator<E> implements Spliterator<E> {
        private Object[] array;
        private int expectedModCount;
        private int fence;
        private int index;
        private final Vector<E> list;

        VectorSpliterator(Vector<E> list, Object[] array, int origin, int fence, int expectedModCount) {
            this.list = list;
            this.array = array;
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() {
            int hi = this.fence;
            if (hi < 0) {
                synchronized (this.list) {
                    this.array = this.list.elementData;
                    this.expectedModCount = this.list.modCount;
                    hi = this.list.elementCount;
                    this.fence = hi;
                }
            }
            return hi;
        }

        public Spliterator<E> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = (lo + hi) >>> 1;
            if (lo >= mid) {
                return null;
            }
            Vector vector = this.list;
            Object[] objArr = this.array;
            this.index = mid;
            return new VectorSpliterator(vector, objArr, lo, mid, this.expectedModCount);
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int fence = getFence();
            int i = this.index;
            if (fence <= i) {
                return false;
            }
            this.index = i + 1;
            action.accept(this.array[i]);
            if (this.list.modCount == this.expectedModCount) {
                return true;
            }
            throw new ConcurrentModificationException();
        }

        public void forEachRemaining(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Vector<E> lst = this.list;
            if (lst != null) {
                Object[] a;
                int hi = this.fence;
                if (hi < 0) {
                    synchronized (lst) {
                        this.expectedModCount = lst.modCount;
                        a = lst.elementData;
                        this.array = a;
                        hi = lst.elementCount;
                        this.fence = hi;
                    }
                } else {
                    a = this.array;
                }
                if (a != null) {
                    int i = this.index;
                    if (i >= 0) {
                        this.index = hi;
                        if (hi <= a.length) {
                            int i2 = i;
                            while (i2 < hi) {
                                i = i2 + 1;
                                action.accept(a[i2]);
                                i2 = i;
                            }
                            if (lst.modCount == this.expectedModCount) {
                                return;
                            }
                        }
                    }
                }
            }
            throw new ConcurrentModificationException();
        }

        public long estimateSize() {
            return (long) (getFence() - this.index);
        }

        public int characteristics() {
            return 16464;
        }
    }

    public Vector(int initialCapacity, int capacityIncrement) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.elementData = new Object[initialCapacity];
        this.capacityIncrement = capacityIncrement;
    }

    public Vector(int initialCapacity) {
        this(initialCapacity, 0);
    }

    public Vector() {
        this(10);
    }

    public Vector(Collection<? extends E> c) {
        this.elementData = c.toArray();
        this.elementCount = this.elementData.length;
        if (this.elementData.getClass() != Object[].class) {
            this.elementData = Arrays.copyOf(this.elementData, this.elementCount, Object[].class);
        }
    }

    public synchronized void copyInto(Object[] anArray) {
        System.arraycopy(this.elementData, 0, (Object) anArray, 0, this.elementCount);
    }

    public synchronized void trimToSize() {
        this.modCount++;
        if (this.elementCount < this.elementData.length) {
            this.elementData = Arrays.copyOf(this.elementData, this.elementCount);
        }
    }

    public synchronized void ensureCapacity(int minCapacity) {
        if (minCapacity > 0) {
            this.modCount++;
            ensureCapacityHelper(minCapacity);
        }
    }

    private void ensureCapacityHelper(int minCapacity) {
        if (minCapacity - this.elementData.length > 0) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        int i;
        int oldCapacity = this.elementData.length;
        if (this.capacityIncrement > 0) {
            i = this.capacityIncrement;
        } else {
            i = oldCapacity;
        }
        int newCapacity = oldCapacity + i;
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }
        this.elementData = Arrays.copyOf(this.elementData, newCapacity);
    }

    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new OutOfMemoryError();
        } else if (minCapacity > MAX_ARRAY_SIZE) {
            return PlatformLogger.OFF;
        } else {
            return MAX_ARRAY_SIZE;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setSize(int newSize) {
        this.modCount++;
        if (newSize <= this.elementCount) {
            int i = newSize;
            while (true) {
                if (i >= this.elementCount) {
                    break;
                }
                this.elementData[i] = null;
                i++;
            }
        } else {
            ensureCapacityHelper(newSize);
        }
        this.elementCount = newSize;
    }

    public synchronized int capacity() {
        return this.elementData.length;
    }

    public synchronized int size() {
        return this.elementCount;
    }

    public synchronized boolean isEmpty() {
        boolean z = false;
        synchronized (this) {
            if (this.elementCount == 0) {
                z = true;
            }
        }
        return z;
    }

    public Enumeration<E> elements() {
        return new Enumeration<E>() {
            int count;

            {
                this.count = 0;
            }

            public boolean hasMoreElements() {
                return this.count < Vector.this.elementCount;
            }

            public E nextElement() {
                synchronized (Vector.this) {
                    if (this.count < Vector.this.elementCount) {
                        Vector vector = Vector.this;
                        int i = this.count;
                        this.count = i + 1;
                        E elementData = vector.elementData(i);
                        return elementData;
                    }
                    throw new NoSuchElementException("Vector Enumeration");
                }
            }
        };
    }

    public boolean contains(Object o) {
        return indexOf(o, 0) >= 0;
    }

    public int indexOf(Object o) {
        return indexOf(o, 0);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int indexOf(Object o, int index) {
        int i;
        if (o != null) {
            i = index;
            while (true) {
                if (i >= this.elementCount) {
                    break;
                } else if (o.equals(this.elementData[i])) {
                    return i;
                } else {
                    i++;
                }
            }
        } else {
            i = index;
            while (true) {
                if (i >= this.elementCount) {
                    break;
                } else if (this.elementData[i] == null) {
                    return i;
                } else {
                    i++;
                }
            }
        }
        return -1;
    }

    public synchronized int lastIndexOf(Object o) {
        return lastIndexOf(o, this.elementCount - 1);
    }

    public synchronized int lastIndexOf(Object o, int index) {
        if (index >= this.elementCount) {
            throw new IndexOutOfBoundsException(index + " >= " + this.elementCount);
        }
        int i;
        if (o == null) {
            for (i = index; i >= 0; i--) {
                if (this.elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (i = index; i >= 0; i--) {
                if (o.equals(this.elementData[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public synchronized E elementAt(int index) {
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + this.elementCount);
        }
        return elementData(index);
    }

    public synchronized E firstElement() {
        if (this.elementCount == 0) {
            throw new NoSuchElementException();
        }
        return elementData(0);
    }

    public synchronized E lastElement() {
        if (this.elementCount == 0) {
            throw new NoSuchElementException();
        }
        return elementData(this.elementCount - 1);
    }

    public synchronized void setElementAt(E obj, int index) {
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + this.elementCount);
        }
        this.elementData[index] = obj;
    }

    public synchronized void removeElementAt(int index) {
        this.modCount++;
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " >= " + this.elementCount);
        } else if (index < 0) {
            throw new ArrayIndexOutOfBoundsException(index);
        } else {
            int j = (this.elementCount - index) - 1;
            if (j > 0) {
                System.arraycopy(this.elementData, index + 1, this.elementData, index, j);
            }
            this.elementCount--;
            this.elementData[this.elementCount] = null;
        }
    }

    public synchronized void insertElementAt(E obj, int index) {
        this.modCount++;
        if (index > this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index + " > " + this.elementCount);
        }
        ensureCapacityHelper(this.elementCount + 1);
        System.arraycopy(this.elementData, index, this.elementData, index + 1, this.elementCount - index);
        this.elementData[index] = obj;
        this.elementCount++;
    }

    public synchronized void addElement(E obj) {
        this.modCount++;
        ensureCapacityHelper(this.elementCount + 1);
        Object[] objArr = this.elementData;
        int i = this.elementCount;
        this.elementCount = i + 1;
        objArr[i] = obj;
    }

    public synchronized boolean removeElement(Object obj) {
        this.modCount++;
        int i = indexOf(obj);
        if (i < 0) {
            return false;
        }
        removeElementAt(i);
        return true;
    }

    public synchronized void removeAllElements() {
        this.modCount++;
        for (int i = 0; i < this.elementCount; i++) {
            this.elementData[i] = null;
        }
        this.elementCount = 0;
    }

    public synchronized Object clone() {
        Vector<E> v;
        try {
            v = (Vector) super.clone();
            v.elementData = Arrays.copyOf(this.elementData, this.elementCount);
            v.modCount = 0;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
        return v;
    }

    public synchronized Object[] toArray() {
        return Arrays.copyOf(this.elementData, this.elementCount);
    }

    public synchronized <T> T[] toArray(T[] a) {
        if (a.length < this.elementCount) {
            return Arrays.copyOf(this.elementData, this.elementCount, a.getClass());
        }
        System.arraycopy(this.elementData, 0, (Object) a, 0, this.elementCount);
        if (a.length > this.elementCount) {
            a[this.elementCount] = null;
        }
        return a;
    }

    E elementData(int index) {
        return this.elementData[index];
    }

    public synchronized E get(int index) {
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return elementData(index);
    }

    public synchronized E set(int index, E element) {
        E oldValue;
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        oldValue = elementData(index);
        this.elementData[index] = element;
        return oldValue;
    }

    public synchronized boolean add(E e) {
        this.modCount++;
        ensureCapacityHelper(this.elementCount + 1);
        Object[] objArr = this.elementData;
        int i = this.elementCount;
        this.elementCount = i + 1;
        objArr[i] = e;
        return true;
    }

    public boolean remove(Object o) {
        return removeElement(o);
    }

    public void add(int index, E element) {
        insertElementAt(element, index);
    }

    public synchronized E remove(int index) {
        E oldValue;
        this.modCount++;
        if (index >= this.elementCount) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        oldValue = elementData(index);
        int numMoved = (this.elementCount - index) - 1;
        if (numMoved > 0) {
            System.arraycopy(this.elementData, index + 1, this.elementData, index, numMoved);
        }
        Object[] objArr = this.elementData;
        int i = this.elementCount - 1;
        this.elementCount = i;
        objArr[i] = null;
        return oldValue;
    }

    public void clear() {
        removeAllElements();
    }

    public synchronized boolean containsAll(Collection<?> c) {
        return super.containsAll(c);
    }

    public synchronized boolean addAll(Collection<? extends E> c) {
        boolean z = false;
        synchronized (this) {
            this.modCount++;
            Object a = c.toArray();
            int numNew = a.length;
            ensureCapacityHelper(this.elementCount + numNew);
            System.arraycopy(a, 0, this.elementData, this.elementCount, numNew);
            this.elementCount += numNew;
            if (numNew != 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean removeAll(Collection<?> c) {
        return super.removeAll(c);
    }

    public synchronized boolean retainAll(Collection<?> c) {
        return super.retainAll(c);
    }

    public synchronized boolean addAll(int index, Collection<? extends E> c) {
        boolean z = false;
        synchronized (this) {
            this.modCount++;
            if (index < 0 || index > this.elementCount) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            Object a = c.toArray();
            int numNew = a.length;
            ensureCapacityHelper(this.elementCount + numNew);
            int numMoved = this.elementCount - index;
            if (numMoved > 0) {
                System.arraycopy(this.elementData, index, this.elementData, index + numNew, numMoved);
            }
            System.arraycopy(a, 0, this.elementData, index, numNew);
            this.elementCount += numNew;
            if (numNew != 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean equals(Object o) {
        return super.equals(o);
    }

    public synchronized int hashCode() {
        return super.hashCode();
    }

    public synchronized String toString() {
        return super.toString();
    }

    public synchronized List<E> subList(int fromIndex, int toIndex) {
        return Collections.synchronizedList(super.subList(fromIndex, toIndex), this);
    }

    protected synchronized void removeRange(int fromIndex, int toIndex) {
        this.modCount++;
        System.arraycopy(this.elementData, toIndex, this.elementData, fromIndex, this.elementCount - toIndex);
        int newElementCount = this.elementCount - (toIndex - fromIndex);
        while (this.elementCount != newElementCount) {
            Object[] objArr = this.elementData;
            int i = this.elementCount - 1;
            this.elementCount = i;
            objArr[i] = null;
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        Object data;
        PutField fields = s.putFields();
        synchronized (this) {
            fields.put("capacityIncrement", this.capacityIncrement);
            fields.put("elementCount", this.elementCount);
            data = (Object[]) this.elementData.clone();
        }
        fields.put("elementData", data);
        s.writeFields();
    }

    public synchronized ListIterator<E> listIterator(int index) {
        if (index >= 0) {
            if (index <= this.elementCount) {
            }
        }
        throw new IndexOutOfBoundsException("Index: " + index);
        return new ListItr(index);
    }

    public synchronized ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    public synchronized Iterator<E> iterator() {
        return new Itr();
    }

    public synchronized void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int expectedModCount = this.modCount;
        E[] elementData = this.elementData;
        int elementCount = this.elementCount;
        int i = 0;
        while (this.modCount == expectedModCount && i < elementCount) {
            action.accept(elementData[i]);
            i++;
        }
        if (this.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    public Spliterator<E> spliterator() {
        return new VectorSpliterator(this, null, 0, -1, 0);
    }

    public synchronized boolean removeIf(Predicate<? super E> filter) {
        boolean anyToRemove = false;
        synchronized (this) {
            Objects.requireNonNull(filter);
            int removeCount = 0;
            int size = this.elementCount;
            BitSet removeSet = new BitSet(size);
            int expectedModCount = this.modCount;
            int i = 0;
            while (this.modCount == expectedModCount && i < size) {
                if (filter.test(this.elementData[i])) {
                    removeSet.set(i);
                    removeCount++;
                }
                i++;
            }
            if (this.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (removeCount > 0) {
                anyToRemove = true;
            }
            if (anyToRemove) {
                int newSize = size - removeCount;
                i = 0;
                int j = 0;
                while (i < size && j < newSize) {
                    i = removeSet.nextClearBit(i);
                    this.elementData[j] = this.elementData[i];
                    i++;
                    j++;
                }
                for (int k = newSize; k < size; k++) {
                    this.elementData[k] = null;
                }
                this.elementCount = newSize;
                if (this.modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                this.modCount++;
            }
        }
        return anyToRemove;
    }

    public synchronized void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        int expectedModCount = this.modCount;
        int size = this.elementCount;
        int i = 0;
        while (this.modCount == expectedModCount && i < size) {
            this.elementData[i] = operator.apply(this.elementData[i]);
            i++;
        }
        if (this.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        this.modCount++;
    }

    public synchronized void sort(Comparator<? super E> c) {
        int expectedModCount = this.modCount;
        Arrays.sort(this.elementData, 0, this.elementCount, c);
        if (this.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        this.modCount++;
    }
}
