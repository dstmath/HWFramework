package com.huawei.odmf.utils;

public interface Cache<K, V> {
    boolean clear();

    boolean containsKey(K k);

    boolean containsValue(V v);

    V get(K k);

    V put(K k, V v);

    V remove(K k);
}
