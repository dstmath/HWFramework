package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import libcore.util.EmptyArray;

public class CopyOnWriteArrayList<E> implements List<E>, RandomAccess, Cloneable, Serializable {
    private static final long serialVersionUID = 8673264195747942595L;
    private volatile transient Object[] elements;

    static class CowIterator<E> implements ListIterator<E> {
        private final int from;
        private int index;
        private final Object[] snapshot;
        private final int to;

        CowIterator(Object[] snapshot, int from, int to) {
            this.index = 0;
            this.snapshot = snapshot;
            this.from = from;
            this.to = to;
            this.index = from;
        }

        public void add(E e) {
            throw new UnsupportedOperationException();
        }

        public boolean hasNext() {
            return this.index < this.to;
        }

        public boolean hasPrevious() {
            return this.index > this.from;
        }

        public E next() {
            if (this.index < this.to) {
                Object[] objArr = this.snapshot;
                int i = this.index;
                this.index = i + 1;
                return objArr[i];
            }
            throw new NoSuchElementException();
        }

        public int nextIndex() {
            return this.index;
        }

        public E previous() {
            if (this.index > this.from) {
                Object[] objArr = this.snapshot;
                int i = this.index - 1;
                this.index = i;
                return objArr[i];
            }
            throw new NoSuchElementException();
        }

        public int previousIndex() {
            return this.index - 1;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            Object[] elements = this.snapshot;
            for (int i = this.index; i < this.to; i++) {
                action.accept(elements[i]);
            }
            this.index = this.to;
        }
    }

    class CowSubList extends AbstractList<E> {
        private volatile Slice slice;

        public CowSubList(Object[] expectedElements, int from, int to) {
            this.slice = new Slice(expectedElements, from, to);
        }

        public int size() {
            Slice slice = this.slice;
            return slice.to - slice.from;
        }

        public boolean isEmpty() {
            Slice slice = this.slice;
            return slice.from == slice.to;
        }

        public E get(int index) {
            Slice slice = this.slice;
            Object[] snapshot = CopyOnWriteArrayList.this.elements;
            slice.checkElementIndex(index);
            slice.checkConcurrentModification(snapshot);
            return snapshot[slice.from + index];
        }

        public Iterator<E> iterator() {
            return listIterator(0);
        }

        public ListIterator<E> listIterator() {
            return listIterator(0);
        }

        public ListIterator<E> listIterator(int index) {
            Slice slice = this.slice;
            Object[] snapshot = CopyOnWriteArrayList.this.elements;
            slice.checkPositionIndex(index);
            slice.checkConcurrentModification(snapshot);
            CowIterator<E> result = new CowIterator(snapshot, slice.from, slice.to);
            result.index = slice.from + index;
            return result;
        }

        public int indexOf(Object object) {
            Slice slice = this.slice;
            Object[] snapshot = CopyOnWriteArrayList.this.elements;
            slice.checkConcurrentModification(snapshot);
            int result = CopyOnWriteArrayList.indexOf(object, snapshot, slice.from, slice.to);
            if (result != -1) {
                return result - slice.from;
            }
            return -1;
        }

        public int lastIndexOf(Object object) {
            Slice slice = this.slice;
            Object[] snapshot = CopyOnWriteArrayList.this.elements;
            slice.checkConcurrentModification(snapshot);
            int result = CopyOnWriteArrayList.lastIndexOf(object, snapshot, slice.from, slice.to);
            if (result != -1) {
                return result - slice.from;
            }
            return -1;
        }

        public boolean contains(Object object) {
            return indexOf(object) != -1;
        }

        public boolean containsAll(Collection<?> collection) {
            Slice slice = this.slice;
            Object[] snapshot = CopyOnWriteArrayList.this.elements;
            slice.checkConcurrentModification(snapshot);
            return CopyOnWriteArrayList.containsAll(collection, snapshot, slice.from, slice.to);
        }

        public List<E> subList(int from, int to) {
            Slice slice = this.slice;
            if (from >= 0 && from <= to && to <= size()) {
                return new CowSubList(slice.expectedElements, slice.from + from, slice.from + to);
            }
            throw new IndexOutOfBoundsException("from=" + from + ", to=" + to + ", list size=" + size());
        }

