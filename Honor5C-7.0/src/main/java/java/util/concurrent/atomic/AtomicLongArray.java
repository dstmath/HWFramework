package java.util.concurrent.atomic;

import java.io.Serializable;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;
import sun.misc.Unsafe;

public class AtomicLongArray implements Serializable {
    private static final int ABASE = 0;
    private static final int ASHIFT = 0;
    private static final Unsafe U = null;
    private static final long serialVersionUID = -2308431214976778248L;
    private final long[] array;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.atomic.AtomicLongArray.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.atomic.AtomicLongArray.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.atomic.AtomicLongArray.<clinit>():void");
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

    public AtomicLongArray(int length) {
        this.array = new long[length];
    }

    public AtomicLongArray(long[] array) {
        this.array = (long[]) array.clone();
    }

    public final int length() {
        return this.array.length;
    }

    public final long get(int i) {
        return getRaw(checkedByteOffset(i));
    }

    private long getRaw(long offset) {
        return U.getLongVolatile(this.array, offset);
    }

    public final void set(int i, long newValue) {
        U.putLongVolatile(this.array, checkedByteOffset(i), newValue);
    }

    public final void lazySet(int i, long newValue) {
        U.putOrderedLong(this.array, checkedByteOffset(i), newValue);
    }

    public final long getAndSet(int i, long newValue) {
        return U.getAndSetLong(this.array, checkedByteOffset(i), newValue);
    }

    public final boolean compareAndSet(int i, long expect, long update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }

    private boolean compareAndSetRaw(long offset, long expect, long update) {
        return U.compareAndSwapLong(this.array, offset, expect, update);
    }

    public final boolean weakCompareAndSet(int i, long expect, long update) {
        return compareAndSet(i, expect, update);
    }

    public final long getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    public final long getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    public final long getAndAdd(int i, long delta) {
        return U.getAndAddLong(this.array, checkedByteOffset(i), delta);
    }

    public final long incrementAndGet(int i) {
        return getAndAdd(i, 1) + 1;
    }

    public final long decrementAndGet(int i) {
        return getAndAdd(i, -1) - 1;
    }

    public long addAndGet(int i, long delta) {
        return getAndAdd(i, delta) + delta;
    }

    public final long getAndUpdate(int i, LongUnaryOperator updateFunction) {
        long prev;
        long offset = checkedByteOffset(i);
        do {
            prev = getRaw(offset);
        } while (!compareAndSetRaw(offset, prev, updateFunction.applyAsLong(prev)));
        return prev;
    }

    public final long updateAndGet(int i, LongUnaryOperator updateFunction) {
        long next;
        long offset = checkedByteOffset(i);
        long prev;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    public final long getAndAccumulate(int i, long x, LongBinaryOperator accumulatorFunction) {
        long prev;
        long offset = checkedByteOffset(i);
        do {
            prev = getRaw(offset);
        } while (!compareAndSetRaw(offset, prev, accumulatorFunction.applyAsLong(prev, x)));
        return prev;
    }

    public final long accumulateAndGet(int i, long x, LongBinaryOperator accumulatorFunction) {
        long next;
        long offset = checkedByteOffset(i);
        long prev;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsLong(prev, x);
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
}
