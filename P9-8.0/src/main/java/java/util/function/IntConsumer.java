package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntConsumer {
    void accept(int i);

    IntConsumer andThen(IntConsumer after) {
        Objects.requireNonNull(after);
        return new -$Lambda$FmHaHrzLqKsEWotEYE8x1Clp-KE(this, after);
    }

    /* synthetic */ void lambda$-java_util_function_IntConsumer_2686(IntConsumer after, int t) {
        accept(t);
        after.accept(t);
    }
}
