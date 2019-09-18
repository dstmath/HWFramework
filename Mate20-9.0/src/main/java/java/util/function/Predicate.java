package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);

    Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return new Predicate(other) {
            private final /* synthetic */ Predicate f$1;

            {
                this.f$1 = r2;
            }

            public final boolean test(Object obj) {
                return Predicate.lambda$and$0(Predicate.this, this.f$1, obj);
            }
        };
    }

    static /* synthetic */ boolean lambda$and$0(Predicate predicate, Predicate other, Object t) {
        return predicate.test(t) && other.test(t);
    }

    static /* synthetic */ boolean lambda$negate$1(Predicate predicate, Object t) {
        return !predicate.test(t);
    }

    Predicate<T> negate() {
        return new Predicate() {
            public final boolean test(Object obj) {
                return Predicate.lambda$negate$1(Predicate.this, obj);
            }
        };
    }

    Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return new Predicate(other) {
            private final /* synthetic */ Predicate f$1;

            {
                this.f$1 = r2;
            }

            public final boolean test(Object obj) {
                return Predicate.lambda$or$2(Predicate.this, this.f$1, obj);
            }
        };
    }

    static /* synthetic */ boolean lambda$or$2(Predicate predicate, Predicate other, Object t) {
        return predicate.test(t) || other.test(t);
    }

    static <T> Predicate<T> isEqual(Object targetRef) {
        if (targetRef == null) {
            return $$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE;
        }
        return new Predicate() {
            public final boolean test(Object obj) {
                return Object.this.equals(obj);
            }
        };
    }
}
