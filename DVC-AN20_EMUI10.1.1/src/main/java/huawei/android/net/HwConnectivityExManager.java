package huawei.android.net;

import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.os.ServiceManagerEx;
import huawei.android.net.IConnectivityExManager;

public class HwConnectivityExManager {
    private static final int INVALID_NETID = -1;
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
        this.mService = IConnectivityExManager.Stub.asInterface(ServiceManagerEx.getService("hwConnectivityExService"));
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
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager != null) {
            try {
                iConnectivityExManager.setApIpv4AddressFixed(isFixed);
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
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return false;
        }
        try {
            return iConnectivityExManager.isApIpv4AddressFixed();
        } catch (RemoteException e) {
            Log.d(TAG, "RemoteException" + e.getMessage());
            return false;
        }
    }

    public boolean bindUidProcessToNetwork(int netId, int uid) {
        if (this.mService == null) {
            Log.e(TAG, "mService is null");
            getDefault();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return false;
        }
        try {
            return iConnectivityExManager.bindUidProcessToNetwork(netId, uid);
        } catch (RemoteException e) {
            Log.e(TAG, "bindUidProcessToNetwork RemoteException occurs.");
            return false;
        }
    }

    public boolean unbindAllUidProcessToNetwork(int netId) {
        if (this.mService == null) {
            Log.e(TAG, "mService is null");
            getDefault();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return false;
        }
        try {
            return iConnectivityExManager.unbindAllUidProcessToNetwork(netId);
        } catch (RemoteException e) {
            Log.e(TAG, "unbindAllUidProcessToNetwork RemoteException occurs.");
            return false;
        }
    }

    public boolean isUidProcessBindedToNetwork(int netId, int uid) {
        if (this.mService == null) {
            Log.e(TAG, "mService is null");
            getDefault();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return false;
        }
        try {
            return iConnectivityExManager.isUidProcessBindedToNetwork(netId, uid);
        } catch (RemoteException e) {
            Log.e(TAG, "isUidProcessBindedToNetwork RemoteException occurs.");
            return false;
        }
    }

    public boolean isAllUidProcessUnbindToNetwork(int netId) {
        if (this.mService == null) {
            Log.e(TAG, "mService is null");
            getDefault();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return false;
        }
        try {
            return iConnectivityExManager.isAllUidProcessUnbindToNetwork(netId);
        } catch (RemoteException e) {
            Log.e(TAG, "isAllUidProcessUnbindToNetwork RemoteException occurs.");
            return false;
        }
    }

    public int getNetIdBySlotId(int slotId) {
        if (this.mService == null) {
            Log.e(TAG, "mService is null");
            getDefault();
        }
        IConnectivityExManager iConnectivityExManager = this.mService;
        if (iConnectivityExManager == null) {
            return -1;
        }
        try {
            return iConnectivityExManager.getNetIdBySlotId(slotId);
        } catch (RemoteException e) {
            Log.e(TAG, "getNetIdBySlotId RemoteException occurs.");
            return -1;
        }
    }
}
