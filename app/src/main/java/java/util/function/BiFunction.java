package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiFunction<T, U, R> {

    final /* synthetic */ class -java_util_function_BiFunction_andThen_java_util_function_Function_after_LambdaImpl0 implements BiFunction {
        private /* synthetic */ Function val$after;
        private /* synthetic */ BiFunction val$this;

        public /* synthetic */ -java_util_function_BiFunction_andThen_java_util_function_Function_after_LambdaImpl0(BiFunction biFunction, Function function) {
            this.val$this = biFunction;
            this.val$after = function;
        }

        public Object apply(Object arg0, Object arg1) {
            return this.val$this.-java_util_function_BiFunction_lambda$1(this.val$after, arg0, arg1);
        }
    }

    R apply(T t, U u);

    <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return new -java_util_function_BiFunction_andThen_java_util_function_Function_after_LambdaImpl0(this, after);
    }

    /* synthetic */ Object -java_util_function_BiFunction_lambda$1(Function after, Object t, Object u) {
        return after.apply(apply(t, u));
    }
}
