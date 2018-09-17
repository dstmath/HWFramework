package huawei.android.os;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import huawei.android.content.HwContextEx;
import huawei.android.os.IHwAntiTheftManager.Stub;

public class HwAntiTheftManager {
    private static final String TAG = "AntiTheftManager";
    private static volatile HwAntiTheftManager mInstance = null;

    public static synchronized HwAntiTheftManager getInstance() {
        HwAntiTheftManager hwAntiTheftManager;
        synchronized (HwAntiTheftManager.class) {
            if (mInstance == null) {
                mInstance = new HwAntiTheftManager();
            }
            hwAntiTheftManager = mInstance;
        }
        return hwAntiTheftManager;
    }

    private HwAntiTheftManager() {
    }

    private IHwAntiTheftManager getService() {
        return Stub.asInterface(ServiceManager.getService(HwContextEx.HW_ANTI_THEFT_SERVICE));
    }

    public byte[] readAntiTheftData() {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.readAntiTheftData();
            }
            Slog.w(TAG, "AntiTheft is null!");
            return null;
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
    }

    public int wipeAntiTheftData() {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.wipeAntiTheftData();
            }
            Slog.w(TAG, "AntiTheft is null!");
            return -1;
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
    }

    public int writeAntiTheftData(byte[] writeToNative) {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.writeAntiTheftData(writeToNative);
            }
            Slog.w(TAG, "AntiTheft is null!");
            return -1;
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
    }

    public int getAntiTheftDataBlockSize() {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.getAntiTheftDataBlockSize();
            }
            Slog.w(TAG, "AntiTheft is null!");
            return 0;
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
    }

    public int setAntiTheftEnabled(boolean enable) {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.setAntiTheftEnabled(enable);
            }
            Slog.w(TAG, "AntiTheft is null!");
            return -1;
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
    }

    public boolean getAntiTheftEnabled() {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.getAntiTheftEnabled();
            }
            Slog.w(TAG, "AntiTheft is null!");
            return false;
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
    }

    public boolean checkRootState() {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.checkRootState();
            }
            Slog.w(TAG, "AntiTheft is null!");
            return false;
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
    }

    public boolean isAntiTheftSupported() {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.isAntiTheftSupported();
            }
            Slog.w(TAG, "AntiTheft is null!");
            return false;
        } catch (RemoteException e) {
            Slog.e(TAG, "AntiTheft binder error!");
        }
    }
}
