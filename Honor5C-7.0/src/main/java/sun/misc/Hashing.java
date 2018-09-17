package sun.misc;

public class Hashing {
    private Hashing() {
        throw new Error("No instances");
    }

    public static int singleWordWangJenkinsHash(Object k) {
        int h = k.hashCode();
        h += (h << 15) ^ -12931;
        h ^= h >>> 10;
        h += h << 3;
        h ^= h >>> 6;
        h += (h << 2) + (h << 14);
        return (h >>> 16) ^ h;
    }
}
