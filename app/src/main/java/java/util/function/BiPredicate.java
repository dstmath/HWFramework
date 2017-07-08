package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiPredicate<T, U> {

    final /* synthetic */ class -java_util_function_BiPredicate_and_java_util_function_BiPredicate_other_LambdaImpl0 implements BiPredicate {
        private /* synthetic */ BiPredicate val$other;
        private /* synthetic */ BiPredicate val$this;

        public /* synthetic */ -java_util_function_BiPredicate_and_java_util_function_BiPredicate_other_LambdaImpl0(BiPredicate biPredicate, BiPredicate biPredicate2) {
            this.val$this = biPredicate;
            this.val$other = biPredicate2;
        }

        public boolean test(Object arg0, Object arg1) {
            return this.val$this.-java_util_function_BiPredicate_lambda$1(this.val$other, arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_function_BiPredicate_negate__LambdaImpl0 implements BiPredicate {
        private /* synthetic */ BiPredicate val$this;

        public /* synthetic */ -java_util_function_BiPredicate_negate__LambdaImpl0(BiPredicate biPredicate) {
            this.val$this = biPredicate;
        }

        public boolean test(Object arg0, Object arg1) {
            return this.val$this.-java_util_function_BiPredicate_lambda$2(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_function_BiPredicate_or_java_util_function_BiPredicate_other_LambdaImpl0 implements BiPredicate {
        private /* synthetic */ BiPredicate val$other;
        private /* synthetic */ BiPredicate val$this;

        public /* synthetic */ -java_util_function_BiPredicate_or_java_util_function_BiPredicate_other_LambdaImpl0(BiPredicate biPredicate, BiPredicate biPredicate2) {
            this.val$this = biPredicate;
            this.val$other = biPredicate2;
        }

        public boolean test(Object arg0, Object arg1) {
            return this.val$this.-java_util_function_BiPredicate_lambda$3(this.val$other, arg0, arg1);
        }
    }

    boolean test(T t, U u);

    BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return new -java_util_function_BiPredicate_and_java_util_function_BiPredicate_other_LambdaImpl0(this, other);
    }

    /* synthetic */ boolean -java_util_function_BiPredicate_lambda$1(BiPredicate other, Object t, Object u) {
        return test(t, u) ? other.test(t, u) : false;
    }

    /* synthetic */ boolean -java_util_function_BiPredicate_lambda$2(Object t, Object u) {
        return !test(t, u);
    }

    BiPredicate<T, U> negate() {
        return new -java_util_function_BiPredicate_negate__LambdaImpl0();
    }

    BiPredicate<T, U> or(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return new -java_util_function_BiPredicate_or_java_util_function_BiPredicate_other_LambdaImpl0(this, other);
    }

    /* synthetic */ boolean -java_util_function_BiPredicate_lambda$3(BiPredicate other, Object t, Object u) {
        return !test(t, u) ? other.test(t, u) : true;
    }
}
