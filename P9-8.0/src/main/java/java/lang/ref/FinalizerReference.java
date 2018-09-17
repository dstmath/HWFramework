package java.lang.ref;

public final class FinalizerReference<T> extends Reference<T> {
    private static final Object LIST_LOCK = new Object();
    private static FinalizerReference<?> head = null;
    public static final ReferenceQueue<Object> queue = new ReferenceQueue();
    private FinalizerReference<?> next;
    private FinalizerReference<?> prev;
    private T zombie;

    private static class Sentinel {
        boolean finalized;

        /* synthetic */ Sentinel(Sentinel -this0) {
            this();
        }

        private Sentinel() {
            this.finalized = false;
        }

        protected synchronized void finalize() throws Throwable {
            if (this.finalized) {
                throw new AssertionError();
            }
            this.finalized = true;
            notifyAll();
        }

        synchronized void awaitFinalization(long timeout) throws InterruptedException {
            long endTime = System.nanoTime() + timeout;
            while (!this.finalized) {
                if (timeout != 0) {
                    long currentTime = System.nanoTime();
                    if (currentTime >= endTime) {
                        break;
                    }
                    long deltaTime = endTime - currentTime;
                    wait(deltaTime / 1000000, (int) (deltaTime % 1000000));
                } else {
                    wait();
                }
            }
        }
    }

    private final native T getReferent();

    private native boolean makeCircularListIfUnenqueued();

    public FinalizerReference(T r, ReferenceQueue<? super T> q) {
        super(r, q);
    }

    public T get() {
        return this.zombie;
    }

    public void clear() {
        this.zombie = null;
    }

    public static void add(Object referent) {
        FinalizerReference<?> reference = new FinalizerReference(referent, queue);
        synchronized (LIST_LOCK) {
            reference.prev = null;
            reference.next = head;
            if (head != null) {
                head.prev = reference;
            }
            head = reference;
        }
    }

    public static void remove(FinalizerReference<?> reference) {
        synchronized (LIST_LOCK) {
            FinalizerReference<?> next = reference.next;
            FinalizerReference<?> prev = reference.prev;
            reference.next = null;
            reference.prev = null;
            if (prev != null) {
                prev.next = next;
            } else {
                head = next;
            }
            if (next != null) {
                next.prev = prev;
            }
        }
    }

    public static void finalizeAllEnqueued(long timeout) throws InterruptedException {
        Sentinel sentinel;
        do {
            sentinel = new Sentinel();
        } while (!enqueueSentinelReference(sentinel));
        sentinel.awaitFinalization(timeout);
    }

    private static boolean enqueueSentinelReference(Sentinel sentinel) {
        synchronized (LIST_LOCK) {
            for (FinalizerReference<?> r = head; r != null; r = r.next) {
                if (r.getReferent() == sentinel) {
                    FinalizerReference<Sentinel> sentinelReference = r;
                    sentinelReference.clearReferent();
                    sentinelReference.zombie = sentinel;
                    if (sentinelReference.makeCircularListIfUnenqueued()) {
                        ReferenceQueue.add(sentinelReference);
                        return true;
                    }
                    return false;
                }
            }
            throw new AssertionError("newly-created live Sentinel not on list!");
        }
    }
}
