package huawei.android.net;

import android.os.RemoteException;
import android.os.ServiceManager;
import huawei.android.content.HwContextEx;
import huawei.android.net.IConnectivityExManager.Stub;

public class HwConnectivityExManager {
    private static volatile HwConnectivityExManager mInstance = null;
    IConnectivityExManager mService;

    public static synchronized HwConnectivityExManager getDefault() {
        HwConnectivityExManager hwConnectivityExManager;
        synchronized (HwConnectivityExManager.class) {
            if (mInstance == null) {
                mInstance = new HwConnectivityExManager();
            }
            hwConnectivityExManager = mInstance;
        }
        return hwConnectivityExManager;
    }

    public HwConnectivityExManager() {
        this.mService = null;
        this.mService = Stub.asInterface(ServiceManager.getService(HwContextEx.HW_CONNECTIVITY_EX_SERVICE));
    }

    public void setSmartKeyguardLevel(String level) {
        try {
            this.mService.setSmartKeyguardLevel(level);
        } catch (RemoteException e) {
        }
    }

    public void setUseCtrlSocket(boolean flag) {
    }
}
