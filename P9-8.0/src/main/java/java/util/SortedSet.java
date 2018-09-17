package java.util;

public interface SortedSet<E> extends Set<E> {
    Comparator<? super E> comparator();

    E first();

    SortedSet<E> headSet(E e);

    E last();

    SortedSet<E> subSet(E e, E e2);

    SortedSet<E> tailSet(E e);

    Spliterator<E> spliterator() {
        return new IteratorSpliterator<E>(this, 21) {
            public Comparator<? super E> getComparator() {
                return SortedSet.this.comparator();
            }
        };
    }
}
