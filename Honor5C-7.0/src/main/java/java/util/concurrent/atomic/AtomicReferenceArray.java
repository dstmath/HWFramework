package java.util.concurrent.atomic;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import sun.misc.Unsafe;

public class AtomicReferenceArray<E> implements Serializable {
    private static final int ABASE = 0;
    private static final long ARRAY = 0;
    private static final int ASHIFT = 0;
    private static final Unsafe U = null;
    private static final long serialVersionUID = -6209656149925076980L;
    private final Object[] array;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.atomic.AtomicReferenceArray.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.atomic.AtomicReferenceArray.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.atomic.AtomicReferenceArray.<clinit>():void");
    }

    private long checkedByteOffset(int i) {
        if (i >= 0 && i < this.array.length) {
            return byteOffset(i);
        }
        throw new IndexOutOfBoundsException("index " + i);
    }

    private static long byteOffset(int i) {
        return (((long) i) << ASHIFT) + ((long) ABASE);
    }

    public AtomicReferenceArray(int length) {
        this.array = new Object[length];
    }

    public AtomicReferenceArray(E[] array) {
        this.array = Arrays.copyOf(array, array.length, Object[].class);
    }

    public final int length() {
        return this.array.length;
    }

    public final E get(int i) {
        return getRaw(checkedByteOffset(i));
    }

    private E getRaw(long offset) {
        return U.getObjectVolatile(this.array, offset);
    }

    public final void set(int i, E newValue) {
        U.putObjectVolatile(this.array, checkedByteOffset(i), newValue);
    }

    public final void lazySet(int i, E newValue) {
        U.putOrderedObject(this.array, checkedByteOffset(i), newValue);
    }

    public final E getAndSet(int i, E newValue) {
        return U.getAndSetObject(this.array, checkedByteOffset(i), newValue);
    }

    public final boolean compareAndSet(int i, E expect, E update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }

    private boolean compareAndSetRaw(long offset, E expect, E update) {
        return U.compareAndSwapObject(this.array, offset, expect, update);
    }

    public final boolean weakCompareAndSet(int i, E expect, E update) {
        return compareAndSet(i, expect, update);
    }

    public final E getAndUpdate(int i, UnaryOperator<E> updateFunction) {
        E prev;
        long offset = checkedByteOffset(i);
        do {
            prev = getRaw(offset);
        } while (!compareAndSetRaw(offset, prev, updateFunction.apply(prev)));
        return prev;
    }

    public final E updateAndGet(int i, UnaryOperator<E> updateFunction) {
        E next;
        long offset = checkedByteOffset(i);
        E prev;
        do {
            prev = getRaw(offset);
            next = updateFunction.apply(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    public final E getAndAccumulate(int i, E x, BinaryOperator<E> accumulatorFunction) {
        E prev;
        long offset = checkedByteOffset(i);
        do {
            prev = getRaw(offset);
        } while (!compareAndSetRaw(offset, prev, accumulatorFunction.apply(prev, x)));
        return prev;
    }

    public final E accumulateAndGet(int i, E x, BinaryOperator<E> accumulatorFunction) {
        E next;
        long offset = checkedByteOffset(i);
        E prev;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.apply(prev, x);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    public String toString() {
        int iMax = this.array.length - 1;
        if (iMax == -1) {
            return "[]";
        }
        StringBuilder b = new StringBuilder();
        b.append('[');
        int i = ASHIFT;
        while (true) {
            b.append(getRaw(byteOffset(i)));
            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(',').append(' ');
            i++;
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        Object a = s.readFields().get("array", null);
        if (a == null || !a.getClass().isArray()) {
            throw new InvalidObjectException("Not array type");
        }
        if (a.getClass() != Object[].class) {
            a = Arrays.copyOf((Object[]) a, Array.getLength(a), Object[].class);
        }
        U.putObjectVolatile(this, ARRAY, a);
    }
}
