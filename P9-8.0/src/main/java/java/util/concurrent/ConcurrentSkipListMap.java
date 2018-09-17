package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import sun.misc.Unsafe;

public class ConcurrentSkipListMap<K, V> extends AbstractMap<K, V> implements ConcurrentNavigableMap<K, V>, Cloneable, Serializable {
    static final Object BASE_HEADER = new Object();
    private static final int EQ = 1;
    private static final int GT = 0;
    private static final long HEAD;
    private static final int LT = 2;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = -8627078645895051609L;
    final Comparator<? super K> comparator;
    private transient ConcurrentNavigableMap<K, V> descendingMap;
    private transient EntrySet<K, V> entrySet;
    private volatile transient HeadIndex<K, V> head;
    private transient KeySet<K, V> keySet;
    private transient Values<K, V> values;

    static abstract class CSLMSpliterator<K, V> {
        final Comparator<? super K> comparator;
        Node<K, V> current;
        int est;
        final K fence;
        Index<K, V> row;

        CSLMSpliterator(Comparator<? super K> comparator, Index<K, V> row, Node<K, V> origin, K fence, int est) {
            this.comparator = comparator;
            this.row = row;
            this.current = origin;
            this.fence = fence;
            this.est = est;
        }

        public final long estimateSize() {
            return (long) this.est;
        }
    }

    abstract class Iter<T> implements Iterator<T> {
        Node<K, V> lastReturned;
        Node<K, V> next;
        V nextValue;

