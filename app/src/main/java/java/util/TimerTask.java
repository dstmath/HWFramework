package java.util;

public abstract class TimerTask implements Runnable {
    static final int CANCELLED = 3;
    static final int EXECUTED = 2;
    static final int SCHEDULED = 1;
    static final int VIRGIN = 0;
    final Object lock;
    long nextExecutionTime;
    long period;
    int state;

    public abstract void run();

    protected TimerTask() {
        this.lock = new Object();
        this.state = 0;
        this.period = 0;
    }

    public boolean cancel() {
        boolean result;
        synchronized (this.lock) {
            result = this.state == SCHEDULED;
            this.state = CANCELLED;
        }
        return result;
    }

    public long scheduledExecutionTime() {
        long j;
        synchronized (this.lock) {
            if (this.period < 0) {
                j = this.nextExecutionTime + this.period;
            } else {
                j = this.nextExecutionTime - this.period;
            }
        }
        return j;
    }
}
