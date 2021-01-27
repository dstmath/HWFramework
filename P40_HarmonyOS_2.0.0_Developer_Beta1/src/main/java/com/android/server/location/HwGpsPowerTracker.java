package com.android.server.location;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import com.android.internal.os.BackgroundThread;
import com.android.server.LocationManagerServiceUtil;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwGpsPowerTracker {
    private static final long CHECK_TIME_INTERVEL = 120000;
    private static final int CMD_CHECK_HIGH_POWER = 2001;
    private static final int DEFAULTSIZE = 16;
    public static final String DEL_PKG = "pkg";
    private static final boolean ENABLE_GPS_POWER_TRACK = SystemProperties.getBoolean("ro.config.hw_gps_power_track", true);
    private static final int EVENT_LOCATION_BEGIN = 1000;
    private static final int EVENT_LOCATION_END = 1001;
    public static final int EVENT_REMOVE_PACKAGE_LOCATION = 3001;
    private static final String PACKAGE_NAME_ANDROID = "android";
    private static final String PACKAGE_NAME_FUSED = "com.android.location.fused";
    private static final String PACKAGE_NAME_HMS = "com.huawei.hms";
    private static final String PACKAGE_NAME_HWID = "com.huawei.hwid";
    private static final String TAG = "HwGpsPowerTracker";
    private HashMap<String, GpsAppTracker> mAppTrackers;
    private Context mContext;
    private LocationManagerServiceUtil mLocationManagerServiceUtil;
    private final Object mLock = new Object();
    private BroadcastReceiver mPackegeChangeReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.HwGpsPowerTracker.AnonymousClass1 */

        private String getPackageName(Intent intent) {
            Uri uri = intent.getData();
            if (uri != null) {
                return uri.getSchemeSpecificPart();
            }
            return null;
        }

        private boolean explicitlyStopped(String pkg) {
            PackageManager pm = HwGpsPowerTracker.this.mContext.getPackageManager();
            if (pm == null) {
                return false;
            }
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                if (ai == null || (ai.flags & 2097152) == 0) {
                    return false;
                }
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                LBSLog.w(HwGpsPowerTracker.TAG, false, "package info not found:%{public}s", pkg);
                return false;
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                LBSLog.i(HwGpsPowerTracker.TAG, false, "LocalReceiver receives:%{public}s", action);
                if ("android.intent.action.PACKAGE_DATA_CLEARED".equals(action) || "android.intent.action.PACKAGE_RESTARTED".equals(action)) {
                    String pkg = getPackageName(intent);
                    if (explicitlyStopped(pkg)) {
                        HwGpsPowerTracker.this.removeGpsLocation(pkg);
                    }
                }
            }
        }
    };
    private PowerHandler mPowerHandler;
    private Handler mRemoveLocationHandler;
    private HandlerThread mThread;

    public HwGpsPowerTracker(Context context) {
        if (ENABLE_GPS_POWER_TRACK) {
            this.mThread = new HandlerThread("LocationManagerPowerTracker");
            this.mThread.start();
            this.mPowerHandler = new PowerHandler(this.mThread.getLooper());
            this.mAppTrackers = new HashMap<>(16);
            this.mContext = context;
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
            packageFilter.addAction("android.intent.action.PACKAGE_RESTARTED");
            packageFilter.addDataScheme("package");
            this.mContext.registerReceiverAsUser(this.mPackegeChangeReceiver, UserHandle.OWNER, packageFilter, null, null);
            this.mRemoveLocationHandler = new RemoveLocationHandler(BackgroundThread.get().getLooper());
            this.mLocationManagerServiceUtil = LocationManagerServiceUtil.getDefault();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeGpsLocation(String pkg) {
        synchronized (this.mLock) {
            GpsAppTracker appTracker = this.mAppTrackers.get(pkg);
            if (appTracker != null) {
                appTracker.removeLocation();
            }
        }
    }

    private class PowerHandler extends Handler {
        PowerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            LBSLog.i(HwGpsPowerTracker.TAG, false, "PowerHandler msg.what: %{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i != 1000) {
                if (i == 1001) {
                    synchronized (HwGpsPowerTracker.this.mLock) {
                        if (HwGpsPowerTracker.this.noLocationRunCheck()) {
                            removeMessages(2001);
                        }
                        HwGpsPowerTracker.this.clearAppTrackerData();
                    }
                } else if (i == 2001) {
                    synchronized (HwGpsPowerTracker.this.mLock) {
                        HwGpsPowerTracker.this.checkHighPower();
                        if (!HwGpsPowerTracker.this.noLocationRunCheck()) {
                            LBSLog.i(HwGpsPowerTracker.TAG, false, "send CMD_CHECK_HIGH_POWER", new Object[0]);
                            sendEmptyMessageDelayed(2001, HwGpsPowerTracker.CHECK_TIME_INTERVEL);
                        }
                    }
                }
            } else if (!hasMessages(2001)) {
                LBSLog.i(HwGpsPowerTracker.TAG, false, "send CMD_CHECK_HIGH_POWER", new Object[0]);
                sendEmptyMessageDelayed(2001, HwGpsPowerTracker.CHECK_TIME_INTERVEL);
            }
        }
    }

    private class RemoveLocationHandler extends Handler {
        RemoveLocationHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            String pkg;
            if (msg.what == 3001 && (pkg = msg.getData().getString("pkg")) != null) {
                HwGpsPowerTracker.this.mLocationManagerServiceUtil.clearPackageLocation(pkg);
            }
            super.handleMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearAppTrackerData() {
        synchronized (this.mLock) {
            if (this.mAppTrackers.size() != 0) {
                ArrayList<String> couldClearApps = new ArrayList<>(16);
                for (Map.Entry<String, GpsAppTracker> entry : this.mAppTrackers.entrySet()) {
                    GpsAppTracker appTracker = entry.getValue();
                    if (appTracker.noNeedToTrack()) {
                        couldClearApps.add(appTracker.mPkgName);
                    }
                }
                int appSize = couldClearApps.size();
                if (appSize > 0) {
                    for (int i = 0; i < appSize; i++) {
                        LBSLog.d(TAG, false, "clearAppTrackerData AppName: %{public}s", couldClearApps.get(i));
                        this.mAppTrackers.remove(couldClearApps.get(i));
                    }
                }
            }
        }
    }

    public void recordRequest(String pkgName, int quality, boolean isIntent) {
        if (ENABLE_GPS_POWER_TRACK) {
            LBSLog.i(TAG, false, "recordRequest AppName: %{public}s quality: %{public}d", pkgName, Integer.valueOf(quality));
            if (quality == 100) {
                synchronized (this.mLock) {
                    if (this.mAppTrackers.containsKey(pkgName)) {
                        this.mAppTrackers.get(pkgName).recordStart(isIntent);
                    } else {
                        this.mAppTrackers.put(pkgName, new GpsAppTracker(pkgName, isIntent));
                    }
                }
                this.mPowerHandler.sendEmptyMessage(1000);
            }
        }
    }

    public void removeRequest(String pkgName) {
        if (ENABLE_GPS_POWER_TRACK) {
            LBSLog.i(TAG, false, "removeRequest: %{public}s", pkgName);
            synchronized (this.mLock) {
                if (this.mAppTrackers.containsKey(pkgName)) {
                    this.mAppTrackers.get(pkgName).recordEnd();
                    this.mPowerHandler.sendEmptyMessage(1001);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkHighPower() {
        synchronized (this.mLock) {
            if (this.mAppTrackers.size() != 0) {
                for (Map.Entry<String, GpsAppTracker> entry : this.mAppTrackers.entrySet()) {
                    entry.getValue().checkHighPower();
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean noLocationRunCheck() {
        synchronized (this.mLock) {
            if (this.mAppTrackers.size() != 0) {
                for (Map.Entry<String, GpsAppTracker> entry : this.mAppTrackers.entrySet()) {
                    if (entry.getValue().isGpsLocationRunning()) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    public final class GpsAppTracker {
        private ArrayList<NavigateRecord> mCurrentLocationList = new ArrayList<>(16);
        private final String mPkgName;

        public GpsAppTracker(String appName, boolean isIntent) {
            this.mPkgName = appName;
            recordStart(isIntent);
        }

        public String toString() {
            int listSize = this.mCurrentLocationList.size();
            StringBuffer result = new StringBuffer("  Gps history of ");
            result.append(this.mPkgName);
            result.append("\n");
            for (int i = 0; i < listSize; i++) {
                result.append(this.mCurrentLocationList.get(i).toString());
            }
            return result.toString();
        }

        public boolean noNeedToTrack() {
            return this.mCurrentLocationList.isEmpty();
        }

        public void removeLocation() {
            LBSLog.w(HwGpsPowerTracker.TAG, false, "stop %{public}s location", this.mPkgName);
            Message msg = Message.obtain();
            msg.what = 3001;
            Bundle data = new Bundle();
            data.putString("pkg", this.mPkgName);
            msg.setData(data);
            HwGpsPowerTracker.this.mRemoveLocationHandler.sendMessage(msg);
        }

        public void checkHighPower() {
            if (!noPidRunning()) {
                return;
            }
            if (isIntentLocation()) {
                LBSLog.w(HwGpsPowerTracker.TAG, false, "no Process running, stop it", new Object[0]);
                removeLocation();
            } else if (!this.mCurrentLocationList.isEmpty()) {
                LBSLog.e(HwGpsPowerTracker.TAG, false, "impossible here", new Object[0]);
                removeLocation();
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:24:0x007b  */
        private boolean noPidRunning() {
            if (this.mPkgName == null) {
                LBSLog.i(HwGpsPowerTracker.TAG, false, "packageName is null", new Object[0]);
                return false;
            }
            String packageNameHms = HwMultiNlpPolicy.getHmsPackageName(HwGpsPowerTracker.this.mContext);
            if (HwGpsPowerTracker.PACKAGE_NAME_FUSED.equals(this.mPkgName) || HwGpsPowerTracker.PACKAGE_NAME_HWID.equals(this.mPkgName) || "android".equals(this.mPkgName) || HwGpsPowerTracker.PACKAGE_NAME_HMS.equals(this.mPkgName) || (packageNameHms != null && !"".equals(packageNameHms) && packageNameHms.equals(this.mPkgName))) {
                LBSLog.i(HwGpsPowerTracker.TAG, false, "find app mPkgName= %{public}s", this.mPkgName);
                return false;
            }
            List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) HwGpsPowerTracker.this.mContext.getSystemService("activity")).getRunningAppProcesses();
            if (appProcessList == null) {
                LBSLog.w(HwGpsPowerTracker.TAG, false, "no Process find", new Object[0]);
                return false;
            }
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
                if (appProcess.processName.indexOf(this.mPkgName) >= 0 || this.mPkgName.indexOf(appProcess.processName) >= 0) {
                    LBSLog.i(HwGpsPowerTracker.TAG, false, "find processName is %{public}s , mPkgName is %{public}s", appProcess.processName, this.mPkgName);
                    return false;
                }
                while (r6.hasNext()) {
                }
            }
            LBSLog.i(HwGpsPowerTracker.TAG, false, "mPkgName is not find in appProcess, remove request", new Object[0]);
            return true;
        }

        private boolean isIntentLocation() {
            int listSize = this.mCurrentLocationList.size();
            for (int i = 0; i < listSize; i++) {
                if (this.mCurrentLocationList.get(i).mIsIntent) {
                    return true;
                }
            }
            return false;
        }

        public void recordEnd() {
            if (!this.mCurrentLocationList.isEmpty()) {
                this.mCurrentLocationList.remove(0);
            }
        }

        public void recordStart(boolean isIntent) {
            this.mCurrentLocationList.add(new NavigateRecord(isIntent));
        }

        public boolean isGpsLocationRunning() {
            return !this.mCurrentLocationList.isEmpty();
        }
    }

    /* access modifiers changed from: private */
    public static class NavigateRecord {
        private boolean mIsIntent;
        private long mStartTime = System.currentTimeMillis();

        public NavigateRecord() {
        }

        public NavigateRecord(boolean isIntent) {
            this.mIsIntent = isIntent;
        }

        public String toString() {
            return "      Record [Start=" + this.mStartTime + "]\n";
        }

        public long getStartTime() {
            return this.mStartTime;
        }
    }

    public void dump(PrintWriter pw) {
        if (ENABLE_GPS_POWER_TRACK) {
            pw.println("  LocationPowerTracker:");
            synchronized (this.mLock) {
                Iterator iter = this.mAppTrackers.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = null;
                    if (iter.next() instanceof Map.Entry) {
                        entry = iter.next();
                    }
                    if (entry != null) {
                        GpsAppTracker appTracker = null;
                        if (entry.getValue() instanceof GpsAppTracker) {
                            appTracker = entry.getValue();
                        }
                        if (appTracker != null) {
                            pw.println("  " + appTracker.toString());
                        }
                    }
                }
            }
        }
    }
}
