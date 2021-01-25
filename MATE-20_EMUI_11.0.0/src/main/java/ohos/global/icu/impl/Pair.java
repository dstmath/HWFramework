package ohos.global.icu.impl;

public class Pair<F, S> {
    public final F first;
    public final S second;

    protected Pair(F f, S s) {
        this.first = f;
        this.second = s;
    }

    public static <F, S> Pair<F, S> of(F f, S s) {
        if (f != null && s != null) {
            return new Pair<>(f, s);
        }
        throw new IllegalArgumentException("Pair.of requires non null values.");
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair pair = (Pair) obj;
        return this.first.equals(pair.first) && this.second.equals(pair.second);
    }

    public int hashCode() {
        return (this.first.hashCode() * 37) + this.second.hashCode();
    }
}
