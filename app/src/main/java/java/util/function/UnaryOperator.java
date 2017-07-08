package java.util.function;

@FunctionalInterface
public interface UnaryOperator<T> extends Function<T, T> {

    final /* synthetic */ class -java_util_function_UnaryOperator_identity__LambdaImpl0 implements UnaryOperator {
        public Object apply(Object arg0) {
            return UnaryOperator.-java_util_function_UnaryOperator_lambda$1(arg0);
        }
    }

    static /* synthetic */ Object -java_util_function_UnaryOperator_lambda$1(Object t) {
        return t;
    }

    static <T> UnaryOperator<T> identity() {
        return new -java_util_function_UnaryOperator_identity__LambdaImpl0();
    }
}
