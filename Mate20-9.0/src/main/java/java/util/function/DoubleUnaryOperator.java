package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface DoubleUnaryOperator {
    double applyAsDouble(double d);

    DoubleUnaryOperator compose(DoubleUnaryOperator before) {
        Objects.requireNonNull(before);
        return new DoubleUnaryOperator(before) {
            private final /* synthetic */ DoubleUnaryOperator f$1;

            {
                this.f$1 = r2;
            }

            public final double applyAsDouble(double d) {
                return DoubleUnaryOperator.this.applyAsDouble(this.f$1.applyAsDouble(d));
            }
        };
    }

    DoubleUnaryOperator andThen(DoubleUnaryOperator after) {
        Objects.requireNonNull(after);
        return new DoubleUnaryOperator(after) {
            private final /* synthetic */ DoubleUnaryOperator f$1;

            {
                this.f$1 = r2;
            }

            public final double applyAsDouble(double d) {
                return this.f$1.applyAsDouble(DoubleUnaryOperator.this.applyAsDouble(d));
            }
        };
    }

    static DoubleUnaryOperator identity() {
        return $$Lambda$DoubleUnaryOperator$i7wtM_8OusCB32HCfZ4usZ4zaQ.INSTANCE;
    }

    static /* synthetic */ double lambda$identity$2(double t) {
        return t;
    }
}
