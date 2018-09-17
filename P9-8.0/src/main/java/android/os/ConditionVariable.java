package android.os;

public class ConditionVariable {
    private volatile boolean mCondition;

    public ConditionVariable() {
        this.mCondition = false;
    }

    public ConditionVariable(boolean state) {
        this.mCondition = state;
    }

    public void open() {
        synchronized (this) {
            boolean old = this.mCondition;
            this.mCondition = true;
            if (!old) {
                notifyAll();
            }
        }
    }

    public void close() {
        synchronized (this) {
            this.mCondition = false;
        }
    }

    public void block() {
        synchronized (this) {
            while (!this.mCondition) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public boolean block(long timeout) {
        if (timeout != 0) {
            boolean z;
            synchronized (this) {
                long now = System.currentTimeMillis();
                long end = now + timeout;
                while (!this.mCondition && now < end) {
                    try {
                        wait(end - now);
                    } catch (InterruptedException e) {
                    }
                    now = System.currentTimeMillis();
                }
                z = this.mCondition;
            }
            return z;
        }
        block();
        return true;
    }
}
