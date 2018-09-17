package java.util;

class JumboEnumSet<E extends Enum<E>> extends EnumSet<E> {
    private static final long serialVersionUID = 334349849919042784L;
    private long[] elements;
    private int size = 0;

    private class EnumSetIterator<E extends Enum<E>> implements Iterator<E> {
        long lastReturned = 0;
        int lastReturnedIndex = 0;
        long unseen;
        int unseenIndex = 0;

        EnumSetIterator() {
            this.unseen = JumboEnumSet.this.elements[0];
        }

        public boolean hasNext() {
            while (this.unseen == 0 && this.unseenIndex < JumboEnumSet.this.elements.length - 1) {
                long[] -get0 = JumboEnumSet.this.elements;
                int i = this.unseenIndex + 1;
                this.unseenIndex = i;
                this.unseen = -get0[i];
            }
            return this.unseen != 0;
        }

        public E next() {
            if (hasNext()) {
                this.lastReturned = this.unseen & (-this.unseen);
                this.lastReturnedIndex = this.unseenIndex;
                this.unseen -= this.lastReturned;
                return JumboEnumSet.this.universe[(this.lastReturnedIndex << 6) + Long.numberOfTrailingZeros(this.lastReturned)];
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            if (this.lastReturned == 0) {
                throw new IllegalStateException();
            }
            long oldElements = JumboEnumSet.this.elements[this.lastReturnedIndex];
            long[] -get0 = JumboEnumSet.this.elements;
            int i = this.lastReturnedIndex;
            -get0[i] = -get0[i] & (~this.lastReturned);
            if (oldElements != JumboEnumSet.this.elements[this.lastReturnedIndex]) {
                JumboEnumSet jumboEnumSet = JumboEnumSet.this;
                jumboEnumSet.size = jumboEnumSet.size - 1;
            }
            this.lastReturned = 0;
        }
    }

    JumboEnumSet(Class<E> elementType, Enum<?>[] universe) {
        super(elementType, universe);
        this.elements = new long[((universe.length + 63) >>> 6)];
    }

    void addRange(E from, E to) {
        int fromIndex = from.ordinal() >>> 6;
        int toIndex = to.ordinal() >>> 6;
        if (fromIndex == toIndex) {
            this.elements[fromIndex] = (-1 >>> ((from.ordinal() - to.ordinal()) - 1)) << from.ordinal();
        } else {
            this.elements[fromIndex] = -1 << from.ordinal();
            for (int i = fromIndex + 1; i < toIndex; i++) {
                this.elements[i] = -1;
            }
            this.elements[toIndex] = -1 >>> (63 - to.ordinal());
        }
        this.size = (to.ordinal() - from.ordinal()) + 1;
    }

    void addAll() {
        for (int i = 0; i < this.elements.length; i++) {
            this.elements[i] = -1;
        }
        long[] jArr = this.elements;
        int length = this.elements.length - 1;
        jArr[length] = jArr[length] >>> (-this.universe.length);
        this.size = this.universe.length;
    }

    void complement() {
        for (int i = 0; i < this.elements.length; i++) {
            this.elements[i] = ~this.elements[i];
        }
        long[] jArr = this.elements;
        int length = this.elements.length - 1;
        jArr[length] = jArr[length] & (-1 >>> (-this.universe.length));
        this.size = this.universe.length - this.size;
    }

