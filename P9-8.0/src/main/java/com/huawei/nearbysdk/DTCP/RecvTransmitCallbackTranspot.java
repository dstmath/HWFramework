package com.huawei.nearbysdk.DTCP;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.DTCP.ITransmitCallback.Stub;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.NearbyDevice;

class RecvTransmitCallbackTranspot extends Stub implements Callback {
    private static final int MSG_ERROR = 4;
    private static final int MSG_PROGRESS = 1;
    private static final int MSG_STATUS = 3;
    private static final int MSG_SUCCESS = 2;
    private static final String TAG = "RecvTransmitCallbackTranspot";
    private Handler mHandler = null;
    private RecvTransmitCallback mRecvTransmitCB = null;

    RecvTransmitCallbackTranspot(RecvTransmitCallback recvCB, Looper looper) {
        this.mRecvTransmitCB = recvCB;
        this.mHandler = new Handler(looper, this);
    }

    public void onProgress(int percent) throws RemoteException {
        Message.obtain(this.mHandler, 1, percent, 0).sendToTarget();
    }

    public void onSuccess(String[] filePathList) throws RemoteException {
        Message.obtain(this.mHandler, 2, filePathList).sendToTarget();
    }

    public void onStatus(int status) throws RemoteException {
        Message.obtain(this.mHandler, 3, status, 0).sendToTarget();
    }

    public void onError(int errorCode) throws RemoteException {
        Message.obtain(this.mHandler, 4, errorCode, 0).sendToTarget();
    }

    public void onHwIDHeadImageReceive(NearbyDevice recvDevice, byte[] headImage) throws RemoteException {
        HwLog.w(TAG, "onHwIDHeadImageReceive not use in receiver, can not be here!");
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mRecvTransmitCB.onProgress(msg.arg1);
                break;
            case 2:
                this.mRecvTransmitCB.onSuccess((String[]) msg.obj);
                break;
            case 3:
                this.mRecvTransmitCB.onStatus(msg.arg1);
                break;
            case 4:
                this.mRecvTransmitCB.onError(msg.arg1);
                break;
            default:
                HwLog.e(TAG, "Can not be here!Unknow msg=" + msg.what);
                break;
        }
        return true;
    }
}
