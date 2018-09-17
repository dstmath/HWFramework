package java.util;

public abstract class AbstractSequentialList<E> extends AbstractList<E> {
    public abstract ListIterator<E> listIterator(int i);

    protected AbstractSequentialList() {
    }

    public E get(int index) {
        try {
            return listIterator(index).next();
        } catch (NoSuchElementException e) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    public E set(int index, E element) {
        try {
            ListIterator<E> e = listIterator(index);
            E oldVal = e.next();
            e.set(element);
            return oldVal;
        } catch (NoSuchElementException e2) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    public void add(int index, E element) {
        try {
            listIterator(index).add(element);
        } catch (NoSuchElementException e) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    public E remove(int index) {
        try {
            ListIterator<E> e = listIterator(index);
            E outCast = e.next();
            e.remove();
            return outCast;
        } catch (NoSuchElementException e2) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        boolean modified = false;
        try {
            ListIterator<E> e1 = listIterator(index);
            for (Object add : c) {
                e1.add(add);
                modified = true;
            }
            return modified;
        } catch (NoSuchElementException e) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
    }

    public Iterator<E> iterator() {
        return listIterator();
    }
}
