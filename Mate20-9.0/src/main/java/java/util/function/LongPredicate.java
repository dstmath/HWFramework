package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface LongPredicate {
    boolean test(long j);

    LongPredicate and(LongPredicate other) {
        Objects.requireNonNull(other);
        return new LongPredicate(other) {
            private final /* synthetic */ LongPredicate f$1;

            {
                this.f$1 = r2;
            }

            public final boolean test(long j) {
                return LongPredicate.lambda$and$0(LongPredicate.this, this.f$1, j);
            }
        };
    }

    static /* synthetic */ boolean lambda$and$0(LongPredicate longPredicate, LongPredicate other, long value) {
        return longPredicate.test(value) && other.test(value);
    }

    static /* synthetic */ boolean lambda$negate$1(LongPredicate longPredicate, long value) {
        return !longPredicate.test(value);
    }

    LongPredicate negate() {
        return new LongPredicate() {
            public final boolean test(long j) {
                return LongPredicate.lambda$negate$1(LongPredicate.this, j);
            }
        };
    }

    LongPredicate or(LongPredicate other) {
        Objects.requireNonNull(other);
        return new LongPredicate(other) {
            private final /* synthetic */ LongPredicate f$1;

            {
                this.f$1 = r2;
            }

            public final boolean test(long j) {
                return LongPredicate.lambda$or$2(LongPredicate.this, this.f$1, j);
            }
        };
    }

    static /* synthetic */ boolean lambda$or$2(LongPredicate longPredicate, LongPredicate other, long value) {
        return longPredicate.test(value) || other.test(value);
    }
}
