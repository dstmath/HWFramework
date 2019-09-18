package org.apache.http.impl.conn.tsccm;

import java.util.Date;
import java.util.concurrent.locks.Condition;

@Deprecated
public class WaitingThread {
    private boolean aborted;
    private final Condition cond;
    private final RouteSpecificPool pool;
    private Thread waiter;

    public WaitingThread(Condition cond2, RouteSpecificPool pool2) {
        if (cond2 != null) {
            this.cond = cond2;
            this.pool = pool2;
            return;
        }
        throw new IllegalArgumentException("Condition must not be null.");
    }

    public final Condition getCondition() {
        return this.cond;
    }

    public final RouteSpecificPool getPool() {
        return this.pool;
    }

    public final Thread getThread() {
        return this.waiter;
    }

    public boolean await(Date deadline) throws InterruptedException {
        boolean success;
        if (this.waiter != null) {
            throw new IllegalStateException("A thread is already waiting on this object.\ncaller: " + Thread.currentThread() + "\nwaiter: " + this.waiter);
        } else if (!this.aborted) {
            this.waiter = Thread.currentThread();
            if (deadline != null) {
                try {
                    success = this.cond.awaitUntil(deadline);
                } catch (Throwable th) {
                    this.waiter = null;
                    throw th;
                }
            } else {
                this.cond.await();
                success = true;
            }
            if (!this.aborted) {
                this.waiter = null;
                return success;
            }
            throw new InterruptedException("Operation interrupted");
        } else {
            throw new InterruptedException("Operation interrupted");
        }
    }

    public void wakeup() {
        if (this.waiter != null) {
            this.cond.signalAll();
            return;
        }
        throw new IllegalStateException("Nobody waiting on this object.");
    }

    public void interrupt() {
        this.aborted = true;
        this.cond.signalAll();
    }
}
