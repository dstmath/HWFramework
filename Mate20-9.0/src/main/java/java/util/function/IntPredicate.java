package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntPredicate {
    boolean test(int i);

    IntPredicate and(IntPredicate other) {
        Objects.requireNonNull(other);
        return new IntPredicate(other) {
            private final /* synthetic */ IntPredicate f$1;

            {
                this.f$1 = r2;
            }

            public final boolean test(int i) {
                return IntPredicate.lambda$and$0(IntPredicate.this, this.f$1, i);
            }
        };
    }

    static /* synthetic */ boolean lambda$and$0(IntPredicate intPredicate, IntPredicate other, int value) {
        return intPredicate.test(value) && other.test(value);
    }

    static /* synthetic */ boolean lambda$negate$1(IntPredicate intPredicate, int value) {
        return !intPredicate.test(value);
    }

    IntPredicate negate() {
        return new IntPredicate() {
            public final boolean test(int i) {
                return IntPredicate.lambda$negate$1(IntPredicate.this, i);
            }
        };
    }

    IntPredicate or(IntPredicate other) {
        Objects.requireNonNull(other);
        return new IntPredicate(other) {
            private final /* synthetic */ IntPredicate f$1;

            {
                this.f$1 = r2;
            }

            public final boolean test(int i) {
                return IntPredicate.lambda$or$2(IntPredicate.this, this.f$1, i);
            }
        };
    }

    static /* synthetic */ boolean lambda$or$2(IntPredicate intPredicate, IntPredicate other, int value) {
        return intPredicate.test(value) || other.test(value);
    }
}
