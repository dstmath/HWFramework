package com.huawei.chr;

import android.os.RemoteException;
import android.util.Log;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.radio.chr.V1_0.IRadioChr;
import vendor.huawei.hardware.radio.chr.V1_0.IRadioChrIndication;
import vendor.huawei.hardware.radio.chr.V1_0.IRadioChrResponse;

public class RadioChrEx {
    private static final String TAG = "RadioChrEx";
    private IRadioChr radioChr;

    private RadioChrEx(IRadioChr radioChr2) {
        this.radioChr = radioChr2;
    }

    public static RadioChrEx getService(String name) throws RemoteException, NoSuchElementException {
        return new RadioChrEx(IRadioChr.getService(name));
    }

    public void setResponseFunctionsHuawei(RadioChrResponseEx radioChrResponseEx, RadioChrIndicationEx radioChrIndicationEx) throws RemoteException {
        if (this.radioChr == null) {
            Log.e(TAG, "setResponseFunctionsHuawei: radiochr is null.");
            return;
        }
        IRadioChrIndication radioChrIndication = null;
        IRadioChrResponse radioChrResponse = radioChrResponseEx == null ? null : radioChrResponseEx.getRadioChrResponse();
        if (radioChrIndicationEx != null) {
            radioChrIndication = radioChrIndicationEx.getRadioChrIndication();
        }
        this.radioChr.setResponseFunctionsHuawei(radioChrResponse, radioChrIndication);
    }

    public void responseAcknowledgement() throws RemoteException {
        IRadioChr iRadioChr = this.radioChr;
        if (iRadioChr == null) {
            Log.e(TAG, "responseAcknowledgement: radiochr is null.");
        } else {
            iRadioChr.responseAcknowledgement();
        }
    }

    public boolean linkToDeath(DeathRecipientEx deathRecipient, long cookie) throws RemoteException {
        IRadioChr iRadioChr = this.radioChr;
        if (iRadioChr != null) {
            return iRadioChr.linkToDeath(deathRecipient.getDeathRecipient(), cookie);
        }
        Log.e(TAG, "linkToDeath: radiochr is null.");
        return false;
    }
}
