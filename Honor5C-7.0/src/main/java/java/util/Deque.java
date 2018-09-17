package java.util;

public interface Deque<E> extends Queue<E> {
    boolean add(E e);

    void addFirst(E e);

    void addLast(E e);

    boolean contains(Object obj);

    Iterator<E> descendingIterator();

    E element();

    E getFirst();

    E getLast();

    Iterator<E> iterator();

    boolean offer(E e);

    boolean offerFirst(E e);

    boolean offerLast(E e);

    E peek();

    E peekFirst();

    E peekLast();

    E poll();

    E pollFirst();

    E pollLast();

    E pop();

    void push(E e);

    E remove();

    boolean remove(Object obj);

    E removeFirst();

    boolean removeFirstOccurrence(Object obj);

    E removeLast();

    boolean removeLastOccurrence(Object obj);

    int size();
}
