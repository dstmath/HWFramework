package huawei.android.savedata;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import huawei.android.savedata.IHwSaveData;
import java.util.Map;

public class HwSaveDataManager {
    private static final Object LOCK = new Object();
    private static final String SERVICE_NAME = "hwSaveDataService";
    private static final String TAG = "HwSaveDataManager";
    private static HwSaveDataManager sHwSaveDataManager;
    private IHwSaveData mHwSaveDataService;
    private final Object mInstanceSync = new Object();

    private HwSaveDataManager() {
    }

    public static HwSaveDataManager getInstance() {
        HwSaveDataManager hwSaveDataManager;
        synchronized (LOCK) {
            if (sHwSaveDataManager == null) {
                sHwSaveDataManager = new HwSaveDataManager();
            }
            hwSaveDataManager = sHwSaveDataManager;
        }
        return hwSaveDataManager;
    }

    public IHwSaveData getService() {
        synchronized (this.mInstanceSync) {
            if (this.mHwSaveDataService != null) {
                return this.mHwSaveDataService;
            }
            this.mHwSaveDataService = IHwSaveData.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
            return this.mHwSaveDataService;
        }
    }

    public void putBinderObject(String name, IBinder binder) {
        try {
            IHwSaveData service = getService();
            if (service != null) {
                service.putBinderObject(name, binder);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "putBinderObject SaveData service binder error!");
        }
    }

    public void removeBinderObject(String name) {
        try {
            IHwSaveData service = getService();
            if (service != null) {
                service.removeBinderObject(name);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "putBinderObject SaveData service binder error!");
        }
    }

    public IBinder getBinderObject(String name) {
        try {
            IHwSaveData service = getService();
            if (service != null) {
                return service.getBinderObject(name);
            }
            return null;
        } catch (RemoteException e) {
            Slog.e(TAG, "getBinder SaveData service binder error! It can be ignore when phone start.");
            return null;
        }
    }

    public Map<String, IBinder> getBinderObjects(String prefix) {
        try {
            IHwSaveData service = getService();
            if (service != null) {
                return service.getBinderObjects(prefix);
            }
            return null;
        } catch (RemoteException e) {
            Slog.e(TAG, "getBinderObjects SaveData service binder error! It can be ignore when phone start.");
            return null;
        }
    }
}
