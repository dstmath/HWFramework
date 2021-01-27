package ohos.utils;

import java.lang.Comparable;
import java.util.Objects;

public final class Scope<T extends Comparable<? super T>> {
    private final T lower;
    private final T upper;

    public Scope(T t, T t2) {
        Objects.requireNonNull(t, "Argument lowerObj must not be null");
        Objects.requireNonNull(t2, "Argument upperObj must not be null");
        if (t2.compareTo(t) >= 0) {
            this.lower = t;
            this.upper = t2;
            return;
        }
        throw new IllegalArgumentException("upper must be larger than lower, or equal to lower");
    }

    public static <T extends Comparable<? super T>> Scope<T> create(T t, T t2) {
        return new Scope<>(t, t2);
    }

    public T getLower() {
        return this.lower;
    }

    public T getUpper() {
        return this.upper;
    }

    public T clamp(T t) {
        Objects.requireNonNull(t, "Argument value must not be null");
        if (t.compareTo(this.upper) > 0) {
            return this.upper;
        }
        return t.compareTo(this.lower) < 0 ? this.lower : t;
    }

    public boolean contains(T t) {
        Objects.requireNonNull(t, "Argument value must not be null");
        return t.compareTo(this.lower) >= 0 && t.compareTo(this.upper) <= 0;
    }

    public boolean contains(Scope<T> scope) {
        Objects.requireNonNull(scope, "Argument range must not be null");
        return scope.lower.compareTo(this.lower) >= 0 && scope.upper.compareTo(this.upper) <= 0;
    }

    public Scope<T> expand(Scope<T> scope) {
        Objects.requireNonNull(scope, "Argument range must not be null");
        return create(scope.lower.compareTo(this.lower) >= 0 ? this.lower : scope.lower, scope.upper.compareTo(this.upper) >= 0 ? scope.upper : this.upper);
    }

    public Scope<T> expand(T t, T t2) {
        Objects.requireNonNull(t, "Argument lowerObj must not be null");
        Objects.requireNonNull(t2, "Argument upperObj must not be null");
        int compareTo = t.compareTo(this.lower);
        int compareTo2 = t2.compareTo(this.upper);
        if (compareTo >= 0) {
            t = this.lower;
        }
        if (compareTo2 < 0) {
            t2 = this.upper;
        }
        return create(t, t2);
    }

    public Scope<T> expand(T t) {
        Objects.requireNonNull(t, "Argument value must not be null");
        return expand(t, t);
    }

    public Scope<T> intersect(Scope<T> scope) {
        Objects.requireNonNull(scope, "Argument range must not be null");
        T t = scope.lower.compareTo(this.lower) >= 0 ? scope.lower : this.lower;
        T t2 = scope.upper.compareTo(this.upper) >= 0 ? this.upper : scope.upper;
        if (t2.compareTo(t) >= 0) {
            return create(t, t2);
        }
        throw new IllegalArgumentException("cross by range error, cmpUpper must be larger or equal than cmpLower");
    }

    public Scope<T> intersect(T t, T t2) {
        Objects.requireNonNull(t, "Argument lowerObj must not be null");
        Objects.requireNonNull(t2, "Argument upperObj must not be null");
        if (t.compareTo(this.lower) < 0) {
            t = this.lower;
        }
        if (t2.compareTo(this.upper) >= 0) {
            t2 = this.upper;
        }
        if (t2.compareTo(t) >= 0) {
            return create(t, t2);
        }
        throw new IllegalArgumentException("intersect by lowerObj and upperObj error, cmpUpper must be larger or equal than cmpLower");
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Scope)) {
            return false;
        }
        Scope scope = (Scope) obj;
        if (!scope.lower.equals(this.lower) || !scope.upper.equals(this.upper)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return Objects.hash(this.lower, this.upper);
    }

    public String toString() {
        return String.format("[%s, %s]", this.lower, this.upper);
    }
}
