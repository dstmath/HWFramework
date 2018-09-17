package java.util;

class RegularEnumSet<E extends Enum<E>> extends EnumSet<E> {
    private static final long serialVersionUID = 3411599620347842686L;
    private long elements;

    private class EnumSetIterator<E extends Enum<E>> implements Iterator<E> {
        long lastReturned;
        long unseen;

        public void remove() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.RegularEnumSet.EnumSetIterator.remove():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 6 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.RegularEnumSet.EnumSetIterator.remove():void");
        }

        EnumSetIterator() {
            this.lastReturned = 0;
            this.unseen = RegularEnumSet.this.elements;
        }

        public boolean hasNext() {
            return this.unseen != 0;
        }

        public E next() {
            if (this.unseen == 0) {
                throw new NoSuchElementException();
            }
            this.lastReturned = this.unseen & (-this.unseen);
            this.unseen -= this.lastReturned;
            return RegularEnumSet.this.universe[Long.numberOfTrailingZeros(this.lastReturned)];
        }
    }

    void complement() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.RegularEnumSet.complement():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.RegularEnumSet.complement():void");
    }

    public boolean containsAll(java.util.Collection<?> r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.RegularEnumSet.containsAll(java.util.Collection):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.RegularEnumSet.containsAll(java.util.Collection):boolean");
    }

    public boolean remove(java.lang.Object r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.RegularEnumSet.remove(java.lang.Object):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.RegularEnumSet.remove(java.lang.Object):boolean");
    }

    public boolean removeAll(java.util.Collection<?> r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.RegularEnumSet.removeAll(java.util.Collection):boolean
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-long
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:568)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:99)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.RegularEnumSet.removeAll(java.util.Collection):boolean");
    }

    RegularEnumSet(Class<E> elementType, Enum[] universe) {
        super(elementType, universe);
        this.elements = 0;
    }

    void addRange(E from, E to) {
        this.elements = (-1 >>> ((from.ordinal() - to.ordinal()) - 1)) << from.ordinal();
    }

    void addAll() {
        if (this.universe.length != 0) {
            this.elements = -1 >>> (-this.universe.length);
        }
    }

    public Iterator<E> iterator() {
        return new EnumSetIterator();
    }

    public int size() {
        return Long.bitCount(this.elements);
    }

    public boolean isEmpty() {
        return this.elements == 0;
    }

    public boolean contains(Object e) {
        boolean z = false;
        if (e == null) {
            return false;
        }
        Class eClass = e.getClass();
        if (eClass != this.elementType && eClass.getSuperclass() != this.elementType) {
            return false;
        }
        if ((this.elements & (1 << ((Enum) e).ordinal())) != 0) {
            z = true;
        }
        return z;
    }

    public boolean add(E e) {
        typeCheck(e);
        long oldElements = this.elements;
        this.elements |= 1 << e.ordinal();
        return this.elements != oldElements;
    }

    public boolean addAll(Collection<? extends E> c) {
        boolean z = false;
        if (!(c instanceof RegularEnumSet)) {
            return super.addAll(c);
        }
        RegularEnumSet es = (RegularEnumSet) c;
        if (es.elementType == this.elementType) {
            long oldElements = this.elements;
            this.elements |= es.elements;
            if (this.elements != oldElements) {
                z = true;
            }
            return z;
        } else if (es.isEmpty()) {
            return false;
        } else {
            throw new ClassCastException(es.elementType + " != " + this.elementType);
        }
    }

    public boolean retainAll(Collection<?> c) {
        if (!(c instanceof RegularEnumSet)) {
            return super.retainAll(c);
        }
        RegularEnumSet<?> es = (RegularEnumSet) c;
        if (es.elementType != this.elementType) {
            boolean changed = this.elements != 0;
            this.elements = 0;
            return changed;
        }
        long oldElements = this.elements;
        this.elements &= es.elements;
        return this.elements != oldElements;
    }

    public void clear() {
        this.elements = 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (!(o instanceof RegularEnumSet)) {
            return super.equals(o);
        }
        RegularEnumSet es = (RegularEnumSet) o;
        if (es.elementType != this.elementType) {
            if (!(this.elements == 0 && es.elements == 0)) {
                z = false;
            }
            return z;
        }
        if (es.elements != this.elements) {
            z = false;
        }
        return z;
    }
}
