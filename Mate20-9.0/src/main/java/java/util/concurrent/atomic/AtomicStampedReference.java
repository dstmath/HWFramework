package java.util.concurrent.atomic;

import sun.misc.Unsafe;

public class AtomicStampedReference<V> {
    private static final long PAIR;
    private static final Unsafe U = Unsafe.getUnsafe();
    private volatile Pair<V> pair;

    private static class Pair<T> {
        final T reference;
        final int stamp;

        private Pair(T reference2, int stamp2) {
            this.reference = reference2;
            this.stamp = stamp2;
        }

        static <T> Pair<T> of(T reference2, int stamp2) {
            return new Pair<>(reference2, stamp2);
        }
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
        Pair<V> pair2 = this.pair;
        stampHolder[0] = pair2.stamp;
        return pair2.reference;
    }

    public boolean weakCompareAndSet(V expectedReference, V newReference, int expectedStamp, int newStamp) {
        return compareAndSet(expectedReference, newReference, expectedStamp, newStamp);
    }

    public boolean compareAndSet(V expectedReference, V newReference, int expectedStamp, int newStamp) {
        Pair<V> current = this.pair;
        return expectedReference == current.reference && expectedStamp == current.stamp && ((newReference == current.reference && newStamp == current.stamp) || casPair(current, Pair.of(newReference, newStamp)));
    }

    public void set(V newReference, int newStamp) {
        Pair<V> current = this.pair;
        if (newReference != current.reference || newStamp != current.stamp) {
            this.pair = Pair.of(newReference, newStamp);
        }
    }

    public boolean attemptStamp(V expectedReference, int newStamp) {
        Pair<V> current = this.pair;
        return expectedReference == current.reference && (newStamp == current.stamp || casPair(current, Pair.of(expectedReference, newStamp)));
    }

    static {
        try {
            PAIR = U.objectFieldOffset(AtomicStampedReference.class.getDeclaredField("pair"));
        } catch (ReflectiveOperationException e) {
            throw new Error((Throwable) e);
        }
    }

    private boolean casPair(Pair<V> cmp, Pair<V> val) {
        return U.compareAndSwapObject(this, PAIR, cmp, val);
    }
}
