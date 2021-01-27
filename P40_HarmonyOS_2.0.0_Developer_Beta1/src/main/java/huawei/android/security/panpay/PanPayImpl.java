package huawei.android.security.panpay;

import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.app.ActivityThreadEx;
import com.huawei.hwpanpayservice.IHwSEService;
import huawei.android.ukey.HwSEServiceManager;
import java.util.HashMap;

public class PanPayImpl {
    private static final int FAILED = -1;
    private static final Object INSTANCE_SYNC = new Object();
    private static final String TAG = "PanPayImpl";
    private static IHwSEService sHwSEService = null;
    private static volatile PanPayImpl sSelf = null;

    private PanPayImpl() {
    }

    public static PanPayImpl getInstance() {
        if (sSelf == null) {
            synchronized (PanPayImpl.class) {
                if (sSelf == null) {
                    sSelf = new PanPayImpl();
                }
            }
        }
        return sSelf;
    }

    private static IHwSEService getHwSEService() {
        IHwSEService iHwSEService;
        synchronized (INSTANCE_SYNC) {
            HwSEServiceManager.initRemoteService(ActivityThreadEx.currentApplication().getApplicationContext());
            sHwSEService = HwSEServiceManager.getRemoteServiceInstance();
            if (sHwSEService != null) {
                Log.d(TAG, "getHwPanPayService successfully.");
            } else {
                Log.e(TAG, "getHwPanPayService failed.");
            }
            iHwSEService = sHwSEService;
        }
        return iHwSEService;
    }

