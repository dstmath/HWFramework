package java.util.concurrent.atomic;

import java.io.Serializable;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import sun.misc.Unsafe;

public class AtomicIntegerArray implements Serializable {
    private static final int ABASE = U.arrayBaseOffset(int[].class);
    private static final int ASHIFT;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long serialVersionUID = 2862133569453604235L;
    private final int[] array;

    static {
        int scale = U.arrayIndexScale(int[].class);
        if (((scale - 1) & scale) != 0) {
            throw new Error("array index scale not a power of two");
        }
        ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
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

    public AtomicIntegerArray(int length) {
        this.array = new int[length];
    }

    public AtomicIntegerArray(int[] array) {
        this.array = (int[]) array.clone();
    }

    public final int length() {
        return this.array.length;
    }

    public final int get(int i) {
        return getRaw(checkedByteOffset(i));
    }

    private int getRaw(long offset) {
        return U.getIntVolatile(this.array, offset);
    }

    public final void set(int i, int newValue) {
        U.putIntVolatile(this.array, checkedByteOffset(i), newValue);
    }

    public final void lazySet(int i, int newValue) {
        U.putOrderedInt(this.array, checkedByteOffset(i), newValue);
    }

    public final int getAndSet(int i, int newValue) {
        return U.getAndSetInt(this.array, checkedByteOffset(i), newValue);
    }

    public final boolean compareAndSet(int i, int expect, int update) {
        return compareAndSetRaw(checkedByteOffset(i), expect, update);
    }

    private boolean compareAndSetRaw(long offset, int expect, int update) {
        return U.compareAndSwapInt(this.array, offset, expect, update);
    }

    public final boolean weakCompareAndSet(int i, int expect, int update) {
        return compareAndSet(i, expect, update);
    }

    public final int getAndIncrement(int i) {
        return getAndAdd(i, 1);
    }

    public final int getAndDecrement(int i) {
        return getAndAdd(i, -1);
    }

    public final int getAndAdd(int i, int delta) {
        return U.getAndAddInt(this.array, checkedByteOffset(i), delta);
    }

    public final int incrementAndGet(int i) {
        return getAndAdd(i, 1) + 1;
    }

    public final int decrementAndGet(int i) {
        return getAndAdd(i, -1) - 1;
    }

    public final int addAndGet(int i, int delta) {
        return getAndAdd(i, delta) + delta;
    }

    public final int getAndUpdate(int i, IntUnaryOperator updateFunction) {
        int prev;
        long offset = checkedByteOffset(i);
        do {
            prev = getRaw(offset);
        } while (!compareAndSetRaw(offset, prev, updateFunction.applyAsInt(prev)));
        return prev;
    }

    public final int updateAndGet(int i, IntUnaryOperator updateFunction) {
        int next;
        long offset = checkedByteOffset(i);
        int prev;
        do {
            prev = getRaw(offset);
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSetRaw(offset, prev, next));
        return next;
    }

    public final int getAndAccumulate(int i, int x, IntBinaryOperator accumulatorFunction) {
        int prev;
        long offset = checkedByteOffset(i);
        do {
            prev = getRaw(offset);
        } while (!compareAndSetRaw(offset, prev, accumulatorFunction.applyAsInt(prev, x)));
        return prev;
    }

    public final int accumulateAndGet(int i, int x, IntBinaryOperator accumulatorFunction) {
        int next;
        long offset = checkedByteOffset(i);
        int prev;
        do {
            prev = getRaw(offset);
            next = accumulatorFunction.applyAsInt(prev, x);
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
        int i = 0;
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
