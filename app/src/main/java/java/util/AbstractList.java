package java.util;

public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
    protected transient int modCount;

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
            if (this.lastRet < 0) {
                throw new IllegalStateException();
            }
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
        }

        final void checkForComodification() {
            if (AbstractList.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private class ListItr extends Itr implements ListIterator<E> {
        ListItr(int index) {
            super(null);
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
            if (this.lastRet < 0) {
                throw new IllegalStateException();
            }
            checkForComodification();
            try {
                AbstractList.this.set(this.lastRet, e);
                this.expectedModCount = AbstractList.this.modCount;
            } catch (IndexOutOfBoundsException e2) {
                throw new ConcurrentModificationException();
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
        this.modCount = 0;
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
        }
        while (it.hasNext()) {
            if (o.equals(it.next())) {
                return it.previousIndex();
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
        }
        while (it.hasPrevious()) {
            if (o.equals(it.previous())) {
                return it.nextIndex();
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
            int index2 = index + 1;
            add(index, e);
            modified = true;
            index = index2;
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
        ListIterator e2 = ((List) o).listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            boolean equals;
            E o1 = e1.next();
            Object o2 = e2.next();
            if (o1 != null) {
                equals = o1.equals(o2);
                continue;
            } else if (o2 == null) {
                equals = true;
                continue;
            } else {
                equals = false;
                continue;
            }
            if (!equals) {
                return false;
            }
        }
        if (e1.hasNext() || e2.hasNext()) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int hashCode = 1;
        for (E e : this) {
            hashCode = (hashCode * 31) + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }

    protected void removeRange(int fromIndex, int toIndex) {
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
