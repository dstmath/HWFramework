package huawei.android.deviceinfo;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import com.huawei.android.os.storage.StorageManagerExt;
import huawei.android.deviceinfo.IHwDeviceInfoEx;

public class HwDeviceInfoManager {
    private static final String DEVICEINFO_SERVICE = "deviceinfoex";
    private static final String TAG = "HwDeviceInfoManager";
    private static volatile HwDeviceInfoManager sInstance = null;
    private IHwDeviceInfoEx mHwDeviceInfoExService;
    private final Object mInstanceSync = new Object();

    public static synchronized HwDeviceInfoManager getInstance() {
        HwDeviceInfoManager hwDeviceInfoManager;
        synchronized (HwDeviceInfoManager.class) {
            if (sInstance == null) {
                sInstance = new HwDeviceInfoManager();
            }
            hwDeviceInfoManager = sInstance;
        }
        return hwDeviceInfoManager;
    }

    public String getDeviceInfo(int type) {
        try {
            this.mHwDeviceInfoExService = getHwDeviceInfoEXService();
            if (this.mHwDeviceInfoExService != null) {
                return this.mHwDeviceInfoExService.getDeviceInfo(type);
            }
            return StorageManagerExt.INVALID_KEY_DESC;
        } catch (RemoteException e) {
            Slog.e(TAG, "getHwDeviceInfoEXService error!");
            return StorageManagerExt.INVALID_KEY_DESC;
        }
    }

    private IHwDeviceInfoEx getHwDeviceInfoEXService() {
        synchronized (this.mInstanceSync) {
            if (this.mHwDeviceInfoExService != null) {
                return this.mHwDeviceInfoExService;
            }
            this.mHwDeviceInfoExService = IHwDeviceInfoEx.Stub.asInterface(ServiceManager.getService(DEVICEINFO_SERVICE));
            return this.mHwDeviceInfoExService;
        }
    }
}
