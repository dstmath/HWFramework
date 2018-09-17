package huawei.android.ukey;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.huawei.hsm.permission.StubController;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.IHwSecurityService.Stub;
import huawei.android.security.ITSMAgent;
import huawei.android.security.IUKeyManager;

public class UKeyManagerImpl {
    private static boolean DEBUG = Log.HWINFO;
    private static final int FAILED = -1;
    private static final int SDK_VERSION = 1;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "UKeyManagerImpl";
    private static final int TSMAGENT_PLUGIN_ID = 7;
    private static final int UKEY_PLUGIN_ID = 6;
    public static final int UNSUPPORT_UKEY = 0;
    private static final Object mInstanceSync = new Object();
    private static IHwSecurityService mSecurityService;
    private static final Object mTsmLock = new Object();
    private static volatile UKeyManagerImpl sSelf = null;
    private static IUKeyManager sUKeyManager;

    private UKeyManagerImpl() {
    }

    public static UKeyManagerImpl getInstance() {
        if (sSelf == null) {
            synchronized (UKeyManagerImpl.class) {
                if (sSelf == null) {
                    sSelf = new UKeyManagerImpl();
                    mSecurityService = Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
                    if (mSecurityService == null) {
                        Log.e(TAG, "error, securityserver was null");
                    }
                }
            }
        }
        return sSelf;
    }

    private IUKeyManager getUKeyManagerService() {
        synchronized (mInstanceSync) {
            IUKeyManager iUKeyManager;
            if (sUKeyManager != null) {
                iUKeyManager = sUKeyManager;
                return iUKeyManager;
            } else if (mSecurityService != null) {
                try {
                    sUKeyManager = IUKeyManager.Stub.asInterface(mSecurityService.querySecurityInterface(6));
                    iUKeyManager = sUKeyManager;
                    return iUKeyManager;
                } catch (RemoteException e) {
                    if (DEBUG) {
                        Log.e("ukey", "Get UKeyManagerService failed!");
                    }
                    return null;
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:10:0x001f, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ITSMAgent getTSMAgentService() {
        synchronized (mTsmLock) {
            if (mSecurityService != null) {
                try {
                    ITSMAgent tsmAgent = ITSMAgent.Stub.asInterface(mSecurityService.querySecurityInterface(7));
                    if (tsmAgent == null) {
                        Log.e(TAG, "TSMAgent is null");
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException while getting TSMAgent");
                }
            }
            Log.e(TAG, "SecurityService is null");
            return null;
        }
    }

    public int getSDKVersion() {
        return 1;
    }

    public int getUKeyVersion() {
        if (getUKeyManagerService() != null) {
            try {
                return sUKeyManager.isSwitchFeatureOn();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while getting ukey version");
            }
        }
        return 0;
    }

    public int getUKeyStatus(String packageName) {
        if (getUKeyManagerService() != null) {
            try {
                return sUKeyManager.isUKeySwitchDisabled(packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while getting ukey switch status");
            }
        }
        return -1;
    }

    public void requestUKeyPermission(Context context, int requestCode) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.ukey.UKeyPermissionActivity");
        intent.putExtra(StubController.TABLE_COLUM_PACKAGE_NAME, context.getApplicationInfo().packageName);
        intent.putExtra("appName", context.getApplicationInfo().loadLabel(context.getPackageManager()));
        Log.i(TAG, "packageName = " + context.getApplicationInfo().packageName + "appName = " + context.getApplicationInfo().loadLabel(context.getPackageManager()));
        context.startActivityForResult(null, intent, requestCode, null);
    }

    public int createUKey(String spID, String ssdAid, String sign, String timeStamp) {
        Log.d(TAG, "createUKey...");
        ITSMAgent tsmAgent = getTSMAgentService();
        if (tsmAgent != null) {
            try {
                return tsmAgent.createSSD(spID, ssdAid, sign, timeStamp);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when createUKey is invoked");
            }
        }
        return -1;
    }

    public int deleteUKey(String spID, String ssdAid, String sign, String timeStamp) {
        Log.d(TAG, "deleteUKey...");
        ITSMAgent tsmAgent = getTSMAgentService();
        if (tsmAgent != null) {
            try {
                return tsmAgent.deleteSSD(spID, ssdAid, sign, timeStamp);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when deleteUKey is invoked");
            }
        }
        return -1;
    }

    public String getUKeyID() {
        Log.d(TAG, "getUKeyID...");
        ITSMAgent tsmAgent = getTSMAgentService();
        if (tsmAgent != null) {
            try {
                return tsmAgent.getCplc();
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when getUKeyID is invoked");
            }
        }
        return null;
    }

    public int syncUKey(String spID, String sign, String timeStamp) {
        Log.d(TAG, "syncUKey...");
        ITSMAgent tsmAgent = getTSMAgentService();
        if (tsmAgent != null) {
            try {
                return tsmAgent.initEse(spID, sign, timeStamp);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException when syncUKey is invoked");
            }
        }
        return -1;
    }
}
