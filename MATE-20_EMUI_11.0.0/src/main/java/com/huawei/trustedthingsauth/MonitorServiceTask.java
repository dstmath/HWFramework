package com.huawei.trustedthingsauth;

import java.util.Timer;
import java.util.TimerTask;

public class MonitorServiceTask {
    private static final long MAX_SERVICE_IDLE_TIME = 180000;
    private static final String TAG = "MonitorServiceTask";
    private static final long TIMER_DELAY = 0;
    private static final long TIMER_PERIOD = 60000;
    private static volatile MonitorServiceTask instance;
    private boolean isSetTimer = false;
    private long lastExecutionTime = System.currentTimeMillis();
    private TimeoutCallback timeoutCallback;
    private Timer timer = new Timer();
    private ServiceTimerTask timerTask = null;

    /* access modifiers changed from: package-private */
    public interface TimeoutCallback {
        void onResult();
    }

    private MonitorServiceTask() {
    }

    public static MonitorServiceTask getInstance() {
        if (instance == null) {
            synchronized (MonitorServiceTask.class) {
                if (instance == null) {
                    instance = new MonitorServiceTask();
                }
            }
        }
        return instance;
    }

    public void startOrRestartTimerTask(TimeoutCallback callback) {
        if (callback == null) {
            LogUtil.error(TAG, "startOrRestartTimerTask callback is null");
            return;
        }
        this.lastExecutionTime = System.currentTimeMillis();
        this.timeoutCallback = callback;
        if (!this.isSetTimer) {
            timerStart();
        }
    }

    private void timerStart() {
        LogUtil.debug(TAG, "timerStart");
        this.timer.purge();
        if (this.timerTask == null) {
            this.timerTask = new ServiceTimerTask();
        }
        this.timer.schedule(this.timerTask, TIMER_DELAY, TIMER_PERIOD);
        this.isSetTimer = true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onTimeout() {
        LogUtil.debug(TAG, "onTimeout");
        ServiceTimerTask serviceTimerTask = this.timerTask;
        if (serviceTimerTask != null) {
            serviceTimerTask.cancel();
            this.timerTask = null;
        }
        this.isSetTimer = false;
        TimeoutCallback timeoutCallback2 = this.timeoutCallback;
        if (timeoutCallback2 != null) {
            timeoutCallback2.onResult();
            this.timeoutCallback = null;
        }
    }

    /* access modifiers changed from: private */
    public class ServiceTimerTask extends TimerTask {
        private ServiceTimerTask() {
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            if (System.currentTimeMillis() - MonitorServiceTask.this.lastExecutionTime > MonitorServiceTask.MAX_SERVICE_IDLE_TIME) {
                MonitorServiceTask.this.onTimeout();
            }
        }
    }
}
