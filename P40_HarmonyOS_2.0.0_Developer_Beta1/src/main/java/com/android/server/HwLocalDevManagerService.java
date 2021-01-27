package com.android.server;

import android.content.Context;
import android.util.Log;

public final class HwLocalDevManagerService {
    private static final String TAG = "HwLocalDevManagerService";
    private static volatile HwLocalDevManagerService mInstance = null;
    private Context mContext;

    public static synchronized HwLocalDevManagerService getInstance(Context context) {
        HwLocalDevManagerService hwLocalDevManagerService;
        synchronized (HwLocalDevManagerService.class) {
            if (mInstance == null) {
                mInstance = new HwLocalDevManagerService(context);
            }
            hwLocalDevManagerService = mInstance;
        }
        return hwLocalDevManagerService;
    }

    public HwLocalDevManagerService(Context context) {
        this.mContext = context;
    }

    public int getLocalDevStat(int dev) {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "getLocalDevStat");
        try {
            return StorageManagerService.sSelf.mVold.sdbackupGetDevStat(dev);
        } catch (Exception e) {
            Log.e(TAG, "getLocalDevStat has exception!");
            return -1;
        }
    }

    public String getDeviceId(int dev) {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "getDeviceId");
        try {
            return StorageManagerService.sSelf.mVold.sdbackupGetCid(dev);
        } catch (Exception e) {
            Log.e(TAG, "getDeviceId has exception!");
            return null;
        }
    }

    public int doSdcardCheckRW() {
        this.mContext.enforceCallingPermission("android.permission.BACKUP", "doSdcardCheckRW");
        try {
            return StorageManagerService.sSelf.mVold.sdbackupCheckRW();
        } catch (Exception e) {
            Log.e(TAG, "doSdcardCheckRW has exception!");
            return -1;
        }
    }
}
