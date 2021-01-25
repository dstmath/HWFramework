package huawei.android.os;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import huawei.android.os.IHwGeneralManager;

public class HwStorageManager {
    private static final String TAG = "HwStorageManager";
    private static HwStorageManager sInstance;
    private final IHwGeneralManager mIHwGeneralManager = IHwGeneralManager.Stub.asInterface(ServiceManager.getService("hwGeneralService"));

    private HwStorageManager() {
    }

    public static synchronized HwStorageManager getInstance() {
        HwStorageManager hwStorageManager;
        synchronized (HwStorageManager.class) {
            if (sInstance == null) {
                sInstance = new HwStorageManager();
            }
            hwStorageManager = sInstance;
        }
        return hwStorageManager;
    }

    public boolean isIsolatedStorageApp(int uid, String packageName) {
        try {
            if (this.mIHwGeneralManager != null) {
                return this.mIHwGeneralManager.isIsolatedStorageApp(uid, packageName);
            }
            Slog.w(TAG, "isIsolatedStorageApp mIHwGeneralManager is null");
            return true;
        } catch (RemoteException e) {
            Slog.e(TAG, "isIsolatedStorageApp RemoteException");
            return true;
        }
    }

    public String[] getIsolatedStorageApps(int excludeFlag) {
        try {
            if (this.mIHwGeneralManager != null) {
                return this.mIHwGeneralManager.getIsolatedStorageApps(excludeFlag);
            }
            Slog.w(TAG, "getIsolatedStorageApps mIHwGeneralManager is null");
            return new String[0];
        } catch (RemoteException e) {
            Slog.e(TAG, "getIsolatedStorageApps RemoteException");
        }
    }
}