        public E remove(int index) {
            E removed;
            synchronized (CopyOnWriteArrayList.this) {
                this.slice.checkElementIndex(index);
                this.slice.checkConcurrentModification(CopyOnWriteArrayList.this.elements);
                removed = CopyOnWriteArrayList.this.remove(this.slice.from + index);
                this.slice = new Slice(CopyOnWriteArrayList.this.elements, this.slice.from, this.slice.to - 1);
            }
            return removed;
        }

        public void clear() {
            synchronized (CopyOnWriteArrayList.this) {
                this.slice.checkConcurrentModification(CopyOnWriteArrayList.this.elements);
                CopyOnWriteArrayList.this.removeRange(this.slice.from, this.slice.to);
                this.slice = new Slice(CopyOnWriteArrayList.this.elements, this.slice.from, this.slice.from);
            }
        }

        public void add(int index, E object) {
            synchronized (CopyOnWriteArrayList.this) {
                this.slice.checkPositionIndex(index);
                this.slice.checkConcurrentModification(CopyOnWriteArrayList.this.elements);
                CopyOnWriteArrayList.this.add(this.slice.from + index, object);
                this.slice = new Slice(CopyOnWriteArrayList.this.elements, this.slice.from, this.slice.to + 1);
            }
        }

        public boolean add(E object) {
            synchronized (CopyOnWriteArrayList.this) {
                add(this.slice.to - this.slice.from, object);
            }
            return true;
        }

        public boolean addAll(int index, Collection<? extends E> collection) {
            boolean result;
            synchronized (CopyOnWriteArrayList.this) {
                this.slice.checkPositionIndex(index);
                this.slice.checkConcurrentModification(CopyOnWriteArrayList.this.elements);
                int oldSize = CopyOnWriteArrayList.this.elements.length;
                result = CopyOnWriteArrayList.this.addAll(this.slice.from + index, collection);
                this.slice = new Slice(CopyOnWriteArrayList.this.elements, this.slice.from, this.slice.to + (CopyOnWriteArrayList.this.elements.length - oldSize));
            }
            return result;
        }

        public boolean addAll(Collection<? extends E> collection) {
            boolean addAll;
            synchronized (CopyOnWriteArrayList.this) {
                addAll = addAll(size(), collection);
            }
            return addAll;
        }

        public E set(int index, E object) {
            E result;
            synchronized (CopyOnWriteArrayList.this) {
                this.slice.checkElementIndex(index);
                this.slice.checkConcurrentModification(CopyOnWriteArrayList.this.elements);
                result = CopyOnWriteArrayList.this.set(this.slice.from + index, object);
                this.slice = new Slice(CopyOnWriteArrayList.this.elements, this.slice.from, this.slice.to);
            }
            return result;
        }

        public boolean remove(Object object) {
            synchronized (CopyOnWriteArrayList.this) {
                int index = indexOf(object);
                if (index == -1) {
                    return false;
                }
                remove(index);
                return true;
            }
        }

        public boolean removeAll(Collection<?> collection) {
            boolean z = false;
            synchronized (CopyOnWriteArrayList.this) {
                this.slice.checkConcurrentModification(CopyOnWriteArrayList.this.elements);
                int removed = CopyOnWriteArrayList.this.removeOrRetain(collection, false, this.slice.from, this.slice.to);
                this.slice = new Slice(CopyOnWriteArrayList.this.elements, this.slice.from, this.slice.to - removed);
                if (removed != 0) {
                    z = true;
                }
            }
            return z;
        }

        public boolean retainAll(Collection<?> collection) {
            boolean z = true;
            synchronized (CopyOnWriteArrayList.this) {
                this.slice.checkConcurrentModification(CopyOnWriteArrayList.this.elements);
                int removed = CopyOnWriteArrayList.this.removeOrRetain(collection, true, this.slice.from, this.slice.to);
                this.slice = new Slice(CopyOnWriteArrayList.this.elements, this.slice.from, this.slice.to - removed);
                if (removed == 0) {
                    z = false;
                }
            }
            return z;
        }

        public void forEach(Consumer<? super E> action) {
            CopyOnWriteArrayList.this.forInRange(this.slice.from, this.slice.to, action);
        }

