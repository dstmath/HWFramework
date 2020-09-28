package com.huawei.systemmanager.power;

import android.os.IDeviceIdleController;
import android.os.RemoteException;
import com.huawei.android.os.ServiceManagerEx;

public class HwDeviceIdleController {
    private static final String DEVICE_IDLE_SERVICE = "deviceidle";

    public static void addPowerSaveWhitelistApp(String var1) throws RemoteException {
        IDeviceIdleController.Stub.asInterface(ServiceManagerEx.getService(DEVICE_IDLE_SERVICE)).addPowerSaveWhitelistApp(var1);
    }

    public static void removePowerSaveWhitelistApp(String var1) throws RemoteException {
        IDeviceIdleController.Stub.asInterface(ServiceManagerEx.getService(DEVICE_IDLE_SERVICE)).removePowerSaveWhitelistApp(var1);
    }
}
