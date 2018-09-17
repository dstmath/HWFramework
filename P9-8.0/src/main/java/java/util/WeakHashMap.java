package java.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap.SimpleEntry;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class WeakHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final int MAXIMUM_CAPACITY = 1073741824;
    private static final Object NULL_KEY = new Object();
    private transient Set<java.util.Map.Entry<K, V>> entrySet;
    private final float loadFactor;
    int modCount;
    private final ReferenceQueue<Object> queue;
    private int size;
    Entry<K, V>[] table;
    private int threshold;

    private static class Entry<K, V> extends WeakReference<Object> implements java.util.Map.Entry<K, V> {
        final int hash;
        Entry<K, V> next;
        V value;

        Entry(Object key, V value, ReferenceQueue<Object> queue, int hash, Entry<K, V> next) {
            super(key, queue);
            this.value = value;
            this.hash = hash;
            this.next = next;
        }

        public K getKey() {
            return WeakHashMap.unmaskNull(get());
        }

        public V getValue() {
            return this.value;
        }

        public V setValue(V newValue) {
            V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof java.util.Map.Entry)) {
                return false;
            }
            java.util.Map.Entry<?, ?> e = (java.util.Map.Entry) o;
            K k1 = getKey();
            K k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.lambda$-java_util_function_Predicate_4628(k2))) {
                V v1 = getValue();
                V v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.lambda$-java_util_function_Predicate_4628(v2))) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        public String toString() {
            return getKey() + "=" + getValue();
        }
    }

    private abstract class HashIterator<T> implements Iterator<T> {
        private Object currentKey;
        private Entry<K, V> entry;
        private int expectedModCount = WeakHashMap.this.modCount;
        private int index;
        private Entry<K, V> lastReturned;
        private Object nextKey;

        HashIterator() {
            this.index = WeakHashMap.this.isEmpty() ? 0 : WeakHashMap.this.table.length;
        }

        public boolean hasNext() {
            Entry<K, V>[] t = WeakHashMap.this.table;
            while (this.nextKey == null) {
                Entry<K, V> e = this.entry;
                int i = this.index;
                while (e == null && i > 0) {
                    i--;
                    e = t[i];
                }
                this.entry = e;
                this.index = i;
                if (e == null) {
                    this.currentKey = null;
                    return false;
                }
                this.nextKey = e.get();
                if (this.nextKey == null) {
                    this.entry = this.entry.next;
                }
            }
            return true;
        }

        protected Entry<K, V> nextEntry() {
            if (WeakHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else if (this.nextKey != null || (hasNext() ^ 1) == 0) {
                this.lastReturned = this.entry;
                this.entry = this.entry.next;
                this.currentKey = this.nextKey;
                this.nextKey = null;
                return this.lastReturned;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            } else if (WeakHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                WeakHashMap.this.remove(this.currentKey);
                this.expectedModCount = WeakHashMap.this.modCount;
                this.lastReturned = null;
                this.currentKey = null;
            }
        }
    }

    private class EntryIterator extends HashIterator<java.util.Map.Entry<K, V>> {
        /* synthetic */ EntryIterator(WeakHashMap this$0, EntryIterator -this1) {
            this();
        }

        private EntryIterator() {
            super();
        }

        public java.util.Map.Entry<K, V> next() {
            return nextEntry();
        }
    }

    private class EntrySet extends AbstractSet<java.util.Map.Entry<K, V>> {
        /* synthetic */ EntrySet(WeakHashMap this$0, EntrySet -this1) {
            this();
        }

        private EntrySet() {
        }

        public Iterator<java.util.Map.Entry<K, V>> iterator() {
            return new EntryIterator(WeakHashMap.this, null);
        }

        public boolean contains(Object o) {
            boolean z = false;
            if (!(o instanceof java.util.Map.Entry)) {
                return false;
            }
            java.util.Map.Entry<?, ?> e = (java.util.Map.Entry) o;
            Entry<K, V> candidate = WeakHashMap.this.getEntry(e.getKey());
            if (candidate != null) {
                z = candidate.equals(e);
            }
            return z;
        }

        public boolean remove(Object o) {
            return WeakHashMap.this.removeMapping(o);
        }

        public int size() {
            return WeakHashMap.this.size();
        }

        public void clear() {
            WeakHashMap.this.clear();
        }

        private List<java.util.Map.Entry<K, V>> deepCopy() {
            List<java.util.Map.Entry<K, V>> list = new ArrayList(size());
            for (java.util.Map.Entry<K, V> e : this) {
                list.add(new SimpleEntry(e));
            }
            return list;
        }

        public Object[] toArray() {
            return deepCopy().toArray();
        }

        public <T> T[] toArray(T[] a) {
            return deepCopy().toArray(a);
        }

        public Spliterator<java.util.Map.Entry<K, V>> spliterator() {
            return new EntrySpliterator(WeakHashMap.this, 0, -1, 0, 0);
        }
    }

    static class WeakHashMapSpliterator<K, V> {
        Entry<K, V> current;
        int est;
        int expectedModCount;
        int fence;
        int index;
        final WeakHashMap<K, V> map;

        WeakHashMapSpliterator(WeakHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() {
            int hi = this.fence;
            if (hi >= 0) {
                return hi;
            }
            WeakHashMap<K, V> m = this.map;
            this.est = m.size();
            this.expectedModCount = m.modCount;
            hi = m.table.length;
            this.fence = hi;
            return hi;
        }

        public final long estimateSize() {
            getFence();
            return (long) this.est;
        }
    }

    static final class EntrySpliterator<K, V> extends WeakHashMapSpliterator<K, V> implements Spliterator<java.util.Map.Entry<K, V>> {
        EntrySpliterator(WeakHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K, V> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = (lo + hi) >>> 1;
            if (lo >= mid) {
                return null;
            }
            WeakHashMap weakHashMap = this.map;
            this.index = mid;
            int i = this.est >>> 1;
            this.est = i;
            return new EntrySpliterator(weakHashMap, lo, mid, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super java.util.Map.Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int mc;
            WeakHashMap<K, V> m = this.map;
            Entry<K, V>[] tab = m.table;
            int hi = this.fence;
            if (hi < 0) {
                mc = m.modCount;
                this.expectedModCount = mc;
                hi = tab.length;
                this.fence = hi;
            } else {
                mc = this.expectedModCount;
            }
            if (tab.length >= hi) {
                int i = this.index;
                if (i >= 0) {
                    this.index = hi;
                    if (i < hi || this.current != null) {
                        Entry<K, V> p = this.current;
                        this.current = null;
                        int i2 = i;
                        while (true) {
                            if (p == null) {
                                i = i2 + 1;
                                p = tab[i2];
                            } else {
                                Object x = p.get();
                                V v = p.value;
                                p = p.next;
                                if (x != null) {
                                    action.accept(new SimpleImmutableEntry(WeakHashMap.unmaskNull(x), v));
                                    i = i2;
                                } else {
                                    i = i2;
                                }
                            }
                            if (p == null && i >= hi) {
                                break;
                            }
                            i2 = i;
                        }
                    }
                }
            }
            if (m.modCount != mc) {
                throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super java.util.Map.Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Entry<K, V>[] tab = this.map.table;
            int length = tab.length;
            int hi = getFence();
            if (length >= hi && this.index >= 0) {
                while (true) {
                    if (this.current == null && this.index >= hi) {
                        break;
                    } else if (this.current == null) {
                        length = this.index;
                        this.index = length + 1;
                        this.current = tab[length];
                    } else {
                        Object x = this.current.get();
                        V v = this.current.value;
                        this.current = this.current.next;
                        if (x != null) {
                            action.accept(new SimpleImmutableEntry(WeakHashMap.unmaskNull(x), v));
                            if (this.map.modCount == this.expectedModCount) {
                                return true;
                            }
                            throw new ConcurrentModificationException();
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return 1;
        }
    }

    private class KeyIterator extends HashIterator<K> {
        /* synthetic */ KeyIterator(WeakHashMap this$0, KeyIterator -this1) {
            this();
        }

        private KeyIterator() {
            super();
        }

        public K next() {
            return nextEntry().getKey();
        }
    }

    private class KeySet extends AbstractSet<K> {
        /* synthetic */ KeySet(WeakHashMap this$0, KeySet -this1) {
            this();
        }

        private KeySet() {
        }

        public Iterator<K> iterator() {
            return new KeyIterator(WeakHashMap.this, null);
        }

        public int size() {
            return WeakHashMap.this.size();
        }

        public boolean contains(Object o) {
            return WeakHashMap.this.containsKey(o);
        }

        public boolean remove(Object o) {
            if (!WeakHashMap.this.containsKey(o)) {
                return false;
            }
            WeakHashMap.this.remove(o);
            return true;
        }

        public void clear() {
            WeakHashMap.this.clear();
        }

        public Spliterator<K> spliterator() {
            return new KeySpliterator(WeakHashMap.this, 0, -1, 0, 0);
        }
    }

    static final class KeySpliterator<K, V> extends WeakHashMapSpliterator<K, V> implements Spliterator<K> {
        KeySpliterator(WeakHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K, V> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = (lo + hi) >>> 1;
            if (lo >= mid) {
                return null;
            }
            WeakHashMap weakHashMap = this.map;
            this.index = mid;
            int i = this.est >>> 1;
            this.est = i;
            return new KeySpliterator(weakHashMap, lo, mid, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int mc;
            WeakHashMap<K, V> m = this.map;
            Entry<K, V>[] tab = m.table;
            int hi = this.fence;
            if (hi < 0) {
                mc = m.modCount;
                this.expectedModCount = mc;
                hi = tab.length;
                this.fence = hi;
            } else {
                mc = this.expectedModCount;
            }
            if (tab.length >= hi) {
                int i = this.index;
                if (i >= 0) {
                    this.index = hi;
                    if (i < hi || this.current != null) {
                        Entry<K, V> p = this.current;
                        this.current = null;
                        int i2 = i;
                        while (true) {
                            if (p == null) {
                                i = i2 + 1;
                                p = tab[i2];
                            } else {
                                Object x = p.get();
                                p = p.next;
                                if (x != null) {
                                    action.accept(WeakHashMap.unmaskNull(x));
                                    i = i2;
                                } else {
                                    i = i2;
                                }
                            }
                            if (p == null && i >= hi) {
                                break;
                            }
                            i2 = i;
                        }
                    }
                }
            }
            if (m.modCount != mc) {
                throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Entry<K, V>[] tab = this.map.table;
            int length = tab.length;
            int hi = getFence();
            if (length >= hi && this.index >= 0) {
                while (true) {
                    if (this.current == null && this.index >= hi) {
                        break;
                    } else if (this.current == null) {
                        length = this.index;
                        this.index = length + 1;
                        this.current = tab[length];
                    } else {
                        Object x = this.current.get();
                        this.current = this.current.next;
                        if (x != null) {
                            action.accept(WeakHashMap.unmaskNull(x));
                            if (this.map.modCount == this.expectedModCount) {
                                return true;
                            }
                            throw new ConcurrentModificationException();
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return 1;
        }
    }

    private class ValueIterator extends HashIterator<V> {
        /* synthetic */ ValueIterator(WeakHashMap this$0, ValueIterator -this1) {
            this();
        }

        private ValueIterator() {
            super();
        }

        public V next() {
            return nextEntry().value;
        }
    }

    static final class ValueSpliterator<K, V> extends WeakHashMapSpliterator<K, V> implements Spliterator<V> {
        ValueSpliterator(WeakHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K, V> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = (lo + hi) >>> 1;
            if (lo >= mid) {
                return null;
            }
            WeakHashMap weakHashMap = this.map;
            this.index = mid;
            int i = this.est >>> 1;
            this.est = i;
            return new ValueSpliterator(weakHashMap, lo, mid, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int mc;
            WeakHashMap<K, V> m = this.map;
            Entry<K, V>[] tab = m.table;
            int hi = this.fence;
            if (hi < 0) {
                mc = m.modCount;
                this.expectedModCount = mc;
                hi = tab.length;
                this.fence = hi;
            } else {
                mc = this.expectedModCount;
            }
            if (tab.length >= hi) {
                int i = this.index;
                if (i >= 0) {
                    this.index = hi;
                    if (i < hi || this.current != null) {
                        Entry<K, V> p = this.current;
                        this.current = null;
                        int i2 = i;
                        while (true) {
                            if (p == null) {
                                i = i2 + 1;
                                p = tab[i2];
                            } else {
                                Object x = p.get();
                                V v = p.value;
                                p = p.next;
                                if (x != null) {
                                    action.accept(v);
                                    i = i2;
                                } else {
                                    i = i2;
                                }
                            }
                            if (p == null && i >= hi) {
                                break;
                            }
                            i2 = i;
                        }
                    }
                }
            }
            if (m.modCount != mc) {
                throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Entry<K, V>[] tab = this.map.table;
            int length = tab.length;
            int hi = getFence();
            if (length >= hi && this.index >= 0) {
                while (true) {
                    if (this.current == null && this.index >= hi) {
                        break;
                    } else if (this.current == null) {
                        length = this.index;
                        this.index = length + 1;
                        this.current = tab[length];
                    } else {
                        Object x = this.current.get();
                        V v = this.current.value;
                        this.current = this.current.next;
                        if (x != null) {
                            action.accept(v);
                            if (this.map.modCount == this.expectedModCount) {
                                return true;
                            }
                            throw new ConcurrentModificationException();
                        }
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return 0;
        }
    }

    private class Values extends AbstractCollection<V> {
        /* synthetic */ Values(WeakHashMap this$0, Values -this1) {
            this();
        }

        private Values() {
        }

        public Iterator<V> iterator() {
            return new ValueIterator(WeakHashMap.this, null);
        }

        public int size() {
            return WeakHashMap.this.size();
        }

        public boolean contains(Object o) {
            return WeakHashMap.this.containsValue(o);
        }

        public void clear() {
            WeakHashMap.this.clear();
        }

        public Spliterator<V> spliterator() {
            return new ValueSpliterator(WeakHashMap.this, 0, -1, 0, 0);
        }
    }

    private Entry<K, V>[] newTable(int n) {
        return new Entry[n];
    }

    public WeakHashMap(int initialCapacity, float loadFactor) {
        this.queue = new ReferenceQueue();
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Initial Capacity: " + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal Load factor: " + loadFactor);
        }
        int capacity = 1;
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }
        this.table = newTable(capacity);
        this.loadFactor = loadFactor;
        this.threshold = (int) (((float) capacity) * loadFactor);
    }

    public WeakHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public WeakHashMap() {
        this(16, DEFAULT_LOAD_FACTOR);
    }

    public WeakHashMap(Map<? extends K, ? extends V> m) {
        this(Math.max(((int) (((float) m.size()) / DEFAULT_LOAD_FACTOR)) + 1, 16), DEFAULT_LOAD_FACTOR);
        putAll(m);
    }

    private static Object maskNull(Object key) {
        return key == null ? NULL_KEY : key;
    }

    static Object unmaskNull(Object key) {
        return key == NULL_KEY ? null : key;
    }

    private static boolean eq(Object x, Object y) {
        return x != y ? x.lambda$-java_util_function_Predicate_4628(y) : true;
    }

    final int hash(Object k) {
        int h = k.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        return ((h >>> 7) ^ h) ^ (h >>> 4);
    }

    private static int indexFor(int h, int length) {
        return (length - 1) & h;
    }

    private void expungeStaleEntries() {
        while (true) {
            Object x = this.queue.poll();
            if (x != null) {
                synchronized (this.queue) {
                    Entry<K, V> e = (Entry) x;
                    int i = indexFor(e.hash, this.table.length);
                    Entry<K, V> prev = this.table[i];
                    Entry<K, V> p = prev;
                    while (p != null) {
                        Entry<K, V> next = p.next;
                        if (p == e) {
                            if (prev == e) {
                                this.table[i] = next;
                            } else {
                                prev.next = next;
                            }
                            e.value = null;
                            this.size--;
                        } else {
                            prev = p;
                            p = next;
                        }
                    }
                }
            } else {
                return;
            }
        }
    }

    private Entry<K, V>[] getTable() {
        expungeStaleEntries();
        return this.table;
    }

    public int size() {
        if (this.size == 0) {
            return 0;
        }
        expungeStaleEntries();
        return this.size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public V get(Object key) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K, V>[] tab = getTable();
        Entry<K, V> e = tab[indexFor(h, tab.length)];
        while (e != null) {
            if (e.hash == h && eq(k, e.get())) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }

    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    Entry<K, V> getEntry(Object key) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K, V>[] tab = getTable();
        Entry<K, V> e = tab[indexFor(h, tab.length)];
        while (e != null) {
            if (((e.hash == h ? eq(k, e.get()) : 0) ^ 1) == 0) {
                break;
            }
            e = e.next;
        }
        return e;
    }

    public V put(K key, V value) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K, V>[] tab = getTable();
        int i = indexFor(h, tab.length);
        Entry<K, V> e = tab[i];
        while (e != null) {
            if (h == e.hash && eq(k, e.get())) {
                V oldValue = e.value;
                if (value != oldValue) {
                    e.value = value;
                }
                return oldValue;
            }
            e = e.next;
        }
        this.modCount++;
        V v = value;
        tab[i] = new Entry(k, v, this.queue, h, tab[i]);
        int i2 = this.size + 1;
        this.size = i2;
        if (i2 >= this.threshold) {
            resize(tab.length * 2);
        }
        return null;
    }

    void resize(int newCapacity) {
        Entry<K, V>[] oldTable = getTable();
        if (oldTable.length == MAXIMUM_CAPACITY) {
            this.threshold = Integer.MAX_VALUE;
            return;
        }
        Entry<K, V>[] newTable = newTable(newCapacity);
        transfer(oldTable, newTable);
        this.table = newTable;
        if (this.size >= this.threshold / 2) {
            this.threshold = (int) (((float) newCapacity) * this.loadFactor);
        } else {
            expungeStaleEntries();
            transfer(newTable, oldTable);
            this.table = oldTable;
        }
    }

    private void transfer(Entry<K, V>[] src, Entry<K, V>[] dest) {
        for (int j = 0; j < src.length; j++) {
            Entry<K, V> e = src[j];
            src[j] = null;
            while (e != null) {
                Entry<K, V> next = e.next;
                if (e.get() == null) {
                    e.next = null;
                    e.value = null;
                    this.size--;
                } else {
                    int i = indexFor(e.hash, dest.length);
                    e.next = dest[i];
                    dest[i] = e;
                }
                e = next;
            }
        }
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded != 0) {
            if (numKeysToBeAdded > this.threshold) {
                int targetCapacity = (int) ((((float) numKeysToBeAdded) / this.loadFactor) + 1.0f);
                if (targetCapacity > MAXIMUM_CAPACITY) {
                    targetCapacity = MAXIMUM_CAPACITY;
                }
                int newCapacity = this.table.length;
                while (newCapacity < targetCapacity) {
                    newCapacity <<= 1;
                }
                if (newCapacity > this.table.length) {
                    resize(newCapacity);
                }
            }
            for (java.util.Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }
    }

    public V remove(Object key) {
        Object k = maskNull(key);
        int h = hash(k);
        Entry<K, V>[] tab = getTable();
        int i = indexFor(h, tab.length);
        Entry<K, V> prev = tab[i];
        Entry<K, V> e = prev;
        while (e != null) {
            Entry<K, V> next = e.next;
            if (h == e.hash && eq(k, e.get())) {
                this.modCount++;
                this.size--;
                if (prev == e) {
                    tab[i] = next;
                } else {
                    prev.next = next;
                }
                return e.value;
            }
            prev = e;
            e = next;
        }
        return null;
    }

    boolean removeMapping(Object o) {
        if (!(o instanceof java.util.Map.Entry)) {
            return false;
        }
        Entry<K, V>[] tab = getTable();
        java.util.Map.Entry<?, ?> entry = (java.util.Map.Entry) o;
        int h = hash(maskNull(entry.getKey()));
        int i = indexFor(h, tab.length);
        Entry<K, V> prev = tab[i];
        Entry<K, V> e = prev;
        while (e != null) {
            Entry<K, V> next = e.next;
            if (h == e.hash && e.equals(entry)) {
                this.modCount++;
                this.size--;
                if (prev == e) {
                    tab[i] = next;
                } else {
                    prev.next = next;
                }
                return true;
            }
            prev = e;
            e = next;
        }
        return false;
    }

    public void clear() {
        do {
        } while (this.queue.poll() != null);
        this.modCount++;
        Arrays.fill(this.table, null);
        this.size = 0;
        do {
        } while (this.queue.poll() != null);
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            return containsNullValue();
        }
        Entry<K, V>[] tab = getTable();
        int i = tab.length;
        while (true) {
            int i2 = i;
            i = i2 - 1;
            if (i2 <= 0) {
                return false;
            }
            for (Entry<K, V> e = tab[i]; e != null; e = e.next) {
                if (value.lambda$-java_util_function_Predicate_4628(e.value)) {
                    return true;
                }
            }
        }
    }

    private boolean containsNullValue() {
        Entry<K, V>[] tab = getTable();
        int i = tab.length;
        while (true) {
            int i2 = i;
            i = i2 - 1;
            if (i2 <= 0) {
                return false;
            }
            for (Entry<K, V> e = tab[i]; e != null; e = e.next) {
                if (e.value == null) {
                    return true;
                }
            }
        }
    }

    public Set<K> keySet() {
        Set<K> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        ks = new KeySet(this, null);
        this.keySet = ks;
        return ks;
    }

    public Collection<V> values() {
        Collection<V> vs = this.values;
        if (vs != null) {
            return vs;
        }
        vs = new Values(this, null);
        this.values = vs;
        return vs;
    }

    public Set<java.util.Map.Entry<K, V>> entrySet() {
        Set<java.util.Map.Entry<K, V>> es = this.entrySet;
        if (es != null) {
            return es;
        }
        es = new EntrySet(this, null);
        this.entrySet = es;
        return es;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = this.modCount;
        for (Entry<K, V> entry : getTable()) {
            Entry<K, V> entry2;
            while (entry2 != null) {
                Object key = entry2.get();
                if (key != null) {
                    action.accept(unmaskNull(key), entry2.value);
                }
                entry2 = entry2.next;
                if (expectedModCount != this.modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        int expectedModCount = this.modCount;
        for (Entry<K, V> entry : getTable()) {
            Entry<K, V> entry2;
            while (entry2 != null) {
                Object key = entry2.get();
                if (key != null) {
                    entry2.value = function.apply(unmaskNull(key), entry2.value);
                }
                entry2 = entry2.next;
                if (expectedModCount != this.modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }
}
