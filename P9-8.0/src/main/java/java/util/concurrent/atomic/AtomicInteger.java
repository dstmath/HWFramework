package java.util.concurrent.atomic;

import java.io.Serializable;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import sun.misc.Unsafe;

public class AtomicInteger extends Number implements Serializable {
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long VALUE;
    private static final long serialVersionUID = 6214790243416807050L;
    private volatile int value;

    static {
        try {
            VALUE = U.objectFieldOffset(AtomicInteger.class.getDeclaredField("value"));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public AtomicInteger(int initialValue) {
        this.value = initialValue;
    }

    public final int get() {
        return this.value;
    }

    public final void set(int newValue) {
        this.value = newValue;
    }

    public final void lazySet(int newValue) {
        U.putOrderedInt(this, VALUE, newValue);
    }

    public final int getAndSet(int newValue) {
        return U.getAndSetInt(this, VALUE, newValue);
    }

    public final boolean compareAndSet(int expect, int update) {
        return U.compareAndSwapInt(this, VALUE, expect, update);
    }

    public final boolean weakCompareAndSet(int expect, int update) {
        return U.compareAndSwapInt(this, VALUE, expect, update);
    }

    public final int getAndIncrement() {
        return U.getAndAddInt(this, VALUE, 1);
    }

    public final int getAndDecrement() {
        return U.getAndAddInt(this, VALUE, -1);
    }

    public final int getAndAdd(int delta) {
        return U.getAndAddInt(this, VALUE, delta);
    }

    public final int incrementAndGet() {
        return U.getAndAddInt(this, VALUE, 1) + 1;
    }

    public final int decrementAndGet() {
        return U.getAndAddInt(this, VALUE, -1) - 1;
    }

    public final int addAndGet(int delta) {
        return U.getAndAddInt(this, VALUE, delta) + delta;
    }

    public final int getAndUpdate(IntUnaryOperator updateFunction) {
        int prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, updateFunction.applyAsInt(prev)));
        return prev;
    }

    public final int updateAndGet(IntUnaryOperator updateFunction) {
        int next;
        int prev;
        do {
            prev = get();
            next = updateFunction.applyAsInt(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public final int getAndAccumulate(int x, IntBinaryOperator accumulatorFunction) {
        int prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, accumulatorFunction.applyAsInt(prev, x)));
        return prev;
    }

    public final int accumulateAndGet(int x, IntBinaryOperator accumulatorFunction) {
        int next;
        int prev;
        do {
            prev = get();
            next = accumulatorFunction.applyAsInt(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public String toString() {
        return Integer.toString(get());
    }

    public int intValue() {
        return get();
    }

    public long longValue() {
        return (long) get();
    }

    public float floatValue() {
        return (float) get();
    }

    public double doubleValue() {
        return (double) get();
    }
}
