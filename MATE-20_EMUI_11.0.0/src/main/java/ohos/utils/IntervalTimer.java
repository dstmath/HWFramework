package ohos.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class IntervalTimer {
    private final long duration;
    private volatile long finishTime;
    private final long interval;
    private volatile ScheduledExecutorService timer;

    public abstract void onFinish();

    public abstract void onInterval(long j);

    public IntervalTimer(long j, long j2) {
        this.duration = j;
        this.interval = j2;
    }

    public final void schedule() {
        long j = this.interval;
        if (j <= 0) {
            throw new IllegalArgumentException("interval cannot be less than or equal to 0");
        } else if (j <= this.duration) {
            long currentTimeMillis = System.currentTimeMillis();
            long j2 = this.duration;
            this.finishTime = currentTimeMillis + j2;
            long j3 = this.interval;
            startTimer(j3, j3, j2);
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
        this.timer.shutdown();
    }

    /* access modifiers changed from: private */
    public class IntervalTask implements Runnable {
        private static final int FAULT_TOLERANT_INTERVAL = 3;
        private final Object lock;

        private IntervalTask() {
            this.lock = new Object();
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (this.lock) {
                long currentTimeMillis = IntervalTimer.this.finishTime - System.currentTimeMillis();
                long currentTimeMillis2 = System.currentTimeMillis();
                IntervalTimer.this.onInterval(currentTimeMillis < 0 ? 0 : currentTimeMillis);
                if (IntervalTimer.this.interval >= 3) {
                    long currentTimeMillis3 = System.currentTimeMillis() - currentTimeMillis2;
                    if (currentTimeMillis3 > IntervalTimer.this.interval) {
                        long j = IntervalTimer.this.interval - currentTimeMillis3;
                        while (j < 0) {
                            j += IntervalTimer.this.interval;
                        }
                        long j2 = currentTimeMillis - currentTimeMillis3;
                        if (j2 > 0) {
                            IntervalTimer.this.timer.shutdown();
                            IntervalTimer.this.startTimer(j, ((currentTimeMillis3 / IntervalTimer.this.interval) + 1) * IntervalTimer.this.interval, j2);
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
