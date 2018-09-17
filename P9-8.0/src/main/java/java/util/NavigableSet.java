package java.util;

public interface NavigableSet<E> extends SortedSet<E> {
    E ceiling(E e);

    Iterator<E> descendingIterator();

    NavigableSet<E> descendingSet();

    E floor(E e);

    NavigableSet<E> headSet(E e, boolean z);

    SortedSet<E> headSet(E e);

    E higher(E e);

    Iterator<E> iterator();

    E lower(E e);

    E pollFirst();

    E pollLast();

    NavigableSet<E> subSet(E e, boolean z, E e2, boolean z2);

    SortedSet<E> subSet(E e, E e2);

    NavigableSet<E> tailSet(E e, boolean z);

    SortedSet<E> tailSet(E e);
}
