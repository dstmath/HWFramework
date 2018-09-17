package java.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class LinkedHashMap<K, V> extends HashMap<K, V> implements Map<K, V> {
    private static final long serialVersionUID = 3801124242820219131L;
    final boolean accessOrder;
    transient LinkedHashMapEntry<K, V> head;
    transient LinkedHashMapEntry<K, V> tail;

    static class LinkedHashMapEntry<K, V> extends Node<K, V> {
        LinkedHashMapEntry<K, V> after;
        LinkedHashMapEntry<K, V> before;

        LinkedHashMapEntry(int hash, K key, V value, Node<K, V> next) {
            super(hash, key, value, next);
        }
    }

    abstract class LinkedHashIterator {
        LinkedHashMapEntry<K, V> current = null;
        int expectedModCount;
        LinkedHashMapEntry<K, V> next;

        LinkedHashIterator() {
            this.next = LinkedHashMap.this.head;
            this.expectedModCount = LinkedHashMap.this.modCount;
        }

        public final boolean hasNext() {
            return this.next != null;
        }

        final LinkedHashMapEntry<K, V> nextNode() {
            LinkedHashMapEntry<K, V> e = this.next;
            if (LinkedHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else if (e == null) {
                throw new NoSuchElementException();
            } else {
                this.current = e;
                this.next = e.after;
                return e;
            }
        }

        public final void remove() {
            Node<K, V> p = this.current;
            if (p == null) {
                throw new IllegalStateException();
            } else if (LinkedHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                this.current = null;
                K key = p.key;
                LinkedHashMap.this.removeNode(HashMap.hash(key), key, null, false, false);
                this.expectedModCount = LinkedHashMap.this.modCount;
            }
        }
    }

    final class LinkedEntryIterator extends LinkedHashIterator implements Iterator<Entry<K, V>> {
        LinkedEntryIterator() {
            super();
        }

        public final Entry<K, V> next() {
            return nextNode();
        }
    }

    final class LinkedEntrySet extends AbstractSet<Entry<K, V>> {
        LinkedEntrySet() {
        }

        public final int size() {
            return LinkedHashMap.this.size;
        }

        public final void clear() {
            LinkedHashMap.this.clear();
        }

        public final Iterator<Entry<K, V>> iterator() {
            return new LinkedEntryIterator();
        }

        public final boolean contains(Object o) {
            boolean z = false;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> e = (Entry) o;
            Object key = e.getKey();
            Node<K, V> candidate = LinkedHashMap.this.getNode(HashMap.hash(key), key);
            if (candidate != null) {
                z = candidate.equals(e);
            }
            return z;
        }

        public final boolean remove(Object o) {
            boolean z = true;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> e = (Entry) o;
            Object key = e.getKey();
            if (LinkedHashMap.this.removeNode(HashMap.hash(key), key, e.getValue(), true, true) == null) {
                z = false;
            }
            return z;
        }

        public final Spliterator<Entry<K, V>> spliterator() {
            return Spliterators.spliterator((Collection) this, 81);
        }

        public final void forEach(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int mc = LinkedHashMap.this.modCount;
            for (LinkedHashMapEntry<K, V> e = LinkedHashMap.this.head; e != null && mc == LinkedHashMap.this.modCount; e = e.after) {
                action.accept(e);
            }
            if (LinkedHashMap.this.modCount != mc) {
                throw new ConcurrentModificationException();
            }
        }
    }

    final class LinkedKeyIterator extends LinkedHashIterator implements Iterator<K> {
        LinkedKeyIterator() {
            super();
        }

        public final K next() {
            return nextNode().getKey();
        }
    }

    final class LinkedKeySet extends AbstractSet<K> {
        LinkedKeySet() {
        }

        public final int size() {
            return LinkedHashMap.this.size;
        }

        public final void clear() {
            LinkedHashMap.this.clear();
        }

        public final Iterator<K> iterator() {
            return new LinkedKeyIterator();
        }

        public final boolean contains(Object o) {
            return LinkedHashMap.this.containsKey(o);
        }

        public final boolean remove(Object key) {
            return LinkedHashMap.this.removeNode(HashMap.hash(key), key, null, false, true) != null;
        }

        public final Spliterator<K> spliterator() {
            return Spliterators.spliterator((Collection) this, 81);
        }

        public final void forEach(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int mc = LinkedHashMap.this.modCount;
            for (LinkedHashMapEntry<K, V> e = LinkedHashMap.this.head; e != null && LinkedHashMap.this.modCount == mc; e = e.after) {
                action.accept(e.key);
            }
            if (LinkedHashMap.this.modCount != mc) {
                throw new ConcurrentModificationException();
            }
        }
    }

    final class LinkedValueIterator extends LinkedHashIterator implements Iterator<V> {
        LinkedValueIterator() {
            super();
        }

        public final V next() {
            return nextNode().value;
        }
    }

    final class LinkedValues extends AbstractCollection<V> {
        LinkedValues() {
        }

        public final int size() {
            return LinkedHashMap.this.size;
        }

        public final void clear() {
            LinkedHashMap.this.clear();
        }

        public final Iterator<V> iterator() {
            return new LinkedValueIterator();
        }

        public final boolean contains(Object o) {
            return LinkedHashMap.this.containsValue(o);
        }

        public final Spliterator<V> spliterator() {
            return Spliterators.spliterator((Collection) this, 80);
        }

        public final void forEach(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int mc = LinkedHashMap.this.modCount;
            for (LinkedHashMapEntry<K, V> e = LinkedHashMap.this.head; e != null && LinkedHashMap.this.modCount == mc; e = e.after) {
                action.accept(e.value);
            }
            if (LinkedHashMap.this.modCount != mc) {
                throw new ConcurrentModificationException();
            }
        }
    }

    private void linkNodeLast(LinkedHashMapEntry<K, V> p) {
        LinkedHashMapEntry<K, V> last = this.tail;
        this.tail = p;
        if (last == null) {
            this.head = p;
            return;
        }
        p.before = last;
        last.after = p;
    }

    private void transferLinks(LinkedHashMapEntry<K, V> src, LinkedHashMapEntry<K, V> dst) {
        LinkedHashMapEntry<K, V> b = src.before;
        dst.before = b;
        LinkedHashMapEntry<K, V> a = src.after;
        dst.after = a;
        if (b == null) {
            this.head = dst;
        } else {
            b.after = dst;
        }
        if (a == null) {
            this.tail = dst;
        } else {
            a.before = dst;
        }
    }

    void reinitialize() {
        super.reinitialize();
        this.tail = null;
        this.head = null;
    }

    Node<K, V> newNode(int hash, K key, V value, Node<K, V> e) {
        LinkedHashMapEntry<K, V> p = new LinkedHashMapEntry(hash, key, value, e);
        linkNodeLast(p);
        return p;
    }

    Node<K, V> replacementNode(Node<K, V> p, Node<K, V> next) {
        LinkedHashMapEntry<K, V> q = (LinkedHashMapEntry) p;
        LinkedHashMapEntry<K, V> t = new LinkedHashMapEntry(q.hash, q.key, q.value, next);
        transferLinks(q, t);
        return t;
    }

    TreeNode<K, V> newTreeNode(int hash, K key, V value, Node<K, V> next) {
        TreeNode<K, V> p = new TreeNode(hash, key, value, next);
        linkNodeLast(p);
        return p;
    }

    TreeNode<K, V> replacementTreeNode(Node<K, V> p, Node<K, V> next) {
        LinkedHashMapEntry<K, V> q = (LinkedHashMapEntry) p;
        TreeNode<K, V> t = new TreeNode(q.hash, q.key, q.value, next);
        transferLinks(q, t);
        return t;
    }

    void afterNodeRemoval(Node<K, V> e) {
        LinkedHashMapEntry<K, V> p = (LinkedHashMapEntry) e;
        LinkedHashMapEntry<K, V> b = p.before;
        LinkedHashMapEntry<K, V> a = p.after;
        p.after = null;
        p.before = null;
        if (b == null) {
            this.head = a;
        } else {
            b.after = a;
        }
        if (a == null) {
            this.tail = b;
        } else {
            a.before = b;
        }
    }

    void afterNodeInsertion(boolean evict) {
        if (evict) {
            LinkedHashMapEntry<K, V> first = this.head;
            if (first != null && removeEldestEntry(first)) {
                K key = first.key;
                removeNode(HashMap.hash(key), key, null, false, true);
            }
        }
    }

    void afterNodeAccess(Node<K, V> e) {
        if (this.accessOrder) {
            LinkedHashMapEntry<K, V> last = this.tail;
            if (last != e) {
                LinkedHashMapEntry<K, V> p = (LinkedHashMapEntry) e;
                LinkedHashMapEntry<K, V> b = p.before;
                LinkedHashMapEntry<K, V> a = p.after;
                p.after = null;
                if (b == null) {
                    this.head = a;
                } else {
                    b.after = a;
                }
                if (a != null) {
                    a.before = b;
                } else {
                    last = b;
                }
                if (last == null) {
                    this.head = p;
                } else {
                    p.before = last;
                    last.after = p;
                }
                this.tail = p;
                this.modCount++;
            }
        }
    }

    void internalWriteEntries(ObjectOutputStream s) throws IOException {
        for (LinkedHashMapEntry<K, V> e = this.head; e != null; e = e.after) {
            s.writeObject(e.key);
            s.writeObject(e.value);
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
        this.accessOrder = false;
        putMapEntries(m, false);
    }

    public LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor);
        this.accessOrder = accessOrder;
    }

    public boolean containsValue(Object value) {
        for (LinkedHashMapEntry<K, V> e = this.head; e != null; e = e.after) {
            V v = e.value;
            if (v == value || (value != null && value.equals(v))) {
                return true;
            }
        }
        return false;
    }

    public V get(Object key) {
        Node<K, V> e = getNode(HashMap.hash(key), key);
        if (e == null) {
            return null;
        }
        if (this.accessOrder) {
            afterNodeAccess(e);
        }
        return e.value;
    }

    public V getOrDefault(Object key, V defaultValue) {
        Node<K, V> e = getNode(HashMap.hash(key), key);
        if (e == null) {
            return defaultValue;
        }
        if (this.accessOrder) {
            afterNodeAccess(e);
        }
        return e.value;
    }

    public void clear() {
        super.clear();
        this.tail = null;
        this.head = null;
    }

    public Entry<K, V> eldest() {
        return this.head;
    }

    protected boolean removeEldestEntry(Entry<K, V> entry) {
        return false;
    }

    public Set<K> keySet() {
        Set<K> ks = this.keySet;
        if (ks != null) {
            return ks;
        }
        ks = new LinkedKeySet();
        this.keySet = ks;
        return ks;
    }

    public Collection<V> values() {
        Collection<V> vs = this.values;
        if (vs != null) {
            return vs;
        }
        vs = new LinkedValues();
        this.values = vs;
        return vs;
    }

    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> set = this.entrySet;
        if (set != null) {
            return set;
        }
        set = new LinkedEntrySet();
        this.entrySet = set;
        return set;
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        int mc = this.modCount;
        LinkedHashMapEntry<K, V> e = this.head;
        while (this.modCount == mc && e != null) {
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
        LinkedHashMapEntry<K, V> e = this.head;
        while (this.modCount == mc && e != null) {
            e.value = function.apply(e.key, e.value);
            e = e.after;
        }
        if (this.modCount != mc) {
            throw new ConcurrentModificationException();
        }
    }
}
