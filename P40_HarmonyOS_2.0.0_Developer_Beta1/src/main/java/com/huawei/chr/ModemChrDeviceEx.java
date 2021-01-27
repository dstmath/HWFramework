package com.huawei.chr;

import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.modemchr.V1_0.IModemchrDevice;

public class ModemChrDeviceEx {
    private static final String TAG = "ModemChrDeviceEx";
    private IModemchrDevice modemChrDevice;

    private ModemChrDeviceEx(IModemchrDevice modemChrDevice2) {
        this.modemChrDevice = modemChrDevice2;
    }

    public static ModemChrDeviceEx getService() throws RemoteException, NoSuchElementException {
        return new ModemChrDeviceEx(IModemchrDevice.getService());
    }

    public int regChrReceiver(int contextId, ModemChrResponseEx modemChrResponseEx) throws RemoteException {
        if (this.modemChrDevice == null) {
            Log.e(TAG, "regChrReceiver: modemChrDevice is null.");
            return -1;
        }
        return this.modemChrDevice.regChrReceiver(contextId, modemChrResponseEx == null ? null : modemChrResponseEx.getModemChrResponse());
    }

    public int unregChrReceiver(int contextId) throws RemoteException {
        IModemchrDevice iModemchrDevice = this.modemChrDevice;
        if (iModemchrDevice != null) {
            return iModemchrDevice.unregChrReceiver(contextId);
        }
        Log.e(TAG, "unregChrReceiver: modemChrDevice is null.");
        return -1;
    }

    public boolean linkToDeath(DeathRecipientEx deathRecipient, long cookie) throws RemoteException {
        IModemchrDevice iModemchrDevice = this.modemChrDevice;
        if (iModemchrDevice != null) {
            return iModemchrDevice.linkToDeath(deathRecipient.getDeathRecipient(), cookie);
        }
        Log.e(TAG, "linkToDeath: modemChrDevice is null.");
        return false;
    }

    public int sendChrRequest(int contextId, ArrayList<Byte> reqMsgData, int reqMsgLen) throws RemoteException {
        IModemchrDevice iModemchrDevice = this.modemChrDevice;
        if (iModemchrDevice != null) {
            return iModemchrDevice.sendChrRequest(contextId, reqMsgData, reqMsgLen);
        }
        Log.e(TAG, "sendChrRequest: modemChrDevice is null.");
        return -1;
    }
}
