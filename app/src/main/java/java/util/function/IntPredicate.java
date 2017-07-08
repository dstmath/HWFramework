package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntPredicate {

    final /* synthetic */ class -java_util_function_IntPredicate_and_java_util_function_IntPredicate_other_LambdaImpl0 implements IntPredicate {
        private /* synthetic */ IntPredicate val$other;
        private /* synthetic */ IntPredicate val$this;

        public /* synthetic */ -java_util_function_IntPredicate_and_java_util_function_IntPredicate_other_LambdaImpl0(IntPredicate intPredicate, IntPredicate intPredicate2) {
            this.val$this = intPredicate;
            this.val$other = intPredicate2;
        }

        public boolean test(int arg0) {
            return this.val$this.-java_util_function_IntPredicate_lambda$1(this.val$other, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_IntPredicate_negate__LambdaImpl0 implements IntPredicate {
        private /* synthetic */ IntPredicate val$this;

        public /* synthetic */ -java_util_function_IntPredicate_negate__LambdaImpl0(IntPredicate intPredicate) {
            this.val$this = intPredicate;
        }

        public boolean test(int arg0) {
            return this.val$this.-java_util_function_IntPredicate_lambda$2(arg0);
        }
    }

    final /* synthetic */ class -java_util_function_IntPredicate_or_java_util_function_IntPredicate_other_LambdaImpl0 implements IntPredicate {
        private /* synthetic */ IntPredicate val$other;
        private /* synthetic */ IntPredicate val$this;

        public /* synthetic */ -java_util_function_IntPredicate_or_java_util_function_IntPredicate_other_LambdaImpl0(IntPredicate intPredicate, IntPredicate intPredicate2) {
            this.val$this = intPredicate;
            this.val$other = intPredicate2;
        }

        public boolean test(int arg0) {
            return this.val$this.-java_util_function_IntPredicate_lambda$3(this.val$other, arg0);
        }
    }

    boolean test(int i);

    IntPredicate and(IntPredicate other) {
        Objects.requireNonNull(other);
        return new -java_util_function_IntPredicate_and_java_util_function_IntPredicate_other_LambdaImpl0(this, other);
    }

    /* synthetic */ boolean -java_util_function_IntPredicate_lambda$1(IntPredicate other, int value) {
        return test(value) ? other.test(value) : false;
    }

    /* synthetic */ boolean -java_util_function_IntPredicate_lambda$2(int value) {
        return !test(value);
    }

    IntPredicate negate() {
        return new -java_util_function_IntPredicate_negate__LambdaImpl0();
    }

    IntPredicate or(IntPredicate other) {
        Objects.requireNonNull(other);
        return new -java_util_function_IntPredicate_or_java_util_function_IntPredicate_other_LambdaImpl0(this, other);
    }

    /* synthetic */ boolean -java_util_function_IntPredicate_lambda$3(IntPredicate other, int value) {
        return !test(value) ? other.test(value) : true;
    }
}
