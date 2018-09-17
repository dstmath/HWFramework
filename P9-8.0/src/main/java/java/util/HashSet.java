package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class HashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, Serializable {
    private static final Object PRESENT = new Object();
    static final long serialVersionUID = -5024744406713321676L;
    private transient HashMap<E, Object> map;

    public HashSet() {
        this.map = new HashMap();
    }

    public HashSet(Collection<? extends E> c) {
        this.map = new HashMap(Math.max(((int) (((float) c.size()) / 0.75f)) + 1, 16));
        addAll(c);
    }

    public HashSet(int initialCapacity, float loadFactor) {
        this.map = new HashMap(initialCapacity, loadFactor);
    }

    public HashSet(int initialCapacity) {
        this.map = new HashMap(initialCapacity);
    }

    HashSet(int initialCapacity, float loadFactor, boolean dummy) {
        this.map = new LinkedHashMap(initialCapacity, loadFactor);
    }

    public Iterator<E> iterator() {
        return this.map.keySet().iterator();
    }

    public int size() {
        return this.map.size();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public boolean contains(Object o) {
        return this.map.containsKey(o);
    }

    public boolean add(E e) {
        return this.map.put(e, PRESENT) == null;
    }

    public boolean remove(Object o) {
        return this.map.remove(o) == PRESENT;
    }

    public void clear() {
        this.map.clear();
    }

    public Object clone() {
        try {
            HashSet<E> newSet = (HashSet) super.clone();
            newSet.map = (HashMap) this.map.clone();
            return newSet;
        } catch (Throwable e) {
            throw new InternalError(e);
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(this.map.capacity());
        s.writeFloat(this.map.loadFactor());
        s.writeInt(this.map.size());
        for (E e : this.map.keySet()) {
            s.writeObject(e);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        int capacity = s.readInt();
        if (capacity < 0) {
            throw new InvalidObjectException("Illegal capacity: " + capacity);
        }
        float loadFactor = s.readFloat();
        if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new InvalidObjectException("Illegal load factor: " + loadFactor);
        }
        int size = s.readInt();
        if (size < 0) {
            throw new InvalidObjectException("Illegal size: " + size);
        }
        HashMap linkedHashMap;
        capacity = (int) Math.min(((float) size) * Math.min(1.0f / loadFactor, 4.0f), 1.07374182E9f);
        if (this instanceof LinkedHashSet) {
            linkedHashMap = new LinkedHashMap(capacity, loadFactor);
        } else {
            linkedHashMap = new HashMap(capacity, loadFactor);
        }
        this.map = linkedHashMap;
        for (int i = 0; i < size; i++) {
            this.map.put(s.readObject(), PRESENT);
        }
    }

    public Spliterator<E> spliterator() {
        return new KeySpliterator(this.map, 0, -1, 0, 0);
    }
}
