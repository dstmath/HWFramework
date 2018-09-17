package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ArrayList<E> extends AbstractList<E> implements List<E>, RandomAccess, Cloneable, Serializable {
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = new Object[0];
    private static final int DEFAULT_CAPACITY = 10;
    private static final Object[] EMPTY_ELEMENTDATA = new Object[0];
    private static final int MAX_ARRAY_SIZE = 2147483639;
    private static final long serialVersionUID = 8683452581122892189L;
    transient Object[] elementData;
    private int size;

    static final class ArrayListSpliterator<E> implements Spliterator<E> {
        private int expectedModCount;
        private int fence;
        private int index;
        private final ArrayList<E> list;

        ArrayListSpliterator(ArrayList<E> list, int origin, int fence, int expectedModCount) {
            this.list = list;
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }

        private int getFence() {
            int hi = this.fence;
            if (hi >= 0) {
                return hi;
            }
            ArrayList<E> lst = this.list;
            if (lst == null) {
                this.fence = 0;
                return 0;
            }
            this.expectedModCount = lst.modCount;
            hi = lst.size;
            this.fence = hi;
            return hi;
        }

        public ArrayListSpliterator<E> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = (lo + hi) >>> 1;
            if (lo >= mid) {
                return null;
            }
            ArrayList arrayList = this.list;
            this.index = mid;
            return new ArrayListSpliterator(arrayList, lo, mid, this.expectedModCount);
        }

        public boolean tryAdvance(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int hi = getFence();
            int i = this.index;
            if (i >= hi) {
                return false;
            }
            this.index = i + 1;
            action.accept(this.list.elementData[i]);
            if (this.list.modCount == this.expectedModCount) {
                return true;
            }
            throw new ConcurrentModificationException();
        }

        public void forEachRemaining(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            ArrayList<E> lst = this.list;
            if (lst != null) {
                Object[] a = lst.elementData;
                if (a != null) {
                    int mc;
                    int hi = this.fence;
                    if (hi < 0) {
                        mc = lst.modCount;
                        hi = lst.size;
                    } else {
                        mc = this.expectedModCount;
                    }
                    int i = this.index;
                    if (i >= 0) {
                        this.index = hi;
                        if (hi <= a.length) {
                            while (i < hi) {
                                action.accept(a[i]);
                                i++;
                            }
                            if (lst.modCount == mc) {
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

    private class Itr implements Iterator<E> {
        int cursor;
        int expectedModCount;
        int lastRet;
        protected int limit;

        /* synthetic */ Itr(ArrayList this$0, Itr -this1) {
            this();
        }

        private Itr() {
            this.limit = ArrayList.this.size;
            this.lastRet = -1;
            this.expectedModCount = ArrayList.this.modCount;
        }

        public boolean hasNext() {
            return this.cursor < this.limit;
        }

        public E next() {
            if (ArrayList.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            int i = this.cursor;
            if (i >= this.limit) {
                throw new NoSuchElementException();
            }
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            this.cursor = i + 1;
            this.lastRet = i;
            return elementData[i];
        }

        public void remove() {
            if (this.lastRet < 0) {
                throw new IllegalStateException();
            } else if (ArrayList.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                try {
                    ArrayList.this.remove(this.lastRet);
                    this.cursor = this.lastRet;
                    this.lastRet = -1;
                    this.expectedModCount = ArrayList.this.modCount;
                    this.limit--;
                } catch (IndexOutOfBoundsException e) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        public void forEachRemaining(Consumer<? super E> consumer) {
            Objects.requireNonNull(consumer);
            int size = ArrayList.this.size;
            int i = this.cursor;
            if (i < size) {
                Object[] elementData = ArrayList.this.elementData;
                if (i >= elementData.length) {
                    throw new ConcurrentModificationException();
                }
                int i2;
                while (true) {
                    i2 = i;
                    if (i2 == size || ArrayList.this.modCount != this.expectedModCount) {
                        this.cursor = i2;
                        this.lastRet = i2 - 1;
                    } else {
                        i = i2 + 1;
                        consumer.accept(elementData[i2]);
                    }
                }
                this.cursor = i2;
                this.lastRet = i2 - 1;
                if (ArrayList.this.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super(ArrayList.this, null);
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
            if (ArrayList.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            int i = this.cursor - 1;
            if (i < 0) {
                throw new NoSuchElementException();
            }
            Object[] elementData = ArrayList.this.elementData;
            if (i >= elementData.length) {
                throw new ConcurrentModificationException();
            }
            this.cursor = i;
            this.lastRet = i;
            return elementData[i];
        }

        public void set(E e) {
            if (this.lastRet < 0) {
                throw new IllegalStateException();
            } else if (ArrayList.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                try {
                    ArrayList.this.set(this.lastRet, e);
                } catch (IndexOutOfBoundsException e2) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        public void add(E e) {
            if (ArrayList.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            try {
                int i = this.cursor;
                ArrayList.this.add(i, e);
                this.cursor = i + 1;
                this.lastRet = -1;
                this.expectedModCount = ArrayList.this.modCount;
                this.limit++;
            } catch (IndexOutOfBoundsException e2) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private class SubList extends AbstractList<E> implements RandomAccess {
        private final int offset;
        private final AbstractList<E> parent;
        private final int parentOffset;
        int size;

        SubList(AbstractList<E> parent, int offset, int fromIndex, int toIndex) {
            this.parent = parent;
            this.parentOffset = fromIndex;
            this.offset = offset + fromIndex;
            this.size = toIndex - fromIndex;
            this.modCount = ArrayList.this.modCount;
        }

        public E set(int index, E e) {
            if (index < 0 || index >= this.size) {
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            } else if (ArrayList.this.modCount != this.modCount) {
                throw new ConcurrentModificationException();
            } else {
                E oldValue = ArrayList.this.elementData[this.offset + index];
                ArrayList.this.elementData[this.offset + index] = e;
                return oldValue;
            }
        }

        public E get(int index) {
            if (index < 0 || index >= this.size) {
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            } else if (ArrayList.this.modCount == this.modCount) {
                return ArrayList.this.elementData[this.offset + index];
            } else {
                throw new ConcurrentModificationException();
            }
        }

        public int size() {
            if (ArrayList.this.modCount == this.modCount) {
                return this.size;
            }
            throw new ConcurrentModificationException();
        }

        public void add(int index, E e) {
            if (index < 0 || index > this.size) {
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            } else if (ArrayList.this.modCount != this.modCount) {
                throw new ConcurrentModificationException();
            } else {
                this.parent.add(this.parentOffset + index, e);
                this.modCount = this.parent.modCount;
                this.size++;
            }
        }

        public E remove(int index) {
            if (index < 0 || index >= this.size) {
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            } else if (ArrayList.this.modCount != this.modCount) {
                throw new ConcurrentModificationException();
            } else {
                E result = this.parent.remove(this.parentOffset + index);
                this.modCount = this.parent.modCount;
                this.size--;
                return result;
            }
        }

        protected void removeRange(int fromIndex, int toIndex) {
            if (ArrayList.this.modCount != this.modCount) {
                throw new ConcurrentModificationException();
            }
            this.parent.removeRange(this.parentOffset + fromIndex, this.parentOffset + toIndex);
            this.modCount = this.parent.modCount;
            this.size -= toIndex - fromIndex;
        }

        public boolean addAll(Collection<? extends E> c) {
            return addAll(this.size, c);
        }

        public boolean addAll(int index, Collection<? extends E> c) {
            if (index < 0 || index > this.size) {
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            }
            int cSize = c.size();
            if (cSize == 0) {
                return false;
            }
            if (ArrayList.this.modCount != this.modCount) {
                throw new ConcurrentModificationException();
            }
            this.parent.addAll(this.parentOffset + index, c);
            this.modCount = this.parent.modCount;
            this.size += cSize;
            return true;
        }

        public Iterator<E> iterator() {
            return listIterator();
        }

        public ListIterator<E> listIterator(final int index) {
            if (ArrayList.this.modCount != this.modCount) {
                throw new ConcurrentModificationException();
            } else if (index < 0 || index > this.size) {
                throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
            } else {
                final int offset = this.offset;
                return new ListIterator<E>() {
                    int cursor = index;
                    int expectedModCount = ArrayList.this.modCount;
                    int lastRet = -1;

                    public boolean hasNext() {
                        return this.cursor != SubList.this.size;
                    }

                    public E next() {
                        if (this.expectedModCount != ArrayList.this.modCount) {
                            throw new ConcurrentModificationException();
                        }
                        int i = this.cursor;
                        if (i >= SubList.this.size) {
                            throw new NoSuchElementException();
                        }
                        Object[] elementData = ArrayList.this.elementData;
                        if (offset + i >= elementData.length) {
                            throw new ConcurrentModificationException();
                        }
                        this.cursor = i + 1;
                        int i2 = offset;
                        this.lastRet = i;
                        return elementData[i2 + i];
                    }

                    public boolean hasPrevious() {
                        return this.cursor != 0;
                    }

                    public E previous() {
                        if (this.expectedModCount != ArrayList.this.modCount) {
                            throw new ConcurrentModificationException();
                        }
                        int i = this.cursor - 1;
                        if (i < 0) {
                            throw new NoSuchElementException();
                        }
                        Object[] elementData = ArrayList.this.elementData;
                        if (offset + i >= elementData.length) {
                            throw new ConcurrentModificationException();
                        }
                        this.cursor = i;
                        int i2 = offset;
                        this.lastRet = i;
                        return elementData[i2 + i];
                    }

                    public void forEachRemaining(Consumer<? super E> consumer) {
                        Objects.requireNonNull(consumer);
                        int size = SubList.this.size;
                        int i = this.cursor;
                        if (i < size) {
                            Object[] elementData = ArrayList.this.elementData;
                            if (offset + i >= elementData.length) {
                                throw new ConcurrentModificationException();
                            }
                            int i2;
                            while (true) {
                                i2 = i;
                                if (i2 == size || SubList.this.modCount != this.expectedModCount) {
                                    this.cursor = i2;
                                    this.lastRet = i2;
                                } else {
                                    i = i2 + 1;
                                    consumer.accept(elementData[offset + i2]);
                                }
                            }
                            this.cursor = i2;
                            this.lastRet = i2;
                            if (this.expectedModCount != ArrayList.this.modCount) {
                                throw new ConcurrentModificationException();
                            }
                        }
                    }

                    public int nextIndex() {
                        return this.cursor;
                    }

                    public int previousIndex() {
                        return this.cursor - 1;
                    }

                    public void remove() {
                        if (this.lastRet < 0) {
                            throw new IllegalStateException();
                        } else if (this.expectedModCount != ArrayList.this.modCount) {
                            throw new ConcurrentModificationException();
                        } else {
                            try {
                                SubList.this.remove(this.lastRet);
                                this.cursor = this.lastRet;
                                this.lastRet = -1;
                                this.expectedModCount = ArrayList.this.modCount;
                            } catch (IndexOutOfBoundsException e) {
                                throw new ConcurrentModificationException();
                            }
                        }
                    }

                    public void set(E e) {
                        if (this.lastRet < 0) {
                            throw new IllegalStateException();
                        } else if (this.expectedModCount != ArrayList.this.modCount) {
                            throw new ConcurrentModificationException();
                        } else {
                            try {
                                ArrayList.this.set(offset + this.lastRet, e);
                            } catch (IndexOutOfBoundsException e2) {
                                throw new ConcurrentModificationException();
                            }
                        }
                    }

                    public void add(E e) {
                        if (this.expectedModCount != ArrayList.this.modCount) {
                            throw new ConcurrentModificationException();
                        }
                        try {
                            int i = this.cursor;
                            SubList.this.add(i, e);
                            this.cursor = i + 1;
                            this.lastRet = -1;
                            this.expectedModCount = ArrayList.this.modCount;
                        } catch (IndexOutOfBoundsException e2) {
                            throw new ConcurrentModificationException();
                        }
                    }
                };
            }
        }

        public List<E> subList(int fromIndex, int toIndex) {
            ArrayList.subListRangeCheck(fromIndex, toIndex, this.size);
            return new SubList(this, this.offset, fromIndex, toIndex);
        }

        private String outOfBoundsMsg(int index) {
            return "Index: " + index + ", Size: " + this.size;
        }

        public Spliterator<E> spliterator() {
            if (this.modCount == ArrayList.this.modCount) {
                return new ArrayListSpliterator(ArrayList.this, this.offset, this.offset + this.size, this.modCount);
            }
            throw new ConcurrentModificationException();
        }
    }

    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
    }

    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    public ArrayList(Collection<? extends E> c) {
        this.elementData = c.toArray();
        int length = this.elementData.length;
        this.size = length;
        if (length == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else if (this.elementData.getClass() != Object[].class) {
            this.elementData = Arrays.copyOf(this.elementData, this.size, Object[].class);
        }
    }

    public void trimToSize() {
        this.modCount++;
        if (this.size < this.elementData.length) {
            Object[] objArr;
            if (this.size == 0) {
                objArr = EMPTY_ELEMENTDATA;
            } else {
                objArr = Arrays.copyOf(this.elementData, this.size);
            }
            this.elementData = objArr;
        }
    }

    public void ensureCapacity(int minCapacity) {
        int minExpand;
        if (this.elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minExpand = 0;
        } else {
            minExpand = 10;
        }
        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    private void ensureCapacityInternal(int minCapacity) {
        if (this.elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(10, minCapacity);
        }
        ensureExplicitCapacity(minCapacity);
    }

    private void ensureExplicitCapacity(int minCapacity) {
        this.modCount++;
        if (minCapacity - this.elementData.length > 0) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        int oldCapacity = this.elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
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
            return Integer.MAX_VALUE;
        } else {
            return MAX_ARRAY_SIZE;
        }
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    public int indexOf(Object o) {
        int i;
        if (o == null) {
            for (i = 0; i < this.size; i++) {
                if (this.elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (i = 0; i < this.size; i++) {
                if (o.lambda$-java_util_function_Predicate_4628(this.elementData[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        int i;
        if (o == null) {
            for (i = this.size - 1; i >= 0; i--) {
                if (this.elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (i = this.size - 1; i >= 0; i--) {
                if (o.lambda$-java_util_function_Predicate_4628(this.elementData[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList) super.clone();
            v.elementData = Arrays.copyOf(this.elementData, this.size);
            v.modCount = 0;
            return v;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    public Object[] toArray() {
        return Arrays.copyOf(this.elementData, this.size);
    }

    public <T> T[] toArray(T[] a) {
        if (a.length < this.size) {
            return Arrays.copyOf(this.elementData, this.size, a.getClass());
        }
        System.arraycopy(this.elementData, 0, (Object) a, 0, this.size);
        if (a.length > this.size) {
            a[this.size] = null;
        }
        return a;
    }

    public E get(int index) {
        if (index < this.size) {
            return this.elementData[index];
        }
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    public E set(int index, E element) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        E oldValue = this.elementData[index];
        this.elementData[index] = element;
        return oldValue;
    }

    public boolean add(E e) {
        ensureCapacityInternal(this.size + 1);
        Object[] objArr = this.elementData;
        int i = this.size;
        this.size = i + 1;
        objArr[i] = e;
        return true;
    }

    public void add(int index, E element) {
        if (index > this.size || index < 0) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        ensureCapacityInternal(this.size + 1);
        System.arraycopy(this.elementData, index, this.elementData, index + 1, this.size - index);
        this.elementData[index] = element;
        this.size++;
    }

    public E remove(int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        this.modCount++;
        E oldValue = this.elementData[index];
        int numMoved = (this.size - index) - 1;
        if (numMoved > 0) {
            System.arraycopy(this.elementData, index + 1, this.elementData, index, numMoved);
        }
        Object[] objArr = this.elementData;
        int i = this.size - 1;
        this.size = i;
        objArr[i] = null;
        return oldValue;
    }

    public boolean remove(Object o) {
        int index;
        if (o == null) {
            for (index = 0; index < this.size; index++) {
                if (this.elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
            }
        } else {
            for (index = 0; index < this.size; index++) {
                if (o.lambda$-java_util_function_Predicate_4628(this.elementData[index])) {
                    fastRemove(index);
                    return true;
                }
            }
        }
        return false;
    }

    private void fastRemove(int index) {
        this.modCount++;
        int numMoved = (this.size - index) - 1;
        if (numMoved > 0) {
            System.arraycopy(this.elementData, index + 1, this.elementData, index, numMoved);
        }
        Object[] objArr = this.elementData;
        int i = this.size - 1;
        this.size = i;
        objArr[i] = null;
    }

    public void clear() {
        this.modCount++;
        for (int i = 0; i < this.size; i++) {
            this.elementData[i] = null;
        }
        this.size = 0;
    }

    public boolean addAll(Collection<? extends E> c) {
        Object a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(this.size + numNew);
        System.arraycopy(a, 0, this.elementData, this.size, numNew);
        this.size += numNew;
        if (numNew != 0) {
            return true;
        }
        return false;
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        if (index > this.size || index < 0) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
        Object a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(this.size + numNew);
        int numMoved = this.size - index;
        if (numMoved > 0) {
            System.arraycopy(this.elementData, index, this.elementData, index + numNew, numMoved);
        }
        System.arraycopy(a, 0, this.elementData, index, numNew);
        this.size += numNew;
        if (numNew != 0) {
            return true;
        }
        return false;
    }

    protected void removeRange(int fromIndex, int toIndex) {
        if (toIndex < fromIndex) {
            throw new IndexOutOfBoundsException("toIndex < fromIndex");
        }
        this.modCount++;
        System.arraycopy(this.elementData, toIndex, this.elementData, fromIndex, this.size - toIndex);
        int newSize = this.size - (toIndex - fromIndex);
        for (int i = newSize; i < this.size; i++) {
            this.elementData[i] = null;
        }
        this.size = newSize;
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + this.size;
    }

    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, false);
    }

    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return batchRemove(c, true);
    }

    private boolean batchRemove(Collection<?> c, boolean complement) {
        Throwable th;
        Object elementData = this.elementData;
        int r = 0;
        int w = 0;
        while (true) {
            int w2;
            int i;
            try {
                w2 = w;
                if (r < this.size) {
                    if (c.contains(elementData[r]) == complement) {
                        w = w2 + 1;
                        try {
                            elementData[w2] = elementData[r];
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } else {
                        w = w2;
                    }
                    r++;
                } else {
                    if (r != this.size) {
                        System.arraycopy(elementData, r, elementData, w2, this.size - r);
                        w = w2 + (this.size - r);
                    } else {
                        w = w2;
                    }
                    if (w == this.size) {
                        return false;
                    }
                    for (i = w; i < this.size; i++) {
                        elementData[i] = null;
                    }
                    this.modCount += this.size - w;
                    this.size = w;
                    return true;
                }
            } catch (Throwable th3) {
                th = th3;
                w = w2;
                if (r != this.size) {
                    System.arraycopy(elementData, r, elementData, w, this.size - r);
                    w += this.size - r;
                }
                if (w != this.size) {
                    for (i = w; i < this.size; i++) {
                        elementData[i] = null;
                    }
                    this.modCount += this.size - w;
                    this.size = w;
                }
                throw th;
            }
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        int expectedModCount = this.modCount;
        s.defaultWriteObject();
        s.writeInt(this.size);
        for (int i = 0; i < this.size; i++) {
            s.writeObject(this.elementData[i]);
        }
        if (this.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        this.elementData = EMPTY_ELEMENTDATA;
        s.defaultReadObject();
        s.readInt();
        if (this.size > 0) {
            ensureCapacityInternal(this.size);
            Object[] a = this.elementData;
            for (int i = 0; i < this.size; i++) {
                a[i] = s.readObject();
            }
        }
    }

    public ListIterator<E> listIterator(int index) {
        if (index >= 0 && index <= this.size) {
            return new ListItr(index);
        }
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    public ListIterator<E> listIterator() {
        return new ListItr(0);
    }

    public Iterator<E> iterator() {
        return new Itr(this, null);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        subListRangeCheck(fromIndex, toIndex, this.size);
        return new SubList(this, 0, fromIndex, toIndex);
    }

    static void subListRangeCheck(int fromIndex, int toIndex, int size) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        } else if (toIndex > size) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        } else if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
    }

    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        int expectedModCount = this.modCount;
        E[] elementData = this.elementData;
        int size = this.size;
        int i = 0;
        while (this.modCount == expectedModCount && i < size) {
            action.accept(elementData[i]);
            i++;
        }
        if (this.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
    }

    public Spliterator<E> spliterator() {
        return new ArrayListSpliterator(this, 0, -1, 0);
    }

    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        int removeCount = 0;
        BitSet removeSet = new BitSet(this.size);
        int expectedModCount = this.modCount;
        int size = this.size;
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
        boolean anyToRemove = removeCount > 0;
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
            this.size = newSize;
            if (this.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            this.modCount++;
        }
        return anyToRemove;
    }

    public void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        int expectedModCount = this.modCount;
        int size = this.size;
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

    public void sort(Comparator<? super E> c) {
        int expectedModCount = this.modCount;
        Arrays.sort(this.elementData, 0, this.size, c);
        if (this.modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        this.modCount++;
    }
}
