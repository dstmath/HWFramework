package com.android.server.hidata.wavemapping.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TMapList<T, V> extends HashMap<T, List<V>> {
    private static final long serialVersionUID = 1;

    public List<V> add(T key, V value) {
        if (!super.containsKey(key)) {
            super.put(key, new ArrayList());
        }
        List<V> list = (List) super.get(key);
        list.add(value);
        return (List) super.get(list);
    }

    public List<V> insert(T key, V value) {
        if (!super.containsKey(key)) {
            super.put(key, new ArrayList());
        }
        List<V> list = (List) super.get(key);
        list.add(value);
        return (List) super.get(list);
    }

    public List<V> add(T key, List<V> value) {
        return (List) super.put(key, value);
    }

    public List<V> get(Object key) {
        return (List) super.get(key);
    }
}
