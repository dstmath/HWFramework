package android.net.booster;

import android.net.booster.IHwCommBoosterService;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

public class HwCommBoosterServiceManager implements IHwCommBoosterServiceManager {
    private static final boolean BOOSTER_SUPPORT = SystemProperties.getBoolean("ro.config.hw_booster", true);
    private static final String TAG = "HwCommBoosterServiceManager";
    private static final Object mLock = new Object();
    private static HwCommBoosterServiceManager sInstance = null;

    public static HwCommBoosterServiceManager getInstance() {
        HwCommBoosterServiceManager hwCommBoosterServiceManager;
        synchronized (mLock) {
            if (sInstance == null) {
                sInstance = new HwCommBoosterServiceManager();
            }
            hwCommBoosterServiceManager = sInstance;
        }
        return hwCommBoosterServiceManager;
    }

    private HwCommBoosterServiceManager() {
    }

    private IHwCommBoosterService getService() {
        return IHwCommBoosterService.Stub.asInterface(ServiceManager.getService("HwCommBoosterService"));
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
}
