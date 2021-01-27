package huawei.android.os;

import android.os.RemoteException;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.util.SlogEx;
import huawei.android.content.HwContextEx;
import huawei.android.os.IHwAntiTheftManager;

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
        return IHwAntiTheftManager.Stub.asInterface(ServiceManagerEx.getService(HwContextEx.HW_ANTI_THEFT_SERVICE));
    }

    public byte[] readAntiTheftData() {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.readAntiTheftData();
            }
            SlogEx.w(TAG, "AntiTheft is null!");
            return null;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "AntiTheft binder error!");
            return null;
        }
    }

    public int wipeAntiTheftData() {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.wipeAntiTheftData();
            }
            SlogEx.w(TAG, "AntiTheft is null!");
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "AntiTheft binder error!");
            return -1;
        }
    }

    public int writeAntiTheftData(byte[] writeToNative) {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.writeAntiTheftData(writeToNative);
            }
            SlogEx.w(TAG, "AntiTheft is null!");
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "AntiTheft binder error!");
            return -1;
        }
    }

    public int getAntiTheftDataBlockSize() {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.getAntiTheftDataBlockSize();
            }
            SlogEx.w(TAG, "AntiTheft is null!");
            return 0;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "AntiTheft binder error!");
            return 0;
        }
    }

    public int setAntiTheftEnabled(boolean enable) {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.setAntiTheftEnabled(enable);
            }
            SlogEx.w(TAG, "AntiTheft is null!");
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "AntiTheft binder error!");
            return -1;
        }
    }

    public boolean getAntiTheftEnabled() {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.getAntiTheftEnabled();
            }
            SlogEx.w(TAG, "AntiTheft is null!");
            return false;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "AntiTheft binder error!");
            return false;
        }
    }

    public boolean isAntiTheftSupported() {
        try {
            IHwAntiTheftManager service = getService();
            if (service != null) {
                return service.isAntiTheftSupported();
            }
            SlogEx.w(TAG, "AntiTheft is null!");
            return false;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "AntiTheft binder error!");
            return false;
        }
    }
}
