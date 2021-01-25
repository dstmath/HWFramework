package ohos.hiviewdfx.xcollie;

public class XCollieTimer {
    private long mTimeout;
    private String mTimerName;

    public void onTimerTimeout() {
    }

    public XCollieTimer(String str, String str2, long j) {
        this.mTimerName = "timer: " + str2 + " packageName: " + str;
        this.mTimeout = j;
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
