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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import sun.misc.Unsafe;

public class CopyOnWriteArrayList<E> implements List<E>, RandomAccess, Cloneable, Serializable {
    private static final long LOCK;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = 8673264195747942595L;
    private volatile transient Object[] elements;
    final transient Object lock = new Object();

    static final class COWIterator<E> implements ListIterator<E> {
        private int cursor;
        private final Object[] snapshot;

        COWIterator(Object[] elements, int initialCursor) {
            this.cursor = initialCursor;
            this.snapshot = elements;
        }

        public boolean hasNext() {
            return this.cursor < this.snapshot.length;
        }

        public boolean hasPrevious() {
            return this.cursor > 0;
        }

        public E next() {
            if (hasNext()) {
                Object[] objArr = this.snapshot;
                int i = this.cursor;
                this.cursor = i + 1;
                return objArr[i];
            }
            throw new NoSuchElementException();
        }

        public E previous() {
            if (hasPrevious()) {
                Object[] objArr = this.snapshot;
                int i = this.cursor - 1;
                this.cursor = i;
                return objArr[i];
            }
            throw new NoSuchElementException();
        }

        public int nextIndex() {
            return this.cursor;
        }

        public int previousIndex() {
            return this.cursor - 1;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        public void add(E e) {
            throw new UnsupportedOperationException();
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            int size = this.snapshot.length;
            for (int i = this.cursor; i < size; i++) {
                action.accept(this.snapshot[i]);
            }
            this.cursor = size;
        }
    }

    private static class COWSubList<E> extends AbstractList<E> implements RandomAccess {
        private Object[] expectedArray = this.l.getArray();
        private final CopyOnWriteArrayList<E> l;
        private final int offset;
        private int size;

        COWSubList(CopyOnWriteArrayList<E> list, int fromIndex, int toIndex) {
            this.l = list;
            this.offset = fromIndex;
            this.size = toIndex - fromIndex;
        }

        private void checkForComodification() {
            if (this.l.getArray() != this.expectedArray) {
                throw new ConcurrentModificationException();
            }
        }

        private void rangeCheck(int index) {
            if (index < 0 || index >= this.size) {
                throw new IndexOutOfBoundsException(CopyOnWriteArrayList.outOfBounds(index, this.size));
            }
        }

        public E set(int index, E element) {
            E x;
            synchronized (this.l.lock) {
                rangeCheck(index);
                checkForComodification();
                x = this.l.set(this.offset + index, element);
                this.expectedArray = this.l.getArray();
            }
            return x;
        }

        public E get(int index) {
            E e;
            synchronized (this.l.lock) {
                rangeCheck(index);
                checkForComodification();
                e = this.l.get(this.offset + index);
            }
            return e;
        }

        public int size() {
            int i;
            synchronized (this.l.lock) {
                checkForComodification();
                i = this.size;
            }
            return i;
        }

        public void add(int index, E element) {
            synchronized (this.l.lock) {
                checkForComodification();
                if (index < 0 || index > this.size) {
                    throw new IndexOutOfBoundsException(CopyOnWriteArrayList.outOfBounds(index, this.size));
                }
                this.l.add(this.offset + index, element);
                this.expectedArray = this.l.getArray();
                this.size++;
            }
        }

        public void clear() {
            synchronized (this.l.lock) {
                checkForComodification();
                this.l.removeRange(this.offset, this.offset + this.size);
                this.expectedArray = this.l.getArray();
                this.size = 0;
            }
        }

        public E remove(int index) {
            E result;
            synchronized (this.l.lock) {
                rangeCheck(index);
                checkForComodification();
                result = this.l.remove(this.offset + index);
                this.expectedArray = this.l.getArray();
                this.size--;
            }
            return result;
        }

        public boolean remove(Object o) {
            int index = indexOf(o);
            if (index == -1) {
                return false;
            }
            remove(index);
            return true;
        }

        public Iterator<E> iterator() {
            Iterator cOWSubListIterator;
            synchronized (this.l.lock) {
                checkForComodification();
                cOWSubListIterator = new COWSubListIterator(this.l, 0, this.offset, this.size);
            }
            return cOWSubListIterator;
        }

