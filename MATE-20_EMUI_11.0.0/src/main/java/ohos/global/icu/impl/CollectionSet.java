package ohos.global.icu.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class CollectionSet<E> implements Set<E> {
    private final Collection<E> data;

    public CollectionSet(Collection<E> collection) {
        this.data = collection;
    }

    @Override // java.util.Set, java.util.Collection
    public int size() {
        return this.data.size();
    }

    @Override // java.util.Set, java.util.Collection
    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    @Override // java.util.Set, java.util.Collection
    public boolean contains(Object obj) {
        return this.data.contains(obj);
    }

    @Override // java.util.Set, java.util.Collection, java.lang.Iterable
    public Iterator<E> iterator() {
        return this.data.iterator();
    }

    @Override // java.util.Set, java.util.Collection
    public Object[] toArray() {
        return this.data.toArray();
    }

    @Override // java.util.Set, java.util.Collection
    public <T> T[] toArray(T[] tArr) {
        return (T[]) this.data.toArray(tArr);
    }

    @Override // java.util.Set, java.util.Collection
    public boolean add(E e) {
        return this.data.add(e);
    }

    @Override // java.util.Set, java.util.Collection
    public boolean remove(Object obj) {
        return this.data.remove(obj);
    }

    @Override // java.util.Set, java.util.Collection
    public boolean containsAll(Collection<?> collection) {
        return this.data.containsAll(collection);
    }

    @Override // java.util.Set, java.util.Collection
    public boolean addAll(Collection<? extends E> collection) {
        return this.data.addAll(collection);
    }

    @Override // java.util.Set, java.util.Collection
    public boolean retainAll(Collection<?> collection) {
        return this.data.retainAll(collection);
    }

    @Override // java.util.Set, java.util.Collection
    public boolean removeAll(Collection<?> collection) {
        return this.data.removeAll(collection);
    }

    @Override // java.util.Set, java.util.Collection
    public void clear() {
        this.data.clear();
    }
}
