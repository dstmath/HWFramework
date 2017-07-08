package java.util.concurrent.atomic;

import java.io.Serializable;
import sun.misc.Unsafe;

public class AtomicBoolean implements Serializable {
    private static final Unsafe U = null;
    private static final long VALUE = 0;
    private static final long serialVersionUID = 4654671469794556979L;
    private volatile int value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.atomic.AtomicBoolean.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.atomic.AtomicBoolean.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.atomic.AtomicBoolean.<clinit>():void");
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
