package java.util.stream;

import java.util.Iterator;
import java.util.Spliterator;

public interface BaseStream<T, S extends BaseStream<T, S>> extends AutoCloseable {
    void close();

    boolean isParallel();

    Iterator<T> iterator();

    S onClose(Runnable runnable);

    S parallel();

    S sequential();

    Spliterator<T> spliterator();

    S unordered();
}
