package java.util.concurrent.atomic;

import sun.misc.Unsafe;

public class AtomicStampedReference<V> {
    private static final long PAIR = 0;
    private static final Unsafe U = null;
    private volatile Pair<V> pair;

    private static class Pair<T> {
        final T reference;
        final int stamp;

        private Pair(T reference, int stamp) {
            this.reference = reference;
            this.stamp = stamp;
        }

        static <T> Pair<T> of(T reference, int stamp) {
            return new Pair(reference, stamp);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.atomic.AtomicStampedReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.atomic.AtomicStampedReference.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.atomic.AtomicStampedReference.<clinit>():void");
    }

    public AtomicStampedReference(V initialRef, int initialStamp) {
        this.pair = Pair.of(initialRef, initialStamp);
    }

    public V getReference() {
        return this.pair.reference;
    }

    public int getStamp() {
        return this.pair.stamp;
    }

    public V get(int[] stampHolder) {
        Pair<V> pair = this.pair;
        stampHolder[0] = pair.stamp;
        return pair.reference;
    }

    public boolean weakCompareAndSet(V expectedReference, V newReference, int expectedStamp, int newStamp) {
        return compareAndSet(expectedReference, newReference, expectedStamp, newStamp);
    }

    public boolean compareAndSet(V expectedReference, V newReference, int expectedStamp, int newStamp) {
        Pair<V> current = this.pair;
        if (expectedReference != current.reference || expectedStamp != current.stamp) {
            return false;
        }
        if (newReference == current.reference && newStamp == current.stamp) {
            return true;
        }
        return casPair(current, Pair.of(newReference, newStamp));
    }

    public void set(V newReference, int newStamp) {
        Pair<V> current = this.pair;
        if (newReference != current.reference || newStamp != current.stamp) {
            this.pair = Pair.of(newReference, newStamp);
        }
    }

    public boolean attemptStamp(V expectedReference, int newStamp) {
        Pair<V> current = this.pair;
        if (expectedReference != current.reference) {
            return false;
        }
        if (newStamp != current.stamp) {
            return casPair(current, Pair.of(expectedReference, newStamp));
        }
        return true;
    }

    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return U.compareAndSwapObject(this, PAIR, cmp, val);
    }
}
