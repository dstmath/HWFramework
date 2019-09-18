package com.huawei.nearbysdk.DTCP;

import android.os.RemoteException;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.NearbyDevice;

public class DTCPSender {
    private static final String TAG = "DTCPSender";
    private IDTCPSender mSender = null;
    private SendTransmitCallbackTranspot mTransmitCB = null;

    DTCPSender(IDTCPSender sender, SendTransmitCallbackTranspot transmitCB) {
        this.mSender = sender;
        this.mTransmitCB = transmitCB;
    }

    /* access modifiers changed from: package-private */
    public SendTransmitCallbackTranspot getTransmitTranspot() {
        return this.mTransmitCB;
    }

    public int cancelSend() {
        try {
            return this.mSender.cancelSend();
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call dtcp sender cancel RemoteException, maybe dtcp service died");
            return -10;
        }
    }

    public NearbyDevice getReceiverDevice() {
        try {
            return this.mSender.getReceiverDevice();
        } catch (RemoteException e) {
            HwLog.e(TAG, "Call dtcp sender getReceiverDevice RemoteException, maybe dtcp service died");
            return null;
        }
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof DTCPSender) {
            return ((DTCPSender) object).mSender.asBinder().equals(this.mSender.asBinder());
        }
        if (object instanceof IDTCPSender) {
            return ((IDTCPSender) object).asBinder().equals(this.mSender.asBinder());
        }
        return false;
    }
}
