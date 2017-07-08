package java.util.concurrent.atomic;

import java.io.Serializable;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;
import sun.misc.Unsafe;

public class AtomicLong extends Number implements Serializable {
    private static final Unsafe U = null;
    private static final long VALUE = 0;
    static final boolean VM_SUPPORTS_LONG_CAS = false;
    private static final long serialVersionUID = 1927816293512124184L;
    private volatile long value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.atomic.AtomicLong.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.atomic.AtomicLong.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.atomic.AtomicLong.<clinit>():void");
    }

    private static native boolean VMSupportsCS8();

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
