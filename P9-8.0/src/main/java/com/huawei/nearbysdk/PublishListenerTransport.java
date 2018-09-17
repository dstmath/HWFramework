package com.huawei.nearbysdk;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.nearbysdk.IPublishListener.Stub;

public class PublishListenerTransport extends Stub {
    private static final int STAUS_FAIL = 1;
    private static final int STAUS_SUCCESS = 0;
    private static final int STAUS_TIMEOUT = -1;
    static final String TAG = "ListenerTransport";
    private static final int TYPE_DEVICE_FOUND = 2;
    private static final int TYPE_DEVICE_LOST = 3;
    private static final int TYPE_LOCAL_DEVICE_CHANGE = 4;
    private static final int TYPE_STATUS_CHANGED = 1;
    private PublishListener mListener;
    private final Handler mListenerHandler;

    PublishListenerTransport(PublishListener listener, Looper looper) {
        this.mListener = listener;
        this.mListenerHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                PublishListenerTransport.this._handleMessage(msg);
            }
        };
    }

    public void onStatusChanged(int status) {
        Message msg = Message.obtain();
        msg.what = 1;
        msg.arg1 = status;
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onStatusChanged: handler quitting,remove the listener. ");
        }
    }

    public void onDeviceFound(NearbyDevice device) {
        HwLog.d(TAG, "onDeviceFound");
        Message msg = Message.obtain();
        msg.what = 2;
        msg.obj = device;
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onDeviceFound: handler quitting,remove the listener. ");
        }
    }

    public void onDeviceLost(NearbyDevice device) {
        HwLog.d(TAG, "onDeviceLost");
        Message msg = Message.obtain();
        msg.what = 3;
        msg.obj = device;
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onDeviceLost: handler quitting,remove the listener. ");
        }
    }

    public void onLocalDeviceChange(int status) {
        HwLog.d(TAG, "onLocalDeviceChange status = " + status);
        Message msg = Message.obtain();
        msg.what = 4;
        msg.arg1 = status;
        if (!this.mListenerHandler.sendMessage(msg)) {
            HwLog.e(TAG, "onLocalDeviceChange: handler quitting,remove the listener. ");
        }
    }

    private void _handleMessage(Message msg) {
        HwLog.d(TAG, "_handleMessage: " + msg.toString());
        switch (msg.what) {
            case 1:
                HwLog.d(TAG, "TYPE_STATUS_CHANGED Listener.onStatusChanged");
                this.mListener.onStatusChanged(msg.arg1);
                return;
            case 2:
                HwLog.d(TAG, "TYPE_STATUS_CHANGED Listener.onDeviceFound");
                this.mListener.onDeviceFound((NearbyDevice) msg.obj);
                return;
            case 3:
                HwLog.d(TAG, "TYPE_STATUS_CHANGED Listener.onDeviceLost");
                this.mListener.onDeviceLost((NearbyDevice) msg.obj);
                return;
            case 4:
                HwLog.d(TAG, "TYPE_LOCAL_DEVICE_CHANGE Listener.onLocalDeviceChange");
                this.mListener.onLocalDeviceChange(msg.arg1);
                return;
            default:
                HwLog.e(TAG, "Unknow message id:" + msg.what + ", can not be here!");
                return;
        }
    }
}