    public int checkEligibility(String spID) {
        if (TextUtils.isEmpty(spID)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.i(TAG, "CheckEligibility..spID is " + spID);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.checkEligibility(spID);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when checkEligibility is invoked.");
                }
            }
            return -1;
        }
    }

    public int syncSeInfo(String spID, String sign, String timeStamp) {
        if (TextUtils.isEmpty(spID) || TextUtils.isEmpty(sign) || TextUtils.isEmpty(timeStamp)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "SyncSeInfo..spID is " + spID);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.syncSeInfo(spID, sign, timeStamp);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when syncSeInfo is invoked.");
                }
            }
            return -1;
        }
    }

    public int createSSD(String spID, String sign, String timeStamp, String ssdAid) {
        if (TextUtils.isEmpty(spID) || TextUtils.isEmpty(sign) || TextUtils.isEmpty(timeStamp) || TextUtils.isEmpty(ssdAid)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "CreateSSD..spID is " + spID + " ssdAid is " + ssdAid);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.createSSD(spID, sign, timeStamp, ssdAid);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when createSSD is invoked." + e);
                }
            }
            return -1;
        }
    }

    public int deleteSSD(String spID, String sign, String timeStamp, String ssdAid) {
        if (TextUtils.isEmpty(spID) || TextUtils.isEmpty(sign) || TextUtils.isEmpty(timeStamp) || TextUtils.isEmpty(ssdAid)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "DeleteSSD..spID is " + spID + " ssdAid is " + ssdAid);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.deleteSSD(spID, sign, timeStamp, ssdAid);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when createSSD is invoked.");
                }
            }
            return -1;
        }
    }

    public int checkEligibilityEx(String serviceId, String funCallId) {
        if (TextUtils.isEmpty(serviceId) || TextUtils.isEmpty(funCallId)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "CheckEligibility..serviceId is " + serviceId + " funCallId is " + funCallId);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.checkEligibilityEx(serviceId, funCallId);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when createSSD is invoked.");
                }
            }
            return -1;
        }
    }

    public int syncSeInfoEx(String serviceId, String funCallId) {
        if (TextUtils.isEmpty(serviceId) || TextUtils.isEmpty(funCallId)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "SyncSeInfoX..serviceId is " + serviceId + " funCallId is " + funCallId);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.syncSeInfoEx(serviceId, funCallId);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when createSSD is invoked.");
                }
            }
            return -1;
        }
    }

    public int createSSDEx(String serviceId, String funCallId, String ssdAid) {
        if (TextUtils.isEmpty(serviceId) || TextUtils.isEmpty(funCallId) || TextUtils.isEmpty(ssdAid)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "CreateSSD..serviceId is " + serviceId + " funCallId is " + funCallId + " ssdAid is " + ssdAid);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.createSSDEx(serviceId, funCallId, ssdAid);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when createSSD is invoked.");
                }
            }
            return -1;
        }
    }

    public int deleteSSDEx(String serviceId, String funCallId, String ssdAid) {
        if (TextUtils.isEmpty(serviceId) || TextUtils.isEmpty(funCallId) || TextUtils.isEmpty(ssdAid)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "DeleteSSD..serviceId is " + serviceId + " funCallId is " + funCallId + " ssdAid is " + ssdAid);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.deleteSSDEx(serviceId, funCallId, ssdAid);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when deleteSSD is invoked.");
                }
            }
            return -1;
        }
    }

    public int installApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        if (TextUtils.isEmpty(serviceId) || TextUtils.isEmpty(funCallId) || TextUtils.isEmpty(appletAid) || TextUtils.isEmpty(appletVersion)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "InstallApplet..serviceId is " + serviceId + " funCallId is " + funCallId + " appletAid is " + appletAid);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.installApplet(serviceId, funCallId, appletAid, appletVersion);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when installApplet is invoked.");
                }
            }
            return -1;
        }
    }

    public int deleteApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        if (TextUtils.isEmpty(serviceId) || TextUtils.isEmpty(funCallId) || TextUtils.isEmpty(appletAid) || TextUtils.isEmpty(appletVersion)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "DeleteApplet..serviceId is " + serviceId + " funCallId is " + funCallId + " appletAid is " + appletAid);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.deleteSSD(serviceId, funCallId, appletAid, appletVersion);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when deleteApplet is invoked.");
                }
            }
            return -1;
        }
    }

    public int lockApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        if (TextUtils.isEmpty(serviceId) || TextUtils.isEmpty(funCallId) || TextUtils.isEmpty(appletAid) || TextUtils.isEmpty(appletVersion)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "LockApplet..serviceId is " + serviceId + " funCallId is " + funCallId + " appletAid is " + appletAid);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.lockApplet(serviceId, funCallId, appletAid, appletVersion);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when lockApplet is invoked.");
                }
            }
            return -1;
        }
    }

    public int unlockApplet(String serviceId, String funCallId, String appletAid, String appletVersion) {
        if (TextUtils.isEmpty(serviceId) || TextUtils.isEmpty(funCallId) || TextUtils.isEmpty(appletAid) || TextUtils.isEmpty(appletVersion)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "UnlockApplet..serviceId is " + serviceId + " funCallId is " + funCallId + " appletAid is " + appletAid);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.unlockApplet(serviceId, funCallId, appletAid, appletVersion);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when unlockApplet is invoked.");
                }
            }
            return -1;
        }
    }

    public int activateApplet(String serviceId, String funCallId, String appletAid) {
        if (TextUtils.isEmpty(serviceId) || TextUtils.isEmpty(funCallId) || TextUtils.isEmpty(appletAid)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "ActivateApplet..serviceId is " + serviceId + " funCallId is " + funCallId + " appletAid is " + appletAid);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.activateApplet(serviceId, funCallId, appletAid);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when activateApplet is invoked.");
                }
            }
            return -1;
        }
    }

    public int commonExecute(String spID, String serviceId, String funCallId) {
        if (TextUtils.isEmpty(spID) || TextUtils.isEmpty(serviceId) || TextUtils.isEmpty(funCallId)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.d(TAG, "CommonExecute..serviceId is " + serviceId + " funCallId is " + funCallId);
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.commonExecute(spID, serviceId, funCallId);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when commonExecute is invoked.");
                }
            }
            return -1;
        }
    }

    public String getCPLC(String spID) {
        if (TextUtils.isEmpty(spID)) {
            return null;
        }
        synchronized (INSTANCE_SYNC) {
            Log.i(TAG, "GetCPLC..");
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.getCPLC(spID);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getCPLC is invoked." + e);
                }
            }
            return null;
        }
    }

    public String getCIN(String spID) {
        if (TextUtils.isEmpty(spID)) {
            return null;
        }
        synchronized (INSTANCE_SYNC) {
            Log.i(TAG, "GetCIN..");
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.getCIN(spID);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getCIN is invoked.");
                }
            }
            return null;
        }
    }

    public String getIIN(String spID) {
        if (TextUtils.isEmpty(spID)) {
            return null;
        }
        synchronized (INSTANCE_SYNC) {
            Log.i(TAG, "GetIIN..");
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.getIIN(spID);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getIIN is invoked.");
                }
            }
            return null;
        }
    }

    public boolean getSwitch(String spID) {
        synchronized (INSTANCE_SYNC) {
            if (TextUtils.isEmpty(spID)) {
                return false;
            }
            Log.i(TAG, "GetSwitch..");
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.getSwitch(spID);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getSwitch is invoked.");
                }
            }
            return false;
        }
    }

    public int setSwitch(String spID, boolean choice) {
        if (TextUtils.isEmpty(spID)) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.i(TAG, "SetSwitch..");
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.setSwitch(spID, choice);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getSwitch is invoked.");
                }
            }
            return -1;
        }
    }

    public String[] getLastErrorInfo(String spID) {
        if (TextUtils.isEmpty(spID)) {
            return new String[0];
        }
        synchronized (INSTANCE_SYNC) {
            Log.i(TAG, "GetLastErrorInfo..");
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.getLastErrorInfo(spID);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getLastErrorInfo is invoked.");
                }
            }
            return new String[0];
        }
    }

    public int setConfig(String spID, HashMap<String, String> config) {
        if (TextUtils.isEmpty(spID) || config == null || config.size() == 0) {
            return -1;
        }
        synchronized (INSTANCE_SYNC) {
            Log.i(TAG, "SetConfig..");
            IHwSEService hwSEService = getHwSEService();
            if (hwSEService != null) {
                try {
                    return hwSEService.setConfig(spID, config);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException when getSwitch is invoked.");
                }
            }
            return -1;
        }
    }
}
