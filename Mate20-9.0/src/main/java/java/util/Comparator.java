package java.util;

import java.util.Comparators;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

@FunctionalInterface
public interface Comparator<T> {
    int compare(T t, T t2);

    boolean equals(Object obj);

    Comparator<T> reversed() {
        return Collections.reverseOrder(this);
    }

    Comparator<T> thenComparing(Comparator<? super T> other) {
        Objects.requireNonNull(other);
        return new Object(other) {
            private final /* synthetic */ Comparator f$1;

            {
                this.f$1 = r2;
            }

            public final int compare(Object obj, Object obj2) {
                return Comparator.lambda$thenComparing$36697e65$1(Comparator.this, this.f$1, obj, obj2);
            }
        };
    }

    static /* synthetic */ int lambda$thenComparing$36697e65$1(Comparator comparator, Comparator other, Object c1, Object c2) {
        int res = comparator.compare(c1, c2);
        return res != 0 ? res : other.compare(c1, c2);
    }

    <U> Comparator<T> thenComparing(Function<? super T, ? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        return thenComparing(comparing(keyExtractor, keyComparator));
    }

    <U extends Comparable<? super U>> Comparator<T> thenComparing(Function<? super T, ? extends U> keyExtractor) {
        return thenComparing(comparing(keyExtractor));
    }

    Comparator<T> thenComparingInt(ToIntFunction<? super T> keyExtractor) {
        return thenComparing(comparingInt(keyExtractor));
    }

    Comparator<T> thenComparingLong(ToLongFunction<? super T> keyExtractor) {
        return thenComparing(comparingLong(keyExtractor));
    }

    Comparator<T> thenComparingDouble(ToDoubleFunction<? super T> keyExtractor) {
        return thenComparing(comparingDouble(keyExtractor));
    }

    static <T extends Comparable<? super T>> Comparator<T> reverseOrder() {
        return Collections.reverseOrder();
    }

    static <T extends Comparable<? super T>> Comparator<T> naturalOrder() {
        return Comparators.NaturalOrderComparator.INSTANCE;
    }

    static <T> Comparator<T> nullsFirst(Comparator<? super T> comparator) {
        return new Comparators.NullComparator(true, comparator);
    }

    static <T> Comparator<T> nullsLast(Comparator<? super T> comparator) {
        return new Comparators.NullComparator(false, comparator);
    }

    static <T, U> Comparator<T> comparing(Function<? super T, ? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        return new Object(keyExtractor) {
            private final /* synthetic */ Function f$1;

            {
                this.f$1 = r2;
            }

            public final int compare(Object obj, Object obj2) {
                return Comparator.this.compare(this.f$1.apply(obj), this.f$1.apply(obj2));
            }
        };
    }

    static <T, U extends Comparable<? super U>> Comparator<T> comparing(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return new Object() {
            public final int compare(Object obj, Object obj2) {
                return ((Comparable) Function.this.apply(obj)).compareTo(Function.this.apply(obj2));
            }
        };
    }

    static <T> Comparator<T> comparingInt(ToIntFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return new Object() {
            public final int compare(Object obj, Object obj2) {
                return Integer.compare(ToIntFunction.this.applyAsInt(obj), ToIntFunction.this.applyAsInt(obj2));
            }
        };
    }

    static <T> Comparator<T> comparingLong(ToLongFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return new Object() {
            public final int compare(Object obj, Object obj2) {
                return Long.compare(ToLongFunction.this.applyAsLong(obj), ToLongFunction.this.applyAsLong(obj2));
            }
        };
    }

    static <T> Comparator<T> comparingDouble(ToDoubleFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return new Object() {
            public final int compare(Object obj, Object obj2) {
                return Double.compare(ToDoubleFunction.this.applyAsDouble(obj), ToDoubleFunction.this.applyAsDouble(obj2));
            }
        };
    }
}
