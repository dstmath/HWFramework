package java.util.function;

@FunctionalInterface
public interface ToDoubleBiFunction<T, U> {
    double applyAsDouble(T t, U u);
}
