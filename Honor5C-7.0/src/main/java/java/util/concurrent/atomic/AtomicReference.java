package java.util.concurrent.atomic;

import java.io.Serializable;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;
import sun.misc.Unsafe;

public class AtomicReference<V> implements Serializable {
    private static final Unsafe U = null;
    private static final long VALUE = 0;
    private static final long serialVersionUID = -1848883965231344442L;
    private volatile V value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.atomic.AtomicReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.atomic.AtomicReference.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.atomic.AtomicReference.<clinit>():void");
    }

    public AtomicReference(V initialValue) {
        this.value = initialValue;
    }

    public final V get() {
        return this.value;
    }

    public final void set(V newValue) {
        this.value = newValue;
    }

    public final void lazySet(V newValue) {
        U.putOrderedObject(this, VALUE, newValue);
    }

    public final boolean compareAndSet(V expect, V update) {
        return U.compareAndSwapObject(this, VALUE, expect, update);
    }

    public final boolean weakCompareAndSet(V expect, V update) {
        return U.compareAndSwapObject(this, VALUE, expect, update);
    }

    public final V getAndSet(V newValue) {
        return U.getAndSetObject(this, VALUE, newValue);
    }

    public final V getAndUpdate(UnaryOperator<V> updateFunction) {
        V prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, updateFunction.apply(prev)));
        return prev;
    }

    public final V updateAndGet(UnaryOperator<V> updateFunction) {
        V next;
        V prev;
        do {
            prev = get();
            next = updateFunction.apply(prev);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public final V getAndAccumulate(V x, BinaryOperator<V> accumulatorFunction) {
        V prev;
        do {
            prev = get();
        } while (!compareAndSet(prev, accumulatorFunction.apply(prev, x)));
        return prev;
    }

    public final V accumulateAndGet(V x, BinaryOperator<V> accumulatorFunction) {
        V next;
        V prev;
        do {
            prev = get();
            next = accumulatorFunction.apply(prev, x);
        } while (!compareAndSet(prev, next));
        return next;
    }

    public String toString() {
        return String.valueOf(get());
    }
}
