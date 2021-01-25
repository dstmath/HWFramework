package ohos.utils;

import java.util.Objects;

public class Pair<F, S> {
    public final F f;
    public final S s;

    public Pair(F f2, S s2) {
        this.f = f2;
        this.s = s2;
    }

    public static <F, S> Pair<F, S> create(F f2, S s2) {
        return new Pair<>(f2, s2);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair pair = (Pair) obj;
        if (!Objects.equals(pair.f, this.f) || !Objects.equals(pair.s, this.s)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return System.identityHashCode(this);
    }

    public String toString() {
        return "Pair{" + String.valueOf(this.f) + ": " + String.valueOf(this.s) + "}";
    }
}
