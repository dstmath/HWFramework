package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import sun.misc.Hashing;
import sun.util.logging.PlatformLogger;

public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {
    static final int DEFAULT_INITIAL_CAPACITY = 4;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final HashMapEntry<?, ?>[] EMPTY_TABLE = null;
    static final int MAXIMUM_CAPACITY = 1073741824;
    private static final long serialVersionUID = 362498820763181265L;
    private transient Set<Entry<K, V>> entrySet;
    final float loadFactor;
    transient int modCount;
    transient int size;
    transient HashMapEntry<K, V>[] table;
    int threshold;

    private abstract class HashIterator<E> implements Iterator<E> {
        HashMapEntry<K, V> current;
        int expectedModCount;
        int index;
        HashMapEntry<K, V> next;

        HashIterator() {
            this.expectedModCount = HashMap.this.modCount;
            if (HashMap.this.size > 0) {
                HashMapEntry[] t = HashMap.this.table;
                while (this.index < t.length) {
                    int i = this.index;
                    this.index = i + 1;
                    HashMapEntry hashMapEntry = t[i];
                    this.next = hashMapEntry;
                    if (hashMapEntry != null) {
                        return;
                    }
                }
            }
        }

        public final boolean hasNext() {
            return this.next != null;
        }

        final Entry<K, V> nextEntry() {
            if (HashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            HashMapEntry<K, V> e = this.next;
            if (e == null) {
                throw new NoSuchElementException();
            }
            HashMapEntry hashMapEntry = e.next;
            this.next = hashMapEntry;
            if (hashMapEntry == null) {
                HashMapEntry[] t = HashMap.this.table;
                while (this.index < t.length) {
                    int i = this.index;
                    this.index = i + 1;
                    hashMapEntry = t[i];
                    this.next = hashMapEntry;
                    if (hashMapEntry != null) {
                        break;
                    }
                }
            }
            this.current = e;
            return e;
        }

        public void remove() {
            if (this.current == null) {
                throw new IllegalStateException();
            } else if (HashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                Object k = this.current.key;
                this.current = null;
                HashMap.this.removeEntryForKey(k);
                this.expectedModCount = HashMap.this.modCount;
            }
        }
    }

    private final class EntryIterator extends HashIterator<Entry<K, V>> {
        private EntryIterator() {
            super();
        }

        public Entry<K, V> next() {
            return nextEntry();
        }
    }

    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        private EntrySet() {
        }

        public Iterator<Entry<K, V>> iterator() {
            return HashMap.this.newEntryIterator();
        }

        public boolean contains(Object o) {
            boolean z = false;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<K, V> e = (Entry) o;
            Entry<K, V> candidate = HashMap.this.getEntry(e.getKey());
            if (candidate != null) {
                z = candidate.equals(e);
            }
            return z;
        }

        public boolean remove(Object o) {
            return HashMap.this.removeMapping(o) != null;
        }

        public int size() {
            return HashMap.this.size;
        }

        public void clear() {
            HashMap.this.clear();
        }

        public final Spliterator<Entry<K, V>> spliterator() {
            return new EntrySpliterator(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (HashMap.this.size > 0) {
                HashMapEntry<K, V>[] tab = HashMap.this.table;
                if (tab != null) {
                    int mc = HashMap.this.modCount;
                    for (HashMapEntry<K, V> e : tab) {
                        for (HashMapEntry<K, V> e2 = tab[i]; e2 != null; e2 = e2.next) {
                            action.accept(e2);
                            if (HashMap.this.modCount != mc) {
                                throw new ConcurrentModificationException();
                            }
                        }
                    }
                }
            }
        }
    }

    static class HashMapSpliterator<K, V> {
        HashMapEntry<K, V> current;
        int est;
        int expectedModCount;
        int fence;
        int index;
        final HashMap<K, V> map;

        HashMapSpliterator(HashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() {
            int hi = this.fence;
            if (hi < 0) {
                HashMap<K, V> m = this.map;
                this.est = m.size;
                this.expectedModCount = m.modCount;
                HashMapEntry<K, V>[] tab = m.table;
                hi = tab == null ? 0 : tab.length;
                this.fence = hi;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence();
            return (long) this.est;
        }
    }

    static final class EntrySpliterator<K, V> extends HashMapSpliterator<K, V> implements Spliterator<Entry<K, V>> {
        EntrySpliterator(HashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K, V> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = (lo + hi) >>> 1;
            if (lo >= mid || this.current != null) {
                return null;
            }
            HashMap hashMap = this.map;
            this.index = mid;
            int i = this.est >>> 1;
            this.est = i;
            return new EntrySpliterator(hashMap, lo, mid, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int mc;
            HashMap<K, V> m = this.map;
            HashMapEntry<K, V>[] tab = m.table;
            int hi = this.fence;
            if (hi < 0) {
                mc = m.modCount;
                this.expectedModCount = mc;
                hi = tab == null ? 0 : tab.length;
                this.fence = hi;
            } else {
                mc = this.expectedModCount;
            }
            if (tab != null && tab.length >= hi) {
                int i = this.index;
                if (i >= 0) {
                    this.index = hi;
                    if (i < hi || this.current != null) {
                        HashMapEntry<K, V> p = this.current;
                        this.current = null;
                        int i2 = i;
                        while (true) {
                            if (p == null) {
                                i = i2 + 1;
                                p = tab[i2];
                            } else {
                                action.accept(p);
                                p = p.next;
                                i = i2;
                            }
                            if (p == null && i >= hi) {
                                break;
                            }
                            i2 = i;
                        }
                        if (m.modCount != mc) {
                            throw new ConcurrentModificationException();
                        }
                    }
                }
            }
        }

        public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            HashMapEntry<K, V>[] tab = this.map.table;
            if (tab != null) {
                int length = tab.length;
                int hi = getFence();
                if (length >= hi && this.index >= 0) {
                    while (true) {
                        if (this.current != null || this.index < hi) {
                            if (this.current != null) {
                                break;
                            }
                            length = this.index;
                            this.index = length + 1;
                            this.current = tab[length];
                        } else {
                            break;
                        }
                    }
                    HashMapEntry<K, V> e = this.current;
                    this.current = this.current.next;
                    action.accept(e);
                    if (this.map.modCount == this.expectedModCount) {
                        return true;
                    }
                    throw new ConcurrentModificationException();
                }
            }
            return false;
        }

        public int characteristics() {
            int i = 0;
            int i2 = (this.fence < 0 || this.est == this.map.size) ? 64 : 0;
            i2 |= 1;
            if (this.map instanceof LinkedHashMap) {
                i = 16;
            }
            return i2 | i;
        }
    }

    static class HashMapEntry<K, V> implements Entry<K, V> {
        int hash;
        final K key;
        HashMapEntry<K, V> next;
        V value;

        HashMapEntry(int h, K k, V v, HashMapEntry<K, V> n) {
            this.value = v;
            this.next = n;
            this.key = k;
            this.hash = h;
        }

        public final K getKey() {
            return this.key;
        }

        public final V getValue() {
            return this.value;
        }

        public final V setValue(V newValue) {
            V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry e = (Entry) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                    return true;
                }
            }
            return false;
        }

        public final int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        public final String toString() {
            return getKey() + "=" + getValue();
        }

        void recordAccess(HashMap<K, V> hashMap) {
        }

        void recordRemoval(HashMap<K, V> hashMap) {
        }
    }

    private final class KeyIterator extends HashIterator<K> {
        private KeyIterator() {
            super();
        }

        public K next() {
            return nextEntry().getKey();
        }
    }

    private final class KeySet extends AbstractSet<K> {
        private KeySet() {
        }

        public Iterator<K> iterator() {
            return HashMap.this.newKeyIterator();
        }

        public int size() {
            return HashMap.this.size;
        }

        public boolean contains(Object o) {
            return HashMap.this.containsKey(o);
        }

        public boolean remove(Object o) {
            return HashMap.this.removeEntryForKey(o) != null;
        }

        public void clear() {
            HashMap.this.clear();
        }

        public final Spliterator<K> spliterator() {
            return new KeySpliterator(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (HashMap.this.size > 0) {
                HashMapEntry<K, V>[] tab = HashMap.this.table;
                if (tab != null) {
                    int mc = HashMap.this.modCount;
                    for (HashMapEntry<K, V> e : tab) {
                        for (HashMapEntry<K, V> e2 = tab[i]; e2 != null; e2 = e2.next) {
                            action.accept(e2.key);
                            if (HashMap.this.modCount != mc) {
                                throw new ConcurrentModificationException();
                            }
                        }
                    }
                }
            }
        }
    }

    static final class KeySpliterator<K, V> extends HashMapSpliterator<K, V> implements Spliterator<K> {
        KeySpliterator(HashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K, V> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = (lo + hi) >>> 1;
            if (lo >= mid || this.current != null) {
                return null;
            }
            HashMap hashMap = this.map;
            this.index = mid;
            int i = this.est >>> 1;
            this.est = i;
            return new KeySpliterator(hashMap, lo, mid, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int mc;
            HashMap<K, V> m = this.map;
            HashMapEntry<K, V>[] tab = m.table;
            int hi = this.fence;
            if (hi < 0) {
                mc = m.modCount;
                this.expectedModCount = mc;
                hi = tab == null ? 0 : tab.length;
                this.fence = hi;
            } else {
                mc = this.expectedModCount;
            }
            if (tab != null && tab.length >= hi) {
                int i = this.index;
                if (i >= 0) {
                    this.index = hi;
                    if (i < hi || this.current != null) {
                        HashMapEntry<K, V> p = this.current;
                        this.current = null;
                        int i2 = i;
                        while (true) {
                            if (p == null) {
                                i = i2 + 1;
                                p = tab[i2];
                            } else {
                                action.accept(p.key);
                                p = p.next;
                                i = i2;
                            }
                            if (p == null && i >= hi) {
                                break;
                            }
                            i2 = i;
                        }
                        if (m.modCount != mc) {
                            throw new ConcurrentModificationException();
                        }
                    }
                }
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            HashMapEntry<K, V>[] tab = this.map.table;
            if (tab != null) {
                int length = tab.length;
                int hi = getFence();
                if (length >= hi && this.index >= 0) {
                    while (true) {
                        if (this.current != null || this.index < hi) {
                            if (this.current != null) {
                                break;
                            }
                            length = this.index;
                            this.index = length + 1;
                            this.current = tab[length];
                        } else {
                            break;
                        }
                    }
                    K k = this.current.key;
                    this.current = this.current.next;
                    action.accept(k);
                    if (this.map.modCount == this.expectedModCount) {
                        return true;
                    }
                    throw new ConcurrentModificationException();
                }
            }
            return false;
        }

        public int characteristics() {
            int i = 0;
            int i2 = (this.fence < 0 || this.est == this.map.size) ? 64 : 0;
            i2 |= 1;
            if (this.map instanceof LinkedHashMap) {
                i = 16;
            }
            return i2 | i;
        }
    }

    private final class ValueIterator extends HashIterator<V> {
        private ValueIterator() {
            super();
        }

        public V next() {
            return nextEntry().getValue();
        }
    }

    static final class ValueSpliterator<K, V> extends HashMapSpliterator<K, V> implements Spliterator<V> {
        ValueSpliterator(HashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K, V> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = (lo + hi) >>> 1;
            if (lo >= mid || this.current != null) {
                return null;
            }
            HashMap hashMap = this.map;
            this.index = mid;
            int i = this.est >>> 1;
            this.est = i;
            return new ValueSpliterator(hashMap, lo, mid, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int mc;
            HashMap<K, V> m = this.map;
            HashMapEntry<K, V>[] tab = m.table;
            int hi = this.fence;
            if (hi < 0) {
                mc = m.modCount;
                this.expectedModCount = mc;
                hi = tab == null ? 0 : tab.length;
                this.fence = hi;
            } else {
                mc = this.expectedModCount;
            }
            if (tab != null && tab.length >= hi) {
                int i = this.index;
                if (i >= 0) {
                    this.index = hi;
                    if (i < hi || this.current != null) {
                        HashMapEntry<K, V> p = this.current;
                        this.current = null;
                        int i2 = i;
                        while (true) {
                            if (p == null) {
                                i = i2 + 1;
                                p = tab[i2];
                            } else {
                                action.accept(p.value);
                                p = p.next;
                                i = i2;
                            }
                            if (p == null && i >= hi) {
                                break;
                            }
                            i2 = i;
                        }
                        if (m.modCount != mc) {
                            throw new ConcurrentModificationException();
                        }
                    }
                }
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            HashMapEntry<K, V>[] tab = this.map.table;
            if (tab != null) {
                int length = tab.length;
                int hi = getFence();
                if (length >= hi && this.index >= 0) {
                    while (true) {
                        if (this.current != null || this.index < hi) {
                            if (this.current != null) {
                                break;
                            }
                            length = this.index;
                            this.index = length + 1;
                            this.current = tab[length];
                        } else {
                            break;
                        }
                    }
                    V v = this.current.value;
                    this.current = this.current.next;
                    action.accept(v);
                    if (this.map.modCount == this.expectedModCount) {
                        return true;
                    }
                    throw new ConcurrentModificationException();
                }
            }
            return false;
        }

        public int characteristics() {
            int i = 0;
            int i2 = (this.fence < 0 || this.est == this.map.size) ? 64 : 0;
            if (this.map instanceof LinkedHashMap) {
                i = 16;
            }
            return i2 | i;
        }
    }

    private final class Values extends AbstractCollection<V> {
        private Values() {
        }

        public Iterator<V> iterator() {
            return HashMap.this.newValueIterator();
        }

        public int size() {
            return HashMap.this.size;
        }

        public boolean contains(Object o) {
            return HashMap.this.containsValue(o);
        }

        public void clear() {
            HashMap.this.clear();
        }

        public final Spliterator<V> spliterator() {
            return new ValueSpliterator(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (HashMap.this.size > 0) {
                HashMapEntry<K, V>[] tab = HashMap.this.table;
                if (tab != null) {
                    int mc = HashMap.this.modCount;
                    for (HashMapEntry<K, V> e : tab) {
                        for (HashMapEntry<K, V> e2 = tab[i]; e2 != null; e2 = e2.next) {
                            action.accept(e2.value);
                            if (HashMap.this.modCount != mc) {
                                throw new ConcurrentModificationException();
                            }
                        }
                    }
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.HashMap.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.HashMap.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.HashMap.<clinit>():void");
    }

    public HashMap(int initialCapacity, float loadFactor) {
        this.table = EMPTY_TABLE;
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.entrySet = null;
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        } else if (initialCapacity < DEFAULT_INITIAL_CAPACITY) {
            initialCapacity = DEFAULT_INITIAL_CAPACITY;
        }
        if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        this.threshold = initialCapacity;
        init();
    }

    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public HashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public HashMap(Map<? extends K, ? extends V> m) {
        this(Math.max(((int) (((float) m.size()) / DEFAULT_LOAD_FACTOR)) + 1, (int) DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
        inflateTable(this.threshold);
        putAllForCreate(m);
    }

    private static int roundUpToPowerOf2(int number) {
        if (number >= MAXIMUM_CAPACITY) {
            return MAXIMUM_CAPACITY;
        }
        int rounded = Integer.highestOneBit(number);
        if (rounded == 0) {
            return 1;
        }
        if (Integer.bitCount(number) > 1) {
            return rounded << 1;
        }
        return rounded;
    }

    private void inflateTable(int toSize) {
        int capacity = roundUpToPowerOf2(toSize);
        float thresholdFloat = ((float) capacity) * DEFAULT_LOAD_FACTOR;
        if (thresholdFloat > 1.07374182E9f) {
            thresholdFloat = 1.07374182E9f;
        }
        this.threshold = (int) thresholdFloat;
        this.table = new HashMapEntry[capacity];
    }

    void init() {
    }

    static int indexFor(int h, int length) {
        return (length - 1) & h;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public V get(Object key) {
        V v = null;
        if (key == null) {
            return getForNullKey();
        }
        Entry<K, V> entry = getEntry(key);
        if (entry != null) {
            v = entry.getValue();
        }
        return v;
    }

    private V getForNullKey() {
        if (this.size == 0) {
            return null;
        }
        for (HashMapEntry<K, V> e = this.table[0]; e != null; e = e.next) {
            if (e.key == null) {
                return e.value;
            }
        }
        return null;
    }

    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    final Entry<K, V> getEntry(Object key) {
        if (this.size == 0) {
            return null;
        }
        int hash = key == null ? 0 : Hashing.singleWordWangJenkinsHash(key);
        for (HashMapEntry<K, V> e = this.table[indexFor(hash, this.table.length)]; e != null; e = e.next) {
            if (e.hash == hash) {
                Object k = e.key;
                if (k == key || (key != null && key.equals(k))) {
                    return e;
                }
            }
        }
        return null;
    }

    public V put(K key, V value) {
        if (this.table == EMPTY_TABLE) {
            inflateTable(this.threshold);
        }
        if (key == null) {
            return putForNullKey(value);
        }
        int hash = Hashing.singleWordWangJenkinsHash(key);
        int i = indexFor(hash, this.table.length);
        for (HashMapEntry<K, V> e = this.table[i]; e != null; e = e.next) {
            if (e.hash == hash) {
                K k = e.key;
                if (k == key || key.equals(k)) {
                    V oldValue = e.value;
                    e.value = value;
                    e.recordAccess(this);
                    return oldValue;
                }
            }
        }
        this.modCount++;
        addEntry(hash, key, value, i);
        return null;
    }

    private V putForNullKey(V value) {
        for (HashMapEntry<K, V> e = this.table[0]; e != null; e = e.next) {
            if (e.key == null) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }
        this.modCount++;
        addEntry(0, null, value, 0);
        return null;
    }

    private void putForCreate(K key, V value) {
        int hash = key == null ? 0 : Hashing.singleWordWangJenkinsHash(key);
        int i = indexFor(hash, this.table.length);
        for (HashMapEntry<K, V> e = this.table[i]; e != null; e = e.next) {
            if (e.hash == hash) {
                K k = e.key;
                if (k == key || (key != null && key.equals(k))) {
                    e.value = value;
                    return;
                }
            }
        }
        createEntry(hash, key, value, i);
    }

    private void putAllForCreate(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            putForCreate(e.getKey(), e.getValue());
        }
    }

    void resize(int newCapacity) {
        if (this.table.length == MAXIMUM_CAPACITY) {
            this.threshold = PlatformLogger.OFF;
            return;
        }
        HashMapEntry[] newTable = new HashMapEntry[newCapacity];
        transfer(newTable);
        this.table = newTable;
        this.threshold = (int) Math.min(((float) newCapacity) * DEFAULT_LOAD_FACTOR, 1.07374182E9f);
    }

    void transfer(HashMapEntry[] newTable) {
        int newCapacity = newTable.length;
        for (HashMapEntry<K, V> e : this.table) {
            HashMapEntry<K, V> e2;
            while (e2 != null) {
                HashMapEntry<K, V> next = e2.next;
                int i = indexFor(e2.hash, newCapacity);
                e2.next = newTable[i];
                newTable[i] = e2;
                e2 = next;
            }
        }
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        int numKeysToBeAdded = m.size();
        if (numKeysToBeAdded != 0) {
            if (this.table == EMPTY_TABLE) {
                inflateTable((int) Math.max(((float) numKeysToBeAdded) * DEFAULT_LOAD_FACTOR, (float) this.threshold));
            }
            if (numKeysToBeAdded > this.threshold) {
                int targetCapacity = (int) ((((float) numKeysToBeAdded) / DEFAULT_LOAD_FACTOR) + 1.0f);
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
            for (Entry<? extends K, ? extends V> e : m.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }
    }

    public V remove(Object key) {
        Entry<K, V> e = removeEntryForKey(key);
        if (e == null) {
            return null;
        }
        return e.getValue();
    }

    final Entry<K, V> removeEntryForKey(Object key) {
        if (this.size == 0) {
            return null;
        }
        int hash = key == null ? 0 : Hashing.singleWordWangJenkinsHash(key);
        int i = indexFor(hash, this.table.length);
        HashMapEntry<K, V> prev = this.table[i];
        HashMapEntry<K, V> e = prev;
        while (e != null) {
            HashMapEntry<K, V> next = e.next;
            if (e.hash == hash) {
                Object k = e.key;
                if (k == key || (key != null && key.equals(k))) {
                    this.modCount++;
                    this.size--;
                    if (prev == e) {
                        this.table[i] = next;
                    } else {
                        prev.next = next;
                    }
                    e.recordRemoval(this);
                    return e;
                }
            }
            prev = e;
            e = next;
        }
        return e;
    }

    final Entry<K, V> removeMapping(Object o) {
        if (this.size == 0 || !(o instanceof Entry)) {
            return null;
        }
        Entry<K, V> entry = (Entry) o;
        Object key = entry.getKey();
        int hash = key == null ? 0 : Hashing.singleWordWangJenkinsHash(key);
        int i = indexFor(hash, this.table.length);
        HashMapEntry<K, V> prev = this.table[i];
        HashMapEntry<K, V> e = prev;
        while (e != null) {
            HashMapEntry<K, V> next = e.next;
            if (e.hash == hash && e.equals(entry)) {
                this.modCount++;
                this.size--;
                if (prev == e) {
                    this.table[i] = next;
                } else {
                    prev.next = next;
                }
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }
        return e;
    }

    public void clear() {
        this.modCount++;
        Arrays.fill(this.table, null);
        this.size = 0;
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            return containsNullValue();
        }
        HashMapEntry[] tab = this.table;
        for (HashMapEntry e : tab) {
            for (HashMapEntry e2 = tab[i]; e2 != null; e2 = e2.next) {
                if (value.equals(e2.value)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsNullValue() {
        HashMapEntry[] tab = this.table;
        for (HashMapEntry e : tab) {
            for (HashMapEntry e2 = tab[i]; e2 != null; e2 = e2.next) {
                if (e2.value == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public Object clone() {
        HashMap result = null;
        try {
            result = (HashMap) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        if (result.table != EMPTY_TABLE) {
            result.inflateTable(Math.min((int) Math.min(((float) this.size) * Math.min(1.3333334f, 4.0f), 1.07374182E9f), this.table.length));
        }
        result.entrySet = null;
        result.modCount = 0;
        result.size = 0;
        result.init();
        result.putAllForCreate(this);
        return result;
    }

    void addEntry(int hash, K key, V value, int bucketIndex) {
        if (this.size >= this.threshold && this.table[bucketIndex] != null) {
            resize(this.table.length * 2);
            hash = key != null ? Hashing.singleWordWangJenkinsHash(key) : 0;
            bucketIndex = indexFor(hash, this.table.length);
        }
        createEntry(hash, key, value, bucketIndex);
    }

    void createEntry(int hash, K key, V value, int bucketIndex) {
        this.table[bucketIndex] = new HashMapEntry(hash, key, value, this.table[bucketIndex]);
        this.size++;
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

    public Set<K> keySet() {
        Set<K> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        ks = new KeySet();
        this.keySet = ks;
        return ks;
    }

    public Collection<V> values() {
        Collection<V> vs = this.values;
        if (vs != null) {
            return vs;
        }
        vs = new Values();
        this.values = vs;
        return vs;
    }

    public Set<Entry<K, V>> entrySet() {
        return entrySet0();
    }

    private Set<Entry<K, V>> entrySet0() {
        Set<Entry<K, V>> es = this.entrySet;
        if (es != null) {
            return es;
        }
        es = new EntrySet();
        this.entrySet = es;
        return es;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) {
            throw new NullPointerException();
        } else if (this.size > 0) {
            HashMapEntry<K, V>[] tab = this.table;
            if (tab != null) {
                int mc = this.modCount;
                for (HashMapEntry<K, V> e : tab) {
                    for (HashMapEntry<K, V> e2 = tab[i]; e2 != null; e2 = e2.next) {
                        action.accept(e2.key, e2.value);
                        if (this.modCount != mc) {
                            throw new ConcurrentModificationException();
                        }
                    }
                }
            }
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null) {
            throw new NullPointerException();
        } else if (this.size > 0) {
            HashMapEntry<K, V>[] tab = this.table;
            if (tab != null) {
                int mc = this.modCount;
                for (HashMapEntry<K, V> e : tab) {
                    for (HashMapEntry<K, V> e2 = tab[i]; e2 != null; e2 = e2.next) {
                        e2.value = function.apply(e2.key, e2.value);
                    }
                }
                if (this.modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        if (this.table == EMPTY_TABLE) {
            s.writeInt(roundUpToPowerOf2(this.threshold));
        } else {
            s.writeInt(this.table.length);
        }
        s.writeInt(this.size);
        if (this.size > 0) {
            for (Entry<K, V> e : entrySet0()) {
                s.writeObject(e.getKey());
                s.writeObject(e.getValue());
            }
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        if (Float.isNaN(DEFAULT_LOAD_FACTOR)) {
            throw new InvalidObjectException("Illegal load factor: 0.75");
        }
        this.table = EMPTY_TABLE;
        s.readInt();
        int mappings = s.readInt();
        if (mappings < 0) {
            throw new InvalidObjectException("Illegal mappings count: " + mappings);
        }
        int capacity = (int) Math.min(((float) mappings) * Math.min(1.3333334f, 4.0f), 1.07374182E9f);
        if (mappings > 0) {
            inflateTable(capacity);
        } else {
            this.threshold = capacity;
        }
        init();
        for (int i = 0; i < mappings; i++) {
            putForCreate(s.readObject(), s.readObject());
        }
    }

    public boolean replace(K key, V oldValue, V newValue) {
        HashMapEntry<K, V> e = (HashMapEntry) getEntry(key);
        if (e != null) {
            V v = e.value;
            if (v == oldValue || (v != null && v.equals(oldValue))) {
                e.value = newValue;
                e.recordAccess(this);
                return true;
            }
        }
        return false;
    }

    int capacity() {
        return this.table.length;
    }

    float loadFactor() {
        return DEFAULT_LOAD_FACTOR;
    }
}
