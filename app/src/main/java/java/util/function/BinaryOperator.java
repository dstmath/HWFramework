package java.util.function;

import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T, T, T> {

    final /* synthetic */ class -java_util_function_BinaryOperator_maxBy_java_util_Comparator_comparator_LambdaImpl0 implements BinaryOperator {
        private /* synthetic */ Comparator val$comparator;

        public /* synthetic */ -java_util_function_BinaryOperator_maxBy_java_util_Comparator_comparator_LambdaImpl0(Comparator comparator) {
            this.val$comparator = comparator;
        }

        public Object apply(Object arg0, Object arg1) {
            return BinaryOperator.-java_util_function_BinaryOperator_lambda$2(this.val$comparator, arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_function_BinaryOperator_minBy_java_util_Comparator_comparator_LambdaImpl0 implements BinaryOperator {
        private /* synthetic */ Comparator val$comparator;

        public /* synthetic */ -java_util_function_BinaryOperator_minBy_java_util_Comparator_comparator_LambdaImpl0(Comparator comparator) {
            this.val$comparator = comparator;
        }

        public Object apply(Object arg0, Object arg1) {
            return BinaryOperator.-java_util_function_BinaryOperator_lambda$1(this.val$comparator, arg0, arg1);
        }
    }

    static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return new -java_util_function_BinaryOperator_minBy_java_util_Comparator_comparator_LambdaImpl0(comparator);
    }

    static /* synthetic */ Object -java_util_function_BinaryOperator_lambda$1(Comparator comparator, Object a, Object b) {
        return comparator.compare(a, b) <= 0 ? a : b;
    }

    static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return new -java_util_function_BinaryOperator_maxBy_java_util_Comparator_comparator_LambdaImpl0(comparator);
    }

    static /* synthetic */ Object -java_util_function_BinaryOperator_lambda$2(Comparator comparator, Object a, Object b) {
        return comparator.compare(a, b) >= 0 ? a : b;
    }
}
