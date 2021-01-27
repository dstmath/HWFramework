package com.huawei.chr;

import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.radio_radar.V1_0.IRadioIndicationRadar;
import vendor.huawei.hardware.radio_radar.V1_0.IRadioRadar;
import vendor.huawei.hardware.radio_radar.V1_0.IRadioResponseRadar;

public class RadioRadarEx {
    private static final String TAG = "RadioRadarEx";
    private IRadioRadar radioRadar;

    private RadioRadarEx(IRadioRadar radioRadar2) {
        this.radioRadar = radioRadar2;
    }

    public static RadioRadarEx getService(String name) throws RemoteException, NoSuchElementException {
        return new RadioRadarEx(IRadioRadar.getService(name));
    }

    public void setResponseFunctions(RadioResponseRadarEx radioResponseRadarEx, RadioIndicationRadarEx radioIndicationRadarEx) throws RemoteException {
        if (this.radioRadar == null) {
            Log.e(TAG, "setResponseFunctions: radioRadar is null.");
            return;
        }
        IRadioIndicationRadar radioIndicationRadar = null;
        IRadioResponseRadar radioResponseRadar = radioResponseRadarEx == null ? null : radioResponseRadarEx.getRadioResponseRadar();
        if (radioIndicationRadarEx != null) {
            radioIndicationRadar = radioIndicationRadarEx.getRadioIndicationRadar();
        }
        this.radioRadar.setResponseFunctions(radioResponseRadar, radioIndicationRadar);
    }

    public void responseAcknowledgement() throws RemoteException {
        IRadioRadar iRadioRadar = this.radioRadar;
        if (iRadioRadar == null) {
            Log.e(TAG, "responseAcknowledgement: radioRadar is null.");
        } else {
            iRadioRadar.responseAcknowledgement();
        }
    }

    public void sendRequest(int serial, ArrayList<Byte> datas) throws RemoteException {
        IRadioRadar iRadioRadar = this.radioRadar;
        if (iRadioRadar == null) {
            Log.e(TAG, "sendRequest: radioRadar is null.");
        } else {
            iRadioRadar.sendRequest(serial, datas);
        }
    }

    public boolean linkToDeath(DeathRecipientEx deathRecipient, long cookie) throws RemoteException {
        IRadioRadar iRadioRadar = this.radioRadar;
        if (iRadioRadar != null) {
            return iRadioRadar.linkToDeath(deathRecipient.getDeathRecipient(), cookie);
        }
        Log.e(TAG, "linkToDeath: radioRadar is null.");
        return false;
    }
}
