package huawei.android.security.privacyability;

import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.privacyability.IIDAnonymizationManager;

public class IDAnonymizationManagerImpl {
    private static final int FAILED = -1;
    private static final int ID_ANONYMIZATION_PLUGIN_ID = 21;
    private static final Object INSTANCE_SYNC = new Object();
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "IDAnonymizationManagerImpl";
    private static IIDAnonymizationManager sIDAnonymizationManager;
    private static volatile IDAnonymizationManagerImpl sInstance = null;
    private static IHwSecurityService sSecurityService;

    private IDAnonymizationManagerImpl() {
    }

    public static IDAnonymizationManagerImpl getInstance() {
        if (sInstance == null) {
            synchronized (IDAnonymizationManagerImpl.class) {
                if (sInstance == null) {
                    sInstance = new IDAnonymizationManagerImpl();
                }
            }
        }
        return sInstance;
    }

    private IIDAnonymizationManager getIDAnonymizationManagerService() {
        synchronized (INSTANCE_SYNC) {
            if (sIDAnonymizationManager != null) {
                return sIDAnonymizationManager;
            }
            try {
                sSecurityService = IHwSecurityService.Stub.asInterface(ServiceManagerEx.getService(SECURITY_SERVICE));
                if (sSecurityService != null) {
                    IBinder secPlugin = sSecurityService.querySecurityInterface(21);
                    sIDAnonymizationManager = IIDAnonymizationManager.Stub.asInterface(secPlugin);
                    if (secPlugin != null) {
                        secPlugin.linkToDeath(new IBinder.DeathRecipient() {
                            /* class huawei.android.security.privacyability.IDAnonymizationManagerImpl.AnonymousClass1 */

                            @Override // android.os.IBinder.DeathRecipient
                            public void binderDied() {
                                synchronized (IDAnonymizationManagerImpl.INSTANCE_SYNC) {
                                    IHwSecurityService unused = IDAnonymizationManagerImpl.sSecurityService = null;
                                    Log.e(IDAnonymizationManagerImpl.TAG, "secPlugin is died.");
                                }
                            }
                        }, 0);
                    } else {
                        Log.i(TAG, "sIDAnonymizationManager is null.");
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Get getIDAnonymizationManagerService failed!");
            } catch (Exception e2) {
                Log.e(TAG, "getService occurs Exception.");
            } catch (Error e3) {
                Log.e(TAG, "getService occurs Error.");
            }
            return sIDAnonymizationManager;
        }
    }

    public String getCUID() {
        synchronized (INSTANCE_SYNC) {
            IIDAnonymizationManager idAnonymizationManager = getIDAnonymizationManagerService();
            if (idAnonymizationManager != null) {
                try {
                    return idAnonymizationManager.getCUID();
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getCUID is invoked.");
                } catch (Exception e2) {
                    Log.e(TAG, "getCUID occurs Exception.");
                }
            }
            return null;
        }
    }

    public String getCFID(String containerID, String contentProviderTag) {
        synchronized (INSTANCE_SYNC) {
            IIDAnonymizationManager idAnonymizationManager = getIDAnonymizationManagerService();
            if (idAnonymizationManager != null) {
                try {
                    return idAnonymizationManager.getCFID(containerID, contentProviderTag);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getCFID is invoked.");
                } catch (Exception e2) {
                    Log.e(TAG, "getCFID occurs Exception.");
                }
            }
            return null;
        }
    }

    public int resetCUID() {
        synchronized (INSTANCE_SYNC) {
            IIDAnonymizationManager idAnonymizationManager = getIDAnonymizationManagerService();
            if (idAnonymizationManager != null) {
                try {
                    return idAnonymizationManager.resetCUID();
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when resetCUID is invoked.");
                } catch (Exception e2) {
                    Log.e(TAG, "resetCUID occurs Exception.");
                }
            }
            return -1;
        }
    }
}
