package java.util;

public interface SortedSet<E> extends Set<E> {

    /* renamed from: java.util.SortedSet.1 */
    class AnonymousClass1 extends IteratorSpliterator<E> {
        AnonymousClass1(Collection $anonymous0, int $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public Comparator<? super E> getComparator() {
            return SortedSet.this.comparator();
        }
    }

    Comparator<? super E> comparator();

    E first();

    SortedSet<E> headSet(E e);

    E last();

    SortedSet<E> subSet(E e, E e2);

    SortedSet<E> tailSet(E e);

    Spliterator<E> spliterator() {
        return new AnonymousClass1(this, 21);
    }
}
