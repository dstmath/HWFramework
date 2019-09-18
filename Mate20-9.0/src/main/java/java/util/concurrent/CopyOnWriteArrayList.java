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
                E[] eArr = this.snapshot;
                int i = this.cursor;
                this.cursor = i + 1;
                return eArr[i];
            }
            throw new NoSuchElementException();
        }

        public E previous() {
            if (hasPrevious()) {
                E[] eArr = this.snapshot;
                int i = this.cursor - 1;
                this.cursor = i;
                return eArr[i];
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
            COWSubListIterator cOWSubListIterator;
            synchronized (this.l.lock) {
                checkForComodification();
                cOWSubListIterator = new COWSubListIterator(this.l, 0, this.offset, this.size);
            }
            return cOWSubListIterator;
        }

        public ListIterator<E> listIterator(int index) {
            COWSubListIterator cOWSubListIterator;
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
            COWSubList cOWSubList;
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
            if (action != null) {
                int lo = this.offset;
                int hi = this.offset + this.size;
                E[] a = this.expectedArray;
                if (this.l.getArray() != a) {
                    throw new ConcurrentModificationException();
                } else if (lo < 0 || hi > a.length) {
                    throw new IndexOutOfBoundsException();
                } else {
                    for (int i = lo; i < hi; i++) {
                        action.accept(a[i]);
                    }
                }
            } else {
                throw new NullPointerException();
            }
        }

        public void replaceAll(UnaryOperator<E> operator) {
            if (operator != null) {
                synchronized (this.l.lock) {
                    int lo = this.offset;
                    int hi = this.offset + this.size;
                    E[] elements = this.expectedArray;
                    if (this.l.getArray() == elements) {
                        int len = elements.length;
                        if (lo < 0 || hi > len) {
                            throw new IndexOutOfBoundsException();
                        }
                        Object[] newElements = Arrays.copyOf((T[]) elements, len);
                        for (int i = lo; i < hi; i++) {
                            newElements[i] = operator.apply(elements[i]);
                        }
                        CopyOnWriteArrayList<E> copyOnWriteArrayList = this.l;
                        this.expectedArray = newElements;
                        copyOnWriteArrayList.setArray(newElements);
                    } else {
                        throw new ConcurrentModificationException();
                    }
                }
                return;
            }
            throw new NullPointerException();
        }

        public void sort(Comparator<? super E> c) {
            synchronized (this.l.lock) {
                int lo = this.offset;
                int hi = this.offset + this.size;
                Object[] elements = this.expectedArray;
                if (this.l.getArray() == elements) {
                    int len = elements.length;
                    if (lo < 0 || hi > len) {
                        throw new IndexOutOfBoundsException();
                    }
                    E[] newElements = Arrays.copyOf((T[]) elements, len);
                    Arrays.sort(newElements, lo, hi, c);
                    CopyOnWriteArrayList<E> copyOnWriteArrayList = this.l;
                    this.expectedArray = newElements;
                    copyOnWriteArrayList.setArray(newElements);
                } else {
                    throw new ConcurrentModificationException();
                }
            }
        }

        public boolean removeAll(Collection<?> c) {
            if (c != null) {
                boolean removed = false;
                synchronized (this.l.lock) {
                    int n = this.size;
                    if (n > 0) {
                        int lo = this.offset;
                        int hi = this.offset + n;
                        Object[] elements = this.expectedArray;
                        if (this.l.getArray() == elements) {
                            int len = elements.length;
                            if (lo < 0 || hi > len) {
                                throw new IndexOutOfBoundsException();
                            }
                            Object[] temp = new Object[n];
                            int newSize = 0;
                            for (int i = lo; i < hi; i++) {
                                Object element = elements[i];
                                if (!c.contains(element)) {
                                    temp[newSize] = element;
                                    newSize++;
                                }
                            }
                            if (newSize != n) {
                                Object[] newElements = new Object[((len - n) + newSize)];
                                System.arraycopy((Object) elements, 0, (Object) newElements, 0, lo);
                                System.arraycopy((Object) temp, 0, (Object) newElements, lo, newSize);
                                System.arraycopy((Object) elements, hi, (Object) newElements, lo + newSize, len - hi);
                                this.size = newSize;
                                removed = true;
                                CopyOnWriteArrayList<E> copyOnWriteArrayList = this.l;
                                this.expectedArray = newElements;
                                copyOnWriteArrayList.setArray(newElements);
                            }
                        } else {
                            throw new ConcurrentModificationException();
                        }
                    }
                }
                return removed;
            }
            throw new NullPointerException();
        }

        public boolean retainAll(Collection<?> c) {
            if (c != null) {
                boolean removed = false;
                synchronized (this.l.lock) {
                    int n = this.size;
                    if (n > 0) {
                        int lo = this.offset;
                        int hi = this.offset + n;
                        Object[] elements = this.expectedArray;
                        if (this.l.getArray() == elements) {
                            int len = elements.length;
                            if (lo < 0 || hi > len) {
                                throw new IndexOutOfBoundsException();
                            }
                            Object[] temp = new Object[n];
                            int newSize = 0;
                            for (int i = lo; i < hi; i++) {
                                Object element = elements[i];
                                if (c.contains(element)) {
                                    temp[newSize] = element;
                                    newSize++;
                                }
                            }
                            if (newSize != n) {
                                Object[] newElements = new Object[((len - n) + newSize)];
                                System.arraycopy((Object) elements, 0, (Object) newElements, 0, lo);
                                System.arraycopy((Object) temp, 0, (Object) newElements, lo, newSize);
                                System.arraycopy((Object) elements, hi, (Object) newElements, lo + newSize, len - hi);
                                this.size = newSize;
                                removed = true;
                                CopyOnWriteArrayList<E> copyOnWriteArrayList = this.l;
                                this.expectedArray = newElements;
                                copyOnWriteArrayList.setArray(newElements);
                            }
                        } else {
                            throw new ConcurrentModificationException();
                        }
                    }
                }
                return removed;
            }
            throw new NullPointerException();
        }

        public boolean removeIf(Predicate<? super E> filter) {
            if (filter != null) {
                boolean removed = false;
                synchronized (this.l.lock) {
                    int n = this.size;
                    if (n > 0) {
                        int lo = this.offset;
                        int hi = this.offset + n;
                        E[] elements = this.expectedArray;
                        if (this.l.getArray() == elements) {
                            int len = elements.length;
                            if (lo < 0 || hi > len) {
                                throw new IndexOutOfBoundsException();
                            }
                            Object[] temp = new Object[n];
                            int newSize = 0;
                            for (int i = lo; i < hi; i++) {
                                E e = elements[i];
                                if (!filter.test(e)) {
                                    temp[newSize] = e;
                                    newSize++;
                                }
                            }
                            if (newSize != n) {
                                Object[] newElements = new Object[((len - n) + newSize)];
                                System.arraycopy((Object) elements, 0, (Object) newElements, 0, lo);
                                System.arraycopy((Object) temp, 0, (Object) newElements, lo, newSize);
                                System.arraycopy((Object) elements, hi, (Object) newElements, lo + newSize, len - hi);
                                this.size = newSize;
                                removed = true;
                                CopyOnWriteArrayList<E> copyOnWriteArrayList = this.l;
                                this.expectedArray = newElements;
                                copyOnWriteArrayList.setArray(newElements);
                            }
                        } else {
                            throw new ConcurrentModificationException();
                        }
                    }
                }
                return removed;
            }
            throw new NullPointerException();
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

        COWSubListIterator(List<E> l, int index, int offset2, int size2) {
            this.offset = offset2;
            this.size = size2;
            this.it = l.listIterator(index + offset2);
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

    /* access modifiers changed from: package-private */
    public final Object[] getArray() {
        return this.elements;
    }

    /* access modifiers changed from: package-private */
    public final void setArray(Object[] a) {
        this.elements = a;
    }

    public CopyOnWriteArrayList() {
        setArray(new Object[0]);
    }

    public CopyOnWriteArrayList(Collection<? extends E> c) {
        Object[] elements2;
        if (c.getClass() == CopyOnWriteArrayList.class) {
            elements2 = ((CopyOnWriteArrayList) c).getArray();
        } else {
            elements2 = c.toArray();
            if (elements2.getClass() != Object[].class) {
                elements2 = Arrays.copyOf(elements2, elements2.length, Object[].class);
            }
        }
        setArray(elements2);
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

    private static int indexOf(Object o, Object[] elements2, int index, int fence) {
        if (o == null) {
            for (int i = index; i < fence; i++) {
                if (elements2[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i2 = index; i2 < fence; i2++) {
                if (o.equals(elements2[i2])) {
                    return i2;
                }
            }
        }
        return -1;
    }

    private static int lastIndexOf(Object o, Object[] elements2, int index) {
        if (o == null) {
            for (int i = index; i >= 0; i--) {
                if (elements2[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i2 = index; i2 >= 0; i2--) {
                if (o.equals(elements2[i2])) {
                    return i2;
                }
            }
        }
        return -1;
    }

    public boolean contains(Object o) {
        Object[] elements2 = getArray();
        return indexOf(o, elements2, 0, elements2.length) >= 0;
    }

    public int indexOf(Object o) {
        Object[] elements2 = getArray();
        return indexOf(o, elements2, 0, elements2.length);
    }

    public int indexOf(E e, int index) {
        Object[] elements2 = getArray();
        return indexOf(e, elements2, index, elements2.length);
    }

    public int lastIndexOf(Object o) {
        Object[] elements2 = getArray();
        return lastIndexOf(o, elements2, elements2.length - 1);
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
        Object[] elements2 = getArray();
        return Arrays.copyOf((T[]) elements2, elements2.length);
    }

    public <T> T[] toArray(T[] a) {
        Object[] elements2 = getArray();
        int len = elements2.length;
        if (a.length < len) {
            return Arrays.copyOf(elements2, len, a.getClass());
        }
        System.arraycopy((Object) elements2, 0, (Object) a, 0, len);
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
            Object[] elements2 = getArray();
            oldValue = get(elements2, index);
            if (oldValue != element) {
                Object[] newElements = Arrays.copyOf((T[]) elements2, elements2.length);
                newElements[index] = element;
                setArray(newElements);
            } else {
                setArray(elements2);
            }
        }
        return oldValue;
    }

    public boolean add(E e) {
        synchronized (this.lock) {
            Object[] elements2 = getArray();
            int len = elements2.length;
            Object[] newElements = Arrays.copyOf((T[]) elements2, len + 1);
            newElements[len] = e;
            setArray(newElements);
        }
        return true;
    }

    public void add(int index, E element) {
        Object[] newElements;
        synchronized (this.lock) {
            Object[] elements2 = getArray();
            int len = elements2.length;
            if (index > len || index < 0) {
                throw new IndexOutOfBoundsException(outOfBounds(index, len));
            }
            int numMoved = len - index;
            if (numMoved == 0) {
                newElements = Arrays.copyOf((T[]) elements2, len + 1);
            } else {
                newElements = new Object[(len + 1)];
                System.arraycopy((Object) elements2, 0, (Object) newElements, 0, index);
                System.arraycopy((Object) elements2, index, (Object) newElements, index + 1, numMoved);
            }
            newElements[index] = element;
            setArray(newElements);
        }
    }

    public E remove(int index) {
        E oldValue;
        synchronized (this.lock) {
            Object[] elements2 = getArray();
            int len = elements2.length;
            oldValue = get(elements2, index);
            int numMoved = (len - index) - 1;
            if (numMoved == 0) {
                setArray(Arrays.copyOf((T[]) elements2, len - 1));
            } else {
                Object[] newElements = new Object[(len - 1)];
                System.arraycopy((Object) elements2, 0, (Object) newElements, 0, index);
                System.arraycopy((Object) elements2, index + 1, (Object) newElements, index, numMoved);
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
            Object[] current = getArray();
            int len = current.length;
            if (snapshot != current) {
                int prefix = Math.min(index, len);
                int i = 0;
                while (true) {
                    if (i < prefix) {
                        if (current[i] != snapshot[i] && Objects.equals(o, current[i])) {
                            index = i;
                            break;
                        }
                        i++;
                    } else if (index >= len) {
                        return false;
                    } else {
                        if (current[index] != o) {
                            index = indexOf(o, current, index, len);
                            if (index < 0) {
                                return false;
                            }
                        }
                    }
                }
            }
            Object[] newElements = new Object[(len - 1)];
            System.arraycopy((Object) current, 0, (Object) newElements, 0, index);
            System.arraycopy((Object) current, index + 1, (Object) newElements, index, (len - index) - 1);
            setArray(newElements);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void removeRange(int fromIndex, int toIndex) {
        synchronized (this.lock) {
            Object[] elements2 = getArray();
            int len = elements2.length;
            if (fromIndex < 0 || toIndex > len || toIndex < fromIndex) {
                throw new IndexOutOfBoundsException();
            }
            int newlen = len - (toIndex - fromIndex);
            int numMoved = len - toIndex;
            if (numMoved == 0) {
                setArray(Arrays.copyOf((T[]) elements2, newlen));
            } else {
                Object[] newElements = new Object[newlen];
                System.arraycopy((Object) elements2, 0, (Object) newElements, 0, fromIndex);
                System.arraycopy((Object) elements2, toIndex, (Object) newElements, fromIndex, numMoved);
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
                for (int i = 0; i < common; i++) {
                    if (current[i] != snapshot[i] && Objects.equals(e, current[i])) {
                        return false;
                    }
                }
                if (indexOf(e, current, common, len) >= 0) {
                    return false;
                }
            }
            Object[] newElements = Arrays.copyOf((T[]) current, len + 1);
            newElements[len] = e;
            setArray(newElements);
            return true;
        }
    }

    public boolean containsAll(Collection<?> c) {
        Object[] elements2 = getArray();
        int len = elements2.length;
        for (Object e : c) {
            if (indexOf(e, elements2, 0, len) < 0) {
                return false;
            }
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0031, code lost:
        return false;
     */
    public boolean removeAll(Collection<?> c) {
        if (c != null) {
            synchronized (this.lock) {
                if (len != 0) {
                    Object[] temp = new Object[len];
                    int newlen = 0;
                    for (Object element : getArray()) {
                        if (!c.contains(element)) {
                            temp[newlen] = element;
                            newlen++;
                        }
                    }
                    if (newlen != len) {
                        setArray(Arrays.copyOf((T[]) temp, newlen));
                        return true;
                    }
                }
            }
        } else {
            throw new NullPointerException();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0031, code lost:
        return false;
     */
    public boolean retainAll(Collection<?> c) {
        if (c != null) {
            synchronized (this.lock) {
                if (len != 0) {
                    Object[] temp = new Object[len];
                    int newlen = 0;
                    for (Object element : getArray()) {
                        if (c.contains(element)) {
                            temp[newlen] = element;
                            newlen++;
                        }
                    }
                    if (newlen != len) {
                        setArray(Arrays.copyOf((T[]) temp, newlen));
                        return true;
                    }
                }
            }
        } else {
            throw new NullPointerException();
        }
    }

    public int addAllAbsent(Collection<? extends E> c) {
        int added;
        Object[] cs = c.toArray();
        if (cs.length == 0) {
            return 0;
        }
        synchronized (this.lock) {
            Object[] elements2 = getArray();
            int len = elements2.length;
            added = 0;
            for (Object e : cs) {
                if (indexOf(e, elements2, 0, len) < 0 && indexOf(e, cs, 0, added) < 0) {
                    cs[added] = e;
                    added++;
                }
            }
            if (added > 0) {
                Object[] newElements = Arrays.copyOf((T[]) elements2, len + added);
                System.arraycopy((Object) cs, 0, (Object) newElements, len, added);
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
        Object[] cs = c.getClass() == CopyOnWriteArrayList.class ? ((CopyOnWriteArrayList) c).getArray() : c.toArray();
        if (cs.length == 0) {
            return false;
        }
        synchronized (this.lock) {
            Object[] elements2 = getArray();
            int len = elements2.length;
            if (len == 0 && cs.getClass() == Object[].class) {
                setArray(cs);
            } else {
                Object[] newElements = Arrays.copyOf((T[]) elements2, cs.length + len);
                System.arraycopy((Object) cs, 0, (Object) newElements, len, cs.length);
                setArray(newElements);
            }
        }
        return true;
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        Object[] newElements;
        Object[] cs = c.toArray();
        synchronized (this.lock) {
            Object[] elements2 = getArray();
            int len = elements2.length;
            if (index > len || index < 0) {
                throw new IndexOutOfBoundsException(outOfBounds(index, len));
            } else if (cs.length == 0) {
                return false;
            } else {
                int numMoved = len - index;
                if (numMoved == 0) {
                    newElements = Arrays.copyOf((T[]) elements2, cs.length + len);
                } else {
                    newElements = new Object[(cs.length + len)];
                    System.arraycopy((Object) elements2, 0, (Object) newElements, 0, index);
                    System.arraycopy((Object) elements2, index, (Object) newElements, cs.length + index, numMoved);
                }
                System.arraycopy((Object) cs, 0, (Object) newElements, index, cs.length);
                setArray(newElements);
                return true;
            }
        }
    }

    public void forEach(Consumer<? super E> action) {
        if (action != null) {
            for (E x : getArray()) {
                action.accept(x);
            }
            return;
        }
        throw new NullPointerException();
    }

    public boolean removeIf(Predicate<? super E> filter) {
        Object[] objArr;
        if (filter != null) {
            synchronized (this.lock) {
                E[] elements2 = getArray();
                int len = elements2.length;
                for (int i = 0; i < len; i++) {
                    if (filter.test(elements2[i])) {
                        int newlen = i;
                        Object[] newElements = new Object[(len - 1)];
                        System.arraycopy((Object) elements2, 0, (Object) newElements, 0, newlen);
                        for (int i2 = i + 1; i2 < len; i2++) {
                            E x = elements2[i2];
                            if (!filter.test(x)) {
                                newElements[newlen] = x;
                                newlen++;
                            }
                        }
                        if (newlen == len - 1) {
                            objArr = newElements;
                        } else {
                            objArr = Arrays.copyOf((T[]) newElements, newlen);
                        }
                        setArray(objArr);
                        return true;
                    }
                }
                return false;
            }
        }
        throw new NullPointerException();
    }

    public void replaceAll(UnaryOperator<E> operator) {
        if (operator != null) {
            synchronized (this.lock) {
                E[] elements2 = getArray();
                int len = elements2.length;
                Object[] newElements = Arrays.copyOf((T[]) elements2, len);
                for (int i = 0; i < len; i++) {
                    newElements[i] = operator.apply(elements2[i]);
                }
                setArray(newElements);
            }
            return;
        }
        throw new NullPointerException();
    }

    public void sort(Comparator<? super E> c) {
        synchronized (this.lock) {
            Object[] elements2 = getArray();
            Object[] newElements = Arrays.copyOf((T[]) elements2, elements2.length);
            Arrays.sort(newElements, c);
            setArray(newElements);
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        Object[] elements2 = getArray();
        s.writeInt(elements2.length);
        for (Object element : elements2) {
            s.writeObject(element);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        resetLock();
        int len = s.readInt();
        Object[] elements2 = new Object[len];
        for (int i = 0; i < len; i++) {
            elements2[i] = s.readObject();
        }
        setArray(elements2);
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
        for (Object equals : getArray()) {
            if (!it.hasNext() || !Objects.equals(equals, it.next())) {
                return false;
            }
        }
        if (it.hasNext() != 0) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        Object[] array = getArray();
        int length = array.length;
        int hashCode = 1;
        for (int hashCode2 = 0; hashCode2 < length; hashCode2++) {
            Object x = array[hashCode2];
            hashCode = (31 * hashCode) + (x == null ? 0 : x.hashCode());
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
        Object[] elements2 = getArray();
        int len = elements2.length;
        if (index >= 0 && index <= len) {
            return new COWIterator(elements2, index);
        }
        throw new IndexOutOfBoundsException(outOfBounds(index, len));
    }

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(getArray(), 1040);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        COWSubList cOWSubList;
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
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }
}
