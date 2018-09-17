package java.lang.ref;

import sun.misc.Cleaner;

public class ReferenceQueue<T> {
    private static final Reference sQueueNextUnenqueued = null;
    public static Reference<?> unenqueued;
    private Reference<? extends T> head;
    private final Object lock;
    private Reference<? extends T> tail;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.lang.ref.ReferenceQueue.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.lang.ref.ReferenceQueue.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: java.lang.ref.ReferenceQueue.<clinit>():void");
    }

    public ReferenceQueue() {
        this.head = null;
        this.tail = null;
        this.lock = new Object();
    }

    private boolean enqueueLocked(Reference<? extends T> r) {
        if (r.queueNext != null) {
            return false;
        }
        if (r instanceof Cleaner) {
            ((Cleaner) r).clean();
            r.queueNext = sQueueNextUnenqueued;
            return true;
        }
        if (this.tail == null) {
            this.head = r;
        } else {
            this.tail.queueNext = r;
        }
        this.tail = r;
        this.tail.queueNext = r;
        return true;
    }

    boolean isEnqueued(Reference<? extends T> reference) {
        boolean z = false;
        synchronized (this.lock) {
            if (!(reference.queueNext == null || reference.queueNext == sQueueNextUnenqueued)) {
                z = true;
            }
        }
        return z;
    }

    boolean enqueue(Reference<? extends T> reference) {
        synchronized (this.lock) {
            if (enqueueLocked(reference)) {
                this.lock.notifyAll();
                return true;
            }
            return false;
        }
    }

    private Reference<? extends T> reallyPollLocked() {
        if (this.head == null) {
            return null;
        }
        Reference<? extends T> r = this.head;
        if (this.head == this.tail) {
            this.tail = null;
            this.head = null;
        } else {
            this.head = this.head.queueNext;
        }
        r.queueNext = sQueueNextUnenqueued;
        return r;
    }

    public Reference<? extends T> poll() {
        synchronized (this.lock) {
            if (this.head == null) {
                return null;
            }
            Reference<? extends T> reallyPollLocked = reallyPollLocked();
            return reallyPollLocked;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Reference<? extends T> remove(long timeout) throws IllegalArgumentException, InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("Negative timeout value");
        }
        synchronized (this.lock) {
            Reference<? extends T> r = reallyPollLocked();
            if (r != null) {
                return r;
            }
            long start = timeout == 0 ? 0 : System.nanoTime();
            while (true) {
                this.lock.wait(timeout);
                r = reallyPollLocked();
                if (r != null) {
                    return r;
                } else if (timeout != 0) {
                    long end = System.nanoTime();
                    timeout -= (end - start) / 1000000;
                    if (timeout <= 0) {
                        return null;
                    }
                    start = end;
                }
            }
        }
    }

    public Reference<? extends T> remove() throws InterruptedException {
        return remove(0);
    }

    public static void enqueuePending(Reference<?> list) {
        Reference<?> start = list;
        do {
            ReferenceQueue queue = list.queue;
            Reference<?> next;
            if (queue == null) {
                next = list.pendingNext;
                list.pendingNext = list;
                list = next;
                continue;
            } else {
                synchronized (queue.lock) {
                    do {
                        next = list.pendingNext;
                        list.pendingNext = list;
                        queue.enqueueLocked(list);
                        list = next;
                        if (next == start) {
                            break;
                        }
                    } while (next.queue == queue);
                    queue.lock.notifyAll();
                }
                continue;
            }
        } while (list != start);
    }

    static void add(Reference<?> list) {
        synchronized (ReferenceQueue.class) {
            if (unenqueued == null) {
                unenqueued = list;
            } else {
                Reference<?> last = unenqueued;
                while (last.pendingNext != unenqueued) {
                    last = last.pendingNext;
                }
                last.pendingNext = list;
                last = list;
                while (last.pendingNext != list) {
                    last = last.pendingNext;
                }
                last.pendingNext = unenqueued;
            }
            ReferenceQueue.class.notifyAll();
        }
    }
}
