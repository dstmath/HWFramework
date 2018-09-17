package java.util;

public abstract class AbstractQueue<E> extends AbstractCollection<E> implements Queue<E> {
    protected AbstractQueue() {
    }

    public boolean add(E e) {
        if (offer(e)) {
            return true;
        }
        throw new IllegalStateException("Queue full");
    }

    public E remove() {
        E x = poll();
        if (x != null) {
            return x;
        }
        throw new NoSuchElementException();
    }

    public E element() {
        E x = peek();
        if (x != null) {
            return x;
        }
        throw new NoSuchElementException();
    }

    public void clear() {
        do {
        } while (poll() != null);
    }

    public boolean addAll(Collection<? extends E> c) {
        if (c == null) {
            throw new NullPointerException();
        } else if (c == this) {
            throw new IllegalArgumentException();
        } else {
            boolean modified = false;
            for (E e : c) {
                if (add(e)) {
                    modified = true;
                }
            }
            return modified;
        }
    }
}
