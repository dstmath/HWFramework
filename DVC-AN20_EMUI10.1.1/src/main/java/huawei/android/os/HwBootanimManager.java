package huawei.android.os;

import android.os.RemoteException;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.util.SlogEx;
import huawei.android.content.HwContextEx;
import huawei.android.os.IHwBootanimManager;

public class HwBootanimManager {
    private static final String TAG = "HwBootanimManager";
    private static volatile HwBootanimManager mInstance = null;

    public static synchronized HwBootanimManager getInstance() {
        HwBootanimManager hwBootanimManager;
        synchronized (HwBootanimManager.class) {
            if (mInstance == null) {
                mInstance = new HwBootanimManager();
            }
            hwBootanimManager = mInstance;
        }
        return hwBootanimManager;
    }

    private IHwBootanimManager getService() {
        return IHwBootanimManager.Stub.asInterface(ServiceManagerEx.getService(HwContextEx.HW_BOOTANIM_EX_SERVICE));
    }

    public void switchBootOrShutSound(String openOrClose) {
        IHwBootanimManager service = getService();
        if (service != null) {
            try {
                service.switchBootOrShutSound(openOrClose);
            } catch (RemoteException e) {
                SlogEx.e(TAG, "HwBootanimManager binder error!");
            }
        } else {
            SlogEx.e(TAG, "HwBootanimManager not exist!");
        }
    }

    public int getBootAnimSoundSwitch() {
        IHwBootanimManager service = getService();
        if (service != null) {
            try {
                return service.getBootAnimSoundSwitch();
            } catch (RemoteException e) {
                SlogEx.e(TAG, "HwBootanimManager binder error!");
                return -1;
            }
        } else {
            SlogEx.e(TAG, "HwBootanimManager not exist!");
            return -1;
        }
    }

    public boolean isBootOrShutdownSoundCapable() {
        IHwBootanimManager service = getService();
        if (service != null) {
            try {
                return service.isBootOrShutdownSoundCapable();
            } catch (RemoteException e) {
                SlogEx.e(TAG, "HwBootanimManager binder error!");
                return false;
            }
        } else {
            SlogEx.e(TAG, "HwBootanimManager not exist!");
            return false;
        }
    }
}
