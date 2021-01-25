package com.huawei.nearbysdk.DTCP;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.DTCP.ITransmitCallback;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.NearbyDevice;
import com.huawei.nearbysdk.NearbyRecvBean;

/* access modifiers changed from: package-private */
public class SendTransmitCallbackTranspot extends ITransmitCallback.Stub implements Handler.Callback {
    private static final int MSG_ERROR = 4;
    private static final int MSG_HWIDIMG = 5;
    private static final int MSG_IMPORTPROGRESS = 8;
    private static final int MSG_IMPORTSTARTED = 9;
    private static final int MSG_PROGRESS = 1;
    private static final int MSG_SIZE = 6;
    private static final int MSG_SPEED = 7;
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

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onProgress(int percent) throws RemoteException {
        Message.obtain(this.mHandler, 1, percent, 0).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onSuccess(String[] filePathList) throws RemoteException {
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onRecvSuccess(NearbyRecvBean recvBean) throws RemoteException {
        this.mHandler.sendEmptyMessage(2);
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onStatus(int status) throws RemoteException {
        Message.obtain(this.mHandler, 3, status, 0).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onError(int errorCode) throws RemoteException {
        Message.obtain(this.mHandler, 4, errorCode, 0).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onTotalFileLength(long totalFileLength) throws RemoteException {
        Message.obtain(this.mHandler, 6, Long.valueOf(totalFileLength)).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onSpeed(int speed) throws RemoteException {
        Message.obtain(this.mHandler, 7, speed, 0).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onHwIDHeadImageReceive(NearbyDevice recvDevice, byte[] headImage) throws RemoteException {
        this.mRecvDevice = recvDevice;
        Message.obtain(this.mHandler, 5, headImage).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onImportProgress(int percent) throws RemoteException {
        Message.obtain(this.mHandler, 8, percent, 0).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onImportStarted() throws RemoteException {
        this.mHandler.sendEmptyMessage(9);
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mTransmitCB.onProgress(msg.arg1);
                return true;
            case 2:
                this.mTransmitCB.onSuccess();
                return true;
            case 3:
                this.mTransmitCB.onStatus(msg.arg1);
                return true;
            case 4:
                this.mTransmitCB.onError(msg.arg1);
                return true;
            case 5:
                this.mTransmitCB.onHwIDHeadImageReceive(this.mRecvDevice, (byte[]) msg.obj);
                this.mRecvDevice = null;
                return true;
            case 6:
                this.mTransmitCB.onTotalFileLength(((Long) msg.obj).longValue());
                return true;
            case 7:
                this.mTransmitCB.onSpeed(msg.arg1);
                return true;
            case 8:
                this.mTransmitCB.onImportProgress(msg.arg1);
                return true;
            case 9:
                this.mTransmitCB.onImportStarted();
                return true;
            default:
                HwLog.e(TAG, "Can not be here!Unknow msg=" + msg.what);
                return true;
        }
    }
}
