package android.os;

public abstract class CountDownTimer {
    private static final int MSG = 1;
    private boolean mCancelled = false;
    private final long mCountdownInterval;
    private Handler mHandler = new Handler() {
        /* JADX WARNING: Missing block: B:12:0x0025, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            synchronized (CountDownTimer.this) {
                if (CountDownTimer.this.mCancelled) {
                    return;
                }
                long millisLeft = CountDownTimer.this.mStopTimeInFuture - SystemClock.elapsedRealtime();
                if (millisLeft <= 0) {
                    CountDownTimer.this.onFinish();
                } else {
                    long delay;
                    long lastTickStart = SystemClock.elapsedRealtime();
                    CountDownTimer.this.onTick(millisLeft);
                    long lastTickDuration = SystemClock.elapsedRealtime() - lastTickStart;
                    if (millisLeft < CountDownTimer.this.mCountdownInterval) {
                        delay = millisLeft - lastTickDuration;
                        if (delay < 0) {
                            delay = 0;
                        }
                    } else {
                        delay = CountDownTimer.this.mCountdownInterval - lastTickDuration;
                        while (delay < 0) {
                            delay += CountDownTimer.this.mCountdownInterval;
                        }
                    }
                    sendMessageDelayed(obtainMessage(1), delay);
                }
            }
        }
    };
    private final long mMillisInFuture;
    private long mStopTimeInFuture;

    public abstract void onFinish();

    public abstract void onTick(long j);

    public CountDownTimer(long millisInFuture, long countDownInterval) {
        this.mMillisInFuture = millisInFuture;
        this.mCountdownInterval = countDownInterval;
    }

    public final synchronized void cancel() {
        this.mCancelled = true;
        this.mHandler.removeMessages(1);
    }

    public final synchronized CountDownTimer start() {
        this.mCancelled = false;
        if (this.mMillisInFuture <= 0) {
            onFinish();
            return this;
        }
        this.mStopTimeInFuture = SystemClock.elapsedRealtime() + this.mMillisInFuture;
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
        return this;
    }
}