        public ListIterator<E> listIterator(int index) {
            ListIterator cOWSubListIterator;
            synchronized (this.l.lock) {
                checkForComodification();
                if (index < 0 || index > this.size) {
                    throw new IndexOutOfBoundsException(CopyOnWriteArrayList.outOfBounds(index, this.size));
                }
                cOWSubListIterator = new COWSubListIterator(this.l, index, this.offset, this.size);
            }
            return cOWSubListIterator;
        }

        public List<E> subList(int fromIndex, int toIndex) {
            List cOWSubList;
            synchronized (this.l.lock) {
                checkForComodification();
                if (fromIndex < 0 || toIndex > this.size || fromIndex > toIndex) {
                    throw new IndexOutOfBoundsException();
                }
                cOWSubList = new COWSubList(this.l, this.offset + fromIndex, this.offset + toIndex);
            }
            return cOWSubList;
        }

        public void forEach(Consumer<? super E> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int lo = this.offset;
            int hi = this.offset + this.size;
            Object[] a = this.expectedArray;
            if (this.l.getArray() != a) {
                throw new ConcurrentModificationException();
            } else if (lo < 0 || hi > a.length) {
                throw new IndexOutOfBoundsException();
            } else {
                for (int i = lo; i < hi; i++) {
                    action.accept(a[i]);
                }
            }
        }

        public void replaceAll(UnaryOperator<E> operator) {
            if (operator == null) {
                throw new NullPointerException();
            }
            synchronized (this.l.lock) {
                int lo = this.offset;
                int hi = this.offset + this.size;
                Object[] elements = this.expectedArray;
                if (this.l.getArray() != elements) {
                    throw new ConcurrentModificationException();
                }
                int len = elements.length;
                if (lo < 0 || hi > len) {
                    throw new IndexOutOfBoundsException();
                }
                Object[] newElements = Arrays.copyOf(elements, len);
                for (int i = lo; i < hi; i++) {
                    newElements[i] = operator.apply(elements[i]);
                }
                CopyOnWriteArrayList copyOnWriteArrayList = this.l;
                this.expectedArray = newElements;
                copyOnWriteArrayList.setArray(newElements);
            }
        }

        public void sort(Comparator<? super E> c) {
            synchronized (this.l.lock) {
                int lo = this.offset;
                int hi = this.offset + this.size;
                Object[] elements = this.expectedArray;
                if (this.l.getArray() != elements) {
                    throw new ConcurrentModificationException();
                }
                int len = elements.length;
                if (lo < 0 || hi > len) {
                    throw new IndexOutOfBoundsException();
                }
                E[] newElements = Arrays.copyOf(elements, len);
                E[] es = newElements;
                Arrays.sort(newElements, lo, hi, c);
                CopyOnWriteArrayList copyOnWriteArrayList = this.l;
                this.expectedArray = newElements;
                copyOnWriteArrayList.setArray(newElements);
            }
        }

        public boolean removeAll(Collection<?> c) {
            if (c == null) {
                throw new NullPointerException();
            }
            boolean removed = false;
            synchronized (this.l.lock) {
                int n = this.size;
                if (n > 0) {
                    int lo = this.offset;
                    int hi = this.offset + n;
                    Object elements = this.expectedArray;
                    if (this.l.getArray() != elements) {
                        throw new ConcurrentModificationException();
                    }
                    int len = elements.length;
                    if (lo < 0 || hi > len) {
                        throw new IndexOutOfBoundsException();
                    }
                    Object temp = new Object[n];
                    int i = lo;
                    int newSize = 0;
                    while (i < hi) {
                        int newSize2;
                        Object element = elements[i];
                        if (c.contains(element)) {
                            newSize2 = newSize;
                        } else {
                            newSize2 = newSize + 1;
                            temp[newSize] = element;
                        }
                        i++;
                        newSize = newSize2;
                    }
                    if (newSize != n) {
                        Object newElements = new Object[((len - n) + newSize)];
                        System.arraycopy(elements, 0, newElements, 0, lo);
                        System.arraycopy(temp, 0, newElements, lo, newSize);
                        System.arraycopy(elements, hi, newElements, lo + newSize, len - hi);
                        this.size = newSize;
                        removed = true;
                        CopyOnWriteArrayList copyOnWriteArrayList = this.l;
                        this.expectedArray = newElements;
                        copyOnWriteArrayList.setArray(newElements);
                    }
                }
            }
            return removed;
        }

