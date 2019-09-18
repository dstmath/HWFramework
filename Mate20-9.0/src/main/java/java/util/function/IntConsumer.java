package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntConsumer {
    void accept(int i);

    IntConsumer andThen(IntConsumer after) {
        Objects.requireNonNull(after);
        return new IntConsumer(after) {
            private final /* synthetic */ IntConsumer f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(int i) {
                IntConsumer.lambda$andThen$0(IntConsumer.this, this.f$1, i);
            }
        };
    }

    static /* synthetic */ void lambda$andThen$0(IntConsumer intConsumer, IntConsumer after, int t) {
        intConsumer.accept(t);
        after.accept(t);
    }
}