    public Iterator<E> iterator() {
        return new EnumSetIterator();
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public boolean contains(Object e) {
        boolean z = false;
        if (e == null) {
            return false;
        }
        Class<?> eClass = e.getClass();
        if (eClass != this.elementType && eClass.getSuperclass() != this.elementType) {
            return false;
        }
        int eOrdinal = ((Enum) e).ordinal();
        if ((this.elements[eOrdinal >>> 6] & (1 << eOrdinal)) != 0) {
            z = true;
        }
        return z;
    }

    public boolean add(E e) {
        typeCheck(e);
        int eOrdinal = e.ordinal();
        int eWordNum = eOrdinal >>> 6;
        long oldElements = this.elements[eWordNum];
        long[] jArr = this.elements;
        jArr[eWordNum] = jArr[eWordNum] | (1 << eOrdinal);
        boolean result = this.elements[eWordNum] != oldElements;
        if (result) {
            this.size++;
        }
        return result;
    }

    public boolean remove(Object e) {
        if (e == null) {
            return false;
        }
        Class<?> eClass = e.getClass();
        if (eClass != this.elementType && eClass.getSuperclass() != this.elementType) {
            return false;
        }
        int eOrdinal = ((Enum) e).ordinal();
        int eWordNum = eOrdinal >>> 6;
        long oldElements = this.elements[eWordNum];
        long[] jArr = this.elements;
        jArr[eWordNum] = jArr[eWordNum] & (~(1 << eOrdinal));
        boolean result = this.elements[eWordNum] != oldElements;
        if (result) {
            this.size--;
        }
        return result;
    }

    public boolean containsAll(Collection<?> c) {
        if (!(c instanceof JumboEnumSet)) {
            return super.containsAll(c);
        }
        JumboEnumSet<?> es = (JumboEnumSet) c;
        if (es.elementType != this.elementType) {
            return es.isEmpty();
        }
        for (int i = 0; i < this.elements.length; i++) {
            if ((es.elements[i] & (~this.elements[i])) != 0) {
                return false;
            }
        }
        return true;
    }

    public boolean addAll(Collection<? extends E> c) {
        if (!(c instanceof JumboEnumSet)) {
            return super.addAll(c);
        }
        JumboEnumSet<?> es = (JumboEnumSet) c;
        if (es.elementType == this.elementType) {
            for (int i = 0; i < this.elements.length; i++) {
                long[] jArr = this.elements;
                jArr[i] = jArr[i] | es.elements[i];
            }
            return recalculateSize();
        } else if (es.isEmpty()) {
            return false;
        } else {
            throw new ClassCastException(es.elementType + " != " + this.elementType);
        }
    }

    public boolean removeAll(Collection<?> c) {
        if (!(c instanceof JumboEnumSet)) {
            return super.removeAll(c);
        }
        JumboEnumSet<?> es = (JumboEnumSet) c;
        if (es.elementType != this.elementType) {
            return false;
        }
        for (int i = 0; i < this.elements.length; i++) {
            long[] jArr = this.elements;
            jArr[i] = jArr[i] & (~es.elements[i]);
        }
        return recalculateSize();
    }

    public boolean retainAll(Collection<?> c) {
        if (!(c instanceof JumboEnumSet)) {
            return super.retainAll(c);
        }
        JumboEnumSet<?> es = (JumboEnumSet) c;
        if (es.elementType != this.elementType) {
            boolean changed = this.size != 0;
            clear();
            return changed;
        }
        for (int i = 0; i < this.elements.length; i++) {
            long[] jArr = this.elements;
            jArr[i] = jArr[i] & es.elements[i];
        }
        return recalculateSize();
    }

    public void clear() {
        Arrays.fill(this.elements, 0);
        this.size = 0;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof JumboEnumSet)) {
            return super.equals(o);
        }
        JumboEnumSet<?> es = (JumboEnumSet) o;
        if (es.elementType == this.elementType) {
            return Arrays.equals(es.elements, this.elements);
        }
        if (this.size == 0 && es.size == 0) {
            z = true;
        }
        return z;
    }

    private boolean recalculateSize() {
        int oldSize = this.size;
        this.size = 0;
        for (long elt : this.elements) {
            this.size += Long.bitCount(elt);
        }
        if (this.size != oldSize) {
            return true;
        }
        return false;
    }

    public EnumSet<E> clone() {
        JumboEnumSet<E> result = (JumboEnumSet) super.clone();
        result.elements = (long[]) result.elements.clone();
        return result;
    }
}
