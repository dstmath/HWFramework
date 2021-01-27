package com.huawei.android.os;

import android.os.IBinder;
import android.os.IInstalld;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

public class InstalldEx {
    private static final Object INSTALLD_LOCK = new Object();
    private static final String SERVICE_NAME_INSTALLD = "installd";
    private static final String TAG = "HwSfpService";
    private static IInstalld sInstalld;

    @HwSystemApi
    public static void setFileXattr(String path, String keyDesc, int storageType, int fileType) {
        IInstalld installd = getInstalld();
        if (installd == null) {
            Log.e(TAG, "IInstalld is not found");
            return;
        }
        try {
            installd.setFileXattr(path, keyDesc, storageType, fileType);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException");
        }
    }

    private static IInstalld getInstalld() {
        synchronized (INSTALLD_LOCK) {
            if (sInstalld == null) {
                IBinder service = ServiceManager.getService(SERVICE_NAME_INSTALLD);
                if (service == null) {
                    return null;
                }
                sInstalld = IInstalld.Stub.asInterface(service);
            }
            return sInstalld;
        }
    }
}
