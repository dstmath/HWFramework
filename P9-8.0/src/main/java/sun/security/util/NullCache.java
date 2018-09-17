package sun.security.util;

import sun.security.util.Cache.CacheVisitor;

/* compiled from: Cache */
class NullCache<K, V> extends Cache<K, V> {
    static final Cache<Object, Object> INSTANCE = new NullCache();

    private NullCache() {
    }

    public int size() {
        return 0;
    }

    public void clear() {
    }

    public void put(K k, V v) {
    }

    public V get(Object key) {
        return null;
    }

    public void remove(Object key) {
    }

    public void setCapacity(int size) {
    }

    public void setTimeout(int timeout) {
    }

    public void accept(CacheVisitor<K, V> cacheVisitor) {
    }
}
