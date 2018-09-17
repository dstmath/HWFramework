package java.util.function;

import java.util.Objects;
import java.util.function.-$Lambda$Y5kXEBZc5fDOtRicvFfczerD_ZI.AnonymousClass1;
import java.util.function.-$Lambda$Y5kXEBZc5fDOtRicvFfczerD_ZI.AnonymousClass2;

@FunctionalInterface
public interface DoublePredicate {
    boolean test(double d);

    DoublePredicate and(DoublePredicate other) {
        Objects.requireNonNull(other);
        return new AnonymousClass1(this, other);
    }

    /* synthetic */ boolean lambda$-java_util_function_DoublePredicate_2852(DoublePredicate other, double value) {
        return test(value) ? other.test(value) : false;
    }

    /* synthetic */ boolean lambda$-java_util_function_DoublePredicate_3160(double value) {
        return test(value) ^ 1;
    }

    DoublePredicate negate() {
        return new -$Lambda$Y5kXEBZc5fDOtRicvFfczerD_ZI(this);
    }

    DoublePredicate or(DoublePredicate other) {
        Objects.requireNonNull(other);
        return new AnonymousClass2(this, other);
    }

    /* synthetic */ boolean lambda$-java_util_function_DoublePredicate_4102(DoublePredicate other, double value) {
        return !test(value) ? other.test(value) : true;
    }
}
