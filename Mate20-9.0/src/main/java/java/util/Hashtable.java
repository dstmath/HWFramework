package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.lang.annotation.RCWeakRef;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Hashtable<K, V> extends Dictionary<K, V> implements Map<K, V>, Cloneable, Serializable {
    private static final int ENTRIES = 2;
    private static final int KEYS = 0;
    private static final int MAX_ARRAY_SIZE = 2147483639;
    private static final int VALUES = 1;
    private static final long serialVersionUID = 1421746759512286392L;
    /* access modifiers changed from: private */
    public transient int count;
    @RCWeakRef
    private volatile transient Set<Map.Entry<K, V>> entrySet;
    @RCWeakRef
    private volatile transient Set<K> keySet;
    private float loadFactor;
    /* access modifiers changed from: private */
    public transient int modCount;
    /* access modifiers changed from: private */
    public transient HashtableEntry<?, ?>[] table;
    private int threshold;
    @RCWeakRef
    private volatile transient Collection<V> values;

    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        private EntrySet() {
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            return Hashtable.this.getIterator(2);
        }

        public boolean add(Map.Entry<K, V> o) {
            return super.add(o);
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry) o;
            Object key = entry.getKey();
            HashtableEntry<K, V>[] access$400 = Hashtable.this.table;
            int hash = key.hashCode();
            for (HashtableEntry<K, V> hashtableEntry = access$400[(Integer.MAX_VALUE & hash) % access$400.length]; hashtableEntry != null; hashtableEntry = hashtableEntry.next) {
                if (hashtableEntry.hash == hash && hashtableEntry.equals(entry)) {
                    return true;
                }
            }
            return false;
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry) o;
            Object key = entry.getKey();
            HashtableEntry<K, V>[] access$400 = Hashtable.this.table;
            int hash = key.hashCode();
            int index = (Integer.MAX_VALUE & hash) % access$400.length;
            HashtableEntry<K, V> e = access$400[index];
            HashtableEntry<K, V> prev = null;
            while (e != null) {
                if (e.hash != hash || !e.equals(entry)) {
                    prev = e;
                    e = e.next;
                } else {
                    int unused = Hashtable.this.modCount = Hashtable.this.modCount + 1;
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        access$400[index] = e.next;
                    }
                    Hashtable.access$210(Hashtable.this);
                    e.value = null;
                    return true;
                }
            }
            return false;
        }

        public int size() {
            return Hashtable.this.count;
        }

        public void clear() {
            Hashtable.this.clear();
        }
    }

    private class Enumerator<T> implements Enumeration<T>, Iterator<T> {
        HashtableEntry<?, ?> entry;
        protected int expectedModCount = Hashtable.this.modCount;
        int index = this.table.length;
        boolean iterator;
        HashtableEntry<?, ?> lastReturned;
        HashtableEntry<?, ?>[] table = Hashtable.this.table;
        int type;

        Enumerator(int type2, boolean iterator2) {
            this.type = type2;
            this.iterator = iterator2;
        }

        public boolean hasMoreElements() {
            HashtableEntry<?, ?> e = this.entry;
            int i = this.index;
            HashtableEntry<?, ?>[] t = this.table;
            while (e == null && i > 0) {
                i--;
                e = t[i];
            }
            this.entry = e;
            this.index = i;
            return e != null;
        }

        public T nextElement() {
            HashtableEntry<?, ?> et = this.entry;
            int i = this.index;
            HashtableEntry<?, ?>[] t = this.table;
            while (et == null && i > 0) {
                i--;
                et = t[i];
            }
            this.entry = et;
            this.index = i;
            if (et != null) {
                HashtableEntry<?, ?> e = this.entry;
                this.lastReturned = e;
                this.entry = e.next;
                if (this.type == 0) {
                    return e.key;
                }
                return this.type == 1 ? e.value : e;
            }
            throw new NoSuchElementException("Hashtable Enumerator");
        }

        public boolean hasNext() {
            return hasMoreElements();
        }

        public T next() {
            if (Hashtable.this.modCount == this.expectedModCount) {
                return nextElement();
            }
            throw new ConcurrentModificationException();
        }

        public void remove() {
            if (!this.iterator) {
                throw new UnsupportedOperationException();
            } else if (this.lastReturned == null) {
                throw new IllegalStateException("Hashtable Enumerator");
            } else if (Hashtable.this.modCount == this.expectedModCount) {
                synchronized (Hashtable.this) {
                    HashtableEntry<K, V>[] access$400 = Hashtable.this.table;
                    int index2 = (this.lastReturned.hash & Integer.MAX_VALUE) % access$400.length;
                    HashtableEntry<K, V> e = access$400[index2];
                    HashtableEntry<K, V> prev = null;
                    while (e != null) {
                        if (e == this.lastReturned) {
                            int unused = Hashtable.this.modCount = Hashtable.this.modCount + 1;
                            this.expectedModCount++;
                            if (prev == null) {
                                access$400[index2] = e.next;
                            } else {
                                prev.next = e.next;
                            }
                            Hashtable.access$210(Hashtable.this);
                            this.lastReturned = null;
                        } else {
                            prev = e;
                            e = e.next;
                        }
                    }
                    throw new ConcurrentModificationException();
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    private static class HashtableEntry<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        HashtableEntry<K, V> next;
        V value;

        protected HashtableEntry(int hash2, K key2, V value2, HashtableEntry<K, V> next2) {
            this.hash = hash2;
            this.key = key2;
            this.value = value2;
            this.next = next2;
        }

        /* access modifiers changed from: protected */
        public Object clone() {
            return new HashtableEntry(this.hash, this.key, this.value, this.next == null ? null : (HashtableEntry) this.next.clone());
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public V setValue(V value2) {
            if (value2 != null) {
                V oldValue = this.value;
                this.value = value2;
                return oldValue;
            }
            throw new NullPointerException();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry) o;
            if (this.key != null ? this.key.equals(e.getKey()) : e.getKey() == null) {
                if (this.value != null ? this.value.equals(e.getValue()) : e.getValue() == null) {
                    z = true;
                }
            }
            return z;
        }

        public int hashCode() {
            return this.hash ^ Objects.hashCode(this.value);
        }

        public String toString() {
            return this.key.toString() + "=" + this.value.toString();
        }
    }

    private class KeySet extends AbstractSet<K> {
        private KeySet() {
        }

        public Iterator<K> iterator() {
            return Hashtable.this.getIterator(0);
        }

        public int size() {
            return Hashtable.this.count;
        }

        public boolean contains(Object o) {
            return Hashtable.this.containsKey(o);
        }

        public boolean remove(Object o) {
            return Hashtable.this.remove(o) != null;
        }

        public void clear() {
            Hashtable.this.clear();
        }
    }

    private class ValueCollection extends AbstractCollection<V> {
        private ValueCollection() {
        }

        public Iterator<V> iterator() {
            return Hashtable.this.getIterator(1);
        }

        public int size() {
            return Hashtable.this.count;
        }

        public boolean contains(Object o) {
            return Hashtable.this.containsValue(o);
        }

        public void clear() {
            Hashtable.this.clear();
        }
    }

    static /* synthetic */ int access$210(Hashtable x0) {
        int i = x0.count;
        x0.count = i - 1;
        return i;
    }

    public Hashtable(int initialCapacity, float loadFactor2) {
        this.modCount = 0;
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        } else if (loadFactor2 <= 0.0f || Float.isNaN(loadFactor2)) {
            throw new IllegalArgumentException("Illegal Load: " + loadFactor2);
        } else {
            initialCapacity = initialCapacity == 0 ? 1 : initialCapacity;
            this.loadFactor = loadFactor2;
            this.table = new HashtableEntry[initialCapacity];
            this.threshold = Math.min(initialCapacity, 2147483640);
        }
    }

    public Hashtable(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    public Hashtable() {
        this(11, 0.75f);
    }

    public Hashtable(Map<? extends K, ? extends V> t) {
        this(Math.max(2 * t.size(), 11), 0.75f);
        putAll(t);
    }

    public synchronized int size() {
        return this.count;
    }

    public synchronized boolean isEmpty() {
        return this.count == 0;
    }

    public synchronized Enumeration<K> keys() {
        return getEnumeration(0);
    }

    public synchronized Enumeration<V> elements() {
        return getEnumeration(1);
    }

    public synchronized boolean contains(Object value) {
        if (value != null) {
            HashtableEntry<?, ?>[] tab = this.table;
            int i = tab.length;
            while (true) {
                int i2 = i - 1;
                if (i <= 0) {
                    return false;
                }
                for (HashtableEntry<K, V> e = tab[i2]; e != null; e = e.next) {
                    if (e.value.equals(value)) {
                        return true;
                    }
                }
                i = i2;
            }
        } else {
            throw new NullPointerException();
        }
    }

    public boolean containsValue(Object value) {
        return contains(value);
    }

    public synchronized boolean containsKey(Object key) {
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        for (HashtableEntry<K, V> e = tab[(Integer.MAX_VALUE & hash) % tab.length]; e != null; e = e.next) {
            if (e.hash == hash && e.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    public synchronized V get(Object key) {
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        HashtableEntry<K, V> e = tab[(Integer.MAX_VALUE & hash) % tab.length];
        while (e != null) {
            if (e.hash != hash || !e.key.equals(key)) {
                e = e.next;
            } else {
                return e.value;
            }
        }
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: java.util.Hashtable$HashtableEntry<K, V>[]} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v1, resolved type: java.util.Hashtable$HashtableEntry<K, V>} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    public void rehash() {
        int oldCapacity = this.table.length;
        HashtableEntry[] hashtableEntryArr = this.table;
        int newCapacity = (oldCapacity << 1) + 1;
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            if (oldCapacity != MAX_ARRAY_SIZE) {
                newCapacity = MAX_ARRAY_SIZE;
            } else {
                return;
            }
        }
        HashtableEntry<K, V>[] hashtableEntryArr2 = new HashtableEntry[newCapacity];
        this.modCount++;
        this.threshold = (int) Math.min(((float) newCapacity) * this.loadFactor, 2.14748365E9f);
        this.table = hashtableEntryArr2;
        int i = oldCapacity;
        while (true) {
            int i2 = i - 1;
            if (i > 0) {
                HashtableEntry hashtableEntry = hashtableEntryArr[i2];
                while (hashtableEntry != null) {
                    HashtableEntry hashtableEntry2 = hashtableEntry;
                    hashtableEntry = hashtableEntry.next;
                    int index = (hashtableEntry2.hash & Integer.MAX_VALUE) % newCapacity;
                    hashtableEntry2.next = hashtableEntryArr2[index];
                    hashtableEntryArr2[index] = hashtableEntry2;
                }
                i = i2;
            } else {
                return;
            }
        }
    }

    private void addEntry(int hash, K key, V value, int index) {
        this.modCount++;
        HashtableEntry<?, ?>[] tab = this.table;
        if (this.count >= this.threshold) {
            rehash();
            tab = this.table;
            hash = key.hashCode();
            index = (Integer.MAX_VALUE & hash) % tab.length;
        }
        tab[index] = new HashtableEntry<>(hash, key, value, tab[index]);
        this.count++;
    }

    public synchronized V put(K key, V value) {
        if (value != null) {
            V[] tab = this.table;
            int hash = key.hashCode();
            int index = (Integer.MAX_VALUE & hash) % tab.length;
            V v = tab[index];
            while (v != null) {
                if (v.hash != hash || !v.key.equals(key)) {
                    v = v.next;
                } else {
                    V old = v.value;
                    v.value = value;
                    return old;
                }
            }
            addEntry(hash, key, value, index);
            return null;
        }
        throw new NullPointerException();
    }

    public synchronized V remove(Object key) {
        HashtableEntry[] hashtableEntryArr = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % hashtableEntryArr.length;
        HashtableEntry<K, V> e = hashtableEntryArr[index];
        HashtableEntry hashtableEntry = null;
        while (e != null) {
            if (e.hash != hash || !e.key.equals(key)) {
                hashtableEntry = e;
                e = e.next;
            } else {
                this.modCount++;
                if (hashtableEntry != null) {
                    hashtableEntry.next = e.next;
                } else {
                    hashtableEntryArr[index] = e.next;
                }
                this.count--;
                V oldValue = e.value;
                e.value = null;
                return oldValue;
            }
        }
        return null;
    }

    public synchronized void putAll(Map<? extends K, ? extends V> t) {
        for (Map.Entry<? extends K, ? extends V> e : t.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    public synchronized void clear() {
        HashtableEntry<?, ?>[] tab = this.table;
        this.modCount++;
        int index = tab.length;
        while (true) {
            index--;
            if (index >= 0) {
                tab[index] = null;
            } else {
                this.count = 0;
            }
        }
    }

    public synchronized Object clone() {
        Hashtable<?, ?> t;
        try {
            t = (Hashtable) super.clone();
            t.table = new HashtableEntry[this.table.length];
            int i = this.table.length;
            while (true) {
                int i2 = i - 1;
                HashtableEntry<?, ?> hashtableEntry = null;
                if (i > 0) {
                    HashtableEntry<?, ?>[] hashtableEntryArr = t.table;
                    if (this.table[i2] != null) {
                        hashtableEntry = (HashtableEntry) this.table[i2].clone();
                    }
                    hashtableEntryArr[i2] = hashtableEntry;
                    i = i2;
                } else {
                    t.keySet = null;
                    t.entrySet = null;
                    t.values = null;
                    t.modCount = 0;
                }
            }
        } catch (CloneNotSupportedException e) {
            throw new InternalError((Throwable) e);
        }
        return t;
    }

    public synchronized String toString() {
        int max = size() - 1;
        if (max == -1) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<K, V>> it = entrySet().iterator();
        sb.append('{');
        int i = 0;
        while (true) {
            Map.Entry<K, V> e = it.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == this ? "(this Map)" : key.toString());
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value.toString());
            if (i == max) {
                sb.append('}');
                return sb.toString();
            }
            sb.append(", ");
            i++;
        }
    }

    private <T> Enumeration<T> getEnumeration(int type) {
        if (this.count == 0) {
            return Collections.emptyEnumeration();
        }
        return new Enumerator(type, false);
    }

    /* access modifiers changed from: private */
    public <T> Iterator<T> getIterator(int type) {
        if (this.count == 0) {
            return Collections.emptyIterator();
        }
        return new Enumerator(type, true);
    }

    public Set<K> keySet() {
        Set<K> res = this.keySet;
        if (res != null) {
            return res;
        }
        Set<K> res2 = Collections.synchronizedSet(new KeySet(), this);
        this.keySet = res2;
        return res2;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> res = this.entrySet;
        if (res != null) {
            return res;
        }
        Set<Map.Entry<K, V>> res2 = Collections.synchronizedSet(new EntrySet(), this);
        this.entrySet = res2;
        return res2;
    }

    public Collection<V> values() {
        Collection<V> res = this.values;
        if (res != null) {
            return res;
        }
        Collection<V> res2 = Collections.synchronizedCollection(new ValueCollection(), this);
        this.values = res2;
        return res2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0047, code lost:
        return false;
     */
    public synchronized boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Map)) {
            return false;
        }
        Map<?, ?> t = (Map) o;
        if (t.size() != size()) {
            return false;
        }
        try {
            for (Map.Entry<K, V> e : entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (t.get(key) != null || !t.containsKey(key)) {
                    }
                } else if (!value.equals(t.get(key))) {
                    return false;
                }
            }
            return true;
        } catch (ClassCastException e2) {
            return false;
        } catch (NullPointerException e3) {
            return false;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0031, code lost:
        return 0;
     */
    public synchronized int hashCode() {
        int h = 0;
        if (this.count != 0) {
            if (this.loadFactor >= 0.0f) {
                this.loadFactor = -this.loadFactor;
                for (HashtableEntry<K, V> entry : this.table) {
                    while (entry != null) {
                        h += entry.hashCode();
                        entry = entry.next;
                    }
                }
                this.loadFactor = -this.loadFactor;
                return h;
            }
        }
    }

    public synchronized V getOrDefault(Object key, V defaultValue) {
        V result;
        result = get(key);
        return result == null ? defaultValue : result;
    }

    public synchronized void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = this.modCount;
        for (HashtableEntry<K, V> entry : this.table) {
            while (entry != null) {
                action.accept(entry.key, entry.value);
                entry = entry.next;
                if (expectedModCount != this.modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    public synchronized void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        int expectedModCount = this.modCount;
        for (HashtableEntry<K, V> entry : this.table) {
            while (entry != null) {
                entry.value = Objects.requireNonNull(function.apply(entry.key, entry.value));
                entry = entry.next;
                if (expectedModCount != this.modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0027, code lost:
        return r4;
     */
    public synchronized V putIfAbsent(K key, V value) {
        Objects.requireNonNull(value);
        V[] tab = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % tab.length;
        V v = tab[index];
        while (v != null) {
            if (v.hash != hash || !v.key.equals(key)) {
                v = v.next;
            } else {
                V old = v.value;
                if (old == null) {
                    v.value = value;
                }
            }
        }
        addEntry(hash, key, value, index);
        return null;
    }

    public synchronized boolean remove(Object key, Object value) {
        Objects.requireNonNull(value);
        HashtableEntry[] hashtableEntryArr = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % hashtableEntryArr.length;
        HashtableEntry<K, V> e = hashtableEntryArr[index];
        HashtableEntry hashtableEntry = null;
        while (e != null) {
            if (e.hash != hash || !e.key.equals(key) || !e.value.equals(value)) {
                hashtableEntry = e;
                e = e.next;
            } else {
                this.modCount++;
                if (hashtableEntry != null) {
                    hashtableEntry.next = e.next;
                } else {
                    hashtableEntryArr[index] = e.next;
                }
                this.count--;
                e.value = null;
                return true;
            }
        }
        return false;
    }

    public synchronized boolean replace(K key, V oldValue, V newValue) {
        Objects.requireNonNull(oldValue);
        Objects.requireNonNull(newValue);
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        HashtableEntry<K, V> e = tab[(Integer.MAX_VALUE & hash) % tab.length];
        while (e != null) {
            if (e.hash != hash || !e.key.equals(key)) {
                e = e.next;
            } else if (!e.value.equals(oldValue)) {
                return false;
            } else {
                e.value = newValue;
                return true;
            }
        }
        return false;
    }

    public synchronized V replace(K key, V value) {
        Objects.requireNonNull(value);
        V[] tab = this.table;
        int hash = key.hashCode();
        V v = tab[(Integer.MAX_VALUE & hash) % tab.length];
        while (v != null) {
            if (v.hash != hash || !v.key.equals(key)) {
                v = v.next;
            } else {
                V oldValue = v.value;
                v.value = value;
                return oldValue;
            }
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0032, code lost:
        return r4;
     */
    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % tab.length;
        HashtableEntry<K, V> e = tab[index];
        while (e != null) {
            if (e.hash != hash || !e.key.equals(key)) {
                e = e.next;
            } else {
                return e.value;
            }
        }
        V newValue = mappingFunction.apply(key);
        if (newValue != null) {
            addEntry(hash, key, newValue, index);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0046, code lost:
        return r4;
     */
    public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        HashtableEntry[] hashtableEntryArr = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % hashtableEntryArr.length;
        HashtableEntry<K, V> e = hashtableEntryArr[index];
        HashtableEntry hashtableEntry = null;
        while (e != null) {
            if (e.hash != hash || !e.key.equals(key)) {
                hashtableEntry = e;
                e = e.next;
            } else {
                V newValue = remappingFunction.apply(key, e.value);
                if (newValue == null) {
                    this.modCount++;
                    if (hashtableEntry != null) {
                        hashtableEntry.next = e.next;
                    } else {
                        hashtableEntryArr[index] = e.next;
                    }
                    this.count--;
                } else {
                    e.value = newValue;
                }
            }
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0046, code lost:
        return r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0056, code lost:
        return r3;
     */
    public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        HashtableEntry[] hashtableEntryArr = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % hashtableEntryArr.length;
        HashtableEntry<K, V> e = hashtableEntryArr[index];
        HashtableEntry hashtableEntry = null;
        while (e != null) {
            if (e.hash != hash || !Objects.equals(e.key, key)) {
                hashtableEntry = e;
                e = e.next;
            } else {
                V newValue = remappingFunction.apply(key, e.value);
                if (newValue == null) {
                    this.modCount++;
                    if (hashtableEntry != null) {
                        hashtableEntry.next = e.next;
                    } else {
                        hashtableEntryArr[index] = e.next;
                    }
                    this.count--;
                } else {
                    e.value = newValue;
                }
            }
        }
        V newValue2 = remappingFunction.apply(key, null);
        if (newValue2 != null) {
            addEntry(hash, key, newValue2, index);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0044, code lost:
        return r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0050, code lost:
        return r9;
     */
    public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        HashtableEntry[] hashtableEntryArr = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % hashtableEntryArr.length;
        HashtableEntry<K, V> e = hashtableEntryArr[index];
        HashtableEntry hashtableEntry = null;
        while (e != null) {
            if (e.hash != hash || !e.key.equals(key)) {
                hashtableEntry = e;
                e = e.next;
            } else {
                V newValue = remappingFunction.apply(e.value, value);
                if (newValue == null) {
                    this.modCount++;
                    if (hashtableEntry != null) {
                        hashtableEntry.next = e.next;
                    } else {
                        hashtableEntryArr[index] = e.next;
                    }
                    this.count--;
                } else {
                    e.value = newValue;
                }
            }
        }
        if (value != null) {
            addEntry(hash, key, value, index);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0030, code lost:
        if (r2 == null) goto L_0x003f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0032, code lost:
        r8.writeObject(r2.key);
        r8.writeObject(r2.value);
        r2 = r2.next;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x003f, code lost:
        return;
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        synchronized (this) {
            try {
                s.defaultWriteObject();
                s.writeInt(this.table.length);
                s.writeInt(this.count);
                HashtableEntry<K, V> hashtableEntry = null;
                int index = 0;
                while (index < this.table.length) {
                    try {
                        for (HashtableEntry<K, V> entry = this.table[index]; entry != null; entry = entry.next) {
                            hashtableEntry = new HashtableEntry<>(0, entry.key, entry.value, hashtableEntry);
                        }
                        index++;
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (this.loadFactor <= 0.0f || Float.isNaN(this.loadFactor)) {
            throw new StreamCorruptedException("Illegal Load: " + this.loadFactor);
        }
        int origlength = s.readInt();
        int elements = s.readInt();
        if (elements >= 0) {
            int origlength2 = Math.max(origlength, ((int) (((float) elements) / this.loadFactor)) + 1);
            int length = ((int) (((float) ((elements / 20) + elements)) / this.loadFactor)) + 3;
            if (length > elements && (length & 1) == 0) {
                length--;
            }
            int length2 = Math.min(length, origlength2);
            this.table = new HashtableEntry[length2];
            this.threshold = (int) Math.min(((float) length2) * this.loadFactor, 2.14748365E9f);
            this.count = 0;
            while (elements > 0) {
                reconstitutionPut(this.table, s.readObject(), s.readObject());
                elements--;
            }
            return;
        }
        throw new StreamCorruptedException("Illegal # of Elements: " + elements);
    }

    private void reconstitutionPut(HashtableEntry<?, ?>[] tab, K key, V value) throws StreamCorruptedException {
        if (value != null) {
            int hash = key.hashCode();
            int index = (Integer.MAX_VALUE & hash) % tab.length;
            HashtableEntry<K, V> hashtableEntry = tab[index];
            while (hashtableEntry != null) {
                if (hashtableEntry.hash != hash || !hashtableEntry.key.equals(key)) {
                    hashtableEntry = hashtableEntry.next;
                } else {
                    throw new StreamCorruptedException();
                }
            }
            tab[index] = new HashtableEntry<>(hash, key, value, tab[index]);
            this.count++;
            return;
        }
        throw new StreamCorruptedException();
    }
}
