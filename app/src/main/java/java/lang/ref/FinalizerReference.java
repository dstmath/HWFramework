package java.lang.ref;

public final class FinalizerReference<T> extends Reference<T> {
    private static final Object LIST_LOCK = null;
    private static FinalizerReference<?> head;
    public static final ReferenceQueue<Object> queue = null;
    private FinalizerReference<?> next;
    private FinalizerReference<?> prev;
    private T zombie;

    private static class Sentinel {
        boolean finalized;

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.ref.FinalizerReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.ref.FinalizerReference.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.ref.FinalizerReference.<clinit>():void");
    }

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
            FinalizerReference<?> r = head;
            while (r != null) {
                if (r.referent == sentinel) {
                    FinalizerReference<Sentinel> sentinelReference = r;
                    sentinelReference.referent = null;
                    sentinelReference.zombie = sentinel;
                    if (sentinelReference.makeCircularListIfUnenqueued()) {
                        ReferenceQueue.add(sentinelReference);
                        return true;
                    }
                    return false;
                }
                r = r.next;
            }
            throw new AssertionError("newly-created live Sentinel not on list!");
        }
    }
}
