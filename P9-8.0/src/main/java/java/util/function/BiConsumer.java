package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiConsumer<T, U> {
    void accept(T t, U u);

    BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return new -$Lambda$DtNZ_81o0FmX_vCDZwj1sq5RMmA(this, after);
    }

    /* synthetic */ void lambda$-java_util_function_BiConsumer_2859(BiConsumer after, Object l, Object r) {
        accept(l, r);
        after.accept(l, r);
    }
}
