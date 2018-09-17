package com.android.server.input;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DMDUtils {
    private static final int DMD_DELAY = 100;
    public static final int DMD_GO_LAUNCHER_ERROR = 912001014;
    public static final int DMD_GO_RECENT_ERROOR = 912001015;
    public static final int DMD_NO_ERROR = 0;
    private static final String DMD_WORKING_THREAD = "DMD_woring_thread";
    private static final int MAX_TRY = 10;
    private static final String TAG = DMDUtils.class.getSimpleName();
    private static final Object dmdLock = new Object();
    private static boolean isDMDWorking = false;
    private static int mCurrentDMDType = 0;
    private Runnable dmdWorking = new Runnable() {
        int ret = -1;
        int time = 0;

        public void run() {
            while (this.time < 10) {
                this.time++;
                this.ret = DMDUtils.this.checkCurrentStatus();
                if (this.ret == 0) {
                    break;
                } else if (this.time < 10) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.e(DMDUtils.TAG, "sleep error");
                    }
                } else {
                    this.ret = DMDUtils.mCurrentDMDType;
                }
            }
            DMDUtils.this.sendDMDLogToService(this.ret);
            this.time = 0;
            this.ret = -1;
            synchronized (DMDUtils.dmdLock) {
                DMDUtils.isDMDWorking = false;
            }
        }
    };
    private ActivityManager mAm = null;
    private Context mContext;

    public DMDUtils(Context context) {
        this.mContext = context;
        this.mAm = (ActivityManager) context.getSystemService("activity");
    }

    public boolean isTopTask(int stackId) {
        boolean z = false;
        if (this.mAm == null) {
            return false;
        }
        List<RunningTaskInfo> tasks = this.mAm.getRunningTasks(1);
        if (!(tasks == null || (tasks.isEmpty() ^ 1) == 0)) {
            RunningTaskInfo topTask = (RunningTaskInfo) tasks.get(0);
            if (topTask != null) {
                if (topTask.stackId == stackId) {
                    z = getHomes().contains(topTask.topActivity.getPackageName()) ^ 1;
                }
                return z;
            }
        }
        return false;
    }

    private boolean isTopTaskHome() {
        if (this.mAm == null) {
            return false;
        }
        List<RunningTaskInfo> tasks = this.mAm.getRunningTasks(1);
        if (!(tasks == null || (tasks.isEmpty() ^ 1) == 0)) {
            RunningTaskInfo topTask = (RunningTaskInfo) tasks.get(0);
            if (topTask != null) {
                return getHomes().contains(topTask.topActivity.getPackageName());
            }
        }
        return false;
    }

    public List<String> getHomes() {
        List<String> packages = new ArrayList();
        PackageManager packageManager = this.mContext.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        for (ResolveInfo info : packageManager.queryIntentActivities(intent, 65536)) {
            packages.add(info.activityInfo.packageName);
        }
        return packages;
    }

    public boolean doDMDDetect(int type) {
        boolean ret = false;
        synchronized (dmdLock) {
            if (!isDMDWorking) {
                mCurrentDMDType = type;
                isDMDWorking = true;
                Thread thread = new Thread(this.dmdWorking);
                thread.setName(DMD_WORKING_THREAD);
                thread.start();
                ret = true;
            }
        }
        return ret;
    }

    private int checkCurrentStatus() {
        switch (mCurrentDMDType) {
            case DMD_GO_LAUNCHER_ERROR /*912001014*/:
                if (isTopTaskHome()) {
                    return 0;
                }
                return -1;
            case DMD_GO_RECENT_ERROOR /*912001015*/:
                if (isTopTask(5)) {
                    return 0;
                }
                return -1;
            default:
                return -1;
        }
    }

    private void sendDMDLogToService(int errorType) {
        if (errorType != 0) {
            Log.e(TAG, "DMD ERROR: " + errorType);
            switch (errorType) {
                case DMD_GO_LAUNCHER_ERROR /*912001014*/:
                    EventStream eStream1 = IMonitor.openEventStream(errorType);
                    eStream1.setParam((short) 0, 6);
                    eStream1.setParam((short) 1, "Default");
                    eStream1.setParam((short) 2, "Default");
                    eStream1.setParam((short) 3, "Default");
                    IMonitor.sendEvent(eStream1);
                    IMonitor.closeEventStream(eStream1);
                    return;
                case DMD_GO_RECENT_ERROOR /*912001015*/:
                    EventStream eStream2 = IMonitor.openEventStream(errorType);
                    eStream2.setParam((short) 0, 6);
                    eStream2.setParam((short) 1, "Default");
                    eStream2.setParam((short) 2, "Default");
                    eStream2.setParam((short) 3, "Default");
                    IMonitor.sendEvent(eStream2);
                    IMonitor.closeEventStream(eStream2);
                    return;
                default:
                    return;
            }
        }
    }
}
