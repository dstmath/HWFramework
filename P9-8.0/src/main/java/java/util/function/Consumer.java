package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);

    Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return new -$Lambda$1p5Vyyh8oouwyHrSoM2AUzgQc18(this, after);
    }

    /* synthetic */ void lambda$-java_util_function_Consumer_2620(Consumer after, Object t) {
        accept(t);
        after.accept(t);
    }
}
