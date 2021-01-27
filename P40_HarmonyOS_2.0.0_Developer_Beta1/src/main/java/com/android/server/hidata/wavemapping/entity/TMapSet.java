package com.android.server.hidata.wavemapping.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TMapSet<T, V> extends HashMap<T, Set<V>> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final long serialVersionUID = 1;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.hidata.wavemapping.entity.TMapSet<T, V> */
    /* JADX WARN: Multi-variable type inference failed */
    public Set<V> add(T key, V value) {
        if (!super.containsKey(key)) {
            super.put(key, new HashSet(16));
        }
        Set<V> set = (Set) super.get((Object) key);
        set.add(value);
        return (Set) super.get((Object) set);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.util.Set<V> */
    /* JADX WARN: Multi-variable type inference failed */
    public Set<V> add(T key, Set<V> value) {
        return (Set) super.put(key, value);
    }

    @Override // java.util.HashMap, java.util.AbstractMap, java.util.Map
    public Set<V> get(Object key) {
        return (Set) super.get(key);
    }
}
