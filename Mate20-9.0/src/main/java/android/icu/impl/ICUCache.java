package android.icu.impl;

public interface ICUCache<K, V> {
    public static final Object NULL = new Object();
    public static final int SOFT = 0;
    public static final int WEAK = 1;

    void clear();

    V get(Object obj);

    void put(K k, V v);
}
