package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);

    Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return new Consumer(after) {
            private final /* synthetic */ Consumer f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                Consumer.lambda$andThen$0(Consumer.this, this.f$1, obj);
            }
        };
    }

    static /* synthetic */ void lambda$andThen$0(Consumer consumer, Consumer after, Object t) {
        consumer.accept(t);
        after.accept(t);
    }
}
