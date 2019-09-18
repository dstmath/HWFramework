package android.securityprofile;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.ISecurityProfileService;
import huawei.android.security.securityprofile.HwSignedInfo;
import java.util.List;

public class SecurityProfileManager {
    public static final int ACTION_ADD = 2;
    public static final int ACTION_REMOVE = 3;
    public static final int ACTION_UPDATE_ALL = 1;
    private static final int SECURITYPROFILE_PLUGIN_ID = 8;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "SecurityProfileManager";
    private static final Object mInstanceSync = new Object();
    private static ISecurityProfileService sSecurityProfileService;
    private static SecurityProfileManager sSelf = null;

    private SecurityProfileManager() {
    }

    public static SecurityProfileManager getDefault() {
        SecurityProfileManager securityProfileManager;
        synchronized (SecurityProfileManager.class) {
            if (sSelf == null) {
                sSelf = new SecurityProfileManager();
            }
            securityProfileManager = sSelf;
        }
        return securityProfileManager;
    }

    private static ISecurityProfileService getService() {
        synchronized (mInstanceSync) {
            if (sSecurityProfileService != null) {
                ISecurityProfileService iSecurityProfileService = sSecurityProfileService;
                return iSecurityProfileService;
            }
            IHwSecurityService secService = IHwSecurityService.Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    sSecurityProfileService = ISecurityProfileService.Stub.asInterface(secService.querySecurityInterface(8));
                } catch (RemoteException e) {
                    Log.e(TAG, "getService occurs RemoteException");
                }
            }
            RemoteException e2 = sSecurityProfileService;
            return e2;
        }
    }

    public void updateBlackApp(List blackListedPackages, int action) {
        if (getService() != null) {
            try {
                sSecurityProfileService.updateBlackApp(blackListedPackages, action);
            } catch (RemoteException e) {
                Log.e(TAG, "updateBlackApp occurs RemoteException");
            }
        }
    }

    public boolean isBlackApp(String packageName) {
        if (getService() != null) {
            try {
                return sSecurityProfileService.isBlackApp(packageName);
            } catch (RemoteException e) {
                Log.e(TAG, "isBlackApp occurs RemoteException");
            }
        }
        return false;
    }

    public Bundle getHwSignedInfo(String packageName, Bundle extraParams) {
        if (getService() != null) {
            return HwSignedInfo.getHwSignedInfo(sSecurityProfileService, packageName, extraParams);
        }
        return null;
    }

    public Bundle setHwSignedInfoToSEAPP(Bundle params) {
        if (getService() != null) {
            return HwSignedInfo.setHwSignedInfoToSEAPP(sSecurityProfileService, params);
        }
        return null;
    }
}
