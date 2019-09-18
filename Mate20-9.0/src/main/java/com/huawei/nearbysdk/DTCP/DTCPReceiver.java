package com.huawei.nearbysdk.DTCP;

import android.os.Looper;
import android.os.RemoteException;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.NearbyDevice;

public class DTCPReceiver {
    private static final String TAG = "DTCPReceiver";
    private IDTCPReceiver mIDTCPReceiver = null;
    private RecvTransmitCallbackTranspot mTransmitCallback = null;

    DTCPReceiver(IDTCPReceiver idtcpReceiver) {
        this.mIDTCPReceiver = idtcpReceiver;
    }

    public int confirm(String saveDir, RecvTransmitCallback transCallback) {
        return confirm(saveDir, transCallback, Looper.myLooper());
    }

    public synchronized int confirm(String saveDir, RecvTransmitCallback transCallback, Looper looper) {
        if (this.mTransmitCallback != null) {
            return -3;
        }
        this.mTransmitCallback = new RecvTransmitCallbackTranspot(transCallback, looper);
        try {
            return this.mIDTCPReceiver.confirm(saveDir, this.mTransmitCallback);
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call dtcp receiver confirm RemoteException, maybe dtcp service died");
            this.mTransmitCallback = null;
            return -10;
        }
    }

    public int cancelReceive() {
        try {
            return this.mIDTCPReceiver.cancelReceive();
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call dtcp receiver cancel RemoteException, maybe dtcp service died");
            return -10;
        }
    }

    public int reject() {
        try {
            return this.mIDTCPReceiver.reject();
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call dtcp receiver reject RemoteException, maybe dtcp service died");
            return -10;
        }
    }

    public NearbyDevice getSenderDevice() {
        try {
            return this.mIDTCPReceiver.getSenderDevice();
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call dtcp receiver getSenderDevice RemoteException, maybe dtcp service died");
            return null;
        }
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof DTCPReceiver) {
            return ((DTCPReceiver) object).mIDTCPReceiver.asBinder().equals(this.mIDTCPReceiver.asBinder());
        }
        if (object instanceof IDTCPReceiver) {
            return ((IDTCPReceiver) object).asBinder().equals(this.mIDTCPReceiver.asBinder());
        }
        return false;
    }
}
