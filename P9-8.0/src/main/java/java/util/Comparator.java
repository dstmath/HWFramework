package java.util;

import java.util.-$Lambda$4EqhxufgNKat19m0CB0-toH_lzo.AnonymousClass1;
import java.util.-$Lambda$4EqhxufgNKat19m0CB0-toH_lzo.AnonymousClass2;
import java.util.-$Lambda$4EqhxufgNKat19m0CB0-toH_lzo.AnonymousClass3;
import java.util.-$Lambda$4EqhxufgNKat19m0CB0-toH_lzo.AnonymousClass4;
import java.util.-$Lambda$4EqhxufgNKat19m0CB0-toH_lzo.AnonymousClass5;
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
        return new AnonymousClass5(this, other);
    }

    /* synthetic */ int lambda$-java_util_Comparator_10127(Comparator other, Object c1, Object c2) {
        int res = compare(c1, c2);
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
        return NaturalOrderComparator.INSTANCE;
    }

    static <T> Comparator<T> nullsFirst(Comparator<? super T> comparator) {
        return new NullComparator(true, comparator);
    }

    static <T> Comparator<T> nullsLast(Comparator<? super T> comparator) {
        return new NullComparator(false, comparator);
    }

    static <T, U> Comparator<T> comparing(Function<? super T, ? extends U> keyExtractor, Comparator<? super U> keyComparator) {
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(keyComparator);
        return new AnonymousClass4(keyComparator, keyExtractor);
    }

    static <T, U extends Comparable<? super U>> Comparator<T> comparing(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return new -$Lambda$4EqhxufgNKat19m0CB0-toH_lzo(keyExtractor);
    }

    static <T> Comparator<T> comparingInt(ToIntFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return new AnonymousClass2(keyExtractor);
    }

    static <T> Comparator<T> comparingLong(ToLongFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return new AnonymousClass3(keyExtractor);
    }

    static <T> Comparator<T> comparingDouble(ToDoubleFunction<? super T> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return new AnonymousClass1(keyExtractor);
    }
}
