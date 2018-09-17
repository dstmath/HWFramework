package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map.Entry;
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
    transient Set<Entry<K, V>> entrySet;
    final float loadFactor;
    transient int modCount;
    transient int size;
    transient Node<K, V>[] table;
    int threshold;

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
                    Node node = t[i];
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

        final Node<K, V> nextNode() {
            Node<K, V> e = this.next;
            if (HashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else if (e == null) {
                throw new NoSuchElementException();
            } else {
                this.current = e;
                Node node = e.next;
                this.next = node;
                if (node == null) {
                    Node<K, V>[] t = HashMap.this.table;
                    if (t != null) {
                        while (this.index < t.length) {
                            int i = this.index;
                            this.index = i + 1;
                            node = t[i];
                            this.next = node;
                            if (node != null) {
                                break;
                            }
                        }
                    }
                }
                return e;
            }
        }

        public final void remove() {
            Node<K, V> p = this.current;
            if (p == null) {
                throw new IllegalStateException();
            } else if (HashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            } else {
                this.current = null;
                K key = p.key;
                HashMap.this.removeNode(HashMap.hash(key), key, null, false, false);
                this.expectedModCount = HashMap.this.modCount;
            }
        }
    }

    final class EntryIterator extends HashIterator implements Iterator<Entry<K, V>> {
        EntryIterator() {
            super();
        }

        public final Entry<K, V> next() {
            return nextNode();
        }
    }

    final class EntrySet extends AbstractSet<Entry<K, V>> {
        EntrySet() {
        }

        public final int size() {
            return HashMap.this.size;
        }

        public final void clear() {
            HashMap.this.clear();
        }

        public final Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        public final boolean contains(Object o) {
            boolean z = false;
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> e = (Entry) o;
            Object key = e.getKey();
            Node<K, V> candidate = HashMap.this.getNode(HashMap.hash(key), key);
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
            if (HashMap.this.removeNode(HashMap.hash(key), key, e.getValue(), true, true) == null) {
                z = false;
            }
            return z;
        }

        public final Spliterator<Entry<K, V>> spliterator() {
            return new EntrySpliterator(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (HashMap.this.size > 0) {
                Node<K, V>[] tab = HashMap.this.table;
                if (tab != null) {
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

    static class HashMapSpliterator<K, V> {
        Node<K, V> current;
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
                Node<K, V>[] tab = m.table;
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
            Node<K, V>[] tab = m.table;
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
                        Node<K, V> p = this.current;
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
            Node<K, V>[] tab = this.map.table;
            if (tab != null) {
                int length = tab.length;
                int hi = getFence();
                if (length >= hi && this.index >= 0) {
                    while (true) {
                        if (this.current == null && this.index >= hi) {
                            break;
                        } else if (this.current == null) {
                            length = this.index;
                            this.index = length + 1;
                            this.current = tab[length];
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

        public int characteristics() {
            int i = 0;
            if (this.fence < 0 || this.est == this.map.size) {
                i = 64;
            }
            return i | 1;
        }
    }

    final class KeyIterator extends HashIterator implements Iterator<K> {
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
            return new KeySpliterator(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (HashMap.this.size > 0) {
                Node<K, V>[] tab = HashMap.this.table;
                if (tab != null) {
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
            return new KeySpliterator(hashMap, lo, mid, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int mc;
            HashMap<K, V> m = this.map;
            Node<K, V>[] tab = m.table;
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
                        Node<K, V> p = this.current;
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
            Node<K, V>[] tab = this.map.table;
            if (tab != null) {
                int length = tab.length;
                int hi = getFence();
                if (length >= hi && this.index >= 0) {
                    while (true) {
                        if (this.current == null && this.index >= hi) {
                            break;
                        } else if (this.current == null) {
                            length = this.index;
                            this.index = length + 1;
                            this.current = tab[length];
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

        public int characteristics() {
            int i = 0;
            if (this.fence < 0 || this.est == this.map.size) {
                i = 64;
            }
            return i | 1;
        }
    }

    static class Node<K, V> implements Entry<K, V> {
        final int hash;
        final K key;
        Node<K, V> next;
        V value;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
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
            if (o instanceof Entry) {
                Entry<?, ?> e = (Entry) o;
                if (Objects.equals(this.key, e.getKey()) && Objects.equals(this.value, e.getValue())) {
                    return true;
                }
            }
            return false;
        }
    }

    static final class TreeNode<K, V> extends LinkedHashMapEntry<K, V> {
        static final /* synthetic */ boolean -assertionsDisabled = (TreeNode.class.desiredAssertionStatus() ^ 1);
        TreeNode<K, V> left;
        TreeNode<K, V> parent;
        TreeNode<K, V> prev;
        boolean red;
        TreeNode<K, V> right;

        TreeNode(int hash, K key, V val, Node<K, V> next) {
            super(hash, key, val, next);
        }

        final TreeNode<K, V> root() {
            TreeNode<K, V> r = this;
            while (true) {
                TreeNode<K, V> p = r.parent;
                if (p == null) {
                    return r;
                }
                r = p;
            }
        }

        static <K, V> void moveRootToFront(Node<K, V>[] tab, TreeNode<K, V> root) {
            if (root != null && tab != null) {
                int n = tab.length;
                if (n > 0) {
                    int index = (n - 1) & root.hash;
                    TreeNode<K, V> first = tab[index];
                    if (root != first) {
                        tab[index] = root;
                        TreeNode<K, V> rp = root.prev;
                        Node<K, V> rn = root.next;
                        if (rn != null) {
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
                    if (!-assertionsDisabled && !checkInvariants(root)) {
                        throw new AssertionError();
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:20:0x002d, code:
            if (r12 != null) goto L_0x002f;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        final TreeNode<K, V> find(int h, Object k, Class<?> kc) {
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
                    if (pk == k || (k != null && k.equals(pk))) {
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
                            kc = HashMap.comparableClassFor(k);
                        }
                        int dir = HashMap.compareComparables(kc, k, pk);
                        if (dir != 0) {
                            if (dir < 0) {
                                p = pl;
                                continue;
                            } else {
                                p = pr;
                                continue;
                            }
                        }
                        TreeNode<K, V> q = pr.find(h, k, kc);
                        if (q != null) {
                            return q;
                        }
                        p = pl;
                        continue;
                    }
                }
            } while (p != null);
            return null;
        }

        final TreeNode<K, V> getTreeNode(int h, Object k) {
            TreeNode<K, V> thisR;
            if (this.parent != null) {
                thisR = root();
            }
            return thisR.find(h, k, null);
        }

        static int tieBreakOrder(Object a, Object b) {
            if (!(a == null || b == null)) {
                int d = a.getClass().getName().compareTo(b.getClass().getName());
                if (d != 0) {
                    return d;
                }
            }
            return System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1;
        }

        /* JADX WARNING: Missing block: B:22:0x0040, code:
            if (r3 == null) goto L_0x0042;
     */
        /* JADX WARNING: Missing block: B:23:0x0042, code:
            r0 = tieBreakOrder(r2, r7);
     */
        /* JADX WARNING: Missing block: B:25:0x004b, code:
            if (r0 == 0) goto L_0x0042;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        final void treeify(Node<K, V>[] tab) {
            TreeNode<K, V> root = null;
            TreeNode<K, V> x = this;
            while (x != null) {
                TreeNode<K, V> next = x.next;
                x.right = null;
                x.left = null;
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                } else {
                    int dir;
                    TreeNode<K, V> xp;
                    K k = x.key;
                    int h = x.hash;
                    Class kc = null;
                    TreeNode<K, V> p = root;
                    do {
                        K pk = p.key;
                        int ph = p.hash;
                        if (ph > h) {
                            dir = -1;
                        } else if (ph < h) {
                            dir = 1;
                        } else {
                            if (kc == null) {
                                kc = HashMap.comparableClassFor(k);
                            }
                            dir = HashMap.compareComparables(kc, k, pk);
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
                    root = balanceInsertion(root, x);
                }
                x = next;
            }
            moveRootToFront(tab, root);
        }

        final Node<K, V> untreeify(HashMap<K, V> map) {
            Node<K, V> hd = null;
            Node<K, V> tl = null;
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

        /* JADX WARNING: Missing block: B:31:0x0069, code:
            if (r6 == null) goto L_0x006b;
     */
        /* JADX WARNING: Missing block: B:32:0x006b, code:
            if (r12 != false) goto L_0x0094;
     */
        /* JADX WARNING: Missing block: B:33:0x006d, code:
            r12 = true;
            r4 = r7.left;
     */
        /* JADX WARNING: Missing block: B:34:0x0070, code:
            if (r4 == null) goto L_0x0086;
     */
        /* JADX WARNING: Missing block: B:35:0x0072, code:
            r10 = r4.find(r20, r21, r6);
     */
        /* JADX WARNING: Missing block: B:36:0x007a, code:
            if (r10 == null) goto L_0x0086;
     */
        /* JADX WARNING: Missing block: B:37:0x007c, code:
            return r10;
     */
        /* JADX WARNING: Missing block: B:39:0x0083, code:
            if (r5 == 0) goto L_0x006b;
     */
        /* JADX WARNING: Missing block: B:40:0x0086, code:
            r4 = r7.right;
     */
        /* JADX WARNING: Missing block: B:41:0x0088, code:
            if (r4 == null) goto L_0x0094;
     */
        /* JADX WARNING: Missing block: B:42:0x008a, code:
            r10 = r4.find(r20, r21, r6);
     */
        /* JADX WARNING: Missing block: B:43:0x0092, code:
            if (r10 != null) goto L_0x007c;
     */
        /* JADX WARNING: Missing block: B:44:0x0094, code:
            r5 = tieBreakOrder(r21, r9);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        final TreeNode<K, V> putTreeVal(HashMap<K, V> map, Node<K, V>[] tab, int h, K k, V v) {
            int dir;
            TreeNode<K, V> xp;
            Class kc = null;
            boolean searched = false;
            TreeNode<K, V> root = this.parent != null ? root() : this;
            TreeNode<K, V> p = root;
            do {
                int ph = p.hash;
                if (ph > h) {
                    dir = -1;
                } else if (ph < h) {
                    dir = 1;
                } else {
                    K pk = p.key;
                    if (pk == k || (k != null && k.equals(pk))) {
                        return p;
                    }
                    if (kc == null) {
                        kc = HashMap.comparableClassFor(k);
                    }
                    dir = HashMap.compareComparables(kc, k, pk);
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
            Node<K, V> xpn = xp.next;
            TreeNode<K, V> x = map.newTreeNode(h, k, v, xpn);
            if (dir <= 0) {
                xp.left = x;
            } else {
                xp.right = x;
            }
            xp.next = x;
            x.prev = xp;
            x.parent = xp;
            if (xpn != null) {
                ((TreeNode) xpn).prev = x;
            }
            moveRootToFront(tab, balanceInsertion(root, x));
            return null;
        }

        final void removeTreeNode(HashMap<K, V> map, Node<K, V>[] tab, boolean movable) {
            if (tab != null) {
                int n = tab.length;
                if (n != 0) {
                    int index = (n - 1) & this.hash;
                    TreeNode<K, V> first = tab[index];
                    TreeNode<K, V> root = first;
                    Node succ = (TreeNode) this.next;
                    TreeNode<K, V> pred = this.prev;
                    if (pred == null) {
                        first = succ;
                        tab[index] = succ;
                    } else {
                        pred.next = succ;
                    }
                    if (succ != null) {
                        succ.prev = pred;
                    }
                    if (first != null) {
                        if (root.parent != null) {
                            root = root.root();
                        }
                        if (!(root == null || root.right == null)) {
                            TreeNode<K, V> rl = root.left;
                            if (!(rl == null || rl.left == null)) {
                                TreeNode<K, V> pp;
                                TreeNode<K, V> replacement;
                                TreeNode<K, V> pl = this.left;
                                TreeNode<K, V> pr = this.right;
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
                                    s.red = this.red;
                                    this.red = c;
                                    TreeNode<K, V> sr = s.right;
                                    pp = this.parent;
                                    if (s == pr) {
                                        this.parent = s;
                                        s.right = this;
                                    } else {
                                        TreeNode<K, V> sp = s.parent;
                                        this.parent = sp;
                                        if (sp != null) {
                                            if (s == sp.left) {
                                                sp.left = this;
                                            } else {
                                                sp.right = this;
                                            }
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
                                        root = s;
                                    } else {
                                        if (this == pp.left) {
                                            pp.left = s;
                                        } else {
                                            pp.right = s;
                                        }
                                    }
                                    if (sr != null) {
                                        replacement = sr;
                                    } else {
                                        replacement = this;
                                    }
                                } else if (pl != null) {
                                    replacement = pl;
                                } else if (pr != null) {
                                    replacement = pr;
                                } else {
                                    replacement = this;
                                }
                                if (replacement != this) {
                                    pp = this.parent;
                                    replacement.parent = pp;
                                    if (pp == null) {
                                        root = replacement;
                                    } else {
                                        if (this == pp.left) {
                                            pp.left = replacement;
                                        } else {
                                            pp.right = replacement;
                                        }
                                    }
                                    this.parent = null;
                                    this.right = null;
                                    this.left = null;
                                }
                                TreeNode<K, V> r = this.red ? root : balanceDeletion(root, replacement);
                                if (replacement == this) {
                                    pp = this.parent;
                                    this.parent = null;
                                    if (pp != null) {
                                        if (this == pp.left) {
                                            pp.left = null;
                                        } else {
                                            if (this == pp.right) {
                                                pp.right = null;
                                            }
                                        }
                                    }
                                }
                                if (movable) {
                                    moveRootToFront(tab, r);
                                }
                                return;
                            }
                        }
                        tab[index] = first.untreeify(map);
                    }
                }
            }
        }

        final void split(HashMap<K, V> map, Node<K, V>[] tab, int index, int bit) {
            TreeNode loHead = null;
            TreeNode loTail = null;
            TreeNode hiHead = null;
            TreeNode<K, V> hiTail = null;
            int lc = 0;
            int hc = 0;
            TreeNode<K, V> e = this;
            while (e != null) {
                TreeNode<K, V> next = e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    e.prev = loTail;
                    if (loTail == null) {
                        loHead = e;
                    } else {
                        loTail.next = e;
                    }
                    loTail = e;
                    lc++;
                } else {
                    e.prev = hiTail;
                    if (hiTail == null) {
                        hiHead = e;
                    } else {
                        hiTail.next = e;
                    }
                    hiTail = e;
                    hc++;
                }
                e = next;
            }
            if (loHead != null) {
                if (lc <= 6) {
                    tab[index] = loHead.untreeify(map);
                } else {
                    tab[index] = loHead;
                    if (hiHead != null) {
                        loHead.treeify(tab);
                    }
                }
            }
            if (hiHead == null) {
                return;
            }
            if (hc <= 6) {
                tab[index + bit] = hiHead.untreeify(map);
                return;
            }
            tab[index + bit] = hiHead;
            if (loHead != null) {
                hiHead.treeify(tab);
            }
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
                            xp = xp.parent;
                            xpp = xp == null ? null : xp.parent;
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
                TreeNode<K, V> xp = x.parent;
                if (xp == null) {
                    x.red = false;
                    return x;
                } else if (x.red) {
                    x.red = false;
                    return root;
                } else {
                    TreeNode xpl = xp.left;
                    TreeNode<K, V> sl;
                    TreeNode<K, V> sr;
                    if (xpl == x) {
                        TreeNode xpr = xp.right;
                        if (xpr != null && xpr.red) {
                            xpr.red = false;
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
                                        sl.red = false;
                                    }
                                    xpr.red = true;
                                    root = rotateRight(root, xpr);
                                    xp = x.parent;
                                    xpr = xp == null ? null : xp.right;
                                }
                                if (xpr != null) {
                                    xpr.red = xp == null ? false : xp.red;
                                    sr = xpr.right;
                                    if (sr != null) {
                                        sr.red = false;
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
                                        sr.red = false;
                                    }
                                    xpl.red = true;
                                    root = rotateLeft(root, xpl);
                                    xp = x.parent;
                                    xpl = xp == null ? null : xp.left;
                                }
                                if (xpl != null) {
                                    xpl.red = xp == null ? false : xp.red;
                                    sl = xpl.left;
                                    if (sl != null) {
                                        sl.red = false;
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
            TreeNode<K, V> tn = t.next;
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
            if (tl != null && (checkInvariants(tl) ^ 1) != 0) {
                return false;
            }
            if (tr == null || (checkInvariants(tr) ^ 1) == 0) {
                return true;
            }
            return false;
        }
    }

    final class ValueIterator extends HashIterator implements Iterator<V> {
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
            return new ValueSpliterator(hashMap, lo, mid, i, this.expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            int mc;
            HashMap<K, V> m = this.map;
            Node<K, V>[] tab = m.table;
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
                        Node<K, V> p = this.current;
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
            Node<K, V>[] tab = this.map.table;
            if (tab != null) {
                int length = tab.length;
                int hi = getFence();
                if (length >= hi && this.index >= 0) {
                    while (true) {
                        if (this.current == null && this.index >= hi) {
                            break;
                        } else if (this.current == null) {
                            length = this.index;
                            this.index = length + 1;
                            this.current = tab[length];
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
            return new ValueSpliterator(HashMap.this, 0, -1, 0, 0);
        }

        public final void forEach(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            } else if (HashMap.this.size > 0) {
                Node<K, V>[] tab = HashMap.this.table;
                if (tab != null) {
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
        return (h >>> 16) ^ h;
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

    static final int tableSizeFor(int cap) {
        int n = cap - 1;
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

    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
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

    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
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
            for (Entry<? extends K, ? extends V> e : m.entrySet()) {
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
        Node<K, V> e = getNode(hash(key), key);
        return e == null ? null : e.value;
    }

    final Node<K, V> getNode(int hash, Object key) {
        Node<K, V>[] tab = this.table;
        if (tab != null) {
            int n = tab.length;
            if (n > 0) {
                Node<K, V> first = tab[(n - 1) & hash];
                if (first != null) {
                    K k;
                    if (first.hash == hash) {
                        k = first.key;
                        if (k == key || (key != null && key.equals(k))) {
                            return first;
                        }
                    }
                    Node<K, V> e = first.next;
                    if (e != null) {
                        if (first instanceof TreeNode) {
                            return ((TreeNode) first).getTreeNode(hash, key);
                        }
                        do {
                            if (e.hash == hash) {
                                k = e.key;
                                if (k == key || (key != null && key.equals(k))) {
                                    return e;
                                }
                            }
                            e = e.next;
                        } while (e != null);
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

    /* JADX WARNING: Removed duplicated region for block: B:22:0x0069  */
    /* JADX WARNING: Missing block: B:3:0x0007, code:
            if (r14 == 0) goto L_0x0009;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {
        int n;
        Node<K, V>[] tab = this.table;
        if (tab != null) {
            n = tab.length;
        }
        tab = resize();
        n = tab.length;
        int i = (n - 1) & hash;
        Node<K, V> p = tab[i];
        if (p == null) {
            tab[i] = newNode(hash, key, value, null);
        } else {
            K k;
            Node<K, V> e;
            if (p.hash == hash) {
                k = p.key;
                if (k == key || (key != null && key.equals(k))) {
                    e = p;
                    if (e != null) {
                        V oldValue = e.value;
                        if (!onlyIfAbsent || oldValue == null) {
                            e.value = value;
                        }
                        afterNodeAccess(e);
                        return oldValue;
                    }
                }
            }
            if (p instanceof TreeNode) {
                e = ((TreeNode) p).putTreeVal(this, tab, hash, key, value);
            } else {
                int binCount = 0;
                while (true) {
                    e = p.next;
                    if (e == null) {
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= 7) {
                            treeifyBin(tab, hash);
                        }
                    } else {
                        if (e.hash == hash) {
                            k = e.key;
                            if (k != key) {
                                if (key != null && key.equals(k)) {
                                    break;
                                }
                            }
                            break;
                        }
                        p = e;
                        binCount++;
                    }
                }
            }
            if (e != null) {
            }
        }
        this.modCount++;
        int i2 = this.size + 1;
        this.size = i2;
        if (i2 > this.threshold) {
            resize();
        }
        afterNodeInsertion(evict);
        return null;
    }

    final Node<K, V>[] resize() {
        int newCap;
        Node<K, V>[] oldTab = this.table;
        int oldCap = oldTab == null ? 0 : oldTab.length;
        int oldThr = this.threshold;
        int newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                this.threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            newCap = oldCap << 1;
            if (newCap < MAXIMUM_CAPACITY && oldCap >= 16) {
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
            newThr = (newCap >= MAXIMUM_CAPACITY || ft >= 1.07374182E9f) ? Integer.MAX_VALUE : (int) ft;
        }
        this.threshold = newThr;
        Node<K, V>[] newTab = new Node[newCap];
        this.table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; j++) {
                Node<K, V> e = oldTab[j];
                if (e != null) {
                    oldTab[j] = null;
                    if (e.next == null) {
                        newTab[e.hash & (newCap - 1)] = e;
                    } else if (e instanceof TreeNode) {
                        ((TreeNode) e).split(this, newTab, j, oldCap);
                    } else {
                        Node<K, V> loHead = null;
                        Node loTail = null;
                        Node<K, V> hiHead = null;
                        Node<K, V> hiTail = null;
                        Node<K, V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null) {
                                    loHead = e;
                                } else {
                                    loTail.next = e;
                                }
                                loTail = e;
                            } else {
                                if (hiTail == null) {
                                    hiHead = e;
                                } else {
                                    hiTail.next = e;
                                }
                                hiTail = e;
                            }
                            e = next;
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

    final void treeifyBin(Node<K, V>[] tab, int hash) {
        if (tab != null) {
            int n = tab.length;
            if (n >= 64) {
                int index = (n - 1) & hash;
                Node<K, V> e = tab[index];
                if (e != null) {
                    TreeNode<K, V> hd = null;
                    TreeNode tl = null;
                    do {
                        TreeNode<K, V> p = replacementTreeNode(e, null);
                        if (tl == null) {
                            hd = p;
                        } else {
                            p.prev = tl;
                            tl.next = p;
                        }
                        TreeNode<K, V> tl2 = p;
                        e = e.next;
                    } while (e != null);
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
        Node<K, V> e = removeNode(hash(key), key, null, false, true);
        if (e == null) {
            return null;
        }
        return e.value;
    }

    /* JADX WARNING: Removed duplicated region for block: B:15:0x0024  */
    /* JADX WARNING: Missing block: B:40:0x0071, code:
            if (r13.equals(r7) != false) goto L_0x002a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final Node<K, V> removeNode(int hash, Object key, Object value, boolean matchValue, boolean movable) {
        Node<K, V>[] tab = this.table;
        if (tab != null) {
            int n = tab.length;
            if (n > 0) {
                int index = (n - 1) & hash;
                Node<K, V> p = tab[index];
                if (p != null) {
                    K k;
                    Node<K, V> node = null;
                    if (p.hash == hash) {
                        k = p.key;
                        if (k == key || (key != null && key.equals(k))) {
                            node = p;
                            if (node != null) {
                                if (matchValue) {
                                    V v = node.value;
                                    if (v != value) {
                                        if (value != null) {
                                        }
                                    }
                                }
                                if (node instanceof TreeNode) {
                                    ((TreeNode) node).removeTreeNode(this, tab, movable);
                                } else if (node == p) {
                                    tab[index] = node.next;
                                } else {
                                    p.next = node.next;
                                }
                                this.modCount++;
                                this.size--;
                                afterNodeRemoval(node);
                                return node;
                            }
                        }
                    }
                    Node<K, V> e = p.next;
                    if (e != null) {
                        if (p instanceof TreeNode) {
                            node = ((TreeNode) p).getTreeNode(hash, key);
                        } else {
                            do {
                                if (e.hash == hash) {
                                    k = e.key;
                                    if (k == key || (key != null && key.equals(k))) {
                                        node = e;
                                        break;
                                    }
                                }
                                p = e;
                                e = e.next;
                            } while (e != null);
                        }
                    }
                    if (node != null) {
                    }
                }
            }
        }
        return null;
    }

    public void clear() {
        this.modCount++;
        Node<K, V>[] tab = this.table;
        if (tab != null && this.size > 0) {
            this.size = 0;
            for (int i = 0; i < tab.length; i++) {
                tab[i] = null;
            }
        }
    }

    public boolean containsValue(Object value) {
        Node<K, V>[] tab = this.table;
        if (tab != null && this.size > 0) {
            for (Node<K, V> e : tab) {
                for (Node<K, V> e2 = tab[i]; e2 != null; e2 = e2.next) {
                    V v = e2.value;
                    if (v == value || (value != null && value.equals(v))) {
                        return true;
                    }
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
        Set<Entry<K, V>> set = this.entrySet;
        if (set != null) {
            return set;
        }
        set = new EntrySet();
        this.entrySet = set;
        return set;
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
        Node<K, V> e = getNode(hash(key), key);
        if (e != null) {
            V v = e.value;
            if (v == oldValue || (v != null && v.equals(oldValue))) {
                e.value = newValue;
                afterNodeAccess(e);
                return true;
            }
        }
        return false;
    }

    public V replace(K key, V value) {
        Node<K, V> e = getNode(hash(key), key);
        if (e == null) {
            return null;
        }
        V oldValue = e.value;
        e.value = value;
        afterNodeAccess(e);
        return oldValue;
    }

    /* JADX WARNING: Missing block: B:19:0x0046, code:
            if (r13 == 0) goto L_0x001f;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null) {
            throw new NullPointerException();
        }
        Node<K, V>[] tab;
        int n;
        int hash = hash(key);
        int binCount = 0;
        TreeNode t = null;
        Node old = null;
        if (this.size <= this.threshold) {
            tab = this.table;
            if (tab != null) {
                n = tab.length;
            }
        }
        tab = resize();
        n = tab.length;
        int i = (n - 1) & hash;
        Node<K, V> first = tab[i];
        if (first != null) {
            if (first instanceof TreeNode) {
                t = (TreeNode) first;
                old = t.getTreeNode(hash, key);
            } else {
                Node<K, V> old2;
                Node<K, V> e = first;
                while (true) {
                    if (e.hash == hash) {
                        K k = e.key;
                        if (k == key || (key != null && key.equals(k))) {
                            old2 = e;
                        }
                    }
                    binCount++;
                    e = e.next;
                    if (e == null) {
                        break;
                    }
                }
                old2 = e;
            }
            if (old != null) {
                V oldValue = old.value;
                if (oldValue != null) {
                    afterNodeAccess(old);
                    return oldValue;
                }
            }
        }
        V v = mappingFunction.apply(key);
        if (v == null) {
            return null;
        }
        if (old != null) {
            old.value = v;
            afterNodeAccess(old);
            return v;
        }
        if (t != null) {
            t.putTreeVal(this, tab, hash, key, v);
        } else {
            tab[i] = newNode(hash, key, v, first);
            if (binCount >= 7) {
                treeifyBin(tab, hash);
            }
        }
        this.modCount++;
        this.size++;
        afterNodeInsertion(true);
        return v;
    }

    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null) {
            throw new NullPointerException();
        }
        int hash = hash(key);
        Node<K, V> e = getNode(hash, key);
        if (e != null) {
            V oldValue = e.value;
            if (oldValue != null) {
                V v = remappingFunction.apply(key, oldValue);
                if (v != null) {
                    e.value = v;
                    afterNodeAccess(e);
                    return v;
                }
                removeNode(hash, key, null, false, true);
            }
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:21:0x005d, code:
            if (r16 == 0) goto L_0x0020;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null) {
            throw new NullPointerException();
        }
        Node<K, V>[] tab;
        int n;
        int hash = hash(key);
        int binCount = 0;
        TreeNode t = null;
        Node<K, V> old = null;
        if (this.size <= this.threshold) {
            tab = this.table;
            if (tab != null) {
                n = tab.length;
            }
        }
        tab = resize();
        n = tab.length;
        int i = (n - 1) & hash;
        Node<K, V> first = tab[i];
        if (first != null) {
            if (!(first instanceof TreeNode)) {
                Node<K, V> e = first;
                while (true) {
                    if (e.hash == hash) {
                        K k = e.key;
                        if (k == key || (key != null && key.equals(k))) {
                            old = e;
                        }
                    }
                    binCount++;
                    e = e.next;
                    if (e == null) {
                        break;
                    }
                }
            } else {
                t = (TreeNode) first;
                old = t.getTreeNode(hash, key);
            }
        }
        V v = remappingFunction.apply(key, old == null ? null : old.value);
        if (old != null) {
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            } else {
                removeNode(hash, key, null, null, true);
            }
        } else if (v != null) {
            if (t != null) {
                t.putTreeVal(this, tab, hash, key, v);
            } else {
                tab[i] = newNode(hash, key, v, first);
                if (binCount >= 7) {
                    treeifyBin(tab, hash);
                }
            }
            this.modCount++;
            this.size++;
            afterNodeInsertion(true);
        }
        return v;
    }

    /* JADX WARNING: Missing block: B:24:0x006b, code:
            if (r16 == 0) goto L_0x0028;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null) {
            throw new NullPointerException();
        } else if (remappingFunction == null) {
            throw new NullPointerException();
        } else {
            Node<K, V>[] tab;
            int n;
            int hash = hash(key);
            int binCount = 0;
            TreeNode t = null;
            Node<K, V> old = null;
            if (this.size <= this.threshold) {
                tab = this.table;
                if (tab != null) {
                    n = tab.length;
                }
            }
            tab = resize();
            n = tab.length;
            int i = (n - 1) & hash;
            Node<K, V> first = tab[i];
            if (first != null) {
                if (!(first instanceof TreeNode)) {
                    Node<K, V> e = first;
                    while (true) {
                        if (e.hash == hash) {
                            K k = e.key;
                            if (k == key || (key != null && key.equals(k))) {
                                old = e;
                            }
                        }
                        binCount++;
                        e = e.next;
                        if (e == null) {
                            break;
                        }
                    }
                } else {
                    t = (TreeNode) first;
                    old = t.getTreeNode(hash, key);
                }
            }
            if (old != null) {
                V v;
                if (old.value != null) {
                    v = remappingFunction.apply(old.value, value);
                } else {
                    v = value;
                }
                if (v != null) {
                    old.value = v;
                    afterNodeAccess(old);
                } else {
                    removeNode(hash, key, null, null, true);
                }
                return v;
            }
            if (value != null) {
                if (t != null) {
                    t.putTreeVal(this, tab, hash, key, value);
                } else {
                    tab[i] = newNode(hash, key, value, first);
                    if (binCount >= 7) {
                        treeifyBin(tab, hash);
                    }
                }
                this.modCount++;
                this.size++;
                afterNodeInsertion(true);
            }
            return value;
        }
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) {
            throw new NullPointerException();
        } else if (this.size > 0) {
            Node<K, V>[] tab = this.table;
            if (tab != null) {
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
            Node<K, V>[] tab = this.table;
            if (tab != null) {
                int mc = this.modCount;
                for (Node<K, V> e : tab) {
                    for (Node<K, V> e2 = tab[i]; e2 != null; e2 = e2.next) {
                        e2.value = function.apply(e2.key, e2.value);
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
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    final float loadFactor() {
        return this.loadFactor;
    }

    final int capacity() {
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
            int cap;
            float lf = Math.min(Math.max(0.25f, this.loadFactor), 4.0f);
            float fc = (((float) mappings) / lf) + 1.0f;
            if (fc < 16.0f) {
                cap = 16;
            } else if (fc >= 1.07374182E9f) {
                cap = MAXIMUM_CAPACITY;
            } else {
                cap = tableSizeFor((int) fc);
            }
            float ft = ((float) cap) * lf;
            int i = (cap >= MAXIMUM_CAPACITY || ft >= 1.07374182E9f) ? Integer.MAX_VALUE : (int) ft;
            this.threshold = i;
            this.table = new Node[cap];
            for (int i2 = 0; i2 < mappings; i2++) {
                K key = s.readObject();
                putVal(hash(key), key, s.readObject(), false, false);
            }
        }
    }

    Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
        return new Node(hash, key, value, next);
    }

    Node<K, V> replacementNode(Node<K, V> p, Node<K, V> next) {
        return new Node(p.hash, p.key, p.value, next);
    }

    TreeNode<K, V> newTreeNode(int hash, K key, V value, Node<K, V> next) {
        return new TreeNode(hash, key, value, next);
    }

    TreeNode<K, V> replacementTreeNode(Node<K, V> p, Node<K, V> next) {
        return new TreeNode(p.hash, p.key, p.value, next);
    }

    void reinitialize() {
        this.table = null;
        this.entrySet = null;
        this.keySet = null;
        this.values = null;
        this.modCount = 0;
        this.threshold = 0;
        this.size = 0;
    }

    void afterNodeAccess(Node<K, V> node) {
    }

    void afterNodeInsertion(boolean evict) {
    }

    void afterNodeRemoval(Node<K, V> node) {
    }

    void internalWriteEntries(ObjectOutputStream s) throws IOException {
        if (this.size > 0) {
            Node<K, V>[] tab = this.table;
            if (tab != null) {
                for (Node<K, V> e : tab) {
                    for (Node<K, V> e2 = tab[i]; e2 != null; e2 = e2.next) {
                        s.writeObject(e2.key);
                        s.writeObject(e2.value);
                    }
                }
            }
        }
    }
}
