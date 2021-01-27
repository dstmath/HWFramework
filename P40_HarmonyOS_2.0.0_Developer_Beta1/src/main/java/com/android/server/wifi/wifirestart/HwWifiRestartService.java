package com.android.server.wifi.wifirestart;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.util.Calendar;

public class HwWifiRestartService {
    private static final long DOWNLOAD_BYTE_THRESHOLD = 1048576;
    private static final Object LOCK_OBJECT = new Object();
    private static final int MSG_CLOSE_WIFI = 3;
    private static final int MSG_GET_TRAFFICSTATS = 1;
    private static final int MSG_OPEN_WIFI = 2;
    private static final int MSG_SCREEN_OFF = 4;
    private static final int MSG_SCREEN_ON = 5;
    private static final int SDK_THRESHOLD = 19;
    private static final int SEND_DELAY_TIME = 1000;
    private static final String TAG = HwWifiRestartService.class.getSimpleName();
    private static final int TASK_DELAY_MINUTE = 30;
    private static final int TIME_AFTERNOON_21 = 21;
    private static final int TIME_AFTERNOON_24 = 24;
    private static final int TIME_MORNING_0 = 0;
    private static final int TIME_MORNING_5 = 5;
    private static volatile HwWifiRestartService sInstance;
    private volatile boolean isStartedTask;
    private AlarmListener mAlarmListener;
    private AlarmManager mAlarmManager;
    private Calendar mCalendar;
    private Context mContext;
    private Handler mLocalHandler;
    private int mRestartedDay = 0;
    private long mStartRxBytes = 0;
    private long mStartTxBytes = 0;
    private WifiManager mWifiManager;

    private HwWifiRestartService(Context context) {
        this.mContext = context;
        this.isStartedTask = false;
        this.mCalendar = Calendar.getInstance();
        this.mAlarmListener = new AlarmListener();
        initSystemService();
        initHandlerThread();
        registerScreenReceiver();
        Log.d(TAG, "HwWifiRestartService init success");
    }

