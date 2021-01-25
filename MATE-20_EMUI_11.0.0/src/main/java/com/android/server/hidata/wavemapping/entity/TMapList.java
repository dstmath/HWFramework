package com.android.server.hidata.wavemapping.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TMapList<T, V> extends HashMap<T, List<V>> {
    private static final int DEFAULT_CAPACITY = 10;
    private static final long serialVersionUID = 1;

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.hidata.wavemapping.entity.TMapList<T, V> */
    /* JADX WARN: Multi-variable type inference failed */
    public List<V> add(T key, V value) {
        if (!super.containsKey(key)) {
            super.put(key, new ArrayList(10));
        }
        List<V> list = (List) super.get((Object) key);
        list.add(value);
        return (List) super.get((Object) list);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.util.List<V> */
    /* JADX WARN: Multi-variable type inference failed */
    public List<V> add(T key, List<V> value) {
        return (List) super.put(key, value);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.hidata.wavemapping.entity.TMapList<T, V> */
    /* JADX WARN: Multi-variable type inference failed */
    public List<V> insert(T key, V value) {
        if (!super.containsKey(key)) {
            super.put(key, new ArrayList(10));
        }
        List<V> list = (List) super.get((Object) key);
        list.add(value);
        return (List) super.get((Object) list);
    }

    @Override // java.util.HashMap, java.util.AbstractMap, java.util.Map
    public List<V> get(Object key) {
        return (List) super.get(key);
    }
}
