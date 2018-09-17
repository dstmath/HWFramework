package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface Function<T, R> {

    final /* synthetic */ class -java_util_function_Function_andThen_java_util_function_Function_after_LambdaImpl0 implements Function {
        private /* synthetic */ Function val$after;
        private /* synthetic */ Function val$this;

        public /* synthetic */ -java_util_function_Function_andThen_java_util_function_Function_after_LambdaImpl0(Function function, Function function2) {
            this.val$this = function;
            this.val$after = function2;
        }

        public Object apply(Object arg0) {
            return this.val$this.-java_util_function_Function_lambda$2(this.val$after, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_Function_compose_java_util_function_Function_before_LambdaImpl0 implements Function {
        private /* synthetic */ Function val$before;
        private /* synthetic */ Function val$this;

        public /* synthetic */ -java_util_function_Function_compose_java_util_function_Function_before_LambdaImpl0(Function function, Function function2) {
            this.val$this = function;
            this.val$before = function2;
        }

        public Object apply(Object arg0) {
            return this.val$this.-java_util_function_Function_lambda$1(this.val$before, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_Function_identity__LambdaImpl0 implements Function {
        public Object apply(Object arg0) {
            return Function.-java_util_function_Function_lambda$3(arg0);
        }
    }

    R apply(T t);

    <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return new -java_util_function_Function_compose_java_util_function_Function_before_LambdaImpl0(this, before);
    }

    /* synthetic */ Object -java_util_function_Function_lambda$1(Function before, Object v) {
        return apply(before.apply(v));
    }

    <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return new -java_util_function_Function_andThen_java_util_function_Function_after_LambdaImpl0(this, after);
    }

    /* synthetic */ Object -java_util_function_Function_lambda$2(Function after, Object t) {
        return after.apply(apply(t));
    }

    static /* synthetic */ Object -java_util_function_Function_lambda$3(Object t) {
        return t;
    }

    static <T> Function<T, T> identity() {
        return new -java_util_function_Function_identity__LambdaImpl0();
    }
}
