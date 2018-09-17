package java.util.concurrent.atomic;

import sun.misc.Unsafe;

public class AtomicMarkableReference<V> {
    private static final long PAIR;
    private static final Unsafe U = Unsafe.getUnsafe();
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

    static {
        try {
            PAIR = U.objectFieldOffset(AtomicMarkableReference.class.getDeclaredField("pair"));
        } catch (Throwable e) {
            throw new Error(e);
        }
    }

    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return U.compareAndSwapObject(this, PAIR, cmp, val);
    }
}
