package ohos.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class IntervalTimer {
    private volatile long duration;
    private volatile long interval;
    private volatile ScheduledExecutorService timer;

    public abstract void onFinish();

    public abstract void onInterval(long j);

    public IntervalTimer(long j, long j2) {
        this.duration = j;
        this.interval = j2;
    }

    public final void schedule() {
        if (this.interval <= 0) {
            throw new IllegalArgumentException("interval cannot be less than or equal to 0");
        } else if (this.interval <= this.duration) {
            startTimer(this.interval, this.interval, this.duration);
        } else {
            throw new IllegalArgumentException("interval cannot be larger than duration");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startTimer(long j, long j2, long j3) {
        this.timer = Executors.newSingleThreadScheduledExecutor();
        this.timer.scheduleAtFixedRate(new IntervalTask(), j, j2, TimeUnit.MILLISECONDS);
        this.timer.schedule(new FinishTask(), j3, TimeUnit.MILLISECONDS);
    }

    public final void cancel() {
        this.timer.shutdownNow();
    }

    /* access modifiers changed from: private */
    public class IntervalTask implements Runnable {
        private static final int FAULT_TOLERANT_INTERVAL = 10;
        private final Object lock;

        private IntervalTask() {
            this.lock = new Object();
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (this.lock) {
                IntervalTimer.this.duration -= IntervalTimer.this.interval;
                long currentTimeMillis = System.currentTimeMillis();
                IntervalTimer.this.onInterval(IntervalTimer.this.duration);
                if (IntervalTimer.this.interval >= 10) {
                    long currentTimeMillis2 = System.currentTimeMillis() - currentTimeMillis;
                    if (currentTimeMillis2 > IntervalTimer.this.interval) {
                        long j = IntervalTimer.this.interval - currentTimeMillis2;
                        while (j < 0) {
                            j += IntervalTimer.this.interval;
                        }
                        long j2 = IntervalTimer.this.duration - currentTimeMillis2;
                        if (j2 > 0) {
                            IntervalTimer.this.timer.shutdownNow();
                            IntervalTimer.this.interval = ((currentTimeMillis2 / IntervalTimer.this.interval) + 1) * IntervalTimer.this.interval;
                            IntervalTimer.this.startTimer(j, IntervalTimer.this.interval, j2);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class FinishTask implements Runnable {
        private FinishTask() {
        }

        @Override // java.lang.Runnable
        public void run() {
            IntervalTimer.this.timer.shutdown();
            IntervalTimer.this.onFinish();
        }
    }
}
