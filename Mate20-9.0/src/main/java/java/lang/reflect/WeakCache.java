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
    private final ReferenceQueue<K> refQueue = new ReferenceQueue<>();
    /* access modifiers changed from: private */
    public final ConcurrentMap<Supplier<V>, Boolean> reverseMap = new ConcurrentHashMap();
    private final BiFunction<K, P, ?> subKeyFactory;
    /* access modifiers changed from: private */
    public final BiFunction<K, P, V> valueFactory;

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

        /* JADX WARNING: Code restructure failed: missing block: B:7:0x001c, code lost:
            if (r1 == ((java.lang.reflect.WeakCache.CacheKey) r3).get()) goto L_0x0021;
         */
        public boolean equals(Object obj) {
            if (obj != this) {
                if (obj != null && obj.getClass() == getClass()) {
                    K k = get();
                    K key = k;
                    if (k != null) {
                    }
                }
                return false;
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public void expungeFrom(ConcurrentMap<?, ? extends ConcurrentMap<?, ?>> map, ConcurrentMap<?, Boolean> reverseMap) {
            ConcurrentMap<?, ?> valuesMap = (ConcurrentMap) map.remove(this);
            if (valuesMap != null) {
                for (Object cacheValue : valuesMap.values()) {
                    reverseMap.remove(cacheValue);
                }
            }
        }
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

        /* JADX WARNING: Code restructure failed: missing block: B:6:0x0014, code lost:
            if (r1 == ((java.lang.reflect.WeakCache.Value) r3).get()) goto L_0x0019;
         */
        public boolean equals(Object obj) {
            if (obj != this) {
                if (obj instanceof Value) {
                    V v = get();
                    V value = v;
                    if (v != null) {
                    }
                }
                return false;
            }
            return true;
        }
    }

    private final class Factory implements Supplier<V> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private final K key;
        private final P parameter;
        private final Object subKey;
        private final ConcurrentMap<Object, Supplier<V>> valuesMap;

        static {
            Class<WeakCache> cls = WeakCache.class;
        }

        Factory(K key2, P parameter2, Object subKey2, ConcurrentMap<Object, Supplier<V>> valuesMap2) {
            this.key = key2;
            this.parameter = parameter2;
            this.subKey = subKey2;
            this.valuesMap = valuesMap2;
        }

        public synchronized V get() {
            if (this.valuesMap.get(this.subKey) != this) {
                return null;
            }
            try {
                V value = Objects.requireNonNull(WeakCache.this.valueFactory.apply(this.key, this.parameter));
                if (value == null) {
                    this.valuesMap.remove(this.subKey, this);
                }
                CacheValue<V> cacheValue = new CacheValue<>(value);
                if (this.valuesMap.replace(this.subKey, this, cacheValue)) {
                    WeakCache.this.reverseMap.put(cacheValue, Boolean.TRUE);
                    return value;
                }
                throw new AssertionError((Object) "Should not reach here");
            } catch (Throwable th) {
                if (0 == 0) {
                    this.valuesMap.remove(this.subKey, this);
                }
                throw th;
            }
        }
    }

    private static final class LookupValue<V> implements Value<V> {
        private final V value;

        LookupValue(V value2) {
            this.value = value2;
        }

        public V get() {
            return this.value;
        }

        public int hashCode() {
            return System.identityHashCode(this.value);
        }

        public boolean equals(Object obj) {
            return obj == this || ((obj instanceof Value) && this.value == ((Value) obj).get());
        }
    }

    private interface Value<V> extends Supplier<V> {
    }

    public WeakCache(BiFunction<K, P, ?> subKeyFactory2, BiFunction<K, P, V> valueFactory2) {
        this.subKeyFactory = (BiFunction) Objects.requireNonNull(subKeyFactory2);
        this.valueFactory = (BiFunction) Objects.requireNonNull(valueFactory2);
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v3, resolved type: java.util.function.Supplier} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v6, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v6, resolved type: java.util.function.Supplier} */
    /* JADX WARNING: Multi-variable type inference failed */
    public V get(K key, P parameter) {
        Objects.requireNonNull(parameter);
        expungeStaleEntries();
        Object cacheKey = CacheKey.valueOf(key, this.refQueue);
        ConcurrentMap<Object, Supplier<V>> valuesMap = this.map.get(cacheKey);
        if (valuesMap == null) {
            ConcurrentMap<Object, ConcurrentMap<Object, Supplier<V>>> concurrentMap = this.map;
            ConcurrentMap<Object, Supplier<V>> concurrentHashMap = new ConcurrentHashMap<>();
            valuesMap = concurrentHashMap;
            ConcurrentMap<Object, Supplier<V>> oldValuesMap = concurrentMap.putIfAbsent(cacheKey, concurrentHashMap);
            if (oldValuesMap != null) {
                valuesMap = oldValuesMap;
            }
        }
        Object subKey = Objects.requireNonNull(this.subKeyFactory.apply(key, parameter));
        WeakCache<K, P, V>.Factory factory = null;
        Supplier<V> supplier = valuesMap.get(subKey);
        while (true) {
            WeakCache<K, P, V>.Factory factory2 = factory;
            if (supplier != null) {
                V value = supplier.get();
                if (value != null) {
                    return value;
                }
            }
            if (factory2 == null) {
                WeakCache<K, P, V>.Factory factory3 = new Factory(key, parameter, subKey, valuesMap);
                factory = factory3;
            } else {
                factory = factory2;
            }
            if (supplier == null) {
                supplier = valuesMap.putIfAbsent(subKey, factory);
                if (supplier == null) {
                    supplier = factory;
                }
            } else if (valuesMap.replace(subKey, supplier, factory)) {
                supplier = factory;
            } else {
                supplier = valuesMap.get(subKey);
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
            CacheKey<K> cacheKey2 = cacheKey;
            if (cacheKey != null) {
                cacheKey2.expungeFrom(this.map, this.reverseMap);
            } else {
                return;
            }
        }
    }
}
