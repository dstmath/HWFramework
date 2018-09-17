package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Hashtable<K, V> extends Dictionary<K, V> implements Map<K, V>, Cloneable, Serializable {
    private static final int ENTRIES = 2;
    private static final int KEYS = 0;
    private static final int MAX_ARRAY_SIZE = 2147483639;
    private static final int VALUES = 1;
    private static final long serialVersionUID = 1421746759512286392L;
    private transient int count;
    private volatile transient Set<Entry<K, V>> entrySet;
    private volatile transient Set<K> keySet;
    private float loadFactor;
    private transient int modCount;
    private transient HashtableEntry<?, ?>[] table;
    private int threshold;
    private volatile transient Collection<V> values;

    private class EntrySet extends AbstractSet<Entry<K, V>> {
        /* synthetic */ EntrySet(Hashtable this$0, EntrySet -this1) {
            this();
        }

        private EntrySet() {
        }

        public Iterator<Entry<K, V>> iterator() {
            return Hashtable.this.getIterator(2);
        }

        public boolean add(Entry<K, V> o) {
            return super.add(o);
        }

        public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> entry = (Entry) o;
            Object key = entry.getKey();
            HashtableEntry<?, ?>[] tab = Hashtable.this.table;
            int hash = key.hashCode();
            HashtableEntry<?, ?> e = tab[(Integer.MAX_VALUE & hash) % tab.length];
            while (e != null) {
                if (e.hash == hash && e.equals(entry)) {
                    return true;
                }
                e = e.next;
            }
            return false;
        }

        public boolean remove(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> entry = (Entry) o;
            Object key = entry.getKey();
            HashtableEntry<?, ?>[] tab = Hashtable.this.table;
            int hash = key.hashCode();
            int index = (Integer.MAX_VALUE & hash) % tab.length;
            HashtableEntry<K, V> e = tab[index];
            HashtableEntry prev = null;
            while (e != null) {
                if (e.hash == hash && e.equals(entry)) {
                    Hashtable hashtable = Hashtable.this;
                    hashtable.modCount = hashtable.modCount + 1;
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index] = e.next;
                    }
                    hashtable = Hashtable.this;
                    hashtable.count = hashtable.count - 1;
                    e.value = null;
                    return true;
                }
                HashtableEntry<K, V> prev2 = e;
                e = e.next;
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

        Enumerator(int type, boolean iterator) {
            this.type = type;
            this.iterator = iterator;
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
            if (e != null) {
                return true;
            }
            return false;
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
            } else {
                throw new NoSuchElementException("Hashtable Enumerator");
            }
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
            } else if (Hashtable.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                synchronized (Hashtable.this) {
                    HashtableEntry<?, ?>[] tab = Hashtable.this.table;
                    int index = (this.lastReturned.hash & Integer.MAX_VALUE) % tab.length;
                    HashtableEntry<K, V> e = tab[index];
                    HashtableEntry prev = null;
                    while (e != null) {
                        if (e == this.lastReturned) {
                            Hashtable hashtable = Hashtable.this;
                            hashtable.modCount = hashtable.modCount + 1;
                            this.expectedModCount++;
                            if (prev == null) {
                                tab[index] = e.next;
                            } else {
                                prev.next = e.next;
                            }
                            hashtable = Hashtable.this;
                            hashtable.count = hashtable.count - 1;
                            this.lastReturned = null;
                        } else {
                            HashtableEntry<K, V> prev2 = e;
                            e = e.next;
                        }
                    }
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    private static class HashtableEntry<K, V> implements Entry<K, V> {
        final int hash;
        final K key;
        HashtableEntry<K, V> next;
        V value;

        protected HashtableEntry(int hash, K key, V value, HashtableEntry<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        protected Object clone() {
            HashtableEntry hashtableEntry = null;
            int i = this.hash;
            Object obj = this.key;
            Object obj2 = this.value;
            if (this.next != null) {
                hashtableEntry = (HashtableEntry) this.next.clone();
            }
            return new HashtableEntry(i, obj, obj2, hashtableEntry);
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public V setValue(V value) {
            if (value == null) {
                throw new NullPointerException();
            }
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> e = (Entry) o;
            if (this.key != null ? !this.key.lambda$-java_util_function_Predicate_4628(e.getKey()) : e.getKey() != null) {
                if (this.value != null) {
                    z = this.value.lambda$-java_util_function_Predicate_4628(e.getValue());
                } else if (e.getValue() == null) {
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
        /* synthetic */ KeySet(Hashtable this$0, KeySet -this1) {
            this();
        }

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
        /* synthetic */ ValueCollection(Hashtable this$0, ValueCollection -this1) {
            this();
        }

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

    public Hashtable(int initialCapacity, float loadFactor) {
        this.modCount = 0;
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        } else if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        } else {
            if (initialCapacity == 0) {
                initialCapacity = 1;
            }
            this.loadFactor = loadFactor;
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
        this(Math.max(t.size() * 2, 11), 0.75f);
        putAll(t);
    }

    public synchronized int size() {
        return this.count;
    }

    public synchronized boolean isEmpty() {
        boolean z = false;
        synchronized (this) {
            if (this.count == 0) {
                z = true;
            }
        }
        return z;
    }

    public synchronized Enumeration<K> keys() {
        return getEnumeration(0);
    }

    public synchronized Enumeration<V> elements() {
        return getEnumeration(1);
    }

    public synchronized boolean contains(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        HashtableEntry<?, ?>[] tab = this.table;
        int i = tab.length;
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                return false;
            }
            for (HashtableEntry<?, ?> e = tab[i2]; e != null; e = e.next) {
                if (e.value.lambda$-java_util_function_Predicate_4628(value)) {
                    return true;
                }
            }
            i = i2;
        }
    }

    public boolean containsValue(Object value) {
        return contains(value);
    }

    public synchronized boolean containsKey(Object key) {
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        HashtableEntry<?, ?> e = tab[(Integer.MAX_VALUE & hash) % tab.length];
        while (e != null) {
            if (e.hash == hash && e.key.lambda$-java_util_function_Predicate_4628(key)) {
                return true;
            }
            e = e.next;
        }
        return false;
    }

    public synchronized V get(Object key) {
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        HashtableEntry<?, ?> e = tab[(Integer.MAX_VALUE & hash) % tab.length];
        while (e != null) {
            if (e.hash == hash && e.key.lambda$-java_util_function_Predicate_4628(key)) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }

    protected void rehash() {
        int oldCapacity = this.table.length;
        HashtableEntry<?, ?>[] oldMap = this.table;
        int newCapacity = (oldCapacity << 1) + 1;
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            if (oldCapacity != MAX_ARRAY_SIZE) {
                newCapacity = MAX_ARRAY_SIZE;
            } else {
                return;
            }
        }
        HashtableEntry<?, ?>[] newMap = new HashtableEntry[newCapacity];
        this.modCount++;
        this.threshold = (int) Math.min(((float) newCapacity) * this.loadFactor, 2.14748365E9f);
        this.table = newMap;
        int i = oldCapacity;
        while (true) {
            int i2 = i;
            i = i2 - 1;
            if (i2 > 0) {
                HashtableEntry<K, V> old = oldMap[i];
                while (old != null) {
                    HashtableEntry<K, V> e = old;
                    old = old.next;
                    int index = (e.hash & Integer.MAX_VALUE) % newCapacity;
                    e.next = newMap[index];
                    newMap[index] = e;
                }
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
        tab[index] = new HashtableEntry(hash, key, value, tab[index]);
        this.count++;
    }

    public synchronized V put(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % tab.length;
        HashtableEntry<K, V> entry = tab[index];
        while (entry != null) {
            if (entry.hash == hash && entry.key.lambda$-java_util_function_Predicate_4628(key)) {
                V old = entry.value;
                entry.value = value;
                return old;
            }
            entry = entry.next;
        }
        addEntry(hash, key, value, index);
        return null;
    }

    public synchronized V remove(Object key) {
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % tab.length;
        HashtableEntry<K, V> e = tab[index];
        HashtableEntry prev = null;
        while (e != null) {
            if (e.hash == hash && e.key.lambda$-java_util_function_Predicate_4628(key)) {
                this.modCount++;
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                this.count--;
                V oldValue = e.value;
                e.value = null;
                return oldValue;
            }
            HashtableEntry<K, V> prev2 = e;
            e = e.next;
        }
        return null;
    }

    public synchronized void putAll(Map<? extends K, ? extends V> t) {
        for (Entry<? extends K, ? extends V> e : t.entrySet()) {
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
                if (i > 0) {
                    HashtableEntry hashtableEntry;
                    HashtableEntry[] hashtableEntryArr = t.table;
                    if (this.table[i2] != null) {
                        hashtableEntry = (HashtableEntry) this.table[i2].clone();
                    } else {
                        hashtableEntry = null;
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
        } catch (Throwable e) {
            throw new InternalError(e);
        }
        return t;
    }

    public synchronized String toString() {
        int max = size() - 1;
        if (max == -1) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<K, V>> it = entrySet().iterator();
        sb.append('{');
        int i = 0;
        while (true) {
            Entry<K, V> e = (Entry) it.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == this ? "(this Map)" : key.toString());
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value.toString());
            if (i == max) {
                return sb.append('}').toString();
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

    private <T> Iterator<T> getIterator(int type) {
        if (this.count == 0) {
            return Collections.emptyIterator();
        }
        return new Enumerator(type, true);
    }

    public Set<K> keySet() {
        if (this.keySet == null) {
            this.keySet = Collections.synchronizedSet(new KeySet(this, null), this);
        }
        return this.keySet;
    }

    public Set<Entry<K, V>> entrySet() {
        if (this.entrySet == null) {
            this.entrySet = Collections.synchronizedSet(new EntrySet(this, null), this);
        }
        return this.entrySet;
    }

    public Collection<V> values() {
        if (this.values == null) {
            this.values = Collections.synchronizedCollection(new ValueCollection(this, null), this);
        }
        return this.values;
    }

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
            for (Entry<K, V> e : entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    boolean containsKey;
                    if (t.get(key) == null) {
                        containsKey = t.containsKey(key);
                    } else {
                        containsKey = false;
                    }
                    if (!containsKey) {
                        return false;
                    }
                } else if (!value.lambda$-java_util_function_Predicate_4628(t.get(key))) {
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

    /* JADX WARNING: Missing block: B:10:0x000f, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int hashCode() {
        int i = 0;
        synchronized (this) {
            int h = 0;
            if (this.count == 0 || this.loadFactor < 0.0f) {
            } else {
                this.loadFactor = -this.loadFactor;
                HashtableEntry<?, ?>[] tab = this.table;
                int length = tab.length;
                while (i < length) {
                    for (HashtableEntry<?, ?> entry = tab[i]; entry != null; entry = entry.next) {
                        h += entry.hashCode();
                    }
                    i++;
                }
                this.loadFactor = -this.loadFactor;
                return h;
            }
        }
    }

    public synchronized V getOrDefault(Object key, V defaultValue) {
        V result = get(key);
        if (result != null) {
            defaultValue = result;
        }
        return defaultValue;
    }

    public synchronized void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = this.modCount;
        for (HashtableEntry<?, ?> entry : this.table) {
            HashtableEntry<?, ?> entry2;
            while (entry2 != null) {
                action.accept(entry2.key, entry2.value);
                entry2 = entry2.next;
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
            HashtableEntry<K, V> entry2;
            while (entry2 != null) {
                entry2.value = Objects.requireNonNull(function.apply(entry2.key, entry2.value));
                entry2 = entry2.next;
                if (expectedModCount != this.modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:13:0x0029, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized V putIfAbsent(K key, V value) {
        Objects.requireNonNull(value);
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % tab.length;
        HashtableEntry<K, V> entry = tab[index];
        while (entry != null) {
            if (entry.hash == hash && entry.key.lambda$-java_util_function_Predicate_4628(key)) {
                V old = entry.value;
                if (old == null) {
                    entry.value = value;
                }
            } else {
                entry = entry.next;
            }
        }
        addEntry(hash, key, value, index);
        return null;
    }

    public synchronized boolean remove(Object key, Object value) {
        Objects.requireNonNull(value);
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % tab.length;
        HashtableEntry<K, V> e = tab[index];
        HashtableEntry prev = null;
        while (e != null) {
            if (e.hash == hash && e.key.lambda$-java_util_function_Predicate_4628(key) && e.value.lambda$-java_util_function_Predicate_4628(value)) {
                this.modCount++;
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                this.count--;
                e.value = null;
                return true;
            }
            HashtableEntry<K, V> prev2 = e;
            e = e.next;
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
            if (e.hash != hash || !e.key.lambda$-java_util_function_Predicate_4628(key)) {
                e = e.next;
            } else if (!e.value.lambda$-java_util_function_Predicate_4628(oldValue)) {
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
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        HashtableEntry<K, V> e = tab[(Integer.MAX_VALUE & hash) % tab.length];
        while (e != null) {
            if (e.hash == hash && e.key.lambda$-java_util_function_Predicate_4628(key)) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
            e = e.next;
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:17:0x0032, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % tab.length;
        HashtableEntry<K, V> e = tab[index];
        while (e != null) {
            if (e.hash == hash && e.key.lambda$-java_util_function_Predicate_4628(key)) {
                return e.value;
            }
            e = e.next;
        }
        V newValue = mappingFunction.apply(key);
        if (newValue != null) {
            addEntry(hash, key, newValue, index);
        }
    }

    /* JADX WARNING: Missing block: B:16:0x003e, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % tab.length;
        HashtableEntry<K, V> e = tab[index];
        HashtableEntry prev = null;
        while (e != null) {
            if (e.hash == hash && e.key.lambda$-java_util_function_Predicate_4628(key)) {
                V newValue = remappingFunction.apply(key, e.value);
                if (newValue == null) {
                    this.modCount++;
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index] = e.next;
                    }
                    this.count--;
                } else {
                    e.value = newValue;
                }
            } else {
                HashtableEntry<K, V> prev2 = e;
                e = e.next;
            }
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:15:0x003d, code:
            return r3;
     */
    /* JADX WARNING: Missing block: B:28:0x0058, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        V newValue;
        Objects.requireNonNull(remappingFunction);
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % tab.length;
        HashtableEntry<K, V> e = tab[index];
        HashtableEntry prev = null;
        while (e != null) {
            if (e.hash == hash && Objects.equals(e.key, key)) {
                newValue = remappingFunction.apply(key, e.value);
                if (newValue == null) {
                    this.modCount++;
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index] = e.next;
                    }
                    this.count--;
                } else {
                    e.value = newValue;
                }
            } else {
                HashtableEntry<K, V> prev2 = e;
                e = e.next;
            }
        }
        newValue = remappingFunction.apply(key, null);
        if (newValue != null) {
            addEntry(hash, key, newValue, index);
        }
    }

    /* JADX WARNING: Missing block: B:15:0x003d, code:
            return r3;
     */
    /* JADX WARNING: Missing block: B:27:0x0053, code:
            return r10;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        HashtableEntry<?, ?>[] tab = this.table;
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % tab.length;
        HashtableEntry<K, V> e = tab[index];
        HashtableEntry prev = null;
        while (e != null) {
            if (e.hash == hash && e.key.lambda$-java_util_function_Predicate_4628(key)) {
                V newValue = remappingFunction.apply(e.value, value);
                if (newValue == null) {
                    this.modCount++;
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index] = e.next;
                    }
                    this.count--;
                } else {
                    e.value = newValue;
                }
            } else {
                HashtableEntry<K, V> prev2 = e;
                e = e.next;
            }
        }
        if (value != null) {
            addEntry(hash, key, value, index);
        }
    }

    /* JADX WARNING: Missing block: B:16:0x0030, code:
            if (r1 == null) goto L_0x0042;
     */
    /* JADX WARNING: Missing block: B:17:0x0032, code:
            r8.writeObject(r1.key);
            r8.writeObject(r1.value);
            r1 = r1.next;
     */
    /* JADX WARNING: Missing block: B:21:0x0042, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeObject(ObjectOutputStream s) throws IOException {
        Throwable th;
        HashtableEntry entryStack = null;
        synchronized (this) {
            try {
                s.defaultWriteObject();
                s.writeInt(this.table.length);
                s.writeInt(this.count);
                int index = 0;
                while (index < this.table.length) {
                    HashtableEntry<Object, Object> entryStack2;
                    HashtableEntry<?, ?> entry = this.table[index];
                    HashtableEntry<Object, Object> entryStack3 = entryStack;
                    while (entry != null) {
                        try {
                            entryStack2 = new HashtableEntry(0, entry.key, entry.value, entryStack3);
                            entry = entry.next;
                            entryStack3 = entryStack2;
                        } catch (Throwable th2) {
                            th = th2;
                            entryStack2 = entryStack3;
                        }
                    }
                    index++;
                    entryStack2 = entryStack3;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
        throw th;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (this.loadFactor <= 0.0f || Float.isNaN(this.loadFactor)) {
            throw new StreamCorruptedException("Illegal Load: " + this.loadFactor);
        }
        int origlength = s.readInt();
        int elements = s.readInt();
        if (elements < 0) {
            throw new StreamCorruptedException("Illegal # of Elements: " + elements);
        }
        origlength = Math.max(origlength, ((int) (((float) elements) / this.loadFactor)) + 1);
        int length = ((int) (((float) ((elements / 20) + elements)) / this.loadFactor)) + 3;
        if (length > elements && (length & 1) == 0) {
            length--;
        }
        length = Math.min(length, origlength);
        this.table = new HashtableEntry[length];
        this.threshold = (int) Math.min(((float) length) * this.loadFactor, 2.14748365E9f);
        this.count = 0;
        while (elements > 0) {
            reconstitutionPut(this.table, s.readObject(), s.readObject());
            elements--;
        }
    }

    private void reconstitutionPut(HashtableEntry<?, ?>[] tab, K key, V value) throws StreamCorruptedException {
        if (value == null) {
            throw new StreamCorruptedException();
        }
        int hash = key.hashCode();
        int index = (Integer.MAX_VALUE & hash) % tab.length;
        HashtableEntry<?, ?> e = tab[index];
        while (e != null) {
            if (e.hash == hash && e.key.lambda$-java_util_function_Predicate_4628(key)) {
                throw new StreamCorruptedException();
            }
            e = e.next;
        }
        tab[index] = new HashtableEntry(hash, key, value, tab[index]);
        this.count++;
    }
}
