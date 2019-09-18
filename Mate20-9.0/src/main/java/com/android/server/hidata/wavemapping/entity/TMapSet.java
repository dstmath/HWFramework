package com.android.server.hidata.wavemapping.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TMapSet<T, V> extends HashMap<T, Set<V>> {
    private static final long serialVersionUID = 1;

    public Set<V> add(T key, V value) {
        if (!super.containsKey(key)) {
            super.put(key, new HashSet());
        }
        Set<V> set = (Set) super.get(key);
        set.add(value);
        return (Set) super.get(set);
    }

    public Set<V> add(T key, Set<V> value) {
        return (Set) super.put(key, value);
    }

    public Set<V> get(Object key) {
        return (Set) super.get(key);
    }
}
