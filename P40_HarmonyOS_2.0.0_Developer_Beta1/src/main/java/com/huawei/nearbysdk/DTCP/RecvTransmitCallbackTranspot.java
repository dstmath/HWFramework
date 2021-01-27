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
public class RecvTransmitCallbackTranspot extends ITransmitCallback.Stub implements Handler.Callback {
    private static final int MSG_ERROR = 4;
    private static final int MSG_IMPORTPROGRESS = 7;
    private static final int MSG_IMPORTSTARTED = 8;
    private static final int MSG_PROGRESS = 1;
    private static final int MSG_SIZE = 5;
    private static final int MSG_SPEED = 6;
    private static final int MSG_STATUS = 3;
    private static final int MSG_SUCCESS = 2;
    private static final String TAG = "RecvTransmitCallbackTranspot";
    private Handler mHandler = null;
    private RecvTransmitCallback mRecvTransmitCB = null;

    RecvTransmitCallbackTranspot(RecvTransmitCallback recvCB, Looper looper) {
        this.mRecvTransmitCB = recvCB;
        this.mHandler = new Handler(looper, this);
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onProgress(int percent) throws RemoteException {
        Message.obtain(this.mHandler, 1, percent, 0).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onSuccess(String[] filePathList) throws RemoteException {
        Message.obtain(this.mHandler, 2, filePathList).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onRecvSuccess(NearbyRecvBean recvBean) throws RemoteException {
        if (recvBean == null) {
            HwLog.e(TAG, "OnRecvSuccess recvBean null.");
        } else {
            Message.obtain(this.mHandler, 2, recvBean.getFilePathList()).sendToTarget();
        }
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
        Message.obtain(this.mHandler, 5, Long.valueOf(totalFileLength)).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onSpeed(int speed) throws RemoteException {
        Message.obtain(this.mHandler, 6, speed, 0).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onHwIDHeadImageReceive(NearbyDevice recvDevice, byte[] headImage) throws RemoteException {
        HwLog.w(TAG, "onHwIDHeadImageReceive not use in receiver, can not be here!");
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onImportProgress(int percent) throws RemoteException {
        Message.obtain(this.mHandler, 7, percent, 0).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.DTCP.ITransmitCallback
    public void onImportStarted() throws RemoteException {
        this.mHandler.sendEmptyMessage(8);
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mRecvTransmitCB.onProgress(msg.arg1);
                return true;
            case 2:
                this.mRecvTransmitCB.onSuccess((String[]) msg.obj);
                return true;
            case 3:
                this.mRecvTransmitCB.onStatus(msg.arg1);
                return true;
            case 4:
                this.mRecvTransmitCB.onError(msg.arg1);
                return true;
            case 5:
                this.mRecvTransmitCB.onTotalFileLength(((Long) msg.obj).longValue());
                return true;
            case 6:
                this.mRecvTransmitCB.onSpeed(msg.arg1);
                return true;
            case 7:
                this.mRecvTransmitCB.onImportProgress(msg.arg1);
                return true;
            case 8:
                this.mRecvTransmitCB.onImportStarted();
                return true;
            default:
                HwLog.e(TAG, "Can not be here!Unknow msg=" + msg.what);
                return true;
        }
    }
}
