package com.huawei.nearbysdk.DTCP;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.IPublishListener;
import com.huawei.nearbysdk.NearbyDevice;
import com.huawei.nearbysdk.PublishListener;

class DTCPPublishListenerTransport extends IPublishListener.Stub implements Handler.Callback {
    private static final int MSG_DEVICE_FOUND = 2;
    private static final int MSG_DEVICE_LOST = 3;
    private static final int MSG_LOCAL_DEVICE_CHANGE = 4;
    private static final int MSG_STATUS_CHANGED = 1;
    private static final String TAG = "DTCPPublishListenerTransport";
    private Handler mHandler = null;
    private PublishListener mPublishListener = null;

    DTCPPublishListenerTransport(PublishListener publishListener, Looper looper) {
        this.mPublishListener = publishListener;
        this.mHandler = new Handler(looper, this);
    }

    @Override // com.huawei.nearbysdk.IPublishListener
    public void onStatusChanged(int state) throws RemoteException {
        Message.obtain(this.mHandler, 1, state, 0).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.IPublishListener
    public void onDeviceFound(NearbyDevice device) throws RemoteException {
        Message.obtain(this.mHandler, 2, device).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.IPublishListener
    public void onDeviceLost(NearbyDevice device) throws RemoteException {
        Message.obtain(this.mHandler, 3, device).sendToTarget();
    }

    @Override // com.huawei.nearbysdk.IPublishListener
    public void onLocalDeviceChange(int status) throws RemoteException {
        Message.obtain(this.mHandler, 4, status, 0).sendToTarget();
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mPublishListener.onStatusChanged(msg.arg1);
                return true;
            case 2:
                this.mPublishListener.onDeviceFound((NearbyDevice) msg.obj);
                return true;
            case 3:
                this.mPublishListener.onDeviceLost((NearbyDevice) msg.obj);
                return true;
            case 4:
                this.mPublishListener.onLocalDeviceChange(msg.arg1);
                return true;
            default:
                HwLog.e(TAG, "Can not be here!Unknow msg=" + msg.what);
                return true;
        }
    }
}
