package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntUnaryOperator {
    int applyAsInt(int i);

    IntUnaryOperator compose(IntUnaryOperator before) {
        Objects.requireNonNull(before);
        return new IntUnaryOperator(before) {
            private final /* synthetic */ IntUnaryOperator f$1;

            {
                this.f$1 = r2;
            }

            public final int applyAsInt(int i) {
                return IntUnaryOperator.this.applyAsInt(this.f$1.applyAsInt(i));
            }
        };
    }

    IntUnaryOperator andThen(IntUnaryOperator after) {
        Objects.requireNonNull(after);
        return new IntUnaryOperator(after) {
            private final /* synthetic */ IntUnaryOperator f$1;

            {
                this.f$1 = r2;
            }

            public final int applyAsInt(int i) {
                return this.f$1.applyAsInt(IntUnaryOperator.this.applyAsInt(i));
            }
        };
    }

    static IntUnaryOperator identity() {
        return $$Lambda$IntUnaryOperator$DKxG0oyUAYjk17nXTQ5xEXFgU.INSTANCE;
    }

    static /* synthetic */ int lambda$identity$2(int t) {
        return t;
    }
}
