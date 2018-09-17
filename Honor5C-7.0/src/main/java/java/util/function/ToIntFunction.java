package java.util.function;

@FunctionalInterface
public interface ToIntFunction<T> {
    int applyAsInt(T t);
}
