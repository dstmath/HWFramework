package java.util;

public abstract class Dictionary<K, V> {
    public abstract Enumeration<V> elements();

    public abstract V get(Object obj);

    public abstract boolean isEmpty();

    public abstract Enumeration<K> keys();

    public abstract V put(K k, V v);

    public abstract V remove(Object obj);

    public abstract int size();
}
