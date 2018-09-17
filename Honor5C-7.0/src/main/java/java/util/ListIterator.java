package java.util;

public interface ListIterator<E> extends Iterator<E> {
    void add(E e);

    boolean hasNext();

    boolean hasPrevious();

    E next();

    int nextIndex();

    E previous();

    int previousIndex();

    void remove();

    void set(E e);
}
