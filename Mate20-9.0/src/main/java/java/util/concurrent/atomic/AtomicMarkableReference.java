package java.util.concurrent.atomic;

import sun.misc.Unsafe;

public class AtomicMarkableReference<V> {
    private static final long PAIR;
    private static final Unsafe U = Unsafe.getUnsafe();
    private volatile Pair<V> pair;

    private static class Pair<T> {
        final boolean mark;
        final T reference;

        private Pair(T reference2, boolean mark2) {
            this.reference = reference2;
            this.mark = mark2;
        }

        static <T> Pair<T> of(T reference2, boolean mark2) {
            return new Pair<>(reference2, mark2);
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
        Pair<V> pair2 = this.pair;
        markHolder[0] = pair2.mark;
        return pair2.reference;
    }

    public boolean weakCompareAndSet(V expectedReference, V newReference, boolean expectedMark, boolean newMark) {
        return compareAndSet(expectedReference, newReference, expectedMark, newMark);
    }

    public boolean compareAndSet(V expectedReference, V newReference, boolean expectedMark, boolean newMark) {
        Pair<V> current = this.pair;
        return expectedReference == current.reference && expectedMark == current.mark && ((newReference == current.reference && newMark == current.mark) || casPair(current, Pair.of(newReference, newMark)));
    }

    public void set(V newReference, boolean newMark) {
        Pair<V> current = this.pair;
        if (newReference != current.reference || newMark != current.mark) {
            this.pair = Pair.of(newReference, newMark);
        }
    }

    public boolean attemptMark(V expectedReference, boolean newMark) {
        Pair<V> current = this.pair;
        return expectedReference == current.reference && (newMark == current.mark || casPair(current, Pair.of(expectedReference, newMark)));
    }

    static {
        try {
            PAIR = U.objectFieldOffset(AtomicMarkableReference.class.getDeclaredField("pair"));
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }

    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return U.compareAndSwapObject(this, PAIR, cmp, val);
    }
}
