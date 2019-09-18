package java.util.function;

import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface BinaryOperator<T> extends BiFunction<T, T, T> {
    static <T> BinaryOperator<T> minBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return new BinaryOperator() {
            public final Object apply(Object obj, Object obj2) {
                return BinaryOperator.lambda$minBy$0(Comparator.this, obj, obj2);
            }
        };
    }

    static /* synthetic */ Object lambda$minBy$0(Comparator comparator, Object a, Object b) {
        return comparator.compare(a, b) <= 0 ? a : b;
    }

    static <T> BinaryOperator<T> maxBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        return new BinaryOperator() {
            public final Object apply(Object obj, Object obj2) {
                return BinaryOperator.lambda$maxBy$1(Comparator.this, obj, obj2);
            }
        };
    }

    static /* synthetic */ Object lambda$maxBy$1(Comparator comparator, Object a, Object b) {
        return comparator.compare(a, b) >= 0 ? a : b;
    }
}
