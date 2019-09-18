package android.icu.impl;

public class Pair<F, S> {
    public final F first;
    public final S second;

    protected Pair(F first2, S second2) {
        this.first = first2;
        this.second = second2;
    }

    public static <F, S> Pair<F, S> of(F first2, S second2) {
        if (first2 != null && second2 != null) {
            return new Pair<>(first2, second2);
        }
        throw new IllegalArgumentException("Pair.of requires non null values.");
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == this) {
            return true;
        }
        if (!(other instanceof Pair)) {
            return false;
        }
        Pair<?, ?> rhs = (Pair) other;
        if (!this.first.equals(rhs.first) || !this.second.equals(rhs.second)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return (this.first.hashCode() * 37) + this.second.hashCode();
    }
}
