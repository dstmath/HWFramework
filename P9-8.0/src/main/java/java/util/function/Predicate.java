package java.util.function;

import java.util.Objects;
import java.util.function.-$Lambda$1rDGbc8p7Mv-tQJZzJy5uAxVFbE.AnonymousClass1;
import java.util.function.-$Lambda$1rDGbc8p7Mv-tQJZzJy5uAxVFbE.AnonymousClass2;
import java.util.function.-$Lambda$1rDGbc8p7Mv-tQJZzJy5uAxVFbE.AnonymousClass3;
import java.util.function.-$Lambda$1rDGbc8p7Mv-tQJZzJy5uAxVFbE.AnonymousClass4;

@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);

    Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return new AnonymousClass3(this, other);
    }

    /* synthetic */ boolean lambda$-java_util_function_Predicate_2759(Predicate other, Object t) {
        return test(t) ? other.test(t) : false;
    }

    /* synthetic */ boolean lambda$-java_util_function_Predicate_3052(Object t) {
        return test(t) ^ 1;
    }

    Predicate<T> negate() {
        return new AnonymousClass2(this);
    }

    Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return new AnonymousClass4(this, other);
    }

    /* synthetic */ boolean lambda$-java_util_function_Predicate_3988(Predicate other, Object t) {
        return !test(t) ? other.test(t) : true;
    }

    static <T> Predicate<T> isEqual(Object targetRef) {
        if (targetRef == null) {
            return new -$Lambda$1rDGbc8p7Mv-tQJZzJy5uAxVFbE();
        }
        return new AnonymousClass1(targetRef);
    }
}
