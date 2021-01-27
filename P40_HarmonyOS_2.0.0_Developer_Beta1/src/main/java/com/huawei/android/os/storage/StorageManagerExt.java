package com.huawei.android.os.storage;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.storage.IStorageManager;
import android.os.storage.StorageManager;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

public class StorageManagerExt {
    public static final String INVALID_KEY_DESC = "";
    public static final int INVALID_PRELOAD_STATUS = -1;
    private static final String SERVICE_NAME_MOUNT = "mount";
    private static final Object SMS_LOCK = new Object();
    private static final String TAG = "StorageManagerExt";
    private static IStorageManager sStorageManager;

    @HwSystemApi
    public static int getPreLoadPolicyFlag(int userId, int serialNumber) {
        IStorageManager storageManager = getStorageManager();
        if (storageManager == null) {
            Log.e(TAG, "StorageManager is not found");
            return -1;
        } else if (userId < 0) {
            Log.e(TAG, "Invalid userid!");
            return -1;
        } else {
            try {
                return storageManager.getPreLoadPolicyFlag(userId, serialNumber);
            } catch (RemoteException e) {
                Log.e(TAG, "fialed to get flag!");
                return -1;
            }
        }
    }

    @HwSystemApi
    public static void lockUserScreenISec(int userId, int serialNumber) {
        IStorageManager storageManager = getStorageManager();
        if (storageManager == null) {
            Log.e(TAG, "deleteKey storage manager is null!");
            return;
        }
        try {
            storageManager.lockUserScreenISec(userId, serialNumber);
        } catch (RemoteException e) {
            Log.e(TAG, "fialed to delete key!");
        }
    }

    @HwSystemApi
    public static String getKeyDesc(int userId, int serialNumber, int storageType) {
        IStorageManager storageManager = getStorageManager();
        if (storageManager == null) {
            Log.e(TAG, "StorageManager is not found");
            return INVALID_KEY_DESC;
        } else if (userId < 0) {
            Log.e(TAG, "Invalid userid!");
            return INVALID_KEY_DESC;
        } else {
            try {
                return storageManager.getKeyDesc(userId, serialNumber, storageType);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to getKeyDesc");
                return INVALID_KEY_DESC;
            }
        }
    }

    public static boolean isUserKeyUnlocked(int userId) {
        return StorageManager.isUserKeyUnlocked(userId);
    }

    public static long getPrimaryStorageSize(StorageManager storageManager) {
        return storageManager.getPrimaryStorageSize();
    }

    private static IStorageManager getStorageManager() {
        synchronized (SMS_LOCK) {
            if (sStorageManager == null) {
                IBinder service = ServiceManager.getService(SERVICE_NAME_MOUNT);
                if (service == null) {
                    return null;
                }
                sStorageManager = IStorageManager.Stub.asInterface(service);
            }
            return sStorageManager;
        }
    }
}
