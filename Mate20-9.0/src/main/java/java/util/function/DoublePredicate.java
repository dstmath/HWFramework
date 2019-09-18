package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface DoublePredicate {
    boolean test(double d);

    DoublePredicate and(DoublePredicate other) {
        Objects.requireNonNull(other);
        return new DoublePredicate(other) {
            private final /* synthetic */ DoublePredicate f$1;

            {
                this.f$1 = r2;
            }

            public final boolean test(double d) {
                return DoublePredicate.lambda$and$0(DoublePredicate.this, this.f$1, d);
            }
        };
    }

    static /* synthetic */ boolean lambda$and$0(DoublePredicate doublePredicate, DoublePredicate other, double value) {
        return doublePredicate.test(value) && other.test(value);
    }

    static /* synthetic */ boolean lambda$negate$1(DoublePredicate doublePredicate, double value) {
        return !doublePredicate.test(value);
    }

    DoublePredicate negate() {
        return new DoublePredicate() {
            public final boolean test(double d) {
                return DoublePredicate.lambda$negate$1(DoublePredicate.this, d);
            }
        };
    }

    DoublePredicate or(DoublePredicate other) {
        Objects.requireNonNull(other);
        return new DoublePredicate(other) {
            private final /* synthetic */ DoublePredicate f$1;

            {
                this.f$1 = r2;
            }

            public final boolean test(double d) {
                return DoublePredicate.lambda$or$2(DoublePredicate.this, this.f$1, d);
            }
        };
    }

    static /* synthetic */ boolean lambda$or$2(DoublePredicate doublePredicate, DoublePredicate other, double value) {
        return doublePredicate.test(value) || other.test(value);
    }
}
