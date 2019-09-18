package com.android.server.security.deviceusage;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.List;

public class ActivationMonitor {
    private static final String ACTION_CHECK_POINT = "com.android.server.security.deviceusage.ACTION_CHECK_POINT";
    private static final long CHECK_POINT_GAP = 3600000;
    private static final long CONNECTION_TIME_DEFAULT = 0;
    /* access modifiers changed from: private */
    public static final boolean IS_TABLET = "tablet".equals(SystemProperties.get("ro.build.characteristics", MemoryConstant.MEM_SCENE_DEFAULT));
    private static final int MINIMUM_SSID_LENGTH = 2;
    private static final int MSG_START_MONITOR = 10;
    private static final int NETWORK_NONE = -1;
    private static final int NETWORK_WIFI = 1;
    private static final String SETTINGS_KEY_CONNECTION_TIME = "connection_time_for_activation";
    static final long SIX_HOURS_MS = 21600000;
    private static final long START_DELAY = 60000;
    private static final String TAG = "ActivationMonitor";
    private long mActivationDuration = SIX_HOURS_MS;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mHasSimCard = false;
    /* access modifiers changed from: private */
    public IntergrateReceiver mIntergrateReceiver = new IntergrateReceiver();
    /* access modifiers changed from: private */
    public boolean mIsBroadcastRegistered = false;
    private boolean mIsNetworkConnected;
    private boolean mIsWifiOnly = false;
    private long mLastCheckTime;
    private AlarmManager.OnAlarmListener mNetChangeListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            Slog.i(ActivationMonitor.TAG, "Report Activation");
            ActivationRecorder.record();
            ActivationRecorder.report(ActivationMonitor.this.mContext);
            if (ActivationMonitor.this.mIsBroadcastRegistered) {
                ActivationMonitor.this.mContext.unregisterReceiver(ActivationMonitor.this.mIntergrateReceiver);
                boolean unused = ActivationMonitor.this.mIsBroadcastRegistered = false;
            }
            if (ActivationMonitor.IS_TABLET) {
                ActivationMonitor.this.cancleCheckPerHour();
            }
            ActivationMonitor.this.mHandler.removeMessages(10);
        }
    };
    private PendingIntent mPendingIntent;
    private AlarmManager.OnAlarmListener mSimChangeListener = new AlarmManager.OnAlarmListener() {
        public void onAlarm() {
            Slog.i(ActivationMonitor.TAG, "Report Activation");
            ActivationRecorder.record();
            ActivationRecorder.report(ActivationMonitor.this.mContext);
            if (ActivationMonitor.this.mIsBroadcastRegistered) {
                ActivationMonitor.this.mContext.unregisterReceiver(ActivationMonitor.this.mIntergrateReceiver);
                boolean unused = ActivationMonitor.this.mIsBroadcastRegistered = false;
            }
            if (ActivationMonitor.IS_TABLET) {
                ActivationMonitor.this.cancleCheckPerHour();
            }
            ActivationMonitor.this.mHandler.removeMessages(10);
        }
    };

    private static class ActivationRecorder {
        private static final String ACTION_WARRANTY_DETECTED = "com.huawei.action.ACTION_ACTIVATION_DETECTED";
        private static final String RECEIVER_CLASS_NAME = "com.huawei.android.hwouc.biz.impl.reveiver.DeviceActivatedReceiver";
        private static final String RECEIVER_PACKAGE_NAME = "com.huawei.android.hwouc";
        private static final String RECEIVE_WARRANTY_REPORT = "com.huawei.permission.RECEIVE_ACTIVATION_REPORT";

        private ActivationRecorder() {
        }

        static void record() {
            HwOEMInfoUtil.setActivated();
        }

        static boolean isActivated() {
            return HwOEMInfoUtil.getActivationStatus();
        }

        static void report(Context context) {
            Intent intent = new Intent(ACTION_WARRANTY_DETECTED);
            intent.setClassName(RECEIVER_PACKAGE_NAME, RECEIVER_CLASS_NAME);
            context.sendBroadcast(intent, RECEIVE_WARRANTY_REPORT);
        }
    }

    private class IntergrateReceiver extends BroadcastReceiver {
        static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";

        private IntergrateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                Slog.w(ActivationMonitor.TAG, "Intent or Action is null");
                return;
            }
            String action = intent.getAction();
            char c = 65535;
            int hashCode = action.hashCode();
            if (hashCode != -1325911614) {
                if (hashCode != -1172645946) {
                    if (hashCode == -229777127 && action.equals(ACTION_SIM_STATE_CHANGED)) {
                        c = 0;
                    }
                } else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                    c = 1;
                }
            } else if (action.equals(ActivationMonitor.ACTION_CHECK_POINT)) {
                c = 2;
            }
            switch (c) {
                case 0:
                    ActivationMonitor.this.countDownIfHasSim();
                    break;
                case 1:
                case 2:
                    ActivationMonitor.this.onNetStatusChanged(ActivationMonitor.getNetWorkState(context));
                    break;
                default:
                    Slog.i(ActivationMonitor.TAG, "Unidentified broadcast!");
                    break;
            }
        }
    }

    private class MyHandler extends Handler {
        private MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                if (!ActivationMonitor.this.mIsBroadcastRegistered) {
                    Slog.i(ActivationMonitor.TAG, "start monitor");
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
                    if (ActivationMonitor.IS_TABLET) {
                        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                        intentFilter.addAction(ActivationMonitor.ACTION_CHECK_POINT);
                    }
                    ActivationMonitor.this.mContext.registerReceiver(ActivationMonitor.this.mIntergrateReceiver, intentFilter);
                    boolean unused = ActivationMonitor.this.mIsBroadcastRegistered = true;
                }
                ActivationMonitor.this.countDownIfHasSim();
                if (ActivationMonitor.IS_TABLET) {
                    ActivationMonitor.this.checkNetworkPerHour();
                }
                removeMessages(10);
            }
        }
    }

    private void saveConnectionTime(long time) {
        if (!Settings.Global.putLong(this.mContext.getContentResolver(), SETTINGS_KEY_CONNECTION_TIME, time)) {
            Slog.e(TAG, "write connection time to settings failed!");
            return;
        }
        Slog.i(TAG, "mConnection Time Saved: " + time);
    }

    private long getConnectionTime() {
        return Settings.Global.getLong(this.mContext.getContentResolver(), SETTINGS_KEY_CONNECTION_TIME, 0);
    }

    public ActivationMonitor(Context context) {
        this.mContext = context;
    }

    public void start() {
        if (ActivationRecorder.isActivated()) {
            Slog.i(TAG, "Device already activated");
            return;
        }
        if (this.mHandlerThread == null) {
            this.mHandlerThread = new HandlerThread(TAG);
            this.mHandlerThread.start();
        }
        if (this.mHandler == null) {
            this.mHandler = new MyHandler(this.mHandlerThread.getLooper());
        }
        this.mHandler.sendEmptyMessageDelayed(10, 60000);
        this.mIsWifiOnly = isWifiOnly();
    }

    public void reStart(long duration) {
        if (duration <= SIX_HOURS_MS && duration >= 0) {
            this.mActivationDuration = duration;
            Slog.i(TAG, "reStart : " + this.mActivationDuration);
            if (this.mHandlerThread == null) {
                this.mHandlerThread = new HandlerThread(TAG);
                this.mHandlerThread.start();
            }
            if (this.mHandler == null) {
                this.mHandler = new MyHandler(this.mHandlerThread.getLooper());
            }
            this.mHandler.sendEmptyMessage(10);
            this.mIsWifiOnly = isWifiOnly();
        }
    }

    public boolean isActivated() {
        return ActivationRecorder.isActivated();
    }

    public void resetActivation() {
        HwOEMInfoUtil.resetActivation();
    }

    /* access modifiers changed from: private */
    public void countDownIfHasSim() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager == null) {
            Slog.e(TAG, "SIM state cannot be detected");
            return;
        }
        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        Slog.i(TAG, "SIM state is " + telephonyManager.getSimState());
        if (1 == telephonyManager.getSimState() || this.mIsWifiOnly) {
            alarmManager.cancel(this.mSimChangeListener);
            this.mHasSimCard = false;
            Slog.i(TAG, "SIM removed, count down stop");
        } else if (!this.mHasSimCard) {
            AlarmManager alarmManager2 = alarmManager;
            alarmManager2.set(2, this.mActivationDuration + SystemClock.elapsedRealtime(), "ACTIVATION_COUNTING", this.mSimChangeListener, this.mHandler);
            this.mHasSimCard = true;
            Slog.i(TAG, "SIM inserted, count down start");
        }
    }

    /* access modifiers changed from: private */
    public void checkNetworkPerHour() {
        if (((ConnectivityManager) this.mContext.getSystemService("connectivity")) == null) {
            Slog.w(TAG, "Network state cannot be detected");
            return;
        }
        int currentNetState = getNetWorkState(this.mContext);
        this.mIsNetworkConnected = currentNetState != -1;
        long currentTimePoint = SystemClock.elapsedRealtime();
        Slog.i(TAG, "checkNetworkPerHour currentNetState is " + currentNetState + ", currentTimePoint is " + currentTimePoint);
        this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_CHECK_POINT), 268435456);
        ((AlarmManager) this.mContext.getSystemService("alarm")).setInexactRepeating(3, currentTimePoint, 3600000, this.mPendingIntent);
    }

    /* access modifiers changed from: private */
    public void cancleCheckPerHour() {
        if (this.mPendingIntent != null) {
            ((AlarmManager) this.mContext.getSystemService("alarm")).cancel(this.mPendingIntent);
        }
        Slog.i(TAG, "cancleCheckPerHour complete");
    }

    private void checkIfActivateByTime(long totaltime) {
        if (totaltime >= this.mActivationDuration) {
            Slog.i(TAG, "Report Activation");
            ActivationRecorder.record();
            ActivationRecorder.report(this.mContext);
            cancleCheckPerHour();
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (ActivationMonitor.this.mIsBroadcastRegistered) {
                        ActivationMonitor.this.mContext.unregisterReceiver(ActivationMonitor.this.mIntergrateReceiver);
                        boolean unused = ActivationMonitor.this.mIsBroadcastRegistered = false;
                    }
                }
            });
            this.mHandler.removeMessages(10);
        }
        Slog.i(TAG, "checkIfActivateByTime complete");
    }

    /* access modifiers changed from: private */
    public void onNetStatusChanged(int networkstate) {
        long totalConnectionTime = getConnectionTime();
        Slog.i(TAG, "onNetStatusChanged totalConnectionTime is " + totalConnectionTime);
        AlarmManager alarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        if (networkstate == 1 && !checkIfCurrentWifiHasPassword()) {
            Slog.i(TAG, "onNetStatusChanged Free Wifi Connected!");
            if (!this.mIsNetworkConnected) {
                checkIfActivateByTime(totalConnectionTime);
            } else {
                onNetDisconnected(totalConnectionTime, this.mLastCheckTime);
                this.mIsNetworkConnected = false;
                alarmManager.cancel(this.mNetChangeListener);
            }
        } else if (this.mIsNetworkConnected || -1 == networkstate) {
            if (this.mIsNetworkConnected && -1 == networkstate) {
                onNetDisconnected(totalConnectionTime, this.mLastCheckTime);
                this.mIsNetworkConnected = false;
                alarmManager.cancel(this.mNetChangeListener);
            }
            if (this.mIsNetworkConnected && -1 != networkstate) {
                long currentCheckTime = SystemClock.elapsedRealtime();
                onNetStable(totalConnectionTime, currentCheckTime, this.mLastCheckTime);
                this.mLastCheckTime = currentCheckTime;
            }
            checkIfActivateByTime(getConnectionTime());
        } else {
            this.mLastCheckTime = SystemClock.elapsedRealtime();
            this.mIsNetworkConnected = true;
            onNetConnected(alarmManager, totalConnectionTime, this.mLastCheckTime);
        }
    }

    /* access modifiers changed from: private */
    public static int getNetWorkState(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            switch (activeNetworkInfo.getType()) {
                case 0:
                    return 0;
                case 1:
                    return 1;
            }
        }
        return -1;
    }

    private boolean checkIfCurrentWifiHasPassword() {
        WifiManager wifiManager = (WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null) {
            return true;
        }
        List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
        String currentSSID = wifiInfo.getSSID();
        if (currentSSID == null || currentSSID.length() <= 2) {
            return true;
        }
        String currentSSID2 = currentSSID.replace("\"", "");
        if (wifiConfigurationList == null || wifiConfigurationList.size() == 0) {
            return true;
        }
        for (WifiConfiguration configuration : wifiConfigurationList) {
            if (configuration != null && configuration.status == 0) {
                String ssid = null;
                if (!TextUtils.isEmpty(configuration.SSID)) {
                    ssid = configuration.SSID.replace("\"", "");
                }
                if (currentSSID2.equalsIgnoreCase(ssid)) {
                    Slog.i(TAG, "current WIFI ssid is " + ssid);
                    return true ^ configuration.allowedKeyManagement.get(0);
                }
            }
        }
        return true;
    }

    private void onNetConnected(AlarmManager alarmManager, long totalConnectionTime, long lastCheckTime) {
        long timeRemianing = this.mActivationDuration - totalConnectionTime;
        if (timeRemianing <= 0) {
            checkIfActivateByTime(this.mActivationDuration);
            return;
        }
        Slog.i(TAG, "onNetStatusChanged Connection established! mLastCheckTime is " + lastCheckTime + ", timeRemianing is " + timeRemianing);
        if (timeRemianing > 0) {
            AlarmManager alarmManager2 = alarmManager;
            alarmManager2.set(2, SystemClock.elapsedRealtime() + timeRemianing, "ACTIVATION_COUNTING", this.mNetChangeListener, this.mHandler);
        }
    }

    private void onNetDisconnected(long totalConnectionTime, long lastCheckTime) {
        long currentCheckTime = SystemClock.elapsedRealtime();
        long totalConnectionTime2 = totalConnectionTime + (currentCheckTime - lastCheckTime);
        Slog.i(TAG, "onNetStatusChanged Connection down! currentCheckTime is " + currentCheckTime + ", totalConnectionTime is " + totalConnectionTime2);
        saveConnectionTime(totalConnectionTime2);
    }

    private void onNetStable(long totalConnectionTime, long currentCheckTime, long lastCheckTime) {
        long totalConnectionTime2 = totalConnectionTime + (currentCheckTime - lastCheckTime);
        Slog.i(TAG, "onNetStatusChanged Connection steady, currentCheckTime is " + currentCheckTime + ", totalConnectionTime is " + totalConnectionTime2);
        saveConnectionTime(totalConnectionTime2);
    }

    private boolean isWifiOnly() {
        ConnectivityManager cm = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (cm != null) {
            return !cm.isNetworkSupported(0);
        }
        return false;
    }
}
