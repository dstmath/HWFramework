package java.io;

public interface Closeable extends AutoCloseable {
    void close() throws IOException;
}
