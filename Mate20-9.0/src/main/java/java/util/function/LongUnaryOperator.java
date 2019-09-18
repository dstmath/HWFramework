package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface LongUnaryOperator {
    long applyAsLong(long j);

    LongUnaryOperator compose(LongUnaryOperator before) {
        Objects.requireNonNull(before);
        return new LongUnaryOperator(before) {
            private final /* synthetic */ LongUnaryOperator f$1;

            {
                this.f$1 = r2;
            }

            public final long applyAsLong(long j) {
                return LongUnaryOperator.this.applyAsLong(this.f$1.applyAsLong(j));
            }
        };
    }

    LongUnaryOperator andThen(LongUnaryOperator after) {
        Objects.requireNonNull(after);
        return new LongUnaryOperator(after) {
            private final /* synthetic */ LongUnaryOperator f$1;

            {
                this.f$1 = r2;
            }

            public final long applyAsLong(long j) {
                return this.f$1.applyAsLong(LongUnaryOperator.this.applyAsLong(j));
            }
        };
    }

    static LongUnaryOperator identity() {
        return $$Lambda$LongUnaryOperator$kI3lBaNH3h6ldTmGeiEUd61CYJI.INSTANCE;
    }

    static /* synthetic */ long lambda$identity$2(long t) {
        return t;
    }
}
