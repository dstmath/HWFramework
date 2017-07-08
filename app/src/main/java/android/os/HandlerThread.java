package android.os;

public class HandlerThread extends Thread {
    Looper mLooper;
    int mPriority;
    int mTid;

    public HandlerThread(String name) {
        super(name);
        this.mTid = -1;
        this.mPriority = 0;
    }

    public HandlerThread(String name, int priority) {
        super(name);
        this.mTid = -1;
        this.mPriority = priority;
    }

    protected void onLooperPrepared() {
    }

    public void run() {
        this.mTid = Process.myTid();
        Looper.prepare();
        synchronized (this) {
            this.mLooper = Looper.myLooper();
            notifyAll();
        }
        Process.setThreadPriority(this.mPriority);
        onLooperPrepared();
        Looper.loop();
        this.mTid = -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Looper getLooper() {
        if (!isAlive()) {
            return null;
        }
        synchronized (this) {
            while (true) {
                if (isAlive() && this.mLooper == null) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        return this.mLooper;
    }

    public boolean quit() {
        Looper looper = getLooper();
        if (looper == null) {
            return false;
        }
        looper.quit();
        return true;
    }

    public boolean quitSafely() {
        Looper looper = getLooper();
        if (looper == null) {
            return false;
        }
        looper.quitSafely();
        return true;
    }

    public int getThreadId() {
        return this.mTid;
    }
}