        public void replaceAll(UnaryOperator<E> operator) {
            synchronized (CopyOnWriteArrayList.this) {
                this.slice.checkConcurrentModification(CopyOnWriteArrayList.this.elements);
                CopyOnWriteArrayList.this.replaceInRange(this.slice.from, this.slice.to, operator);
                this.slice = new Slice(CopyOnWriteArrayList.this.elements, this.slice.from, this.slice.to);
            }
        }

        public synchronized void sort(Comparator<? super E> c) {
            synchronized (CopyOnWriteArrayList.this) {
                this.slice.checkConcurrentModification(CopyOnWriteArrayList.this.elements);
                CopyOnWriteArrayList.this.sortInRange(this.slice.from, this.slice.to, c);
                this.slice = new Slice(CopyOnWriteArrayList.this.elements, this.slice.from, this.slice.to);
            }
        }
    }

    static class Slice {
        private final Object[] expectedElements;
        private final int from;
        private final int to;

        Slice(Object[] expectedElements, int from, int to) {
            this.expectedElements = expectedElements;
            this.from = from;
            this.to = to;
        }

        void checkElementIndex(int index) {
            if (index < 0 || index >= this.to - this.from) {
                throw new IndexOutOfBoundsException("index=" + index + ", size=" + (this.to - this.from));
            }
        }

        void checkPositionIndex(int index) {
            if (index < 0 || index > this.to - this.from) {
                throw new IndexOutOfBoundsException("index=" + index + ", size=" + (this.to - this.from));
            }
        }

        void checkConcurrentModification(Object[] snapshot) {
            if (this.expectedElements != snapshot) {
                throw new ConcurrentModificationException();
            }
        }
    }

    public CopyOnWriteArrayList() {
        this.elements = EmptyArray.OBJECT;
    }

    public CopyOnWriteArrayList(Collection<? extends E> collection) {
        this(collection.toArray());
    }

    public CopyOnWriteArrayList(E[] array) {
        this.elements = Arrays.copyOf(array, array.length, Object[].class);
    }

