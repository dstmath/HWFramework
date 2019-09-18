package java.lang.ref;

import sun.misc.Cleaner;

public class ReferenceQueue<T> {
    private static final Reference sQueueNextUnenqueued = new PhantomReference(null, null);
    public static Reference<?> unenqueued = null;
    private Reference<? extends T> head = null;
    private final Object lock = new Object();
    private Reference<? extends T> tail = null;

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

    /* access modifiers changed from: package-private */
    public boolean isEnqueued(Reference<? extends T> reference) {
        boolean z;
        synchronized (this.lock) {
            z = (reference.queueNext == null || reference.queueNext == sQueueNextUnenqueued) ? false : true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean enqueue(Reference<? extends T> reference) {
        synchronized (this.lock) {
            if (!enqueueLocked(reference)) {
                return false;
            }
            this.lock.notifyAll();
            return true;
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

    public Reference<? extends T> remove(long timeout) throws IllegalArgumentException, InterruptedException {
        if (timeout >= 0) {
            synchronized (this.lock) {
                Reference<? extends T> r = reallyPollLocked();
                if (r != null) {
                    return r;
                }
                long start = timeout == 0 ? 0 : System.nanoTime();
                while (true) {
                    this.lock.wait(timeout);
                    Reference<? extends T> r2 = reallyPollLocked();
                    if (r2 != null) {
                        return r2;
                    }
                    if (timeout != 0) {
                        long end = System.nanoTime();
                        timeout -= (end - start) / 1000000;
                        if (timeout <= 0) {
                            return null;
                        }
                        start = end;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Negative timeout value");
        }
    }

    public Reference<? extends T> remove() throws InterruptedException {
        return remove(0);
    }

    public static void enqueuePending(Reference<?> start) {
        Reference<?> list = start;
        do {
            ReferenceQueue queue = list.queue;
            if (queue == null) {
                Reference<?> next = list.pendingNext;
                list.pendingNext = list;
                list = next;
                continue;
            } else {
                synchronized (queue.lock) {
                    do {
                        Reference<?> next2 = list.pendingNext;
                        list.pendingNext = list;
                        queue.enqueueLocked(list);
                        list = next2;
                        if (list == start) {
                            break;
                        }
                    } while (list.queue == queue);
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
                Reference<?> last2 = list;
                while (last2.pendingNext != list) {
                    last2 = last2.pendingNext;
                }
                last2.pendingNext = unenqueued;
            }
            ReferenceQueue.class.notifyAll();
        }
    }
}
