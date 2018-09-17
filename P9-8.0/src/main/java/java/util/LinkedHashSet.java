package java.util;

import java.io.Serializable;

public class LinkedHashSet<E> extends HashSet<E> implements Set<E>, Cloneable, Serializable {
    private static final long serialVersionUID = -2851667679971038690L;

    public LinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
    }

    public LinkedHashSet(int initialCapacity) {
        super(initialCapacity, 0.75f, true);
    }

    public LinkedHashSet() {
        super(16, 0.75f, true);
    }

    public LinkedHashSet(Collection<? extends E> c) {
        super(Math.max(c.size() * 2, 11), 0.75f, true);
        addAll(c);
    }

    public Spliterator<E> spliterator() {
        return Spliterators.spliterator((Collection) this, 17);
    }
}
