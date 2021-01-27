package android.net.booster;

import android.net.booster.IHwCommBoosterService;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public class HwCommBoosterServiceManager implements IHwCommBoosterServiceManager {
    private static final boolean BOOSTER_SUPPORT = SystemProperties.getBoolean("ro.config.hw_booster", true);
    private static final String TAG = "HwCommBoosterServiceManager";
    private static final Object mLock = new Object();
    private static BoosterCallback sBoosterCallbackInstance = null;
    private static IHwCommBoosterService sHwBoosterService = null;
    private static HwCommBoosterServiceManager sInstance = null;

    public static HwCommBoosterServiceManager getInstance() {
        HwCommBoosterServiceManager hwCommBoosterServiceManager;
        synchronized (mLock) {
            if (sInstance == null) {
                Log.i(TAG, "getInstance");
                sInstance = new HwCommBoosterServiceManager();
            }
            registerBoosterCallback();
            hwCommBoosterServiceManager = sInstance;
        }
        return hwCommBoosterServiceManager;
    }

    private static void registerBoosterCallback() {
        if (sBoosterCallbackInstance == null) {
            sBoosterCallbackInstance = new BoosterCallback();
        }
        sBoosterCallbackInstance.linkToBoosterService();
    }

    private HwCommBoosterServiceManager() {
    }

    private IHwCommBoosterService getService() {
        if (sHwBoosterService == null) {
            Log.i(TAG, "getService if null");
            sHwBoosterService = IHwCommBoosterService.Stub.asInterface(ServiceManager.getService("HwCommBoosterService"));
        }
        registerBoosterCallback();
        return sHwBoosterService;
    }

    public int registerCallBack(String pkgName, IHwCommBoosterCallback cb) {
        if (!BOOSTER_SUPPORT || pkgName == null || cb == null) {
            Log.w(TAG, "registerCallBack failed invalid input");
            return -3;
        }
        IHwCommBoosterService service = getService();
        if (service != null) {
            try {
                return service.registerCallBack(pkgName, cb);
            } catch (RemoteException ex) {
                Log.w(TAG, "registerCallBack exception! ", ex);
                return -2;
            }
        } else {
            Log.e(TAG, "registerCallBack failed, IHwCommBoosterService is null");
            return -1;
        }
    }

    public int unRegisterCallBack(String pkgName, IHwCommBoosterCallback cb) {
        if (!BOOSTER_SUPPORT || pkgName == null || cb == null) {
            Log.w(TAG, "unRegisterCallBack failed invalid input");
            return -3;
        }
        IHwCommBoosterService service = getService();
        if (service != null) {
            try {
                return service.unRegisterCallBack(pkgName, cb);
            } catch (RemoteException ex) {
                Log.w(TAG, "unRegisterCallBack exception! ", ex);
                return -2;
            }
        } else {
            Log.e(TAG, "unRegisterCallBack failed, IHwCommBoosterService is null");
            return -1;
        }
    }

    public int reportBoosterPara(String pkgName, int dataType, Bundle data) {
        if (!BOOSTER_SUPPORT || pkgName == null || data == null) {
            Log.w(TAG, "reportBoosterPara failed invalid input");
            return -3;
        }
        IHwCommBoosterService service = getService();
        if (service != null) {
            try {
                return service.reportBoosterPara(pkgName, dataType, data);
            } catch (RemoteException ex) {
                Log.w(TAG, "reportBoosterPara exception! ", ex);
                return -2;
            }
        } else {
            Log.e(TAG, "reportBoosterPara failed, IHwCommBoosterService is null");
            return -1;
        }
    }

    public Bundle getBoosterPara(String pkgName, int dataType, Bundle data) {
        if (!BOOSTER_SUPPORT || pkgName == null || data == null) {
            Log.w(TAG, "getBoosterPara failed, invalid input");
            return null;
        }
        IHwCommBoosterService service = getService();
        if (service != null) {
            try {
                return service.getBoosterPara(pkgName, dataType, data);
            } catch (RemoteException e) {
                Log.w(TAG, "getBoosterPara exception!");
                return null;
            }
        } else {
            Log.e(TAG, "getBoosterPara failed, IHwCommBoosterService is null");
            return null;
        }
    }

    public static class BoosterCallback extends Binder implements IBinder.DeathRecipient {
        private static final String DESCRIPTOR = "HwCommBoosterServiceManager.BoosterCallback";
        private IBinder boosterService = null;

        public void linkToBoosterService() {
            if (this.boosterService == null) {
                try {
                    this.boosterService = ServiceManager.getService("HwCommBoosterService");
                    if (this.boosterService != null) {
                        this.boosterService.linkToDeath(this, 0);
                        Log.i(HwCommBoosterServiceManager.TAG, "linkToBoosterService. " + this.boosterService);
                        return;
                    }
                    Log.e(HwCommBoosterServiceManager.TAG, "failed to get IBoosterService.");
                } catch (RemoteException e) {
                    Log.e(HwCommBoosterServiceManager.TAG, "RemoteException");
                }
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            Log.i(HwCommBoosterServiceManager.TAG, "Booster died." + this.boosterService);
            BoosterCallback unused = HwCommBoosterServiceManager.sBoosterCallbackInstance = null;
            IHwCommBoosterService unused2 = HwCommBoosterServiceManager.sHwBoosterService = null;
        }
    }
}
