package com.android.server;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import com.android.server.LocationManagerService;
import com.android.server.location.LBSLog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HwLocationLockManager {
    public static final long CHECK_LOCATION_INTERVAL = 300000;
    private static final int DEFAULT_VALUE = 16;
    private static final Object LOCK = new Object();
    public static final int MSG_CHECK_LOCATION = 7;
    private static final String TAG = "HwLocationLockManager";
    private static HwLocationLockManager sInstance = null;
    private Context mContext;
    private BroadcastReceiver mPackegeClearReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwLocationLockManager.AnonymousClass1 */

        private String getPackageName(Intent intent) {
            Uri uri = intent.getData();
            if (uri != null) {
                return uri.getSchemeSpecificPart();
            }
            return null;
        }

        private boolean explicitlyStopped(String packageName) {
            PackageManager pm = HwLocationLockManager.this.mContext.getPackageManager();
            if (pm == null) {
                return false;
            }
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
                if (applicationInfo == null || (applicationInfo.flags & 2097152) == 0) {
                    return false;
                }
                return true;
            } catch (PackageManager.NameNotFoundException e) {
                LBSLog.w(HwLocationLockManager.TAG, "package info not found:" + packageName);
                return false;
            }
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.PACKAGE_DATA_CLEARED".equals(action) || "android.intent.action.PACKAGE_RESTARTED".equals(action)) {
                    String packageName = getPackageName(intent);
                    if (explicitlyStopped(packageName)) {
                        HwLocationLockManager.this.removeStoppedRecords(packageName);
                    }
                }
            }
        }
    };
    private LocationManagerServiceUtil mUtil;

    private HwLocationLockManager(Context context) {
        this.mContext = context;
        this.mUtil = LocationManagerServiceUtil.getDefault();
        registerPkgClearReceiver();
    }

    public static HwLocationLockManager getInstance(Context context) {
        HwLocationLockManager hwLocationLockManager;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new HwLocationLockManager(context);
            }
            hwLocationLockManager = sInstance;
        }
        return hwLocationLockManager;
    }

    private void registerPkgClearReceiver() {
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_DATA_CLEARED");
        packageFilter.addAction("android.intent.action.PACKAGE_RESTARTED");
        packageFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mPackegeClearReceiver, UserHandle.OWNER, packageFilter, null, null);
    }

    public void hwLocationLockCheck() {
        removeNotRunAndNotExistRecords();
        finalCheck();
    }

    private void removeNotRunAndNotExistRecords() {
        List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) this.mContext.getSystemService("activity")).getRunningAppProcesses();
        synchronized (this.mUtil.getmLock()) {
            HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> recordsByProvider = this.mUtil.getRecordsByProvider();
            if (recordsByProvider != null) {
                if (!recordsByProvider.isEmpty()) {
                    if (appProcessList == null) {
                        Log.w(TAG, "no Process find");
                        return;
                    }
                    HashMap<Object, LocationManagerService.Receiver> receivers = (HashMap) this.mUtil.getReceivers().clone();
                    for (Map.Entry<String, ArrayList<LocationManagerService.UpdateRecord>> entry : recordsByProvider.entrySet()) {
                        ArrayList<LocationManagerService.UpdateRecord> records = entry.getValue();
                        if (records != null) {
                            Iterator<LocationManagerService.UpdateRecord> it = ((ArrayList) records.clone()).iterator();
                            while (it.hasNext()) {
                                LocationManagerService.UpdateRecord record = it.next();
                                if (!receivers.containsValue(record.mReceiver)) {
                                    record.disposeLocked(false);
                                    logUpdateRecordInfo(record);
                                } else {
                                    boolean isFound = false;
                                    Iterator<ActivityManager.RunningAppProcessInfo> it2 = appProcessList.iterator();
                                    while (true) {
                                        if (!it2.hasNext()) {
                                            break;
                                        }
                                        ActivityManager.RunningAppProcessInfo appProcess = it2.next();
                                        if (appProcess.pid == record.mReceiver.mCallerIdentity.mPid && appProcess.uid == record.mReceiver.mCallerIdentity.mUid) {
                                            isFound = true;
                                            break;
                                        }
                                    }
                                    if (!isFound) {
                                        this.mUtil.removeUpdatesLocked(record.mReceiver);
                                        logReceiverInfo(record);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void logUpdateRecordInfo(LocationManagerService.UpdateRecord record) {
        StringBuilder stringBuilder = new StringBuilder(16);
        stringBuilder.append("receiver not exists, but updateRecord not remove! pid = ");
        stringBuilder.append(record.mReceiver.mCallerIdentity.mPid);
        stringBuilder.append(" uid = ");
        stringBuilder.append(record.mReceiver.mCallerIdentity.mUid);
        stringBuilder.append(" UpdateRecord = ");
        stringBuilder.append(record);
        LBSLog.w(TAG, stringBuilder.toString());
    }

    private void logReceiverInfo(LocationManagerService.UpdateRecord record) {
        StringBuilder stringBuilder = new StringBuilder(16);
        stringBuilder.append("process may be died, but request not remove! pid = ");
        stringBuilder.append(record.mReceiver.mCallerIdentity.mPid);
        stringBuilder.append(" uid = ");
        stringBuilder.append(record.mReceiver.mCallerIdentity.mUid);
        stringBuilder.append(" receiver = ");
        stringBuilder.append(record.mReceiver);
        LBSLog.w(TAG, stringBuilder.toString());
    }

    private void finalCheck() {
        boolean isNeedCheckAgain;
        int totalPendingBroadcasts = 0;
        synchronized (this.mUtil.getmLock()) {
            HashMap<String, ArrayList<LocationManagerService.UpdateRecord>> recordsByProvider = this.mUtil.getRecordsByProvider();
            for (Map.Entry<String, ArrayList<LocationManagerService.UpdateRecord>> entry : recordsByProvider.entrySet()) {
                Iterator<LocationManagerService.UpdateRecord> it = entry.getValue().iterator();
                while (it.hasNext()) {
                    totalPendingBroadcasts += it.next().mReceiver.mPendingBroadcasts;
                }
            }
            ArrayList<LocationManagerService.UpdateRecord> gpsRecords = recordsByProvider.get("gps");
            isNeedCheckAgain = totalPendingBroadcasts > 0 && gpsRecords != null && !gpsRecords.isEmpty();
        }
        LBSLog.i(TAG, "hwCheckLock finalCheck " + totalPendingBroadcasts + " isNeedCheckAgain " + isNeedCheckAgain);
        if (isNeedCheckAgain) {
            Handler locationHandler = this.mUtil.getLocationHandler();
            if (!locationHandler.hasMessages(7)) {
                locationHandler.sendEmptyMessageDelayed(7, CHECK_LOCATION_INTERVAL);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeStoppedRecords(String pkgName) {
        if (pkgName != null) {
            boolean isPkgHasRecord = false;
            synchronized (this.mUtil.getmLock()) {
                Iterator<Map.Entry<String, ArrayList<LocationManagerService.UpdateRecord>>> it = this.mUtil.getRecordsByProvider().entrySet().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ArrayList<LocationManagerService.UpdateRecord> records = it.next().getValue();
                    if (records != null) {
                        Iterator<LocationManagerService.UpdateRecord> it2 = ((ArrayList) records.clone()).iterator();
                        while (true) {
                            if (!it2.hasNext()) {
                                break;
                            } else if (pkgName.equals(it2.next().mReceiver.mCallerIdentity.mPackageName)) {
                                LBSLog.i(TAG, "package stopped,remove updateRecords and receivers:" + pkgName);
                                isPkgHasRecord = true;
                                break;
                            }
                        }
                        if (isPkgHasRecord) {
                            break;
                        }
                    }
                }
            }
            if (isPkgHasRecord) {
                removeNotRunAndNotExistRecords();
            }
        }
    }
}
