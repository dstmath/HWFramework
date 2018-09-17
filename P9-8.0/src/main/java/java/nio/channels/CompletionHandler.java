package java.nio.channels;

public interface CompletionHandler<V, A> {
    void completed(V v, A a);

    void failed(Throwable th, A a);
}
