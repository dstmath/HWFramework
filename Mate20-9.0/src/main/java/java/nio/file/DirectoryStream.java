package java.nio.file;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

public interface DirectoryStream<T> extends Closeable, Iterable<T> {

    @FunctionalInterface
    public interface Filter<T> {
        boolean accept(T t) throws IOException;
    }

    Iterator<T> iterator();
}
