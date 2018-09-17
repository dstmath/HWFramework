package java.util.function;

import java.util.Objects;
import java.util.function.-$Lambda$XT6V3FCwc0LyV9_0ISNecLl_A-U.AnonymousClass1;
import java.util.function.-$Lambda$XT6V3FCwc0LyV9_0ISNecLl_A-U.AnonymousClass2;

@FunctionalInterface
public interface BiPredicate<T, U> {
    boolean test(T t, U u);

    BiPredicate<T, U> and(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return new AnonymousClass1(this, other);
    }

    /* synthetic */ boolean lambda$-java_util_function_BiPredicate_2994(BiPredicate other, Object t, Object u) {
        return test(t, u) ? other.test(t, u) : false;
    }

    /* synthetic */ boolean lambda$-java_util_function_BiPredicate_3305(Object t, Object u) {
        return test(t, u) ^ 1;
    }

    BiPredicate<T, U> negate() {
        return new -$Lambda$XT6V3FCwc0LyV9_0ISNecLl_A-U(this);
    }

    BiPredicate<T, U> or(BiPredicate<? super T, ? super U> other) {
        Objects.requireNonNull(other);
        return new AnonymousClass2(this, other);
    }

    /* synthetic */ boolean lambda$-java_util_function_BiPredicate_4269(BiPredicate other, Object t, Object u) {
        return !test(t, u) ? other.test(t, u) : true;
    }
}
