package java.util.concurrent.atomic;

import java.io.Serializable;
import sun.misc.Unsafe;

public class AtomicBoolean implements Serializable {
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long VALUE;
    private static final long serialVersionUID = 4654671469794556979L;
    private volatile int value;

    static {
        try {
            VALUE = U.objectFieldOffset(AtomicBoolean.class.getDeclaredField("value"));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    public AtomicBoolean(boolean initialValue) {
        this.value = initialValue ? 1 : 0;
    }

    public final boolean get() {
        return this.value != 0;
    }

    public final boolean compareAndSet(boolean expect, boolean update) {
        int i;
        int i2 = 1;
        Unsafe unsafe = U;
        long j = VALUE;
        if (expect) {
            i = 1;
        } else {
            i = 0;
        }
        if (!update) {
            i2 = 0;
        }
        return unsafe.compareAndSwapInt(this, j, i, i2);
    }

    public boolean weakCompareAndSet(boolean expect, boolean update) {
        int i;
        int i2 = 1;
        Unsafe unsafe = U;
        long j = VALUE;
        if (expect) {
            i = 1;
        } else {
            i = 0;
        }
        if (!update) {
            i2 = 0;
        }
        return unsafe.compareAndSwapInt(this, j, i, i2);
    }

    public final void set(boolean newValue) {
        this.value = newValue ? 1 : 0;
    }

    public final void lazySet(boolean newValue) {
        U.putOrderedInt(this, VALUE, newValue ? 1 : 0);
    }

    public final boolean getAndSet(boolean newValue) {
        boolean prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, newValue));
        return prev;
    }

    public String toString() {
        return Boolean.toString(get());
    }
}
