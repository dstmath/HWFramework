package sun.misc;

import java.lang.annotation.RCUnownedRef;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class Cleaner extends PhantomReference<Object> {
    private static final ReferenceQueue<Object> dummyQueue = new ReferenceQueue<>();
    private static Cleaner first = null;
    private Cleaner next = null;
    @RCUnownedRef
    private Cleaner prev = null;
    private final Runnable thunk;

    private static synchronized Cleaner add(Cleaner cl) {
        synchronized (Cleaner.class) {
            if (first != null) {
                cl.next = first;
                first.prev = cl;
            }
            first = cl;
        }
        return cl;
    }

    private static synchronized boolean remove(Cleaner cl) {
        synchronized (Cleaner.class) {
            if (cl.next == cl) {
                return false;
            }
            if (first == cl) {
                if (cl.next != null) {
                    first = cl.next;
                } else {
                    first = cl.prev;
                }
            }
            if (cl.next != null) {
                cl.next.prev = cl.prev;
            }
            if (cl.prev != null) {
                cl.prev.next = cl.next;
            }
            cl.next = cl;
            cl.prev = cl;
            return true;
        }
    }

    private Cleaner(Object referent, Runnable thunk2) {
        super(referent, dummyQueue);
        this.thunk = thunk2;
    }

    public static Cleaner create(Object ob, Runnable thunk2) {
        if (thunk2 == null) {
            return null;
        }
        return add(new Cleaner(ob, thunk2));
    }

    public void clean() {
        if (remove(this) && this.thunk != null) {
            try {
                this.thunk.run();
            } catch (Throwable x) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        if (System.err != null) {
                            new Error("Cleaner terminated abnormally", x).printStackTrace();
                        }
                        System.exit(1);
                        return null;
                    }
                });
            }
        }
    }
}
