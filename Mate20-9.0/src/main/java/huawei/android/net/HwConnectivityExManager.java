package huawei.android.net;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import huawei.android.content.HwContextEx;
import huawei.android.net.IConnectivityExManager;

public class HwConnectivityExManager {
    private static final String TAG = "HwConnectivityExManager";
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
        this.mService = IConnectivityExManager.Stub.asInterface(ServiceManager.getService(HwContextEx.HW_CONNECTIVITY_EX_SERVICE));
    }

    public void setSmartKeyguardLevel(String level) {
        try {
            this.mService.setSmartKeyguardLevel(level);
        } catch (RemoteException e) {
        }
    }

    public void setUseCtrlSocket(boolean flag) {
    }

    public void setApIpv4AddressFixed(boolean isFixed) {
        Log.d(TAG, "setApIpv4AddressFixed:" + isFixed);
        if (this.mService == null) {
            Log.d(TAG, "mService is null");
            getDefault();
        }
        if (this.mService != null) {
            try {
                this.mService.setApIpv4AddressFixed(isFixed);
            } catch (RemoteException e) {
                Log.d(TAG, "RemoteException" + e.getMessage());
            }
        }
    }

    public boolean isApIpv4AddressFixed() {
        Log.d(TAG, "isApIpv4AddressFixed");
        if (this.mService == null) {
            Log.d(TAG, "mService is null");
            getDefault();
        }
        if (this.mService == null) {
            return false;
        }
        try {
            return this.mService.isApIpv4AddressFixed();
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException" + e.getMessage());
            return false;
        }
    }
}
