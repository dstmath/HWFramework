package com.android.server.wifi.tcpack;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.RemoteException;
import android.util.wifi.HwHiLog;
import java.util.List;

public class TcpAckMonitor {
    private static final String CHARIOT_APK = "com.ixia.ixchariot";
    private static final int CURRENT_TASK_NUMBER = 0;
    private static final String INVALID_STRING = "";
    private static final int RUNNING_TASK_NUMBER = 1;
    private static final String TAG = "TcpAckMonitor";
    private ActionReceiver mActionReceiver = null;
    private ActivityManager mActivityManager = null;
    private Context mContext;
    private Handler mHandler;
    private HwProcessObserver mHwProcessObserver = null;

    public TcpAckMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        registerProcessObserver();
        registerActionReceiver();
    }

    private void registerActionReceiver() {
        this.mActionReceiver = new ActionReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        this.mContext.registerReceiver(this.mActionReceiver, filter);
    }

    /* access modifiers changed from: private */
    public class ActionReceiver extends BroadcastReceiver {
        private ActionReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    TcpAckMonitor.this.mHandler.sendEmptyMessage(3);
                } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                    TcpAckMonitor.this.mHandler.sendEmptyMessage(4);
                } else {
                    HwHiLog.d(TcpAckMonitor.TAG, false, "TcpAckBcastReceiver unknown action: %{public}s", new Object[]{action});
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class HwProcessObserver extends IProcessObserver.Stub {
        private HwProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean isForegroundActivities) {
            boolean isChariotApp = false;
            if (isForegroundActivities) {
                if (TcpAckMonitor.CHARIOT_APK.equals(TcpAckMonitor.this.getAppNameUid(uid))) {
                    isChariotApp = true;
                }
            } else if (TcpAckMonitor.CHARIOT_APK.equals(TcpAckMonitor.this.getTopPackageName())) {
                isChariotApp = true;
            }
            TcpAckMonitor.this.mHandler.sendMessage(TcpAckMonitor.this.mHandler.obtainMessage(2, TcpAckUtils.booleanToInt(isChariotApp), 0));
        }

        public void onProcessDied(int pid, int uid) {
        }

        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) {
        }
    }

    private void registerProcessObserver() {
        this.mHwProcessObserver = new HwProcessObserver();
        try {
            ActivityManagerNative.getDefault().registerProcessObserver(this.mHwProcessObserver);
        } catch (RemoteException e) {
            HwHiLog.e(TAG, false, "register process observer failed", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getAppNameUid(int uid) {
        List<ActivityManager.RunningAppProcessInfo> appProcessList;
        ActivityManager activityManager = this.mActivityManager;
        if (activityManager == null || (appProcessList = activityManager.getRunningAppProcesses()) == null || appProcessList.isEmpty()) {
            return "";
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
            if (appProcess.uid == uid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getTopPackageName() {
        ActivityManager.RunningTaskInfo runningTask;
        List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        if (tasks == null || tasks.isEmpty() || (runningTask = tasks.get(0)) == null || runningTask.topActivity == null) {
            return "";
        }
        return runningTask.topActivity.getPackageName();
    }
}
