package java.util.function;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.-$Lambda$mIdd76aENPtnGc8CGzUjiSYitJQ.AnonymousClass1;

@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T, T, T> {
    static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return new AnonymousClass1(comparator);
    }

    static /* synthetic */ Object lambda$-java_util_function_BinaryOperator_2544(Comparator comparator, Object a, Object b) {
        return comparator.compare(a, b) <= 0 ? a : b;
    }

    static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return new -$Lambda$mIdd76aENPtnGc8CGzUjiSYitJQ(comparator);
    }

    static /* synthetic */ Object lambda$-java_util_function_BinaryOperator_3246(Comparator comparator, Object a, Object b) {
        return comparator.compare(a, b) >= 0 ? a : b;
    }
}
