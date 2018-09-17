package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface DoubleConsumer {
    void accept(double d);

    DoubleConsumer andThen(DoubleConsumer after) {
        Objects.requireNonNull(after);
        return new -$Lambda$Tr2ZVotyK7bvB1MALZ-WioewFls(this, after);
    }

    /* synthetic */ void lambda$-java_util_function_DoubleConsumer_2716(DoubleConsumer after, double t) {
        accept(t);
        after.accept(t);
    }
}
