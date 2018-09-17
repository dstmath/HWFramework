package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class TreeMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V>, Cloneable, Serializable {
    private static final boolean BLACK = true;
    private static final boolean RED = false;
    private static final Object UNBOUNDED = new Object();
    private static final long serialVersionUID = 919286545866124006L;
    private final Comparator<? super K> comparator;
    private transient NavigableMap<K, V> descendingMap;
    private transient EntrySet entrySet;
    private transient int modCount;
    private transient KeySet<K> navigableKeySet;
    private transient TreeMapEntry<K, V> root;
    private transient int size;

    static abstract class NavigableSubMap<K, V> extends AbstractMap<K, V> implements NavigableMap<K, V>, Serializable {
        private static final long serialVersionUID = 2765629423043303731L;
        transient NavigableMap<K, V> descendingMapView;
        transient EntrySetView entrySetView;
        final boolean fromStart;
        final K hi;
        final boolean hiInclusive;
        final K lo;
        final boolean loInclusive;
        final TreeMap<K, V> m;
        transient KeySet<K> navigableKeySetView;
        final boolean toEnd;

        abstract class EntrySetView extends AbstractSet<Entry<K, V>> {
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
                return n != null ? NavigableSubMap.this.tooHigh(n.key) : TreeMap.BLACK;
            }

            public boolean contains(Object o) {
                boolean z = TreeMap.RED;
                if (!(o instanceof Entry)) {
                    return TreeMap.RED;
                }
                Entry<?, ?> entry = (Entry) o;
                Object key = entry.getKey();
                if (!NavigableSubMap.this.inRange(key)) {
                    return TreeMap.RED;
                }
                TreeMapEntry<?, ?> node = NavigableSubMap.this.m.getEntry(key);
                if (node != null) {
                    z = TreeMap.valEquals(node.getValue(), entry.getValue());
                }
                return z;
            }

            public boolean remove(Object o) {
                if (!(o instanceof Entry)) {
                    return TreeMap.RED;
                }
                Entry<?, ?> entry = (Entry) o;
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

            final TreeMapEntry<K, V> nextEntry() {
                TreeMapEntry<K, V> e = this.next;
                if (e == null || e.key == this.fenceKey) {
                    throw new NoSuchElementException();
                } else if (NavigableSubMap.this.m.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                } else {
                    this.next = TreeMap.successor(e);
                    this.lastReturned = e;
                    return e;
                }
            }

            final TreeMapEntry<K, V> prevEntry() {
                TreeMapEntry<K, V> e = this.next;
                if (e == null || e.key == this.fenceKey) {
                    throw new NoSuchElementException();
                } else if (NavigableSubMap.this.m.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                } else {
                    this.next = TreeMap.predecessor(e);
                    this.lastReturned = e;
                    return e;
                }
            }

            final void removeAscending() {
                if (this.lastReturned == null) {
                    throw new IllegalStateException();
                } else if (NavigableSubMap.this.m.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                } else {
                    if (!(this.lastReturned.left == null || this.lastReturned.right == null)) {
                        this.next = this.lastReturned;
                    }
                    NavigableSubMap.this.m.deleteEntry(this.lastReturned);
                    this.lastReturned = null;
                    this.expectedModCount = NavigableSubMap.this.m.modCount;
                }
            }

            final void removeDescending() {
                if (this.lastReturned == null) {
                    throw new IllegalStateException();
                } else if (NavigableSubMap.this.m.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                } else {
                    NavigableSubMap.this.m.deleteEntry(this.lastReturned);
                    this.lastReturned = null;
                    this.expectedModCount = NavigableSubMap.this.m.modCount;
                }
            }
        }

        final class DescendingSubMapEntryIterator extends SubMapIterator<Entry<K, V>> {
            DescendingSubMapEntryIterator(TreeMapEntry<K, V> last, TreeMapEntry<K, V> fence) {
                super(last, fence);
            }

            public Entry<K, V> next() {
                return prevEntry();
            }

            public void remove() {
                removeDescending();
            }
        }

        final class DescendingSubMapKeyIterator extends SubMapIterator<K> implements Spliterator<K> {
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

        final class SubMapEntryIterator extends SubMapIterator<Entry<K, V>> {
            SubMapEntryIterator(TreeMapEntry<K, V> first, TreeMapEntry<K, V> fence) {
                super(first, fence);
            }

            public Entry<K, V> next() {
                return nextEntry();
            }

            public void remove() {
                removeAscending();
            }
        }

        final class SubMapKeyIterator extends SubMapIterator<K> implements Spliterator<K> {
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

        abstract Iterator<K> descendingKeyIterator();

        abstract Iterator<K> keyIterator();

        abstract Spliterator<K> keySpliterator();

        abstract TreeMapEntry<K, V> subCeiling(K k);

        abstract TreeMapEntry<K, V> subFloor(K k);

        abstract TreeMapEntry<K, V> subHigher(K k);

        abstract TreeMapEntry<K, V> subHighest();

        abstract TreeMapEntry<K, V> subLower(K k);

        abstract TreeMapEntry<K, V> subLowest();

        NavigableSubMap(TreeMap<K, V> m, boolean fromStart, K lo, boolean loInclusive, boolean toEnd, K hi, boolean hiInclusive) {
            if (fromStart || (toEnd ^ 1) == 0) {
                if (!fromStart) {
                    m.compare(lo, lo);
                }
                if (!toEnd) {
                    m.compare(hi, hi);
                }
            } else if (m.compare(lo, hi) > 0) {
                throw new IllegalArgumentException("fromKey > toKey");
            }
            this.m = m;
            this.fromStart = fromStart;
            this.lo = lo;
            this.loInclusive = loInclusive;
            this.toEnd = toEnd;
            this.hi = hi;
            this.hiInclusive = hiInclusive;
        }

        final boolean tooLow(Object key) {
            if (!this.fromStart) {
                int c = this.m.compare(key, this.lo);
                if (c < 0 || (c == 0 && (this.loInclusive ^ 1) != 0)) {
                    return TreeMap.BLACK;
                }
            }
            return TreeMap.RED;
        }

        final boolean tooHigh(Object key) {
            if (!this.toEnd) {
                int c = this.m.compare(key, this.hi);
                if (c > 0 || (c == 0 && (this.hiInclusive ^ 1) != 0)) {
                    return TreeMap.BLACK;
                }
            }
            return TreeMap.RED;
        }

        final boolean inRange(Object key) {
            return !tooLow(key) ? tooHigh(key) ^ 1 : TreeMap.RED;
        }

        final boolean inClosedRange(Object key) {
            boolean z = TreeMap.BLACK;
            if (!this.fromStart && this.m.compare(key, this.lo) < 0) {
                return TreeMap.RED;
            }
            if (!this.toEnd && this.m.compare(this.hi, key) < 0) {
                z = TreeMap.RED;
            }
            return z;
        }

        final boolean inRange(Object key, boolean inclusive) {
            return inclusive ? inRange(key) : inClosedRange(key);
        }

        final TreeMapEntry<K, V> absLowest() {
            TreeMapEntry<K, V> e;
            if (this.fromStart) {
                e = this.m.getFirstEntry();
            } else if (this.loInclusive) {
                e = this.m.getCeilingEntry(this.lo);
            } else {
                e = this.m.getHigherEntry(this.lo);
            }
            return (e == null || tooHigh(e.key)) ? null : e;
        }

        final TreeMapEntry<K, V> absHighest() {
            TreeMapEntry<K, V> e;
            if (this.toEnd) {
                e = this.m.getLastEntry();
            } else if (this.hiInclusive) {
                e = this.m.getFloorEntry(this.hi);
            } else {
                e = this.m.getLowerEntry(this.hi);
            }
            return (e == null || tooLow(e.key)) ? null : e;
        }

        final TreeMapEntry<K, V> absCeiling(K key) {
            if (tooLow(key)) {
                return absLowest();
            }
            TreeMapEntry<K, V> e = this.m.getCeilingEntry(key);
            if (e == null || tooHigh(e.key)) {
                e = null;
            }
            return e;
        }

        final TreeMapEntry<K, V> absHigher(K key) {
            if (tooLow(key)) {
                return absLowest();
            }
            TreeMapEntry<K, V> e = this.m.getHigherEntry(key);
            if (e == null || tooHigh(e.key)) {
                e = null;
            }
            return e;
        }

        final TreeMapEntry<K, V> absFloor(K key) {
            if (tooHigh(key)) {
                return absHighest();
            }
            TreeMapEntry<K, V> e = this.m.getFloorEntry(key);
            if (e == null || tooLow(e.key)) {
                e = null;
            }
            return e;
        }

        final TreeMapEntry<K, V> absLower(K key) {
            if (tooHigh(key)) {
                return absHighest();
            }
            TreeMapEntry<K, V> e = this.m.getLowerEntry(key);
            if (e == null || tooLow(e.key)) {
                e = null;
            }
            return e;
        }

        final TreeMapEntry<K, V> absHighFence() {
            if (this.toEnd) {
                return null;
            }
            if (this.hiInclusive) {
                return this.m.getHigherEntry(this.hi);
            }
            return this.m.getCeilingEntry(this.hi);
        }

        final TreeMapEntry<K, V> absLowFence() {
            if (this.fromStart) {
                return null;
            }
            if (this.loInclusive) {
                return this.m.getLowerEntry(this.lo);
            }
            return this.m.getFloorEntry(this.lo);
        }

        public boolean isEmpty() {
            return (this.fromStart && this.toEnd) ? this.m.isEmpty() : entrySet().isEmpty();
        }

        public int size() {
            return (this.fromStart && this.toEnd) ? this.m.size() : entrySet().size();
        }

        public final boolean containsKey(Object key) {
            return inRange(key) ? this.m.containsKey(key) : TreeMap.RED;
        }

        public final V put(K key, V value) {
            if (inRange(key)) {
                return this.m.put(key, value);
            }
            throw new IllegalArgumentException("key out of range");
        }

        public final V get(Object key) {
            return !inRange(key) ? null : this.m.get(key);
        }

        public final V remove(Object key) {
            return !inRange(key) ? null : this.m.remove(key);
        }

        public final Entry<K, V> ceilingEntry(K key) {
            return TreeMap.exportEntry(subCeiling(key));
        }

        public final K ceilingKey(K key) {
            return TreeMap.keyOrNull(subCeiling(key));
        }

        public final Entry<K, V> higherEntry(K key) {
            return TreeMap.exportEntry(subHigher(key));
        }

        public final K higherKey(K key) {
            return TreeMap.keyOrNull(subHigher(key));
        }

        public final Entry<K, V> floorEntry(K key) {
            return TreeMap.exportEntry(subFloor(key));
        }

        public final K floorKey(K key) {
            return TreeMap.keyOrNull(subFloor(key));
        }

        public final Entry<K, V> lowerEntry(K key) {
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

        public final Entry<K, V> firstEntry() {
            return TreeMap.exportEntry(subLowest());
        }

        public final Entry<K, V> lastEntry() {
            return TreeMap.exportEntry(subHighest());
        }

        public final Entry<K, V> pollFirstEntry() {
            TreeMapEntry<K, V> e = subLowest();
            Entry<K, V> result = TreeMap.exportEntry(e);
            if (e != null) {
                this.m.deleteEntry(e);
            }
            return result;
        }

        public final Entry<K, V> pollLastEntry() {
            TreeMapEntry<K, V> e = subHighest();
            Entry<K, V> result = TreeMap.exportEntry(e);
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
            nksv = new KeySet(this);
            this.navigableKeySetView = nksv;
            return nksv;
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

    static final class AscendingSubMap<K, V> extends NavigableSubMap<K, V> {
        private static final long serialVersionUID = 912986545866124060L;

        final class AscendingEntrySetView extends EntrySetView {
            AscendingEntrySetView() {
                super();
            }

            public Iterator<Entry<K, V>> iterator() {
                return new SubMapEntryIterator(AscendingSubMap.this.absLowest(), AscendingSubMap.this.absHighFence());
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
                return new AscendingSubMap(this.m, TreeMap.RED, fromKey, fromInclusive, TreeMap.RED, toKey, toInclusive);
            } else {
                throw new IllegalArgumentException("toKey out of range");
            }
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            if (inRange(toKey) || (!this.toEnd && this.m.compare(toKey, this.hi) == 0 && !this.hiInclusive && !inclusive)) {
                return new AscendingSubMap(this.m, this.fromStart, this.lo, this.loInclusive, TreeMap.RED, toKey, inclusive);
            }
            throw new IllegalArgumentException("toKey out of range");
        }

        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            if (inRange(fromKey) || !(this.fromStart || this.m.compare(fromKey, this.lo) != 0 || this.loInclusive || inclusive)) {
                return new AscendingSubMap(this.m, TreeMap.RED, fromKey, inclusive, this.toEnd, this.hi, this.hiInclusive);
            }
            throw new IllegalArgumentException("fromKey out of range");
        }

        public NavigableMap<K, V> descendingMap() {
            NavigableMap<K, V> mv = this.descendingMapView;
            if (mv != null) {
                return mv;
            }
            NavigableMap<K, V> descendingSubMap = new DescendingSubMap(this.m, this.fromStart, this.lo, this.loInclusive, this.toEnd, this.hi, this.hiInclusive);
            this.descendingMapView = descendingSubMap;
            return descendingSubMap;
        }

        Iterator<K> keyIterator() {
            return new SubMapKeyIterator(absLowest(), absHighFence());
        }

        Spliterator<K> keySpliterator() {
            return new SubMapKeyIterator(absLowest(), absHighFence());
        }

        Iterator<K> descendingKeyIterator() {
            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
        }

        public Set<Entry<K, V>> entrySet() {
            EntrySetView es = this.entrySetView;
            if (es != null) {
                return es;
            }
            es = new AscendingEntrySetView();
            this.entrySetView = es;
            return es;
        }

        TreeMapEntry<K, V> subLowest() {
            return absLowest();
        }

        TreeMapEntry<K, V> subHighest() {
            return absHighest();
        }

        TreeMapEntry<K, V> subCeiling(K key) {
            return absCeiling(key);
        }

        TreeMapEntry<K, V> subHigher(K key) {
            return absHigher(key);
        }

        TreeMapEntry<K, V> subFloor(K key) {
            return absFloor(key);
        }

        TreeMapEntry<K, V> subLower(K key) {
            return absLower(key);
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

        final TreeMapEntry<K, V> nextEntry() {
            TreeMapEntry<K, V> e = this.next;
            if (e == null) {
                throw new NoSuchElementException();
            } else if (TreeMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                this.next = TreeMap.successor(e);
                this.lastReturned = e;
                return e;
            }
        }

        final TreeMapEntry<K, V> prevEntry() {
            TreeMapEntry<K, V> e = this.next;
            if (e == null) {
                throw new NoSuchElementException();
            } else if (TreeMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                this.next = TreeMap.predecessor(e);
                this.lastReturned = e;
                return e;
            }
        }

        public void remove() {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            } else if (TreeMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                if (!(this.lastReturned.left == null || this.lastReturned.right == null)) {
                    this.next = this.lastReturned;
                }
                TreeMap.this.deleteEntry(this.lastReturned);
                this.expectedModCount = TreeMap.this.modCount;
                this.lastReturned = null;
            }
        }
    }

    final class DescendingKeyIterator extends PrivateEntryIterator<K> {
        DescendingKeyIterator(TreeMapEntry<K, V> first) {
            super(first);
        }

        public K next() {
            return prevEntry().key;
        }

        public void remove() {
            if (this.lastReturned == null) {
                throw new IllegalStateException();
            } else if (TreeMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                TreeMap.this.deleteEntry(this.lastReturned);
                this.lastReturned = null;
                this.expectedModCount = TreeMap.this.modCount;
            }
        }
    }

    static class TreeMapSpliterator<K, V> {
        TreeMapEntry<K, V> current;
        int est;
        int expectedModCount;
        TreeMapEntry<K, V> fence;
        int side;
        final TreeMap<K, V> tree;

        TreeMapSpliterator(TreeMap<K, V> tree, TreeMapEntry<K, V> origin, TreeMapEntry<K, V> fence, int side, int est, int expectedModCount) {
            this.tree = tree;
            this.current = origin;
            this.fence = fence;
            this.side = side;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEstimate() {
            int s = this.est;
            if (s >= 0) {
                return s;
            }
            TreeMap<K, V> t = this.tree;
            if (t != null) {
                this.current = s == -1 ? t.getFirstEntry() : t.getLastEntry();
                s = t.size;
                this.est = s;
                this.expectedModCount = t.modCount;
                return s;
            }
            this.est = 0;
            return 0;
        }

        public final long estimateSize() {
            return (long) getEstimate();
        }
    }

    static final class DescendingKeySpliterator<K, V> extends TreeMapSpliterator<K, V> implements Spliterator<K> {
        DescendingKeySpliterator(TreeMap<K, V> tree, TreeMapEntry<K, V> origin, TreeMapEntry<K, V> fence, int side, int est, int expectedModCount) {
            super(tree, origin, fence, side, est, expectedModCount);
        }

        public DescendingKeySpliterator<K, V> trySplit() {
            TreeMapEntry<K, V> s;
            if (this.est < 0) {
                getEstimate();
            }
            int d = this.side;
            TreeMapEntry<K, V> e = this.current;
            TreeMapEntry<K, V> f = this.fence;
            if (e == null || e == f) {
                s = null;
            } else if (d == 0) {
                s = this.tree.root;
            } else if (d < 0) {
                s = e.left;
            } else if (d <= 0 || f == null) {
                s = null;
            } else {
                s = f.right;
            }
            if (s == null || s == e || s == f || this.tree.compare(e.key, s.key) <= 0) {
                return null;
            }
            this.side = 1;
            TreeMap treeMap = this.tree;
            this.current = s;
            int i = this.est >>> 1;
            this.est = i;
            return new DescendingKeySpliterator(treeMap, e, s, -1, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.est < 0) {
                getEstimate();
            }
            TreeMapEntry<K, V> f = this.fence;
            TreeMapEntry<K, V> e = this.current;
            if (e != null && e != f) {
                this.current = f;
                do {
                    action.accept(e.key);
                    TreeMapEntry<K, V> p = e.left;
                    if (p == null) {
                        while (true) {
                            p = e.parent;
                            if (p == null || e != p.left) {
                                break;
                            }
                            e = p;
                        }
                    } else {
                        while (true) {
                            TreeMapEntry<K, V> pr = p.right;
                            if (pr == null) {
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
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.est < 0) {
                getEstimate();
            }
            TreeMapEntry<K, V> e = this.current;
            if (e == null || e == this.fence) {
                return TreeMap.RED;
            }
            this.current = TreeMap.predecessor(e);
            action.accept(e.key);
            if (this.tree.modCount == this.expectedModCount) {
                return TreeMap.BLACK;
            }
            throw new ConcurrentModificationException();
        }

        public int characteristics() {
            int i = 0;
            if (this.side == 0) {
                i = 64;
            }
            return (i | 1) | 16;
        }
    }

    static final class DescendingSubMap<K, V> extends NavigableSubMap<K, V> {
        private static final long serialVersionUID = 912986545866120460L;
        private final Comparator<? super K> reverseComparator = Collections.reverseOrder(this.m.comparator);

        final class DescendingEntrySetView extends EntrySetView {
            DescendingEntrySetView() {
                super();
            }

            public Iterator<Entry<K, V>> iterator() {
                return new DescendingSubMapEntryIterator(DescendingSubMap.this.absHighest(), DescendingSubMap.this.absLowFence());
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
                return new DescendingSubMap(this.m, TreeMap.RED, toKey, toInclusive, TreeMap.RED, fromKey, fromInclusive);
            } else {
                throw new IllegalArgumentException("toKey out of range");
            }
        }

        public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
            if (inRange(toKey) || !(this.fromStart || this.m.compare(toKey, this.lo) != 0 || this.loInclusive || inclusive)) {
                return new DescendingSubMap(this.m, TreeMap.RED, toKey, inclusive, this.toEnd, this.hi, this.hiInclusive);
            }
            throw new IllegalArgumentException("toKey out of range");
        }

        public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
            if (inRange(fromKey) || (!this.toEnd && this.m.compare(fromKey, this.hi) == 0 && !this.hiInclusive && !inclusive)) {
                return new DescendingSubMap(this.m, this.fromStart, this.lo, this.loInclusive, TreeMap.RED, fromKey, inclusive);
            }
            throw new IllegalArgumentException("fromKey out of range");
        }

        public NavigableMap<K, V> descendingMap() {
            NavigableMap<K, V> mv = this.descendingMapView;
            if (mv != null) {
                return mv;
            }
            NavigableMap<K, V> ascendingSubMap = new AscendingSubMap(this.m, this.fromStart, this.lo, this.loInclusive, this.toEnd, this.hi, this.hiInclusive);
            this.descendingMapView = ascendingSubMap;
            return ascendingSubMap;
        }

        Iterator<K> keyIterator() {
            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
        }

        Spliterator<K> keySpliterator() {
            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
        }

        Iterator<K> descendingKeyIterator() {
            return new SubMapKeyIterator(absLowest(), absHighFence());
        }

        public Set<Entry<K, V>> entrySet() {
            EntrySetView es = this.entrySetView;
            if (es != null) {
                return es;
            }
            es = new DescendingEntrySetView();
            this.entrySetView = es;
            return es;
        }

        TreeMapEntry<K, V> subLowest() {
            return absHighest();
        }

        TreeMapEntry<K, V> subHighest() {
            return absLowest();
        }

        TreeMapEntry<K, V> subCeiling(K key) {
            return absFloor(key);
        }

        TreeMapEntry<K, V> subHigher(K key) {
            return absLower(key);
        }

        TreeMapEntry<K, V> subFloor(K key) {
            return absCeiling(key);
        }

        TreeMapEntry<K, V> subLower(K key) {
            return absHigher(key);
        }
    }

    final class EntryIterator extends PrivateEntryIterator<Entry<K, V>> {
        EntryIterator(TreeMapEntry<K, V> first) {
            super(first);
        }

        public Entry<K, V> next() {
            return nextEntry();
        }
    }

    class EntrySet extends AbstractSet<Entry<K, V>> {
        EntrySet() {
        }

        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator(TreeMap.this.getFirstEntry());
        }

        public boolean contains(Object o) {
            boolean z = TreeMap.RED;
            if (!(o instanceof Entry)) {
                return TreeMap.RED;
            }
            Entry<?, ?> entry = (Entry) o;
            Object value = entry.getValue();
            TreeMapEntry<K, V> p = TreeMap.this.getEntry(entry.getKey());
            if (p != null) {
                z = TreeMap.valEquals(p.getValue(), value);
            }
            return z;
        }

        public boolean remove(Object o) {
            if (!(o instanceof Entry)) {
                return TreeMap.RED;
            }
            Entry<?, ?> entry = (Entry) o;
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

        public Spliterator<Entry<K, V>> spliterator() {
            return new EntrySpliterator(TreeMap.this, null, null, 0, -1, 0);
        }
    }

    static final class EntrySpliterator<K, V> extends TreeMapSpliterator<K, V> implements Spliterator<Entry<K, V>> {
        EntrySpliterator(TreeMap<K, V> tree, TreeMapEntry<K, V> origin, TreeMapEntry<K, V> fence, int side, int est, int expectedModCount) {
            super(tree, origin, fence, side, est, expectedModCount);
        }

        public EntrySpliterator<K, V> trySplit() {
            TreeMapEntry<K, V> s;
            if (this.est < 0) {
                getEstimate();
            }
            int d = this.side;
            TreeMapEntry<K, V> e = this.current;
            TreeMapEntry<K, V> f = this.fence;
            if (e == null || e == f) {
                s = null;
            } else if (d == 0) {
                s = this.tree.root;
            } else if (d > 0) {
                s = e.right;
            } else if (d >= 0 || f == null) {
                s = null;
            } else {
                s = f.left;
            }
            if (s == null || s == e || s == f || this.tree.compare(e.key, s.key) >= 0) {
                return null;
            }
            this.side = 1;
            TreeMap treeMap = this.tree;
            this.current = s;
            int i = this.est >>> 1;
            this.est = i;
            return new EntrySpliterator(treeMap, e, s, -1, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.est < 0) {
                getEstimate();
            }
            TreeMapEntry<K, V> f = this.fence;
            TreeMapEntry<K, V> e = this.current;
            if (e != null && e != f) {
                this.current = f;
                do {
                    action.accept(e);
                    TreeMapEntry<K, V> p = e.right;
                    if (p == null) {
                        while (true) {
                            p = e.parent;
                            if (p == null || e != p.right) {
                                break;
                            }
                            e = p;
                        }
                    } else {
                        while (true) {
                            TreeMapEntry<K, V> pl = p.left;
                            if (pl == null) {
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
            }
        }

        public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.est < 0) {
                getEstimate();
            }
            TreeMapEntry<K, V> e = this.current;
            if (e == null || e == this.fence) {
                return TreeMap.RED;
            }
            this.current = TreeMap.successor(e);
            action.accept(e);
            if (this.tree.modCount == this.expectedModCount) {
                return TreeMap.BLACK;
            }
            throw new ConcurrentModificationException();
        }

        public int characteristics() {
            int i = 0;
            if (this.side == 0) {
                i = 64;
            }
            return ((i | 1) | 4) | 16;
        }

        public Comparator<Entry<K, V>> getComparator() {
            if (this.tree.comparator != null) {
                return Entry.comparingByKey(this.tree.comparator);
            }
            return new -$Lambda$KL__89hzgZoHMWOIXIRQExBZHRE();
        }
    }

    final class KeyIterator extends PrivateEntryIterator<K> {
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
            Entry<E, ?> e = this.m.pollFirstEntry();
            if (e == null) {
                return null;
            }
            return e.getKey();
        }

        public E pollLast() {
            Entry<E, ?> e = this.m.pollLastEntry();
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
            TreeMapEntry<K, V> s;
            if (this.est < 0) {
                getEstimate();
            }
            int d = this.side;
            TreeMapEntry<K, V> e = this.current;
            TreeMapEntry<K, V> f = this.fence;
            if (e == null || e == f) {
                s = null;
            } else if (d == 0) {
                s = this.tree.root;
            } else if (d > 0) {
                s = e.right;
            } else if (d >= 0 || f == null) {
                s = null;
            } else {
                s = f.left;
            }
            if (s == null || s == e || s == f || this.tree.compare(e.key, s.key) >= 0) {
                return null;
            }
            this.side = 1;
            TreeMap treeMap = this.tree;
            this.current = s;
            int i = this.est >>> 1;
            this.est = i;
            return new KeySpliterator(treeMap, e, s, -1, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.est < 0) {
                getEstimate();
            }
            TreeMapEntry<K, V> f = this.fence;
            TreeMapEntry<K, V> e = this.current;
            if (e != null && e != f) {
                this.current = f;
                do {
                    action.accept(e.key);
                    TreeMapEntry<K, V> p = e.right;
                    if (p == null) {
                        while (true) {
                            p = e.parent;
                            if (p == null || e != p.right) {
                                break;
                            }
                            e = p;
                        }
                    } else {
                        while (true) {
                            TreeMapEntry<K, V> pl = p.left;
                            if (pl == null) {
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
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.est < 0) {
                getEstimate();
            }
            TreeMapEntry<K, V> e = this.current;
            if (e == null || e == this.fence) {
                return TreeMap.RED;
            }
            this.current = TreeMap.successor(e);
            action.accept(e.key);
            if (this.tree.modCount == this.expectedModCount) {
                return TreeMap.BLACK;
            }
            throw new ConcurrentModificationException();
        }

        public int characteristics() {
            int i = 0;
            if (this.side == 0) {
                i = 64;
            }
            return ((i | 1) | 4) | 16;
        }

        public final Comparator<? super K> getComparator() {
            return this.tree.comparator;
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
            return new AscendingSubMap(TreeMap.this, this.fromStart, this.fromKey, TreeMap.BLACK, this.toEnd, this.toKey, TreeMap.RED);
        }

        public Set<Entry<K, V>> entrySet() {
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

    static final class TreeMapEntry<K, V> implements Entry<K, V> {
        boolean color = TreeMap.BLACK;
        K key;
        TreeMapEntry<K, V> left;
        TreeMapEntry<K, V> parent;
        TreeMapEntry<K, V> right;
        V value;

        TreeMapEntry(K key, V value, TreeMapEntry<K, V> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }

        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            boolean z = TreeMap.RED;
            if (!(o instanceof Entry)) {
                return TreeMap.RED;
            }
            Entry<?, ?> e = (Entry) o;
            if (TreeMap.valEquals(this.key, e.getKey())) {
                z = TreeMap.valEquals(this.value, e.getValue());
            }
            return z;
        }

        public int hashCode() {
            return (this.key == null ? 0 : this.key.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
        }

        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    final class ValueIterator extends PrivateEntryIterator<V> {
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
            TreeMapEntry<K, V> s;
            if (this.est < 0) {
                getEstimate();
            }
            int d = this.side;
            TreeMapEntry<K, V> e = this.current;
            TreeMapEntry<K, V> f = this.fence;
            if (e == null || e == f) {
                s = null;
            } else if (d == 0) {
                s = this.tree.root;
            } else if (d > 0) {
                s = e.right;
            } else if (d >= 0 || f == null) {
                s = null;
            } else {
                s = f.left;
            }
            if (s == null || s == e || s == f || this.tree.compare(e.key, s.key) >= 0) {
                return null;
            }
            this.side = 1;
            TreeMap treeMap = this.tree;
            this.current = s;
            int i = this.est >>> 1;
            this.est = i;
            return new ValueSpliterator(treeMap, e, s, -1, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.est < 0) {
                getEstimate();
            }
            TreeMapEntry<K, V> f = this.fence;
            TreeMapEntry<K, V> e = this.current;
            if (e != null && e != f) {
                this.current = f;
                do {
                    action.accept(e.value);
                    TreeMapEntry<K, V> p = e.right;
                    if (p == null) {
                        while (true) {
                            p = e.parent;
                            if (p == null || e != p.right) {
                                break;
                            }
                            e = p;
                        }
                    } else {
                        while (true) {
                            TreeMapEntry<K, V> pl = p.left;
                            if (pl == null) {
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
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.est < 0) {
                getEstimate();
            }
            TreeMapEntry<K, V> e = this.current;
            if (e == null || e == this.fence) {
                return TreeMap.RED;
            }
            this.current = TreeMap.successor(e);
            action.accept(e.value);
            if (this.tree.modCount == this.expectedModCount) {
                return TreeMap.BLACK;
            }
            throw new ConcurrentModificationException();
        }

        public int characteristics() {
            int i = 0;
            if (this.side == 0) {
                i = 64;
            }
            return i | 16;
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
            return new ValueSpliterator(TreeMap.this, null, null, 0, -1, 0);
        }
    }

    public TreeMap() {
        this.size = 0;
        this.modCount = 0;
        this.comparator = null;
    }

    public TreeMap(Comparator<? super K> comparator) {
        this.size = 0;
        this.modCount = 0;
        this.comparator = comparator;
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
        } catch (IOException e) {
        } catch (ClassNotFoundException e2) {
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
                } catch (IOException e) {
                } catch (ClassNotFoundException e2) {
                }
                return;
            }
        }
        super.putAll(map);
    }

    final TreeMapEntry<K, V> getEntry(Object key) {
        if (this.comparator != null) {
            return getEntryUsingComparator(key);
        }
        if (key == null) {
            throw new NullPointerException();
        }
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

    final TreeMapEntry<K, V> getEntryUsingComparator(Object key) {
        K k = key;
        Comparator<? super K> cpr = this.comparator;
        if (cpr != null) {
            TreeMapEntry<K, V> p = this.root;
            while (p != null) {
                int cmp = cpr.compare(key, p.key);
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

    final TreeMapEntry<K, V> getCeilingEntry(K key) {
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

    final TreeMapEntry<K, V> getFloorEntry(K key) {
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

    final TreeMapEntry<K, V> getHigherEntry(K key) {
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

    final TreeMapEntry<K, V> getLowerEntry(K key) {
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
        TreeMapEntry<K, V> t = this.root;
        if (t == null) {
            if (this.comparator != null) {
                if (key == null) {
                    this.comparator.compare(key, key);
                }
            } else if (key == null) {
                throw new NullPointerException("key == null");
            } else if (!(key instanceof Comparable)) {
                throw new ClassCastException("Cannot cast" + key.getClass().getName() + " to Comparable.");
            }
            this.root = new TreeMapEntry(key, value, null);
            this.size = 1;
            this.modCount++;
            return null;
        }
        TreeMapEntry<K, V> parent;
        int cmp;
        Comparator<? super K> cpr = this.comparator;
        if (cpr == null) {
            if (key != null) {
                Comparable<? super K> k = (Comparable) key;
                while (true) {
                    parent = t;
                    cmp = k.compareTo(t.key);
                    if (cmp < 0) {
                        t = t.left;
                    } else if (cmp <= 0) {
                        return t.setValue(value);
                    } else {
                        t = t.right;
                    }
                    if (t == null) {
                        break;
                    }
                }
            } else {
                throw new NullPointerException();
            }
        }
        do {
            parent = t;
            cmp = cpr.compare(key, t.key);
            if (cmp < 0) {
                t = t.left;
                continue;
            } else if (cmp <= 0) {
                return t.setValue(value);
            } else {
                t = t.right;
                continue;
            }
        } while (t != null);
        TreeMapEntry<K, V> e = new TreeMapEntry(key, value, parent);
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
            } catch (IOException e) {
            } catch (ClassNotFoundException e2) {
            }
            return clone;
        } catch (Throwable e3) {
            throw new InternalError(e3);
        }
    }

    public Entry<K, V> firstEntry() {
        return exportEntry(getFirstEntry());
    }

    public Entry<K, V> lastEntry() {
        return exportEntry(getLastEntry());
    }

    public Entry<K, V> pollFirstEntry() {
        TreeMapEntry<K, V> p = getFirstEntry();
        Entry<K, V> result = exportEntry(p);
        if (p != null) {
            deleteEntry(p);
        }
        return result;
    }

    public Entry<K, V> pollLastEntry() {
        TreeMapEntry<K, V> p = getLastEntry();
        Entry<K, V> result = exportEntry(p);
        if (p != null) {
            deleteEntry(p);
        }
        return result;
    }

    public Entry<K, V> lowerEntry(K key) {
        return exportEntry(getLowerEntry(key));
    }

    public K lowerKey(K key) {
        return keyOrNull(getLowerEntry(key));
    }

    public Entry<K, V> floorEntry(K key) {
        return exportEntry(getFloorEntry(key));
    }

    public K floorKey(K key) {
        return keyOrNull(getFloorEntry(key));
    }

    public Entry<K, V> ceilingEntry(K key) {
        return exportEntry(getCeilingEntry(key));
    }

    public K ceilingKey(K key) {
        return keyOrNull(getCeilingEntry(key));
    }

    public Entry<K, V> higherEntry(K key) {
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
        nks = new KeySet(this);
        this.navigableKeySet = nks;
        return nks;
    }

    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
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
        EntrySet es = this.entrySet;
        if (es != null) {
            return es;
        }
        es = new EntrySet();
        this.entrySet = es;
        return es;
    }

    public NavigableMap<K, V> descendingMap() {
        NavigableMap<K, V> km = this.descendingMap;
        if (km != null) {
            return km;
        }
        NavigableMap<K, V> descendingSubMap = new DescendingSubMap(this, BLACK, null, BLACK, BLACK, null, BLACK);
        this.descendingMap = descendingSubMap;
        return descendingSubMap;
    }

    public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        return new AscendingSubMap(this, RED, fromKey, fromInclusive, RED, toKey, toInclusive);
    }

    public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
        return new AscendingSubMap(this, BLACK, null, BLACK, RED, toKey, inclusive);
    }

    public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
        return new AscendingSubMap(this, RED, fromKey, inclusive, BLACK, null, BLACK);
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
        for (TreeMapEntry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
            action.accept(e.key, e.value);
            if (expectedModCount != this.modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        int expectedModCount = this.modCount;
        for (TreeMapEntry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
            e.value = function.apply(e.key, e.value);
            if (expectedModCount != this.modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    Iterator<K> keyIterator() {
        return new KeyIterator(getFirstEntry());
    }

    Iterator<K> descendingKeyIterator() {
        return new DescendingKeyIterator(getLastEntry());
    }

    final int compare(Object k1, Object k2) {
        if (this.comparator == null) {
            return ((Comparable) k1).compareTo(k2);
        }
        return this.comparator.compare(k1, k2);
    }

    static final boolean valEquals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null ? BLACK : RED;
        } else {
            return o1.lambda$-java_util_function_Predicate_4628(o2);
        }
    }

    static <K, V> Entry<K, V> exportEntry(TreeMapEntry<K, V> e) {
        if (e == null) {
            return null;
        }
        return new SimpleImmutableEntry(e);
    }

    static <K, V> K keyOrNull(TreeMapEntry<K, V> e) {
        return e == null ? null : e.key;
    }

    static <K> K key(TreeMapEntry<K, ?> e) {
        if (e != null) {
            return e.key;
        }
        throw new NoSuchElementException();
    }

    final TreeMapEntry<K, V> getFirstEntry() {
        TreeMapEntry<K, V> p = this.root;
        if (p != null) {
            while (p.left != null) {
                p = p.left;
            }
        }
        return p;
    }

    final TreeMapEntry<K, V> getLastEntry() {
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
        TreeMapEntry<K, V> p;
        if (t.right != null) {
            p = t.right;
            while (p.left != null) {
                p = p.left;
            }
            return p;
        }
        p = t.parent;
        TreeMapEntry<K, V> ch = t;
        while (p != null && ch == p.right) {
            ch = p;
            p = p.parent;
        }
        return p;
    }

    static <K, V> TreeMapEntry<K, V> predecessor(TreeMapEntry<K, V> t) {
        if (t == null) {
            return null;
        }
        TreeMapEntry<K, V> p;
        if (t.left != null) {
            p = t.left;
            while (p.right != null) {
                p = p.right;
            }
            return p;
        }
        p = t.parent;
        TreeMapEntry<K, V> ch = t;
        while (p != null && ch == p.left) {
            ch = p;
            p = p.parent;
        }
        return p;
    }

    private static <K, V> boolean colorOf(TreeMapEntry<K, V> p) {
        return p == null ? BLACK : p.color;
    }

    private static <K, V> TreeMapEntry<K, V> parentOf(TreeMapEntry<K, V> p) {
        return p == null ? null : p.parent;
    }

    private static <K, V> void setColor(TreeMapEntry<K, V> p, boolean c) {
        if (p != null) {
            p.color = c;
        }
    }

    private static <K, V> TreeMapEntry<K, V> leftOf(TreeMapEntry<K, V> p) {
        return p == null ? null : p.left;
    }

    private static <K, V> TreeMapEntry<K, V> rightOf(TreeMapEntry<K, V> p) {
        return p == null ? null : p.right;
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
            TreeMapEntry<K, V> y;
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y)) {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                } else {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                }
            } else {
                y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y)) {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                } else {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                }
            }
        }
        this.root.color = BLACK;
    }

    private void deleteEntry(TreeMapEntry<K, V> p) {
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
            TreeMapEntry<K, V> sib;
            if (x == leftOf(parentOf(x))) {
                sib = rightOf(parentOf(x));
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
                sib = leftOf(parentOf(x));
                if (!colorOf(sib)) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }
                if (colorOf(rightOf(sib)) && colorOf(leftOf(sib))) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib))) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
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
        for (Entry<K, V> e : entrySet()) {
            s.writeObject(e.getKey());
            s.writeObject(e.getValue());
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        buildFromSorted(s.readInt(), null, s, null);
    }

    void readTreeSet(int size, ObjectInputStream s, V defaultVal) throws IOException, ClassNotFoundException {
        buildFromSorted(size, null, s, defaultVal);
    }

    void addAllForTreeSet(SortedSet<? extends K> set, V defaultVal) {
        try {
            buildFromSorted(set.size(), set.iterator(), null, defaultVal);
        } catch (IOException e) {
        } catch (ClassNotFoundException e2) {
        }
    }

    private void buildFromSorted(int size, Iterator<?> it, ObjectInputStream str, V defaultVal) throws IOException, ClassNotFoundException {
        this.size = size;
        this.root = buildFromSorted(0, 0, size - 1, computeRedLevel(size), it, str, defaultVal);
    }

    private final TreeMapEntry<K, V> buildFromSorted(int level, int lo, int hi, int redLevel, Iterator<?> it, ObjectInputStream str, V defaultVal) throws IOException, ClassNotFoundException {
        if (hi < lo) {
            return null;
        }
        K key;
        V value;
        int mid = (lo + hi) >>> 1;
        TreeMapEntry left = null;
        if (lo < mid) {
            left = buildFromSorted(level + 1, lo, mid - 1, redLevel, it, str, defaultVal);
        }
        if (it == null) {
            key = str.readObject();
            value = defaultVal != null ? defaultVal : str.readObject();
        } else if (defaultVal == null) {
            Entry<?, ?> entry = (Entry) it.next();
            key = entry.getKey();
            value = entry.getValue();
        } else {
            key = it.next();
            value = defaultVal;
        }
        TreeMapEntry<K, V> middle = new TreeMapEntry(key, value, null);
        if (level == redLevel) {
            middle.color = RED;
        }
        if (left != null) {
            middle.left = left;
            left.parent = middle;
        }
        if (mid < hi) {
            TreeMapEntry<K, V> right = buildFromSorted(level + 1, mid + 1, hi, redLevel, it, str, defaultVal);
            middle.right = right;
            right.parent = middle;
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
            TreeMap<K, ?> tm = dm.m;
            if (dm == tm.descendingMap) {
                TreeMap<K, ?> treeMap = tm;
                return tm.descendingKeySpliterator();
            }
        }
        return ((NavigableSubMap) m).keySpliterator();
    }

    final Spliterator<K> keySpliterator() {
        return new KeySpliterator(this, null, null, 0, -1, 0);
    }

    final Spliterator<K> descendingKeySpliterator() {
        return new DescendingKeySpliterator(this, null, null, 0, -2, 0);
    }
}
