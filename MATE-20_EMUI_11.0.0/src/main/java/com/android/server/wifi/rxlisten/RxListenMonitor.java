package com.android.server.wifi.rxlisten;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.util.wifi.HwHiLog;
import com.android.server.hidata.appqoe.HwAPPQoEResourceManger;
import java.util.List;

public class RxListenMonitor {
    private static final int CURRENT_TASK_NUMBER = 0;
    private static final String INVALID_STRING = "";
    private static final int RUNNING_TASK_NUMBER = 1;
    private static final String TAG = "FSMonitor";
    private ActivityManager mActivityManager = null;
    private Context mContext;
    private Handler mHandler;
    private HwProcessObserver mHwProcessObserver = null;

    public RxListenMonitor(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        registerProcessObserver();
    }

    /* access modifiers changed from: private */
    public class HwProcessObserver extends IProcessObserver.Stub {
        private HwProcessObserver() {
        }

        public void onForegroundActivitiesChanged(int pid, int uid, boolean isForegroundActivities) {
            HwHiLog.d(RxListenMonitor.TAG, false, "onForegroundActivitiesChanged: %{public}d, %{public}d, %{public}s", new Object[]{Integer.valueOf(pid), Integer.valueOf(uid), String.valueOf(isForegroundActivities)});
            boolean isGameActivity = false;
            HwAPPQoEResourceManger qoeResourceManager = HwAPPQoEResourceManger.getInstance();
            if (isForegroundActivities) {
                String currentAppName = RxListenMonitor.this.getAppNameUid(uid);
                if (RxListenMonitor.this.isInGameScene(qoeResourceManager, currentAppName)) {
                    HwHiLog.d(RxListenMonitor.TAG, false, "activity switch to foreground: %{public}s", new Object[]{currentAppName});
                    isGameActivity = true;
                }
            } else {
                String currentAppName2 = RxListenMonitor.this.getTopPackageName();
                if (RxListenMonitor.this.isInGameScene(qoeResourceManager, currentAppName2)) {
                    HwHiLog.d(RxListenMonitor.TAG, false, "activity switch to background, current top app: %{public}s", new Object[]{currentAppName2});
                    isGameActivity = true;
                }
            }
            RxListenMonitor.this.mHandler.sendMessage(RxListenMonitor.this.mHandler.obtainMessage(2, RxListenUtils.booleanToInt(isGameActivity), 0));
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
        if (activityManager == null || (appProcessList = activityManager.getRunningAppProcesses()) == null) {
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
    private boolean isInGameScene(HwAPPQoEResourceManger qoeResourceManager, String appName) {
        if (qoeResourceManager == null || appName == null || qoeResourceManager.checkIsMonitorGameScence(appName) == null) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getTopPackageName() {
        List<ActivityManager.RunningTaskInfo> tasks = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningTasks(1);
        if (tasks == null || tasks.isEmpty() || tasks.get(0) == null || tasks.get(0).topActivity == null) {
            return "";
        }
        return tasks.get(0).topActivity.getPackageName();
    }
}