    public Object clone() {
        try {
            CopyOnWriteArrayList result = (CopyOnWriteArrayList) super.clone();
            result.elements = (Object[]) result.elements.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    public int size() {
        return this.elements.length;
    }

    public E get(int index) {
        return this.elements[index];
    }

    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    public boolean containsAll(Collection<?> collection) {
        Object[] snapshot = this.elements;
        return containsAll(collection, snapshot, 0, snapshot.length);
    }

    static boolean containsAll(Collection<?> collection, Object[] snapshot, int from, int to) {
        for (Object o : collection) {
            if (indexOf(o, snapshot, from, to) == -1) {
                return false;
            }
        }
        return true;
    }

    public int indexOf(E object, int from) {
        Object[] snapshot = this.elements;
        return indexOf(object, snapshot, from, snapshot.length);
    }

    public int indexOf(Object object) {
        Object[] snapshot = this.elements;
        return indexOf(object, snapshot, 0, snapshot.length);
    }

    public int lastIndexOf(E object, int to) {
        return lastIndexOf(object, this.elements, 0, to);
    }

    public int lastIndexOf(Object object) {
        Object[] snapshot = this.elements;
        return lastIndexOf(object, snapshot, 0, snapshot.length);
    }

    public boolean isEmpty() {
        return this.elements.length == 0;
    }

    public Iterator<E> iterator() {
        Object[] snapshot = this.elements;
        return new CowIterator(snapshot, 0, snapshot.length);
    }

    public ListIterator<E> listIterator(int index) {
        Object[] snapshot = this.elements;
        if (index < 0 || index > snapshot.length) {
            throw new IndexOutOfBoundsException("index=" + index + ", length=" + snapshot.length);
        }
        CowIterator<E> result = new CowIterator(snapshot, 0, snapshot.length);
        result.index = index;
        return result;
    }

    public ListIterator<E> listIterator() {
        Object[] snapshot = this.elements;
        return new CowIterator(snapshot, 0, snapshot.length);
    }

    public List<E> subList(int from, int to) {
        Object[] snapshot = this.elements;
        if (from >= 0 && from <= to && to <= snapshot.length) {
            return new CowSubList(snapshot, from, to);
        }
        throw new IndexOutOfBoundsException("from=" + from + ", to=" + to + ", list size=" + snapshot.length);
    }

    public Object[] toArray() {
        return (Object[]) this.elements.clone();
    }

    public <T> T[] toArray(T[] contents) {
        Object[] snapshot = this.elements;
        if (snapshot.length > contents.length) {
            return Arrays.copyOf(snapshot, snapshot.length, contents.getClass());
        }
        System.arraycopy(snapshot, 0, contents, 0, snapshot.length);
        if (snapshot.length < contents.length) {
            contents[snapshot.length] = null;
        }
        return contents;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other instanceof CopyOnWriteArrayList) {
            if (this != other) {
                z = Arrays.equals(this.elements, ((CopyOnWriteArrayList) other).elements);
            }
            return z;
        } else if (!(other instanceof List)) {
            return false;
        } else {
            Object[] snapshot = this.elements;
            Iterator<?> i = ((List) other).iterator();
            for (Object o : snapshot) {
                if (!i.hasNext() || !libcore.util.Objects.equal(o, i.next())) {
                    return false;
                }
            }
            if (i.hasNext()) {
                z = false;
            }
            return z;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(this.elements);
    }

    public String toString() {
        return Arrays.toString(this.elements);
    }

    public synchronized boolean add(E e) {
        Object[] newElements = new Object[(this.elements.length + 1)];
        System.arraycopy(this.elements, 0, newElements, 0, this.elements.length);
        newElements[this.elements.length] = e;
        this.elements = newElements;
        return true;
    }

    public synchronized void add(int index, E e) {
        Object[] newElements = new Object[(this.elements.length + 1)];
        System.arraycopy(this.elements, 0, newElements, 0, index);
        newElements[index] = e;
        System.arraycopy(this.elements, index, newElements, index + 1, this.elements.length - index);
        this.elements = newElements;
    }

    public synchronized boolean addAll(Collection<? extends E> collection) {
        return addAll(this.elements.length, collection);
    }

    public synchronized boolean addAll(int index, Collection<? extends E> collection) {
        boolean z = false;
        synchronized (this) {
            Object[] toAdd = collection.toArray();
            Object[] newElements = new Object[(this.elements.length + toAdd.length)];
            System.arraycopy(this.elements, 0, newElements, 0, index);
            System.arraycopy(toAdd, 0, newElements, index, toAdd.length);
            System.arraycopy(this.elements, index, newElements, toAdd.length + index, this.elements.length - index);
            this.elements = newElements;
            if (toAdd.length > 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized int addAllAbsent(Collection<? extends E> collection) {
        int addedCount;
        int i = 0;
        synchronized (this) {
            Object[] toAdd = collection.toArray();
            Object[] newElements = new Object[(this.elements.length + toAdd.length)];
            System.arraycopy(this.elements, 0, newElements, 0, this.elements.length);
            int length = toAdd.length;
            addedCount = 0;
            while (i < length) {
                int addedCount2;
                Object o = toAdd[i];
                if (indexOf(o, newElements, 0, this.elements.length + addedCount) == -1) {
                    addedCount2 = addedCount + 1;
                    newElements[this.elements.length + addedCount] = o;
                } else {
                    addedCount2 = addedCount;
                }
                i++;
                addedCount = addedCount2;
            }
            if (addedCount < toAdd.length) {
                newElements = Arrays.copyOfRange(newElements, 0, this.elements.length + addedCount);
            }
            this.elements = newElements;
        }
        return addedCount;
    }

    public synchronized boolean addIfAbsent(E object) {
        if (contains(object)) {
            return false;
        }
        add(object);
        return true;
    }

    public synchronized void clear() {
        this.elements = EmptyArray.OBJECT;
    }

    public synchronized E remove(int index) {
        E removed;
        removed = this.elements[index];
        removeRange(index, index + 1);
        return removed;
    }

    public synchronized boolean remove(Object o) {
        int index = indexOf(o);
        if (index == -1) {
            return false;
        }
        remove(index);
        return true;
    }

    public synchronized boolean removeAll(Collection<?> collection) {
        boolean z = false;
        synchronized (this) {
            if (removeOrRetain(collection, false, 0, this.elements.length) != 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean retainAll(Collection<?> collection) {
        boolean z = true;
        synchronized (this) {
            if (removeOrRetain(collection, true, 0, this.elements.length) == 0) {
                z = false;
            }
        }
        return z;
    }

    public synchronized void replaceAll(UnaryOperator<E> operator) {
        replaceInRange(0, this.elements.length, operator);
    }

    private void replaceInRange(int from, int to, UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        Object[] newElements = new Object[this.elements.length];
        System.arraycopy(this.elements, 0, newElements, 0, newElements.length);
        for (int i = from; i < to; i++) {
            newElements[i] = operator.apply(this.elements[i]);
        }
        this.elements = newElements;
    }

    public synchronized void sort(Comparator<? super E> c) {
        sortInRange(0, this.elements.length, c);
    }

    private synchronized void sortInRange(int from, int to, Comparator<? super E> c) {
        Objects.requireNonNull(c);
        Object[] newElements = new Object[this.elements.length];
        System.arraycopy(this.elements, 0, newElements, 0, newElements.length);
        Arrays.sort(newElements, from, to, c);
        this.elements = newElements;
    }

    public void forEach(Consumer<? super E> action) {
        forInRange(0, this.elements.length, action);
    }

    private void forInRange(int from, int to, Consumer<? super E> action) {
        Objects.requireNonNull(action);
        Object[] newElements = new Object[this.elements.length];
        System.arraycopy(this.elements, 0, newElements, 0, newElements.length);
        for (int i = from; i < to; i++) {
            action.accept(newElements[i]);
        }
    }

    private int removeOrRetain(Collection<?> collection, boolean retain, int from, int to) {
        int i = from;
        while (i < to) {
            if (collection.contains(this.elements[i]) == retain) {
                i++;
            } else {
                int newSize;
                Object[] newElements = new Object[(this.elements.length - 1)];
                System.arraycopy(this.elements, 0, newElements, 0, i);
                int j = i + 1;
                int newSize2 = i;
                while (j < to) {
                    if (collection.contains(this.elements[j]) == retain) {
                        newSize = newSize2 + 1;
                        newElements[newSize2] = this.elements[j];
                    } else {
                        newSize = newSize2;
                    }
                    j++;
                    newSize2 = newSize;
                }
                System.arraycopy(this.elements, to, newElements, newSize2, this.elements.length - to);
                newSize = newSize2 + (this.elements.length - to);
                if (newSize < newElements.length) {
                    newElements = Arrays.copyOfRange(newElements, 0, newSize);
                }
                int removed = this.elements.length - newElements.length;
                this.elements = newElements;
                return removed;
            }
        }
        return 0;
    }

    public synchronized E set(int index, E e) {
        E result;
        Object[] newElements = (Object[]) this.elements.clone();
        result = newElements[index];
        newElements[index] = e;
        this.elements = newElements;
        return result;
    }

    private void removeRange(int from, int to) {
        Object[] newElements = new Object[(this.elements.length - (to - from))];
        System.arraycopy(this.elements, 0, newElements, 0, from);
        System.arraycopy(this.elements, to, newElements, from, this.elements.length - to);
        this.elements = newElements;
    }

    static int lastIndexOf(Object o, Object[] data, int from, int to) {
        int i;
        if (o == null) {
            for (i = to - 1; i >= from; i--) {
                if (data[i] == null) {
                    return i;
                }
            }
        } else {
            for (i = to - 1; i >= from; i--) {
                if (o.equals(data[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    static int indexOf(Object o, Object[] data, int from, int to) {
        int i;
        if (o == null) {
            for (i = from; i < to; i++) {
                if (data[i] == null) {
                    return i;
                }
            }
        } else {
            for (i = from; i < to; i++) {
                if (o.equals(data[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    final Object[] getArray() {
        return this.elements;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        Object[] snapshot = this.elements;
        out.defaultWriteObject();
        out.writeInt(snapshot.length);
        for (Object o : snapshot) {
            out.writeObject(o);
        }
    }

    private synchronized void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Object[] snapshot = new Object[in.readInt()];
        for (int i = 0; i < snapshot.length; i++) {
            snapshot[i] = in.readObject();
        }
        this.elements = snapshot;
    }
}
