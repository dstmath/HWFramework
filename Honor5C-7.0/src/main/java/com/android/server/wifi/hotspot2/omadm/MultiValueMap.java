package com.android.server.wifi.hotspot2.omadm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MultiValueMap<T> {
    private final Map<String, ArrayList<T>> mMap;

    public MultiValueMap() {
        this.mMap = new LinkedHashMap();
    }

    public void put(String key, T value) {
        key = key.toLowerCase();
        ArrayList<T> values = (ArrayList) this.mMap.get(key);
        if (values == null) {
            values = new ArrayList();
            this.mMap.put(key, values);
        }
        values.add(value);
    }

    public T get(String key) {
        List<T> values = (List) this.mMap.get(key.toLowerCase());
        if (values == null) {
            return null;
        }
        if (values.size() == 1) {
            return values.get(0);
        }
        throw new IllegalArgumentException("Cannot do get on multi-value");
    }

    public T replace(String key, T oldValue, T newValue) {
        List<T> values = (List) this.mMap.get(key.toLowerCase());
        if (values == null) {
            return null;
        }
        for (int n = 0; n < values.size(); n++) {
            T value = values.get(n);
            if (value == oldValue) {
                values.set(n, newValue);
                return value;
            }
        }
        return null;
    }

    public T remove(String key, T value) {
        key = key.toLowerCase();
        List<T> values = (List) this.mMap.get(key);
        if (values == null) {
            return null;
        }
        T result = null;
        Iterator<T> valueIterator = values.iterator();
        while (valueIterator.hasNext()) {
            if (valueIterator.next() == value) {
                valueIterator.remove();
                result = value;
                break;
            }
        }
        if (values.isEmpty()) {
            this.mMap.remove(key);
        }
        return result;
    }

    public T remove(T value) {
        T result = null;
        Iterator<Entry<String, ArrayList<T>>> iterator = this.mMap.entrySet().iterator();
        while (iterator.hasNext()) {
            ArrayList<T> values = (ArrayList) ((Entry) iterator.next()).getValue();
            Iterator<T> valueIterator = values.iterator();
            while (valueIterator.hasNext()) {
                if (valueIterator.next() == value) {
                    valueIterator.remove();
                    result = value;
                    break;
                    continue;
                }
            }
            if (result != null) {
                if (values.isEmpty()) {
                    iterator.remove();
                }
                return result;
            }
        }
        return result;
    }

    public Collection<T> values() {
        List<T> allValues = new ArrayList(this.mMap.size());
        for (ArrayList<T> values : this.mMap.values()) {
            for (T value : values) {
                allValues.add(value);
            }
        }
        return allValues;
    }

    public T getSingletonValue() {
        if (this.mMap.size() != 1) {
            throw new IllegalArgumentException("Map is not a single entry map");
        }
        List<T> values = (List) this.mMap.values().iterator().next();
        if (values.size() == 1) {
            return values.iterator().next();
        }
        throw new IllegalArgumentException("Map is not a single entry map");
    }
}
