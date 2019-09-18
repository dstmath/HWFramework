package huawei.android.security.panpay;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.security.IHwSecurityService;
import huawei.android.security.panpay.IPanPay;
import java.util.HashMap;

public class PanPayImpl {
    private static final int FAILED = -1;
    private static final int PAN_PAY_PLUGIN_ID = 12;
    private static final String SECURITY_SERVICE = "securityserver";
    private static final String TAG = "PanPayImpl";
    private static final Object mInstanceSync = new Object();
    private static IHwSecurityService mSecurityService;
    private static IPanPay sPanPayManager;
    private static volatile PanPayImpl sSelf = null;

    private PanPayImpl() {
    }

    public static PanPayImpl getInstance() {
        if (sSelf == null) {
            synchronized (PanPayImpl.class) {
                if (sSelf == null) {
                    sSelf = new PanPayImpl();
                    mSecurityService = IHwSecurityService.Stub.asInterface(ServiceManager.getService(SECURITY_SERVICE));
                    if (mSecurityService == null) {
                        Log.e(TAG, "error, securityserver was null");
                    }
                }
            }
        }
        return sSelf;
    }

    private IPanPay getPanPayManagerService() {
        synchronized (mInstanceSync) {
            if (sPanPayManager != null) {
                IPanPay iPanPay = sPanPayManager;
                return iPanPay;
            }
            if (mSecurityService != null) {
                try {
                    sPanPayManager = IPanPay.Stub.asInterface(mSecurityService.querySecurityInterface(12));
                    IPanPay iPanPay2 = sPanPayManager;
                    return iPanPay2;
                } catch (RemoteException e) {
                    Log.e(TAG, "Get getPanPayManagerService failed!");
                }
            }
            return null;
        }
    }