    public static HwWifiRestartService getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK_OBJECT) {
                if (sInstance == null) {
                    sInstance = new HwWifiRestartService(context);
                }
            }
        }
        return sInstance;
    }

    private void initSystemService() {
        Context context = this.mContext;
        if (context == null) {
            Log.e(TAG, "initSystemService, mContext is null");
            return;
        }
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
    }

    private void registerScreenReceiver() {
        if (this.mContext == null) {
            Log.e(TAG, "registerScreenReceiver, mContext is null");
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        this.mContext.registerReceiver(new ScreenReceiver(), filter);
    }

    private void initHandlerThread() {
        HandlerThread handlerThread = new HandlerThread("HwRestartWifiService Thread");
        handlerThread.start();
        this.mLocalHandler = new LocalHandler(handlerThread.getLooper());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTrafficStats() {
        if (this.mStartTxBytes != 0 && this.mStartRxBytes != 0) {
            if ((TrafficStats.getTotalTxBytes() - this.mStartTxBytes) + (TrafficStats.getTotalRxBytes() - this.mStartRxBytes) < DOWNLOAD_BYTE_THRESHOLD) {
                Log.d(TAG, "updateTrafficStats, not exist background download, will process wifi restart");
                processWifiRestart();
                return;
            }
            Log.d(TAG, "updateTrafficStats, exist background download, and do not allow wifi restart");
        }
    }

    private void processWifiRestart() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            Log.e(TAG, "processWifiRestart, mWifiManager is null");
            return;
        }
        if (wifiManager.isWifiEnabled()) {
            this.mWifiManager.setWifiEnabled(false);
            this.mLocalHandler.sendEmptyMessageDelayed(2, 1000);
        } else {
            this.mWifiManager.setWifiEnabled(true);
            this.mLocalHandler.sendEmptyMessageDelayed(3, 1000);
        }
        this.mRestartedDay = this.mCalendar.get(6);
        Log.d(TAG, "process wifi restart success");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWiFiOpen() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            Log.e(TAG, "handleWiFiOpen, mWifiManager is null");
        } else {
            wifiManager.setWifiEnabled(true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWiFiClose() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            Log.e(TAG, "handleWiFiClose, mWifiManager is null");
        } else {
            wifiManager.setWifiEnabled(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOn() {
        stopAlarmTask();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleScreenOff() {
        Calendar calendar = this.mCalendar;
        if (calendar == null) {
            Log.e(TAG, "handleScreenOff, mCalendar is null");
            return;
        }
        int currentHour = calendar.get(11);
        if (currentHour >= 21) {
            this.mCalendar.add(11, 24 - currentHour);
            this.mCalendar.add(12, 30);
            startAlarmTask(this.mCalendar.getTimeInMillis());
            return;
        }
        int currentDay = this.mCalendar.get(6);
        if (currentHour >= 0 && currentHour <= 5) {
            if (currentDay == this.mRestartedDay) {
                Log.d(TAG, "handleScreenOff, already processed scheduled task");
                return;
            }
            this.mCalendar.add(12, 30);
            startAlarmTask(this.mCalendar.getTimeInMillis());
        }
    }

    private void startAlarmTask(long time) {
        if (this.mAlarmManager == null || this.mAlarmListener == null) {
            Log.e(TAG, "startAlarmTask, mAlarmManager or mAlarmListener is null");
            return;
        }
        if (this.isStartedTask) {
            this.mAlarmManager.cancel(this.mAlarmListener);
        }
        if (Build.VERSION.SDK_INT < 19) {
            this.mAlarmManager.set(0, time, TAG, this.mAlarmListener, null);
        } else {
            this.mAlarmManager.setExact(0, time, TAG, this.mAlarmListener, null);
        }
        this.isStartedTask = true;
        Log.d(TAG, "start alarm task success");
    }

    private void stopAlarmTask() {
        AlarmListener alarmListener;
        if (!this.isStartedTask) {
            Log.d(TAG, "stopAlarmTask, no task need stop");
            return;
        }
        AlarmManager alarmManager = this.mAlarmManager;
        if (alarmManager == null || (alarmListener = this.mAlarmListener) == null) {
            Log.e(TAG, "stopAlarmTask, mAlarmManager or mAlarmListener is null");
            return;
        }
        alarmManager.cancel(alarmListener);
        this.isStartedTask = false;
        Log.d(TAG, "stop alarm task success");
    }

    /* access modifiers changed from: private */
    public class AlarmListener implements AlarmManager.OnAlarmListener {
        private AlarmListener() {
        }

        @Override // android.app.AlarmManager.OnAlarmListener
        public void onAlarm() {
            if (HwWifiRestartService.this.mLocalHandler == null) {
                Log.e(HwWifiRestartService.TAG, "onAlarm, mLocalHandler is null");
                return;
            }
            HwWifiRestartService.this.mStartTxBytes = TrafficStats.getTotalTxBytes();
            HwWifiRestartService.this.mStartRxBytes = TrafficStats.getTotalRxBytes();
            HwWifiRestartService.this.mLocalHandler.sendEmptyMessageDelayed(1, 1000);
        }
    }

    /* access modifiers changed from: private */
    public class ScreenReceiver extends BroadcastReceiver {
        private ScreenReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || HwWifiRestartService.this.mLocalHandler == null) {
                Log.e(HwWifiRestartService.TAG, "onReceive, intent or mLocalHandler is null");
                return;
            }
            String action = intent.getAction();
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                HwWifiRestartService.this.mLocalHandler.sendEmptyMessage(4);
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                HwWifiRestartService.this.mLocalHandler.sendEmptyMessage(5);
            } else {
                String str = HwWifiRestartService.TAG;
                Log.d(str, "onReceive, unknown action : " + action);
            }
        }
    }

    /* access modifiers changed from: private */
    public class LocalHandler extends Handler {
        public LocalHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                HwWifiRestartService.this.updateTrafficStats();
            } else if (i == 2) {
                HwWifiRestartService.this.handleWiFiOpen();
            } else if (i == 3) {
                HwWifiRestartService.this.handleWiFiClose();
            } else if (i == 4) {
                HwWifiRestartService.this.handleScreenOff();
            } else if (i == 5) {
                HwWifiRestartService.this.handleScreenOn();
            }
        }
    }
}
