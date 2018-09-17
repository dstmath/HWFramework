package android.filterfw.core;

import android.os.SystemClock;
import android.util.Log;

/* compiled from: StopWatchMap */
class StopWatch {
    private int STOP_WATCH_LOGGING_PERIOD = 200;
    private String TAG = "MFF";
    private String mName;
    private int mNumCalls;
    private long mStartTime;
    private long mTotalTime;

    public StopWatch(String name) {
        this.mName = name;
        this.mStartTime = -1;
        this.mTotalTime = 0;
        this.mNumCalls = 0;
    }

    public void start() {
        if (this.mStartTime != -1) {
            throw new RuntimeException("Calling start with StopWatch already running");
        }
        this.mStartTime = SystemClock.elapsedRealtime();
    }

    public void stop() {
        if (this.mStartTime == -1) {
            throw new RuntimeException("Calling stop with StopWatch already stopped");
        }
        this.mTotalTime += SystemClock.elapsedRealtime() - this.mStartTime;
        this.mNumCalls++;
        this.mStartTime = -1;
        if (this.mNumCalls % this.STOP_WATCH_LOGGING_PERIOD == 0) {
            Log.i(this.TAG, "AVG ms/call " + this.mName + ": " + String.format("%.1f", new Object[]{Float.valueOf((((float) this.mTotalTime) * 1.0f) / ((float) this.mNumCalls))}));
            this.mTotalTime = 0;
            this.mNumCalls = 0;
        }
    }
}
