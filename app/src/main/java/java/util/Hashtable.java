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
import sun.util.logging.PlatformLogger;

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
    private transient HashtableEntry<K, V>[] table;
    private int threshold;
    private volatile transient Collection<V> values;

    private class EntrySet extends AbstractSet<Entry<K, V>> {
        private EntrySet() {
        }

        public Iterator<Entry<K, V>> iterator() {
            return Hashtable.this.getIterator(Hashtable.ENTRIES);
        }

        public boolean add(Entry<K, V> o) {
            return super.add(o);
        }

        public boolean contains(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry entry = (Entry) o;
            Object key = entry.getKey();
            HashtableEntry[] tab = Hashtable.this.table;
            int hash = Hashtable.hash(key);
            HashtableEntry e = tab[(PlatformLogger.OFF & hash) % tab.length];
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
            Entry<K, V> entry = (Entry) o;
            K key = entry.getKey();
            HashtableEntry[] tab = Hashtable.this.table;
            int hash = Hashtable.hash(key);
            int index = (PlatformLogger.OFF & hash) % tab.length;
            HashtableEntry<K, V> e = tab[index];
            HashtableEntry hashtableEntry = null;
            while (e != null) {
                if (e.hash == hash && e.equals(entry)) {
                    Hashtable hashtable = Hashtable.this;
                    hashtable.modCount = hashtable.modCount + Hashtable.VALUES;
                    if (hashtableEntry != null) {
                        hashtableEntry.next = e.next;
                    } else {
                        tab[index] = e.next;
                    }
                    hashtable = Hashtable.this;
                    hashtable.count = hashtable.count - 1;
                    e.value = null;
                    return true;
                }
                HashtableEntry<K, V> prev = e;
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
        HashtableEntry<K, V> entry;
        protected int expectedModCount;
        int index;
        boolean iterator;
        HashtableEntry<K, V> lastReturned;
        HashtableEntry[] table;
        int type;

        Enumerator(int type, boolean iterator) {
            this.table = Hashtable.this.table;
            this.index = this.table.length;
            this.entry = null;
            this.lastReturned = null;
            this.expectedModCount = Hashtable.this.modCount;
            this.type = type;
            this.iterator = iterator;
        }

        public boolean hasMoreElements() {
            HashtableEntry<K, V> e = this.entry;
            int i = this.index;
            HashtableEntry[] t = this.table;
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
            HashtableEntry<K, V> et = this.entry;
            int i = this.index;
            HashtableEntry[] t = this.table;
            while (et == null && i > 0) {
                i--;
                et = t[i];
            }
            this.entry = et;
            this.index = i;
            if (et != null) {
                HashtableEntry<K, V> e = this.entry;
                this.lastReturned = e;
                this.entry = e.next;
                if (this.type == 0) {
                    return e.key;
                }
                return this.type == Hashtable.VALUES ? e.value : e;
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
                    HashtableEntry[] tab = Hashtable.this.table;
                    int index = (this.lastReturned.hash & PlatformLogger.OFF) % tab.length;
                    HashtableEntry<K, V> e = tab[index];
                    HashtableEntry hashtableEntry = null;
                    while (e != null) {
                        if (e == this.lastReturned) {
                            Hashtable hashtable = Hashtable.this;
                            hashtable.modCount = hashtable.modCount + Hashtable.VALUES;
                            this.expectedModCount += Hashtable.VALUES;
                            if (hashtableEntry == null) {
                                tab[index] = e.next;
                            } else {
                                hashtableEntry.next = e.next;
                            }
                            hashtable = Hashtable.this;
                            hashtable.count = hashtable.count - 1;
                            this.lastReturned = null;
                        } else {
                            HashtableEntry<K, V> prev = e;
                            e = e.next;
                        }
                    }
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    static class HashtableEntry<K, V> implements Entry<K, V> {
        int hash;
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
            if (this.key.equals(e.getKey())) {
                z = this.value.equals(e.getValue());
            }
            return z;
        }

        public int hashCode() {
            return Objects.hashCode(this.key) ^ Objects.hashCode(this.value);
        }

        public String toString() {
            return this.key.toString() + "=" + this.value.toString();
        }
    }

    private class KeySet extends AbstractSet<K> {
        private KeySet() {
        }

        public Iterator<K> iterator() {
            return Hashtable.this.getIterator(Hashtable.KEYS);
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
            return Hashtable.this.getIterator(Hashtable.VALUES);
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

    private static int hash(Object k) {
        return k.hashCode();
    }

    public Hashtable(int initialCapacity, float loadFactor) {
        this.modCount = KEYS;
        this.keySet = null;
        this.entrySet = null;
        this.values = null;
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        } else if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal Load: " + loadFactor);
        } else {
            if (initialCapacity == 0) {
                initialCapacity = VALUES;
            }
            this.loadFactor = loadFactor;
            this.table = new HashtableEntry[initialCapacity];
            if (initialCapacity > 2147483640) {
                initialCapacity = 2147483640;
            }
            this.threshold = initialCapacity;
        }
    }

    public Hashtable(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    public Hashtable() {
        this(11, 0.75f);
    }

    public Hashtable(Map<? extends K, ? extends V> t) {
        this(Math.max(t.size() * ENTRIES, 11), 0.75f);
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
        return getEnumeration(KEYS);
    }

    public synchronized Enumeration<V> elements() {
        return getEnumeration(VALUES);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized boolean contains(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        HashtableEntry[] tab = this.table;
        int i = tab.length;
        loop0:
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
    }

    public boolean containsValue(Object value) {
        return contains(value);
    }

    public synchronized boolean containsKey(Object key) {
        HashtableEntry[] tab = this.table;
        int hash = hash(key);
        HashtableEntry<K, V> e = tab[(PlatformLogger.OFF & hash) % tab.length];
        while (e != null) {
            if (e.hash == hash && e.key.equals(key)) {
                return true;
            }
            e = e.next;
        }
        return false;
    }

    public synchronized V get(Object key) {
        HashtableEntry[] tab = this.table;
        int hash = hash(key);
        HashtableEntry<K, V> e = tab[(PlatformLogger.OFF & hash) % tab.length];
        while (e != null) {
            if (e.hash == hash && e.key.equals(key)) {
                return e.value;
            }
            e = e.next;
        }
        return null;
    }

    protected void rehash() {
        int oldCapacity = this.table.length;
        HashtableEntry<K, V>[] oldMap = this.table;
        int newCapacity = (oldCapacity << VALUES) + VALUES;
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            if (oldCapacity != MAX_ARRAY_SIZE) {
                newCapacity = MAX_ARRAY_SIZE;
            } else {
                return;
            }
        }
        HashtableEntry<K, V>[] newMap = new HashtableEntry[newCapacity];
        this.modCount += VALUES;
        this.threshold = (int) Math.min(((float) newCapacity) * this.loadFactor, 2.14748365E9f);
        this.table = newMap;
        int i = oldCapacity;
        while (true) {
            int i2 = i - 1;
            if (i > 0) {
                HashtableEntry<K, V> old = oldMap[i2];
                while (old != null) {
                    HashtableEntry<K, V> e = old;
                    old = old.next;
                    int index = (e.hash & PlatformLogger.OFF) % newCapacity;
                    e.next = newMap[index];
                    newMap[index] = e;
                }
                i = i2;
            } else {
                return;
            }
        }
    }

    public synchronized V put(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        HashtableEntry[] tab = this.table;
        int hash = hash(key);
        int index = (hash & PlatformLogger.OFF) % tab.length;
        HashtableEntry<K, V> e = tab[index];
        while (e != null) {
            if (e.hash == hash && e.key.equals(key)) {
                V old = e.value;
                e.value = value;
                return old;
            }
            e = e.next;
        }
        this.modCount += VALUES;
        if (this.count >= this.threshold) {
            rehash();
            tab = this.table;
            hash = hash(key);
            index = (hash & PlatformLogger.OFF) % tab.length;
        }
        tab[index] = new HashtableEntry(hash, key, value, tab[index]);
        this.count += VALUES;
        return null;
    }

    public synchronized V remove(Object key) {
        HashtableEntry[] tab = this.table;
        int hash = hash(key);
        int index = (PlatformLogger.OFF & hash) % tab.length;
        HashtableEntry<K, V> e = tab[index];
        HashtableEntry hashtableEntry = null;
        while (e != null) {
            if (e.hash == hash && e.key.equals(key)) {
                this.modCount += VALUES;
                if (hashtableEntry != null) {
                    hashtableEntry.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                this.count--;
                V oldValue = e.value;
                e.value = null;
                return oldValue;
            }
            HashtableEntry<K, V> prev = e;
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
        HashtableEntry[] tab = this.table;
        this.modCount += VALUES;
        int index = tab.length;
        while (true) {
            index--;
            if (index < 0) {
                break;
            }
            tab[index] = null;
        }
        this.count = KEYS;
    }

    public synchronized Object clone() {
        Hashtable<K, V> t;
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
                    t.modCount = KEYS;
                }
            }
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
        return t;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized String toString() {
        int max = size() - 1;
        if (max == -1) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<K, V>> it = entrySet().iterator();
        sb.append('{');
        int i = KEYS;
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
            i += VALUES;
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
            this.keySet = Collections.synchronizedSet(new KeySet(), this);
        }
        return this.keySet;
    }

    public Set<Entry<K, V>> entrySet() {
        if (this.entrySet == null) {
            this.entrySet = Collections.synchronizedSet(new EntrySet(), this);
        }
        return this.entrySet;
    }

    public Collection<V> values() {
        if (this.values == null) {
            this.values = Collections.synchronizedCollection(new ValueCollection(), this);
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
        Map<K, V> t = (Map) o;
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

    public synchronized int hashCode() {
        int i = KEYS;
        synchronized (this) {
            int h = KEYS;
            if (this.count == 0 || this.loadFactor < 0.0f) {
                return KEYS;
            }
            this.loadFactor = -this.loadFactor;
            HashtableEntry[] tab = this.table;
            int length = tab.length;
            while (i < length) {
                for (HashtableEntry<K, V> entry = tab[i]; entry != null; entry = entry.next) {
                    h += entry.hashCode();
                }
                i += VALUES;
            }
            this.loadFactor = -this.loadFactor;
            return h;
        }
    }

    public synchronized void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = this.modCount;
        HashtableEntry<?, ?>[] tab = this.table;
        int length = tab.length;
        for (int i = KEYS; i < length; i += VALUES) {
            HashtableEntry<?, ?> entry = tab[i];
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
        HashtableEntry<K, V>[] tab = this.table;
        int length = tab.length;
        for (int i = KEYS; i < length; i += VALUES) {
            HashtableEntry<K, V> entry = tab[i];
            while (entry != null) {
                entry.value = Objects.requireNonNull(function.apply(entry.key, entry.value));
                entry = entry.next;
                if (expectedModCount != this.modCount) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    public synchronized V getOrDefault(Object key, V defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    public synchronized V putIfAbsent(K key, V value) {
        return super.putIfAbsent(key, value);
    }

    public synchronized boolean remove(Object key, Object value) {
        return super.remove(key, value);
    }

    public synchronized boolean replace(K key, V oldValue, V newValue) {
        return super.replace(key, oldValue, newValue);
    }

    public synchronized V replace(K key, V value) {
        return super.replace(key, value);
    }

    public synchronized V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return super.computeIfAbsent(key, mappingFunction);
    }

    public synchronized V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.computeIfPresent(key, remappingFunction);
    }

    public synchronized V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return super.compute(key, remappingFunction);
    }

    public synchronized V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return super.merge(key, value, remappingFunction);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void writeObject(ObjectOutputStream s) throws IOException {
        Throwable th;
        HashtableEntry entryStack = null;
        synchronized (this) {
            try {
                s.defaultWriteObject();
                s.writeInt(this.table.length);
                s.writeInt(this.count);
                int index = KEYS;
                while (index < this.table.length) {
                    HashtableEntry<K, V> entryStack2;
                    HashtableEntry<K, V> entry = this.table[index];
                    HashtableEntry<K, V> entryStack3 = entryStack;
                    while (entry != null) {
                        try {
                            entryStack2 = new HashtableEntry(KEYS, entry.key, entry.value, entryStack3);
                            entry = entry.next;
                            entryStack3 = entryStack2;
                        } catch (Throwable th2) {
                            th = th2;
                            entryStack2 = entryStack3;
                        }
                    }
                    index += VALUES;
                    entryStack2 = entryStack3;
                }
                while (entryStack != null) {
                    s.writeObject(entryStack.key);
                    s.writeObject(entryStack.value);
                    entryStack = entryStack.next;
                }
            } catch (Throwable th3) {
                th = th3;
            }
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int origlength = s.readInt();
        int elements = s.readInt();
        int length = (((int) (((float) elements) * this.loadFactor)) + (elements / 20)) + 3;
        if (length > elements && (length & VALUES) == 0) {
            length--;
        }
        if (origlength > 0 && length > origlength) {
            length = origlength;
        }
        HashtableEntry<K, V>[] newTable = new HashtableEntry[length];
        this.threshold = (int) Math.min(((float) length) * this.loadFactor, 2.14748365E9f);
        this.count = KEYS;
        while (elements > 0) {
            reconstitutionPut(newTable, s.readObject(), s.readObject());
            elements--;
        }
        this.table = newTable;
    }

    private void reconstitutionPut(HashtableEntry<K, V>[] tab, K key, V value) throws StreamCorruptedException {
        if (value == null) {
            throw new StreamCorruptedException();
        }
        int hash = hash(key);
        int index = (PlatformLogger.OFF & hash) % tab.length;
        HashtableEntry<K, V> e = tab[index];
        while (e != null) {
            if (e.hash == hash && e.key.equals(key)) {
                throw new StreamCorruptedException();
            }
            e = e.next;
        }
        tab[index] = new HashtableEntry(hash, key, value, tab[index]);
        this.count += VALUES;
    }
}
