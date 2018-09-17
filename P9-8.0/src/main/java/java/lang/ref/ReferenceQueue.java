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
