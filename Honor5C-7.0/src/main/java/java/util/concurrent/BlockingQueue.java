package java.util.concurrent;

import java.util.Collection;
import java.util.Queue;

public interface BlockingQueue<E> extends Queue<E> {
    boolean add(E e);

    boolean contains(Object obj);

    int drainTo(Collection<? super E> collection);

    int drainTo(Collection<? super E> collection, int i);

    boolean offer(E e);

    boolean offer(E e, long j, TimeUnit timeUnit) throws InterruptedException;

    E poll(long j, TimeUnit timeUnit) throws InterruptedException;

    void put(E e) throws InterruptedException;

    int remainingCapacity();

    boolean remove(Object obj);

    E take() throws InterruptedException;
}