        public boolean retainAll(Collection<?> c) {
            if (c == null) {
                throw new NullPointerException();
            }
            boolean removed = false;
            synchronized (this.l.lock) {
                int n = this.size;
                if (n > 0) {
                    int lo = this.offset;
                    int hi = this.offset + n;
                    Object elements = this.expectedArray;
                    if (this.l.getArray() != elements) {
                        throw new ConcurrentModificationException();
                    }
                    int len = elements.length;
                    if (lo < 0 || hi > len) {
                        throw new IndexOutOfBoundsException();
                    }
                    Object temp = new Object[n];
                    int i = lo;
                    int newSize = 0;
                    while (i < hi) {
                        int newSize2;
                        Object element = elements[i];
                        if (c.contains(element)) {
                            newSize2 = newSize + 1;
                            temp[newSize] = element;
                        } else {
                            newSize2 = newSize;
                        }
                        i++;
                        newSize = newSize2;
                    }
                    if (newSize != n) {
                        Object newElements = new Object[((len - n) + newSize)];
                        System.arraycopy(elements, 0, newElements, 0, lo);
                        System.arraycopy(temp, 0, newElements, lo, newSize);
                        System.arraycopy(elements, hi, newElements, lo + newSize, len - hi);
                        this.size = newSize;
                        removed = true;
                        CopyOnWriteArrayList copyOnWriteArrayList = this.l;
                        this.expectedArray = newElements;
                        copyOnWriteArrayList.setArray(newElements);
                    }
                }
            }
            return removed;
        }

        public boolean removeIf(Predicate<? super E> filter) {
            if (filter == null) {
                throw new NullPointerException();
            }
            boolean removed = false;
            synchronized (this.l.lock) {
                int n = this.size;
                if (n > 0) {
                    int lo = this.offset;
                    int hi = this.offset + n;
                    Object elements = this.expectedArray;
                    if (this.l.getArray() != elements) {
                        throw new ConcurrentModificationException();
                    }
                    int len = elements.length;
                    if (lo < 0 || hi > len) {
                        throw new IndexOutOfBoundsException();
                    }
                    Object temp = new Object[n];
                    int i = lo;
                    int newSize = 0;
                    while (i < hi) {
                        int newSize2;
                        E e = elements[i];
                        if (filter.test(e)) {
                            newSize2 = newSize;
                        } else {
                            newSize2 = newSize + 1;
                            temp[newSize] = e;
                        }
                        i++;
                        newSize = newSize2;
                    }
                    if (newSize != n) {
                        Object newElements = new Object[((len - n) + newSize)];
                        System.arraycopy(elements, 0, newElements, 0, lo);
                        System.arraycopy(temp, 0, newElements, lo, newSize);
                        System.arraycopy(elements, hi, newElements, lo + newSize, len - hi);
                        this.size = newSize;
                        removed = true;
                        CopyOnWriteArrayList copyOnWriteArrayList = this.l;
                        this.expectedArray = newElements;
                        copyOnWriteArrayList.setArray(newElements);
                    }
                }
            }
            return removed;
        }

        public Spliterator<E> spliterator() {
            int lo = this.offset;
            int hi = this.offset + this.size;
            Object[] a = this.expectedArray;
            if (this.l.getArray() != a) {
                throw new ConcurrentModificationException();
            } else if (lo >= 0 && hi <= a.length) {
                return Spliterators.spliterator(a, lo, hi, 1040);
            } else {
                throw new IndexOutOfBoundsException();
            }
        }
    }

    private static class COWSubListIterator<E> implements ListIterator<E> {
        private final ListIterator<E> it;
        private final int offset;
        private final int size;

