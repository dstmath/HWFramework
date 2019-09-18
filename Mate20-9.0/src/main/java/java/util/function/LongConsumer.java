package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface LongConsumer {
    void accept(long j);

    LongConsumer andThen(LongConsumer after) {
        Objects.requireNonNull(after);
        return new LongConsumer(after) {
            private final /* synthetic */ LongConsumer f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(long j) {
                LongConsumer.lambda$andThen$0(LongConsumer.this, this.f$1, j);
            }
        };
    }

    static /* synthetic */ void lambda$andThen$0(LongConsumer longConsumer, LongConsumer after, long t) {
        longConsumer.accept(t);
        after.accept(t);
    }
}
