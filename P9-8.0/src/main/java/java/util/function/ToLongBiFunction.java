package java.util.function;

@FunctionalInterface
public interface ToLongBiFunction<T, U> {
    long applyAsLong(T t, U u);
}
