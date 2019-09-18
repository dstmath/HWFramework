package android.icu.impl;

public abstract class CacheBase<K, V, D> {
    /* access modifiers changed from: protected */
    public abstract V createInstance(K k, D d);

    public abstract V getInstance(K k, D d);
}
