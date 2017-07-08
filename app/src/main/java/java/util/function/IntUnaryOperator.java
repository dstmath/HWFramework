package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntUnaryOperator {

    final /* synthetic */ class -java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0 implements IntUnaryOperator {
        private /* synthetic */ IntUnaryOperator val$after;
        private /* synthetic */ IntUnaryOperator val$this;

        public /* synthetic */ -java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0(IntUnaryOperator intUnaryOperator, IntUnaryOperator intUnaryOperator2) {
            this.val$this = intUnaryOperator;
            this.val$after = intUnaryOperator2;
        }

        public int applyAsInt(int arg0) {
            return this.val$this.-java_util_function_IntUnaryOperator_lambda$2(this.val$after, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0 implements IntUnaryOperator {
        private /* synthetic */ IntUnaryOperator val$before;
        private /* synthetic */ IntUnaryOperator val$this;

        public /* synthetic */ -java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0(IntUnaryOperator intUnaryOperator, IntUnaryOperator intUnaryOperator2) {
            this.val$this = intUnaryOperator;
            this.val$before = intUnaryOperator2;
        }

        public int applyAsInt(int arg0) {
            return this.val$this.-java_util_function_IntUnaryOperator_lambda$1(this.val$before, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_IntUnaryOperator_identity__LambdaImpl0 implements IntUnaryOperator {
        public int applyAsInt(int arg0) {
            return IntUnaryOperator.-java_util_function_IntUnaryOperator_lambda$3(arg0);
        }
    }

    int applyAsInt(int i);

    IntUnaryOperator compose(IntUnaryOperator before) {
        Objects.requireNonNull(before);
        return new -java_util_function_IntUnaryOperator_compose_java_util_function_IntUnaryOperator_before_LambdaImpl0(this, before);
    }

    /* synthetic */ int -java_util_function_IntUnaryOperator_lambda$1(IntUnaryOperator before, int v) {
        return applyAsInt(before.applyAsInt(v));
    }

    IntUnaryOperator andThen(IntUnaryOperator after) {
        Objects.requireNonNull(after);
        return new -java_util_function_IntUnaryOperator_andThen_java_util_function_IntUnaryOperator_after_LambdaImpl0(this, after);
    }

    /* synthetic */ int -java_util_function_IntUnaryOperator_lambda$2(IntUnaryOperator after, int t) {
        return after.applyAsInt(applyAsInt(t));
    }

    static /* synthetic */ int -java_util_function_IntUnaryOperator_lambda$3(int t) {
        return t;
    }

    static IntUnaryOperator identity() {
        return new -java_util_function_IntUnaryOperator_identity__LambdaImpl0();
    }
}
