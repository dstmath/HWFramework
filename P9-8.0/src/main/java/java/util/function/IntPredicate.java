package java.util.function;

import java.util.Objects;
import java.util.function.-$Lambda$7wUuYdVTmZGtkCqNZ8xzJcRU4aE.AnonymousClass1;
import java.util.function.-$Lambda$7wUuYdVTmZGtkCqNZ8xzJcRU4aE.AnonymousClass2;

@FunctionalInterface
public interface IntPredicate {
    boolean test(int i);

    IntPredicate and(IntPredicate other) {
        Objects.requireNonNull(other);
        return new AnonymousClass1(this, other);
    }

    /* synthetic */ boolean lambda$-java_util_function_IntPredicate_2831(IntPredicate other, int value) {
        return test(value) ? other.test(value) : false;
    }

    /* synthetic */ boolean lambda$-java_util_function_IntPredicate_3136(int value) {
        return test(value) ^ 1;
    }

    IntPredicate negate() {
        return new -$Lambda$7wUuYdVTmZGtkCqNZ8xzJcRU4aE(this);
    }

    IntPredicate or(IntPredicate other) {
        Objects.requireNonNull(other);
        return new AnonymousClass2(this, other);
    }

    /* synthetic */ boolean lambda$-java_util_function_IntPredicate_4072(IntPredicate other, int value) {
        return !test(value) ? other.test(value) : true;
    }
}
