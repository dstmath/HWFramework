package android.support.v4.util;

public class Pair<F, S> {
    public final F first;
    public final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair) o;
        if (objectsEqual(p.first, this.first)) {
            z = objectsEqual(p.second, this.second);
        }
        return z;
    }

    private static boolean objectsEqual(Object a, Object b) {
        if (a != b) {
            return a != null ? a.equals(b) : false;
        } else {
            return true;
        }
    }

    public int hashCode() {
        int i = 0;
        int hashCode = this.first == null ? 0 : this.first.hashCode();
        if (this.second != null) {
            i = this.second.hashCode();
        }
        return hashCode ^ i;
    }

    public String toString() {
        return "Pair{" + String.valueOf(this.first) + " " + String.valueOf(this.second) + "}";
    }

    public static <A, B> Pair<A, B> create(A a, B b) {
        return new Pair(a, b);
    }
}
