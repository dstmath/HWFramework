package android.icu.impl;

public abstract class CacheBase<K, V, D> {
    protected abstract V createInstance(K k, D d);

    public abstract V getInstance(K k, D d);
}
