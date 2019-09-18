package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class IdentityHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Serializable, Cloneable {
    private static final int DEFAULT_CAPACITY = 32;
    private static final int MAXIMUM_CAPACITY = 536870912;
    private static final int MINIMUM_CAPACITY = 4;
    static final Object NULL_KEY = new Object();
    private static final long serialVersionUID = 8188218128353913216L;
    private transient Set<Map.Entry<K, V>> entrySet;
    transient int modCount;
    int size;
    transient Object[] table;

    private class EntryIterator extends IdentityHashMap<K, V>.IdentityHashMapIterator<Map.Entry<K, V>> {
        private IdentityHashMap<K, V>.EntryIterator.Entry lastReturnedEntry;

        private class Entry implements Map.Entry<K, V> {
            /* access modifiers changed from: private */
            public int index;

            private Entry(int index2) {
                this.index = index2;
            }

            public K getKey() {
                checkIndexForEntryUse();
                return IdentityHashMap.unmaskNull(EntryIterator.this.traversalTable[this.index]);
            }

            public V getValue() {
                checkIndexForEntryUse();
                return EntryIterator.this.traversalTable[this.index + 1];
            }

            public V setValue(V value) {
                checkIndexForEntryUse();
                V oldValue = EntryIterator.this.traversalTable[this.index + 1];
                EntryIterator.this.traversalTable[this.index + 1] = value;
                if (EntryIterator.this.traversalTable != IdentityHashMap.this.table) {
                    IdentityHashMap.this.put(EntryIterator.this.traversalTable[this.index], value);
                }
                return oldValue;
            }

            public boolean equals(Object o) {
                if (this.index < 0) {
                    return super.equals(o);
                }
                boolean z = false;
                if (!(o instanceof Map.Entry)) {
                    return false;
                }
                Map.Entry<?, ?> e = (Map.Entry) o;
                if (e.getKey() == IdentityHashMap.unmaskNull(EntryIterator.this.traversalTable[this.index]) && e.getValue() == EntryIterator.this.traversalTable[this.index + 1]) {
                    z = true;
                }
                return z;
            }

            public int hashCode() {
                if (EntryIterator.this.lastReturnedIndex < 0) {
                    return super.hashCode();
                }
                return System.identityHashCode(IdentityHashMap.unmaskNull(EntryIterator.this.traversalTable[this.index])) ^ System.identityHashCode(EntryIterator.this.traversalTable[this.index + 1]);
            }

            public String toString() {
                if (this.index < 0) {
                    return super.toString();
                }
                return IdentityHashMap.unmaskNull(EntryIterator.this.traversalTable[this.index]) + "=" + EntryIterator.this.traversalTable[this.index + 1];
            }

            private void checkIndexForEntryUse() {
                if (this.index < 0) {
                    throw new IllegalStateException("Entry was removed");
                }
            }
        }

        private EntryIterator() {
            super();
        }

        public Map.Entry<K, V> next() {
            this.lastReturnedEntry = new Entry(nextIndex());
            return this.lastReturnedEntry;
        }

        public void remove() {
            this.lastReturnedIndex = this.lastReturnedEntry == null ? -1 : this.lastReturnedEntry.index;
            super.remove();
            int unused = this.lastReturnedEntry.index = this.lastReturnedIndex;
            this.lastReturnedEntry = null;
        }
    }

    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        private EntrySet() {
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry) o;
            return IdentityHashMap.this.containsMapping(entry.getKey(), entry.getValue());
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry) o;
            return IdentityHashMap.this.removeMapping(entry.getKey(), entry.getValue());
        }

        public int size() {
            return IdentityHashMap.this.size;
        }

        public void clear() {
            IdentityHashMap.this.clear();
        }

        public boolean removeAll(Collection<?> c) {
            Objects.requireNonNull(c);
            boolean modified = false;
            Iterator<Map.Entry<K, V>> i = iterator();
            while (i.hasNext()) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
            return modified;
        }

        public Object[] toArray() {
            return toArray(new Object[0]);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r11v2, resolved type: T[]} */
        /* JADX WARNING: Multi-variable type inference failed */
        public <T> T[] toArray(T[] a) {
            int expectedModCount = IdentityHashMap.this.modCount;
            int size = size();
            if (a.length < size) {
                a = (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
            }
            Object[] tab = IdentityHashMap.this.table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                Object obj = tab[si];
                Object key = obj;
                if (obj != null) {
                    if (ti < size) {
                        a[ti] = new AbstractMap.SimpleEntry(IdentityHashMap.unmaskNull(key), tab[si + 1]);
                        ti++;
                    } else {
                        throw new ConcurrentModificationException();
                    }
                }
            }
            if (ti < size || expectedModCount != IdentityHashMap.this.modCount) {
                throw new ConcurrentModificationException();
            }
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }

        public Spliterator<Map.Entry<K, V>> spliterator() {
            EntrySpliterator entrySpliterator = new EntrySpliterator(IdentityHashMap.this, 0, -1, 0, 0);
            return entrySpliterator;
        }
    }

    static final class EntrySpliterator<K, V> extends IdentityHashMapSpliterator<K, V> implements Spliterator<Map.Entry<K, V>> {
        EntrySpliterator(IdentityHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K, V> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = ((lo + hi) >>> 1) & -2;
            if (lo >= mid) {
                return null;
            }
            IdentityHashMap identityHashMap = this.map;
            this.index = mid;
            int i = this.est >>> 1;
            this.est = i;
            EntrySpliterator<K, V> entrySpliterator = new EntrySpliterator<>(identityHashMap, lo, mid, i, this.expectedModCount);
            return entrySpliterator;
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            if (action != null) {
                IdentityHashMap<K, V> identityHashMap = this.map;
                IdentityHashMap<K, V> m = identityHashMap;
                if (identityHashMap != null) {
                    V[] vArr = m.table;
                    V[] a = vArr;
                    if (vArr != null) {
                        int i = this.index;
                        if (i >= 0) {
                            int fence = getFence();
                            int hi = fence;
                            this.index = fence;
                            if (fence <= a.length) {
                                for (int i2 = i; i2 < hi; i2 += 2) {
                                    V key = a[i2];
                                    if (key != null) {
                                        action.accept(new AbstractMap.SimpleImmutableEntry(IdentityHashMap.unmaskNull(key), a[i2 + 1]));
                                    }
                                }
                                if (m.modCount == this.expectedModCount) {
                                    return;
                                }
                            }
                        }
                    }
                }
                throw new ConcurrentModificationException();
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            if (action != null) {
                V[] a = this.map.table;
                int hi = getFence();
                while (this.index < hi) {
                    V key = a[this.index];
                    V v = a[this.index + 1];
                    this.index += 2;
                    if (key != null) {
                        action.accept(new AbstractMap.SimpleImmutableEntry(IdentityHashMap.unmaskNull(key), v));
                        if (this.map.modCount == this.expectedModCount) {
                            return true;
                        }
                        throw new ConcurrentModificationException();
                    }
                }
                return false;
            }
            throw new NullPointerException();
        }

        public int characteristics() {
            return ((this.fence < 0 || this.est == this.map.size) ? 64 : 0) | 1;
        }
    }

    private abstract class IdentityHashMapIterator<T> implements Iterator<T> {
        int expectedModCount;
        int index;
        boolean indexValid;
        int lastReturnedIndex;
        Object[] traversalTable;

        private IdentityHashMapIterator() {
            this.index = IdentityHashMap.this.size != 0 ? 0 : IdentityHashMap.this.table.length;
            this.expectedModCount = IdentityHashMap.this.modCount;
            this.lastReturnedIndex = -1;
            this.traversalTable = IdentityHashMap.this.table;
        }

        public boolean hasNext() {
            Object[] tab = this.traversalTable;
            for (int i = this.index; i < tab.length; i += 2) {
                if (tab[i] != null) {
                    this.index = i;
                    this.indexValid = true;
                    return true;
                }
            }
            this.index = tab.length;
            return false;
        }

        /* access modifiers changed from: protected */
        public int nextIndex() {
            if (IdentityHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else if (this.indexValid || hasNext()) {
                this.indexValid = false;
                this.lastReturnedIndex = this.index;
                this.index += 2;
                return this.lastReturnedIndex;
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (this.lastReturnedIndex == -1) {
                throw new IllegalStateException();
            } else if (IdentityHashMap.this.modCount == this.expectedModCount) {
                IdentityHashMap identityHashMap = IdentityHashMap.this;
                int i = identityHashMap.modCount + 1;
                identityHashMap.modCount = i;
                this.expectedModCount = i;
                int deletedSlot = this.lastReturnedIndex;
                this.lastReturnedIndex = -1;
                this.index = deletedSlot;
                this.indexValid = false;
                Object[] tab = this.traversalTable;
                int len = tab.length;
                int d = deletedSlot;
                Object key = tab[d];
                tab[d] = null;
                tab[d + 1] = null;
                if (tab != IdentityHashMap.this.table) {
                    IdentityHashMap.this.remove(key);
                    this.expectedModCount = IdentityHashMap.this.modCount;
                    return;
                }
                IdentityHashMap identityHashMap2 = IdentityHashMap.this;
                identityHashMap2.size--;
                int i2 = IdentityHashMap.nextKeyIndex(d, len);
                while (true) {
                    Object obj = tab[i2];
                    Object item = obj;
                    if (obj != null) {
                        int r = IdentityHashMap.hash(item, len);
                        if ((i2 < r && (r <= d || d <= i2)) || (r <= d && d <= i2)) {
                            if (i2 < deletedSlot && d >= deletedSlot && this.traversalTable == IdentityHashMap.this.table) {
                                int remaining = len - deletedSlot;
                                Object[] newTable = new Object[remaining];
                                System.arraycopy((Object) tab, deletedSlot, (Object) newTable, 0, remaining);
                                this.traversalTable = newTable;
                                this.index = 0;
                            }
                            tab[d] = item;
                            tab[d + 1] = tab[i2 + 1];
                            tab[i2] = null;
                            tab[i2 + 1] = null;
                            d = i2;
                        }
                        i2 = IdentityHashMap.nextKeyIndex(i2, len);
                    } else {
                        return;
                    }
                }
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    static class IdentityHashMapSpliterator<K, V> {
        int est;
        int expectedModCount;
        int fence;
        int index;
        final IdentityHashMap<K, V> map;

        IdentityHashMapSpliterator(IdentityHashMap<K, V> map2, int origin, int fence2, int est2, int expectedModCount2) {
            this.map = map2;
            this.index = origin;
            this.fence = fence2;
            this.est = est2;
            this.expectedModCount = expectedModCount2;
        }

        /* access modifiers changed from: package-private */
        public final int getFence() {
            int i = this.fence;
            int hi = i;
            if (i >= 0) {
                return hi;
            }
            this.est = this.map.size;
            this.expectedModCount = this.map.modCount;
            int hi2 = this.map.table.length;
            this.fence = hi2;
            return hi2;
        }

        public final long estimateSize() {
            getFence();
            return (long) this.est;
        }
    }

    private class KeyIterator extends IdentityHashMap<K, V>.IdentityHashMapIterator<K> {
        private KeyIterator() {
            super();
        }

        public K next() {
            return IdentityHashMap.unmaskNull(this.traversalTable[nextIndex()]);
        }
    }

    private class KeySet extends AbstractSet<K> {
        private KeySet() {
        }

        public Iterator<K> iterator() {
            return new KeyIterator();
        }

        public int size() {
            return IdentityHashMap.this.size;
        }

        public boolean contains(Object o) {
            return IdentityHashMap.this.containsKey(o);
        }

        public boolean remove(Object o) {
            int oldSize = IdentityHashMap.this.size;
            IdentityHashMap.this.remove(o);
            return IdentityHashMap.this.size != oldSize;
        }

        public boolean removeAll(Collection<?> c) {
            Objects.requireNonNull(c);
            boolean modified = false;
            Iterator<K> i = iterator();
            while (i.hasNext()) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
            return modified;
        }

        public void clear() {
            IdentityHashMap.this.clear();
        }

        public int hashCode() {
            int result = 0;
            Iterator it = iterator();
            while (it.hasNext()) {
                result += System.identityHashCode(it.next());
            }
            return result;
        }

        public Object[] toArray() {
            return toArray(new Object[0]);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r9v2, resolved type: T[]} */
        /* JADX WARNING: Multi-variable type inference failed */
        public <T> T[] toArray(T[] a) {
            int expectedModCount = IdentityHashMap.this.modCount;
            int size = size();
            if (a.length < size) {
                a = (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
            }
            Object[] tab = IdentityHashMap.this.table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                Object obj = tab[si];
                Object key = obj;
                if (obj != null) {
                    if (ti < size) {
                        a[ti] = IdentityHashMap.unmaskNull(key);
                        ti++;
                    } else {
                        throw new ConcurrentModificationException();
                    }
                }
            }
            if (ti < size || expectedModCount != IdentityHashMap.this.modCount) {
                throw new ConcurrentModificationException();
            }
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }

        public Spliterator<K> spliterator() {
            KeySpliterator keySpliterator = new KeySpliterator(IdentityHashMap.this, 0, -1, 0, 0);
            return keySpliterator;
        }
    }

    static final class KeySpliterator<K, V> extends IdentityHashMapSpliterator<K, V> implements Spliterator<K> {
        KeySpliterator(IdentityHashMap<K, V> map, int origin, int fence, int est, int expectedModCount) {
            super(map, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K, V> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = ((lo + hi) >>> 1) & -2;
            if (lo >= mid) {
                return null;
            }
            IdentityHashMap identityHashMap = this.map;
            this.index = mid;
            int i = this.est >>> 1;
            this.est = i;
            KeySpliterator<K, V> keySpliterator = new KeySpliterator<>(identityHashMap, lo, mid, i, this.expectedModCount);
            return keySpliterator;
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action != null) {
                IdentityHashMap<K, V> identityHashMap = this.map;
                IdentityHashMap<K, V> m = identityHashMap;
                if (identityHashMap != null) {
                    Object[] objArr = m.table;
                    Object[] a = objArr;
                    if (objArr != null) {
                        int i = this.index;
                        if (i >= 0) {
                            int fence = getFence();
                            int hi = fence;
                            this.index = fence;
                            if (fence <= a.length) {
                                for (int i2 = i; i2 < hi; i2 += 2) {
                                    Object obj = a[i2];
                                    Object key = obj;
                                    if (obj != null) {
                                        action.accept(IdentityHashMap.unmaskNull(key));
                                    }
                                }
                                if (m.modCount == this.expectedModCount) {
                                    return;
                                }
                            }
                        }
                    }
                }
                throw new ConcurrentModificationException();
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action != null) {
                Object[] a = this.map.table;
                int hi = getFence();
                while (this.index < hi) {
                    Object key = a[this.index];
                    this.index += 2;
                    if (key != null) {
                        action.accept(IdentityHashMap.unmaskNull(key));
                        if (this.map.modCount == this.expectedModCount) {
                            return true;
                        }
                        throw new ConcurrentModificationException();
                    }
                }
                return false;
            }
            throw new NullPointerException();
        }

        public int characteristics() {
            return ((this.fence < 0 || this.est == this.map.size) ? 64 : 0) | 1;
        }
    }

    private class ValueIterator extends IdentityHashMap<K, V>.IdentityHashMapIterator<V> {
        private ValueIterator() {
            super();
        }

        public V next() {
            return this.traversalTable[nextIndex() + 1];
        }
    }

    static final class ValueSpliterator<K, V> extends IdentityHashMapSpliterator<K, V> implements Spliterator<V> {
        ValueSpliterator(IdentityHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K, V> trySplit() {
            int hi = getFence();
            int lo = this.index;
            int mid = ((lo + hi) >>> 1) & -2;
            if (lo >= mid) {
                return null;
            }
            IdentityHashMap identityHashMap = this.map;
            this.index = mid;
            int i = this.est >>> 1;
            this.est = i;
            ValueSpliterator<K, V> valueSpliterator = new ValueSpliterator<>(identityHashMap, lo, mid, i, this.expectedModCount);
            return valueSpliterator;
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action != null) {
                IdentityHashMap<K, V> identityHashMap = this.map;
                IdentityHashMap<K, V> m = identityHashMap;
                if (identityHashMap != null) {
                    V[] vArr = m.table;
                    V[] a = vArr;
                    if (vArr != null) {
                        int i = this.index;
                        if (i >= 0) {
                            int fence = getFence();
                            int hi = fence;
                            this.index = fence;
                            if (fence <= a.length) {
                                for (int i2 = i; i2 < hi; i2 += 2) {
                                    if (a[i2] != null) {
                                        action.accept(a[i2 + 1]);
                                    }
                                }
                                if (m.modCount == this.expectedModCount) {
                                    return;
                                }
                            }
                        }
                    }
                }
                throw new ConcurrentModificationException();
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action != null) {
                V[] a = this.map.table;
                int hi = getFence();
                while (this.index < hi) {
                    V key = a[this.index];
                    V v = a[this.index + 1];
                    this.index += 2;
                    if (key != null) {
                        action.accept(v);
                        if (this.map.modCount == this.expectedModCount) {
                            return true;
                        }
                        throw new ConcurrentModificationException();
                    }
                }
                return false;
            }
            throw new NullPointerException();
        }

        public int characteristics() {
            return (this.fence < 0 || this.est == this.map.size) ? 64 : 0;
        }
    }

    private class Values extends AbstractCollection<V> {
        private Values() {
        }

        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        public int size() {
            return IdentityHashMap.this.size;
        }

        public boolean contains(Object o) {
            return IdentityHashMap.this.containsValue(o);
        }

        public boolean remove(Object o) {
            Iterator<V> i = iterator();
            while (i.hasNext()) {
                if (i.next() == o) {
                    i.remove();
                    return true;
                }
            }
            return false;
        }

        public void clear() {
            IdentityHashMap.this.clear();
        }

        public Object[] toArray() {
            return toArray(new Object[0]);
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v5, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r8v2, resolved type: T[]} */
        /* JADX WARNING: Multi-variable type inference failed */
        public <T> T[] toArray(T[] a) {
            int expectedModCount = IdentityHashMap.this.modCount;
            int size = size();
            if (a.length < size) {
                a = (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
            }
            Object[] tab = IdentityHashMap.this.table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                if (tab[si] != null) {
                    if (ti < size) {
                        a[ti] = tab[si + 1];
                        ti++;
                    } else {
                        throw new ConcurrentModificationException();
                    }
                }
            }
            if (ti < size || expectedModCount != IdentityHashMap.this.modCount) {
                throw new ConcurrentModificationException();
            }
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }

        public Spliterator<V> spliterator() {
            ValueSpliterator valueSpliterator = new ValueSpliterator(IdentityHashMap.this, 0, -1, 0, 0);
            return valueSpliterator;
        }
    }

    private static Object maskNull(Object key) {
        return key == null ? NULL_KEY : key;
    }

    static final Object unmaskNull(Object key) {
        if (key == NULL_KEY) {
            return null;
        }
        return key;
    }

    public IdentityHashMap() {
        init(32);
    }

    public IdentityHashMap(int expectedMaxSize) {
        if (expectedMaxSize >= 0) {
            init(capacity(expectedMaxSize));
            return;
        }
        throw new IllegalArgumentException("expectedMaxSize is negative: " + expectedMaxSize);
    }

    private static int capacity(int expectedMaxSize) {
        if (expectedMaxSize > 178956970) {
            return MAXIMUM_CAPACITY;
        }
        if (expectedMaxSize <= 2) {
            return 4;
        }
        return Integer.highestOneBit((expectedMaxSize << 1) + expectedMaxSize);
    }

    private void init(int initCapacity) {
        this.table = new Object[(2 * initCapacity)];
    }

    public IdentityHashMap(Map<? extends K, ? extends V> m) {
        this((int) (((double) (1 + m.size())) * 1.1d));
        putAll(m);
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    /* access modifiers changed from: private */
    public static int hash(Object x, int length) {
        int h = System.identityHashCode(x);
        return ((h << 1) - (h << 8)) & (length - 1);
    }

    /* access modifiers changed from: private */
    public static int nextKeyIndex(int i, int len) {
        if (i + 2 < len) {
            return i + 2;
        }
        return 0;
    }

    public V get(Object key) {
        Object k = maskNull(key);
        Object[] tab = this.table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            if (item == k) {
                return tab[i + 1];
            }
            if (item == null) {
                return null;
            }
            i = nextKeyIndex(i, len);
        }
    }

    public boolean containsKey(Object key) {
        Object k = maskNull(key);
        Object[] tab = this.table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            if (item == k) {
                return true;
            }
            if (item == null) {
                return false;
            }
            i = nextKeyIndex(i, len);
        }
    }

    public boolean containsValue(Object value) {
        Object[] tab = this.table;
        for (int i = 1; i < tab.length; i += 2) {
            if (tab[i] == value && tab[i - 1] != null) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public boolean containsMapping(Object key, Object value) {
        Object k = maskNull(key);
        Object[] tab = this.table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            boolean z = false;
            if (item == k) {
                if (tab[i + 1] == value) {
                    z = true;
                }
                return z;
            } else if (item == null) {
                return false;
            } else {
                i = nextKeyIndex(i, len);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0020, code lost:
        r4 = r7.size + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0027, code lost:
        if (((r4 << 1) + r4) <= r2) goto L_0x0030;
     */
    public V put(K key, V value) {
        V[] tab;
        int len;
        int i;
        int s;
        V k = maskNull(key);
        do {
            tab = this.table;
            len = tab.length;
            i = hash(k, len);
            while (true) {
                V v = tab[i];
                V item = v;
                if (v == null) {
                    break;
                } else if (item == k) {
                    V oldValue = tab[i + 1];
                    tab[i + 1] = value;
                    return oldValue;
                } else {
                    i = nextKeyIndex(i, len);
                }
            }
        } while (resize(len));
        this.modCount++;
        tab[i] = k;
        tab[i + 1] = value;
        this.size = s;
        return null;
    }

    private boolean resize(int newCapacity) {
        int newLength = newCapacity * 2;
        Object[] oldTable = this.table;
        int oldLength = oldTable.length;
        if (oldLength == 1073741824) {
            if (this.size != 536870911) {
                return false;
            }
            throw new IllegalStateException("Capacity exhausted.");
        } else if (oldLength >= newLength) {
            return false;
        } else {
            Object[] newTable = new Object[newLength];
            for (int j = 0; j < oldLength; j += 2) {
                Object key = oldTable[j];
                if (key != null) {
                    Object value = oldTable[j + 1];
                    oldTable[j] = null;
                    oldTable[j + 1] = null;
                    int i = hash(key, newLength);
                    while (newTable[i] != null) {
                        i = nextKeyIndex(i, newLength);
                    }
                    newTable[i] = key;
                    newTable[i + 1] = value;
                }
            }
            this.table = newTable;
            return true;
        }
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        int n = m.size();
        if (n != 0) {
            if (n > this.size) {
                resize(capacity(n));
            }
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                put(e.getKey(), e.getValue());
            }
        }
    }

    public V remove(Object key) {
        V k = maskNull(key);
        V[] tab = this.table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            V item = tab[i];
            if (item == k) {
                this.modCount++;
                this.size--;
                V oldValue = tab[i + 1];
                tab[i + 1] = null;
                tab[i] = null;
                closeDeletion(i);
                return oldValue;
            } else if (item == null) {
                return null;
            } else {
                i = nextKeyIndex(i, len);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean removeMapping(Object key, Object value) {
        Object k = maskNull(key);
        Object[] tab = this.table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object item = tab[i];
            if (item == k) {
                if (tab[i + 1] != value) {
                    return false;
                }
                this.modCount++;
                this.size--;
                tab[i] = null;
                tab[i + 1] = null;
                closeDeletion(i);
                return true;
            } else if (item == null) {
                return false;
            } else {
                i = nextKeyIndex(i, len);
            }
        }
    }

    private void closeDeletion(int d) {
        Object[] tab = this.table;
        int len = tab.length;
        int i = nextKeyIndex(d, len);
        while (true) {
            Object obj = tab[i];
            Object item = obj;
            if (obj != null) {
                int r = hash(item, len);
                if ((i < r && (r <= d || d <= i)) || (r <= d && d <= i)) {
                    tab[d] = item;
                    tab[d + 1] = tab[i + 1];
                    tab[i] = null;
                    tab[i + 1] = null;
                    d = i;
                }
                i = nextKeyIndex(i, len);
            } else {
                return;
            }
        }
    }

    public void clear() {
        this.modCount++;
        Object[] tab = this.table;
        for (int i = 0; i < tab.length; i++) {
            tab[i] = null;
        }
        this.size = 0;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof IdentityHashMap) {
            IdentityHashMap<?, ?> m = (IdentityHashMap) o;
            if (m.size() != this.size) {
                return false;
            }
            Object[] tab = m.table;
            for (int i = 0; i < tab.length; i += 2) {
                Object k = tab[i];
                if (k != null && !containsMapping(k, tab[i + 1])) {
                    return false;
                }
            }
            return true;
        } else if (o instanceof Map) {
            return entrySet().equals(((Map) o).entrySet());
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = 0;
        Object[] tab = this.table;
        for (int i = 0; i < tab.length; i += 2) {
            Object key = tab[i];
            if (key != null) {
                result += System.identityHashCode(unmaskNull(key)) ^ System.identityHashCode(tab[i + 1]);
            }
        }
        return result;
    }

    public Object clone() {
        try {
            IdentityHashMap<?, ?> m = (IdentityHashMap) super.clone();
            m.entrySet = null;
            m.table = (Object[]) this.table.clone();
            return m;
        } catch (CloneNotSupportedException e) {
            throw new InternalError((Throwable) e);
        }
    }

    public Set<K> keySet() {
        Set<K> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        Set<K> ks2 = new KeySet();
        this.keySet = ks2;
        return ks2;
    }

    public Collection<V> values() {
        Collection<V> vs = this.values;
        if (vs != null) {
            return vs;
        }
        Collection<V> vs2 = new Values();
        this.values = vs2;
        return vs2;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> es = this.entrySet;
        if (es != null) {
            return es;
        }
        EntrySet entrySet2 = new EntrySet();
        this.entrySet = entrySet2;
        return entrySet2;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(this.size);
        Object[] tab = this.table;
        for (int i = 0; i < tab.length; i += 2) {
            Object key = tab[i];
            if (key != null) {
                s.writeObject(unmaskNull(key));
                s.writeObject(tab[i + 1]);
            }
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int size2 = s.readInt();
        if (size2 >= 0) {
            init(capacity(size2));
            for (int i = 0; i < size2; i++) {
                putForCreate(s.readObject(), s.readObject());
            }
            return;
        }
        throw new StreamCorruptedException("Illegal mappings count: " + size2);
    }

    private void putForCreate(K key, V value) throws StreamCorruptedException {
        Object k = maskNull(key);
        Object[] tab = this.table;
        int len = tab.length;
        int i = hash(k, len);
        while (true) {
            Object obj = tab[i];
            Object item = obj;
            if (obj == null) {
                tab[i] = k;
                tab[i + 1] = value;
                return;
            } else if (item != k) {
                i = nextKeyIndex(i, len);
            } else {
                throw new StreamCorruptedException();
            }
        }
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = this.modCount;
        Object[] t = this.table;
        int index = 0;
        while (index < t.length) {
            Object k = t[index];
            if (k != null) {
                action.accept(unmaskNull(k), t[index + 1]);
            }
            if (this.modCount == expectedModCount) {
                index += 2;
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        int expectedModCount = this.modCount;
        Object[] t = this.table;
        int index = 0;
        while (index < t.length) {
            Object k = t[index];
            if (k != null) {
                t[index + 1] = function.apply(unmaskNull(k), t[index + 1]);
            }
            if (this.modCount == expectedModCount) {
                index += 2;
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }
}
