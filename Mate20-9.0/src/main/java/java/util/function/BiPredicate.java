package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiPredicate<T, U> {
    boolean test(T t, U u);

    BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return new BiPredicate(other) {
            private final /* synthetic */ BiPredicate f$1;

            {
                this.f$1 = r2;
            }

            public final boolean test(Object obj, Object obj2) {
                return BiPredicate.lambda$and$0(BiPredicate.this, this.f$1, obj, obj2);
            }
        };
    }

    static /* synthetic */ boolean lambda$and$0(BiPredicate biPredicate, BiPredicate other, Object t, Object u) {
        return biPredicate.test(t, u) && other.test(t, u);
    }

    static /* synthetic */ boolean lambda$negate$1(BiPredicate biPredicate, Object t, Object u) {
        return !biPredicate.test(t, u);
    }

    BiPredicate<T, U> negate() {
        return new BiPredicate() {
            public final boolean test(Object obj, Object obj2) {
                return BiPredicate.lambda$negate$1(BiPredicate.this, obj, obj2);
            }
        };
    }

    BiPredicate<T, U> or(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return new BiPredicate(other) {
            private final /* synthetic */ BiPredicate f$1;

            {
                this.f$1 = r2;
            }

            public final boolean test(Object obj, Object obj2) {
                return BiPredicate.lambda$or$2(BiPredicate.this, this.f$1, obj, obj2);
            }
        };
    }

    static /* synthetic */ boolean lambda$or$2(BiPredicate biPredicate, BiPredicate other, Object t, Object u) {
        return biPredicate.test(t, u) || other.test(t, u);
    }
}
