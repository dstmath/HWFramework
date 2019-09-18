package java.util;

public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
    protected transient int modCount = 0;

    private class Itr implements Iterator<E> {
        int cursor;
        int expectedModCount;
        int lastRet;

        private Itr() {
            this.cursor = 0;
            this.lastRet = -1;
            this.expectedModCount = AbstractList.this.modCount;
        }

        public boolean hasNext() {
            return this.cursor != AbstractList.this.size();
        }

        public E next() {
            checkForComodification();
            try {
                int i = this.cursor;
                E next = AbstractList.this.get(i);
                this.lastRet = i;
                this.cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (this.lastRet >= 0) {
                checkForComodification();
                try {
                    AbstractList.this.remove(this.lastRet);
                    if (this.lastRet < this.cursor) {
                        this.cursor--;
                    }
                    this.lastRet = -1;
                    this.expectedModCount = AbstractList.this.modCount;
                } catch (IndexOutOfBoundsException e) {
                    throw new ConcurrentModificationException();
                }
            } else {
                throw new IllegalStateException();
            }
        }

        /* access modifiers changed from: package-private */
        public final void checkForComodification() {
            if (AbstractList.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private class ListItr extends AbstractList<E>.Itr implements ListIterator<E> {
        ListItr(int index) {
            super();
            this.cursor = index;
        }

        public boolean hasPrevious() {
            return this.cursor != 0;
        }

        public E previous() {
            checkForComodification();
            try {
                int i = this.cursor - 1;
                E previous = AbstractList.this.get(i);
                this.cursor = i;
                this.lastRet = i;
                return previous;
            } catch (IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            return this.cursor;
        }

        public int previousIndex() {
            return this.cursor - 1;
        }

        public void set(E e) {
            if (this.lastRet >= 0) {
                checkForComodification();
                try {
                    AbstractList.this.set(this.lastRet, e);
                    this.expectedModCount = AbstractList.this.modCount;
                } catch (IndexOutOfBoundsException e2) {
                    throw new ConcurrentModificationException();
                }
            } else {
                throw new IllegalStateException();
            }
        }

        public void add(E e) {
            checkForComodification();
            try {
                int i = this.cursor;
                AbstractList.this.add(i, e);
                this.lastRet = -1;
                this.cursor = i + 1;
                this.expectedModCount = AbstractList.this.modCount;
            } catch (IndexOutOfBoundsException e2) {
                throw new ConcurrentModificationException();
            }
        }
    }

    public abstract E get(int i);

    protected AbstractList() {
    }

    public boolean add(E e) {
        add(size(), e);
        return true;
    }

    public E set(int index, E e) {
        throw new UnsupportedOperationException();
    }

    public void add(int index, E e) {
        throw new UnsupportedOperationException();
    }

    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    public int indexOf(Object o) {
        ListIterator<E> it = listIterator();
        if (o == null) {
            while (it.hasNext()) {
                if (it.next() == null) {
                    return it.previousIndex();
                }
            }
        } else {
            while (it.hasNext()) {
                if (o.equals(it.next())) {
                    return it.previousIndex();
                }
            }
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        ListIterator<E> it = listIterator(size());
        if (o == null) {
            while (it.hasPrevious()) {
                if (it.previous() == null) {
                    return it.nextIndex();
                }
            }
        } else {
            while (it.hasPrevious()) {
                if (o.equals(it.previous())) {
                    return it.nextIndex();
                }
            }
        }
        return -1;
    }

    public void clear() {
        removeRange(0, size());
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);
        boolean modified = false;
        for (E e : c) {
            add(index, e);
            modified = true;
            index++;
        }
        return modified;
    }

    public Iterator<E> iterator() {
        return new Itr();
    }

    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    public ListIterator<E> listIterator(int index) {
        rangeCheckForAdd(index);
        return new ListItr(index);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        if (this instanceof RandomAccess) {
            return new RandomAccessSubList(this, fromIndex, toIndex);
        }
        return new SubList(this, fromIndex, toIndex);
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }
        ListIterator<E> e1 = listIterator();
        ListIterator<?> e2 = ((List) o).listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            E o1 = e1.next();
            Object o2 = e2.next();
            if (o1 == null) {
                if (o2 == null) {
                }
            } else if (!o1.equals(o2)) {
            }
            return false;
        }
        if (e1.hasNext() || e2.hasNext()) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int hashCode = 1;
        Iterator it = iterator();
        while (it.hasNext()) {
            E e = it.next();
            hashCode = (31 * hashCode) + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    /* access modifiers changed from: protected */
    public void removeRange(int fromIndex, int toIndex) {
        ListIterator<E> it = listIterator(fromIndex);
        int n = toIndex - fromIndex;
        for (int i = 0; i < n; i++) {
            it.next();
            it.remove();
        }
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size();
    }
}
