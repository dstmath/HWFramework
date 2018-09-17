package java.util.concurrent;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CopyOnWriteArraySet<E> extends AbstractSet<E> implements Serializable {
    private static final long serialVersionUID = 5457747651344034263L;
    private final CopyOnWriteArrayList<E> al;

    public CopyOnWriteArraySet() {
        this.al = new CopyOnWriteArrayList();
    }

    public CopyOnWriteArraySet(Collection<? extends E> c) {
        if (c.getClass() == CopyOnWriteArraySet.class) {
            this.al = new CopyOnWriteArrayList(((CopyOnWriteArraySet) c).al);
            return;
        }
        this.al = new CopyOnWriteArrayList();
        this.al.addAllAbsent(c);
    }

    public int size() {
        return this.al.size();
    }

    public boolean isEmpty() {
        return this.al.isEmpty();
    }

    public boolean contains(Object o) {
        return this.al.contains(o);
    }

    public Object[] toArray() {
        return this.al.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return this.al.toArray(a);
    }

    public void clear() {
        this.al.clear();
    }

    public boolean remove(Object o) {
        return this.al.remove(o);
    }

    public boolean add(E e) {
        return this.al.addIfAbsent(e);
    }

    public boolean containsAll(Collection<?> c) {
        if (!(c instanceof Set)) {
            return this.al.containsAll(c);
        }
        if (compareSets(this.al.getArray(), (Set) c) >= 0) {
            return true;
        }
        return false;
    }

    private static int compareSets(Object[] snapshot, Set<?> set) {
        int i = 1;
        int len = snapshot.length;
        boolean[] matched = new boolean[len];
        int j = 0;
        for (Object x : set) {
            int i2 = j;
            while (i2 < len) {
                if (matched[i2] || !Objects.equals(x, snapshot[i2])) {
                    i2++;
                } else {
                    matched[i2] = true;
                    if (i2 == j) {
                        while (true) {
                            j++;
                            if (j >= len || !matched[j]) {
                                break;
                            }
                        }
                    }
                }
            }
            return -1;
        }
        if (j == len) {
            i = 0;
        }
        return i;
    }

    public boolean addAll(Collection<? extends E> c) {
        return this.al.addAllAbsent(c) > 0;
    }

    public boolean removeAll(Collection<?> c) {
        return this.al.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return this.al.retainAll(c);
    }

    public Iterator<E> iterator() {
        return this.al.iterator();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Set) {
            return compareSets(this.al.getArray(), (Set) o) == 0;
        } else {
            return false;
        }
    }

    public boolean removeIf(Predicate<? super E> filter) {
        return this.al.removeIf(filter);
    }

    public void forEach(Consumer<? super E> action) {
        this.al.forEach(action);
    }

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this.al.getArray(), 1025);
    }
}