        Iter() {
            while (true) {
                Node findFirst = ConcurrentSkipListMap.this.findFirst();
                this.next = findFirst;
                if (findFirst != null) {
                    V x = this.next.value;
                    if (x != null && x != this.next) {
                        V vv = x;
                        this.nextValue = x;
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        public final boolean hasNext() {
            return this.next != null;
        }

        final void advance() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            this.lastReturned = this.next;
            while (true) {
                Node node = this.next.next;
                this.next = node;
                if (node != null) {
                    V x = this.next.value;
                    if (x != null && x != this.next) {
                        V vv = x;
                        this.nextValue = x;
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        public void remove() {
            Node<K, V> l = this.lastReturned;
            if (l == null) {
                throw new IllegalStateException();
            }
            ConcurrentSkipListMap.this.remove(l.key);
            this.lastReturned = null;
        }
    }

    final class EntryIterator extends Iter<Entry<K, V>> {
        EntryIterator() {
            super();
        }

        public Entry<K, V> next() {
            Node<K, V> n = this.next;
            V v = this.nextValue;
            advance();
            return new SimpleImmutableEntry(n.key, v);
        }
    }

    static final class EntrySet<K, V> extends AbstractSet<Entry<K, V>> {
        final ConcurrentNavigableMap<K, V> m;

        EntrySet(ConcurrentNavigableMap<K, V> map) {
            this.m = map;
        }

        public Iterator<Entry<K, V>> iterator() {
            if (this.m instanceof ConcurrentSkipListMap) {
                ConcurrentSkipListMap concurrentSkipListMap = (ConcurrentSkipListMap) this.m;
                concurrentSkipListMap.getClass();
                return new EntryIterator();
            }
            SubMap subMap = (SubMap) this.m;
            subMap.getClass();
            return new SubMapEntryIterator();
        }

        public boolean contains(Object o) {
            boolean z = false;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> e = (Entry) o;
            V v = this.m.get(e.getKey());
            if (v != null) {
                z = v.lambda$-java_util_function_Predicate_4628(e.getValue());
            }
            return z;
        }

        public boolean remove(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> e = (Entry) o;
            return this.m.remove(e.getKey(), e.getValue());
        }

        public boolean isEmpty() {
            return this.m.isEmpty();
        }

        public int size() {
            return this.m.size();
        }

        public void clear() {
            this.m.clear();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (o == this) {
                return true;
            }
            if (!(o instanceof Set)) {
                return false;
            }
            Collection<?> c = (Collection) o;
            try {
                if (containsAll(c)) {
                    z = c.containsAll(this);
                }
                return z;
            } catch (ClassCastException e) {
                return false;
            } catch (NullPointerException e2) {
                return false;
            }
        }

        public Object[] toArray() {
            return ConcurrentSkipListMap.toList(this).toArray();
        }

        public <T> T[] toArray(T[] a) {
            return ConcurrentSkipListMap.toList(this).toArray(a);
        }

        public Spliterator<Entry<K, V>> spliterator() {
            if (this.m instanceof ConcurrentSkipListMap) {
                return ((ConcurrentSkipListMap) this.m).entrySpliterator();
            }
            SubMap subMap = (SubMap) this.m;
            subMap.getClass();
            return new SubMapEntryIterator();
        }

        public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
            if (filter == null) {
                throw new NullPointerException();
            } else if (this.m instanceof ConcurrentSkipListMap) {
                return ((ConcurrentSkipListMap) this.m).removeEntryIf(filter);
            } else {
                SubMap subMap = (SubMap) this.m;
                subMap.getClass();
                Iterator<Entry<K, V>> it = new SubMapEntryIterator();
                boolean removed = false;
                while (it.hasNext()) {
                    Entry<K, V> e = (Entry) it.next();
                    if (filter.test(e) && this.m.remove(e.getKey(), e.getValue())) {
                        removed = true;
                    }
                }
                return removed;
            }
        }
    }

    static final class EntrySpliterator<K, V> extends CSLMSpliterator<K, V> implements Spliterator<Entry<K, V>> {
        EntrySpliterator(Comparator<? super K> comparator, Index<K, V> row, Node<K, V> origin, K fence, int est) {
            super(comparator, row, origin, fence, est);
        }

        public EntrySpliterator<K, V> trySplit() {
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            Node<K, V> e = this.current;
            if (e != null) {
                K ek = e.key;
                if (ek != null) {
                    Index<K, V> q = this.row;
                    while (q != null) {
                        Index<K, V> s = q.right;
                        if (s != null) {
                            Node<K, V> b = s.node;
                            if (b != null) {
                                Node<K, V> n = b.next;
                                if (!(n == null || n.value == null)) {
                                    K sk = n.key;
                                    if (sk != null && ConcurrentSkipListMap.cpr(cmp, sk, ek) > 0 && (f == null || ConcurrentSkipListMap.cpr(cmp, sk, f) < 0)) {
                                        this.current = n;
                                        Index<K, V> r = q.down;
                                        if (s.right == null) {
                                            s = s.down;
                                        }
                                        this.row = s;
                                        this.est -= this.est >>> 2;
                                        return new EntrySpliterator(cmp, r, e, sk, this.est);
                                    }
                                }
                            }
                            continue;
                        }
                        q = q.down;
                        this.row = q;
                    }
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            V e = this.current;
            this.current = null;
            while (e != null) {
                K k = e.key;
                if (k == null || f == null || ConcurrentSkipListMap.cpr(cmp, f, k) > 0) {
                    V v = e.value;
                    if (!(v == null || v == e)) {
                        V vv = v;
                        action.accept(new SimpleImmutableEntry(k, v));
                    }
                    e = e.next;
                } else {
                    return;
                }
            }
        }

        public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K, V> e;
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            V e2 = this.current;
            while (e2 != null) {
                K k = e2.key;
                if (k != null && f != null && ConcurrentSkipListMap.cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                V v = e2.value;
                if (v == null || v == e2) {
                    e2 = e2.next;
                } else {
                    this.current = e2.next;
                    V vv = v;
                    action.accept(new SimpleImmutableEntry(k, v));
                    return true;
                }
            }
            this.current = e;
            return false;
        }

        public int characteristics() {
            return 4373;
        }

        public final Comparator<Entry<K, V>> getComparator() {
            if (this.comparator != null) {
                return Entry.comparingByKey(this.comparator);
            }
            return new -$Lambda$maBT9lzzYgJpLy8T-HMyKdSOI8M();
        }
    }

    static class Index<K, V> {
        private static final long RIGHT;
        private static final Unsafe U = Unsafe.getUnsafe();
        final Index<K, V> down;
        final Node<K, V> node;
        volatile Index<K, V> right;

        Index(Node<K, V> node, Index<K, V> down, Index<K, V> right) {
            this.node = node;
            this.down = down;
            this.right = right;
        }

        final boolean casRight(Index<K, V> cmp, Index<K, V> val) {
            return U.compareAndSwapObject(this, RIGHT, cmp, val);
        }

        final boolean indexesDeletedNode() {
            return this.node.value == null;
        }

        final boolean link(Index<K, V> succ, Index<K, V> newSucc) {
            Node<K, V> n = this.node;
            newSucc.right = succ;
            return n.value != null ? casRight(succ, newSucc) : false;
        }

        final boolean unlink(Index<K, V> succ) {
            return this.node.value != null ? casRight(succ, succ.right) : false;
        }

        static {
            try {
                RIGHT = U.objectFieldOffset(Index.class.getDeclaredField("right"));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
    }

    static final class HeadIndex<K, V> extends Index<K, V> {
        final int level;

        HeadIndex(Node<K, V> node, Index<K, V> down, Index<K, V> right, int level) {
            super(node, down, right);
            this.level = level;
        }
    }

    final class KeyIterator extends Iter<K> {
        KeyIterator() {
            super();
        }

        public K next() {
            Node<K, V> n = this.next;
            advance();
            return n.key;
        }
    }

    static final class KeySet<K, V> extends AbstractSet<K> implements NavigableSet<K> {
        final ConcurrentNavigableMap<K, V> m;

        KeySet(ConcurrentNavigableMap<K, V> map) {
            this.m = map;
        }

        public int size() {
            return this.m.size();
        }

        public boolean isEmpty() {
            return this.m.isEmpty();
        }

        public boolean contains(Object o) {
            return this.m.containsKey(o);
        }

        public boolean remove(Object o) {
            return this.m.remove(o) != null;
        }

        public void clear() {
            this.m.clear();
        }

        public K lower(K e) {
            return this.m.lowerKey(e);
        }

        public K floor(K e) {
            return this.m.floorKey(e);
        }

        public K ceiling(K e) {
            return this.m.ceilingKey(e);
        }

        public K higher(K e) {
            return this.m.higherKey(e);
        }

        public Comparator<? super K> comparator() {
            return this.m.comparator();
        }

        public K first() {
            return this.m.firstKey();
        }

        public K last() {
            return this.m.lastKey();
        }

        public K pollFirst() {
            Entry<K, V> e = this.m.pollFirstEntry();
            if (e == null) {
                return null;
            }
            return e.getKey();
        }

        public K pollLast() {
            Entry<K, V> e = this.m.pollLastEntry();
            if (e == null) {
                return null;
            }
            return e.getKey();
        }

        public Iterator<K> iterator() {
            if (this.m instanceof ConcurrentSkipListMap) {
                ConcurrentSkipListMap concurrentSkipListMap = (ConcurrentSkipListMap) this.m;
                concurrentSkipListMap.getClass();
                return new KeyIterator();
            }
            SubMap subMap = (SubMap) this.m;
            subMap.getClass();
            return new SubMapKeyIterator();
        }

        public boolean equals(Object o) {
            boolean z = false;
            if (o == this) {
                return true;
            }
            if (!(o instanceof Set)) {
                return false;
            }
            Collection<?> c = (Collection) o;
            try {
                if (containsAll(c)) {
                    z = c.containsAll(this);
                }
                return z;
            } catch (ClassCastException e) {
                return false;
            } catch (NullPointerException e2) {
                return false;
            }
        }

        public Object[] toArray() {
            return ConcurrentSkipListMap.toList(this).toArray();
        }

        public <T> T[] toArray(T[] a) {
            return ConcurrentSkipListMap.toList(this).toArray(a);
        }

        public Iterator<K> descendingIterator() {
            return descendingSet().iterator();
        }

        public NavigableSet<K> subSet(K fromElement, boolean fromInclusive, K toElement, boolean toInclusive) {
            return new KeySet(this.m.subMap((Object) fromElement, fromInclusive, (Object) toElement, toInclusive));
        }

        public NavigableSet<K> headSet(K toElement, boolean inclusive) {
            return new KeySet(this.m.headMap((Object) toElement, inclusive));
        }

        public NavigableSet<K> tailSet(K fromElement, boolean inclusive) {
            return new KeySet(this.m.tailMap((Object) fromElement, inclusive));
        }

        public NavigableSet<K> subSet(K fromElement, K toElement) {
            return subSet(fromElement, true, toElement, false);
        }

        public NavigableSet<K> headSet(K toElement) {
            return headSet(toElement, false);
        }

        public NavigableSet<K> tailSet(K fromElement) {
            return tailSet(fromElement, true);
        }

        public NavigableSet<K> descendingSet() {
            return new KeySet(this.m.descendingMap());
        }

        public Spliterator<K> spliterator() {
            if (this.m instanceof ConcurrentSkipListMap) {
                return ((ConcurrentSkipListMap) this.m).keySpliterator();
            }
            SubMap subMap = (SubMap) this.m;
            subMap.getClass();
            return new SubMapKeyIterator();
        }
    }

    static final class KeySpliterator<K, V> extends CSLMSpliterator<K, V> implements Spliterator<K> {
        KeySpliterator(Comparator<? super K> comparator, Index<K, V> row, Node<K, V> origin, K fence, int est) {
            super(comparator, row, origin, fence, est);
        }

        public KeySpliterator<K, V> trySplit() {
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            Node<K, V> e = this.current;
            if (e != null) {
                K ek = e.key;
                if (ek != null) {
                    Index<K, V> q = this.row;
                    while (q != null) {
                        Index<K, V> s = q.right;
                        if (s != null) {
                            Node<K, V> b = s.node;
                            if (b != null) {
                                Node<K, V> n = b.next;
                                if (!(n == null || n.value == null)) {
                                    K sk = n.key;
                                    if (sk != null && ConcurrentSkipListMap.cpr(cmp, sk, ek) > 0 && (f == null || ConcurrentSkipListMap.cpr(cmp, sk, f) < 0)) {
                                        this.current = n;
                                        Index<K, V> r = q.down;
                                        if (s.right == null) {
                                            s = s.down;
                                        }
                                        this.row = s;
                                        this.est -= this.est >>> 2;
                                        return new KeySpliterator(cmp, r, e, sk, this.est);
                                    }
                                }
                            }
                            continue;
                        }
                        q = q.down;
                        this.row = q;
                    }
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            Node<K, V> e = this.current;
            this.current = null;
            while (e != null) {
                K k = e.key;
                if (k == null || f == null || ConcurrentSkipListMap.cpr(cmp, f, k) > 0) {
                    Node<K, V> v = e.value;
                    if (!(v == null || v == e)) {
                        action.accept(k);
                    }
                    e = e.next;
                } else {
                    return;
                }
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            Node<K, V> e = this.current;
            while (e != null) {
                K k = e.key;
                if (k != null && f != null && ConcurrentSkipListMap.cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                Node<K, V> v = e.value;
                if (v == null || v == e) {
                    e = e.next;
                } else {
                    this.current = e.next;
                    action.accept(k);
                    return true;
                }
            }
            this.current = e;
            return false;
        }

        public int characteristics() {
            return 4373;
        }

        public final Comparator<? super K> getComparator() {
            return this.comparator;
        }
    }

    static final class Node<K, V> {
        private static final long NEXT;
        private static final Unsafe U = Unsafe.getUnsafe();
        private static final long VALUE;
        final K key;
        volatile Node<K, V> next;
        volatile Object value;

        Node(K key, Object value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        Node(Node<K, V> next) {
            this.key = null;
            this.value = this;
            this.next = next;
        }

        boolean casValue(Object cmp, Object val) {
            return U.compareAndSwapObject(this, VALUE, cmp, val);
        }

        boolean casNext(Node<K, V> cmp, Node<K, V> val) {
            return U.compareAndSwapObject(this, NEXT, cmp, val);
        }

        boolean isMarker() {
            return this.value == this;
        }

        boolean isBaseHeader() {
            return this.value == ConcurrentSkipListMap.BASE_HEADER;
        }

        boolean appendMarker(Node<K, V> f) {
            return casNext(f, new Node(f));
        }

        void helpDelete(Node<K, V> b, Node<K, V> f) {
            if (f != this.next || this != b.next) {
                return;
            }
            if (f == null || f.value != f) {
                casNext(f, new Node(f));
            } else {
                b.casNext(this, f.next);
            }
        }

        V getValidValue() {
            V v = this.value;
            if (v == this || v == ConcurrentSkipListMap.BASE_HEADER) {
                return null;
            }
            V vv = v;
            return v;
        }

        SimpleImmutableEntry<K, V> createSnapshot() {
            V v = this.value;
            if (v == null || v == this || v == ConcurrentSkipListMap.BASE_HEADER) {
                return null;
            }
            V vv = v;
            return new SimpleImmutableEntry(this.key, v);
        }

        static {
            try {
                VALUE = U.objectFieldOffset(Node.class.getDeclaredField("value"));
                NEXT = U.objectFieldOffset(Node.class.getDeclaredField("next"));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
    }

    static final class SubMap<K, V> extends AbstractMap<K, V> implements ConcurrentNavigableMap<K, V>, Cloneable, Serializable {
        private static final long serialVersionUID = -7647078645895051609L;
        private transient Set<Entry<K, V>> entrySetView;
        private final K hi;
        private final boolean hiInclusive;
        final boolean isDescending;
        private transient KeySet<K, V> keySetView;
        private final K lo;
        private final boolean loInclusive;
        final ConcurrentSkipListMap<K, V> m;
        private transient Collection<V> valuesView;

        abstract class SubMapIter<T> implements Iterator<T>, Spliterator<T> {
            Node<K, V> lastReturned;
            Node<K, V> next;
            V nextValue;

            SubMapIter() {
                Comparator<? super K> cmp = SubMap.this.m.comparator;
                while (true) {
                    this.next = SubMap.this.isDescending ? SubMap.this.hiNode(cmp) : SubMap.this.loNode(cmp);
                    if (this.next != null) {
                        V x = this.next.value;
                        if (x != null && x != this.next) {
                            if (SubMap.this.inBounds(this.next.key, cmp)) {
                                V vv = x;
                                this.nextValue = x;
                                return;
                            }
                            this.next = null;
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }

            public final boolean hasNext() {
                return this.next != null;
            }

            final void advance() {
                if (this.next == null) {
                    throw new NoSuchElementException();
                }
                this.lastReturned = this.next;
                if (SubMap.this.isDescending) {
                    descend();
                } else {
                    ascend();
                }
            }

            private void ascend() {
                Comparator<? super K> cmp = SubMap.this.m.comparator;
                while (true) {
                    this.next = this.next.next;
                    if (this.next != null) {
                        V x = this.next.value;
                        if (x != null && x != this.next) {
                            if (SubMap.this.tooHigh(this.next.key, cmp)) {
                                this.next = null;
                                return;
                            }
                            V vv = x;
                            this.nextValue = x;
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }

            private void descend() {
                Comparator<? super K> cmp = SubMap.this.m.comparator;
                while (true) {
                    this.next = SubMap.this.m.findNear(this.lastReturned.key, 2, cmp);
                    if (this.next != null) {
                        V x = this.next.value;
                        if (x != null && x != this.next) {
                            if (SubMap.this.tooLow(this.next.key, cmp)) {
                                this.next = null;
                                return;
                            }
                            V vv = x;
                            this.nextValue = x;
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }

            public void remove() {
                Node<K, V> l = this.lastReturned;
                if (l == null) {
                    throw new IllegalStateException();
                }
                SubMap.this.m.remove(l.key);
                this.lastReturned = null;
            }

            public Spliterator<T> trySplit() {
                return null;
            }

            public boolean tryAdvance(Consumer<? super T> action) {
                if (!hasNext()) {
                    return false;
                }
                action.accept(next());
                return true;
            }

            public void forEachRemaining(Consumer<? super T> action) {
                while (hasNext()) {
                    action.accept(next());
                }
            }

            public long estimateSize() {
                return Long.MAX_VALUE;
            }
        }

        final class SubMapEntryIterator extends SubMapIter<Entry<K, V>> {
            SubMapEntryIterator() {
                super();
            }

            public Entry<K, V> next() {
                Node<K, V> n = this.next;
                V v = this.nextValue;
                advance();
                return new SimpleImmutableEntry(n.key, v);
            }

            public int characteristics() {
                return 1;
            }
        }

        final class SubMapKeyIterator extends SubMapIter<K> {
            SubMapKeyIterator() {
                super();
            }

            public K next() {
                Node<K, V> n = this.next;
                advance();
                return n.key;
            }

            public int characteristics() {
                return 21;
            }

            public final Comparator<? super K> getComparator() {
                return SubMap.this.comparator();
            }
        }

        final class SubMapValueIterator extends SubMapIter<V> {
            SubMapValueIterator() {
                super();
            }

            public V next() {
                V v = this.nextValue;
                advance();
                return v;
            }

            public int characteristics() {
                return 0;
            }
        }

        SubMap(ConcurrentSkipListMap<K, V> map, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive, boolean isDescending) {
            Comparator<? super K> cmp = map.comparator;
            if (fromKey == null || toKey == null || ConcurrentSkipListMap.cpr(cmp, fromKey, toKey) <= 0) {
                this.m = map;
                this.lo = fromKey;
                this.hi = toKey;
                this.loInclusive = fromInclusive;
                this.hiInclusive = toInclusive;
                this.isDescending = isDescending;
                return;
            }
            throw new IllegalArgumentException("inconsistent range");
        }

        boolean tooLow(Object key, Comparator<? super K> cmp) {
            if (this.lo == null) {
                return false;
            }
            int c = ConcurrentSkipListMap.cpr(cmp, key, this.lo);
            if (c < 0) {
                return true;
            }
            if (c == 0) {
                return this.loInclusive ^ 1;
            }
            return false;
        }

        boolean tooHigh(Object key, Comparator<? super K> cmp) {
            if (this.hi == null) {
                return false;
            }
            int c = ConcurrentSkipListMap.cpr(cmp, key, this.hi);
            if (c > 0) {
                return true;
            }
            if (c == 0) {
                return this.hiInclusive ^ 1;
            }
            return false;
        }

        boolean inBounds(Object key, Comparator<? super K> cmp) {
            return !tooLow(key, cmp) ? tooHigh(key, cmp) ^ 1 : false;
        }

        void checkKeyBounds(K key, Comparator<? super K> cmp) {
            if (key == null) {
                throw new NullPointerException();
            } else if (!inBounds(key, cmp)) {
                throw new IllegalArgumentException("key out of range");
            }
        }

        boolean isBeforeEnd(Node<K, V> n, Comparator<? super K> cmp) {
            if (n == null) {
                return false;
            }
            if (this.hi == null) {
                return true;
            }
            K k = n.key;
            if (k == null) {
                return true;
            }
            int c = ConcurrentSkipListMap.cpr(cmp, k, this.hi);
            return c <= 0 && (c != 0 || (this.hiInclusive ^ 1) == 0);
        }

        Node<K, V> loNode(Comparator<? super K> cmp) {
            if (this.lo == null) {
                return this.m.findFirst();
            }
            if (this.loInclusive) {
                return this.m.findNear(this.lo, 1, cmp);
            }
            return this.m.findNear(this.lo, 0, cmp);
        }

        Node<K, V> hiNode(Comparator<? super K> cmp) {
            if (this.hi == null) {
                return this.m.findLast();
            }
            if (this.hiInclusive) {
                return this.m.findNear(this.hi, 3, cmp);
            }
            return this.m.findNear(this.hi, 2, cmp);
        }

        K lowestKey() {
            Comparator<? super K> cmp = this.m.comparator;
            Node<K, V> n = loNode(cmp);
            if (isBeforeEnd(n, cmp)) {
                return n.key;
            }
            throw new NoSuchElementException();
        }

        K highestKey() {
            Comparator<? super K> cmp = this.m.comparator;
            Node<K, V> n = hiNode(cmp);
            if (n != null) {
                K last = n.key;
                if (inBounds(last, cmp)) {
                    return last;
                }
            }
            throw new NoSuchElementException();
        }

        Entry<K, V> lowestEntry() {
            Entry<K, V> e;
            Comparator<? super K> cmp = this.m.comparator;
            do {
                Node<K, V> n = loNode(cmp);
                if (!isBeforeEnd(n, cmp)) {
                    return null;
                }
                e = n.createSnapshot();
            } while (e == null);
            return e;
        }

        Entry<K, V> highestEntry() {
            Entry<K, V> e;
            Comparator<? super K> cmp = this.m.comparator;
            do {
                Node<K, V> n = hiNode(cmp);
                if (n == null || (inBounds(n.key, cmp) ^ 1) != 0) {
                    return null;
                }
                e = n.createSnapshot();
            } while (e == null);
            return e;
        }

        Entry<K, V> removeLowest() {
            V v;
            K k;
            Comparator<? super K> cmp = this.m.comparator;
            do {
                Node<K, V> n = loNode(cmp);
                if (n == null) {
                    return null;
                }
                k = n.key;
                if (!inBounds(k, cmp)) {
                    return null;
                }
                v = this.m.doRemove(k, null);
            } while (v == null);
            return new SimpleImmutableEntry(k, v);
        }

        Entry<K, V> removeHighest() {
            V v;
            K k;
            Comparator<? super K> cmp = this.m.comparator;
            do {
                Node<K, V> n = hiNode(cmp);
                if (n == null) {
                    return null;
                }
                k = n.key;
                if (!inBounds(k, cmp)) {
                    return null;
                }
                v = this.m.doRemove(k, null);
            } while (v == null);
            return new SimpleImmutableEntry(k, v);
        }

        Entry<K, V> getNearEntry(K key, int rel) {
            Entry<K, V> entry = null;
            Comparator<? super K> cmp = this.m.comparator;
            if (this.isDescending) {
                if ((rel & 2) == 0) {
                    rel |= 2;
                } else {
                    rel &= -3;
                }
            }
            if (tooLow(key, cmp)) {
                if ((rel & 2) == 0) {
                    entry = lowestEntry();
                }
                return entry;
            } else if (tooHigh(key, cmp)) {
                if ((rel & 2) != 0) {
                    entry = highestEntry();
                }
                return entry;
            } else {
                V v;
                K k;
                do {
                    Node<K, V> n = this.m.findNear(key, rel, cmp);
                    if (n == null || (inBounds(n.key, cmp) ^ 1) != 0) {
                        return null;
                    }
                    k = n.key;
                    v = n.getValidValue();
                } while (v == null);
                return new SimpleImmutableEntry(k, v);
            }
        }

        K getNearKey(K key, int rel) {
            Comparator<? super K> cmp = this.m.comparator;
            if (this.isDescending) {
                if ((rel & 2) == 0) {
                    rel |= 2;
                } else {
                    rel &= -3;
                }
            }
            Node<K, V> n;
            if (tooLow(key, cmp)) {
                if ((rel & 2) == 0) {
                    n = loNode(cmp);
                    if (isBeforeEnd(n, cmp)) {
                        return n.key;
                    }
                }
                return null;
            } else if (tooHigh(key, cmp)) {
                if ((rel & 2) != 0) {
                    n = hiNode(cmp);
                    if (n != null) {
                        K last = n.key;
                        if (inBounds(last, cmp)) {
                            return last;
                        }
                    }
                }
                return null;
            } else {
                K k;
                do {
                    n = this.m.findNear(key, rel, cmp);
                    if (n == null || (inBounds(n.key, cmp) ^ 1) != 0) {
                        return null;
                    }
                    k = n.key;
                } while (n.getValidValue() == null);
                return k;
            }
        }

        public boolean containsKey(Object key) {
            if (key != null) {
                return inBounds(key, this.m.comparator) ? this.m.containsKey(key) : false;
            } else {
                throw new NullPointerException();
            }
        }

        public V get(Object key) {
            if (key == null) {
                throw new NullPointerException();
            } else if (inBounds(key, this.m.comparator)) {
                return this.m.get(key);
            } else {
                return null;
            }
        }

        public V put(K key, V value) {
            checkKeyBounds(key, this.m.comparator);
            return this.m.put(key, value);
        }

        public V remove(Object key) {
            return !inBounds(key, this.m.comparator) ? null : this.m.remove(key);
        }

        public int size() {
            Comparator<? super K> cmp = this.m.comparator;
            long count = ConcurrentSkipListMap.HEAD;
            for (Node<K, V> n = loNode(cmp); isBeforeEnd(n, cmp); n = n.next) {
                if (n.getValidValue() != null) {
                    count++;
                }
            }
            return count >= 2147483647L ? Integer.MAX_VALUE : (int) count;
        }

        public boolean isEmpty() {
            Comparator<? super K> cmp = this.m.comparator;
            return isBeforeEnd(loNode(cmp), cmp) ^ 1;
        }

        public boolean containsValue(Object value) {
            if (value == null) {
                throw new NullPointerException();
            }
            Comparator<? super K> cmp = this.m.comparator;
            for (Node<K, V> n = loNode(cmp); isBeforeEnd(n, cmp); n = n.next) {
                V v = n.getValidValue();
                if (v != null && value.lambda$-java_util_function_Predicate_4628(v)) {
                    return true;
                }
            }
            return false;
        }

        public void clear() {
            Comparator<? super K> cmp = this.m.comparator;
            for (Node<K, V> n = loNode(cmp); isBeforeEnd(n, cmp); n = n.next) {
                if (n.getValidValue() != null) {
                    this.m.remove(n.key);
                }
            }
        }

        public V putIfAbsent(K key, V value) {
            checkKeyBounds(key, this.m.comparator);
            return this.m.putIfAbsent(key, value);
        }

        public boolean remove(Object key, Object value) {
            return inBounds(key, this.m.comparator) ? this.m.remove(key, value) : false;
        }

        public boolean replace(K key, V oldValue, V newValue) {
            checkKeyBounds(key, this.m.comparator);
            return this.m.replace(key, oldValue, newValue);
        }

        public V replace(K key, V value) {
            checkKeyBounds(key, this.m.comparator);
            return this.m.replace(key, value);
        }

        public Comparator<? super K> comparator() {
            Comparator<? super K> cmp = this.m.comparator();
            if (this.isDescending) {
                return Collections.reverseOrder(cmp);
            }
            return cmp;
        }

        SubMap<K, V> newSubMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            int c;
            Comparator<? super K> cmp = this.m.comparator;
            if (this.isDescending) {
                K tk = fromKey;
                fromKey = toKey;
                toKey = tk;
                boolean ti = fromInclusive;
                fromInclusive = toInclusive;
                toInclusive = ti;
            }
            if (this.lo != null) {
                if (fromKey == null) {
                    fromKey = this.lo;
                    fromInclusive = this.loInclusive;
                } else {
                    c = ConcurrentSkipListMap.cpr(cmp, fromKey, this.lo);
                    if (c < 0 || (c == 0 && (this.loInclusive ^ 1) != 0 && fromInclusive)) {
                        throw new IllegalArgumentException("key out of range");
                    }
                }
            }
            if (this.hi != null) {
                if (toKey == null) {
                    toKey = this.hi;
                    toInclusive = this.hiInclusive;
                } else {
                    c = ConcurrentSkipListMap.cpr(cmp, toKey, this.hi);
                    if (c > 0 || (c == 0 && (this.hiInclusive ^ 1) != 0 && toInclusive)) {
                        throw new IllegalArgumentException("key out of range");
                    }
                }
            }
            return new SubMap(this.m, fromKey, fromInclusive, toKey, toInclusive, this.isDescending);
        }

        public SubMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            if (fromKey != null && toKey != null) {
                return newSubMap(fromKey, fromInclusive, toKey, toInclusive);
            }
            throw new NullPointerException();
        }

        public SubMap<K, V> headMap(K toKey, boolean inclusive) {
            if (toKey != null) {
                return newSubMap(null, false, toKey, inclusive);
            }
            throw new NullPointerException();
        }

        public SubMap<K, V> tailMap(K fromKey, boolean inclusive) {
            if (fromKey != null) {
                return newSubMap(fromKey, inclusive, null, false);
            }
            throw new NullPointerException();
        }

        public SubMap<K, V> subMap(K fromKey, K toKey) {
            return subMap((Object) fromKey, true, (Object) toKey, false);
        }

        public SubMap<K, V> headMap(K toKey) {
            return headMap((Object) toKey, false);
        }

        public SubMap<K, V> tailMap(K fromKey) {
            return tailMap((Object) fromKey, true);
        }

        public SubMap<K, V> descendingMap() {
            return new SubMap(this.m, this.lo, this.loInclusive, this.hi, this.hiInclusive, this.isDescending ^ 1);
        }

        public Entry<K, V> ceilingEntry(K key) {
            return getNearEntry(key, 1);
        }

        public K ceilingKey(K key) {
            return getNearKey(key, 1);
        }

        public Entry<K, V> lowerEntry(K key) {
            return getNearEntry(key, 2);
        }

        public K lowerKey(K key) {
            return getNearKey(key, 2);
        }

        public Entry<K, V> floorEntry(K key) {
            return getNearEntry(key, 3);
        }

        public K floorKey(K key) {
            return getNearKey(key, 3);
        }

        public Entry<K, V> higherEntry(K key) {
            return getNearEntry(key, 0);
        }

        public K higherKey(K key) {
            return getNearKey(key, 0);
        }

        public K firstKey() {
            return this.isDescending ? highestKey() : lowestKey();
        }

        public K lastKey() {
            return this.isDescending ? lowestKey() : highestKey();
        }

        public Entry<K, V> firstEntry() {
            return this.isDescending ? highestEntry() : lowestEntry();
        }

        public Entry<K, V> lastEntry() {
            return this.isDescending ? lowestEntry() : highestEntry();
        }

        public Entry<K, V> pollFirstEntry() {
            return this.isDescending ? removeHighest() : removeLowest();
        }

        public Entry<K, V> pollLastEntry() {
            return this.isDescending ? removeLowest() : removeHighest();
        }

        public NavigableSet<K> keySet() {
            KeySet<K, V> ks = this.keySetView;
            if (ks != null) {
                return ks;
            }
            ks = new KeySet(this);
            this.keySetView = ks;
            return ks;
        }

        public NavigableSet<K> navigableKeySet() {
            KeySet<K, V> ks = this.keySetView;
            if (ks != null) {
                return ks;
            }
            ks = new KeySet(this);
            this.keySetView = ks;
            return ks;
        }

        public Collection<V> values() {
            Collection<V> vs = this.valuesView;
            if (vs != null) {
                return vs;
            }
            vs = new Values(this);
            this.valuesView = vs;
            return vs;
        }

        public Set<Entry<K, V>> entrySet() {
            Set<Entry<K, V>> es = this.entrySetView;
            if (es != null) {
                return es;
            }
            es = new EntrySet(this);
            this.entrySetView = es;
            return es;
        }

        public NavigableSet<K> descendingKeySet() {
            return descendingMap().navigableKeySet();
        }
    }

    final class ValueIterator extends Iter<V> {
        ValueIterator() {
            super();
        }

        public V next() {
            V v = this.nextValue;
            advance();
            return v;
        }
    }

    static final class ValueSpliterator<K, V> extends CSLMSpliterator<K, V> implements Spliterator<V> {
        ValueSpliterator(Comparator<? super K> comparator, Index<K, V> row, Node<K, V> origin, K fence, int est) {
            super(comparator, row, origin, fence, est);
        }

        public ValueSpliterator<K, V> trySplit() {
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            Node<K, V> e = this.current;
            if (e != null) {
                K ek = e.key;
                if (ek != null) {
                    Index<K, V> q = this.row;
                    while (q != null) {
                        Index<K, V> s = q.right;
                        if (s != null) {
                            Node<K, V> b = s.node;
                            if (b != null) {
                                Node<K, V> n = b.next;
                                if (!(n == null || n.value == null)) {
                                    K sk = n.key;
                                    if (sk != null && ConcurrentSkipListMap.cpr(cmp, sk, ek) > 0 && (f == null || ConcurrentSkipListMap.cpr(cmp, sk, f) < 0)) {
                                        this.current = n;
                                        Index<K, V> r = q.down;
                                        if (s.right == null) {
                                            s = s.down;
                                        }
                                        this.row = s;
                                        this.est -= this.est >>> 2;
                                        return new ValueSpliterator(cmp, r, e, sk, this.est);
                                    }
                                }
                            }
                            continue;
                        }
                        q = q.down;
                        this.row = q;
                    }
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            V e = this.current;
            this.current = null;
            while (e != null) {
                K k = e.key;
                if (k == null || f == null || ConcurrentSkipListMap.cpr(cmp, f, k) > 0) {
                    V v = e.value;
                    if (!(v == null || v == e)) {
                        V vv = v;
                        action.accept(v);
                    }
                    e = e.next;
                } else {
                    return;
                }
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K, V> e;
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            V e2 = this.current;
            while (e2 != null) {
                K k = e2.key;
                if (k != null && f != null && ConcurrentSkipListMap.cpr(cmp, f, k) <= 0) {
                    e = null;
                    break;
                }
                V v = e2.value;
                if (v == null || v == e2) {
                    e2 = e2.next;
                } else {
                    this.current = e2.next;
                    V vv = v;
                    action.accept(v);
                    return true;
                }
            }
            this.current = e;
            return false;
        }

        public int characteristics() {
            return 4368;
        }
    }

    static final class Values<K, V> extends AbstractCollection<V> {
        final ConcurrentNavigableMap<K, V> m;

        Values(ConcurrentNavigableMap<K, V> map) {
            this.m = map;
        }

        public Iterator<V> iterator() {
            if (this.m instanceof ConcurrentSkipListMap) {
                ConcurrentSkipListMap concurrentSkipListMap = (ConcurrentSkipListMap) this.m;
                concurrentSkipListMap.getClass();
                return new ValueIterator();
            }
            SubMap subMap = (SubMap) this.m;
            subMap.getClass();
            return new SubMapValueIterator();
        }

        public int size() {
            return this.m.size();
        }

        public boolean isEmpty() {
            return this.m.isEmpty();
        }

        public boolean contains(Object o) {
            return this.m.containsValue(o);
        }

        public void clear() {
            this.m.clear();
        }

        public Object[] toArray() {
            return ConcurrentSkipListMap.toList(this).toArray();
        }

        public <T> T[] toArray(T[] a) {
            return ConcurrentSkipListMap.toList(this).toArray(a);
        }

        public Spliterator<V> spliterator() {
            if (this.m instanceof ConcurrentSkipListMap) {
                return ((ConcurrentSkipListMap) this.m).valueSpliterator();
            }
            SubMap subMap = (SubMap) this.m;
            subMap.getClass();
            return new SubMapValueIterator();
        }

        public boolean removeIf(Predicate<? super V> filter) {
            if (filter == null) {
                throw new NullPointerException();
            } else if (this.m instanceof ConcurrentSkipListMap) {
                return ((ConcurrentSkipListMap) this.m).removeValueIf(filter);
            } else {
                SubMap subMap = (SubMap) this.m;
                subMap.getClass();
                Iterator<Entry<K, V>> it = new SubMapEntryIterator();
                boolean removed = false;
                while (it.hasNext()) {
                    Entry<K, V> e = (Entry) it.next();
                    V v = e.getValue();
                    if (filter.test(v) && this.m.remove(e.getKey(), v)) {
                        removed = true;
                    }
                }
                return removed;
            }
        }
    }

    static {
        try {
            HEAD = U.objectFieldOffset(ConcurrentSkipListMap.class.getDeclaredField("head"));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    private void initialize() {
        this.keySet = null;
        this.entrySet = null;
        this.values = null;
        this.descendingMap = null;
        this.head = new HeadIndex(new Node(null, BASE_HEADER, null), null, null, 1);
    }

    private boolean casHead(HeadIndex<K, V> cmp, HeadIndex<K, V> val) {
        return U.compareAndSwapObject(this, HEAD, cmp, val);
    }

    static final int cpr(Comparator c, Object x, Object y) {
        return c != null ? c.compare(x, y) : ((Comparable) x).compareTo(y);
    }

    private Node<K, V> findPredecessor(Object key, Comparator<? super K> cmp) {
        if (key == null) {
            throw new NullPointerException();
        }
        while (true) {
            Index<K, V> q = this.head;
            Index<K, V> r = q.right;
            while (true) {
                if (r != null) {
                    Node<K, V> n = r.node;
                    K k = n.key;
                    if (n.value == null) {
                        if (q.unlink(r)) {
                            r = q.right;
                        }
                    } else if (cpr(cmp, key, k) > 0) {
                        q = r;
                        r = r.right;
                    }
                }
                Index<K, V> d = q.down;
                if (d == null) {
                    return q.node;
                }
                q = d;
                r = d.right;
            }
        }
    }

    private Node<K, V> findNode(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }
        Comparator<? super K> cmp = this.comparator;
        loop0:
        while (true) {
            Node<K, V> b = findPredecessor(key, cmp);
            Node<K, V> n = b.next;
            while (n != null) {
                Node<K, V> f = n.next;
                if (n == b.next) {
                    Node<K, V> v = n.value;
                    if (v != null) {
                        if (!(b.value == null || v == n)) {
                            int c = cpr(cmp, key, n.key);
                            if (c != 0) {
                                if (c < 0) {
                                    break loop0;
                                }
                                b = n;
                                n = f;
                            } else {
                                return n;
                            }
                        }
                    }
                    n.helpDelete(b, f);
                }
            }
            break loop0;
        }
        return null;
    }

    private V doGet(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }
        Comparator<? super K> cmp = this.comparator;
        loop0:
        while (true) {
            Node<K, V> b = findPredecessor(key, cmp);
            V n = b.next;
            while (n != null) {
                V f = n.next;
                if (n == b.next) {
                    V v = n.value;
                    if (v != null) {
                        if (!(b.value == null || v == n)) {
                            int c = cpr(cmp, key, n.key);
                            if (c != 0) {
                                if (c < 0) {
                                    break loop0;
                                }
                                V b2 = n;
                                n = f;
                            } else {
                                V vv = v;
                                return v;
                            }
                        }
                    }
                    n.helpDelete(b2, f);
                }
            }
            break loop0;
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:86:0x000c A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0081 A:{SYNTHETIC} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private V doPut(K key, V value, boolean onlyIfAbsent) {
        if (key == null) {
            throw new NullPointerException();
        }
        V v;
        V vv;
        Comparator<? super K> cmp = this.comparator;
        while (true) {
            Node<K, V> node;
            Node<K, V> b = findPredecessor(key, cmp);
            V n = b.next;
            while (n != null) {
                V f = n.next;
                if (n != b.next) {
                    continue;
                    break;
                }
                v = n.value;
                if (v != null) {
                    if (b.value == null || v == n) {
                        break;
                    }
                    int c = cpr(cmp, key, n.key);
                    if (c > 0) {
                        V b2 = n;
                        n = f;
                    } else {
                        if (c == 0) {
                            if (onlyIfAbsent || n.casValue(v, value)) {
                                vv = v;
                            }
                        }
                        node = new Node(key, value, n);
                        if (!b2.casNext(n, node)) {
                            int rnd = ThreadLocalRandom.nextSecondarySeed();
                            if ((-2147483647 & rnd) == 0) {
                                Index<K, V> idx;
                                int j;
                                int level = 1;
                                while (true) {
                                    rnd >>>= 1;
                                    if ((rnd & 1) == 0) {
                                        break;
                                    }
                                    level++;
                                }
                                Index<K, V> h = this.head;
                                int max = h.level;
                                int i;
                                Index<K, V> idx2;
                                if (level <= max) {
                                    i = 1;
                                    idx2 = null;
                                    while (i <= level) {
                                        i++;
                                        idx2 = new Index(node, idx2, null);
                                    }
                                    idx = idx2;
                                } else {
                                    Index<K, V> newh;
                                    int oldLevel;
                                    level = max + 1;
                                    Index<K, V>[] idxs = new Index[(level + 1)];
                                    i = 1;
                                    idx2 = null;
                                    while (i <= level) {
                                        idx = new Index(node, idx2, null);
                                        idxs[i] = idx;
                                        i++;
                                        idx2 = idx;
                                    }
                                    do {
                                        h = this.head;
                                        oldLevel = h.level;
                                        if (level <= oldLevel) {
                                            idx = idx2;
                                            break;
                                        }
                                        Index<K, V> newh2 = h;
                                        Node<K, V> oldbase = h.node;
                                        j = oldLevel + 1;
                                        newh = newh2;
                                        while (j <= level) {
                                            Index<K, V> headIndex = new HeadIndex(oldbase, newh, idxs[j], j);
                                            j++;
                                            newh = headIndex;
                                        }
                                    } while (!casHead(h, newh));
                                    h = newh;
                                    level = oldLevel;
                                    idx = idxs[oldLevel];
                                }
                                int insertionLevel = level;
                                loop6:
                                while (true) {
                                    j = h.level;
                                    Index<K, V> q = h;
                                    Index<K, V> r = h.right;
                                    Index<K, V> t = idx;
                                    while (q != null && t != null) {
                                        if (r != null) {
                                            Node<K, V> n2 = r.node;
                                            c = cpr(cmp, key, n2.key);
                                            if (n2.value == null) {
                                                if (q.unlink(r)) {
                                                    r = q.right;
                                                }
                                            } else if (c > 0) {
                                                q = r;
                                                r = r.right;
                                            }
                                        }
                                        if (j == insertionLevel) {
                                            if (q.link(r, t)) {
                                                if (t.node.value != null) {
                                                    insertionLevel--;
                                                    if (insertionLevel == 0) {
                                                        break loop6;
                                                    }
                                                }
                                                findNode(key);
                                                break loop6;
                                            }
                                            continue;
                                        }
                                        j--;
                                        if (j >= insertionLevel && j < level) {
                                            t = t.down;
                                        }
                                        q = q.down;
                                        r = q.right;
                                    }
                                }
                            }
                            return null;
                        }
                    }
                } else {
                    n.helpDelete(b2, f);
                    break;
                }
            }
            node = new Node(key, value, n);
            if (!b2.casNext(n, node)) {
            }
        }
        vv = v;
        return v;
    }

    final V doRemove(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException();
        }
        Comparator<? super K> cmp = this.comparator;
        loop0:
        while (true) {
            Node<K, V> b = findPredecessor(key, cmp);
            V n = b.next;
            while (n != null) {
                V f = n.next;
                if (n == b.next) {
                    V v = n.value;
                    if (v != null) {
                        if (!(b.value == null || v == n)) {
                            int c = cpr(cmp, key, n.key);
                            if (c >= 0) {
                                if (c <= 0) {
                                    if (value != null && (value.lambda$-java_util_function_Predicate_4628(v) ^ 1) != 0) {
                                        break;
                                    } else if (n.casValue(v, null)) {
                                        if (n.appendMarker(f) && (b.casNext(n, f) ^ 1) == 0) {
                                            findPredecessor(key, cmp);
                                            if (this.head.right == null) {
                                                tryReduceLevel();
                                            }
                                        } else {
                                            findNode(key);
                                        }
                                        V vv = v;
                                        return v;
                                    }
                                } else {
                                    V b2 = n;
                                    n = f;
                                }
                            } else {
                                break loop0;
                            }
                        }
                    }
                    n.helpDelete(b2, f);
                }
            }
            break loop0;
        }
        return null;
    }

    private void tryReduceLevel() {
        HeadIndex<K, V> h = this.head;
        if (h.level > 3) {
            HeadIndex<K, V> d = h.down;
            if (d != null) {
                HeadIndex<K, V> e = d.down;
                if (e != null && e.right == null && d.right == null && h.right == null && casHead(h, d) && h.right != null) {
                    casHead(d, h);
                }
            }
        }
    }

    final Node<K, V> findFirst() {
        while (true) {
            Node<K, V> b = this.head.node;
            Node<K, V> n = b.next;
            if (n == null) {
                return null;
            }
            if (n.value != null) {
                return n;
            }
            n.helpDelete(b, n.next);
        }
    }

    private Entry<K, V> doRemoveFirstEntry() {
        while (true) {
            Node<K, V> b = this.head.node;
            Node<K, V> n = b.next;
            if (n == null) {
                return null;
            }
            Node<K, V> f = n.next;
            if (n == b.next) {
                V v = n.value;
                if (v == null) {
                    n.helpDelete(b, f);
                } else if (n.casValue(v, null)) {
                    if (!(n.appendMarker(f) && (b.casNext(n, f) ^ 1) == 0)) {
                        findFirst();
                    }
                    clearIndexToFirst();
                    V vv = v;
                    return new SimpleImmutableEntry(n.key, v);
                }
            }
        }
    }

    private void clearIndexToFirst() {
        loop0:
        while (true) {
            Index<K, V> q = this.head;
            while (true) {
                Index<K, V> r = q.right;
                if (r == null || !r.indexesDeletedNode() || (q.unlink(r) ^ 1) == 0) {
                    q = q.down;
                    if (q == null) {
                        break loop0;
                    }
                }
            }
        }
        if (this.head.right == null) {
            tryReduceLevel();
        }
    }

    private Entry<K, V> doRemoveLastEntry() {
        while (true) {
            Node<K, V> b = findPredecessorOfLast();
            V n = b.next;
            if (n != null) {
                while (true) {
                    V f = n.next;
                    if (n != b.next) {
                        continue;
                        break;
                    }
                    V v = n.value;
                    if (v == null) {
                        n.helpDelete(b, f);
                        break;
                    } else if (b.value == null) {
                        continue;
                        break;
                    } else if (v == n) {
                        break;
                    } else if (f != null) {
                        b = n;
                        n = f;
                    } else if (n.casValue(v, null)) {
                        K key = n.key;
                        if (n.appendMarker(f) && (b.casNext(n, f) ^ 1) == 0) {
                            findPredecessor(key, this.comparator);
                            if (this.head.right == null) {
                                tryReduceLevel();
                            }
                        } else {
                            findNode(key);
                        }
                        V vv = v;
                        return new SimpleImmutableEntry(key, v);
                    }
                }
            } else if (b.isBaseHeader()) {
                return null;
            }
        }
    }

    final Node<K, V> findLast() {
        Node<K, V> b;
        Index<K, V> q = this.head;
        loop0:
        while (true) {
            Index<K, V> r = q.right;
            if (r == null) {
                Index<K, V> d = q.down;
                if (d == null) {
                    b = q.node;
                    Node<K, V> n = b.next;
                    while (n != null) {
                        Node<K, V> f = n.next;
                        if (n == b.next) {
                            Node<K, V> v = n.value;
                            if (v == null) {
                                n.helpDelete(b, f);
                            } else if (!(b.value == null || v == n)) {
                                b = n;
                                n = f;
                            }
                        }
                        q = this.head;
                    }
                    break loop0;
                }
                q = d;
            } else if (r.indexesDeletedNode()) {
                q.unlink(r);
                q = this.head;
            } else {
                q = r;
            }
        }
        return b.isBaseHeader() ? null : b;
    }

    private Node<K, V> findPredecessorOfLast() {
        while (true) {
            Index<K, V> r;
            Index<K, V> q = this.head;
            while (true) {
                r = q.right;
                if (r != null) {
                    if (r.indexesDeletedNode()) {
                        break;
                    } else if (r.node.next != null) {
                        q = r;
                    }
                }
                Index<K, V> d = q.down;
                if (d == null) {
                    return q.node;
                }
                q = d;
            }
            q.unlink(r);
        }
    }

    final Node<K, V> findNear(K key, int rel, Comparator<? super K> cmp) {
        Node<K, V> node = null;
        if (key == null) {
            throw new NullPointerException();
        }
        Node<K, V> n;
        loop0:
        while (true) {
            Node<K, V> b = findPredecessor(key, cmp);
            n = b.next;
            while (n != null) {
                Node<K, V> f = n.next;
                if (n == b.next) {
                    Node<K, V> v = n.value;
                    if (v == null) {
                        n.helpDelete(b, f);
                    } else if (!(b.value == null || v == n)) {
                        int c = cpr(cmp, key, n.key);
                        if ((c != 0 || (rel & 1) == 0) && (c >= 0 || (rel & 2) != 0)) {
                            if (c > 0 || (rel & 2) == 0) {
                                b = n;
                                n = f;
                            } else {
                                if (!b.isBaseHeader()) {
                                    node = b;
                                }
                                return node;
                            }
                        }
                    }
                }
            }
            if ((rel & 2) == 0 || b.isBaseHeader()) {
                b = null;
            }
            return b;
        }
        return n;
    }

    final SimpleImmutableEntry<K, V> getNear(K key, int rel) {
        SimpleImmutableEntry<K, V> e;
        Comparator<? super K> cmp = this.comparator;
        do {
            Node<K, V> n = findNear(key, rel, cmp);
            if (n == null) {
                return null;
            }
            e = n.createSnapshot();
        } while (e == null);
        return e;
    }

    public ConcurrentSkipListMap() {
        this.comparator = null;
        initialize();
    }

    public ConcurrentSkipListMap(Comparator<? super K> comparator) {
        this.comparator = comparator;
        initialize();
    }

    public ConcurrentSkipListMap(Map<? extends K, ? extends V> m) {
        this.comparator = null;
        initialize();
        putAll(m);
    }

    public ConcurrentSkipListMap(SortedMap<K, ? extends V> m) {
        this.comparator = m.comparator();
        initialize();
        buildFromSorted(m);
    }

    public ConcurrentSkipListMap<K, V> clone() {
        try {
            ConcurrentSkipListMap<K, V> clone = (ConcurrentSkipListMap) super.clone();
            clone.initialize();
            clone.buildFromSorted(this);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    private void buildFromSorted(SortedMap<K, ? extends V> map) {
        if (map == null) {
            throw new NullPointerException();
        }
        int i;
        HeadIndex<K, V> h = this.head;
        Node<K, V> basepred = h.node;
        ArrayList<Index<K, V>> preds = new ArrayList();
        for (i = 0; i <= h.level; i++) {
            preds.add(null);
        }
        Index<K, V> q = h;
        for (i = h.level; i > 0; i--) {
            preds.set(i, q);
            q = q.down;
        }
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            int rnd = ThreadLocalRandom.current().nextInt();
            int j = 0;
            if ((-2147483647 & rnd) == 0) {
                do {
                    j++;
                    rnd >>>= 1;
                } while ((rnd & 1) != 0);
                if (j > h.level) {
                    j = h.level + 1;
                }
            }
            K k = e.getKey();
            V v = e.getValue();
            if (k == null || v == null) {
                throw new NullPointerException();
            }
            Node<K, V> z = new Node(k, v, null);
            basepred.next = z;
            basepred = z;
            if (j > 0) {
                i = 1;
                Index<K, V> idx = null;
                HeadIndex<K, V> h2 = h;
                while (i <= j) {
                    Index<K, V> idx2 = new Index(z, idx, null);
                    if (i > h2.level) {
                        h = new HeadIndex(h2.node, h2, idx2, i);
                    } else {
                        h = h2;
                    }
                    if (i < preds.size()) {
                        ((Index) preds.get(i)).right = idx2;
                        preds.set(i, idx2);
                    } else {
                        preds.add(idx2);
                    }
                    i++;
                    idx = idx2;
                    h2 = h;
                }
                h = h2;
            }
        }
        this.head = h;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        for (Node<K, V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null) {
                s.writeObject(n.key);
                s.writeObject(v);
            }
        }
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        int i;
        s.defaultReadObject();
        initialize();
        HeadIndex<K, V> h = this.head;
        Node<K, V> basepred = h.node;
        ArrayList<Index<K, V>> preds = new ArrayList();
        for (i = 0; i <= h.level; i++) {
            preds.add(null);
        }
        Index<K, V> q = h;
        for (i = h.level; i > 0; i--) {
            preds.set(i, q);
            q = q.down;
        }
        while (true) {
            Object k = s.readObject();
            if (k == null) {
                this.head = h;
                return;
            }
            Object v = s.readObject();
            if (v == null) {
                throw new NullPointerException();
            }
            Object obj = k;
            Object obj2 = v;
            int rnd = ThreadLocalRandom.current().nextInt();
            int j = 0;
            if ((-2147483647 & rnd) == 0) {
                do {
                    j++;
                    rnd >>>= 1;
                } while ((rnd & 1) != 0);
                if (j > h.level) {
                    j = h.level + 1;
                }
            }
            Node<K, V> z = new Node(k, v, null);
            basepred.next = z;
            basepred = z;
            if (j > 0) {
                i = 1;
                Index<K, V> idx = null;
                HeadIndex<K, V> h2 = h;
                while (i <= j) {
                    Index<K, V> idx2 = new Index(z, idx, null);
                    if (i > h2.level) {
                        h = new HeadIndex(h2.node, h2, idx2, i);
                    } else {
                        h = h2;
                    }
                    if (i < preds.size()) {
                        ((Index) preds.get(i)).right = idx2;
                        preds.set(i, idx2);
                    } else {
                        preds.add(idx2);
                    }
                    i++;
                    idx = idx2;
                    h2 = h;
                }
                h = h2;
            }
        }
    }

    public boolean containsKey(Object key) {
        return doGet(key) != null;
    }

    public V get(Object key) {
        return doGet(key);
    }

    public V getOrDefault(Object key, V defaultValue) {
        V v = doGet(key);
        return v == null ? defaultValue : v;
    }

    public V put(K key, V value) {
        if (value != null) {
            return doPut(key, value, false);
        }
        throw new NullPointerException();
    }

    public V remove(Object key) {
        return doRemove(key, null);
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        for (Node<K, V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null && value.lambda$-java_util_function_Predicate_4628(v)) {
                return true;
            }
        }
        return false;
    }

    public int size() {
        long count = HEAD;
        for (Node<K, V> n = findFirst(); n != null; n = n.next) {
            if (n.getValidValue() != null) {
                count++;
            }
        }
        return count >= 2147483647L ? Integer.MAX_VALUE : (int) count;
    }

    public boolean isEmpty() {
        return findFirst() == null;
    }

    public void clear() {
        initialize();
    }

    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null) {
            throw new NullPointerException();
        }
        V v = doGet(key);
        if (v != null) {
            return v;
        }
        V r = mappingFunction.apply(key);
        if (r == null) {
            return v;
        }
        V p = doPut(key, r, true);
        return p == null ? r : p;
    }

    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null) {
            throw new NullPointerException();
        }
        while (true) {
            Node<K, V> n = findNode(key);
            if (n == null) {
                break;
            }
            V v = n.value;
            if (v != null) {
                V vv = v;
                V r = remappingFunction.apply(key, v);
                if (r != null) {
                    if (n.casValue(v, r)) {
                        return r;
                    }
                } else if (doRemove(key, v) != null) {
                    break;
                }
            }
        }
        return null;
    }

    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null) {
            throw new NullPointerException();
        }
        while (true) {
            Node<K, V> n = findNode(key);
            V r;
            if (n == null) {
                r = remappingFunction.apply(key, null);
                if (r == null) {
                    break;
                } else if (doPut(key, r, true) == null) {
                    return r;
                }
            } else {
                V v = n.value;
                if (v != null) {
                    V vv = v;
                    r = remappingFunction.apply(key, v);
                    if (r != null) {
                        if (n.casValue(v, r)) {
                            return r;
                        }
                    } else if (doRemove(key, v) != null) {
                        break;
                    }
                } else {
                    continue;
                }
            }
        }
        return null;
    }

    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (key == null || value == null || remappingFunction == null) {
            throw new NullPointerException();
        }
        while (true) {
            Node<K, V> n = findNode(key);
            if (n != null) {
                V v = n.value;
                if (v != null) {
                    V vv = v;
                    V r = remappingFunction.apply(v, value);
                    if (r != null) {
                        if (n.casValue(v, r)) {
                            return r;
                        }
                    } else if (doRemove(key, v) != null) {
                        return null;
                    }
                } else {
                    continue;
                }
            } else if (doPut(key, value, true) == null) {
                return value;
            }
        }
    }

    public NavigableSet<K> keySet() {
        KeySet<K, V> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        ks = new KeySet(this);
        this.keySet = ks;
        return ks;
    }

    public NavigableSet<K> navigableKeySet() {
        KeySet<K, V> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        ks = new KeySet(this);
        this.keySet = ks;
        return ks;
    }

    public Collection<V> values() {
        Values<K, V> vs = this.values;
        if (vs != null) {
            return vs;
        }
        vs = new Values(this);
        this.values = vs;
        return vs;
    }

    public Set<Entry<K, V>> entrySet() {
        EntrySet<K, V> es = this.entrySet;
        if (es != null) {
            return es;
        }
        es = new EntrySet(this);
        this.entrySet = es;
        return es;
    }

    public ConcurrentNavigableMap<K, V> descendingMap() {
        ConcurrentNavigableMap<K, V> dm = this.descendingMap;
        if (dm != null) {
            return dm;
        }
        ConcurrentNavigableMap<K, V> subMap = new SubMap(this, null, false, null, false, true);
        this.descendingMap = subMap;
        return subMap;
    }

    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Map)) {
            return false;
        }
        Map<?, ?> m = (Map) o;
        try {
            for (Entry<K, V> e : entrySet()) {
                if (!e.getValue().lambda$-java_util_function_Predicate_4628(m.get(e.getKey()))) {
                    return false;
                }
            }
            for (Entry<?, ?> e2 : m.entrySet()) {
                Object k = e2.getKey();
                Object v = e2.getValue();
                if (k == null || v == null) {
                    return false;
                }
                if ((v.lambda$-java_util_function_Predicate_4628(get(k)) ^ 1) != 0) {
                    return false;
                }
            }
            return true;
        } catch (ClassCastException e3) {
            return false;
        } catch (NullPointerException e4) {
            return false;
        }
    }

    public V putIfAbsent(K key, V value) {
        if (value != null) {
            return doPut(key, value, true);
        }
        throw new NullPointerException();
    }

    public boolean remove(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException();
        } else if (value == null || doRemove(key, value) == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean replace(K key, V oldValue, V newValue) {
        if (key == null || oldValue == null || newValue == null) {
            throw new NullPointerException();
        }
        while (true) {
            Node<K, V> n = findNode(key);
            if (n == null) {
                return false;
            }
            Object v = n.value;
            if (v != null) {
                if (!oldValue.lambda$-java_util_function_Predicate_4628(v)) {
                    return false;
                }
                if (n.casValue(v, newValue)) {
                    return true;
                }
            }
        }
    }

    public V replace(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        while (true) {
            Node<K, V> n = findNode(key);
            if (n == null) {
                return null;
            }
            V v = n.value;
            if (v != null && n.casValue(v, value)) {
                V vv = v;
                return v;
            }
        }
    }

    public Comparator<? super K> comparator() {
        return this.comparator;
    }

    public K firstKey() {
        Node<K, V> n = findFirst();
        if (n != null) {
            return n.key;
        }
        throw new NoSuchElementException();
    }

    public K lastKey() {
        Node<K, V> n = findLast();
        if (n != null) {
            return n.key;
        }
        throw new NoSuchElementException();
    }

    public ConcurrentNavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        if (fromKey != null && toKey != null) {
            return new SubMap(this, fromKey, fromInclusive, toKey, toInclusive, false);
        }
        throw new NullPointerException();
    }

    public ConcurrentNavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        if (toKey != null) {
            return new SubMap(this, null, false, toKey, inclusive, false);
        }
        throw new NullPointerException();
    }

    public ConcurrentNavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        if (fromKey != null) {
            return new SubMap(this, fromKey, inclusive, null, false, false);
        }
        throw new NullPointerException();
    }

    public ConcurrentNavigableMap<K, V> subMap(K fromKey, K toKey) {
        return subMap((Object) fromKey, true, (Object) toKey, false);
    }

    public ConcurrentNavigableMap<K, V> headMap(K toKey) {
        return headMap((Object) toKey, false);
    }

    public ConcurrentNavigableMap<K, V> tailMap(K fromKey) {
        return tailMap((Object) fromKey, true);
    }

    public Entry<K, V> lowerEntry(K key) {
        return getNear(key, 2);
    }

    public K lowerKey(K key) {
        Node<K, V> n = findNear(key, 2, this.comparator);
        if (n == null) {
            return null;
        }
        return n.key;
    }

    public Entry<K, V> floorEntry(K key) {
        return getNear(key, 3);
    }

    public K floorKey(K key) {
        Node<K, V> n = findNear(key, 3, this.comparator);
        if (n == null) {
            return null;
        }
        return n.key;
    }

    public Entry<K, V> ceilingEntry(K key) {
        return getNear(key, 1);
    }

    public K ceilingKey(K key) {
        Node<K, V> n = findNear(key, 1, this.comparator);
        if (n == null) {
            return null;
        }
        return n.key;
    }

    public Entry<K, V> higherEntry(K key) {
        return getNear(key, 0);
    }

    public K higherKey(K key) {
        Node<K, V> n = findNear(key, 0, this.comparator);
        if (n == null) {
            return null;
        }
        return n.key;
    }

    public Entry<K, V> firstEntry() {
        SimpleImmutableEntry<K, V> e;
        do {
            Node<K, V> n = findFirst();
            if (n == null) {
                return null;
            }
            e = n.createSnapshot();
        } while (e == null);
        return e;
    }

    public Entry<K, V> lastEntry() {
        SimpleImmutableEntry<K, V> e;
        do {
            Node<K, V> n = findLast();
            if (n == null) {
                return null;
            }
            e = n.createSnapshot();
        } while (e == null);
        return e;
    }

    public Entry<K, V> pollFirstEntry() {
        return doRemoveFirstEntry();
    }

    public Entry<K, V> pollLastEntry() {
        return doRemoveLastEntry();
    }

    static final <E> List<E> toList(Collection<E> c) {
        ArrayList<E> list = new ArrayList();
        for (E e : c) {
            list.add(e);
        }
        return list;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        for (Node<K, V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null) {
                action.accept(n.key, v);
            }
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        for (Node<K, V> n = findFirst(); n != null; n = n.next) {
            V v;
            V r;
            do {
                v = n.getValidValue();
                if (v == null) {
                    break;
                }
                r = function.apply(n.key, v);
                if (r == null) {
                    throw new NullPointerException();
                }
            } while (!n.casValue(v, r));
        }
    }

    boolean removeEntryIf(Predicate<? super Entry<K, V>> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        boolean removed = false;
        for (Node<K, V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null) {
                K k = n.key;
                if (function.test(new SimpleImmutableEntry(k, v)) && remove(k, v)) {
                    removed = true;
                }
            }
        }
        return removed;
    }

    boolean removeValueIf(Predicate<? super V> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        boolean removed = false;
        for (Node<K, V> n = findFirst(); n != null; n = n.next) {
            V v = n.getValidValue();
            if (v != null) {
                K k = n.key;
                if (function.test(v) && remove(k, v)) {
                    removed = true;
                }
            }
        }
        return removed;
    }

    final KeySpliterator<K, V> keySpliterator() {
        HeadIndex<K, V> h;
        Node<K, V> p;
        Comparator<? super K> cmp = this.comparator;
        while (true) {
            h = this.head;
            Node<K, V> b = h.node;
            p = b.next;
            if (p != null && p.value == null) {
                p.helpDelete(b, p.next);
            }
        }
        return new KeySpliterator(cmp, h, p, null, p == null ? 0 : Integer.MAX_VALUE);
    }

    final ValueSpliterator<K, V> valueSpliterator() {
        HeadIndex<K, V> h;
        Node<K, V> p;
        Comparator<? super K> cmp = this.comparator;
        while (true) {
            h = this.head;
            Node<K, V> b = h.node;
            p = b.next;
            if (p != null && p.value == null) {
                p.helpDelete(b, p.next);
            }
        }
        return new ValueSpliterator(cmp, h, p, null, p == null ? 0 : Integer.MAX_VALUE);
    }

    final EntrySpliterator<K, V> entrySpliterator() {
        HeadIndex<K, V> h;
        Node<K, V> p;
        Comparator<? super K> cmp = this.comparator;
        while (true) {
            h = this.head;
            Node<K, V> b = h.node;
            p = b.next;
            if (p != null && p.value == null) {
                p.helpDelete(b, p.next);
            }
        }
        return new EntrySpliterator(cmp, h, p, null, p == null ? 0 : Integer.MAX_VALUE);
    }
}
