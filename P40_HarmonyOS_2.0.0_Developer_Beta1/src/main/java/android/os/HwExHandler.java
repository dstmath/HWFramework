package android.os;

import android.util.Log;

public class HwExHandler extends Handler {
    private static final String TAG = "HwExHandler";
    private static final long TIME_OUT_RUNNING = 3000;
    long mLastStartTime = 0;
    private long runningTimeout = TIME_OUT_RUNNING;

    public HwExHandler(Looper looper) {
        super(looper);
    }

    public HwExHandler(Looper looper, long timeout) {
        super(looper);
        this.runningTimeout = timeout;
    }

    @Override // android.os.Handler
    public void dispatchMessage(Message msg) {
        this.mLastStartTime = SystemClock.uptimeMillis();
        super.dispatchMessage(msg);
        this.mLastStartTime = 0;
    }

    @Override // android.os.Handler
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        long nowStartTime = SystemClock.uptimeMillis();
        long j = this.mLastStartTime;
        long threadRunningTime = nowStartTime - j;
        if (j != 0 && threadRunningTime > this.runningTimeout && getLooper().getThread().isAlive()) {
            Log.e(TAG, "Thread:" + getLooper().getThread().getName() + ",threadRunningTime:" + threadRunningTime);
            StackTraceElement[] stackTrace = getLooper().getThread().getStackTrace();
            if (stackTrace != null) {
                for (StackTraceElement stack : stackTrace) {
                    Log.e(TAG, stack.toString());
                }
            }
        }
        return super.sendMessageAtTime(msg, uptimeMillis);
    }
}
