package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface LongConsumer {
    void accept(long j);

    LongConsumer andThen(LongConsumer after) {
        Objects.requireNonNull(after);
        return new -$Lambda$53LHM5ipFEm8sLT6IDMWnoxlVfg(this, after);
    }

    /* synthetic */ void lambda$-java_util_function_LongConsumer_2696(LongConsumer after, long t) {
        accept(t);
        after.accept(t);
    }
}