        COWSubListIterator(List<E> l, int index, int offset, int size) {
            this.offset = offset;
            this.size = size;
            this.it = l.listIterator(index + offset);
        }

        public boolean hasNext() {
            return nextIndex() < this.size;
        }

        public E next() {
            if (hasNext()) {
                return this.it.next();
            }
            throw new NoSuchElementException();
        }

        public boolean hasPrevious() {
            return previousIndex() >= 0;
        }

        public E previous() {
            if (hasPrevious()) {
                return this.it.previous();
            }
            throw new NoSuchElementException();
        }

        public int nextIndex() {
            return this.it.nextIndex() - this.offset;
        }

        public int previousIndex() {
            return this.it.previousIndex() - this.offset;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(E e) {
            throw new UnsupportedOperationException();
        }

        public void add(E e) {
            throw new UnsupportedOperationException();
        }

        public void forEachRemaining(Consumer<? super E> action) {
            Objects.requireNonNull(action);
            while (nextIndex() < this.size) {
                action.accept(this.it.next());
            }
        }
    }

    final Object[] getArray() {
        return this.elements;
    }

    final void setArray(Object[] a) {
        this.elements = a;
    }

    public CopyOnWriteArrayList() {
        setArray(new Object[0]);
    }

    public CopyOnWriteArrayList(Collection<? extends E> c) {
        Object[] elements;
        if (c.getClass() == CopyOnWriteArrayList.class) {
            elements = ((CopyOnWriteArrayList) c).getArray();
        } else {
            elements = c.toArray();
            if (elements.getClass() != Object[].class) {
                elements = Arrays.copyOf(elements, elements.length, Object[].class);
            }
        }
        setArray(elements);
    }

    public CopyOnWriteArrayList(E[] toCopyIn) {
        setArray(Arrays.copyOf(toCopyIn, toCopyIn.length, Object[].class));
    }

