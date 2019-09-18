package java.util.concurrent;

import java.awt.font.NumericShaper;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.annotation.RCWeakRef;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;
import sun.misc.Unsafe;

public class ConcurrentHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V>, Serializable {
    private static final int ABASE;
    private static final int ASHIFT;
    private static final long BASECOUNT;
    private static final long CELLSBUSY;
    private static final long CELLVALUE;
    private static final int DEFAULT_CAPACITY = 16;
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    static final int HASH_BITS = Integer.MAX_VALUE;
    private static final float LOAD_FACTOR = 0.75f;
    private static final int MAXIMUM_CAPACITY = 1073741824;
    static final int MAX_ARRAY_SIZE = 2147483639;
    private static final int MAX_RESIZERS = 65535;
    private static final int MIN_TRANSFER_STRIDE = 16;
    static final int MIN_TREEIFY_CAPACITY = 64;
    static final int MOVED = -1;
    static final int NCPU = Runtime.getRuntime().availableProcessors();
    static final int RESERVED = -3;
    private static final int RESIZE_STAMP_BITS = 16;
    private static final int RESIZE_STAMP_SHIFT = 16;
    private static final long SIZECTL;
    private static final long TRANSFERINDEX;
    static final int TREEBIN = -2;
    static final int TREEIFY_THRESHOLD = 8;
    private static final Unsafe U = Unsafe.getUnsafe();
    static final int UNTREEIFY_THRESHOLD = 6;
    private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("segments", Segment[].class), new ObjectStreamField("segmentMask", Integer.TYPE), new ObjectStreamField("segmentShift", Integer.TYPE)};
    private static final long serialVersionUID = 7249069246763182397L;
    private volatile transient long baseCount;
    private volatile transient int cellsBusy;
    private volatile transient CounterCell[] counterCells;
    @RCWeakRef
    private transient EntrySetView<K, V> entrySet;
    private transient KeySetView<K, V> keySet;
    private volatile transient Node<K, V>[] nextTable;
    private volatile transient int sizeCtl;
    volatile transient Node<K, V>[] table;
    private volatile transient int transferIndex;
    private transient ValuesView<K, V> values;

    static class BaseIterator<K, V> extends Traverser<K, V> {
        Node<K, V> lastReturned;
        final ConcurrentHashMap<K, V> map;

        BaseIterator(Node<K, V>[] tab, int size, int index, int limit, ConcurrentHashMap<K, V> map2) {
            super(tab, size, index, limit);
            this.map = map2;
            advance();
        }

        public final boolean hasNext() {
            return this.next != null;
        }

        public final boolean hasMoreElements() {
            return this.next != null;
        }

        public final void remove() {
            Node<K, V> node = this.lastReturned;
            Node<K, V> p = node;
            if (node != null) {
                this.lastReturned = null;
                this.map.replaceNode(p.key, null, null);
                return;
            }
            throw new IllegalStateException();
        }
    }

    static abstract class BulkTask<K, V, R> extends CountedCompleter<R> {
        int baseIndex;
        int baseLimit;
        final int baseSize;
        int batch;
        int index;
        Node<K, V> next;
        TableStack<K, V> spare;
        TableStack<K, V> stack;
        Node<K, V>[] tab;

        BulkTask(BulkTask<K, V, ?> par, int b, int i, int f, Node<K, V>[] t) {
            super(par);
            this.batch = b;
            this.baseIndex = i;
            this.index = i;
            this.tab = t;
            if (t == null) {
                this.baseLimit = 0;
                this.baseSize = 0;
            } else if (par == null) {
                int length = t.length;
                this.baseLimit = length;
                this.baseSize = length;
            } else {
                this.baseLimit = f;
                this.baseSize = par.baseSize;
            }
        }

        /* access modifiers changed from: package-private */
        public final Node<K, V> advance() {
            Node<K, V> e;
            Node<K, V> e2;
            Node<K, V> node = this.next;
            Node<K, V> e3 = node;
            if (node != null) {
                e3 = e3.next;
            }
            while (e == null) {
                if (this.baseIndex < this.baseLimit) {
                    Node<K, V>[] nodeArr = this.tab;
                    Node<K, V>[] t = nodeArr;
                    if (nodeArr != null) {
                        int length = t.length;
                        int n = length;
                        int i = this.index;
                        int i2 = i;
                        if (length > i && i2 >= 0) {
                            Node<K, V> tabAt = ConcurrentHashMap.tabAt(t, i2);
                            e = tabAt;
                            if (tabAt != null && e.hash < 0) {
                                if (e instanceof ForwardingNode) {
                                    this.tab = ((ForwardingNode) e).nextTable;
                                    e = null;
                                    pushState(t, i2, n);
                                } else {
                                    if (e instanceof TreeBin) {
                                        e2 = ((TreeBin) e).first;
                                    } else {
                                        e2 = null;
                                    }
                                    e = e2;
                                }
                            }
                            if (this.stack != null) {
                                recoverState(n);
                            } else {
                                int i3 = this.baseSize + i2;
                                this.index = i3;
                                if (i3 >= n) {
                                    int i4 = this.baseIndex + 1;
                                    this.baseIndex = i4;
                                    this.index = i4;
                                }
                            }
                        }
                    }
                }
                this.next = null;
                return null;
            }
            this.next = e;
            return e;
        }

        private void pushState(Node<K, V>[] t, int i, int n) {
            TableStack<K, V> s = this.spare;
            if (s != null) {
                this.spare = s.next;
            } else {
                s = new TableStack<>();
            }
            s.tab = t;
            s.length = n;
            s.index = i;
            s.next = this.stack;
            this.stack = s;
        }

        private void recoverState(int n) {
            TableStack<K, V> s;
            while (true) {
                TableStack<K, V> tableStack = this.stack;
                s = tableStack;
                if (tableStack == null) {
                    break;
                }
                int i = this.index;
                int i2 = s.length;
                int len = i2;
                int i3 = i + i2;
                this.index = i3;
                if (i3 < n) {
                    break;
                }
                n = len;
                this.index = s.index;
                this.tab = s.tab;
                s.tab = null;
                TableStack<K, V> next2 = s.next;
                s.next = this.spare;
                this.stack = next2;
                this.spare = s;
            }
            if (s == null) {
                int i4 = this.index + this.baseSize;
                this.index = i4;
                if (i4 >= n) {
                    int i5 = this.baseIndex + 1;
                    this.baseIndex = i5;
                    this.index = i5;
                }
            }
        }
    }

    static abstract class CollectionView<K, V, E> implements Collection<E>, Serializable {
        private static final String OOME_MSG = "Required array size too large";
        private static final long serialVersionUID = 7249069246763182397L;
        final ConcurrentHashMap<K, V> map;

        public abstract boolean contains(Object obj);

        public abstract Iterator<E> iterator();

        public abstract boolean remove(Object obj);

        CollectionView(ConcurrentHashMap<K, V> map2) {
            this.map = map2;
        }

        public ConcurrentHashMap<K, V> getMap() {
            return this.map;
        }

        public final void clear() {
            this.map.clear();
        }

        public final int size() {
            return this.map.size();
        }

        public final boolean isEmpty() {
            return this.map.isEmpty();
        }

        public final Object[] toArray() {
            long sz = this.map.mappingCount();
            if (sz <= 2147483639) {
                int n = (int) sz;
                Object[] r = new Object[n];
                int i = 0;
                Iterator it = iterator();
                while (it.hasNext()) {
                    E e = it.next();
                    if (i == n) {
                        if (n < ConcurrentHashMap.MAX_ARRAY_SIZE) {
                            if (n >= 1073741819) {
                                n = ConcurrentHashMap.MAX_ARRAY_SIZE;
                            } else {
                                n += (n >>> 1) + 1;
                            }
                            r = Arrays.copyOf((T[]) r, n);
                        } else {
                            throw new OutOfMemoryError(OOME_MSG);
                        }
                    }
                    r[i] = e;
                    i++;
                }
                return i == n ? r : Arrays.copyOf((T[]) r, i);
            }
            throw new OutOfMemoryError(OOME_MSG);
        }

        public final <T> T[] toArray(T[] a) {
            long sz = this.map.mappingCount();
            if (sz <= 2147483639) {
                int m = (int) sz;
                T[] r = a.length >= m ? a : (Object[]) Array.newInstance(a.getClass().getComponentType(), m);
                int n = r.length;
                int i = 0;
                Iterator it = iterator();
                while (it.hasNext()) {
                    E e = it.next();
                    if (i == n) {
                        if (n < ConcurrentHashMap.MAX_ARRAY_SIZE) {
                            if (n >= 1073741819) {
                                n = ConcurrentHashMap.MAX_ARRAY_SIZE;
                            } else {
                                n += (n >>> 1) + 1;
                            }
                            r = Arrays.copyOf(r, n);
                        } else {
                            throw new OutOfMemoryError(OOME_MSG);
                        }
                    }
                    r[i] = e;
                    i++;
                }
                if (a != r || i >= n) {
                    return i == n ? r : Arrays.copyOf(r, i);
                }
                r[i] = null;
                return r;
            }
            throw new OutOfMemoryError(OOME_MSG);
        }

        public final String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            Iterator<E> it = iterator();
            if (it.hasNext()) {
                while (true) {
                    Object e = it.next();
                    sb.append(e == this ? "(this Collection)" : e);
                    if (!it.hasNext()) {
                        break;
                    }
                    sb.append(',');
                    sb.append(' ');
                }
            }
            sb.append(']');
            return sb.toString();
        }

        public final boolean containsAll(Collection<?> c) {
            if (c != this) {
                for (Object e : c) {
                    if (e != null) {
                        if (!contains(e)) {
                        }
                    }
                    return false;
                }
            }
            return true;
        }

        public final boolean removeAll(Collection<?> c) {
            if (c != null) {
                boolean modified = false;
                Iterator<E> it = iterator();
                while (it.hasNext()) {
                    if (c.contains(it.next())) {
                        it.remove();
                        modified = true;
                    }
                }
                return modified;
            }
            throw new NullPointerException();
        }

        public final boolean retainAll(Collection<?> c) {
            if (c != null) {
                boolean modified = false;
                Iterator<E> it = iterator();
                while (it.hasNext()) {
                    if (!c.contains(it.next())) {
                        it.remove();
                        modified = true;
                    }
                }
                return modified;
            }
            throw new NullPointerException();
        }
    }

    static final class CounterCell {
        volatile long value;

        CounterCell(long x) {
            this.value = x;
        }
    }

    static final class EntryIterator<K, V> extends BaseIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        EntryIterator(Node<K, V>[] tab, int index, int size, int limit, ConcurrentHashMap<K, V> map) {
            super(tab, index, size, limit, map);
        }

        public final Map.Entry<K, V> next() {
            Node<K, V> node = this.next;
            Node<K, V> p = node;
            if (node != null) {
                K k = p.key;
                V v = p.val;
                this.lastReturned = p;
                advance();
                return new MapEntry(k, v, this.map);
            }
            throw new NoSuchElementException();
        }
    }

    static final class EntrySetView<K, V> extends CollectionView<K, V, Map.Entry<K, V>> implements Set<Map.Entry<K, V>>, Serializable {
        private static final long serialVersionUID = 2249069246763182397L;

        EntrySetView(ConcurrentHashMap<K, V> map) {
            super(map);
        }

        public boolean contains(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry) o;
                Map.Entry<?, ?> e = entry;
                Object key = entry.getKey();
                Object k = key;
                if (key != null) {
                    Object obj = this.map.get(k);
                    Object r = obj;
                    if (obj != null) {
                        Object value = e.getValue();
                        Object v = value;
                        if (value != null && (v == r || v.equals(r))) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        public boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry) o;
                Map.Entry<?, ?> e = entry;
                Object key = entry.getKey();
                Object k = key;
                if (key != null) {
                    Object value = e.getValue();
                    Object v = value;
                    if (value != null && this.map.remove(k, v)) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Iterator<Map.Entry<K, V>> iterator() {
            ConcurrentHashMap<K, V> m = this.map;
            Node<K, V>[] nodeArr = m.table;
            Node<K, V>[] t = nodeArr;
            int f = nodeArr == null ? 0 : t.length;
            EntryIterator entryIterator = new EntryIterator(t, f, 0, f, m);
            return entryIterator;
        }

        public boolean add(Map.Entry<K, V> e) {
            return this.map.putVal(e.getKey(), e.getValue(), false) == null;
        }

        public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
            boolean added = false;
            for (Map.Entry<K, V> e : c) {
                if (add(e)) {
                    added = true;
                }
            }
            return added;
        }

        public boolean removeIf(Predicate<? super Map.Entry<K, V>> filter) {
            return this.map.removeEntryIf(filter);
        }

        public final int hashCode() {
            int h = 0;
            Node<K, V>[] nodeArr = this.map.table;
            Node<K, V>[] t = nodeArr;
            if (nodeArr != null) {
                Traverser<K, V> it = new Traverser<>(t, t.length, 0, t.length);
                while (true) {
                    Node<K, V> advance = it.advance();
                    Node<K, V> p = advance;
                    if (advance == null) {
                        break;
                    }
                    h += p.hashCode();
                }
            }
            return h;
        }

        public final boolean equals(Object o) {
            if (o instanceof Set) {
                Set set = (Set) o;
                Set set2 = set;
                if (set == this || (containsAll(set2) && set2.containsAll(this))) {
                    return true;
                }
            }
            return false;
        }

        public Spliterator<Map.Entry<K, V>> spliterator() {
            ConcurrentHashMap<K, V> m = this.map;
            long n = m.sumCount();
            Node<K, V>[] nodeArr = m.table;
            Node<K, V>[] t = nodeArr;
            int f = nodeArr == null ? 0 : t.length;
            EntrySpliterator entrySpliterator = new EntrySpliterator(t, f, 0, f, n < 0 ? 0 : n, m);
            return entrySpliterator;
        }

        public void forEach(Consumer<? super Map.Entry<K, V>> action) {
            if (action != null) {
                Node<K, V>[] nodeArr = this.map.table;
                Node<K, V>[] t = nodeArr;
                if (nodeArr != null) {
                    Traverser<K, V> it = new Traverser<>(t, t.length, 0, t.length);
                    while (true) {
                        Node<K, V> advance = it.advance();
                        Node<K, V> p = advance;
                        if (advance != null) {
                            action.accept(new MapEntry(p.key, p.val, this.map));
                        } else {
                            return;
                        }
                    }
                }
            } else {
                throw new NullPointerException();
            }
        }
    }

    static final class EntrySpliterator<K, V> extends Traverser<K, V> implements Spliterator<Map.Entry<K, V>> {
        long est;
        final ConcurrentHashMap<K, V> map;

        EntrySpliterator(Node<K, V>[] tab, int size, int index, int limit, long est2, ConcurrentHashMap<K, V> map2) {
            super(tab, size, index, limit);
            this.map = map2;
            this.est = est2;
        }

        public EntrySpliterator<K, V> trySplit() {
            int i = this.baseIndex;
            int i2 = i;
            int i3 = this.baseLimit;
            int f = i3;
            int i4 = (i + i3) >>> 1;
            int h = i4;
            if (i4 <= i2) {
                return null;
            }
            Node[] nodeArr = this.tab;
            int i5 = this.baseSize;
            this.baseLimit = h;
            long j = this.est >>> 1;
            this.est = j;
            EntrySpliterator entrySpliterator = new EntrySpliterator(nodeArr, i5, h, f, j, this.map);
            return entrySpliterator;
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            if (action != null) {
                while (true) {
                    Node<K, V> advance = advance();
                    Node<K, V> p = advance;
                    if (advance != null) {
                        action.accept(new MapEntry(p.key, p.val, this.map));
                    } else {
                        return;
                    }
                }
            } else {
                throw new NullPointerException();
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            if (action != null) {
                Node<K, V> advance = advance();
                Node<K, V> p = advance;
                if (advance == null) {
                    return false;
                }
                action.accept(new MapEntry(p.key, p.val, this.map));
                return true;
            }
            throw new NullPointerException();
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return 4353;
        }
    }

    static final class ForEachEntryTask<K, V> extends BulkTask<K, V, Void> {
        final Consumer<? super Map.Entry<K, V>> action;

        ForEachEntryTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Consumer<? super Map.Entry<K, V>> action2) {
            super(p, b, i, f, t);
            this.action = action2;
        }

        public final void compute() {
            Consumer<? super Map.Entry<K, V>> consumer = this.action;
            Consumer<? super Map.Entry<K, V>> action2 = consumer;
            if (consumer != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int i2 = this.baseLimit;
                    int f = i2;
                    int i3 = (i2 + i) >>> 1;
                    int h = i3;
                    if (i3 <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i4 = this.batch >>> 1;
                    this.batch = i4;
                    this.baseLimit = h;
                    ForEachEntryTask forEachEntryTask = new ForEachEntryTask(this, i4, h, f, this.tab, action2);
                    forEachEntryTask.fork();
                }
                while (true) {
                    Node<K, V> advance = advance();
                    Node<K, V> p = advance;
                    if (advance != null) {
                        action2.accept(p);
                    } else {
                        propagateCompletion();
                        return;
                    }
                }
            }
        }
    }

    static final class ForEachKeyTask<K, V> extends BulkTask<K, V, Void> {
        final Consumer<? super K> action;

        ForEachKeyTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Consumer<? super K> action2) {
            super(p, b, i, f, t);
            this.action = action2;
        }

        public final void compute() {
            Consumer<? super K> consumer = this.action;
            Consumer<? super K> action2 = consumer;
            if (consumer != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int i2 = this.baseLimit;
                    int f = i2;
                    int i3 = (i2 + i) >>> 1;
                    int h = i3;
                    if (i3 <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i4 = this.batch >>> 1;
                    this.batch = i4;
                    this.baseLimit = h;
                    ForEachKeyTask forEachKeyTask = new ForEachKeyTask(this, i4, h, f, this.tab, action2);
                    forEachKeyTask.fork();
                }
                while (true) {
                    Node<K, V> advance = advance();
                    Node<K, V> p = advance;
                    if (advance != null) {
                        action2.accept(p.key);
                    } else {
                        propagateCompletion();
                        return;
                    }
                }
            }
        }
    }

    static final class ForEachMappingTask<K, V> extends BulkTask<K, V, Void> {
        final BiConsumer<? super K, ? super V> action;

        ForEachMappingTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, BiConsumer<? super K, ? super V> action2) {
            super(p, b, i, f, t);
            this.action = action2;
        }

        public final void compute() {
            BiConsumer<? super K, ? super V> biConsumer = this.action;
            BiConsumer<? super K, ? super V> action2 = biConsumer;
            if (biConsumer != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int i2 = this.baseLimit;
                    int f = i2;
                    int i3 = (i2 + i) >>> 1;
                    int h = i3;
                    if (i3 <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i4 = this.batch >>> 1;
                    this.batch = i4;
                    this.baseLimit = h;
                    ForEachMappingTask forEachMappingTask = new ForEachMappingTask(this, i4, h, f, this.tab, action2);
                    forEachMappingTask.fork();
                }
                while (true) {
                    Node<K, V> advance = advance();
                    Node<K, V> p = advance;
                    if (advance != null) {
                        action2.accept(p.key, p.val);
                    } else {
                        propagateCompletion();
                        return;
                    }
                }
            }
        }
    }

    static final class ForEachTransformedEntryTask<K, V, U> extends BulkTask<K, V, Void> {
        final Consumer<? super U> action;
        final Function<Map.Entry<K, V>, ? extends U> transformer;

        ForEachTransformedEntryTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<Map.Entry<K, V>, ? extends U> transformer2, Consumer<? super U> action2) {
            super(p, b, i, f, t);
            this.transformer = transformer2;
            this.action = action2;
        }

        public final void compute() {
            Function<Map.Entry<K, V>, ? extends U> function = this.transformer;
            Function<Map.Entry<K, V>, ? extends U> transformer2 = function;
            if (function != null) {
                Consumer<? super U> consumer = this.action;
                Consumer<? super U> action2 = consumer;
                if (consumer != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i4 = this.batch >>> 1;
                        this.batch = i4;
                        this.baseLimit = h;
                        ForEachTransformedEntryTask forEachTransformedEntryTask = new ForEachTransformedEntryTask(this, i4, h, f, this.tab, transformer2, action2);
                        forEachTransformedEntryTask.fork();
                    }
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance != null) {
                            U apply = transformer2.apply(p);
                            U u = apply;
                            if (apply != null) {
                                action2.accept(u);
                            }
                        } else {
                            propagateCompletion();
                            return;
                        }
                    }
                }
            }
        }
    }

    static final class ForEachTransformedKeyTask<K, V, U> extends BulkTask<K, V, Void> {
        final Consumer<? super U> action;
        final Function<? super K, ? extends U> transformer;

        ForEachTransformedKeyTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super K, ? extends U> transformer2, Consumer<? super U> action2) {
            super(p, b, i, f, t);
            this.transformer = transformer2;
            this.action = action2;
        }

        public final void compute() {
            Function<? super K, ? extends U> function = this.transformer;
            Function<? super K, ? extends U> transformer2 = function;
            if (function != null) {
                Consumer<? super U> consumer = this.action;
                Consumer<? super U> action2 = consumer;
                if (consumer != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i4 = this.batch >>> 1;
                        this.batch = i4;
                        this.baseLimit = h;
                        ForEachTransformedKeyTask forEachTransformedKeyTask = new ForEachTransformedKeyTask(this, i4, h, f, this.tab, transformer2, action2);
                        forEachTransformedKeyTask.fork();
                    }
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance != null) {
                            U apply = transformer2.apply(p.key);
                            U u = apply;
                            if (apply != null) {
                                action2.accept(u);
                            }
                        } else {
                            propagateCompletion();
                            return;
                        }
                    }
                }
            }
        }
    }

    static final class ForEachTransformedMappingTask<K, V, U> extends BulkTask<K, V, Void> {
        final Consumer<? super U> action;
        final BiFunction<? super K, ? super V, ? extends U> transformer;

        ForEachTransformedMappingTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, BiFunction<? super K, ? super V, ? extends U> transformer2, Consumer<? super U> action2) {
            super(p, b, i, f, t);
            this.transformer = transformer2;
            this.action = action2;
        }

        public final void compute() {
            BiFunction<? super K, ? super V, ? extends U> biFunction = this.transformer;
            BiFunction<? super K, ? super V, ? extends U> transformer2 = biFunction;
            if (biFunction != null) {
                Consumer<? super U> consumer = this.action;
                Consumer<? super U> action2 = consumer;
                if (consumer != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i4 = this.batch >>> 1;
                        this.batch = i4;
                        this.baseLimit = h;
                        ForEachTransformedMappingTask forEachTransformedMappingTask = new ForEachTransformedMappingTask(this, i4, h, f, this.tab, transformer2, action2);
                        forEachTransformedMappingTask.fork();
                    }
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance != null) {
                            U apply = transformer2.apply(p.key, p.val);
                            U u = apply;
                            if (apply != null) {
                                action2.accept(u);
                            }
                        } else {
                            propagateCompletion();
                            return;
                        }
                    }
                }
            }
        }
    }

    static final class ForEachTransformedValueTask<K, V, U> extends BulkTask<K, V, Void> {
        final Consumer<? super U> action;
        final Function<? super V, ? extends U> transformer;

        ForEachTransformedValueTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super V, ? extends U> transformer2, Consumer<? super U> action2) {
            super(p, b, i, f, t);
            this.transformer = transformer2;
            this.action = action2;
        }

        public final void compute() {
            Function<? super V, ? extends U> function = this.transformer;
            Function<? super V, ? extends U> transformer2 = function;
            if (function != null) {
                Consumer<? super U> consumer = this.action;
                Consumer<? super U> action2 = consumer;
                if (consumer != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i4 = this.batch >>> 1;
                        this.batch = i4;
                        this.baseLimit = h;
                        ForEachTransformedValueTask forEachTransformedValueTask = new ForEachTransformedValueTask(this, i4, h, f, this.tab, transformer2, action2);
                        forEachTransformedValueTask.fork();
                    }
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance != null) {
                            U apply = transformer2.apply(p.val);
                            U u = apply;
                            if (apply != null) {
                                action2.accept(u);
                            }
                        } else {
                            propagateCompletion();
                            return;
                        }
                    }
                }
            }
        }
    }

    static final class ForEachValueTask<K, V> extends BulkTask<K, V, Void> {
        final Consumer<? super V> action;

        ForEachValueTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Consumer<? super V> action2) {
            super(p, b, i, f, t);
            this.action = action2;
        }

        public final void compute() {
            Consumer<? super V> consumer = this.action;
            Consumer<? super V> action2 = consumer;
            if (consumer != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int i2 = this.baseLimit;
                    int f = i2;
                    int i3 = (i2 + i) >>> 1;
                    int h = i3;
                    if (i3 <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i4 = this.batch >>> 1;
                    this.batch = i4;
                    this.baseLimit = h;
                    ForEachValueTask forEachValueTask = new ForEachValueTask(this, i4, h, f, this.tab, action2);
                    forEachValueTask.fork();
                }
                while (true) {
                    Node<K, V> advance = advance();
                    Node<K, V> p = advance;
                    if (advance != null) {
                        action2.accept(p.val);
                    } else {
                        propagateCompletion();
                        return;
                    }
                }
            }
        }
    }

    static final class ForwardingNode<K, V> extends Node<K, V> {
        final Node<K, V>[] nextTable;

        ForwardingNode(Node<K, V>[] tab) {
            super(-1, null, null, null);
            this.nextTable = tab;
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x002e, code lost:
            if ((r2 instanceof java.util.concurrent.ConcurrentHashMap.ForwardingNode) == false) goto L_0x0036;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x0030, code lost:
            r0 = ((java.util.concurrent.ConcurrentHashMap.ForwardingNode) r2).nextTable;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x003a, code lost:
            return r2.find(r8, r9);
         */
        public Node<K, V> find(int h, Object k) {
            Node<K, V> e;
            Node<K, V>[] tab = this.nextTable;
            loop0:
            while (k != null && tab != null) {
                int length = tab.length;
                int n = length;
                if (length == 0) {
                    break;
                }
                Node<K, V> tabAt = ConcurrentHashMap.tabAt(tab, (n - 1) & h);
                Node<K, V> e2 = tabAt;
                if (tabAt == null) {
                    break;
                }
                while (true) {
                    e = e2;
                    int i = e.hash;
                    int eh = i;
                    if (i == h) {
                        K k2 = e.key;
                        K ek = k2;
                        if (k2 == k || (ek != null && k.equals(ek))) {
                            return e;
                        }
                    }
                    if (eh < 0) {
                        break;
                    }
                    Node<K, V> node = e.next;
                    Node<K, V> e3 = node;
                    if (node == null) {
                        return null;
                    }
                    e2 = e3;
                }
                return e;
            }
            return null;
        }
    }

    static final class KeyIterator<K, V> extends BaseIterator<K, V> implements Iterator<K>, Enumeration<K> {
        KeyIterator(Node<K, V>[] tab, int index, int size, int limit, ConcurrentHashMap<K, V> map) {
            super(tab, index, size, limit, map);
        }

        public final K next() {
            Node<K, V> node = this.next;
            Node<K, V> p = node;
            if (node != null) {
                K k = p.key;
                this.lastReturned = p;
                advance();
                return k;
            }
            throw new NoSuchElementException();
        }

        public final K nextElement() {
            return next();
        }
    }

    public static class KeySetView<K, V> extends CollectionView<K, V, K> implements Set<K>, Serializable {
        private static final long serialVersionUID = 7249069246763182397L;
        private final V value;

        public /* bridge */ /* synthetic */ ConcurrentHashMap getMap() {
            return super.getMap();
        }

        KeySetView(ConcurrentHashMap<K, V> map, V value2) {
            super(map);
            this.value = value2;
        }

        public V getMappedValue() {
            return this.value;
        }

        public boolean contains(Object o) {
            return this.map.containsKey(o);
        }

        public boolean remove(Object o) {
            return this.map.remove(o) != null;
        }

        public Iterator<K> iterator() {
            ConcurrentHashMap<K, V> m = this.map;
            Node<K, V>[] nodeArr = m.table;
            Node<K, V>[] t = nodeArr;
            int f = nodeArr == null ? 0 : t.length;
            KeyIterator keyIterator = new KeyIterator(t, f, 0, f, m);
            return keyIterator;
        }

        public boolean add(K e) {
            V v = this.value;
            V v2 = v;
            if (v != null) {
                return this.map.putVal(e, v2, true) == null;
            }
            throw new UnsupportedOperationException();
        }

        public boolean addAll(Collection<? extends K> c) {
            boolean added = false;
            V v = this.value;
            V v2 = v;
            if (v != null) {
                for (K e : c) {
                    if (this.map.putVal(e, v2, true) == null) {
                        added = true;
                    }
                }
                return added;
            }
            throw new UnsupportedOperationException();
        }

        public int hashCode() {
            int h = 0;
            Iterator it = iterator();
            while (it.hasNext()) {
                h += it.next().hashCode();
            }
            return h;
        }

        public boolean equals(Object o) {
            if (o instanceof Set) {
                Set set = (Set) o;
                Set set2 = set;
                if (set == this || (containsAll(set2) && set2.containsAll(this))) {
                    return true;
                }
            }
            return false;
        }

        public Spliterator<K> spliterator() {
            ConcurrentHashMap<K, V> m = this.map;
            long n = m.sumCount();
            Node<K, V>[] nodeArr = m.table;
            Node<K, V>[] t = nodeArr;
            int f = nodeArr == null ? 0 : t.length;
            KeySpliterator keySpliterator = new KeySpliterator(t, f, 0, f, n < 0 ? 0 : n);
            return keySpliterator;
        }

        public void forEach(Consumer<? super K> action) {
            if (action != null) {
                Node<K, V>[] nodeArr = this.map.table;
                Node<K, V>[] t = nodeArr;
                if (nodeArr != null) {
                    Traverser<K, V> it = new Traverser<>(t, t.length, 0, t.length);
                    while (true) {
                        Node<K, V> advance = it.advance();
                        Node<K, V> p = advance;
                        if (advance != null) {
                            action.accept(p.key);
                        } else {
                            return;
                        }
                    }
                }
            } else {
                throw new NullPointerException();
            }
        }
    }

    static final class KeySpliterator<K, V> extends Traverser<K, V> implements Spliterator<K> {
        long est;

        KeySpliterator(Node<K, V>[] tab, int size, int index, int limit, long est2) {
            super(tab, size, index, limit);
            this.est = est2;
        }

        public KeySpliterator<K, V> trySplit() {
            int i = this.baseIndex;
            int i2 = i;
            int i3 = this.baseLimit;
            int f = i3;
            int i4 = (i + i3) >>> 1;
            int h = i4;
            if (i4 <= i2) {
                return null;
            }
            Node[] nodeArr = this.tab;
            int i5 = this.baseSize;
            this.baseLimit = h;
            long j = this.est >>> 1;
            this.est = j;
            KeySpliterator keySpliterator = new KeySpliterator(nodeArr, i5, h, f, j);
            return keySpliterator;
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action != null) {
                while (true) {
                    Node<K, V> advance = advance();
                    Node<K, V> p = advance;
                    if (advance != null) {
                        action.accept(p.key);
                    } else {
                        return;
                    }
                }
            } else {
                throw new NullPointerException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action != null) {
                Node<K, V> advance = advance();
                Node<K, V> p = advance;
                if (advance == null) {
                    return false;
                }
                action.accept(p.key);
                return true;
            }
            throw new NullPointerException();
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return 4353;
        }
    }

    static final class MapEntry<K, V> implements Map.Entry<K, V> {
        final K key;
        final ConcurrentHashMap<K, V> map;
        V val;

        MapEntry(K key2, V val2, ConcurrentHashMap<K, V> map2) {
            this.key = key2;
            this.val = val2;
            this.map = map2;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.val;
        }

        public int hashCode() {
            return this.key.hashCode() ^ this.val.hashCode();
        }

        public String toString() {
            return Helpers.mapEntryToString(this.key, this.val);
        }

        public boolean equals(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry) o;
                Map.Entry<?, ?> e = entry;
                Object key2 = entry.getKey();
                Object k = key2;
                if (key2 != null) {
                    Object value = e.getValue();
                    Object v = value;
                    if (value != null && ((k == this.key || k.equals(this.key)) && (v == this.val || v.equals(this.val)))) {
                        return true;
                    }
                }
            }
            return false;
        }

        public V setValue(V value) {
            if (value != null) {
                V v = this.val;
                this.val = value;
                this.map.put(this.key, value);
                return v;
            }
            throw new NullPointerException();
        }
    }

    static final class MapReduceEntriesTask<K, V, U> extends BulkTask<K, V, U> {
        MapReduceEntriesTask<K, V, U> nextRight;
        final BiFunction<? super U, ? super U, ? extends U> reducer;
        U result;
        MapReduceEntriesTask<K, V, U> rights;
        final Function<Map.Entry<K, V>, ? extends U> transformer;

        MapReduceEntriesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesTask<K, V, U> nextRight2, Function<Map.Entry<K, V>, ? extends U> transformer2, BiFunction<? super U, ? super U, ? extends U> reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.reducer = reducer2;
        }

        public final U getRawResult() {
            return this.result;
        }

        public final void compute() {
            Function<Map.Entry<K, V>, ? extends U> function = this.transformer;
            Function<Map.Entry<K, V>, ? extends U> transformer2 = function;
            if (function != null) {
                BiFunction<? super U, ? super U, ? extends U> biFunction = this.reducer;
                BiFunction<? super U, ? super U, ? extends U> reducer2 = biFunction;
                if (biFunction != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i4 = this.batch >>> 1;
                        this.batch = i4;
                        this.baseLimit = h;
                        MapReduceEntriesTask mapReduceEntriesTask = new MapReduceEntriesTask(this, i4, h, f, this.tab, this.rights, transformer2, reducer2);
                        this.rights = mapReduceEntriesTask;
                        mapReduceEntriesTask.fork();
                    }
                    U r = null;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        U apply = transformer2.apply(p);
                        U u = apply;
                        if (apply != null) {
                            r = r == null ? u : reducer2.apply(r, u);
                        }
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceEntriesTask<K, V, U> t = (MapReduceEntriesTask) c;
                        MapReduceEntriesTask<K, V, U> s = t.rights;
                        while (s != null) {
                            U u2 = s.result;
                            U sr = u2;
                            if (u2 != null) {
                                U tr = t.result;
                                t.result = tr == null ? sr : reducer2.apply(tr, sr);
                            }
                            MapReduceEntriesTask<K, V, U> mapReduceEntriesTask2 = s.nextRight;
                            t.rights = mapReduceEntriesTask2;
                            s = mapReduceEntriesTask2;
                        }
                    }
                }
            }
        }
    }

    static final class MapReduceEntriesToDoubleTask<K, V> extends BulkTask<K, V, Double> {
        final double basis;
        MapReduceEntriesToDoubleTask<K, V> nextRight;
        final DoubleBinaryOperator reducer;
        double result;
        MapReduceEntriesToDoubleTask<K, V> rights;
        final ToDoubleFunction<Map.Entry<K, V>> transformer;

        MapReduceEntriesToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesToDoubleTask<K, V> nextRight2, ToDoubleFunction<Map.Entry<K, V>> transformer2, double basis2, DoubleBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Double getRawResult() {
            return Double.valueOf(this.result);
        }

        public final void compute() {
            ToDoubleFunction<Map.Entry<K, V>> toDoubleFunction = this.transformer;
            ToDoubleFunction<Map.Entry<K, V>> transformer2 = toDoubleFunction;
            if (toDoubleFunction != null) {
                DoubleBinaryOperator doubleBinaryOperator = this.reducer;
                DoubleBinaryOperator reducer2 = doubleBinaryOperator;
                if (doubleBinaryOperator != null) {
                    double r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        ToDoubleFunction<Map.Entry<K, V>> toDoubleFunction2 = transformer2;
                        int i6 = h;
                        ToDoubleFunction<Map.Entry<K, V>> transformer3 = transformer2;
                        MapReduceEntriesToDoubleTask<K, V> mapReduceEntriesToDoubleTask = r0;
                        MapReduceEntriesToDoubleTask<K, V> mapReduceEntriesToDoubleTask2 = new MapReduceEntriesToDoubleTask<>(this, i5, h, f, this.tab, this.rights, toDoubleFunction2, r, reducer2);
                        this.rights = mapReduceEntriesToDoubleTask;
                        mapReduceEntriesToDoubleTask.fork();
                        transformer2 = transformer3;
                        i = i2;
                    }
                    ToDoubleFunction<Map.Entry<K, V>> transformer4 = transformer2;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r = reducer2.applyAsDouble(r, transformer4.applyAsDouble(p));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceEntriesToDoubleTask<K, V> t = (MapReduceEntriesToDoubleTask) c;
                        MapReduceEntriesToDoubleTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsDouble(t.result, s.result);
                            MapReduceEntriesToDoubleTask<K, V> mapReduceEntriesToDoubleTask3 = s.nextRight;
                            t.rights = mapReduceEntriesToDoubleTask3;
                            s = mapReduceEntriesToDoubleTask3;
                        }
                    }
                    return;
                }
            }
        }
    }

    static final class MapReduceEntriesToIntTask<K, V> extends BulkTask<K, V, Integer> {
        final int basis;
        MapReduceEntriesToIntTask<K, V> nextRight;
        final IntBinaryOperator reducer;
        int result;
        MapReduceEntriesToIntTask<K, V> rights;
        final ToIntFunction<Map.Entry<K, V>> transformer;

        MapReduceEntriesToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesToIntTask<K, V> nextRight2, ToIntFunction<Map.Entry<K, V>> transformer2, int basis2, IntBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Integer getRawResult() {
            return Integer.valueOf(this.result);
        }

        public final void compute() {
            ToIntFunction<Map.Entry<K, V>> toIntFunction = this.transformer;
            ToIntFunction<Map.Entry<K, V>> transformer2 = toIntFunction;
            if (toIntFunction != null) {
                IntBinaryOperator intBinaryOperator = this.reducer;
                IntBinaryOperator reducer2 = intBinaryOperator;
                if (intBinaryOperator != null) {
                    int r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        int i6 = r;
                        int r2 = r;
                        MapReduceEntriesToIntTask<K, V> mapReduceEntriesToIntTask = r0;
                        MapReduceEntriesToIntTask<K, V> mapReduceEntriesToIntTask2 = new MapReduceEntriesToIntTask<>(this, i5, h, f, this.tab, this.rights, transformer2, i6, reducer2);
                        this.rights = mapReduceEntriesToIntTask;
                        mapReduceEntriesToIntTask.fork();
                        i = i2;
                        r = r2;
                    }
                    int r3 = r;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r3 = reducer2.applyAsInt(r3, transformer2.applyAsInt(p));
                    }
                    this.result = r3;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceEntriesToIntTask<K, V> t = (MapReduceEntriesToIntTask) c;
                        MapReduceEntriesToIntTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsInt(t.result, s.result);
                            MapReduceEntriesToIntTask<K, V> mapReduceEntriesToIntTask3 = s.nextRight;
                            t.rights = mapReduceEntriesToIntTask3;
                            s = mapReduceEntriesToIntTask3;
                        }
                    }
                }
            }
        }
    }

    static final class MapReduceEntriesToLongTask<K, V> extends BulkTask<K, V, Long> {
        final long basis;
        MapReduceEntriesToLongTask<K, V> nextRight;
        final LongBinaryOperator reducer;
        long result;
        MapReduceEntriesToLongTask<K, V> rights;
        final ToLongFunction<Map.Entry<K, V>> transformer;

        MapReduceEntriesToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesToLongTask<K, V> nextRight2, ToLongFunction<Map.Entry<K, V>> transformer2, long basis2, LongBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Long getRawResult() {
            return Long.valueOf(this.result);
        }

        public final void compute() {
            ToLongFunction<Map.Entry<K, V>> toLongFunction = this.transformer;
            ToLongFunction<Map.Entry<K, V>> transformer2 = toLongFunction;
            if (toLongFunction != null) {
                LongBinaryOperator longBinaryOperator = this.reducer;
                LongBinaryOperator reducer2 = longBinaryOperator;
                if (longBinaryOperator != null) {
                    long r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        ToLongFunction<Map.Entry<K, V>> toLongFunction2 = transformer2;
                        int i6 = h;
                        ToLongFunction<Map.Entry<K, V>> transformer3 = transformer2;
                        MapReduceEntriesToLongTask<K, V> mapReduceEntriesToLongTask = r0;
                        MapReduceEntriesToLongTask<K, V> mapReduceEntriesToLongTask2 = new MapReduceEntriesToLongTask<>(this, i5, h, f, this.tab, this.rights, toLongFunction2, r, reducer2);
                        this.rights = mapReduceEntriesToLongTask;
                        mapReduceEntriesToLongTask.fork();
                        transformer2 = transformer3;
                        i = i2;
                    }
                    ToLongFunction<Map.Entry<K, V>> transformer4 = transformer2;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r = reducer2.applyAsLong(r, transformer4.applyAsLong(p));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceEntriesToLongTask<K, V> t = (MapReduceEntriesToLongTask) c;
                        MapReduceEntriesToLongTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsLong(t.result, s.result);
                            MapReduceEntriesToLongTask<K, V> mapReduceEntriesToLongTask3 = s.nextRight;
                            t.rights = mapReduceEntriesToLongTask3;
                            s = mapReduceEntriesToLongTask3;
                        }
                    }
                    return;
                }
            }
        }
    }

    static final class MapReduceKeysTask<K, V, U> extends BulkTask<K, V, U> {
        MapReduceKeysTask<K, V, U> nextRight;
        final BiFunction<? super U, ? super U, ? extends U> reducer;
        U result;
        MapReduceKeysTask<K, V, U> rights;
        final Function<? super K, ? extends U> transformer;

        MapReduceKeysTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysTask<K, V, U> nextRight2, Function<? super K, ? extends U> transformer2, BiFunction<? super U, ? super U, ? extends U> reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.reducer = reducer2;
        }

        public final U getRawResult() {
            return this.result;
        }

        public final void compute() {
            Function<? super K, ? extends U> function = this.transformer;
            Function<? super K, ? extends U> transformer2 = function;
            if (function != null) {
                BiFunction<? super U, ? super U, ? extends U> biFunction = this.reducer;
                BiFunction<? super U, ? super U, ? extends U> reducer2 = biFunction;
                if (biFunction != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i4 = this.batch >>> 1;
                        this.batch = i4;
                        this.baseLimit = h;
                        MapReduceKeysTask mapReduceKeysTask = new MapReduceKeysTask(this, i4, h, f, this.tab, this.rights, transformer2, reducer2);
                        this.rights = mapReduceKeysTask;
                        mapReduceKeysTask.fork();
                    }
                    U r = null;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        U apply = transformer2.apply(p.key);
                        U u = apply;
                        if (apply != null) {
                            r = r == null ? u : reducer2.apply(r, u);
                        }
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceKeysTask<K, V, U> t = (MapReduceKeysTask) c;
                        MapReduceKeysTask<K, V, U> s = t.rights;
                        while (s != null) {
                            U u2 = s.result;
                            U sr = u2;
                            if (u2 != null) {
                                U tr = t.result;
                                t.result = tr == null ? sr : reducer2.apply(tr, sr);
                            }
                            MapReduceKeysTask<K, V, U> mapReduceKeysTask2 = s.nextRight;
                            t.rights = mapReduceKeysTask2;
                            s = mapReduceKeysTask2;
                        }
                    }
                }
            }
        }
    }

    static final class MapReduceKeysToDoubleTask<K, V> extends BulkTask<K, V, Double> {
        final double basis;
        MapReduceKeysToDoubleTask<K, V> nextRight;
        final DoubleBinaryOperator reducer;
        double result;
        MapReduceKeysToDoubleTask<K, V> rights;
        final ToDoubleFunction<? super K> transformer;

        MapReduceKeysToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysToDoubleTask<K, V> nextRight2, ToDoubleFunction<? super K> transformer2, double basis2, DoubleBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Double getRawResult() {
            return Double.valueOf(this.result);
        }

        public final void compute() {
            ToDoubleFunction<? super K> toDoubleFunction = this.transformer;
            ToDoubleFunction<? super K> transformer2 = toDoubleFunction;
            if (toDoubleFunction != null) {
                DoubleBinaryOperator doubleBinaryOperator = this.reducer;
                DoubleBinaryOperator reducer2 = doubleBinaryOperator;
                if (doubleBinaryOperator != null) {
                    double r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        ToDoubleFunction<? super K> toDoubleFunction2 = transformer2;
                        int i6 = h;
                        ToDoubleFunction<? super K> transformer3 = transformer2;
                        MapReduceKeysToDoubleTask<K, V> mapReduceKeysToDoubleTask = r0;
                        MapReduceKeysToDoubleTask<K, V> mapReduceKeysToDoubleTask2 = new MapReduceKeysToDoubleTask<>(this, i5, h, f, this.tab, this.rights, toDoubleFunction2, r, reducer2);
                        this.rights = mapReduceKeysToDoubleTask;
                        mapReduceKeysToDoubleTask.fork();
                        transformer2 = transformer3;
                        i = i2;
                    }
                    ToDoubleFunction<? super K> transformer4 = transformer2;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r = reducer2.applyAsDouble(r, transformer4.applyAsDouble(p.key));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceKeysToDoubleTask<K, V> t = (MapReduceKeysToDoubleTask) c;
                        MapReduceKeysToDoubleTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsDouble(t.result, s.result);
                            MapReduceKeysToDoubleTask<K, V> mapReduceKeysToDoubleTask3 = s.nextRight;
                            t.rights = mapReduceKeysToDoubleTask3;
                            s = mapReduceKeysToDoubleTask3;
                        }
                    }
                    return;
                }
            }
        }
    }

    static final class MapReduceKeysToIntTask<K, V> extends BulkTask<K, V, Integer> {
        final int basis;
        MapReduceKeysToIntTask<K, V> nextRight;
        final IntBinaryOperator reducer;
        int result;
        MapReduceKeysToIntTask<K, V> rights;
        final ToIntFunction<? super K> transformer;

        MapReduceKeysToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysToIntTask<K, V> nextRight2, ToIntFunction<? super K> transformer2, int basis2, IntBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Integer getRawResult() {
            return Integer.valueOf(this.result);
        }

        public final void compute() {
            ToIntFunction<? super K> toIntFunction = this.transformer;
            ToIntFunction<? super K> transformer2 = toIntFunction;
            if (toIntFunction != null) {
                IntBinaryOperator intBinaryOperator = this.reducer;
                IntBinaryOperator reducer2 = intBinaryOperator;
                if (intBinaryOperator != null) {
                    int r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        int i6 = r;
                        int r2 = r;
                        MapReduceKeysToIntTask<K, V> mapReduceKeysToIntTask = r0;
                        MapReduceKeysToIntTask<K, V> mapReduceKeysToIntTask2 = new MapReduceKeysToIntTask<>(this, i5, h, f, this.tab, this.rights, transformer2, i6, reducer2);
                        this.rights = mapReduceKeysToIntTask;
                        mapReduceKeysToIntTask.fork();
                        i = i2;
                        r = r2;
                    }
                    int r3 = r;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r3 = reducer2.applyAsInt(r3, transformer2.applyAsInt(p.key));
                    }
                    this.result = r3;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceKeysToIntTask<K, V> t = (MapReduceKeysToIntTask) c;
                        MapReduceKeysToIntTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsInt(t.result, s.result);
                            MapReduceKeysToIntTask<K, V> mapReduceKeysToIntTask3 = s.nextRight;
                            t.rights = mapReduceKeysToIntTask3;
                            s = mapReduceKeysToIntTask3;
                        }
                    }
                }
            }
        }
    }

    static final class MapReduceKeysToLongTask<K, V> extends BulkTask<K, V, Long> {
        final long basis;
        MapReduceKeysToLongTask<K, V> nextRight;
        final LongBinaryOperator reducer;
        long result;
        MapReduceKeysToLongTask<K, V> rights;
        final ToLongFunction<? super K> transformer;

        MapReduceKeysToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysToLongTask<K, V> nextRight2, ToLongFunction<? super K> transformer2, long basis2, LongBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Long getRawResult() {
            return Long.valueOf(this.result);
        }

        public final void compute() {
            ToLongFunction<? super K> toLongFunction = this.transformer;
            ToLongFunction<? super K> transformer2 = toLongFunction;
            if (toLongFunction != null) {
                LongBinaryOperator longBinaryOperator = this.reducer;
                LongBinaryOperator reducer2 = longBinaryOperator;
                if (longBinaryOperator != null) {
                    long r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        ToLongFunction<? super K> toLongFunction2 = transformer2;
                        int i6 = h;
                        ToLongFunction<? super K> transformer3 = transformer2;
                        MapReduceKeysToLongTask<K, V> mapReduceKeysToLongTask = r0;
                        MapReduceKeysToLongTask<K, V> mapReduceKeysToLongTask2 = new MapReduceKeysToLongTask<>(this, i5, h, f, this.tab, this.rights, toLongFunction2, r, reducer2);
                        this.rights = mapReduceKeysToLongTask;
                        mapReduceKeysToLongTask.fork();
                        transformer2 = transformer3;
                        i = i2;
                    }
                    ToLongFunction<? super K> transformer4 = transformer2;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r = reducer2.applyAsLong(r, transformer4.applyAsLong(p.key));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceKeysToLongTask<K, V> t = (MapReduceKeysToLongTask) c;
                        MapReduceKeysToLongTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsLong(t.result, s.result);
                            MapReduceKeysToLongTask<K, V> mapReduceKeysToLongTask3 = s.nextRight;
                            t.rights = mapReduceKeysToLongTask3;
                            s = mapReduceKeysToLongTask3;
                        }
                    }
                    return;
                }
            }
        }
    }

    static final class MapReduceMappingsTask<K, V, U> extends BulkTask<K, V, U> {
        MapReduceMappingsTask<K, V, U> nextRight;
        final BiFunction<? super U, ? super U, ? extends U> reducer;
        U result;
        MapReduceMappingsTask<K, V, U> rights;
        final BiFunction<? super K, ? super V, ? extends U> transformer;

        MapReduceMappingsTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsTask<K, V, U> nextRight2, BiFunction<? super K, ? super V, ? extends U> transformer2, BiFunction<? super U, ? super U, ? extends U> reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.reducer = reducer2;
        }

        public final U getRawResult() {
            return this.result;
        }

        public final void compute() {
            BiFunction<? super K, ? super V, ? extends U> biFunction = this.transformer;
            BiFunction<? super K, ? super V, ? extends U> transformer2 = biFunction;
            if (biFunction != null) {
                BiFunction<? super U, ? super U, ? extends U> biFunction2 = this.reducer;
                BiFunction<? super U, ? super U, ? extends U> reducer2 = biFunction2;
                if (biFunction2 != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i4 = this.batch >>> 1;
                        this.batch = i4;
                        this.baseLimit = h;
                        MapReduceMappingsTask mapReduceMappingsTask = new MapReduceMappingsTask(this, i4, h, f, this.tab, this.rights, transformer2, reducer2);
                        this.rights = mapReduceMappingsTask;
                        mapReduceMappingsTask.fork();
                    }
                    U r = null;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        U apply = transformer2.apply(p.key, p.val);
                        U u = apply;
                        if (apply != null) {
                            r = r == null ? u : reducer2.apply(r, u);
                        }
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceMappingsTask<K, V, U> t = (MapReduceMappingsTask) c;
                        MapReduceMappingsTask<K, V, U> s = t.rights;
                        while (s != null) {
                            U u2 = s.result;
                            U sr = u2;
                            if (u2 != null) {
                                U tr = t.result;
                                t.result = tr == null ? sr : reducer2.apply(tr, sr);
                            }
                            MapReduceMappingsTask<K, V, U> mapReduceMappingsTask2 = s.nextRight;
                            t.rights = mapReduceMappingsTask2;
                            s = mapReduceMappingsTask2;
                        }
                    }
                }
            }
        }
    }

    static final class MapReduceMappingsToDoubleTask<K, V> extends BulkTask<K, V, Double> {
        final double basis;
        MapReduceMappingsToDoubleTask<K, V> nextRight;
        final DoubleBinaryOperator reducer;
        double result;
        MapReduceMappingsToDoubleTask<K, V> rights;
        final ToDoubleBiFunction<? super K, ? super V> transformer;

        MapReduceMappingsToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsToDoubleTask<K, V> nextRight2, ToDoubleBiFunction<? super K, ? super V> transformer2, double basis2, DoubleBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Double getRawResult() {
            return Double.valueOf(this.result);
        }

        public final void compute() {
            ToDoubleBiFunction<? super K, ? super V> toDoubleBiFunction = this.transformer;
            ToDoubleBiFunction<? super K, ? super V> transformer2 = toDoubleBiFunction;
            if (toDoubleBiFunction != null) {
                DoubleBinaryOperator doubleBinaryOperator = this.reducer;
                DoubleBinaryOperator reducer2 = doubleBinaryOperator;
                if (doubleBinaryOperator != null) {
                    double r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        ToDoubleBiFunction<? super K, ? super V> toDoubleBiFunction2 = transformer2;
                        int i6 = h;
                        ToDoubleBiFunction<? super K, ? super V> transformer3 = transformer2;
                        MapReduceMappingsToDoubleTask<K, V> mapReduceMappingsToDoubleTask = r0;
                        MapReduceMappingsToDoubleTask<K, V> mapReduceMappingsToDoubleTask2 = new MapReduceMappingsToDoubleTask<>(this, i5, h, f, this.tab, this.rights, toDoubleBiFunction2, r, reducer2);
                        this.rights = mapReduceMappingsToDoubleTask;
                        mapReduceMappingsToDoubleTask.fork();
                        transformer2 = transformer3;
                        i = i2;
                    }
                    ToDoubleBiFunction<? super K, ? super V> transformer4 = transformer2;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r = reducer2.applyAsDouble(r, transformer4.applyAsDouble(p.key, p.val));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceMappingsToDoubleTask<K, V> t = (MapReduceMappingsToDoubleTask) c;
                        MapReduceMappingsToDoubleTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsDouble(t.result, s.result);
                            MapReduceMappingsToDoubleTask<K, V> mapReduceMappingsToDoubleTask3 = s.nextRight;
                            t.rights = mapReduceMappingsToDoubleTask3;
                            s = mapReduceMappingsToDoubleTask3;
                        }
                    }
                    return;
                }
            }
        }
    }

    static final class MapReduceMappingsToIntTask<K, V> extends BulkTask<K, V, Integer> {
        final int basis;
        MapReduceMappingsToIntTask<K, V> nextRight;
        final IntBinaryOperator reducer;
        int result;
        MapReduceMappingsToIntTask<K, V> rights;
        final ToIntBiFunction<? super K, ? super V> transformer;

        MapReduceMappingsToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsToIntTask<K, V> nextRight2, ToIntBiFunction<? super K, ? super V> transformer2, int basis2, IntBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Integer getRawResult() {
            return Integer.valueOf(this.result);
        }

        public final void compute() {
            ToIntBiFunction<? super K, ? super V> toIntBiFunction = this.transformer;
            ToIntBiFunction<? super K, ? super V> transformer2 = toIntBiFunction;
            if (toIntBiFunction != null) {
                IntBinaryOperator intBinaryOperator = this.reducer;
                IntBinaryOperator reducer2 = intBinaryOperator;
                if (intBinaryOperator != null) {
                    int r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        int i6 = r;
                        int r2 = r;
                        MapReduceMappingsToIntTask<K, V> mapReduceMappingsToIntTask = r0;
                        MapReduceMappingsToIntTask<K, V> mapReduceMappingsToIntTask2 = new MapReduceMappingsToIntTask<>(this, i5, h, f, this.tab, this.rights, transformer2, i6, reducer2);
                        this.rights = mapReduceMappingsToIntTask;
                        mapReduceMappingsToIntTask.fork();
                        i = i2;
                        r = r2;
                    }
                    int r3 = r;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r3 = reducer2.applyAsInt(r3, transformer2.applyAsInt(p.key, p.val));
                    }
                    this.result = r3;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceMappingsToIntTask<K, V> t = (MapReduceMappingsToIntTask) c;
                        MapReduceMappingsToIntTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsInt(t.result, s.result);
                            MapReduceMappingsToIntTask<K, V> mapReduceMappingsToIntTask3 = s.nextRight;
                            t.rights = mapReduceMappingsToIntTask3;
                            s = mapReduceMappingsToIntTask3;
                        }
                    }
                }
            }
        }
    }

    static final class MapReduceMappingsToLongTask<K, V> extends BulkTask<K, V, Long> {
        final long basis;
        MapReduceMappingsToLongTask<K, V> nextRight;
        final LongBinaryOperator reducer;
        long result;
        MapReduceMappingsToLongTask<K, V> rights;
        final ToLongBiFunction<? super K, ? super V> transformer;

        MapReduceMappingsToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsToLongTask<K, V> nextRight2, ToLongBiFunction<? super K, ? super V> transformer2, long basis2, LongBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Long getRawResult() {
            return Long.valueOf(this.result);
        }

        public final void compute() {
            ToLongBiFunction<? super K, ? super V> toLongBiFunction = this.transformer;
            ToLongBiFunction<? super K, ? super V> transformer2 = toLongBiFunction;
            if (toLongBiFunction != null) {
                LongBinaryOperator longBinaryOperator = this.reducer;
                LongBinaryOperator reducer2 = longBinaryOperator;
                if (longBinaryOperator != null) {
                    long r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        ToLongBiFunction<? super K, ? super V> toLongBiFunction2 = transformer2;
                        int i6 = h;
                        ToLongBiFunction<? super K, ? super V> transformer3 = transformer2;
                        MapReduceMappingsToLongTask<K, V> mapReduceMappingsToLongTask = r0;
                        MapReduceMappingsToLongTask<K, V> mapReduceMappingsToLongTask2 = new MapReduceMappingsToLongTask<>(this, i5, h, f, this.tab, this.rights, toLongBiFunction2, r, reducer2);
                        this.rights = mapReduceMappingsToLongTask;
                        mapReduceMappingsToLongTask.fork();
                        transformer2 = transformer3;
                        i = i2;
                    }
                    ToLongBiFunction<? super K, ? super V> transformer4 = transformer2;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r = reducer2.applyAsLong(r, transformer4.applyAsLong(p.key, p.val));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceMappingsToLongTask<K, V> t = (MapReduceMappingsToLongTask) c;
                        MapReduceMappingsToLongTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsLong(t.result, s.result);
                            MapReduceMappingsToLongTask<K, V> mapReduceMappingsToLongTask3 = s.nextRight;
                            t.rights = mapReduceMappingsToLongTask3;
                            s = mapReduceMappingsToLongTask3;
                        }
                    }
                    return;
                }
            }
        }
    }

    static final class MapReduceValuesTask<K, V, U> extends BulkTask<K, V, U> {
        MapReduceValuesTask<K, V, U> nextRight;
        final BiFunction<? super U, ? super U, ? extends U> reducer;
        U result;
        MapReduceValuesTask<K, V, U> rights;
        final Function<? super V, ? extends U> transformer;

        MapReduceValuesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesTask<K, V, U> nextRight2, Function<? super V, ? extends U> transformer2, BiFunction<? super U, ? super U, ? extends U> reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.reducer = reducer2;
        }

        public final U getRawResult() {
            return this.result;
        }

        public final void compute() {
            Function<? super V, ? extends U> function = this.transformer;
            Function<? super V, ? extends U> transformer2 = function;
            if (function != null) {
                BiFunction<? super U, ? super U, ? extends U> biFunction = this.reducer;
                BiFunction<? super U, ? super U, ? extends U> reducer2 = biFunction;
                if (biFunction != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i4 = this.batch >>> 1;
                        this.batch = i4;
                        this.baseLimit = h;
                        MapReduceValuesTask mapReduceValuesTask = new MapReduceValuesTask(this, i4, h, f, this.tab, this.rights, transformer2, reducer2);
                        this.rights = mapReduceValuesTask;
                        mapReduceValuesTask.fork();
                    }
                    U r = null;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        U apply = transformer2.apply(p.val);
                        U u = apply;
                        if (apply != null) {
                            r = r == null ? u : reducer2.apply(r, u);
                        }
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceValuesTask<K, V, U> t = (MapReduceValuesTask) c;
                        MapReduceValuesTask<K, V, U> s = t.rights;
                        while (s != null) {
                            U u2 = s.result;
                            U sr = u2;
                            if (u2 != null) {
                                U tr = t.result;
                                t.result = tr == null ? sr : reducer2.apply(tr, sr);
                            }
                            MapReduceValuesTask<K, V, U> mapReduceValuesTask2 = s.nextRight;
                            t.rights = mapReduceValuesTask2;
                            s = mapReduceValuesTask2;
                        }
                    }
                }
            }
        }
    }

    static final class MapReduceValuesToDoubleTask<K, V> extends BulkTask<K, V, Double> {
        final double basis;
        MapReduceValuesToDoubleTask<K, V> nextRight;
        final DoubleBinaryOperator reducer;
        double result;
        MapReduceValuesToDoubleTask<K, V> rights;
        final ToDoubleFunction<? super V> transformer;

        MapReduceValuesToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesToDoubleTask<K, V> nextRight2, ToDoubleFunction<? super V> transformer2, double basis2, DoubleBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Double getRawResult() {
            return Double.valueOf(this.result);
        }

        public final void compute() {
            ToDoubleFunction<? super V> toDoubleFunction = this.transformer;
            ToDoubleFunction<? super V> transformer2 = toDoubleFunction;
            if (toDoubleFunction != null) {
                DoubleBinaryOperator doubleBinaryOperator = this.reducer;
                DoubleBinaryOperator reducer2 = doubleBinaryOperator;
                if (doubleBinaryOperator != null) {
                    double r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        ToDoubleFunction<? super V> toDoubleFunction2 = transformer2;
                        int i6 = h;
                        ToDoubleFunction<? super V> transformer3 = transformer2;
                        MapReduceValuesToDoubleTask<K, V> mapReduceValuesToDoubleTask = r0;
                        MapReduceValuesToDoubleTask<K, V> mapReduceValuesToDoubleTask2 = new MapReduceValuesToDoubleTask<>(this, i5, h, f, this.tab, this.rights, toDoubleFunction2, r, reducer2);
                        this.rights = mapReduceValuesToDoubleTask;
                        mapReduceValuesToDoubleTask.fork();
                        transformer2 = transformer3;
                        i = i2;
                    }
                    ToDoubleFunction<? super V> transformer4 = transformer2;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r = reducer2.applyAsDouble(r, transformer4.applyAsDouble(p.val));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceValuesToDoubleTask<K, V> t = (MapReduceValuesToDoubleTask) c;
                        MapReduceValuesToDoubleTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsDouble(t.result, s.result);
                            MapReduceValuesToDoubleTask<K, V> mapReduceValuesToDoubleTask3 = s.nextRight;
                            t.rights = mapReduceValuesToDoubleTask3;
                            s = mapReduceValuesToDoubleTask3;
                        }
                    }
                    return;
                }
            }
        }
    }

    static final class MapReduceValuesToIntTask<K, V> extends BulkTask<K, V, Integer> {
        final int basis;
        MapReduceValuesToIntTask<K, V> nextRight;
        final IntBinaryOperator reducer;
        int result;
        MapReduceValuesToIntTask<K, V> rights;
        final ToIntFunction<? super V> transformer;

        MapReduceValuesToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesToIntTask<K, V> nextRight2, ToIntFunction<? super V> transformer2, int basis2, IntBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Integer getRawResult() {
            return Integer.valueOf(this.result);
        }

        public final void compute() {
            ToIntFunction<? super V> toIntFunction = this.transformer;
            ToIntFunction<? super V> transformer2 = toIntFunction;
            if (toIntFunction != null) {
                IntBinaryOperator intBinaryOperator = this.reducer;
                IntBinaryOperator reducer2 = intBinaryOperator;
                if (intBinaryOperator != null) {
                    int r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        int i6 = r;
                        int r2 = r;
                        MapReduceValuesToIntTask<K, V> mapReduceValuesToIntTask = r0;
                        MapReduceValuesToIntTask<K, V> mapReduceValuesToIntTask2 = new MapReduceValuesToIntTask<>(this, i5, h, f, this.tab, this.rights, transformer2, i6, reducer2);
                        this.rights = mapReduceValuesToIntTask;
                        mapReduceValuesToIntTask.fork();
                        i = i2;
                        r = r2;
                    }
                    int r3 = r;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r3 = reducer2.applyAsInt(r3, transformer2.applyAsInt(p.val));
                    }
                    this.result = r3;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceValuesToIntTask<K, V> t = (MapReduceValuesToIntTask) c;
                        MapReduceValuesToIntTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsInt(t.result, s.result);
                            MapReduceValuesToIntTask<K, V> mapReduceValuesToIntTask3 = s.nextRight;
                            t.rights = mapReduceValuesToIntTask3;
                            s = mapReduceValuesToIntTask3;
                        }
                    }
                }
            }
        }
    }

    static final class MapReduceValuesToLongTask<K, V> extends BulkTask<K, V, Long> {
        final long basis;
        MapReduceValuesToLongTask<K, V> nextRight;
        final LongBinaryOperator reducer;
        long result;
        MapReduceValuesToLongTask<K, V> rights;
        final ToLongFunction<? super V> transformer;

        MapReduceValuesToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesToLongTask<K, V> nextRight2, ToLongFunction<? super V> transformer2, long basis2, LongBinaryOperator reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.transformer = transformer2;
            this.basis = basis2;
            this.reducer = reducer2;
        }

        public final Long getRawResult() {
            return Long.valueOf(this.result);
        }

        public final void compute() {
            ToLongFunction<? super V> toLongFunction = this.transformer;
            ToLongFunction<? super V> transformer2 = toLongFunction;
            if (toLongFunction != null) {
                LongBinaryOperator longBinaryOperator = this.reducer;
                LongBinaryOperator reducer2 = longBinaryOperator;
                if (longBinaryOperator != null) {
                    long r = this.basis;
                    int i = this.baseIndex;
                    while (true) {
                        int i2 = i;
                        if (this.batch <= 0) {
                            break;
                        }
                        int i3 = this.baseLimit;
                        int f = i3;
                        int i4 = (i3 + i2) >>> 1;
                        int h = i4;
                        if (i4 <= i2) {
                            break;
                        }
                        addToPendingCount(1);
                        int i5 = this.batch >>> 1;
                        this.batch = i5;
                        this.baseLimit = h;
                        ToLongFunction<? super V> toLongFunction2 = transformer2;
                        int i6 = h;
                        ToLongFunction<? super V> transformer3 = transformer2;
                        MapReduceValuesToLongTask<K, V> mapReduceValuesToLongTask = r0;
                        MapReduceValuesToLongTask<K, V> mapReduceValuesToLongTask2 = new MapReduceValuesToLongTask<>(this, i5, h, f, this.tab, this.rights, toLongFunction2, r, reducer2);
                        this.rights = mapReduceValuesToLongTask;
                        mapReduceValuesToLongTask.fork();
                        transformer2 = transformer3;
                        i = i2;
                    }
                    ToLongFunction<? super V> transformer4 = transformer2;
                    while (true) {
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            break;
                        }
                        r = reducer2.applyAsLong(r, transformer4.applyAsLong(p.val));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceValuesToLongTask<K, V> t = (MapReduceValuesToLongTask) c;
                        MapReduceValuesToLongTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer2.applyAsLong(t.result, s.result);
                            MapReduceValuesToLongTask<K, V> mapReduceValuesToLongTask3 = s.nextRight;
                            t.rights = mapReduceValuesToLongTask3;
                            s = mapReduceValuesToLongTask3;
                        }
                    }
                    return;
                }
            }
        }
    }

    static class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        volatile Node<K, V> next;
        volatile V val;

        Node(int hash2, K key2, V val2, Node<K, V> next2) {
            this.hash = hash2;
            this.key = key2;
            this.val = val2;
            this.next = next2;
        }

        public final K getKey() {
            return this.key;
        }

        public final V getValue() {
            return this.val;
        }

        public final int hashCode() {
            return this.key.hashCode() ^ this.val.hashCode();
        }

        public final String toString() {
            return Helpers.mapEntryToString(this.key, this.val);
        }

        public final V setValue(V v) {
            throw new UnsupportedOperationException();
        }

        public final boolean equals(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry) o;
                Map.Entry<?, ?> e = entry;
                Object key2 = entry.getKey();
                Object k = key2;
                if (key2 != null) {
                    Object value = e.getValue();
                    Object v = value;
                    if (value != null && (k == this.key || k.equals(this.key))) {
                        Object obj = this.val;
                        Object u = obj;
                        if (v == obj || v.equals(u)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public Node<K, V> find(int h, Object k) {
            Node<K, V> node;
            Node<K, V> e = this;
            if (k != null) {
                do {
                    if (e.hash == h) {
                        K k2 = e.key;
                        K ek = k2;
                        if (k2 == k || (ek != null && k.equals(ek))) {
                            return e;
                        }
                    }
                    node = e.next;
                    e = node;
                } while (node != null);
            }
            return null;
        }
    }

    static final class ReduceEntriesTask<K, V> extends BulkTask<K, V, Map.Entry<K, V>> {
        ReduceEntriesTask<K, V> nextRight;
        final BiFunction<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer;
        Map.Entry<K, V> result;
        ReduceEntriesTask<K, V> rights;

        ReduceEntriesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, ReduceEntriesTask<K, V> nextRight2, BiFunction<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.reducer = reducer2;
        }

        public final Map.Entry<K, V> getRawResult() {
            return this.result;
        }

        public final void compute() {
            BiFunction<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> biFunction = this.reducer;
            BiFunction<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer2 = biFunction;
            if (biFunction != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int i2 = this.baseLimit;
                    int f = i2;
                    int i3 = (i2 + i) >>> 1;
                    int h = i3;
                    if (i3 <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i4 = this.batch >>> 1;
                    this.batch = i4;
                    this.baseLimit = h;
                    ReduceEntriesTask reduceEntriesTask = new ReduceEntriesTask(this, i4, h, f, this.tab, this.rights, reducer2);
                    this.rights = reduceEntriesTask;
                    reduceEntriesTask.fork();
                }
                Map.Entry<K, V> r = null;
                while (true) {
                    Node<K, V> advance = advance();
                    Node<K, V> p = advance;
                    if (advance == null) {
                        break;
                    }
                    r = r == null ? p : (Map.Entry) reducer2.apply(r, p);
                }
                this.result = r;
                for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                    ReduceEntriesTask<K, V> t = (ReduceEntriesTask) c;
                    ReduceEntriesTask<K, V> s = t.rights;
                    while (s != null) {
                        Map.Entry<K, V> entry = s.result;
                        Map.Entry<K, V> sr = entry;
                        if (entry != null) {
                            Map.Entry<K, V> tr = t.result;
                            t.result = tr == null ? sr : (Map.Entry) reducer2.apply(tr, sr);
                        }
                        ReduceEntriesTask<K, V> reduceEntriesTask2 = s.nextRight;
                        t.rights = reduceEntriesTask2;
                        s = reduceEntriesTask2;
                    }
                }
            }
        }
    }

    static final class ReduceKeysTask<K, V> extends BulkTask<K, V, K> {
        ReduceKeysTask<K, V> nextRight;
        final BiFunction<? super K, ? super K, ? extends K> reducer;
        K result;
        ReduceKeysTask<K, V> rights;

        ReduceKeysTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, ReduceKeysTask<K, V> nextRight2, BiFunction<? super K, ? super K, ? extends K> reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.reducer = reducer2;
        }

        public final K getRawResult() {
            return this.result;
        }

        public final void compute() {
            BiFunction<? super K, ? super K, ? extends K> biFunction = this.reducer;
            BiFunction<? super K, ? super K, ? extends K> reducer2 = biFunction;
            if (biFunction != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int i2 = this.baseLimit;
                    int f = i2;
                    int i3 = (i2 + i) >>> 1;
                    int h = i3;
                    if (i3 <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i4 = this.batch >>> 1;
                    this.batch = i4;
                    this.baseLimit = h;
                    ReduceKeysTask reduceKeysTask = new ReduceKeysTask(this, i4, h, f, this.tab, this.rights, reducer2);
                    this.rights = reduceKeysTask;
                    reduceKeysTask.fork();
                }
                K r = null;
                while (true) {
                    Node<K, V> advance = advance();
                    Node<K, V> p = advance;
                    if (advance == null) {
                        break;
                    }
                    K u = p.key;
                    r = r == null ? u : u == null ? r : reducer2.apply(r, u);
                }
                this.result = r;
                for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                    ReduceKeysTask<K, V> t = (ReduceKeysTask) c;
                    ReduceKeysTask<K, V> s = t.rights;
                    while (s != null) {
                        K k = s.result;
                        K sr = k;
                        if (k != null) {
                            K tr = t.result;
                            t.result = tr == null ? sr : reducer2.apply(tr, sr);
                        }
                        ReduceKeysTask<K, V> reduceKeysTask2 = s.nextRight;
                        t.rights = reduceKeysTask2;
                        s = reduceKeysTask2;
                    }
                }
            }
        }
    }

    static final class ReduceValuesTask<K, V> extends BulkTask<K, V, V> {
        ReduceValuesTask<K, V> nextRight;
        final BiFunction<? super V, ? super V, ? extends V> reducer;
        V result;
        ReduceValuesTask<K, V> rights;

        ReduceValuesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, ReduceValuesTask<K, V> nextRight2, BiFunction<? super V, ? super V, ? extends V> reducer2) {
            super(p, b, i, f, t);
            this.nextRight = nextRight2;
            this.reducer = reducer2;
        }

        public final V getRawResult() {
            return this.result;
        }

        public final void compute() {
            BiFunction<? super V, ? super V, ? extends V> biFunction = this.reducer;
            BiFunction<? super V, ? super V, ? extends V> reducer2 = biFunction;
            if (biFunction != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int i2 = this.baseLimit;
                    int f = i2;
                    int i3 = (i2 + i) >>> 1;
                    int h = i3;
                    if (i3 <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i4 = this.batch >>> 1;
                    this.batch = i4;
                    this.baseLimit = h;
                    ReduceValuesTask reduceValuesTask = new ReduceValuesTask(this, i4, h, f, this.tab, this.rights, reducer2);
                    this.rights = reduceValuesTask;
                    reduceValuesTask.fork();
                }
                V r = null;
                while (true) {
                    Node<K, V> advance = advance();
                    Node<K, V> p = advance;
                    if (advance == null) {
                        break;
                    }
                    V v = p.val;
                    r = r == null ? v : reducer2.apply(r, v);
                }
                this.result = r;
                for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                    ReduceValuesTask<K, V> t = (ReduceValuesTask) c;
                    ReduceValuesTask<K, V> s = t.rights;
                    while (s != null) {
                        V v2 = s.result;
                        V sr = v2;
                        if (v2 != null) {
                            V tr = t.result;
                            t.result = tr == null ? sr : reducer2.apply(tr, sr);
                        }
                        ReduceValuesTask<K, V> reduceValuesTask2 = s.nextRight;
                        t.rights = reduceValuesTask2;
                        s = reduceValuesTask2;
                    }
                }
            }
        }
    }

    static final class ReservationNode<K, V> extends Node<K, V> {
        ReservationNode() {
            super(-3, null, null, null);
        }

        /* access modifiers changed from: package-private */
        public Node<K, V> find(int h, Object k) {
            return null;
        }
    }

    static final class SearchEntriesTask<K, V, U> extends BulkTask<K, V, U> {
        final AtomicReference<U> result;
        final Function<Map.Entry<K, V>, ? extends U> searchFunction;

        SearchEntriesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<Map.Entry<K, V>, ? extends U> searchFunction2, AtomicReference<U> result2) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction2;
            this.result = result2;
        }

        public final U getRawResult() {
            return this.result.get();
        }

        public final void compute() {
            U apply;
            U u;
            Function<Map.Entry<K, V>, ? extends U> function = this.searchFunction;
            Function<Map.Entry<K, V>, ? extends U> searchFunction2 = function;
            if (function != null) {
                AtomicReference<U> atomicReference = this.result;
                AtomicReference<U> result2 = atomicReference;
                if (atomicReference != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        } else if (result2.get() == null) {
                            addToPendingCount(1);
                            int i4 = this.batch >>> 1;
                            this.batch = i4;
                            this.baseLimit = h;
                            SearchEntriesTask searchEntriesTask = new SearchEntriesTask(this, i4, h, f, this.tab, searchFunction2, result2);
                            searchEntriesTask.fork();
                        } else {
                            return;
                        }
                    }
                    do {
                        if (result2.get() == null) {
                            Node<K, V> advance = advance();
                            Node<K, V> p = advance;
                            if (advance == null) {
                                propagateCompletion();
                            } else {
                                apply = searchFunction2.apply(p);
                                u = apply;
                            }
                        }
                    } while (apply == null);
                    if (result2.compareAndSet(null, u)) {
                        quietlyCompleteRoot();
                    }
                }
            }
        }
    }

    static final class SearchKeysTask<K, V, U> extends BulkTask<K, V, U> {
        final AtomicReference<U> result;
        final Function<? super K, ? extends U> searchFunction;

        SearchKeysTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super K, ? extends U> searchFunction2, AtomicReference<U> result2) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction2;
            this.result = result2;
        }

        public final U getRawResult() {
            return this.result.get();
        }

        public final void compute() {
            Function<? super K, ? extends U> function = this.searchFunction;
            Function<? super K, ? extends U> searchFunction2 = function;
            if (function != null) {
                AtomicReference<U> atomicReference = this.result;
                AtomicReference<U> result2 = atomicReference;
                if (atomicReference != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        } else if (result2.get() == null) {
                            addToPendingCount(1);
                            int i4 = this.batch >>> 1;
                            this.batch = i4;
                            this.baseLimit = h;
                            SearchKeysTask searchKeysTask = new SearchKeysTask(this, i4, h, f, this.tab, searchFunction2, result2);
                            searchKeysTask.fork();
                        } else {
                            return;
                        }
                    }
                    while (true) {
                        if (result2.get() != null) {
                            break;
                        }
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            propagateCompletion();
                            break;
                        }
                        U apply = searchFunction2.apply(p.key);
                        U u = apply;
                        if (apply != null) {
                            if (result2.compareAndSet(null, u)) {
                                quietlyCompleteRoot();
                            }
                        }
                    }
                }
            }
        }
    }

    static final class SearchMappingsTask<K, V, U> extends BulkTask<K, V, U> {
        final AtomicReference<U> result;
        final BiFunction<? super K, ? super V, ? extends U> searchFunction;

        SearchMappingsTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, BiFunction<? super K, ? super V, ? extends U> searchFunction2, AtomicReference<U> result2) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction2;
            this.result = result2;
        }

        public final U getRawResult() {
            return this.result.get();
        }

        public final void compute() {
            BiFunction<? super K, ? super V, ? extends U> biFunction = this.searchFunction;
            BiFunction<? super K, ? super V, ? extends U> searchFunction2 = biFunction;
            if (biFunction != null) {
                AtomicReference<U> atomicReference = this.result;
                AtomicReference<U> result2 = atomicReference;
                if (atomicReference != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        } else if (result2.get() == null) {
                            addToPendingCount(1);
                            int i4 = this.batch >>> 1;
                            this.batch = i4;
                            this.baseLimit = h;
                            SearchMappingsTask searchMappingsTask = new SearchMappingsTask(this, i4, h, f, this.tab, searchFunction2, result2);
                            searchMappingsTask.fork();
                        } else {
                            return;
                        }
                    }
                    while (true) {
                        if (result2.get() != null) {
                            break;
                        }
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            propagateCompletion();
                            break;
                        }
                        U apply = searchFunction2.apply(p.key, p.val);
                        U u = apply;
                        if (apply != null) {
                            if (result2.compareAndSet(null, u)) {
                                quietlyCompleteRoot();
                            }
                        }
                    }
                }
            }
        }
    }

    static final class SearchValuesTask<K, V, U> extends BulkTask<K, V, U> {
        final AtomicReference<U> result;
        final Function<? super V, ? extends U> searchFunction;

        SearchValuesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super V, ? extends U> searchFunction2, AtomicReference<U> result2) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction2;
            this.result = result2;
        }

        public final U getRawResult() {
            return this.result.get();
        }

        public final void compute() {
            Function<? super V, ? extends U> function = this.searchFunction;
            Function<? super V, ? extends U> searchFunction2 = function;
            if (function != null) {
                AtomicReference<U> atomicReference = this.result;
                AtomicReference<U> result2 = atomicReference;
                if (atomicReference != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int i2 = this.baseLimit;
                        int f = i2;
                        int i3 = (i2 + i) >>> 1;
                        int h = i3;
                        if (i3 <= i) {
                            break;
                        } else if (result2.get() == null) {
                            addToPendingCount(1);
                            int i4 = this.batch >>> 1;
                            this.batch = i4;
                            this.baseLimit = h;
                            SearchValuesTask searchValuesTask = new SearchValuesTask(this, i4, h, f, this.tab, searchFunction2, result2);
                            searchValuesTask.fork();
                        } else {
                            return;
                        }
                    }
                    while (true) {
                        if (result2.get() != null) {
                            break;
                        }
                        Node<K, V> advance = advance();
                        Node<K, V> p = advance;
                        if (advance == null) {
                            propagateCompletion();
                            break;
                        }
                        U apply = searchFunction2.apply(p.val);
                        U u = apply;
                        if (apply != null) {
                            if (result2.compareAndSet(null, u)) {
                                quietlyCompleteRoot();
                            }
                        }
                    }
                }
            }
        }
    }

    static class Segment<K, V> extends ReentrantLock implements Serializable {
        private static final long serialVersionUID = 2249069246763182397L;
        final float loadFactor;

        Segment(float lf) {
            this.loadFactor = lf;
        }
    }

    static final class TableStack<K, V> {
        int index;
        int length;
        TableStack<K, V> next;
        Node<K, V>[] tab;

        TableStack() {
        }
    }

    static class Traverser<K, V> {
        int baseIndex;
        int baseLimit;
        final int baseSize;
        int index;
        Node<K, V> next = null;
        TableStack<K, V> spare;
        TableStack<K, V> stack;
        Node<K, V>[] tab;

        Traverser(Node<K, V>[] tab2, int size, int index2, int limit) {
            this.tab = tab2;
            this.baseSize = size;
            this.index = index2;
            this.baseIndex = index2;
            this.baseLimit = limit;
        }

        /* access modifiers changed from: package-private */
        public final Node<K, V> advance() {
            Node<K, V> e;
            Node<K, V> e2;
            Node<K, V> node = this.next;
            Node<K, V> e3 = node;
            if (node != null) {
                e3 = e3.next;
            }
            while (e == null) {
                if (this.baseIndex < this.baseLimit) {
                    Node<K, V>[] nodeArr = this.tab;
                    Node<K, V>[] t = nodeArr;
                    if (nodeArr != null) {
                        int length = t.length;
                        int n = length;
                        int i = this.index;
                        int i2 = i;
                        if (length > i && i2 >= 0) {
                            Node<K, V> tabAt = ConcurrentHashMap.tabAt(t, i2);
                            e = tabAt;
                            if (tabAt != null && e.hash < 0) {
                                if (e instanceof ForwardingNode) {
                                    this.tab = ((ForwardingNode) e).nextTable;
                                    e = null;
                                    pushState(t, i2, n);
                                } else {
                                    if (e instanceof TreeBin) {
                                        e2 = ((TreeBin) e).first;
                                    } else {
                                        e2 = null;
                                    }
                                    e = e2;
                                }
                            }
                            if (this.stack != null) {
                                recoverState(n);
                            } else {
                                int i3 = this.baseSize + i2;
                                this.index = i3;
                                if (i3 >= n) {
                                    int i4 = this.baseIndex + 1;
                                    this.baseIndex = i4;
                                    this.index = i4;
                                }
                            }
                        }
                    }
                }
                this.next = null;
                return null;
            }
            this.next = e;
            return e;
        }

        private void pushState(Node<K, V>[] t, int i, int n) {
            TableStack<K, V> s = this.spare;
            if (s != null) {
                this.spare = s.next;
            } else {
                s = new TableStack<>();
            }
            s.tab = t;
            s.length = n;
            s.index = i;
            s.next = this.stack;
            this.stack = s;
        }

        private void recoverState(int n) {
            TableStack<K, V> s;
            while (true) {
                TableStack<K, V> tableStack = this.stack;
                s = tableStack;
                if (tableStack == null) {
                    break;
                }
                int i = this.index;
                int i2 = s.length;
                int len = i2;
                int i3 = i + i2;
                this.index = i3;
                if (i3 < n) {
                    break;
                }
                n = len;
                this.index = s.index;
                this.tab = s.tab;
                s.tab = null;
                TableStack<K, V> next2 = s.next;
                s.next = this.spare;
                this.stack = next2;
                this.spare = s;
            }
            if (s == null) {
                int i4 = this.index + this.baseSize;
                this.index = i4;
                if (i4 >= n) {
                    int i5 = this.baseIndex + 1;
                    this.baseIndex = i5;
                    this.index = i5;
                }
            }
        }
    }

    static final class TreeBin<K, V> extends Node<K, V> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private static final long LOCKSTATE;
        static final int READER = 4;
        private static final Unsafe U = Unsafe.getUnsafe();
        static final int WAITER = 2;
        static final int WRITER = 1;
        volatile TreeNode<K, V> first;
        volatile int lockState;
        TreeNode<K, V> root;
        volatile Thread waiter;

        static {
            Class<ConcurrentHashMap> cls = ConcurrentHashMap.class;
            try {
                LOCKSTATE = U.objectFieldOffset(TreeBin.class.getDeclaredField("lockState"));
            } catch (ReflectiveOperationException e) {
                throw new Error((Throwable) e);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:3:0x0019, code lost:
            if (r0 == 0) goto L_0x001b;
         */
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (!(a == null || b == null)) {
                int compareTo = a.getClass().getName().compareTo(b.getClass().getName());
                d = compareTo;
            }
            d = System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1;
            return d;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0038, code lost:
            if (r9 != null) goto L_0x003a;
         */
        TreeBin(TreeNode<K, V> b) {
            super(-2, null, null, null);
            int dir;
            TreeNode<K, V> xp;
            TreeNode<K, V> treeNode;
            this.first = b;
            TreeNode<K, V> r = null;
            TreeNode<K, V> x = b;
            while (x != null) {
                TreeNode<K, V> next = (TreeNode) x.next;
                x.right = null;
                x.left = null;
                if (r == null) {
                    x.parent = null;
                    x.red = $assertionsDisabled;
                    r = x;
                } else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    TreeNode<K, V> p = r;
                    do {
                        K pk = p.key;
                        int i = p.hash;
                        int ph = i;
                        if (i > h) {
                            dir = -1;
                        } else if (ph < h) {
                            dir = 1;
                        } else {
                            if (kc == null) {
                                Class<?> comparableClassFor = ConcurrentHashMap.comparableClassFor(k);
                                kc = comparableClassFor;
                            }
                            int compareComparables = ConcurrentHashMap.compareComparables(kc, k, pk);
                            int dir2 = compareComparables;
                            if (compareComparables != 0) {
                                dir = dir2;
                            }
                            dir = tieBreakOrder(k, pk);
                        }
                        xp = p;
                        treeNode = dir <= 0 ? p.left : p.right;
                        p = treeNode;
                    } while (treeNode != null);
                    x.parent = xp;
                    if (dir <= 0) {
                        xp.left = x;
                    } else {
                        xp.right = x;
                    }
                    r = balanceInsertion(r, x);
                }
                x = next;
            }
            this.root = r;
        }

        private final void lockRoot() {
            if (!U.compareAndSwapInt(this, LOCKSTATE, 0, 1)) {
                contendedLock();
            }
        }

        private final void unlockRoot() {
            this.lockState = 0;
        }

        private final void contendedLock() {
            boolean waiting = $assertionsDisabled;
            while (true) {
                int i = this.lockState;
                int s = i;
                if ((i & -3) == 0) {
                    if (U.compareAndSwapInt(this, LOCKSTATE, s, 1)) {
                        break;
                    }
                } else if ((s & 2) == 0) {
                    if (U.compareAndSwapInt(this, LOCKSTATE, s, s | 2)) {
                        waiting = true;
                        this.waiter = Thread.currentThread();
                    }
                } else if (waiting) {
                    LockSupport.park(this);
                }
            }
            if (waiting) {
                this.waiter = null;
            }
        }

        /* access modifiers changed from: package-private */
        public final Node<K, V> find(int h, Object k) {
            TreeNode<K, V> p = null;
            if (k != null) {
                Node<K, V> e = this.first;
                while (e != null) {
                    int i = this.lockState;
                    int s = i;
                    if ((i & 3) != 0) {
                        if (e.hash == h) {
                            K k2 = e.key;
                            K ek = k2;
                            if (k2 == k || (ek != null && k.equals(ek))) {
                                return e;
                            }
                        }
                        e = e.next;
                    } else {
                        if (U.compareAndSwapInt(this, LOCKSTATE, s, s + 4)) {
                            try {
                                TreeNode<K, V> treeNode = this.root;
                                TreeNode<K, V> r = treeNode;
                                if (treeNode != null) {
                                    p = r.findTreeNode(h, k, null);
                                }
                                return p;
                            } finally {
                                if (U.getAndAddInt(this, LOCKSTATE, -4) == 6) {
                                    Thread thread = this.waiter;
                                    Thread w = thread;
                                    if (thread != null) {
                                        LockSupport.unpark(w);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0044, code lost:
            if (r2 != null) goto L_0x0046;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:29:0x0068, code lost:
            return r5;
         */
        public final TreeNode<K, V> putTreeVal(int h, K k, V v) {
            int dir;
            TreeNode<K, V> q;
            int i = h;
            K k2 = k;
            TreeNode<K, V> p = this.root;
            Class<?> kc = null;
            boolean searched = false;
            while (true) {
                TreeNode<K, V> p2 = p;
                if (p2 == null) {
                    TreeNode treeNode = new TreeNode(i, k2, v, null, null);
                    this.root = treeNode;
                    this.first = treeNode;
                    break;
                }
                int i2 = p2.hash;
                int ph = i2;
                if (i2 > i) {
                    dir = -1;
                } else if (ph < i) {
                    dir = 1;
                } else {
                    K k3 = p2.key;
                    K pk = k3;
                    if (k3 == k2 || (pk != null && k2.equals(pk))) {
                        return p2;
                    }
                    if (kc == null) {
                        Class<?> comparableClassFor = ConcurrentHashMap.comparableClassFor(k);
                        kc = comparableClassFor;
                    }
                    int compareComparables = ConcurrentHashMap.compareComparables(kc, k2, pk);
                    int dir2 = compareComparables;
                    if (compareComparables != 0) {
                        dir = dir2;
                    }
                    if (!searched) {
                        searched = true;
                        TreeNode<K, V> treeNode2 = p2.left;
                        TreeNode<K, V> ch = treeNode2;
                        if (treeNode2 != null) {
                            TreeNode<K, V> findTreeNode = ch.findTreeNode(i, k2, kc);
                            q = findTreeNode;
                            if (findTreeNode != null) {
                                break;
                            }
                        }
                        TreeNode<K, V> treeNode3 = p2.right;
                        TreeNode<K, V> ch2 = treeNode3;
                        if (treeNode3 != null) {
                            TreeNode<K, V> findTreeNode2 = ch2.findTreeNode(i, k2, kc);
                            q = findTreeNode2;
                            if (findTreeNode2 != null) {
                                break;
                            }
                        }
                    }
                    dir = tieBreakOrder(k2, pk);
                }
                Class<?> kc2 = kc;
                boolean searched2 = searched;
                int dir3 = dir;
                TreeNode<K, V> xp = p2;
                TreeNode<K, V> treeNode4 = dir3 <= 0 ? p2.left : p2.right;
                TreeNode<K, V> p3 = treeNode4;
                if (treeNode4 == null) {
                    TreeNode<K, V> f = this.first;
                    TreeNode<K, V> f2 = f;
                    TreeNode<K, V> x = new TreeNode<>(i, k2, v, f, xp);
                    this.first = x;
                    if (f2 != null) {
                        f2.prev = x;
                    }
                    if (dir3 <= 0) {
                        xp.left = x;
                    } else {
                        xp.right = x;
                    }
                    if (!xp.red) {
                        x.red = true;
                    } else {
                        lockRoot();
                        try {
                            this.root = balanceInsertion(this.root, x);
                        } finally {
                            unlockRoot();
                        }
                    }
                    boolean z = searched2;
                    Class<?> cls = kc2;
                } else {
                    searched = searched2;
                    p = p3;
                    kc = kc2;
                    i = h;
                }
            }
            return null;
        }

        /* JADX INFO: finally extract failed */
        /* access modifiers changed from: package-private */
        public final boolean removeTreeNode(TreeNode<K, V> p) {
            TreeNode<K, V> replacement;
            TreeNode<K, V> replacement2;
            TreeNode<K, V> next = (TreeNode) p.next;
            TreeNode<K, V> pred = p.prev;
            if (pred == null) {
                this.first = next;
            } else {
                pred.next = next;
            }
            if (next != null) {
                next.prev = pred;
            }
            if (this.first == null) {
                this.root = null;
                return true;
            }
            TreeNode<K, V> treeNode = this.root;
            TreeNode<K, V> r = treeNode;
            if (!(treeNode == null || r.right == null)) {
                TreeNode<K, V> treeNode2 = r.left;
                TreeNode<K, V> rl = treeNode2;
                if (!(treeNode2 == null || rl.left == null)) {
                    lockRoot();
                    try {
                        TreeNode<K, V> pl = p.left;
                        TreeNode<K, V> pr = p.right;
                        if (pl != null && pr != null) {
                            TreeNode<K, V> s = pr;
                            while (true) {
                                TreeNode<K, V> treeNode3 = s.left;
                                TreeNode<K, V> sl = treeNode3;
                                if (treeNode3 == null) {
                                    break;
                                }
                                s = sl;
                            }
                            boolean c = s.red;
                            s.red = p.red;
                            p.red = c;
                            TreeNode<K, V> sr = s.right;
                            TreeNode<K, V> pp = p.parent;
                            if (s == pr) {
                                p.parent = s;
                                s.right = p;
                            } else {
                                TreeNode<K, V> sp = s.parent;
                                p.parent = sp;
                                if (sp != null) {
                                    if (s == sp.left) {
                                        sp.left = p;
                                    } else {
                                        sp.right = p;
                                    }
                                }
                                s.right = pr;
                                if (pr != null) {
                                    pr.parent = s;
                                }
                            }
                            p.left = null;
                            p.right = sr;
                            if (sr != null) {
                                sr.parent = p;
                            }
                            s.left = pl;
                            if (pl != null) {
                                pl.parent = s;
                            }
                            s.parent = pp;
                            if (pp == null) {
                                r = s;
                            } else if (p == pp.left) {
                                pp.left = s;
                            } else {
                                pp.right = s;
                            }
                            if (sr != null) {
                                replacement2 = sr;
                            } else {
                                replacement2 = p;
                            }
                            replacement = replacement2;
                        } else if (pl != null) {
                            replacement = pl;
                        } else if (pr != null) {
                            replacement = pr;
                        } else {
                            replacement = p;
                        }
                        if (replacement != p) {
                            TreeNode<K, V> pp2 = p.parent;
                            replacement.parent = pp2;
                            if (pp2 == null) {
                                r = replacement;
                            } else if (p == pp2.left) {
                                pp2.left = replacement;
                            } else {
                                pp2.right = replacement;
                            }
                            p.parent = null;
                            p.right = null;
                            p.left = null;
                        }
                        this.root = p.red ? r : balanceDeletion(r, replacement);
                        if (p == replacement) {
                            TreeNode<K, V> treeNode4 = p.parent;
                            TreeNode<K, V> pp3 = treeNode4;
                            if (treeNode4 != null) {
                                if (p == pp3.left) {
                                    pp3.left = null;
                                } else if (p == pp3.right) {
                                    pp3.right = null;
                                }
                                p.parent = null;
                            }
                        }
                        unlockRoot();
                        return $assertionsDisabled;
                    } catch (Throwable th) {
                        unlockRoot();
                        throw th;
                    }
                }
            }
            return true;
        }

        static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root2, TreeNode<K, V> p) {
            if (p != null) {
                TreeNode<K, V> treeNode = p.right;
                TreeNode<K, V> r = treeNode;
                if (treeNode != null) {
                    TreeNode<K, V> treeNode2 = r.left;
                    p.right = treeNode2;
                    TreeNode<K, V> rl = treeNode2;
                    if (treeNode2 != null) {
                        rl.parent = p;
                    }
                    TreeNode<K, V> treeNode3 = p.parent;
                    r.parent = treeNode3;
                    TreeNode<K, V> pp = treeNode3;
                    if (treeNode3 == null) {
                        root2 = r;
                        r.red = $assertionsDisabled;
                    } else if (pp.left == p) {
                        pp.left = r;
                    } else {
                        pp.right = r;
                    }
                    r.left = p;
                    p.parent = r;
                }
            }
            return root2;
        }

        static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root2, TreeNode<K, V> p) {
            if (p != null) {
                TreeNode<K, V> treeNode = p.left;
                TreeNode<K, V> l = treeNode;
                if (treeNode != null) {
                    TreeNode<K, V> treeNode2 = l.right;
                    p.left = treeNode2;
                    TreeNode<K, V> lr = treeNode2;
                    if (treeNode2 != null) {
                        lr.parent = p;
                    }
                    TreeNode<K, V> treeNode3 = p.parent;
                    l.parent = treeNode3;
                    TreeNode<K, V> pp = treeNode3;
                    if (treeNode3 == null) {
                        root2 = l;
                        l.red = $assertionsDisabled;
                    } else if (pp.right == p) {
                        pp.right = l;
                    } else {
                        pp.left = l;
                    }
                    l.right = p;
                    p.parent = l;
                }
            }
            return root2;
        }

        static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root2, TreeNode<K, V> x) {
            x.red = true;
            while (true) {
                TreeNode<K, V> treeNode = x.parent;
                TreeNode<K, V> xp = treeNode;
                if (treeNode != null) {
                    if (!xp.red) {
                        break;
                    }
                    TreeNode<K, V> treeNode2 = xp.parent;
                    TreeNode<K, V> xpp = treeNode2;
                    if (treeNode2 == null) {
                        break;
                    }
                    TreeNode<K, V> treeNode3 = xpp.left;
                    TreeNode<K, V> xppl = treeNode3;
                    TreeNode<K, V> treeNode4 = null;
                    if (xp == treeNode3) {
                        TreeNode<K, V> treeNode5 = xpp.right;
                        TreeNode<K, V> xppr = treeNode5;
                        if (treeNode5 == null || !xppr.red) {
                            if (x == xp.right) {
                                x = xp;
                                root2 = rotateLeft(root2, xp);
                                TreeNode<K, V> treeNode6 = x.parent;
                                xp = treeNode6;
                                if (treeNode6 != null) {
                                    treeNode4 = xp.parent;
                                }
                                xpp = treeNode4;
                            }
                            if (xp != null) {
                                xp.red = $assertionsDisabled;
                                if (xpp != null) {
                                    xpp.red = true;
                                    root2 = rotateRight(root2, xpp);
                                }
                            }
                        } else {
                            xppr.red = $assertionsDisabled;
                            xp.red = $assertionsDisabled;
                            xpp.red = true;
                            x = xpp;
                        }
                    } else if (xppl == null || !xppl.red) {
                        if (x == xp.left) {
                            x = xp;
                            root2 = rotateRight(root2, xp);
                            TreeNode<K, V> treeNode7 = x.parent;
                            xp = treeNode7;
                            if (treeNode7 != null) {
                                treeNode4 = xp.parent;
                            }
                            xpp = treeNode4;
                        }
                        if (xp != null) {
                            xp.red = $assertionsDisabled;
                            if (xpp != null) {
                                xpp.red = true;
                                root2 = rotateLeft(root2, xpp);
                            }
                        }
                    } else {
                        xppl.red = $assertionsDisabled;
                        xp.red = $assertionsDisabled;
                        xpp.red = true;
                        x = xpp;
                    }
                } else {
                    x.red = $assertionsDisabled;
                    return x;
                }
            }
            return root2;
        }

        static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root2, TreeNode<K, V> x) {
            while (x != null && x != root2) {
                TreeNode<K, V> treeNode = x.parent;
                TreeNode<K, V> xp = treeNode;
                if (treeNode == null) {
                    x.red = $assertionsDisabled;
                    return x;
                } else if (x.red) {
                    x.red = $assertionsDisabled;
                    return root2;
                } else {
                    TreeNode<K, V> treeNode2 = xp.left;
                    TreeNode<K, V> xpl = treeNode2;
                    TreeNode<K, V> xpr = null;
                    if (treeNode2 == x) {
                        TreeNode<K, V> treeNode3 = xp.right;
                        TreeNode<K, V> xpr2 = treeNode3;
                        if (treeNode3 != null && xpr2.red) {
                            xpr2.red = $assertionsDisabled;
                            xp.red = true;
                            root2 = rotateLeft(root2, xp);
                            TreeNode<K, V> treeNode4 = x.parent;
                            xp = treeNode4;
                            xpr2 = treeNode4 == null ? null : xp.right;
                        }
                        if (xpr2 == null) {
                            x = xp;
                        } else {
                            TreeNode<K, V> sl = xpr2.left;
                            TreeNode<K, V> sr = xpr2.right;
                            if ((sr == null || !sr.red) && (sl == null || !sl.red)) {
                                xpr2.red = true;
                                x = xp;
                            } else {
                                if (sr == null || !sr.red) {
                                    if (sl != null) {
                                        sl.red = $assertionsDisabled;
                                    }
                                    xpr2.red = true;
                                    root2 = rotateRight(root2, xpr2);
                                    TreeNode<K, V> treeNode5 = x.parent;
                                    xp = treeNode5;
                                    if (treeNode5 != null) {
                                        xpr = xp.right;
                                    }
                                    xpr2 = xpr;
                                }
                                if (xpr2 != null) {
                                    xpr2.red = xp == null ? false : xp.red;
                                    TreeNode<K, V> treeNode6 = xpr2.right;
                                    TreeNode<K, V> sr2 = treeNode6;
                                    if (treeNode6 != null) {
                                        sr2.red = $assertionsDisabled;
                                    }
                                }
                                if (xp != null) {
                                    xp.red = $assertionsDisabled;
                                    root2 = rotateLeft(root2, xp);
                                }
                                x = root2;
                            }
                        }
                    } else {
                        if (xpl != null && xpl.red) {
                            xpl.red = $assertionsDisabled;
                            xp.red = true;
                            root2 = rotateRight(root2, xp);
                            TreeNode<K, V> treeNode7 = x.parent;
                            xp = treeNode7;
                            xpl = treeNode7 == null ? null : xp.left;
                        }
                        if (xpl == null) {
                            x = xp;
                        } else {
                            TreeNode<K, V> sl2 = xpl.left;
                            TreeNode<K, V> sr3 = xpl.right;
                            if ((sl2 == null || !sl2.red) && (sr3 == null || !sr3.red)) {
                                xpl.red = true;
                                x = xp;
                            } else {
                                if (sl2 == null || !sl2.red) {
                                    if (sr3 != null) {
                                        sr3.red = $assertionsDisabled;
                                    }
                                    xpl.red = true;
                                    root2 = rotateLeft(root2, xpl);
                                    TreeNode<K, V> treeNode8 = x.parent;
                                    xp = treeNode8;
                                    if (treeNode8 != null) {
                                        xpr = xp.left;
                                    }
                                    xpl = xpr;
                                }
                                if (xpl != null) {
                                    xpl.red = xp == null ? false : xp.red;
                                    TreeNode<K, V> treeNode9 = xpl.left;
                                    TreeNode<K, V> sl3 = treeNode9;
                                    if (treeNode9 != null) {
                                        sl3.red = $assertionsDisabled;
                                    }
                                }
                                if (xp != null) {
                                    xp.red = $assertionsDisabled;
                                    root2 = rotateRight(root2, xp);
                                }
                                x = root2;
                            }
                        }
                    }
                }
            }
            return root2;
        }

        static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
            TreeNode<K, V> tp = t.parent;
            TreeNode<K, V> tl = t.left;
            TreeNode<K, V> tr = t.right;
            TreeNode<K, V> tb = t.prev;
            TreeNode<K, V> tn = (TreeNode) t.next;
            if (tb != null && tb.next != t) {
                return $assertionsDisabled;
            }
            if (tn != null && tn.prev != t) {
                return $assertionsDisabled;
            }
            if (tp != null && t != tp.left && t != tp.right) {
                return $assertionsDisabled;
            }
            if (tl != null && (tl.parent != t || tl.hash > t.hash)) {
                return $assertionsDisabled;
            }
            if (tr != null && (tr.parent != t || tr.hash < t.hash)) {
                return $assertionsDisabled;
            }
            if (t.red && tl != null && tl.red && tr != null && tr.red) {
                return $assertionsDisabled;
            }
            if (tl != null && !checkInvariants(tl)) {
                return $assertionsDisabled;
            }
            if (tr == null || checkInvariants(tr)) {
                return true;
            }
            return $assertionsDisabled;
        }
    }

    static final class TreeNode<K, V> extends Node<K, V> {
        TreeNode<K, V> left;
        TreeNode<K, V> parent;
        TreeNode<K, V> prev;
        boolean red;
        TreeNode<K, V> right;

        TreeNode(int hash, K key, V val, Node<K, V> next, TreeNode<K, V> parent2) {
            super(hash, key, val, next);
            this.parent = parent2;
        }

        /* access modifiers changed from: package-private */
        public Node<K, V> find(int h, Object k) {
            return findTreeNode(h, k, null);
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:18:0x0030, code lost:
            if (r3 != null) goto L_0x0032;
         */
        public final TreeNode<K, V> findTreeNode(int h, Object k, Class<?> kc) {
            if (k != null) {
                Class<?> kc2 = kc;
                TreeNode<K, V> p = this;
                do {
                    TreeNode<K, V> pl = p.left;
                    TreeNode<K, V> pr = p.right;
                    int i = p.hash;
                    int ph = i;
                    if (i > h) {
                        p = pl;
                        continue;
                    } else if (ph < h) {
                        p = pr;
                        continue;
                    } else {
                        K k2 = p.key;
                        K pk = k2;
                        if (k2 == k || (pk != null && k.equals(pk))) {
                            return p;
                        }
                        if (pl == null) {
                            p = pr;
                            continue;
                        } else if (pr == null) {
                            p = pl;
                            continue;
                        } else {
                            if (kc2 == null) {
                                Class<?> comparableClassFor = ConcurrentHashMap.comparableClassFor(k);
                                kc2 = comparableClassFor;
                            }
                            int compareComparables = ConcurrentHashMap.compareComparables(kc2, k, pk);
                            int dir = compareComparables;
                            if (compareComparables != 0) {
                                p = dir < 0 ? pl : pr;
                                continue;
                            }
                            TreeNode<K, V> findTreeNode = pr.findTreeNode(h, k, kc2);
                            TreeNode<K, V> q = findTreeNode;
                            if (findTreeNode != null) {
                                return q;
                            }
                            p = pl;
                            continue;
                        }
                    }
                } while (p != null);
                Class<?> cls = kc2;
            }
            return null;
        }
    }

    static final class ValueIterator<K, V> extends BaseIterator<K, V> implements Iterator<V>, Enumeration<V> {
        ValueIterator(Node<K, V>[] tab, int index, int size, int limit, ConcurrentHashMap<K, V> map) {
            super(tab, index, size, limit, map);
        }

        public final V next() {
            Node<K, V> node = this.next;
            Node<K, V> p = node;
            if (node != null) {
                V v = p.val;
                this.lastReturned = p;
                advance();
                return v;
            }
            throw new NoSuchElementException();
        }

        public final V nextElement() {
            return next();
        }
    }

    static final class ValueSpliterator<K, V> extends Traverser<K, V> implements Spliterator<V> {
        long est;

        ValueSpliterator(Node<K, V>[] tab, int size, int index, int limit, long est2) {
            super(tab, size, index, limit);
            this.est = est2;
        }

        public ValueSpliterator<K, V> trySplit() {
            int i = this.baseIndex;
            int i2 = i;
            int i3 = this.baseLimit;
            int f = i3;
            int i4 = (i + i3) >>> 1;
            int h = i4;
            if (i4 <= i2) {
                return null;
            }
            Node[] nodeArr = this.tab;
            int i5 = this.baseSize;
            this.baseLimit = h;
            long j = this.est >>> 1;
            this.est = j;
            ValueSpliterator valueSpliterator = new ValueSpliterator(nodeArr, i5, h, f, j);
            return valueSpliterator;
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action != null) {
                while (true) {
                    Node<K, V> advance = advance();
                    Node<K, V> p = advance;
                    if (advance != null) {
                        action.accept(p.val);
                    } else {
                        return;
                    }
                }
            } else {
                throw new NullPointerException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action != null) {
                Node<K, V> advance = advance();
                Node<K, V> p = advance;
                if (advance == null) {
                    return false;
                }
                action.accept(p.val);
                return true;
            }
            throw new NullPointerException();
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return 4352;
        }
    }

    static final class ValuesView<K, V> extends CollectionView<K, V, V> implements Collection<V>, Serializable {
        private static final long serialVersionUID = 2249069246763182397L;

        ValuesView(ConcurrentHashMap<K, V> map) {
            super(map);
        }

        public final boolean contains(Object o) {
            return this.map.containsValue(o);
        }

        public final boolean remove(Object o) {
            if (o != null) {
                Iterator<V> it = iterator();
                while (it.hasNext()) {
                    if (o.equals(it.next())) {
                        it.remove();
                        return true;
                    }
                }
            }
            return false;
        }

        public final Iterator<V> iterator() {
            ConcurrentHashMap<K, V> m = this.map;
            Node<K, V>[] nodeArr = m.table;
            Node<K, V>[] t = nodeArr;
            int f = nodeArr == null ? 0 : t.length;
            ValueIterator valueIterator = new ValueIterator(t, f, 0, f, m);
            return valueIterator;
        }

        public final boolean add(V v) {
            throw new UnsupportedOperationException();
        }

        public final boolean addAll(Collection<? extends V> collection) {
            throw new UnsupportedOperationException();
        }

        public boolean removeIf(Predicate<? super V> filter) {
            return this.map.removeValueIf(filter);
        }

        public Spliterator<V> spliterator() {
            ConcurrentHashMap<K, V> m = this.map;
            long n = m.sumCount();
            Node<K, V>[] nodeArr = m.table;
            Node<K, V>[] t = nodeArr;
            int f = nodeArr == null ? 0 : t.length;
            ValueSpliterator valueSpliterator = new ValueSpliterator(t, f, 0, f, n < 0 ? 0 : n);
            return valueSpliterator;
        }

        public void forEach(Consumer<? super V> action) {
            if (action != null) {
                Node<K, V>[] nodeArr = this.map.table;
                Node<K, V>[] t = nodeArr;
                if (nodeArr != null) {
                    Traverser<K, V> it = new Traverser<>(t, t.length, 0, t.length);
                    while (true) {
                        Node<K, V> advance = it.advance();
                        Node<K, V> p = advance;
                        if (advance != null) {
                            action.accept(p.val);
                        } else {
                            return;
                        }
                    }
                }
            } else {
                throw new NullPointerException();
            }
        }
    }

    static {
        try {
            SIZECTL = U.objectFieldOffset(ConcurrentHashMap.class.getDeclaredField("sizeCtl"));
            TRANSFERINDEX = U.objectFieldOffset(ConcurrentHashMap.class.getDeclaredField("transferIndex"));
            BASECOUNT = U.objectFieldOffset(ConcurrentHashMap.class.getDeclaredField("baseCount"));
            CELLSBUSY = U.objectFieldOffset(ConcurrentHashMap.class.getDeclaredField("cellsBusy"));
            CELLVALUE = U.objectFieldOffset(CounterCell.class.getDeclaredField("value"));
            ABASE = U.arrayBaseOffset(Node[].class);
            int scale = U.arrayIndexScale(Node[].class);
            if (((scale - 1) & scale) == 0) {
                ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
                Class<LockSupport> cls = LockSupport.class;
                return;
            }
            throw new Error("array index scale not a power of two");
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }

    static final int spread(int h) {
        return ((h >>> 16) ^ h) & Integer.MAX_VALUE;
    }

    private static final int tableSizeFor(int c) {
        int n = c - 1;
        int n2 = n | (n >>> 1);
        int n3 = n2 | (n2 >>> 2);
        int n4 = n3 | (n3 >>> 4);
        int n5 = n4 | (n4 >>> 8);
        int n6 = n5 | (n5 >>> 16);
        if (n6 < 0) {
            return 1;
        }
        return n6 >= MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : n6 + 1;
    }

    /* JADX WARNING: Multi-variable type inference failed */
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> cls = x.getClass();
            Class<?> c = cls;
            if (cls == String.class) {
                return c;
            }
            Type[] genericInterfaces = c.getGenericInterfaces();
            Type[] ts = genericInterfaces;
            if (genericInterfaces != null) {
                for (Type type : ts) {
                    Type t = type;
                    if (type instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) t;
                        ParameterizedType p = parameterizedType;
                        if (parameterizedType.getRawType() == Comparable.class) {
                            Type[] actualTypeArguments = p.getActualTypeArguments();
                            Type[] as = actualTypeArguments;
                            if (actualTypeArguments != null && as.length == 1 && as[0] == c) {
                                return c;
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
        return null;
    }

    static int compareComparables(Class<?> kc, Object k, Object x) {
        if (x == null || x.getClass() != kc) {
            return 0;
        }
        return ((Comparable) k).compareTo(x);
    }

    static final <K, V> Node<K, V> tabAt(Node<K, V>[] tab, int i) {
        return (Node) U.getObjectVolatile(tab, (((long) i) << ASHIFT) + ((long) ABASE));
    }

    static final <K, V> boolean casTabAt(Node<K, V>[] tab, int i, Node<K, V> c, Node<K, V> v) {
        return U.compareAndSwapObject(tab, ((long) ABASE) + (((long) i) << ASHIFT), c, v);
    }

    static final <K, V> void setTabAt(Node<K, V>[] tab, int i, Node<K, V> v) {
        U.putObjectVolatile(tab, (((long) i) << ASHIFT) + ((long) ABASE), v);
    }

    public ConcurrentHashMap() {
    }

    public ConcurrentHashMap(int initialCapacity) {
        int cap;
        if (initialCapacity >= 0) {
            if (initialCapacity >= 536870912) {
                cap = MAXIMUM_CAPACITY;
            } else {
                cap = tableSizeFor((initialCapacity >>> 1) + initialCapacity + 1);
            }
            this.sizeCtl = cap;
            return;
        }
        throw new IllegalArgumentException();
    }

    public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
        this.sizeCtl = 16;
        putAll(m);
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, 1);
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        if (loadFactor <= 0.0f || initialCapacity < 0 || concurrencyLevel <= 0) {
            throw new IllegalArgumentException();
        }
        long size = (long) (1.0d + ((double) (((float) ((long) (initialCapacity < concurrencyLevel ? concurrencyLevel : initialCapacity))) / loadFactor)));
        this.sizeCtl = size >= 1073741824 ? MAXIMUM_CAPACITY : tableSizeFor((int) size);
    }

    public int size() {
        long n = sumCount();
        if (n < 0) {
            return 0;
        }
        if (n > 2147483647L) {
            return Integer.MAX_VALUE;
        }
        return (int) n;
    }

    public boolean isEmpty() {
        return sumCount() <= 0;
    }

    public V get(Object key) {
        int h = spread(key.hashCode());
        Node<K, V>[] nodeArr = this.table;
        Node<K, V>[] tab = nodeArr;
        V v = null;
        if (nodeArr != null) {
            int length = tab.length;
            int n = length;
            if (length > 0) {
                Node<K, V> tabAt = tabAt(tab, (n - 1) & h);
                Node<K, V> e = tabAt;
                if (tabAt != null) {
                    int i = e.hash;
                    int eh = i;
                    if (i == h) {
                        K k = e.key;
                        K ek = k;
                        if (k == key || (ek != null && key.equals(ek))) {
                            return e.val;
                        }
                    } else if (eh < 0) {
                        Node<K, V> find = e.find(h, key);
                        Node<K, V> p = find;
                        if (find != null) {
                            v = p.val;
                        }
                        return v;
                    }
                    while (true) {
                        Node<K, V> node = e.next;
                        e = node;
                        if (node == null) {
                            break;
                        } else if (e.hash == h) {
                            K k2 = e.key;
                            K ek2 = k2;
                            if (k2 == key || (ek2 != null && key.equals(ek2))) {
                            }
                        }
                    }
                    return e.val;
                }
            }
        }
        return null;
    }

    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    public boolean containsValue(Object value) {
        if (value != null) {
            Node<K, V>[] nodeArr = this.table;
            Node<K, V>[] t = nodeArr;
            if (nodeArr != null) {
                Traverser<K, V> it = new Traverser<>(t, t.length, 0, t.length);
                while (true) {
                    Node<K, V> advance = it.advance();
                    Node<K, V> p = advance;
                    if (advance == null) {
                        break;
                    }
                    V v = p.val;
                    V v2 = v;
                    if (v == value || (v2 != null && value.equals(v2))) {
                    }
                }
                return true;
            }
            return false;
        }
        throw new NullPointerException();
    }

    public V put(K key, V value) {
        return putVal(key, value, false);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x009f, code lost:
        if (r1 == 0) goto L_0x000f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00a3, code lost:
        if (r1 < 8) goto L_0x00a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00a5, code lost:
        treeifyBin(r2, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00a8, code lost:
        if (r3 == null) goto L_0x00ab;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00aa, code lost:
        return r3;
     */
    public final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        int hash = spread(key.hashCode());
        int binCount = 0;
        Node<K, V>[] tab = this.table;
        while (true) {
            if (tab != null) {
                int length = tab.length;
                int n = length;
                if (length != 0) {
                    int i = (n - 1) & hash;
                    int i2 = i;
                    Node<K, V> tabAt = tabAt(tab, i);
                    Node<K, V> f = tabAt;
                    if (tabAt != null) {
                        int i3 = f.hash;
                        int fh = i3;
                        if (i3 == -1) {
                            tab = helpTransfer(tab, f);
                        } else {
                            V oldVal = null;
                            synchronized (f) {
                                try {
                                    if (tabAt(tab, i2) == f) {
                                        if (fh >= 0) {
                                            int binCount2 = 1;
                                            Node<K, V> e = f;
                                            while (true) {
                                                try {
                                                    if (e.hash == hash) {
                                                        K k = e.key;
                                                        K ek = k;
                                                        if (k == key || (ek != null && key.equals(ek))) {
                                                            oldVal = e.val;
                                                        }
                                                    }
                                                    Node<K, V> pred = e;
                                                    Node<K, V> node = e.next;
                                                    e = node;
                                                    if (node == null) {
                                                        pred.next = new Node<>(hash, key, value, null);
                                                        break;
                                                    }
                                                    binCount2++;
                                                } catch (Throwable th) {
                                                    th = th;
                                                    int i4 = binCount2;
                                                    throw th;
                                                }
                                            }
                                            oldVal = e.val;
                                            if (!onlyIfAbsent) {
                                                e.val = value;
                                            }
                                            binCount = binCount2;
                                        } else if ((f instanceof TreeBin) != 0) {
                                            binCount = 2;
                                            Node<K, V> putTreeVal = ((TreeBin) f).putTreeVal(hash, key, value);
                                            Node<K, V> p = putTreeVal;
                                            if (putTreeVal != null) {
                                                oldVal = p.val;
                                                if (!onlyIfAbsent) {
                                                    p.val = value;
                                                }
                                            }
                                        } else if (f instanceof ReservationNode) {
                                            throw new IllegalStateException("Recursive update");
                                        }
                                    }
                                } catch (Throwable th2) {
                                    th = th2;
                                    throw th;
                                }
                            }
                        }
                    } else if (casTabAt(tab, i2, null, new Node(hash, key, value, null))) {
                        break;
                    }
                }
            }
            tab = initTable();
        }
        addCount(1, binCount);
        return null;
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        tryPresize(m.size());
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            putVal(e.getKey(), e.getValue(), false);
        }
    }

    public V remove(Object key) {
        return replaceNode(key, null, null);
    }

    /* access modifiers changed from: package-private */
    public final V replaceNode(Object key, V value, Object cv) {
        Object obj = key;
        V v = value;
        Object obj2 = cv;
        int hash = spread(key.hashCode());
        Node<K, V>[] tab = this.table;
        while (true) {
            Node<K, V>[] tab2 = tab;
            if (tab2 == null) {
                break;
            }
            int length = tab2.length;
            int n = length;
            if (length == 0) {
                break;
            }
            int i = (n - 1) & hash;
            int i2 = i;
            Node<K, V> tabAt = tabAt(tab2, i);
            Node<K, V> f = tabAt;
            if (tabAt == null) {
                break;
            }
            int i3 = f.hash;
            int fh = i3;
            if (i3 == -1) {
                tab = helpTransfer(tab2, f);
            } else {
                V oldVal = null;
                boolean validated = false;
                synchronized (f) {
                    if (tabAt(tab2, i2) == f) {
                        if (fh >= 0) {
                            validated = true;
                            Node<K, V> e = f;
                            Node<K, V> pred = null;
                            while (true) {
                                if (e.hash == hash) {
                                    K k = e.key;
                                    K ek = k;
                                    if (k == obj) {
                                        break;
                                    }
                                    K ek2 = ek;
                                    if (ek2 != null && obj.equals(ek2)) {
                                        break;
                                    }
                                }
                                pred = e;
                                Node<K, V> node = e.next;
                                e = node;
                                if (node == null) {
                                    break;
                                }
                            }
                            V ev = e.val;
                            if (obj2 == null || obj2 == ev || (ev != null && obj2.equals(ev))) {
                                oldVal = ev;
                                if (v != null) {
                                    e.val = v;
                                } else if (pred != null) {
                                    V v2 = ev;
                                    pred.next = e.next;
                                } else {
                                    setTabAt(tab2, i2, e.next);
                                }
                            }
                        } else if (f instanceof TreeBin) {
                            validated = true;
                            TreeBin<K, V> t = (TreeBin) f;
                            TreeNode<K, V> treeNode = t.root;
                            TreeNode<K, V> r = treeNode;
                            if (treeNode != null) {
                                TreeNode<K, V> findTreeNode = r.findTreeNode(hash, obj, null);
                                TreeNode<K, V> p = findTreeNode;
                                if (findTreeNode != null) {
                                    V pv = p.val;
                                    if (obj2 == null || obj2 == pv || (pv != null && obj2.equals(pv))) {
                                        oldVal = pv;
                                        if (v != null) {
                                            p.val = v;
                                        } else if (t.removeTreeNode(p)) {
                                            setTabAt(tab2, i2, untreeify(t.first));
                                        }
                                    }
                                }
                            }
                        } else if (f instanceof ReservationNode) {
                            throw new IllegalStateException("Recursive update");
                        }
                    }
                }
                if (!validated) {
                    tab = tab2;
                } else if (oldVal != null) {
                    if (v == null) {
                        addCount(-1, -1);
                    }
                    return oldVal;
                }
            }
            obj = key;
        }
        return null;
    }

    public void clear() {
        long delta = 0;
        int i = 0;
        Node<K, V>[] tab = this.table;
        while (tab != null && i < tab.length) {
            Node<K, V> f = tabAt(tab, i);
            if (f == null) {
                i++;
            } else {
                int i2 = f.hash;
                int fh = i2;
                if (i2 == -1) {
                    tab = helpTransfer(tab, f);
                    i = 0;
                } else {
                    synchronized (f) {
                        try {
                            if (tabAt(tab, i) == f) {
                                for (Node<K, V> p = fh >= 0 ? f : f instanceof TreeBin ? ((TreeBin) f).first : null; p != null; p = p.next) {
                                    delta--;
                                }
                                int i3 = i + 1;
                                try {
                                    setTabAt(tab, i, null);
                                    i = i3;
                                } catch (Throwable th) {
                                    th = th;
                                    int i4 = i3;
                                    throw th;
                                }
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                }
            }
        }
        if (delta != 0) {
            addCount(delta, -1);
        }
    }

    public Set<K> keySet() {
        KeySetView<K, V> keySetView = this.keySet;
        KeySetView<K, V> ks = keySetView;
        if (keySetView != null) {
            return ks;
        }
        KeySetView<K, V> keySetView2 = new KeySetView<>(this, null);
        this.keySet = keySetView2;
        return keySetView2;
    }

    public Collection<V> values() {
        ValuesView<K, V> valuesView = this.values;
        ValuesView<K, V> vs = valuesView;
        if (valuesView != null) {
            return vs;
        }
        ValuesView<K, V> valuesView2 = new ValuesView<>(this);
        this.values = valuesView2;
        return valuesView2;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        EntrySetView<K, V> entrySetView = this.entrySet;
        EntrySetView<K, V> es = entrySetView;
        if (entrySetView != null) {
            return es;
        }
        EntrySetView<K, V> entrySetView2 = new EntrySetView<>(this);
        this.entrySet = entrySetView2;
        return entrySetView2;
    }

    public int hashCode() {
        int h = 0;
        Node<K, V>[] nodeArr = this.table;
        Node<K, V>[] t = nodeArr;
        if (nodeArr != null) {
            Traverser<K, V> it = new Traverser<>(t, t.length, 0, t.length);
            while (true) {
                Node<K, V> advance = it.advance();
                Node<K, V> p = advance;
                if (advance == null) {
                    break;
                }
                h += p.key.hashCode() ^ p.val.hashCode();
            }
        }
        return h;
    }

    public String toString() {
        Node<K, V>[] nodeArr = this.table;
        Node<K, V>[] t = nodeArr;
        int f = nodeArr == null ? 0 : t.length;
        Traverser<K, V> it = new Traverser<>(t, f, 0, f);
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        Node<K, V> advance = it.advance();
        Node<K, V> p = advance;
        if (advance != null) {
            while (true) {
                K k = p.key;
                V v = p.val;
                sb.append((Object) k == this ? "(this Map)" : k);
                sb.append('=');
                sb.append((Object) v == this ? "(this Map)" : v);
                Node<K, V> advance2 = it.advance();
                p = advance2;
                if (advance2 == null) {
                    break;
                }
                sb.append(',');
                sb.append(' ');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (o != this) {
            if (!(o instanceof Map)) {
                return false;
            }
            Map<?, ?> m = (Map) o;
            Node<K, V>[] nodeArr = this.table;
            Node<K, V>[] t = nodeArr;
            int f = nodeArr == null ? 0 : t.length;
            Traverser<K, V> it = new Traverser<>(t, f, 0, f);
            while (true) {
                Node<K, V> advance = it.advance();
                Node<K, V> p = advance;
                if (advance != null) {
                    Object val = p.val;
                    Object v = m.get(p.key);
                    if (v == null || (v != val && !v.equals(val))) {
                        return false;
                    }
                } else {
                    for (Map.Entry<?, ?> e : m.entrySet()) {
                        Object key = e.getKey();
                        Object mk = key;
                        if (key != null) {
                            Object value = e.getValue();
                            Object mv = value;
                            if (value != null) {
                                Object obj = get(mk);
                                Object v2 = obj;
                                if (obj != null && (mv == v2 || mv.equals(v2))) {
                                }
                            }
                        }
                        return false;
                    }
                }
            }
            return false;
        }
        return true;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        int sshift = 0;
        int ssize = 1;
        while (ssize < 16) {
            sshift++;
            ssize <<= 1;
        }
        int segmentShift = 32 - sshift;
        int segmentMask = ssize - 1;
        Segment<K, V>[] segments = new Segment[16];
        for (int i = 0; i < segments.length; i++) {
            segments[i] = new Segment<>(LOAD_FACTOR);
        }
        ObjectOutputStream.PutField streamFields = s.putFields();
        streamFields.put("segments", (Object) segments);
        streamFields.put("segmentShift", segmentShift);
        streamFields.put("segmentMask", segmentMask);
        s.writeFields();
        Node<K, V>[] nodeArr = this.table;
        Node<K, V>[] t = nodeArr;
        if (nodeArr != null) {
            Traverser<K, V> it = new Traverser<>(t, t.length, 0, t.length);
            while (true) {
                Node<K, V> advance = it.advance();
                Node<K, V> p = advance;
                if (advance == null) {
                    break;
                }
                s.writeObject(p.key);
                s.writeObject(p.val);
            }
        }
        s.writeObject(null);
        s.writeObject(null);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        int sz;
        long size;
        boolean insertAtFront;
        long j;
        this.sizeCtl = -1;
        s.defaultReadObject();
        long size2 = 0;
        Node<K, V> p = null;
        while (true) {
            K k = s.readObject();
            V v = s.readObject();
            if (k == null || v == null) {
                long added = 0;
            } else {
                p = new Node<>(spread(k.hashCode()), k, v, p);
                size2++;
            }
        }
        long added2 = 0;
        if (size2 == 0) {
            this.sizeCtl = 0;
            long j2 = size2;
            return;
        }
        if (size2 >= 536870912) {
            sz = MAXIMUM_CAPACITY;
        } else {
            int sz2 = (int) size2;
            sz = tableSizeFor((sz2 >>> 1) + sz2 + 1);
        }
        Node<K, V>[] tab = new Node[sz];
        int mask = sz - 1;
        while (p != null) {
            Node<K, V> next = p.next;
            int h = p.hash;
            int j3 = h & mask;
            Node<K, V> tabAt = tabAt(tab, j3);
            Node<K, V> first = tabAt;
            if (tabAt == null) {
                insertAtFront = true;
                size = size2;
            } else {
                K k2 = p.key;
                if (first.hash < 0) {
                    if (((TreeBin) first).putTreeVal(h, k2, p.val) == null) {
                        added2++;
                    }
                    size = size2;
                    insertAtFront = false;
                } else {
                    boolean insertAtFront2 = true;
                    size = size2;
                    int binCount = 0;
                    Node<K, V> q = first;
                    while (true) {
                        Node<K, V> q2 = q;
                        if (q2 == null) {
                            break;
                        }
                        if (q2.hash == h) {
                            K k3 = q2.key;
                            K qk = k3;
                            if (k3 == k2) {
                                break;
                            }
                            K qk2 = qk;
                            if (qk2 != null && k2.equals(qk2)) {
                                break;
                            }
                        }
                        binCount++;
                        q = q2.next;
                    }
                    insertAtFront2 = false;
                    if (!insertAtFront2 || binCount < 8) {
                        insertAtFront = insertAtFront2;
                    } else {
                        boolean insertAtFront3 = false;
                        long added3 = added2 + 1;
                        p.next = first;
                        Node<K, V> q3 = p;
                        int i = binCount;
                        TreeNode<K, V> hd = null;
                        TreeNode<K, V> tl = null;
                        while (q3 != null) {
                            long added4 = added3;
                            boolean insertAtFront4 = insertAtFront3;
                            TreeNode<K, V> treeNode = new TreeNode<>(q3.hash, q3.key, q3.val, null, null);
                            TreeNode<K, V> t = treeNode;
                            t.prev = tl;
                            if (tl == null) {
                                hd = t;
                            } else {
                                tl.next = t;
                            }
                            tl = t;
                            q3 = q3.next;
                            added3 = added4;
                            insertAtFront3 = insertAtFront4;
                        }
                        setTabAt(tab, j3, new TreeBin(hd));
                        added2 = added3;
                        insertAtFront = insertAtFront3;
                    }
                }
            }
            if (insertAtFront) {
                j = 1;
                added2++;
                p.next = first;
                setTabAt(tab, j3, p);
            } else {
                j = 1;
            }
            p = next;
            long j4 = j;
            size2 = size;
        }
        this.table = tab;
        this.sizeCtl = sz - (sz >>> 2);
        this.baseCount = added2;
    }

    public V putIfAbsent(K key, V value) {
        return putVal(key, value, true);
    }

    public boolean remove(Object key, Object value) {
        if (key != null) {
            return (value == null || replaceNode(key, null, value) == null) ? false : true;
        }
        throw new NullPointerException();
    }

    public boolean replace(K key, V oldValue, V newValue) {
        if (key != null && oldValue != null && newValue != null) {
            return replaceNode(key, newValue, oldValue) != null;
        }
        throw new NullPointerException();
    }

    public V replace(K key, V value) {
        if (key != null && value != null) {
            return replaceNode(key, value, null);
        }
        throw new NullPointerException();
    }

    public V getOrDefault(Object key, V defaultValue) {
        V v = get(key);
        return v == null ? defaultValue : v;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action != null) {
            Node<K, V>[] nodeArr = this.table;
            Node<K, V>[] t = nodeArr;
            if (nodeArr != null) {
                Traverser<K, V> it = new Traverser<>(t, t.length, 0, t.length);
                while (true) {
                    Node<K, V> advance = it.advance();
                    Node<K, V> p = advance;
                    if (advance != null) {
                        action.accept(p.key, p.val);
                    } else {
                        return;
                    }
                }
            }
        } else {
            throw new NullPointerException();
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        V v;
        if (function != null) {
            Node<K, V>[] nodeArr = this.table;
            Node<K, V>[] t = nodeArr;
            if (nodeArr != null) {
                Traverser<K, V> it = new Traverser<>(t, t.length, 0, t.length);
                while (true) {
                    Node<K, V> advance = it.advance();
                    Node<K, V> p = advance;
                    if (advance != null) {
                        V oldValue = p.val;
                        K key = p.key;
                        do {
                            V newValue = function.apply(key, oldValue);
                            if (newValue != null) {
                                if (replaceNode(key, newValue, oldValue) != null) {
                                    break;
                                }
                                v = get(key);
                                oldValue = v;
                            } else {
                                throw new NullPointerException();
                            }
                        } while (v != null);
                    } else {
                        return;
                    }
                }
            }
        } else {
            throw new NullPointerException();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean removeEntryIf(Predicate<? super Map.Entry<K, V>> function) {
        if (function != null) {
            boolean removed = false;
            Node<K, V>[] nodeArr = this.table;
            Node<K, V>[] t = nodeArr;
            if (nodeArr != null) {
                Traverser<K, V> it = new Traverser<>(t, t.length, 0, t.length);
                while (true) {
                    Node<K, V> advance = it.advance();
                    Node<K, V> p = advance;
                    if (advance == null) {
                        break;
                    }
                    K k = p.key;
                    V v = p.val;
                    if (function.test(new AbstractMap.SimpleImmutableEntry<>(k, v)) && replaceNode(k, null, v) != null) {
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
            Node<K, V>[] nodeArr = this.table;
            Node<K, V>[] t = nodeArr;
            if (nodeArr != null) {
                Traverser<K, V> it = new Traverser<>(t, t.length, 0, t.length);
                while (true) {
                    Node<K, V> advance = it.advance();
                    Node<K, V> p = advance;
                    if (advance == null) {
                        break;
                    }
                    K k = p.key;
                    V v = p.val;
                    if (function.test(v) && replaceNode(k, null, v) != null) {
                        removed = true;
                    }
                }
            }
            return removed;
        }
        throw new NullPointerException();
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x00da, code lost:
        if (r2 == 0) goto L_0x0010;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x00de, code lost:
        if (r2 < 8) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x00e0, code lost:
        treeifyBin(r3, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x00e3, code lost:
        if (r4 != false) goto L_0x00e6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x00e5, code lost:
        return r1;
     */
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V val;
        if (key == null || mappingFunction == null) {
            throw new NullPointerException();
        }
        int h = spread(key.hashCode());
        V val2 = null;
        int binCount = 0;
        Node<K, V>[] tab = this.table;
        while (true) {
            if (tab != null) {
                int length = tab.length;
                int n = length;
                if (length != 0) {
                    int i = (n - 1) & h;
                    int i2 = i;
                    Node<K, V> tabAt = tabAt(tab, i);
                    Node<K, V> f = tabAt;
                    if (tabAt == null) {
                        Node<K, V> r = new ReservationNode<>();
                        synchronized (r) {
                            if (casTabAt(tab, i2, null, r)) {
                                binCount = 1;
                                Node<K, V> node = null;
                                try {
                                    V apply = mappingFunction.apply(key);
                                    val2 = apply;
                                    if (apply != null) {
                                        node = new Node<>(h, key, val2, null);
                                    }
                                    setTabAt(tab, i2, node);
                                } catch (Throwable th) {
                                    setTabAt(tab, i2, null);
                                    throw th;
                                }
                            }
                        }
                        if (binCount != 0) {
                            break;
                        }
                    } else {
                        int i3 = f.hash;
                        int fh = i3;
                        if (i3 == -1) {
                            tab = helpTransfer(tab, f);
                        } else {
                            boolean added = false;
                            synchronized (f) {
                                try {
                                    if (tabAt(tab, i2) == f) {
                                        if (fh >= 0) {
                                            int binCount2 = 1;
                                            Node<K, V> e = f;
                                            while (true) {
                                                try {
                                                    if (e.hash == h) {
                                                        K k = e.key;
                                                        K ek = k;
                                                        if (k == key || (ek != null && key.equals(ek))) {
                                                            val2 = e.val;
                                                        }
                                                    }
                                                    Node<K, V> pred = e;
                                                    Node<K, V> node2 = e.next;
                                                    e = node2;
                                                    if (node2 == null) {
                                                        V apply2 = mappingFunction.apply(key);
                                                        val2 = apply2;
                                                        if (apply2 != null) {
                                                            if (pred.next == null) {
                                                                added = true;
                                                                pred.next = new Node<>(h, key, val2, null);
                                                            } else {
                                                                throw new IllegalStateException("Recursive update");
                                                            }
                                                        }
                                                    } else {
                                                        binCount2++;
                                                    }
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    throw th;
                                                }
                                            }
                                            val2 = e.val;
                                            binCount = binCount2;
                                        } else if ((f instanceof TreeBin) != 0) {
                                            binCount = 2;
                                            TreeBin<K, V> t = (TreeBin) f;
                                            TreeNode<K, V> treeNode = t.root;
                                            TreeNode<K, V> r2 = treeNode;
                                            if (treeNode != null) {
                                                TreeNode<K, V> findTreeNode = r2.findTreeNode(h, key, null);
                                                TreeNode<K, V> p = findTreeNode;
                                                if (findTreeNode != null) {
                                                    val = p.val;
                                                }
                                            }
                                            V apply3 = mappingFunction.apply(key);
                                            val = apply3;
                                            if (apply3 != null) {
                                                added = true;
                                                t.putTreeVal(h, key, val);
                                            }
                                        } else if (f instanceof ReservationNode) {
                                            throw new IllegalStateException("Recursive update");
                                        }
                                    }
                                } catch (Throwable th3) {
                                    int i4 = binCount;
                                    th = th3;
                                    throw th;
                                }
                            }
                        }
                    }
                }
            }
            tab = initTable();
        }
        if (val2 != null) {
            addCount(1, binCount);
        }
        return val2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:57:0x00b4, code lost:
        if (r2 == 0) goto L_0x00ba;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b6, code lost:
        addCount((long) r2, r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00ba, code lost:
        return r1;
     */
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (key == null || remappingFunction == null) {
            throw new NullPointerException();
        }
        int h = spread(key.hashCode());
        V val = null;
        int delta = 0;
        int binCount = 0;
        Node<K, V>[] tab = this.table;
        while (true) {
            if (tab != null) {
                int length = tab.length;
                int n = length;
                if (length != 0) {
                    int i = (n - 1) & h;
                    int i2 = i;
                    Node<K, V> tabAt = tabAt(tab, i);
                    Node<K, V> f = tabAt;
                    if (tabAt == null) {
                        break;
                    }
                    int i3 = f.hash;
                    int fh = i3;
                    if (i3 == -1) {
                        tab = helpTransfer(tab, f);
                    } else {
                        synchronized (f) {
                            if (tabAt(tab, i2) == f) {
                                Node<K, V> pred = null;
                                if (fh >= 0) {
                                    binCount = 1;
                                    Node<K, V> e = f;
                                    while (true) {
                                        if (e.hash == h) {
                                            K k = e.key;
                                            K ek = k;
                                            if (k == key || (ek != null && key.equals(ek))) {
                                                val = remappingFunction.apply(key, e.val);
                                            }
                                        }
                                        pred = e;
                                        Node<K, V> node = e.next;
                                        e = node;
                                        if (node == null) {
                                            break;
                                        }
                                        binCount++;
                                    }
                                    val = remappingFunction.apply(key, e.val);
                                    if (val != null) {
                                        e.val = val;
                                    } else {
                                        delta = -1;
                                        Node<K, V> en = e.next;
                                        if (pred != null) {
                                            pred.next = en;
                                        } else {
                                            setTabAt(tab, i2, en);
                                        }
                                    }
                                } else if (f instanceof TreeBin) {
                                    binCount = 2;
                                    TreeBin<K, V> t = (TreeBin) f;
                                    TreeNode<K, V> treeNode = t.root;
                                    TreeNode<K, V> r = treeNode;
                                    if (treeNode != null) {
                                        TreeNode<K, V> findTreeNode = r.findTreeNode(h, key, null);
                                        TreeNode<K, V> p = findTreeNode;
                                        if (findTreeNode != null) {
                                            val = remappingFunction.apply(key, p.val);
                                            if (val != null) {
                                                p.val = val;
                                            } else {
                                                delta = -1;
                                                if (t.removeTreeNode(p)) {
                                                    setTabAt(tab, i2, untreeify(t.first));
                                                }
                                            }
                                        }
                                    }
                                } else if (f instanceof ReservationNode) {
                                    throw new IllegalStateException("Recursive update");
                                }
                            }
                        }
                        if (binCount != 0) {
                            break;
                        }
                    }
                }
            }
            tab = initTable();
        }
        while (true) {
        }
    }

    /* JADX INFO: finally extract failed */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x011f, code lost:
        if (r6 < 8) goto L_0x0124;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x0121, code lost:
        treeifyBin(r7, r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x011b, code lost:
        if (r6 == 0) goto L_0x0133;
     */
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Node<K, V>[] tab;
        TreeNode<K, V> p;
        K k = key;
        BiFunction<? super K, ? super V, ? extends V> biFunction = remappingFunction;
        if (k == null || biFunction == null) {
            throw new NullPointerException();
        }
        int h = spread(key.hashCode());
        int binCount = 0;
        Node<K, V>[] tab2 = this.table;
        int delta = 0;
        V val = null;
        while (true) {
            if (tab2 != null) {
                int length = tab2.length;
                int n = length;
                if (length != 0) {
                    int i = (n - 1) & h;
                    int i2 = i;
                    Node<K, V> tabAt = tabAt(tab2, i);
                    Node<K, V> f = tabAt;
                    V pv = null;
                    if (tabAt == null) {
                        Node<K, V> r = new ReservationNode<>();
                        synchronized (r) {
                            if (casTabAt(tab2, i2, null, r)) {
                                binCount = 1;
                                Node<K, V> node = null;
                                try {
                                    V apply = biFunction.apply(k, null);
                                    val = apply;
                                    if (apply != null) {
                                        delta = 1;
                                        node = new Node<>(h, k, val, null);
                                    }
                                    setTabAt(tab2, i2, node);
                                } catch (Throwable th) {
                                    setTabAt(tab2, i2, null);
                                    throw th;
                                }
                            }
                        }
                        if (binCount != 0) {
                            break;
                        }
                        k = key;
                    } else {
                        int i3 = f.hash;
                        int fh = i3;
                        if (i3 == -1) {
                            tab = helpTransfer(tab2, f);
                            tab2 = tab;
                            k = key;
                        } else {
                            synchronized (f) {
                                try {
                                    if (tabAt(tab2, i2) == f) {
                                        if (fh >= 0) {
                                            Node<K, V> e = f;
                                            int binCount2 = 1;
                                            Node<K, V> pred = null;
                                            while (true) {
                                                try {
                                                    if (e.hash == h) {
                                                        K k2 = e.key;
                                                        K ek = k2;
                                                        if (k2 == k) {
                                                            break;
                                                        }
                                                        K ek2 = ek;
                                                        if (ek2 != null && k.equals(ek2)) {
                                                            break;
                                                        }
                                                    }
                                                    pred = e;
                                                    Node<K, V> node2 = e.next;
                                                    e = node2;
                                                    if (node2 == null) {
                                                        val = biFunction.apply(k, null);
                                                        if (val != null) {
                                                            if (pred.next == null) {
                                                                delta = 1;
                                                                pred.next = new Node<>(h, k, val, null);
                                                            } else {
                                                                throw new IllegalStateException("Recursive update");
                                                            }
                                                        }
                                                    } else {
                                                        binCount2++;
                                                    }
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    int i4 = binCount2;
                                                    throw th;
                                                }
                                            }
                                            val = biFunction.apply(k, e.val);
                                            if (val != null) {
                                                e.val = val;
                                            } else {
                                                delta = -1;
                                                Node<K, V> en = e.next;
                                                if (pred != null) {
                                                    pred.next = en;
                                                } else {
                                                    setTabAt(tab2, i2, en);
                                                }
                                            }
                                            binCount = binCount2;
                                        } else if (f instanceof TreeBin) {
                                            binCount = 1;
                                            TreeBin<K, V> t = (TreeBin) f;
                                            TreeNode<K, V> treeNode = t.root;
                                            TreeNode<K, V> r2 = treeNode;
                                            if (treeNode != null) {
                                                p = r2.findTreeNode(h, k, null);
                                            } else {
                                                p = null;
                                            }
                                            if (p != null) {
                                                pv = p.val;
                                            }
                                            val = biFunction.apply(k, pv);
                                            if (val != null) {
                                                if (p != null) {
                                                    p.val = val;
                                                } else {
                                                    delta = 1;
                                                    t.putTreeVal(h, k, val);
                                                }
                                            } else if (p != null) {
                                                delta = -1;
                                                if (t.removeTreeNode(p)) {
                                                    setTabAt(tab2, i2, untreeify(t.first));
                                                }
                                            }
                                        } else if (f instanceof ReservationNode) {
                                            throw new IllegalStateException("Recursive update");
                                        }
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                    throw th;
                                }
                            }
                        }
                    }
                }
            }
            tab = initTable();
            tab2 = tab;
            k = key;
        }
        if (delta != 0) {
            addCount((long) delta, binCount);
        }
        return val;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:114:0x0133, code lost:
        if (r9 == 0) goto L_0x0155;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:116:0x0137, code lost:
        if (r9 < 8) goto L_0x013c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x0139, code lost:
        treeifyBin(r8, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x013c, code lost:
        r0 = r6;
        r6 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00a9, code lost:
        r7 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:?, code lost:
        r0.next = new java.util.concurrent.ConcurrentHashMap.Node<>(r5, r2, r7, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00b5, code lost:
        r6 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x00bb, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00bc, code lost:
        r9 = r15;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x00c1, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x00c2, code lost:
        r9 = r15;
     */
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        int delta;
        V val;
        int delta2;
        int delta3;
        int delta4;
        K k = key;
        V v = value;
        BiFunction<? super V, ? super V, ? extends V> biFunction = remappingFunction;
        if (k == null || v == null || biFunction == null) {
            throw new NullPointerException();
        }
        int h = spread(key.hashCode());
        int delta5 = 0;
        Node<K, V>[] tab = this.table;
        int binCount = 0;
        V val2 = null;
        while (true) {
            if (tab != null) {
                int length = tab.length;
                int n = length;
                if (length == 0) {
                    delta = delta5;
                } else {
                    int i = (n - 1) & h;
                    int i2 = i;
                    Node<K, V> tabAt = tabAt(tab, i);
                    Node<K, V> f = tabAt;
                    if (tabAt != null) {
                        int delta6 = f.hash;
                        int fh = delta6;
                        if (delta6 == -1) {
                            tab = helpTransfer(tab, f);
                        } else {
                            synchronized (f) {
                                try {
                                    if (tabAt(tab, i2) != f) {
                                        delta3 = delta5;
                                    } else if (fh >= 0) {
                                        Node<K, V> e = f;
                                        int binCount2 = 1;
                                        Node<K, V> pred = null;
                                        while (true) {
                                            try {
                                                if (e.hash == h) {
                                                    K k2 = e.key;
                                                    K ek = k2;
                                                    if (k2 == k) {
                                                        K k3 = ek;
                                                        break;
                                                    }
                                                    K ek2 = ek;
                                                    if (ek2 != null) {
                                                        try {
                                                            if (k.equals(ek2)) {
                                                                break;
                                                            }
                                                        } catch (Throwable th) {
                                                            th = th;
                                                            int i3 = binCount2;
                                                            throw th;
                                                        }
                                                    }
                                                    delta3 = delta5;
                                                } else {
                                                    delta3 = delta5;
                                                }
                                                pred = e;
                                                Node<K, V> node = e.next;
                                                e = node;
                                                if (node == null) {
                                                    break;
                                                }
                                                binCount2++;
                                                delta5 = delta3;
                                            } catch (Throwable th2) {
                                                th = th2;
                                                int i4 = delta5;
                                                int i5 = binCount2;
                                                throw th;
                                            }
                                        }
                                        delta3 = delta5;
                                        try {
                                            val2 = biFunction.apply(e.val, v);
                                            if (val2 != null) {
                                                e.val = val2;
                                                delta5 = delta3;
                                            } else {
                                                try {
                                                    Node<K, V> en = e.next;
                                                    if (pred != null) {
                                                        pred.next = en;
                                                    } else {
                                                        setTabAt(tab, i2, en);
                                                    }
                                                    delta5 = -1;
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                    int i6 = binCount2;
                                                    throw th;
                                                }
                                            }
                                            binCount = binCount2;
                                        } catch (Throwable th4) {
                                            th = th4;
                                            int i7 = binCount2;
                                            throw th;
                                        }
                                    } else {
                                        delta3 = delta5;
                                        try {
                                            if (f instanceof TreeBin) {
                                                binCount = 2;
                                                TreeBin<K, V> t = (TreeBin) f;
                                                TreeNode<K, V> r = t.root;
                                                TreeNode<K, V> p = r == null ? null : r.findTreeNode(h, k, null);
                                                val2 = p == null ? v : biFunction.apply(p.val, v);
                                                if (val2 == null) {
                                                    if (p != null) {
                                                        delta4 = -1;
                                                        if (t.removeTreeNode(p)) {
                                                            setTabAt(tab, i2, untreeify(t.first));
                                                        }
                                                    }
                                                    delta5 = delta3;
                                                } else if (p != null) {
                                                    p.val = val2;
                                                    delta5 = delta3;
                                                } else {
                                                    delta4 = 1;
                                                    try {
                                                        t.putTreeVal(h, k, val2);
                                                    } catch (Throwable th5) {
                                                        th = th5;
                                                        throw th;
                                                    }
                                                }
                                                delta5 = delta4;
                                            } else if (f instanceof ReservationNode) {
                                                throw new IllegalStateException("Recursive update");
                                            }
                                        } catch (Throwable th6) {
                                            th = th6;
                                            throw th;
                                        }
                                    }
                                    delta5 = delta3;
                                    try {
                                    } catch (Throwable th7) {
                                        th = th7;
                                        throw th;
                                    }
                                } catch (Throwable th8) {
                                    th = th8;
                                    int i8 = delta5;
                                    throw th;
                                }
                            }
                        }
                        k = key;
                    } else if (casTabAt(tab, i2, null, new Node(h, k, v, null))) {
                        delta2 = 1;
                        val = v;
                        break;
                    } else {
                        k = key;
                    }
                }
            } else {
                delta = delta5;
            }
            tab = initTable();
            delta5 = delta;
            k = key;
        }
        if (delta2 != 0) {
            addCount((long) delta2, binCount);
        }
        return val;
    }

    public boolean contains(Object value) {
        return containsValue(value);
    }

    public Enumeration<K> keys() {
        Node<K, V>[] nodeArr = this.table;
        Node<K, V>[] t = nodeArr;
        int f = nodeArr == null ? 0 : t.length;
        KeyIterator keyIterator = new KeyIterator(t, f, 0, f, this);
        return keyIterator;
    }

    public Enumeration<V> elements() {
        Node<K, V>[] nodeArr = this.table;
        Node<K, V>[] t = nodeArr;
        int f = nodeArr == null ? 0 : t.length;
        ValueIterator valueIterator = new ValueIterator(t, f, 0, f, this);
        return valueIterator;
    }

    public long mappingCount() {
        long n = sumCount();
        if (n < 0) {
            return 0;
        }
        return n;
    }

    public static <K> KeySetView<K, Boolean> newKeySet() {
        return new KeySetView<>(new ConcurrentHashMap(), Boolean.TRUE);
    }

    public static <K> KeySetView<K, Boolean> newKeySet(int initialCapacity) {
        return new KeySetView<>(new ConcurrentHashMap(initialCapacity), Boolean.TRUE);
    }

    public KeySetView<K, V> keySet(V mappedValue) {
        if (mappedValue != null) {
            return new KeySetView<>(this, mappedValue);
        }
        throw new NullPointerException();
    }

    static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) | NumericShaper.MYANMAR;
    }

    private final Node<K, V>[] initTable() {
        Node<K, V>[] tab;
        while (true) {
            Node<K, V>[] nodeArr = this.table;
            tab = nodeArr;
            if (nodeArr != null && tab.length != 0) {
                break;
            }
            int i = this.sizeCtl;
            int sc = i;
            if (i < 0) {
                Thread.yield();
            } else {
                if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                    try {
                        Node<K, V>[] nodeArr2 = this.table;
                        tab = nodeArr2;
                        if (nodeArr2 == null || tab.length == 0) {
                            int n = sc > 0 ? sc : 16;
                            Node<K, V>[] nt = new Node[n];
                            tab = nt;
                            this.table = nt;
                            sc = n - (n >>> 2);
                        }
                    } finally {
                        this.sizeCtl = sc;
                    }
                }
            }
        }
        return tab;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x001a, code lost:
        if (r0.compareAndSwapLong(r8, r2, r4, r6) == false) goto L_0x001c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00b9, code lost:
        r23 = r6;
     */
    private final void addCount(long x, int check) {
        long s;
        long s2;
        long j = x;
        int i = check;
        CounterCell[] counterCellArr = this.counterCells;
        CounterCell[] as = counterCellArr;
        if (counterCellArr == null) {
            Unsafe unsafe = U;
            long j2 = BASECOUNT;
            long b = this.baseCount;
            long j3 = b + j;
            s = j3;
        }
        boolean uncontended = true;
        if (as != null) {
            int length = as.length - 1;
            int m = length;
            if (length >= 0) {
                CounterCell counterCell = as[ThreadLocalRandom.getProbe() & m];
                CounterCell a = counterCell;
                if (counterCell != null) {
                    Unsafe unsafe2 = U;
                    long j4 = CELLVALUE;
                    long v = a.value;
                    boolean compareAndSwapLong = unsafe2.compareAndSwapLong(a, j4, v, v + j);
                    uncontended = compareAndSwapLong;
                    if (compareAndSwapLong) {
                        if (i > 1) {
                            s = sumCount();
                            long s3 = s;
                            if (i >= 0) {
                                long s4 = s3;
                                while (true) {
                                    int i2 = this.sizeCtl;
                                    int sc = i2;
                                    if (s4 < ((long) i2)) {
                                        break;
                                    }
                                    Node<K, V>[] nodeArr = this.table;
                                    Node<K, V>[] tab = nodeArr;
                                    if (nodeArr == null) {
                                        break;
                                    }
                                    int length2 = tab.length;
                                    int n = length2;
                                    if (length2 >= MAXIMUM_CAPACITY) {
                                        break;
                                    }
                                    int rs = resizeStamp(n);
                                    if (sc < 0) {
                                        if ((sc >>> 16) == rs && sc != rs + 1 && sc != MAX_RESIZERS + rs) {
                                            Node<K, V>[] nodeArr2 = this.nextTable;
                                            Node<K, V>[] nt = nodeArr2;
                                            if (nodeArr2 == null) {
                                                break;
                                            } else if (this.transferIndex <= 0) {
                                                s2 = s4;
                                                break;
                                            } else {
                                                long j5 = s4;
                                                Node<K, V>[] nt2 = nt;
                                                int i3 = rs;
                                                if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                                                    transfer(tab, nt2);
                                                }
                                            }
                                        } else {
                                            break;
                                        }
                                    } else {
                                        if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << 16) + 2)) {
                                            transfer(tab, null);
                                        }
                                    }
                                    s4 = sumCount();
                                }
                                long j6 = s2;
                            }
                            return;
                        }
                        return;
                    }
                }
            }
        }
        fullAddCount(j, uncontended);
    }

    /* access modifiers changed from: package-private */
    public final Node<K, V>[] helpTransfer(Node<K, V>[] tab, Node<K, V> f) {
        if (tab != null && (f instanceof ForwardingNode)) {
            Node<K, V>[] nodeArr = ((ForwardingNode) f).nextTable;
            Node<K, V>[] nextTab = nodeArr;
            if (nodeArr != null) {
                int rs = resizeStamp(tab.length);
                while (true) {
                    if (nextTab != this.nextTable || this.table != tab) {
                        break;
                    }
                    int i = this.sizeCtl;
                    int sc = i;
                    if (i >= 0 || (sc >>> 16) != rs || sc == rs + 1 || sc == MAX_RESIZERS + rs || this.transferIndex <= 0) {
                        break;
                    }
                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                        transfer(tab, nextTab);
                        break;
                    }
                }
                return nextTab;
            }
        }
        return this.table;
    }

    private final void tryPresize(int size) {
        int c = size >= 536870912 ? MAXIMUM_CAPACITY : tableSizeFor((size >>> 1) + size + 1);
        while (true) {
            int i = this.sizeCtl;
            int sc = i;
            if (i >= 0) {
                Node<K, V>[] tab = this.table;
                if (tab != null) {
                    int length = tab.length;
                    int n = length;
                    if (length != 0) {
                        if (c > sc && n < MAXIMUM_CAPACITY) {
                            if (tab == this.table) {
                                if (U.compareAndSwapInt(this, SIZECTL, sc, (resizeStamp(n) << 16) + 2)) {
                                    transfer(tab, null);
                                }
                            }
                        } else {
                            return;
                        }
                    }
                }
                int n2 = sc > c ? sc : c;
                if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                    try {
                        if (this.table == tab) {
                            this.table = new Node[n2];
                            sc = n2 - (n2 >>> 2);
                        }
                    } finally {
                        this.sizeCtl = sc;
                    }
                }
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:132:0x01e6, code lost:
        r1 = true;
        r3 = 16;
        r7 = r37;
     */
    private final void transfer(Node<K, V>[] tab, Node<K, V>[] nextTab) {
        Node<K, V>[] nextTab2;
        int nextn;
        int stride;
        char c;
        boolean z;
        int stride2;
        int nextn2;
        Node<K, V> ln;
        Node<K, V> hn;
        Node<K, V> hn2;
        Node<K, V> lastRun;
        boolean advance;
        int bound;
        ConcurrentHashMap concurrentHashMap = this;
        Node<K, V>[] nodeArr = tab;
        int n = nodeArr.length;
        boolean z2 = true;
        int i = NCPU > 1 ? (n >>> 3) / NCPU : n;
        int stride3 = i;
        char c2 = 16;
        if (i < 16) {
            stride3 = 16;
        }
        int stride4 = stride3;
        if (nextTab == null) {
            try {
                Node<K, V>[] nextTab3 = new Node[(n << 1)];
                concurrentHashMap.nextTable = nextTab3;
                concurrentHashMap.transferIndex = n;
                nextTab2 = nextTab3;
            } catch (Throwable th) {
                Throwable th2 = th;
                concurrentHashMap.sizeCtl = Integer.MAX_VALUE;
                return;
            }
        } else {
            nextTab2 = nextTab;
        }
        int nextn3 = nextTab2.length;
        ForwardingNode forwardingNode = new ForwardingNode(nextTab2);
        boolean advance2 = true;
        boolean finishing = false;
        int nextIndex = 0;
        int i2 = 0;
        while (true) {
            int bound2 = i2;
            if (advance2) {
                int i3 = nextIndex - 1;
                if (i3 >= bound2) {
                    bound = bound2;
                } else if (finishing) {
                    bound = bound2;
                } else {
                    int i4 = concurrentHashMap.transferIndex;
                    int nextIndex2 = i4;
                    if (i4 <= 0) {
                        nextIndex = -1;
                        advance2 = false;
                        i2 = bound2;
                    } else {
                        Unsafe unsafe = U;
                        long j = TRANSFERINDEX;
                        int nextIndex3 = nextIndex2 > stride4 ? nextIndex2 - stride4 : 0;
                        int nextBound = nextIndex3;
                        bound = bound2;
                        int nextIndex4 = nextIndex2;
                        if (unsafe.compareAndSwapInt(concurrentHashMap, j, nextIndex2, nextIndex3)) {
                            nextIndex = nextIndex4 - 1;
                            advance2 = false;
                            i2 = nextBound;
                        } else {
                            nextIndex = i3;
                            i2 = bound;
                        }
                    }
                }
                nextIndex = i3;
                advance2 = false;
                i2 = bound;
            } else {
                int bound3 = bound2;
                Node<K, V> ln2 = null;
                if (nextIndex < 0 || nextIndex >= n) {
                    stride2 = stride4;
                    nextn2 = nextn3;
                } else if (nextIndex + n >= nextn3) {
                    stride2 = stride4;
                    nextn2 = nextn3;
                } else {
                    Node<K, V> tabAt = tabAt(nodeArr, nextIndex);
                    Node<K, V> f = tabAt;
                    if (tabAt == null) {
                        advance = casTabAt(nodeArr, nextIndex, null, forwardingNode);
                    } else {
                        int i5 = f.hash;
                        int fh = i5;
                        if (i5 == -1) {
                            advance = true;
                        } else {
                            synchronized (f) {
                                try {
                                    if (tabAt(nodeArr, nextIndex) != f) {
                                        stride = stride4;
                                        nextn = nextn3;
                                    } else if (fh >= 0) {
                                        int runBit = fh & n;
                                        Node<K, V> lastRun2 = f;
                                        try {
                                            Node<K, V> p = f.next;
                                            while (p != null) {
                                                try {
                                                    int b = p.hash & n;
                                                    if (b != runBit) {
                                                        runBit = b;
                                                        lastRun2 = p;
                                                    }
                                                    p = p.next;
                                                } catch (Throwable th3) {
                                                    th = th3;
                                                    int i6 = fh;
                                                    throw th;
                                                }
                                            }
                                            if (runBit == 0) {
                                                ln2 = lastRun2;
                                                hn2 = null;
                                            } else {
                                                hn2 = lastRun2;
                                            }
                                            Node<K, V> hn3 = hn2;
                                            Node<K, V> ln3 = ln2;
                                            Node<K, V> p2 = f;
                                            while (p2 != lastRun2) {
                                                int ph = p2.hash;
                                                int runBit2 = runBit;
                                                K pk = p2.key;
                                                int fh2 = fh;
                                                try {
                                                    V pv = p2.val;
                                                    if ((ph & n) == 0) {
                                                        lastRun = lastRun2;
                                                        ln3 = new Node<>(ph, pk, pv, ln3);
                                                    } else {
                                                        lastRun = lastRun2;
                                                        hn3 = new Node<>(ph, pk, pv, hn3);
                                                    }
                                                    p2 = p2.next;
                                                    runBit = runBit2;
                                                    fh = fh2;
                                                    lastRun2 = lastRun;
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    throw th;
                                                }
                                            }
                                            int i7 = fh;
                                            Node<K, V> node = lastRun2;
                                            setTabAt(nextTab2, nextIndex, ln3);
                                            setTabAt(nextTab2, nextIndex + n, hn3);
                                            setTabAt(nodeArr, nextIndex, forwardingNode);
                                            advance2 = true;
                                            stride = stride4;
                                            nextn = nextn3;
                                        } catch (Throwable th5) {
                                            th = th5;
                                            int i8 = fh;
                                            int i9 = stride4;
                                            int i10 = nextn3;
                                            throw th;
                                        }
                                    } else {
                                        try {
                                            if (f instanceof TreeBin) {
                                                TreeBin<K, V> t = (TreeBin) f;
                                                int lc = 0;
                                                int hc = 0;
                                                Node<K, V> e = t.first;
                                                TreeBin<K, V> t2 = t;
                                                TreeNode<K, V> hiTail = null;
                                                TreeNode<K, V> hi = null;
                                                TreeNode<K, V> loTail = null;
                                                TreeNode<K, V> lo = null;
                                                while (e != null) {
                                                    int stride5 = stride4;
                                                    try {
                                                        int h = e.hash;
                                                        int nextn4 = nextn3;
                                                        TreeNode<K, V> treeNode = new TreeNode<>(h, e.key, e.val, null, null);
                                                        TreeNode<K, V> p3 = treeNode;
                                                        if ((h & n) == 0) {
                                                            p3.prev = loTail;
                                                            if (loTail == null) {
                                                                lo = p3;
                                                            } else {
                                                                loTail.next = p3;
                                                            }
                                                            loTail = p3;
                                                            lc++;
                                                        } else {
                                                            p3.prev = hiTail;
                                                            if (hiTail == null) {
                                                                hi = p3;
                                                            } else {
                                                                hiTail.next = p3;
                                                            }
                                                            hiTail = p3;
                                                            hc++;
                                                        }
                                                        e = e.next;
                                                        stride4 = stride5;
                                                        nextn3 = nextn4;
                                                    } catch (Throwable th6) {
                                                        th = th6;
                                                        throw th;
                                                    }
                                                }
                                                stride = stride4;
                                                nextn = nextn3;
                                                if (lc <= 6) {
                                                    ln = untreeify(lo);
                                                } else {
                                                    ln = hc != 0 ? new TreeBin<>(lo) : t2;
                                                }
                                                if (hc <= 6) {
                                                    hn = untreeify(hi);
                                                } else {
                                                    hn = lc != 0 ? new TreeBin<>(hi) : t2;
                                                }
                                                setTabAt(nextTab2, nextIndex, ln);
                                                setTabAt(nextTab2, nextIndex + n, hn);
                                                setTabAt(nodeArr, nextIndex, forwardingNode);
                                                advance2 = true;
                                            } else {
                                                stride = stride4;
                                                nextn = nextn3;
                                            }
                                        } catch (Throwable th7) {
                                            th = th7;
                                            int i11 = stride4;
                                            int i12 = nextn3;
                                            throw th;
                                        }
                                    }
                                } catch (Throwable th8) {
                                    th = th8;
                                    int i13 = fh;
                                    int i14 = stride4;
                                    int i15 = nextn3;
                                    throw th;
                                }
                            }
                        }
                    }
                    advance2 = advance;
                    z = z2;
                    c = c2;
                    stride = stride4;
                    nextn = nextn3;
                    z2 = z;
                    c2 = c;
                    i2 = bound3;
                    stride4 = stride;
                    nextn3 = nextn;
                }
                if (finishing) {
                    this.nextTable = null;
                    this.table = nextTab2;
                    this.sizeCtl = (n << 1) - (n >>> 1);
                    return;
                }
                concurrentHashMap = this;
                Unsafe unsafe2 = U;
                long j2 = SIZECTL;
                int i16 = concurrentHashMap.sizeCtl;
                int sc = i16;
                int i17 = nextIndex;
                if (unsafe2.compareAndSwapInt(concurrentHashMap, j2, i16, sc - 1)) {
                    c = 16;
                    if (sc - 2 == (resizeStamp(n) << 16)) {
                        z = true;
                        advance2 = true;
                        finishing = true;
                        i17 = n;
                    } else {
                        return;
                    }
                } else {
                    z = true;
                    c = 16;
                }
                nextIndex = i17;
                z2 = z;
                c2 = c;
                i2 = bound3;
                stride4 = stride;
                nextn3 = nextn;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final long sumCount() {
        CounterCell[] as = this.counterCells;
        long sum = this.baseCount;
        if (as != null) {
            for (CounterCell counterCell : as) {
                CounterCell a = counterCell;
                if (counterCell != null) {
                    sum += a.value;
                }
            }
        }
        return sum;
    }

    /* JADX INFO: finally extract failed */
    private final void fullAddCount(long x, boolean wasUncontended) {
        boolean wasUncontended2;
        CounterCell a;
        long j = x;
        int probe = ThreadLocalRandom.getProbe();
        int h = probe;
        if (probe == 0) {
            ThreadLocalRandom.localInit();
            h = ThreadLocalRandom.getProbe();
            wasUncontended2 = true;
        } else {
            wasUncontended2 = wasUncontended;
        }
        boolean wasUncontended3 = wasUncontended2;
        int h2 = h;
        boolean wasUncontended4 = false;
        while (true) {
            boolean collide = wasUncontended4;
            CounterCell[] counterCellArr = this.counterCells;
            CounterCell[] as = counterCellArr;
            if (counterCellArr != null) {
                int length = as.length;
                int n = length;
                if (length > 0) {
                    CounterCell counterCell = as[(n - 1) & h2];
                    CounterCell a2 = counterCell;
                    if (counterCell == null) {
                        if (this.cellsBusy == 0) {
                            CounterCell r = new CounterCell(j);
                            if (this.cellsBusy == 0) {
                                a = a2;
                                if (U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                                    boolean created = false;
                                    try {
                                        CounterCell[] counterCellArr2 = this.counterCells;
                                        CounterCell[] rs = counterCellArr2;
                                        if (counterCellArr2 != null) {
                                            int length2 = rs.length;
                                            int m = length2;
                                            if (length2 > 0) {
                                                int i = (m - 1) & h2;
                                                int j2 = i;
                                                if (rs[i] == null) {
                                                    rs[j2] = r;
                                                    created = true;
                                                }
                                            }
                                        }
                                        if (!created) {
                                            wasUncontended4 = collide;
                                        } else {
                                            return;
                                        }
                                    } finally {
                                        this.cellsBusy = 0;
                                    }
                                }
                                collide = false;
                            }
                        }
                        a = a2;
                        collide = false;
                    } else {
                        a = a2;
                        if (!wasUncontended3) {
                            wasUncontended3 = true;
                        } else {
                            Unsafe unsafe = U;
                            long j3 = CELLVALUE;
                            CounterCell a3 = a;
                            long v = a3.value;
                            if (!unsafe.compareAndSwapLong(a3, j3, v, v + j)) {
                                if (this.counterCells != as) {
                                } else if (n >= NCPU) {
                                    CounterCell counterCell2 = a3;
                                } else {
                                    if (!collide) {
                                        collide = true;
                                        CounterCell counterCell3 = a3;
                                    } else if (this.cellsBusy == 0) {
                                        CounterCell counterCell4 = a3;
                                        if (U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                                            try {
                                                if (this.counterCells == as) {
                                                    CounterCell[] rs2 = new CounterCell[(n << 1)];
                                                    for (int i2 = 0; i2 < n; i2++) {
                                                        rs2[i2] = as[i2];
                                                    }
                                                    this.counterCells = rs2;
                                                }
                                                this.cellsBusy = 0;
                                                wasUncontended4 = false;
                                            } catch (Throwable th) {
                                                this.cellsBusy = 0;
                                                throw th;
                                            }
                                        }
                                    }
                                    h2 = ThreadLocalRandom.advanceProbe(h2);
                                    wasUncontended4 = collide;
                                }
                                collide = false;
                                h2 = ThreadLocalRandom.advanceProbe(h2);
                                wasUncontended4 = collide;
                            } else {
                                return;
                            }
                        }
                    }
                    h2 = ThreadLocalRandom.advanceProbe(h2);
                    wasUncontended4 = collide;
                }
            }
            if (this.cellsBusy == 0 && this.counterCells == as) {
                if (U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                    boolean init = false;
                    try {
                        if (this.counterCells == as) {
                            CounterCell[] rs3 = new CounterCell[2];
                            rs3[h2 & 1] = new CounterCell(j);
                            this.counterCells = rs3;
                            init = true;
                        }
                        if (init) {
                            return;
                        }
                        wasUncontended4 = collide;
                    } finally {
                        this.cellsBusy = 0;
                    }
                }
            }
            Unsafe unsafe2 = U;
            long j4 = BASECOUNT;
            long v2 = this.baseCount;
            CounterCell[] counterCellArr3 = as;
            if (unsafe2.compareAndSwapLong(this, j4, v2, v2 + j)) {
                return;
            }
            wasUncontended4 = collide;
        }
    }

    private final void treeifyBin(Node<K, V>[] tab, int index) {
        if (tab != null) {
            int length = tab.length;
            int n = length;
            if (length < 64) {
                tryPresize(n << 1);
                return;
            }
            Node<K, V> tabAt = tabAt(tab, index);
            Node<K, V> b = tabAt;
            if (tabAt != null && b.hash >= 0) {
                synchronized (b) {
                    if (tabAt(tab, index) == b) {
                        TreeNode<K, V> tl = null;
                        TreeNode<K, V> hd = null;
                        for (Node<K, V> e = b; e != null; e = e.next) {
                            TreeNode<K, V> treeNode = new TreeNode<>(e.hash, e.key, e.val, null, null);
                            TreeNode<K, V> p = treeNode;
                            p.prev = tl;
                            if (tl == null) {
                                hd = p;
                            } else {
                                tl.next = p;
                            }
                            tl = p;
                        }
                        setTabAt(tab, index, new TreeBin(hd));
                    }
                }
            }
        }
    }

    static <K, V> Node<K, V> untreeify(Node<K, V> b) {
        Node<K, V> tl = null;
        Node<K, V> hd = null;
        for (Node<K, V> q = b; q != null; q = q.next) {
            Node<K, V> p = new Node<>(q.hash, q.key, q.val, null);
            if (tl == null) {
                hd = p;
            } else {
                tl.next = p;
            }
            tl = p;
        }
        return hd;
    }

    /* access modifiers changed from: package-private */
    public final int batchFor(long b) {
        int i;
        if (b != Long.MAX_VALUE) {
            long sumCount = sumCount();
            long n = sumCount;
            if (sumCount > 1 && n >= b) {
                int sp = ForkJoinPool.getCommonPoolParallelism() << 2;
                if (b > 0) {
                    long j = n / b;
                    long n2 = j;
                    if (j < ((long) sp)) {
                        i = (int) n2;
                        return i;
                    }
                }
                i = sp;
                return i;
            }
        }
        return 0;
    }

    public void forEach(long parallelismThreshold, BiConsumer<? super K, ? super V> action) {
        if (action != null) {
            ForEachMappingTask forEachMappingTask = new ForEachMappingTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action);
            forEachMappingTask.invoke();
            return;
        }
        throw new NullPointerException();
    }

    public <U> void forEach(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null) {
            throw new NullPointerException();
        }
        ForEachTransformedMappingTask forEachTransformedMappingTask = new ForEachTransformedMappingTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action);
        forEachTransformedMappingTask.invoke();
    }

    public <U> U search(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> searchFunction) {
        if (searchFunction != null) {
            SearchMappingsTask searchMappingsTask = new SearchMappingsTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference());
            return searchMappingsTask.invoke();
        }
        throw new NullPointerException();
    }

    public <U> U reduce(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceMappingsTask mapReduceMappingsTask = new MapReduceMappingsTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer);
        return mapReduceMappingsTask.invoke();
    }

    public double reduceToDouble(long parallelismThreshold, ToDoubleBiFunction<? super K, ? super V> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceMappingsToDoubleTask mapReduceMappingsToDoubleTask = new MapReduceMappingsToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Double) mapReduceMappingsToDoubleTask.invoke()).doubleValue();
    }

    public long reduceToLong(long parallelismThreshold, ToLongBiFunction<? super K, ? super V> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceMappingsToLongTask mapReduceMappingsToLongTask = new MapReduceMappingsToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Long) mapReduceMappingsToLongTask.invoke()).longValue();
    }

    public int reduceToInt(long parallelismThreshold, ToIntBiFunction<? super K, ? super V> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceMappingsToIntTask mapReduceMappingsToIntTask = new MapReduceMappingsToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Integer) mapReduceMappingsToIntTask.invoke()).intValue();
    }

    public void forEachKey(long parallelismThreshold, Consumer<? super K> action) {
        if (action != null) {
            ForEachKeyTask forEachKeyTask = new ForEachKeyTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action);
            forEachKeyTask.invoke();
            return;
        }
        throw new NullPointerException();
    }

    public <U> void forEachKey(long parallelismThreshold, Function<? super K, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null) {
            throw new NullPointerException();
        }
        ForEachTransformedKeyTask forEachTransformedKeyTask = new ForEachTransformedKeyTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action);
        forEachTransformedKeyTask.invoke();
    }

    public <U> U searchKeys(long parallelismThreshold, Function<? super K, ? extends U> searchFunction) {
        if (searchFunction != null) {
            SearchKeysTask searchKeysTask = new SearchKeysTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference());
            return searchKeysTask.invoke();
        }
        throw new NullPointerException();
    }

    public K reduceKeys(long parallelismThreshold, BiFunction<? super K, ? super K, ? extends K> reducer) {
        if (reducer != null) {
            ReduceKeysTask reduceKeysTask = new ReduceKeysTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, reducer);
            return reduceKeysTask.invoke();
        }
        throw new NullPointerException();
    }

    public <U> U reduceKeys(long parallelismThreshold, Function<? super K, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceKeysTask mapReduceKeysTask = new MapReduceKeysTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer);
        return mapReduceKeysTask.invoke();
    }

    public double reduceKeysToDouble(long parallelismThreshold, ToDoubleFunction<? super K> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceKeysToDoubleTask mapReduceKeysToDoubleTask = new MapReduceKeysToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Double) mapReduceKeysToDoubleTask.invoke()).doubleValue();
    }

    public long reduceKeysToLong(long parallelismThreshold, ToLongFunction<? super K> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceKeysToLongTask mapReduceKeysToLongTask = new MapReduceKeysToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Long) mapReduceKeysToLongTask.invoke()).longValue();
    }

    public int reduceKeysToInt(long parallelismThreshold, ToIntFunction<? super K> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceKeysToIntTask mapReduceKeysToIntTask = new MapReduceKeysToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Integer) mapReduceKeysToIntTask.invoke()).intValue();
    }

    public void forEachValue(long parallelismThreshold, Consumer<? super V> action) {
        if (action != null) {
            ForEachValueTask forEachValueTask = new ForEachValueTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action);
            forEachValueTask.invoke();
            return;
        }
        throw new NullPointerException();
    }

    public <U> void forEachValue(long parallelismThreshold, Function<? super V, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null) {
            throw new NullPointerException();
        }
        ForEachTransformedValueTask forEachTransformedValueTask = new ForEachTransformedValueTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action);
        forEachTransformedValueTask.invoke();
    }

    public <U> U searchValues(long parallelismThreshold, Function<? super V, ? extends U> searchFunction) {
        if (searchFunction != null) {
            SearchValuesTask searchValuesTask = new SearchValuesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference());
            return searchValuesTask.invoke();
        }
        throw new NullPointerException();
    }

    public V reduceValues(long parallelismThreshold, BiFunction<? super V, ? super V, ? extends V> reducer) {
        if (reducer != null) {
            ReduceValuesTask reduceValuesTask = new ReduceValuesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, reducer);
            return reduceValuesTask.invoke();
        }
        throw new NullPointerException();
    }

    public <U> U reduceValues(long parallelismThreshold, Function<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceValuesTask mapReduceValuesTask = new MapReduceValuesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer);
        return mapReduceValuesTask.invoke();
    }

    public double reduceValuesToDouble(long parallelismThreshold, ToDoubleFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceValuesToDoubleTask mapReduceValuesToDoubleTask = new MapReduceValuesToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Double) mapReduceValuesToDoubleTask.invoke()).doubleValue();
    }

    public long reduceValuesToLong(long parallelismThreshold, ToLongFunction<? super V> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceValuesToLongTask mapReduceValuesToLongTask = new MapReduceValuesToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Long) mapReduceValuesToLongTask.invoke()).longValue();
    }

    public int reduceValuesToInt(long parallelismThreshold, ToIntFunction<? super V> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceValuesToIntTask mapReduceValuesToIntTask = new MapReduceValuesToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Integer) mapReduceValuesToIntTask.invoke()).intValue();
    }

    public void forEachEntry(long parallelismThreshold, Consumer<? super Map.Entry<K, V>> action) {
        if (action != null) {
            ForEachEntryTask forEachEntryTask = new ForEachEntryTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action);
            forEachEntryTask.invoke();
            return;
        }
        throw new NullPointerException();
    }

    public <U> void forEachEntry(long parallelismThreshold, Function<Map.Entry<K, V>, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null) {
            throw new NullPointerException();
        }
        ForEachTransformedEntryTask forEachTransformedEntryTask = new ForEachTransformedEntryTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action);
        forEachTransformedEntryTask.invoke();
    }

    public <U> U searchEntries(long parallelismThreshold, Function<Map.Entry<K, V>, ? extends U> searchFunction) {
        if (searchFunction != null) {
            SearchEntriesTask searchEntriesTask = new SearchEntriesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference());
            return searchEntriesTask.invoke();
        }
        throw new NullPointerException();
    }

    public Map.Entry<K, V> reduceEntries(long parallelismThreshold, BiFunction<Map.Entry<K, V>, Map.Entry<K, V>, ? extends Map.Entry<K, V>> reducer) {
        if (reducer != null) {
            ReduceEntriesTask reduceEntriesTask = new ReduceEntriesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, reducer);
            return (Map.Entry) reduceEntriesTask.invoke();
        }
        throw new NullPointerException();
    }

    public <U> U reduceEntries(long parallelismThreshold, Function<Map.Entry<K, V>, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceEntriesTask mapReduceEntriesTask = new MapReduceEntriesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer);
        return mapReduceEntriesTask.invoke();
    }

    public double reduceEntriesToDouble(long parallelismThreshold, ToDoubleFunction<Map.Entry<K, V>> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceEntriesToDoubleTask mapReduceEntriesToDoubleTask = new MapReduceEntriesToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Double) mapReduceEntriesToDoubleTask.invoke()).doubleValue();
    }

    public long reduceEntriesToLong(long parallelismThreshold, ToLongFunction<Map.Entry<K, V>> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceEntriesToLongTask mapReduceEntriesToLongTask = new MapReduceEntriesToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Long) mapReduceEntriesToLongTask.invoke()).longValue();
    }

    public int reduceEntriesToInt(long parallelismThreshold, ToIntFunction<Map.Entry<K, V>> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        MapReduceEntriesToIntTask mapReduceEntriesToIntTask = new MapReduceEntriesToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer);
        return ((Integer) mapReduceEntriesToIntTask.invoke()).intValue();
    }
}
