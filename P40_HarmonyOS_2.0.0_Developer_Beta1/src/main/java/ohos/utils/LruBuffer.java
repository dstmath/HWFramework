package ohos.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LruBuffer<K, V> {
    private static final int DEFAULT_CAPACITY = 64;
    private int capacity;
    private int createCount;
    private LinkedHashMap<K, V> data;
    private int matchCount;
    private int missCount;
    private int putCount;
    private int removalCount;

    /* access modifiers changed from: protected */
    public void afterRemoval(boolean z, K k, V v, V v2) {
    }

    /* access modifiers changed from: protected */
    public V createDefault(K k) {
        return null;
    }

    public LruBuffer() {
        this(64);
    }

    public LruBuffer(int i) {
        this.capacity = 0;
        if (i > 0) {
            this.data = new LinkedHashMap<>(i, 0.75f, true);
            this.capacity = i;
            return;
        }
        throw new IllegalArgumentException("capacity <= 0.");
    }

    public final int capacity() {
        int i;
        synchronized (this) {
            i = this.capacity;
        }
        return i;
    }

    private void setCapacity(int i) {
        this.capacity = i;
    }

    public final void clear() {
        synchronized (this) {
            Iterator<Map.Entry<K, V>> it = this.data.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<K, V> next = it.next();
                it.remove();
                afterRemoval(false, next.getKey(), next.getValue(), null);
            }
            this.matchCount = 0;
            this.missCount = 0;
            this.putCount = 0;
        }
    }

    public final int getMatchCount() {
        int i;
        synchronized (this) {
            i = this.matchCount;
        }
        return i;
    }

    private void addMatchCount() {
        this.matchCount++;
    }

    public final int getMissCount() {
        int i;
        synchronized (this) {
            i = this.missCount;
        }
        return i;
    }

    private void addMissCount() {
        this.missCount++;
    }

    public final int getPutCount() {
        int i;
        synchronized (this) {
            i = this.putCount;
        }
        return i;
    }

    private void addPutCount() {
        this.putCount++;
    }

    public final boolean contains(K k) {
        synchronized (this) {
            if (k != null) {
                if (this.data.containsKey(k)) {
                    this.data.get(k);
                    addMatchCount();
                    return true;
                }
            }
            addMissCount();
            return false;
        }
    }

    public final V get(K k) {
        V put;
        Objects.requireNonNull(k, "Key is null.");
        synchronized (this) {
            V v = this.data.get(k);
            if (v != null) {
                addMatchCount();
                return v;
            }
            addMissCount();
        }
        V createDefault = createDefault(k);
        if (createDefault == null) {
            return null;
        }
        synchronized (this) {
            this.createCount++;
            put = this.data.put(k, createDefault);
            if (put != null) {
                this.data.put(k, put);
            }
        }
        if (put != null) {
            afterRemoval(false, k, createDefault, put);
            return put;
        }
        synchronized (this) {
            capacityRebalance(this.capacity);
        }
        return createDefault;
    }

    public final int getCreateCount() {
        int i;
        synchronized (this) {
            i = this.createCount;
        }
        return i;
    }

    public final boolean isEmpty() {
        boolean isEmpty;
        synchronized (this) {
            isEmpty = this.data.isEmpty();
        }
        return isEmpty;
    }

    public final List<K> keys() {
        ArrayList arrayList;
        synchronized (this) {
            arrayList = new ArrayList(this.data.keySet());
        }
        return arrayList;
    }

    public final V put(K k, V v) {
        V put;
        Objects.requireNonNull(k, "key is null.");
        Objects.requireNonNull(v, "value is null.");
        synchronized (this) {
            boolean containsKey = this.data.containsKey(k);
            put = this.data.put(k, v);
            addPutCount();
            if (containsKey) {
                afterRemoval(false, k, put, v);
            } else if (this.data.size() > this.capacity) {
                capacityRebalance(this.capacity);
            }
        }
        return put;
    }

    private void capacityRebalance(int i) {
        Iterator<Map.Entry<K, V>> it = this.data.entrySet().iterator();
        while (this.data.size() > i) {
            Map.Entry<K, V> next = it.next();
            it.remove();
            this.removalCount++;
            afterRemoval(true, next.getKey(), next.getValue(), null);
        }
    }

    public final int getRemovalCount() {
        int i;
        synchronized (this) {
            i = this.removalCount;
        }
        return i;
    }

    public final Optional<V> remove(K k) {
        Objects.requireNonNull(k, "Key is null.");
        synchronized (this) {
            if (!this.data.containsKey(k)) {
                return Optional.empty();
            }
            V remove = this.data.remove(k);
            afterRemoval(false, k, remove, null);
            return Optional.of(remove);
        }
    }

    public final int size() {
        int size;
        synchronized (this) {
            size = this.data.size();
        }
        return size;
    }

    public String toString() {
        String linkedHashMap;
        synchronized (this) {
            linkedHashMap = this.data.toString();
        }
        return linkedHashMap;
    }

    public final void updateCapacity(int i) {
        if (i > 0) {
            synchronized (this) {
                if (i < this.capacity) {
                    capacityRebalance(i);
                }
                setCapacity(i);
            }
            return;
        }
        throw new IllegalArgumentException("capacity <= 0.");
    }

    public final List<V> values() {
        ArrayList arrayList;
        synchronized (this) {
            arrayList = new ArrayList(this.data.values());
        }
        return arrayList;
    }
}