    public int size() {
        return getArray().length;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    private static int indexOf(Object o, Object[] elements, int index, int fence) {
        int i;
        if (o == null) {
            for (i = index; i < fence; i++) {
                if (elements[i] == null) {
                    return i;
                }
            }
        } else {
            for (i = index; i < fence; i++) {
                if (o.lambda$-java_util_function_Predicate_4628(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int lastIndexOf(Object o, Object[] elements, int index) {
        int i;
        if (o == null) {
            for (i = index; i >= 0; i--) {
                if (elements[i] == null) {
                    return i;
                }
            }
        } else {
            for (i = index; i >= 0; i--) {
                if (o.lambda$-java_util_function_Predicate_4628(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean contains(Object o) {
        Object[] elements = getArray();
        if (indexOf(o, elements, 0, elements.length) >= 0) {
            return true;
        }
        return false;
    }

    public int indexOf(Object o) {
        Object[] elements = getArray();
        return indexOf(o, elements, 0, elements.length);
    }

    public int indexOf(E e, int index) {
        Object[] elements = getArray();
        return indexOf(e, elements, index, elements.length);
    }

    public int lastIndexOf(Object o) {
        Object[] elements = getArray();
        return lastIndexOf(o, elements, elements.length - 1);
    }

    public int lastIndexOf(E e, int index) {
        return lastIndexOf(e, getArray(), index);
    }

    public Object clone() {
        try {
            CopyOnWriteArrayList<E> clone = (CopyOnWriteArrayList) super.clone();
            clone.resetLock();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public Object[] toArray() {
        Object[] elements = getArray();
        return Arrays.copyOf(elements, elements.length);
    }

    public <T> T[] toArray(T[] a) {
        Object elements = getArray();
        int len = elements.length;
        if (a.length < len) {
            return Arrays.copyOf(elements, len, a.getClass());
        }
        System.arraycopy(elements, 0, (Object) a, 0, len);
        if (a.length > len) {
            a[len] = null;
        }
        return a;
    }

    private E get(Object[] a, int index) {
        return a[index];
    }

    static String outOfBounds(int index, int size) {
        return "Index: " + index + ", Size: " + size;
    }

    public E get(int index) {
        return get(getArray(), index);
    }

    public E set(int index, E element) {
        E oldValue;
        synchronized (this.lock) {
            Object[] elements = getArray();
            oldValue = get(elements, index);
            if (oldValue != element) {
                Object[] newElements = Arrays.copyOf(elements, elements.length);
                newElements[index] = element;
                setArray(newElements);
            } else {
                setArray(elements);
            }
        }
        return oldValue;
    }

    public boolean add(E e) {
        synchronized (this.lock) {
            Object[] elements = getArray();
            int len = elements.length;
            Object[] newElements = Arrays.copyOf(elements, len + 1);
            newElements[len] = e;
            setArray(newElements);
        }
        return true;
    }

    public void add(int index, E element) {
        synchronized (this.lock) {
            Object elements = getArray();
            int len = elements.length;
            if (index > len || index < 0) {
                throw new IndexOutOfBoundsException(outOfBounds(index, len));
            }
            Object[] newElements;
            int numMoved = len - index;
            if (numMoved == 0) {
                newElements = Arrays.copyOf((Object[]) elements, len + 1);
            } else {
                Object newElements2 = new Object[(len + 1)];
                System.arraycopy(elements, 0, newElements2, 0, index);
                System.arraycopy(elements, index, newElements2, index + 1, numMoved);
            }
            newElements2[index] = element;
            setArray(newElements2);
        }
    }

    public E remove(int index) {
        E oldValue;
        synchronized (this.lock) {
            Object elements = getArray();
            int len = elements.length;
            oldValue = get(elements, index);
            int numMoved = (len - index) - 1;
            if (numMoved == 0) {
                setArray(Arrays.copyOf((Object[]) elements, len - 1));
            } else {
                Object newElements = new Object[(len - 1)];
                System.arraycopy(elements, 0, newElements, 0, index);
                System.arraycopy(elements, index + 1, newElements, index, numMoved);
                setArray(newElements);
            }
        }
        return oldValue;
    }

    public boolean remove(Object o) {
        Object[] snapshot = getArray();
        int index = indexOf(o, snapshot, 0, snapshot.length);
        if (index < 0) {
            return false;
        }
        return remove(o, snapshot, index);
    }

    private boolean remove(Object o, Object[] snapshot, int index) {
        synchronized (this.lock) {
            Object current = getArray();
            int len = current.length;
            if (snapshot != current) {
                int prefix = Math.min(index, len);
                int i = 0;
                while (i < prefix) {
                    if (current[i] != snapshot[i] && Objects.equals(o, current[i])) {
                        index = i;
                        break;
                    }
                    i++;
                }
                if (index >= len) {
                    return false;
                } else if (current[index] != o) {
                    index = indexOf(o, current, index, len);
                    if (index < 0) {
                        return false;
                    }
                }
            }
            Object newElements = new Object[(len - 1)];
            System.arraycopy(current, 0, newElements, 0, index);
            System.arraycopy(current, index + 1, newElements, index, (len - index) - 1);
            setArray(newElements);
            return true;
        }
    }

    void removeRange(int fromIndex, int toIndex) {
        synchronized (this.lock) {
            Object elements = getArray();
            int len = elements.length;
            if (fromIndex < 0 || toIndex > len || toIndex < fromIndex) {
                throw new IndexOutOfBoundsException();
            }
            int newlen = len - (toIndex - fromIndex);
            int numMoved = len - toIndex;
            if (numMoved == 0) {
                setArray(Arrays.copyOf((Object[]) elements, newlen));
            } else {
                Object newElements = new Object[newlen];
                System.arraycopy(elements, 0, newElements, 0, fromIndex);
                System.arraycopy(elements, toIndex, newElements, fromIndex, numMoved);
                setArray(newElements);
            }
        }
    }

    public boolean addIfAbsent(E e) {
        Object[] snapshot = getArray();
        if (indexOf(e, snapshot, 0, snapshot.length) >= 0) {
            return false;
        }
        return addIfAbsent(e, snapshot);
    }

    private boolean addIfAbsent(E e, Object[] snapshot) {
        synchronized (this.lock) {
            Object[] current = getArray();
            int len = current.length;
            if (snapshot != current) {
                int common = Math.min(snapshot.length, len);
                int i = 0;
                while (i < common) {
                    if (current[i] == snapshot[i] || !Objects.equals(e, current[i])) {
                        i++;
                    } else {
                        return false;
                    }
                }
                if (indexOf(e, current, common, len) >= 0) {
                    return false;
                }
            }
            Object[] newElements = Arrays.copyOf(current, len + 1);
            newElements[len] = e;
            setArray(newElements);
            return true;
        }
    }

    public boolean containsAll(Collection<?> c) {
        Object[] elements = getArray();
        int len = elements.length;
        for (Object e : c) {
            if (indexOf(e, elements, 0, len) < 0) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:21:0x0037, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean removeAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        synchronized (this.lock) {
            Object[] elements = getArray();
            int len = elements.length;
            if (len != 0) {
                Object[] temp = new Object[len];
                int i = 0;
                int newlen = 0;
                while (i < len) {
                    int newlen2;
                    Object element = elements[i];
                    if (c.contains(element)) {
                        newlen2 = newlen;
                    } else {
                        newlen2 = newlen + 1;
                        temp[newlen] = element;
                    }
                    i++;
                    newlen = newlen2;
                }
                if (newlen != len) {
                    setArray(Arrays.copyOf(temp, newlen));
                    return true;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:21:0x0037, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean retainAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        synchronized (this.lock) {
            Object[] elements = getArray();
            int len = elements.length;
            if (len != 0) {
                Object[] temp = new Object[len];
                int i = 0;
                int newlen = 0;
                while (i < len) {
                    int newlen2;
                    Object element = elements[i];
                    if (c.contains(element)) {
                        newlen2 = newlen + 1;
                        temp[newlen] = element;
                    } else {
                        newlen2 = newlen;
                    }
                    i++;
                    newlen = newlen2;
                }
                if (newlen != len) {
                    setArray(Arrays.copyOf(temp, newlen));
                    return true;
                }
            }
        }
    }

    public int addAllAbsent(Collection<? extends E> c) {
        Object cs = c.toArray();
        if (cs.length == 0) {
            return 0;
        }
        int added;
        synchronized (this.lock) {
            Object[] elements = getArray();
            int len = elements.length;
            int added2 = 0;
            int i = 0;
            while (true) {
                added = added2;
                if (i >= cs.length) {
                    break;
                }
                Object e = cs[i];
                if (indexOf(e, elements, 0, len) >= 0 || indexOf(e, cs, 0, added) >= 0) {
                    added2 = added;
                } else {
                    added2 = added + 1;
                    cs[added] = e;
                }
                i++;
            }
            if (added > 0) {
                Object newElements = Arrays.copyOf(elements, len + added);
                System.arraycopy(cs, 0, newElements, len, added);
                setArray(newElements);
            }
        }
        return added;
    }

    public void clear() {
        synchronized (this.lock) {
            setArray(new Object[0]);
        }
    }

    public boolean addAll(Collection<? extends E> c) {
        Object cs = c.getClass() == CopyOnWriteArrayList.class ? ((CopyOnWriteArrayList) c).getArray() : c.toArray();
        if (cs.length == 0) {
            return false;
        }
        synchronized (this.lock) {
            Object[] elements = getArray();
            int len = elements.length;
            if (len == 0 && cs.getClass() == Object[].class) {
                setArray(cs);
            } else {
                Object newElements = Arrays.copyOf(elements, cs.length + len);
                System.arraycopy(cs, 0, newElements, len, cs.length);
                setArray(newElements);
            }
        }
        return true;
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        Object cs = c.toArray();
        synchronized (this.lock) {
            Object elements = getArray();
            int len = elements.length;
            if (index > len || index < 0) {
                throw new IndexOutOfBoundsException(outOfBounds(index, len));
            } else if (cs.length == 0) {
                return false;
            } else {
                Object newElements;
                int numMoved = len - index;
                if (numMoved == 0) {
                    newElements = Arrays.copyOf((Object[]) elements, cs.length + len);
                } else {
                    newElements = new Object[(cs.length + len)];
                    System.arraycopy(elements, 0, newElements, 0, index);
                    System.arraycopy(elements, index, newElements, cs.length + index, numMoved);
                }
                System.arraycopy(cs, 0, newElements, index, cs.length);
                setArray(newElements);
                return true;
            }
        }
    }

    public void forEach(Consumer<? super E> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        for (E x : getArray()) {
            E e = x;
            action.accept(x);
        }
    }

    public boolean removeIf(Predicate<? super E> filter) {
        if (filter == null) {
            throw new NullPointerException();
        }
        synchronized (this.lock) {
            Object elements = getArray();
            int len = elements.length;
            for (int i = 0; i < len; i++) {
                if (filter.test(elements[i])) {
                    int newlen = i;
                    Object[] newElements = new Object[(len - 1)];
                    System.arraycopy(elements, 0, (Object) newElements, 0, newlen);
                    i++;
                    int newlen2 = newlen;
                    while (i < len) {
                        E x = elements[i];
                        if (filter.test(x)) {
                            newlen = newlen2;
                        } else {
                            newlen = newlen2 + 1;
                            newElements[newlen2] = x;
                        }
                        i++;
                        newlen2 = newlen;
                    }
                    if (newlen2 != len - 1) {
                        newElements = Arrays.copyOf(newElements, newlen2);
                    }
                    setArray(newElements);
                    return true;
                }
            }
            return false;
        }
    }

    public void replaceAll(UnaryOperator<E> operator) {
        if (operator == null) {
            throw new NullPointerException();
        }
        synchronized (this.lock) {
            Object[] elements = getArray();
            int len = elements.length;
            Object[] newElements = Arrays.copyOf(elements, len);
            for (int i = 0; i < len; i++) {
                newElements[i] = operator.apply(elements[i]);
            }
            setArray(newElements);
        }
    }

    public void sort(Comparator<? super E> c) {
        synchronized (this.lock) {
            Object[] elements = getArray();
            E[] newElements = Arrays.copyOf(elements, elements.length);
            E[] es = newElements;
            Arrays.sort(newElements, c);
            setArray(newElements);
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Object[] elements = getArray();
        s.writeInt(elements.length);
        for (Object element : elements) {
            s.writeObject(element);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        resetLock();
        int len = s.readInt();
        Object[] elements = new Object[len];
        for (int i = 0; i < len; i++) {
            elements[i] = s.readObject();
        }
        setArray(elements);
    }

    public String toString() {
        return Arrays.toString(getArray());
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }
        Iterator<?> it = ((List) o).iterator();
        Object[] elements = getArray();
        int i = 0;
        int len = elements.length;
        while (i < len) {
            if (!it.hasNext() || (Objects.equals(elements[i], it.next()) ^ 1) != 0) {
                return false;
            }
            i++;
        }
        return !it.hasNext();
    }

    public int hashCode() {
        int hashCode = 1;
        for (Object x : getArray()) {
            hashCode = (hashCode * 31) + (x == null ? 0 : x.hashCode());
        }
        return hashCode;
    }

    public Iterator<E> iterator() {
        return new COWIterator(getArray(), 0);
    }

    public ListIterator<E> listIterator() {
        return new COWIterator(getArray(), 0);
    }

    public ListIterator<E> listIterator(int index) {
        Object[] elements = getArray();
        int len = elements.length;
        if (index >= 0 && index <= len) {
            return new COWIterator(elements, index);
        }
        throw new IndexOutOfBoundsException(outOfBounds(index, len));
    }

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(getArray(), 1040);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        List cOWSubList;
        synchronized (this.lock) {
            int len = getArray().length;
            if (fromIndex < 0 || toIndex > len || fromIndex > toIndex) {
                throw new IndexOutOfBoundsException();
            }
            cOWSubList = new COWSubList(this, fromIndex, toIndex);
        }
        return cOWSubList;
    }

    private void resetLock() {
        U.putObjectVolatile(this, LOCK, new Object());
    }

    static {
        try {
            LOCK = U.objectFieldOffset(CopyOnWriteArrayList.class.getDeclaredField("lock"));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
