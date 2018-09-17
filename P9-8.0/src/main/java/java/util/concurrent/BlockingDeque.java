package java.util.concurrent;

import java.util.Deque;
import java.util.Iterator;

public interface BlockingDeque<E> extends BlockingQueue<E>, Deque<E> {
    boolean add(E e);

    void addFirst(E e);

    void addLast(E e);

    boolean contains(Object obj);

    E element();

    Iterator<E> iterator();

    boolean offer(E e);

    boolean offer(E e, long j, TimeUnit timeUnit) throws InterruptedException;

    boolean offerFirst(E e);

    boolean offerFirst(E e, long j, TimeUnit timeUnit) throws InterruptedException;

    boolean offerLast(E e);

    boolean offerLast(E e, long j, TimeUnit timeUnit) throws InterruptedException;

    E peek();

    E poll();

    E poll(long j, TimeUnit timeUnit) throws InterruptedException;

    E pollFirst(long j, TimeUnit timeUnit) throws InterruptedException;

    E pollLast(long j, TimeUnit timeUnit) throws InterruptedException;

    void push(E e);

    void put(E e) throws InterruptedException;

    void putFirst(E e) throws InterruptedException;

    void putLast(E e) throws InterruptedException;

    E remove();

    boolean remove(Object obj);

    boolean removeFirstOccurrence(Object obj);

    boolean removeLastOccurrence(Object obj);

    int size();

    E take() throws InterruptedException;

    E takeFirst() throws InterruptedException;

    E takeLast() throws InterruptedException;
}
