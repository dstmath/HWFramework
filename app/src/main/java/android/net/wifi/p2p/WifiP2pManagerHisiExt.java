package android.net.wifi.p2p;

import android.content.Context;
import android.net.wifi.p2p.IWifiP2pManager.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class WifiP2pManagerHisiExt {
    public static final String SWITCH_TO_P2P_MODE = "android.net.wifi.p2p.hisi.SWITCH_TO_P2P_MODE";
    private static final String TAG = "WifiP2pManagerHisiExt";
    private IWifiP2pManager wifiP2pService;

    public WifiP2pManagerHisiExt() {
        this.wifiP2pService = null;
        this.wifiP2pService = Stub.asInterface(ServiceManager.getService(Context.WIFI_P2P_SERVICE));
    }

    public boolean setWifiP2pEnabled(int p2pFlag) {
        Log.d(TAG, "setWifiP2pEnabled() is called! p2pFlag =" + p2pFlag);
        try {
            if (this.wifiP2pService != null) {
                return this.wifiP2pService.setWifiP2pEnabled(p2pFlag);
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean isWifiP2pEnabled() {
        Log.d(TAG, "isWifiP2pEnabled() is called!");
        try {
            if (this.wifiP2pService != null) {
                return this.wifiP2pService.isWifiP2pEnabled();
            }
            return false;
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setRecoveryWifiFlag(boolean flag) {
        Log.d(TAG, "setRecoveryWifiFlag() is called! flag = " + flag);
        try {
            if (this.wifiP2pService != null) {
                this.wifiP2pService.setRecoveryWifiFlag(flag);
            }
        } catch (RemoteException e) {
        }
    }
}
