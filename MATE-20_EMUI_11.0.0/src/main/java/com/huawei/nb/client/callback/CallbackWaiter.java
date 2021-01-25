package com.huawei.nb.client.callback;

import com.huawei.nb.utils.Waiter;
import com.huawei.nb.utils.logger.DSLog;

public class CallbackWaiter<T> {
    private static final int INVALID_TRANSACTION_ID = -1;
    private final T failedValue;
    private final Object locker = new Object();
    private T result = null;
    private Status status = Status.INIT;
    private int transactionId = -1;
    private final Waiter waiter = new Waiter();

    /* access modifiers changed from: private */
    public enum Status {
        INIT,
        WAITING,
        INVOKED
    }

    public CallbackWaiter(T t) {
        this.failedValue = t;
    }

    public void interrupt() {
        this.waiter.interrupt();
    }

    public T await(int i, long j) {
        T t;
        if (i == -1) {
            DSLog.e("Failed to wait callback, error: received invalid transaction id.", new Object[0]);
            return this.failedValue;
        } else if (waitCallbackToBeInvoked(i, j)) {
            synchronized (this.locker) {
                t = this.result;
            }
            return t;
        } else {
            DSLog.e("Failed to wait callback for transaction %d, error: timeout.", Integer.valueOf(i));
            return this.failedValue;
        }
    }

    private boolean waitCallbackToBeInvoked(int i, long j) {
        synchronized (this.locker) {
            if (this.status == Status.INVOKED) {
                return true;
            }
            this.transactionId = i;
            this.status = Status.WAITING;
            return this.waiter.await(j);
        }
    }

    public void set(int i, T t) {
        synchronized (this.locker) {
            if (this.status == Status.INIT) {
                this.result = t;
                this.status = Status.INVOKED;
            }
            if (this.status == Status.WAITING && i == this.transactionId) {
                this.result = t;
                this.status = Status.INVOKED;
                this.waiter.signal();
            }
        }
    }
}
