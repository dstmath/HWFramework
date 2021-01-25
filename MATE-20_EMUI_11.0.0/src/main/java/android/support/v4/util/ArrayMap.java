package android.support.v4.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ArrayMap<K, V> extends SimpleArrayMap<K, V> implements Map<K, V> {
    MapCollections<K, V> mCollections;

    public ArrayMap() {
    }

    public ArrayMap(int capacity) {
        super(capacity);
    }

    public ArrayMap(SimpleArrayMap map) {
        super(map);
    }

    private MapCollections<K, V> getCollection() {
        if (this.mCollections == null) {
            this.mCollections = new MapCollections<K, V>() {
                /* class android.support.v4.util.ArrayMap.AnonymousClass1 */

                /* access modifiers changed from: protected */
                @Override // android.support.v4.util.MapCollections
                public int colGetSize() {
                    return ArrayMap.this.mSize;
                }

                /* access modifiers changed from: protected */
                @Override // android.support.v4.util.MapCollections
                public Object colGetEntry(int index, int offset) {
                    return ArrayMap.this.mArray[(index << 1) + offset];
                }

                /* access modifiers changed from: protected */
                @Override // android.support.v4.util.MapCollections
                public int colIndexOfKey(Object key) {
                    return ArrayMap.this.indexOfKey(key);
                }

                /* access modifiers changed from: protected */
                @Override // android.support.v4.util.MapCollections
                public int colIndexOfValue(Object value) {
                    return ArrayMap.this.indexOfValue(value);
                }

                /* access modifiers changed from: protected */
                @Override // android.support.v4.util.MapCollections
                public Map<K, V> colGetMap() {
                    return ArrayMap.this;
                }

                /* access modifiers changed from: protected */
                @Override // android.support.v4.util.MapCollections
                public void colPut(K key, V value) {
                    ArrayMap.this.put(key, value);
                }

                /* access modifiers changed from: protected */
                @Override // android.support.v4.util.MapCollections
                public V colSetValue(int index, V value) {
                    return (V) ArrayMap.this.setValueAt(index, value);
                }

                /* access modifiers changed from: protected */
                @Override // android.support.v4.util.MapCollections
                public void colRemoveAt(int index) {
                    ArrayMap.this.removeAt(index);
                }

                /* access modifiers changed from: protected */
                @Override // android.support.v4.util.MapCollections
                public void colClear() {
                    ArrayMap.this.clear();
                }
            };
        }
        return this.mCollections;
    }

    public boolean containsAll(Collection<?> collection) {
        return MapCollections.containsAllHelper(this, collection);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: android.support.v4.util.ArrayMap<K, V> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.Map
    public void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(this.mSize + map.size());
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public boolean removeAll(Collection<?> collection) {
        return MapCollections.removeAllHelper(this, collection);
    }

    public boolean retainAll(Collection<?> collection) {
        return MapCollections.retainAllHelper(this, collection);
    }

    @Override // java.util.Map
    public Set<Map.Entry<K, V>> entrySet() {
        return getCollection().getEntrySet();
    }

    @Override // java.util.Map
    public Set<K> keySet() {
        return getCollection().getKeySet();
    }

    @Override // java.util.Map
    public Collection<V> values() {
        return getCollection().getValues();
    }
}
