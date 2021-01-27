package com.huawei.security.dpermission.monitor;

import android.content.Context;

public class DPermissionMonitor {
    private static final Object INSTANCE_LOCK = new Object();
    private static volatile DPermissionMonitor sInstance;
    private Context mContext;
    private DangerousPermissionTrackerReceiver trackerReceiver = new DangerousPermissionTrackerReceiver(this.mContext);

    private DPermissionMonitor(Context context) {
        this.mContext = context;
    }

    public static DPermissionMonitor getInstance(Context context) {
        if (sInstance == null) {
            synchronized (INSTANCE_LOCK) {
                if (sInstance == null) {
                    sInstance = new DPermissionMonitor(context);
                }
            }
        }
        return sInstance;
    }

    public void register() {
        HwPermissionChangeListener.getInstance(this.mContext).register();
        RunTimePermissionChangeListener.getInstance(this.mContext).register();
        AppOpsChangeListener.getInstance(this.mContext).register();
        PackageChangeReceiver.getInstance(this.mContext).register();
        this.trackerReceiver.register();
        DangerousPermissionTracker.startTracker(this.mContext);
    }
}
