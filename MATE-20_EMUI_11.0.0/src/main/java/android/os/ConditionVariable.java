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

    public boolean block(long timeoutMs) {
        boolean z;
        if (timeoutMs != 0) {
            synchronized (this) {
                long now = SystemClock.elapsedRealtime();
                long end = now + timeoutMs;
                while (!this.mCondition && now < end) {
                    try {
                        wait(end - now);
                    } catch (InterruptedException e) {
                    }
                    now = SystemClock.elapsedRealtime();
                }
                z = this.mCondition;
            }
            return z;
        }
        block();
        return true;
    }
}
