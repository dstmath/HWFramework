package java.util;

class JumboEnumSet<E extends Enum<E>> extends EnumSet<E> {
    private static final long serialVersionUID = 334349849919042784L;
    private long[] elements;
    private int size;

    private class EnumSetIterator<E extends Enum<E>> implements Iterator<E> {
        long lastReturned;
        int lastReturnedIndex;
        long unseen;
        int unseenIndex;

        public void remove() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.JumboEnumSet.EnumSetIterator.remove():void
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
            throw new UnsupportedOperationException("Method not decompiled: java.util.JumboEnumSet.EnumSetIterator.remove():void");
        }

        EnumSetIterator() {
            this.unseenIndex = 0;
            this.lastReturned = 0;
            this.lastReturnedIndex = 0;
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
    }

    void complement() {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.JumboEnumSet.complement():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.JumboEnumSet.complement():void");
    }

    public boolean containsAll(java.util.Collection<?> r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.JumboEnumSet.containsAll(java.util.Collection):boolean
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.JumboEnumSet.containsAll(java.util.Collection):boolean");
    }

    public boolean remove(java.lang.Object r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.JumboEnumSet.remove(java.lang.Object):boolean
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.JumboEnumSet.remove(java.lang.Object):boolean");
    }

    public boolean removeAll(java.util.Collection<?> r1) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.JumboEnumSet.removeAll(java.util.Collection):boolean
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.JumboEnumSet.removeAll(java.util.Collection):boolean");
    }

    JumboEnumSet(Class<E> elementType, Enum[] universe) {
        super(elementType, universe);
        this.size = 0;
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
        Class eClass = e.getClass();
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

    public boolean addAll(Collection<? extends E> c) {
        if (!(c instanceof JumboEnumSet)) {
            return super.addAll(c);
        }
        JumboEnumSet es = (JumboEnumSet) c;
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
        JumboEnumSet es = (JumboEnumSet) o;
        if (es.elementType == this.elementType) {
            return Arrays.equals(es.elements, this.elements);
        }
        if (this.size == 0 && es.size == 0) {
            z = true;
        }
        return z;
    }

    private boolean recalculateSize() {
        /* JADX: method processing error */
/*
        Error: java.lang.IndexOutOfBoundsException: bitIndex < 0: -1
	at java.util.BitSet.get(BitSet.java:623)
	at jadx.core.dex.visitors.CodeShrinker$ArgsInfo.usedArgAssign(CodeShrinker.java:139)
	at jadx.core.dex.visitors.CodeShrinker$ArgsInfo.access$300(CodeShrinker.java:44)
	at jadx.core.dex.visitors.CodeShrinker.canMoveBetweenBlocks(CodeShrinker.java:306)
	at jadx.core.dex.visitors.CodeShrinker.shrinkBlock(CodeShrinker.java:229)
	at jadx.core.dex.visitors.CodeShrinker.shrinkMethod(CodeShrinker.java:40)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.checkArrayForEach(LoopRegionVisitor.java:195)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.checkForIndexedLoop(LoopRegionVisitor.java:118)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.processLoopRegion(LoopRegionVisitor.java:64)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.enterRegion(LoopRegionVisitor.java:52)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:56)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverseInternal(DepthRegionTraversal.java:58)
	at jadx.core.dex.visitors.regions.DepthRegionTraversal.traverse(DepthRegionTraversal.java:18)
	at jadx.core.dex.visitors.regions.LoopRegionVisitor.visit(LoopRegionVisitor.java:46)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r9 = this;
        r3 = 0;
        r2 = r9.size;
        r9.size = r3;
        r5 = r9.elements;
        r6 = r5.length;
        r4 = r3;
    L_0x0009:
        if (r4 >= r6) goto L_0x0019;
    L_0x000b:
        r0 = r5[r4];
        r7 = r9.size;
        r8 = java.lang.Long.bitCount(r0);
        r7 = r7 + r8;
        r9.size = r7;
        r4 = r4 + 1;
        goto L_0x0009;
    L_0x0019:
        r4 = r9.size;
        if (r4 == r2) goto L_0x001e;
    L_0x001d:
        r3 = 1;
    L_0x001e:
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.JumboEnumSet.recalculateSize():boolean");
    }

    public EnumSet<E> clone() {
        JumboEnumSet<E> result = (JumboEnumSet) super.clone();
        result.elements = (long[]) result.elements.clone();
        return result;
    }
}
