package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface DoubleConsumer {
    void accept(double d);

    DoubleConsumer andThen(DoubleConsumer after) {
        Objects.requireNonNull(after);
        return new DoubleConsumer(after) {
            private final /* synthetic */ DoubleConsumer f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(double d) {
                DoubleConsumer.lambda$andThen$0(DoubleConsumer.this, this.f$1, d);
            }
        };
    }

    static /* synthetic */ void lambda$andThen$0(DoubleConsumer doubleConsumer, DoubleConsumer after, double t) {
        doubleConsumer.accept(t);
        after.accept(t);
    }
}
