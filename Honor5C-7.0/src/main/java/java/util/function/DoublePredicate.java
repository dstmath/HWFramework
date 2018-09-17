package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface DoublePredicate {

    final /* synthetic */ class -java_util_function_DoublePredicate_and_java_util_function_DoublePredicate_other_LambdaImpl0 implements DoublePredicate {
        private /* synthetic */ DoublePredicate val$other;
        private /* synthetic */ DoublePredicate val$this;

        public /* synthetic */ -java_util_function_DoublePredicate_and_java_util_function_DoublePredicate_other_LambdaImpl0(DoublePredicate doublePredicate, DoublePredicate doublePredicate2) {
            this.val$this = doublePredicate;
            this.val$other = doublePredicate2;
        }

        public boolean test(double arg0) {
            return this.val$this.-java_util_function_DoublePredicate_lambda$1(this.val$other, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_DoublePredicate_negate__LambdaImpl0 implements DoublePredicate {
        private /* synthetic */ DoublePredicate val$this;

        public /* synthetic */ -java_util_function_DoublePredicate_negate__LambdaImpl0(DoublePredicate doublePredicate) {
            this.val$this = doublePredicate;
        }

        public boolean test(double arg0) {
            return this.val$this.-java_util_function_DoublePredicate_lambda$2(arg0);
        }
    }

    final /* synthetic */ class -java_util_function_DoublePredicate_or_java_util_function_DoublePredicate_other_LambdaImpl0 implements DoublePredicate {
        private /* synthetic */ DoublePredicate val$other;
        private /* synthetic */ DoublePredicate val$this;

        public /* synthetic */ -java_util_function_DoublePredicate_or_java_util_function_DoublePredicate_other_LambdaImpl0(DoublePredicate doublePredicate, DoublePredicate doublePredicate2) {
            this.val$this = doublePredicate;
            this.val$other = doublePredicate2;
        }

        public boolean test(double arg0) {
            return this.val$this.-java_util_function_DoublePredicate_lambda$3(this.val$other, arg0);
        }
    }

    boolean test(double d);

    DoublePredicate and(DoublePredicate other) {
        Objects.requireNonNull(other);
        return new -java_util_function_DoublePredicate_and_java_util_function_DoublePredicate_other_LambdaImpl0(this, other);
    }

    /* synthetic */ boolean -java_util_function_DoublePredicate_lambda$1(DoublePredicate other, double value) {
        return test(value) ? other.test(value) : false;
    }

    /* synthetic */ boolean -java_util_function_DoublePredicate_lambda$2(double value) {
        return !test(value);
    }

    DoublePredicate negate() {
        return new -java_util_function_DoublePredicate_negate__LambdaImpl0();
    }

    DoublePredicate or(DoublePredicate other) {
        Objects.requireNonNull(other);
        return new -java_util_function_DoublePredicate_or_java_util_function_DoublePredicate_other_LambdaImpl0(this, other);
    }

    /* synthetic */ boolean -java_util_function_DoublePredicate_lambda$3(DoublePredicate other, double value) {
        return !test(value) ? other.test(value) : true;
    }
}
