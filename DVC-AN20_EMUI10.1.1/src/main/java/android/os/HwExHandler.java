package android.os;

import android.util.Log;

public class HwExHandler extends Handler {
    private static final String TAG = "HwExHandler";
    long mLastStartTime = 0;
    private long runningTimeout = 3000;

    public HwExHandler(Looper looper) {
        super(looper);
    }

    public HwExHandler(Looper looper, long timeout) {
        super(looper);
        this.runningTimeout = timeout;
    }

    public void dispatchMessage(Message msg) {
        this.mLastStartTime = SystemClock.uptimeMillis();
        super.dispatchMessage(msg);
        this.mLastStartTime = 0;
    }

    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        long mNowStartTime = SystemClock.uptimeMillis();
        long j = this.mLastStartTime;
        long threadRunningTime = mNowStartTime - j;
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
