package com.huawei.ukey;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.IHwSecurityService.Stub;
import huawei.android.security.IUKeyManager;

public class UKeyManager {
    private static boolean DEBUG = Log.HWINFO;
    public static final int FAILED = -1;
    private static final String SECURITY_SERVICE = "securityserver";
    public static final int SUCCESS = 0;
    public static final int SWITCH_OFF = 0;
    public static final int SWITCH_ON = 1;
    private static final String TAG = "UkeyManager";
    public static final int UEKY_VERSION_2 = 2;
    private static final int UKEY_PLUGIN_ID = 6;
    public static final int UKEY_VERSION_1 = 1;
    public static final int UNSUPPORT_UKEY = 0;
    private static final Object mInstanceSync = new Object();
    private static volatile UKeyManager sSelf = null;
    private static IUKeyManager sUKeyManager;

    private UKeyManager() {
    }

    public static UKeyManager getInstance() {
        if (sSelf == null) {
            synchronized (UKeyManager.class) {
                if (sSelf == null) {
                    sSelf = new UKeyManager();
                }
            }
        }
        return sSelf;
    }

    private static IUKeyManager getUKeyManagerService() {
        synchronized (mInstanceSync) {
            IUKeyManager iUKeyManager;
            if (sUKeyManager != null) {
                iUKeyManager = sUKeyManager;
                return iUKeyManager;
            }
            IHwSecurityService secService = Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    sUKeyManager = IUKeyManager.Stub.asInterface(secService.querySecurityInterface(6));
                } catch (RemoteException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Get UKeyManagerService failed!");
                    }
                }
            }
            iUKeyManager = sUKeyManager;
            return iUKeyManager;
        }
    }

    public int getSupportVersion() {
        if (getUKeyManagerService() != null) {
            try {
                return sUKeyManager.isSwitchFeatureOn();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while getting ukey version");
            }
        }
        return 0;
    }

    public int isUKeySwitchDisabled(String packageName) {
        if (getUKeyManagerService() != null) {
            try {
                return sUKeyManager.isUKeySwitchDisabled(packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while getting ukey switch status");
            }
        }
        return -1;
    }

    public int setUKeySwitchDisabled(String packageName, boolean isDisabled) {
        if (getUKeyManagerService() != null) {
            try {
                return sUKeyManager.setUKeySwitchDisabled(packageName, isDisabled);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while settings ukey switch status");
            }
        }
        return -1;
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Bundle getUKeyApkInfo(String packageName) {
        if (!(packageName == null || packageName.isEmpty() || getUKeyManagerService() == null)) {
            try {
                return sUKeyManager.getUKeyApkInfo(packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while getting ukey app infomation for system ukey app white list ");
            }
        }
        return null;
    }
}
