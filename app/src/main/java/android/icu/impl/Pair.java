package android.icu.impl;

public class Pair<F, S> {
    public final F first;
    public final S second;

    protected Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> of(F first, S second) {
        if (first != null && second != null) {
            return new Pair(first, second);
        }
        throw new IllegalArgumentException("Pair.of requires non null values.");
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (other == this) {
            return true;
        }
        if (!(other instanceof Pair)) {
            return false;
        }
        Pair<?, ?> rhs = (Pair) other;
        if (this.first.equals(rhs.first)) {
            z = this.second.equals(rhs.second);
        }
        return z;
    }

    public int hashCode() {
        return (this.first.hashCode() * 37) + this.second.hashCode();
    }
}
