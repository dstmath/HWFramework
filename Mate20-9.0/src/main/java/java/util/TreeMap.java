package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.RCUnownedRef;
import java.lang.annotation.RCWeakRef;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class TreeMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V>, Cloneable, Serializable {
    private static final boolean BLACK = true;
    private static final boolean RED = false;
    /* access modifiers changed from: private */
    public static final Object UNBOUNDED = new Object();
    private static final long serialVersionUID = 919286545866124006L;
    /* access modifiers changed from: private */
    public final Comparator<? super K> comparator;
    private transient NavigableMap<K, V> descendingMap;
    private transient TreeMap<K, V>.EntrySet entrySet;
    /* access modifiers changed from: private */
    public transient int modCount;
    @RCWeakRef
    private transient KeySet<K> navigableKeySet;
    /* access modifiers changed from: private */
    public transient TreeMapEntry<K, V> root;
    /* access modifiers changed from: private */
    public transient int size;

    static final class AscendingSubMap<K, V> extends NavigableSubMap<K, V> {
        private static final long serialVersionUID = 912986545866124060L;

        final class AscendingEntrySetView extends NavigableSubMap<K, V>.EntrySetView {
            AscendingEntrySetView() {
                super();
            }

            public Iterator<Map.Entry<K, V>> iterator() {
                return new NavigableSubMap.SubMapEntryIterator(AscendingSubMap.this.absLowest(), AscendingSubMap.this.absHighFence());
            }
        }

        AscendingSubMap(TreeMap<K, V> m, boolean fromStart, K lo, boolean loInclusive, boolean toEnd, K hi, boolean hiInclusive) {
            super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
        }

        public Comparator<? super K> comparator() {
            return this.m.comparator();
        }

        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            if (!inRange(fromKey, fromInclusive)) {
                throw new IllegalArgumentException("fromKey out of range");
            } else if (inRange(toKey, toInclusive)) {
                AscendingSubMap ascendingSubMap = new AscendingSubMap(this.m, TreeMap.RED, fromKey, fromInclusive, TreeMap.RED, toKey, toInclusive);
                return ascendingSubMap;
            } else {
                throw new IllegalArgumentException("toKey out of range");
            }
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            if (inRange(toKey) || (!this.toEnd && this.m.compare(toKey, this.hi) == 0 && !this.hiInclusive && !inclusive)) {
                AscendingSubMap ascendingSubMap = new AscendingSubMap(this.m, this.fromStart, this.lo, this.loInclusive, TreeMap.RED, toKey, inclusive);
                return ascendingSubMap;
            }
            throw new IllegalArgumentException("toKey out of range");
        }

        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            if (inRange(fromKey) || (!this.fromStart && this.m.compare(fromKey, this.lo) == 0 && !this.loInclusive && !inclusive)) {
                AscendingSubMap ascendingSubMap = new AscendingSubMap(this.m, TreeMap.RED, fromKey, inclusive, this.toEnd, this.hi, this.hiInclusive);
                return ascendingSubMap;
            }
            throw new IllegalArgumentException("fromKey out of range");
        }

        public NavigableMap<K, V> descendingMap() {
            NavigableMap<K, V> mv = this.descendingMapView;
            if (mv != null) {
                return mv;
            }
            DescendingSubMap descendingSubMap = new DescendingSubMap(this.m, this.fromStart, this.lo, this.loInclusive, this.toEnd, this.hi, this.hiInclusive);
            this.descendingMapView = descendingSubMap;
            return descendingSubMap;
        }

        /* access modifiers changed from: package-private */
        public Iterator<K> keyIterator() {
            return new NavigableSubMap.SubMapKeyIterator(absLowest(), absHighFence());
        }

        /* access modifiers changed from: package-private */
        public Spliterator<K> keySpliterator() {
            return new NavigableSubMap.SubMapKeyIterator(absLowest(), absHighFence());
        }

        /* access modifiers changed from: package-private */
        public Iterator<K> descendingKeyIterator() {
            return new NavigableSubMap.DescendingSubMapKeyIterator(absHighest(), absLowFence());
        }

        public Set<Map.Entry<K, V>> entrySet() {
            NavigableSubMap<K, V>.EntrySetView es = this.entrySetView;
            if (es != null) {
                return es;
            }
            AscendingEntrySetView ascendingEntrySetView = new AscendingEntrySetView();
            this.entrySetView = ascendingEntrySetView;
            return ascendingEntrySetView;
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subLowest() {
            return absLowest();
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subHighest() {
            return absHighest();
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subCeiling(K key) {
            return absCeiling(key);
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subHigher(K key) {
            return absHigher(key);
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subFloor(K key) {
            return absFloor(key);
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subLower(K key) {
            return absLower(key);
        }
    }

    final class DescendingKeyIterator extends TreeMap<K, V>.PrivateEntryIterator<K> {
        DescendingKeyIterator(TreeMapEntry<K, V> first) {
            super(first);
        }

        public K next() {
            return prevEntry().key;
        }

        public void remove() {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            } else if (TreeMap.this.modCount == this.expectedModCount) {
                TreeMap.this.deleteEntry(this.lastReturned);
                this.lastReturned = null;
                this.expectedModCount = TreeMap.this.modCount;
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    static final class DescendingKeySpliterator<K, V> extends TreeMapSpliterator<K, V> implements Spliterator<K> {
        DescendingKeySpliterator(TreeMap<K, V> tree, TreeMapEntry<K, V> origin, TreeMapEntry<K, V> fence, int side, int est, int expectedModCount) {
            super(tree, origin, fence, side, est, expectedModCount);
        }

        public DescendingKeySpliterator<K, V> trySplit() {
            TreeMapEntry<K, V> treeMapEntry;
            TreeMapEntry<K, V> s;
            if (this.est < 0) {
                getEstimate();
            }
            int d = this.side;
            TreeMapEntry<K, V> e = this.current;
            TreeMapEntry<K, V> f = this.fence;
            if (!(e == null || e == f)) {
                if (d == 0) {
                    treeMapEntry = this.tree.root;
                } else if (d < 0) {
                    treeMapEntry = e.left;
                } else if (d > 0 && f != null) {
                    treeMapEntry = f.right;
                }
                s = treeMapEntry;
                if (s != null || s == e || s == f || this.tree.compare(e.key, s.key) <= 0) {
                    return null;
                }
                this.side = 1;
                TreeMap treeMap = this.tree;
                this.current = s;
                int i = this.est >>> 1;
                this.est = i;
                DescendingKeySpliterator descendingKeySpliterator = new DescendingKeySpliterator(treeMap, e, s, -1, i, this.expectedModCount);
                return descendingKeySpliterator;
            }
            treeMapEntry = null;
            s = treeMapEntry;
            if (s != null) {
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action != null) {
                if (this.est < 0) {
                    getEstimate();
                }
                TreeMapEntry<K, V> f = this.fence;
                TreeMapEntry<K, V> treeMapEntry = this.current;
                TreeMapEntry<K, V> e = treeMapEntry;
                if (treeMapEntry != null && e != f) {
                    this.current = f;
                    do {
                        action.accept(e.key);
                        TreeMapEntry<K, V> treeMapEntry2 = e.left;
                        TreeMapEntry<K, V> p = treeMapEntry2;
                        if (treeMapEntry2 == null) {
                            while (true) {
                                TreeMapEntry<K, V> treeMapEntry3 = e.parent;
                                p = treeMapEntry3;
                                if (treeMapEntry3 == null || e != p.left) {
                                    break;
                                }
                                e = p;
                            }
                        } else {
                            while (true) {
                                TreeMapEntry<K, V> treeMapEntry4 = p.right;
                                TreeMapEntry<K, V> pr = treeMapEntry4;
                                if (treeMapEntry4 == null) {
                                    break;
                                }
                                p = pr;
                            }
                        }
                        e = p;
                        if (p == null) {
                            break;
                        }
                    } while (e != f);
                    if (this.tree.modCount != this.expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action != null) {
                if (this.est < 0) {
                    getEstimate();
                }
                TreeMapEntry<K, V> treeMapEntry = this.current;
                TreeMapEntry<K, V> e = treeMapEntry;
                if (treeMapEntry == null || e == this.fence) {
                    return TreeMap.RED;
                }
                this.current = TreeMap.predecessor(e);
                action.accept(e.key);
                if (this.tree.modCount == this.expectedModCount) {
                    return TreeMap.BLACK;
                }
                throw new ConcurrentModificationException();
            }
            throw new NullPointerException();
        }

        public int characteristics() {
            return (this.side == 0 ? 64 : 0) | 1 | 16;
        }
    }

    static final class DescendingSubMap<K, V> extends NavigableSubMap<K, V> {
        private static final long serialVersionUID = 912986545866120460L;
        private final Comparator<? super K> reverseComparator = Collections.reverseOrder(this.m.comparator);

        final class DescendingEntrySetView extends NavigableSubMap<K, V>.EntrySetView {
            DescendingEntrySetView() {
                super();
            }

            public Iterator<Map.Entry<K, V>> iterator() {
                return new NavigableSubMap.DescendingSubMapEntryIterator(DescendingSubMap.this.absHighest(), DescendingSubMap.this.absLowFence());
            }
        }

        DescendingSubMap(TreeMap<K, V> m, boolean fromStart, K lo, boolean loInclusive, boolean toEnd, K hi, boolean hiInclusive) {
            super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
        }

        public Comparator<? super K> comparator() {
            return this.reverseComparator;
        }

        public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
            if (!inRange(fromKey, fromInclusive)) {
                throw new IllegalArgumentException("fromKey out of range");
            } else if (inRange(toKey, toInclusive)) {
                DescendingSubMap descendingSubMap = new DescendingSubMap(this.m, TreeMap.RED, toKey, toInclusive, TreeMap.RED, fromKey, fromInclusive);
                return descendingSubMap;
            } else {
                throw new IllegalArgumentException("toKey out of range");
            }
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            if (inRange(toKey) || (!this.fromStart && this.m.compare(toKey, this.lo) == 0 && !this.loInclusive && !inclusive)) {
                DescendingSubMap descendingSubMap = new DescendingSubMap(this.m, TreeMap.RED, toKey, inclusive, this.toEnd, this.hi, this.hiInclusive);
                return descendingSubMap;
            }
            throw new IllegalArgumentException("toKey out of range");
        }

        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            if (inRange(fromKey) || (!this.toEnd && this.m.compare(fromKey, this.hi) == 0 && !this.hiInclusive && !inclusive)) {
                DescendingSubMap descendingSubMap = new DescendingSubMap(this.m, this.fromStart, this.lo, this.loInclusive, TreeMap.RED, fromKey, inclusive);
                return descendingSubMap;
            }
            throw new IllegalArgumentException("fromKey out of range");
        }

        public NavigableMap<K, V> descendingMap() {
            NavigableMap<K, V> mv = this.descendingMapView;
            if (mv != null) {
                return mv;
            }
            AscendingSubMap ascendingSubMap = new AscendingSubMap(this.m, this.fromStart, this.lo, this.loInclusive, this.toEnd, this.hi, this.hiInclusive);
            this.descendingMapView = ascendingSubMap;
            return ascendingSubMap;
        }

        /* access modifiers changed from: package-private */
        public Iterator<K> keyIterator() {
            return new NavigableSubMap.DescendingSubMapKeyIterator(absHighest(), absLowFence());
        }

        /* access modifiers changed from: package-private */
        public Spliterator<K> keySpliterator() {
            return new NavigableSubMap.DescendingSubMapKeyIterator(absHighest(), absLowFence());
        }

        /* access modifiers changed from: package-private */
        public Iterator<K> descendingKeyIterator() {
            return new NavigableSubMap.SubMapKeyIterator(absLowest(), absHighFence());
        }

        public Set<Map.Entry<K, V>> entrySet() {
            NavigableSubMap<K, V>.EntrySetView es = this.entrySetView;
            if (es != null) {
                return es;
            }
            DescendingEntrySetView descendingEntrySetView = new DescendingEntrySetView();
            this.entrySetView = descendingEntrySetView;
            return descendingEntrySetView;
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subLowest() {
            return absHighest();
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subHighest() {
            return absLowest();
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subCeiling(K key) {
            return absFloor(key);
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subHigher(K key) {
            return absLower(key);
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subFloor(K key) {
            return absCeiling(key);
        }

        /* access modifiers changed from: package-private */
        public TreeMapEntry<K, V> subLower(K key) {
            return absHigher(key);
        }
    }

    final class EntryIterator extends TreeMap<K, V>.PrivateEntryIterator<Map.Entry<K, V>> {
        EntryIterator(TreeMapEntry<K, V> first) {
            super(first);
        }

        public Map.Entry<K, V> next() {
            return nextEntry();
        }
    }

    class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        EntrySet() {
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator(TreeMap.this.getFirstEntry());
        }

        public boolean contains(Object o) {
            boolean z = o instanceof Map.Entry;
            boolean z2 = TreeMap.RED;
            if (!z) {
                return TreeMap.RED;
            }
            Map.Entry<?, ?> entry = (Map.Entry) o;
            Object value = entry.getValue();
            TreeMapEntry<K, V> p = TreeMap.this.getEntry(entry.getKey());
            if (p != null && TreeMap.valEquals(p.getValue(), value)) {
                z2 = TreeMap.BLACK;
            }
            return z2;
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return TreeMap.RED;
            }
            Map.Entry<?, ?> entry = (Map.Entry) o;
            Object value = entry.getValue();
            TreeMapEntry<K, V> p = TreeMap.this.getEntry(entry.getKey());
            if (p == null || !TreeMap.valEquals(p.getValue(), value)) {
                return TreeMap.RED;
            }
            TreeMap.this.deleteEntry(p);
            return TreeMap.BLACK;
        }

        public int size() {
            return TreeMap.this.size();
        }

        public void clear() {
            TreeMap.this.clear();
        }

        public Spliterator<Map.Entry<K, V>> spliterator() {
            EntrySpliterator entrySpliterator = new EntrySpliterator(TreeMap.this, null, null, 0, -1, 0);
            return entrySpliterator;
        }
    }

    static final class EntrySpliterator<K, V> extends TreeMapSpliterator<K, V> implements Spliterator<Map.Entry<K, V>> {
        EntrySpliterator(TreeMap<K, V> tree, TreeMapEntry<K, V> origin, TreeMapEntry<K, V> fence, int side, int est, int expectedModCount) {
            super(tree, origin, fence, side, est, expectedModCount);
        }

        public EntrySpliterator<K, V> trySplit() {
            TreeMapEntry<K, V> treeMapEntry;
            TreeMapEntry<K, V> s;
            if (this.est < 0) {
                getEstimate();
            }
            int d = this.side;
            TreeMapEntry<K, V> e = this.current;
            TreeMapEntry<K, V> f = this.fence;
            if (!(e == null || e == f)) {
                if (d == 0) {
                    treeMapEntry = this.tree.root;
                } else if (d > 0) {
                    treeMapEntry = e.right;
                } else if (d < 0 && f != null) {
                    treeMapEntry = f.left;
                }
                s = treeMapEntry;
                if (s != null || s == e || s == f || this.tree.compare(e.key, s.key) >= 0) {
                    return null;
                }
                this.side = 1;
                TreeMap treeMap = this.tree;
                this.current = s;
                int i = this.est >>> 1;
                this.est = i;
                EntrySpliterator entrySpliterator = new EntrySpliterator(treeMap, e, s, -1, i, this.expectedModCount);
                return entrySpliterator;
            }
            treeMapEntry = null;
            s = treeMapEntry;
            if (s != null) {
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            if (action != null) {
                if (this.est < 0) {
                    getEstimate();
                }
                TreeMapEntry<K, V> f = this.fence;
                TreeMapEntry<K, V> treeMapEntry = this.current;
                TreeMapEntry<K, V> e = treeMapEntry;
                if (treeMapEntry != null && e != f) {
                    this.current = f;
                    do {
                        action.accept(e);
                        TreeMapEntry<K, V> treeMapEntry2 = e.right;
                        TreeMapEntry<K, V> p = treeMapEntry2;
                        if (treeMapEntry2 == null) {
                            while (true) {
                                TreeMapEntry<K, V> treeMapEntry3 = e.parent;
                                p = treeMapEntry3;
                                if (treeMapEntry3 == null || e != p.right) {
                                    break;
                                }
                                e = p;
                            }
                        } else {
                            while (true) {
                                TreeMapEntry<K, V> treeMapEntry4 = p.left;
                                TreeMapEntry<K, V> pl = treeMapEntry4;
                                if (treeMapEntry4 == null) {
                                    break;
                                }
                                p = pl;
                            }
                        }
                        e = p;
                        if (p == null) {
                            break;
                        }
                    } while (e != f);
                    if (this.tree.modCount != this.expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            if (action != null) {
                if (this.est < 0) {
                    getEstimate();
                }
                TreeMapEntry<K, V> treeMapEntry = this.current;
                TreeMapEntry<K, V> e = treeMapEntry;
                if (treeMapEntry == null || e == this.fence) {
                    return TreeMap.RED;
                }
                this.current = TreeMap.successor(e);
                action.accept(e);
                if (this.tree.modCount == this.expectedModCount) {
                    return TreeMap.BLACK;
                }
                throw new ConcurrentModificationException();
            }
            throw new NullPointerException();
        }

        public int characteristics() {
            return (this.side == 0 ? 64 : 0) | 1 | 4 | 16;
        }

        public Comparator<Map.Entry<K, V>> getComparator() {
            if (this.tree.comparator != null) {
                return Map.Entry.comparingByKey(this.tree.comparator);
            }
            return $$Lambda$TreeMap$EntrySpliterator$YqCulUmBGNzQr1PJ_ERFnbxUmTQ.INSTANCE;
        }
    }

    final class KeyIterator extends TreeMap<K, V>.PrivateEntryIterator<K> {
        KeyIterator(TreeMapEntry<K, V> first) {
            super(first);
        }

        public K next() {
            return nextEntry().key;
        }
    }

    static final class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
        private final NavigableMap<E, ?> m;

        KeySet(NavigableMap<E, ?> map) {
            this.m = map;
        }

        public Iterator<E> iterator() {
            if (this.m instanceof TreeMap) {
                return ((TreeMap) this.m).keyIterator();
            }
            return ((NavigableSubMap) this.m).keyIterator();
        }

        public Iterator<E> descendingIterator() {
            if (this.m instanceof TreeMap) {
                return ((TreeMap) this.m).descendingKeyIterator();
            }
            return ((NavigableSubMap) this.m).descendingKeyIterator();
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

        public void clear() {
            this.m.clear();
        }

        public E lower(E e) {
            return this.m.lowerKey(e);
        }

        public E floor(E e) {
            return this.m.floorKey(e);
        }

        public E ceiling(E e) {
            return this.m.ceilingKey(e);
        }

        public E higher(E e) {
            return this.m.higherKey(e);
        }

        public E first() {
            return this.m.firstKey();
        }

        public E last() {
            return this.m.lastKey();
        }

        public Comparator<? super E> comparator() {
            return this.m.comparator();
        }

        public E pollFirst() {
            Map.Entry<E, ?> e = this.m.pollFirstEntry();
            if (e == null) {
                return null;
            }
            return e.getKey();
        }

        public E pollLast() {
            Map.Entry<E, ?> e = this.m.pollLastEntry();
            if (e == null) {
                return null;
            }
            return e.getKey();
        }

        public boolean remove(Object o) {
            int oldSize = size();
            this.m.remove(o);
            return size() != oldSize ? TreeMap.BLACK : TreeMap.RED;
        }

        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            return new KeySet(this.m.subMap(fromElement, fromInclusive, toElement, toInclusive));
        }

        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return new KeySet(this.m.headMap(toElement, inclusive));
        }

        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return new KeySet(this.m.tailMap(fromElement, inclusive));
        }

        public SortedSet<E> subSet(E fromElement, E toElement) {
            return subSet(fromElement, TreeMap.BLACK, toElement, TreeMap.RED);
        }

        public SortedSet<E> headSet(E toElement) {
            return headSet(toElement, TreeMap.RED);
        }

        public SortedSet<E> tailSet(E fromElement) {
            return tailSet(fromElement, TreeMap.BLACK);
        }

        public NavigableSet<E> descendingSet() {
            return new KeySet(this.m.descendingMap());
        }

        public Spliterator<E> spliterator() {
            return TreeMap.keySpliteratorFor(this.m);
        }
    }

    static final class KeySpliterator<K, V> extends TreeMapSpliterator<K, V> implements Spliterator<K> {
        KeySpliterator(TreeMap<K, V> tree, TreeMapEntry<K, V> origin, TreeMapEntry<K, V> fence, int side, int est, int expectedModCount) {
            super(tree, origin, fence, side, est, expectedModCount);
        }

        public KeySpliterator<K, V> trySplit() {
            TreeMapEntry<K, V> treeMapEntry;
            TreeMapEntry<K, V> s;
            if (this.est < 0) {
                getEstimate();
            }
            int d = this.side;
            TreeMapEntry<K, V> e = this.current;
            TreeMapEntry<K, V> f = this.fence;
            if (!(e == null || e == f)) {
                if (d == 0) {
                    treeMapEntry = this.tree.root;
                } else if (d > 0) {
                    treeMapEntry = e.right;
                } else if (d < 0 && f != null) {
                    treeMapEntry = f.left;
                }
                s = treeMapEntry;
                if (s != null || s == e || s == f || this.tree.compare(e.key, s.key) >= 0) {
                    return null;
                }
                this.side = 1;
                TreeMap treeMap = this.tree;
                this.current = s;
                int i = this.est >>> 1;
                this.est = i;
                KeySpliterator keySpliterator = new KeySpliterator(treeMap, e, s, -1, i, this.expectedModCount);
                return keySpliterator;
            }
            treeMapEntry = null;
            s = treeMapEntry;
            if (s != null) {
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action != null) {
                if (this.est < 0) {
                    getEstimate();
                }
                TreeMapEntry<K, V> f = this.fence;
                TreeMapEntry<K, V> treeMapEntry = this.current;
                TreeMapEntry<K, V> e = treeMapEntry;
                if (treeMapEntry != null && e != f) {
                    this.current = f;
                    do {
                        action.accept(e.key);
                        TreeMapEntry<K, V> treeMapEntry2 = e.right;
                        TreeMapEntry<K, V> p = treeMapEntry2;
                        if (treeMapEntry2 == null) {
                            while (true) {
                                TreeMapEntry<K, V> treeMapEntry3 = e.parent;
                                p = treeMapEntry3;
                                if (treeMapEntry3 == null || e != p.right) {
                                    break;
                                }
                                e = p;
                            }
                        } else {
                            while (true) {
                                TreeMapEntry<K, V> treeMapEntry4 = p.left;
                                TreeMapEntry<K, V> pl = treeMapEntry4;
                                if (treeMapEntry4 == null) {
                                    break;
                                }
                                p = pl;
                            }
                        }
                        e = p;
                        if (p == null) {
                            break;
                        }
                    } while (e != f);
                    if (this.tree.modCount != this.expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action != null) {
                if (this.est < 0) {
                    getEstimate();
                }
                TreeMapEntry<K, V> treeMapEntry = this.current;
                TreeMapEntry<K, V> e = treeMapEntry;
                if (treeMapEntry == null || e == this.fence) {
                    return TreeMap.RED;
                }
                this.current = TreeMap.successor(e);
                action.accept(e.key);
                if (this.tree.modCount == this.expectedModCount) {
                    return TreeMap.BLACK;
                }
                throw new ConcurrentModificationException();
            }
            throw new NullPointerException();
        }

        public int characteristics() {
            return (this.side == 0 ? 64 : 0) | 1 | 4 | 16;
        }

        public final Comparator<? super K> getComparator() {
            return this.tree.comparator;
        }
    }

    static abstract class NavigableSubMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V>, Serializable {
        private static final long serialVersionUID = 2765629423043303731L;
        transient NavigableMap<K, V> descendingMapView;
        transient NavigableSubMap<K, V>.EntrySetView entrySetView;
        final boolean fromStart;
        final K hi;
        final boolean hiInclusive;
        final K lo;
        final boolean loInclusive;
        final TreeMap<K, V> m;
        transient KeySet<K> navigableKeySetView;
        final boolean toEnd;

        final class DescendingSubMapEntryIterator extends NavigableSubMap<K, V>.SubMapIterator<Map.Entry<K, V>> {
            DescendingSubMapEntryIterator(TreeMapEntry<K, V> last, TreeMapEntry<K, V> fence) {
                super(last, fence);
            }

            public Map.Entry<K, V> next() {
                return prevEntry();
            }

            public void remove() {
                removeDescending();
            }
        }

        final class DescendingSubMapKeyIterator extends NavigableSubMap<K, V>.SubMapIterator<K> implements Spliterator<K> {
            DescendingSubMapKeyIterator(TreeMapEntry<K, V> last, TreeMapEntry<K, V> fence) {
                super(last, fence);
            }

            public K next() {
                return prevEntry().key;
            }

            public void remove() {
                removeDescending();
            }

            public Spliterator<K> trySplit() {
                return null;
            }

            public void forEachRemaining(Consumer<? super K> action) {
                while (hasNext()) {
                    action.accept(next());
                }
            }

            public boolean tryAdvance(Consumer<? super K> action) {
                if (!hasNext()) {
                    return TreeMap.RED;
                }
                action.accept(next());
                return TreeMap.BLACK;
            }

            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            public int characteristics() {
                return 17;
            }
        }

        abstract class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
            private transient int size = -1;
            private transient int sizeModCount;

            EntrySetView() {
            }

            public int size() {
                if (NavigableSubMap.this.fromStart && NavigableSubMap.this.toEnd) {
                    return NavigableSubMap.this.m.size();
                }
                if (this.size == -1 || this.sizeModCount != NavigableSubMap.this.m.modCount) {
                    this.sizeModCount = NavigableSubMap.this.m.modCount;
                    this.size = 0;
                    Iterator<?> i = iterator();
                    while (i.hasNext()) {
                        this.size++;
                        i.next();
                    }
                }
                return this.size;
            }

            public boolean isEmpty() {
                TreeMapEntry<K, V> n = NavigableSubMap.this.absLowest();
                return (n == null || NavigableSubMap.this.tooHigh(n.key)) ? TreeMap.BLACK : TreeMap.RED;
            }

            public boolean contains(Object o) {
                boolean z = o instanceof Map.Entry;
                boolean z2 = TreeMap.RED;
                if (!z) {
                    return TreeMap.RED;
                }
                Map.Entry<?, ?> entry = (Map.Entry) o;
                Object key = entry.getKey();
                if (!NavigableSubMap.this.inRange(key)) {
                    return TreeMap.RED;
                }
                TreeMapEntry<K, V> entry2 = NavigableSubMap.this.m.getEntry(key);
                if (entry2 != null && TreeMap.valEquals(entry2.getValue(), entry.getValue())) {
                    z2 = TreeMap.BLACK;
                }
                return z2;
            }

            public boolean remove(Object o) {
                if (!(o instanceof Map.Entry)) {
                    return TreeMap.RED;
                }
                Map.Entry<?, ?> entry = (Map.Entry) o;
                Object key = entry.getKey();
                if (!NavigableSubMap.this.inRange(key)) {
                    return TreeMap.RED;
                }
                TreeMapEntry<K, V> node = NavigableSubMap.this.m.getEntry(key);
                if (node == null || !TreeMap.valEquals(node.getValue(), entry.getValue())) {
                    return TreeMap.RED;
                }
                NavigableSubMap.this.m.deleteEntry(node);
                return TreeMap.BLACK;
            }
        }

        final class SubMapEntryIterator extends NavigableSubMap<K, V>.SubMapIterator<Map.Entry<K, V>> {
            SubMapEntryIterator(TreeMapEntry<K, V> first, TreeMapEntry<K, V> fence) {
                super(first, fence);
            }

            public Map.Entry<K, V> next() {
                return nextEntry();
            }

            public void remove() {
                removeAscending();
            }
        }

        abstract class SubMapIterator<T> implements Iterator<T> {
            int expectedModCount;
            final Object fenceKey;
            TreeMapEntry<K, V> lastReturned = null;
            TreeMapEntry<K, V> next;

            SubMapIterator(TreeMapEntry<K, V> first, TreeMapEntry<K, V> fence) {
                this.expectedModCount = NavigableSubMap.this.m.modCount;
                this.next = first;
                this.fenceKey = fence == null ? TreeMap.UNBOUNDED : fence.key;
            }

            public final boolean hasNext() {
                return (this.next == null || this.next.key == this.fenceKey) ? TreeMap.RED : TreeMap.BLACK;
            }

            /* access modifiers changed from: package-private */
            public final TreeMapEntry<K, V> nextEntry() {
                TreeMapEntry<K, V> e = this.next;
                if (e == null || e.key == this.fenceKey) {
                    throw new NoSuchElementException();
                } else if (NavigableSubMap.this.m.modCount == this.expectedModCount) {
                    this.next = TreeMap.successor(e);
                    this.lastReturned = e;
                    return e;
                } else {
                    throw new ConcurrentModificationException();
                }
            }

            /* access modifiers changed from: package-private */
            public final TreeMapEntry<K, V> prevEntry() {
                TreeMapEntry<K, V> e = this.next;
                if (e == null || e.key == this.fenceKey) {
                    throw new NoSuchElementException();
                } else if (NavigableSubMap.this.m.modCount == this.expectedModCount) {
                    this.next = TreeMap.predecessor(e);
                    this.lastReturned = e;
                    return e;
                } else {
                    throw new ConcurrentModificationException();
                }
            }

            /* access modifiers changed from: package-private */
            public final void removeAscending() {
                if (this.lastReturned == null) {
                    throw new IllegalStateException();
                } else if (NavigableSubMap.this.m.modCount == this.expectedModCount) {
                    if (!(this.lastReturned.left == null || this.lastReturned.right == null)) {
                        this.next = this.lastReturned;
                    }
                    NavigableSubMap.this.m.deleteEntry(this.lastReturned);
                    this.lastReturned = null;
                    this.expectedModCount = NavigableSubMap.this.m.modCount;
                } else {
                    throw new ConcurrentModificationException();
                }
            }

            /* access modifiers changed from: package-private */
            public final void removeDescending() {
                if (this.lastReturned == null) {
                    throw new IllegalStateException();
                } else if (NavigableSubMap.this.m.modCount == this.expectedModCount) {
                    NavigableSubMap.this.m.deleteEntry(this.lastReturned);
                    this.lastReturned = null;
                    this.expectedModCount = NavigableSubMap.this.m.modCount;
                } else {
                    throw new ConcurrentModificationException();
                }
            }
        }

        final class SubMapKeyIterator extends NavigableSubMap<K, V>.SubMapIterator<K> implements Spliterator<K> {
            SubMapKeyIterator(TreeMapEntry<K, V> first, TreeMapEntry<K, V> fence) {
                super(first, fence);
            }

            public K next() {
                return nextEntry().key;
            }

            public void remove() {
                removeAscending();
            }

            public Spliterator<K> trySplit() {
                return null;
            }

            public void forEachRemaining(Consumer<? super K> action) {
                while (hasNext()) {
                    action.accept(next());
                }
            }

            public boolean tryAdvance(Consumer<? super K> action) {
                if (!hasNext()) {
                    return TreeMap.RED;
                }
                action.accept(next());
                return TreeMap.BLACK;
            }

            public long estimateSize() {
                return Long.MAX_VALUE;
            }

            public int characteristics() {
                return 21;
            }

            public final Comparator<? super K> getComparator() {
                return NavigableSubMap.this.comparator();
            }
        }

        /* access modifiers changed from: package-private */
        public abstract Iterator<K> descendingKeyIterator();

        /* access modifiers changed from: package-private */
        public abstract Iterator<K> keyIterator();

        /* access modifiers changed from: package-private */
        public abstract Spliterator<K> keySpliterator();

        /* access modifiers changed from: package-private */
        public abstract TreeMapEntry<K, V> subCeiling(K k);

        /* access modifiers changed from: package-private */
        public abstract TreeMapEntry<K, V> subFloor(K k);

        /* access modifiers changed from: package-private */
        public abstract TreeMapEntry<K, V> subHigher(K k);

        /* access modifiers changed from: package-private */
        public abstract TreeMapEntry<K, V> subHighest();

        /* access modifiers changed from: package-private */
        public abstract TreeMapEntry<K, V> subLower(K k);

        /* access modifiers changed from: package-private */
        public abstract TreeMapEntry<K, V> subLowest();

        NavigableSubMap(TreeMap<K, V> m2, boolean fromStart2, K lo2, boolean loInclusive2, boolean toEnd2, K hi2, boolean hiInclusive2) {
            if (fromStart2 || toEnd2) {
                if (!fromStart2) {
                    m2.compare(lo2, lo2);
                }
                if (!toEnd2) {
                    m2.compare(hi2, hi2);
                }
            } else if (m2.compare(lo2, hi2) > 0) {
                throw new IllegalArgumentException("fromKey > toKey");
            }
            this.m = m2;
            this.fromStart = fromStart2;
            this.lo = lo2;
            this.loInclusive = loInclusive2;
            this.toEnd = toEnd2;
            this.hi = hi2;
            this.hiInclusive = hiInclusive2;
        }

        /* access modifiers changed from: package-private */
        public final boolean tooLow(Object key) {
            if (!this.fromStart) {
                int c = this.m.compare(key, this.lo);
                if (c < 0 || (c == 0 && !this.loInclusive)) {
                    return TreeMap.BLACK;
                }
            }
            return TreeMap.RED;
        }

        /* access modifiers changed from: package-private */
        public final boolean tooHigh(Object key) {
            if (!this.toEnd) {
                int c = this.m.compare(key, this.hi);
                if (c > 0 || (c == 0 && !this.hiInclusive)) {
                    return TreeMap.BLACK;
                }
            }
            return TreeMap.RED;
        }

        /* access modifiers changed from: package-private */
        public final boolean inRange(Object key) {
            return (tooLow(key) || tooHigh(key)) ? TreeMap.RED : TreeMap.BLACK;
        }

        /* access modifiers changed from: package-private */
        public final boolean inClosedRange(Object key) {
            return ((this.fromStart || this.m.compare(key, this.lo) >= 0) && (this.toEnd || this.m.compare(this.hi, key) >= 0)) ? TreeMap.BLACK : TreeMap.RED;
        }

        /* access modifiers changed from: package-private */
        public final boolean inRange(Object key, boolean inclusive) {
            return inclusive ? inRange(key) : inClosedRange(key);
        }

        /* access modifiers changed from: package-private */
        public final TreeMapEntry<K, V> absLowest() {
            TreeMapEntry<K, V> e;
            if (this.fromStart) {
                e = this.m.getFirstEntry();
            } else if (this.loInclusive) {
                e = this.m.getCeilingEntry(this.lo);
            } else {
                e = this.m.getHigherEntry(this.lo);
            }
            if (e == null || tooHigh(e.key)) {
                return null;
            }
            return e;
        }

        /* access modifiers changed from: package-private */
        public final TreeMapEntry<K, V> absHighest() {
            TreeMapEntry<K, V> e;
            if (this.toEnd) {
                e = this.m.getLastEntry();
            } else if (this.hiInclusive) {
                e = this.m.getFloorEntry(this.hi);
            } else {
                e = this.m.getLowerEntry(this.hi);
            }
            if (e == null || tooLow(e.key)) {
                return null;
            }
            return e;
        }

        /* access modifiers changed from: package-private */
        public final TreeMapEntry<K, V> absCeiling(K key) {
            if (tooLow(key)) {
                return absLowest();
            }
            TreeMapEntry<K, V> e = this.m.getCeilingEntry(key);
            return (e == null || tooHigh(e.key)) ? null : e;
        }

        /* access modifiers changed from: package-private */
        public final TreeMapEntry<K, V> absHigher(K key) {
            if (tooLow(key)) {
                return absLowest();
            }
            TreeMapEntry<K, V> e = this.m.getHigherEntry(key);
            return (e == null || tooHigh(e.key)) ? null : e;
        }

        /* access modifiers changed from: package-private */
        public final TreeMapEntry<K, V> absFloor(K key) {
            if (tooHigh(key)) {
                return absHighest();
            }
            TreeMapEntry<K, V> e = this.m.getFloorEntry(key);
            return (e == null || tooLow(e.key)) ? null : e;
        }

        /* access modifiers changed from: package-private */
        public final TreeMapEntry<K, V> absLower(K key) {
            if (tooHigh(key)) {
                return absHighest();
            }
            TreeMapEntry<K, V> e = this.m.getLowerEntry(key);
            return (e == null || tooLow(e.key)) ? null : e;
        }

        /* access modifiers changed from: package-private */
        public final TreeMapEntry<K, V> absHighFence() {
            if (this.toEnd) {
                return null;
            }
            if (this.hiInclusive) {
                return this.m.getHigherEntry(this.hi);
            }
            return this.m.getCeilingEntry(this.hi);
        }

        /* access modifiers changed from: package-private */
        public final TreeMapEntry<K, V> absLowFence() {
            if (this.fromStart) {
                return null;
            }
            if (this.loInclusive) {
                return this.m.getLowerEntry(this.lo);
            }
            return this.m.getFloorEntry(this.lo);
        }

        public boolean isEmpty() {
            return (!this.fromStart || !this.toEnd) ? entrySet().isEmpty() : this.m.isEmpty();
        }

        public int size() {
            return (!this.fromStart || !this.toEnd) ? entrySet().size() : this.m.size();
        }

        public final boolean containsKey(Object key) {
            return (!inRange(key) || !this.m.containsKey(key)) ? TreeMap.RED : TreeMap.BLACK;
        }

        public final V put(K key, V value) {
            if (inRange(key)) {
                return this.m.put(key, value);
            }
            throw new IllegalArgumentException("key out of range");
        }

        public final V get(Object key) {
            if (!inRange(key)) {
                return null;
            }
            return this.m.get(key);
        }

        public final V remove(Object key) {
            if (!inRange(key)) {
                return null;
            }
            return this.m.remove(key);
        }

        public final Map.Entry<K, V> ceilingEntry(K key) {
            return TreeMap.exportEntry(subCeiling(key));
        }

        public final K ceilingKey(K key) {
            return TreeMap.keyOrNull(subCeiling(key));
        }

        public final Map.Entry<K, V> higherEntry(K key) {
            return TreeMap.exportEntry(subHigher(key));
        }

        public final K higherKey(K key) {
            return TreeMap.keyOrNull(subHigher(key));
        }

        public final Map.Entry<K, V> floorEntry(K key) {
            return TreeMap.exportEntry(subFloor(key));
        }

        public final K floorKey(K key) {
            return TreeMap.keyOrNull(subFloor(key));
        }

        public final Map.Entry<K, V> lowerEntry(K key) {
            return TreeMap.exportEntry(subLower(key));
        }

        public final K lowerKey(K key) {
            return TreeMap.keyOrNull(subLower(key));
        }

        public final K firstKey() {
            return TreeMap.key(subLowest());
        }

        public final K lastKey() {
            return TreeMap.key(subHighest());
        }

        public final Map.Entry<K, V> firstEntry() {
            return TreeMap.exportEntry(subLowest());
        }

        public final Map.Entry<K, V> lastEntry() {
            return TreeMap.exportEntry(subHighest());
        }

        public final Map.Entry<K, V> pollFirstEntry() {
            TreeMapEntry<K, V> e = subLowest();
            Map.Entry<K, V> result = TreeMap.exportEntry(e);
            if (e != null) {
                this.m.deleteEntry(e);
            }
            return result;
        }

        public final Map.Entry<K, V> pollLastEntry() {
            TreeMapEntry<K, V> e = subHighest();
            Map.Entry<K, V> result = TreeMap.exportEntry(e);
            if (e != null) {
                this.m.deleteEntry(e);
            }
            return result;
        }

        public final NavigableSet<K> navigableKeySet() {
            KeySet<K> nksv = this.navigableKeySetView;
            if (nksv != null) {
                return nksv;
            }
            KeySet<K> keySet = new KeySet<>(this);
            this.navigableKeySetView = keySet;
            return keySet;
        }

        public final Set<K> keySet() {
            return navigableKeySet();
        }

        public NavigableSet<K> descendingKeySet() {
            return descendingMap().navigableKeySet();
        }

        public final SortedMap<K, V> subMap(K fromKey, K toKey) {
            return subMap(fromKey, TreeMap.BLACK, toKey, TreeMap.RED);
        }

        public final SortedMap<K, V> headMap(K toKey) {
            return headMap(toKey, TreeMap.RED);
        }

        public final SortedMap<K, V> tailMap(K fromKey) {
            return tailMap(fromKey, TreeMap.BLACK);
        }
    }

    abstract class PrivateEntryIterator<T> implements Iterator<T> {
        int expectedModCount;
        TreeMapEntry<K, V> lastReturned = null;
        TreeMapEntry<K, V> next;

        PrivateEntryIterator(TreeMapEntry<K, V> first) {
            this.expectedModCount = TreeMap.this.modCount;
            this.next = first;
        }

        public final boolean hasNext() {
            return this.next != null ? TreeMap.BLACK : TreeMap.RED;
        }

        /* access modifiers changed from: package-private */
        public final TreeMapEntry<K, V> nextEntry() {
            TreeMapEntry<K, V> e = this.next;
            if (e == null) {
                throw new NoSuchElementException();
            } else if (TreeMap.this.modCount == this.expectedModCount) {
                this.next = TreeMap.successor(e);
                this.lastReturned = e;
                return e;
            } else {
                throw new ConcurrentModificationException();
            }
        }

        /* access modifiers changed from: package-private */
        public final TreeMapEntry<K, V> prevEntry() {
            TreeMapEntry<K, V> e = this.next;
            if (e == null) {
                throw new NoSuchElementException();
            } else if (TreeMap.this.modCount == this.expectedModCount) {
                this.next = TreeMap.predecessor(e);
                this.lastReturned = e;
                return e;
            } else {
                throw new ConcurrentModificationException();
            }
        }

        public void remove() {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            } else if (TreeMap.this.modCount == this.expectedModCount) {
                if (!(this.lastReturned.left == null || this.lastReturned.right == null)) {
                    this.next = this.lastReturned;
                }
                TreeMap.this.deleteEntry(this.lastReturned);
                this.expectedModCount = TreeMap.this.modCount;
                this.lastReturned = null;
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    private class SubMap extends AbstractMap<K, V> implements SortedMap<K, V>, Serializable {
        private static final long serialVersionUID = -6520786458950516097L;
        private K fromKey;
        private boolean fromStart = TreeMap.RED;
        private boolean toEnd = TreeMap.RED;
        private K toKey;

        private SubMap() {
        }

        private Object readResolve() {
            AscendingSubMap ascendingSubMap = new AscendingSubMap(TreeMap.this, this.fromStart, this.fromKey, TreeMap.BLACK, this.toEnd, this.toKey, TreeMap.RED);
            return ascendingSubMap;
        }

        public Set<Map.Entry<K, V>> entrySet() {
            throw new InternalError();
        }

        public K lastKey() {
            throw new InternalError();
        }

        public K firstKey() {
            throw new InternalError();
        }

        public SortedMap<K, V> subMap(K k, K k2) {
            throw new InternalError();
        }

        public SortedMap<K, V> headMap(K k) {
            throw new InternalError();
        }

        public SortedMap<K, V> tailMap(K k) {
            throw new InternalError();
        }

        public Comparator<? super K> comparator() {
            throw new InternalError();
        }
    }

    static final class TreeMapEntry<K, V> implements Map.Entry<K, V> {
        boolean color = TreeMap.BLACK;
        K key;
        TreeMapEntry<K, V> left;
        @RCUnownedRef
        TreeMapEntry<K, V> parent;
        TreeMapEntry<K, V> right;
        V value;

        TreeMapEntry(K key2, V value2, TreeMapEntry<K, V> parent2) {
            this.key = key2;
            this.value = value2;
            this.parent = parent2;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public V setValue(V value2) {
            V oldValue = this.value;
            this.value = value2;
            return oldValue;
        }

        public boolean equals(Object o) {
            boolean z = o instanceof Map.Entry;
            boolean z2 = TreeMap.RED;
            if (!z) {
                return TreeMap.RED;
            }
            Map.Entry<?, ?> e = (Map.Entry) o;
            if (TreeMap.valEquals(this.key, e.getKey()) && TreeMap.valEquals(this.value, e.getValue())) {
                z2 = TreeMap.BLACK;
            }
            return z2;
        }

        public int hashCode() {
            int valueHash = 0;
            int keyHash = this.key == null ? 0 : this.key.hashCode();
            if (this.value != null) {
                valueHash = this.value.hashCode();
            }
            return keyHash ^ valueHash;
        }

        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    static class TreeMapSpliterator<K, V> {
        TreeMapEntry<K, V> current;
        int est;
        int expectedModCount;
        TreeMapEntry<K, V> fence;
        int side;
        final TreeMap<K, V> tree;

        TreeMapSpliterator(TreeMap<K, V> tree2, TreeMapEntry<K, V> origin, TreeMapEntry<K, V> fence2, int side2, int est2, int expectedModCount2) {
            this.tree = tree2;
            this.current = origin;
            this.fence = fence2;
            this.side = side2;
            this.est = est2;
            this.expectedModCount = expectedModCount2;
        }

        /* access modifiers changed from: package-private */
        public final int getEstimate() {
            int i = this.est;
            int s = i;
            if (i >= 0) {
                return s;
            }
            TreeMap<K, V> treeMap = this.tree;
            TreeMap<K, V> t = treeMap;
            if (treeMap != null) {
                this.current = s == -1 ? t.getFirstEntry() : t.getLastEntry();
                int access$400 = t.size;
                this.est = access$400;
                int s2 = access$400;
                this.expectedModCount = t.modCount;
                return s2;
            }
            this.est = 0;
            return 0;
        }

        public final long estimateSize() {
            return (long) getEstimate();
        }
    }

    final class ValueIterator extends TreeMap<K, V>.PrivateEntryIterator<V> {
        ValueIterator(TreeMapEntry<K, V> first) {
            super(first);
        }

        public V next() {
            return nextEntry().value;
        }
    }

    static final class ValueSpliterator<K, V> extends TreeMapSpliterator<K, V> implements Spliterator<V> {
        ValueSpliterator(TreeMap<K, V> tree, TreeMapEntry<K, V> origin, TreeMapEntry<K, V> fence, int side, int est, int expectedModCount) {
            super(tree, origin, fence, side, est, expectedModCount);
        }

        public ValueSpliterator<K, V> trySplit() {
            TreeMapEntry<K, V> treeMapEntry;
            TreeMapEntry<K, V> s;
            if (this.est < 0) {
                getEstimate();
            }
            int d = this.side;
            TreeMapEntry<K, V> e = this.current;
            TreeMapEntry<K, V> f = this.fence;
            if (!(e == null || e == f)) {
                if (d == 0) {
                    treeMapEntry = this.tree.root;
                } else if (d > 0) {
                    treeMapEntry = e.right;
                } else if (d < 0 && f != null) {
                    treeMapEntry = f.left;
                }
                s = treeMapEntry;
                if (s != null || s == e || s == f || this.tree.compare(e.key, s.key) >= 0) {
                    return null;
                }
                this.side = 1;
                TreeMap treeMap = this.tree;
                this.current = s;
                int i = this.est >>> 1;
                this.est = i;
                ValueSpliterator valueSpliterator = new ValueSpliterator(treeMap, e, s, -1, i, this.expectedModCount);
                return valueSpliterator;
            }
            treeMapEntry = null;
            s = treeMapEntry;
            if (s != null) {
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action != null) {
                if (this.est < 0) {
                    getEstimate();
                }
                TreeMapEntry<K, V> f = this.fence;
                TreeMapEntry<K, V> treeMapEntry = this.current;
                TreeMapEntry<K, V> e = treeMapEntry;
                if (treeMapEntry != null && e != f) {
                    this.current = f;
                    do {
                        action.accept(e.value);
                        TreeMapEntry<K, V> treeMapEntry2 = e.right;
                        TreeMapEntry<K, V> p = treeMapEntry2;
                        if (treeMapEntry2 == null) {
                            while (true) {
                                TreeMapEntry<K, V> treeMapEntry3 = e.parent;
                                p = treeMapEntry3;
                                if (treeMapEntry3 == null || e != p.right) {
                                    break;
                                }
                                e = p;
                            }
                        } else {
                            while (true) {
                                TreeMapEntry<K, V> treeMapEntry4 = p.left;
                                TreeMapEntry<K, V> pl = treeMapEntry4;
                                if (treeMapEntry4 == null) {
                                    break;
                                }
                                p = pl;
                            }
                        }
                        e = p;
                        if (p == null) {
                            break;
                        }
                    } while (e != f);
                    if (this.tree.modCount != this.expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action != null) {
                if (this.est < 0) {
                    getEstimate();
                }
                TreeMapEntry<K, V> treeMapEntry = this.current;
                TreeMapEntry<K, V> e = treeMapEntry;
                if (treeMapEntry == null || e == this.fence) {
                    return TreeMap.RED;
                }
                this.current = TreeMap.successor(e);
                action.accept(e.value);
                if (this.tree.modCount == this.expectedModCount) {
                    return TreeMap.BLACK;
                }
                throw new ConcurrentModificationException();
            }
            throw new NullPointerException();
        }

        public int characteristics() {
            return (this.side == 0 ? 64 : 0) | 16;
        }
    }

    class Values extends AbstractCollection<V> {
        Values() {
        }

        public Iterator<V> iterator() {
            return new ValueIterator(TreeMap.this.getFirstEntry());
        }

        public int size() {
            return TreeMap.this.size();
        }

        public boolean contains(Object o) {
            return TreeMap.this.containsValue(o);
        }

        public boolean remove(Object o) {
            for (TreeMapEntry<K, V> e = TreeMap.this.getFirstEntry(); e != null; e = TreeMap.successor(e)) {
                if (TreeMap.valEquals(e.getValue(), o)) {
                    TreeMap.this.deleteEntry(e);
                    return TreeMap.BLACK;
                }
            }
            return TreeMap.RED;
        }

        public void clear() {
            TreeMap.this.clear();
        }

        public Spliterator<V> spliterator() {
            ValueSpliterator valueSpliterator = new ValueSpliterator(TreeMap.this, null, null, 0, -1, 0);
            return valueSpliterator;
        }
    }

    public TreeMap() {
        this.size = 0;
        this.modCount = 0;
        this.comparator = null;
    }

    public TreeMap(Comparator<? super K> comparator2) {
        this.size = 0;
        this.modCount = 0;
        this.comparator = comparator2;
    }

    public TreeMap(Map<? extends K, ? extends V> m) {
        this.size = 0;
        this.modCount = 0;
        this.comparator = null;
        putAll(m);
    }

    public TreeMap(SortedMap<K, ? extends V> m) {
        this.size = 0;
        this.modCount = 0;
        this.comparator = m.comparator();
        try {
            buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
        } catch (IOException | ClassNotFoundException e) {
        }
    }

    public int size() {
        return this.size;
    }

    public boolean containsKey(Object key) {
        return getEntry(key) != null ? BLACK : RED;
    }

    public boolean containsValue(Object value) {
        for (TreeMapEntry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
            if (valEquals(value, e.value)) {
                return BLACK;
            }
        }
        return RED;
    }

    public V get(Object key) {
        TreeMapEntry<K, V> p = getEntry(key);
        if (p == null) {
            return null;
        }
        return p.value;
    }

    public Comparator<? super K> comparator() {
        return this.comparator;
    }

    public K firstKey() {
        return key(getFirstEntry());
    }

    public K lastKey() {
        return key(getLastEntry());
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        int mapSize = map.size();
        if (this.size == 0 && mapSize != 0 && (map instanceof SortedMap)) {
            Comparator<?> c = ((SortedMap) map).comparator();
            if (c == this.comparator || (c != null && c.equals(this.comparator))) {
                this.modCount++;
                try {
                    buildFromSorted(mapSize, map.entrySet().iterator(), null, null);
                } catch (IOException | ClassNotFoundException e) {
                }
                return;
            }
        }
        super.putAll(map);
    }

    /* access modifiers changed from: package-private */
    public final TreeMapEntry<K, V> getEntry(Object key) {
        if (this.comparator != null) {
            return getEntryUsingComparator(key);
        }
        if (key != null) {
            Comparable<? super K> k = (Comparable) key;
            TreeMapEntry<K, V> p = this.root;
            while (p != null) {
                int cmp = k.compareTo(p.key);
                if (cmp < 0) {
                    p = p.left;
                } else if (cmp <= 0) {
                    return p;
                } else {
                    p = p.right;
                }
            }
            return null;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public final TreeMapEntry<K, V> getEntryUsingComparator(Object key) {
        Object obj = key;
        Comparator<? super K> cpr = this.comparator;
        if (cpr != null) {
            TreeMapEntry<K, V> p = this.root;
            while (p != null) {
                int cmp = cpr.compare(obj, p.key);
                if (cmp < 0) {
                    p = p.left;
                } else if (cmp <= 0) {
                    return p;
                } else {
                    p = p.right;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final TreeMapEntry<K, V> getCeilingEntry(K key) {
        TreeMapEntry<K, V> p = this.root;
        while (p != null) {
            int cmp = compare(key, p.key);
            if (cmp < 0) {
                if (p.left == null) {
                    return p;
                }
                p = p.left;
            } else if (cmp <= 0) {
                return p;
            } else {
                if (p.right != null) {
                    p = p.right;
                } else {
                    TreeMapEntry<K, V> parent = p.parent;
                    TreeMapEntry<K, V> ch = p;
                    while (parent != null && ch == parent.right) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final TreeMapEntry<K, V> getFloorEntry(K key) {
        TreeMapEntry<K, V> p = this.root;
        while (p != null) {
            int cmp = compare(key, p.key);
            if (cmp > 0) {
                if (p.right == null) {
                    return p;
                }
                p = p.right;
            } else if (cmp >= 0) {
                return p;
            } else {
                if (p.left != null) {
                    p = p.left;
                } else {
                    TreeMapEntry<K, V> parent = p.parent;
                    TreeMapEntry<K, V> ch = p;
                    while (parent != null && ch == parent.left) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final TreeMapEntry<K, V> getHigherEntry(K key) {
        TreeMapEntry<K, V> p = this.root;
        while (p != null) {
            if (compare(key, p.key) < 0) {
                if (p.left == null) {
                    return p;
                }
                p = p.left;
            } else if (p.right != null) {
                p = p.right;
            } else {
                TreeMapEntry<K, V> parent = p.parent;
                TreeMapEntry<K, V> ch = p;
                while (parent != null && ch == parent.right) {
                    ch = parent;
                    parent = parent.parent;
                }
                return parent;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public final TreeMapEntry<K, V> getLowerEntry(K key) {
        TreeMapEntry<K, V> p = this.root;
        while (p != null) {
            if (compare(key, p.key) > 0) {
                if (p.right == null) {
                    return p;
                }
                p = p.right;
            } else if (p.left != null) {
                p = p.left;
            } else {
                TreeMapEntry<K, V> parent = p.parent;
                TreeMapEntry<K, V> ch = p;
                while (parent != null && ch == parent.left) {
                    ch = parent;
                    parent = parent.parent;
                }
                return parent;
            }
        }
        return null;
    }

    public V put(K key, V value) {
        int cmp;
        TreeMapEntry<K, V> parent;
        TreeMapEntry<K, V> parent2;
        int cmp2;
        TreeMapEntry<K, V> t;
        TreeMapEntry<K, V> t2 = this.root;
        if (t2 == null) {
            compare(key, key);
            this.root = new TreeMapEntry<>(key, value, null);
            this.size = 1;
            this.modCount++;
            return null;
        }
        Comparator<? super K> cpr = this.comparator;
        if (cpr != null) {
            do {
                parent = t2;
                cmp = cpr.compare(key, t2.key);
                if (cmp < 0) {
                    t2 = t2.left;
                    continue;
                } else if (cmp <= 0) {
                    return t2.setValue(value);
                } else {
                    t2 = t2.right;
                    continue;
                }
            } while (t2 != null);
        } else if (key != null) {
            Comparable<? super K> k = key;
            do {
                parent2 = t2;
                cmp2 = k.compareTo(t2.key);
                if (cmp2 < 0) {
                    t = t2.left;
                    continue;
                } else if (cmp2 <= 0) {
                    return t2.setValue(value);
                } else {
                    t = t2.right;
                    continue;
                }
            } while (t2 != null);
            parent = parent2;
            cmp = cmp2;
        } else {
            throw new NullPointerException();
        }
        TreeMapEntry<K, V> e = new TreeMapEntry<>(key, value, parent);
        if (cmp < 0) {
            parent.left = e;
        } else {
            parent.right = e;
        }
        fixAfterInsertion(e);
        this.size++;
        this.modCount++;
        return null;
    }

    public V remove(Object key) {
        TreeMapEntry<K, V> p = getEntry(key);
        if (p == null) {
            return null;
        }
        V oldValue = p.value;
        deleteEntry(p);
        return oldValue;
    }

    public void clear() {
        this.modCount++;
        this.size = 0;
        this.root = null;
    }

    public Object clone() {
        try {
            TreeMap<?, ?> clone = (TreeMap) super.clone();
            clone.root = null;
            clone.size = 0;
            clone.modCount = 0;
            clone.entrySet = null;
            clone.navigableKeySet = null;
            clone.descendingMap = null;
            try {
                clone.buildFromSorted(this.size, entrySet().iterator(), null, null);
            } catch (IOException | ClassNotFoundException e) {
            }
            return clone;
        } catch (CloneNotSupportedException e2) {
            throw new InternalError((Throwable) e2);
        }
    }

    public Map.Entry<K, V> firstEntry() {
        return exportEntry(getFirstEntry());
    }

    public Map.Entry<K, V> lastEntry() {
        return exportEntry(getLastEntry());
    }

    public Map.Entry<K, V> pollFirstEntry() {
        TreeMapEntry<K, V> p = getFirstEntry();
        Map.Entry<K, V> result = exportEntry(p);
        if (p != null) {
            deleteEntry(p);
        }
        return result;
    }

    public Map.Entry<K, V> pollLastEntry() {
        TreeMapEntry<K, V> p = getLastEntry();
        Map.Entry<K, V> result = exportEntry(p);
        if (p != null) {
            deleteEntry(p);
        }
        return result;
    }

    public Map.Entry<K, V> lowerEntry(K key) {
        return exportEntry(getLowerEntry(key));
    }

    public K lowerKey(K key) {
        return keyOrNull(getLowerEntry(key));
    }

    public Map.Entry<K, V> floorEntry(K key) {
        return exportEntry(getFloorEntry(key));
    }

    public K floorKey(K key) {
        return keyOrNull(getFloorEntry(key));
    }

    public Map.Entry<K, V> ceilingEntry(K key) {
        return exportEntry(getCeilingEntry(key));
    }

    public K ceilingKey(K key) {
        return keyOrNull(getCeilingEntry(key));
    }

    public Map.Entry<K, V> higherEntry(K key) {
        return exportEntry(getHigherEntry(key));
    }

    public K higherKey(K key) {
        return keyOrNull(getHigherEntry(key));
    }

    public Set<K> keySet() {
        return navigableKeySet();
    }

    public NavigableSet<K> navigableKeySet() {
        KeySet<K> nks = this.navigableKeySet;
        if (nks != null) {
            return nks;
        }
        KeySet<K> keySet = new KeySet<>(this);
        this.navigableKeySet = keySet;
        return keySet;
    }

    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
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
        TreeMap<K, V>.EntrySet es = this.entrySet;
        if (es != null) {
            return es;
        }
        TreeMap<K, V>.EntrySet entrySet2 = new EntrySet();
        this.entrySet = entrySet2;
        return entrySet2;
    }

    public NavigableMap<K, V> descendingMap() {
        NavigableMap<K, V> km = this.descendingMap;
        if (km != null) {
            return km;
        }
        DescendingSubMap descendingSubMap = new DescendingSubMap(this, BLACK, null, BLACK, BLACK, null, BLACK);
        this.descendingMap = descendingSubMap;
        return descendingSubMap;
    }

    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        AscendingSubMap ascendingSubMap = new AscendingSubMap(this, RED, fromKey, fromInclusive, RED, toKey, toInclusive);
        return ascendingSubMap;
    }

    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        AscendingSubMap ascendingSubMap = new AscendingSubMap(this, BLACK, null, BLACK, RED, toKey, inclusive);
        return ascendingSubMap;
    }

    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        AscendingSubMap ascendingSubMap = new AscendingSubMap(this, RED, fromKey, inclusive, BLACK, null, BLACK);
        return ascendingSubMap;
    }

    public SortedMap<K, V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, BLACK, toKey, RED);
    }

    public SortedMap<K, V> headMap(K toKey) {
        return headMap(toKey, RED);
    }

    public SortedMap<K, V> tailMap(K fromKey) {
        return tailMap(fromKey, BLACK);
    }

    public boolean replace(K key, V oldValue, V newValue) {
        TreeMapEntry<K, V> p = getEntry(key);
        if (p == null || !Objects.equals(oldValue, p.value)) {
            return RED;
        }
        p.value = newValue;
        return BLACK;
    }

    public V replace(K key, V value) {
        TreeMapEntry<K, V> p = getEntry(key);
        if (p == null) {
            return null;
        }
        V oldValue = p.value;
        p.value = value;
        return oldValue;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = this.modCount;
        TreeMapEntry<K, V> e = getFirstEntry();
        while (e != null) {
            action.accept(e.key, e.value);
            if (expectedModCount == this.modCount) {
                e = successor(e);
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        int expectedModCount = this.modCount;
        TreeMapEntry<K, V> e = getFirstEntry();
        while (e != null) {
            e.value = function.apply(e.key, e.value);
            if (expectedModCount == this.modCount) {
                e = successor(e);
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Iterator<K> keyIterator() {
        return new KeyIterator(getFirstEntry());
    }

    /* access modifiers changed from: package-private */
    public Iterator<K> descendingKeyIterator() {
        return new DescendingKeyIterator(getLastEntry());
    }

    /* access modifiers changed from: package-private */
    public final int compare(Object k1, Object k2) {
        if (this.comparator == null) {
            return ((Comparable) k1).compareTo(k2);
        }
        return this.comparator.compare(k1, k2);
    }

    static final boolean valEquals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null ? BLACK : RED;
        }
        return o1.equals(o2);
    }

    static <K, V> Map.Entry<K, V> exportEntry(TreeMapEntry<K, V> e) {
        if (e == null) {
            return null;
        }
        return new AbstractMap.SimpleImmutableEntry(e);
    }

    static <K, V> K keyOrNull(TreeMapEntry<K, V> e) {
        if (e == null) {
            return null;
        }
        return e.key;
    }

    static <K> K key(TreeMapEntry<K, ?> e) {
        if (e != null) {
            return e.key;
        }
        throw new NoSuchElementException();
    }

    /* access modifiers changed from: package-private */
    public final TreeMapEntry<K, V> getFirstEntry() {
        TreeMapEntry<K, V> p = this.root;
        if (p != null) {
            while (p.left != null) {
                p = p.left;
            }
        }
        return p;
    }

    /* access modifiers changed from: package-private */
    public final TreeMapEntry<K, V> getLastEntry() {
        TreeMapEntry<K, V> p = this.root;
        if (p != null) {
            while (p.right != null) {
                p = p.right;
            }
        }
        return p;
    }

    static <K, V> TreeMapEntry<K, V> successor(TreeMapEntry<K, V> t) {
        if (t == null) {
            return null;
        }
        if (t.right != null) {
            TreeMapEntry<K, V> p = t.right;
            while (p.left != null) {
                p = p.left;
            }
            return p;
        }
        TreeMapEntry<K, V> p2 = t.parent;
        TreeMapEntry<K, V> ch = t;
        while (p2 != null && ch == p2.right) {
            ch = p2;
            p2 = p2.parent;
        }
        return p2;
    }

    static <K, V> TreeMapEntry<K, V> predecessor(TreeMapEntry<K, V> t) {
        if (t == null) {
            return null;
        }
        if (t.left != null) {
            TreeMapEntry<K, V> p = t.left;
            while (p.right != null) {
                p = p.right;
            }
            return p;
        }
        TreeMapEntry<K, V> p2 = t.parent;
        TreeMapEntry<K, V> ch = t;
        while (p2 != null && ch == p2.left) {
            ch = p2;
            p2 = p2.parent;
        }
        return p2;
    }

    private static <K, V> boolean colorOf(TreeMapEntry<K, V> p) {
        return p == null ? BLACK : p.color;
    }

    private static <K, V> TreeMapEntry<K, V> parentOf(TreeMapEntry<K, V> p) {
        if (p == null) {
            return null;
        }
        return p.parent;
    }

    private static <K, V> void setColor(TreeMapEntry<K, V> p, boolean c) {
        if (p != null) {
            p.color = c;
        }
    }

    private static <K, V> TreeMapEntry<K, V> leftOf(TreeMapEntry<K, V> p) {
        if (p == null) {
            return null;
        }
        return p.left;
    }

    private static <K, V> TreeMapEntry<K, V> rightOf(TreeMapEntry<K, V> p) {
        if (p == null) {
            return null;
        }
        return p.right;
    }

    private void rotateLeft(TreeMapEntry<K, V> p) {
        if (p != null) {
            TreeMapEntry<K, V> r = p.right;
            p.right = r.left;
            if (r.left != null) {
                r.left.parent = p;
            }
            r.parent = p.parent;
            if (p.parent == null) {
                this.root = r;
            } else if (p.parent.left == p) {
                p.parent.left = r;
            } else {
                p.parent.right = r;
            }
            r.left = p;
            p.parent = r;
        }
    }

    private void rotateRight(TreeMapEntry<K, V> p) {
        if (p != null) {
            TreeMapEntry<K, V> l = p.left;
            p.left = l.right;
            if (l.right != null) {
                l.right.parent = p;
            }
            l.parent = p.parent;
            if (p.parent == null) {
                this.root = l;
            } else if (p.parent.right == p) {
                p.parent.right = l;
            } else {
                p.parent.left = l;
            }
            l.right = p;
            p.parent = l;
        }
    }

    private void fixAfterInsertion(TreeMapEntry<K, V> x) {
        x.color = RED;
        while (x != null && x != this.root && !x.parent.color) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                TreeMapEntry<K, V> y = rightOf(parentOf(parentOf(x)));
                if (!colorOf(y)) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
                TreeMapEntry<K, V> y2 = leftOf(parentOf(parentOf(x)));
                if (!colorOf(y2)) {
                    setColor(parentOf(x), BLACK);
                    setColor(y2, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        this.root.color = BLACK;
    }

    /* access modifiers changed from: private */
    public void deleteEntry(TreeMapEntry<K, V> p) {
        this.modCount++;
        this.size--;
        if (!(p.left == null || p.right == null)) {
            TreeMapEntry<K, V> s = successor(p);
            p.key = s.key;
            p.value = s.value;
            p = s;
        }
        TreeMapEntry<K, V> replacement = p.left != null ? p.left : p.right;
        if (replacement != null) {
            replacement.parent = p.parent;
            if (p.parent == null) {
                this.root = replacement;
            } else if (p == p.parent.left) {
                p.parent.left = replacement;
            } else {
                p.parent.right = replacement;
            }
            p.parent = null;
            p.right = null;
            p.left = null;
            if (p.color) {
                fixAfterDeletion(replacement);
            }
        } else if (p.parent == null) {
            this.root = null;
        } else {
            if (p.color) {
                fixAfterDeletion(p);
            }
            if (p.parent != null) {
                if (p == p.parent.left) {
                    p.parent.left = null;
                } else if (p == p.parent.right) {
                    p.parent.right = null;
                }
                p.parent = null;
            }
        }
    }

    private void fixAfterDeletion(TreeMapEntry<K, V> x) {
        while (x != this.root && colorOf(x)) {
            if (x == leftOf(parentOf(x))) {
                TreeMapEntry<K, V> sib = rightOf(parentOf(x));
                if (!colorOf(sib)) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }
                if (colorOf(leftOf(sib)) && colorOf(rightOf(sib))) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib))) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = this.root;
                }
            } else {
                TreeMapEntry<K, V> sib2 = leftOf(parentOf(x));
                if (!colorOf(sib2)) {
                    setColor(sib2, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib2 = leftOf(parentOf(x));
                }
                if (colorOf(rightOf(sib2)) && colorOf(leftOf(sib2))) {
                    setColor(sib2, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib2))) {
                        setColor(rightOf(sib2), BLACK);
                        setColor(sib2, RED);
                        rotateLeft(sib2);
                        sib2 = leftOf(parentOf(x));
                    }
                    setColor(sib2, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib2), BLACK);
                    rotateRight(parentOf(x));
                    x = this.root;
                }
            }
        }
        setColor(x, BLACK);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(this.size);
        for (Map.Entry<K, V> e : entrySet()) {
            s.writeObject(e.getKey());
            s.writeObject(e.getValue());
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        buildFromSorted(s.readInt(), null, s, null);
    }

    /* access modifiers changed from: package-private */
    public void readTreeSet(int size2, ObjectInputStream s, V defaultVal) throws IOException, ClassNotFoundException {
        buildFromSorted(size2, null, s, defaultVal);
    }

    /* access modifiers changed from: package-private */
    public void addAllForTreeSet(SortedSet<? extends K> set, V defaultVal) {
        try {
            buildFromSorted(set.size(), set.iterator(), null, defaultVal);
        } catch (IOException | ClassNotFoundException e) {
        }
    }

    private void buildFromSorted(int size2, Iterator<?> it, ObjectInputStream str, V defaultVal) throws IOException, ClassNotFoundException {
        this.size = size2;
        this.root = buildFromSorted(0, 0, size2 - 1, computeRedLevel(size2), it, str, defaultVal);
    }

    private final TreeMapEntry<K, V> buildFromSorted(int level, int lo, int hi, int redLevel, Iterator<?> it, ObjectInputStream str, V defaultVal) throws IOException, ClassNotFoundException {
        K key;
        V value;
        int i = level;
        int i2 = lo;
        int i3 = hi;
        if (i3 < i2) {
            return null;
        }
        int mid = (i2 + i3) >>> 1;
        TreeMapEntry<K, V> left = null;
        if (i2 < mid) {
            left = buildFromSorted(i + 1, i2, mid - 1, redLevel, it, str, defaultVal);
        }
        if (it == null) {
            key = str.readObject();
            value = defaultVal != null ? defaultVal : str.readObject();
        } else if (defaultVal == null) {
            Map.Entry entry = (Map.Entry) it.next();
            key = entry.getKey();
            value = entry.getValue();
        } else {
            key = it.next();
            value = defaultVal;
        }
        K key2 = key;
        V value2 = value;
        TreeMapEntry<K, V> middle = new TreeMapEntry<>(key2, value2, null);
        int i4 = redLevel;
        if (i == i4) {
            middle.color = RED;
        }
        if (left != null) {
            middle.left = left;
            left.parent = middle;
        }
        if (mid < i3) {
            V v = value2;
            K k = key2;
            TreeMapEntry<K, V> right = buildFromSorted(i + 1, mid + 1, i3, i4, it, str, defaultVal);
            middle.right = right;
            right.parent = middle;
        } else {
            K k2 = key2;
        }
        return middle;
    }

    private static int computeRedLevel(int sz) {
        int level = 0;
        for (int m = sz - 1; m >= 0; m = (m / 2) - 1) {
            level++;
        }
        return level;
    }

    static <K> Spliterator<K> keySpliteratorFor(NavigableMap<K, ?> m) {
        if (m instanceof TreeMap) {
            return ((TreeMap) m).keySpliterator();
        }
        if (m instanceof DescendingSubMap) {
            DescendingSubMap<K, ?> dm = (DescendingSubMap) m;
            TreeMap<K, Object> tm = dm.m;
            if (dm == tm.descendingMap) {
                return tm.descendingKeySpliterator();
            }
        }
        return ((NavigableSubMap) m).keySpliterator();
    }

    /* access modifiers changed from: package-private */
    public final Spliterator<K> keySpliterator() {
        KeySpliterator keySpliterator = new KeySpliterator(this, null, null, 0, -1, 0);
        return keySpliterator;
    }

    /* access modifiers changed from: package-private */
    public final Spliterator<K> descendingKeySpliterator() {
        DescendingKeySpliterator descendingKeySpliterator = new DescendingKeySpliterator(this, null, null, 0, -2, 0);
        return descendingKeySpliterator;
    }
}
