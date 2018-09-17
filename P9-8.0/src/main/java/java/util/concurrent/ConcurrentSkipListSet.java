package java.util.concurrent;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import sun.misc.Unsafe;

public class ConcurrentSkipListSet<E> extends AbstractSet<E> implements NavigableSet<E>, Cloneable, Serializable {
    private static final long MAP;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = -2479143111061671589L;
    private final ConcurrentNavigableMap<E, Object> m;

    public ConcurrentSkipListSet() {
        this.m = new ConcurrentSkipListMap();
    }

    public ConcurrentSkipListSet(Comparator<? super E> comparator) {
        this.m = new ConcurrentSkipListMap((Comparator) comparator);
    }

    public ConcurrentSkipListSet(Collection<? extends E> c) {
        this.m = new ConcurrentSkipListMap();
        addAll(c);
    }

    public ConcurrentSkipListSet(SortedSet<E> s) {
        this.m = new ConcurrentSkipListMap(s.comparator());
        addAll(s);
    }

    ConcurrentSkipListSet(ConcurrentNavigableMap<E, Object> m) {
        this.m = m;
    }

    public ConcurrentSkipListSet<E> clone() {
        try {
            ConcurrentSkipListSet<E> clone = (ConcurrentSkipListSet) super.clone();
            clone.setMap(new ConcurrentSkipListMap(this.m));
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
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

    public boolean add(E e) {
        return this.m.putIfAbsent(e, Boolean.TRUE) == null;
    }

    public boolean remove(Object o) {
        return this.m.remove(o, Boolean.TRUE);
    }

    public void clear() {
        this.m.clear();
    }

    public Iterator<E> iterator() {
        return this.m.navigableKeySet().iterator();
    }

    public Iterator<E> descendingIterator() {
        return this.m.descendingKeySet().iterator();
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

    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object e : c) {
            if (remove(e)) {
                modified = true;
            }
        }
        return modified;
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

    public E pollFirst() {
        Entry<E, Object> e = this.m.pollFirstEntry();
        if (e == null) {
            return null;
        }
        return e.getKey();
    }

    public E pollLast() {
        Entry<E, Object> e = this.m.pollLastEntry();
        if (e == null) {
            return null;
        }
        return e.getKey();
    }

    public Comparator<? super E> comparator() {
        return this.m.comparator();
    }

    public E first() {
        return this.m.firstKey();
    }

    public E last() {
        return this.m.lastKey();
    }

    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new ConcurrentSkipListSet(this.m.subMap((Object) fromElement, fromInclusive, (Object) toElement, toInclusive));
    }

    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new ConcurrentSkipListSet(this.m.headMap((Object) toElement, inclusive));
    }

    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new ConcurrentSkipListSet(this.m.tailMap((Object) fromElement, inclusive));
    }

    public NavigableSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    public NavigableSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    public NavigableSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    public NavigableSet<E> descendingSet() {
        return new ConcurrentSkipListSet(this.m.descendingMap());
    }

    public Spliterator<E> spliterator() {
        if (this.m instanceof ConcurrentSkipListMap) {
            return ((ConcurrentSkipListMap) this.m).keySpliterator();
        }
        SubMap subMap = (SubMap) this.m;
        subMap.getClass();
        return new SubMapKeyIterator();
    }

    private void setMap(ConcurrentNavigableMap<E, Object> map) {
        U.putObjectVolatile(this, MAP, map);
    }

    static {
        try {
            MAP = U.objectFieldOffset(ConcurrentSkipListSet.class.getDeclaredField("m"));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
