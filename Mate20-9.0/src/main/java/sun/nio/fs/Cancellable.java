package sun.nio.fs;

import java.util.concurrent.ExecutionException;
import sun.misc.Unsafe;

abstract class Cancellable implements Runnable {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private boolean completed;
    private Throwable exception;
    private final Object lock = new Object();
    private final long pollingAddress = unsafe.allocateMemory(4);

    /* access modifiers changed from: package-private */
    public abstract void implRun() throws Throwable;

    protected Cancellable() {
        unsafe.putIntVolatile(null, this.pollingAddress, 0);
    }

    /* access modifiers changed from: protected */
    public long addressToPollForCancel() {
        return this.pollingAddress;
    }

    /* access modifiers changed from: protected */
    public int cancelValue() {
        return Integer.MAX_VALUE;
    }

    /* access modifiers changed from: package-private */
    public final void cancel() {
        synchronized (this.lock) {
            if (!this.completed) {
                unsafe.putIntVolatile(null, this.pollingAddress, cancelValue());
            }
        }
    }

    private Throwable exception() {
        Throwable th;
        synchronized (this.lock) {
            th = this.exception;
        }
        return th;
    }

    public final void run() {
        try {
            implRun();
            synchronized (this.lock) {
                this.completed = true;
                unsafe.freeMemory(this.pollingAddress);
            }
        } catch (Throwable t) {
            try {
                synchronized (this.lock) {
                    this.exception = t;
                    synchronized (this.lock) {
                        this.completed = true;
                        unsafe.freeMemory(this.pollingAddress);
                    }
                }
            } catch (Throwable th) {
                synchronized (this.lock) {
                    this.completed = true;
                    unsafe.freeMemory(this.pollingAddress);
                    throw th;
                }
            }
        }
    }

    static void runInterruptibly(Cancellable task) throws ExecutionException {
        Thread t = new Thread((Runnable) task);
        t.start();
        boolean cancelledByInterrupt = false;
        while (t.isAlive()) {
            try {
                t.join();
            } catch (InterruptedException e) {
                cancelledByInterrupt = true;
                task.cancel();
            }
        }
        if (cancelledByInterrupt) {
            Thread.currentThread().interrupt();
        }
        Throwable exc = task.exception();
        if (exc != null) {
            throw new ExecutionException(exc);
        }
    }
}
