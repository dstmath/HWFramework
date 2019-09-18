package java.util.concurrent.locks;

import java.io.Serializable;

public abstract class AbstractOwnableSynchronizer implements Serializable {
    private static final long serialVersionUID = 3737899427754241961L;
    private transient Thread exclusiveOwnerThread;

    protected AbstractOwnableSynchronizer() {
    }

    /* access modifiers changed from: protected */
    public final void setExclusiveOwnerThread(Thread thread) {
        this.exclusiveOwnerThread = thread;
    }

    /* access modifiers changed from: protected */
    public final Thread getExclusiveOwnerThread() {
        return this.exclusiveOwnerThread;
    }
}
