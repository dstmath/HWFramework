package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class HashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, Serializable {
    private static final Object PRESENT = null;
    static final long serialVersionUID = -5024744406713321676L;
    private transient HashMap<E, Object> map;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.HashSet.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.HashSet.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.HashSet.<clinit>():void");
    }

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
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
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
        HashMap linkedHashMap;
        s.defaultReadObject();
        int capacity = s.readInt();
        float loadFactor = s.readFloat();
        if (this instanceof LinkedHashSet) {
            linkedHashMap = new LinkedHashMap(capacity, loadFactor);
        } else {
            linkedHashMap = new HashMap(capacity, loadFactor);
        }
        this.map = linkedHashMap;
        int size = s.readInt();
        for (int i = 0; i < size; i++) {
            this.map.put(s.readObject(), PRESENT);
        }
    }

    public Spliterator<E> spliterator() {
        return new KeySpliterator(this.map, 0, -1, 0, 0);
    }
}
