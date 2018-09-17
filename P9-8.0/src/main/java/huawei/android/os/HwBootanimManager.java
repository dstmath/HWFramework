package huawei.android.os;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import huawei.android.content.HwContextEx;
import huawei.android.os.IHwBootanimManager.Stub;

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
        return Stub.asInterface(ServiceManager.getService(HwContextEx.HW_BOOTANIM_EX_SERVICE));
    }

    public void switchBootOrShutSound(String openOrClose) {
        IHwBootanimManager service = getService();
        if (service != null) {
            try {
                service.switchBootOrShutSound(openOrClose);
                return;
            } catch (RemoteException e) {
                Slog.e(TAG, "HwBootanimManager binder error!");
                return;
            }
        }
        Slog.e(TAG, "HwBootanimManager not exist!");
    }

    public int getBootAnimSoundSwitch() {
        IHwBootanimManager service = getService();
        if (service != null) {
            try {
                return service.getBootAnimSoundSwitch();
            } catch (RemoteException e) {
                Slog.e(TAG, "HwBootanimManager binder error!");
            }
        } else {
            Slog.e(TAG, "HwBootanimManager not exist!");
            return -1;
        }
    }

    public boolean isBootOrShutdownSoundCapable() {
        IHwBootanimManager service = getService();
        if (service != null) {
            try {
                return service.isBootOrShutdownSoundCapable();
            } catch (RemoteException e) {
                Slog.e(TAG, "HwBootanimManager binder error!");
            }
        } else {
            Slog.e(TAG, "HwBootanimManager not exist!");
            return false;
        }
    }
}
