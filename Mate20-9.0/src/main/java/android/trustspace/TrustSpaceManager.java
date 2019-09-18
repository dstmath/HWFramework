package android.trustspace;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.trustspace.ITrustSpaceManager;
import android.util.Log;
import huawei.android.security.IHwSecurityService;
import java.util.ArrayList;
import java.util.List;

public class TrustSpaceManager {
    private static boolean DEBUG = Log.HWINFO;
    public static final int FLAG_APPEND = 1;
    @Deprecated
    public static final int FLAG_HW_TRUSTSPACE = 16777216;
    public static final int FLAG_MATCH_ALL = 1;
    public static final int FLAG_MATCH_HIGH = 4;
    public static final int FLAG_MATCH_NORMAL = 2;
    public static final int FLAG_REMOVE = 3;
    public static final int FLAG_REMOVE_ALL = 4;
    public static final int FLAG_REPLACE_ALL = 2;
    public static final int PROTECTION_HIGH = 2;
    public static final int PROTECTION_INVALID = 0;
    public static final int PROTECTION_MASK_BASE = 255;
    public static final int PROTECTION_NORMAL = 1;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "TrustSpaceManager";
    private static final int TRUSTSPACE_PLUGIN_ID = 4;
    private static final Object mInstanceSync = new Object();
    private static volatile TrustSpaceManager sSelf = null;
    private static ITrustSpaceManager sTrustSpaceManager;

    private TrustSpaceManager() {
    }

    public static TrustSpaceManager getDefault() {
        if (sSelf == null) {
            sSelf = new TrustSpaceManager();
        }
        return sSelf;
    }

    public boolean isSupportTrustSpace() {
        if (getTrustSpaceManagerService() != null) {
            try {
                return sTrustSpaceManager.isSupportTrustSpace();
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    private static ITrustSpaceManager getTrustSpaceManagerService() {
        synchronized (mInstanceSync) {
            if (sTrustSpaceManager != null) {
                ITrustSpaceManager iTrustSpaceManager = sTrustSpaceManager;
                return iTrustSpaceManager;
            }
            IHwSecurityService secService = IHwSecurityService.Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    sTrustSpaceManager = ITrustSpaceManager.Stub.asInterface(secService.querySecurityInterface(4));
                } catch (RemoteException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Get TrustSpaceManagerService failed!");
                    }
                }
            }
            ITrustSpaceManager iTrustSpaceManager2 = sTrustSpaceManager;
            return iTrustSpaceManager2;
        }
    }

    public boolean addIntentProtectedApps(List<String> packages, int flags) {
        if (getTrustSpaceManagerService() != null) {
            try {
                return sTrustSpaceManager.addIntentProtectedApps(packages, flags);
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    @Deprecated
    public boolean addIntentProtectedApp(String packageName) {
        List<String> packages = new ArrayList<>(1);
        packages.add(packageName);
        return addIntentProtectedApps(packages, 1);
    }

    public boolean updateTrustApps(List<String> packages, int flag) {
        if (getTrustSpaceManagerService() != null) {
            try {
                return sTrustSpaceManager.updateTrustApps(packages, flag);
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    public boolean removeIntentProtectedApp(String packageName) {
        if (getTrustSpaceManagerService() != null) {
            try {
                return sTrustSpaceManager.removeIntentProtectedApp(packageName);
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    public List<String> getIntentProtectedApps(int flags) {
        if (getTrustSpaceManagerService() != null) {
            try {
                return sTrustSpaceManager.getIntentProtectedApps(flags);
            } catch (RemoteException e) {
            }
        }
        return new ArrayList(0);
    }

    public List<String> getAllIntentProtectedApps() {
        return getIntentProtectedApps(1);
    }

    public boolean removeIntentProtectedApps(List<String> packages, int flags) {
        if (getTrustSpaceManagerService() != null) {
            try {
                return sTrustSpaceManager.removeIntentProtectedApps(packages, flags);
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    public boolean removeAllIntentProtectedApps() {
        return removeIntentProtectedApps(null, 1);
    }

    public boolean isIntentProtectedApp(String packageName) {
        if (getTrustSpaceManagerService() != null) {
            try {
                return sTrustSpaceManager.isIntentProtectedApp(packageName);
            } catch (RemoteException e) {
            }
        }
        return false;
    }

    public int getProtectionLevel(String packageName) {
        if (getTrustSpaceManagerService() != null) {
            try {
                return sTrustSpaceManager.getProtectionLevel(packageName);
            } catch (RemoteException e) {
            }
        }
        return 0;
    }

    @Deprecated
    public boolean isHwTrustSpace(int userId) {
        if (getTrustSpaceManagerService() != null) {
            try {
                return sTrustSpaceManager.isHwTrustSpace(userId);
            } catch (RemoteException e) {
            }
        }
        return false;
    }
}
