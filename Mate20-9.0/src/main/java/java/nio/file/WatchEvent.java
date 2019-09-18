package java.nio.file;

public interface WatchEvent<T> {

    public interface Kind<T> {
        String name();

        Class<T> type();
    }

    public interface Modifier {
        String name();
    }

    T context();

    int count();

    Kind<T> kind();
}
