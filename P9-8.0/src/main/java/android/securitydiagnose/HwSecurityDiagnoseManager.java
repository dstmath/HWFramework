package android.securitydiagnose;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.security.IHwSecurityDiagnoseCallback;
import huawei.android.security.IHwSecurityDiagnosePlugin;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.IHwSecurityService.Stub;

public class HwSecurityDiagnoseManager {
    private static final int DEVICE_SECURE_DIAGNOSE_ID = 2;
    private static final int GET_ROOT_STATUS_ERR = -1;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "HwSecurityDiagnoseManager";
    private static volatile HwSecurityDiagnoseManager sInstance;
    private IHwSecurityService mSecurityService = Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));

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

    /* JADX WARNING: Missing block: B:10:0x001d, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private IHwSecurityDiagnosePlugin getHwSecurityDiagnosePlugin() {
        synchronized (this) {
            if (this.mSecurityService != null) {
                try {
                    IHwSecurityDiagnosePlugin securityDiagnoseService = IHwSecurityDiagnosePlugin.Stub.asInterface(this.mSecurityService.querySecurityInterface(2));
                    if (securityDiagnoseService == null) {
                        Log.e(TAG, "error, HwSecurityDiagnosePlugin is null");
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getHwSecurityDiagnosePlugin invoked");
                }
            }
            Log.e(TAG, "error, SecurityService is null");
            return null;
        }
    }

    public void getRootStatus(IHwSecurityDiagnoseCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Must supply an get root status callback");
        }
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
    }
}
