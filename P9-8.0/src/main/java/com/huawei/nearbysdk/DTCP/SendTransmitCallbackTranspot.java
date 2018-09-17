package com.huawei.nearbysdk.DTCP;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.DTCP.ITransmitCallback.Stub;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.NearbyDevice;

class SendTransmitCallbackTranspot extends Stub implements Callback {
    private static final int MSG_ERROR = 4;
    private static final int MSG_HWIDIMG = 5;
    private static final int MSG_PROGRESS = 1;
    private static final int MSG_STATUS = 3;
    private static final int MSG_SUCCESS = 2;
    private static final String TAG = "SendTransmitCallbackTranspot";
    private Handler mHandler = null;
    private NearbyDevice mRecvDevice = null;
    private SendTransmitCallback mTransmitCB = null;

    SendTransmitCallbackTranspot(SendTransmitCallback transmitCB, Looper looper) {
        this.mTransmitCB = transmitCB;
        this.mHandler = new Handler(looper, this);
    }

    public void onProgress(int percent) throws RemoteException {
        Message.obtain(this.mHandler, 1, percent, 0).sendToTarget();
    }

    public void onSuccess(String[] filePathList) throws RemoteException {
        this.mHandler.sendEmptyMessage(2);
    }

    public void onStatus(int status) throws RemoteException {
        Message.obtain(this.mHandler, 3, status, 0).sendToTarget();
    }

    public void onError(int errorCode) throws RemoteException {
        Message.obtain(this.mHandler, 4, errorCode, 0).sendToTarget();
    }

    public void onHwIDHeadImageReceive(NearbyDevice recvDevice, byte[] headImage) throws RemoteException {
        this.mRecvDevice = recvDevice;
        Message.obtain(this.mHandler, 5, headImage).sendToTarget();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mTransmitCB.onProgress(msg.arg1);
                break;
            case 2:
                this.mTransmitCB.onSuccess();
                break;
            case 3:
                this.mTransmitCB.onStatus(msg.arg1);
                break;
            case 4:
                this.mTransmitCB.onError(msg.arg1);
                break;
            case 5:
                this.mTransmitCB.onHwIDHeadImageReceive(this.mRecvDevice, (byte[]) msg.obj);
                this.mRecvDevice = null;
                break;
            default:
                HwLog.e(TAG, "Can not be here!Unknow msg=" + msg.what);
                break;
        }
        return true;
    }
}