    public int checkEligibility(String spID) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "checkEligibility...spID is " + spID);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int checkEligibility = panpayManager.checkEligibility(spID);
                    return checkEligibility;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when checkEligibility is invoked");
                }
            }
            return -1;
        }
    }

    public int syncSeInfo(String spID, String sign, String timeStamp) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "syncSeInfo...spID is " + spID);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int syncSeInfo = panpayManager.syncSeInfo(spID, sign, timeStamp);
                    return syncSeInfo;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when syncSeInfo is invoked");
                }
            }
            return -1;
        }
    }

    public int createSSD(String spID, String sign, String timeStamp, String ssdAid) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "createSSD...spID is " + spID + " ssdAid is " + ssdAid);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int createSSD = panpayManager.createSSD(spID, sign, timeStamp, ssdAid);
                    return createSSD;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when createSSD is invoked");
                }
            }
            return -1;
        }
    }

    public int deleteSSD(String spID, String sign, String timeStamp, String ssdAid) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "deleteSSD...spID is " + spID + " ssdAid is " + ssdAid);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int deleteSSD = panpayManager.deleteSSD(spID, sign, timeStamp, ssdAid);
                    return deleteSSD;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when createSSD is invoked");
                }
            }
            return -1;
        }
    }

    public int checkEligibilityEx(String serviceId, String funCallId) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "checkEligibility...serviceId is " + serviceId + " funCallId is " + funCallId);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int checkEligibilityEx = panpayManager.checkEligibilityEx(serviceId, funCallId);
                    return checkEligibilityEx;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when createSSD is invoked");
                }
            }
            return -1;
        }
    }

    public int syncSeInfoEx(String serviceId, String funCallId) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "syncSeInfoX...serviceId is " + serviceId + " funCallId is " + funCallId);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int syncSeInfoEx = panpayManager.syncSeInfoEx(serviceId, funCallId);
                    return syncSeInfoEx;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when createSSD is invoked");
                }
            }
            return -1;
        }
    }

    public int createSSDEx(String serviceId, String funCallId, String ssdAid) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "createSSD...serviceId is " + serviceId + " funCallId is " + funCallId + " ssdAid is " + ssdAid);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int createSSDEx = panpayManager.createSSDEx(serviceId, funCallId, ssdAid);
                    return createSSDEx;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when createSSD is invoked");
                }
            }
            return -1;
        }
    }

    public int deleteSSDEx(String serviceId, String funCallId, String ssdAid) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "deleteSSD...serviceId is " + serviceId + " funCallId is " + funCallId + " ssdAid is " + ssdAid);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int deleteSSDEx = panpayManager.deleteSSDEx(serviceId, funCallId, ssdAid);
                    return deleteSSDEx;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when deleteSSD is invoked");
                }
            }
            return -1;
        }
    }

    public int installApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "installApplet...serviceId is " + serviceId + " funCallId is " + funCallId + " appletAid is " + appletAid);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int installApplet = panpayManager.installApplet(serviceId, funCallId, appletAid, appletVersion);
                    return installApplet;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when installApplet is invoked");
                }
            }
            return -1;
        }
    }

    public int deleteApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "deleteApplet...serviceId is " + serviceId + " funCallId is " + funCallId + " appletAid is " + appletAid);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int deleteSSD = panpayManager.deleteSSD(serviceId, funCallId, appletAid, appletAid);
                    return deleteSSD;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when deleteApplet is invoked");
                }
            }
            return -1;
        }
    }

    public int lockApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "lockApplet...serviceId is " + serviceId + " funCallId is " + funCallId + " appletAid is " + appletAid);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int lockApplet = panpayManager.lockApplet(serviceId, funCallId, appletAid, appletVersion);
                    return lockApplet;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when lockApplet is invoked");
                }
            }
            return -1;
        }
    }

    public int unlockApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "unlockApplet...serviceId is " + serviceId + " funCallId is " + funCallId + " appletAid is " + appletAid);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int unlockApplet = panpayManager.unlockApplet(serviceId, funCallId, appletAid, appletVersion);
                    return unlockApplet;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when unlockApplet is invoked");
                }
            }
            return -1;
        }
    }

    public int activateApplet(String serviceId, String funCallId, String appletAid) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "activateApplet...serviceId is " + serviceId + " funCallId is " + funCallId + " appletAid is " + appletAid);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int activateApplet = panpayManager.activateApplet(serviceId, funCallId, appletAid);
                    return activateApplet;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when activateApplet is invoked");
                }
            }
            return -1;
        }
    }

    public int commonExecute(String spID, String serviceId, String funCallId) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "commonExecute...serviceId is " + serviceId + " funCallId is " + funCallId);
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int commonExecute = panpayManager.commonExecute(spID, serviceId, funCallId);
                    return commonExecute;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when commonExecute is invoked");
                }
            }
            return -1;
        }
    }

    public String getCPLC(String spID) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "getCPLC...");
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    String cplc = panpayManager.getCPLC(spID);
                    return cplc;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getCPLC is invoked");
                }
            }
            return null;
        }
    }

    public String getCIN(String spID) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "getCIN...");
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    String cin = panpayManager.getCIN(spID);
                    return cin;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getCIN is invoked");
                }
            }
            return null;
        }
    }

    public String getIIN(String spID) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "getIIN...");
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    String iin = panpayManager.getIIN(spID);
                    return iin;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getIIN is invoked");
                }
            }
            return null;
        }
    }

    public boolean getSwitch(String spID) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "getSwitch...");
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    boolean z = panpayManager.getSwitch(spID);
                    return z;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getSwitch is invoked");
                }
            }
            return false;
        }
    }

    public int setSwitch(String spID, boolean choice) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "setSwitch...");
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int i = panpayManager.setSwitch(spID, choice);
                    return i;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getSwitch is invoked");
                }
            }
            return -1;
        }
    }

    public String[] getLastErrorInfo(String spID) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "getLastErrorInfo...");
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    String[] lastErrorInfo = panpayManager.getLastErrorInfo(spID);
                    return lastErrorInfo;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getLastErrorInfo is invoked");
                }
            }
            String[] strArr = new String[0];
            return strArr;
        }
    }

    public int setConfig(String spID, HashMap config) {
        synchronized (mInstanceSync) {
            Log.d(TAG, "setConfig...");
            IPanPay panpayManager = getPanPayManagerService();
            if (panpayManager != null) {
                try {
                    int config2 = panpayManager.setConfig(spID, config);
                    return config2;
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getSwitch is invoked");
                }
            }
            return -1;
        }
    }
}
