package com.huawei.vmock.setwifi;

import android.net.wifi.IWifiManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import java.io.PrintStream;

public class setwifi {
    public static void main(String[] args) {
        IWifiManager wifiMgr = IWifiManager.Stub.asInterface(ServiceManager.getService("wifi"));
        if (wifiMgr == null) {
            System.err.println("wifiMgr = null, Wi-Fi service is not ready");
            return;
        }
        try {
            int status = wifiMgr.getWifiApEnabledState();
            PrintStream printStream = System.err;
            printStream.println("wifi enable status = " + status);
        } catch (RemoteException e) {
            PrintStream printStream2 = System.err;
            printStream2.println("Wi-Fi operation failed: " + e);
        }
    }
}
