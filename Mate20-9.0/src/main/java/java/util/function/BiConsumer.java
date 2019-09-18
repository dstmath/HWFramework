package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiConsumer<T, U> {
    void accept(T t, U u);

    BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return new BiConsumer(after) {
            private final /* synthetic */ BiConsumer f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj, Object obj2) {
                BiConsumer.lambda$andThen$0(BiConsumer.this, this.f$1, obj, obj2);
            }
        };
    }

    static /* synthetic */ void lambda$andThen$0(BiConsumer biConsumer, BiConsumer after, Object l, Object r) {
        biConsumer.accept(l, r);
        after.accept(l, r);
    }
}
