package java.lang.reflect;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

final class WeakCache<K, P, V> {
    private final ConcurrentMap<Object, ConcurrentMap<Object, Supplier<V>>> map = new ConcurrentHashMap();
    private final ReferenceQueue<K> refQueue = new ReferenceQueue();
    private final ConcurrentMap<Supplier<V>, Boolean> reverseMap = new ConcurrentHashMap();
    private final BiFunction<K, P, ?> subKeyFactory;
    private final BiFunction<K, P, V> valueFactory;

    private static final class CacheKey<K> extends WeakReference<K> {
        private static final Object NULL_KEY = new Object();
        private final int hash;

        static <K> Object valueOf(K key, ReferenceQueue<K> refQueue) {
            if (key == null) {
                return NULL_KEY;
            }
            return new CacheKey(key, refQueue);
        }

        private CacheKey(K key, ReferenceQueue<K> refQueue) {
            super(key, refQueue);
            this.hash = System.identityHashCode(key);
        }

        public int hashCode() {
            return this.hash;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj != null && obj.getClass() == getClass()) {
                K key = get();
                if (key != null) {
                    return key == ((CacheKey) obj).get();
                }
            }
            return false;
        }

        void expungeFrom(ConcurrentMap<?, ? extends ConcurrentMap<?, ?>> map, ConcurrentMap<?, Boolean> reverseMap) {
            ConcurrentMap<?, ?> valuesMap = (ConcurrentMap) map.remove(this);
            if (valuesMap != null) {
                for (Object cacheValue : valuesMap.values()) {
                    reverseMap.remove(cacheValue);
                }
            }
        }
    }

    private interface Value<V> extends Supplier<V> {
    }

    private static final class CacheValue<V> extends WeakReference<V> implements Value<V> {
        private final int hash;

        CacheValue(V value) {
            super(value);
            this.hash = System.identityHashCode(value);
        }

        public int hashCode() {
            return this.hash;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Value) {
                V value = get();
                if (value != null) {
                    return value == ((Value) obj).get();
                }
            }
            return false;
        }
    }

    private final class Factory implements Supplier<V> {
        static final /* synthetic */ boolean -assertionsDisabled = (Factory.class.desiredAssertionStatus() ^ 1);
        final /* synthetic */ boolean $assertionsDisabled;
        private final K key;
        private final P parameter;
        private final Object subKey;
        private final ConcurrentMap<Object, Supplier<V>> valuesMap;

        Factory(K key, P parameter, Object subKey, ConcurrentMap<Object, Supplier<V>> valuesMap) {
            this.key = key;
            this.parameter = parameter;
            this.subKey = subKey;
            this.valuesMap = valuesMap;
        }

        public synchronized V get() {
            if (((Supplier) this.valuesMap.get(this.subKey)) != this) {
                return null;
            }
            V value = null;
            try {
                value = Objects.requireNonNull(WeakCache.this.valueFactory.apply(this.key, this.parameter));
                if (value == null) {
                }
                if (-assertionsDisabled || value != null) {
                    CacheValue<V> cacheValue = new CacheValue(value);
                    if (this.valuesMap.replace(this.subKey, this, cacheValue)) {
                        WeakCache.this.reverseMap.put(cacheValue, Boolean.TRUE);
                        return value;
                    }
                    throw new AssertionError((Object) "Should not reach here");
                }
                throw new AssertionError();
            } finally {
                this.valuesMap.remove(this.subKey, this);
            }
        }
    }

    private static final class LookupValue<V> implements Value<V> {
        private final V value;

        LookupValue(V value) {
            this.value = value;
        }

        public V get() {
            return this.value;
        }

        public int hashCode() {
            return System.identityHashCode(this.value);
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Value) {
                return this.value == ((Value) obj).get();
            } else {
                return false;
            }
        }
    }

    public WeakCache(BiFunction<K, P, ?> subKeyFactory, BiFunction<K, P, V> valueFactory) {
        this.subKeyFactory = (BiFunction) Objects.requireNonNull(subKeyFactory);
        this.valueFactory = (BiFunction) Objects.requireNonNull(valueFactory);
    }

    public V get(K key, P parameter) {
        Objects.requireNonNull(parameter);
        expungeStaleEntries();
        Object cacheKey = CacheKey.valueOf(key, this.refQueue);
        ConcurrentMap<Object, Supplier<V>> valuesMap = (ConcurrentMap) this.map.get(cacheKey);
        if (valuesMap == null) {
            ConcurrentMap concurrentMap = this.map;
            valuesMap = new ConcurrentHashMap();
            ConcurrentMap<Object, Supplier<V>> oldValuesMap = (ConcurrentMap) concurrentMap.putIfAbsent(cacheKey, valuesMap);
            if (oldValuesMap != null) {
                valuesMap = oldValuesMap;
            }
        }
        Object subKey = Objects.requireNonNull(this.subKeyFactory.apply(key, parameter));
        Supplier<V> supplier = (Supplier) valuesMap.get(subKey);
        Object obj = null;
        while (true) {
            if (supplier != null) {
                V value = supplier.get();
                if (value != null) {
                    return value;
                }
            }
            if (obj == null) {
                obj = new Factory(key, parameter, subKey, valuesMap);
            }
            if (supplier == null) {
                supplier = (Supplier) valuesMap.putIfAbsent(subKey, obj);
                if (supplier == null) {
                    supplier = obj;
                }
            } else if (valuesMap.replace(subKey, supplier, obj)) {
                supplier = obj;
            } else {
                supplier = (Supplier) valuesMap.get(subKey);
            }
        }
    }

    public boolean containsValue(V value) {
        Objects.requireNonNull(value);
        expungeStaleEntries();
        return this.reverseMap.containsKey(new LookupValue(value));
    }

    public int size() {
        expungeStaleEntries();
        return this.reverseMap.size();
    }

    private void expungeStaleEntries() {
        while (true) {
            CacheKey<K> cacheKey = (CacheKey) this.refQueue.poll();
            if (cacheKey != null) {
                cacheKey.expungeFrom(this.map, this.reverseMap);
            } else {
                return;
            }
        }
    }
}
