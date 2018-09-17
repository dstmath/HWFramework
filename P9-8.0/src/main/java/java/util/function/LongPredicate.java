package java.util.function;

import java.util.Objects;
import java.util.function.-$Lambda$iBcNfuYkNoKgH3GCUZob50qquB0.AnonymousClass1;
import java.util.function.-$Lambda$iBcNfuYkNoKgH3GCUZob50qquB0.AnonymousClass2;

@FunctionalInterface
public interface LongPredicate {
    boolean test(long j);

    LongPredicate and(LongPredicate other) {
        Objects.requireNonNull(other);
        return new AnonymousClass1(this, other);
    }

    /* synthetic */ boolean lambda$-java_util_function_LongPredicate_2838(LongPredicate other, long value) {
        return test(value) ? other.test(value) : false;
    }

    /* synthetic */ boolean lambda$-java_util_function_LongPredicate_3144(long value) {
        return test(value) ^ 1;
    }

    LongPredicate negate() {
        return new -$Lambda$iBcNfuYkNoKgH3GCUZob50qquB0(this);
    }

    LongPredicate or(LongPredicate other) {
        Objects.requireNonNull(other);
        return new AnonymousClass2(this, other);
    }

    /* synthetic */ boolean lambda$-java_util_function_LongPredicate_4082(LongPredicate other, long value) {
        return !test(value) ? other.test(value) : true;
    }
}
