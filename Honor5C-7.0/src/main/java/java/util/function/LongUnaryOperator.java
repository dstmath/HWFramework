package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface LongUnaryOperator {

    final /* synthetic */ class -java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0 implements LongUnaryOperator {
        private /* synthetic */ LongUnaryOperator val$after;
        private /* synthetic */ LongUnaryOperator val$this;

        public /* synthetic */ -java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0(LongUnaryOperator longUnaryOperator, LongUnaryOperator longUnaryOperator2) {
            this.val$this = longUnaryOperator;
            this.val$after = longUnaryOperator2;
        }

        public long applyAsLong(long arg0) {
            return this.val$this.-java_util_function_LongUnaryOperator_lambda$2(this.val$after, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0 implements LongUnaryOperator {
        private /* synthetic */ LongUnaryOperator val$before;
        private /* synthetic */ LongUnaryOperator val$this;

        public /* synthetic */ -java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0(LongUnaryOperator longUnaryOperator, LongUnaryOperator longUnaryOperator2) {
            this.val$this = longUnaryOperator;
            this.val$before = longUnaryOperator2;
        }

        public long applyAsLong(long arg0) {
            return this.val$this.-java_util_function_LongUnaryOperator_lambda$1(this.val$before, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_LongUnaryOperator_identity__LambdaImpl0 implements LongUnaryOperator {
        public long applyAsLong(long arg0) {
            return LongUnaryOperator.-java_util_function_LongUnaryOperator_lambda$3(arg0);
        }
    }

    long applyAsLong(long j);

    LongUnaryOperator compose(LongUnaryOperator before) {
        Objects.requireNonNull(before);
        return new -java_util_function_LongUnaryOperator_compose_java_util_function_LongUnaryOperator_before_LambdaImpl0(this, before);
    }

    /* synthetic */ long -java_util_function_LongUnaryOperator_lambda$1(LongUnaryOperator before, long v) {
        return applyAsLong(before.applyAsLong(v));
    }

    LongUnaryOperator andThen(LongUnaryOperator after) {
        Objects.requireNonNull(after);
        return new -java_util_function_LongUnaryOperator_andThen_java_util_function_LongUnaryOperator_after_LambdaImpl0(this, after);
    }

    /* synthetic */ long -java_util_function_LongUnaryOperator_lambda$2(LongUnaryOperator after, long t) {
        return after.applyAsLong(applyAsLong(t));
    }

    static /* synthetic */ long -java_util_function_LongUnaryOperator_lambda$3(long t) {
        return t;
    }

    static LongUnaryOperator identity() {
        return new -java_util_function_LongUnaryOperator_identity__LambdaImpl0();
    }
}
