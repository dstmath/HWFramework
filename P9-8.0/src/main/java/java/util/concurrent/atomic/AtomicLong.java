package java.util.concurrent.atomic;

import java.io.Serializable;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;
import sun.misc.Unsafe;

public class AtomicLong extends Number implements Serializable {
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long VALUE;
    static final boolean VM_SUPPORTS_LONG_CAS = VMSupportsCS8();
    private static final long serialVersionUID = 1927816293512124184L;
    private volatile long value;

    private static native boolean VMSupportsCS8();

    static {
        try {
            VALUE = U.objectFieldOffset(AtomicLong.class.getDeclaredField("value"));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public AtomicLong(long initialValue) {
        this.value = initialValue;
    }

    public final long get() {
        return this.value;
    }

    public final void set(long newValue) {
        U.putLongVolatile(this, VALUE, newValue);
    }

    public final void lazySet(long newValue) {
        U.putOrderedLong(this, VALUE, newValue);
    }

    public final long getAndSet(long newValue) {
        return U.getAndSetLong(this, VALUE, newValue);
    }

    public final boolean compareAndSet(long expect, long update) {
        return U.compareAndSwapLong(this, VALUE, expect, update);
    }

    public final boolean weakCompareAndSet(long expect, long update) {
        return U.compareAndSwapLong(this, VALUE, expect, update);
    }

    public final long getAndIncrement() {
        return U.getAndAddLong(this, VALUE, 1);
    }

    public final long getAndDecrement() {
        return U.getAndAddLong(this, VALUE, -1);
    }

    public final long getAndAdd(long delta) {
        return U.getAndAddLong(this, VALUE, delta);
    }

    public final long incrementAndGet() {
        return U.getAndAddLong(this, VALUE, 1) + 1;
    }

    public final long decrementAndGet() {
        return U.getAndAddLong(this, VALUE, -1) - 1;
    }

    public final long addAndGet(long delta) {
        return U.getAndAddLong(this, VALUE, delta) + delta;
    }

    public final long getAndUpdate(LongUnaryOperator updateFunction) {
        long prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, updateFunction.applyAsLong(prev)));
        return prev;
    }

    public final long updateAndGet(LongUnaryOperator updateFunction) {
        long next;
        long prev;
        do {
            prev = get();
            next = updateFunction.applyAsLong(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public final long getAndAccumulate(long x, LongBinaryOperator accumulatorFunction) {
        long prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, accumulatorFunction.applyAsLong(prev, x)));
        return prev;
    }

    public final long accumulateAndGet(long x, LongBinaryOperator accumulatorFunction) {
        long next;
        long prev;
        do {
            prev = get();
            next = accumulatorFunction.applyAsLong(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public String toString() {
        return Long.toString(get());
    }

    public int intValue() {
        return (int) get();
    }

    public long longValue() {
        return get();
    }

    public float floatValue() {
        return (float) get();
    }

    public double doubleValue() {
        return (double) get();
    }
}
