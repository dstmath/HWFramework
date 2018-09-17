package java.util;

import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class LinkedHashMap<K, V> extends HashMap<K, V> implements Map<K, V> {
    private static final long serialVersionUID = 3801124242820219131L;
    private final boolean accessOrder;
    private transient LinkedHashMapEntry<K, V> header;

    private abstract class LinkedHashIterator<T> implements Iterator<T> {
        int expectedModCount;
        LinkedHashMapEntry<K, V> lastReturned;
        LinkedHashMapEntry<K, V> nextEntry;

        private LinkedHashIterator() {
            this.nextEntry = LinkedHashMap.this.header.after;
            this.lastReturned = null;
            this.expectedModCount = LinkedHashMap.this.modCount;
        }

        public boolean hasNext() {
            return this.nextEntry != LinkedHashMap.this.header;
        }

        public void remove() {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            } else if (LinkedHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                LinkedHashMap.this.remove(this.lastReturned.key);
                this.lastReturned = null;
                this.expectedModCount = LinkedHashMap.this.modCount;
            }
        }

        Entry<K, V> nextEntry() {
            if (LinkedHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else if (this.nextEntry == LinkedHashMap.this.header) {
                throw new NoSuchElementException();
            } else {
                LinkedHashMapEntry<K, V> e = this.nextEntry;
                this.lastReturned = e;
                this.nextEntry = e.after;
                return e;
            }
        }
    }

    private class EntryIterator extends LinkedHashIterator<Entry<K, V>> {
        private EntryIterator() {
            super(null);
        }

        public Entry<K, V> next() {
            return nextEntry();
        }
    }

    private class KeyIterator extends LinkedHashIterator<K> {
        private KeyIterator() {
            super(null);
        }

        public K next() {
            return nextEntry().getKey();
        }
    }

    private static class LinkedHashMapEntry<K, V> extends HashMapEntry<K, V> {
        LinkedHashMapEntry<K, V> after;
        LinkedHashMapEntry<K, V> before;

        LinkedHashMapEntry(int hash, K key, V value, HashMapEntry<K, V> next) {
            super(hash, key, value, next);
        }

        private void remove() {
            this.before.after = this.after;
            this.after.before = this.before;
        }

        private void addBefore(LinkedHashMapEntry<K, V> existingEntry) {
            this.after = existingEntry;
            this.before = existingEntry.before;
            this.before.after = this;
            this.after.before = this;
        }

        void recordAccess(HashMap<K, V> m) {
            LinkedHashMap<K, V> lm = (LinkedHashMap) m;
            if (lm.accessOrder) {
                lm.modCount++;
                remove();
                addBefore(lm.header);
            }
        }

        void recordRemoval(HashMap<K, V> hashMap) {
            remove();
        }
    }

    private class ValueIterator extends LinkedHashIterator<V> {
        private ValueIterator() {
            super(null);
        }

        public V next() {
            return nextEntry().getValue();
        }
    }

    public LinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.accessOrder = false;
    }

    public LinkedHashMap(int initialCapacity) {
        super(initialCapacity);
        this.accessOrder = false;
    }

    public LinkedHashMap() {
        this.accessOrder = false;
    }

    public LinkedHashMap(Map<? extends K, ? extends V> m) {
        super((Map) m);
        this.accessOrder = false;
    }

    public LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor);
        this.accessOrder = accessOrder;
    }

    void init() {
        this.header = new LinkedHashMapEntry(-1, null, null, null);
        LinkedHashMapEntry linkedHashMapEntry = this.header;
        LinkedHashMapEntry linkedHashMapEntry2 = this.header;
        this.header.after = linkedHashMapEntry2;
        linkedHashMapEntry.before = linkedHashMapEntry2;
    }

    void transfer(HashMapEntry[] newTable) {
        int newCapacity = newTable.length;
        for (LinkedHashMapEntry<K, V> e = this.header.after; e != this.header; e = e.after) {
            int index = HashMap.indexFor(e.hash, newCapacity);
            e.next = newTable[index];
            newTable[index] = e;
        }
    }

    public boolean containsValue(Object value) {
        LinkedHashMapEntry e;
        if (value == null) {
            for (e = this.header.after; e != this.header; e = e.after) {
                if (e.value == null) {
                    return true;
                }
            }
        } else {
            for (e = this.header.after; e != this.header; e = e.after) {
                if (value.equals(e.value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public V get(Object key) {
        LinkedHashMapEntry<K, V> e = (LinkedHashMapEntry) getEntry(key);
        if (e == null) {
            return null;
        }
        e.recordAccess(this);
        return e.value;
    }

    public void clear() {
        super.clear();
        LinkedHashMapEntry linkedHashMapEntry = this.header;
        LinkedHashMapEntry linkedHashMapEntry2 = this.header;
        this.header.after = linkedHashMapEntry2;
        linkedHashMapEntry.before = linkedHashMapEntry2;
    }

    Iterator<K> newKeyIterator() {
        return new KeyIterator();
    }

    Iterator<V> newValueIterator() {
        return new ValueIterator();
    }

    Iterator<Entry<K, V>> newEntryIterator() {
        return new EntryIterator();
    }

    void addEntry(int hash, K key, V value, int bucketIndex) {
        LinkedHashMapEntry<K, V> eldest = this.header.after;
        if (eldest != this.header) {
            this.size++;
            try {
                boolean removeEldest = removeEldestEntry(eldest);
                if (removeEldest) {
                    removeEntryForKey(eldest.key);
                }
            } finally {
                this.size--;
            }
        }
        super.addEntry(hash, key, value, bucketIndex);
    }

    public Entry<K, V> eldest() {
        Entry<K, V> eldest = this.header.after;
        return eldest != this.header ? eldest : null;
    }

    void createEntry(int hash, K key, V value, int bucketIndex) {
        LinkedHashMapEntry<K, V> e = new LinkedHashMapEntry(hash, key, value, this.table[bucketIndex]);
        this.table[bucketIndex] = e;
        e.addBefore(this.header);
        this.size++;
    }

    protected boolean removeEldestEntry(Entry<K, V> entry) {
        return false;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        int mc = this.modCount;
        LinkedHashMapEntry<K, V> e = this.header.after;
        while (this.modCount == mc && e != this.header) {
            action.accept(e.key, e.value);
            e = e.after;
        }
        if (this.modCount != mc) {
            throw new ConcurrentModificationException();
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        int mc = this.modCount;
        LinkedHashMapEntry<K, V> e = this.header.after;
        while (this.modCount == mc && e != this.header) {
            e.value = function.apply(e.key, e.value);
            e = e.after;
        }
        if (this.modCount != mc) {
            throw new ConcurrentModificationException();
        }
    }
}
