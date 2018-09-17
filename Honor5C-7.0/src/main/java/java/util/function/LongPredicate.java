package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface LongPredicate {

    final /* synthetic */ class -java_util_function_LongPredicate_and_java_util_function_LongPredicate_other_LambdaImpl0 implements LongPredicate {
        private /* synthetic */ LongPredicate val$other;
        private /* synthetic */ LongPredicate val$this;

        public /* synthetic */ -java_util_function_LongPredicate_and_java_util_function_LongPredicate_other_LambdaImpl0(LongPredicate longPredicate, LongPredicate longPredicate2) {
            this.val$this = longPredicate;
            this.val$other = longPredicate2;
        }

        public boolean test(long arg0) {
            return this.val$this.-java_util_function_LongPredicate_lambda$1(this.val$other, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_LongPredicate_negate__LambdaImpl0 implements LongPredicate {
        private /* synthetic */ LongPredicate val$this;

        public /* synthetic */ -java_util_function_LongPredicate_negate__LambdaImpl0(LongPredicate longPredicate) {
            this.val$this = longPredicate;
        }

        public boolean test(long arg0) {
            return this.val$this.-java_util_function_LongPredicate_lambda$2(arg0);
        }
    }

    final /* synthetic */ class -java_util_function_LongPredicate_or_java_util_function_LongPredicate_other_LambdaImpl0 implements LongPredicate {
        private /* synthetic */ LongPredicate val$other;
        private /* synthetic */ LongPredicate val$this;

        public /* synthetic */ -java_util_function_LongPredicate_or_java_util_function_LongPredicate_other_LambdaImpl0(LongPredicate longPredicate, LongPredicate longPredicate2) {
            this.val$this = longPredicate;
            this.val$other = longPredicate2;
        }

        public boolean test(long arg0) {
            return this.val$this.-java_util_function_LongPredicate_lambda$3(this.val$other, arg0);
        }
    }

    boolean test(long j);

    LongPredicate and(LongPredicate other) {
        Objects.requireNonNull(other);
        return new -java_util_function_LongPredicate_and_java_util_function_LongPredicate_other_LambdaImpl0(this, other);
    }

    /* synthetic */ boolean -java_util_function_LongPredicate_lambda$1(LongPredicate other, long value) {
        return test(value) ? other.test(value) : false;
    }

    /* synthetic */ boolean -java_util_function_LongPredicate_lambda$2(long value) {
        return !test(value);
    }

    LongPredicate negate() {
        return new -java_util_function_LongPredicate_negate__LambdaImpl0();
    }

    LongPredicate or(LongPredicate other) {
        Objects.requireNonNull(other);
        return new -java_util_function_LongPredicate_or_java_util_function_LongPredicate_other_LambdaImpl0(this, other);
    }

    /* synthetic */ boolean -java_util_function_LongPredicate_lambda$3(LongPredicate other, long value) {
        return !test(value) ? other.test(value) : true;
    }
}
