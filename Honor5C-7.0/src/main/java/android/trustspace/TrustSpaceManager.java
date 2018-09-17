package android.trustspace;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.IHwSecurityService.Stub;
import java.util.ArrayList;
import java.util.List;

public class TrustSpaceManager {
    private static boolean DEBUG = false;
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
    private static final Object mInstanceSync = null;
    private static volatile TrustSpaceManager sSelf;
    private static ITrustSpaceManager sTrustSpaceManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.trustspace.TrustSpaceManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.trustspace.TrustSpaceManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.trustspace.TrustSpaceManager.<clinit>():void");
    }

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
            IHwSecurityService secService = Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
            if (secService != null) {
                try {
                    sTrustSpaceManager = ITrustSpaceManager.Stub.asInterface(secService.querySecurityInterface(TRUSTSPACE_PLUGIN_ID));
                } catch (RemoteException e) {
                    if (DEBUG) {
                        Log.e(TAG, "Get TrustSpaceManagerService failed!");
                    }
                }
            }
            iTrustSpaceManager = sTrustSpaceManager;
            return iTrustSpaceManager;
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
        List<String> packages = new ArrayList(PROTECTION_NORMAL);
        packages.add(packageName);
        return addIntentProtectedApps(packages, PROTECTION_NORMAL);
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
        return new ArrayList(PROTECTION_INVALID);
    }

    public List<String> getAllIntentProtectedApps() {
        return getIntentProtectedApps(PROTECTION_NORMAL);
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
        return removeIntentProtectedApps(null, PROTECTION_NORMAL);
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
        return PROTECTION_INVALID;
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
