package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.RCWeakRef;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {
    static final int DEFAULT_INITIAL_CAPACITY = 16;
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final int MAXIMUM_CAPACITY = 1073741824;
    static final int MIN_TREEIFY_CAPACITY = 64;
    static final int TREEIFY_THRESHOLD = 8;
    static final int UNTREEIFY_THRESHOLD = 6;
    private static final long serialVersionUID = 362498820763181265L;
    @RCWeakRef
    transient Set<Map.Entry<K, V>> entrySet;
    final float loadFactor;
    transient int modCount;
    transient int size;
    transient Node<K, V>[] table;
    int threshold;

    final class EntryIterator extends HashMap<K, V>.HashIterator implements Iterator<Map.Entry<K, V>> {
        EntryIterator() {
            super();
        }

        public final Map.Entry<K, V> next() {
            return nextNode();
        }
    }

    final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
        EntrySet() {
        }

        public final int size() {
            return HashMap.this.size;
        }

        public final void clear() {
            HashMap.this.clear();
        }

        public final Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public final boolean contains(Object o) {
            boolean z = false;
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry) o;
            Object key = e.getKey();
            Node<K, V> candidate = HashMap.this.getNode(HashMap.hash(key), key);
            if (candidate != null && candidate.equals(e)) {
                z = true;
            }
            return z;
        }

        public final boolean remove(Object o) {
            boolean z = false;
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry) o;
            Object key = e.getKey();
            if (HashMap.this.removeNode(HashMap.hash(key), key, e.getValue(), true, true) != null) {
                z = true;
            }
            return z;
        }

        public final Spliterator<Map.Entry<K, V>> spliterator() {
            EntrySpliterator entrySpliterator = new EntrySpliterator(HashMap.this, 0, -1, 0, 0);
            return entrySpliterator;
        }

        public final void forEach(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (HashMap.this.size > 0) {
                Node<K, V>[] nodeArr = HashMap.this.table;
                Node<K, V>[] tab = nodeArr;
                if (nodeArr != null) {
                    int mc = HashMap.this.modCount;
                    for (int i = 0; i < tab.length && HashMap.this.modCount == mc; i++) {
                        for (Node<K, V> e = tab[i]; e != null; e = e.next) {
                            action.accept(e);
                        }
                    }
                    if (HashMap.this.modCount != mc) {
                        throw new ConcurrentModificationException();
                    }
                }
            }
        }
    }

    static final class EntrySpliterator<K, V> extends HashMapSpliterator<K, V> implements Spliterator<Map.Entry<K, V>> {
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
            EntrySpliterator entrySpliterator = new EntrySpliterator(hashMap, lo, mid, i, this.expectedModCount);
            return entrySpliterator;
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            int mc;
            if (action != null) {
                HashMap<K, V> m = this.map;
                Node<K, V>[] tab = m.table;
                int i = this.fence;
                int hi = i;
                if (i < 0) {
                    mc = m.modCount;
                    this.expectedModCount = mc;
                    int length = tab == null ? 0 : tab.length;
                    this.fence = length;
                    hi = length;
                } else {
                    mc = this.expectedModCount;
                }
                if (tab != null && tab.length >= hi) {
                    int i2 = this.index;
                    int i3 = i2;
                    if (i2 >= 0) {
                        this.index = hi;
                        if (i3 < hi || this.current != null) {
                            Node<K, V> p = this.current;
                            this.current = null;
                            while (true) {
                                if (p == null) {
                                    p = tab[i3];
                                    i3++;
                                } else {
                                    action.accept(p);
                                    p = p.next;
                                }
                                if (p == null && i3 >= hi) {
                                    break;
                                }
                            }
                            if (m.modCount != mc) {
                                throw new ConcurrentModificationException();
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            if (action != null) {
                Node<K, V>[] tab = this.map.table;
                if (tab != null) {
                    int length = tab.length;
                    int fence = getFence();
                    int hi = fence;
                    if (length >= fence && this.index >= 0) {
                        while (true) {
                            if (this.current == null && this.index >= hi) {
                                break;
                            } else if (this.current == null) {
                                int i = this.index;
                                this.index = i + 1;
                                this.current = tab[i];
                            } else {
                                Node<K, V> e = this.current;
                                this.current = this.current.next;
                                action.accept(e);
                                if (this.map.modCount == this.expectedModCount) {
                                    return true;
                                }
                                throw new ConcurrentModificationException();
                            }
                        }
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

    abstract class HashIterator {
        Node<K, V> current = null;
        int expectedModCount;
        int index = 0;
        Node<K, V> next = null;

        HashIterator() {
            this.expectedModCount = HashMap.this.modCount;
            Node<K, V>[] t = HashMap.this.table;
            if (t != null && HashMap.this.size > 0) {
                while (this.index < t.length) {
                    int i = this.index;
                    this.index = i + 1;
                    Node<K, V> node = t[i];
                    this.next = node;
                    if (node != null) {
                        return;
                    }
                }
            }
        }

        public final boolean hasNext() {
            return this.next != null;
        }

        /* access modifiers changed from: package-private */
        public final Node<K, V> nextNode() {
            Node<K, V> e = this.next;
            if (HashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else if (e != null) {
                this.current = e;
                Node<K, V> node = e.next;
                this.next = node;
                if (node == null) {
                    Node<K, V>[] nodeArr = HashMap.this.table;
                    Node<K, V>[] t = nodeArr;
                    if (nodeArr != null) {
                        while (this.index < t.length) {
                            int i = this.index;
                            this.index = i + 1;
                            Node<K, V> node2 = t[i];
                            this.next = node2;
                            if (node2 != null) {
                                break;
                            }
                        }
                    }
                }
                return e;
            } else {
                throw new NoSuchElementException();
            }
        }

        public final void remove() {
            Node<K, V> p = this.current;
            if (p == null) {
                throw new IllegalStateException();
            } else if (HashMap.this.modCount == this.expectedModCount) {
                this.current = null;
                K key = p.key;
                HashMap.this.removeNode(HashMap.hash(key), key, null, false, false);
                this.expectedModCount = HashMap.this.modCount;
            } else {
                throw new ConcurrentModificationException();
            }
        }
    }

    static class HashMapSpliterator<K, V> {
        Node<K, V> current;
        int est;
        int expectedModCount;
        int fence;
        int index;
        final HashMap<K, V> map;

        HashMapSpliterator(HashMap<K, V> m, int origin, int fence2, int est2, int expectedModCount2) {
            this.map = m;
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
            HashMap<K, V> m = this.map;
            this.est = m.size;
            this.expectedModCount = m.modCount;
            Node<K, V>[] tab = m.table;
            int hi2 = tab == null ? 0 : tab.length;
            this.fence = hi2;
            return hi2;
        }

        public final long estimateSize() {
            getFence();
            return (long) this.est;
        }
    }

    final class KeyIterator extends HashMap<K, V>.HashIterator implements Iterator<K> {
        KeyIterator() {
            super();
        }

        public final K next() {
            return nextNode().key;
        }
    }

    final class KeySet extends AbstractSet<K> {
        KeySet() {
        }

        public final int size() {
            return HashMap.this.size;
        }

        public final void clear() {
            HashMap.this.clear();
        }

        public final Iterator<K> iterator() {
            return new KeyIterator();
        }

        public final boolean contains(Object o) {
            return HashMap.this.containsKey(o);
        }

        public final boolean remove(Object key) {
            return HashMap.this.removeNode(HashMap.hash(key), key, null, false, true) != null;
        }

        public final Spliterator<K> spliterator() {
            KeySpliterator keySpliterator = new KeySpliterator(HashMap.this, 0, -1, 0, 0);
            return keySpliterator;
        }

        public final void forEach(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (HashMap.this.size > 0) {
                Node<K, V>[] nodeArr = HashMap.this.table;
                Node<K, V>[] tab = nodeArr;
                if (nodeArr != null) {
                    int mc = HashMap.this.modCount;
                    for (int i = 0; i < tab.length && HashMap.this.modCount == mc; i++) {
                        for (Node<K, V> e = tab[i]; e != null; e = e.next) {
                            action.accept(e.key);
                        }
                    }
                    if (HashMap.this.modCount != mc) {
                        throw new ConcurrentModificationException();
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
            KeySpliterator keySpliterator = new KeySpliterator(hashMap, lo, mid, i, this.expectedModCount);
            return keySpliterator;
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int mc;
            if (action != null) {
                HashMap<K, V> m = this.map;
                Node<K, V>[] tab = m.table;
                int i = this.fence;
                int hi = i;
                if (i < 0) {
                    mc = m.modCount;
                    this.expectedModCount = mc;
                    int length = tab == null ? 0 : tab.length;
                    this.fence = length;
                    hi = length;
                } else {
                    mc = this.expectedModCount;
                }
                if (tab != null && tab.length >= hi) {
                    int i2 = this.index;
                    int i3 = i2;
                    if (i2 >= 0) {
                        this.index = hi;
                        if (i3 < hi || this.current != null) {
                            Node<K, V> p = this.current;
                            this.current = null;
                            while (true) {
                                if (p == null) {
                                    p = tab[i3];
                                    i3++;
                                } else {
                                    action.accept(p.key);
                                    p = p.next;
                                }
                                if (p == null && i3 >= hi) {
                                    break;
                                }
                            }
                            if (m.modCount != mc) {
                                throw new ConcurrentModificationException();
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            if (action != null) {
                Node<K, V>[] tab = this.map.table;
                if (tab != null) {
                    int length = tab.length;
                    int fence = getFence();
                    int hi = fence;
                    if (length >= fence && this.index >= 0) {
                        while (true) {
                            if (this.current == null && this.index >= hi) {
                                break;
                            } else if (this.current == null) {
                                int i = this.index;
                                this.index = i + 1;
                                this.current = tab[i];
                            } else {
                                K k = this.current.key;
                                this.current = this.current.next;
                                action.accept(k);
                                if (this.map.modCount == this.expectedModCount) {
                                    return true;
                                }
                                throw new ConcurrentModificationException();
                            }
                        }
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

    static class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        Node<K, V> next;
        V value;

        Node(int hash2, K key2, V value2, Node<K, V> next2) {
            this.hash = hash2;
            this.key = key2;
            this.value = value2;
            this.next = next2;
        }

        public final K getKey() {
            return this.key;
        }

        public final V getValue() {
            return this.value;
        }

        public final String toString() {
            return this.key + "=" + this.value;
        }

        public final int hashCode() {
            return Objects.hashCode(this.key) ^ Objects.hashCode(this.value);
        }

        public final V setValue(V newValue) {
            V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry) o;
                if (Objects.equals(this.key, e.getKey()) && Objects.equals(this.value, e.getValue())) {
                    return true;
                }
            }
            return false;
        }
    }

    static final class TreeNode<K, V> extends LinkedHashMap.LinkedHashMapEntry<K, V> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        TreeNode<K, V> left;
        TreeNode<K, V> parent;
        TreeNode<K, V> prev;
        boolean red;
        TreeNode<K, V> right;

        static {
            Class<HashMap> cls = HashMap.class;
        }

        TreeNode(int hash, K key, V val, Node<K, V> next) {
            super(hash, key, val, next);
        }

        /* access modifiers changed from: package-private */
        public final TreeNode<K, V> root() {
            TreeNode<K, V> r = this;
            while (true) {
                TreeNode<K, V> treeNode = r.parent;
                TreeNode<K, V> p = treeNode;
                if (treeNode == null) {
                    return r;
                }
                r = p;
            }
        }

        static <K, V> void moveRootToFront(Node<K, V>[] tab, TreeNode<K, V> root) {
            if (root != null && tab != null) {
                int length = tab.length;
                int n = length;
                if (length > 0) {
                    int index = (n - 1) & root.hash;
                    TreeNode<K, V> first = (TreeNode) tab[index];
                    if (root != first) {
                        tab[index] = root;
                        TreeNode<K, V> rp = root.prev;
                        Node<K, V> node = root.next;
                        Node<K, V> rn = node;
                        if (node != null) {
                            ((TreeNode) rn).prev = rp;
                        }
                        if (rp != null) {
                            rp.next = rn;
                        }
                        if (first != null) {
                            first.prev = root;
                        }
                        root.next = first;
                        root.prev = null;
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x002e, code lost:
            if (r3 != null) goto L_0x0030;
         */
        public final TreeNode<K, V> find(int h, Object k, Class<?> kc) {
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
                    Object obj = p.key;
                    Object obj2 = obj;
                    if (obj == k || (k != null && k.equals(obj2))) {
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
                            Class<?> comparableClassFor = HashMap.comparableClassFor(k);
                            kc2 = comparableClassFor;
                        }
                        int compareComparables = HashMap.compareComparables(kc2, k, obj2);
                        int dir = compareComparables;
                        if (compareComparables != 0) {
                            p = dir < 0 ? pl : pr;
                            continue;
                        }
                        TreeNode<K, V> find = pr.find(h, k, kc2);
                        TreeNode<K, V> q = find;
                        if (find != null) {
                            return q;
                        }
                        p = pl;
                        continue;
                    }
                }
            } while (p != null);
            return null;
        }

        /* access modifiers changed from: package-private */
        public final TreeNode<K, V> getTreeNode(int h, Object k) {
            return (this.parent != null ? root() : this).find(h, k, null);
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

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0032, code lost:
            if (r8 != null) goto L_0x0034;
         */
        public final void treeify(Node<K, V>[] tab) {
            int dir;
            TreeNode<K, V> xp;
            TreeNode<K, V> treeNode;
            TreeNode<K, V> root = null;
            TreeNode<K, V> x = this;
            while (x != null) {
                TreeNode<K, V> next = (TreeNode) x.next;
                x.right = null;
                x.left = null;
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                } else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    TreeNode<K, V> p = root;
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
                                Class<?> comparableClassFor = HashMap.comparableClassFor(k);
                                kc = comparableClassFor;
                            }
                            int compareComparables = HashMap.compareComparables(kc, k, pk);
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
                    root = balanceInsertion(root, x);
                }
                x = next;
            }
            moveRootToFront(tab, root);
        }

        /* access modifiers changed from: package-private */
        public final Node<K, V> untreeify(HashMap<K, V> map) {
            Node<K, V> tl = null;
            Node<K, V> hd = null;
            for (Node<K, V> q = this; q != null; q = q.next) {
                Node<K, V> p = map.replacementNode(q, null);
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
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0030, code lost:
            if (r4 != null) goto L_0x0032;
         */
        public final TreeNode<K, V> putTreeVal(HashMap<K, V> map, Node<K, V>[] tab, int h, K k, V v) {
            int dir;
            TreeNode treeNode;
            TreeNode<K, V> treeNode2;
            TreeNode<K, V> q;
            TreeNode root = this.parent != null ? root() : this;
            boolean searched = false;
            Class<?> kc = null;
            TreeNode<K, V> p = root;
            do {
                int i = p.hash;
                int ph = i;
                if (i > h) {
                    dir = -1;
                } else if (ph < h) {
                    dir = 1;
                } else {
                    K k2 = p.key;
                    K pk = k2;
                    if (k2 == k || (k != null && k.equals(pk))) {
                        return p;
                    }
                    if (kc == null) {
                        Class<?> comparableClassFor = HashMap.comparableClassFor(k);
                        kc = comparableClassFor;
                    }
                    int compareComparables = HashMap.compareComparables(kc, k, pk);
                    int dir2 = compareComparables;
                    if (compareComparables != 0) {
                        dir = dir2;
                    }
                    if (!searched) {
                        searched = true;
                        TreeNode<K, V> treeNode3 = p.left;
                        TreeNode<K, V> ch = treeNode3;
                        if (treeNode3 != null) {
                            TreeNode<K, V> find = ch.find(h, k, kc);
                            q = find;
                            if (find != null) {
                                return q;
                            }
                        }
                        TreeNode<K, V> treeNode4 = p.right;
                        TreeNode<K, V> ch2 = treeNode4;
                        if (treeNode4 != null) {
                            TreeNode<K, V> find2 = ch2.find(h, k, kc);
                            q = find2;
                            if (find2 != null) {
                                return q;
                            }
                        }
                    }
                    dir = tieBreakOrder(k, pk);
                }
                treeNode = p;
                treeNode2 = dir <= 0 ? p.left : p.right;
                p = treeNode2;
            } while (treeNode2 != null);
            Node<K, V> xpn = treeNode.next;
            TreeNode<K, V> x = map.newTreeNode(h, k, v, xpn);
            if (dir <= 0) {
                treeNode.left = x;
            } else {
                treeNode.right = x;
            }
            treeNode.next = x;
            x.prev = treeNode;
            x.parent = treeNode;
            if (xpn != null) {
                ((TreeNode) xpn).prev = x;
            }
            moveRootToFront(tab, balanceInsertion(root, x));
            return null;
        }

        /* access modifiers changed from: package-private */
        public final void removeTreeNode(HashMap<K, V> map, Node<K, V>[] tab, boolean movable) {
            TreeNode<K, V> root;
            TreeNode<K, V> replacement;
            TreeNode<K, V> root2;
            TreeNode<K, V> root3;
            TreeNode<K, V> replacement2;
            Node<K, V>[] nodeArr = tab;
            if (nodeArr != null) {
                int length = nodeArr.length;
                int n = length;
                if (length != 0) {
                    int index = (n - 1) & this.hash;
                    TreeNode<K, V> first = (TreeNode) nodeArr[index];
                    TreeNode<K, V> root4 = first;
                    TreeNode<K, V> succ = (TreeNode) this.next;
                    TreeNode<K, V> pred = this.prev;
                    if (pred == null) {
                        first = succ;
                        nodeArr[index] = succ;
                    } else {
                        pred.next = succ;
                    }
                    if (succ != null) {
                        succ.prev = pred;
                    }
                    if (first != null) {
                        if (root4.parent != null) {
                            root4 = root4.root();
                        }
                        if (!(root4 == null || root4.right == null)) {
                            TreeNode<K, V> treeNode = root4.left;
                            TreeNode<K, V> rl = treeNode;
                            if (treeNode != null) {
                                if (rl.left == null) {
                                    int i = n;
                                    TreeNode<K, V> treeNode2 = root4;
                                    nodeArr[index] = first.untreeify(map);
                                    return;
                                }
                                TreeNode<K, V> pl = this.left;
                                TreeNode<K, V> pr = this.right;
                                if (pl == null || pr == null) {
                                    root = root4;
                                    if (pl != null) {
                                        replacement = pl;
                                    } else if (pr != null) {
                                        replacement = pr;
                                    } else {
                                        replacement = this;
                                    }
                                } else {
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
                                    s.red = this.red;
                                    this.red = c;
                                    TreeNode<K, V> sr = s.right;
                                    TreeNode<K, V> pp = this.parent;
                                    if (s == pr) {
                                        this.parent = s;
                                        s.right = this;
                                        int i2 = n;
                                        root2 = root4;
                                    } else {
                                        int i3 = n;
                                        TreeNode<K, V> sp = s.parent;
                                        this.parent = sp;
                                        if (sp != null) {
                                            root2 = root4;
                                            if (s == sp.left) {
                                                sp.left = this;
                                            } else {
                                                sp.right = this;
                                            }
                                        } else {
                                            root2 = root4;
                                        }
                                        s.right = pr;
                                        if (pr != null) {
                                            pr.parent = s;
                                        }
                                    }
                                    this.left = null;
                                    this.right = sr;
                                    if (sr != null) {
                                        sr.parent = this;
                                    }
                                    s.left = pl;
                                    if (pl != null) {
                                        pl.parent = s;
                                    }
                                    s.parent = pp;
                                    if (pp == null) {
                                        root3 = s;
                                    } else {
                                        if (this == pp.left) {
                                            pp.left = s;
                                        } else {
                                            pp.right = s;
                                        }
                                        root3 = root2;
                                    }
                                    if (sr != null) {
                                        replacement2 = sr;
                                    } else {
                                        replacement2 = this;
                                    }
                                    replacement = replacement2;
                                    root = root3;
                                }
                                if (replacement != this) {
                                    TreeNode<K, V> pp2 = this.parent;
                                    replacement.parent = pp2;
                                    if (pp2 == null) {
                                        root = replacement;
                                    } else if (this == pp2.left) {
                                        pp2.left = replacement;
                                    } else {
                                        pp2.right = replacement;
                                    }
                                    this.parent = null;
                                    this.right = null;
                                    this.left = null;
                                }
                                TreeNode<K, V> root5 = root;
                                TreeNode<K, V> r = this.red ? root5 : balanceDeletion(root5, replacement);
                                if (replacement == this) {
                                    TreeNode<K, V> pp3 = this.parent;
                                    this.parent = null;
                                    if (pp3 != null) {
                                        if (this == pp3.left) {
                                            pp3.left = null;
                                        } else if (this == pp3.right) {
                                            pp3.right = null;
                                        }
                                    }
                                }
                                if (movable) {
                                    moveRootToFront(nodeArr, r);
                                }
                                return;
                            }
                        }
                        TreeNode<K, V> treeNode4 = root4;
                        nodeArr[index] = first.untreeify(map);
                        return;
                    }
                    return;
                }
            }
            HashMap<K, V> hashMap = map;
        }

        /* access modifiers changed from: package-private */
        public final void split(HashMap<K, V> map, Node<K, V>[] tab, int index, int bit) {
            TreeNode treeNode = null;
            int lc = 0;
            int hc = 0;
            TreeNode treeNode2 = null;
            TreeNode treeNode3 = null;
            TreeNode treeNode4 = null;
            TreeNode<K, V> e = this;
            while (e != null) {
                TreeNode<K, V> next = (TreeNode) e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    e.prev = treeNode3;
                    if (treeNode3 == null) {
                        treeNode4 = e;
                    } else {
                        treeNode3.next = e;
                    }
                    treeNode3 = e;
                    lc++;
                } else {
                    e.prev = treeNode;
                    if (treeNode == null) {
                        treeNode2 = e;
                    } else {
                        treeNode.next = e;
                    }
                    treeNode = e;
                    hc++;
                }
                e = next;
            }
            if (treeNode4 != null) {
                if (lc <= 6) {
                    tab[index] = treeNode4.untreeify(map);
                } else {
                    tab[index] = treeNode4;
                    if (treeNode2 != null) {
                        treeNode4.treeify(tab);
                    }
                }
            }
            if (treeNode2 == null) {
                return;
            }
            if (hc <= 6) {
                tab[index + bit] = treeNode2.untreeify(map);
                return;
            }
            tab[index + bit] = treeNode2;
            if (treeNode4 != null) {
                treeNode2.treeify(tab);
            }
        }

        static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root, TreeNode<K, V> p) {
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
                        root = r;
                        r.red = false;
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
                        root = l;
                        l.red = false;
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
                                root = rotateLeft(root, xp);
                                TreeNode<K, V> treeNode6 = x.parent;
                                xp = treeNode6;
                                if (treeNode6 != null) {
                                    treeNode4 = xp.parent;
                                }
                                xpp = treeNode4;
                            }
                            if (xp != null) {
                                xp.red = false;
                                if (xpp != null) {
                                    xpp.red = true;
                                    root = rotateRight(root, xpp);
                                }
                            }
                        } else {
                            xppr.red = false;
                            xp.red = false;
                            xpp.red = true;
                            x = xpp;
                        }
                    } else if (xppl == null || !xppl.red) {
                        if (x == xp.left) {
                            x = xp;
                            root = rotateRight(root, xp);
                            TreeNode<K, V> treeNode7 = x.parent;
                            xp = treeNode7;
                            if (treeNode7 != null) {
                                treeNode4 = xp.parent;
                            }
                            xpp = treeNode4;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    } else {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                } else {
                    x.red = false;
                    return x;
                }
            }
            return root;
        }

        static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root, TreeNode<K, V> x) {
            while (x != null && x != root) {
                TreeNode<K, V> treeNode = x.parent;
                TreeNode<K, V> xp = treeNode;
                if (treeNode == null) {
                    x.red = false;
                    return x;
                } else if (x.red) {
                    x.red = false;
                    return root;
                } else {
                    TreeNode<K, V> treeNode2 = xp.left;
                    TreeNode<K, V> xpl = treeNode2;
                    TreeNode<K, V> xpr = null;
                    if (treeNode2 == x) {
                        TreeNode<K, V> treeNode3 = xp.right;
                        TreeNode<K, V> xpr2 = treeNode3;
                        if (treeNode3 != null && xpr2.red) {
                            xpr2.red = false;
                            xp.red = true;
                            root = rotateLeft(root, xp);
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
                                        sl.red = false;
                                    }
                                    xpr2.red = true;
                                    root = rotateRight(root, xpr2);
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
                                        sr2.red = false;
                                    }
                                }
                                if (xp != null) {
                                    xp.red = false;
                                    root = rotateLeft(root, xp);
                                }
                                x = root;
                            }
                        }
                    } else {
                        if (xpl != null && xpl.red) {
                            xpl.red = false;
                            xp.red = true;
                            root = rotateRight(root, xp);
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
                                        sr3.red = false;
                                    }
                                    xpl.red = true;
                                    root = rotateLeft(root, xpl);
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
                                        sl3.red = false;
                                    }
                                }
                                if (xp != null) {
                                    xp.red = false;
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
            TreeNode<K, V> tn = (TreeNode) t.next;
            if (tb != null && tb.next != t) {
                return false;
            }
            if (tn != null && tn.prev != t) {
                return false;
            }
            if (tp != null && t != tp.left && t != tp.right) {
                return false;
            }
            if (tl != null && (tl.parent != t || tl.hash > t.hash)) {
                return false;
            }
            if (tr != null && (tr.parent != t || tr.hash < t.hash)) {
                return false;
            }
            if (t.red && tl != null && tl.red && tr != null && tr.red) {
                return false;
            }
            if (tl != null && !checkInvariants(tl)) {
                return false;
            }
            if (tr == null || checkInvariants(tr)) {
                return true;
            }
            return false;
        }
    }

    final class ValueIterator extends HashMap<K, V>.HashIterator implements Iterator<V> {
        ValueIterator() {
            super();
        }

        public final V next() {
            return nextNode().value;
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
            ValueSpliterator valueSpliterator = new ValueSpliterator(hashMap, lo, mid, i, this.expectedModCount);
            return valueSpliterator;
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int mc;
            if (action != null) {
                HashMap<K, V> m = this.map;
                Node<K, V>[] tab = m.table;
                int i = this.fence;
                int hi = i;
                if (i < 0) {
                    mc = m.modCount;
                    this.expectedModCount = mc;
                    int length = tab == null ? 0 : tab.length;
                    this.fence = length;
                    hi = length;
                } else {
                    mc = this.expectedModCount;
                }
                if (tab != null && tab.length >= hi) {
                    int i2 = this.index;
                    int i3 = i2;
                    if (i2 >= 0) {
                        this.index = hi;
                        if (i3 < hi || this.current != null) {
                            Node<K, V> p = this.current;
                            this.current = null;
                            while (true) {
                                if (p == null) {
                                    p = tab[i3];
                                    i3++;
                                } else {
                                    action.accept(p.value);
                                    p = p.next;
                                }
                                if (p == null && i3 >= hi) {
                                    break;
                                }
                            }
                            if (m.modCount != mc) {
                                throw new ConcurrentModificationException();
                            }
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            }
            throw new NullPointerException();
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            if (action != null) {
                Node<K, V>[] tab = this.map.table;
                if (tab != null) {
                    int length = tab.length;
                    int fence = getFence();
                    int hi = fence;
                    if (length >= fence && this.index >= 0) {
                        while (true) {
                            if (this.current == null && this.index >= hi) {
                                break;
                            } else if (this.current == null) {
                                int i = this.index;
                                this.index = i + 1;
                                this.current = tab[i];
                            } else {
                                V v = this.current.value;
                                this.current = this.current.next;
                                action.accept(v);
                                if (this.map.modCount == this.expectedModCount) {
                                    return true;
                                }
                                throw new ConcurrentModificationException();
                            }
                        }
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

    final class Values extends AbstractCollection<V> {
        Values() {
        }

        public final int size() {
            return HashMap.this.size;
        }

        public final void clear() {
            HashMap.this.clear();
        }

        public final Iterator<V> iterator() {
            return new ValueIterator();
        }

        public final boolean contains(Object o) {
            return HashMap.this.containsValue(o);
        }

        public final Spliterator<V> spliterator() {
            ValueSpliterator valueSpliterator = new ValueSpliterator(HashMap.this, 0, -1, 0, 0);
            return valueSpliterator;
        }

        public final void forEach(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (HashMap.this.size > 0) {
                Node<K, V>[] nodeArr = HashMap.this.table;
                Node<K, V>[] tab = nodeArr;
                if (nodeArr != null) {
                    int mc = HashMap.this.modCount;
                    for (int i = 0; i < tab.length && HashMap.this.modCount == mc; i++) {
                        for (Node<K, V> e = tab[i]; e != null; e = e.next) {
                            action.accept(e.value);
                        }
                    }
                    if (HashMap.this.modCount != mc) {
                        throw new ConcurrentModificationException();
                    }
                }
            }
        }
    }

    static final int hash(Object key) {
        if (key == null) {
            return 0;
        }
        int h = key.hashCode();
        return h ^ (h >>> 16);
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

    static final int tableSizeFor(int cap) {
        int n = cap - 1;
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

    public HashMap(int initialCapacity, float loadFactor2) {
        if (initialCapacity >= 0) {
            initialCapacity = initialCapacity > MAXIMUM_CAPACITY ? MAXIMUM_CAPACITY : initialCapacity;
            if (loadFactor2 <= 0.0f || Float.isNaN(loadFactor2)) {
                throw new IllegalArgumentException("Illegal load factor: " + loadFactor2);
            }
            this.loadFactor = loadFactor2;
            this.threshold = tableSizeFor(initialCapacity);
            return;
        }
        throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    }

    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }

    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }

    /* access modifiers changed from: package-private */
    public final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
            if (this.table == null) {
                float ft = (((float) s) / this.loadFactor) + 1.0f;
                int t = ft < 1.07374182E9f ? (int) ft : MAXIMUM_CAPACITY;
                if (t > this.threshold) {
                    this.threshold = tableSizeFor(t);
                }
            } else if (s > this.threshold) {
                resize();
            }
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                putVal(hash(key), key, e.getValue(), false, evict);
            }
        }
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public V get(Object key) {
        Node<K, V> node = getNode(hash(key), key);
        Node<K, V> e = node;
        if (node == null) {
            return null;
        }
        return e.value;
    }

    /* access modifiers changed from: package-private */
    public final Node<K, V> getNode(int hash, Object key) {
        Node<K, V> node;
        Node<K, V>[] nodeArr = this.table;
        Node<K, V>[] tab = nodeArr;
        if (nodeArr != null) {
            int length = tab.length;
            int n = length;
            if (length > 0) {
                Node<K, V> node2 = tab[(n - 1) & hash];
                Node<K, V> first = node2;
                if (node2 != null) {
                    if (first.hash == hash) {
                        K k = first.key;
                        K k2 = k;
                        if (k == key || (key != null && key.equals(k2))) {
                            return first;
                        }
                    }
                    Node<K, V> node3 = first.next;
                    Node<K, V> e = node3;
                    if (node3 != null) {
                        if (first instanceof TreeNode) {
                            return ((TreeNode) first).getTreeNode(hash, key);
                        }
                        do {
                            if (e.hash == hash) {
                                K k3 = e.key;
                                K k4 = k3;
                                if (k3 == key || (key != null && key.equals(k4))) {
                                    return e;
                                }
                            }
                            node = e.next;
                            e = node;
                        } while (node != null);
                    }
                }
            }
        }
        return null;
    }

    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }

    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:3:0x000e, code lost:
        if (r0 == 0) goto L_0x0013;
     */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0079  */
    public final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
        int n;
        Node<K, V> e;
        Node<K, V> e2;
        int i = hash;
        K k = key;
        V v = value;
        Node<K, V>[] nodeArr = this.table;
        Node<K, V>[] tab = nodeArr;
        if (nodeArr != null) {
            int length = tab.length;
            n = length;
        }
        Node<K, V>[] resize = resize();
        tab = resize;
        n = resize.length;
        Node<K, V>[] tab2 = tab;
        int i2 = (n - 1) & i;
        int i3 = i2;
        Node<K, V> node = tab2[i2];
        Node<K, V> p = node;
        if (node == null) {
            tab2[i3] = newNode(i, k, v, null);
        } else {
            if (p.hash == i) {
                K k2 = p.key;
                K k3 = k2;
                if (k2 == k || (k != null && k.equals(k3))) {
                    e = p;
                    if (e != null) {
                        V oldValue = e.value;
                        if (!onlyIfAbsent || oldValue == null) {
                            e.value = v;
                        }
                        afterNodeAccess(e);
                        return oldValue;
                    }
                }
            }
            if (p instanceof TreeNode) {
                e = ((TreeNode) p).putTreeVal(this, tab2, i, k, v);
            } else {
                int binCount = 0;
                while (true) {
                    Node<K, V> node2 = p.next;
                    e2 = node2;
                    if (node2 == null) {
                        p.next = newNode(i, k, v, null);
                        if (binCount >= 7) {
                            treeifyBin(tab2, i);
                        }
                    } else {
                        if (e2.hash == i) {
                            K k4 = e2.key;
                            K k5 = k4;
                            if (k4 != k) {
                                if (k != null && k.equals(k5)) {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        boolean z = evict;
                        p = e2;
                        binCount++;
                    }
                }
                e = e2;
            }
            if (e != null) {
            }
        }
        this.modCount++;
        int i4 = this.size + 1;
        this.size = i4;
        if (i4 > this.threshold) {
            resize();
        }
        afterNodeInsertion(evict);
        return null;
    }

    /* access modifiers changed from: package-private */
    public final Node<K, V>[] resize() {
        int newCap;
        Node<K, V> next;
        Node<K, V>[] oldTab = this.table;
        int oldCap = oldTab == null ? 0 : oldTab.length;
        int oldThr = this.threshold;
        int newThr = 0;
        int i = Integer.MAX_VALUE;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                this.threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            int i2 = oldCap << 1;
            newCap = i2;
            if (i2 < MAXIMUM_CAPACITY && oldCap >= 16) {
                newThr = oldThr << 1;
            }
        } else if (oldThr > 0) {
            newCap = oldThr;
        } else {
            newCap = 16;
            newThr = 12;
        }
        if (newThr == 0) {
            float ft = ((float) newCap) * this.loadFactor;
            if (newCap < MAXIMUM_CAPACITY && ft < 1.07374182E9f) {
                i = (int) ft;
            }
            newThr = i;
        }
        this.threshold = newThr;
        Node<K, V>[] newTab = new Node[newCap];
        this.table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; j++) {
                Node<K, V> node = oldTab[j];
                Node<K, V> e = node;
                if (node != null) {
                    oldTab[j] = null;
                    if (e.next == null) {
                        newTab[e.hash & (newCap - 1)] = e;
                    } else if (e instanceof TreeNode) {
                        ((TreeNode) e).split(this, newTab, j, oldCap);
                    } else {
                        Node<K, V> loTail = null;
                        Node<K, V> hiHead = null;
                        Node<K, V> loHead = null;
                        Node<K, V> e2 = e;
                        Node<K, V> hiTail = null;
                        do {
                            next = e2.next;
                            if ((e2.hash & oldCap) == 0) {
                                if (loTail == null) {
                                    loHead = e2;
                                } else {
                                    loTail.next = e2;
                                }
                                loTail = e2;
                            } else {
                                if (hiTail == null) {
                                    hiHead = e2;
                                } else {
                                    hiTail.next = e2;
                                }
                                hiTail = e2;
                            }
                            e2 = next;
                        } while (next != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

    /* access modifiers changed from: package-private */
    public final void treeifyBin(Node<K, V>[] tab, int hash) {
        Node<K, V> node;
        if (tab != null) {
            int length = tab.length;
            int n = length;
            if (length >= 64) {
                int i = (n - 1) & hash;
                int index = i;
                Node<K, V> node2 = tab[i];
                Node<K, V> e = node2;
                if (node2 != null) {
                    TreeNode<K, V> hd = null;
                    TreeNode<K, V> tl = null;
                    do {
                        TreeNode<K, V> p = replacementTreeNode(e, null);
                        if (tl == null) {
                            hd = p;
                        } else {
                            p.prev = tl;
                            tl.next = p;
                        }
                        tl = p;
                        node = e.next;
                        e = node;
                    } while (node != null);
                    tab[index] = hd;
                    if (hd != null) {
                        hd.treeify(tab);
                        return;
                    }
                    return;
                }
                return;
            }
        }
        resize();
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m, true);
    }

    public V remove(Object key) {
        Node<K, V> removeNode = removeNode(hash(key), key, null, false, true);
        Node<K, V> e = removeNode;
        if (removeNode == null) {
            return null;
        }
        return e.value;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x005f, code lost:
        if (r11.equals(r6) == false) goto L_0x0087;
     */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0052  */
    public final Node<K, V> removeNode(int hash, Object key, Object value, boolean matchValue, boolean movable) {
        Node<K, V>[] nodeArr = this.table;
        Node<K, V>[] tab = nodeArr;
        if (nodeArr != null) {
            int length = tab.length;
            int n = length;
            if (length > 0) {
                int i = (n - 1) & hash;
                int index = i;
                Node<K, V> node = tab[i];
                Node<K, V> p = node;
                if (node != null) {
                    Node<K, V> node2 = null;
                    if (p.hash == hash) {
                        K k = p.key;
                        K k2 = k;
                        if (k == key || (key != null && key.equals(k2))) {
                            node2 = p;
                            if (node2 != null) {
                                if (matchValue) {
                                    V v = node2.value;
                                    V v2 = v;
                                    if (v != value) {
                                        if (value != null) {
                                        }
                                    }
                                }
                                if (node2 instanceof TreeNode) {
                                    ((TreeNode) node2).removeTreeNode(this, tab, movable);
                                } else if (node2 == p) {
                                    tab[index] = node2.next;
                                } else {
                                    p.next = node2.next;
                                }
                                this.modCount++;
                                this.size--;
                                afterNodeRemoval(node2);
                                return node2;
                            }
                        }
                    }
                    Node<K, V> node3 = p.next;
                    Node<K, V> e = node3;
                    if (node3 != null) {
                        if (p instanceof TreeNode) {
                            node2 = ((TreeNode) p).getTreeNode(hash, key);
                        } else {
                            while (true) {
                                if (e.hash == hash) {
                                    K k3 = e.key;
                                    K k4 = k3;
                                    if (k3 == key || (key != null && key.equals(k4))) {
                                        node2 = e;
                                    }
                                }
                                p = e;
                                Node<K, V> node4 = e.next;
                                e = node4;
                                if (node4 == null) {
                                    break;
                                }
                            }
                            node2 = e;
                        }
                    }
                    if (node2 != null) {
                    }
                }
            }
        }
        return null;
    }

    public void clear() {
        this.modCount++;
        Node<K, V>[] nodeArr = this.table;
        Node<K, V>[] tab = nodeArr;
        if (nodeArr != null && this.size > 0) {
            this.size = 0;
            for (int i = 0; i < tab.length; i++) {
                tab[i] = null;
            }
        }
    }

    public boolean containsValue(Object value) {
        Node<K, V>[] nodeArr = this.table;
        Node<K, V>[] tab = nodeArr;
        if (nodeArr != null && this.size > 0) {
            for (Node<K, V> e : tab) {
                while (e != null) {
                    V v = e.value;
                    V v2 = v;
                    if (v == value || (value != null && value.equals(v2))) {
                        return true;
                    }
                    e = e.next;
                }
            }
        }
        return false;
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
        Set<Map.Entry<K, V>> set = this.entrySet;
        Set<Map.Entry<K, V>> es = set;
        if (set != null) {
            return es;
        }
        EntrySet entrySet2 = new EntrySet();
        this.entrySet = entrySet2;
        return entrySet2;
    }

    public V getOrDefault(Object key, V defaultValue) {
        Node<K, V> e = getNode(hash(key), key);
        return e == null ? defaultValue : e.value;
    }

    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, true, true);
    }

    public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }

    public boolean replace(K key, V oldValue, V newValue) {
        Node<K, V> node = getNode(hash(key), key);
        Node<K, V> e = node;
        if (node != null) {
            V v = e.value;
            V v2 = v;
            if (v == oldValue || (v2 != null && v2.equals(oldValue))) {
                e.value = newValue;
                afterNodeAccess(e);
                return true;
            }
        }
        return false;
    }

    public V replace(K key, V value) {
        Node<K, V> node = getNode(hash(key), key);
        Node<K, V> e = node;
        if (node == null) {
            return null;
        }
        V oldValue = e.value;
        e.value = value;
        afterNodeAccess(e);
        return oldValue;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x001c, code lost:
        if (r3 == 0) goto L_0x001e;
     */
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        int n;
        Node<K, V>[] tab;
        V v;
        K k = key;
        Function<? super K, ? extends V> function = mappingFunction;
        if (function != null) {
            int hash = hash(key);
            int binCount = 0;
            TreeNode treeNode = null;
            Node<K, V> old = null;
            if (this.size <= this.threshold) {
                Node<K, V>[] nodeArr = this.table;
                tab = nodeArr;
                if (nodeArr != null) {
                    int length = tab.length;
                    n = length;
                }
            }
            Node<K, V>[] resize = resize();
            tab = resize;
            n = resize.length;
            Node<K, V>[] tab2 = tab;
            int i = (n - 1) & hash;
            int i2 = i;
            Node<K, V> node = tab2[i];
            Node<K, V> first = node;
            if (node != null) {
                if (first instanceof TreeNode) {
                    TreeNode treeNode2 = (TreeNode) first;
                    treeNode = treeNode2;
                    old = treeNode2.getTreeNode(hash, k);
                } else {
                    int binCount2 = 0;
                    Node<K, V> e = first;
                    while (true) {
                        if (e.hash == hash) {
                            K k2 = e.key;
                            K k3 = k2;
                            if (k2 == k || (k != null && k.equals(k3))) {
                                old = e;
                            }
                        }
                        binCount2++;
                        Node<K, V> node2 = e.next;
                        e = node2;
                        if (node2 == null) {
                            break;
                        }
                    }
                    old = e;
                    binCount = binCount2;
                }
                if (old != null) {
                    V v2 = old.value;
                    V oldValue = v2;
                    if (v2 != null) {
                        afterNodeAccess(old);
                        return oldValue;
                    }
                }
            }
            int binCount3 = binCount;
            TreeNode treeNode3 = treeNode;
            Node<K, V> old2 = old;
            V v3 = function.apply(k);
            if (v3 == null) {
                return null;
            }
            if (old2 != null) {
                old2.value = v3;
                afterNodeAccess(old2);
                return v3;
            }
            if (treeNode3 != null) {
                V v4 = v3;
                int i3 = binCount3;
                treeNode3.putTreeVal(this, tab2, hash, k, v4);
                v = v4;
            } else {
                int binCount4 = binCount3;
                v = v3;
                tab2[i2] = newNode(hash, k, v, first);
                if (binCount4 >= 7) {
                    treeifyBin(tab2, hash);
                }
            }
            this.modCount++;
            this.size++;
            afterNodeInsertion(true);
            return v;
        }
        throw new NullPointerException();
    }

    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction != null) {
            int hash = hash(key);
            Node<K, V> node = getNode(hash, key);
            Node<K, V> e = node;
            if (node != null) {
                V v = e.value;
                V oldValue = v;
                if (v != null) {
                    V v2 = remappingFunction.apply(key, oldValue);
                    if (v2 != null) {
                        e.value = v2;
                        afterNodeAccess(e);
                        return v2;
                    }
                    removeNode(hash, key, null, false, true);
                }
            }
            return null;
        }
        throw new NullPointerException();
    }

    /* JADX WARNING: Code restructure failed: missing block: B:7:0x001c, code lost:
        if (r3 == 0) goto L_0x001e;
     */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0061  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0063  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x006c  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0093  */
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        int n;
        Node<K, V>[] tab;
        TreeNode<K, V> t;
        Node<K, V> old;
        int binCount;
        V v;
        K k = key;
        BiFunction<? super K, ? super V, ? extends V> biFunction = remappingFunction;
        if (biFunction != null) {
            int hash = hash(key);
            TreeNode<K, V> t2 = null;
            Node<K, V> old2 = null;
            if (this.size <= this.threshold) {
                Node<K, V>[] nodeArr = this.table;
                tab = nodeArr;
                if (nodeArr != null) {
                    int length = tab.length;
                    n = length;
                }
            }
            Node<K, V>[] resize = resize();
            tab = resize;
            n = resize.length;
            Node<K, V>[] tab2 = tab;
            int i = (n - 1) & hash;
            int i2 = i;
            Node<K, V> node = tab2[i];
            Node<K, V> first = node;
            if (node != null) {
                if (first instanceof TreeNode) {
                    TreeNode<K, V> treeNode = (TreeNode) first;
                    t2 = treeNode;
                    old2 = treeNode.getTreeNode(hash, k);
                } else {
                    int binCount2 = 0;
                    Node<K, V> e = first;
                    while (true) {
                        if (e.hash == hash) {
                            K k2 = e.key;
                            K k3 = k2;
                            if (k2 == k || (k != null && k.equals(k3))) {
                                old2 = e;
                            }
                        }
                        binCount2++;
                        Node<K, V> node2 = e.next;
                        e = node2;
                        if (node2 == null) {
                            break;
                        }
                    }
                    old2 = e;
                    t = null;
                    old = old2;
                    binCount = binCount2;
                    V oldValue = old != null ? null : old.value;
                    V v2 = biFunction.apply(k, oldValue);
                    if (old != null) {
                        V v3 = oldValue;
                        int binCount3 = binCount;
                        V v4 = v2;
                        if (v4 == null) {
                            return v4;
                        }
                        if (t != null) {
                            t.putTreeVal(this, tab2, hash, k, v4);
                            v = v4;
                        } else {
                            v = v4;
                            tab2[i2] = newNode(hash, k, v, first);
                            if (binCount3 >= 7) {
                                treeifyBin(tab2, hash);
                            }
                        }
                        this.modCount++;
                        this.size++;
                        afterNodeInsertion(true);
                        return v;
                    } else if (v2 != null) {
                        old.value = v2;
                        afterNodeAccess(old);
                        V v5 = oldValue;
                        int i3 = binCount;
                        return v2;
                    } else {
                        V v6 = oldValue;
                        int i4 = binCount;
                        removeNode(hash, k, null, false, true);
                        return v2;
                    }
                }
            }
            binCount = 0;
            t = t2;
            old = old2;
            V oldValue2 = old != null ? null : old.value;
            V v22 = biFunction.apply(k, oldValue2);
            if (old != null) {
            }
        } else {
            throw new NullPointerException();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0020, code lost:
        if (r3 == 0) goto L_0x0022;
     */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0067  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0092  */
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        int n;
        Node<K, V>[] tab;
        TreeNode<K, V> t;
        Node<K, V> old;
        int binCount;
        V v;
        V v2;
        K k = key;
        V v3 = value;
        BiFunction<? super V, ? super V, ? extends V> biFunction = remappingFunction;
        if (v3 == null) {
            throw new NullPointerException();
        } else if (biFunction != null) {
            int hash = hash(key);
            TreeNode<K, V> t2 = null;
            Node<K, V> old2 = null;
            if (this.size <= this.threshold) {
                Node<K, V>[] nodeArr = this.table;
                tab = nodeArr;
                if (nodeArr != null) {
                    int length = tab.length;
                    n = length;
                }
            }
            Node<K, V>[] resize = resize();
            tab = resize;
            n = resize.length;
            Node<K, V>[] tab2 = tab;
            int i = (n - 1) & hash;
            int i2 = i;
            Node<K, V> node = tab2[i];
            Node<K, V> first = node;
            if (node != null) {
                if (first instanceof TreeNode) {
                    TreeNode<K, V> treeNode = (TreeNode) first;
                    t2 = treeNode;
                    old2 = treeNode.getTreeNode(hash, k);
                } else {
                    int binCount2 = 0;
                    Node<K, V> e = first;
                    while (true) {
                        if (e.hash == hash) {
                            K k2 = e.key;
                            K k3 = k2;
                            if (k2 == k || (k != null && k.equals(k3))) {
                                old2 = e;
                            }
                        }
                        binCount2++;
                        Node<K, V> node2 = e.next;
                        e = node2;
                        if (node2 == null) {
                            break;
                        }
                    }
                    t = null;
                    old = old2;
                    binCount = binCount2;
                    if (old == null) {
                        if (old.value != null) {
                            v = biFunction.apply(old.value, v3);
                        } else {
                            v = v3;
                        }
                        V v4 = v;
                        if (v4 != null) {
                            old.value = v4;
                            afterNodeAccess(old);
                            v2 = v4;
                            int i3 = binCount;
                        } else {
                            v2 = v4;
                            int i4 = binCount;
                            removeNode(hash, k, null, false, true);
                        }
                        return v2;
                    }
                    int binCount3 = binCount;
                    if (v3 != null) {
                        if (t != null) {
                            t.putTreeVal(this, tab2, hash, k, v3);
                        } else {
                            tab2[i2] = newNode(hash, k, v3, first);
                            if (binCount3 >= 7) {
                                treeifyBin(tab2, hash);
                            }
                        }
                        this.modCount++;
                        this.size++;
                        afterNodeInsertion(true);
                    }
                    return v3;
                }
            }
            binCount = 0;
            t = t2;
            old = old2;
            if (old == null) {
            }
        } else {
            throw new NullPointerException();
        }
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) {
            throw new NullPointerException();
        } else if (this.size > 0) {
            Node<K, V>[] nodeArr = this.table;
            Node<K, V>[] tab = nodeArr;
            if (nodeArr != null) {
                int mc = this.modCount;
                for (int i = 0; i < tab.length && mc == this.modCount; i++) {
                    for (Node<K, V> e = tab[i]; e != null; e = e.next) {
                        action.accept(e.key, e.value);
                    }
                }
                if (this.modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null) {
            throw new NullPointerException();
        } else if (this.size > 0) {
            Node<K, V>[] nodeArr = this.table;
            Node<K, V>[] tab = nodeArr;
            if (nodeArr != null) {
                int mc = this.modCount;
                for (Node<K, V> e : tab) {
                    while (e != null) {
                        e.value = function.apply(e.key, e.value);
                        e = e.next;
                    }
                }
                if (this.modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    public Object clone() {
        try {
            HashMap<K, V> result = (HashMap) super.clone();
            result.reinitialize();
            result.putMapEntries(this, false);
            return result;
        } catch (CloneNotSupportedException e) {
            throw new InternalError((Throwable) e);
        }
    }

    /* access modifiers changed from: package-private */
    public final float loadFactor() {
        return this.loadFactor;
    }

    /* access modifiers changed from: package-private */
    public final int capacity() {
        if (this.table != null) {
            return this.table.length;
        }
        if (this.threshold > 0) {
            return this.threshold;
        }
        return 16;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        int buckets = capacity();
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(this.size);
        internalWriteEntries(s);
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        int i;
        s.defaultReadObject();
        reinitialize();
        if (this.loadFactor <= 0.0f || Float.isNaN(this.loadFactor)) {
            throw new InvalidObjectException("Illegal load factor: " + this.loadFactor);
        }
        s.readInt();
        int mappings = s.readInt();
        if (mappings < 0) {
            throw new InvalidObjectException("Illegal mappings count: " + mappings);
        } else if (mappings > 0) {
            float lf = Math.min(Math.max(0.25f, this.loadFactor), 4.0f);
            float fc = (((float) mappings) / lf) + 1.0f;
            if (fc < 16.0f) {
                i = 16;
            } else {
                i = fc >= 1.07374182E9f ? MAXIMUM_CAPACITY : tableSizeFor((int) fc);
            }
            int cap = i;
            float ft = ((float) cap) * lf;
            this.threshold = (cap >= MAXIMUM_CAPACITY || ft >= 1.07374182E9f) ? Integer.MAX_VALUE : (int) ft;
            this.table = new Node[cap];
            int i2 = 0;
            while (true) {
                int i3 = i2;
                if (i3 < mappings) {
                    K key = s.readObject();
                    putVal(hash(key), key, s.readObject(), false, false);
                    i2 = i3 + 1;
                } else {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
        return new Node<>(hash, key, value, next);
    }

    /* access modifiers changed from: package-private */
    public Node<K, V> replacementNode(Node<K, V> p, Node<K, V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    /* access modifiers changed from: package-private */
    public TreeNode<K, V> newTreeNode(int hash, K key, V value, Node<K, V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    /* access modifiers changed from: package-private */
    public TreeNode<K, V> replacementTreeNode(Node<K, V> p, Node<K, V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    /* access modifiers changed from: package-private */
    public void reinitialize() {
        this.table = null;
        this.entrySet = null;
        this.keySet = null;
        this.values = null;
        this.modCount = 0;
        this.threshold = 0;
        this.size = 0;
    }

    /* access modifiers changed from: package-private */
    public void afterNodeAccess(Node<K, V> node) {
    }

    /* access modifiers changed from: package-private */
    public void afterNodeInsertion(boolean evict) {
    }

    /* access modifiers changed from: package-private */
    public void afterNodeRemoval(Node<K, V> node) {
    }

    /* access modifiers changed from: package-private */
    public void internalWriteEntries(ObjectOutputStream s) throws IOException {
        if (this.size > 0) {
            Node<K, V>[] nodeArr = this.table;
            Node<K, V>[] tab = nodeArr;
            if (nodeArr != null) {
                for (Node<K, V> e : tab) {
                    while (e != null) {
                        s.writeObject(e.key);
                        s.writeObject(e.value);
                        e = e.next;
                    }
                }
            }
        }
    }
}
