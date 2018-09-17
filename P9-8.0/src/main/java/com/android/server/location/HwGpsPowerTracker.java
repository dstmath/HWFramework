package com.android.server.location;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.server.LocationManagerServiceUtil;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import vendor.huawei.hardware.hwdisplay.displayengine.V1_0.HighBitsCompModeID;

public class HwGpsPowerTracker {
    private static final long CHECK_TIME_INTERVEL = 120000;
    private static final int CMD_CHECK_HIGH_POWER = 2001;
    private static final boolean DBG = true;
    public static final String DEL_PKG = "pkg";
    private static final int EVENT_LOCATION_BEGIN = 1000;
    private static final int EVENT_LOCATION_END = 1001;
    public static final int EVENT_REMOVE_PACKAGE_LOCATION = 3001;
    private static final String TAG = "HwGpsPowerTracker";
    private static final boolean mEnableGpsPowerTrack = SystemProperties.getBoolean("ro.config.hw_gps_power_track", false);
    private HashMap<String, GpsAppTracker> mAppTrackers;
    private Context mContext;
    private LocationManagerServiceUtil mLocationManagerServiceUtil;
    private BroadcastReceiver mPackegeChangeReceiver = new BroadcastReceiver() {
        private String getPackageName(Intent intent) {
            Uri uri = intent.getData();
            return uri != null ? uri.getSchemeSpecificPart() : null;
        }

        private boolean explicitlyStopped(String pkg) {
            PackageManager pm = HwGpsPowerTracker.this.mContext.getPackageManager();
            if (pm == null) {
                return false;
            }
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                if (ai == null || (ai.flags & HighBitsCompModeID.MODE_EYE_PROTECT) == 0) {
                    return false;
                }
                return true;
            } catch (NameNotFoundException e) {
                Log.w(HwGpsPowerTracker.TAG, "package info not found:" + pkg);
                return false;
            }
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                Log.d(HwGpsPowerTracker.TAG, "LocalReceiver receives:" + action);
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

    private class GpsAppTracker {
        private ArrayList<NavigateRecord> mCurrentLocationList = new ArrayList();
        private final String mPkgName;

        public GpsAppTracker(String appName, boolean isIntent) {
            this.mPkgName = appName;
            recordStart(isIntent);
        }

        public String toString() {
            int list_size = this.mCurrentLocationList.size();
            StringBuffer result = new StringBuffer("  Gps history of ");
            result.append(this.mPkgName);
            result.append("\n");
            for (int i = 0; i < list_size; i++) {
                result.append(((NavigateRecord) this.mCurrentLocationList.get(i)).toString());
            }
            return result.toString();
        }

        public boolean noNeedToTrack() {
            return this.mCurrentLocationList.isEmpty();
        }

        public void removeLocation() {
            Log.w(HwGpsPowerTracker.TAG, "stop " + this.mPkgName + " location");
            Message msg = new Message();
            msg.what = HwGpsPowerTracker.EVENT_REMOVE_PACKAGE_LOCATION;
            Bundle data = new Bundle();
            data.putString(HwGpsPowerTracker.DEL_PKG, this.mPkgName);
            msg.setData(data);
            HwGpsPowerTracker.this.mRemoveLocationHandler.sendMessage(msg);
        }

        public void checkHighPower() {
            if (!noPidRunning()) {
                return;
            }
            if (isIntentLocation()) {
                Log.w(HwGpsPowerTracker.TAG, "no Process running, stop it");
                removeLocation();
            } else if (!this.mCurrentLocationList.isEmpty()) {
                Log.e(HwGpsPowerTracker.TAG, "impossible here");
                removeLocation();
            }
        }

        private boolean noPidRunning() {
            List<RunningAppProcessInfo> appProcessList = ((ActivityManager) HwGpsPowerTracker.this.mContext.getSystemService("activity")).getRunningAppProcesses();
            if (appProcessList == null) {
                Log.w(HwGpsPowerTracker.TAG, "no Process find");
                return false;
            }
            for (RunningAppProcessInfo appProcess : appProcessList) {
                if (appProcess.processName.equals(this.mPkgName)) {
                    return false;
                }
            }
            return true;
        }

        private boolean isIntentLocation() {
            int list_size = this.mCurrentLocationList.size();
            for (int i = 0; i < list_size; i++) {
                if (((NavigateRecord) this.mCurrentLocationList.get(i)).mIsIntent) {
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
            return this.mCurrentLocationList.isEmpty() ^ 1;
        }
    }

    private static class NavigateRecord {
        private boolean mIsIntent;
        private long mStartTime = System.currentTimeMillis();

        public NavigateRecord(boolean isIntent) {
            this.mIsIntent = isIntent;
        }

        public String toString() {
            return "      Record [Start=" + this.mStartTime + "]" + "\n";
        }

        public long getStartTime() {
            return this.mStartTime;
        }
    }

    private class PowerHandler extends Handler {
        PowerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            Log.d(HwGpsPowerTracker.TAG, "PowerHandler msg.what:" + msg.what);
            HashMap -get0;
            switch (msg.what) {
                case 1000:
                    if (!hasMessages(2001)) {
                        Log.d(HwGpsPowerTracker.TAG, "send CMD_CHECK_HIGH_POWER");
                        sendEmptyMessageDelayed(2001, HwGpsPowerTracker.CHECK_TIME_INTERVEL);
                        return;
                    }
                    return;
                case 1001:
                    -get0 = HwGpsPowerTracker.this.mAppTrackers;
                    synchronized (-get0) {
                        if (HwGpsPowerTracker.this.noLocationRunCheck()) {
                            removeMessages(2001);
                        }
                        HwGpsPowerTracker.this.clearAppTrackerData();
                        break;
                    }
                case 2001:
                    -get0 = HwGpsPowerTracker.this.mAppTrackers;
                    synchronized (-get0) {
                        HwGpsPowerTracker.this.checkHighPower();
                        if (!HwGpsPowerTracker.this.noLocationRunCheck()) {
                            Log.d(HwGpsPowerTracker.TAG, "send CMD_CHECK_HIGH_POWER");
                            sendEmptyMessageDelayed(2001, HwGpsPowerTracker.CHECK_TIME_INTERVEL);
                            break;
                        }
                    }
                    break;
                default:
                    return;
            }
        }
    }

    private class RemoveLocationHandler extends Handler {
        RemoveLocationHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (HwGpsPowerTracker.EVENT_REMOVE_PACKAGE_LOCATION == msg.what) {
                String pkg = msg.getData().getString(HwGpsPowerTracker.DEL_PKG);
                if (pkg != null) {
                    HwGpsPowerTracker.this.mLocationManagerServiceUtil.clearPackageLocation(pkg);
                }
            }
            super.handleMessage(msg);
        }
    }

    public HwGpsPowerTracker(Context context) {
        if (mEnableGpsPowerTrack) {
            this.mThread = new HandlerThread("LocationManagerPowerTracker");
            this.mThread.start();
            this.mPowerHandler = new PowerHandler(this.mThread.getLooper());
            this.mAppTrackers = new HashMap();
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

    private void removeGpsLocation(String pkg) {
        synchronized (this.mAppTrackers) {
            GpsAppTracker appTracker = (GpsAppTracker) this.mAppTrackers.get(pkg);
            if (appTracker == null) {
                return;
            }
            appTracker.removeLocation();
        }
    }

    private void clearAppTrackerData() {
        synchronized (this.mAppTrackers) {
            if (this.mAppTrackers.size() != 0) {
                ArrayList<String> couldClearApps = new ArrayList();
                for (Entry entry : this.mAppTrackers.entrySet()) {
                    GpsAppTracker appTracker = (GpsAppTracker) entry.getValue();
                    if (appTracker.noNeedToTrack()) {
                        couldClearApps.add(appTracker.mPkgName);
                    }
                }
                int appSize = couldClearApps.size();
                if (appSize > 0) {
                    for (int i = 0; i < appSize; i++) {
                        Log.d(TAG, "clearAppTrackerData AppName:" + ((String) couldClearApps.get(i)));
                        this.mAppTrackers.remove(couldClearApps.get(i));
                    }
                }
            }
        }
    }

    public void recordRequest(String pkgName, int quality, boolean isIntent) {
        if (mEnableGpsPowerTrack) {
            Log.d(TAG, "recordRequest AppName:" + pkgName + " quality:" + quality);
            if (quality == 100) {
                synchronized (this.mAppTrackers) {
                    if (this.mAppTrackers.containsKey(pkgName)) {
                        ((GpsAppTracker) this.mAppTrackers.get(pkgName)).recordStart(isIntent);
                    } else {
                        this.mAppTrackers.put(pkgName, new GpsAppTracker(pkgName, isIntent));
                    }
                }
                this.mPowerHandler.sendEmptyMessage(1000);
            }
        }
    }

    public void removeRequest(String pkgName) {
        if (mEnableGpsPowerTrack) {
            Log.d(TAG, "removeRequest:" + pkgName);
            synchronized (this.mAppTrackers) {
                if (this.mAppTrackers.containsKey(pkgName)) {
                    ((GpsAppTracker) this.mAppTrackers.get(pkgName)).recordEnd();
                    this.mPowerHandler.sendEmptyMessage(1001);
                    return;
                }
            }
        }
    }

    private void checkHighPower() {
        synchronized (this.mAppTrackers) {
            if (this.mAppTrackers.size() != 0) {
                for (Entry entry : this.mAppTrackers.entrySet()) {
                    ((GpsAppTracker) entry.getValue()).checkHighPower();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:0x0032, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean noLocationRunCheck() {
        synchronized (this.mAppTrackers) {
            if (this.mAppTrackers.size() != 0) {
                for (Entry entry : this.mAppTrackers.entrySet()) {
                    if (((GpsAppTracker) entry.getValue()).isGpsLocationRunning()) {
                        return false;
                    }
                }
            }
        }
    }

    public void dump(PrintWriter pw) {
        if (mEnableGpsPowerTrack) {
            pw.println("  LocationPowerTracker:");
            synchronized (this.mAppTrackers) {
                for (Entry entry : this.mAppTrackers.entrySet()) {
                    pw.println("  " + ((GpsAppTracker) entry.getValue()).toString());
                }
            }
        }
    }
}
