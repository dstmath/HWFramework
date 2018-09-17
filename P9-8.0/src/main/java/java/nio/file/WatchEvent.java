package java.nio.file;

public interface WatchEvent<T> {

    public interface Modifier {
        String name();
    }

    public interface Kind<T> {
        String name();

        Class<T> type();
    }

    T context();

    int count();

    Kind<T> kind();
}
