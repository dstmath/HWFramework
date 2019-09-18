package android.securitydiagnose;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.security.IHwAntiMalPlugin;
import huawei.android.security.IHwDeviceUsagePlugin;
import huawei.android.security.IHwSecurityDiagnoseCallback;
import huawei.android.security.IHwSecurityDiagnosePlugin;
import huawei.android.security.IHwSecurityService;

public class HwSecurityDiagnoseManager {
    private static final String ANTIMAL_KEY_PROTECT_TYPE = "protect_type";
    private static final int ANTIMAL_PROTECT_TYPE_DEFAULT = 0;
    private static final int ANTIMAL_PROTECT_TYPE_LAUNCHER = 2;
    private static final long CALL_ENOUGH_TIME_FOR_ANTIMAL = 900;
    private static final int DEVICE_SECURE_DIAGNOSE_ID = 2;
    private static final int DEVICE_USAGE_PLUGIN_ID = 1;
    private static final int GET_ROOT_STATUS_ERR = -1;
    private static final int HW_ANTIMAL_PLUGIN_ID = 16;
    private static final long SCREENON_ENOUGH_TIME_FOR_ANTIMAL = 360000;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final int STP_ITEM_CREDIBLE = 1;
    private static final int STP_ITEM_REFERENCE = 0;
    private static final int STP_ITEM_RISK = 1;
    private static final int STP_ITEM_SAFE = 0;
    private static final String TAG = "HwSecurityDiagnoseManager";
    private static final int USER_CLIENT_DEFAULT_ERROR = -2000;
    private static volatile HwSecurityDiagnoseManager sInstance;
    private IHwSecurityService mSecurityService = IHwSecurityService.Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));

    private HwSecurityDiagnoseManager() {
        if (this.mSecurityService == null) {
            Log.e(TAG, "error, securityservice was null");
        }
    }

    public static HwSecurityDiagnoseManager getInstance() {
        if (sInstance == null) {
            synchronized (HwSecurityDiagnoseManager.class) {
                if (sInstance == null) {
                    sInstance = new HwSecurityDiagnoseManager();
                }
            }
        }
        return sInstance;
    }

    private IHwSecurityDiagnosePlugin getHwSecurityDiagnosePlugin() {
        synchronized (this) {
            if (this.mSecurityService != null) {
                try {
                    IHwSecurityDiagnosePlugin securityDiagnoseService = IHwSecurityDiagnosePlugin.Stub.asInterface(this.mSecurityService.querySecurityInterface(2));
                    if (securityDiagnoseService == null) {
                        Log.e(TAG, "error, HwSecurityDiagnosePlugin is null");
                    }
                    return securityDiagnoseService;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getHwSecurityDiagnosePlugin invoked");
                }
            }
            Log.e(TAG, "error, SecurityService is null");
            return null;
        }
    }

    private IHwDeviceUsagePlugin getHwDeviceUsagePlugin() {
        synchronized (this) {
            if (this.mSecurityService != null) {
                try {
                    IHwDeviceUsagePlugin deviceUsageService = IHwDeviceUsagePlugin.Stub.asInterface(this.mSecurityService.querySecurityInterface(1));
                    if (deviceUsageService == null) {
                        Log.e(TAG, "error, HwDeviceUsagePlugin is null");
                    }
                    return deviceUsageService;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getHwDeviceUsagePlugin invoked");
                }
            }
            Log.e(TAG, "error, SecurityService is null");
            return null;
        }
    }

    private IHwAntiMalPlugin getHwAntiMalPlugin() {
        synchronized (this) {
            if (this.mSecurityService != null) {
                try {
                    IHwAntiMalPlugin hwAntiMalPlugin = IHwAntiMalPlugin.Stub.asInterface(this.mSecurityService.querySecurityInterface(16));
                    if (hwAntiMalPlugin == null) {
                        Log.e(TAG, "error, HwAntiMalPlugin is null");
                    }
                    return hwAntiMalPlugin;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getHwAntiMalPlugin invoked");
                }
            }
            Log.e(TAG, "error, SecurityService is null");
            return null;
        }
    }

    public void getRootStatus(IHwSecurityDiagnoseCallback callback) {
        if (callback != null) {
            IHwSecurityDiagnosePlugin plugin = getHwSecurityDiagnosePlugin();
            if (plugin != null) {
                try {
                    plugin.getRootStatus(callback);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getRootStatus is invoked");
                    try {
                        callback.onRootStatus(-1);
                    } catch (RemoteException e2) {
                        Log.e(TAG, "RemoteException when onRootStatus is invoked");
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Must supply an get root status callback");
        }
    }

    public boolean isThirdPartyLauncherProtectionOn() {
        if (ActivityManager.getCurrentUser() != 0) {
            Log.d(TAG, "not owner.");
            return false;
        }
        IHwDeviceUsagePlugin plugin = getHwDeviceUsagePlugin();
        if (plugin != null) {
            try {
                if (plugin.getScreenOnTime() < SCREENON_ENOUGH_TIME_FOR_ANTIMAL || plugin.getTalkTime() < CALL_ENOUGH_TIME_FOR_ANTIMAL) {
                    Log.d(TAG, "third party launcher protection on");
                    return true;
                }
                Log.d(TAG, "third party launcher protection off");
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when isThirdPartyLauncherProtectionOn is invoked");
            }
        }
        return false;
    }

    public boolean isAntiMalProtectionOn(Context context, Bundle params) {
        IHwAntiMalPlugin plugin = getHwAntiMalPlugin();
        if (plugin != null) {
            try {
                if (plugin.isAntiMalProtectionOn(params) && !isHwIDLogin(context, params)) {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when isAntiMalProtectionOn is invoked");
            }
        }
        return false;
    }

    private boolean isHwIDLogin(Context context, Bundle params) {
        if (params.getInt(ANTIMAL_KEY_PROTECT_TYPE, 0) == 2) {
            Log.i(TAG, "No need to check HwID!");
            return false;
        }
        Account[] accs = AccountManager.get(context).getAccountsByType("com.huawei.hwid");
        if (accs == null || accs.length == 0) {
            Log.i(TAG, "HwID is not login!");
            return false;
        }
        Log.i(TAG, "HwID is login!");
        return true;
    }

    public int getRootStatusSync() {
        IHwSecurityDiagnosePlugin plugin = getHwSecurityDiagnosePlugin();
        if (plugin == null) {
            return USER_CLIENT_DEFAULT_ERROR;
        }
        try {
            return plugin.getRootStatusSync();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException occurs when get root status synchronized");
            return USER_CLIENT_DEFAULT_ERROR;
        }
    }

    public int sendThreatenInfo(int id, byte status, byte credible, byte version, String name, String addition_info) {
        byte b = status;
        byte b2 = credible;
        int ret = USER_CLIENT_DEFAULT_ERROR;
        if ((b == 0 || b == 1) && (b2 == 0 || b2 == 1)) {
            IHwSecurityDiagnosePlugin plugin = getHwSecurityDiagnosePlugin();
            if (plugin != null) {
                try {
                    ret = plugin.sendThreatenInfo(id, b, b2, version, name, addition_info);
                } catch (RemoteException e) {
                    RemoteException remoteException = e;
                    Log.e(TAG, "RemoteException occurs when sending threaten info to stp hidl deamon");
                }
            }
            return ret;
        }
        Log.e(TAG, "Invalid stp item input arguments , status : " + b + " ,credible: " + b2);
        return USER_CLIENT_DEFAULT_ERROR;
    }
}
