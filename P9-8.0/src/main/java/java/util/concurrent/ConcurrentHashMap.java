package java.util.concurrent;

import java.awt.font.NumericShaper;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectOutputStream.PutField;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
    private static final ObjectStreamField[] serialPersistentFields = new ObjectStreamField[]{new ObjectStreamField("segments", Segment[].class), new ObjectStreamField("segmentMask", Integer.TYPE), new ObjectStreamField("segmentShift", Integer.TYPE)};
    private static final long serialVersionUID = 7249069246763182397L;
    private volatile transient long baseCount;
    private volatile transient int cellsBusy;
    private volatile transient CounterCell[] counterCells;
    private transient EntrySetView<K, V> entrySet;
    private transient KeySetView<K, V> keySet;
    private volatile transient Node<K, V>[] nextTable;
    private volatile transient int sizeCtl;
    volatile transient Node<K, V>[] table;
    private volatile transient int transferIndex;
    private transient ValuesView<K, V> values;

    static class Traverser<K, V> {
        int baseIndex;
        int baseLimit;
        final int baseSize;
        int index;
        Node<K, V> next = null;
        TableStack<K, V> spare;
        TableStack<K, V> stack;
        Node<K, V>[] tab;

        Traverser(Node<K, V>[] tab, int size, int index, int limit) {
            this.tab = tab;
            this.baseSize = size;
            this.index = index;
            this.baseIndex = index;
            this.baseLimit = limit;
        }

        final Node<K, V> advance() {
            Node<K, V> e = this.next;
            if (e != null) {
                e = e.next;
            }
            while (e == null) {
                if (this.baseIndex < this.baseLimit) {
                    Node<K, V>[] t = this.tab;
                    if (t != null) {
                        int n = t.length;
                        int i = this.index;
                        if (n > i && i >= 0) {
                            e = ConcurrentHashMap.tabAt(t, i);
                            if (e != null && e.hash < 0) {
                                if (e instanceof ForwardingNode) {
                                    this.tab = ((ForwardingNode) e).nextTable;
                                    e = null;
                                    pushState(t, i, n);
                                } else {
                                    e = e instanceof TreeBin ? ((TreeBin) e).first : null;
                                }
                            }
                            if (this.stack != null) {
                                recoverState(n);
                            } else {
                                int i2 = this.baseSize + i;
                                this.index = i2;
                                if (i2 >= n) {
                                    i2 = this.baseIndex + 1;
                                    this.baseIndex = i2;
                                    this.index = i2;
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
                s = new TableStack();
            }
            s.tab = t;
            s.length = n;
            s.index = i;
            s.next = this.stack;
            this.stack = s;
        }

        private void recoverState(int n) {
            TableStack<K, V> s;
            int i;
            while (true) {
                s = this.stack;
                if (s == null) {
                    break;
                }
                i = this.index;
                int len = s.length;
                i += len;
                this.index = i;
                if (i < n) {
                    break;
                }
                n = len;
                this.index = s.index;
                this.tab = s.tab;
                s.tab = null;
                TableStack<K, V> next = s.next;
                s.next = this.spare;
                this.stack = next;
                this.spare = s;
            }
            if (s == null) {
                i = this.index + this.baseSize;
                this.index = i;
                if (i >= n) {
                    i = this.baseIndex + 1;
                    this.baseIndex = i;
                    this.index = i;
                }
            }
        }
    }

    static class BaseIterator<K, V> extends Traverser<K, V> {
        Node<K, V> lastReturned;
        final ConcurrentHashMap<K, V> map;

        BaseIterator(Node<K, V>[] tab, int size, int index, int limit, ConcurrentHashMap<K, V> map) {
            super(tab, size, index, limit);
            this.map = map;
            advance();
        }

        public final boolean hasNext() {
            return this.next != null;
        }

        public final boolean hasMoreElements() {
            return this.next != null;
        }

        public final void remove() {
            Node<K, V> p = this.lastReturned;
            if (p == null) {
                throw new IllegalStateException();
            }
            this.lastReturned = null;
            this.map.replaceNode(p.key, null, null);
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

        final Node<K, V> advance() {
            Node<K, V> e = this.next;
            if (e != null) {
                e = e.next;
            }
            while (e == null) {
                if (this.baseIndex < this.baseLimit) {
                    Node<K, V>[] t = this.tab;
                    if (t != null) {
                        int n = t.length;
                        int i = this.index;
                        if (n > i && i >= 0) {
                            e = ConcurrentHashMap.tabAt(t, i);
                            if (e != null && e.hash < 0) {
                                if (e instanceof ForwardingNode) {
                                    this.tab = ((ForwardingNode) e).nextTable;
                                    e = null;
                                    pushState(t, i, n);
                                } else {
                                    e = e instanceof TreeBin ? ((TreeBin) e).first : null;
                                }
                            }
                            if (this.stack != null) {
                                recoverState(n);
                            } else {
                                int i2 = this.baseSize + i;
                                this.index = i2;
                                if (i2 >= n) {
                                    i2 = this.baseIndex + 1;
                                    this.baseIndex = i2;
                                    this.index = i2;
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
                s = new TableStack();
            }
            s.tab = t;
            s.length = n;
            s.index = i;
            s.next = this.stack;
            this.stack = s;
        }

        private void recoverState(int n) {
            TableStack<K, V> s;
            int i;
            while (true) {
                s = this.stack;
                if (s == null) {
                    break;
                }
                i = this.index;
                int len = s.length;
                i += len;
                this.index = i;
                if (i < n) {
                    break;
                }
                n = len;
                this.index = s.index;
                this.tab = s.tab;
                s.tab = null;
                TableStack<K, V> next = s.next;
                s.next = this.spare;
                this.stack = next;
                this.spare = s;
            }
            if (s == null) {
                i = this.index + this.baseSize;
                this.index = i;
                if (i >= n) {
                    i = this.baseIndex + 1;
                    this.baseIndex = i;
                    this.index = i;
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

        CollectionView(ConcurrentHashMap<K, V> map) {
            this.map = map;
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
            if (sz > 2147483639) {
                throw new OutOfMemoryError(OOME_MSG);
            }
            int n = (int) sz;
            Object[] r = new Object[n];
            int i = 0;
            for (E e : this) {
                if (i == n) {
                    if (n >= ConcurrentHashMap.MAX_ARRAY_SIZE) {
                        throw new OutOfMemoryError(OOME_MSG);
                    }
                    if (n >= 1073741819) {
                        n = ConcurrentHashMap.MAX_ARRAY_SIZE;
                    } else {
                        n += (n >>> 1) + 1;
                    }
                    r = Arrays.copyOf(r, n);
                }
                int i2 = i + 1;
                r[i] = e;
                i = i2;
            }
            return i == n ? r : Arrays.copyOf(r, i);
        }

        public final <T> T[] toArray(T[] a) {
            long sz = this.map.mappingCount();
            if (sz > 2147483639) {
                throw new OutOfMemoryError(OOME_MSG);
            }
            Object[] r;
            int m = (int) sz;
            if (a.length >= m) {
                r = a;
            } else {
                r = (Object[]) Array.newInstance(a.getClass().getComponentType(), m);
            }
            int n = r.length;
            int i = 0;
            for (E e : this) {
                if (i == n) {
                    if (n >= ConcurrentHashMap.MAX_ARRAY_SIZE) {
                        throw new OutOfMemoryError(OOME_MSG);
                    }
                    if (n >= 1073741819) {
                        n = ConcurrentHashMap.MAX_ARRAY_SIZE;
                    } else {
                        n += (n >>> 1) + 1;
                    }
                    r = Arrays.copyOf(r, n);
                }
                int i2 = i + 1;
                r[i] = e;
                i = i2;
            }
            if (a != r || i >= n) {
                T[] r2;
                if (i != n) {
                    r2 = Arrays.copyOf(r, i);
                }
                return r2;
            }
            r[i] = null;
            return r;
        }

        public final String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            Iterator<E> it = iterator();
            if (it.hasNext()) {
                while (true) {
                    Object e = it.next();
                    if (e == this) {
                        e = "(this Collection)";
                    }
                    sb.append(e);
                    if (!it.hasNext()) {
                        break;
                    }
                    sb.append(',').append(' ');
                }
            }
            return sb.append(']').toString();
        }

        public final boolean containsAll(Collection<?> c) {
            if (c != this) {
                for (Object e : c) {
                    if (e != null) {
                        if ((contains(e) ^ 1) != 0) {
                        }
                    }
                    return false;
                }
            }
            return true;
        }

        public final boolean removeAll(Collection<?> c) {
            if (c == null) {
                throw new NullPointerException();
            }
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

        public final boolean retainAll(Collection<?> c) {
            if (c == null) {
                throw new NullPointerException();
            }
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
    }

    static final class CounterCell {
        volatile long value;

        CounterCell(long x) {
            this.value = x;
        }
    }

    static final class EntryIterator<K, V> extends BaseIterator<K, V> implements Iterator<Entry<K, V>> {
        EntryIterator(Node<K, V>[] tab, int index, int size, int limit, ConcurrentHashMap<K, V> map) {
            super(tab, index, size, limit, map);
        }

        public final Entry<K, V> next() {
            Node<K, V> p = this.next;
            if (p == null) {
                throw new NoSuchElementException();
            }
            K k = p.key;
            V v = p.val;
            this.lastReturned = p;
            advance();
            return new MapEntry(k, v, this.map);
        }
    }

    static final class EntrySetView<K, V> extends CollectionView<K, V, Entry<K, V>> implements Set<Entry<K, V>>, Serializable {
        private static final long serialVersionUID = 2249069246763182397L;

        EntrySetView(ConcurrentHashMap<K, V> map) {
            super(map);
        }

        public boolean contains(Object o) {
            if (o instanceof Entry) {
                Entry<?, ?> e = (Entry) o;
                Object k = e.getKey();
                if (k != null) {
                    Object r = this.map.get(k);
                    if (r != null) {
                        Object v = e.getValue();
                        if (v != null) {
                            return v != r ? v.lambda$-java_util_function_Predicate_4628(r) : true;
                        }
                    }
                }
            }
            return false;
        }

        public boolean remove(Object o) {
            if (o instanceof Entry) {
                Entry<?, ?> e = (Entry) o;
                Object k = e.getKey();
                if (k != null) {
                    Object v = e.getValue();
                    if (v != null) {
                        return this.map.remove(k, v);
                    }
                }
            }
            return false;
        }

        public Iterator<Entry<K, V>> iterator() {
            ConcurrentHashMap<K, V> m = this.map;
            Node<K, V>[] t = m.table;
            int f = t == null ? 0 : t.length;
            return new EntryIterator(t, f, 0, f, m);
        }

        public boolean add(Entry<K, V> e) {
            return this.map.putVal(e.getKey(), e.getValue(), false) == null;
        }

        public boolean addAll(Collection<? extends Entry<K, V>> c) {
            boolean added = false;
            for (Entry e : c) {
                if (add(e)) {
                    added = true;
                }
            }
            return added;
        }

        public boolean removeIf(Predicate<? super Entry<K, V>> filter) {
            return this.map.removeEntryIf(filter);
        }

        public final int hashCode() {
            int h = 0;
            Node<K, V>[] t = this.map.table;
            if (t != null) {
                Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
                while (true) {
                    Node<K, V> p = it.advance();
                    if (p == null) {
                        break;
                    }
                    h += p.hashCode();
                }
            }
            return h;
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Set)) {
                return false;
            }
            Object c = (Set) o;
            if (c == this) {
                return true;
            }
            if (containsAll(c)) {
                return c.containsAll(this);
            }
            return false;
        }

        public Spliterator<Entry<K, V>> spliterator() {
            long j = 0;
            ConcurrentHashMap<K, V> m = this.map;
            long n = m.sumCount();
            Node<K, V>[] t = m.table;
            int f = t == null ? 0 : t.length;
            if (n >= 0) {
                j = n;
            }
            return new EntrySpliterator(t, f, 0, f, j, m);
        }

        public void forEach(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K, V>[] t = this.map.table;
            if (t != null) {
                Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
                while (true) {
                    Node<K, V> p = it.advance();
                    if (p != null) {
                        action.accept(new MapEntry(p.key, p.val, this.map));
                    } else {
                        return;
                    }
                }
            }
        }
    }

    static final class EntrySpliterator<K, V> extends Traverser<K, V> implements Spliterator<Entry<K, V>> {
        long est;
        final ConcurrentHashMap<K, V> map;

        EntrySpliterator(Node<K, V>[] tab, int size, int index, int limit, long est, ConcurrentHashMap<K, V> map) {
            super(tab, size, index, limit);
            this.map = map;
            this.est = est;
        }

        public EntrySpliterator<K, V> trySplit() {
            int i = this.baseIndex;
            int f = this.baseLimit;
            int h = (i + f) >>> 1;
            if (h <= i) {
                return null;
            }
            Node[] nodeArr = this.tab;
            int i2 = this.baseSize;
            this.baseLimit = h;
            long j = this.est >>> 1;
            this.est = j;
            return new EntrySpliterator(nodeArr, i2, h, f, j, this.map);
        }

        public void forEachRemaining(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            while (true) {
                Node<K, V> p = advance();
                if (p != null) {
                    action.accept(new MapEntry(p.key, p.val, this.map));
                } else {
                    return;
                }
            }
        }

        public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K, V> p = advance();
            if (p == null) {
                return false;
            }
            action.accept(new MapEntry(p.key, p.val, this.map));
            return true;
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return 4353;
        }
    }

    static final class ForEachEntryTask<K, V> extends BulkTask<K, V, Void> {
        final Consumer<? super Entry<K, V>> action;

        ForEachEntryTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Consumer<? super Entry<K, V>> action) {
            super(p, b, i, f, t);
            this.action = action;
        }

        public final void compute() {
            Consumer<? super Entry<K, V>> action = this.action;
            if (action != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int f = this.baseLimit;
                    int h = (f + i) >>> 1;
                    if (h <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i2 = this.batch >>> 1;
                    this.batch = i2;
                    this.baseLimit = h;
                    new ForEachEntryTask(this, i2, h, f, this.tab, action).fork();
                }
                while (true) {
                    Node<K, V> p = advance();
                    if (p != null) {
                        action.accept(p);
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

        ForEachKeyTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Consumer<? super K> action) {
            super(p, b, i, f, t);
            this.action = action;
        }

        public final void compute() {
            Consumer<? super K> action = this.action;
            if (action != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int f = this.baseLimit;
                    int h = (f + i) >>> 1;
                    if (h <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i2 = this.batch >>> 1;
                    this.batch = i2;
                    this.baseLimit = h;
                    new ForEachKeyTask(this, i2, h, f, this.tab, action).fork();
                }
                while (true) {
                    Node<K, V> p = advance();
                    if (p != null) {
                        action.accept(p.key);
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

        ForEachMappingTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, BiConsumer<? super K, ? super V> action) {
            super(p, b, i, f, t);
            this.action = action;
        }

        public final void compute() {
            BiConsumer<? super K, ? super V> action = this.action;
            if (action != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int f = this.baseLimit;
                    int h = (f + i) >>> 1;
                    if (h <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i2 = this.batch >>> 1;
                    this.batch = i2;
                    this.baseLimit = h;
                    new ForEachMappingTask(this, i2, h, f, this.tab, action).fork();
                }
                while (true) {
                    Node<K, V> p = advance();
                    if (p != null) {
                        action.accept(p.key, p.val);
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
        final Function<Entry<K, V>, ? extends U> transformer;

        ForEachTransformedEntryTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<Entry<K, V>, ? extends U> transformer, Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer;
            this.action = action;
        }

        public final void compute() {
            Function<Entry<K, V>, ? extends U> transformer = this.transformer;
            if (transformer != null) {
                Consumer<? super U> action = this.action;
                if (action != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        new ForEachTransformedEntryTask(this, i2, h, f, this.tab, transformer, action).fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p != null) {
                            U u = transformer.apply(p);
                            if (u != null) {
                                action.accept(u);
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

        ForEachTransformedKeyTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super K, ? extends U> transformer, Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer;
            this.action = action;
        }

        public final void compute() {
            Function<? super K, ? extends U> transformer = this.transformer;
            if (transformer != null) {
                Consumer<? super U> action = this.action;
                if (action != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        new ForEachTransformedKeyTask(this, i2, h, f, this.tab, transformer, action).fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p != null) {
                            U u = transformer.apply(p.key);
                            if (u != null) {
                                action.accept(u);
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

        ForEachTransformedMappingTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, BiFunction<? super K, ? super V, ? extends U> transformer, Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer;
            this.action = action;
        }

        public final void compute() {
            BiFunction<? super K, ? super V, ? extends U> transformer = this.transformer;
            if (transformer != null) {
                Consumer<? super U> action = this.action;
                if (action != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        new ForEachTransformedMappingTask(this, i2, h, f, this.tab, transformer, action).fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p != null) {
                            U u = transformer.apply(p.key, p.val);
                            if (u != null) {
                                action.accept(u);
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

        ForEachTransformedValueTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super V, ? extends U> transformer, Consumer<? super U> action) {
            super(p, b, i, f, t);
            this.transformer = transformer;
            this.action = action;
        }

        public final void compute() {
            Function<? super V, ? extends U> transformer = this.transformer;
            if (transformer != null) {
                Consumer<? super U> action = this.action;
                if (action != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        new ForEachTransformedValueTask(this, i2, h, f, this.tab, transformer, action).fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p != null) {
                            U u = transformer.apply(p.val);
                            if (u != null) {
                                action.accept(u);
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

        ForEachValueTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Consumer<? super V> action) {
            super(p, b, i, f, t);
            this.action = action;
        }

        public final void compute() {
            Consumer<? super V> action = this.action;
            if (action != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int f = this.baseLimit;
                    int h = (f + i) >>> 1;
                    if (h <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i2 = this.batch >>> 1;
                    this.batch = i2;
                    this.baseLimit = h;
                    new ForEachValueTask(this, i2, h, f, this.tab, action).fork();
                }
                while (true) {
                    Node<K, V> p = advance();
                    if (p != null) {
                        action.accept(p.val);
                    } else {
                        propagateCompletion();
                        return;
                    }
                }
            }
        }
    }

    static class Node<K, V> implements Entry<K, V> {
        final int hash;
        final K key;
        volatile Node<K, V> next;
        volatile V val;

        Node(int hash, K key, V val, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
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
            if (o instanceof Entry) {
                Entry<?, ?> e = (Entry) o;
                Object k = e.getKey();
                if (k != null) {
                    Object v = e.getValue();
                    if (v != null && (k == this.key || k.lambda$-java_util_function_Predicate_4628(this.key))) {
                        Object u = this.val;
                        return v != u ? v.lambda$-java_util_function_Predicate_4628(u) : true;
                    }
                }
            }
            return false;
        }

        Node<K, V> find(int h, Object k) {
            Node<K, V> e = this;
            if (k != null) {
                do {
                    if (e.hash == h) {
                        K ek = e.key;
                        if (ek == k || (ek != null && k.lambda$-java_util_function_Predicate_4628(ek))) {
                            return e;
                        }
                    }
                    e = e.next;
                } while (e != null);
            }
            return null;
        }
    }

    static final class ForwardingNode<K, V> extends Node<K, V> {
        final Node<K, V>[] nextTable;

        ForwardingNode(Node<K, V>[] tab) {
            super(-1, null, null, null);
            this.nextTable = tab;
        }

        /* JADX WARNING: Missing block: B:18:0x0029, code:
            if ((r0 instanceof java.util.concurrent.ConcurrentHashMap.ForwardingNode) == false) goto L_0x0030;
     */
        /* JADX WARNING: Missing block: B:19:0x002b, code:
            r4 = ((java.util.concurrent.ConcurrentHashMap.ForwardingNode) r0).nextTable;
     */
        /* JADX WARNING: Missing block: B:21:0x0034, code:
            return r0.find(r8, r9);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        Node<K, V> find(int h, Object k) {
            Node<K, V>[] tab = this.nextTable;
            loop0:
            while (k != null && tab != null) {
                int n = tab.length;
                if (n == 0) {
                    break;
                }
                Node<K, V> e = ConcurrentHashMap.tabAt(tab, (n - 1) & h);
                if (e == null) {
                    break;
                }
                while (true) {
                    int eh = e.hash;
                    if (eh == h) {
                        K ek = e.key;
                        if (ek == k || (ek != null && k.lambda$-java_util_function_Predicate_4628(ek))) {
                            return e;
                        }
                    }
                    if (eh < 0) {
                        break;
                    }
                    e = e.next;
                    if (e == null) {
                        return null;
                    }
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
            Node<K, V> p = this.next;
            if (p == null) {
                throw new NoSuchElementException();
            }
            K k = p.key;
            this.lastReturned = p;
            advance();
            return k;
        }

        public final K nextElement() {
            return next();
        }
    }

    public static class KeySetView<K, V> extends CollectionView<K, V, K> implements Set<K>, Serializable {
        private static final long serialVersionUID = 7249069246763182397L;
        private final V value;

        KeySetView(ConcurrentHashMap<K, V> map, V value) {
            super(map);
            this.value = value;
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
            Node<K, V>[] t = m.table;
            int f = t == null ? 0 : t.length;
            return new KeyIterator(t, f, 0, f, m);
        }

        public boolean add(K e) {
            V v = this.value;
            if (v == null) {
                throw new UnsupportedOperationException();
            } else if (this.map.putVal(e, v, true) == null) {
                return true;
            } else {
                return false;
            }
        }

        public boolean addAll(Collection<? extends K> c) {
            boolean added = false;
            V v = this.value;
            if (v == null) {
                throw new UnsupportedOperationException();
            }
            for (K e : c) {
                if (this.map.putVal(e, v, true) == null) {
                    added = true;
                }
            }
            return added;
        }

        public int hashCode() {
            int h = 0;
            for (K e : this) {
                h += e.hashCode();
            }
            return h;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Set)) {
                return false;
            }
            Object c = (Set) o;
            if (c == this) {
                return true;
            }
            if (containsAll(c)) {
                return c.containsAll(this);
            }
            return false;
        }

        public Spliterator<K> spliterator() {
            long j = 0;
            ConcurrentHashMap<K, V> m = this.map;
            long n = m.sumCount();
            Node<K, V>[] t = m.table;
            int f = t == null ? 0 : t.length;
            if (n >= 0) {
                j = n;
            }
            return new KeySpliterator(t, f, 0, f, j);
        }

        public void forEach(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K, V>[] t = this.map.table;
            if (t != null) {
                Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
                while (true) {
                    Node<K, V> p = it.advance();
                    if (p != null) {
                        action.accept(p.key);
                    } else {
                        return;
                    }
                }
            }
        }
    }

    static final class KeySpliterator<K, V> extends Traverser<K, V> implements Spliterator<K> {
        long est;

        KeySpliterator(Node<K, V>[] tab, int size, int index, int limit, long est) {
            super(tab, size, index, limit);
            this.est = est;
        }

        public KeySpliterator<K, V> trySplit() {
            int i = this.baseIndex;
            int f = this.baseLimit;
            int h = (i + f) >>> 1;
            if (h <= i) {
                return null;
            }
            Node[] nodeArr = this.tab;
            int i2 = this.baseSize;
            this.baseLimit = h;
            long j = this.est >>> 1;
            this.est = j;
            return new KeySpliterator(nodeArr, i2, h, f, j);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            while (true) {
                Node<K, V> p = advance();
                if (p != null) {
                    action.accept(p.key);
                } else {
                    return;
                }
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K, V> p = advance();
            if (p == null) {
                return false;
            }
            action.accept(p.key);
            return true;
        }

        public long estimateSize() {
            return this.est;
        }

        public int characteristics() {
            return 4353;
        }
    }

    static final class MapEntry<K, V> implements Entry<K, V> {
        final K key;
        final ConcurrentHashMap<K, V> map;
        V val;

        MapEntry(K key, V val, ConcurrentHashMap<K, V> map) {
            this.key = key;
            this.val = val;
            this.map = map;
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
            if (o instanceof Entry) {
                Entry<?, ?> e = (Entry) o;
                Object k = e.getKey();
                if (k != null) {
                    Object v = e.getValue();
                    if (v != null && (k == this.key || k.lambda$-java_util_function_Predicate_4628(this.key))) {
                        return v != this.val ? v.lambda$-java_util_function_Predicate_4628(this.val) : true;
                    }
                }
            }
            return false;
        }

        public V setValue(V value) {
            if (value == null) {
                throw new NullPointerException();
            }
            V v = this.val;
            this.val = value;
            this.map.put(this.key, value);
            return v;
        }
    }

    static final class MapReduceEntriesTask<K, V, U> extends BulkTask<K, V, U> {
        MapReduceEntriesTask<K, V, U> nextRight;
        final BiFunction<? super U, ? super U, ? extends U> reducer;
        U result;
        MapReduceEntriesTask<K, V, U> rights;
        final Function<Entry<K, V>, ? extends U> transformer;

        MapReduceEntriesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesTask<K, V, U> nextRight, Function<Entry<K, V>, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }

        public final U getRawResult() {
            return this.result;
        }

        public final void compute() {
            Function<Entry<K, V>, ? extends U> transformer = this.transformer;
            if (transformer != null) {
                BiFunction<? super U, ? super U, ? extends U> reducer = this.reducer;
                if (reducer != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceEntriesTask mapReduceEntriesTask = new MapReduceEntriesTask(this, i2, h, f, this.tab, this.rights, transformer, reducer);
                        this.rights = mapReduceEntriesTask;
                        mapReduceEntriesTask.fork();
                    }
                    Object r = null;
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        U u = transformer.apply(p);
                        if (u != null) {
                            r = r == null ? u : reducer.apply(r, u);
                        }
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceEntriesTask<K, V, U> t = (MapReduceEntriesTask) c;
                        MapReduceEntriesTask<K, V, U> s = t.rights;
                        while (s != null) {
                            Object sr = s.result;
                            if (sr != null) {
                                U tr = t.result;
                                if (tr != null) {
                                    sr = reducer.apply(tr, sr);
                                }
                                t.result = sr;
                            }
                            s = s.nextRight;
                            t.rights = s;
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
        final ToDoubleFunction<Entry<K, V>> transformer;

        MapReduceEntriesToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesToDoubleTask<K, V> nextRight, ToDoubleFunction<Entry<K, V>> transformer, double basis, DoubleBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Double getRawResult() {
            return Double.valueOf(this.result);
        }

        public final void compute() {
            ToDoubleFunction<Entry<K, V>> transformer = this.transformer;
            if (transformer != null) {
                DoubleBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    double r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceEntriesToDoubleTask mapReduceEntriesToDoubleTask = new MapReduceEntriesToDoubleTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceEntriesToDoubleTask;
                        mapReduceEntriesToDoubleTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsDouble(r, transformer.applyAsDouble(p));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceEntriesToDoubleTask<K, V> t = (MapReduceEntriesToDoubleTask) c;
                        MapReduceEntriesToDoubleTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsDouble(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
                        }
                    }
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
        final ToIntFunction<Entry<K, V>> transformer;

        MapReduceEntriesToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesToIntTask<K, V> nextRight, ToIntFunction<Entry<K, V>> transformer, int basis, IntBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Integer getRawResult() {
            return Integer.valueOf(this.result);
        }

        public final void compute() {
            ToIntFunction<Entry<K, V>> transformer = this.transformer;
            if (transformer != null) {
                IntBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    int r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceEntriesToIntTask mapReduceEntriesToIntTask = new MapReduceEntriesToIntTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceEntriesToIntTask;
                        mapReduceEntriesToIntTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsInt(r, transformer.applyAsInt(p));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceEntriesToIntTask<K, V> t = (MapReduceEntriesToIntTask) c;
                        MapReduceEntriesToIntTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsInt(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
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
        final ToLongFunction<Entry<K, V>> transformer;

        MapReduceEntriesToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceEntriesToLongTask<K, V> nextRight, ToLongFunction<Entry<K, V>> transformer, long basis, LongBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Long getRawResult() {
            return Long.valueOf(this.result);
        }

        public final void compute() {
            ToLongFunction<Entry<K, V>> transformer = this.transformer;
            if (transformer != null) {
                LongBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    long r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceEntriesToLongTask mapReduceEntriesToLongTask = new MapReduceEntriesToLongTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceEntriesToLongTask;
                        mapReduceEntriesToLongTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsLong(r, transformer.applyAsLong(p));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceEntriesToLongTask<K, V> t = (MapReduceEntriesToLongTask) c;
                        MapReduceEntriesToLongTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsLong(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
                        }
                    }
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

        MapReduceKeysTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysTask<K, V, U> nextRight, Function<? super K, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }

        public final U getRawResult() {
            return this.result;
        }

        public final void compute() {
            Function<? super K, ? extends U> transformer = this.transformer;
            if (transformer != null) {
                BiFunction<? super U, ? super U, ? extends U> reducer = this.reducer;
                if (reducer != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceKeysTask mapReduceKeysTask = new MapReduceKeysTask(this, i2, h, f, this.tab, this.rights, transformer, reducer);
                        this.rights = mapReduceKeysTask;
                        mapReduceKeysTask.fork();
                    }
                    Object r = null;
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        U u = transformer.apply(p.key);
                        if (u != null) {
                            r = r == null ? u : reducer.apply(r, u);
                        }
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceKeysTask<K, V, U> t = (MapReduceKeysTask) c;
                        MapReduceKeysTask<K, V, U> s = t.rights;
                        while (s != null) {
                            Object sr = s.result;
                            if (sr != null) {
                                U tr = t.result;
                                if (tr != null) {
                                    sr = reducer.apply(tr, sr);
                                }
                                t.result = sr;
                            }
                            s = s.nextRight;
                            t.rights = s;
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

        MapReduceKeysToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysToDoubleTask<K, V> nextRight, ToDoubleFunction<? super K> transformer, double basis, DoubleBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Double getRawResult() {
            return Double.valueOf(this.result);
        }

        public final void compute() {
            ToDoubleFunction<? super K> transformer = this.transformer;
            if (transformer != null) {
                DoubleBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    double r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceKeysToDoubleTask mapReduceKeysToDoubleTask = new MapReduceKeysToDoubleTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceKeysToDoubleTask;
                        mapReduceKeysToDoubleTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceKeysToDoubleTask<K, V> t = (MapReduceKeysToDoubleTask) c;
                        MapReduceKeysToDoubleTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsDouble(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
                        }
                    }
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

        MapReduceKeysToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysToIntTask<K, V> nextRight, ToIntFunction<? super K> transformer, int basis, IntBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Integer getRawResult() {
            return Integer.valueOf(this.result);
        }

        public final void compute() {
            ToIntFunction<? super K> transformer = this.transformer;
            if (transformer != null) {
                IntBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    int r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceKeysToIntTask mapReduceKeysToIntTask = new MapReduceKeysToIntTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceKeysToIntTask;
                        mapReduceKeysToIntTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsInt(r, transformer.applyAsInt(p.key));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceKeysToIntTask<K, V> t = (MapReduceKeysToIntTask) c;
                        MapReduceKeysToIntTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsInt(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
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

        MapReduceKeysToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceKeysToLongTask<K, V> nextRight, ToLongFunction<? super K> transformer, long basis, LongBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Long getRawResult() {
            return Long.valueOf(this.result);
        }

        public final void compute() {
            ToLongFunction<? super K> transformer = this.transformer;
            if (transformer != null) {
                LongBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    long r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceKeysToLongTask mapReduceKeysToLongTask = new MapReduceKeysToLongTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceKeysToLongTask;
                        mapReduceKeysToLongTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsLong(r, transformer.applyAsLong(p.key));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceKeysToLongTask<K, V> t = (MapReduceKeysToLongTask) c;
                        MapReduceKeysToLongTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsLong(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
                        }
                    }
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

        MapReduceMappingsTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsTask<K, V, U> nextRight, BiFunction<? super K, ? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }

        public final U getRawResult() {
            return this.result;
        }

        public final void compute() {
            BiFunction<? super K, ? super V, ? extends U> transformer = this.transformer;
            if (transformer != null) {
                BiFunction<? super U, ? super U, ? extends U> reducer = this.reducer;
                if (reducer != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceMappingsTask mapReduceMappingsTask = new MapReduceMappingsTask(this, i2, h, f, this.tab, this.rights, transformer, reducer);
                        this.rights = mapReduceMappingsTask;
                        mapReduceMappingsTask.fork();
                    }
                    Object r = null;
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        U u = transformer.apply(p.key, p.val);
                        if (u != null) {
                            r = r == null ? u : reducer.apply(r, u);
                        }
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceMappingsTask<K, V, U> t = (MapReduceMappingsTask) c;
                        MapReduceMappingsTask<K, V, U> s = t.rights;
                        while (s != null) {
                            Object sr = s.result;
                            if (sr != null) {
                                U tr = t.result;
                                if (tr != null) {
                                    sr = reducer.apply(tr, sr);
                                }
                                t.result = sr;
                            }
                            s = s.nextRight;
                            t.rights = s;
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

        MapReduceMappingsToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsToDoubleTask<K, V> nextRight, ToDoubleBiFunction<? super K, ? super V> transformer, double basis, DoubleBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Double getRawResult() {
            return Double.valueOf(this.result);
        }

        public final void compute() {
            ToDoubleBiFunction<? super K, ? super V> transformer = this.transformer;
            if (transformer != null) {
                DoubleBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    double r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceMappingsToDoubleTask mapReduceMappingsToDoubleTask = new MapReduceMappingsToDoubleTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceMappingsToDoubleTask;
                        mapReduceMappingsToDoubleTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.key, p.val));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceMappingsToDoubleTask<K, V> t = (MapReduceMappingsToDoubleTask) c;
                        MapReduceMappingsToDoubleTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsDouble(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
                        }
                    }
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

        MapReduceMappingsToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsToIntTask<K, V> nextRight, ToIntBiFunction<? super K, ? super V> transformer, int basis, IntBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Integer getRawResult() {
            return Integer.valueOf(this.result);
        }

        public final void compute() {
            ToIntBiFunction<? super K, ? super V> transformer = this.transformer;
            if (transformer != null) {
                IntBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    int r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceMappingsToIntTask mapReduceMappingsToIntTask = new MapReduceMappingsToIntTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceMappingsToIntTask;
                        mapReduceMappingsToIntTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsInt(r, transformer.applyAsInt(p.key, p.val));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceMappingsToIntTask<K, V> t = (MapReduceMappingsToIntTask) c;
                        MapReduceMappingsToIntTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsInt(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
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

        MapReduceMappingsToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceMappingsToLongTask<K, V> nextRight, ToLongBiFunction<? super K, ? super V> transformer, long basis, LongBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Long getRawResult() {
            return Long.valueOf(this.result);
        }

        public final void compute() {
            ToLongBiFunction<? super K, ? super V> transformer = this.transformer;
            if (transformer != null) {
                LongBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    long r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceMappingsToLongTask mapReduceMappingsToLongTask = new MapReduceMappingsToLongTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceMappingsToLongTask;
                        mapReduceMappingsToLongTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsLong(r, transformer.applyAsLong(p.key, p.val));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceMappingsToLongTask<K, V> t = (MapReduceMappingsToLongTask) c;
                        MapReduceMappingsToLongTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsLong(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
                        }
                    }
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

        MapReduceValuesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesTask<K, V, U> nextRight, Function<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.reducer = reducer;
        }

        public final U getRawResult() {
            return this.result;
        }

        public final void compute() {
            Function<? super V, ? extends U> transformer = this.transformer;
            if (transformer != null) {
                BiFunction<? super U, ? super U, ? extends U> reducer = this.reducer;
                if (reducer != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceValuesTask mapReduceValuesTask = new MapReduceValuesTask(this, i2, h, f, this.tab, this.rights, transformer, reducer);
                        this.rights = mapReduceValuesTask;
                        mapReduceValuesTask.fork();
                    }
                    Object r = null;
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        U u = transformer.apply(p.val);
                        if (u != null) {
                            r = r == null ? u : reducer.apply(r, u);
                        }
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceValuesTask<K, V, U> t = (MapReduceValuesTask) c;
                        MapReduceValuesTask<K, V, U> s = t.rights;
                        while (s != null) {
                            Object sr = s.result;
                            if (sr != null) {
                                U tr = t.result;
                                if (tr != null) {
                                    sr = reducer.apply(tr, sr);
                                }
                                t.result = sr;
                            }
                            s = s.nextRight;
                            t.rights = s;
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

        MapReduceValuesToDoubleTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesToDoubleTask<K, V> nextRight, ToDoubleFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Double getRawResult() {
            return Double.valueOf(this.result);
        }

        public final void compute() {
            ToDoubleFunction<? super V> transformer = this.transformer;
            if (transformer != null) {
                DoubleBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    double r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceValuesToDoubleTask mapReduceValuesToDoubleTask = new MapReduceValuesToDoubleTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceValuesToDoubleTask;
                        mapReduceValuesToDoubleTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsDouble(r, transformer.applyAsDouble(p.val));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceValuesToDoubleTask<K, V> t = (MapReduceValuesToDoubleTask) c;
                        MapReduceValuesToDoubleTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsDouble(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
                        }
                    }
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

        MapReduceValuesToIntTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesToIntTask<K, V> nextRight, ToIntFunction<? super V> transformer, int basis, IntBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Integer getRawResult() {
            return Integer.valueOf(this.result);
        }

        public final void compute() {
            ToIntFunction<? super V> transformer = this.transformer;
            if (transformer != null) {
                IntBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    int r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceValuesToIntTask mapReduceValuesToIntTask = new MapReduceValuesToIntTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceValuesToIntTask;
                        mapReduceValuesToIntTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsInt(r, transformer.applyAsInt(p.val));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceValuesToIntTask<K, V> t = (MapReduceValuesToIntTask) c;
                        MapReduceValuesToIntTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsInt(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
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

        MapReduceValuesToLongTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, MapReduceValuesToLongTask<K, V> nextRight, ToLongFunction<? super V> transformer, long basis, LongBinaryOperator reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.transformer = transformer;
            this.basis = basis;
            this.reducer = reducer;
        }

        public final Long getRawResult() {
            return Long.valueOf(this.result);
        }

        public final void compute() {
            ToLongFunction<? super V> transformer = this.transformer;
            if (transformer != null) {
                LongBinaryOperator reducer = this.reducer;
                if (reducer != null) {
                    long r = this.basis;
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        }
                        addToPendingCount(1);
                        int i2 = this.batch >>> 1;
                        this.batch = i2;
                        this.baseLimit = h;
                        MapReduceValuesToLongTask mapReduceValuesToLongTask = new MapReduceValuesToLongTask(this, i2, h, f, this.tab, this.rights, transformer, r, reducer);
                        this.rights = mapReduceValuesToLongTask;
                        mapReduceValuesToLongTask.fork();
                    }
                    while (true) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            break;
                        }
                        r = reducer.applyAsLong(r, transformer.applyAsLong(p.val));
                    }
                    this.result = r;
                    for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                        MapReduceValuesToLongTask<K, V> t = (MapReduceValuesToLongTask) c;
                        MapReduceValuesToLongTask<K, V> s = t.rights;
                        while (s != null) {
                            t.result = reducer.applyAsLong(t.result, s.result);
                            s = s.nextRight;
                            t.rights = s;
                        }
                    }
                }
            }
        }
    }

    static final class ReduceEntriesTask<K, V> extends BulkTask<K, V, Entry<K, V>> {
        ReduceEntriesTask<K, V> nextRight;
        final BiFunction<Entry<K, V>, Entry<K, V>, ? extends Entry<K, V>> reducer;
        Entry<K, V> result;
        ReduceEntriesTask<K, V> rights;

        ReduceEntriesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, ReduceEntriesTask<K, V> nextRight, BiFunction<Entry<K, V>, Entry<K, V>, ? extends Entry<K, V>> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.reducer = reducer;
        }

        public final Entry<K, V> getRawResult() {
            return this.result;
        }

        public final void compute() {
            BiFunction<Entry<K, V>, Entry<K, V>, ? extends Entry<K, V>> reducer = this.reducer;
            if (reducer != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int f = this.baseLimit;
                    int h = (f + i) >>> 1;
                    if (h <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i2 = this.batch >>> 1;
                    this.batch = i2;
                    this.baseLimit = h;
                    ReduceEntriesTask reduceEntriesTask = new ReduceEntriesTask(this, i2, h, f, this.tab, this.rights, reducer);
                    this.rights = reduceEntriesTask;
                    reduceEntriesTask.fork();
                }
                Entry r = null;
                while (true) {
                    Node<K, V> p = advance();
                    if (p == null) {
                        break;
                    }
                    r = r == null ? p : (Entry) reducer.apply(r, p);
                }
                this.result = r;
                for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                    ReduceEntriesTask<K, V> t = (ReduceEntriesTask) c;
                    ReduceEntriesTask<K, V> s = t.rights;
                    while (s != null) {
                        Entry<K, V> sr = s.result;
                        if (sr != null) {
                            Entry<K, V> tr = t.result;
                            if (tr != null) {
                                sr = (Entry) reducer.apply(tr, sr);
                            }
                            t.result = sr;
                        }
                        s = s.nextRight;
                        t.rights = s;
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

        ReduceKeysTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, ReduceKeysTask<K, V> nextRight, BiFunction<? super K, ? super K, ? extends K> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.reducer = reducer;
        }

        public final K getRawResult() {
            return this.result;
        }

        public final void compute() {
            BiFunction<? super K, ? super K, ? extends K> reducer = this.reducer;
            if (reducer != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int f = this.baseLimit;
                    int h = (f + i) >>> 1;
                    if (h <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i2 = this.batch >>> 1;
                    this.batch = i2;
                    this.baseLimit = h;
                    ReduceKeysTask reduceKeysTask = new ReduceKeysTask(this, i2, h, f, this.tab, this.rights, reducer);
                    this.rights = reduceKeysTask;
                    reduceKeysTask.fork();
                }
                Object r = null;
                while (true) {
                    Node<K, V> p = advance();
                    if (p == null) {
                        break;
                    }
                    K u = p.key;
                    if (r == null) {
                        r = u;
                    } else if (u != null) {
                        r = reducer.apply(r, u);
                    }
                }
                this.result = r;
                for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                    ReduceKeysTask<K, V> t = (ReduceKeysTask) c;
                    ReduceKeysTask<K, V> s = t.rights;
                    while (s != null) {
                        K sr = s.result;
                        if (sr != null) {
                            K tr = t.result;
                            if (tr != null) {
                                sr = reducer.apply(tr, sr);
                            }
                            t.result = sr;
                        }
                        s = s.nextRight;
                        t.rights = s;
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

        ReduceValuesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, ReduceValuesTask<K, V> nextRight, BiFunction<? super V, ? super V, ? extends V> reducer) {
            super(p, b, i, f, t);
            this.nextRight = nextRight;
            this.reducer = reducer;
        }

        public final V getRawResult() {
            return this.result;
        }

        public final void compute() {
            BiFunction<? super V, ? super V, ? extends V> reducer = this.reducer;
            if (reducer != null) {
                int i = this.baseIndex;
                while (this.batch > 0) {
                    int f = this.baseLimit;
                    int h = (f + i) >>> 1;
                    if (h <= i) {
                        break;
                    }
                    addToPendingCount(1);
                    int i2 = this.batch >>> 1;
                    this.batch = i2;
                    this.baseLimit = h;
                    ReduceValuesTask reduceValuesTask = new ReduceValuesTask(this, i2, h, f, this.tab, this.rights, reducer);
                    this.rights = reduceValuesTask;
                    reduceValuesTask.fork();
                }
                Object r = null;
                while (true) {
                    Node<K, V> p = advance();
                    if (p == null) {
                        break;
                    }
                    V v = p.val;
                    r = r == null ? v : reducer.apply(r, v);
                }
                this.result = r;
                for (CountedCompleter<?> c = firstComplete(); c != null; c = c.nextComplete()) {
                    ReduceValuesTask<K, V> t = (ReduceValuesTask) c;
                    ReduceValuesTask<K, V> s = t.rights;
                    while (s != null) {
                        V sr = s.result;
                        if (sr != null) {
                            V tr = t.result;
                            if (tr != null) {
                                sr = reducer.apply(tr, sr);
                            }
                            t.result = sr;
                        }
                        s = s.nextRight;
                        t.rights = s;
                    }
                }
            }
        }
    }

    static final class ReservationNode<K, V> extends Node<K, V> {
        ReservationNode() {
            super(-3, null, null, null);
        }

        Node<K, V> find(int h, Object k) {
            return null;
        }
    }

    static final class SearchEntriesTask<K, V, U> extends BulkTask<K, V, U> {
        final AtomicReference<U> result;
        final Function<Entry<K, V>, ? extends U> searchFunction;

        SearchEntriesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<Entry<K, V>, ? extends U> searchFunction, AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction;
            this.result = result;
        }

        public final U getRawResult() {
            return this.result.get();
        }

        public final void compute() {
            Function<Entry<K, V>, ? extends U> searchFunction = this.searchFunction;
            if (searchFunction != null) {
                AtomicReference<U> result = this.result;
                if (result != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        } else if (result.get() == null) {
                            addToPendingCount(1);
                            int i2 = this.batch >>> 1;
                            this.batch = i2;
                            this.baseLimit = h;
                            new SearchEntriesTask(this, i2, h, f, this.tab, searchFunction, result).fork();
                        } else {
                            return;
                        }
                    }
                    while (result.get() == null) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            propagateCompletion();
                            break;
                        }
                        U u = searchFunction.apply(p);
                        if (u != null) {
                            if (result.compareAndSet(null, u)) {
                                quietlyCompleteRoot();
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    static final class SearchKeysTask<K, V, U> extends BulkTask<K, V, U> {
        final AtomicReference<U> result;
        final Function<? super K, ? extends U> searchFunction;

        SearchKeysTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super K, ? extends U> searchFunction, AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction;
            this.result = result;
        }

        public final U getRawResult() {
            return this.result.get();
        }

        public final void compute() {
            Function<? super K, ? extends U> searchFunction = this.searchFunction;
            if (searchFunction != null) {
                AtomicReference<U> result = this.result;
                if (result != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        } else if (result.get() == null) {
                            addToPendingCount(1);
                            int i2 = this.batch >>> 1;
                            this.batch = i2;
                            this.baseLimit = h;
                            new SearchKeysTask(this, i2, h, f, this.tab, searchFunction, result).fork();
                        } else {
                            return;
                        }
                    }
                    while (result.get() == null) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            propagateCompletion();
                            break;
                        }
                        U u = searchFunction.apply(p.key);
                        if (u != null) {
                            if (result.compareAndSet(null, u)) {
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

        SearchMappingsTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, BiFunction<? super K, ? super V, ? extends U> searchFunction, AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction;
            this.result = result;
        }

        public final U getRawResult() {
            return this.result.get();
        }

        public final void compute() {
            BiFunction<? super K, ? super V, ? extends U> searchFunction = this.searchFunction;
            if (searchFunction != null) {
                AtomicReference<U> result = this.result;
                if (result != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        } else if (result.get() == null) {
                            addToPendingCount(1);
                            int i2 = this.batch >>> 1;
                            this.batch = i2;
                            this.baseLimit = h;
                            new SearchMappingsTask(this, i2, h, f, this.tab, searchFunction, result).fork();
                        } else {
                            return;
                        }
                    }
                    while (result.get() == null) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            propagateCompletion();
                            break;
                        }
                        U u = searchFunction.apply(p.key, p.val);
                        if (u != null) {
                            if (result.compareAndSet(null, u)) {
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

        SearchValuesTask(BulkTask<K, V, ?> p, int b, int i, int f, Node<K, V>[] t, Function<? super V, ? extends U> searchFunction, AtomicReference<U> result) {
            super(p, b, i, f, t);
            this.searchFunction = searchFunction;
            this.result = result;
        }

        public final U getRawResult() {
            return this.result.get();
        }

        public final void compute() {
            Function<? super V, ? extends U> searchFunction = this.searchFunction;
            if (searchFunction != null) {
                AtomicReference<U> result = this.result;
                if (result != null) {
                    int i = this.baseIndex;
                    while (this.batch > 0) {
                        int f = this.baseLimit;
                        int h = (f + i) >>> 1;
                        if (h <= i) {
                            break;
                        } else if (result.get() == null) {
                            addToPendingCount(1);
                            int i2 = this.batch >>> 1;
                            this.batch = i2;
                            this.baseLimit = h;
                            new SearchValuesTask(this, i2, h, f, this.tab, searchFunction, result).fork();
                        } else {
                            return;
                        }
                    }
                    while (result.get() == null) {
                        Node<K, V> p = advance();
                        if (p == null) {
                            propagateCompletion();
                            break;
                        }
                        U u = searchFunction.apply(p.val);
                        if (u != null) {
                            if (result.compareAndSet(null, u)) {
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

    static final class TreeBin<K, V> extends Node<K, V> {
        static final /* synthetic */ boolean -assertionsDisabled = (TreeBin.class.desiredAssertionStatus() ^ 1);
        private static final long LOCKSTATE;
        static final int READER = 4;
        private static final Unsafe U = Unsafe.getUnsafe();
        static final int WAITER = 2;
        static final int WRITER = 1;
        volatile TreeNode<K, V> first;
        volatile int lockState;
        TreeNode<K, V> root;
        volatile Thread waiter;

        static int tieBreakOrder(Object a, Object b) {
            if (!(a == null || b == null)) {
                int d = a.getClass().getName().compareTo(b.getClass().getName());
                if (d != 0) {
                    return d;
                }
            }
            return System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1;
        }

        /* JADX WARNING: Missing block: B:22:0x0046, code:
            if (r3 == null) goto L_0x0048;
     */
        /* JADX WARNING: Missing block: B:23:0x0048, code:
            r0 = tieBreakOrder(r2, r7);
     */
        /* JADX WARNING: Missing block: B:25:0x0051, code:
            if (r0 == 0) goto L_0x0048;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        TreeBin(TreeNode<K, V> b) {
            super(-2, null, null, null);
            this.first = b;
            TreeNode<K, V> r = null;
            TreeNode<K, V> x = b;
            while (x != null) {
                TreeNode<K, V> next = x.next;
                x.right = null;
                x.left = null;
                if (r == null) {
                    x.parent = null;
                    x.red = -assertionsDisabled;
                    r = x;
                } else {
                    int dir;
                    TreeNode<K, V> xp;
                    K k = x.key;
                    int h = x.hash;
                    Class kc = null;
                    TreeNode<K, V> p = r;
                    do {
                        K pk = p.key;
                        int ph = p.hash;
                        if (ph > h) {
                            dir = -1;
                        } else if (ph < h) {
                            dir = 1;
                        } else {
                            if (kc == null) {
                                kc = ConcurrentHashMap.comparableClassFor(k);
                            }
                            dir = ConcurrentHashMap.compareComparables(kc, k, pk);
                        }
                        xp = p;
                        if (dir <= 0) {
                            p = p.left;
                            continue;
                        } else {
                            p = p.right;
                            continue;
                        }
                    } while (p != null);
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
            if (!-assertionsDisabled && !checkInvariants(this.root)) {
                throw new AssertionError();
            }
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
            boolean waiting = -assertionsDisabled;
            while (true) {
                int s = this.lockState;
                if ((s & -3) == 0) {
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

        final Node<K, V> find(int h, Object k) {
            if (k != null) {
                Node<K, V> e = this.first;
                while (e != null) {
                    int s = this.lockState;
                    if ((s & 3) != 0) {
                        if (e.hash == h) {
                            K ek = e.key;
                            if (ek == k || (ek != null && k.lambda$-java_util_function_Predicate_4628(ek))) {
                                return e;
                            }
                        }
                        e = e.next;
                    } else {
                        if (U.compareAndSwapInt(this, LOCKSTATE, s, s + 4)) {
                            Thread w;
                            try {
                                Node<K, V> p;
                                TreeNode<K, V> r = this.root;
                                if (r == null) {
                                    p = null;
                                } else {
                                    p = r.findTreeNode(h, k, null);
                                }
                                if (U.getAndAddInt(this, LOCKSTATE, -4) == 6) {
                                    w = this.waiter;
                                    if (w != null) {
                                        LockSupport.unpark(w);
                                    }
                                }
                                return p;
                            } catch (Throwable th) {
                                if (U.getAndAddInt(this, LOCKSTATE, -4) == 6) {
                                    w = this.waiter;
                                    if (w != null) {
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

        /* JADX WARNING: Missing block: B:35:0x007f, code:
            if (r10 == null) goto L_0x0081;
     */
        /* JADX WARNING: Missing block: B:36:0x0081, code:
            if (r15 != false) goto L_0x00aa;
     */
        /* JADX WARNING: Missing block: B:37:0x0083, code:
            r15 = true;
            r8 = r11.left;
     */
        /* JADX WARNING: Missing block: B:38:0x0086, code:
            if (r8 == null) goto L_0x009c;
     */
        /* JADX WARNING: Missing block: B:39:0x0088, code:
            r14 = r8.findTreeNode(r17, r18, r10);
     */
        /* JADX WARNING: Missing block: B:40:0x0090, code:
            if (r14 == null) goto L_0x009c;
     */
        /* JADX WARNING: Missing block: B:41:0x0092, code:
            return r14;
     */
        /* JADX WARNING: Missing block: B:43:0x0099, code:
            if (r9 == 0) goto L_0x0081;
     */
        /* JADX WARNING: Missing block: B:44:0x009c, code:
            r8 = r11.right;
     */
        /* JADX WARNING: Missing block: B:45:0x009e, code:
            if (r8 == null) goto L_0x00aa;
     */
        /* JADX WARNING: Missing block: B:46:0x00a0, code:
            r14 = r8.findTreeNode(r17, r18, r10);
     */
        /* JADX WARNING: Missing block: B:47:0x00a8, code:
            if (r14 != null) goto L_0x0092;
     */
        /* JADX WARNING: Missing block: B:48:0x00aa, code:
            r9 = tieBreakOrder(r18, r13);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        final TreeNode<K, V> putTreeVal(int h, K k, V v) {
            Class kc = null;
            boolean searched = -assertionsDisabled;
            TreeNode<K, V> p = this.root;
            while (p != null) {
                int dir;
                int ph = p.hash;
                if (ph > h) {
                    dir = -1;
                } else if (ph < h) {
                    dir = 1;
                } else {
                    K pk = p.key;
                    if (pk == k || (pk != null && k.lambda$-java_util_function_Predicate_4628(pk))) {
                        return p;
                    }
                    if (kc == null) {
                        kc = ConcurrentHashMap.comparableClassFor(k);
                    }
                    dir = ConcurrentHashMap.compareComparables(kc, k, pk);
                }
                TreeNode<K, V> xp = p;
                if (dir <= 0) {
                    p = p.left;
                    continue;
                } else {
                    p = p.right;
                    continue;
                }
                if (p == null) {
                    TreeNode<K, V> f = this.first;
                    TreeNode<K, V> x = new TreeNode(h, k, v, f, xp);
                    this.first = x;
                    if (f != null) {
                        f.prev = x;
                    }
                    if (dir <= 0) {
                        xp.left = x;
                    } else {
                        xp.right = x;
                    }
                    if (xp.red) {
                        lockRoot();
                        try {
                            this.root = balanceInsertion(this.root, x);
                        } finally {
                            unlockRoot();
                        }
                    } else {
                        x.red = true;
                    }
                    if (!-assertionsDisabled || checkInvariants(this.root)) {
                        return null;
                    }
                    throw new AssertionError();
                }
            }
            TreeNode treeNode = new TreeNode(h, k, v, null, null);
            this.root = treeNode;
            this.first = treeNode;
            if (!-assertionsDisabled) {
            }
            return null;
        }

        final boolean removeTreeNode(TreeNode<K, V> p) {
            TreeNode<K, V> next = p.next;
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
            TreeNode<K, V> r = this.root;
            if (!(r == null || r.right == null)) {
                TreeNode<K, V> rl = r.left;
                if (!(rl == null || rl.left == null)) {
                    lockRoot();
                    try {
                        TreeNode<K, V> pp;
                        TreeNode<K, V> replacement;
                        TreeNode<K, V> pl = p.left;
                        TreeNode<K, V> pr = p.right;
                        if (pl != null && pr != null) {
                            TreeNode<K, V> s = pr;
                            while (true) {
                                TreeNode<K, V> sl = s.left;
                                if (sl == null) {
                                    break;
                                }
                                s = sl;
                            }
                            boolean c = s.red;
                            s.red = p.red;
                            p.red = c;
                            TreeNode<K, V> sr = s.right;
                            pp = p.parent;
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
                                replacement = sr;
                            } else {
                                replacement = p;
                            }
                        } else if (pl != null) {
                            replacement = pl;
                        } else if (pr != null) {
                            replacement = pr;
                        } else {
                            replacement = p;
                        }
                        if (replacement != p) {
                            pp = p.parent;
                            replacement.parent = pp;
                            if (pp == null) {
                                r = replacement;
                            } else if (p == pp.left) {
                                pp.left = replacement;
                            } else {
                                pp.right = replacement;
                            }
                            p.parent = null;
                            p.right = null;
                            p.left = null;
                        }
                        this.root = p.red ? r : balanceDeletion(r, replacement);
                        if (p == replacement) {
                            pp = p.parent;
                            if (pp != null) {
                                if (p == pp.left) {
                                    pp.left = null;
                                } else if (p == pp.right) {
                                    pp.right = null;
                                }
                                p.parent = null;
                            }
                        }
                        unlockRoot();
                        if (-assertionsDisabled || checkInvariants(this.root)) {
                            return -assertionsDisabled;
                        }
                        throw new AssertionError();
                    } catch (Throwable th) {
                        unlockRoot();
                    }
                }
            }
            return true;
        }

        static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root, TreeNode<K, V> p) {
            if (p != null) {
                TreeNode<K, V> r = p.right;
                if (r != null) {
                    TreeNode<K, V> rl = r.left;
                    p.right = rl;
                    if (rl != null) {
                        rl.parent = p;
                    }
                    TreeNode<K, V> pp = p.parent;
                    r.parent = pp;
                    if (pp == null) {
                        root = r;
                        r.red = -assertionsDisabled;
                    } else if (pp.left == p) {
                        pp.left = r;
                    } else {
                        pp.right = r;
                    }
                    r.left = p;
                    p.parent = r;
                }
            }
            return root;
        }

        static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root, TreeNode<K, V> p) {
            if (p != null) {
                TreeNode<K, V> l = p.left;
                if (l != null) {
                    TreeNode<K, V> lr = l.right;
                    p.left = lr;
                    if (lr != null) {
                        lr.parent = p;
                    }
                    TreeNode<K, V> pp = p.parent;
                    l.parent = pp;
                    if (pp == null) {
                        root = l;
                        l.red = -assertionsDisabled;
                    } else if (pp.right == p) {
                        pp.right = l;
                    } else {
                        pp.left = l;
                    }
                    l.right = p;
                    p.parent = l;
                }
            }
            return root;
        }

        static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root, TreeNode<K, V> x) {
            x.red = true;
            while (true) {
                TreeNode<K, V> xp = x.parent;
                if (xp != null) {
                    if (!xp.red) {
                        break;
                    }
                    TreeNode xpp = xp.parent;
                    if (xpp == null) {
                        break;
                    }
                    TreeNode<K, V> xppl = xpp.left;
                    if (xp == xppl) {
                        TreeNode<K, V> xppr = xpp.right;
                        if (xppr == null || !xppr.red) {
                            if (x == xp.right) {
                                x = xp;
                                root = rotateLeft(root, xp);
                                xp = xp.parent;
                                xpp = xp == null ? null : xp.parent;
                            }
                            if (xp != null) {
                                xp.red = -assertionsDisabled;
                                if (xpp != null) {
                                    xpp.red = true;
                                    root = rotateRight(root, xpp);
                                }
                            }
                        } else {
                            xppr.red = -assertionsDisabled;
                            xp.red = -assertionsDisabled;
                            xpp.red = true;
                            x = xpp;
                        }
                    } else if (xppl == null || !xppl.red) {
                        if (x == xp.left) {
                            x = xp;
                            root = rotateRight(root, xp);
                            xp = xp.parent;
                            xpp = xp == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = -assertionsDisabled;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    } else {
                        xppl.red = -assertionsDisabled;
                        xp.red = -assertionsDisabled;
                        xpp.red = true;
                        x = xpp;
                    }
                } else {
                    x.red = -assertionsDisabled;
                    return x;
                }
            }
            return root;
        }

        static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root, TreeNode<K, V> x) {
            while (x != null && x != root) {
                TreeNode<K, V> xp = x.parent;
                if (xp == null) {
                    x.red = -assertionsDisabled;
                    return x;
                } else if (x.red) {
                    x.red = -assertionsDisabled;
                    return root;
                } else {
                    TreeNode xpl = xp.left;
                    TreeNode<K, V> sl;
                    TreeNode<K, V> sr;
                    if (xpl == x) {
                        TreeNode xpr = xp.right;
                        if (xpr != null && xpr.red) {
                            xpr.red = -assertionsDisabled;
                            xp.red = true;
                            root = rotateLeft(root, xp);
                            xp = x.parent;
                            xpr = xp == null ? null : xp.right;
                        }
                        if (xpr == null) {
                            x = xp;
                        } else {
                            sl = xpr.left;
                            sr = xpr.right;
                            if ((sr == null || (sr.red ^ 1) != 0) && (sl == null || (sl.red ^ 1) != 0)) {
                                xpr.red = true;
                                x = xp;
                            } else {
                                if (sr == null || (sr.red ^ 1) != 0) {
                                    if (sl != null) {
                                        sl.red = -assertionsDisabled;
                                    }
                                    xpr.red = true;
                                    root = rotateRight(root, xpr);
                                    xp = x.parent;
                                    xpr = xp == null ? null : xp.right;
                                }
                                if (xpr != null) {
                                    xpr.red = xp == null ? -assertionsDisabled : xp.red;
                                    sr = xpr.right;
                                    if (sr != null) {
                                        sr.red = -assertionsDisabled;
                                    }
                                }
                                if (xp != null) {
                                    xp.red = -assertionsDisabled;
                                    root = rotateLeft(root, xp);
                                }
                                x = root;
                            }
                        }
                    } else {
                        if (xpl != null && xpl.red) {
                            xpl.red = -assertionsDisabled;
                            xp.red = true;
                            root = rotateRight(root, xp);
                            xp = x.parent;
                            xpl = xp == null ? null : xp.left;
                        }
                        if (xpl == null) {
                            x = xp;
                        } else {
                            sl = xpl.left;
                            sr = xpl.right;
                            if ((sl == null || (sl.red ^ 1) != 0) && (sr == null || (sr.red ^ 1) != 0)) {
                                xpl.red = true;
                                x = xp;
                            } else {
                                if (sl == null || (sl.red ^ 1) != 0) {
                                    if (sr != null) {
                                        sr.red = -assertionsDisabled;
                                    }
                                    xpl.red = true;
                                    root = rotateLeft(root, xpl);
                                    xp = x.parent;
                                    xpl = xp == null ? null : xp.left;
                                }
                                if (xpl != null) {
                                    xpl.red = xp == null ? -assertionsDisabled : xp.red;
                                    sl = xpl.left;
                                    if (sl != null) {
                                        sl.red = -assertionsDisabled;
                                    }
                                }
                                if (xp != null) {
                                    xp.red = -assertionsDisabled;
                                    root = rotateRight(root, xp);
                                }
                                x = root;
                            }
                        }
                    }
                }
            }
            return root;
        }

        static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
            TreeNode<K, V> tp = t.parent;
            TreeNode<K, V> tl = t.left;
            TreeNode<K, V> tr = t.right;
            TreeNode<K, V> tb = t.prev;
            TreeNode<K, V> tn = t.next;
            if (tb != null && tb.next != t) {
                return -assertionsDisabled;
            }
            if (tn != null && tn.prev != t) {
                return -assertionsDisabled;
            }
            if (tp != null && t != tp.left && t != tp.right) {
                return -assertionsDisabled;
            }
            if (tl != null && (tl.parent != t || tl.hash > t.hash)) {
                return -assertionsDisabled;
            }
            if (tr != null && (tr.parent != t || tr.hash < t.hash)) {
                return -assertionsDisabled;
            }
            if (t.red && tl != null && tl.red && tr != null && tr.red) {
                return -assertionsDisabled;
            }
            if (tl != null && (checkInvariants(tl) ^ 1) != 0) {
                return -assertionsDisabled;
            }
            if (tr == null || (checkInvariants(tr) ^ 1) == 0) {
                return true;
            }
            return -assertionsDisabled;
        }

        static {
            try {
                LOCKSTATE = U.objectFieldOffset(TreeBin.class.getDeclaredField("lockState"));
            } catch (Throwable e) {
                throw new Error(e);
            }
        }
    }

    static final class TreeNode<K, V> extends Node<K, V> {
        TreeNode<K, V> left;
        TreeNode<K, V> parent;
        TreeNode<K, V> prev;
        boolean red;
        TreeNode<K, V> right;

        TreeNode(int hash, K key, V val, Node<K, V> next, TreeNode<K, V> parent) {
            super(hash, key, val, next);
            this.parent = parent;
        }

        Node<K, V> find(int h, Object k) {
            return findTreeNode(h, k, null);
        }

        /* JADX WARNING: Missing block: B:22:0x002f, code:
            if (r12 != null) goto L_0x0031;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        final TreeNode<K, V> findTreeNode(int h, Object k, Class<?> kc) {
            if (k != null) {
                TreeNode<K, V> p = this;
                do {
                    TreeNode<K, V> pl = p.left;
                    TreeNode<K, V> pr = p.right;
                    int ph = p.hash;
                    if (ph > h) {
                        p = pl;
                        continue;
                    } else if (ph < h) {
                        p = pr;
                        continue;
                    } else {
                        K pk = p.key;
                        if (pk == k || (pk != null && k.lambda$-java_util_function_Predicate_4628(pk))) {
                            return p;
                        }
                        if (pl == null) {
                            p = pr;
                            continue;
                        } else if (pr == null) {
                            p = pl;
                            continue;
                        } else {
                            if (kc == null) {
                                kc = ConcurrentHashMap.comparableClassFor(k);
                            }
                            int dir = ConcurrentHashMap.compareComparables(kc, k, pk);
                            if (dir != 0) {
                                if (dir < 0) {
                                    p = pl;
                                    continue;
                                } else {
                                    p = pr;
                                    continue;
                                }
                            }
                            TreeNode<K, V> q = pr.findTreeNode(h, k, kc);
                            if (q != null) {
                                return q;
                            }
                            p = pl;
                            continue;
                        }
                    }
                } while (p != null);
            }
            return null;
        }
    }

    static final class ValueIterator<K, V> extends BaseIterator<K, V> implements Iterator<V>, Enumeration<V> {
        ValueIterator(Node<K, V>[] tab, int index, int size, int limit, ConcurrentHashMap<K, V> map) {
            super(tab, index, size, limit, map);
        }

        public final V next() {
            Node<K, V> p = this.next;
            if (p == null) {
                throw new NoSuchElementException();
            }
            V v = p.val;
            this.lastReturned = p;
            advance();
            return v;
        }

        public final V nextElement() {
            return next();
        }
    }

    static final class ValueSpliterator<K, V> extends Traverser<K, V> implements Spliterator<V> {
        long est;

        ValueSpliterator(Node<K, V>[] tab, int size, int index, int limit, long est) {
            super(tab, size, index, limit);
            this.est = est;
        }

        public ValueSpliterator<K, V> trySplit() {
            int i = this.baseIndex;
            int f = this.baseLimit;
            int h = (i + f) >>> 1;
            if (h <= i) {
                return null;
            }
            Node[] nodeArr = this.tab;
            int i2 = this.baseSize;
            this.baseLimit = h;
            long j = this.est >>> 1;
            this.est = j;
            return new ValueSpliterator(nodeArr, i2, h, f, j);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            while (true) {
                Node<K, V> p = advance();
                if (p != null) {
                    action.accept(p.val);
                } else {
                    return;
                }
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K, V> p = advance();
            if (p == null) {
                return false;
            }
            action.accept(p.val);
            return true;
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
                    if (o.lambda$-java_util_function_Predicate_4628(it.next())) {
                        it.remove();
                        return true;
                    }
                }
            }
            return false;
        }

        public final Iterator<V> iterator() {
            ConcurrentHashMap<K, V> m = this.map;
            Node<K, V>[] t = m.table;
            int f = t == null ? 0 : t.length;
            return new ValueIterator(t, f, 0, f, m);
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
            long j = 0;
            ConcurrentHashMap<K, V> m = this.map;
            long n = m.sumCount();
            Node<K, V>[] t = m.table;
            int f = t == null ? 0 : t.length;
            if (n >= 0) {
                j = n;
            }
            return new ValueSpliterator(t, f, 0, f, j);
        }

        public void forEach(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K, V>[] t = this.map.table;
            if (t != null) {
                Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
                while (true) {
                    Node<K, V> p = it.advance();
                    if (p != null) {
                        action.accept(p.val);
                    } else {
                        return;
                    }
                }
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
            if (((scale - 1) & scale) != 0) {
                throw new Error("array index scale not a power of two");
            }
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
            Class<?> ensureLoaded = LockSupport.class;
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    static final int spread(int h) {
        return ((h >>> 16) ^ h) & Integer.MAX_VALUE;
    }

    private static final int tableSizeFor(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        if (n < 0) {
            return 1;
        }
        if (n < MAXIMUM_CAPACITY) {
            return n + 1;
        }
        return MAXIMUM_CAPACITY;
    }

    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Type c = x.getClass();
            if (c == String.class) {
                return c;
            }
            Type[] ts = c.getGenericInterfaces();
            if (ts != null) {
                for (Type t : ts) {
                    if (t instanceof ParameterizedType) {
                        ParameterizedType p = (ParameterizedType) t;
                        if (p.getRawType() == Comparable.class) {
                            Type[] as = p.getActualTypeArguments();
                            if (as != null && as.length == 1 && as[0] == c) {
                                return c;
                            }
                        }
                        continue;
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
        return U.compareAndSwapObject(tab, (((long) i) << ASHIFT) + ((long) ABASE), c, v);
    }

    static final <K, V> void setTabAt(Node<K, V>[] tab, int i, Node<K, V> v) {
        U.putObjectVolatile(tab, (((long) i) << ASHIFT) + ((long) ABASE), v);
    }

    public ConcurrentHashMap(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException();
        }
        int cap;
        if (initialCapacity >= 536870912) {
            cap = MAXIMUM_CAPACITY;
        } else {
            cap = tableSizeFor(((initialCapacity >>> 1) + initialCapacity) + 1);
        }
        this.sizeCtl = cap;
    }

    public ConcurrentHashMap(Map<? extends K, ? extends V> m) {
        this.sizeCtl = 16;
        putAll(m);
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, 1);
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        Object obj = null;
        if (loadFactor > 0.0f) {
            obj = 1;
        }
        if (obj == null || initialCapacity < 0 || concurrencyLevel <= 0) {
            throw new IllegalArgumentException();
        }
        if (initialCapacity < concurrencyLevel) {
            initialCapacity = concurrencyLevel;
        }
        long size = (long) (((double) (((float) ((long) initialCapacity)) / loadFactor)) + 1.0d);
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

    /* JADX WARNING: Missing block: B:30:0x004d, code:
            return r0.val;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public V get(Object key) {
        V v = null;
        int h = spread(key.hashCode());
        Node<K, V>[] tab = this.table;
        if (tab != null) {
            int n = tab.length;
            if (n > 0) {
                Node<K, V> e = tabAt(tab, (n - 1) & h);
                if (e != null) {
                    K ek;
                    int eh = e.hash;
                    if (eh == h) {
                        ek = e.key;
                        if (ek == key || (ek != null && key.lambda$-java_util_function_Predicate_4628(ek))) {
                            return e.val;
                        }
                    } else if (eh < 0) {
                        Node<K, V> p = e.find(h, key);
                        if (p != null) {
                            v = p.val;
                        }
                        return v;
                    }
                    while (true) {
                        e = e.next;
                        if (e == null) {
                            break;
                        } else if (e.hash == h) {
                            ek = e.key;
                            if (ek == key || (ek != null && key.lambda$-java_util_function_Predicate_4628(ek))) {
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        Node<K, V>[] t = this.table;
        if (t != null) {
            Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
            while (true) {
                Node<K, V> p = it.advance();
                if (p == null) {
                    break;
                }
                V v = p.val;
                if (v == value || (v != null && value.lambda$-java_util_function_Predicate_4628(v))) {
                }
            }
            return true;
        }
        return false;
    }

    public V put(K key, V value) {
        return putVal(key, value, false);
    }

    final V putVal(K key, V value, boolean onlyIfAbsent) {
        if (key == null || value == null) {
            throw new NullPointerException();
        }
        int hash = spread(key.hashCode());
        int binCount = 0;
        Node<K, V>[] tab = this.table;
        while (true) {
            if (tab != null) {
                int n = tab.length;
                if (n != 0) {
                    int i = (n - 1) & hash;
                    Node<K, V> f = tabAt(tab, i);
                    if (f != null) {
                        int fh = f.hash;
                        if (fh == -1) {
                            tab = helpTransfer(tab, f);
                        } else {
                            V oldVal = null;
                            synchronized (f) {
                                if (tabAt(tab, i) == f) {
                                    if (fh >= 0) {
                                        binCount = 1;
                                        Node<K, V> e = f;
                                        while (true) {
                                            if (e.hash == hash) {
                                                K ek = e.key;
                                                if (ek == key || (ek != null && key.lambda$-java_util_function_Predicate_4628(ek))) {
                                                    oldVal = e.val;
                                                }
                                            }
                                            Node<K, V> pred = e;
                                            e = e.next;
                                            if (e == null) {
                                                pred.next = new Node(hash, key, value, null);
                                                break;
                                            }
                                            binCount++;
                                        }
                                        oldVal = e.val;
                                        if (!onlyIfAbsent) {
                                            e.val = value;
                                        }
                                    } else if (f instanceof TreeBin) {
                                        binCount = 2;
                                        Node<K, V> p = ((TreeBin) f).putTreeVal(hash, key, value);
                                        if (p != null) {
                                            oldVal = p.val;
                                            if (!onlyIfAbsent) {
                                                p.val = value;
                                            }
                                        }
                                    } else if (f instanceof ReservationNode) {
                                        throw new IllegalStateException("Recursive update");
                                    }
                                }
                            }
                            if (binCount != 0) {
                                if (binCount >= 8) {
                                    treeifyBin(tab, i);
                                }
                                if (oldVal != null) {
                                    return oldVal;
                                }
                            }
                        }
                    } else if (casTabAt(tab, i, null, new Node(hash, key, value, null))) {
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
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            putVal(e.getKey(), e.getValue(), false);
        }
    }

    public V remove(Object key) {
        return replaceNode(key, null, null);
    }

    /* JADX WARNING: Missing block: B:41:0x008d, code:
            if (r26.lambda$-java_util_function_Predicate_4628(r6) != false) goto L_0x0069;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final V replaceNode(Object key, V value, Object cv) {
        int hash = spread(key.hashCode());
        Node<K, V>[] tab = this.table;
        while (tab != null) {
            int n = tab.length;
            if (n != 0) {
                int i = (n - 1) & hash;
                Node<K, V> f = tabAt(tab, i);
                if (f == null) {
                    break;
                }
                int fh = f.hash;
                if (fh == -1) {
                    tab = helpTransfer(tab, f);
                } else {
                    V oldVal = null;
                    boolean validated = false;
                    synchronized (f) {
                        if (tabAt(tab, i) == f) {
                            if (fh >= 0) {
                                validated = true;
                                Node<K, V> e = f;
                                Node pred = null;
                                do {
                                    if (e.hash == hash) {
                                        K ek = e.key;
                                        if (ek == key || (ek != null && key.lambda$-java_util_function_Predicate_4628(ek))) {
                                            V ev = e.val;
                                            if (!(cv == null || cv == ev)) {
                                                if (ev != null) {
                                                }
                                            }
                                            oldVal = ev;
                                            if (value != null) {
                                                e.val = value;
                                            } else if (pred != null) {
                                                pred.next = e.next;
                                            } else {
                                                setTabAt(tab, i, e.next);
                                            }
                                        }
                                    }
                                    Node<K, V> pred2 = e;
                                    e = e.next;
                                } while (e != null);
                            } else if (f instanceof TreeBin) {
                                validated = true;
                                TreeBin<K, V> t = (TreeBin) f;
                                TreeNode<K, V> r = t.root;
                                if (r != null) {
                                    TreeNode<K, V> p = r.findTreeNode(hash, key, null);
                                    if (p != null) {
                                        V pv = p.val;
                                        if (cv == null || cv == pv || (pv != null && cv.lambda$-java_util_function_Predicate_4628(pv))) {
                                            oldVal = pv;
                                            if (value != null) {
                                                p.val = value;
                                            } else if (t.removeTreeNode(p)) {
                                                setTabAt(tab, i, untreeify(t.first));
                                            }
                                        }
                                    }
                                }
                            } else if (f instanceof ReservationNode) {
                                throw new IllegalStateException("Recursive update");
                            }
                        }
                    }
                    if (validated) {
                        if (oldVal != null) {
                            if (value == null) {
                                addCount(-1, -1);
                            }
                            return oldVal;
                        }
                    }
                }
            } else {
                break;
            }
        }
        return null;
    }

    public void clear() {
        Throwable th;
        long delta = 0;
        Node<K, V>[] tab = this.table;
        int i = 0;
        while (tab != null && i < tab.length) {
            int i2;
            Node<K, V> f = tabAt(tab, i);
            if (f == null) {
                i2 = i + 1;
            } else {
                int fh = f.hash;
                if (fh == -1) {
                    tab = helpTransfer(tab, f);
                    i2 = 0;
                } else {
                    synchronized (f) {
                        try {
                            if (tabAt(tab, i) == f) {
                                Node<K, V> p = fh >= 0 ? f : f instanceof TreeBin ? ((TreeBin) f).first : null;
                                while (p != null) {
                                    delta--;
                                    p = p.next;
                                }
                                i2 = i + 1;
                                try {
                                    setTabAt(tab, i, null);
                                } catch (Throwable th2) {
                                    th = th2;
                                }
                            } else {
                                i2 = i;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            i2 = i;
                            throw th;
                        }
                    }
                }
            }
            i = i2;
        }
        if (delta != 0) {
            addCount(delta, -1);
        }
    }

    public Set<K> keySet() {
        KeySetView<K, V> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        ks = new KeySetView(this, null);
        this.keySet = ks;
        return ks;
    }

    public Collection<V> values() {
        ValuesView<K, V> vs = this.values;
        if (vs != null) {
            return vs;
        }
        vs = new ValuesView(this);
        this.values = vs;
        return vs;
    }

    public Set<Entry<K, V>> entrySet() {
        EntrySetView<K, V> es = this.entrySet;
        if (es != null) {
            return es;
        }
        es = new EntrySetView(this);
        this.entrySet = es;
        return es;
    }

    public int hashCode() {
        int h = 0;
        Node<K, V>[] t = this.table;
        if (t != null) {
            Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
            while (true) {
                Node<K, V> p = it.advance();
                if (p == null) {
                    break;
                }
                h += p.key.hashCode() ^ p.val.hashCode();
            }
        }
        return h;
    }

    public String toString() {
        Node<K, V>[] t = this.table;
        int f = t == null ? 0 : t.length;
        Traverser<K, V> it = new Traverser(t, f, 0, f);
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        Node<K, V> p = it.advance();
        if (p != null) {
            while (true) {
                Object k = p.key;
                Object v = p.val;
                if (k == this) {
                    k = "(this Map)";
                }
                sb.append(k);
                sb.append('=');
                if (v == this) {
                    v = "(this Map)";
                }
                sb.append(v);
                p = it.advance();
                if (p == null) {
                    break;
                }
                sb.append(',').append(' ');
            }
        }
        return sb.append('}').toString();
    }

    public boolean equals(Object o) {
        if (o != this) {
            if (!(o instanceof Map)) {
                return false;
            }
            Map<?, ?> m = (Map) o;
            Node<K, V>[] t = this.table;
            int f = t == null ? 0 : t.length;
            Traverser<K, V> it = new Traverser(t, f, 0, f);
            while (true) {
                Node<K, V> p = it.advance();
                if (p != null) {
                    V val = p.val;
                    V v = m.get(p.key);
                    if (v == null || !(v == val || (v.lambda$-java_util_function_Predicate_4628(val) ^ 1) == 0)) {
                        return false;
                    }
                } else {
                    for (Entry<?, ?> e : m.entrySet()) {
                        Object mk = e.getKey();
                        if (mk != null) {
                            Object mv = e.getValue();
                            if (mv != null) {
                                Object v2 = get(mk);
                                if (v2 != null && (mv == v2 || (mv.lambda$-java_util_function_Predicate_4628(v2) ^ 1) == 0)) {
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
        Object segments = new Segment[16];
        for (int i = 0; i < segments.length; i++) {
            segments[i] = new Segment(LOAD_FACTOR);
        }
        PutField streamFields = s.putFields();
        streamFields.put("segments", segments);
        streamFields.put("segmentShift", segmentShift);
        streamFields.put("segmentMask", segmentMask);
        s.writeFields();
        Node<K, V>[] t = this.table;
        if (t != null) {
            Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
            while (true) {
                Node<K, V> p = it.advance();
                if (p == null) {
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
        K k;
        this.sizeCtl = -1;
        s.defaultReadObject();
        long size = 0;
        Node<K, V> p = null;
        while (true) {
            k = s.readObject();
            V v = s.readObject();
            if (k != null && v != null) {
                size++;
                p = new Node(spread(k.hashCode()), k, v, p);
            }
        }
        if (size == 0) {
            this.sizeCtl = 0;
            return;
        }
        int n;
        if (size >= 536870912) {
            n = MAXIMUM_CAPACITY;
        } else {
            int sz = (int) size;
            n = tableSizeFor(((sz >>> 1) + sz) + 1);
        }
        Node<K, V>[] tab = new Node[n];
        int mask = n - 1;
        long added = 0;
        while (p != null) {
            boolean insertAtFront;
            Node<K, V> next = p.next;
            int h = p.hash;
            int j = h & mask;
            Node<K, V> first = tabAt(tab, j);
            if (first == null) {
                insertAtFront = true;
            } else {
                k = p.key;
                if (first.hash < 0) {
                    if (((TreeBin) first).putTreeVal(h, k, p.val) == null) {
                        added++;
                    }
                    insertAtFront = false;
                } else {
                    Node<K, V> q;
                    int binCount = 0;
                    insertAtFront = true;
                    for (q = first; q != null; q = q.next) {
                        if (q.hash == h) {
                            K qk = q.key;
                            if (qk == k || (qk != null && k.lambda$-java_util_function_Predicate_4628(qk))) {
                                insertAtFront = false;
                                break;
                            }
                        }
                        binCount++;
                    }
                    if (insertAtFront && binCount >= 8) {
                        insertAtFront = false;
                        added++;
                        p.next = first;
                        TreeNode hd = null;
                        TreeNode<K, V> tl = null;
                        for (q = p; q != null; q = q.next) {
                            TreeNode<K, V> t = new TreeNode(q.hash, q.key, q.val, null, null);
                            t.prev = tl;
                            if (tl == null) {
                                hd = t;
                            } else {
                                tl.next = t;
                            }
                            tl = t;
                        }
                        setTabAt(tab, j, new TreeBin(hd));
                    }
                }
            }
            if (insertAtFront) {
                added++;
                p.next = first;
                setTabAt(tab, j, p);
            }
            p = next;
        }
        this.table = tab;
        this.sizeCtl = n - (n >>> 2);
        this.baseCount = added;
    }

    public V putIfAbsent(K key, V value) {
        return putVal(key, value, true);
    }

    public boolean remove(Object key, Object value) {
        if (key == null) {
            throw new NullPointerException();
        } else if (value == null || replaceNode(key, null, value) == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean replace(K key, V oldValue, V newValue) {
        if (key != null && oldValue != null && newValue != null) {
            return replaceNode(key, newValue, oldValue) != null;
        } else {
            throw new NullPointerException();
        }
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
        if (action == null) {
            throw new NullPointerException();
        }
        Node<K, V>[] t = this.table;
        if (t != null) {
            Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
            while (true) {
                Node<K, V> p = it.advance();
                if (p != null) {
                    action.accept(p.key, p.val);
                } else {
                    return;
                }
            }
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        Node<K, V>[] t = this.table;
        if (t != null) {
            Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
            while (true) {
                Node<K, V> p = it.advance();
                if (p != null) {
                    V oldValue = p.val;
                    K key = p.key;
                    do {
                        V newValue = function.apply(key, oldValue);
                        if (newValue != null) {
                            if (replaceNode(key, newValue, oldValue) != null) {
                                break;
                            }
                            oldValue = get(key);
                        } else {
                            throw new NullPointerException();
                        }
                    } while (oldValue != null);
                }
                return;
            }
        }
    }

    boolean removeEntryIf(Predicate<? super Entry<K, V>> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        boolean removed = false;
        Node<K, V>[] t = this.table;
        if (t != null) {
            Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
            while (true) {
                Node<K, V> p = it.advance();
                if (p == null) {
                    break;
                }
                K k = p.key;
                V v = p.val;
                if (function.test(new SimpleImmutableEntry(k, v)) && replaceNode(k, null, v) != null) {
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
        Node<K, V>[] t = this.table;
        if (t != null) {
            Traverser<K, V> it = new Traverser(t, t.length, 0, t.length);
            while (true) {
                Node<K, V> p = it.advance();
                if (p == null) {
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

    /* JADX WARNING: Missing block: B:66:0x00d5, code:
            r21 = r26.apply(r25);
     */
    /* JADX WARNING: Missing block: B:67:0x00dd, code:
            if (r21 == null) goto L_0x00bc;
     */
    /* JADX WARNING: Missing block: B:69:0x00e5, code:
            if (r16.next == null) goto L_0x00f3;
     */
    /* JADX WARNING: Missing block: B:71:0x00ef, code:
            throw new java.lang.IllegalStateException("Recursive update");
     */
    /* JADX WARNING: Missing block: B:75:0x00f3, code:
            r4 = true;
     */
    /* JADX WARNING: Missing block: B:77:?, code:
            r16.next = new java.util.concurrent.ConcurrentHashMap.Node(r10, r25, r21, null);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        if (key == null || mappingFunction == null) {
            throw new NullPointerException();
        }
        int h = spread(key.hashCode());
        V val = null;
        int binCount = 0;
        Node<K, V>[] tab = this.table;
        while (true) {
            if (tab != null) {
                int n = tab.length;
                if (n != 0) {
                    int i = (n - 1) & h;
                    Node<K, V> f = tabAt(tab, i);
                    if (f == null) {
                        Node<K, V> r = new ReservationNode();
                        synchronized (r) {
                            if (casTabAt(tab, i, null, r)) {
                                binCount = 1;
                                Node node = null;
                                try {
                                    val = mappingFunction.apply(key);
                                    if (val != null) {
                                        node = new Node(h, key, val, null);
                                    }
                                    setTabAt(tab, i, node);
                                } catch (Throwable th) {
                                    setTabAt(tab, i, null);
                                }
                            }
                        }
                        if (binCount != 0) {
                            break;
                        }
                    } else {
                        int fh = f.hash;
                        if (fh == -1) {
                            tab = helpTransfer(tab, f);
                        } else {
                            boolean added = false;
                            synchronized (f) {
                                if (tabAt(tab, i) == f) {
                                    if (fh >= 0) {
                                        binCount = 1;
                                        Node<K, V> e = f;
                                        while (true) {
                                            if (e.hash == h) {
                                                K ek = e.key;
                                                if (ek == key || (ek != null && key.lambda$-java_util_function_Predicate_4628(ek))) {
                                                    val = e.val;
                                                }
                                            }
                                            Node<K, V> pred = e;
                                            e = e.next;
                                            if (e == null) {
                                                break;
                                            }
                                            binCount++;
                                        }
                                        val = e.val;
                                    } else if (f instanceof TreeBin) {
                                        binCount = 2;
                                        TreeBin<K, V> t = (TreeBin) f;
                                        TreeNode<K, V> r2 = t.root;
                                        if (r2 != null) {
                                            TreeNode<K, V> p = r2.findTreeNode(h, key, null);
                                            if (p != null) {
                                                val = p.val;
                                            }
                                        }
                                        val = mappingFunction.apply(key);
                                        if (val != null) {
                                            added = true;
                                            t.putTreeVal(h, key, val);
                                        }
                                    } else if (f instanceof ReservationNode) {
                                        throw new IllegalStateException("Recursive update");
                                    }
                                }
                            }
                            if (binCount != 0) {
                                if (binCount >= 8) {
                                    treeifyBin(tab, i);
                                }
                                if (!added) {
                                    return val;
                                }
                            }
                        }
                    }
                }
            }
            tab = initTable();
        }
        if (val != null) {
            addCount(1, binCount);
        }
        return val;
    }

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
                int n = tab.length;
                if (n != 0) {
                    int i = (n - 1) & h;
                    Node<K, V> f = tabAt(tab, i);
                    if (f == null) {
                        break;
                    }
                    int fh = f.hash;
                    if (fh == -1) {
                        tab = helpTransfer(tab, f);
                    } else {
                        synchronized (f) {
                            if (tabAt(tab, i) == f) {
                                if (fh >= 0) {
                                    binCount = 1;
                                    Node<K, V> e = f;
                                    Node pred = null;
                                    while (true) {
                                        if (e.hash == h) {
                                            K ek = e.key;
                                            if (ek == key || (ek != null && key.lambda$-java_util_function_Predicate_4628(ek))) {
                                                val = remappingFunction.apply(key, e.val);
                                            }
                                        }
                                        Node<K, V> pred2 = e;
                                        e = e.next;
                                        if (e == null) {
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
                                        if (pred2 != null) {
                                            pred2.next = en;
                                        } else {
                                            setTabAt(tab, i, en);
                                        }
                                    }
                                } else if (f instanceof TreeBin) {
                                    binCount = 2;
                                    TreeBin<K, V> t = (TreeBin) f;
                                    TreeNode<K, V> r = t.root;
                                    if (r != null) {
                                        TreeNode<K, V> p = r.findTreeNode(h, key, null);
                                        if (p != null) {
                                            val = remappingFunction.apply(key, p.val);
                                            if (val != null) {
                                                p.val = val;
                                            } else {
                                                delta = -1;
                                                if (t.removeTreeNode(p)) {
                                                    setTabAt(tab, i, untreeify(t.first));
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
        if (delta != 0) {
            addCount((long) delta, binCount);
        }
        return val;
    }

    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
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
                int n = tab.length;
                if (n != 0) {
                    int i = (n - 1) & h;
                    Node<K, V> f = tabAt(tab, i);
                    if (f == null) {
                        Node<K, V> r = new ReservationNode();
                        synchronized (r) {
                            if (casTabAt(tab, i, null, r)) {
                                binCount = 1;
                                Node node = null;
                                try {
                                    val = remappingFunction.apply(key, null);
                                    if (val != null) {
                                        delta = 1;
                                        node = new Node(h, key, val, null);
                                    }
                                    setTabAt(tab, i, node);
                                } catch (Throwable th) {
                                    setTabAt(tab, i, null);
                                }
                            }
                        }
                        if (binCount != 0) {
                            break;
                        }
                    } else {
                        int fh = f.hash;
                        if (fh == -1) {
                            tab = helpTransfer(tab, f);
                        } else {
                            synchronized (f) {
                                if (tabAt(tab, i) == f) {
                                    if (fh >= 0) {
                                        binCount = 1;
                                        Node<K, V> e = f;
                                        Node<K, V> pred = null;
                                        while (true) {
                                            if (e.hash == h) {
                                                K ek = e.key;
                                                if (ek == key || (ek != null && key.lambda$-java_util_function_Predicate_4628(ek))) {
                                                    val = remappingFunction.apply(key, e.val);
                                                }
                                            }
                                            pred = e;
                                            e = e.next;
                                            if (e == null) {
                                                val = remappingFunction.apply(key, null);
                                                if (val != null) {
                                                    if (pred.next != null) {
                                                        throw new IllegalStateException("Recursive update");
                                                    }
                                                    delta = 1;
                                                    pred.next = new Node(h, key, val, null);
                                                }
                                            } else {
                                                binCount++;
                                            }
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
                                                setTabAt(tab, i, en);
                                            }
                                        }
                                    } else if (f instanceof TreeBin) {
                                        TreeNode p;
                                        binCount = 1;
                                        TreeBin<K, V> t = (TreeBin) f;
                                        TreeNode<K, V> r2 = t.root;
                                        if (r2 != null) {
                                            p = r2.findTreeNode(h, key, null);
                                        } else {
                                            p = null;
                                        }
                                        val = remappingFunction.apply(key, p == null ? null : p.val);
                                        if (val != null) {
                                            if (p != null) {
                                                p.val = val;
                                            } else {
                                                delta = 1;
                                                t.putTreeVal(h, key, val);
                                            }
                                        } else if (p != null) {
                                            delta = -1;
                                            if (t.removeTreeNode(p)) {
                                                setTabAt(tab, i, untreeify(t.first));
                                            }
                                        }
                                    } else if (f instanceof ReservationNode) {
                                        throw new IllegalStateException("Recursive update");
                                    }
                                }
                            }
                            if (binCount != 0) {
                                if (binCount >= 8) {
                                    treeifyBin(tab, i);
                                }
                            }
                        }
                    }
                }
            }
            tab = initTable();
        }
        if (delta != 0) {
            addCount((long) delta, binCount);
        }
        return val;
    }

    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (key == null || value == null || remappingFunction == null) {
            throw new NullPointerException();
        }
        int h = spread(key.hashCode());
        V val = null;
        int delta = 0;
        int binCount = 0;
        Node<K, V>[] tab = this.table;
        while (true) {
            if (tab != null) {
                int n = tab.length;
                if (n != 0) {
                    int i = (n - 1) & h;
                    Node<K, V> f = tabAt(tab, i);
                    if (f == null) {
                        if (casTabAt(tab, i, null, new Node(h, key, value, null))) {
                            delta = 1;
                            val = value;
                            break;
                        }
                    } else {
                        int fh = f.hash;
                        if (fh == -1) {
                            tab = helpTransfer(tab, f);
                        } else {
                            synchronized (f) {
                                if (tabAt(tab, i) == f) {
                                    if (fh >= 0) {
                                        binCount = 1;
                                        Node<K, V> e = f;
                                        Node pred = null;
                                        while (true) {
                                            if (e.hash == h) {
                                                K ek = e.key;
                                                if (ek == key || (ek != null && key.lambda$-java_util_function_Predicate_4628(ek))) {
                                                    val = remappingFunction.apply(e.val, value);
                                                }
                                            }
                                            Node<K, V> pred2 = e;
                                            e = e.next;
                                            if (e == null) {
                                                delta = 1;
                                                val = value;
                                                pred2.next = new Node(h, key, value, null);
                                                break;
                                            }
                                            binCount++;
                                        }
                                        val = remappingFunction.apply(e.val, value);
                                        if (val != null) {
                                            e.val = val;
                                        } else {
                                            delta = -1;
                                            Node<K, V> en = e.next;
                                            if (pred2 != null) {
                                                pred2.next = en;
                                            } else {
                                                setTabAt(tab, i, en);
                                            }
                                        }
                                    } else if (f instanceof TreeBin) {
                                        TreeNode p;
                                        binCount = 2;
                                        TreeBin<K, V> t = (TreeBin) f;
                                        TreeNode<K, V> r = t.root;
                                        if (r == null) {
                                            p = null;
                                        } else {
                                            p = r.findTreeNode(h, key, null);
                                        }
                                        if (p == null) {
                                            val = value;
                                        } else {
                                            val = remappingFunction.apply(p.val, value);
                                        }
                                        if (val != null) {
                                            if (p != null) {
                                                p.val = val;
                                            } else {
                                                delta = 1;
                                                t.putTreeVal(h, key, val);
                                            }
                                        } else if (p != null) {
                                            delta = -1;
                                            if (t.removeTreeNode(p)) {
                                                setTabAt(tab, i, untreeify(t.first));
                                            }
                                        }
                                    } else if (f instanceof ReservationNode) {
                                        throw new IllegalStateException("Recursive update");
                                    }
                                }
                            }
                            if (binCount != 0) {
                                if (binCount >= 8) {
                                    treeifyBin(tab, i);
                                }
                            }
                        }
                    }
                }
            }
            tab = initTable();
        }
        if (delta != 0) {
            addCount((long) delta, binCount);
        }
        return val;
    }

    public boolean contains(Object value) {
        return containsValue(value);
    }

    public Enumeration<K> keys() {
        Node<K, V>[] t = this.table;
        int f = t == null ? 0 : t.length;
        return new KeyIterator(t, f, 0, f, this);
    }

    public Enumeration<V> elements() {
        Node<K, V>[] t = this.table;
        int f = t == null ? 0 : t.length;
        return new ValueIterator(t, f, 0, f, this);
    }

    public long mappingCount() {
        long n = sumCount();
        return n < 0 ? 0 : n;
    }

    public static <K> KeySetView<K, Boolean> newKeySet() {
        return new KeySetView(new ConcurrentHashMap(), Boolean.TRUE);
    }

    public static <K> KeySetView<K, Boolean> newKeySet(int initialCapacity) {
        return new KeySetView(new ConcurrentHashMap(initialCapacity), Boolean.TRUE);
    }

    public KeySetView<K, V> keySet(V mappedValue) {
        if (mappedValue != null) {
            return new KeySetView(this, mappedValue);
        }
        throw new NullPointerException();
    }

    static final int resizeStamp(int n) {
        return Integer.numberOfLeadingZeros(n) | NumericShaper.MYANMAR;
    }

    private final Node<K, V>[] initTable() {
        Node<K, V>[] tab;
        while (true) {
            tab = this.table;
            if (tab != null && tab.length != 0) {
                break;
            }
            int sc = this.sizeCtl;
            if (sc < 0) {
                Thread.yield();
            } else {
                if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                    try {
                        tab = this.table;
                        if (tab == null || tab.length == 0) {
                            int n = sc > 0 ? sc : 16;
                            Node<K, V>[] nt = new Node[n];
                            tab = nt;
                            this.table = nt;
                            sc = n - (n >>> 2);
                        }
                        this.sizeCtl = sc;
                    } catch (Throwable th) {
                        this.sizeCtl = sc;
                    }
                }
            }
        }
        return tab;
    }

    /* JADX WARNING: Missing block: B:3:0x001a, code:
            if ((r4.compareAndSwapLong(r31, r6, r8, r10) ^ 1) != 0) goto L_0x001c;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void addCount(long x, int check) {
        long s;
        CounterCell[] as = this.counterCells;
        if (as == null) {
            Unsafe unsafe = U;
            long j = BASECOUNT;
            long b = this.baseCount;
            s = b + x;
        }
        boolean uncontended = true;
        if (as != null) {
            int m = as.length - 1;
            if (m >= 0) {
                CounterCell a = as[ThreadLocalRandom.getProbe() & m];
                if (a != null) {
                    Unsafe unsafe2 = U;
                    long j2 = CELLVALUE;
                    long v = a.value;
                    uncontended = unsafe2.compareAndSwapLong(a, j2, v, v + x);
                    if ((uncontended ^ 1) == 0) {
                        if (check > 1) {
                            s = sumCount();
                            if (check >= 0) {
                                while (true) {
                                    int sc = this.sizeCtl;
                                    if (s < ((long) sc)) {
                                        break;
                                    }
                                    Node<K, V>[] tab = this.table;
                                    if (tab == null) {
                                        break;
                                    }
                                    int n = tab.length;
                                    if (n >= MAXIMUM_CAPACITY) {
                                        break;
                                    }
                                    int rs = resizeStamp(n);
                                    if (sc < 0) {
                                        if ((sc >>> 16) == rs && sc != rs + 1 && sc != MAX_RESIZERS + rs) {
                                            Node<K, V>[] nt = this.nextTable;
                                            if (nt == null || this.transferIndex <= 0) {
                                                break;
                                            }
                                            if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
                                                transfer(tab, nt);
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                    if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << 16) + 2)) {
                                        transfer(tab, null);
                                    }
                                    s = sumCount();
                                }
                            }
                            return;
                        }
                        return;
                    }
                }
            }
        }
        fullAddCount(x, uncontended);
    }

    final Node<K, V>[] helpTransfer(Node<K, V>[] tab, Node<K, V> f) {
        if (tab != null && (f instanceof ForwardingNode)) {
            Node<K, V>[] nextTab = ((ForwardingNode) f).nextTable;
            if (nextTab != null) {
                int rs = resizeStamp(tab.length);
                while (nextTab == this.nextTable && this.table == tab) {
                    int sc = this.sizeCtl;
                    if (sc >= 0 || (sc >>> 16) != rs || sc == rs + 1 || sc == MAX_RESIZERS + rs || this.transferIndex <= 0) {
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
        int c;
        if (size >= 536870912) {
            c = MAXIMUM_CAPACITY;
        } else {
            c = tableSizeFor(((size >>> 1) + size) + 1);
        }
        while (true) {
            int sc = this.sizeCtl;
            if (sc >= 0) {
                int n;
                Node<K, V>[] tab = this.table;
                if (tab != null) {
                    n = tab.length;
                    if (n != 0) {
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
                n = sc > c ? sc : c;
                if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                    try {
                        if (this.table == tab) {
                            this.table = new Node[n];
                            sc = n - (n >>> 2);
                        }
                        this.sizeCtl = sc;
                    } catch (Throwable th) {
                        this.sizeCtl = sc;
                    }
                }
            } else {
                return;
            }
        }
    }

    private final void transfer(Node<K, V>[] tab, Node<K, V>[] nextTab) {
        int stride;
        int n = tab.length;
        if (NCPU > 1) {
            stride = (n >>> 3) / NCPU;
        } else {
            stride = n;
        }
        if (stride < 16) {
            stride = 16;
        }
        if (nextTab == null) {
            try {
                Node<K, V>[] nt = new Node[(n << 1)];
                nextTab = nt;
                this.nextTable = nt;
                this.transferIndex = n;
            } catch (Throwable th) {
                this.sizeCtl = Integer.MAX_VALUE;
                return;
            }
        }
        int nextn = nextTab.length;
        Node forwardingNode = new ForwardingNode(nextTab);
        boolean advance = true;
        boolean finishing = false;
        int i = 0;
        int bound = 0;
        while (true) {
            if (advance) {
                i--;
                if (i >= bound || finishing) {
                    advance = false;
                } else {
                    int nextIndex = this.transferIndex;
                    if (nextIndex <= 0) {
                        i = -1;
                        advance = false;
                    } else {
                        Unsafe unsafe = U;
                        long j = TRANSFERINDEX;
                        int nextBound = nextIndex > stride ? nextIndex - stride : 0;
                        if (unsafe.compareAndSwapInt(this, j, nextIndex, nextBound)) {
                            bound = nextBound;
                            i = nextIndex - 1;
                            advance = false;
                        }
                    }
                }
            } else if (i >= 0 && i < n && i + n < nextn) {
                Node<K, V> f = tabAt(tab, i);
                if (f == null) {
                    advance = casTabAt(tab, i, null, forwardingNode);
                } else {
                    int fh = f.hash;
                    if (fh == -1) {
                        advance = true;
                    } else {
                        synchronized (f) {
                            if (tabAt(tab, i) == f) {
                                Node<K, V> ln;
                                Node<K, V> hn;
                                Node<K, V> node;
                                if (fh >= 0) {
                                    Node<K, V> p;
                                    Node<K, V> hn2;
                                    Node<K, V> ln2;
                                    int runBit = fh & n;
                                    Node<K, V> lastRun = f;
                                    for (p = f.next; p != null; p = p.next) {
                                        int b = p.hash & n;
                                        if (b != runBit) {
                                            runBit = b;
                                            lastRun = p;
                                        }
                                    }
                                    if (runBit == 0) {
                                        ln = lastRun;
                                        hn = null;
                                    } else {
                                        hn = lastRun;
                                        ln = null;
                                    }
                                    p = f;
                                    while (true) {
                                        hn2 = hn;
                                        ln2 = ln;
                                        if (p == lastRun) {
                                            break;
                                        }
                                        int ph = p.hash;
                                        K pk = p.key;
                                        V pv = p.val;
                                        if ((ph & n) == 0) {
                                            node = new Node(ph, pk, pv, ln2);
                                            hn = hn2;
                                        } else {
                                            node = new Node(ph, pk, pv, hn2);
                                            ln = ln2;
                                        }
                                        p = p.next;
                                    }
                                    setTabAt(nextTab, i, ln2);
                                    setTabAt(nextTab, i + n, hn2);
                                    setTabAt(tab, i, forwardingNode);
                                    advance = true;
                                } else if (f instanceof TreeBin) {
                                    Node<K, V> t = (TreeBin) f;
                                    Node lo = null;
                                    TreeNode<K, V> loTail = null;
                                    Node hi = null;
                                    TreeNode<K, V> hiTail = null;
                                    int lc = 0;
                                    int hc = 0;
                                    for (Node<K, V> e = t.first; e != null; e = e.next) {
                                        int h = e.hash;
                                        Node p2 = new TreeNode(h, e.key, e.val, null, null);
                                        if ((h & n) == 0) {
                                            p2.prev = loTail;
                                            if (loTail == null) {
                                                lo = p2;
                                            } else {
                                                loTail.next = p2;
                                            }
                                            loTail = p2;
                                            lc++;
                                        } else {
                                            p2.prev = hiTail;
                                            if (hiTail == null) {
                                                hi = p2;
                                            } else {
                                                hiTail.next = p2;
                                            }
                                            Node hiTail2 = p2;
                                            hc++;
                                        }
                                    }
                                    if (lc <= 6) {
                                        ln = untreeify(lo);
                                    } else if (hc != 0) {
                                        node = new TreeBin(lo);
                                    } else {
                                        ln = t;
                                    }
                                    if (hc <= 6) {
                                        hn = untreeify(hi);
                                    } else if (lc != 0) {
                                        node = new TreeBin(hi);
                                    } else {
                                        hn = t;
                                    }
                                    setTabAt(nextTab, i, ln);
                                    setTabAt(nextTab, i + n, hn);
                                    setTabAt(tab, i, forwardingNode);
                                    advance = true;
                                }
                            }
                        }
                    }
                }
            } else if (finishing) {
                this.nextTable = null;
                this.table = nextTab;
                this.sizeCtl = (n << 1) - (n >>> 1);
                return;
            } else {
                Unsafe unsafe2 = U;
                long j2 = SIZECTL;
                int sc = this.sizeCtl;
                if (!unsafe2.compareAndSwapInt(this, j2, sc, sc - 1)) {
                    continue;
                } else if (sc - 2 == (resizeStamp(n) << 16)) {
                    advance = true;
                    finishing = true;
                    i = n;
                } else {
                    return;
                }
            }
        }
    }

    final long sumCount() {
        CounterCell[] as = this.counterCells;
        long sum = this.baseCount;
        if (as != null) {
            for (CounterCell a : as) {
                if (a != null) {
                    sum += a.value;
                }
            }
        }
        return sum;
    }

    private final void fullAddCount(long x, boolean wasUncontended) {
        int h = ThreadLocalRandom.getProbe();
        if (h == 0) {
            ThreadLocalRandom.localInit();
            h = ThreadLocalRandom.getProbe();
            wasUncontended = true;
        }
        boolean collide = false;
        while (true) {
            CounterCell[] rs;
            long v;
            CounterCell[] as = this.counterCells;
            if (as != null) {
                int n = as.length;
                if (n > 0) {
                    CounterCell a = as[(n - 1) & h];
                    if (a == null) {
                        if (this.cellsBusy == 0) {
                            CounterCell counterCell = new CounterCell(x);
                            if (this.cellsBusy == 0) {
                                if (U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                                    boolean created = false;
                                    try {
                                        rs = this.counterCells;
                                        if (rs != null) {
                                            int m = rs.length;
                                            if (m > 0) {
                                                int j = (m - 1) & h;
                                                if (rs[j] == null) {
                                                    rs[j] = counterCell;
                                                    created = true;
                                                }
                                            }
                                        }
                                        this.cellsBusy = 0;
                                        if (created) {
                                            return;
                                        }
                                    } catch (Throwable th) {
                                        this.cellsBusy = 0;
                                    }
                                }
                            }
                        }
                        collide = false;
                    } else if (wasUncontended) {
                        Unsafe unsafe = U;
                        long j2 = CELLVALUE;
                        v = a.value;
                        if (!unsafe.compareAndSwapLong(a, j2, v, v + x)) {
                            if (this.counterCells != as || n >= NCPU) {
                                collide = false;
                            } else if (!collide) {
                                collide = true;
                            } else if (this.cellsBusy == 0) {
                                if (U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                                    try {
                                        if (this.counterCells == as) {
                                            rs = new CounterCell[(n << 1)];
                                            for (int i = 0; i < n; i++) {
                                                rs[i] = as[i];
                                            }
                                            this.counterCells = rs;
                                        }
                                        this.cellsBusy = 0;
                                        collide = false;
                                    } catch (Throwable th2) {
                                        this.cellsBusy = 0;
                                    }
                                }
                            }
                        } else {
                            return;
                        }
                    } else {
                        wasUncontended = true;
                    }
                    h = ThreadLocalRandom.advanceProbe(h);
                }
            }
            if (this.cellsBusy == 0 && this.counterCells == as) {
                if (U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                    boolean init = false;
                    try {
                        if (this.counterCells == as) {
                            rs = new CounterCell[2];
                            rs[h & 1] = new CounterCell(x);
                            this.counterCells = rs;
                            init = true;
                        }
                        this.cellsBusy = 0;
                        if (init) {
                            return;
                        }
                    } catch (Throwable th3) {
                        this.cellsBusy = 0;
                    }
                }
            }
            Unsafe unsafe2 = U;
            long j3 = BASECOUNT;
            v = this.baseCount;
            if (unsafe2.compareAndSwapLong(this, j3, v, v + x)) {
                return;
            }
        }
    }

    private final void treeifyBin(Node<K, V>[] tab, int index) {
        if (tab != null) {
            int n = tab.length;
            if (n < 64) {
                tryPresize(n << 1);
                return;
            }
            Node<K, V> b = tabAt(tab, index);
            if (b != null && b.hash >= 0) {
                synchronized (b) {
                    if (tabAt(tab, index) == b) {
                        TreeNode hd = null;
                        TreeNode<K, V> tl = null;
                        for (Node<K, V> e = b; e != null; e = e.next) {
                            TreeNode<K, V> p = new TreeNode(e.hash, e.key, e.val, null, null);
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
        Node<K, V> hd = null;
        Node<K, V> tl = null;
        for (Node<K, V> q = b; q != null; q = q.next) {
            Node<K, V> p = new Node(q.hash, q.key, q.val, null);
            if (tl == null) {
                hd = p;
            } else {
                tl.next = p;
            }
            tl = p;
        }
        return hd;
    }

    final int batchFor(long b) {
        if (b != Long.MAX_VALUE) {
            long n = sumCount();
            if (n > 1 && n >= b) {
                int sp = ForkJoinPool.getCommonPoolParallelism() << 2;
                if (b > 0) {
                    n /= b;
                    if (n < ((long) sp)) {
                        sp = (int) n;
                    }
                }
                return sp;
            }
        }
        return 0;
    }

    public void forEach(long parallelismThreshold, BiConsumer<? super K, ? super V> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        new ForEachMappingTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
    }

    public <U> void forEach(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null) {
            throw new NullPointerException();
        }
        new ForEachTransformedMappingTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action).invoke();
    }

    public <U> U search(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> searchFunction) {
        if (searchFunction == null) {
            throw new NullPointerException();
        }
        return new SearchMappingsTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()).invoke();
    }

    public <U> U reduce(long parallelismThreshold, BiFunction<? super K, ? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        return new MapReduceMappingsTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer).invoke();
    }

    public double reduceToDouble(long parallelismThreshold, ToDoubleBiFunction<? super K, ? super V> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer != null && reducer != null) {
            return ((Double) new MapReduceMappingsToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).doubleValue();
        }
        throw new NullPointerException();
    }

    public long reduceToLong(long parallelismThreshold, ToLongBiFunction<? super K, ? super V> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer != null && reducer != null) {
            return ((Long) new MapReduceMappingsToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).longValue();
        }
        throw new NullPointerException();
    }

    public int reduceToInt(long parallelismThreshold, ToIntBiFunction<? super K, ? super V> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        return ((Integer) new MapReduceMappingsToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).intValue();
    }

    public void forEachKey(long parallelismThreshold, Consumer<? super K> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        new ForEachKeyTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
    }

    public <U> void forEachKey(long parallelismThreshold, Function<? super K, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null) {
            throw new NullPointerException();
        }
        new ForEachTransformedKeyTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action).invoke();
    }

    public <U> U searchKeys(long parallelismThreshold, Function<? super K, ? extends U> searchFunction) {
        if (searchFunction == null) {
            throw new NullPointerException();
        }
        return new SearchKeysTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()).invoke();
    }

    public K reduceKeys(long parallelismThreshold, BiFunction<? super K, ? super K, ? extends K> reducer) {
        if (reducer == null) {
            throw new NullPointerException();
        }
        return new ReduceKeysTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, reducer).invoke();
    }

    public <U> U reduceKeys(long parallelismThreshold, Function<? super K, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        return new MapReduceKeysTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer).invoke();
    }

    public double reduceKeysToDouble(long parallelismThreshold, ToDoubleFunction<? super K> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer != null && reducer != null) {
            return ((Double) new MapReduceKeysToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).doubleValue();
        }
        throw new NullPointerException();
    }

    public long reduceKeysToLong(long parallelismThreshold, ToLongFunction<? super K> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer != null && reducer != null) {
            return ((Long) new MapReduceKeysToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).longValue();
        }
        throw new NullPointerException();
    }

    public int reduceKeysToInt(long parallelismThreshold, ToIntFunction<? super K> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        return ((Integer) new MapReduceKeysToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).intValue();
    }

    public void forEachValue(long parallelismThreshold, Consumer<? super V> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        new ForEachValueTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
    }

    public <U> void forEachValue(long parallelismThreshold, Function<? super V, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null) {
            throw new NullPointerException();
        }
        new ForEachTransformedValueTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action).invoke();
    }

    public <U> U searchValues(long parallelismThreshold, Function<? super V, ? extends U> searchFunction) {
        if (searchFunction == null) {
            throw new NullPointerException();
        }
        return new SearchValuesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()).invoke();
    }

    public V reduceValues(long parallelismThreshold, BiFunction<? super V, ? super V, ? extends V> reducer) {
        if (reducer == null) {
            throw new NullPointerException();
        }
        return new ReduceValuesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, reducer).invoke();
    }

    public <U> U reduceValues(long parallelismThreshold, Function<? super V, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        return new MapReduceValuesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer).invoke();
    }

    public double reduceValuesToDouble(long parallelismThreshold, ToDoubleFunction<? super V> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer != null && reducer != null) {
            return ((Double) new MapReduceValuesToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).doubleValue();
        }
        throw new NullPointerException();
    }

    public long reduceValuesToLong(long parallelismThreshold, ToLongFunction<? super V> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer != null && reducer != null) {
            return ((Long) new MapReduceValuesToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).longValue();
        }
        throw new NullPointerException();
    }

    public int reduceValuesToInt(long parallelismThreshold, ToIntFunction<? super V> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        return ((Integer) new MapReduceValuesToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).intValue();
    }

    public void forEachEntry(long parallelismThreshold, Consumer<? super Entry<K, V>> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        new ForEachEntryTask(null, batchFor(parallelismThreshold), 0, 0, this.table, action).invoke();
    }

    public <U> void forEachEntry(long parallelismThreshold, Function<Entry<K, V>, ? extends U> transformer, Consumer<? super U> action) {
        if (transformer == null || action == null) {
            throw new NullPointerException();
        }
        new ForEachTransformedEntryTask(null, batchFor(parallelismThreshold), 0, 0, this.table, transformer, action).invoke();
    }

    public <U> U searchEntries(long parallelismThreshold, Function<Entry<K, V>, ? extends U> searchFunction) {
        if (searchFunction == null) {
            throw new NullPointerException();
        }
        return new SearchEntriesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, searchFunction, new AtomicReference()).invoke();
    }

    public Entry<K, V> reduceEntries(long parallelismThreshold, BiFunction<Entry<K, V>, Entry<K, V>, ? extends Entry<K, V>> reducer) {
        if (reducer == null) {
            throw new NullPointerException();
        }
        return (Entry) new ReduceEntriesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, reducer).invoke();
    }

    public <U> U reduceEntries(long parallelismThreshold, Function<Entry<K, V>, ? extends U> transformer, BiFunction<? super U, ? super U, ? extends U> reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        return new MapReduceEntriesTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, reducer).invoke();
    }

    public double reduceEntriesToDouble(long parallelismThreshold, ToDoubleFunction<Entry<K, V>> transformer, double basis, DoubleBinaryOperator reducer) {
        if (transformer != null && reducer != null) {
            return ((Double) new MapReduceEntriesToDoubleTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).doubleValue();
        }
        throw new NullPointerException();
    }

    public long reduceEntriesToLong(long parallelismThreshold, ToLongFunction<Entry<K, V>> transformer, long basis, LongBinaryOperator reducer) {
        if (transformer != null && reducer != null) {
            return ((Long) new MapReduceEntriesToLongTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).longValue();
        }
        throw new NullPointerException();
    }

    public int reduceEntriesToInt(long parallelismThreshold, ToIntFunction<Entry<K, V>> transformer, int basis, IntBinaryOperator reducer) {
        if (transformer == null || reducer == null) {
            throw new NullPointerException();
        }
        return ((Integer) new MapReduceEntriesToIntTask(null, batchFor(parallelismThreshold), 0, 0, this.table, null, transformer, basis, reducer).invoke()).intValue();
    }
}
