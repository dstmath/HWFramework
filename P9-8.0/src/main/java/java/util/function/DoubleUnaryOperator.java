package java.util.function;

import java.util.Objects;
import java.util.function.-$Lambda$VGDeaUHZQIZywZW2ttlyhwk3Cmk.AnonymousClass1;
import java.util.function.-$Lambda$VGDeaUHZQIZywZW2ttlyhwk3Cmk.AnonymousClass2;

@FunctionalInterface
public interface DoubleUnaryOperator {
    double applyAsDouble(double d);

    DoubleUnaryOperator compose(DoubleUnaryOperator before) {
        Objects.requireNonNull(before);
        return new AnonymousClass2(this, before);
    }

    /* synthetic */ double lambda$-java_util_function_DoubleUnaryOperator_2626(DoubleUnaryOperator before, double v) {
        return applyAsDouble(before.applyAsDouble(v));
    }

    DoubleUnaryOperator andThen(DoubleUnaryOperator after) {
        Objects.requireNonNull(after);
        return new AnonymousClass1(this, after);
    }

    /* synthetic */ double lambda$-java_util_function_DoubleUnaryOperator_3397(DoubleUnaryOperator after, double t) {
        return after.applyAsDouble(applyAsDouble(t));
    }

    static DoubleUnaryOperator identity() {
        return new -$Lambda$VGDeaUHZQIZywZW2ttlyhwk3Cmk();
    }

    static /* synthetic */ double lambda$-java_util_function_DoubleUnaryOperator_3682(double t) {
        return t;
    }
}
