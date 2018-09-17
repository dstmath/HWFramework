package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface DoubleUnaryOperator {

    final /* synthetic */ class -java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0 implements DoubleUnaryOperator {
        private /* synthetic */ DoubleUnaryOperator val$after;
        private /* synthetic */ DoubleUnaryOperator val$this;

        public /* synthetic */ -java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0(DoubleUnaryOperator doubleUnaryOperator, DoubleUnaryOperator doubleUnaryOperator2) {
            this.val$this = doubleUnaryOperator;
            this.val$after = doubleUnaryOperator2;
        }

        public double applyAsDouble(double arg0) {
            return this.val$this.-java_util_function_DoubleUnaryOperator_lambda$2(this.val$after, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0 implements DoubleUnaryOperator {
        private /* synthetic */ DoubleUnaryOperator val$before;
        private /* synthetic */ DoubleUnaryOperator val$this;

        public /* synthetic */ -java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0(DoubleUnaryOperator doubleUnaryOperator, DoubleUnaryOperator doubleUnaryOperator2) {
            this.val$this = doubleUnaryOperator;
            this.val$before = doubleUnaryOperator2;
        }

        public double applyAsDouble(double arg0) {
            return this.val$this.-java_util_function_DoubleUnaryOperator_lambda$1(this.val$before, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_DoubleUnaryOperator_identity__LambdaImpl0 implements DoubleUnaryOperator {
        public double applyAsDouble(double arg0) {
            return DoubleUnaryOperator.-java_util_function_DoubleUnaryOperator_lambda$3(arg0);
        }
    }

    double applyAsDouble(double d);

    DoubleUnaryOperator compose(DoubleUnaryOperator before) {
        Objects.requireNonNull(before);
        return new -java_util_function_DoubleUnaryOperator_compose_java_util_function_DoubleUnaryOperator_before_LambdaImpl0(this, before);
    }

    /* synthetic */ double -java_util_function_DoubleUnaryOperator_lambda$1(DoubleUnaryOperator before, double v) {
        return applyAsDouble(before.applyAsDouble(v));
    }

    DoubleUnaryOperator andThen(DoubleUnaryOperator after) {
        Objects.requireNonNull(after);
        return new -java_util_function_DoubleUnaryOperator_andThen_java_util_function_DoubleUnaryOperator_after_LambdaImpl0(this, after);
    }

    /* synthetic */ double -java_util_function_DoubleUnaryOperator_lambda$2(DoubleUnaryOperator after, double t) {
        return after.applyAsDouble(applyAsDouble(t));
    }

    static /* synthetic */ double -java_util_function_DoubleUnaryOperator_lambda$3(double t) {
        return t;
    }

    static DoubleUnaryOperator identity() {
        return new -java_util_function_DoubleUnaryOperator_identity__LambdaImpl0();
    }
}
