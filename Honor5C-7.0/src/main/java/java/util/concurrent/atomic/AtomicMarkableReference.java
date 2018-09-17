package java.util.concurrent.atomic;

import sun.misc.Unsafe;

public class AtomicMarkableReference<V> {
    private static final long PAIR = 0;
    private static final Unsafe U = null;
    private volatile Pair<V> pair;

    private static class Pair<T> {
        final boolean mark;
        final T reference;

        private Pair(T reference, boolean mark) {
            this.reference = reference;
            this.mark = mark;
        }

        static <T> Pair<T> of(T reference, boolean mark) {
            return new Pair(reference, mark);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.concurrent.atomic.AtomicMarkableReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.concurrent.atomic.AtomicMarkableReference.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.util.concurrent.atomic.AtomicMarkableReference.<clinit>():void");
    }

    public AtomicMarkableReference(V initialRef, boolean initialMark) {
        this.pair = Pair.of(initialRef, initialMark);
    }

    public V getReference() {
        return this.pair.reference;
    }

    public boolean isMarked() {
        return this.pair.mark;
    }

    public V get(boolean[] markHolder) {
        Pair<V> pair = this.pair;
        markHolder[0] = pair.mark;
        return pair.reference;
    }

    public boolean weakCompareAndSet(V expectedReference, V newReference, boolean expectedMark, boolean newMark) {
        return compareAndSet(expectedReference, newReference, expectedMark, newMark);
    }

    public boolean compareAndSet(V expectedReference, V newReference, boolean expectedMark, boolean newMark) {
        Pair<V> current = this.pair;
        if (expectedReference != current.reference || expectedMark != current.mark) {
            return false;
        }
        if (newReference == current.reference && newMark == current.mark) {
            return true;
        }
        return casPair(current, Pair.of(newReference, newMark));
    }

    public void set(V newReference, boolean newMark) {
        Pair<V> current = this.pair;
        if (newReference != current.reference || newMark != current.mark) {
            this.pair = Pair.of(newReference, newMark);
        }
    }

    public boolean attemptMark(V expectedReference, boolean newMark) {
        Pair<V> current = this.pair;
        if (expectedReference != current.reference) {
            return false;
        }
        if (newMark != current.mark) {
            return casPair(current, Pair.of(expectedReference, newMark));
        }
        return true;
    }

    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return U.compareAndSwapObject(this, PAIR, cmp, val);
    }
}
