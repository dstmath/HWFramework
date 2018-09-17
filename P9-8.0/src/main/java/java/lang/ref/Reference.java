package java.lang.ref;

public abstract class Reference<T> {
    private static boolean disableIntrinsic = false;
    private static boolean slowPathEnabled = false;
    Reference<?> pendingNext;
    final ReferenceQueue<? super T> queue;
    Reference queueNext;
    volatile T referent;

    private final native T getReferent();

    native void clearReferent();

    public T get() {
        return getReferent();
    }

    public void clear() {
        clearReferent();
    }

    public boolean isEnqueued() {
        return this.queue != null ? this.queue.isEnqueued(this) : false;
    }

    public boolean enqueue() {
        return this.queue != null ? this.queue.enqueue(this) : false;
    }

    Reference(T referent) {
        this(referent, null);
    }

    Reference(T referent, ReferenceQueue<? super T> queue) {
        this.referent = referent;
        this.queue = queue;
    }
}
