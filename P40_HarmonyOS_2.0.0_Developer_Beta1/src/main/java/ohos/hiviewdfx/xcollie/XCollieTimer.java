package ohos.hiviewdfx.xcollie;

public class XCollieTimer {
    private Runnable mCallback;
    private long mTimeout;
    private String mTimerName;

    public XCollieTimer(String str, String str2, Runnable runnable, long j) {
        this.mTimerName = "timer: " + str2 + " packageName: " + str;
        this.mTimeout = j;
        this.mCallback = runnable;
    }

    public void onTimerTimeout() {
        Runnable runnable = this.mCallback;
        if (runnable != null) {
            runnable.run();
        }
    }

    public long getTimeout() {
        return this.mTimeout;
    }

    public String getTimerName() {
        return this.mTimerName;
    }

    public void nativeTimeoutCallback() {
        onTimerTimeout();
    }
}
