package java.util;

import java.util.function.UnaryOperator;

public interface List<E> extends Collection<E> {
    void add(int i, E e);

    boolean add(E e);

    boolean addAll(int i, Collection<? extends E> collection);

    boolean addAll(Collection<? extends E> collection);

    void clear();

    boolean contains(Object obj);

    boolean containsAll(Collection<?> collection);

    boolean equals(Object obj);

    E get(int i);

    int hashCode();

    int indexOf(Object obj);

    boolean isEmpty();

    Iterator<E> iterator();

    int lastIndexOf(Object obj);

    ListIterator<E> listIterator();

    ListIterator<E> listIterator(int i);

    E remove(int i);

    boolean remove(Object obj);

    boolean removeAll(Collection<?> collection);

    boolean retainAll(Collection<?> collection);

    E set(int i, E e);

    int size();

    List<E> subList(int i, int i2);

    Object[] toArray();

    <T> T[] toArray(T[] tArr);

    void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        ListIterator<E> li = listIterator();
        while (li.hasNext()) {
            li.set(operator.apply(li.next()));
        }
    }

    void sort(Comparator<? super E> c) {
        Object[] a = toArray();
        Arrays.sort(a, c);
        ListIterator<E> i = listIterator();
        for (Object e : a) {
            i.next();
            i.set(e);
        }
    }

    Spliterator<E> spliterator() {
        return Spliterators.spliterator((Collection) this, 16);
    }
}
