package sun.security.util;

import java.util.Arrays;
import java.util.Map;

public abstract class Cache<K, V> {

    public interface CacheVisitor<K, V> {
        void visit(Map<K, V> map);
    }

    public static class EqualByteArray {
        private final byte[] b;
        private volatile int hash;

        public EqualByteArray(byte[] b) {
            this.b = b;
        }

        public int hashCode() {
            int h = this.hash;
            if (h == 0) {
                h = this.b.length + 1;
                for (byte b : this.b) {
                    h += (b & 255) * 37;
                }
                this.hash = h;
            }
            return h;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof EqualByteArray)) {
                return false;
            }
            return Arrays.equals(this.b, ((EqualByteArray) obj).b);
        }
    }

    public abstract void accept(CacheVisitor<K, V> cacheVisitor);

    public abstract void clear();

    public abstract V get(Object obj);

    public abstract void put(K k, V v);

    public abstract void remove(Object obj);

    public abstract void setCapacity(int i);

    public abstract void setTimeout(int i);

    public abstract int size();

    protected Cache() {
    }

    public static <K, V> Cache<K, V> newSoftMemoryCache(int size) {
        return new MemoryCache(true, size);
    }

    public static <K, V> Cache<K, V> newSoftMemoryCache(int size, int timeout) {
        return new MemoryCache(true, size, timeout);
    }

    public static <K, V> Cache<K, V> newHardMemoryCache(int size) {
        return new MemoryCache(false, size);
    }

    public static <K, V> Cache<K, V> newNullCache() {
        return NullCache.INSTANCE;
    }

    public static <K, V> Cache<K, V> newHardMemoryCache(int size, int timeout) {
        return new MemoryCache(false, size, timeout);
    }
}
