package java.util.concurrent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
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

        CSLMSpliterator(Comparator<? super K> comparator2, Index<K, V> row2, Node<K, V> origin, K fence2, int est2) {
            this.comparator = comparator2;
            this.row = row2;
            this.current = origin;
            this.fence = fence2;
            this.est = est2;
        }

        public final long estimateSize() {
            return (long) this.est;
        }
    }

    final class EntryIterator extends ConcurrentSkipListMap<K, V>.Iter<Map.Entry<K, V>> {
        EntryIterator() {
            super();
        }

        public Map.Entry<K, V> next() {
            Node<K, V> n = this.next;
            V v = this.nextValue;
            advance();
            return new AbstractMap.SimpleImmutableEntry(n.key, v);
        }
    }

    static final class EntrySet<K, V> extends AbstractSet<Map.Entry<K, V>> {
        final ConcurrentNavigableMap<K, V> m;

        EntrySet(ConcurrentNavigableMap<K, V> map) {
            this.m = map;
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            if (this.m instanceof ConcurrentSkipListMap) {
                ConcurrentSkipListMap concurrentSkipListMap = (ConcurrentSkipListMap) this.m;
                Objects.requireNonNull(concurrentSkipListMap);
                return new EntryIterator();
            }
            SubMap subMap = (SubMap) this.m;
            Objects.requireNonNull(subMap);
            return new SubMap.SubMapEntryIterator();
        }

        public boolean contains(Object o) {
            boolean z = false;
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry) o;
            V v = this.m.get(e.getKey());
            if (v != null && v.equals(e.getValue())) {
                z = true;
            }
            return z;
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry) o;
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
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof Set)) {
                return false;
            }
            Collection<?> c = (Collection) o;
            try {
                if (!containsAll(c) || !c.containsAll(this)) {
                    z = false;
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

        public Spliterator<Map.Entry<K, V>> spliterator() {
            if (this.m instanceof ConcurrentSkipListMap) {
                return ((ConcurrentSkipListMap) this.m).entrySpliterator();
            }
            SubMap subMap = (SubMap) this.m;
            Objects.requireNonNull(subMap);
            return new SubMap.SubMapEntryIterator();
        }

        public boolean removeIf(Predicate<? super Map.Entry<K, V>> filter) {
            if (filter == null) {
                throw new NullPointerException();
            } else if (this.m instanceof ConcurrentSkipListMap) {
                return ((ConcurrentSkipListMap) this.m).removeEntryIf(filter);
            } else {
                SubMap subMap = (SubMap) this.m;
                Objects.requireNonNull(subMap);
                Iterator<Map.Entry<K, V>> it = new SubMap.SubMapEntryIterator();
                boolean removed = false;
                while (it.hasNext()) {
                    Map.Entry<K, V> e = it.next();
                    if (filter.test(e) && this.m.remove(e.getKey(), e.getValue())) {
                        removed = true;
                    }
                }
                return removed;
            }
        }
    }

    static final class EntrySpliterator<K, V> extends CSLMSpliterator<K, V> implements Spliterator<Map.Entry<K, V>> {
        EntrySpliterator(Comparator<? super K> comparator, Index<K, V> row, Node<K, V> origin, K fence, int est) {
            super(comparator, row, origin, fence, est);
        }

        public EntrySpliterator<K, V> trySplit() {
            Index<K, V> q;
            Index<K, V> s;
            Node<K, V> n;
            K sk;
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            Node<K, V> node = this.current;
            Node<K, V> e = node;
            if (node != null) {
                K k = e.key;
                K ek = k;
                if (k != null) {
                    Index<K, V> q2 = this.row;
                    while (true) {
                        q = q2;
                        if (q == null) {
                            break;
                        }
                        Index<K, V> index = q.right;
                        s = index;
                        if (index != null) {
                            Node<K, V> node2 = s.node;
                            Node<K, V> b = node2;
                            if (node2 != null) {
                                Node<K, V> node3 = b.next;
                                n = node3;
                                if (!(node3 == null || n.value == null)) {
                                    K k2 = n.key;
                                    sk = k2;
                                    if (k2 != null && ConcurrentSkipListMap.cpr(cmp, sk, ek) > 0 && (f == null || ConcurrentSkipListMap.cpr(cmp, sk, f) < 0)) {
                                        this.current = n;
                                        Index<K, V> r = q.down;
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                        q2 = q.down;
                        this.row = q2;
                    }
                    this.current = n;
                    Index<K, V> r2 = q.down;
                    this.row = s.right != null ? s : s.down;
                    this.est -= this.est >>> 2;
                    Index<K, V> index2 = r2;
                    EntrySpliterator entrySpliterator = new EntrySpliterator(cmp, r2, e, sk, this.est);
                    return entrySpliterator;
                }
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            if (action != null) {
                Comparator<? super K> cmp = this.comparator;
                K f = this.fence;
                V e = this.current;
                this.current = null;
                while (e != null) {
                    K k = e.key;
                    K k2 = k;
                    if (k == null || f == null || ConcurrentSkipListMap.cpr(cmp, f, k2) > 0) {
                        V v = e.value;
                        V v2 = v;
                        if (!(v == null || v2 == e)) {
                            action.accept(new AbstractMap.SimpleImmutableEntry(k2, v2));
                        }
                        e = e.next;
                    } else {
                        return;
                    }
                }
                return;
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            if (action != null) {
                Comparator<? super K> cmp = this.comparator;
                K f = this.fence;
                Node<K, V> e = this.current;
                while (true) {
                    if (e == null) {
                        break;
                    }
                    K k = e.key;
                    K k2 = k;
                    if (k != null && f != null && ConcurrentSkipListMap.cpr(cmp, f, k2) <= 0) {
                        e = null;
                        break;
                    }
                    V v = e.value;
                    Node<K, V> v2 = v;
                    if (v == null || v2 == e) {
                        e = e.next;
                    } else {
                        this.current = e.next;
                        action.accept(new AbstractMap.SimpleImmutableEntry(k2, v2));
                        return true;
                    }
                }
                this.current = e;
                return false;
            }
            throw new NullPointerException();
        }

        public int characteristics() {
            return 4373;
        }

        public final Comparator<Map.Entry<K, V>> getComparator() {
            if (this.comparator != null) {
                return Map.Entry.comparingByKey(this.comparator);
            }
            return $$Lambda$ConcurrentSkipListMap$EntrySpliterator$y0KdhWWpZC4eKUM6bCtPBgl2u2o.INSTANCE;
        }
    }

    static final class HeadIndex<K, V> extends Index<K, V> {
        final int level;

        HeadIndex(Node<K, V> node, Index<K, V> down, Index<K, V> right, int level2) {
            super(node, down, right);
            this.level = level2;
        }
    }

    static class Index<K, V> {
        private static final long RIGHT;
        private static final Unsafe U = Unsafe.getUnsafe();
        final Index<K, V> down;
        final Node<K, V> node;
        volatile Index<K, V> right;

        Index(Node<K, V> node2, Index<K, V> down2, Index<K, V> right2) {
            this.node = node2;
            this.down = down2;
            this.right = right2;
        }

        /* access modifiers changed from: package-private */
        public final boolean casRight(Index<K, V> cmp, Index<K, V> val) {
            return U.compareAndSwapObject(this, RIGHT, cmp, val);
        }

        /* access modifiers changed from: package-private */
        public final boolean indexesDeletedNode() {
            return this.node.value == null;
        }

        /* access modifiers changed from: package-private */
        public final boolean link(Index<K, V> succ, Index<K, V> newSucc) {
            Node<K, V> n = this.node;
            newSucc.right = succ;
            return n.value != null && casRight(succ, newSucc);
        }

        /* access modifiers changed from: package-private */
        public final boolean unlink(Index<K, V> succ) {
            return this.node.value != null && casRight(succ, succ.right);
        }

        static {
            try {
                RIGHT = U.objectFieldOffset(Index.class.getDeclaredField("right"));
            } catch (ReflectiveOperationException e) {
                throw new Error((Throwable) e);
            }
        }
    }

    abstract class Iter<T> implements Iterator<T> {
        Node<K, V> lastReturned;
        Node<K, V> next;
        V nextValue;

        Iter() {
            while (true) {
                Node<K, V> findFirst = ConcurrentSkipListMap.this.findFirst();
                this.next = findFirst;
                if (findFirst != null) {
                    V x = this.next.value;
                    if (x != null && x != this.next) {
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

        /* access modifiers changed from: package-private */
        public final void advance() {
            if (this.next != null) {
                this.lastReturned = this.next;
                while (true) {
                    Node<K, V> node = this.next.next;
                    this.next = node;
                    if (node != null) {
                        V x = this.next.value;
                        if (x != null && x != this.next) {
                            this.nextValue = x;
                            return;
                        }
                    } else {
                        return;
                    }
                }
            } else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            Node<K, V> l = this.lastReturned;
            if (l != null) {
                ConcurrentSkipListMap.this.remove(l.key);
                this.lastReturned = null;
                return;
            }
            throw new IllegalStateException();
        }
    }

    final class KeyIterator extends ConcurrentSkipListMap<K, V>.Iter<K> {
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
            Map.Entry<K, V> e = this.m.pollFirstEntry();
            if (e == null) {
                return null;
            }
            return e.getKey();
        }

        public K pollLast() {
            Map.Entry<K, V> e = this.m.pollLastEntry();
            if (e == null) {
                return null;
            }
            return e.getKey();
        }

        public Iterator<K> iterator() {
            if (this.m instanceof ConcurrentSkipListMap) {
                ConcurrentSkipListMap concurrentSkipListMap = (ConcurrentSkipListMap) this.m;
                Objects.requireNonNull(concurrentSkipListMap);
                return new KeyIterator();
            }
            SubMap subMap = (SubMap) this.m;
            Objects.requireNonNull(subMap);
            return new SubMap.SubMapKeyIterator();
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (o == this) {
                return true;
            }
            if (!(o instanceof Set)) {
                return false;
            }
            Collection<?> c = (Collection) o;
            try {
                if (!containsAll(c) || !c.containsAll(this)) {
                    z = false;
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
            Objects.requireNonNull(subMap);
            return new SubMap.SubMapKeyIterator();
        }
    }

    static final class KeySpliterator<K, V> extends CSLMSpliterator<K, V> implements Spliterator<K> {
        KeySpliterator(Comparator<? super K> comparator, Index<K, V> row, Node<K, V> origin, K fence, int est) {
            super(comparator, row, origin, fence, est);
        }

        public KeySpliterator<K, V> trySplit() {
            Index<K, V> q;
            Index<K, V> s;
            Node<K, V> n;
            K sk;
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            Node<K, V> node = this.current;
            Node<K, V> e = node;
            if (node != null) {
                K k = e.key;
                K ek = k;
                if (k != null) {
                    Index<K, V> q2 = this.row;
                    while (true) {
                        q = q2;
                        if (q == null) {
                            break;
                        }
                        Index<K, V> index = q.right;
                        s = index;
                        if (index != null) {
                            Node<K, V> node2 = s.node;
                            Node<K, V> b = node2;
                            if (node2 != null) {
                                Node<K, V> node3 = b.next;
                                n = node3;
                                if (!(node3 == null || n.value == null)) {
                                    K k2 = n.key;
                                    sk = k2;
                                    if (k2 != null && ConcurrentSkipListMap.cpr(cmp, sk, ek) > 0 && (f == null || ConcurrentSkipListMap.cpr(cmp, sk, f) < 0)) {
                                        this.current = n;
                                        Index<K, V> r = q.down;
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                        q2 = q.down;
                        this.row = q2;
                    }
                    this.current = n;
                    Index<K, V> r2 = q.down;
                    this.row = s.right != null ? s : s.down;
                    this.est -= this.est >>> 2;
                    Index<K, V> index2 = r2;
                    KeySpliterator keySpliterator = new KeySpliterator(cmp, r2, e, sk, this.est);
                    return keySpliterator;
                }
            }
            return null;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: java.util.concurrent.ConcurrentSkipListMap$Node<K, V>} */
        /* JADX WARNING: Multi-variable type inference failed */
        public void forEachRemaining(Consumer<? super K> action) {
            if (action != null) {
                Comparator<? super K> cmp = this.comparator;
                K f = this.fence;
                Node<K, V> e = this.current;
                this.current = null;
                while (e != null) {
                    K k = e.key;
                    K k2 = k;
                    if (k == null || f == null || ConcurrentSkipListMap.cpr(cmp, f, k2) > 0) {
                        Object obj = e.value;
                        Object v = obj;
                        if (!(obj == 0 || v == e)) {
                            action.accept(k2);
                        }
                        e = e.next;
                    } else {
                        return;
                    }
                }
                return;
            }
            throw new NullPointerException();
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: java.util.concurrent.ConcurrentSkipListMap$Node<K, V>} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean tryAdvance(Consumer<? super K> action) {
            if (action != null) {
                Comparator<? super K> cmp = this.comparator;
                K f = this.fence;
                Node<K, V> e = this.current;
                while (true) {
                    if (e == null) {
                        break;
                    }
                    K k = e.key;
                    K k2 = k;
                    if (k != null && f != null && ConcurrentSkipListMap.cpr(cmp, f, k2) <= 0) {
                        e = null;
                        break;
                    }
                    Object obj = e.value;
                    Object v = obj;
                    if (obj == 0 || v == e) {
                        e = e.next;
                    } else {
                        this.current = e.next;
                        action.accept(k2);
                        return true;
                    }
                }
                this.current = e;
                return false;
            }
            throw new NullPointerException();
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

        Node(K key2, Object value2, Node<K, V> next2) {
            this.key = key2;
            this.value = value2;
            this.next = next2;
        }

        Node(Node<K, V> next2) {
            this.key = null;
            this.value = this;
            this.next = next2;
        }

        /* access modifiers changed from: package-private */
        public boolean casValue(Object cmp, Object val) {
            return U.compareAndSwapObject(this, VALUE, cmp, val);
        }

        /* access modifiers changed from: package-private */
        public boolean casNext(Node<K, V> cmp, Node<K, V> val) {
            return U.compareAndSwapObject(this, NEXT, cmp, val);
        }

        /* access modifiers changed from: package-private */
        public boolean isMarker() {
            return this.value == this;
        }

        /* access modifiers changed from: package-private */
        public boolean isBaseHeader() {
            return this.value == ConcurrentSkipListMap.BASE_HEADER;
        }

        /* access modifiers changed from: package-private */
        public boolean appendMarker(Node<K, V> f) {
            return casNext(f, new Node(f));
        }

        /* access modifiers changed from: package-private */
        public void helpDelete(Node<K, V> b, Node<K, V> f) {
            if (f != this.next || this != b.next) {
                return;
            }
            if (f == null || f.value != f) {
                casNext(f, new Node(f));
            } else {
                b.casNext(this, f.next);
            }
        }

        /* access modifiers changed from: package-private */
        public V getValidValue() {
            V v = this.value;
            if (v == this || v == ConcurrentSkipListMap.BASE_HEADER) {
                return null;
            }
            return v;
        }

        /* access modifiers changed from: package-private */
        public AbstractMap.SimpleImmutableEntry<K, V> createSnapshot() {
            V v = this.value;
            if (v == null || v == this || v == ConcurrentSkipListMap.BASE_HEADER) {
                return null;
            }
            return new AbstractMap.SimpleImmutableEntry<>(this.key, v);
        }

        static {
            try {
                VALUE = U.objectFieldOffset(Node.class.getDeclaredField("value"));
                NEXT = U.objectFieldOffset(Node.class.getDeclaredField("next"));
            } catch (ReflectiveOperationException e) {
                throw new Error((Throwable) e);
            }
        }
    }

    static final class SubMap<K, V> extends AbstractMap<K, V> implements ConcurrentNavigableMap<K, V>, Cloneable, Serializable {
        private static final long serialVersionUID = -7647078645895051609L;
        private transient Set<Map.Entry<K, V>> entrySetView;
        private final K hi;
        private final boolean hiInclusive;
        final boolean isDescending;
        private transient KeySet<K, V> keySetView;
        private final K lo;
        private final boolean loInclusive;
        final ConcurrentSkipListMap<K, V> m;
        private transient Collection<V> valuesView;

        final class SubMapEntryIterator extends SubMap<K, V>.SubMapIter<Map.Entry<K, V>> {
            SubMapEntryIterator() {
                super();
            }

            public Map.Entry<K, V> next() {
                Node<K, V> n = this.next;
                V v = this.nextValue;
                advance();
                return new AbstractMap.SimpleImmutableEntry(n.key, v);
            }

            public int characteristics() {
                return 1;
            }
        }

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
                            if (!SubMap.this.inBounds(this.next.key, cmp)) {
                                this.next = null;
                                return;
                            } else {
                                this.nextValue = x;
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                }
            }

            public final boolean hasNext() {
                return this.next != null;
            }

            /* access modifiers changed from: package-private */
            public final void advance() {
                if (this.next != null) {
                    this.lastReturned = this.next;
                    if (SubMap.this.isDescending) {
                        descend();
                    } else {
                        ascend();
                    }
                } else {
                    throw new NoSuchElementException();
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
                            } else {
                                this.nextValue = x;
                                return;
                            }
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
                            } else {
                                this.nextValue = x;
                                return;
                            }
                        }
                    } else {
                        return;
                    }
                }
            }

            public void remove() {
                Node<K, V> l = this.lastReturned;
                if (l != null) {
                    SubMap.this.m.remove(l.key);
                    this.lastReturned = null;
                    return;
                }
                throw new IllegalStateException();
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

        final class SubMapKeyIterator extends SubMap<K, V>.SubMapIter<K> {
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

        final class SubMapValueIterator extends SubMap<K, V>.SubMapIter<V> {
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

        SubMap(ConcurrentSkipListMap<K, V> map, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive, boolean isDescending2) {
            Comparator<? super K> cmp = map.comparator;
            if (fromKey == null || toKey == null || ConcurrentSkipListMap.cpr(cmp, fromKey, toKey) <= 0) {
                this.m = map;
                this.lo = fromKey;
                this.hi = toKey;
                this.loInclusive = fromInclusive;
                this.hiInclusive = toInclusive;
                this.isDescending = isDescending2;
                return;
            }
            throw new IllegalArgumentException("inconsistent range");
        }

        /* access modifiers changed from: package-private */
        public boolean tooLow(Object key, Comparator<? super K> cmp) {
            if (this.lo != null) {
                int cpr = ConcurrentSkipListMap.cpr(cmp, key, this.lo);
                int c = cpr;
                if (cpr < 0 || (c == 0 && !this.loInclusive)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean tooHigh(Object key, Comparator<? super K> cmp) {
            if (this.hi != null) {
                int cpr = ConcurrentSkipListMap.cpr(cmp, key, this.hi);
                int c = cpr;
                if (cpr > 0 || (c == 0 && !this.hiInclusive)) {
                    return true;
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public boolean inBounds(Object key, Comparator<? super K> cmp) {
            return !tooLow(key, cmp) && !tooHigh(key, cmp);
        }

        /* access modifiers changed from: package-private */
        public void checkKeyBounds(K key, Comparator<? super K> cmp) {
            if (key == null) {
                throw new NullPointerException();
            } else if (!inBounds(key, cmp)) {
                throw new IllegalArgumentException("key out of range");
            }
        }

        /* access modifiers changed from: package-private */
        public boolean isBeforeEnd(Node<K, V> n, Comparator<? super K> cmp) {
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
            if (c > 0 || (c == 0 && !this.hiInclusive)) {
                return false;
            }
            return true;
        }

        /* access modifiers changed from: package-private */
        public Node<K, V> loNode(Comparator<? super K> cmp) {
            if (this.lo == null) {
                return this.m.findFirst();
            }
            if (this.loInclusive) {
                return this.m.findNear(this.lo, 1, cmp);
            }
            return this.m.findNear(this.lo, 0, cmp);
        }

        /* access modifiers changed from: package-private */
        public Node<K, V> hiNode(Comparator<? super K> cmp) {
            if (this.hi == null) {
                return this.m.findLast();
            }
            if (this.hiInclusive) {
                return this.m.findNear(this.hi, 3, cmp);
            }
            return this.m.findNear(this.hi, 2, cmp);
        }

        /* access modifiers changed from: package-private */
        public K lowestKey() {
            Comparator<? super K> cmp = this.m.comparator;
            Node<K, V> n = loNode(cmp);
            if (isBeforeEnd(n, cmp)) {
                return n.key;
            }
            throw new NoSuchElementException();
        }

        /* access modifiers changed from: package-private */
        public K highestKey() {
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

        /* access modifiers changed from: package-private */
        public Map.Entry<K, V> lowestEntry() {
            Map.Entry<K, V> e;
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

        /* access modifiers changed from: package-private */
        public Map.Entry<K, V> highestEntry() {
            Map.Entry<K, V> e;
            Comparator<? super K> cmp = this.m.comparator;
            do {
                Node<K, V> n = hiNode(cmp);
                if (n == null || !inBounds(n.key, cmp)) {
                    return null;
                }
                e = n.createSnapshot();
            } while (e == null);
            return e;
        }

        /* access modifiers changed from: package-private */
        public Map.Entry<K, V> removeLowest() {
            K k;
            V v;
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
            return new AbstractMap.SimpleImmutableEntry(k, v);
        }

        /* access modifiers changed from: package-private */
        public Map.Entry<K, V> removeHighest() {
            K k;
            V v;
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
            return new AbstractMap.SimpleImmutableEntry(k, v);
        }

        /* access modifiers changed from: package-private */
        public Map.Entry<K, V> getNearEntry(K key, int rel) {
            K k;
            V v;
            Comparator<? super K> cmp = this.m.comparator;
            if (this.isDescending) {
                if ((rel & 2) == 0) {
                    rel |= 2;
                } else {
                    rel &= -3;
                }
            }
            Map.Entry<K, V> entry = null;
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
                do {
                    Node<K, V> n = this.m.findNear(key, rel, cmp);
                    if (n == null || !inBounds(n.key, cmp)) {
                        return null;
                    }
                    k = n.key;
                    v = n.getValidValue();
                } while (v == null);
                return new AbstractMap.SimpleImmutableEntry(k, v);
            }
        }

        /* access modifiers changed from: package-private */
        public K getNearKey(K key, int rel) {
            Node<K, V> n;
            K k;
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
                    Node<K, V> n2 = loNode(cmp);
                    if (isBeforeEnd(n2, cmp)) {
                        return n2.key;
                    }
                }
                return null;
            } else if (tooHigh(key, cmp)) {
                if ((rel & 2) != 0) {
                    Node<K, V> n3 = hiNode(cmp);
                    if (n3 != null) {
                        K last = n3.key;
                        if (inBounds(last, cmp)) {
                            return last;
                        }
                    }
                }
                return null;
            } else {
                do {
                    n = this.m.findNear(key, rel, cmp);
                    if (n == null || !inBounds(n.key, cmp)) {
                        return null;
                    }
                    k = n.key;
                } while (n.getValidValue() == null);
                return k;
            }
        }

        public boolean containsKey(Object key) {
            if (key != null) {
                return inBounds(key, this.m.comparator) && this.m.containsKey(key);
            }
            throw new NullPointerException();
        }

        public V get(Object key) {
            if (key == null) {
                throw new NullPointerException();
            } else if (!inBounds(key, this.m.comparator)) {
                return null;
            } else {
                return this.m.get(key);
            }
        }

        public V put(K key, V value) {
            checkKeyBounds(key, this.m.comparator);
            return this.m.put(key, value);
        }

        public V remove(Object key) {
            if (!inBounds(key, this.m.comparator)) {
                return null;
            }
            return this.m.remove(key);
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
            return !isBeforeEnd(loNode(cmp), cmp);
        }

        public boolean containsValue(Object value) {
            if (value != null) {
                Comparator<? super K> cmp = this.m.comparator;
                for (Node<K, V> n = loNode(cmp); isBeforeEnd(n, cmp); n = n.next) {
                    V v = n.getValidValue();
                    if (v != null && value.equals(v)) {
                        return true;
                    }
                }
                return false;
            }
            throw new NullPointerException();
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
            return inBounds(key, this.m.comparator) && this.m.remove(key, value);
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

        /* access modifiers changed from: package-private */
        public SubMap<K, V> newSubMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
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
                    int c = ConcurrentSkipListMap.cpr(cmp, fromKey, this.lo);
                    if (c < 0 || (c == 0 && !this.loInclusive && fromInclusive)) {
                        throw new IllegalArgumentException("key out of range");
                    }
                }
            }
            if (this.hi != null) {
                if (toKey == null) {
                    toKey = this.hi;
                    toInclusive = this.hiInclusive;
                } else {
                    int c2 = ConcurrentSkipListMap.cpr(cmp, toKey, this.hi);
                    if (c2 > 0 || (c2 == 0 && !this.hiInclusive && toInclusive)) {
                        throw new IllegalArgumentException("key out of range");
                    }
                }
            }
            SubMap subMap = new SubMap(this.m, fromKey, fromInclusive, toKey, toInclusive, this.isDescending);
            return subMap;
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
            SubMap subMap = new SubMap(this.m, this.lo, this.loInclusive, this.hi, this.hiInclusive, !this.isDescending);
            return subMap;
        }

        public Map.Entry<K, V> ceilingEntry(K key) {
            return getNearEntry(key, 1);
        }

        public K ceilingKey(K key) {
            return getNearKey(key, 1);
        }

        public Map.Entry<K, V> lowerEntry(K key) {
            return getNearEntry(key, 2);
        }

        public K lowerKey(K key) {
            return getNearKey(key, 2);
        }

        public Map.Entry<K, V> floorEntry(K key) {
            return getNearEntry(key, 3);
        }

        public K floorKey(K key) {
            return getNearKey(key, 3);
        }

        public Map.Entry<K, V> higherEntry(K key) {
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

        public Map.Entry<K, V> firstEntry() {
            return this.isDescending ? highestEntry() : lowestEntry();
        }

        public Map.Entry<K, V> lastEntry() {
            return this.isDescending ? lowestEntry() : highestEntry();
        }

        public Map.Entry<K, V> pollFirstEntry() {
            return this.isDescending ? removeHighest() : removeLowest();
        }

        public Map.Entry<K, V> pollLastEntry() {
            return this.isDescending ? removeLowest() : removeHighest();
        }

        public NavigableSet<K> keySet() {
            KeySet<K, V> ks = this.keySetView;
            if (ks != null) {
                return ks;
            }
            KeySet<K, V> keySet = new KeySet<>(this);
            this.keySetView = keySet;
            return keySet;
        }

        public NavigableSet<K> navigableKeySet() {
            KeySet<K, V> ks = this.keySetView;
            if (ks != null) {
                return ks;
            }
            KeySet<K, V> keySet = new KeySet<>(this);
            this.keySetView = keySet;
            return keySet;
        }

        public Collection<V> values() {
            Collection<V> vs = this.valuesView;
            if (vs != null) {
                return vs;
            }
            Values values = new Values(this);
            this.valuesView = values;
            return values;
        }

        public Set<Map.Entry<K, V>> entrySet() {
            Set<Map.Entry<K, V>> es = this.entrySetView;
            if (es != null) {
                return es;
            }
            EntrySet entrySet = new EntrySet(this);
            this.entrySetView = entrySet;
            return entrySet;
        }

        public NavigableSet<K> descendingKeySet() {
            return descendingMap().navigableKeySet();
        }
    }

    final class ValueIterator extends ConcurrentSkipListMap<K, V>.Iter<V> {
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
            Index<K, V> q;
            Index<K, V> s;
            Node<K, V> n;
            K sk;
            Comparator<? super K> cmp = this.comparator;
            K f = this.fence;
            Node<K, V> node = this.current;
            Node<K, V> e = node;
            if (node != null) {
                K k = e.key;
                K ek = k;
                if (k != null) {
                    Index<K, V> q2 = this.row;
                    while (true) {
                        q = q2;
                        if (q == null) {
                            break;
                        }
                        Index<K, V> index = q.right;
                        s = index;
                        if (index != null) {
                            Node<K, V> node2 = s.node;
                            Node<K, V> b = node2;
                            if (node2 != null) {
                                Node<K, V> node3 = b.next;
                                n = node3;
                                if (!(node3 == null || n.value == null)) {
                                    K k2 = n.key;
                                    sk = k2;
                                    if (k2 != null && ConcurrentSkipListMap.cpr(cmp, sk, ek) > 0 && (f == null || ConcurrentSkipListMap.cpr(cmp, sk, f) < 0)) {
                                        this.current = n;
                                        Index<K, V> r = q.down;
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                        q2 = q.down;
                        this.row = q2;
                    }
                    this.current = n;
                    Index<K, V> r2 = q.down;
                    this.row = s.right != null ? s : s.down;
                    this.est -= this.est >>> 2;
                    Index<K, V> index2 = r2;
                    ValueSpliterator valueSpliterator = new ValueSpliterator(cmp, r2, e, sk, this.est);
                    return valueSpliterator;
                }
            }
            return null;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: java.util.concurrent.ConcurrentSkipListMap$Node<K, V>} */
        /* JADX WARNING: Multi-variable type inference failed */
        public void forEachRemaining(Consumer<? super V> action) {
            if (action != null) {
                Comparator<? super K> cmp = this.comparator;
                K f = this.fence;
                V e = this.current;
                this.current = null;
                while (e != null) {
                    K k = e.key;
                    K k2 = k;
                    if (k == null || f == null || ConcurrentSkipListMap.cpr(cmp, f, k2) > 0) {
                        V v = e.value;
                        V vv = v;
                        if (!(v == null || vv == e)) {
                            action.accept(vv);
                        }
                        e = e.next;
                    } else {
                        return;
                    }
                }
                return;
            }
            throw new NullPointerException();
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v2, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: java.util.concurrent.ConcurrentSkipListMap$Node<K, V>} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean tryAdvance(Consumer<? super V> action) {
            if (action != null) {
                Comparator<? super K> cmp = this.comparator;
                K f = this.fence;
                Node<K, V> e = this.current;
                while (true) {
                    if (e == null) {
                        break;
                    }
                    K k = e.key;
                    K k2 = k;
                    if (k != null && f != null && ConcurrentSkipListMap.cpr(cmp, f, k2) <= 0) {
                        e = null;
                        break;
                    }
                    V v = e.value;
                    Node<K, V> vv = v;
                    if (v == null || vv == e) {
                        e = e.next;
                    } else {
                        this.current = e.next;
                        action.accept(vv);
                        return true;
                    }
                }
                this.current = e;
                return false;
            }
            throw new NullPointerException();
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
                Objects.requireNonNull(concurrentSkipListMap);
                return new ValueIterator();
            }
            SubMap subMap = (SubMap) this.m;
            Objects.requireNonNull(subMap);
            return new SubMap.SubMapValueIterator();
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
            Objects.requireNonNull(subMap);
            return new SubMap.SubMapValueIterator();
        }

        public boolean removeIf(Predicate<? super V> filter) {
            if (filter == null) {
                throw new NullPointerException();
            } else if (this.m instanceof ConcurrentSkipListMap) {
                return ((ConcurrentSkipListMap) this.m).removeValueIf(filter);
            } else {
                SubMap subMap = (SubMap) this.m;
                Objects.requireNonNull(subMap);
                Iterator<Map.Entry<K, V>> it = new SubMap.SubMapEntryIterator();
                boolean removed = false;
                while (it.hasNext()) {
                    Map.Entry<K, V> e = it.next();
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
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }

    private void initialize() {
        this.keySet = null;
        this.entrySet = null;
        this.values = null;
        this.descendingMap = null;
        this.head = new HeadIndex<>(new Node(null, BASE_HEADER, null), null, null, 1);
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
                Index<K, V> index = q.down;
                Index<K, V> d = index;
                if (index == null) {
                    return q.node;
                }
                q = d;
                r = d.right;
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: java.util.concurrent.ConcurrentSkipListMap$Node<K, V>} */
    /* JADX WARNING: Multi-variable type inference failed */
    private Node<K, V> findNode(Object key) {
        if (key != null) {
            Comparator<? super K> cmp = this.comparator;
            loop0:
            while (true) {
                Node<K, V> b = findPredecessor(key, cmp);
                Node<K, V> n = b.next;
                while (n != null) {
                    Node<K, V> f = n.next;
                    if (n == b.next) {
                        Object obj = n.value;
                        Object v = obj;
                        if (obj == 0) {
                            n.helpDelete(b, f);
                        } else if (!(b.value == null || v == n)) {
                            int cpr = cpr(cmp, key, n.key);
                            int c = cpr;
                            if (cpr == 0) {
                                return n;
                            }
                            if (c < 0) {
                                break loop0;
                            }
                            b = n;
                            n = f;
                        }
                    }
                }
                break loop0;
            }
            return null;
        }
        throw new NullPointerException();
    }

    private V doGet(Object key) {
        if (key != null) {
            Comparator<? super K> cmp = this.comparator;
            loop0:
            while (true) {
                Node<K, V> b = findPredecessor(key, cmp);
                Node<K, V> n = b.next;
                while (n != null) {
                    Node<K, V> f = n.next;
                    if (n == b.next) {
                        V v = n.value;
                        Node<K, V> v2 = v;
                        if (v == null) {
                            n.helpDelete(b, f);
                        } else if (!(b.value == null || v2 == n)) {
                            int cpr = cpr(cmp, key, n.key);
                            int c = cpr;
                            if (cpr == 0) {
                                return v2;
                            }
                            if (c < 0) {
                                break loop0;
                            }
                            b = n;
                            n = f;
                        }
                    }
                }
                break loop0;
            }
            return null;
        }
        throw new NullPointerException();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0041, code lost:
        r6 = new java.util.concurrent.ConcurrentSkipListMap.Node<>(r1, r2, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004a, code lost:
        if (r4.casNext(r5, r6) != false) goto L_0x004d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x004d, code lost:
        r4 = java.util.concurrent.ThreadLocalRandom.nextSecondarySeed();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0056, code lost:
        if ((-2147483647 & r4) != 0) goto L_0x0130;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0058, code lost:
        r5 = 1;
        r9 = r4;
        r4 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005b, code lost:
        r10 = r9 >>> 1;
        r9 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005f, code lost:
        if ((r10 & 1) == 0) goto L_0x0064;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0061, code lost:
        r4 = r4 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0064, code lost:
        r10 = null;
        r11 = r0.head;
        r12 = r11.level;
        r13 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x006a, code lost:
        if (r4 > r12) goto L_0x007c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006d, code lost:
        if (r5 > r4) goto L_0x0078;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006f, code lost:
        r10 = new java.util.concurrent.ConcurrentSkipListMap.Index<>(r6, r10, null);
        r5 = r5 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0078, code lost:
        r12 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0079, code lost:
        r18 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x007c, code lost:
        r12 = r13 + 1;
        r14 = new java.util.concurrent.ConcurrentSkipListMap.Index[(r12 + 1)];
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0083, code lost:
        r4 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0084, code lost:
        if (r4 > r12) goto L_0x0091;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0086, code lost:
        r5 = new java.util.concurrent.ConcurrentSkipListMap.Index<>(r6, r10, null);
        r10 = r5;
        r14[r4] = r5;
        r5 = r4 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0091, code lost:
        r11 = r0.head;
        r4 = r11.level;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0095, code lost:
        if (r12 > r4) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0098, code lost:
        r5 = r11;
        r15 = r11.node;
        r16 = r4 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x009d, code lost:
        r8 = r16;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00a1, code lost:
        if (r8 > r12) goto L_0x00b5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00a3, code lost:
        r5 = new java.util.concurrent.ConcurrentSkipListMap.HeadIndex<>(r15, r5, r14[r8], r8);
        r16 = r8 + 1;
        r6 = r6;
        r2 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00b5, code lost:
        r18 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00bb, code lost:
        if (casHead(r11, r5) == false) goto L_0x0127;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00bd, code lost:
        r11 = r5;
        r10 = r14[r4];
        r12 = r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00c3, code lost:
        r2 = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00c4, code lost:
        r4 = r11.level;
        r5 = r11;
        r6 = r5.right;
        r8 = r4;
        r4 = r2;
        r2 = r10;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00cc, code lost:
        if (r5 == null) goto L_0x0123;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00ce, code lost:
        if (r2 != null) goto L_0x00d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00d0, code lost:
        r19 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00d4, code lost:
        if (r6 == null) goto L_0x00f7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00d6, code lost:
        r14 = r6.node;
        r15 = cpr(r3, r1, r14.key);
        r19 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00e2, code lost:
        if (r14.value != null) goto L_0x00f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00e8, code lost:
        if (r5.unlink(r6) != false) goto L_0x00eb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00eb, code lost:
        r6 = r5.right;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00ee, code lost:
        r3 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x00f1, code lost:
        if (r15 <= 0) goto L_0x00f9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00f3, code lost:
        r5 = r6;
        r6 = r6.right;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00f7, code lost:
        r19 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x00f9, code lost:
        if (r8 != r4) goto L_0x0116;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x00ff, code lost:
        if (r5.link(r6, r2) != false) goto L_0x0107;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0102, code lost:
        r2 = r4;
        r3 = r19;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x010b, code lost:
        if (r2.node.value != null) goto L_0x0111;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x010d, code lost:
        findNode(r21);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0111, code lost:
        r4 = r4 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0113, code lost:
        if (r4 != 0) goto L_0x0116;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x0116, code lost:
        r8 = r8 - 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0118, code lost:
        if (r8 < r4) goto L_0x011e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x011a, code lost:
        if (r8 >= r12) goto L_0x011e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x011c, code lost:
        r2 = r2.down;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x011e, code lost:
        r5 = r5.down;
        r6 = r5.right;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0123, code lost:
        r19 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x0125, code lost:
        r4 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0127, code lost:
        r19 = r3;
        r6 = r18;
        r2 = r22;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0130, code lost:
        r19 = r3;
        r18 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0135, code lost:
        return null;
     */
    private V doPut(K key, V value, boolean onlyIfAbsent) {
        Node<K, V> v;
        K k = key;
        V v2 = value;
        if (k != null) {
            Comparator<? super K> cmp = this.comparator;
            while (true) {
                Node<K, V> b = findPredecessor(k, cmp);
                Node<K, V> n = b.next;
                while (true) {
                    if (n == null) {
                        break;
                    }
                    Node<K, V> f = n.next;
                    if (n == b.next) {
                        V v3 = n.value;
                        v = v3;
                        if (v3 != null) {
                            if (b.value == null || v == n) {
                                break;
                            }
                            int cpr = cpr(cmp, k, n.key);
                            int c = cpr;
                            if (cpr > 0) {
                                b = n;
                                n = f;
                            } else if (c == 0) {
                                if (onlyIfAbsent || n.casValue(v, v2)) {
                                }
                            }
                        } else {
                            n.helpDelete(b, f);
                            break;
                        }
                    } else {
                        continue;
                        break;
                    }
                }
            }
            return v;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final V doRemove(Object key, Object value) {
        if (key != null) {
            Comparator<? super K> cmp = this.comparator;
            loop0:
            while (true) {
                Node<K, V> b = findPredecessor(key, cmp);
                Node<K, V> n = b.next;
                while (true) {
                    if (n != null) {
                        Node<K, V> f = n.next;
                        if (n == b.next) {
                            V v = n.value;
                            Node<K, V> v2 = v;
                            if (v != null) {
                                if (b.value != null && v2 != n) {
                                    int cpr = cpr(cmp, key, n.key);
                                    int c = cpr;
                                    if (cpr >= 0) {
                                        if (c <= 0) {
                                            if (value != null && !value.equals(v2)) {
                                                break;
                                            } else if (n.casValue(v2, null)) {
                                                if (!n.appendMarker(f) || !b.casNext(n, f)) {
                                                    findNode(key);
                                                } else {
                                                    findPredecessor(key, cmp);
                                                    if (this.head.right == null) {
                                                        tryReduceLevel();
                                                    }
                                                }
                                                return v2;
                                            }
                                        } else {
                                            b = n;
                                            n = f;
                                        }
                                    } else {
                                        break loop0;
                                    }
                                } else {
                                    break;
                                }
                            } else {
                                n.helpDelete(b, f);
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break loop0;
                    }
                }
            }
            return null;
        }
        throw new NullPointerException();
    }

    private void tryReduceLevel() {
        HeadIndex<K, V> h = this.head;
        if (h.level > 3) {
            HeadIndex<K, V> headIndex = (HeadIndex) h.down;
            HeadIndex<K, V> d = headIndex;
            if (headIndex != null) {
                HeadIndex<K, V> headIndex2 = (HeadIndex) d.down;
                HeadIndex<K, V> e = headIndex2;
                if (headIndex2 != null && e.right == null && d.right == null && h.right == null && casHead(h, d) && h.right != null) {
                    casHead(d, h);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final Node<K, V> findFirst() {
        while (true) {
            Node<K, V> node = this.head.node;
            Node<K, V> b = node;
            Node<K, V> node2 = node.next;
            Node<K, V> n = node2;
            if (node2 == null) {
                return null;
            }
            if (n.value != null) {
                return n;
            }
            n.helpDelete(b, n.next);
        }
    }

    private Map.Entry<K, V> doRemoveFirstEntry() {
        while (true) {
            Node<K, V> node = this.head.node;
            Node<K, V> b = node;
            Node<K, V> node2 = node.next;
            Node<K, V> n = node2;
            if (node2 == null) {
                return null;
            }
            Node<K, V> f = n.next;
            if (n == b.next) {
                V v = n.value;
                if (v == null) {
                    n.helpDelete(b, f);
                } else if (n.casValue(v, null)) {
                    if (!n.appendMarker(f) || !b.casNext(n, f)) {
                        findFirst();
                    }
                    clearIndexToFirst();
                    return new AbstractMap.SimpleImmutableEntry(n.key, v);
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
                if (r == null || !r.indexesDeletedNode() || q.unlink(r)) {
                    Index<K, V> index = q.down;
                    q = index;
                    if (index == null) {
                        break loop0;
                    }
                }
            }
        }
        if (this.head.right == null) {
            tryReduceLevel();
        }
    }

    private Map.Entry<K, V> doRemoveLastEntry() {
        while (true) {
            Node<K, V> b = findPredecessorOfLast();
            Node<K, V> n = b.next;
            if (n != null) {
                while (true) {
                    Node<K, V> f = n.next;
                    if (n == b.next) {
                        V v = n.value;
                        if (v != null) {
                            if (b.value == null || v == n) {
                                break;
                            } else if (f != null) {
                                b = n;
                                n = f;
                            } else if (n.casValue(v, null)) {
                                K key = n.key;
                                if (!n.appendMarker(f) || !b.casNext(n, f)) {
                                    findNode(key);
                                } else {
                                    findPredecessor(key, this.comparator);
                                    if (this.head.right == null) {
                                        tryReduceLevel();
                                    }
                                }
                                return new AbstractMap.SimpleImmutableEntry(key, v);
                            }
                        } else {
                            n.helpDelete(b, f);
                            break;
                        }
                    } else {
                        continue;
                        break;
                    }
                }
            } else if (b.isBaseHeader()) {
                return null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final Node<K, V> findLast() {
        Node<K, V> b;
        Index<K, V> q = this.head;
        loop0:
        while (true) {
            Index<K, V> index = q.right;
            Index<K, V> r = index;
            if (index == null) {
                Index<K, V> index2 = q.down;
                Index<K, V> d = index2;
                if (index2 == null) {
                    b = q.node;
                    Node<K, V> n = b.next;
                    while (n != null) {
                        Node<K, V> f = n.next;
                        if (n == b.next) {
                            Object v = n.value;
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
        if (b.isBaseHeader()) {
            return null;
        }
        return b;
    }

    private Node<K, V> findPredecessorOfLast() {
        Index<K, V> r;
        while (true) {
            Index<K, V> q = this.head;
            while (true) {
                Index<K, V> index = q.right;
                r = index;
                if (index != null) {
                    if (r.indexesDeletedNode()) {
                        break;
                    } else if (r.node.next != null) {
                        q = r;
                    }
                }
                Index<K, V> index2 = q.down;
                Index<K, V> d = index2;
                if (index2 == null) {
                    return q.node;
                }
                q = d;
            }
            q.unlink(r);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r5v0, resolved type: java.util.concurrent.ConcurrentSkipListMap$Node<K, V>} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Multi-variable type inference failed */
    public final Node<K, V> findNear(K key, int rel, Comparator<? super K> cmp) {
        Node<K, V> n;
        if (key != null) {
            loop0:
            while (true) {
                Node<K, V> b = findPredecessor(key, cmp);
                n = b.next;
                while (true) {
                    Node<K, V> node = null;
                    if (n != null) {
                        Node<K, V> f = n.next;
                        if (n == b.next) {
                            Object obj = n.value;
                            Object v = obj;
                            if (obj != 0) {
                                if (b.value == null || v == n) {
                                    break;
                                }
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
                            } else {
                                n.helpDelete(b, f);
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        if ((rel & 2) != 0 && !b.isBaseHeader()) {
                            node = b;
                        }
                        return node;
                    }
                }
            }
            return n;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final AbstractMap.SimpleImmutableEntry<K, V> getNear(K key, int rel) {
        AbstractMap.SimpleImmutableEntry<K, V> e;
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

    public ConcurrentSkipListMap(Comparator<? super K> comparator2) {
        this.comparator = comparator2;
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

    /* JADX WARNING: type inference failed for: r7v3, types: [java.util.concurrent.ConcurrentSkipListMap$Index] */
    /* JADX WARNING: type inference failed for: r7v4 */
    /* JADX WARNING: type inference failed for: r7v6 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void buildFromSorted(SortedMap<K, ? extends V> map) {
        Node node;
        int i;
        if (map != null) {
            HeadIndex<K, V> h = this.head;
            Node<K, V> basepred = h.node;
            ArrayList<Index<K, V>> preds = new ArrayList<>();
            int i2 = 0;
            while (true) {
                node = null;
                if (i2 > h.level) {
                    break;
                }
                preds.add(null);
                i2++;
            }
            Index<K, V> q = h;
            for (int i3 = h.level; i3 > 0; i3--) {
                preds.set(i3, q);
                q = q.down;
            }
            for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
                int rnd = ThreadLocalRandom.current().nextInt();
                int j = 0;
                int i4 = 1;
                if ((-2147483647 & rnd) == 0) {
                    do {
                        j++;
                        i = rnd >>> 1;
                        rnd = i;
                    } while ((i & 1) != 0);
                    if (j > h.level) {
                        j = h.level + 1;
                    }
                }
                K k = e.getKey();
                V v = e.getValue();
                if (k == null || v == null) {
                    throw new NullPointerException();
                }
                Node<K, V> z = new Node<>(k, v, node);
                basepred.next = z;
                basepred = z;
                if (j > 0) {
                    Index<K, V> idx = null;
                    ? r7 = node;
                    while (i4 <= j) {
                        idx = new Index<>(z, idx, r7);
                        if (i4 > h.level) {
                            h = new HeadIndex<>(h.node, h, idx, i4);
                        }
                        if (i4 < preds.size()) {
                            preds.get(i4).right = idx;
                            preds.set(i4, idx);
                        } else {
                            preds.add(idx);
                        }
                        i4++;
                        r7 = 0;
                    }
                }
                node = null;
            }
            this.head = h;
            return;
        }
        throw new NullPointerException();
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

    /* JADX WARNING: type inference failed for: r6v4, types: [java.util.concurrent.ConcurrentSkipListMap$Index] */
    /* JADX WARNING: type inference failed for: r6v6 */
    /* JADX WARNING: type inference failed for: r6v10 */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        Node node;
        int i;
        s.defaultReadObject();
        initialize();
        HeadIndex<K, V> h = this.head;
        Node<K, V> basepred = h.node;
        ArrayList<Index<K, V>> preds = new ArrayList<>();
        int i2 = 0;
        while (true) {
            node = null;
            if (i2 > h.level) {
                break;
            }
            preds.add(null);
            i2++;
        }
        Index<K, V> q = h;
        for (int i3 = h.level; i3 > 0; i3--) {
            preds.set(i3, q);
            q = q.down;
        }
        while (true) {
            K k = s.readObject();
            if (k == null) {
                this.head = h;
                return;
            }
            V v = s.readObject();
            if (v != null) {
                K key = k;
                V val = v;
                int rnd = ThreadLocalRandom.current().nextInt();
                int j = 0;
                int i4 = 1;
                if ((-2147483647 & rnd) == 0) {
                    do {
                        j++;
                        i = rnd >>> 1;
                        rnd = i;
                    } while ((i & 1) != 0);
                    if (j > h.level) {
                        j = h.level + 1;
                    }
                }
                Node<K, V> z = new Node<>(key, val, node);
                basepred.next = z;
                basepred = z;
                if (j > 0) {
                    Index<K, V> idx = null;
                    ? r6 = node;
                    while (i4 <= j) {
                        idx = new Index<>(z, idx, r6);
                        if (i4 > h.level) {
                            h = new HeadIndex<>(h.node, h, idx, i4);
                        }
                        if (i4 < preds.size()) {
                            preds.get(i4).right = idx;
                            preds.set(i4, idx);
                        } else {
                            preds.add(idx);
                        }
                        i4++;
                        r6 = 0;
                    }
                }
                node = null;
            } else {
                throw new NullPointerException();
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
        if (value != null) {
            for (Node<K, V> n = findFirst(); n != null; n = n.next) {
                V v = n.getValidValue();
                if (v != null && value.equals(v)) {
                    return true;
                }
            }
            return false;
        }
        throw new NullPointerException();
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
        V doGet = doGet(key);
        V v = doGet;
        if (doGet != null) {
            return v;
        }
        V apply = mappingFunction.apply(key);
        V r = apply;
        if (apply == null) {
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
            Node<K, V> findNode = findNode(key);
            Node<K, V> n = findNode;
            if (findNode == null) {
                break;
            }
            V v = n.value;
            V v2 = v;
            if (v != null) {
                V vv = v2;
                V r = remappingFunction.apply(key, vv);
                if (r != null) {
                    if (n.casValue(vv, r)) {
                        return r;
                    }
                } else if (doRemove(key, vv) != null) {
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
            Node<K, V> findNode = findNode(key);
            Node<K, V> n = findNode;
            if (findNode == null) {
                V apply = remappingFunction.apply(key, null);
                V r = apply;
                if (apply == null) {
                    break;
                } else if (doPut(key, r, true) == null) {
                    return r;
                }
            } else {
                V v = n.value;
                V v2 = v;
                if (v != null) {
                    V vv = v2;
                    V apply2 = remappingFunction.apply(key, vv);
                    V r2 = apply2;
                    if (apply2 != null) {
                        if (n.casValue(vv, r2)) {
                            return r2;
                        }
                    } else if (doRemove(key, vv) != null) {
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
            Node<K, V> findNode = findNode(key);
            Node<K, V> n = findNode;
            if (findNode != null) {
                V v = n.value;
                V v2 = v;
                if (v != null) {
                    V vv = v2;
                    V apply = remappingFunction.apply(vv, value);
                    V r = apply;
                    if (apply != null) {
                        if (n.casValue(vv, r)) {
                            return r;
                        }
                    } else if (doRemove(key, vv) != null) {
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
        KeySet<K, V> keySet2 = new KeySet<>(this);
        this.keySet = keySet2;
        return keySet2;
    }

    public NavigableSet<K> navigableKeySet() {
        KeySet<K, V> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        KeySet<K, V> keySet2 = new KeySet<>(this);
        this.keySet = keySet2;
        return keySet2;
    }

    public Collection<V> values() {
        Values<K, V> vs = this.values;
        if (vs != null) {
            return vs;
        }
        Values<K, V> values2 = new Values<>(this);
        this.values = values2;
        return values2;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        EntrySet<K, V> es = this.entrySet;
        if (es != null) {
            return es;
        }
        EntrySet<K, V> entrySet2 = new EntrySet<>(this);
        this.entrySet = entrySet2;
        return entrySet2;
    }

    public ConcurrentNavigableMap<K, V> descendingMap() {
        ConcurrentNavigableMap<K, V> dm = this.descendingMap;
        if (dm != null) {
            return dm;
        }
        SubMap subMap = new SubMap(this, null, false, null, false, true);
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
            for (Map.Entry<K, V> e : entrySet()) {
                if (!e.getValue().equals(m.get(e.getKey()))) {
                    return false;
                }
            }
            for (Map.Entry<?, ?> e2 : m.entrySet()) {
                Object k = e2.getKey();
                Object v = e2.getValue();
                if (k == null || v == null) {
                    return false;
                }
                if (!v.equals(get(k))) {
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
        if (key != null) {
            return (value == null || doRemove(key, value) == null) ? false : true;
        }
        throw new NullPointerException();
    }

    public boolean replace(K key, V oldValue, V newValue) {
        if (key == null || oldValue == null || newValue == null) {
            throw new NullPointerException();
        }
        while (true) {
            Node<K, V> findNode = findNode(key);
            Node<K, V> n = findNode;
            if (findNode == null) {
                return false;
            }
            Object obj = n.value;
            Object v = obj;
            if (obj != null) {
                if (!oldValue.equals(v)) {
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
            Node<K, V> findNode = findNode(key);
            Node<K, V> n = findNode;
            if (findNode == null) {
                return null;
            }
            V v = n.value;
            V v2 = v;
            if (v != null && n.casValue(v2, value)) {
                return v2;
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
        if (fromKey == null || toKey == null) {
            throw new NullPointerException();
        }
        SubMap subMap = new SubMap(this, fromKey, fromInclusive, toKey, toInclusive, false);
        return subMap;
    }

    public ConcurrentNavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        if (toKey != null) {
            SubMap subMap = new SubMap(this, null, false, toKey, inclusive, false);
            return subMap;
        }
        throw new NullPointerException();
    }

    public ConcurrentNavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        if (fromKey != null) {
            SubMap subMap = new SubMap(this, fromKey, inclusive, null, false, false);
            return subMap;
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

    public Map.Entry<K, V> lowerEntry(K key) {
        return getNear(key, 2);
    }

    public K lowerKey(K key) {
        Node<K, V> n = findNear(key, 2, this.comparator);
        if (n == null) {
            return null;
        }
        return n.key;
    }

    public Map.Entry<K, V> floorEntry(K key) {
        return getNear(key, 3);
    }

    public K floorKey(K key) {
        Node<K, V> n = findNear(key, 3, this.comparator);
        if (n == null) {
            return null;
        }
        return n.key;
    }

    public Map.Entry<K, V> ceilingEntry(K key) {
        return getNear(key, 1);
    }

    public K ceilingKey(K key) {
        Node<K, V> n = findNear(key, 1, this.comparator);
        if (n == null) {
            return null;
        }
        return n.key;
    }

    public Map.Entry<K, V> higherEntry(K key) {
        return getNear(key, 0);
    }

    public K higherKey(K key) {
        Node<K, V> n = findNear(key, 0, this.comparator);
        if (n == null) {
            return null;
        }
        return n.key;
    }

    public Map.Entry<K, V> firstEntry() {
        AbstractMap.SimpleImmutableEntry<K, V> e;
        do {
            Node<K, V> n = findFirst();
            if (n == null) {
                return null;
            }
            e = n.createSnapshot();
        } while (e == null);
        return e;
    }

    public Map.Entry<K, V> lastEntry() {
        AbstractMap.SimpleImmutableEntry<K, V> e;
        do {
            Node<K, V> n = findLast();
            if (n == null) {
                return null;
            }
            e = n.createSnapshot();
        } while (e == null);
        return e;
    }

    public Map.Entry<K, V> pollFirstEntry() {
        return doRemoveFirstEntry();
    }

    public Map.Entry<K, V> pollLastEntry() {
        return doRemoveLastEntry();
    }

    static final <E> List<E> toList(Collection<E> c) {
        ArrayList<E> list = new ArrayList<>();
        for (E e : c) {
            list.add(e);
        }
        return list;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action != null) {
            for (Node<K, V> n = findFirst(); n != null; n = n.next) {
                V validValue = n.getValidValue();
                V v = validValue;
                if (validValue != null) {
                    action.accept(n.key, v);
                }
            }
            return;
        }
        throw new NullPointerException();
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        V v;
        V r;
        if (function != null) {
            for (Node<K, V> n = findFirst(); n != null; n = n.next) {
                do {
                    V validValue = n.getValidValue();
                    v = validValue;
                    if (validValue == null) {
                        break;
                    }
                    r = function.apply(n.key, v);
                    if (r == null) {
                        throw new NullPointerException();
                    }
                } while (!n.casValue(v, r));
            }
            return;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public boolean removeEntryIf(Predicate<? super Map.Entry<K, V>> function) {
        if (function != null) {
            boolean removed = false;
            for (Node<K, V> n = findFirst(); n != null; n = n.next) {
                V validValue = n.getValidValue();
                V v = validValue;
                if (validValue != null) {
                    K k = n.key;
                    if (function.test(new AbstractMap.SimpleImmutableEntry<>(k, v)) && remove(k, v)) {
                        removed = true;
                    }
                }
            }
            return removed;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public boolean removeValueIf(Predicate<? super V> function) {
        if (function != null) {
            boolean removed = false;
            for (Node<K, V> n = findFirst(); n != null; n = n.next) {
                V validValue = n.getValidValue();
                V v = validValue;
                if (validValue != null) {
                    K k = n.key;
                    if (function.test(v) && remove(k, v)) {
                        removed = true;
                    }
                }
            }
            return removed;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final KeySpliterator<K, V> keySpliterator() {
        HeadIndex<K, V> h;
        Node<K, V> p;
        Comparator<? super K> cmp = this.comparator;
        while (true) {
            HeadIndex<K, V> headIndex = this.head;
            h = headIndex;
            Node<K, V> b = headIndex.node;
            Node<K, V> node = b.next;
            p = node;
            if (node != null && p.value == null) {
                p.helpDelete(b, p.next);
            }
        }
        KeySpliterator keySpliterator = new KeySpliterator(cmp, h, p, null, p == null ? 0 : Integer.MAX_VALUE);
        return keySpliterator;
    }

    /* access modifiers changed from: package-private */
    public final ValueSpliterator<K, V> valueSpliterator() {
        HeadIndex<K, V> h;
        Node<K, V> p;
        Comparator<? super K> cmp = this.comparator;
        while (true) {
            HeadIndex<K, V> headIndex = this.head;
            h = headIndex;
            Node<K, V> b = headIndex.node;
            Node<K, V> node = b.next;
            p = node;
            if (node != null && p.value == null) {
                p.helpDelete(b, p.next);
            }
        }
        ValueSpliterator valueSpliterator = new ValueSpliterator(cmp, h, p, null, p == null ? 0 : Integer.MAX_VALUE);
        return valueSpliterator;
    }

    /* access modifiers changed from: package-private */
    public final EntrySpliterator<K, V> entrySpliterator() {
        HeadIndex<K, V> h;
        Node<K, V> p;
        Comparator<? super K> cmp = this.comparator;
        while (true) {
            HeadIndex<K, V> headIndex = this.head;
            h = headIndex;
            Node<K, V> b = headIndex.node;
            Node<K, V> node = b.next;
            p = node;
            if (node != null && p.value == null) {
                p.helpDelete(b, p.next);
            }
        }
        EntrySpliterator entrySpliterator = new EntrySpliterator(cmp, h, p, null, p == null ? 0 : Integer.MAX_VALUE);
        return entrySpliterator;
    }
}
