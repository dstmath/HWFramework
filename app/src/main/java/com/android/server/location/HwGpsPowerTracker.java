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
import android.os.UserHandle;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.pfw.autostartup.comm.XmlConst.ControlScope;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class HwGpsPowerTracker {
    private static final long CHECK_TIME_INTERVEL = 120000;
    private static final int CMD_CHECK_HIGH_POWER = 2001;
    private static final boolean DBG = true;
    public static final String DEL_PKG = "pkg";
    private static final int EVENT_LOCATION_BEGIN = 1000;
    private static final int EVENT_LOCATION_END = 1001;
    public static final int EVENT_REMOVE_PACKAGE_LOCATION = 3001;
    private static final String TAG = "HwGpsPowerTracker";
    private static final boolean mEnableGpsPowerTrack = false;
    private HashMap<String, GpsAppTracker> mAppTrackers;
    private Context mContext;
    private LocationManagerServiceUtil mLocationManagerServiceUtil;
    private BroadcastReceiver mPackegeChangeReceiver;
    private PowerHandler mPowerHandler;
    private Handler mRemoveLocationHandler;
    private HandlerThread mThread;

    private class GpsAppTracker {
        private ArrayList<NavigateRecord> mCurrentLocationList;
        private final String mPkgName;

        public GpsAppTracker(String appName, boolean isIntent) {
            this.mPkgName = appName;
            this.mCurrentLocationList = new ArrayList();
            recordStart(isIntent);
        }

        public String toString() {
            StringBuffer result = new StringBuffer();
            result.append("  Gps history of " + this.mPkgName + "\n");
            for (NavigateRecord navigateRecord : this.mCurrentLocationList) {
                result.append(navigateRecord.toString());
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
            return HwGpsPowerTracker.DBG;
        }

        private boolean isIntentLocation() {
            for (NavigateRecord navigateRecord : this.mCurrentLocationList) {
                if (navigateRecord.mIsIntent) {
                    return HwGpsPowerTracker.DBG;
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
            return this.mCurrentLocationList.isEmpty() ? false : HwGpsPowerTracker.DBG;
        }
    }

    private static class NavigateRecord {
        private boolean mIsIntent;
        private long mStartTime;

        public NavigateRecord() {
            this.mStartTime = System.currentTimeMillis();
        }

        public NavigateRecord(boolean isIntent) {
            this.mStartTime = System.currentTimeMillis();
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
                case HwGpsPowerTracker.EVENT_LOCATION_BEGIN /*1000*/:
                    if (!hasMessages(HwGpsPowerTracker.CMD_CHECK_HIGH_POWER)) {
                        Log.d(HwGpsPowerTracker.TAG, "send CMD_CHECK_HIGH_POWER");
                        sendEmptyMessageDelayed(HwGpsPowerTracker.CMD_CHECK_HIGH_POWER, HwGpsPowerTracker.CHECK_TIME_INTERVEL);
                        return;
                    }
                    return;
                case HwGpsPowerTracker.EVENT_LOCATION_END /*1001*/:
                    -get0 = HwGpsPowerTracker.this.mAppTrackers;
                    synchronized (-get0) {
                        break;
                    }
                    if (HwGpsPowerTracker.this.noLocationRunCheck()) {
                        removeMessages(HwGpsPowerTracker.CMD_CHECK_HIGH_POWER);
                    }
                    HwGpsPowerTracker.this.clearAppTrackerData();
                    break;
                case HwGpsPowerTracker.CMD_CHECK_HIGH_POWER /*2001*/:
                    -get0 = HwGpsPowerTracker.this.mAppTrackers;
                    synchronized (-get0) {
                        break;
                    }
                    HwGpsPowerTracker.this.checkHighPower();
                    if (!HwGpsPowerTracker.this.noLocationRunCheck()) {
                        Log.d(HwGpsPowerTracker.TAG, "send CMD_CHECK_HIGH_POWER");
                        sendEmptyMessageDelayed(HwGpsPowerTracker.CMD_CHECK_HIGH_POWER, HwGpsPowerTracker.CHECK_TIME_INTERVEL);
                        break;
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.HwGpsPowerTracker.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.HwGpsPowerTracker.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwGpsPowerTracker.<clinit>():void");
    }

    public HwGpsPowerTracker(Context context) {
        this.mPackegeChangeReceiver = new BroadcastReceiver() {
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
                    return HwGpsPowerTracker.DBG;
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
        if (mEnableGpsPowerTrack) {
            this.mThread = new HandlerThread("LocationManagerPowerTracker");
            this.mThread.start();
            this.mPowerHandler = new PowerHandler(this.mThread.getLooper());
            this.mAppTrackers = new HashMap();
            this.mContext = context;
            IntentFilter packageFilter = new IntentFilter();
            packageFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
            packageFilter.addAction("android.intent.action.PACKAGE_RESTARTED");
            packageFilter.addDataScheme(ControlScope.PACKAGE_ELEMENT_KEY);
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
                if (couldClearApps.size() > 0) {
                    for (int i = 0; i < couldClearApps.size(); i++) {
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
                this.mPowerHandler.sendEmptyMessage(EVENT_LOCATION_BEGIN);
            }
        }
    }

    public void removeRequest(String pkgName) {
        if (mEnableGpsPowerTrack) {
            Log.d(TAG, "removeRequest:" + pkgName);
            synchronized (this.mAppTrackers) {
                if (this.mAppTrackers.containsKey(pkgName)) {
                    ((GpsAppTracker) this.mAppTrackers.get(pkgName)).recordEnd();
                    this.mPowerHandler.sendEmptyMessage(EVENT_LOCATION_END);
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

    private boolean noLocationRunCheck() {
        synchronized (this.mAppTrackers) {
            if (this.mAppTrackers.size() != 0) {
                for (Entry entry : this.mAppTrackers.entrySet()) {
                    if (((GpsAppTracker) entry.getValue()).isGpsLocationRunning()) {
                        return false;
                    }
                }
            }
            return DBG;
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
