package com.android.server.security.deviceusage;

import android.app.AlarmManager;
import android.app.AlarmManager.OnAlarmListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Slog;

public class ActivationMonitor {
    private static final int MSG_START_MONITOR = 10;
    public static final long SIX_HOURS_MS = 21600000;
    private static final long START_DELAY = 60000;
    private static final String TAG = "ActivationMonitor";
    private long ACTIVATION_DURATION_MS = SIX_HOURS_MS;
    private boolean hasSimCard = false;
    private OnAlarmListener mAlarmListener = new OnAlarmListener() {
        public void onAlarm() {
            Slog.i(ActivationMonitor.TAG, "Report Activation");
            ActivationRecorder.record();
            ActivationRecorder.report(ActivationMonitor.this.mContext);
            ActivationMonitor.this.mContext.unregisterReceiver(ActivationMonitor.this.mSimReceiver);
            ActivationMonitor.this.mHandler.removeMessages(10);
        }
    };
    private Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (10 == msg.what) {
                Slog.i(ActivationMonitor.TAG, "start monitor");
                ActivationMonitor.this.mContext.registerReceiver(ActivationMonitor.this.mSimReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
                ActivationMonitor.this.countDownIfHasSim();
                removeMessages(10);
            }
        }
    };
    private SimReceiver mSimReceiver = new SimReceiver(this, null);

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

    private class SimReceiver extends BroadcastReceiver {
        static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";

        /* synthetic */ SimReceiver(ActivationMonitor this$0, SimReceiver -this1) {
            this();
        }

        private SimReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) {
                Slog.w(ActivationMonitor.TAG, "Intent or Action is null");
                return;
            }
            if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
                ActivationMonitor.this.countDownIfHasSim();
            }
        }
    }

    public ActivationMonitor(Context context) {
        this.mContext = context;
    }

    public void start() {
        if (ActivationRecorder.isActivated()) {
            Slog.i(TAG, "Device already activated");
        } else {
            this.mHandler.sendEmptyMessageDelayed(10, 60000);
        }
    }

    public void reStart(long duration) {
        if (duration <= SIX_HOURS_MS && duration >= 0) {
            this.ACTIVATION_DURATION_MS = duration;
            Slog.i(TAG, "reStart : " + this.ACTIVATION_DURATION_MS);
            this.mHandler.sendEmptyMessage(10);
        }
    }

    public boolean isActivated() {
        return ActivationRecorder.isActivated();
    }

    public void resetActivation() {
        HwOEMInfoUtil.resetActivation();
    }

    private void countDownIfHasSim() {
        TelephonyManager tm = (TelephonyManager) this.mContext.getSystemService("phone");
        if (tm == null) {
            Slog.e(TAG, "SIM state cannot be detected");
            return;
        }
        AlarmManager am = (AlarmManager) this.mContext.getSystemService("alarm");
        if (1 == tm.getSimState()) {
            am.cancel(this.mAlarmListener);
            this.hasSimCard = false;
        } else if (!this.hasSimCard) {
            am.set(2, this.ACTIVATION_DURATION_MS + SystemClock.elapsedRealtime(), "ACTIVATION_COUNTING", this.mAlarmListener, this.mHandler);
            this.hasSimCard = true;
        }
    }
}
