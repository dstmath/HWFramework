package com.android.server.wifi.util;

import android.util.ArrayMap;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map;

public class ObjectCounter<K> implements Iterable<Map.Entry<K, Integer>> {
    private ArrayMap<K, Integer> mCounter = new ArrayMap<>();

    public interface ProtobufConverter<I, O> {
        O convert(I i, int i2);
    }

    public void clear() {
        this.mCounter.clear();
    }

    public int size() {
        return this.mCounter.size();
    }

    public int getCount(K key) {
        return this.mCounter.getOrDefault(key, 0).intValue();
    }

    public void increment(K key) {
        add(key, 1);
    }

    public void add(K key, int count) {
        this.mCounter.put(key, Integer.valueOf(getCount(key) + count));
    }

    @Override // java.lang.Object
    public String toString() {
        return this.mCounter.toString();
    }

    @Override // java.lang.Iterable
    public Iterator<Map.Entry<K, Integer>> iterator() {
        return this.mCounter.entrySet().iterator();
    }

    public <T> T[] toProto(Class<T> protoClass, ProtobufConverter<K, T> converter) {
        T[] output = (T[]) ((Object[]) Array.newInstance((Class<?>) protoClass, size()));
        int i = 0;
        Iterator<Map.Entry<K, Integer>> it = iterator();
        while (it.hasNext()) {
            Map.Entry<K, Integer> entry = it.next();
            output[i] = converter.convert(entry.getKey(), entry.getValue().intValue());
            i++;
        }
        return output;
    }
}
