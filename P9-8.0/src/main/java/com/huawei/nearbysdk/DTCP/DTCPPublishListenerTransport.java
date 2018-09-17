package com.huawei.nearbysdk.DTCP;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import com.huawei.nearbysdk.HwLog;
import com.huawei.nearbysdk.IPublishListener.Stub;
import com.huawei.nearbysdk.NearbyDevice;
import com.huawei.nearbysdk.PublishListener;

class DTCPPublishListenerTransport extends Stub implements Callback {
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

    public void onStatusChanged(int state) throws RemoteException {
        Message.obtain(this.mHandler, 1, state, 0).sendToTarget();
    }

    public void onDeviceFound(NearbyDevice device) throws RemoteException {
        Message.obtain(this.mHandler, 2, device).sendToTarget();
    }

    public void onDeviceLost(NearbyDevice device) throws RemoteException {
        Message.obtain(this.mHandler, 3, device).sendToTarget();
    }

    public void onLocalDeviceChange(int status) throws RemoteException {
        Message.obtain(this.mHandler, 4, status, 0).sendToTarget();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                this.mPublishListener.onStatusChanged(msg.arg1);
                break;
            case 2:
                this.mPublishListener.onDeviceFound((NearbyDevice) msg.obj);
                break;
            case 3:
                this.mPublishListener.onDeviceLost((NearbyDevice) msg.obj);
                break;
            case 4:
                this.mPublishListener.onLocalDeviceChange(msg.arg1);
                break;
            default:
                HwLog.e(TAG, "Can not be here!Unknow msg=" + msg.what);
                break;
        }
        return true;
    }
}
